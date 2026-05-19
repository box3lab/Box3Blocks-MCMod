---
---

# Box3JS API Reference

Write Minecraft server and client scripts in JavaScript/TypeScript.

## 5-Minute Quick Start

```bash
# 1. Create a project in-game
/box3script create mygame

# 2. Install dependencies and build
cd config/box3/script/mygame
npm install && npm run build

# 3. Start the script
/box3script start mygame
```

Open `src/server/app.ts` and write:

```js
world.onChat((entity, message) => {
  if (message === "!hello") {
    entity.player.directMessage("Hello, " + entity.player.name + "!");
    return false; // suppress chat message
  }
  return true;
});

console.log("Script loaded");
```

After each edit, re-run `npm run build`, then use `/box3script reload mygame` to hot-reload. Client logic goes in `src/client/app.ts`; after build it becomes `dist/client.js` and is sent automatically to players who have the Box3JS client mod installed.

::: tip Quick Navigation
[Quick Start Guide](../guide/getting-started.md) | [Architecture](../guide/architecture.md) | [JS vs Java Comparison](../guide/js-vs-java.md)
:::

## API Domain Map

Box3JS APIs are split into server-side, client-side, and shared runtimes. The type declarations are separated: `tsconfig.server.json` does not include client globals, and `tsconfig.client.json` does not include server globals such as `world` / `voxels`.

| Domain                          | Runtime      | Globals                                                                     | Description                                      |
| ------------------------------- | ------------ | --------------------------------------------------------------------------- | ------------------------------------------------ |
| **World & Entities** (server)   | Server       | `world` `voxels`                                                            | World control, blocks, event callbacks           |
| **Players & Data** (server)     | Server       | `entity` `player` `storage` `db` `http`                                     | `entity`/`player` come from callbacks or queries |
| **Client Interaction** (client) | Client       | `audio` `client` `input` `ui` `chat` `gui`                                  | Requires Box3JS client mod                       |
| **Cross-Side**                  | Both         | `remoteChannel`                                                             | Server↔Client event communication                |
| **Registries**                  | Compile-time | `registries`                                                                | Only in `/box3script compile` JAR mode           |
| **Math & Utilities**            | Both         | `GameVector3` `GameBounds3` `GameRGBColor` `GameRGBAColor` `GameQuaternion` | Constructed with `new`                           |
| **Global Tools**                | Both         | `console`                                                                   | Log output                                       |

::: info API Classification
**Server APIs** manipulate the world, entities, players, and blocks. Scripts run on the server by default. **Client APIs** are only available with the Box3JS client mod installed, for UI, input, and audio. **Registry APIs** are only available in compiled JAR mode (`registries` is `undefined` in interpreted mode).
:::

## Read By Runtime

| Entry                               | Use it for                                                                       | Includes                                                                        |
| ----------------------------------- | -------------------------------------------------------------------------------- | ------------------------------------------------------------------------------- |
| [Server API Overview](server.md) | Gameplay logic, events, blocks, entities, players, data, server-to-client events | `world`, `entity`, `player`, `voxels`, `storage`, `db`, `http`, `registries`    |
| [Client API Overview](client.md) | Local UI, input, audio, chat helpers, local data, client-to-server events        | `client`, `audio`, `input`, `ui`, `chat`, `gui`, `storage`, `db`, `http`        |
| [Shared Utilities](math.md)      | Math, color, and spatial code usable on both sides                               | `GameVector3`, `GameBounds3`, `GameRGBColor`, `GameRGBAColor`, `GameQuaternion` |

## API Style Rules

- Every `onXxx(...)` event registration API returns a `GameEventHandlerToken`; call `token.cancel()` to unsubscribe and `token.active()` to check whether it is still live.
- Server APIs are only typed in `src/server/app.ts`; client APIs are only typed in `src/client/app.ts`. Shared APIs are `storage`, `db`, `http`, `remoteChannel`, `console`, and the math classes.
- Cross-side data travels through `remoteChannel` as JSON-serializable objects: clients use `sendServerEvent` / `onClientEvent`; servers use `sendClientEvent` / `broadcastClientEvent` / `onServerEvent`.
- Coordinate APIs that accept `GameVector3` usually also support an `x, y, z` overload; server block coordinates are handled as integers.

## Find by Task — I want to...

Find APIs by what you want to do, not by which global object they live on.

### Messages & Chat

| I want to...         | Use this                                 |
| -------------------- | ---------------------------------------- |
| Broadcast to server  | `world.say("message")`                   |
| Send private message | `player.directMessage("message")`        |
| Show action bar text | `player.actionBar("message")`            |
| Show screen title    | `player.title("Title", "Subtitle")`      |
| Handle chat input    | `world.onChat((entity, msg) => { ... })` |

### Player Properties

| I want to...            | Use this                                        |
| ----------------------- | ----------------------------------------------- |
| Get/set player position | `player.position` → `GameVector3`               |
| Teleport player         | `player.teleport(new GameVector3(x, y, z))`     |
| Change health           | `player.hp = 20` / `player.maxHp = 40`          |
| Change hunger           | `player.food = 20` / `player.saturation = 10`   |
| Switch game mode        | `player.gameMode = "creative"`                  |
| Toggle flight           | `player.canFly = true` / `player.flying = true` |
| Kick player             | `player.kick("reason")`                         |
| Run command as player   | `player.runCommand("say hi")`                   |

### Items & Equipment

| I want to...         | Use this                                     |
| -------------------- | -------------------------------------------- |
| Give a basic item    | `player.giveItem("minecraft:diamond", 1)`    |
| Give enchanted item  | `player.giveEnchantedItem(...)`              |
| Give named item      | `player.giveNamedItem(...)`                  |
| Get held item        | `player.getHeldItem()`                       |
| Clear inventory      | `player.clearInventory()`                    |
| Set entity equipment | `entity.setEquipment("head", "iron_helmet")` |

### Custom Registries (Blocks, Items & Sounds) 🆕

| I want to...                 | Use this                                         |
| ---------------------------- | ------------------------------------------------ |
| Register custom blocks       | `registries/blocks.json` (at compile time)       |
| Register custom items        | `registries/items.json` (at compile time)        |
| Register custom sounds       | `registries/sounds.json` (at compile time)       |
| Register creative tabs       | `registries/creativeTabs.json` (at compile time) |
| Get a registered block       | `registries.getBlock("my_block")`                |
| Get a registered item        | `registries.getItem("chocolate")`                |
| Get a registered sound       | `registries.getSound("victory_fanfare")`         |
| Give a custom block/item     | `player.giveItem(block.itemId, 1)`               |
| Place a custom block         | `voxels.setVoxel(x, y, z, block.block)`          |
| Play a custom sound (server) | `world.playSound(sound.soundId, x, y, z, 1, 1)`  |
| Play a custom sound (client) | `audio.playSound("modId:soundId", 1.0, 1.0)`     |

::: warning
Server-side only. `registries` is `undefined` in client scripts. Only available in `/box3script compile` JAR mode. Client must also install the JAR for textures/models. See [registries.md](registries.md)
:::

### Block Operations

| I want to...               | Use this                                                    |
| -------------------------- | ----------------------------------------------------------- |
| Read a block               | `voxels.getVoxel(x, y, z)`                                  |
| Place/replace a block      | `voxels.setVoxel(x, y, z, "minecraft:stone")`               |
| Fill a region              | `voxels.fillVoxel(x1,y1,z1, x2,y2,z2, "stone")`             |
| Listen for block breaks    | `world.onVoxelDestroy((entity, x, y, z, voxel) => { ... })` |
| Listen for block placement | `world.onBlockPlace((entity, x, y, z, voxel) => { ... })`   |

### Entity Manipulation

| I want to...          | Use this                                      |
| --------------------- | --------------------------------------------- |
| Spawn an entity       | `world.spawnEntity("minecraft:zombie", pos)`  |
| Create with config    | `world.createEntity({ type, position, ... })` |
| Set entity name       | `entity.setNameTag("§cBoss")`                 |
| Toggle AI             | `entity.setAI(true)`                          |
| Navigate to position  | `entity.navigateTo(x, y, z, speed)`           |
| Set attack target     | `entity.setTarget(otherEntity)`               |
| Check if player       | `entity.isPlayer()`                           |
| Get entity type       | `entity.entityType`                           |
| Get entity tags       | `entity.tags()` / `entity.hasTag("boss")`     |
| Query nearby entities | `world.entitiesInRadius(pos, radius)`         |
| Query all entities    | `world.querySelectorAll("*")`                 |

### Client-side Features (requires Box3JS client mod)

| I want to...              | Use this                                                          |
| ------------------------- | ----------------------------------------------------------------- |
| Run every client tick     | `client.onTick(() => { ... })`                                    |
| Check key held down       | `input.isKeyDown("space")`                                        |
| Listen for key press      | `input.onKeyPress("f", () => { ... })`                            |
| Play sound effect         | `audio.playSound("pling", 1.0, 1.0)`                              |
| Play music                | `audio.playMusic("minecraft:music.game", 0.5, 1.0)`               |
| Stop all sounds           | `audio.stopAll()`                                                 |
| Get/set volume            | `audio.getVolume("music")` / `audio.setVolume("player", 0.8)`     |
| Show action bar text      | `ui.showOverlay("text")`                                          |
| Show screen title         | `ui.showTitle("Title", "Subtitle")`                               |
| Send chat message         | `chat.sendMessage("message")`                                     |
| Receive chat messages     | `chat.onMessage((msg, sender, isSystem) => { ... })`              |
| Send event to server      | `remoteChannel.sendServerEvent({ ... })`                          |
| Receive event from server | `remoteChannel.onClientEvent((event) => { ... })`                 |
| Client-side local storage | `storage.getDataStorage("key")`                                   |
| Set fog colour            | `client.setFogColor(255, 100, 50)`                                |
| Set fog distance          | `client.setFogStartDistance(10)` / `client.setFogEndDistance(50)` |
| Reset fog                 | `client.resetFog()`                                               |

### Visual Effects

| I want to...            | Use this                                                  |
| ----------------------- | --------------------------------------------------------- |
| Spawn particles         | `world.spawnParticle("flame", x, y, z, ...)`              |
| Particle circle         | `world.spawnParticleCircle(x, y, z, radius, "heart", 20)` |
| Firework                | `world.launchFirework(x, y, z, "red", "large_ball")`      |
| Lightning               | `world.strikeLightning(x, y, z)`                          |
| Explosion               | `world.explode(x, y, z, power)`                           |
| Play sound (global)     | `world.playSound("pling", pos, 1.0, 1.0)`                 |
| Play sound (per-player) | `player.playSound("pling", 1.0, 1.0)`                     |

### Potion Effects

| I want to...      | Use this                                                              |
| ----------------- | --------------------------------------------------------------------- |
| Apply an effect   | `entity.addEffect("minecraft:speed", duration, level, hideParticles)` |
| Clear all effects | `entity.clearEffects()`                                               |

### Event System

| I want to...          | Use this                                                                    |
| --------------------- | --------------------------------------------------------------------------- |
| Run every tick        | `world.onTick((info) => { ... })`                                           |
| On player join        | `world.onPlayerJoin((entity, tick) => { ... })`                             |
| On player leave       | `world.onPlayerLeave((entity, tick) => { ... })`                            |
| On entity death       | `world.onEntityDeath((entity, killer, tick) => { ... })`                    |
| On entity damaged     | `world.onEntityDamage((entity, amount, source, attacker, tick) => { ... })` |
| On right-click entity | `world.onInteract((entity, target, tick) => { ... })`                       |
| On right-click block  | `world.onBlockActivate((entity, x, y, z, voxel, tick) => { ... })`          |
| On button pressed     | `world.onButtonPressed((entity, button, tick) => { ... })`                  |
| On player respawn     | `world.onPlayerRespawn((entity, tick) => { ... })`                          |
| Run once after delay  | `setTimeout(() => { ... }, ticks)`                                          |
| Run on interval       | `setInterval(() => { ... }, ticks)`                                         |
| Cancel event listener | `token.cancel()`                                                            |
| Check if active       | `token.active()`                                                            |

### Data Persistence

| I want to...         | Use this                        |
| -------------------- | ------------------------------- |
| Read/write JSON data | `storage.getDataStorage("key")` |
| SQL query            | `db.sql("SELECT ...")`          |
| SQL write            | `db.sql("INSERT INTO ...")`     |

### HTTP Requests

| I want to... | Use this                                             |
| ------------ | ---------------------------------------------------- |
| GET request  | `http.fetch("https://...")`                          |
| POST JSON    | `http.fetch(url, { method: "POST", headers, body })` |
| Parse JSON   | `resp.json()` or `{ responseType: "json" }`          |
| Read text    | `resp.text()`                                        |
| Set timeout  | `http.fetch(url, { timeout: 5000 })`                 |

### Game Systems

| I want to...       | Use this                                         |
| ------------------ | ------------------------------------------------ |
| Create scoreboard  | `world.addScoreboard("name")`                    |
| Set score          | `world.setScore("player", "board", 10)`          |
| Display scoreboard | `world.showScoreboard("sidebar", "name")`        |
| Show BossBar       | `world.showBossbar("id", "title", 0.5, "red")`   |
| Create team        | `world.createTeam("teamName", "color")`          |
| Join team          | `world.joinTeam(entity, "teamName")`             |
| Set world border   | `world.borderSize = 500`                         |
| Shrink border      | `world.shrinkBorder(100, 60)`                    |
| Change time        | `world.time = 6000`                              |
| Set weather        | `world.rainDensity = 0` / `world.clearWeather()` |
| Change game rule   | `world.setGameRule("keepInventory", true)`       |

### Math Tools

| I want to...  | Use this                                                      |
| ------------- | ------------------------------------------------------------- |
| 3D coordinate | `new GameVector3(x, y, z)`                                    |
| Vector math   | `v.add(other)`, `v.scale(n)`, `v.length()`                    |
| Bounding box  | `new GameBounds3(min, max)`                                   |
| Color         | `new GameRGBColor(r, g, b)` / `new GameRGBAColor(r, g, b, a)` |

### Cross-Script Messaging

| I want to...               | Use this                                   |
| -------------------------- | ------------------------------------------ |
| Send to another script     | `world.sendMessage("projectName", data)`   |
| Receive from other scripts | `world.onMessage((from, data) => { ... })` |

## Global Objects

| Object           | Type         | Description                                                                                  |
| ---------------- | ------------ | -------------------------------------------------------------------------------------------- |
| `world`          | Server       | World control, see [world.md](world.md)                                                |
| `voxels`         | Server       | Block operations, see [voxels.md](voxels.md)                                           |
| `entity`         | Server value | Entity wrapper (from callbacks or `world.spawnEntity`), see [entity.md](entity.md)     |
| `player`         | Server value | Player wrapper (via `entity.player`), see [player.md](player.md)                       |
| `storage`        | Both         | Data persistence, see [storage.md](storage.md)                                         |
| `db`             | Both         | SQLite database, see [database.md](database.md)                                        |
| `http`           | Both         | HTTP requests, see [http.md](http.md)                                                  |
| `audio`          | Client       | Client sound, music, volume control, see [audio.md](audio.md)                          |
| `client`         | Client       | Client lifecycle, see [client.md](client.md)                                           |
| `input`          | Client       | Client keyboard input, see [input.md](input.md)                                        |
| `ui`             | Client       | Client screen UI, see [ui.md](ui.md)                                                   |
| `chat`           | Client       | Client chat send/receive, see [chat.md](chat.md)                                       |
| `gui`            | Client       | Custom container GUI, see [gui.md](gui.md)                                             |
| `remoteChannel`  | Both         | Server↔client event channel, see [remote-channel.md](remote-channel.md)               |
| `registries`     | Server       | Custom blocks, items & sounds (compiled mode), see [registries.md](registries.md)      |
| `console`        | Both         | Console logging (`log`/`warn`/`error`/`debug`)                                               |
| `GameVector3`    | Both         | 3D vector, see [math.md](math.md)                                                      |
| `GameBounds3`    | Both         | Bounding box, see [math.md](math.md)                                                   |
| `GameRGBColor`   | Both         | RGB color, see [math.md](math.md)                                                      |
| `GameRGBAColor`  | Both         | RGBA color, see [math.md](math.md)                                                     |
| `GameQuaternion` | Both         | Quaternion, see [math.md](math.md)                                                     |

## API Legend

| Label              | Meaning                                                            |
| ------------------ | ------------------------------------------------------------------ |
| ✅ **Box3 API**    | Originates from the Box3 platform; naming and semantics match Box3 |
| ⬆ **MC Extension** | Not in original Box3; added using Minecraft-specific features      |

## Documentation Style

Each API document should follow this structure. Use the same style when adding future APIs:

1. State the runtime at the top: server, client, or shared.
2. List globals and core concepts before method details.
3. Use `object.method(parameters)` for method headings.
4. Document parameters in tables with name, type, default, and meaning.
5. Prefer TypeScript/JavaScript examples and identify server or client context when needed.
6. For cross-side APIs, always state the direction: server → client, or client → server.
7. If docs and types disagree, treat `types/server/index.d.ts` and `types/client/index.d.ts` as the source of truth, then update the docs.

## Detailed Document Index

| Document                             | Content                                                                                                          |
| ------------------------------------ | ---------------------------------------------------------------------------------------------------------------- |
| [server.md](server.md)         | Server API overview: runtime boundary, globals, events, players/entities, blocks, data, cross-side communication |
| [world.md](world.md)           | World state, events, scoreboard, bossbar, teams, border, particles, fireworks, lightning, sounds                 |
| [entity.md](entity.md)         | Entity properties, AI, equipment, potion effects, pathfinding, tags, collisions                                  |
| [player.md](player.md)         | Inventory, messaging, flight, game mode, teleport, commands, XP                                                  |
| [voxels.md](voxels.md)         | Block read/write, region fill, spawner control                                                                   |
| [storage.md](storage.md)       | Persistent data storage                                                                                          |
| [database.md](database.md)     | SQLite database API                                                                                              |
| [http.md](http.md)             | HTTP request API                                                                                                 |
| [client.md](client.md)         | Client lifecycle: onTick, getFPS, getPlayer, getLookingAt, getServerInfo, fog control                            |
| [audio.md](audio.md)           | Client audio playback and volume control                                                                         |
| [input.md](input.md)           | Client keyboard input detection and mouse events                                                                 |
| [ui.md](ui.md)                 | Client screen UI: titles, action bar, custom drawn text                                                          |
| [chat.md](chat.md)             | Client chat message sending/receiving and commands                                                               |
| [gui.md](gui.md)               | Custom container GUI interface                                                                                   |
| [remote-channel.md](remote-channel.md) | Client↔server bidirectional event communication                                                          |
| [registries.md](registries.md) | Custom blocks, items & sounds (blocks.json, items.json, sounds.json, creativeTabs.json)                          |
| [math.md](math.md)             | GameVector3, GameBounds3, GameRGBColor, GameRGBAColor, GameQuaternion                                            |
| [commands.md](commands.md)     | `/box3script` command reference                                                                                  |

## File Modules — TypeScript Build Pipeline

Projects created with `/box3script create` come with a complete TS build environment:

```text
config/box3/script/mygame/
├── package.json          ← esbuild + Babel + @babel/preset-typescript
├── tsconfig.json         ← TS project references root (references server + client)
├── tsconfig.server.json  ← Server-side TS config (standalone compilerOptions + include)
├── tsconfig.client.json  ← Client-side TS config (standalone compilerOptions + include)
├── build.mjs             ← Babel TS→JS → esbuild bundle → dist/
├── types/
│   ├── shared.d.ts       ← Shared types (server & client)
│   ├── server/
│   │   ├── index.d.ts    ← Server type entry point
│   │   ├── server.d.ts
│   │   ├── entity.d.ts
│   │   ├── player.d.ts
│   │   ├── world.d.ts
│   │   └── voxels.d.ts
│   └── client/
│       ├── index.d.ts    ← Client type entry point
│       ├── client.d.ts
│       ├── audio.d.ts
│       ├── input.d.ts
│       ├── ui.d.ts
│       ├── chat.d.ts
│       └── gui.d.ts
├── src/
│   ├── server/
│   │   ├── app.ts        ← Server entry point
│   │   └── ...
│   └── client/
│       ├── app.ts        ← Client entry point
│       └── ...
└── dist/
    ├── server.js          ← Server compiled output
    ├── client.js          ← Client compiled output
    └── <name>-<ver>.jar  ← Standalone JAR (/box3script compile)
```

Run `npm run build` to build. Use `/box3script watch` to enable file watching for auto hot-reload.

## Deployment

When ready to distribute, compile your script into a **standalone JAR mod** that runs on any NeoForge server alongside Box3JS:

```js
/box3script compile <project>
```

Outputs `<project>-<version>.jar` (metadata read from `package.json`: name, displayName, version, description, author, license, homepage, logoFile). Drop it into `mods/` and start the server.

See [full command reference →](commands.md#box3script-compile-project)

## Tick Conversion

| Duration   | Ticks |
| ---------- | ----- |
| 1 second   | 20    |
| 5 seconds  | 100   |
| 30 seconds | 600   |
| 1 minute   | 1200  |
| 5 minutes  | 6000  |

## Deep Dive

| Doc                                           | Content                                                        |
| --------------------------------------------- | -------------------------------------------------------------- |
| [Quick Start](../guide/getting-started.md) | Setup, first script, dev cycle, debugging, deployment          |
| [Architecture](../guide/architecture.md)   | Rhino engine, scopes, event callbacks, build pipeline, network |
| [JS vs Java](../guide/js-vs-java.md)       | Box3JS scripting vs native Java modding comparison             |

## Tutorials

Learn Box3JS from scratch with the tutorial series in `docs/tutorial/`:

| Tutorial                                                     | Content                                                      |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| [01-basics.md](../tutorial/01-basics.md)                     | From zero: first script, chat commands, timers               |
| [02-player-items.md](../tutorial/02-player-items.md)         | Player controls: teleport, items, potion effects, game modes |
| [03-events-entities.md](../tutorial/03-events-entities.md)   | Events & entities: AI, combat, patrols                       |
| [04-advanced-systems.md](../tutorial/04-advanced-systems.md) | Advanced: scoreboard, BossBar, teams, world border           |
| [05-examples.md](../tutorial/05-examples.md)                 | Real-world: PvP arena, effects, fireworks, wave mobs         |
