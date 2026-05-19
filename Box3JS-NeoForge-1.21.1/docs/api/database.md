# Database API

Box3JS 通过全局 `db` 对象提供 SQLite 数据库能力，无需手动管理连接。

::: info 运行环境
服务端和客户端都可用。服务端数据库位于 `config/box3/data/<project>.db`；客户端数据库位于本地游戏目录的 `config/box3/client-db/<project>.db`。两端数据库互不共享，需要同步数据时请使用 `remoteChannel`。
:::

## 依赖与降级行为

- `db` API 依赖外部模组 `minecraft-sqlite-jdbc` 提供 JDBC 驱动。
- **未安装该模组时，不影响 Box3JS 其它功能使用**（`world`、`storage`、`voxels` 等正常可用）。
- 只有在实际调用 `db.sql(...)` 时，才会抛出清晰错误提示：

```text
db API requires SQLite JDBC driver. Install the minecraft-sqlite-jdbc mod, then restart server.
```

安装 `minecraft-sqlite-jdbc` 并重启服务器后，`db` API 即可恢复可用。

::: warning NeoForge 开发环境

- 请将 `minecraft-sqlite-jdbc` 放到 `run/mods/`。
- 模组文件必须是 `.jar`（例如 `xxx.jar`），不要使用 `.zip`，否则不会被 NeoForge 加载。
  :::

## `db.isAvailable()`

检查 SQLite JDBC 驱动是否可用。不可用时，`db.sql(...)` 会返回安全的空错误结果或显示清晰提示，脚本可用该方法提前降级。

## `db.sql(sql, ...params)`

执行 SQL 查询或更新，返回 `GameQueryResult`。

### 参数

| 参数     | 类型                                                    | 说明                                           |
| -------- | ------------------------------------------------------- | ---------------------------------------------- |
| `sql`    | `string` \| `string[]`                                  | SQL 字符串（`?` 占位符）或模板字面量字符串数组 |
| `params` | `(number \| string \| boolean \| null \| Uint8Array)[]` | 绑定到占位符的参数值（含 BLOB）                |

### 返回值

`GameQueryResult` — 查询结果对象。

## GameQueryResult

### 属性

| 属性           | 类型                          | 说明                                               |
| -------------- | ----------------------------- | -------------------------------------------------- |
| `rows`         | `any[]`                       | 所有行（SELECT 查询）                              |
| `firstRow`     | `Record<string, any> \| null` | 第一行，无结果时为 null                            |
| `columnNames`  | `string[]`                    | 列名数组                                           |
| `columnCount`  | `number`                      | 列数                                               |
| `rowCount`     | `number`                      | 行数（SELECT）                                     |
| `affectedRows` | `number`                      | 受影响行数（INSERT/UPDATE/DELETE），SELECT 返回 -1 |
| `isQuery`      | `boolean`                     | 是否为查询（SELECT）                               |

### 方法

| 方法                     | 说明                                     |
| ------------------------ | ---------------------------------------- |
| `next()`                 | 返回下一行 `{done: boolean, value: any}` |
| `reset()`                | 重置内部游标到第一行                     |
| `then(resolve, reject?)` | thenable 支持，resolve 接收全部行数组    |

## 基本用法

`db.sql()` 支持两种调用方式：普通字符串（`?` 占位符）和 tagged template（`${}` 占位符）。两种写法编译后等价，tagged template 写法更接近原生 SQL。

### 创建表

```js
db.sql(
  "CREATE TABLE IF NOT EXISTS players (name TEXT PRIMARY KEY, score INTEGER DEFAULT 0, lastLogin INTEGER)",
);
// tagged template 风格
db.sql`CREATE TABLE IF NOT EXISTS players (name TEXT PRIMARY KEY, score INTEGER DEFAULT 0, lastLogin INTEGER)`;
```

### 插入数据

```js
db.sql(
  "INSERT INTO players (name, score, lastLogin) VALUES (?, ?, ?)",
  "Steve",
  100,
  Date.now(),
);
// tagged template 风格
db.sql`INSERT INTO players (name, score, lastLogin) VALUES (${"Steve"}, ${100}, ${Date.now()})`;
```

### 查询数据

```js
// TypeScript 可直接用 .map() / .filter() / .forEach() / for...of / 箭头函数
// (Babel 编译为 Rhino 兼容的 for 循环)
var rows = db.sql("SELECT * FROM players WHERE score > ?", 50).rows;
rows.forEach((row) => {
  console.log(`${row.name}: ${row.score}`);
});

// tagged template + .filter() + .map() 链式调用
var scores = db.sql`SELECT name, score FROM players WHERE score > ${50}`.rows
  .filter((r) => r.score > 20)
  .map((r) => `${r.name}: ${r.score}`);
scores.forEach((s) => console.log(s));

// 获取第一行
var player = db.sql("SELECT * FROM players WHERE name = ?", "Steve").firstRow;
if (player) {
  console.log("Score: " + player.score);
}

// 逐行迭代
var result = db.sql("SELECT * FROM players ORDER BY score DESC");
var row;
while (!(row = result.next()).done) {
  console.log(row.value.name + ": " + row.value.score);
}
```

### 更新数据

```js
var result = db.sql(
  "UPDATE players SET score = score + ? WHERE name = ?",
  50,
  "Steve",
);
console.log("Updated " + result.affectedRows + " rows");
```

### 删除数据

```js
db.sql("DELETE FROM players WHERE score < ?", 10);
// tagged template 风格
db.sql`DELETE FROM players WHERE score < ${10}`;
```

### Thenable 模式

```js
db.sql("SELECT * FROM players").then(
  function (rows) {
    console.log("Total players: " + rows.length);
  },
  function (err) {
    console.error(err);
  },
);
```

## 完整示例：排行榜

```js
// 初始化表
db.sql(
  "CREATE TABLE IF NOT EXISTS leaderboard (player TEXT PRIMARY KEY, score INTEGER, updated INTEGER)",
);

// 记录分数
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

// 获取 Top 10
function getTop10() {
  return db.sql(
    "SELECT player, score FROM leaderboard ORDER BY score DESC LIMIT 10",
  ).rows;
}

// 获取玩家排名
function getRank(playerName) {
  var row = db.sql(
    "SELECT COUNT(*) + 1 AS rank FROM leaderboard WHERE score > (SELECT score FROM leaderboard WHERE player = ?)",
    playerName,
  ).firstRow;
  return row ? row.rank : 0;
}

// 使用
recordScore("Steve", 500);
recordScore("Alex", 800);
recordScore("Steve", 600); // 更新

var top = getTop10();
for (var i = 0; i < top.length; i++) {
  console.log(i + 1 + ". " + top[i].player + " - " + top[i].score);
}

console.log("Steve rank: " + getRank("Steve"));
```

## 完整示例：玩家数据持久化

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

## 与 storage 的对比

|      | `db` (SQLite)                 | `storage` (JSON)                |
| ---- | ----------------------------- | ------------------------------- |
| 查询 | SQL WHERE/JOIN/ORDER BY/LIMIT | 读全量再 JS 过滤                |
| 写入 | 单行增删改，原子              | 整体覆写                        |
| 适合 | 排行榜、经济、日志、关系数据  | 配置、标记、简单键值            |
| 文件 | `data/<project>.db`           | `storage/<project>/<name>.json` |
| 并发 | 天然安全 (WAL 模式)           | 单项目串行够用                  |

## Tagged Template 语法

使用 `` db.sql`...` `` 语法时，TypeScript/ES6 的模板字面量在编译后自动转换为 `?` 占位符调用：

```ts
// TypeScript 源码
db.sql`SELECT * FROM players WHERE score > ${minScore} AND name = ${playerName}`;

// 编译后
db.sql(
  ["SELECT * FROM players WHERE score > ", " AND name = ", ""],
  minScore,
  playerName,
);
```

::: warning
只有值用 `${}`，标识符（表名、列名）不能做绑定参数。
:::

```ts
// ✅ 正确 — 表名硬编码在模板字符串中
db.sql`SELECT * FROM players WHERE name = ${name}`;
```

::: danger 常见错误
表名不能用占位符，会报 SQL syntax error：

```js
// ❌ 错误
db.sql`SELECT * FROM ${table} WHERE name = ${name}`;
```

:::

## Rhino 兼容性注意事项

Box3JS 使用 Rhino 1.9.1 引擎。**TypeScript 项目用 `npm run build` 编译后，以下特性均可直接使用**（Babel 插件自动转为 Rhino 兼容代码）：

| 特性                                                             | 编译方式                                          |
| ---------------------------------------------------------------- | ------------------------------------------------- |
| 箭头函数 `(x) => x + 1`                                          | Babel `@babel/preset-env`                         |
| 模板字面量 `` `Hello ${name}` ``                                 | `rhinoTemplatePlugin`                             |
| `for...of` (JS 数组 + Java ArrayList)                            | `rhinoForOfPlugin` → 索引 for 循环 + `.toArray()` |
| `.map()` `.filter()` `.forEach()` `.find()` `.some()` `.every()` | `rhinoArrayMethodsPlugin` → IIFE + for 循环       |
| `const` / `let`                                                  | Babel `@babel/preset-env`                         |
| 解构 `const { x, y } = obj`                                      | Babel `@babel/preset-env`                         |

**纯 JS 脚本注意事项：**

- `result.rows` 返回 `NativeArray`，不支持 ES5 数组方法，请使用 for 循环。
- 箭头函数、模板字面量、for...of 等需要用 TypeScript 编译后才能使用。

## 注意事项

- 数据库文件自动创建，无需手动初始化
- 项目停止/移除时自动关闭连接
- 参数使用 `?` 占位符，不要直接拼接 SQL 字符串（防止 SQL 注入）
- SQLite 使用动态类型，整数和浮点数会自动适配
- BLOB 数据通过 `Uint8Array` 传入/传出
