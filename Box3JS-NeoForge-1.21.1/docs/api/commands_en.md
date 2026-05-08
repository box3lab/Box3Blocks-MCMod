# /box3script Command Reference

All commands require **OP level 2** (default admin permission). All `<project>` arguments support **Tab completion**.

## Command List

### `/box3script`

Shows project status overview.

```
/box3script
```

Example output:

```
══ Box3JS Script Engine ══

  Watch: ● Active    Sandbox: ● 1 project(s)

  Projects: 1/2 enabled  |  1 loaded

  ────────────────────────────
  ● colorzone ▐SANDBOX▌
  ◌ demo
  ────────────────────────────

  Start  /box3script start [name|all]
  Stop   /box3script stop [name|all]
  Reload /box3script reload [name]
  New    /box3script create <name>
```

- `◉` = loaded & running, `○` = enabled but not loaded, `◌` = disabled
- `▐SANDBOX▌` = sandbox active

### `/box3script create <name>`

Creates a new TypeScript script project. Generates a complete TS scaffold, **disabled** by default.

```
/box3script create mygame
```

After creation:

```bash
cd config/box3/script/mygame
npm install && npm run build
```

Then enable with `/box3script start mygame`.

### `/box3script start [project|all]`

Enable and load projects. **No args** = all projects. **Project name** = only that project. **`all`** = explicitly all.

```
/box3script start              # enable all
/box3script start all          # enable all (same as no args)
/box3script start mygame       # enable only mygame
```

### `/box3script stop [project|all]`

Disable and unload projects. **No args** = all projects. **Project name** = only that project. **`all`** = explicitly all.

```
/box3script stop               # disable all
/box3script stop all           # disable all (same as no args)
/box3script stop mygame        # disable only mygame
```

### `/box3script reload [project]`

Reload scripts. **No args** = stop all, reload all enabled projects. **With project name** = reload only that project.

```
/box3script reload            # reload all enabled projects
/box3script reload mygame     # reload only mygame
```

After editing code and running `npm run build`, use `reload` to apply changes. Or enable `watch` for auto-reload.

### `/box3script watch`

Toggle file watching. When on, monitors `dist/` across all projects and auto-reloads on `.js` file changes.

```
/box3script watch             # toggle on/off
```

### `/box3script sandbox <project>`

Toggle sandbox mode. When enabled, tracks all block/entity/world state changes. When disabled, rolls back and shows summary.

```
/box3script sandbox mygame    # toggle on/off
```

Typical workflow:

```
/box3script sandbox mygame    # enable sandbox
/box3script start mygame      # load project
# ... test ...
/box3script reload mygame     # reload after code changes
# ... satisfied ...
/box3script sandbox mygame    # disable sandbox → full rollback
```

> **Note:** Sandbox only tracks blocks placed through script APIs (`setVoxel`/`setVoxelId`/`fillVoxel`). Manual mining is unaffected.

## Configuration File

Enable/disable state is saved in `config/box3/scripts.json`:

```json
{
  "colorzone": true,
  "demo": false
}
```

## Script Directory Structure

```
config/box3/
  ├── scripts.json             ← project enable/disable config
  ├── script/                   ← scripts directory
  │   └── mygame/
  │       ├── build.mjs
  │       ├── package.json
  │       ├── eslint.config.mjs
  │       ├── tsconfig.json
  │       ├── types/globals.d.ts
  │       ├── src/app.ts
  │       └── dist/app.js       ← compiled output
  └── storage/                  ← storage API persistence
```
