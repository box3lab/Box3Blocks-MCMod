# client — Client-side API

Client scripts run locally on the player's Minecraft client. The entry file is `src/client/app.ts`, and the compiled output is `dist/client.js`. Client APIs handle local UI, input, audio, chat helpers, local storage, local HTTP/SQLite, and cross-side events.

Client scripts access APIs through these globals:

| Object | Type | Purpose |
|--------|------|---------|
| `audio` | `GameAudio` | Sound & music playback, volume control |
| `client` | `GameClient` | Lifecycle callbacks |
| `input` | `GameInput` | Keyboard input detection |
| `ui` | `GameUI` | On-screen text (ActionBar, titles) |
| `chat` | `GameChat` | Send/receive chat, send commands |
| `storage` | `GameStorage` | Client-side persistent key-value storage |
| `db` | `GameDatabase` | Client-side SQLite database |
| `http` | `GameHttpAPI` | HTTP requests (sync/async) |
| `gui` | `GameGUI` | Custom container GUI interface |
| `remoteChannel` | `RemoteChannel` | Client ↔ Server event communication |

> **Prerequisite:** The client must have the Box3JS mod installed. The server must enable the project's client script, which is automatically sent to connecting players.
> Client scripts go in `src/client/`, server scripts in `src/server/`. The client type entry is `types/client/index.d.ts`; it does not include server APIs such as `world` / `voxels`.

Client scripts cannot directly modify the server world. To change blocks, players, entities, or scoreboards, send an event to the server:

```ts
remoteChannel.sendServerEvent({ type: "requestTeleport" });
```

## audio — Sound Playback

### audio.playSound(path, volume, pitch)

Plays a sound effect (SoundSource.PLAYERS category).

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `path` | string | (required) | Sound ID, e.g. `"minecraft:block.note_block.pling"` |
| `volume` | number | `1.0` | Volume (0–1) |
| `pitch` | number | `1.0` | Pitch (0.5–2) |

```js
audio.playSound("minecraft:block.note_block.pling", 1.0, 1.0);
audio.playSound("minecraft:entity.experience_orb.pickup", 0.5, 1.5);
```

### audio.playMusic(path, volume, pitch)

Plays music (SoundSource.MUSIC category). Same parameters as `playSound`.

```js
audio.playMusic("minecraft:music.creative", 0.5, 1.0);
```

### audio.stopAll()

Stops all currently playing sounds and music.

```js
audio.stopAll();
```

### audio.getVolume(category)

Gets the volume of a specific audio category.

| Parameter | Type | Description |
|-----------|------|-------------|
| `category` | string | Category name, see list below |

```js
var musicVol = audio.getVolume("music"); // 0.0–1.0
```

### audio.setVolume(category, value)

Sets the volume of a specific audio category.

| Parameter | Type | Description |
|-----------|------|-------------|
| `category` | string | Category name |
| `value` | number | Volume (0–1) |

```js
audio.setVolume("music", 0.5);
audio.setVolume("player", 0.8);
```

### Audio Categories

| Category | Description |
|----------|-------------|
| `master` | Master volume |
| `music` | Music |
| `record` | Records/note blocks |
| `weather` | Weather (rain) |
| `block` | Blocks |
| `hostile` | Hostile mobs |
| `neutral` | Neutral mobs |
| `player` | Players |
| `ambient` | Ambient |
| `voice` | Voice |

## client — Lifecycle

### client.onTick(callback)

Registers a callback invoked every client tick (20 times/sec). No parameters, no return value.

```js
client.onTick(() => {
  // Per-frame logic
});
```

> **Note:** Server-side `world.onTick()` receives a `TickInfo` object. Client-side `client.onTick()` receives nothing.

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

## input — Keyboard Input

### input.isKeyDown(key)

Checks whether a key is currently held down.

| Parameter | Type | Description |
|-----------|------|-------------|
| `key` | string | Key name (lowercase), see key list below |

```js
if (input.isKeyDown("space")) {
  // Space key is held
}
```

### input.onKeyPress(key, callback)

Registers a callback fired once when the key is pressed. Returns `GameEventHandlerToken`; call `.cancel()` to unregister.

```js
var token = input.onKeyPress("f", () => {
  chat.sendCommand("fly");
});

// Unregister
token.cancel();
```

### input.getMouseX()

Gets the current mouse X position in screen pixels.

```js
var mx = input.getMouseX();
```

### input.getMouseY()

Gets the current mouse Y position in screen pixels.

```js
var my = input.getMouseY();
```

### input.onMouseClick(callback)

Registers a mouse button callback. Returns `GameEventHandlerToken`; call `.cancel()` to unregister.

Callback: `(button: number, action: number, x: number, y: number) => void`

| Parameter | Description |
|-----------|-------------|
| `button` | 0=left, 1=right, 2=middle |
| `action` | 0=release, 1=press, 2=repeat |
| `x` | Mouse X in screen pixels |
| `y` | Mouse Y in screen pixels |

```js
var token = input.onMouseClick((button, action, x, y) => {
  if (action === 1) { // pressed
    console.log(`Clicked button ${button} at (${x}, ${y})`);
  }
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

Displays text in the action bar (above the hotbar). Supports color codes (`§a`, `§b`, etc.).

```js
ui.showOverlay("§aWelcome to the server!");
```

### ui.showTitle(title, subtitle, fadeIn?, stay?, fadeOut?)

Displays a large centered screen title.

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

Displays text in the action bar (same as `showOverlay`).

```js
ui.showActionBar("§ePress F to use ability");
```

### ui.getScreenSize()

Gets the current game window and GUI-scaled dimensions.

```js
var size = ui.getScreenSize();
console.log(size.width, size.height);           // window pixels
console.log(size.scaledWidth, size.scaledHeight); // GUI-scaled
```

### ui.drawText(id, x, y, text, color?)

Draws custom text on screen (persists every frame until removed via `removeDrawText`).

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `id` | number | (required) | Text ID for later removal or update |
| `x` | number | (required) | X position (GUI-scaled coordinates) |
| `y` | number | (required) | Y position (GUI-scaled coordinates) |
| `text` | string | (required) | Text to display |
| `color` | GameRGBColor | white | Text colour |

Returns the text ID (same as the passed `id`). Reusing the same ID overwrites the previous entry.

```js
var textId = ui.drawText(1, 10, 10, "Hello, Box3JS!");
// Update position or content
ui.drawText(1, 10, 30, "Updated text", new GameRGBColor(1, 0, 0)); // red
```

### ui.removeDrawText(id)

Removes the drawn text with the given ID.

```js
ui.removeDrawText(1);
```

### ui.clearDrawTexts()

Clears all texts drawn via `drawText()`.

```js
ui.clearDrawTexts();
```

## chat — Chat Messages & Commands

### chat.sendMessage(text)

Sends a chat message to the server.

```js
chat.sendMessage("Hello everyone!");
```

### chat.sendCommand(cmd)

Sends a command to the server (equivalent to typing a `/` command in chat).

```js
chat.sendCommand("spawn");
chat.sendCommand("home");
```

### chat.onMessage(handler)

Registers a handler for incoming chat messages. Returns `GameEventHandlerToken`; call `.cancel()` to unregister.

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

## gui — Custom GUI

### gui.openGUI(config)

Opens a script-controlled custom container GUI (chest-like screen), returning a controller object.
The client automatically requests the server to create the container, and returns a `GuiController` for manipulating the GUI and listening to events.

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `config.title` | string | `"Container"` | Title |
| `config.rows` | number | `3` | Number of rows (1–6) |
| `config.slots` | object | `{}` | Initial items, format `{ [slot]: "itemId" }` |

```js
var ctrl = gui.openGUI({
  title: "§6Shop",
  rows: 3,
  slots: { 0: "minecraft:diamond", 4: "minecraft:emerald" },
});

// Set item
ctrl.setItem(1, "minecraft:gold_ingot", 5);

// Get item
var item = ctrl.getItem(0);
console.log(item.id, item.count); // minecraft:diamond, 1

// Slot click listener
var clickToken = ctrl.onSlotClick((slot) => {
  console.log("Clicked slot:", slot);
});

// Close listener
var closeToken = ctrl.onClose(() => {
  console.log("GUI closed");
});

// Close the GUI
ctrl.close();
```

### GuiController Methods

| Method | Description |
|--------|-------------|
| `setItem(slot, itemId, count?)` | Sets the item in the given slot |
| `getItem(slot)` | Gets the item in the given slot, returns `{ id, count }` |
| `onSlotClick(callback)` | Registers a slot click callback and returns `GameEventHandlerToken`, `callback(slot: number)` |
| `onClose(callback)` | Registers a close callback and returns `GameEventHandlerToken`, `callback()` |
| `close()` | Closes the GUI |

## remoteChannel — Client ↔ Server Communication

The client uses `remoteChannel` for bidirectional event communication with the server. Event data is JSON-serialized.

### remoteChannel.sendServerEvent(event)

Sends an event to the server. `event` is any JSON-serializable value.

```js
remoteChannel.sendServerEvent({
  type: "clientReady",
  timestamp: Date.now(),
});
```

### remoteChannel.onClientEvent(handler)

Registers a handler for remote events sent from the server. Returns `GameEventHandlerToken`.

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
