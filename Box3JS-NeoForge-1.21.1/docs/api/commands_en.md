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

### `/box3script compile <project>`

Compiles a script project into a **lightweight standalone JAR mod** (~50KB) that depends on the Box3JS mod for Rhino runtime and API bindings.

```
/box3script compile mygame
```

> **Dependency:** Script JARs do not bundle Rhino or Box3JS API classes. Place the Box3JS mod (`box3js`) alongside your script JAR(s) in `mods/`.

> **Custom registries:** If `registries/blocks.json`, `items.json`, `sounds.json`, `creativeTabs.json` and `assets/` are present, blocks/items/sounds are registered and resources are bundled into the JAR. The client must also install the JAR for rendering. See [registries_en.md](registries_en.md).

The compiler **reads the following `package.json` fields** and writes them to `neoforge.mods.toml`:

| package.json | mods.toml field | Description |
|-------------|---------------|-------------|
| `name` | `modId` | Mod identifier |
| `displayName` | `displayName` | Display name (defaults to `name`) |
| `version` | `version` | Mod version |
| `description` | `description` | Mod description |
| `author` | `credits` | Author / credits |
| `license` | `license` | License (defaults to `All Rights Reserved`) |
| `homepage` | `displayURL` | Project homepage link |
| `bugs.url` | `issueTrackerURL` | Issue tracker link |
| `logoFile` | `logoFile` | Mod icon (PNG path in project, bundled as `logo.png`) |

> **`logoFile` usage:** Set to a relative path of a PNG file in the project root (e.g. `"logoFile": "logo.png"`). The file is automatically bundled as `logo.png` in the JAR root — no manual `neoforge.mods.toml` config needed. NeoForge recommends 128×128 or 256×256, PNG format only. Leave empty for the default mod icon.

Output filename format: `dist/<name>-<version>.jar`. Compilation runs on a background thread — no server tick blocking. The output path is shown in chat on completion.

**Prerequisites:**

- Project must be built (`npm run build`) — `dist/server.js` must exist
- Server must run on **JDK** (not JRE), as `javac` is needed to compile the generated `@Mod` entry class

**Output JAR contents:**

```
mygame-1.0.0.jar
├── META-INF/neoforge.mods.toml      ← mod metadata (depends on box3js)
├── logo.png                         ← mod icon (if specified)
├── assets/mygame/                   ← block models, textures, blockstates (if present)
│   ├── lang/en_us.json              ← language file (auto-generated)
│   ├── blockstates/*.json
│   ├── models/block/*.json
│   ├── models/item/*.json
│   └── textures/block/*.png
├── box3script/mygame/MygameMod.class ← @Mod entry (with DeferredRegister)
├── box3script/mygame/server.js       ← bundled server script
└── box3script/mygame/client.js       ← bundled client script (if present)
```

**Deployment:** Place the script JAR alongside the Box3JS mod in `mods/`:

```
mods/
├── box3js-1.0.0.jar       ← Box3JS main mod
└── mygame-1.0.0.jar       ← compiled script mod
```

**Interpreted vs Compiled:**

| | Interpreted | Compiled |
|---|---|---|
| Load via | `/box3script start` | Drop in `mods/`, start server |
| Command control | `/box3script start/stop/reload` | Not managed by `/box3script` |
| Enable/disable | `/box3script start/stop` | Add/remove JAR from `mods/`, restart server |
| Requires Box3JS | Yes | Yes |
| Hot reload | Yes | No (restart server to update) |
| Use case | Development & debugging | Distribution & deployment |

> **Note:** Compiled JARs are standard NeoForge mods managed by the NeoForge mod loader. They are **not** controlled by `/box3script start/stop/reload`. Multiple compiled JARs can coexist in `mods/` — each runs independently with its own hardcoded metadata.

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
  │       ├── types/
  │       ├── src/
  │       │   ├── server/app.ts
  │       │   └── client/app.ts
  │       ├── registries/         ← block/item/sound registration (compiled mode)
  │       │   ├── blocks.json
  │       │   └── creativeTabs.json
  │       ├── assets/             ← models/textures/sounds/lang (compiled mode)
  │       │   └── textures/block/
  │       └── dist/
  │           ├── server.js       ← compiled output
  │           ├── client.js       ← client compiled output
  │           └── <name>-<ver>.jar ← standalone JAR (compile command)
  ├── data/                      ← SQLite database (db API)
  └── storage/                  ← storage API persistence
```
