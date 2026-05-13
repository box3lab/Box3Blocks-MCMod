# Database API

Box3JS exposes SQLite capabilities through the global `db` object. Connections are managed automatically.

> **Runtime:** Available on both server and client. Server databases live at `config/box3/data/<project>.db`; client databases live under the local game directory at `box3/client-db/<project>.db`. The two sides do not share database files; use `remoteChannel` when data must be synchronized.

## Dependency & Graceful Fallback

- The `db` API depends on the external `minecraft-sqlite-jdbc` mod (JDBC driver provider).
- If this mod is **not** installed, other Box3JS APIs (`world`, `storage`, `voxels`, etc.) continue to work normally.
- A clear error is thrown only when `db.sql(...)` is actually used:

```text
db API requires SQLite JDBC driver. Install the minecraft-sqlite-jdbc mod, then restart server.
```

After installing `minecraft-sqlite-jdbc` and restarting the server, the `db` API becomes available.

> **NeoForge dev environment note:**
>
> - Put `minecraft-sqlite-jdbc` under `run/mods/`.
> - The file must be a `.jar` (for example, `xxx.jar`), not `.zip`, otherwise NeoForge will not load it.

## `db.sql(sql, ...params)`

Executes a SQL query/update and returns `GameQueryResult`.

### Parameters

| Param    | Type                                                    | Description                                                     |
| -------- | ------------------------------------------------------- | --------------------------------------------------------------- |
| `sql`    | `string` \| `string[]`                                  | SQL text with `?` placeholders, or tagged-template string parts |
| `params` | `(number \| string \| boolean \| null \| Uint8Array)[]` | Values bound to placeholders (including BLOB)                   |

### Return

`GameQueryResult` — query result object.

## GameQueryResult

### Properties

| Property       | Type                          | Description                                           |
| -------------- | ----------------------------- | ----------------------------------------------------- |
| `rows`         | `any[]`                       | All rows (for SELECT)                                 |
| `firstRow`     | `Record<string, any> \| null` | First row, or null                                    |
| `columnNames`  | `string[]`                    | Column names                                          |
| `columnCount`  | `number`                      | Number of columns                                     |
| `rowCount`     | `number`                      | Row count (SELECT)                                    |
| `affectedRows` | `number`                      | Affected rows (INSERT/UPDATE/DELETE), `-1` for SELECT |
| `isQuery`      | `boolean`                     | Whether this is a query (SELECT)                      |

### Methods

| Method                   | Description                                       |
| ------------------------ | ------------------------------------------------- |
| `next()`                 | Returns next row as `{done: boolean, value: any}` |
| `reset()`                | Resets internal cursor to first row               |
| `then(resolve, reject?)` | Thenable support; resolve receives all rows       |

## Basic Usage

`db.sql()` supports both plain SQL strings (`?` placeholders) and tagged-template style (`${}` placeholders). They compile to equivalent calls.

### Create table

```js
db.sql(
  "CREATE TABLE IF NOT EXISTS players (name TEXT PRIMARY KEY, score INTEGER DEFAULT 0, lastLogin INTEGER)",
);
// tagged-template style
db.sql`CREATE TABLE IF NOT EXISTS players (name TEXT PRIMARY KEY, score INTEGER DEFAULT 0, lastLogin INTEGER)`;
```

### Insert data

```js
db.sql(
  "INSERT INTO players (name, score, lastLogin) VALUES (?, ?, ?)",
  "Steve",
  100,
  Date.now(),
);
// tagged-template style
db.sql`INSERT INTO players (name, score, lastLogin) VALUES (${"Steve"}, ${100}, ${Date.now()})`;
```

### Query data

```js
// TypeScript: .map() / .filter() / .forEach() / for...of / arrow functions all work
// (Babel compiles them to Rhino-compatible indexed for loops)
var rows = db.sql("SELECT * FROM players WHERE score > ?", 50).rows;
rows.forEach((row) => {
  console.log(`${row.name}: ${row.score}`);
});

// tagged template + .filter() + .map() chaining
var scores = db.sql`SELECT name, score FROM players WHERE score > ${50}`.rows
  .filter((r) => r.score > 20)
  .map((r) => `${r.name}: ${r.score}`);
scores.forEach((s) => console.log(s));

var player = db.sql("SELECT * FROM players WHERE name = ?", "Steve").firstRow;
if (player) {
  console.log(`Score: ${player.score}`);
}
```

## Complete Example: Leaderboard

```js
// Initialize table
db.sql(
  "CREATE TABLE IF NOT EXISTS leaderboard (player TEXT PRIMARY KEY, score INTEGER, updated INTEGER)",
);

// Record a score
function recordScore(playerName, score) {
  var existing = db.sql(
    "SELECT score FROM leaderboard WHERE player = ?",
    playerName,
  ).firstRow;
  if (existing) {
    if (score > existing.score) {
      db.sql(
        "UPDATE leaderboard SET score = ?, updated = ? WHERE player = ?",
        score,
        Date.now(),
        playerName,
      );
    }
  } else {
    db.sql(
      "INSERT INTO leaderboard (player, score, updated) VALUES (?, ?, ?)",
      playerName,
      score,
      Date.now(),
    );
  }
}

// Get Top 10
function getTop10() {
  return db.sql(
    "SELECT player, score FROM leaderboard ORDER BY score DESC LIMIT 10",
  ).rows;
}

// Get player rank
function getRank(playerName) {
  var row = db.sql(
    "SELECT COUNT(*) + 1 AS rank FROM leaderboard WHERE score > (SELECT score FROM leaderboard WHERE player = ?)",
    playerName,
  ).firstRow;
  return row ? row.rank : 0;
}

// Usage
recordScore("Steve", 500);
recordScore("Alex", 800);
recordScore("Steve", 600); // update

var top = getTop10();
for (var i = 0; i < top.length; i++) {
  console.log(i + 1 + ". " + top[i].player + " - " + top[i].score);
}

console.log("Steve rank: " + getRank("Steve"));
```

## Complete Example: Player Data Persistence

```js
db.sql(
  "CREATE TABLE IF NOT EXISTS player_data (uuid TEXT PRIMARY KEY, name TEXT, playtime INTEGER, deaths INTEGER, lastSeen INTEGER)",
);

world.onPlayerJoin(function (entity) {
  var p = entity.player;
  var row = db.sql(
    "SELECT * FROM player_data WHERE uuid = ?",
    p.userId,
  ).firstRow;
  if (row) {
    db.sql(
      "UPDATE player_data SET name = ?, lastSeen = ? WHERE uuid = ?",
      p.name,
      Date.now(),
      p.userId,
    );
  } else {
    db.sql(
      "INSERT INTO player_data (uuid, name, playtime, deaths, lastSeen) VALUES (?, ?, 0, 0, ?)",
      p.userId,
      p.name,
      Date.now(),
    );
  }
});

world.onPlayerLeave(function (entity) {
  var p = entity.player;
  db.sql(
    "UPDATE player_data SET lastSeen = ? WHERE uuid = ?",
    Date.now(),
    p.userId,
  );
});
```

## Comparison with storage

|      | `db` (SQLite)                 | `storage` (JSON)                |
| ---- | ----------------------------- | ------------------------------- |
| Query | SQL WHERE/JOIN/ORDER BY/LIMIT | Read all, filter in JS          |
| Write | Single-row atomic             | Full overwrite                  |
| Best for | Leaderboards, economy, logs, relational data | Config, flags, simple key-value |
| File | `data/<project>.db`           | `storage/<project>/<name>.json` |
| Concurrency | Naturally safe (WAL mode)     | Serial per-project is adequate  |

## Notes

- Database files are auto-created.
- Connections are auto-closed when a project stops/unloads.
- Always use placeholders (`?`) instead of string concatenation (SQL injection risk).
- SQLite uses dynamic typing; integers/floats are adapted automatically.
- BLOB values are passed as `Uint8Array`/byte-array style data.

## Rhino Compatibility

Box3JS uses the Rhino 1.9.1 engine. **TypeScript projects compiled with `npm run build` can use all modern syntax** — Babel plugins convert it to Rhino-compatible code:

| Feature | Compilation |
|---------|------------|
| Arrow functions `(x) => x + 1` | Babel `@babel/preset-env` |
| Template literals `` `Hello ${name}` `` | `rhinoTemplatePlugin` |
| `for...of` (JS arrays + Java ArrayList) | `rhinoForOfPlugin` → indexed for + `.toArray()` |
| `.map()` `.filter()` `.forEach()` `.find()` `.some()` `.every()` | `rhinoArrayMethodsPlugin` → IIFE + for loop |
| `const` / `let` | Babel `@babel/preset-env` |
| Destructuring `const { x, y } = obj` | Babel `@babel/preset-env` |

**Plain JS notes:**

- `result.rows` returns a `NativeArray` — use indexed for loops.
- Avoid regex literals (e.g. `/\s+/`) — use string methods.
- Arrow functions, template literals, `for...of` require TypeScript compilation.

## Tagged Template Safety

Only bind **values** with `${...}`. Do not bind SQL identifiers (table/column names).

```ts
// ✅ correct: value binding
db.sql`SELECT * FROM players WHERE name = ${name}`;

// ❌ incorrect: table name cannot be bound as a parameter
db.sql`SELECT * FROM ${table} WHERE name = ${name}`;
```
