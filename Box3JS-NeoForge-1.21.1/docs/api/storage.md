# storage — 数据存储 API

`storage` 提供 JSON 文件持久化存储，带内存缓存加速读写。数据保存在 `config/box3/storage/<项目名>/` 目录下，每个项目自动拥有独立命名空间。

## 获取存储实例

### storage.getDataStorage(name)

✅ Box3 API | 获取或创建一个命名存储。同名存储返回同一实例。

### storage.getGroupStorage(name)

✅ Box3 API | 获取**跨项目共享**存储。所有项目通过同一 `name` 访问同一份数据（底层使用 `__shared__/` 命名空间）。适合做全服排行榜、全局配置等。

```js
var store = storage.getDataStorage("leaderboard");
var config = storage.getDataStorage("settings");
```

## 读写操作

### store.set(key, value)

✅ Box3 API | 存储键值对。`value` 可以是字符串、数字、对象（自动 JSON 序列化）。

### store.get(key)

✅ Box3 API | 获取值。返回存储时的原始类型。

```js
store.set("highScore", 100);
store.set("lastWinner", "Steve");
store.set("config", { difficulty: "hard", maxPlayers: 10 });

var score = store.get("highScore"); // 100 (number)
var winner = store.get("lastWinner"); // "Steve" (string)
var cfg = store.get("config"); // {difficulty: "hard", ...} (object)
```

> **注意：** 当数据从磁盘重新加载后，复杂对象会以普通 JSON 对象形式返回（例如 `Map` 风格对象），请避免依赖原始 JS 原型方法。

### store.keys()

✅ Box3 API | 返回所有 key 的数组。

```js
var keys = store.keys();
for (var i = 0; i < keys.length; i++) {
  console.log(keys[i] + " = " + store.get(keys[i]));
}
```

## 更新与删除

### store.update(key, handler)

✅ Box3 API | 回调式更新值。`handler` 接收当前值，返回新值。类似于 `store.set(key, handler(store.get(key)))`，但保证原子性。

```js
store.set("counter", 0);
store.update("counter", function (current) {
  return current + 1; // 原子递增
});
```

### store.remove(key)

✅ Box3 API | 删除指定 key，并返回被删除的旧值（不存在时返回 `null`）。

### store.destroy()

✅ Box3 API | 删除整个存储文件（同时清除内存缓存）。

```js
store.remove("tempKey");
store.destroy(); // 删除该存储的所有数据
```

## 数值操作

### store.increment(key, delta)

✅ Box3 API | 递增数值。`delta` 默认为 1，返回递增后的新值。

```js
store.set("kills", 0);
store.increment("kills"); // kills = 1
store.increment("kills", 5); // kills = 6
store.increment("kills", -2); // kills = 4
```

## 分页查询

### store.list(options)

✅ Box3 API | 游标分页查询。`options` 对象支持的字段：

| 字段               | 类型    | 说明                                |
| ------------------ | ------- | ----------------------------------- |
| `cursor`           | number  | 起始游标（页码 × pageSize）         |
| `pageSize`         | number  | 每页条目数（1–100，默认 100）       |
| `ascending`        | boolean | 是否升序排列                        |
| `max`              | number  | 值的上限过滤                        |
| `min`              | number  | 值的下限过滤                        |
| `constraintTarget` | string  | 排序/过滤的嵌套路径（如 `"a.b.c"`） |

返回 `QueryList` 分页对象：

| 属性/方法                 | 说明               |
| ------------------------- | ------------------ |
| `result.isLastPage`       | 是否最后一页       |
| `result.getCurrentPage()` | 返回当前页条目数组 |
| `result.nextPage()`       | 移到下一页         |

每条条目为 `{key, value, updateTime, createTime, version}`。

```js
var result = store.list({ pageSize: 10, ascending: false });

// 遍历当前页
var page = result.getCurrentPage();
for (var i = 0; i < page.length; i++) {
  console.log(page[i].key + ": " + page[i].value);
}

// 下一页
if (!result.isLastPage) {
  result.nextPage();
}
```

## 内存缓存与持久化

所有 `GameDataStorage` 实例共享一个内存缓存（`ConcurrentHashMap`）。首次访问时从磁盘加载 JSON，后续读写均在内存中操作，每次写操作（`set`/`update`/`remove`/`increment`）同步刷盘。

- **同名存储**：同一文件路径多次 `getDataStorage` 返回共享同一份内存数据，避免重复 I/O
- **项目隔离**：`getDataStorage("scores")` 在不同项目中访问不同文件（自动添加项目名前缀）
- **跨项目共享**：`getGroupStorage("leaderboard")` 所有项目访问同一个 `__shared__/leaderboard.json`

## 完整示例：排行榜

```js
// 跨项目共享排行榜 — 所有项目读写同一份数据
var lb = storage.getGroupStorage("leaderboard");

// 保存成绩
function saveScore(name, time) {
  lb.set(name, time);
}

saveScore("Steve", 12345);
saveScore("Alex", 9800);

// 遍历所有条目
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

全部 ✅ Box3 API。
