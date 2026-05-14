# server — Server-side API Overview

Server scripts run on the Minecraft server thread. The entry file is `src/server/app.ts`, and the compiled output is `dist/server.js`. Server APIs handle world state, entities and players, block read/write, event callbacks, persistence, HTTP, and server-to-client event delivery.

> Client UI, keyboard input, local sounds, and local GUI are not server APIs. See [client_en.md](client_en.md).

## Server Globals

| Object | Type | Runtime | Description |
|--------|------|---------|-------------|
| `world` | `GameWorld` | Server | World state, events, entity spawning, scoreboards, bossbars, teams, border, particles, sounds |
| `voxels` | `GameVoxels` | Server | Block read/write, region fill, block ID/name conversion, spawner control |
| `storage` | `GameStorage` | Server/Client | JSON persistence. Server data is stored under `config/box3/storage/<project>/` |
| `db` | `GameDatabase` | Server/Client | SQLite queries. Server DB is `config/box3/data/<project>.db` |
| `http` | `GameHttpAPI` | Server/Client | HTTP requests, synchronous or callback-style async |
| `remoteChannel` | `RemoteChannel` | Both | Send client events from the server, or receive client events |
| `registries` | `GameRegistries \| undefined` | Compiled server JAR | Custom block/item/sound lookup in `/box3script compile` outputs |
| `console` | `GameConsole` | Both | `log` / `debug` / `warn` / `error` logging |

Entities and players are not standalone globals. They usually come from event callbacks, queries, or spawn results:

```ts
world.onPlayerJoin((entity) => {
  const player = entity.player;
  player.directMessage("Welcome, " + player.name);
});
```

## Entry Point And Types

Server code lives under `src/server/` and uses only the server type entry:

```text
src/server/app.ts
types/server/index.d.ts
```

`tsconfig.server.json` does not include `types/client/index.d.ts`, so direct use of `client`, `audio`, `input`, `ui`, `chat`, or `gui` in server code is a TypeScript error. For cross-side behavior, send events to the client script through `remoteChannel`.

```ts
remoteChannel.sendClientEvent(entity, {
  type: "showToast",
  text: "Quest complete",
});
```

## World And Events

`world` is the main server scripting entry point.

| Capability | API |
|------------|-----|
| Current project name | `world.projectName()` |
| Current tick | `world.currentTick()` |
| Server identifier/MOTD | `world.serverId` |
| Broadcast chat | `world.say(text)` |
| Every-tick callback | `world.onTick(handler)` |
| Player join/leave | `world.onPlayerJoin(handler)` / `world.onPlayerLeave(handler)` |
| Chat handling | `world.onChat(handler)` |
| Entity interaction | `world.onInteract(handler)` |
| Block interaction | `world.onBlockActivate(handler)` |
| Block break/place | `world.onVoxelDestroy(handler)` / `world.onBlockPlace(handler)` |
| Timers | `setTimeout(fn, ticks)` / `setInterval(fn, ticks)` |

```ts
world.onChat((entity, message) => {
  if (message === "!tick") {
    entity.player.directMessage("tick = " + world.currentTick());
    return false;
  }
  return true;
});
```

Event registration returns a `GameEventHandlerToken`. Long-running systems should keep the token and cancel it when no longer needed:

```ts
const token = world.onTick(() => {
  // ...
});

token.cancel();
```

## Players And Entities

`GameEntity` represents a player or mob. `GamePlayerEntity` is the narrowed player-entity type. Check `entity.isPlayer()` before accessing `entity.player` when the value may be non-player.

```ts
const target = world.querySelector("#some-uuid");
if (target && target.isPlayer()) {
  target.player.actionBar("You were selected");
}
```

Common entity APIs:

| Capability | API |
|------------|-----|
| Position/velocity | `entity.position` / `entity.velocity` |
| Health | `entity.hp` / `entity.maxHp` |
| Tags | `entity.addTag()` / `entity.removeTag()` / `entity.hasTag()` |
| Equipment | `entity.setEquipment(slot, itemId)` |
| Effects | `entity.addEffect()` / `entity.clearEffects()` |
| AI and navigation | `entity.setAI()` / `entity.navigateTo()` |

Common player APIs:

| Capability | API |
|------------|-----|
| Private message/action bar/title | `player.directMessage()` / `player.actionBar()` / `player.title()` |
| Teleport | `player.teleport(pos)` |
| Inventory | `player.giveItem()` / `player.clearInventory()` / `player.getHeldItem()` |
| Game mode | `player.gameMode` |
| Flight | `player.canFly` / `player.flying` |
| XP/hunger | `player.xp` / `player.food` / `player.saturation` |
| Commands | `player.runCommand(cmd)` |

## Blocks And Voxels

`voxels` handles block read/write. Prefer namespaced IDs such as `"minecraft:stone"`; shorthand like `"stone"` is also resolved when possible.

```ts
const pos = new GameVector3(0, 80, 0);
voxels.setVoxel(pos, "minecraft:diamond_block");

const name = voxels.getVoxelName(pos);
world.say("block = " + name);
```

Region operations may modify many blocks at once; keep bounds controlled and avoid doing large fills every tick:

```ts
voxels.fillVoxel(0, 70, 0, 10, 75, 10, "minecraft:glass");
```

## Server Data

Use `storage` for small JSON-shaped state, config, and cached leaderboards. Use `db` for structured tables and complex queries.

```ts
const scores = storage.getDataStorage<number>("scores");
scores.increment("alice", 1);

const rows = db.sql<{ name: string; score: number }>(
  "SELECT name, score FROM scores ORDER BY score DESC LIMIT ?",
  10,
);
```

Server storage also has shared cross-project namespaces:

```ts
const globalConfig = storage.getGroupStorage("config");
globalConfig.set("season", "spring");
```

## Cross-Side Events

Use `remoteChannel` to communicate with client scripts. Packets are optional — vanilla clients silently ignore them, no manual check needed:

```ts
world.onPlayerJoin((entity) => {
  remoteChannel.sendClientEvent(entity, {
    type: "welcome",
    text: "Welcome to the server",
  });
});
```

Broadcast a client event to every player:

```ts
remoteChannel.broadcastClientEvent({
  type: "serverNotice",
  text: "A server event was triggered",
});
```

Receive client events:

```ts
remoteChannel.onServerEvent<{ type: string; key?: string }>((event) => {
  if (event.args.type === "hotkey") {
    event.entity.player.actionBar("key: " + event.args.key);
  }
});
```

Event payloads must be JSON-serializable. Do not send Java objects, functions, or cyclic objects.

## Custom Registries

`registries` is only available inside standalone JARs produced by `/box3script compile`. It is `undefined` in interpreted mode.

```ts
if (registries) {
  const block = registries.getBlock("rainbow_cube");
  if (block) {
    player.giveItem(block.itemId, 1);
  }
}
```

Custom content files:

| File | Content |
|------|---------|
| `registries/blocks.json` | Block definitions |
| `registries/items.json` | Items, food, tools, armor |
| `registries/sounds.json` | Sound definitions |
| `registries/creativeTabs.json` | Creative mode tabs |

## Server API Index

| Document | Content |
|----------|---------|
| [world_en.md](world_en.md) | World state, events, scoreboard, bossbar, teams, border, particles, sounds |
| [entity_en.md](entity_en.md) | Entity properties, AI, equipment, effects, navigation, tags |
| [player_en.md](player_en.md) | Player inventory, messaging, teleport, flight, game mode, XP |
| [voxels_en.md](voxels_en.md) | Block read/write, region fill, spawners |
| [storage_en.md](storage_en.md) | JSON persistence |
| [database_en.md](database_en.md) | SQLite database |
| [http_en.md](http_en.md) | HTTP requests |
| [registries_en.md](registries_en.md) | Custom registries and compiled JAR mode |
| [math_en.md](math_en.md) | Vectors, bounds, colors, quaternions |

## Recommended Style

- Keep server files focused on server APIs: world, entities, players, blocks, data, network.
- Put client UI/input logic in `src/client/app.ts`, fed by server state through `remoteChannel`.
- Keep tokens for long-lived event listeners and call `cancel()` when disabling a game mode or subsystem.
- Rate-limit large `voxels.fillVoxel()` calls, mass entity spawning, and synchronous HTTP requests to avoid blocking server ticks.
- Give storage namespaces explicit names, such as `storage.getDataStorage("arena/scores")` or `storage.getGroupStorage("global/season")`.
