# Box3JS API Reference

Box3JS is a Minecraft mod that lets you write server-side scripts in JavaScript/TypeScript. All scripts run under `config/box3/script/<project>`.

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

Open `src/app.ts` and write:

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

After each edit, re-run `npm run build`, then use `/box3script reload mygame` to hot-reload.

> **New here?** [Quick Start Guide](../guide/getting-started_en.md) | **How it works:** [Architecture](../guide/architecture_en.md) | **JS vs Java:** [Comparison](../guide/js-vs-java_en.md)

## API Domain Map

Box3JS APIs are divided by runtime environment:

| Domain | Runtime | Globals | Description |
|--------|---------|---------|-------------|
| **World & Entities** (server) | Server | `world` `voxels` | World control, blocks, event callbacks |
| **Players & Data** (server) | Server | `player` `entity` `storage` `db` `http` | Accessed via `entity.player` |
| **Client Interaction** (client) | Client | `audio` `client` `input` `ui` `chat` | Requires Box3JS client mod |
| **Cross-Side** | Both | `remoteChannel` | Server↔Client event communication |
| **Registries** | Compile-time | `registries` | Only in `/box3script compile` JAR mode |
| **Math & Utilities** | Both | `GameVector3` `GameBounds3` `GameRGBColor` `GameRGBAColor` `GameQuaternion` | Constructed with `new` |
| **Global Tools** | Both | `console` | Log output |

> **Server APIs** manipulate the world, entities, players, and blocks. Scripts run on the server by default.  
> **Client APIs** are only available with the Box3JS client mod installed, for UI, input, and audio.  
> **Registry APIs** are only available in compiled JAR mode (`registries` is `undefined` in interpreted mode).

## Find by Task — I want to...

Find APIs by what you want to do, not by which global object they live on.

### Messages & Chat

| I want to... | Use this |
|-------------|----------|
| Broadcast to server | `world.say("message")` |
| Send private message | `player.directMessage("message")` |
| Show action bar text | `player.actionBar("message")` |
| Show screen title | `player.title("Title", "Subtitle")` |
| Handle chat input | `world.onChat((entity, msg) => { ... })` |

### Player Properties

| I want to... | Use this |
|-------------|----------|
| Get/set player position | `player.position` → `GameVector3` |
| Teleport player | `player.teleport(new GameVector3(x, y, z))` |
| Change health | `player.hp = 20` / `player.maxHp = 40` |
| Change hunger | `player.food = 20` / `player.saturation = 10` |
| Switch game mode | `player.gameMode = "creative"` |
| Toggle flight | `player.canFly = true` / `player.flying = true` |
| Kick player | `player.kick("reason")` |
| Run command as player | `player.runCommand("say hi")` |

### Items & Equipment

| I want to... | Use this |
|-------------|----------|
| Give a basic item | `player.giveItem("minecraft:diamond", 1)` |
| Give enchanted item | `player.giveEnchantedItem(...)` |
| Give named item | `player.giveNamedItem(...)` |
| Get held item | `player.getHeldItem()` |
| Clear inventory | `player.clearInventory()` |
| Set entity equipment | `entity.setEquipment("head", "iron_helmet")` |

### Custom Registries (Blocks, Items & Sounds) 🆕

| I want to... | Use this |
|-------------|----------|
| Register custom blocks | `registries/blocks.json` (at compile time) |
| Register custom items | `registries/items.json` (at compile time) |
| Register custom sounds | `registries/sounds.json` (at compile time) |
| Register creative tabs | `registries/creativeTabs.json` (at compile time) |
| Get a registered block | `registries.getBlock("my_block")` |
| Get a registered item | `registries.getItem("chocolate")` |
| Get a registered sound | `registries.getSound("victory_fanfare")` |
| Give a custom block/item | `player.giveItem(block.itemId, 1)` |
| Place a custom block | `voxels.setVoxel(x, y, z, block.block)` |
| Play a custom sound (server) | `world.playSound(sound.soundId, x, y, z, 1, 1)` |
| Play a custom sound (client) | `audio.playSound("modId:soundId", 1.0, 1.0)` |

> **Server-side only.** `registries` is `undefined` in client scripts. **Only available in `/box3script compile` JAR mode.** Client must also install the JAR for textures/models. See [registries_en.md](registries_en.md)

### Block Operations

| I want to... | Use this |
|-------------|----------|
| Read a block | `voxels.getVoxel(x, y, z)` |
| Place/replace a block | `voxels.setVoxel(x, y, z, "minecraft:stone")` |
| Fill a region | `voxels.fillVoxel(x1,y1,z1, x2,y2,z2, "stone")` |
| Listen for block breaks | `world.onVoxelDestroy((entity, x, y, z, voxel) => { ... })` |
| Listen for block placement | `world.onBlockPlace((entity, x, y, z, voxel) => { ... })` |

### Entity Manipulation

| I want to... | Use this |
|-------------|----------|
| Spawn an entity | `world.spawnEntity("minecraft:zombie", pos)` |
| Create with config | `world.createEntity({ type, position, ... })` |
| Set entity name | `entity.setNameTag("§cBoss")` |
| Toggle AI | `entity.setAI(true)` |
| Navigate to position | `entity.navigateTo(x, y, z, speed)` |
| Set attack target | `entity.setTarget(otherEntity)` |
| Check if player | `entity.isPlayer()` |
| Get entity type | `entity.entityType` |
| Get entity tags | `entity.tags()` / `entity.hasTag("boss")` |
| Query nearby entities | `world.entitiesInRadius(pos, radius)` |
| Query all entities | `world.querySelectorAll("*")` |

### Client-side Features (requires Box3JS client mod)

| I want to... | Use this |
|--------------|----------|
| Run every client tick | `client.onTick(() => { ... })` |
| Check key held down | `input.isKeyDown("space")` |
| Listen for key press | `input.onKeyPress("f", () => { ... })` |
| Play sound effect | `audio.playSound("pling", 1.0, 1.0)` |
| Play music | `audio.playMusic("minecraft:music.game", 0.5, 1.0)` |
| Stop all sounds | `audio.stopAll()` |
| Get/set volume | `audio.getVolume("music")` / `audio.setVolume("player", 0.8)` |
| Show action bar text | `ui.showOverlay("text")` |
| Show screen title | `ui.showTitle("Title", "Subtitle")` |
| Send chat message | `chat.sendMessage("message")` |
| Receive chat messages | `chat.onMessage((msg, sender, isSystem) => { ... })` |
| Send event to server | `remoteChannel.sendServerEvent({ ... })` |
| Receive event from server | `remoteChannel.onClientEvent((event) => { ... })` |
| Client-side local storage | `storage.getDataStorage("key")` |

### Visual Effects

| I want to... | Use this |
|-------------|----------|
| Spawn particles | `world.spawnParticle("flame", x, y, z, ...)` |
| Particle circle | `world.spawnParticleCircle(x, y, z, radius, "heart", 20)` |
| Firework | `world.launchFirework(x, y, z, "red", "large_ball")` |
| Lightning | `world.strikeLightning(x, y, z)` |
| Explosion | `world.explode(x, y, z, power)` |
| Play sound (global) | `world.playSound("pling", pos, 1.0, 1.0)` |
| Play sound (per-player) | `player.playSound("pling", 1.0, 1.0)` |

### Potion Effects

| I want to... | Use this |
|-------------|----------|
| Apply an effect | `entity.addEffect("minecraft:speed", duration, level, hideParticles)` |
| Clear all effects | `entity.clearEffects()` |

### Event System

| I want to... | Use this |
|-------------|----------|
| Run every tick | `world.onTick((info) => { ... })` |
| On player join | `world.onPlayerJoin((entity, tick) => { ... })` |
| On player leave | `world.onPlayerLeave((entity, tick) => { ... })` |
| On entity death | `world.onEntityDeath((entity, killer, tick) => { ... })` |
| On entity damaged | `world.onEntityDamage((entity, amount, source, attacker, tick) => { ... })` |
| On right-click entity | `world.onInteract((entity, target, tick) => { ... })` |
| On right-click block | `world.onBlockActivate((entity, x, y, z, voxel, tick) => { ... })` |
| On button pressed | `world.onButtonPressed((entity, button, tick) => { ... })` |
| On player respawn | `world.onPlayerRespawn((entity, tick) => { ... })` |
| Run once after delay | `world.setTimeout(() => { ... }, ticks)` |
| Run on interval | `world.setInterval(() => { ... }, ticks)` |
| Cancel event listener | `token.cancel()` |
| Check if active | `token.active()` |

### Data Persistence

| I want to... | Use this |
|-------------|----------|
| Read/write JSON data | `storage.getDataStorage("key")` |
| SQL query | `db.sql("SELECT ...")` |
| SQL write | `db.sql("INSERT INTO ...")` |

### HTTP Requests

| I want to... | Use this |
|-------------|----------|
| GET request | `http.fetch("https://...")` |
| POST JSON | `http.fetch(url, { method: "POST", headers, body })` |
| Parse JSON | `resp.json()` or `{ responseType: "json" }` |
| Read text | `resp.text()` |
| Set timeout | `http.fetch(url, { timeout: 5000 })` |

### Game Systems

| I want to... | Use this |
|-------------|----------|
| Create scoreboard | `world.addScoreboard("name")` |
| Set score | `world.setScore("player", "board", 10)` |
| Display scoreboard | `world.showScoreboard("sidebar", "name")` |
| Show BossBar | `world.showBossbar("id", "title", 0.5, "red")` |
| Create team | `world.createTeam("teamName", "color")` |
| Join team | `world.joinTeam(entity, "teamName")` |
| Set world border | `world.borderSize = 500` |
| Shrink border | `world.shrinkBorder(100, 60)` |
| Change time | `world.time = 6000` |
| Set weather | `world.rainDensity = 0` / `world.clearWeather()` |
| Change game rule | `world.setGameRule("keepInventory", true)` |

### Math Tools

| I want to... | Use this |
|-------------|----------|
| 3D coordinate | `new GameVector3(x, y, z)` |
| Vector math | `v.add(other)`, `v.scale(n)`, `v.length()` |
| Bounding box | `new GameBounds3(min, max)` |
| Color | `new GameRGBColor(r, g, b)` / `new GameRGBAColor(r, g, b, a)` |

### Cross-Script Messaging

| I want to... | Use this |
|-------------|----------|
| Send to another script | `world.sendMessage("projectName", data)` |
| Receive from other scripts | `world.onMessage((from, data) => { ... })` |

---

## Global Objects

| Object | Type | Description |
|--------|------|-------------|
| `world` | | World control, see [world_en.md](world_en.md) |
| `entity` | | Entity wrapper (from callbacks or `world.spawnEntity`), see [entity_en.md](entity_en.md) |
| `player` | | Player wrapper (via `entity.player`), see [player_en.md](player_en.md) |
| `voxels` | | Block operations, see [voxels_en.md](voxels_en.md) |
| `storage` | | Data persistence, see [storage_en.md](storage_en.md) |
| `db` | | SQLite database, see [database_en.md](database_en.md) |
| `http` | Client | HTTP requests, see [http_en.md](http_en.md) |
| `audio` | Client | Client sound, music, volume control, see [client_en.md](client_en.md) |
| `client` | Client | Client lifecycle, see [client_en.md](client_en.md) |
| `input` | Client | Client keyboard input, see [client_en.md](client_en.md) |
| `ui` | Client | Client screen UI, see [client_en.md](client_en.md) |
| `chat` | Client | Client chat send/receive, see [client_en.md](client_en.md) |
| `remoteChannel` | Client | Server↔client event channel, see [client_en.md](client_en.md) |
| `registries` | Server | Custom blocks, items & sounds (compiled mode), see [registries_en.md](registries_en.md) |
| `console` | | Console logging (`log`/`warn`/`error`/`debug`) |
| `GameVector3` | | 3D vector, see [math_en.md](math_en.md) |
| `GameBounds3` | | Bounding box, see [math_en.md](math_en.md) |
| `GameRGBColor` | | RGB color, see [math_en.md](math_en.md) |
| `GameRGBAColor` | | RGBA color, see [math_en.md](math_en.md) |
| `GameQuaternion` | | Quaternion, see [math_en.md](math_en.md) |

## API Legend

| Label | Meaning |
|-------|---------|
| ✅ **Box3 API** | Originates from the Box3 platform; naming and semantics match Box3 |
| ⬆ **MC Extension** | Not in original Box3; added using Minecraft-specific features |

## Detailed Document Index

| Document | Content |
|----------|---------|
| [world_en.md](world_en.md) | World state, events, scoreboard, bossbar, teams, border, particles, fireworks, lightning, sounds |
| [entity_en.md](entity_en.md) | Entity properties, AI, equipment, potion effects, pathfinding, tags, collisions |
| [player_en.md](player_en.md) | Inventory, messaging, flight, game mode, teleport, commands, XP |
| [voxels_en.md](voxels_en.md) | Block read/write, region fill, spawner control |
| [storage_en.md](storage_en.md) | Persistent data storage |
| [database_en.md](database_en.md) | SQLite database API |
| [http_en.md](http_en.md) | HTTP request API |
| [client_en.md](client_en.md) | Client scripts: lifecycle, keyboard, screen UI, chat, remoteChannel, client-side storage |
| [registries_en.md](registries_en.md) | Custom blocks, items & sounds (blocks.json, items.json, sounds.json, creativeTabs.json) |
| [math_en.md](math_en.md) | GameVector3, GameBounds3, GameRGBColor, GameRGBAColor, GameQuaternion |
| [commands_en.md](commands_en.md) | `/box3script` command reference |

## File Modules — TypeScript Build Pipeline

Projects created with `/box3script create` come with a complete TS build environment:

```
config/box3/script/mygame/
├── package.json          ← esbuild + Babel + @babel/preset-typescript
├── tsconfig.base.json    ← Shared TS compiler options
├── tsconfig.server.json  ← Server-side TS config
├── tsconfig.client.json  ← Client-side TS config
├── build.mjs             ← Babel TS→JS → esbuild bundle → dist/
├── types/
│   ├── shared.d.ts       ← Shared types (server & client)
│   ├── server/
│   │   ├── server.d.ts   ← Server entry point
│   │   ├── entity.d.ts
│   │   ├── player.d.ts
│   │   ├── world.d.ts
│   │   └── voxels.d.ts
│   └── client/
│       ├── client.d.ts   ← Client entry point
│       ├── audio.d.ts
│       ├── input.d.ts
│       ├── ui.d.ts
│       └── chat.d.ts
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

```
/box3script compile <project>
```

Outputs `<project>-<version>.jar` (metadata read from `package.json`: name, displayName, version, description, author, license, homepage, logoFile). Drop it into `mods/` and start the server.

See [full command reference →](commands_en.md#box3script-compile-project)

## Tick Conversion

| Duration | Ticks |
|----------|-------|
| 1 second | 20 |
| 5 seconds | 100 |
| 30 seconds | 600 |
| 1 minute | 1200 |
| 5 minutes | 6000 |

## Deep Dive

| Doc | Content |
|-----|---------|
| [Quick Start](../guide/getting-started_en.md) | Setup, first script, dev cycle, debugging, deployment |
| [Architecture](../guide/architecture_en.md) | Rhino engine, scopes, event callbacks, build pipeline, network |
| [JS vs Java](../guide/js-vs-java_en.md) | Box3JS scripting vs native Java modding comparison |

## Tutorials

Learn Box3JS from scratch with the tutorial series in `docs/tutorial/`:

| Tutorial | Content |
|----------|---------|
| [01-basics.md](../tutorial/01-basics.md) | From zero: first script, chat commands, timers |
| [02-player-items.md](../tutorial/02-player-items.md) | Player controls: teleport, items, potion effects, game modes |
| [03-events-entities.md](../tutorial/03-events-entities.md) | Events & entities: AI, combat, patrols |
| [04-advanced-systems.md](../tutorial/04-advanced-systems.md) | Advanced: scoreboard, BossBar, teams, world border |
| [05-examples.md](../tutorial/05-examples.md) | Real-world: PvP arena, effects, fireworks, wave mobs |
