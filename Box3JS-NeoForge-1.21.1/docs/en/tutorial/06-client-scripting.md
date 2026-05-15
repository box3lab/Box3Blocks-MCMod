---
---

# Tutorial 6: Client-Side Scripting

This tutorial covers all 9 global objects available in Box3JS client scripts: lifecycle, keyboard input, screen UI, chat control, sounds/music, local storage, SQLite, HTTP requests, and bidirectional communication.

## Prerequisites

- Players must have the Box3JS client mod installed
- The server must have the project enabled; client scripts are distributed automatically
- Client code goes in `src/client/app.ts`

## 6.1 Server vs Client Scripts

| Feature | Server Script | Client Script |
|---------|--------------|---------------|
| Runs on | Server | Player's local machine |
| Scope | All players share one instance | Each player has their own |
| World modification | ✅ Can modify world | ❌ Read-only |
| Keyboard input | ❌ Not available | ✅ Detect key presses |
| Screen UI | ❌ Title/ActionBar only | ✅ Full-screen text overlay |
| Sound | Global or per-player | Local only (headphones) |
| Storage | Server-side unified | Client-side local & independent |

## 6.2 Overview of the Full Example

The colorzone project (`src/client/app.ts`) contains a complete client-side demo covering:

| System | Purpose | Key/Command |
|--------|---------|-------------|
| storage | Settings, notes, counters | `!settings` `!notes` `!note` |
| db | Mob cache, favorites | `!mob` `!fav` |
| http | Sync/async GET/POST | `!sync` / F8-F10 |
| remoteChannel | Server↔client communication | `!ping` `!broadcast` |
| input | Keyboard shortcuts | F6-F12, C, V |
| ui | On-screen text | F6 to show settings |
| chat | Chat commands | `!fav` `!mob` |
| audio | Custom sounds | V key |
| fog | Fog colour and distance control | — |

## 6.3 client — Lifecycle

`client.onTick(callback)` is the client's "heartbeat", running 20 times per second. Useful for periodic checks:

```js
let tickCount = 0;
client.onTick(() => {
  tickCount++;
  // Log once every 5 seconds
  if (tickCount % 100 === 0) {
    console.log(`[client] Running: ${tickCount / 20}s`);
  }
});
```

Like other event APIs, `client.onTick()` returns a `GameEventHandlerToken`; call `token.cancel()` when you no longer need the listener.

**Performance tip:** Client onTick also runs on the main thread. Avoid tight loops; use modulo to reduce the effective execution rate.

## 6.4 input — Keyboard Input

### Detecting Key Presses

```js
// Single callback on press
input.onKeyPress("f", () => {
  ui.showOverlay("§aYou pressed F!");
});

// Check if key is held down (run inside onTick for continuous detection)
client.onTick(() => {
  if (input.isKeyDown("space")) {
    // Spacebar is held
  }
});
```

Supported key names: `a`-`z`, `0`-`9`, `f1`-`f12`, `space`, `shift`, `ctrl`, `alt`, `tab`, `enter`, `backspace`, `escape`, `up`, `down`, `left`, `right`

## 6.5 ui — Screen UI

```js
// Overlay text (above the hotbar)
ui.showOverlay("§eA tip message");

// Screen title (large, with fade in/out)
ui.showTitle("§6§lMain Title", "§7Subtitle");
// With timing: (title, subtitle, fadeInTicks, stayTicks, fadeOutTicks)
ui.showTitle("§c§lWarning", "§7Border shrinking in 10 seconds", 5, 40, 10);

// Clear titles
ui.clearTitle();
```

**Server vs client comparison:** `player.title()` and `player.actionBar()` are sent from the server to the player. `ui.showTitle()` and `ui.showOverlay()` are displayed locally on the client. Client-side UI is not affected by network latency.

## 6.6 chat — Send & Receive

```js
// Receive chat messages (including system messages)
chat.onMessage((message: string, sender: string, isSystem: boolean) => {
  if (isSystem) return;  // Ignore system messages

  console.log(`[chat] ${sender}: ${message}`);

  // Client-side local commands (don't affect the server)
  if (message === "!sync") {
    syncGet();
    return false;  // ★ Return false to suppress display in chat
  }
  return true;
});

// Send a chat message
chat.sendMessage("Hello everyone!");

// Send a command (equivalent to typing /command in chat)
chat.sendCommand("box3script");
```

## 6.7 audio — Sound & Music

```js
// Play sound: (path, volume, pitch)
audio.playSound("minecraft:block.note_block.pling", 1.0, 1.0);
audio.playSound("minecraft:entity.experience_orb.pickup", 0.5, 1.5);

// Play music
audio.playMusic("minecraft:music.creative", 0.5, 1.0);

// Stop all sounds
audio.stopAll();

// Volume control
audio.setVolume("music", 0.5);       // Set music volume
audio.setVolume("player", 0.8);      // Set player sound volume
const musicVol = audio.getVolume("music");  // Read current volume

// Custom sounds (requires registries)
audio.playSound("colorzone:victory_fanfare", 1.0, 1.0);
```

## 6.8 fog — Fog Control

Override Minecraft's fog colour and render distance:

```js
// Set fog colour (RGB 0-255)
client.setFogColor(255, 100, 50);

// Set fog distance (in blocks)
client.setFogStartDistance(10);     // fog begins beyond 10 blocks
client.setFogEndDistance(50);       // fully obscured beyond 50 blocks

// Read current fog colour
const color = client.getFogColor(); // returns GameRGBColor or null

// Restore Minecraft default fog
client.resetFog();
```

::: warning
Fog changes take effect locally on each client. Use `remoteChannel` to let the server trigger fog changes on clients, enabling server-controlled weather effects.
:::

## 6.9 storage — Client-Side Local Storage

Client-side `storage` uses the same API as the server but stores data locally on each player's machine:

```js
// Pattern A: Store an entire object under one key (recommended, strongly typed)
type Settings = {
  theme: string;
  overlayEnabled: boolean;
  fontSize: number;
};

const settings = storage.getDataStorage<Settings>("client-settings");

// Initialize defaults
if (settings.get("main") === null) {
  settings.set("main", {
    theme: "dark",
    overlayEnabled: true,
    fontSize: 14,
  });
}

// Read
const cfg = settings.get("main") as Settings;
console.log(cfg.theme);

// Atomic update (read → modify → write back)
settings.update("main", (prev: Settings) => {
  prev.overlayEnabled = !prev.overlayEnabled;
  return prev;
});

// Pattern B: Individual fields (simple key-value pairs)
const prefs = storage.getDataStorage("prefs");
prefs.set("soundVolume", 0.8);
prefs.set("showTips", true);

// Pattern C: Auto-increment counter
const visitCount = storage.getDataStorage<number>("visit-counter");
const count = visitCount.increment("total", 1);

// Pattern D: Notes system (structured data + pagination)
type Note = { title: string; content: string; createdAt: number };
const notes = storage.getDataStorage<Note>("notes");

notes.set("welcome", {
  title: "Welcome",
  content: "Box3JS client demo is ready!",
  createdAt: Date.now(),
});

// Paginated listing
const page = notes.list({ pageSize: 10, ascending: false });
const entries = page.getCurrentPage();
```

## 6.10 db — Client-Side SQLite

The client also supports SQLite (requires `minecraft-sqlite-jdbc` mod):

```js
// Check if database is available
if (!db.isAvailable()) {
  console.warn("SQLite driver not installed");
  return;
}

// Create tables
db.sql(
  "CREATE TABLE IF NOT EXISTS mob_cache (name TEXT PRIMARY KEY, health REAL, type TEXT)"
);

// Insert data
db.sql(
  "INSERT OR REPLACE INTO mob_cache (name, health, type) VALUES (?, ?, ?)",
  "Zombie", 20, "undead"
);

// Query
const allMobs = db.sql("SELECT * FROM mob_cache ORDER BY name");
console.log(`Found ${allMobs.rowCount} mobs`);

// Iterate results
for (let i = 0; i < allMobs.rowCount; i++) {
  const row = allMobs.rows[i];
  console.log(`${row.name} (HP: ${row.health})`);
}

// Tagged template style (SQL injection safe)
function searchMobs(keyword: string): void {
  const result = db.sql(
    ["SELECT * FROM mob_cache WHERE name LIKE '%", "%'"],
    keyword,
  );
  if (result.rowCount > 0) {
    const names: string[] = [];
    for (let i = 0; i < result.rowCount; i++) {
      names.push(result.rows[i].name);
    }
    ui.showOverlay(`§aMobs: §f${names.join(", ")}`);
  }
}
```

::: warning
When `minecraft-sqlite-jdbc` is not installed, `db.isAvailable()` returns `false` and all SQL calls silently return empty results.
:::

## 6.11 http — Client HTTP Requests

```js
// Synchronous GET
const resp = http.fetch("https://httpbin.org/get", {
  method: "GET",
  timeout: 5000,
  responseType: "json",
});

if (resp.ok) {
  console.log(JSON.stringify(resp.data));
  ui.showOverlay(`§aOK — status=${resp.status}`);
} else {
  ui.showOverlay(`§cHTTP ${resp.status} — ${resp.errorMessage}`);
}

// Async GET (non-blocking, doesn't freeze the game)
http.fetch("https://httpbin.org/delay/2", {
  method: "GET",
  timeout: 8000,
  responseType: "json",
  async: true,
  onResponse: (resp) => {
    console.log(`Async OK — status=${resp.status}`);
    ui.showOverlay(`§aAsync response received`);
  },
  onError: (err) => {
    console.error(`Async error: ${err}`);
  },
});

// POST JSON
http.fetch("https://httpbin.org/post", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ source: "box3js-client", timestamp: Date.now() }),
  timeout: 5000,
  responseType: "json",
});
```

## 6.12 remoteChannel — Bidirectional Communication

This is the most powerful client scripting feature: server and client can send events to each other.

### Server → Client

```js
// === Server-side ===
// Send to a specific player
remoteChannel.sendClientEvent(entity, {
  type: "ping",
  message: "Hello from server!",
  serverTick: world.currentTick(),
});

// Broadcast to all players
remoteChannel.broadcastClientEvent({
  type: "broadcast",
  message: "Server announcement!",
});
```

```js
// === Client-side ===
remoteChannel.onClientEvent((event) => {
  const { tick, args } = event;

  switch (args.type) {
    case "ping": {
      console.log(`Ping (tick ${tick}): ${args.message}`);
      // Reply back to server
      remoteChannel.sendServerEvent({
        type: "pong",
        clientTick: tick,
        timestamp: Date.now(),
      });
      break;
    }

    case "broadcast":
      ui.showOverlay(`§e📢 ${args.message}`);
      break;
  }
});
```

### Client → Server

```js
// === Client-side ===
remoteChannel.sendServerEvent({
  type: "clientReady",
  clientVersion: "1.0.0",
});
```

```js
// === Server-side ===
remoteChannel.onServerEvent((event) => {
  const { entity, tick, args } = event;
  const name = entity.player.name;

  console.log(`[server] Received from ${name}: ${JSON.stringify(args)}`);

  if (args.type === "clientReady") {
    console.log(`[server] ${name}'s client has Box3JS installed!`);
    // Send welcome message back
    remoteChannel.sendClientEvent(entity, {
      type: "welcome",
      message: `Welcome, ${name}!`,
    });
  }
});
```

### Detecting Client Compatibility

No manual detection is needed. `remoteChannel.sendClientEvent()` uses optional payloads — players without the Box3JS client mod will silently ignore them without errors or disconnects. You can safely send events to all players.

### Data Format

::: warning
Data sent across the network must be JSON-serializable (string, number, boolean, null, plain objects, arrays). You cannot send functions, Java objects, or `GameVector3`.
:::

## 6.13 Practical Example: Custom HUD Status Bar

Combining input, ui, remoteChannel, and storage to create a custom HUD:

```js
// ── Settings management ──
type HUDConfig = {
  showFPS: boolean;
  showCoords: boolean;
  showPing: boolean;
};

const hudConfig = storage.getDataStorage<HUDConfig>("hud-config");
if (hudConfig.get("main") === null) {
  hudConfig.set("main", { showFPS: true, showCoords: true, showPing: true });
}

// ── Toggle switches ──
input.onKeyPress("f6", () => {
  hudConfig.update("main", (prev: HUDConfig) => {
    prev.showCoords = !prev.showCoords;
    return prev;
  });
  const cfg = hudConfig.get("main") as HUDConfig;
  ui.showOverlay(`Coords: ${cfg.showCoords ? "§aON" : "§cOFF"}`);
});

input.onKeyPress("f7", () => {
  hudConfig.update("main", (prev: HUDConfig) => {
    prev.showFPS = !prev.showFPS;
    return prev;
  });
});

// ── Refresh HUD every 2 seconds ──
let lastPing = 0;
let frameCount = 0;

client.onTick(() => {
  frameCount++;
  if (frameCount % 40 !== 0) return;  // Update every 2 seconds

  const cfg = hudConfig.get("main") as HUDConfig;
  const lines: string[] = [];

  if (cfg.showFPS) lines.push(`§fFPS: §a${Math.round(frameCount / 2)}`);
  if (cfg.showCoords) {
    // Request position from server via remoteChannel
    remoteChannel.sendServerEvent({ type: "requestPosition" });
  }
  if (cfg.showPing && lastPing > 0) lines.push(`§fPing: §e${lastPing}ms`);

  if (lines.length > 0) ui.showOverlay(lines.join(" §7| "));
});

// ── Receive server response ──
remoteChannel.onClientEvent((event) => {
  if (event.args.type === "position") {
    // Position data sent back from server
  }
});

// ── Startup ──
ui.showTitle("§6Custom HUD Active", "§7F6=Coords F7=FPS", 10, 40, 10);
console.log("[HUD] Client HUD demo loaded");
```

## 6.14 Debugging Client Scripts

Client script `console.log` output goes to the **client log** (not the server log). Check your Minecraft launcher or logs directory.

Troubleshooting order:
1. Verify Box3JS mod is installed on the client
2. Check that `dist/client.js` was generated (`npm run build`)
3. Run `/box3script status` on the server to confirm client scripts are enabled
4. Check the client log file

## Next Steps

[API Reference →](../api/client.md) Complete client API docs · [Tutorial 1](01-basics.md) Back to basics
