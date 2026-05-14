# 快速开始：从零到第一个 Box3JS 脚本

本指南面向**零模组开发经验**的读者。你只需要会 JavaScript，就能在 10 分钟内写出第一个 Minecraft 服务端脚本。

## 目录

1. [Box3JS 是什么](#box3js-是什么)
2. [环境搭建](#环境搭建)
3. [创建项目](#创建项目)
4. [第一个脚本](#第一个脚本)
5. [开发循环](#开发循环)
6. [调试技巧](#调试技巧)
7. [发布部署](#发布部署)
8. [下一步](#下一步)

---

## Box3JS 是什么

Box3JS 是一个**服务端脚本引擎模组**（NeoForge 1.21.1）。它在 Minecraft 服务器内嵌入了一个 JavaScript 运行时（Mozilla Rhino），让你用 JS/TypeScript 编写游戏玩法逻辑。

### 能做什么

| 类别 | 示例 |
|------|------|
| 聊天命令 | `!heal`、`!home`、`!shop` |
| 事件响应 | 玩家进服欢迎、死亡惩罚、方块破坏记录 |
| 实体控制 | 生成怪物、设置 AI、自定义 Boss |
| 小游戏 | PvP 竞技场、跑酷、波次刷怪 |
| 世界操作 | 放置/替换方块、填充区域、修改天气时间 |
| 数据持久化 | JSON 存储、SQLite 数据库 |
| 游戏系统 | 计分板、BossBar、队伍、世界边界 |
| HTTP 请求 | 查询 Web API、Webhook 通知 |
| 客户端脚本 | 按键监听、屏幕 UI、客户端音效 |

### 不能做什么

- **渲染自定义模型/粒子** — 需要客户端资源包或 Java 模组
- **添加新方块/物品（实时）** — 需要编译为 JAR 模组（`/box3script compile`）
- **修改原版机制** — 如修改合成表、生物行为，这些需要 Mixin

### 核心设计理念

```
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│ TypeScript   │ ───→ │ Babel ES5    │ ───→ │ Rhino 引擎   │
│ 源码         │      │ 编译         │      │ (JVM 内嵌)   │
└──────────────┘      └──────────────┘      └──────────────┘
                                                    │
                                            ┌───────┴───────┐
                                            │  Minecraft    │
                                            │  NeoForge API │
                                            └───────────────┘
```

- **运行在服务端 JVM 内**，直接调用 Minecraft 和 NeoForge API
- **TypeScript 源码**通过 Babel 编译为 ES5，适配 Rhino 引擎
- **热重载** — 修改代码后不需要重启服务器
- **沙盒隔离** — 每个项目独立作用域，互不影响

---

## 环境搭建

### 你需要

1. **Minecraft 服务端** 安装了 Box3JS + NeoForge 1.21.1
2. **Node.js** 18+ （仅用于本地构建，服务端不需要）
3. 一个文本编辑器（VS Code 推荐）

### 验证安装

进入游戏，执行：

```
/box3script
```

如果看到项目状态面板，说明 Box3JS 已正常运行。

```
══ Box3JS Script Engine ══
  Watch: ○ Inactive    Sandbox: ○ Inactive
  Projects: 0 enabled  |  0 loaded
```

---

## 创建项目

在游戏内执行：

```
/box3script create mygame
```

这会在 `config/box3/script/mygame/` 生成完整的 TypeScript 项目：

```
config/box3/script/mygame/
├── package.json           ← 项目配置（名称、版本、构建依赖）
├── tsconfig.base.json     ← TypeScript 公共编译选项
├── tsconfig.server.json   ← 服务端 TS 配置
├── tsconfig.client.json   ← 客户端 TS 配置
├── build.mjs              ← 构建脚本（esbuild + Babel）
├── eslint.config.mjs      ← ESLint 规则
├── types/
│   ├── shared.d.ts        ← 服务端&客户端共享类型
│   ├── server/
│   │   ├── index.d.ts     ← 服务端类型入口
│   │   ├── server.d.ts
│   │   ├── entity.d.ts
│   │   ├── player.d.ts
│   │   ├── world.d.ts
│   │   └── voxels.d.ts
│   └── client/
│       ├── index.d.ts     ← 客户端类型入口
│       ├── client.d.ts
│       ├── audio.d.ts
│       ├── input.d.ts
│       ├── ui.d.ts
│       ├── chat.d.ts
│       └── gui.d.ts
├── src/
│   ├── server/
│   │   └── app.ts         ← ★ 服务端入口（你写代码的地方）
│   └── client/
│       └── app.ts         ← 客户端入口
├── registries/            ← 自定义内容（方块/物品/音效 JSON）
└── assets/lang/           ← 自定义内容本地化文本
```

### 安装依赖

打开终端，进入项目目录：

```bash
cd config/box3/script/mygame
npm install
```

`npm install` 只需执行一次（安装 esbuild、Babel、TypeScript 等构建工具）。

---

## 第一个脚本

打开 `src/server/app.ts`，清空已有内容，写入：

```js
// 1. 启动时输出日志
console.log("MyGame 脚本已启动！");

// 2. 玩家加入时欢迎
world.onPlayerJoin((entity) => {
  const p = entity.player;
  p.directMessage("§a欢迎 " + p.name + " 来到服务器！");

  // 粒子欢迎特效
  const pos = p.position;
  world.spawnParticleCircle(pos.x, pos.y, pos.z, 1.5, "minecraft:happy_villager", 15);
  world.playSound("minecraft:block.note_block.pling", pos, 1.0, 1.5);
});

// 3. 聊天命令
world.onChat((entity, message) => {
  const p = entity.player;

  if (message === "!hello") {
    p.directMessage("§e你好，" + p.name + "！");
    return false; // 阻止消息显示在聊天栏
  }

  if (message === "!pos") {
    const pos = p.position;
    p.directMessage("§e你的坐标: §f" +
      Math.floor(pos.x) + ", " +
      Math.floor(pos.y) + ", " +
      Math.floor(pos.z));
    return false;
  }

  return true; // 不是命令的消息正常发送
});

// 4. 定时公告
world.setInterval(() => {
  const count = world.querySelectorAll("*").length;
  world.say("§7[公告] §f当前在线: " + count + " 人");
}, 6000); // 6000 ticks = 5 分钟
```

### 关键概念

- **全局对象不需要 import** — `world`、`console`、`player` 等由 Box3JS 注入
- **事件回调返回 false 阻止默认行为** — `onChat` 返回 false 阻止消息广播
- **Tick 是 MC 的时间单位** — 1 秒 = 20 ticks，`setInterval` 参数是 ticks
- **§ 是 MC 颜色代码** — `§a` = 绿色, `§e` = 黄色, `§6` = 金色, `§7` = 灰色

---

## 开发循环

每次修改代码后的标准流程：

```
改代码 → npm run build → /box3script reload mygame → 测试
```

### 构建

```bash
npm run build
```

输出：

```
  dist/server.js  7.1kb
⚡ Done in 240ms
```

构建做了什么：

1. **Babel** 将 TypeScript 编译为 ES5 JavaScript
2. **esbuild** 将所有模块打包为一个文件
3. 输出到 `dist/server.js` 和 `dist/client.js`

### 加载

在游戏内：

```
/box3script start mygame    # 首次启动
/box3script reload mygame   # 修改后重载（无需重启服务器）
```

### 自动热重载

开启文件监控后，保存代码 + build 会自动触发 reload：

```
/box3script watch
```

---

## 调试技巧

### 排查顺序

遇到问题时按以下顺序排查：

1. **看控制台** — 服务端控制台会打印 `[Box3JS] [项目名]` 前缀的日志和错误
2. **看状态** — `/box3script` 检查项目是否是 `◉`（已加载）
3. **看构建** — `npm run build` 是否报错
4. **加日志** — 用 `console.log()` 在关键位置打印变量值
5. **看行号** — Java 异常栈会包含 JS 文件名和行号

### 常见错误

| 错误 | 原因 | 解决 |
|------|------|------|
| `console is not defined` | JS 引擎初始化失败 | 检查模组是否正确安装 |
| `world is not defined` | 作用域问题 | 确保代码在全局作用域，不在函数内 |
| `Cannot find name 'xxx'` | TypeScript 类型错误 | 检查拼写，或查看 `.d.ts` 中的正确 API 名 |
| `npm run build` 报错 | JS 语法错误 | 检查 ESLint 输出 |
| 脚本不执行 | 项目未启用 | `/box3script` 查看状态 |

### 沙盒测试

沙盒模式允许安全测试：开启后所有世界修改被追踪，关闭时一键回滚。

```
/box3script sandbox mygame    # 开启沙盒
# ... 测试脚本（生成实体、修改方块等）...
/box3script sandbox mygame    # 关闭 → 自动回滚所有修改
```

---

## 发布部署

开发完成后，将脚本编译为**独立 JAR 模组**：

```
/box3script compile mygame
```

生成 `mygame-1.0.0.jar`（版本号从 `package.json` 读取），放入任意 NeoForge 服务端的 `mods/` 目录即可运行。

**注意：**
- 需要 Box3JS 作为依赖模组（提供 Rhino 运行时）
- 如果使用了 `registries`（自定义方块/物品），客户端也需要安装 JAR
- JAR 中包含编译后的 JS，无需原始源码

### package.json 配置

```json
{
  "name": "mygame",
  "displayName": "My Game",
  "version": "1.0.0",
  "description": "A custom mini-game",
  "author": "YourName",
  "license": "MIT",
  "homepage": "https://example.com",
  "logoFile": "logo.png"
}
```

这些元数据会被写入 JAR 的 `mods.toml`。

---

## 下一步

- **学 API**: 看 [API 功能速查](../api/README.md) — 按"我想做什么"查找对应 API
- **学事件**: 看 [教程三：事件系统与实体操控](../tutorial/03-events-entities.md)
- **学客户端**: 看 [客户端 API 文档](../api/client.md) — 按键监听、屏幕 UI、客户端音效
- **懂原理**: 看 [运行原理](architecture.md) — Rhino 引擎、作用域、构建管线
- **选技术**: 看 [JS vs Java 对比](js-vs-java.md) — Box3JS 与原生模组怎么选
