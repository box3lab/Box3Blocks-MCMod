# Quick Start: From Zero to Your First Box3JS Script

This guide is for readers with **zero modding experience**. If you know JavaScript, you can write your first Minecraft server script in 10 minutes.

## Table of Contents

1. [What is Box3JS](#what-is-box3js)
2. [Setup](#setup)
3. [Create a Project](#create-a-project)
4. [Your First Script](#your-first-script)
5. [Dev Cycle](#dev-cycle)
6. [Debugging](#debugging)
7. [Deployment](#deployment)
8. [Next Steps](#next-steps)

---

## What is Box3JS

Box3JS is a **server-side scripting engine mod** for NeoForge 1.21.1. It embeds a JavaScript runtime (Mozilla Rhino) inside the Minecraft server, letting you write gameplay logic in JS/TypeScript.

### What You Can Do

| Category | Examples |
|----------|---------|
| Chat Commands | `!heal`, `!home`, `!shop` |
| Event Response | Welcome on join, death penalty, block break logging |
| Entity Control | Spawn mobs, set AI, custom bosses |
| Mini-Games | PvP arena, parkour, wave survival |
| World Manipulation | Place/replace blocks, fill regions, change weather/time |
| Data Persistence | JSON storage, SQLite database |
| Game Systems | Scoreboards, BossBars, teams, world borders |
| HTTP Requests | Web API calls, webhook notifications |
| Client Scripts | Key listeners, screen UI, client audio |

### What You Can't Do

- **Render custom models/particles** — requires a client resource pack or Java mod
- **Add new blocks/items at runtime** — requires compiling to a JAR (`/box3script compile`)
- **Modify vanilla mechanics** — changing recipes, mob behavior requires Mixin

### Core Design

```
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│ TypeScript   │ ───→ │ Babel ES5    │ ───→ │ Rhino Engine │
│ Source       │      │ Compile      │      │ (in JVM)     │
└──────────────┘      └──────────────┘      └──────────────┘
                                                    │
                                            ┌───────┴───────┐
                                            │  Minecraft    │
                                            │  NeoForge API │
                                            └───────────────┘
```

- **Runs inside the server JVM**, directly calling Minecraft/NeoForge APIs
- **TypeScript source** compiled to ES5 via Babel, targeting Rhino
- **Hot reload** — no server restart when you change code
- **Sandbox isolation** — each project has an independent scope

---

## Setup

### What You Need

1. **Minecraft server** with Box3JS + NeoForge 1.21.1 installed
2. **Node.js** 18+ (for local builds only — not needed on the server)
3. A text editor (VS Code recommended)

### Verify Installation

In-game, run:

```
/box3script
```

If you see the project status panel, Box3JS is running.

---

## Create a Project

In-game:

```
/box3script create mygame
```

This generates a complete TypeScript project at `config/box3/script/mygame/`:

```
config/box3/script/mygame/
├── package.json           ← Project config (name, version, build deps)
├── tsconfig.base.json     ← Shared TS compiler options
├── tsconfig.server.json   ← Server TS config
├── tsconfig.client.json   ← Client TS config
├── build.mjs              ← Build script (esbuild + Babel)
├── eslint.config.mjs      ← ESLint rules
├── types/
│   ├── shared.d.ts        ← Shared server & client types
│   ├── server/
│   │   ├── index.d.ts     ← Server type entry point
│   │   └── ...
│   └── client/
│       ├── index.d.ts     ← Client type entry point
│       └── ...
├── src/
│   ├── server/
│   │   └── app.ts         ← ★ Server entry point (where you write code)
│   └── client/
│       └── app.ts         ← Client entry point
└── registries/            ← Custom content (blocks/items/sounds JSON)
```

### Install Dependencies

```bash
cd config/box3/script/mygame
npm install
```

`npm install` only needs to run once (installs esbuild, Babel, TypeScript build tooling).

---

## Your First Script

Open `src/server/app.ts`, clear the contents, and write:

```js
// 1. Startup log
console.log("MyGame script started!");

// 2. Welcome players on join
world.onPlayerJoin((entity) => {
  const p = entity.player;
  p.directMessage("Welcome, " + p.name + "!");

  // Particle welcome effect
  const pos = p.position;
  world.spawnParticleCircle(pos.x, pos.y, pos.z, 1.5, "minecraft:happy_villager", 15);
  world.playSound("minecraft:block.note_block.pling", pos, 1.0, 1.5);
});

// 3. Chat commands
world.onChat((entity, message) => {
  const p = entity.player;

  if (message === "!hello") {
    p.directMessage("Hello, " + p.name + "!");
    return false; // suppress chat message
  }

  if (message === "!pos") {
    const pos = p.position;
    p.directMessage("Your position: " +
      Math.floor(pos.x) + ", " +
      Math.floor(pos.y) + ", " +
      Math.floor(pos.z));
    return false;
  }

  return true; // normal chat messages pass through
});

// 4. Periodic announcement
world.setInterval(() => {
  const count = world.querySelectorAll("*").length;
  world.say("[Info] Players online: " + count);
}, 6000); // 6000 ticks = 5 minutes
```

### Key Concepts

- **Globals need no import** — `world`, `console`, `player` are injected by Box3JS
- **Return false to block default behavior** — `onChat` returning false suppresses the message
- **Ticks are MC time units** — 1 second = 20 ticks, `setInterval` uses ticks
- **§ codes are MC color codes** — `§a` = green, `§e` = yellow, `§6` = gold, `§7` = gray

---

## Dev Cycle

Standard flow after each code change:

```
Edit code → npm run build → /box3script reload mygame → test
```

### Build

```bash
npm run build
```

Output:

```
  dist/server.js  7.1kb
Done in 240ms
```

What the build does:

1. **Babel** compiles TypeScript to ES5 JavaScript
2. **esbuild** bundles all modules into a single file
3. Outputs to `dist/server.js` and `dist/client.js`

### Load

In-game:

```
/box3script start mygame    # first launch
/box3script reload mygame   # reload after changes (no server restart)
```

### Auto Hot-Reload

Enable file watching so build + save auto-triggers reload:

```
/box3script watch
```

---

## Debugging

### Troubleshooting Order

1. **Check console** — server logs show errors with `[Box3JS] [projectName]` prefix
2. **Check status** — `/box3script` to see if project shows `◉` (loaded)
3. **Check build** — `npm run build` should complete without errors
4. **Add logging** — use `console.log()` at key points to print variable values
5. **Read line numbers** — Java exception stacks include JS filenames and line numbers

### Common Errors

| Error | Cause | Fix |
|-------|-------|-----|
| `console is not defined` | Engine init failed | Check mod installation |
| `world is not defined` | Scope issue | Ensure code is at global scope, not inside a function |
| `Cannot find name 'xxx'` | TypeScript type error | Check spelling or look up the correct API name in `.d.ts` |
| `npm run build` fails | JS syntax error | Check ESLint output |
| Script not executing | Project not enabled | Check `/box3script` status |

### Sandbox Testing

Sandbox mode enables safe testing: all world modifications are tracked and rolled back on close.

```
/box3script sandbox mygame    # enable sandbox
# ... test script (spawn entities, modify blocks, etc.)...
/box3script sandbox mygame    # disable → auto-rollback all changes
```

---

## Deployment

Once development is done, compile your script into a **standalone JAR mod**:

```
/box3script compile mygame
```

Generates `mygame-1.0.0.jar` (version from `package.json`). Drop it into any NeoForge server's `mods/` directory.

**Notes:**
- Box3JS must also be installed as a dependency (provides the Rhino runtime)
- If you use `registries` (custom blocks/items), clients must also install the JAR
- The JAR contains compiled JS — no source code needed

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

These metadata fields are written into the JAR's `mods.toml`.

---

## Next Steps

- **Learn APIs**: [API by Task](../api/README_en.md) — find APIs by "I want to..."
- **Learn Events**: [Tutorial 3: Events & Entities](../tutorial/03-events-entities.md)
- **Learn Client**: [Client API](../api/client_en.md) — key listeners, screen UI, client audio
- **Understand Internals**: [Architecture](architecture_en.md) — Rhino engine, scopes, build pipeline
- **Tech Decision**: [JS vs Java](js-vs-java_en.md) — Box3JS vs native modding
