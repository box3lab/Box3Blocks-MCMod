---
---

# Box3JS Architecture

## Overall Architecture

```text
                          ┌──────────────────────────┐
                          │     Minecraft Server      │
                          │        (NeoForge)         │
                          └──────────┬───────────────┘
                                     │
                          ┌──────────▼───────────────┐
                          │     Box3JS.java           │
                          │  @Mod Entry Point         │
                          │  Subscribes to events     │
                          │  Forwards to JS engine    │
                          └──────────┬───────────────┘
                                     │
              ┌──────────────────────┼──────────────────────┐
              │                      │                      │
    ┌─────────▼─────────┐  ┌────────▼────────┐  ┌─────────▼─────────┐
    │ Box3ScriptEngine  │  │ Box3JSClient    │  │ Box3Script        │
    │ (Server engine)   │  │ Engine (client) │  │ Compiler (JAR)    │
    │ - Load scripts    │  │ - Client scripts│  │ - Registry gen    │
    │ - Manage scopes   │  │ - UI/input/audio│  │ - JAR packaging   │
    │ - Event dispatch  │  │ - Network recv  │  │                    │
    └─────────┬─────────┘  └────────┬────────┘  └──────────────────┘
              │                      │
    ┌─────────▼──────────────────────▼─────────┐
    │          Mozilla Rhino 1.9.1              │
    │          (JS engine, runs in JVM)         │
    └─────────┬────────────────────────────────┘
              │
    ┌─────────▼─────────┐
    │    Java API Layer  │
    │  world/entity/     │
    │  player/voxels/    │
    │  storage/db/http   │
    └───────────────────┘
```

### Key Package Structure

```text
com.box3lab.box3js
├── Box3JS.java                  ← @Mod entry point
├── script/                      ← Server engine
│   ├── Box3ScriptEngine.java    ← Rhino engine manager
│   ├── Box3ScriptCommand.java   ← /box3script commands
│   ├── Box3ScriptConfig.java    ← Config file
│   ├── Box3ScriptSandbox.java   ← Sandbox rollback
│   ├── Box3ScriptWatcher.java   ← File watcher
│   ├── Box3ScriptUtils.java     ← Shared utilities
│   ├── Box3JSEventBus.java      ← Event callback storage
│   ├── Box3JSWorld.java         ← world.* API
│   ├── Box3JSEntity.java        ← entity.* API
│   ├── Box3JSPlayer.java        ← player.* API
│   ├── Box3JSVoxels.java        ← voxels.* API
│   └── ...
├── client/                      ← Client engine
│   ├── Box3JSClientEngine.java  ← Client Rhino instance
│   └── ...
└── standalone/                  ← JAR compiler
    └── ...
```

## Rhino Engine

### Why Rhino

| Engine        | Type        | Speed  | JVM Integration        | ES Version |
| ------------- | ----------- | ------ | ---------------------- | ---------- |
| Mozilla Rhino | Interpreted | Medium | Native (Java impl)     | ES5        |
| GraalJS       | JIT         | Fast   | Requires config        | ES2023     |
| Nashorn       | JIT         | Fast   | JDK built-in (removed) | ES6        |

Reasons for choosing Rhino:

- **Pure Java implementation**, zero-config JVM embedding, no startup overhead
- **Mature and stable**, widely validated in the Minecraft modding community
- **Compatible with NeoForge classloader**, no special configuration needed
- ES5 limitation is bypassed via Babel compilation (source code uses modern TS)

### Core Flow

```java
// Box3ScriptEngine.java — simplified init
Context cx = Context.enter();
Scriptable scope = cx.initStandardObjects();

// 1. Inject global Java objects
scope.put("world", scope, worldApi);
scope.put("console", scope, consoleApi);
scope.put("storage", scope, storageApi);
// ...

// 2. Initialize console JS bridge
cx.evaluateString(scope, Box3ScriptUtils.CONSOLE_INIT_JS, "console-init", 1, null);

// 3. Load server entry script
cx.evaluateReader(scope, scriptReader, "server.js", 1, null);
```

### Type Bridging

When Java objects are exposed to JS, Rhino automatically handles type conversion:

| Java Type                 | JS Type   |
| ------------------------- | --------- |
| `String`                  | `string`  |
| `int` / `double`          | `number`  |
| `boolean`                 | `boolean` |
| `Map<String, Object>`     | `object`  |
| `List<Object>`            | `array`   |
| Java object (method call) | JS object |

Most Box3JS return values are **native Java objects** (e.g., `ServerPlayer` wrappers). Complex returns (e.g., `querySelectorAll`) return Java `List`, which Rhino maps to JS arrays.

## Scopes & Isolation

### Per-Project Independent Scopes

```text
                        Rhino Context
                             │
              ┌──────────────┼──────────────┐
              │              │              │
        ┌─────▼─────┐  ┌────▼──────┐  ┌────▼──────┐
        │ Scope A   │  │ Scope B   │  │ Scope C   │
        │ "mygame"  │  │ "lobby"   │  │ "survival"│
        │           │  │           │  │           │
        │ var x = 1 │  │ var x = 2 │  │ var x = 3 │
        └───────────┘  └───────────┘  └───────────┘
```

Each project has:

- **Independent top-level scope** — variables don't cross-contaminate
- **Independent event callback lists** — stored by `Box3JSEventBus` keyed by project name
- **Independent storage namespace** — `storage.getDataStorage("coins")` reads per-project data
- **Independent sandbox tracking** — block/entity modifications tracked separately

### Cleanup Mechanism

When stopping a project:

1. `Box3JSEventBus` clears all event callbacks for that project
2. Scoreboards/BossBars/teams created by the project are removed
3. If sandbox was enabled, all block and entity changes are rolled back
4. Rhino scope is released, GC collects

## Global Object Injection

### Server-Side Injection

```text
Box3ScriptEngine.setupScope(scope)
│
├── scope.put("world",       scope, new Box3JSWorld(...))
├── scope.put("voxels",      scope, new Box3JSVoxels(...))
├── scope.put("storage",     scope, new Box3JSStorage(...))
├── scope.put("db",          scope, new Box3JSDatabase(...))
├── scope.put("http",        scope, new Box3JSHttp(...))
├── scope.put("remoteChannel", scope, new Box3JSRemoteChannel(...))
├── scope.put("console",     scope, new Box3JSConsole(...))
│
├── scope.put("GameVector3",     scope, GameVector3.class)
├── scope.put("GameBounds3",     scope, GameBounds3.class)
├── scope.put("GameRGBColor",    scope, GameRGBColor.class)
├── scope.put("GameRGBAColor",   scope, GameRGBAColor.class)
├── scope.put("GameQuaternion",  scope, GameQuaternion.class)
│
└── cx.evaluateString(scope, CONSOLE_INIT_JS, ...)  ← console JS bridge
```

### Client-Side Injection

```text
Box3JSClientEngine.init(scope)
│
├── scope.put("audio",    scope, audioObj)
├── scope.put("client",   scope, clientObj)    ← onTick lifecycle
├── scope.put("input",    scope, inputObj)     ← keyboard detection
├── scope.put("ui",       scope, uiObj)        ← screen UI
├── scope.put("chat",     scope, chatObj)      ← chat send/receive
├── scope.put("storage",  scope, clientStorage)
├── scope.put("db",       scope, clientDb)     ← with graceful fallback
├── scope.put("http",     scope, clientHttp)
├── scope.put("remoteChannel", scope, remoteChannel)
├── scope.put("console",  scope, Box3JSConsole)
│
└── cx.evaluateString(scope, CONSOLE_INIT_JS, ...)
```

### Why console Needs JS Initialization

The Java `Box3JSConsole` method signature is `log(Object... args)` (varargs). Rhino has issues with direct varargs calling from JS, so a JS wrapper is needed:

```js
// CONSOLE_INIT_JS — injected into every scope
console = {
  log: function () {
    return _jConsole.log.apply(_jConsole, arguments);
  },
  debug: function () {
    return _jConsole.debug.apply(_jConsole, arguments);
  },
  warn: function () {
    return _jConsole.warn.apply(_jConsole, arguments);
  },
  error: function () {
    return _jConsole.error.apply(_jConsole, arguments);
  },
  // ...
};
```

`.apply()` ensures multiple arguments are correctly forwarded to the Java varargs method.

## Event Callback Mechanism

### Complete Chain

```text
Minecraft event fires
        │
        ▼
Box3JS.java (NeoForge event bus)
  │  onPlayerJoin / onEntityDeath / onServerTick ...
  │
  ▼
Box3ScriptEngine.fireCallback(eventType, data)
  │  Iterates all enabled projects
  │  For each project → executor.submit(task)
  │
  ▼
Box3JSEventBus.getCallbacks(project, eventType)
  │  Returns all callbacks registered by that project
  │
  ▼
Rhino Context: calls each callback in sequence
  Function.call(cx, scope, scope, args)
```

### Callback Storage

```java
// Box3JSEventBus — core data structure
Map<String, Map<String, List<Consumer<Object[]>>>> projectCallbacks;
//   │          │          │
//   │          │          └── Callback function list
//   │          └── Event type ("playerJoin", "chat", "tick", ...)
//   └── Project name ("mygame", "lobby", ...)
```

Each event type independently maintains its callback list. Stopping a project batch-cleans its callbacks.

### Callback Registration Example

```js
// JS side
let token = world.onPlayerJoin((entity, tick) => {
  // handle player join
});

// Internal flow:
// 1. world.onPlayerJoin calls Java method
// 2. Box3JSWorld.java stores callback + Function in Box3JSEventBus
// 3. Box3JS.java's PlayerLoggedInEvent handler detects join
// 4. Calls Box3ScriptEngine.fireCallback("playerJoin", entity, tick)
// 5. Engine finds all playerJoin callbacks for that project, executes them
```

### GameEventHandlerToken

```js
let token = world.onTick(() => { ... });
token.cancel();   // unsubscribe
token.active();   // check if still active
```

Java side:

```java
public class GameEventHandlerToken {
    private boolean active = true;
    public void cancel()  { /* remove from Box3JSEventBus */ }
    public boolean active() { return this.active; }
}
```

## Build Pipeline

```text
src/server/app.ts (TypeScript + ES2020 syntax)
        │
        ▼
┌─────────────────────┐
│  Babel              │
│  @babel/preset-     │
│  typescript         │
│                     │
│  class → function   │
│  let/const → var    │
│  => → function(){}  │
│  `` → "" +          │
└────────┬────────────┘
         │
         ▼  ES5 JavaScript
┌─────────────────────┐
│  esbuild            │
│  bundle             │
│                     │
│  Merge multiple     │
│  .ts files into     │
│  one .js file       │
└────────┬────────────┘
         │
         ▼
    dist/server.js
```

### Why Two Steps

1. **Babel**: Rhino 1.9.1 only supports ES5. Babel transforms TS + modern syntax into ES5
2. **esbuild**: Merges multiple files into one bundle (Rhino's `require()` support is limited)

### build.mjs Core Logic

```js
// Simplified
import { build } from "esbuild";
import babel from "@babel/core";

// 1. Babel: TS → ES5 JS
const es5Code = babel.transformSync(tsCode, {
  presets: ["@babel/preset-typescript"],
  targets: { rhino: "1.9.1" },
});

// 2. esbuild: bundle
await build({
  entryPoints: ["src/server/app.ts"],
  bundle: true,
  outfile: "dist/server.js",
  target: "es5",
  format: "iife",
});
```

## Network Communication

### remoteChannel Architecture

```text
┌──────────────────────┐         ┌──────────────────────┐
│   Server (Java)      │         │   Client (Java)      │
│                      │         │                      │
│  Box3JSRemoteChannel │  ────→  │  Box3JSClientEngine  │
│  .sendClientEvent()  │ payload │  .onPayload()        │
│  .broadcastClientEvt │         │                      │
│  .onServerEvent()    │  ←────  │  remoteChannel       │
│                      │ payload │  .sendServerEvent()  │
└──────────────────────┘         └──────────────────────┘
         │                                  │
         │  NeoForge CustomPayload           │
         │  (network packets)               │
         └─────────────┬───────────────────┘
                       │
               ┌───────▼──────┐
               │  Network     │
               │  Protocol    │
               └──────────────┘
```

### Data Flow

**Server → Client:**

```text
JS: remoteChannel.sendClientEvent(player, { type: "boss_bar", hp: 50 })
  → Box3JSRemoteChannel.java
  → JSON.stringify(eventData)
  → Box3JSNetwork.sendToPlayer(player, jsonBytes)
  → NeoForge CustomPayload
  → Client receives
  → Box3JSClientEngine.onPayload(jsonBytes)
  → JSON.parse
  → JS: remoteChannel.onClientEvent handler receives { tick, args }
```

**Client → Server:**

```text
JS: remoteChannel.sendServerEvent({ key: "space" })
  → Box3JSClientEngine
  → JSON.stringify
  → NeoForge CustomPayload to Server
  → Box3JSNetwork.onPayload(jsonBytes)
  → Box3ScriptEngine.fireCallback("remoteChannel", ...)
  → JS: remoteChannel.onServerEvent handler receives { tick, entity, args }
```

### Data Format

All data crossing the network must be **JSON-serializable**:

- `string`, `number`, `boolean`, `null`
- Plain objects `{ key: value }`
- Arrays `[1, 2, 3]`
- NOT supported: functions, `GameVector3` instances, Java objects

## Sandbox System

### How It Works

```text
/box3script sandbox mygame  ← enable sandbox
        │
        ▼
Box3ScriptSandbox.start("mygame")
  │  Begin tracking all world modifications by this project
  │
  ├── voxels.setVoxel()   → record old block → new block
  ├── voxels.fillVoxel()  → record all old blocks in region
  ├── world.spawnEntity() → record spawned entity
  └── world.setBlock()    → same as setVoxel

/box3script sandbox mygame  ← disable sandbox
        │
        ▼
Box3ScriptSandbox.stop("mygame")
  │  Rollback all modifications in reverse order
  ├── Remove tracked entities
  ├── Restore blocks to original state
  └── Clear tracking data
```

### Tracking Data Structure

```java
// Simplified
class SandboxTracker {
    Map<BlockPos, VoxelState> originalVoxels;  // old block records
    List<Entity> spawnedEntities;               // spawned entities
    // Rollback: restore voxels → remove entities
}
```

### Use Cases

- **Safe testing of new scripts** — unsure what a script does? Test in sandbox first
- **Player testing** — let players try new features, rollback after without affecting the live server
- **Debugging** — test destructive operations (explode, fillVoxel)

## File Watching & Hot Reload

### Workflow

```text
/box3script watch  ← enable file watching
        │
        ▼
Box3ScriptWatcher starts
  │  Uses Java WatchService to monitor config/box3/script/
  │
  ▼
Detects .js file change (dist/server.js regenerated)
  │  Debounce: 300ms window merges multiple changes
  │
  ▼
Auto-executes: /box3script reload <project>
  │  Stop → reload script → re-register callbacks
  │
  ▼
New code takes effect (no manual reload needed)
```

### Technical Notes

- Monitors `dist/` compiled output (`.js`), not `src/` source
- 300ms debounce prevents multiple reloads during esbuild multi-chunk writes
- Reload is atomic: stops old script (cleans up callbacks + resources) before loading new one

## Compiled Release Mode

### `/box3script compile` Flow

```text
Input: config/box3/script/mygame/
        │
        ▼
┌─────────────────────────────────────┐
│  Box3ScriptCompiler                  │
│                                      │
│  1. Read package.json metadata       │
│  2. Read dist/server.js              │
│  3. Read dist/client.js              │
│  4. Read registries/*.json           │
│  5. Generate Java registration code  │
│  6. Compile Java sources              │
│  7. Package into JAR                  │
└─────────────┬───────────────────────┘
              │
              ▼
Output: mygame-1.0.0.jar
        │
        ├── META-INF/mods.toml         ← Mod metadata
        ├── META-INF/neoforge.mods.toml
        ├── com/example/mygame/
        │   ├── MygameMod.java         ← @Mod entry
        │   └── registries/             ← Auto-generated registry classes
        ├── assets/mygame/
        │   └── box3js/scripts/
        │       ├── server.js           ← Compiled server script
        │       └── client.js           ← Compiled client script
        └── (textures/models/sound assets)
```

### Registry Code Generation

`Box3JSRegistryGen.java` reads JSON configs and generates Java code:

```json
// registries/blocks.json
{
  "ruby_block": {
    "displayName": "Ruby Block",
    "sound": "metal",
    "mapColor": "color_red",
    "destroyTime": 5.0,
    "creativeTab": "my_tab"
  }
}
```

↓ compile-time generation ↓

```java
// Auto-generated Java code
public static final DeferredBlock<Block> RUBY_BLOCK =
    BLOCKS.register("ruby_block", () -> new Block(Block.Properties.of()
        .sound(SoundType.METAL)
        .mapColor(MapColor.COLOR_RED)
        .destroyTime(5.0f)));
```

**Note:** `registries` is only available in compiled JAR mode. In interpreted mode (`/box3script start`), `registries` is `undefined`.

## Performance Considerations

### Overhead Sources

| Layer                   | Overhead        | Notes                                |
| ----------------------- | --------------- | ------------------------------------ |
| NeoForge event dispatch | Low             | Same as vanilla Minecraft            |
| Box3JS event forwarding | Medium          | Java → JS argument boxing            |
| Rhino execution         | **Medium-High** | Interpreted, no JIT                  |
| JS code itself          | Depends on code | Loops in `onTick` are most sensitive |

### Performance Tips

1. **Avoid large loops in `onTick`** — scan entities on condition trigger, not every tick
2. **Cache query results** — don't put `querySelectorAll` in onTick
3. **Use `setInterval` over `onTick`** — if you don't need 20/sec, use longer intervals
4. **Minimize JS ↔ Java crossings** — batch operations are faster than individual calls

A typical parkour script consumes < 0.5ms/tick, with no impact on server TPS.
