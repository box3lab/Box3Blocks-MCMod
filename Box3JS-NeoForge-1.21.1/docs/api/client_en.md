# client — Client-side API

Client scripts run locally on the player's Minecraft client and are accessed through four globals:

| Object | Type | Purpose |
|--------|------|---------|
| `client` | `GameClient` | Lifecycle callbacks, sound playback, command sending |
| `input` | `GameInput` | Keyboard input detection |
| `ui` | `GameUI` | On-screen text (ActionBar, titles) |
| `chat` | `GameChat` | Send and receive chat messages |
| `storage` | `GameStorage` | Client-side persistent key-value storage |
| `db` | `GameDatabase` | Client-side SQLite database |
| `http` | `GameHttpAPI` | HTTP requests (sync/async) |
| `remoteChannel` | `RemoteChannel` | Client ↔ Server event communication |

> **Prerequisite:** The client must have the Box3JS mod installed. The server must enable the project's client script, which is automatically sent to connecting players.
> Client scripts go in `src/client/`, server scripts in `src/server/`.

## client — Lifecycle & Server Interaction

### client.onTick(callback)

🆕 MC Extension | Registers a callback invoked every client tick (20 times/sec). No parameters, no return value.

```js
client.onTick(() => {
  // Per-frame logic
});
```

> **Note:** Server-side `world.onTick()` receives a `TickInfo` object. Client-side `client.onTick()` receives nothing.

### client.playSound(path, volume, pitch)

🆕 MC Extension | Plays a sound on the client.

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `path` | string | (required) | Sound ID, e.g. `"minecraft:block.note_block.pling"` |
| `volume` | number | `1.0` | Volume (0–1) |
| `pitch` | number | `1.0` | Pitch (0.5–2) |

```js
client.playSound("minecraft:block.note_block.pling", 1.0, 1.0);
client.playSound("minecraft:entity.experience_orb.pickup", 0.5, 1.5);
```

### client.sendCommand(cmd)

🆕 MC Extension | Sends a command to the server (equivalent to typing a `/` command in chat).

```js
client.sendCommand("spawn");
client.sendCommand("home");
```

## input — Keyboard Input

### input.isKeyDown(key)

🆕 MC Extension | Checks whether a key is currently held down.

| Parameter | Type | Description |
|-----------|------|-------------|
| `key` | string | Key name (lowercase), see key list below |

```js
if (input.isKeyDown("space")) {
  // Space key is held
}
```

### input.onKeyPress(key, callback)

🆕 MC Extension | Registers a callback fired once when the key is pressed. Returns `GameEventHandlerToken`; call `.cancel()` to unregister.

```js
var token = input.onKeyPress("f", () => {
  client.sendCommand("fly");
});

// Unregister
token.cancel();
```

### Supported Key Names

| Category | Keys |
|----------|------|
| Letters | `a`–`z` |
| Digits | `0`–`9` |
| Function keys | `f1`–`f12` |
| Arrow keys | `up`, `down`, `left`, `right` |
| Special keys | `space`, `enter`, `escape`, `tab`, `backspace`, `delete` |
| Modifiers | `left_shift`, `right_shift`, `left_ctrl`, `right_ctrl`, `left_alt`, `right_alt` |

## ui — Screen UI

### ui.showOverlay(text)

🆕 MC Extension | Displays text in the action bar (above the hotbar). Supports color codes (`§a`, `§b`, etc.).

```js
ui.showOverlay("§aWelcome to the server!");
```

### ui.showTitle(title, subtitle, fadeIn?, stay?, fadeOut?)

🆕 MC Extension | Displays a large centered screen title.

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `title` | string | (required) | Main title |
| `subtitle` | string | (required) | Subtitle |
| `fadeIn` | number | `10` | Fade-in ticks |
| `stay` | number | `70` | Stay ticks |
| `fadeOut` | number | `20` | Fade-out ticks |

```js
ui.showTitle("Boss Incoming!", "Get ready", 10, 70, 20);
ui.showTitle("§cGame Over", "§7Try again");
```

### ui.showActionBar(text)

🆕 MC Extension | Displays text in the action bar (same as `showOverlay`).

```js
ui.showActionBar("§ePress F to use ability");
```

## chat — Chat Messages

### chat.sendMessage(text)

🆕 MC Extension | Sends a chat message to the server.

```js
chat.sendMessage("Hello everyone!");
```

### chat.onMessage(handler)

🆕 MC Extension | Registers a handler for incoming chat messages. Returns `GameEventHandlerToken`; call `.cancel()` to unregister.

Callback: `(message: string, sender: string, isSystem: boolean) => boolean | void`

Return `false` to suppress the message from appearing in chat.

```js
var token = chat.onMessage((message, sender, isSystem) => {
  console.log(`[chat] ${sender}: ${message}`);

  if (message.includes("filtered_word")) {
    return false; // Suppress this message
  }
});

// Unregister
token.cancel();
```

## remoteChannel — Client ↔ Server Communication

The client uses `remoteChannel` for bidirectional event communication with the server. Event data is JSON-serialized.

### remoteChannel.sendServerEvent(event)

🆕 MC Extension | Sends an event to the server. `event` is any JSON-serializable value.

```js
remoteChannel.sendServerEvent({
  type: "clientReady",
  timestamp: Date.now(),
});
```

### remoteChannel.onClientEvent(handler)

🆕 MC Extension | Registers a handler for remote events sent from the server. Returns `GameEventHandlerToken`.

Callback: `(event: { tick: number, args: T }) => void`

```js
remoteChannel.onClientEvent((event) => {
  const { tick, args } = event;

  switch (args.type) {
    case "ping":
      console.log(`[client] Ping: ${args.message}`);
      remoteChannel.sendServerEvent({ type: "pong" });
      break;
    case "notify":
      ui.showOverlay(`§b${args.message}`);
      break;
  }
});
```

> Server-side equivalents: `remoteChannel.sendClientEvent()` / `broadcastClientEvent()` / `onServerEvent()`.
> See type declarations in `server.d.ts`.

## storage — Client-side Storage

The client has its own `storage`, saving data locally under `.minecraft/config/box3/data/<project>/`. The API is identical to the server-side `storage`:

```js
var store = storage.getDataStorage("settings");
store.set("volume", 0.8);
var volume = store.get("volume"); // 0.8
```

Full API reference: [storage_en.md](storage_en.md).

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
  client.sendCommand("gamemode creative");
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
    client.playSound("minecraft:block.note_block.pling", 1.0, 1.0);
    ui.showOverlay("§c" + event.args.message);
  }
});

console.log("[client] loaded!");
```

All 🆕 MC Extension (client APIs are Box3JS-specific, not from the Box3 platform).
