---
---

# storage — Data Storage API

`storage` provides JSON file persistence with in-memory caching for fast reads/writes.

::: info Runtime
Available on both server and client. Server data is saved under `config/box3/storage/<project>/`; client data is saved under the local game directory at `box3/client-storage/<project>/`. Each project automatically gets an independent namespace.
:::

## Getting a Storage Instance

### storage.key

Readonly. The root `storage` object always returns an empty string; read `store.key` for a concrete namespace name.

### storage.getDataStorage(name)

Gets or creates a named storage. Same name returns the same instance.

### storage.getGroupStorage(name)

Gets a **cross-project shared** storage. All projects access the same data via the same `name` (uses `__shared__/` namespace internally). Useful for global leaderboards, shared config, etc.

::: warning
Server-side only. Client local storage only provides `getDataStorage(name)`.
:::

```js
var store = storage.getDataStorage("leaderboard");
var config = storage.getDataStorage("settings");
```

## Read & Write

### store.set(key, value)

Store a key-value pair. `value` can be a string, number, or object (auto JSON-serialized).

### store.get(key)

Get a value. Returns the original type.

```js
store.set("highScore", 100);
store.set("lastWinner", "Steve");
store.set("config", { difficulty: "hard", maxPlayers: 10 });

var score = store.get("highScore"); // 100 (number)
var winner = store.get("lastWinner"); // "Steve" (string)
var cfg = store.get("config"); // {difficulty: "hard", ...} (object)
```

::: warning Note
After data is reloaded from disk, complex values are returned as plain JSON objects (for example map-like objects). Avoid relying on original JS prototype methods.
:::

### store.keys()

Returns an array of all keys.

```js
var keys = store.keys();
for (var i = 0; i < keys.length; i++) {
  console.log(keys[i] + " = " + store.get(keys[i]));
}
```

## Update & Delete

### store.update(key, handler)

Callback-based value update. `handler` receives the current value and returns the new value. Equivalent to `store.set(key, handler(store.get(key)))` but guarantees atomicity.

```js
store.set("counter", 0);
store.update("counter", function (current) {
  return current + 1; // atomic increment
});
```

### store.remove(key)

Deletes the specified key and returns the previous value (or `null` if missing).

### store.destroy()

Deletes the entire storage file (also clears the in-memory cache).

```js
store.remove("tempKey");
store.destroy(); // delete all data in this storage
```

## Numeric Operations

### store.increment(key, delta)

Increment a numeric value. `delta` defaults to 1 and returns the new value.

```js
store.set("kills", 0);
store.increment("kills"); // kills = 1
store.increment("kills", 5); // kills = 6
store.increment("kills", -2); // kills = 4
```

## Paginated Queries

### store.list(options)

Cursor-based paginated query. Supported `options` fields:

| Field              | Type    | Description                                        |
| ------------------ | ------- | -------------------------------------------------- |
| `cursor`           | number  | Starting cursor (page × pageSize)                  |
| `pageSize`         | number  | Entries per page (1–100, default 100)              |
| `ascending`        | boolean | Sort ascending                                     |
| `max`              | number  | Upper value filter                                 |
| `min`              | number  | Lower value filter                                 |
| `constraintTarget` | string  | Nested path for sorting/filtering (e.g. `"a.b.c"`) |

Returns a `QueryList` page object:

| Property/Method           | Description                                     |
| ------------------------- | ----------------------------------------------- |
| `result.isLastPage`       | Whether this is the last page                   |
| `result.getCurrentPage()` | Returns the current page as an array of entries |
| `result.nextPage()`       | Move to the next page                           |

Each entry is `{key, value, updateTime, createTime, version}`.

```js
var result = store.list({ pageSize: 10, ascending: false });

// Iterate current page
var page = result.getCurrentPage();
for (var i = 0; i < page.length; i++) {
  console.log(page[i].key + ": " + page[i].value);
}

// Next page
if (!result.isLastPage) {
  result.nextPage();
}
```

## Memory Cache & Persistence

All `GameDataStorage` instances share a memory cache (`ConcurrentHashMap`). Data is loaded from disk on first access; subsequent reads/writes operate in memory. Every write operation (`set`/`update`/`remove`/`increment`) syncs to disk immediately.

- **Same-name storage**: multiple `getDataStorage` calls for the same file path share a single in-memory copy, avoiding redundant I/O
- **Project isolation**: `getDataStorage("scores")` accesses different files in different projects (auto-prefixed with project name)
- **Cross-project sharing (server-side only)**: `getGroupStorage("leaderboard")` accesses the same `__shared__/leaderboard.json` from all projects

## Complete Example: Leaderboard

```js
// Server: cross-project shared leaderboard — all projects read/write the same data
var lb = storage.getGroupStorage("leaderboard");

// Save score
function saveScore(name, time) {
  lb.set(name, time);
}

saveScore("Steve", 12345);
saveScore("Alex", 9800);

// Iterate all entries
var result = lb.list({ pageSize: 10, ascending: true });
while (true) {
  var page = result.getCurrentPage();
  for (var i = 0; i < page.length; i++) {
    console.log(page[i].key + ": " + page[i].value);
  }
  if (result.isLastPage) break;
  result.nextPage();
}
```
