---
---

# Box3JS Tutorials

## Learning Path

```text
Beginner                  Intermediate              Advanced
│                          │                          │
│ Tutorial 1: Basics       │ Tutorial 3: Events       │ Tutorial 5: Mini-Games
│ - Create project         │ - All event callbacks    │ - Full PvP arena
│ - Chat commands          │ - Spawn entities / AI    │ - Particles / fireworks
│ - Timers                 │ - Collision detection    │ - Wave spawning
│ - Message types          │                          │
│                          │ Tutorial 4: Systems      │
│ Tutorial 2: Players      │ - Scoreboard / BossBar   │
│ - Teleport / flight      │ - Teams / world border   │
│ - Items / enchants       │ - Cross-script comms     │
│ - Potions / game modes   │                          │
└──────────┬───────────────┴──────────────────────────┘
           │
           ▼
    Want to go deeper?
    → [Architecture](../guide/architecture.md)
    → [JS vs Java](../guide/js-vs-java.md)
```

## Tutorial List

| # | Tutorial | You'll learn |
|---|---------|-------------|
| 1 | [From Zero](01-basics.md) | Create project → build → first script → chat commands → timers |
| 2 | [Players & Items](02-player-items.md) | Teleport, flight, give items, enchantments, potion effects, game modes |
| 3 | [Events & Entities](03-events-entities.md) | All event callbacks, spawn entities, AI control, patrol guards, collision |
| 4 | [Advanced Systems](04-advanced-systems.md) | Scoreboards, BossBars, teams, world border, cross-script messaging |
| 5 | [Real Mini-Games](05-examples.md) | Full PvP arena, particle effects, fireworks, wave spawning, home TP |
| 6 | [Client-Side Scripting](06-client-scripting.md) | Keyboard input, screen UI, sound/music, local storage, SQLite, HTTP, remoteChannel |

## Prerequisites

- **Language:** JavaScript/TypeScript. If you know JS, just write `.ts` files as JS.
- **Environment:** All server-side code runs on the server; players need nothing installed. Client scripts require the Box3JS client mod.
- **Hot Reload:** Edit code → `npm run build` → `/box3script reload` — no server restart needed.
- **Deployment:** When done, `/box3script compile` packages your script into a standalone JAR for `mods/`.
- **API Lookup:** Stuck on "which API does X"? Check the [API Task Reference](../api/README.md).

## Quick Example

Just want to see what Box3JS looks like?

```js
// app.ts — chat commands + periodic broadcast
world.onChat((entity, message) => {
  if (message === "!hello") {
    entity.player.directMessage("Hello, " + entity.player.name + "!");
    return false;
  }
  return true;
});

setInterval(() => {
  world.say("Players online: " + world.querySelectorAll("*").length);
}, 6000);
```

`npm run build` → `/box3script start <project>` and you're running.

## After the Tutorials

| I want to... | Read this |
|-------------|----------|
| Look up a specific API | [API Reference](../api/README.md) |
| Understand Box3JS internals | [Architecture](../guide/architecture.md) |
| Ship my script as a standalone mod | [Quick Start - Deployment](../guide/getting-started.md#deployment) |
| Register custom blocks/items/sounds | [registries API](../api/registries.md) |
| Write client scripts (UI/input/audio) | [client API](../api/client.md) |
| Decide Box3JS vs Java modding | [JS vs Java](../guide/js-vs-java.md) |

## Full API Docs

| Doc | Description |
|-----|-------------|
| [world](../api/world.md) | World state, events, particles, fireworks, sound |
| [entity](../api/entity.md) | Entity properties, AI, equipment, effects |
| [player](../api/player.md) | Inventory, messages, flight, teleport |
| [voxels](../api/voxels.md) | Block read/write, region fill |
| [storage](../api/storage.md) | JSON data persistence |
| [database](../api/database.md) | SQLite database |
| [http](../api/http.md) | HTTP network requests |
| [client](../api/client.md) | Client scripts (UI/input/chat/audio) |
| [registries](../api/registries.md) | Custom blocks/items/sounds |
| [math](../api/math.md) | GameVector3, Color, Quaternion |
| [commands](../api/commands.md) | `/box3script` command reference |
