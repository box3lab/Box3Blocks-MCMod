# /box3script Command Reference

All commands require **OP level 2** (default admin permission). All `<project>` arguments support **Tab completion**.

## Command List

### `/box3script create <name>`

Creates a new TypeScript script project. Generates a complete TS scaffold under `config/box3/script/<name>/`. Created projects are **disabled** by default.

```
/box3script create mygame
```

Generated file structure:

```
config/box3/script/
  └── mygame/
      ├── .gitignore
      ├── package.json          ← dependencies (esbuild, Babel, TypeScript)
      ├── tsconfig.json
      ├── build.mjs             ← build script
      ├── types/
      │   └── globals.d.ts      ← Box3JS type declarations
      └── src/
          └── app.ts            ← entry point (with Hello World example)
```

After creation, manually install dependencies and build:

```bash
cd config/box3/script/mygame
npm install
npm run build          # outputs dist/app.js
```

### `/box3script`

With no arguments, lists all projects and their enable/disable/sandbox status.

```
/box3script
```

Example output:

```
=== Projects ===
  [ON] [SANDBOX]  colorzone
  [ON]  demo
  [OFF]  siege
```

### `/box3script on <project>`

Enables the specified project and **immediately loads and executes** it. Load errors are reported in chat.

```
/box3script on mygame
```

### `/box3script on all`

Enables all projects at once.

```
/box3script on all
```

### `/box3script off <project>`

Disables the specified project. It won't auto-run on next server restart.

```
/box3script off siege
```

### `/box3script off all`

Disables all projects at once and immediately unloads currently running scripts.

```
/box3script off all
```

### `/box3script reload`

Stops all scripts and reloads entry scripts for all enabled projects (prefers `dist/app.js`, with legacy root `app.js` fallback). Load errors are reported in chat.

```
/box3script reload
```

### `/box3script reload <project>`

Reloads the specified project (stop then start). If the project was disabled, it gets auto-enabled before starting.

```
/box3script reload mygame
```

### `/box3script watch`

Toggle file watching on/off. When enabled, monitors the `dist/` directory of all projects and auto hot-reloads when `.js` files change.

```
/box3script watch          # toggle on/off
/box3script watch on       # turn on
/box3script watch off      # turn off
```

### `/box3script sandbox <project>`

Toggle sandbox mode. When enabled, automatically tracks all block modifications, entity/player/world state changes made by the project. **Sandbox state is persistent** — `/box3script stop` and `/box3script reload` do NOT clear sandbox tracking. Only manually running this command again will disable sandbox and roll back all modifications. Rollback summary is displayed in chat.

```
/box3script sandbox mygame    # toggle on/off
```

**Tracked content:**

| Category | Tracked Items                                                                                                        |
| -------- | -------------------------------------------------------------------------------------------------------------------- |
| Blocks   | `setVoxel`/`setVoxelId`/`fillVoxel` modifications (max 5 million blocks)                                             |
| Entities | HP, AI, invisibility, glowing, invulnerability, fire, potion effects, tags, name, equipment, drop rate, attributes   |
| Players  | Gamemode, flight ability, speed, jump power, XP, food, inventory, armor, potions, position, dimension, respawn point |
| World    | Weather, time, difficulty, game rules, world border                                                                  |

Typical workflow:

```
/box3script sandbox mygame    # enable sandbox
/box3script on mygame         # load script
# ... test, observe results ...
/box3script stop mygame       # stop script, world unchanged
# ... edit code, npm run build ...
/box3script on mygame         # test again
# ... satisfied, roll back ...
/box3script sandbox mygame    # disable sandbox → rollback + summary
```

> **Note:** Sandbox only tracks block modifications made through script APIs (`setVoxel`/`setVoxelId`/`fillVoxel`). Blocks mined with a pickaxe are unaffected. Tracking limit is 5 million blocks; console warns at 90%.

### `/box3script stop`

Stops all projects, clearing all callbacks, timers, and scopes. **Projects with sandbox enabled automatically retain their sandbox tracking state** and are not rolled back.

```
/box3script stop
```

### `/box3script stop <project>`

Stops the specified project, clearing only that project's callbacks, timers, and scope — **other running projects are unaffected**. Sandboxed projects retain tracking state without rollback.

```
/box3script stop siege
```

## Configuration File

Enable/disable state is saved in `config/box3/scripts.json`:

```json
{
  "mygame": true,
  "siege": false,
  "demo": true
}
```

## Script Directory Structure

```
config/box3/
  ├── scripts.json        ← project enable/disable config
  ├── script/              ← scripts directory
  │   ├── mygame/
  │   │   ├── build.mjs
  │   │   ├── package.json
  │   │   ├── src/app.ts
  │   │   └── dist/app.js  ← compiled output
  │   └── siege/
  │       ├── build.mjs
  │       ├── package.json
  │       ├── src/app.ts
  │       └── dist/app.js
  └── storage/             ← storage data directory (storage API)
      └── ...
```
