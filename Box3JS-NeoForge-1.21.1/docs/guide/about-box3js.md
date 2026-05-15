# Box3JS 与神奇代码岛

## 神奇代码岛是什么

[神奇代码岛](https://dao3.fun)（Box3）是一款**多人联机 3D 游戏创作平台**。在这里，用户不需要学游戏引擎，只要会 JavaScript，就能在浏览器里创建竞速、对战、RPG、FPS 甚至 MOBA 等各类 3D 游戏。

核心特点：

- **JavaScript 编程** — 用 JS/TypeScript 写游戏逻辑，零游戏引擎基础
- **多人实时联机** — 平台自带服务器、房间系统、网络同步
- **跨端即玩** — 电脑、手机、平板均可，无需下载
- **社区 UGC** — 所有游戏均由玩家创作。

神奇代码岛为成千上万的年轻创作者提供了第一次"写游戏"的体验。它的 API 设计经过了大量创作者长期验证，形成了一套简洁、直觉、高效的编程模型。

## Box3JS 是什么

Box3JS 是一个**社区驱动的 Minecraft 模组**，它在 Minecraft 服务端内部嵌入了一个完整的 JavaScript 引擎（Mozilla Rhino），让开发者用 TypeScript 编写服务端游戏逻辑，并可选择下发客户端脚本实现按键监听、屏幕 UI 等本地交互。

**Box3JS 不是神奇代码岛的官方产品**，而是由熟悉 Box3 生态的社区开发者创建，延续了神奇代码岛 API 的设计哲学和命名风格。

## 为什么要做 Box3JS MC 版

### 出发点

神奇代码岛的 API 经过了长期打磨，设计得非常出色：

- 全局对象注入，不需要 `import`/`require`
- Tick 制定时器，与游戏世界完美同步
- 统一的事件取消模式（`GameEventHandlerToken`）
- 项目作用域隔离，多脚本互不干扰

但神奇代码岛跑在自己的封闭平台里，创作者无法接触到 Minecraft 生态——那里有更大的玩家社区、更丰富的方块和机制、以及成熟的模组分发体系。

**Box3JS 的核心目标：把神奇代码岛级别的开发体验带进 Minecraft。**

### 谁适合用 Box3JS

| 用户画像                   | 为什么适合                                          |
| -------------------------- | --------------------------------------------------- |
| 神奇代码岛开发者           | API 风格一致，已有技能直接复用，零学习成本          |
| 想给 MC 服务器写玩法的服主 | 不需要学 Java、Gradle、Mixin，写 JS 就行            |
| 编程教育场景               | TypeScript + 热重载 + 沙盒回滚 = 理想的编程教学环境 |
| 不想写 Java 模组的开发者   | 开箱即用的 API 覆盖常用功能，无编译管线负担         |

## Box3JS 的独特优势

### 1. 延续 Box3 的 API 设计

如果你写过神奇代码岛的脚本，Box3JS 的代码你几乎能直接读懂：

```js
// 这段代码在神奇代码岛和 Box3JS 中几乎一模一样
world.onPlayerJoin((entity) => {
  entity.player.directMessage(`§a欢迎 ${entity.player.name}！`);
});

world.onChat((entity, message) => {
  if (message === "!hello") {
    entity.player.directMessage("你好！");
    return false;
  }
  return true;
});
```

API 对比详见 [Box3 API vs Box3JS 对比](../BOX3_API_COMPARISON.md)。

### 2. 真正的 Minecraft 世界

Box3JS 直接操作 Minecraft 的世界——真实的方块、原版实体、完整的游戏机制。你可以：

- 操作真实 MC 方块（`voxels.setVoxel`、`voxels.fillVoxel`）
- 生成原版实体并设置 AI、装备、药水效果
- 使用 Minecraft 的计分板、BossBar、队伍系统
- 执行原版命令（`player.runCommand`）
- 控制天气、时间、世界边界、游戏规则

### 3. 热重载 + 沙盒

- **保存即生效** — 改代码 → `npm run build` → `/box3script reload`，不需要重启服务器
- **沙盒保护** — 测试破坏性操作时开启沙盒，关闭时一键回滚所有修改
- **自动热重载** — 开启 `watch` 后，保存构建产物自动触发 reload

### 4. 独立分发

开发完成后，一键编译为独立 JAR 模组：

```js
/box3script compile mygame
```

生成 `mygame-1.0.0.jar`，放入任意 NeoForge 1.21.1 服务端的 `mods/` 目录即可运行，无需源码，无需构建工具。可上传 CurseForge、Modrinth 分发。

### 5. 双端架构

```text
服务端（权威）              客户端（表现）
world.* / voxels.*          client.* / input.*
entity.* / player.*   ←→    ui.* / audio.* / gui.*
storage / db / http         storage / db / http
     └── remoteChannel ──────┘
```

服务端控制游戏权威逻辑，客户端负责本地表现（UI、音效、按键监听），通过 `remoteChannel` 双向通信。

### 6. TypeScript 完整体验

- 完整的 `.d.ts` 类型声明，VS Code 智能提示
- esbuild + Babel 构建管线，支持现代语法（`const`、箭头函数、模板字符串、`async/await`）
- ESLint 代码检查
- `tsconfig.server.json` / `tsconfig.client.json` 互斥，防止 API 混用

## 与神奇代码岛的差异

Box3JS 并非 1:1 复制 Box3 的 API。差异源于两个平台的根本不同：

| 差异领域   | 神奇代码岛                                  | Box3JS (MC)                                    |
| ---------- | ------------------------------------------- | ---------------------------------------------- |
| 渲染引擎   | 自研 3D 引擎                                | Minecraft 原版渲染                             |
| 物理引擎   | 自研物理                                    | Minecraft 原版物理                             |
| 天气系统   | 独立的雨/雪/雾系统（丰富的参数控制）        | 复用 MC 原版天气（rainDensity/thunderDensity） |
| 光照系统   | 手动/自然光照模式（lightMode/sunFrequency） | 复用 MC 原版光照                               |
| 自定义模型 | 内置编辑器                                  | 需 Resource Pack（MC 机制）                    |
| 数据库     | 内置 KV 存储                                | 内置 KV 存储 + SQLite                          |

**设计原则：** 尽量保持 API 命名和语义一致，但对于 MC 无法支持或与 MC 机制冲突的功能，不强行模拟。详细的 API 对照见 [Box3 API vs Box3JS 对比](../BOX3_API_COMPARISON.md)。

## 下一步

- **从零开始**: [快速开始指南](getting-started.md) — 10 分钟写出第一个 MC 脚本
- **理解原理**: [运行原理](architecture.md) — Rhino 引擎、作用域隔离、构建管线
- **API 速查**: [API 功能速查](../api/README.md) — 按"我想做什么"查找 API
