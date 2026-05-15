---
---

# FAQ & Troubleshooting

## Script Loading

### Q: My script doesn't run. `/box3script` shows the project as ○ (not loaded)

Checklist:

1. Did `npm run build` succeed? Is `dist/server.js` present?
2. Has `/box3script start <project>` been run?
3. Check the server console for errors prefixed with `[Box3JS]`
4. Is the project name correct? Run `/box3script` to list loaded projects.

### Q: Changes don't take effect after reload

- Make sure `npm run build` ran before the reload
- Verify build output went to the correct `dist/` directory
- Enable file watching: `/box3script watch` for auto-reload on build
- If watching is enabled but still not working, run `/box3script reload <project>` manually

### Q: Does hot reload lose data?

- **Not lost:** Scoreboard scores, storage JSON files, SQLite data, world block state (with sandbox off)
- **Lost:** JavaScript in-memory variables (`let`/`var`), `Map`/`Set` instances, timers (`setTimeout`/`setInterval` are cleared)
- **Recommendation:** Use `storage` or scoreboards for persistent data, don't rely on in-memory variables

### Q: What's the difference between `/box3script start` and `reload`?

- `start` — First load (or re-enable after stop). Initializes global objects and event callbacks.
- `reload` — Re-load an already-loaded script. First unloads the old one (cleanup callbacks + resources), then loads the new one.
- For daily development, just use `reload`. `start` is only needed after `stop`.

## Build

### Q: `npm run build` fails with "Cannot find module"

```bash
npm install
```

Run `npm install` once after creating or cloning a project. After that, only `npm run build` is needed.

### Q: TypeScript reports type errors but the script runs fine

TypeScript only checks types at build time. At runtime, Rhino doesn't enforce types. To fix:

1. Check the `.d.ts` signatures in `types/server/` and `types/client/` for correctness
2. If the type is genuinely wrong, use `// @ts-expect-error` as a temporary bypass
3. Consider fixing the `.d.ts` file for proper type coverage

### Q: `npm run build` succeeds but the script throws syntax errors at runtime

Babel compiles to ES5 targeting Rhino 1.9.1 (ES5 only). Common pitfalls:

- Don't use `async/await` in `src/` (Babel doesn't fully compile these to ES5)
- Don't use `Promise` (Rhino 1.9.1 doesn't support it)
- `let`/`const`, `=>` arrow functions, and template literals are handled by Babel — safe to use

## Runtime

### Q: Where does `console.log` output go?

- **Server scripts:** Server console (`logs/latest.log`), format `[Box3JS] [project] message`
- **Client scripts:** Client log (launcher log or `.minecraft/logs/`)

### Q: How do I debug scripts?

1. **Add `console.log`** — The most direct debugging method
2. **Check server console** — Java exceptions include JS filenames and line numbers
3. **Sandbox testing** — `/box3script sandbox <project>` tracks all changes for rollback
4. **Narrow the scope** — Comment out most code, gradually uncomment to isolate issues
5. **`/box3script`** — Check if the project shows `◉` (loaded)

### Q: API says "xxx is not a function"

Check:

1. Is the method name spelled correctly? See [API reference](../api/README.md)
2. Is it on the right global object? e.g. `world.say()` not `server.say()`
3. Does it need `new`? e.g. `new GameVector3(x, y, z)`
4. Are you calling a client API from a server script? (`audio`/`input`/`ui`/`chat` only work in `src/client/`)

### Q: Script runs slowly / server lags

Rhino is an interpreted engine (no JIT). Optimization tips:

- **Avoid heavy work in onTick** — Use `setInterval` to reduce frequency
- **Cache query results** — Don't call `querySelectorAll` every tick
- **Minimize JS ↔ Java crossings** — Batch operations are faster than individual calls
- **Avoid `console.log` in tight loops** — Console output has overhead

### Q: How do multiple script projects share data?

- **Cross-script messaging:** `world.sendMessage("projectName", data)` + `world.onMessage()`
- **Shared scoreboards:** Different projects can read/write the same scoreboard
- **Shared database:** SQLite operates on the same database file
- **Not shared:** JS variables (each project has an independent Rhino scope)

### Q: Do old timers survive a reload?

No. `reload` clears all callbacks, timers, and event listeners for the project. To re-register timers after reload, place them at the script's top level (global scope) — the code re-executes after reload, so timers are re-registered automatically.

## Database

### Q: `db.sql()` errors with "SQLite driver not available"

Install the `minecraft-sqlite-jdbc` mod. If you don't use `db`, you don't need it. Restart the server after installing.

### Q: How do I prevent SQL injection?

Use parameterized queries (recommended):

```js
// ✅ Safe: parameterized
db.sql("SELECT * FROM t WHERE name = ?", userInput);

// ✅ Safe: tagged template
db.sql(["SELECT * FROM t WHERE name = '", "'"], userInput);

// ❌ Dangerous: string concatenation
db.sql("SELECT * FROM t WHERE name = '" + userInput + "'");
```

## HTTP

### Q: HTTP requests fail / timeout

- Can the server/client reach the target URL? (Firewall?)
- Is the `timeout` long enough? Default is 5000ms
- Use `async: true` + `onError` callback to see the specific error
- HTTPS certificate issues may occur in Java environments — consider trusting the cert or using HTTP

### Q: Sync vs async HTTP — when to use which?

- **Sync `http.fetch`:** Simple, get results immediately, but blocks the game tick (may cause brief lag)
- **Async `{ async: true, onResponse: ... }`:** Non-blocking, but requires callbacks to handle results
- **Recommendation:** Short requests (<100ms) can be sync. Long or heartbeat/reporting requests should use async.

## Client

### Q: Client script doesn't run

1. Does the player's client have Box3JS mod installed?
2. Has the server project enabled client scripts? (`src/client/app.ts` exists and build output `dist/client.js` is present)
3. Use `/box3script reload` to refresh (clients will re-receive the script)
4. If unsure, add a `remoteChannel` listener on the server to detect `clientReady` events

### Q: How to detect if a player has Box3JS client mod installed?

No manual detection is needed. `remoteChannel.sendClientEvent()` uses optional payloads — players without the Box3JS client mod will silently ignore these packets without errors or disconnects. You can safely send events to all players.

### Q: Can client and server use `remoteChannel` at the same time?

Yes. `remoteChannel` provides bidirectional channels:

- Client → Server: `remoteChannel.sendServerEvent()` → `remoteChannel.onServerEvent()`
- Server → Client (targeted): `remoteChannel.sendClientEvent(entity, ...)` → `remoteChannel.onClientEvent()`
- Server → Client (broadcast): `remoteChannel.broadcastClientEvent(...)` → `remoteChannel.onClientEvent()`

### Q: remoteChannel data format restrictions

Data sent across the network must be JSON-serializable: `string`, `number`, `boolean`, `null`, plain objects, arrays.

Cannot send: functions, `GameVector3` instances, Java objects. To send coordinates, use `{ x, y, z }` format.

## Deployment

### Q: How do I distribute my script?

```js
/box3script compile <project>
```

Outputs `<project>-<version>.jar`. Drop it into `mods/`. Recipients also need Box3JS installed (as a runtime dependency). Custom blocks/items require the client to also install the JAR.

### Q: What's the difference between compiled JAR and interpreted mode?

| Feature      | Interpreted Mode              | Compiled JAR                           |
| ------------ | ----------------------------- | -------------------------------------- |
| registries   | Not available                 | Available (custom blocks/items/sounds) |
| Hot reload   | ✅                            | ❌ (requires restart)                  |
| Distribution | Copy entire project directory | Single JAR file                        |
| Updates      | Edit JS files directly        | Recompile                              |

### Q: Why is registries only available in compiled mode?

Custom blocks/items/sounds require NeoForge's `DeferredRegister`, which must be registered during mod startup. Interpreted mode has no startup registration phase, so `registries` only works when compiled as a JAR.

For more questions, ask on [GitHub Issues](https://github.com/box3lab/Box3JS).
