# Box3JS -- Minecraft Mod

> **Beta** — This project is in early beta. APIs may change, and undiscovered issues may still exist. Feedback is welcome.

[简体中文](README.md) | [English](README_en.md)

`Box3JS` is a Minecraft server-side mod inspired by Box3 coding style. You don’t need to write Java — just use TypeScript to build scripts.

## Features

- **TypeScript Support** — The project template includes TS type declarations with full type checking
- **Box3 API Compatibility** — Implements core Box3 APIs (World / Entity / Player / Voxels / Storage)
- **Minecraft Extensions** — 90+ Minecraft-specific features: scoreboards, bossbars, teams, world border, particles, fireworks, potions, and more
- **Hot Reload** — Reload scripts with `/box3script watch` without restarting
- **Project Management** — Multi-project isolation, independent enable/disable, and auto-run on restart

## Quick Start

In-game (requires OP level ≥ 2):

```
/box3script create mygame
```

This creates a TypeScript scaffold project:

```
config/box3/script/mygame/
├── .gitignore
├── package.json          ← npm dependencies (esbuild, Babel, TypeScript)
├── tsconfig.json
├── build.mjs             ← build script (esbuild → Babel → Rhino)
├── types/
│   └── globals.d.ts      ← full Box3JS type declarations
└── src/
    └── app.ts            ← entry point (includes Hello World example)
```

Then build:

```bash
cd config/box3/script/mygame
npm install
npm run build          # outputs dist/app.js
```

Back in game and enable it:

```
/box3script on mygame
```

## Available APIs

[API Overview →](docs/api/README.md) ([English](docs/api/README_en.md))

## Commands

[Full Command Reference →](docs/api/commands.md) ([English](docs/api/commands_en.md))

## License

Apache License 2.0
