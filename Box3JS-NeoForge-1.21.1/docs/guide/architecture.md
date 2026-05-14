# Box3JS 运行原理

本文深入讲解 Box3JS 的内部架构：JS 引擎如何嵌入 Minecraft、作用域如何管理、构建管线如何工作、网络通信如何实现。

## 目录

1. [整体架构](#整体架构)
2. [Rhino 引擎](#rhino-引擎)
3. [作用域与隔离](#作用域与隔离)
4. [全局对象注入](#全局对象注入)
5. [事件回调机制](#事件回调机制)
6. [构建管线](#构建管线)
7. [网络通信](#网络通信)
8. [沙盒系统](#沙盒系统)
9. [文件监控与热重载](#文件监控与热重载)
10. [编译发布模式](#编译发布模式)

---

## 整体架构

```
                          ┌──────────────────────────┐
                          │     Minecraft Server      │
                          │        (NeoForge)         │
                          └──────────┬───────────────┘
                                     │
                          ┌──────────▼───────────────┐
                          │     Box3JS.java           │
                          │  @Mod 入口                 │
                          │  订阅 NeoForge 事件        │
                          │  转发到 JS 引擎            │
                          └──────────┬───────────────┘
                                     │
              ┌──────────────────────┼──────────────────────┐
              │                      │                      │
    ┌─────────▼─────────┐  ┌────────▼────────┐  ┌─────────▼─────────┐
    │ Box3ScriptEngine  │  │ Box3JSClient    │  │ Box3Script        │
    │ (服务端引擎)       │  │ Engine (客户端)  │  │ Compiler (JAR)    │
    │ - 加载脚本         │  │ - 客户端脚本     │  │ - 注册表代码生成  │
    │ - 管理作用域       │  │ - UI/输入/音效   │  │ - JAR 打包        │
    │ - 事件分发         │  │ - 网络接收       │  │                    │
    └─────────┬─────────┘  └────────┬────────┘  └──────────────────┘
              │                      │
    ┌─────────▼──────────────────────▼─────────┐
    │          Mozilla Rhino 1.9.1              │
    │          (JS 引擎，运行在 JVM 内)          │
    └─────────┬────────────────────────────────┘
              │
    ┌─────────▼─────────┐
    │    Java API 层     │
    │  world/entity/     │
    │  player/voxels/    │
    │  storage/db/http   │
    └───────────────────┘
```

### 关键包结构

```
com.box3lab.box3js
├── Box3JS.java                  ← @Mod 入口
├── script/                      ← 服务端引擎
│   ├── Box3ScriptEngine.java    ← Rhino 引擎管理
│   ├── Box3ScriptCommand.java   ← /box3script 命令
│   ├── Box3ScriptConfig.java    ← 配置文件
│   ├── Box3ScriptSandbox.java   ← 沙盒回滚
│   ├── Box3ScriptWatcher.java   ← 文件监控
│   ├── Box3ScriptUtils.java     ← 公共工具
│   ├── Box3JSEventBus.java      ← 事件回调存储
│   ├── Box3JSCallbacks.java     ← 回调接口定义
│   ├── Box3JSWorld.java         ← world.* API
│   ├── Box3JSEntity.java        ← entity.* API
│   ├── Box3JSPlayer.java        ← player.* API
│   ├── Box3JSVoxels.java        ← voxels.* API
│   ├── Box3JSStorage.java       ← JSON 持久化
│   ├── Box3JSDatabase.java      ← SQLite
│   └── ...
├── client/                      ← 客户端引擎
│   ├── Box3JSClientEngine.java  ← 客户端 Rhino 实例
│   └── ...
└── standalone/                  ← JAR 编译发布
    └── ...
```

---

## Rhino 引擎

### 为什么是 Rhino

| 引擎 | 类型 | 速度 | JVM 集成 | ES 版本 |
|------|------|------|---------|---------|
| Mozilla Rhino | 解释型 | 中 | 原生（Java 实现） | ES5 |
| GraalJS | JIT | 快 | 需要单独配置 | ES2023 |
| Nashorn | JIT | 快 | JDK 内置（已移除） | ES6 |

选择 Rhino 的原因：
- **纯 Java 实现**，嵌入 JVM 零配置，不增加启动开销
- **成熟稳定**，Minecraft 模组社区广泛验证
- **与 NeoForge 类加载器兼容**，不需要特殊配置
- ES5 限制通过 Babel 编译绕开（源码可用现代 TS 语法）

### 核心流程

```java
// Box3ScriptEngine.java — 初始化简化流程
Context cx = Context.enter();
Scriptable scope = cx.initStandardObjects();

// 1. 注入全局 Java 对象
scope.put("world", scope, worldApi);
scope.put("console", scope, consoleApi);
scope.put("storage", scope, storageApi);
// ...

// 2. 初始化 console JS 代码
cx.evaluateString(scope, Box3ScriptUtils.CONSOLE_INIT_JS, "console-init", 1, null);

// 3. 加载服务端入口脚本
cx.evaluateReader(scope, scriptReader, "server.js", 1, null);
```

### 类型桥接

Java 对象暴露给 JS 时，Rhino 自动处理类型转换：

| Java 类型 | JS 类型 |
|-----------|--------|
| `String` | `string` |
| `int` / `double` | `number` |
| `boolean` | `boolean` |
| `Map<String, Object>` | `object` |
| `List<Object>` | `array` |
| Java 对象（方法调用） | JS 对象 |

Box3JS 返回的多是 **Java 原生对象**（如 `ServerPlayer` 包装器），JS 侧可直接调用方法。复杂的返回值（如 `querySelectorAll`）返回 Java `List`，Rhino 映射为 JS 数组。

---

## 作用域与隔离

### 每个项目独立作用域

```
                        Rhino Context
                             │
              ┌──────────────┼──────────────┐
              │              │              │
        ┌─────▼─────┐  ┌────▼──────┐  ┌────▼──────┐
        │ Scope A   │  │ Scope B   │  │ Scope C   │
        │ "mygame"  │  │ "lobby"   │  │ "survival"│
        │           │  │           │  │           │
        │ var x = 1 │  │ var x = 2 │  │ var x = 3 │
        └───────────┘  └───────────┘  └───────────┘
```

每个项目拥有：
- **独立顶级作用域** — 变量不互相污染
- **独立事件回调列表** — 由 `Box3JSEventBus` 按项目名存储
- **独立存储命名空间** — `storage.getDataStorage("coins")` 每个项目读自己的数据
- **独立沙盒追踪** — 每个项目的方块/实体修改单独追踪

### 清理机制

停止项目时：
1. `Box3JSEventBus` 清除该项目所有事件回调
2. 清理该项目创建的计分板/BossBar/队伍
3. 如果沙盒开启，回滚所有方块和实体修改
4. 释放 Rhino scope，GC 回收

---

## 全局对象注入

### 注入流程

```
Box3ScriptEngine.setupScope(scope)
│
├── scope.put("world",       scope, new Box3JSWorld(...))
├── scope.put("voxels",      scope, new Box3JSVoxels(...))
├── scope.put("storage",     scope, new Box3JSStorage(...))
├── scope.put("db",          scope, new Box3JSDatabase(...))
├── scope.put("http",        scope, new Box3JSHttp(...))
├── scope.put("remoteChannel", scope, new Box3JSRemoteChannel(...))
├── scope.put("console",     scope, new Box3JSConsole(...))
│
├── scope.put("GameVector3",     scope, GameVector3.class)
├── scope.put("GameBounds3",     scope, GameBounds3.class)
├── scope.put("GameRGBColor",    scope, GameRGBColor.class)
├── scope.put("GameRGBAColor",   scope, GameRGBAColor.class)
├── scope.put("GameQuaternion",  scope, GameQuaternion.class)
│
└── cx.evaluateString(scope, CONSOLE_INIT_JS, ...)  ← 初始化 console JS 对象
```

### 客户端注入

```
Box3JSClientEngine.init(scope)
│
├── scope.put("audio",    scope, audioObj)
├── scope.put("client",   scope, clientObj)    ← onTick 生命周期
├── scope.put("input",    scope, inputObj)     ← 键盘检测
├── scope.put("ui",       scope, uiObj)        ← 屏幕 UI
├── scope.put("chat",     scope, chatObj)      ← 聊天收发
├── scope.put("storage",  scope, clientStorage)
├── scope.put("db",       scope, clientDb)     ← 带降级处理
├── scope.put("http",     scope, clientHttp)
├── scope.put("remoteChannel", scope, remoteChannel)
├── scope.put("console",  scope, Box3JSConsole)
│
└── cx.evaluateString(scope, CONSOLE_INIT_JS, ...)
```

### 为什么 console 需要 JS 初始化

Java 的 `Box3JSConsole` 方法签名为 `log(Object... args)`（varargs）。Rhino 在 JS 侧直接调用时参数传递有问题，因此通过 JS 包裹一层：

```js
// CONSOLE_INIT_JS — 注入到每个 scope
console = {
  log: function() { return _jConsole.log.apply(_jConsole, arguments); },
  debug: function() { return _jConsole.debug.apply(_jConsole, arguments); },
  warn: function() { return _jConsole.warn.apply(_jConsole, arguments); },
  error: function() { return _jConsole.error.apply(_jConsole, arguments); },
  // ...
};
```

`.apply()` 确保多个参数正确传递给 Java varargs 方法。

---

## 事件回调机制

### 完整链路

```
Minecraft 事件发生
        │
        ▼
Box3JS.java (NeoForge 事件总线)
  │  onPlayerJoin / onEntityDeath / onServerTick ...
  │
  ▼
Box3ScriptEngine.fireCallback(eventType, data)
  │  遍历所有已启用项目
  │  对每个项目 → executor.submit(task)
  │
  ▼
Box3JSEventBus.getCallbacks(project, eventType)
  │  返回该项目注册的所有回调
  │
  ▼
Rhino Context: 依次调用每个回调
  Function.call(cx, scope, scope, args)
```

### 回调存储

```java
// Box3JSEventBus — 核心数据结构
Map<String, Map<String, List<Consumer<Object[]>>>> projectCallbacks;
//   │          │          │
//   │          │          └── 回调函数列表
//   │          └── 事件类型 ("playerJoin", "chat", "tick", ...)
//   └── 项目名 ("mygame", "lobby", ...)
```

每个事件类型独立维护回调列表，停止项目时批量清理。

### 回调注册示例

```js
// JS 侧
let token = world.onPlayerJoin((entity, tick) => {
  // 处理玩家加入
});

// 内部执行：
// 1. world.onPlayerJoin 调用 Java 方法
// 2. Box3JSWorld.java 将 callback + Function 存储到 Box3JSEventBus
// 3. Box3JS.java 的 PlayerLoggedInEvent 处理器检测到 join
// 4. 调用 Box3ScriptEngine.fireCallback("playerJoin", entity, tick)
// 5. 引擎找到该项目注册的所有 playerJoin 回调，依次执行
```

### GameEventHandlerToken

```js
let token = world.onTick(() => { ... });
token.cancel();   // 取消监听
token.active();   // 是否仍在活跃
```

Java 端：
```java
public class GameEventHandlerToken {
    private boolean active = true;
    public void cancel()  { /* 从 Box3JSEventBus 移除 */ }
    public boolean active() { return this.active; }
}
```

---

## 构建管线

```
src/server/app.ts (TypeScript + ES2020 语法)
        │
        ▼
┌─────────────────────┐
│  Babel              │
│  @babel/preset-     │
│  typescript         │
│                     │
│  class → function   │
│  let/const → var    │
│  => → function(){}  │
│  `` → "" +          │
└────────┬────────────┘
         │
         ▼  ES5 JavaScript
┌─────────────────────┐
│  esbuild            │
│  bundle             │
│                     │
│  合并多个 .ts 文件   │
│  为一个 .js 文件    │
└────────┬────────────┘
         │
         ▼
    dist/server.js
```

### 为什么需要两步

1. **Babel**: Rhino 1.9.1 只支持 ES5。Babel 把 TS + 现代语法转换为 ES5
2. **esbuild**: 合并多文件为一个 bundle（Rhino 的 `require()` 支持有限）

### build.mjs 核心逻辑

```js
// 简化版
import { build } from "esbuild";
import babel from "@babel/core";

// 1. Babel: TS → ES5 JS
const es5Code = babel.transformSync(tsCode, {
  presets: ["@babel/preset-typescript"],
  targets: { rhino: "1.9.1" }
});

// 2. esbuild: bundle
await build({
  entryPoints: ["src/server/app.ts"],
  bundle: true,
  outfile: "dist/server.js",
  target: "es5",
  format: "iife"
});
```

---

## 网络通信

### remoteChannel 架构

```
┌──────────────────────┐         ┌──────────────────────┐
│   Server (Java)      │         │   Client (Java)      │
│                      │         │                      │
│  Box3JSRemoteChannel │  ────→  │  Box3JSClientEngine  │
│  .sendClientEvent()  │ 包负载   │  .onPayload()        │
│  .broadcastClientEvt │         │                      │
│  .onServerEvent()    │  ←────  │  remoteChannel       │
│                      │ 包负载   │  .sendServerEvent()  │
└──────────────────────┘         └──────────────────────┘
         │                                  │
         │  NeoForge CustomPayload           │
         │  (网络数据包)                     │
         └─────────────┬───────────────────┘
                       │
               ┌───────▼──────┐
               │  Network     │
               │  Protocol    │
               └──────────────┘
```

### 数据流

**服务端 → 客户端:**
```
JS: remoteChannel.sendClientEvent(player, { type: "boss_bar", hp: 50 })
  → Box3JSRemoteChannel.java
  → JSON.stringify(eventData)
  → Box3JSNetwork.sendToPlayer(player, jsonBytes)
  → NeoForge CustomPayload
  → Client recieves
  → Box3JSClientEngine.onPayload(jsonBytes)
  → JSON.parse
  → JS: remoteChannel.onClientEvent handler receives { tick, args }
```

**客户端 → 服务端:**
```
JS: remoteChannel.sendServerEvent({ key: "space" })
  → Box3JSClientEngine
  → JSON.stringify
  → NeoForge CustomPayload to Server
  → Box3JSNetwork.onPayload(jsonBytes)
  → Box3ScriptEngine.fireCallback("remoteChannel", ...)
  → JS: remoteChannel.onServerEvent handler receives { tick, entity, args }
```

### 数据格式

所有跨网络传输的数据必须是 **JSON 可序列化**的：
- `string`, `number`, `boolean`, `null`
- 纯对象 `{ key: value }`
- 数组 `[1, 2, 3]`
- 不支持：函数、`GameVector3` 实例、Java 对象

---

## 沙盒系统

### 工作原理

```
/box3script sandbox mygame  ← 开启沙盒
        │
        ▼
Box3ScriptSandbox.start("mygame")
  │  开始追踪该项目对世界的所有修改
  │
  ├── voxels.setVoxel()   → 记录旧方块 → 新方块
  ├── voxels.fillVoxel()  → 记录区域内所有旧方块
  ├── world.spawnEntity() → 记录生成的实体
  └── world.setBlock()    → 同 setVoxel

/box3script sandbox mygame  ← 关闭沙盒
        │
        ▼
Box3ScriptSandbox.stop("mygame")
  │  按相反顺序回滚所有修改
  ├── 移除追踪的实体
  ├── 恢复方块到旧状态
  └── 清除追踪数据
```

### 追踪数据结构

```java
// 简化版
class SandboxTracker {
    Map<BlockPos, VoxelState> originalVoxels;  // 旧方块记录
    List<Entity> spawnedEntities;               // 生成的实体
    // 回滚时: 恢复 voxels → 移除 entities
}
```

### 使用场景

- **新脚本安全测试** — 不确定脚本会做什么，先沙盒测试
- **玩家测试** — 让玩家试玩新功能，结束时回滚不影响正式服
- **调试** — 测试有破坏性的操作（explode、fillVoxel）

---

## 文件监控与热重载

### 工作流程

```
/box3script watch  ← 开启文件监控
        │
        ▼
Box3ScriptWatcher 启动
  │  使用 Java WatchService 监控 config/box3/script/ 目录
  │
  ▼
检测到 .js 文件变更 (dist/server.js 重新生成)
  │  防抖: 300ms 内多次变更合并为一次
  │
  ▼
自动执行: /box3script reload <project>
  │  停止 → 重新加载脚本 → 重新注册回调
  │
  ▼
新代码生效 (无需手动 reload)
```

### 技术要点

- 监控的是 `dist/` 下的编译产物（`.js`），不是 `src/` 下的源码
- 300ms 防抖避免 esbuild 写入多个 chunk 时多次 reload
- reload 是原子的：先停止旧脚本（清理回调 + 资源），再加载新脚本

---

## 编译发布模式

### `/box3script compile` 流程

```
输入: config/box3/script/mygame/
        │
        ▼
┌─────────────────────────────────────┐
│  Box3ScriptCompiler                  │
│                                      │
│  1. 读取 package.json 元数据         │
│  2. 读取 dist/server.js              │
│  3. 读取 dist/client.js              │
│  4. 读取 registries/*.json           │
│  5. 生成 Java 注册代码 (RegistryGen) │
│  6. 编译 Java 源码                   │
│  7. 打包为 JAR                       │
└─────────────┬───────────────────────┘
              │
              ▼
输出: mygame-1.0.0.jar
        │
        ├── META-INF/mods.toml         ← 模组元数据
        ├── META-INF/neoforge.mods.toml
        ├── com/example/mygame/
        │   ├── MygameMod.java         ← @Mod 入口
        │   └── registries/            ← 自动生成的注册类
        ├── assets/mygame/
        │   └── box3js/scripts/
        │       ├── server.js          ← 编译后的服务端脚本
        │       └── client.js          ← 编译后的客户端脚本
        └── (纹理/模型/音效资源)
```

### 注册表代码生成

`Box3JSRegistryGen.java` 读取 JSON 配置生成 Java 代码：

```json
// registries/blocks.json
{
  "ruby_block": {
    "displayName": "Ruby Block",
    "sound": "metal",
    "mapColor": "color_red",
    "destroyTime": 5.0,
    "creativeTab": "my_tab"
  }
}
```

↓ 编译时生成 ↓

```java
// 自动生成的 Java 代码
public static final DeferredBlock<Block> RUBY_BLOCK =
    BLOCKS.register("ruby_block", () -> new Block(Block.Properties.of()
        .sound(SoundType.METAL)
        .mapColor(MapColor.COLOR_RED)
        .destroyTime(5.0f)));
```

**注意：** `registries` 只在编译 JAR 模式下可用。解释模式（`/box3script start`）中 `registries` 为 `undefined`。

---

## 性能考虑

### 开销来源

| 层 | 开销 | 说明 |
|----|------|------|
| NeoForge 事件分发 | 低 | 原版 Minecraft 也在用 |
| Box3JS 事件转发 | 中 | Java → JS 参数装箱 |
| Rhino 执行 | **中-高** | 解释执行，无 JIT |
| JS 代码本身 | 取决于写法 | `onTick` 中的循环最敏感 |

### 性能建议

1. **`onTick` 中避免大循环** — 遍历所有实体请在条件触发时做，不要每 tick 做
2. **缓存查询结果** — 不要把 `querySelectorAll` 放在每 tick
3. **用 `setInterval` 代替 `onTick`** — 如果不需要 20次/秒，用更长的间隔
4. **避免 JS ↔ Java 频繁跨越** — 批量操作比逐个操作快

一个跑酷脚本的性能消耗通常 < 0.5ms/tick，对服务器 TPS 无影响。
