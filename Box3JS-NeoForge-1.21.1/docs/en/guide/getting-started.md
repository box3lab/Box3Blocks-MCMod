---
---

# Quick Start: From Zero to Your First Box3JS Script

For readers with **zero modding experience**. Just need to know JavaScript.

## What is Box3JS

Box3JS is a **Minecraft mod** that embeds a JavaScript runtime inside the server, letting you write gameplay in JS/TypeScript. Client-side scripts are optionally delivered for key listeners, screen UI, and local audio.

Box3JS inherits its API design from **[Box3](https://box3.fun) (神奇代码岛)**, a browser-based multiplayer 3D game creation platform where thousands of creators build games with JavaScript. Box3's API has been battle-tested by its community — clean, intuitive, and efficient. Box3JS brings that same API paradigm to Minecraft, so you can build mini-games and custom gameplay the Box3 way.

::: tip More background
→ [Box3JS & Box3](about-box3js.md)
:::

### Architecture at a Glance

```text
You write           Build tools              Minecraft runs
  TypeScript  ───→  compile to ES5  ───→    Rhino engine executes
                                                 │
                                          ┌──────┴──────┐
                                          │  NeoForge    │
                                          │  Minecraft   │
                                          │  API layer   │
                                          └─────────────┘
```

- **You write TypeScript** with full type hints and modern syntax
- **Babel compiles** down to ES5 (Rhino only supports ES5)
- **esbuild bundles** into a single JS file
- **Rhino runs inside the JVM**, directly calling Minecraft APIs
- **Dual-side execution** (server + client), communicating via `remoteChannel`

### What You Can Do

| Category           | Examples                                                |
| ------------------ | ------------------------------------------------------- |
| Chat Commands      | `!heal`, `!home`, `!shop`                               |
| Event Response     | Welcome on join, death penalty, block break logging     |
| Entity Control     | Spawn mobs, set AI, custom bosses                       |
| Mini-Games         | PvP arena, parkour, wave survival                       |
| World Manipulation | Place/replace blocks, fill regions, change weather/time |
| Data Persistence   | JSON storage, SQLite database                           |
| Game Systems       | Scoreboards, BossBars, teams, world borders             |
| HTTP Requests      | Web API calls, webhook notifications                    |
| Client Scripts     | Key listeners, screen UI, client audio, custom GUIs     |

### What You Can't Do

- **Render custom models/particles** — requires a client resource pack or Java mod
- **Add new blocks/items at runtime** — requires compiling to a JAR (`/box3script compile`)
- **Modify vanilla mechanics** — changing recipes or mob AI behavior requires Mixin
- **Use modern JS syntax at runtime** — Rhino supports ES5 only, but you can use modern TypeScript in source (the build step handles transpilation)

## Setup

### What You Need

1. **Minecraft server** with Box3JS + NeoForge 1.21.1 installed
2. **Node.js** 18+ (for local builds only — not needed on the server)
3. A text editor (VS Code recommended, with full TypeScript IntelliSense)

### Verify Installation

In-game, run:

```js
/box3script
```

If you see the project status panel, Box3JS is running.

```text
══ Box3JS Script Engine ══
  Watch: ○ Inactive    Sandbox: ○ Inactive
  Projects: 0 enabled  |  0 loaded
```

## Create a Project

### One-Command Creation

In-game:

```js
/box3script create mygame
```

This generates a complete TypeScript project at `config/box3/script/mygame/`.

### Understanding Project Structure

```text
config/box3/script/mygame/
├── package.json           ← Project config (name, version, build deps)
├── tsconfig.json          ← TS project references root (references server + client)
├── tsconfig.server.json   ← Server TS config (standalone compilerOptions + server/ types)
├── tsconfig.client.json   ← Client TS config (standalone compilerOptions + client/ types)
├── build.mjs              ← Build script (esbuild + Babel)
├── eslint.config.mjs      ← ESLint rules
├── types/                 ← ★ Type declarations (API reference)
│   ├── shared.d.ts        ← Shared server & client types
│   ├── server/
│   │   ├── index.d.ts     ← Server type entry point
│   │   ├── server.d.ts    ← world, remoteChannel, registries
│   │   ├── entity.d.ts    ← GameEntity interface
│   │   ├── player.d.ts    ← GamePlayer interface
│   │   ├── world.d.ts     ← GameWorld interface
│   │   └── voxels.d.ts    ← GameVoxels interface
│   └── client/
│       ├── index.d.ts     ← Client type entry point
│       ├── client.d.ts    ← GameClient, RemoteChannel
│       ├── audio.d.ts     ← GameAudio
│       ├── input.d.ts     ← GameInput
│       ├── ui.d.ts        ← GameUI
│       ├── chat.d.ts      ← GameChat
│       └── gui.d.ts       ← GameGUI, GuiController
├── src/
│   ├── server/
│   │   └── app.ts         ← ★ Server entry point (where you write code)
│   └── client/
│       └── app.ts         ← Client entry point
├── registries/            ← Custom content (blocks/items/sounds JSON)
└── assets/lang/           ← Custom content localization
```

**Key insights:**

- The `.d.ts` files in `types/` are your API reference — VS Code uses them for IntelliSense
- `tsconfig.json` manages two sub-projects via `references`; `tsconfig.server.json` and `tsconfig.client.json` each contain standalone `compilerOptions` and `include`, and are **mutually exclusive** — server code never sees client globals like `client`, `input`, etc.
- You only need to write code in `src/server/app.ts` (and optionally `src/client/app.ts`); the build tooling handles everything else

### Install Dependencies

```bash
cd config/box3/script/mygame
npm install
```

Run `npm install` once (installs esbuild, Babel, TypeScript build tooling).

## Your First Script: Line by Line

Open `src/server/app.ts`, clear the contents, and write the code below. Let's understand each part.

### 1. Startup Log

```js
console.log("MyGame script started!");
```

**What happens:** `console` is a global object injected by Box3JS (no `import` needed). Behind it is a Java class `Box3JSConsole`, bridged to JS via Rhino. `console` supports six methods: `log`, `warn`, `error`, `debug`, `clear`, `assert`.

### 2. Welcome Players on Join

```js
world.onPlayerJoin((entity) => {
  const p = entity.player;

  // Broadcast to all
  world.say(`§e${p.name} §7joined the server`);

  // Private message
  p.directMessage(`§aWelcome to the server, ${p.name}!`);

  // Particle welcome effect
  const { position: pos } = p;
  world.spawnParticleCircle(
    pos.x,
    pos.y,
    pos.z,
    1.5,
    "minecraft:happy_villager",
    15,
  );
  world.playSound("minecraft:block.note_block.pling", pos, 1.0, 1.5);
});
```

**Line by line:**

- `world.onPlayerJoin(...)` — Registers a "player join" event listener. Returns a `GameEventHandlerToken` (not saved in this example, so the listener stays active until script reload).
- `entity.player` — `entity` is the callback parameter, representing the joining entity. `.player` gets the player wrapper (if the entity isn't a player, `.player` is `undefined`).
- `p.directMessage(...)` — Sends a private message visible only to that player.
- `§a` is a Minecraft color code (green). `§e` = yellow, `§6` = gold, `§7` = gray, `§c` = red.
- `p.position` — Returns a `GameVector3` object with `.x`, `.y`, `.z` properties.
- `world.spawnParticleCircle(...)` — Spawns a ring of particles at the player's position.
- `world.playSound(...)` — Plays a sound at the player's position.

### 3. Chat Commands

```js
world.onChat((entity, message) => {
  const p = entity.player;

  if (message === "!hello") {
    p.directMessage(`§eHello, ${p.name}!`);
    return false; // suppress chat message from broadcasting
  }

  if (message === "!pos") {
    const { position: pos2 } = p;
    p.directMessage(
      `§eYour position: §f${Math.floor(pos2.x)}, ${Math.floor(pos2.y)}, ${Math.floor(pos2.z)}`,
    );
    return false;
  }

  if (message === "!help") {
    p.directMessage("§eAvailable commands: !help, !hello, !pos, !home, !shop");
    return false;
  }

  return true; // normal messages pass through
});
```

**Key concepts:**

- `return false` — Blocks the event from continuing (message won't appear in chat). This is the universal Box3JS event convention: **return false to cancel the default behavior**.
- `return true` — Let it through; message broadcasts normally.
- `Math.floor()` — Regular JS, rounds coordinates down for readability.

### 4. Periodic Announcement

```js
setInterval(() => {
  const count = world.querySelectorAll("*").length;
  world.say(`§7[Info] §fPlayers online: ${String(count)}`);
}, 6000); // 6000 ticks = 5 minutes
```

**Key understanding:**

- `setInterval` is a **global function** (not `world.setInterval`), just like in browsers and Node.js.
- The second argument is **ticks** (Minecraft time unit), not milliseconds. 1 second = 20 ticks.
- `setInterval` returns a `GameEventHandlerToken` — call `.cancel()` to stop it.
- `world.querySelectorAll("*")` returns all online entities.
- `world.say(...)` broadcasts to the entire server.

### Tick Conversion Table

| Duration   | Ticks  |
| ---------- | ------ |
| 1 second   | 20     |
| 5 seconds  | 100    |
| 30 seconds | 600    |
| 1 minute   | 1,200  |
| 5 minutes  | 6,000  |
| 10 minutes | 12,000 |
| 30 minutes | 36,000 |

## Core Design Philosophy: Why the API Works This Way

Understanding the design rationale behind Box3JS APIs helps you write more efficient and safer scripts. Here are the most important design decisions and their reasons.

### Design 1: Global Object Injection — No Imports Needed

```js
// In Box3JS, use directly — no import required
world.onTick(() => { ... });
console.log("hello");
storage.getDataStorage("coins");

// Compare: if this were Node.js
// const { world } = require("box3js");  ← Not needed!
```

**Why?** Rhino is a bare ECMAScript engine — it doesn't support CommonJS `require()` or ES Module `import`. Box3JS injects all API objects as global variables at Rhino scope initialization time, directly from Java. The `.d.ts` TypeScript files declare these globals with `declare`, giving you full IntelliSense in VS Code without any imports.

### Design 2: Tick-Based Timing — Not Milliseconds

```js
// Box3JS time unit is ticks (1/20 second)
setTimeout(() => { ... }, 100);  // 100 ticks = 5 seconds later

// Compare browser:
// setTimeout(() => { ... }, 5000);  // 5000 milliseconds = 5 seconds
```

**Why?** Box3JS timers execute **on the main game tick loop**, not on separate Java threads. Each game tick (1/20 second), the engine checks all timers, decrements their remaining ticks, and fires callbacks whose countdown reaches 0. Benefits:

- **Thread safety** — Callbacks always run on the main thread; you can safely call any Minecraft API
- **Precise synchronization** — Timers are perfectly synced with the game world; a lagging server slows timers too
- **Zero overhead** — No extra threads or thread pools

### Design 3: GameEventHandlerToken — Universal Cancellation

```js
// All onXxx() and setTimeout/setInterval return GameEventHandlerToken
const token = world.onTick(() => {
  // runs every tick
});

// Cancel the listener (these are equivalent)
token.cancel();

// Check if still active
if (token.active()) {
  // ...
}
```

**Why?** Early designs provided separate cancellation methods per event type (`removeTickListener`, `removeChatListener`...), which meant:

1. Memorizing different cancellation API names for each event type
2. No unified way to manage them (how do you batch-cancel all listeners on script stop?)

A unified `GameEventHandlerToken` pattern solves this:

- **One pattern for everything** — `onTick`, `onPlayerJoin`, `onChat`, `setInterval` all use `.cancel()`
- **Auto-cleanup on reload** — When stopping a project, the engine iterates all tokens and cancels them in bulk
- **Chainable management** — Collect tokens in an array and cancel them all at once

### Design 4: Per-Project Scope Isolation

```text
Server runs 3 script projects simultaneously, completely independent:

┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ Scope A      │  │ Scope B      │  │ Scope C      │
│ "mygame"     │  │ "lobby"      │  │ "survival"   │
│              │  │              │  │              │
│ var x = 1    │  │ var x = 2    │  │ var x = 3    │
│ own events   │  │ own events   │  │ own events   │
│ own storage  │  │ own storage  │  │ own storage  │
│ own timers   │  │ own timers   │  │ own timers   │
└──────────────┘  └──────────────┘  └──────────────┘
```

**Why?** A server might run multiple scripts simultaneously (lobby system, mini-games, economy...). Without isolation:

- Variable name collisions (`var playerCount` defined in two scripts)
- Event callback interference (`/box3script reload lobby` accidentally clearing survival's callbacks)
- Data leakage (lobby's script reading survival's storage)

Box3JS gives each project an **independent Rhino top-level scope**, backed by separate `Box3JSEventBus` namespaces. Stopping or reloading one project never affects others.

### Design 5: Dual-Side Architecture + remoteChannel

```text
┌──────────────────────┐         ┌──────────────────────┐
│   Server             │         │   Client             │
│                      │         │                      │
│  world.*             │         │  client.*            │
│  voxels.*            │  JSON   │  audio.*             │
│  entity.*            │ ←─────→ │  input.*             │
│  player.*            │ events  │  ui.*                │
│  storage/db/http     │         │  chat.* / gui.*      │
│  remoteChannel ──────┼─────────┼── remoteChannel      │
│                      │         │  storage/db/http     │
└──────────────────────┘         └──────────────────────┘
```

**Why the split?**

- **Security** — The server is the world authority (blocks, entities, data); clients only handle local presentation (UI, audio, input)
- **Performance** — Client scripts run on each player's own machine, consuming zero server resources
- **Flexibility** — Write server-only scripts for most cases, or add client scripts to enhance the experience

**remoteChannel communication rules:**

```js
// Server → single client
remoteChannel.sendClientEvent(player, { type: "welcome", msg: "hi" });

// Server → all clients
remoteChannel.broadcastClientEvent({ type: "game_start" });

// Client → server
remoteChannel.sendServerEvent({ key: "space", pressed: true });
```

**Critical limitation:** Data crossing the network must be **JSON-serializable**. No functions, `GameVector3` instances, or Java objects. To send coordinates, use `{ x: 1, y: 2, z: 3 }` instead of `new GameVector3(1, 2, 3)`.

### Design 6: TypeScript Source + Babel Compilation to ES5

```text
src/server/app.ts         Babel               esbuild        dist/server.js
(TypeScript, ES2020)  ───→  ES5 JavaScript  ───→  bundle  ───→  (single file)
```

**Why the build step?**

- **Rhino 1.9.1 only supports ES5** — `let`, `const`, arrow functions, template literals, and `class` are all ES6+ features Rhino can't parse
- **Babel downlevels** — Transforms modern syntax into `var`, `function`, string concatenation, and other ES5 equivalents
- **esbuild bundles** — You can write multiple `.ts` files, but Rhino has no module system. esbuild merges everything into a single IIFE

**You can safely use in source:**

- `const` / `let` (transpiled to `var`)
- Arrow functions `() => {}` (transpiled to `function(){}`)
- Template literals `` `hello ${name}` `` (transpiled to `"hello " + name`)
- `class` (transpiled to `function` + prototype)
- `async/await` (via regenerator transform)

### Design 7: Event Callbacks Return false to Cancel

```js
world.onChat((entity, message) => {
  if (message.startsWith("!")) {
    // This is a command — don't broadcast
    return false;
  }
  return true; // normal messages pass through
});
```

**Why?** Inspired by the browser DOM event `preventDefault` pattern. Minecraft events typically have "default behavior" (e.g., chat messages broadcast to everyone). `return false` tells the engine: "I've handled this event — skip the default behavior."

### Design 8: Sandbox Mode — Safe Testing

```js
/box3script sandbox mygame    # enable sandbox
# ... test script (spawn entities, modify blocks, explode)...
/box3script sandbox mygame    # disable → auto-rollback all changes
```

**Why?** Once a script modifies the world, those changes are permanent (blocks replaced, entities spawned). Sandbox mode tracks all world modifications made by the script and auto-rolls them back when disabled. This lets developers fearlessly test destructive operations without damaging the live server.

## API Quick Tour

Organized by "what do I want to do?" — the most commonly used APIs. For the complete reference, see the [API docs](../api/README.md).

### Messages & Chat

```js
// Broadcast to all
world.say("§6Server will restart in 5 minutes");

// Private message (only target player sees it)
player.directMessage("§aYour balance: 100 coins");

// Action bar text (above hotbar)
player.actionBar("§ePress F to open menu");

// Screen title (large centered text)
player.title("§6BOSS FIGHT", "§cThe Ancient Dragon awakens");

// Chat interception (command system)
world.onChat((entity, message) => {
  if (message === "!help") {
    entity.player.directMessage("§eAvailable commands: !help, !home, !shop");
    return false;
  }
  return true;
});
```

### Player Properties & Control

```js
// Get player info
const name = player.name;
const pos = player.position; // GameVector3 { x, y, z }
const hp = player.hp;
const mode = player.gameMode; // "survival", "creative", "adventure", "spectator"

// Modify player state
player.hp = 20; // full heal
player.maxHp = 40; // increase max health
player.food = 20; // full hunger
player.gameMode = "creative"; // switch to creative
player.canFly = true; // allow flight
player.flying = true; // start flying

// Teleport
player.teleport(new GameVector3(100, 64, 100));

// Kick
player.kick("You have been kicked by an admin");

// Run vanilla commands as the player
player.runCommand("effect give @s minecraft:speed 30 1");
```

### Items & Inventory

```js
// Give items
player.giveItem("minecraft:diamond", 64);
player.giveItem("minecraft:diamond_sword", 1);

// Give named items (4th param is the lore/description array)
player.giveNamedItem("minecraft:stick", 1, "§6Magic Wand", ["A magical wand"]);

// Give enchanted items (enchantments as { enchantId: level } record)
player.giveEnchantedItem("minecraft:diamond_sword", 1, {
  "minecraft:sharpness": 5,
  "minecraft:unbreaking": 3,
});

// Check held item
const held = player.getHeldItem();

// Clear inventory
player.clearInventory();
```

### Event System

```js
// Every tick (use sparingly! Heavy work in onTick drags down TPS)
const tickToken = world.onTick(() => {
  // runs every tick
});

// Player events
world.onPlayerJoin((entity) => {
  entity.player.directMessage("Welcome!");
});

world.onPlayerLeave((entity, _tick) => {
  world.say(`${entity.player.name} left the server`);
});

world.onPlayerRespawn((entity, _tick) => {
  entity.player.teleport(new GameVector3(0, 100, 0));
  entity.player.directMessage("You respawned!");
});

// Entity events
world.onEntityDeath((entity, _killer, _tick) => {
  if (entity.isPlayer()) {
    world.say(`${entity.player.name} died`);
  }
});

world.onEntityDamage((entity, amount, source, _attacker, _tick) => {
  if (amount > 10) {
    console.log(`High damage: ${String(amount)} from ${source}`);
  }
});

// Interaction events
world.onInteract((entity, target, _tick) => {
  // Player right-clicked an entity
  if (target.hasTag("npc")) {
    entity.player.directMessage("Hello!");
  }
});

world.onBlockActivate((entity, x, y, z, voxel, _tick) => {
  // Player right-clicked a block
  if (voxel === "minecraft:chest") {
    entity.player.directMessage(
      `You clicked a chest at ${String(x)}, ${String(y)}, ${String(z)}`,
    );
  }
});

world.onBlockPlace((entity, x, y, z, voxel, _voxelId, _tick) => {
  // Player placed a block
  console.log(
    `${entity.player.name} placed ${voxel} at ${String(x)}, ${String(y)}, ${String(z)}`,
  );
});

world.onVoxelDestroy((entity, _x, _y, _z, voxel, _tick) => {
  // Player is about to break a block
  if (voxel === "minecraft:diamond_block") {
    entity.player.directMessage("§cYou can't break diamond blocks!");
    return false; // cancel the break
  }
});

// Timers (global functions)
const timer = setTimeout(() => {
  world.say("30 seconds have passed!");
}, 600); // 600 ticks = 30 seconds

const interval = setInterval(() => {
  world.say("Minute announcement");
}, 1200); // 1200 ticks = 1 minute

// Cancel timers
timer.cancel();
interval.cancel();
```

### Entity Manipulation

```js
// Spawn an entity (returns GameEntity | null)
const zombie = world.spawnEntity(
  "minecraft:zombie",
  new GameVector3(100, 64, 100),
);
if (zombie) {
  // use zombie ...
}

// Create with full config (nameTag/glowing/equipment set after creation)
const boss = world.createEntity({
  type: "minecraft:zombie",
  position: new GameVector3(100, 64, 100),
  hp: 200,
  maxHp: 200,
  tags: ["boss"],
});
if (boss) {
  boss.setNameTag("§cAncient Zombie King");
  boss.glowing = true;
  boss.setEquipment("head", "minecraft:diamond_helmet");
  boss.setEquipment("chest", "minecraft:diamond_chestplate");

  // Control entities
  boss.setAI(false); // disable AI (stands still)
  boss.invulnerable = true; // invincible
  boss.navigateTo(110, 64, 100, 1.5); // navigate to target

  // Potion effects
  boss.addEffect("minecraft:strength", 600, 2, false);
  boss.addEffect("minecraft:speed", 600, 1, true);
  boss.clearEffects();

  // Equipment
  boss.setEquipment("head", "minecraft:iron_helmet");
  boss.setEquipment("mainhand", "minecraft:iron_sword");

  // Tags (for marking and querying)
  boss.addTag("boss");
  boss.addTag("stage_1");
  boss.hasTag("boss"); // → true
}

// Query entities
const nearby = world.entitiesInRadius(pos, 10); // within 10 blocks
const all = world.querySelectorAll("*"); // all entities
const players = world.querySelectorAll("player"); // all players
const monsters = world.querySelectorAll("monster"); // all monsters
```

### Block Operations

```js
// Read a block
const block = voxels.getVoxel(100, 64, 100);

// Place a block
voxels.setVoxel(100, 64, 100, "minecraft:stone");
voxels.setVoxel(100, 65, 100, "minecraft:torch");

// Fill a region
voxels.fillVoxel(0, 64, 0, 10, 70, 10, "minecraft:glass");

// Replace blocks (only matching type)
voxels.fillVoxel(0, 64, 0, 10, 70, 10, "minecraft:air", "minecraft:stone");
```

### Data Persistence

```js
// JSON storage (per-project namespace)
const store = storage.getDataStorage("coins");
store.set("player1", 100);
const coins = store.get("player1"); // → 100
store.delete("player1");
const keys = store.keys(); // → array of all keys

// SQLite database
db.sql("CREATE TABLE IF NOT EXISTS players (name TEXT, score INT)");
db.sql("INSERT INTO players VALUES ('Steve', 100)");

const result = db.sql("SELECT * FROM players WHERE score > 50");
// result.rows[0] → { name: "Steve", score: 100 }
// result.firstRow → { name: "Steve", score: 100 }
// result.rowCount → 1
// result.columnNames → ["name", "score"]
```

### Game Systems

```js
// Scoreboard
world.addScoreboard("kills");
world.setScore("Steve", "kills", 42);
world.showScoreboard("sidebar", "kills");

// BossBar
world.showBossbar("boss1", "§cAncient Dragon", 0.8, "red");
world.setBossbar("boss1", "§cAncient Dragon §7[80%]", 0.5);

// Teams
world.createTeam("red", "red");
world.createTeam("blue", "blue");
world.joinTeam(entity, "red");

// World border
world.borderSize = 500; // set border size
world.shrinkBorder(100, 1200); // shrink to 100 over 1200 ticks

// Weather & time
world.time = 6000; // set time (0=dawn, 6000=noon, 12000=dusk, 18000=midnight)
world.rainDensity = 0; // stop rain
world.clearWeather(); // clear skies
world.thunderDensity = 1; // thunderstorm

// Game rules
world.setGameRule("keepInventory", true);
world.setGameRule("doDaylightCycle", false);
```

### Visual Effects

```js
const pos = new GameVector3(100, 64, 100);

// Particles
world.spawnParticle("minecraft:flame", pos.x, pos.y, pos.z, 0, 0, 0, 1, 10);
world.spawnParticleCircle(pos.x, pos.y, pos.z, 2, "minecraft:heart", 30);

// Fireworks
world.launchFirework(pos.x, pos.y, pos.z, "red", "large_ball");
world.launchFirework(pos.x, pos.y, pos.z, "green", "star");

// Lightning & explosion
world.strikeLightning(pos.x, pos.y, pos.z);
world.explode(pos.x, pos.y, pos.z, 4); // power 4 explosion

// Sounds
world.playSound("minecraft:entity.ender_dragon.growl", pos, 1.0, 1.0);
player.playSound("minecraft:block.note_block.pling", 1.0, 2.0); // only that player hears it
```

### HTTP Requests

```js
// GET request
const resp = http.fetch("https://api.example.com/data");
if (resp.ok) {
  const data: unknown = resp.json();
  console.log(data);
}

// POST JSON
const resp2 = http.fetch("https://api.example.com/webhook", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ text: "Server restarted" })
});

// With timeout
const resp3 = http.fetch("https://slow-api.com/data", {
  timeout: 5000 // 5 second timeout
});
```

### Client Scripting (requires Box3JS client mod)

```js
// Client entry: src/client/app.ts

// Every frame
client.onTick(() => {
  // client tick callback
});

// Keyboard input
if (input.isKeyDown("space")) {
  // spacebar is being held down
}

input.onKeyPress("f", () => {
  // F key pressed — notify server
  remoteChannel.sendServerEvent({ action: "open_menu" });
});

// Screen UI
ui.showOverlay("§ePress F to open menu"); // above hotbar
ui.showTitle("§6BOSS SPAWNED!", "§cGet ready!"); // centered screen title

// Client audio
audio.playSound("minecraft:block.note_block.pling", 1.0, 1.0);
audio.playMusic("minecraft:music.game", 0.5, 1.0);
audio.stopAll();

// Fog control (client-side rendering)
client.setFogColor(255, 100, 50); // reddish fog appearance
client.setFogStartDistance(10); // fog begins at 10 blocks
client.setFogEndDistance(50); // fully obscured at 50 blocks
client.resetFog(); // restore default

// Chat
chat.sendMessage("Hello everyone!");
chat.onMessage((msg, _sender, _isSystem) => {
  if (msg.includes("secret")) {
    // handle messages containing "secret"
  }
});

// Receive server events
remoteChannel.onClientEvent((event) => {
  if (event.args.type === "boss_spawned") {
    audio.playSound("minecraft:entity.ender_dragon.growl", 1.0, 1.0);
  }
});
```

## Dev Cycle

### Standard Flow

After each code change:

```js
Edit code → npm run build → /box3script reload mygame → test
```

### Build

```bash
npm run build
```

Output:

```js
  dist/server.js  7.1kb
Done in 240ms
```

What the build does:

1. **Babel** compiles TypeScript to ES5 JavaScript (Rhino only supports ES5)
2. **esbuild** bundles all modules into a single file (Rhino lacks `require()`)
3. Outputs to `dist/server.js` and `dist/client.js`

### Load & Reload

In-game:

```js
/box3script start mygame    # first launch
/box3script reload mygame   # reload after changes (no server restart)
```

`reload` is atomic: stops the old script (cleans up all callbacks, timers, scoreboards), then loads the new one.

### Auto Hot-Reload

Enable file watching so builds auto-trigger reload:

```js
/box3script watch
```

**Note:** `watch` monitors the `dist/` compiled output (`.js`), not `src/` source. So you need to run `npm run build` first to generate new `dist/` files. Combine with `npm run build -- --watch` for save-and-reload workflow.

### Multi-Project Management

```js
/box3script start mygame lobby    # start multiple projects
/box3script stop mygame           # stop one
/box3script stopall               # stop all
/box3script reload mygame          # reload one
/box3script                        # view all project statuses
```

## Debugging

### Troubleshooting Order

1. **Check console** — server logs show errors with `[Box3JS] [projectName]` prefix
2. **Check status** — `/box3script` to see if the project shows `◉` (loaded)
3. **Check build** — `npm run build` should complete without errors
4. **Add logging** — use `console.log()` at key points to print variable values
5. **Read line numbers** — Java exception stacks include JS filenames and line numbers (line numbers correspond to compiled `dist/server.js`, not `.ts` source)

### Common Errors

| Error                       | Cause                     | Fix                                                                              |
| --------------------------- | ------------------------- | -------------------------------------------------------------------------------- |
| `console is not defined`    | Engine init failed        | Check mod installation                                                           |
| `world is not defined`      | Scope issue               | Ensure code is at global scope, not inside a nested function                     |
| `Cannot find name 'xxx'`    | TypeScript type error     | Check spelling, or look up the correct API name in `types/` `.d.ts`              |
| `npm run build` fails       | JS syntax error           | Check ESLint output or terminal error line numbers                               |
| Script not executing        | Project not enabled       | Check `/box3script` status                                                       |
| Timer never fires           | Tick count miscalculation | Remember: 1 sec = 20 ticks, not 1000                                             |
| Client script not working   | Player lacks client mod   | Box3JS client mod must be installed                                              |
| remoteChannel not receiving | Data isn't JSON           | Ensure you're sending plain objects, not Java objects or `GameVector3` instances |

### Sandbox Testing

Sandbox mode enables safe testing: all world modifications are tracked and rolled back when disabled.

```js
/box3script sandbox mygame    # enable sandbox
# ... test script (spawn entities, modify blocks, explode, etc.)...
/box3script sandbox mygame    # disable → auto-rollback all changes
```

**Use cases:**

- **First test of a new script** — unsure what it does? Sandbox first
- **Player play-testing** — let players try new features, rollback after without affecting the live server
- **Debugging destructive operations** — test `fillVoxel`, `explode`, etc.

### Performance Tips

Box3JS scripts run on the server main thread — unreasonable code affects TPS:

1. **Avoid large loops in `onTick`** — scan entities on condition triggers, not every tick
2. **Cache query results** — don't put `querySelectorAll` in onTick
3. **Use `setInterval` over `onTick`** — if you don't need 20/sec, use longer intervals (e.g., 100 ticks = 5 seconds)
4. **Minimize JS ↔ Java crossings** — batch operations are faster than individual calls

A typical parkour script consumes < 0.5ms/tick, with virtually no impact on server TPS.

## Deployment

### Dev Mode → Production Release

When development is done, compile your script into a **standalone JAR mod**:

```js
/box3script compile mygame
```

Generates `mygame-1.0.0.jar` (version from `package.json`). Drop it into any NeoForge server's `mods/` directory.

**Notes:**

- Box3JS must also be installed as a dependency (provides the Rhino runtime)
- If you use `registries` (custom blocks/items), clients must also install the JAR
- The JAR contains compiled JS — no source code needed
- The compiled JAR is a standalone NeoForge mod with its own `mods.toml`

### package.json Config

```json
{
  "name": "mygame",
  "displayName": "My Game",
  "version": "1.0.0",
  "description": "A custom mini-game",
  "author": "YourName",
  "license": "MIT",
  "homepage": "https://example.com",
  "logoFile": "logo.png"
}
```

These metadata fields are written into the JAR's `mods.toml` and shown in the game's mod list.

### Dev Mode vs Compiled Mode

|              | Dev Mode (`/box3script start`) | Compiled Mode (`/box3script compile`) |
| ------------ | ------------------------------ | ------------------------------------- |
| Code changes | Hot reload, no restart         | Must recompile                        |
| `registries` | `undefined`                    | ✅ Available                          |
| Distribution | Source code required           | JAR only                              |
| Use case     | Development, testing           | Release, distribution                 |

## Next Steps

Now you understand Box3JS's core design philosophy and basic API usage. Next:

- **API details**: [API by Task](../api/README.md) — find APIs by "I want to..."
- **Event system**: [Tutorial 3: Events & Entities](../tutorial/03-events-entities.md)
- **Client APIs**: [Client API docs](../api/client.md) — key listeners, screen UI, client audio
- **Internals**: [Architecture](architecture.md) — Rhino engine, scopes, build pipeline, networking
- **Tech choice**: [JS vs Java](js-vs-java.md) — Box3JS scripting vs native Java modding
- **FAQ**: [Frequently Asked Questions](faq.md)
- **Recipes**: [Code Snippets & Recipes](recipes.md) — copy-paste solutions for common tasks
