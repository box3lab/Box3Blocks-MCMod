# Box3JS Tutorials

Learn Box3JS scripting from scratch. Each tutorial takes 10–15 minutes and includes complete runnable code.

## Learning Path

```
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
    → [Architecture](../guide/architecture_en.md)
    → [JS vs Java](../guide/js-vs-java_en.md)
```

## Tutorial List

| # | Tutorial | You'll learn |
|---|---------|-------------|
| 1 | [From Zero](01-basics.md) | Create project → build → first script → chat commands → timers |
| 2 | [Players & Items](02-player-items.md) | Teleport, flight, give items, enchantments, potion effects, game modes |
| 3 | [Events & Entities](03-events-entities.md) | All event callbacks, spawn entities, AI control, patrol guards, collision |
| 4 | [Advanced Systems](04-advanced-systems.md) | Scoreboards, BossBars, teams, world border, cross-script messaging |
| 5 | [Real Mini-Games](05-examples.md) | Full PvP arena, particle effects, fireworks, wave spawning, home TP |

## Prerequisites

- **Language:** JavaScript/TypeScript. If you know JS, just write `.ts` files as JS.
- **Environment:** All server-side code runs on the server; players need nothing installed. Client scripts require the Box3JS client mod.
- **Hot Reload:** Edit code → `npm run build` → `/box3script reload` — no server restart needed.
- **Deployment:** When done, `/box3script compile` packages your script into a standalone JAR for `mods/`.
- **API Lookup:** Stuck on "which API does X"? Check the [API Task Reference](../api/README_en.md).

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

world.setInterval(() => {
  world.say("Players online: " + world.querySelectorAll("*").length);
}, 6000);
```

`npm run build` → `/box3script start <project>` and you're running.

## After the Tutorials

| I want to... | Read this |
|-------------|----------|
| Look up a specific API | [API Reference](../api/README_en.md) |
| Understand Box3JS internals | [Architecture](../guide/architecture_en.md) |
| Ship my script as a standalone mod | [Quick Start - Deployment](../guide/getting-started_en.md#deployment) |
| Register custom blocks/items/sounds | [registries API](../api/registries_en.md) |
| Write client scripts (UI/input/audio) | [client API](../api/client_en.md) |
| Decide Box3JS vs Java modding | [JS vs Java](../guide/js-vs-java_en.md) |

## Full API Docs

| Doc | Description |
|-----|-------------|
| [world](../api/world_en.md) | World state, events, particles, fireworks, sound |
| [entity](../api/entity_en.md) | Entity properties, AI, equipment, effects |
| [player](../api/player_en.md) | Inventory, messages, flight, teleport |
| [voxels](../api/voxels_en.md) | Block read/write, region fill |
| [storage](../api/storage_en.md) | JSON data persistence |
| [database](../api/database_en.md) | SQLite database |
| [http](../api/http_en.md) | HTTP network requests |
| [client](../api/client_en.md) | Client scripts (UI/input/chat/audio) |
| [registries](../api/registries_en.md) | Custom blocks/items/sounds |
| [math](../api/math_en.md) | GameVector3, Color, Quaternion |
| [commands](../api/commands_en.md) | `/box3script` command reference |
