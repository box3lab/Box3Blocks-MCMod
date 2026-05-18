# 常见问题与故障排查

## 脚本加载

### Q: 脚本不执行，`/box3script` 显示项目为 ○（未加载）

检查顺序：

1. `npm run build` 是否成功？`dist/` 下是否生成了 `server.js`？
2. `/box3script start <项目名>` 是否执行过？
3. 服务端控制台是否有 `[Box3JS]` 前缀的错误日志？
4. 项目名是否正确？`/box3script` 查看已加载项目列表。

### Q: reload 后代码没变化

- 确认 `npm run build` 在 reload 之前执行
- 确认 build 输出到了正确的 `dist/` 目录
- 开启文件监控：`/box3script watch` 自动监听 build 变化
- 如果监控已开启仍不生效，手动 `/box3script reload <项目名>`

### Q: 热重载会丢失数据吗？

- **不丢失的：** 计分板分数、storage JSON 文件、SQLite 数据、世界方块状态（沙盒关闭时）
- **会丢失的：** JavaScript 内存变量（`let/var`）、`Map`/`Set`、定时器（`setTimeout`/`setInterval` 会被清除）
- **建议：** 需要持久化的数据用 `storage` 或计分板存储，不要依赖内存变量

### Q: `/box3script start` 和 `/box3script reload` 有什么区别？

- `start` — 首次加载脚本（或 stop 后重新启用），初始化所有全局对象和事件回调
- `reload` — 已经加载过的脚本重新载入，先卸载旧脚本（清理回调+资源）再加载新脚本
- 日常开发只用 `reload` 就够了。`start` 仅用于 stop 之后重新启用。

## 构建

### Q: `npm run build` 报错 "Cannot find module"

```bash
npm install
```

`npm install` 需要在克隆项目或创建项目后执行一次。之后只需要 `npm run build`。

### Q: TypeScript 报类型错误但运行正常

TypeScript 只检查构建时类型，实际运行时 Rhino 不会做类型检查。修复步骤：

1. 检查 `.d.ts` 中的 API 签名是否正确（`types/server/` 和 `types/client/`）
2. 如果类型确实不对，可以加 `// @ts-expect-error` 临时跳过
3. 同时考虑修复 `.d.ts` 文件

### Q: `npm run build` 成功但脚本报语法错误

Babel 编译为 ES5 后，目标引擎是 Rhino 1.9.1（仅支持 ES5）。常见问题：

- 不要在 `src/` 中使用 `async/await`（Babel 不会完整编译为 ES5）
- 不要使用 `Promise`（Rhino 1.9.1 不支持）
- `let`/`const`、`=>` 箭头函数、模板字符串由 Babel 处理，可以放心使用

## 运行时

### Q: `console.log` 输出在哪里？

- **服务端脚本：** 服务端控制台（`logs/latest.log`），格式 `[Box3JS] [项目名] message`
- **客户端脚本：** 客户端日志（启动器日志或 `.minecraft/logs/`）

### Q: 如何调试脚本？

1. **加 `console.log`** — 最直接的调试方式
2. **看服务端控制台** — Java 异常会包含 JS 文件名和行号
3. **沙盒测试** — `/box3script sandbox <项目名>` 开启后所有修改可回滚
4. **缩小范围** — 注释掉大部分代码，逐步取消注释定位问题
5. **`/box3script`** — 查看项目是否 `◉`（已加载）

### Q: API 报 "xxx is not a function"

先确认：

1. 方法名拼写是否正确？参考 [API 文档](../api/README.md)
2. 所在全局对象是否正确？如 `world.say()` 不是 `server.say()`
3. 是否需要 `new`？如 `new GameVector3(x, y, z)`
4. 是否在服务端脚本中调用了客户端 API？（`audio`/`input`/`ui`/`chat` 只能用在 `src/client/`）

### Q: 脚本执行很慢/服务器卡顿

Rhino 是解释型引擎（无 JIT），需要优化：

- **不在 onTick 中做密集操作** — 用 `setInterval` 降低频率
- **缓存查询结果** — 不要每到 tick 都调用 `querySelectorAll`
- **减少 JS ↔ Java 交互** — 批处理比逐个调用快
- **避免大循环中的 `console.log`** — 控制台输出有开销

### Q: 多个脚本项目之间如何共享数据？

- **跨脚本通信：** `world.sendMessage("项目名", data)` + `world.onMessage()`
- **共享计分板：** 不同项目可以读写同一个计分板
- **共享数据库：** SQLite 操作同一个数据库文件
- **不共享：** JS 变量（每个项目独立的 Rhino scope）

### Q: reload 后旧的定时器还在吗？

不在。`reload` 会清除该项目的所有回调、定时器和事件监听。如果需要在 reload 后重新启动定时任务，在脚本顶层（全局作用域）注册它们——reload 后代码会重新执行，定时器也会重新注册。

## 数据库

### Q: `db.sql()` 报 "SQLite driver not available"

安装 `minecraft-sqlite-jdbc` 模组。不用 `db` 的话可以不装。安装后重启服务端。

### Q: 如何防止 SQL 注入？

用参数化查询（推荐）：

```js
// ✅ 安全：参数化
db.sql("SELECT * FROM t WHERE name = ?", userInput);

// ✅ 安全：tagged template
db.sql(["SELECT * FROM t WHERE name = '", "'"], userInput);

// ❌ 危险：字符串拼接
db.sql("SELECT * FROM t WHERE name = '" + userInput + "'");
```

## HTTP

### Q: HTTP 请求失败/超时

- 服务端/客户端能否访问目标 URL？（防火墙？）
- `timeout` 是否够长？默认 5000ms
- 用 `async: true` + `onError` 回调查看具体错误
- HTTPS 证书问题可能出现在 Java 环境，需要信任证书或使用 HTTP

### Q: 同步 vs 异步 HTTP 怎么选择？

- **同步 `http.fetch`：** 简单、立即拿到结果，但会阻塞游戏 tick（可能导致短暂卡顿）
- **异步 `{ async: true, onResponse: ... }`：** 不阻塞游戏，但需要用回调处理结果
- **建议：** 耗时短的请求（< 100ms）用同步，耗时长的用异步。心跳/上报类请求用异步。

## 客户端

### Q: 客户端脚本不运行

1. 玩家客户端是否安装了 Box3JS mod？
2. 服务端项目是否启用了客户端脚本？（`src/client/app.ts` 存在且 build 有输出 `dist/client.js`）
3. 使用 `/box3script reload` 刷新（客户端会重新接收脚本）
4. 如果不确定，在服务端添加 `remoteChannel` 监听检测 `clientReady` 事件

### Q: 如何检测玩家是否安装了 Box3JS 客户端？

无需手动检测。`remoteChannel.sendClientEvent()` 使用可选数据包（optional payload），未安装 Box3JS 客户端的玩家会自动忽略这些数据包，不会产生任何错误或断线。可以直接发送，安全无副作用。

### Q: 客户端和服务端可以同时使用 `remoteChannel` 吗？

可以。`remoteChannel` 提供双向通道：

- 客户端 → 服务端：`remoteChannel.sendServerEvent()` → `remoteChannel.onServerEvent()`
- 服务端 → 客户端（定向）：`remoteChannel.sendClientEvent(entity, ...)` → `remoteChannel.onClientEvent()`
- 服务端 → 客户端（广播）：`remoteChannel.broadcastClientEvent(...)` → `remoteChannel.onClientEvent()`

### Q: remoteChannel 数据格式限制

传输的数据必须是 JSON 可序列化的：`string`、`number`、`boolean`、`null`、普通对象、数组。

不能传输：函数、`GameVector3` 实例、Java 对象。如果需要传坐标，用 `{ x, y, z }` 格式。

## 部署

### Q: 如何发布我的脚本？

```js
/box3script compile <项目名>
```

生成 `<项目名>-<版本>.jar`，放入 `mods/` 即可。接收方也需要安装 Box3JS 模组（作为运行时依赖）。自定义方块/物品需要客户端也安装 JAR。

### Q: 编译时报 "is not a valid mod file" 或 "Invalid modId"

这是由于项目名（modId）不符合 NeoForge 命名规范。modId 必须匹配：`^[a-z][a-z0-9_]{1,63}$`

- ✅ 首字符必须是**小写字母**
- ✅ 后续字符只能用**小写字母、数字、下划线**
- ✅ 长度 **2–64** 个字符
- ❌ 不能用大写、连字符 `-`、点号 `.` 或其他特殊符号

**修复方法：**

1. 检查 `package.json` 中的 `"name"` 字段
2. 将项目名改为合法的 modId（如 `mygame`、`arena_battle`）
3. 或在编译时指定：`/box3script compile mygame --modId mygame`

### Q: 编译的 JAR 和解释模式有什么区别？

| 特性       | 解释模式         | 编译 JAR                     |
| ---------- | ---------------- | ---------------------------- |
| registries | 不可用           | 可用（自定义方块/物品/音效） |
| 热重载     | ✅               | ❌（需重启）                 |
| 分发       | 复制整个项目目录 | 单个 JAR 文件                |
| 更新       | 直接编辑 JS 文件 | 重新编译                     |

### Q: registries 为什么只在编译模式可用？

自定义方块/物品/音效需要 NeoForge 的 `DeferredRegister`，这必须在模组启动时注册。解释模式没有启动期注册阶段，所以 `registries` 只能在编译为 JAR 时使用。

更多问题请在 [GitHub Issues](https://github.com/box3lab/Box3JS) 提出。
