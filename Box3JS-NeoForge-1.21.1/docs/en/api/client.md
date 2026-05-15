# client — Client-side API

Client scripts run locally on the player's Minecraft client. The entry file is `src/client/app.ts`, and the compiled output is `dist/client.js`.

## Client Globals

| Object | Type | Purpose | Docs |
|--------|------|---------|------|
| `client` | `GameClient` | Lifecycle callbacks, player info, fog | This page |
| `audio` | `GameAudio` | Sound & music playback, volume control | [audio.md](audio.md) |
| `input` | `GameInput` | Keyboard input detection, mouse events | [input.md](input.md) |
| `ui` | `GameUI` | On-screen text (ActionBar, titles, custom draw) | [ui.md](ui.md) |
| `chat` | `GameChat` | Send/receive chat, send commands | [chat.md](chat.md) |
| `gui` | `GameGUI` | Custom container GUI interface | [gui.md](gui.md) |
| `remoteChannel` | `RemoteChannel` | Client ↔ Server event communication | [remote-channel.md](remote-channel.md) |
| `storage` | `GameStorage` | Client-side persistent key-value storage | [storage.md](storage.md) |
| `db` | `GameDatabase` | Client-side SQLite database | [database.md](database.md) |
| `http` | `GameHttpAPI` | HTTP requests (sync/async) | [http.md](http.md) |

::: info
The client must have the Box3JS mod installed. Client scripts go in `src/client/`, server scripts in `src/server/`. The client type entry is `types/client/index.d.ts`; it does not include server APIs such as `world` / `voxels`.
:::

Client scripts cannot directly modify the server world. To change blocks, players, entities, or scoreboards, send an event to the server:

```ts
remoteChannel.sendServerEvent({ type: "requestTeleport" });
```

## client — Lifecycle

### client.onTick(callback)

Registers a callback invoked every client tick (20 times/sec). It receives no parameters and returns a `GameEventHandlerToken`; call `cancel()` to unsubscribe.

```js
const token = client.onTick(() => {
  // Per-frame logic
});

// token.cancel();
```

::: info Note
Server-side `world.onTick()` receives a `TickInfo` object. Client-side `client.onTick()` receives nothing.
:::

### client.getFPS()

Gets the current frames per second (FPS).

```js
var fps = client.getFPS();
console.log(`Current FPS: ${fps}`);
```

### client.getPlayer()

Gets local player information. Returns `null` if the player is not yet loaded.

```js
var player = client.getPlayer();
if (player) {
  console.log(`Player: ${player.name}, HP: ${player.health}/${player.maxHealth}`);
  console.log(`Position: ${player.position.x}, ${player.position.y}, ${player.position.z}`);
}
```

### client.getLookingAt()

Gets what the player's crosshair is currently pointing at. Returns `null` when not looking at anything.

```js
var target = client.getLookingAt();
if (target) {
  if (target.type === "entity") {
    console.log(`Looking at entity: ${target.entity.name}`);
  } else if (target.type === "block") {
    console.log(`Looking at block: ${target.blockPos.x}, ${target.blockPos.y}, ${target.blockPos.z}`);
  }
}
```

### client.getServerInfo()

Gets current server connection information. Returns `{ ip: "localhost", name: "Singleplayer", isLocal: true }` for singleplayer.

```js
var info = client.getServerInfo();
console.log(`Server: ${info.name} (${info.ip})`);
if (!info.isLocal) {
  console.log(`Players: ${info.playerCount}/${info.maxPlayers}`);
}
```

## Fog Control

The Box3JS client can override Minecraft's fog colour and distance, providing effects similar to Box3's `world.fogColor` / `world.maxFog`.

### client.getFogColor()

Gets the current custom fog colour. Returns `null` if not set.

```js
var color = client.getFogColor();
if (color) {
  console.log("Fog color: " + color.r + ", " + color.g + ", " + color.b);
}
```

### client.setFogColor(r, g, b)

Sets the fog colour (RGB 0-255).

| Parameter | Type   | Description    |
|-----------|--------|----------------|
| `r`       | number | Red (0-255)    |
| `g`       | number | Green (0-255)  |
| `b`       | number | Blue (0-255)   |

```js
// Red fog effect
client.setFogColor(255, 50, 50);
```

### client.setFogStartDistance(distance)

Sets the distance (in blocks) where fog begins. Fully transparent below this distance.

| Parameter  | Type   | Description               |
|------------|--------|---------------------------|
| `distance` | number | Fog start distance (blocks) |

```js
// Fog starts 10 blocks away
client.setFogStartDistance(10);
```

### client.setFogEndDistance(distance)

Sets the distance (in blocks) where fog becomes fully opaque, equivalent to Box3's `maxFog`.

| Parameter  | Type   | Description             |
|------------|--------|-------------------------|
| `distance` | number | Fog end distance (blocks) |

```js
// Fully obscured by fog beyond 50 blocks
client.setFogEndDistance(50);
```

### client.resetFog()

Resets fog to Minecraft's default behaviour.

```js
client.resetFog();
```

## Complete Client Example

```js
// src/client/app.ts

// Per-frame updates
client.onTick(() => {
  if (input.isKeyDown("space")) {
    // Space key is held
  }
});

// Key-triggered command
input.onKeyPress("g", () => {
  chat.sendCommand("gamemode creative");
});

// Welcome title
ui.showTitle("§aWelcome back", "§7Have fun", 10, 70, 20);

// Chat handler
chat.onMessage((message, sender, isSystem) => {
  if (message === "!info") {
    ui.showOverlay("§eServer: §f" + sender);
    return false;
  }
});

// Server communication
remoteChannel.sendServerEvent({ type: "clientLoaded" });

remoteChannel.onClientEvent((event) => {
  if (event.args.type === "alert") {
    audio.playSound("minecraft:block.note_block.pling", 1.0, 1.0);
    ui.showOverlay("§c" + event.args.message);
  }
});

console.log("[client] loaded!");
```

Client APIs are Box3JS-specific.
