# Database API

Box3JS exposes SQLite capabilities through the global `db` object. Each script project gets its own database file at `config/box3/data/<project>.db`, and connections are managed automatically.

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
// Iterate with for-loop; Rhino NativeArray does not support ES5 array helpers.
var rows = db.sql("SELECT * FROM players WHERE score > ?", 50).rows;
for (var i = 0; i < rows.length; i++) {
  console.log(rows[i].name + ": " + rows[i].score);
}

var player = db.sql("SELECT * FROM players WHERE name = ?", "Steve").firstRow;
if (player) {
  console.log("Score: " + player.score);
}
```

## Notes

- Database files are auto-created.
- Connections are auto-closed when a project stops/unloads.
- Always use placeholders (`?`) instead of string concatenation (SQL injection risk).
- SQLite uses dynamic typing; integers/floats are adapted automatically.
- BLOB values are passed as `Uint8Array`/byte-array style data.

## Tagged Template Safety

Only bind **values** with `${...}`. Do not bind SQL identifiers (table/column names).

```ts
// ✅ correct: value binding
db.sql`SELECT * FROM players WHERE name = ${name}`;

// ❌ incorrect: table name cannot be bound as a parameter
db.sql`SELECT * FROM ${table} WHERE name = ${name}`;
```
