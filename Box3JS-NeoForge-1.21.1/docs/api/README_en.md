# Box3JS API Reference

Box3JS is a Minecraft mod that lets you write server-side scripts in JavaScript. All scripts run under `config/box3/script/<project>`.

## Quick Start

```js
// app.js — minimal example
world.onTick(() => {
  // runs every tick (20 ticks = 1 second)
});

world.onChat((entity, message, tick) => {
  var p = entity.player;
  if (message === "!hello") {
    p.directMessage("Hello, " + p.name + "!");
  }
});

console.log("Script loaded");
```

## Global Objects

| Object           | Type    | Description                                                                        |
| ---------------- | ------- | ---------------------------------------------------------------------------------- |
| `world`          | ✅ Box3 | World control, see [world.md](world.md)                                            |
| `entity`         | ✅ Box3 | Entity wrapper (from callbacks or `world.spawnEntity`), see [entity.md](entity.md) |
| `player`         | ✅ Box3 | Player wrapper (via `entity.player`), see [player.md](player.md)                   |
| `voxels`         | ✅ Box3 | Block operations, see [voxels.md](voxels.md)                                       |
| `storage`        | ✅ Box3 | Data persistence, see [storage.md](storage.md)                                     |
| `console`        | ⬆ MC    | `console.log/debug/warn/error/assert/clear`                                        |
| `require(id)`    | ⬆ MC    | CommonJS module import, see module section below                                   |
| `sleep(ms)`      | ⬆ MC    | Block the thread for the given milliseconds (runtime clamps to at most 10ms)       |
| `GameVector3`    | ✅ Box3 | 3D vector, see [math.md](math.md)                                                  |
| `GameBounds3`    | ✅ Box3 | Bounding box, see [math.md](math.md)                                               |
| `GameRGBColor`   | ✅ Box3 | RGB color, see [math.md](math.md)                                                  |
| `GameRGBAColor`  | ✅ Box3 | RGBA color, see [math.md](math.md)                                                 |
| `GameQuaternion` | ✅ Box3 | Quaternion, see [math.md](math.md)                                                 |

## API Legend

| Label              | Meaning                                                            |
| ------------------ | ------------------------------------------------------------------ |
| ✅ **Box3 API**    | Originates from the Box3 platform; naming and semantics match Box3 |
| ⬆ **MC Extension** | Not in original Box3; added using Minecraft-specific features      |

## Document Index

| Document                   | Content                                                                       |
| -------------------------- | ----------------------------------------------------------------------------- |
| [world.md](world.md)       | World state, events, scoreboard, bossbar, teams, border, particles, fireworks |
| [entity.md](entity.md)     | Entity properties, AI, equipment, potions, pathfinding, tags                  |
| [player.md](player.md)     | Inventory, messaging, flight, gamemode, teleport, commands                    |
| [voxels.md](voxels.md)     | Block read/write, region fill, spawner control                                |
| [storage.md](storage.md)   | Persistent data storage                                                       |
| [math.md](math.md)         | Vector3, Bounds3, Color, Quaternion                                           |
| [commands.md](commands.md) | `/box3script` command reference                                               |

## File Modules

**TypeScript build pipeline:**

Projects created with `/box3script create` come with a complete TS build environment. Write in `src/*.ts`, build outputs to `dist/app.js`:

```
config/box3/script/mygame/
├── package.json          ← esbuild + Babel + @babel/preset-typescript
├── tsconfig.json
├── build.mjs             ← Babel TS→JS → esbuild bundle → dist/
├── types/
│   └── globals.d.ts      ← Full API type declarations (IDE autocomplete)
├── src/
│   ├── app.ts            ← Entry point, require() other modules
│   ├── state.ts          ← Shared game state
│   ├── course.ts         ← Course data & building
│   └── ...
└── dist/
    └── app.js            ← Compiled output (what the mod actually loads)
```

Run `npm run build` or `node build.mjs` to build. Use `/box3script watch` to enable file watching for auto hot-reload.
