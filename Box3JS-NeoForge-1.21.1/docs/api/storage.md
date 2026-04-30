# storage — 数据存储 API

`storage` 提供 JSON 文件持久化存储，项目间数据隔离。数据保存在 `config/box3/data/<项目名>/` 目录下。

---

## 获取存储实例

### storage.getDataStorage(name)

✅ Box3 API | 获取或创建一个命名存储。同名存储返回同一实例。

### storage.getGroupStorage(name)

✅ Box3 API | 获取组存储（目前与 `getDataStorage` 行为一致）。

```js
var store = storage.getDataStorage("leaderboard");
var config = storage.getDataStorage("settings");
```

---

## 读写操作

### store.set(key, value)

✅ Box3 API | 存储键值对。`value` 可以是字符串、数字、对象（自动 JSON 序列化）。

### store.get(key)

✅ Box3 API | 获取值。返回存储时的原始类型。

```js
store.set("highScore", 100);
store.set("lastWinner", "Steve");
store.set("config", { difficulty: "hard", maxPlayers: 10 });

var score = store.get("highScore");        // 100 (number)
var winner = store.get("lastWinner");      // "Steve" (string)
var cfg = store.get("config");             // {difficulty: "hard", ...} (object — 需要 JSON.parse)
```

> **注意：** 存储对象时，`store.get()` 返回 JSON 字符串，需要手动 `JSON.parse()`：
> ```js
> var cfg = JSON.parse(store.get("config"));
> console.log(cfg.difficulty); // "hard"
> ```

### store.keys()

✅ Box3 API | 返回所有 key 的数组。

```js
var keys = store.keys();
for (var i = 0; i < keys.length; i++) {
    console.log(keys[i] + " = " + store.get(keys[i]));
}
```

---

## 更新与删除

### store.update(key, handler)

✅ Box3 API | 回调式更新值。`handler` 接收当前值，返回新值。类似于 `store.set(key, handler(store.get(key)))`，但保证原子性。

```js
store.set("counter", 0);
store.update("counter", function(current) {
    return current + 1; // 原子递增
});
```

### store.remove(key)

✅ Box3 API | 删除指定 key。

### store.destroy()

✅ Box3 API | 删除整个存储文件。

```js
store.remove("tempKey");
store.destroy(); // 删除该存储的所有数据
```

---

## 数值操作

### store.increment(key, delta)

✅ Box3 API | 递增数值。`delta` 默认为 1。

```js
store.set("kills", 0);
store.increment("kills");     // kills = 1
store.increment("kills", 5);  // kills = 6
store.increment("kills", -2); // kills = 4
```

---

## 分页查询

### store.list(options)

✅ Box3 API | 分页排序查询。`options` 对象支持的字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `limit` | number | 返回最多条目数 |
| `offset` | number | 跳过的条目数 |
| `sort` | string | 排序方式，`"asc"` 或 `"desc"`（按 key 排序） |
| `filter` | string | 过滤条件（前缀匹配） |

返回 `[{key, value}]` 数组。

```js
// 返回前 10 条
var top10 = store.list({ limit: 10, sort: "desc" });

// 第 11–20 条
var page2 = store.list({ limit: 10, offset: 10 });

// 查找以 "player_" 开头的 key
var playerData = store.list({ filter: "player_" });

for (var i = 0; i < top10.length; i++) {
    console.log(top10[i].key + ": " + top10[i].value);
}
```

---

## 完整示例：排行榜

```js
var lb = storage.getDataStorage("leaderboard");

// 保存新成绩
function saveScore(name, time) {
    var entry = JSON.stringify({
        name: name,
        time: time,
        date: new Date().toISOString()
    });
    lb.set("entry_" + Date.now(), entry);
}

// 获取排行榜
function getLeaderboard() {
    var entries = lb.list({ limit: 10, sort: "asc" });
    var result = [];
    for (var i = 0; i < entries.length; i++) {
        result.push(JSON.parse(entries[i].value));
    }
    result.sort(function(a, b) { return a.time - b.time; });
    return result;
}

saveScore("Steve", 12345);
saveScore("Alex", 9800);

var top = getLeaderboard();
for (var i = 0; i < top.length; i++) {
    console.log((i + 1) + ". " + top[i].name + " - " + top[i].time);
}
```

---

全部 ✅ Box3 API。
