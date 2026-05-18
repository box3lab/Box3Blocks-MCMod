# 快速开始：从零到第一个 Box3JS 脚本

面向**零模组开发经验**的读者，只需会 JavaScript 即可。

## Box3JS 是什么

Box3JS 是一个 **Minecraft 模组**，在服务器内嵌入 JavaScript 运行时，让你用 JS/TypeScript 编写游戏玩法。客户端脚本可选下发，实现按键监听、屏幕 UI 等本地交互。

Box3JS 的 API 设计继承自**[神奇代码岛](https://dao3.fun)（Box3）**——一款浏览器端的多人 3D 游戏创作平台，成千上万的创作者在上面用 JS 写游戏。神奇代码岛的 API 经过长期社区验证，简洁直观。Box3JS 把这套 API 带到了 Minecraft，让你用同一种编程范式在 MC 里构建小游戏、自定义玩法。

::: tip 更多背景
→ [Box3JS 与神奇代码岛](about-box3js.md)
:::

### 核心架构一览

```text
你在 VS Code 里写       构建工具帮你            Minecraft 帮你跑
  TypeScript    ───→   编译成 ES5 JS   ───→    Rhino 引擎执行
                                                │
                                         ┌──────┴──────┐
                                         │  NeoForge    │
                                         │  Minecraft   │
                                         │  API 层      │
                                         └─────────────┘
```

- **你写 TypeScript**，享受类型提示和现代语法
- **Babel 编译**为 ES5（因为 Rhino 引擎只支持 ES5）
- **esbuild 打包**为单个 JS 文件
- **Rhino 在 JVM 内执行**，直接调用 Minecraft API
- **服务端 + 客户端双端运行**，通过 `remoteChannel` 通信

## 环境搭建

### 你需要

1. **Minecraft 服务端** 安装了 Box3JS + NeoForge 1.21.1
2. **Node.js** 18+ （仅用于本地构建，服务端不需要）
3. 一个文本编辑器（VS Code 推荐，有完整的 TypeScript 智能提示）

### 验证安装

进入游戏，执行：

```js
/box3script
```

如果看到项目状态面板，说明 Box3JS 已正常运行。

```text
══ Box3JS Script Engine ══
  Watch: ○ Inactive    Sandbox: ○ Inactive
  Projects: 0 enabled  |  0 loaded
```

## 创建项目

### 一键创建

在游戏内执行：

```js
/box3script create mygame
```

这会在 `config/box3/script/mygame/` 生成完整的 TypeScript 项目。

::: warning modId 命名规范
项目名会作为 NeoForge 的 **modId** 使用，必须符合命名规则：
- 正则：`^[a-z][a-z0-9_]{1,63}$`
- 首字符必须是**小写字母** `[a-z]`
- 后续字符只能用**小写字母、数字、下划线** `[a-z0-9_]`
- 长度：**2–64** 个字符

✅ 合法：`mygame`、`colorzone`、`arena_battle`、`sky_parkour_2`
❌ 非法：`c`（太短）、`MyGame`（含大写）、`my-game`（含连字符）
:::

### 理解项目结构

```text
config/box3/script/mygame/
├── package.json           ← 项目配置（名称、版本、构建依赖）
├── tsconfig.base.json     ← TypeScript 公共编译选项
├── tsconfig.server.json   ← 服务端 TS 配置（引用 server/ 类型）
├── tsconfig.client.json   ← 客户端 TS 配置（引用 client/ 类型）
├── build.mjs              ← 构建脚本（esbuild + Babel）
├── eslint.config.mjs      ← ESLint 规则
├── types/                 ← ★ 类型声明文件（API 的说明书）
│   ├── shared.d.ts        ← 服务端&客户端共享类型
│   ├── server/
│   │   ├── index.d.ts     ← 服务端类型入口
│   │   ├── server.d.ts    ← world, remoteChannel, registries
│   │   ├── entity.d.ts    ← GameEntity 接口
│   │   ├── player.d.ts    ← GamePlayer 接口
│   │   ├── world.d.ts     ← GameWorld 接口
│   │   └── voxels.d.ts    ← GameVoxels 接口
│   └── client/
│       ├── index.d.ts     ← 客户端类型入口
│       ├── client.d.ts    ← GameClient, RemoteChannel
│       ├── audio.d.ts     ← GameAudio
│       ├── input.d.ts     ← GameInput
│       ├── ui.d.ts        ← GameUI
│       ├── chat.d.ts      ← GameChat
│       └── gui.d.ts       ← GameGUI, GuiController
├── src/
│   ├── server/
│   │   └── app.ts         ← ★ 服务端入口（你写代码的地方）
│   └── client/
│       └── app.ts         ← 客户端入口
├── registries/            ← 自定义内容（方块/物品/音效 JSON）
└── assets/lang/           ← 自定义内容本地化文本
```

**关键理解：**

- `types/` 下的 `.d.ts` 文件是 API 的说明书 — VS Code 靠它们提供智能提示
- `tsconfig.server.json` 和 `tsconfig.client.json` 是**互斥的** — 服务端代码中不会出现 `client`、`input` 等客户端全局对象
- 你只需要在 `src/server/app.ts` 里写代码，构建工具处理剩下的一切

### 安装依赖

打开终端，进入项目目录：

```bash
cd config/box3/script/mygame
npm install
```

`npm install` 只需执行一次（安装 esbuild、Babel、TypeScript 等构建工具）。

## 第一个脚本：逐行详解

打开 `src/server/app.ts`，清空已有内容，写入以下代码。我们来逐行理解。

### 1. 启动日志

```js
console.log("MyGame 脚本已启动！");
```

**发生了什么：** `console` 是 Box3JS 注入的全局对象（不需要 `import`）。它背后是一个 Java 类 `Box3JSConsole`，通过 Rhino 桥接到 JS。`console` 支持 `log`、`warn`、`error`、`debug`、`clear`、`assert` 六个方法。

### 2. 玩家加入欢迎

```js
world.onPlayerJoin((entity) => {
  const p = entity.player;

  // 全服广播
  world.say(`§e${p.name} §7加入了服务器`);

  // 私密消息
  p.directMessage(`§a欢迎来到服务器，${p.name}！`);

  // 粒子欢迎特效
  const { position: pos } = p;
  world.spawnParticleCircle(
    pos.x,
    pos.y,
    pos.z,
    1.5,
    "minecraft:happy_villager",
    15,
  );
  world.playSound("minecraft:block.note_block.pling", pos, 1.0, 1.5);
});
```

**逐行理解：**

- `world.onPlayerJoin(...)` — 注册一个"玩家加入"事件监听器。返回 `GameEventHandlerToken`（本例中没有保存，意味着这个监听器在脚本重载前一直有效）。
- `entity.player` — `entity` 是回调参数，代表加入的实体。`.player` 获取该实体的玩家包装对象（如果实体不是玩家，`.player` 为 `undefined`）。
- `p.directMessage(...)` — 发送私密消息，只有该玩家能看到。
- `§a` 是 Minecraft 颜色代码（绿色）。`§e` = 黄色，`§6` = 金色，`§7` = 灰色，`§c` = 红色。
- `p.position` — 返回 `GameVector3` 对象，包含 `.x`、`.y`、`.z` 属性。
- `world.spawnParticleCircle(...)` — 在玩家位置生成一圈粒子效果。
- `world.playSound(...)` — 在玩家位置播放音效。

### 3. 聊天命令

```js
world.onChat((entity, message) => {
  const p = entity.player;

  if (message === "!hello") {
    p.directMessage(`§e你好，${p.name}！`);
    return false; // 阻止消息显示在聊天栏
  }

  if (message === "!pos") {
    const { position: pos2 } = p;
    p.directMessage(
      `§e你的坐标: §f${Math.floor(pos2.x)}, ${Math.floor(pos2.y)}, ${Math.floor(pos2.z)}`,
    );
    return false;
  }

  if (message === "!help") {
    p.directMessage("§e可用命令: !help, !hello, !pos, !home, !shop");
    return false;
  }

  return true; // 不是命令的消息正常发送
});
```

**关键概念：**

- `return false` — 阻止事件继续（消息不会广播到聊天栏）。这是 Box3JS 事件的通用约定：**返回 false 阻断默认行为**。
- `return true` — 放行，消息正常广播。
- `Math.floor()` — 常规 JS，坐标向下取整，更易读。

### 4. 定时公告

```js
setInterval(() => {
  const count = world.querySelectorAll("*").length;
  world.say(`§7[公告] §f当前在线: ${String(count)} 人`);
}, 6000); // 6000 ticks = 5 分钟
```

**关键理解：**

- `setInterval` 是**全局函数**（不是 `world.setInterval`），和浏览器/Node.js 一致。
- 第二个参数是 **ticks**（Minecraft 时间单位），不是毫秒。1 秒 = 20 ticks。
- `setInterval` 返回 `GameEventHandlerToken`，可以调用 `.cancel()` 取消。
- `world.querySelectorAll("*")` 返回所有在线实体列表。
- `world.say(...)` 向全服广播消息。

### 完整的 Tick 换算表

| 时长    | Ticks  |
| ------- | ------ |
| 1 秒    | 20     |
| 5 秒    | 100    |
| 30 秒   | 600    |
| 1 分钟  | 1,200  |
| 5 分钟  | 6,000  |
| 10 分钟 | 12,000 |
| 30 分钟 | 36,000 |

## 核心设计理念：为什么这样设计 API

理解 Box3JS API 的设计理念，能让你写出更高效、更安全的脚本。以下是最重要的几个设计决策及其原因。

### 设计 1：全局对象注入，不需要 import

```js
// Box3JS 中直接使用，不需要 import
world.onTick(() => { ... });
console.log("hello");
storage.getDataStorage("coins");

// 对比：如果在 Node.js 中
// const { world } = require("box3js");  ← 不需要！
```

**为什么？** Rhino 是一个裸的 ECMAScript 引擎，不支持 CommonJS `require()` 或 ES Module `import`。Box3JS 通过 Java 层在 Rhino 作用域初始化时，直接把所有 API 对象注入为全局变量。TypeScript 的 `.d.ts` 文件用 `declare` 声明这些全局对象，让你在 VS Code 中获得完整的类型提示。

### 设计 2：Tick 制时间，不是毫秒

```js
// Box3JS 的时间单位是 tick（1/20 秒）
setTimeout(() => { ... }, 100);  // 100 ticks = 5 秒后

// 对比浏览器：
// setTimeout(() => { ... }, 5000);  // 5000 毫秒 = 5 秒后
```

**为什么？** Box3JS 的定时器是**在主线程的游戏 Tick 循环中**执行的，不创建任何 Java 线程。每次游戏 Tick（1/20 秒），引擎检查所有定时器，递减剩余 tick 数，到 0 时触发回调。这样做的好处是：

- **线程安全** — 回调总是在主线程执行，你可以安全地调用任何 Minecraft API
- **精确同步** — 定时器与游戏世界完全同步，不会出现"服务器卡了但定时器还在走"的情况
- **零开销** — 不创建额外的线程或线程池

### 设计 3：GameEventHandlerToken — 统一的取消模式

```js
// 所有 onXxx() 和 setTimeout/setInterval 都返回 GameEventHandlerToken
const token = world.onTick(() => {
  // 每 tick 执行
});

// 取消监听（两种方式等效）
token.cancel();

// 检查是否仍活跃
if (token.active()) {
  // ...
}
```

**为什么？** 早期设计为每种事件提供独立的取消方法（如 `removeTickListener`、`removeChatListener`），但这样会导致：

1. 需要记住每种事件的取消 API 名称
2. 无法统一管理（你想在脚本停止时批量取消怎么办？）

统一返回 `GameEventHandlerToken` 后：

- **一个模式适用所有** — 不管是 `onTick`、`onPlayerJoin`、`onChat` 还是 `setInterval`，都用 `.cancel()`
- **脚本重载自动清理** — 停止项目时，引擎遍历所有 token 批量取消，无遗漏
- **链式管理** — 你可以把多个 token 放进数组，统一 `.cancel()`

### 设计 4：项目作用域隔离

```text
服务端同时运行 3 个脚本项目，互不影响：

┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ Scope A      │  │ Scope B      │  │ Scope C      │
│ "mygame"     │  │ "lobby"      │  │ "survival"   │
│              │  │              │  │              │
│ var x = 1    │  │ var x = 2    │  │ var x = 3    │
│ 自己的事件    │  │ 自己的事件    │  │ 自己的事件    │
│ 自己的存储    │  │ 自己的存储    │  │ 自己的存储    │
│ 自己的定时器  │  │ 自己的定时器  │  │ 自己的定时器  │
└──────────────┘  └──────────────┘  └──────────────┘
```

**为什么？** 一个服务器可能同时运行多个脚本（大厅系统、小游戏、经济系统……）。如果没有隔离：

- 变量名冲突（两个脚本都定义了 `var playerCount`）
- 事件回调互相干扰（`/box3script reload lobby` 意外清除了 survival 的回调）
- 数据泄露（lobby 的脚本读到了 survival 的存储数据）

Box3JS 给每个项目分配**独立的 Rhino 顶级作用域**，存储在独立的 `Box3JSEventBus` 命名空间中。停止或重载一个项目完全不影响其他项目。

### 设计 5：双端架构 + remoteChannel

```text
┌──────────────────────┐         ┌──────────────────────┐
│   服务端 (Server)     │         │   客户端 (Client)     │
│                      │         │                      │
│  world.*             │         │  client.*            │
│  voxels.*            │  JSON   │  audio.*             │
│  entity.*            │ ←─────→ │  input.*             │
│  player.*            │ 事件    │  ui.*                │
│  storage/db/http     │         │  chat.* / gui.*      │
│  remoteChannel ──────┼─────────┼── remoteChannel      │
│                      │         │  storage/db/http     │
└──────────────────────┘         └──────────────────────┘
```

**为什么分开？**

- **安全性** — 服务端是世界权威（方块、实体、数据），客户端只能操作本地表现（UI、音效、输入）
- **性能** — 客户端脚本运行在玩家自己的电脑上，不消耗服务器资源
- **灵活性** — 你可以只写服务端脚本（大多数场景），或增加客户端脚本来提升体验

**remoteChannel 通信规则：**

```js
// 服务端 → 单个客户端
remoteChannel.sendClientEvent(player, { type: "welcome", msg: "hi" });

// 服务端 → 所有客户端
remoteChannel.broadcastClientEvent({ type: "game_start" });

// 客户端 → 服务端
remoteChannel.sendServerEvent({ key: "space", pressed: true });
```

**重要限制：** 跨网络传输的数据必须是 **JSON 可序列化**的。不能传函数、`GameVector3` 实例、Java 对象。如果需要传坐标，用 `{ x: 1, y: 2, z: 3 }` 而不是 `new GameVector3(1, 2, 3)`。

### 设计 6：TypeScript 源码 + Babel 编译为 ES5

```text
src/server/app.ts         Babel               esbuild        dist/server.js
(TypeScript, ES2020)  ───→  ES5 JavaScript  ───→  bundle  ───→  (一个文件)
```

**为什么需要构建步骤？**

- **Rhino 1.9.1 只支持 ES5** — `let`、`const`、箭头函数、模板字符串、`class` 都是 ES6+ 语法，Rhino 不认识
- **Babel 负责降级** — 把现代语法转成 `var`、`function`、字符串拼接等 ES5 写法
- **esbuild 负责打包** — 虽然你可以写多个 `.ts` 文件，但 Rhino 没有模块系统。esbuild 把所有文件合并为一个 IIFE

**你可以放心在源码中用：**

- `const` / `let`（转为 `var`）
- 箭头函数 `() => {}`（转为 `function(){}`）
- 模板字符串 `` `hello ${name}` ``（转为 `"hello " + name`）
- `class`（转为 `function` + prototype）
- `async/await`（通过 regenerator 转换）

### 设计 7：事件回调返回 false 阻断

```js
world.onChat((entity, message) => {
  if (message.startsWith("!")) {
    // 这是命令，不要广播
    return false;
  }
  return true; // 正常消息放行
});
```

**为什么？** 借鉴了浏览器 DOM 事件的 `preventDefault` 模式。Minecraft 事件通常有"默认行为"（如聊天消息广播给所有人）。`return false` 告诉引擎："我已经处理了这个事件，不要执行默认行为"。

### 设计 8：沙盒模式 — 安全测试

```js
/box3script sandbox mygame    # 开启沙盒
# ... 测试脚本（生成实体、修改方块、爆炸）...
/box3script sandbox mygame    # 关闭 → 自动回滚所有修改
```

**为什么？** 一旦脚本修改了世界，这些修改是永久性的（方块被替换、实体被生成）。沙盒模式追踪脚本对世界的所有修改，关闭时自动回滚。这让开发者可以大胆测试破坏性操作，不用担心搞坏正式服。

## API 实战速览

以下按"我想做什么"组织，覆盖最常用的 API。完整的 API 参考见 [API 文档](../api/README.md)。

### 消息与聊天

```js
// 全服广播
world.say("§6服务器将在 5 分钟后重启");

// 私密消息（只有目标玩家看到）
player.directMessage("§a你的余额: 100 金币");

// 快捷栏上方文字
player.actionBar("§e按 F 键打开菜单");

// 屏幕中央大标题
player.title("§6BOSS 战", "§c远古巨龙 已苏醒");

// 拦截聊天（做命令系统）
world.onChat((entity, message) => {
  if (message === "!help") {
    entity.player.directMessage("§e可用命令: !help, !home, !shop");
    return false;
  }
  return true;
});
```

### 玩家属性与控制

```js
// 获取玩家信息
const name = player.name;
const pos = player.position; // GameVector3 { x, y, z }
const hp = player.hp;
const mode = player.gameMode; // "survival", "creative", "adventure", "spectator"

// 修改玩家状态
player.hp = 20; // 回满血
player.maxHp = 40; // 增加最大生命值
player.food = 20; // 回满饱食度
player.gameMode = "creative"; // 切换创造模式
player.canFly = true; // 允许飞行
player.flying = true; // 开始飞行

// 传送
player.teleport(new GameVector3(100, 64, 100));

// 踢出
player.kick("你已被管理员踢出");

// 以玩家身份执行原版命令
player.runCommand("effect give @s minecraft:speed 30 1");
```

### 物品与背包

```js
// 给物品
player.giveItem("minecraft:diamond", 64);
player.giveItem("minecraft:diamond_sword", 1);

// 给带名称的物品（第 4 个参数为描述文字数组）
player.giveNamedItem("minecraft:stick", 1, "§6魔法棒", ["右键使用"]);

// 给带附魔的物品（附魔为 { 附魔ID: 等级 } 对象）
player.giveEnchantedItem("minecraft:diamond_sword", 1, {
  "minecraft:sharpness": 5,
  "minecraft:unbreaking": 3,
});

// 查询手持物品
const held = player.getHeldItem();

// 清空背包
player.clearInventory();
```

### 事件系统

```js
// 每 tick（谨慎使用！每 tick 中的重操作会拖慢服务器）
const tickToken = world.onTick(() => {
  // 每 tick 执行
});

// 玩家事件
world.onPlayerJoin((entity) => {
  entity.player.directMessage("欢迎！");
});

world.onPlayerLeave((entity, _tick) => {
  world.say(`${entity.player.name} 离开了服务器`);
});

world.onPlayerRespawn((entity, _tick) => {
  entity.player.teleport(new GameVector3(0, 100, 0));
  entity.player.directMessage("你重生了！");
});

// 实体事件
world.onEntityDeath((entity, _killer, _tick) => {
  if (entity.isPlayer()) {
    world.say(`${entity.player.name} 死了`);
  }
});

world.onEntityDamage((entity, amount, source, _attacker, _tick) => {
  if (amount > 10) {
    console.log(`高额伤害: ${String(amount)} 来源: ${source}`);
  }
});

// 交互事件
world.onInteract((entity, target, _tick) => {
  // 玩家右键实体
  if (target.hasTag("npc")) {
    entity.player.directMessage("你好！");
  }
});

world.onBlockActivate((entity, x, y, z, voxel, _tick) => {
  // 玩家右键方块
  if (voxel === "minecraft:chest") {
    entity.player.directMessage(
      `你点击了位于 ${String(x)}, ${String(y)}, ${String(z)} 的箱子`,
    );
  }
});

world.onBlockPlace((entity, x, y, z, voxel, _voxelId, _tick) => {
  // 玩家放置方块
  console.log(
    `${entity.player.name} 在 ${String(x)}, ${String(y)}, ${String(z)} 放置了 ${voxel}`,
  );
});

world.onVoxelDestroy((entity, _x, _y, _z, voxel, _tick) => {
  // 玩家破坏方块
  if (voxel === "minecraft:diamond_block") {
    entity.player.directMessage("§c不能破坏钻石块！");
    return false; // 阻止破坏
  }
});

// 定时器（全局函数）
const timer = setTimeout(() => {
  world.say("30 秒到了！");
}, 600); // 600 ticks = 30 秒

const interval = setInterval(() => {
  world.say("每分钟公告");
}, 1200); // 1200 ticks = 1 分钟

// 取消定时器
timer.cancel();
interval.cancel();
```

### 实体操控

```js
// 生成实体（返回 GameEntity | null）
const zombie = world.spawnEntity(
  "minecraft:zombie",
  new GameVector3(100, 64, 100),
);
if (zombie) {
  // 使用 zombie ...
}

// 带完整配置创建实体（nameTag/glowing/equipment 需创建后设置）
const boss = world.createEntity({
  type: "minecraft:zombie",
  position: new GameVector3(100, 64, 100),
  hp: 200,
  maxHp: 200,
  tags: ["boss"],
});
if (boss) {
  boss.setNameTag("§c远古僵尸王");
  boss.glowing = true;
  boss.setEquipment("head", "minecraft:diamond_helmet");
  boss.setEquipment("chest", "minecraft:diamond_chestplate");

  // 操控实体
  boss.setAI(false); // 关闭 AI（原地不动）
  boss.invulnerable = true; // 无敌
  boss.navigateTo(110, 64, 100, 1.5); // 导航到目标位置

  // 药水效果
  boss.addEffect("minecraft:strength", 600, 2, false);
  boss.addEffect("minecraft:speed", 600, 1, true);
  boss.clearEffects();

  // 装备
  boss.setEquipment("head", "minecraft:iron_helmet");
  boss.setEquipment("mainhand", "minecraft:iron_sword");

  // 标签（用于标记和查询）
  boss.addTag("boss");
  boss.addTag("stage_1");
  boss.hasTag("boss"); // → true
}

// 查询实体
const nearby = world.entitiesInRadius(pos, 10); // 半径 10 格内
const all = world.querySelectorAll("*"); // 所有实体
const players = world.querySelectorAll("player"); // 所有玩家
const monsters = world.querySelectorAll("monster"); // 所有怪物
```

### 方块操作

```js
// 读取方块
const block = voxels.getVoxel(100, 64, 100);

// 放置方块
voxels.setVoxel(100, 64, 100, "minecraft:stone");
voxels.setVoxel(100, 65, 100, "minecraft:torch");

// 区域填充
voxels.fillVoxel(0, 64, 0, 10, 70, 10, "minecraft:glass");

// 替换方块（只替换指定类型）
voxels.fillVoxel(0, 64, 0, 10, 70, 10, "minecraft:air", "minecraft:stone");
```

### 数据持久化

```js
// JSON 存储（每个项目独立命名空间）
const store = storage.getDataStorage("coins");
store.set("player1", 100);
const coins = store.get("player1"); // → 100
store.delete("player1");
const keys = store.keys(); // → 所有 key 的数组

// SQLite 数据库
db.sql("CREATE TABLE IF NOT EXISTS players (name TEXT, score INT)");
db.sql("INSERT INTO players VALUES ('Steve', 100)");

const result = db.sql("SELECT * FROM players WHERE score > 50");
// result.rows[0] → { name: "Steve", score: 100 }
// result.firstRow → { name: "Steve", score: 100 }
// result.rowCount → 1
// result.columnNames → ["name", "score"]
```

### 游戏系统

```js
// 计分板
world.addScoreboard("kills");
world.setScore("Steve", "kills", 42);
world.showScoreboard("sidebar", "kills");

// BossBar
world.showBossbar("boss1", "§c远古巨龙", 0.8, "red");
world.setBossbar("boss1", "§c远古巨龙 §7[80%]", 0.5);

// 队伍
world.createTeam("red", "red");
world.createTeam("blue", "blue");
world.joinTeam(entity, "red");

// 世界边界
world.borderSize = 500; // 设置边界大小
world.shrinkBorder(100, 1200); // 在 1200 ticks 内缩到 100

// 天气和时间
world.time = 6000; // 设置时间（0=日出, 6000=正午, 12000=日落, 18000=午夜）
world.rainDensity = 0; // 停雨
world.clearWeather(); // 晴天
world.thunderDensity = 1; // 雷暴

// 游戏规则
world.setGameRule("keepInventory", true);
world.setGameRule("doDaylightCycle", false);
```

### 视觉效果

```js
const pos = new GameVector3(100, 64, 100);

// 粒子
world.spawnParticle("minecraft:flame", pos.x, pos.y, pos.z, 0, 0, 0, 1, 10);
world.spawnParticleCircle(pos.x, pos.y, pos.z, 2, "minecraft:heart", 30);

// 烟花
world.launchFirework(pos.x, pos.y, pos.z, "red", "large_ball");
world.launchFirework(pos.x, pos.y, pos.z, "green", "star");

// 闪电和爆炸
world.strikeLightning(pos.x, pos.y, pos.z);
world.explode(pos.x, pos.y, pos.z, 4); // 威力 4 的爆炸

// 音效
world.playSound("minecraft:entity.ender_dragon.growl", pos, 1.0, 1.0);
player.playSound("minecraft:block.note_block.pling", 1.0, 2.0); // 只有该玩家听到
```

### HTTP 请求

```js
// GET 请求
const resp = http.fetch("https://api.example.com/data");
if (resp.ok) {
  const data: unknown = resp.json();
  console.log(data);
}

// POST JSON
const resp2 = http.fetch("https://api.example.com/webhook", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ text: "服务器重启了" })
});

// 超时设置
const resp3 = http.fetch("https://slow-api.com/data", {
  timeout: 5000 // 5 秒超时
});
```

### 客户端脚本（需 Box3JS 客户端 Mod）

```js
// 客户端入口: src/client/app.ts

// 每帧执行
client.onTick(() => {
  // 客户端 tick 回调
});

// 键盘输入
if (input.isKeyDown("space")) {
  // 空格键正在被按住
}

input.onKeyPress("f", () => {
  // F 键被按下时触发
  remoteChannel.sendServerEvent({ action: "open_menu" });
});

// 屏幕 UI
ui.showOverlay("§e按 F 打开菜单"); // 快捷栏上方
ui.showTitle("§6BOSS 出现！", "§c准备战斗"); // 屏幕中央

// 客户端音效
audio.playSound("minecraft:block.note_block.pling", 1.0, 1.0);
audio.playMusic("minecraft:music.game", 0.5, 1.0);
audio.stopAll();

// 雾效控制（客户端渲染）
client.setFogColor(255, 100, 50); // 红雾外观
client.setFogStartDistance(10); // 雾从 10 格开始
client.setFogEndDistance(50); // 50 格外完全遮挡
client.resetFog(); // 恢复默认

// 聊天
chat.sendMessage("大家好！");
chat.onMessage((msg, _sender, _isSystem) => {
  if (msg.includes("秘密")) {
    // 处理包含"秘密"的消息
  }
});

// 接收服务端事件
remoteChannel.onClientEvent((event) => {
  if (event.args.type === "boss_spawned") {
    audio.playSound("minecraft:entity.ender_dragon.growl", 1.0, 1.0);
  }
});
```

## 开发循环

### 标准流程

每次修改代码后：

```js
改代码 → npm run build → /box3script reload mygame → 测试
```

### 构建

```bash
npm run build
```

输出：

```js
  dist/server.js  7.1kb
⚡ Done in 240ms
```

构建做了什么：

1. **Babel** 将 TypeScript 编译为 ES5 JavaScript（因为 Rhino 只支持 ES5）
2. **esbuild** 将所有模块打包为一个文件（因为 Rhino 没有 `require()`）
3. 输出到 `dist/server.js` 和 `dist/client.js`

### 加载与重载

在游戏内：

```js
/box3script start mygame    # 首次启动
/box3script reload mygame   # 修改后重载（无需重启服务器）
```

`reload` 是原子的：先停止旧脚本（清理所有事件回调、计时器、计分板），再加载新脚本。

### 自动热重载

开启文件监控后，保存代码 + build 会自动触发 reload：

```js
/box3script watch
```

**注意**：`watch` 监控的是 `dist/` 下的编译产物（`.js`），不是 `src/` 下的源码。所以你需要先 `npm run build` 生成新的 `dist/` 文件，watch 才会检测到变化。配合 `npm run build -- --watch` 可以实现保存即热重载。

### 多项目管理

```js
/box3script start mygame lobby    # 同时启动多个项目
/box3script stop mygame           # 停止单个
/box3script stopall               # 停止全部
/box3script reload mygame          # 重载单个
/box3script                        # 查看所有项目状态
```

## 调试技巧

### 排查顺序

遇到问题时按以下顺序排查：

1. **看控制台** — 服务端控制台会打印 `[Box3JS] [项目名]` 前缀的日志和错误
2. **看状态** — `/box3script` 检查项目是否是 `◉`（已加载）
3. **看构建** — `npm run build` 是否报错
4. **加日志** — 用 `console.log()` 在关键位置打印变量值
5. **看行号** — Java 异常栈会包含 JS 文件名和行号（因为脚本被 Rhino 解释执行，行号对应编译后的 `dist/server.js`，不是 `.ts` 源码）

### 常见错误

| 错误                     | 原因                | 解决                                                   |
| ------------------------ | ------------------- | ------------------------------------------------------ |
| `console is not defined` | JS 引擎初始化失败   | 检查模组是否正确安装                                   |
| `world is not defined`   | 作用域问题          | 确保代码在全局作用域，不在嵌套函数内定义后又引用       |
| `Cannot find name 'xxx'` | TypeScript 类型错误 | 检查拼写，或查看 `types/` 下的 `.d.ts` 中的正确 API 名 |
| `npm run build` 报错     | JS 语法错误         | 检查 ESLint 输出，或看终端错误行号                     |
| 脚本不执行               | 项目未启用          | `/box3script` 查看状态                                 |
| 定时器不触发             | tick 数算错了       | 记住 1 秒 = 20 ticks，不是 1000                        |
| 客户端脚本无效           | 玩家没装客户端 Mod  | Box3JS 客户端 Mod 必须安装                             |
| remoteChannel 没收到     | 数据不是 JSON       | 确保传的是纯对象，不是 Java 对象或 `GameVector3` 实例  |

### 沙盒测试

沙盒模式允许安全测试：开启后所有世界修改被追踪，关闭时一键回滚。

```js
/box3script sandbox mygame    # 开启沙盒
# ... 测试脚本（生成实体、修改方块、爆炸等）...
/box3script sandbox mygame    # 关闭 → 自动回滚所有修改
```

**适用场景：**

- **新脚本首次测试** — 不确定脚本会做什么，先沙盒测试
- **玩家试玩** — 让玩家试玩新功能，结束时回滚不影响正式服
- **调试破坏性操作** — 测试 `fillVoxel`、`explode` 等操作

### 性能注意事项

Box3JS 脚本运行在服务器主线程上，不合理的代码会影响 TPS：

1. **`onTick` 中避免大循环** — 遍历所有实体请在条件触发时做，不要每 tick 做
2. **缓存查询结果** — 不要把 `querySelectorAll` 放在每 tick
3. **用 `setInterval` 代替 `onTick`** — 如果不需要 20 次/秒，用更长的间隔（比如 100 ticks = 5 秒）
4. **避免 JS ↔ Java 频繁跨越** — 批量操作比逐个操作快

一个跑酷脚本的性能消耗通常 < 0.5ms/tick，对服务器 TPS 几乎无影响。

## 发布部署

### 开发模式 → 生产发布

开发完成后，将脚本编译为**独立 JAR 模组**：

```js
/box3script compile mygame
```

生成 `mygame-1.0.0.jar`（版本号从 `package.json` 读取），放入任意 NeoForge 服务端的 `mods/` 目录即可运行。

**注意：**

- 需要 Box3JS 作为依赖模组（提供 Rhino 运行时）
- 如果使用了 `registries`（自定义方块/物品），客户端也需要安装 JAR
- JAR 中包含编译后的 JS，无需原始源码
- 编译后的 JAR 是一个独立的 NeoForge 模组，有自己的 `mods.toml`

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

这些元数据会被写入 JAR 的 `mods.toml`，在游戏的模组列表中显示。

### 开发模式 vs 编译模式

|              | 开发模式 (`/box3script start`) | 编译模式 (`/box3script compile`) |
| ------------ | ------------------------------ | -------------------------------- |
| 修改代码     | 热重载，无需重启               | 需重新编译                       |
| `registries` | `undefined`                    | ✅ 可用                          |
| 分发         | 需要源码                       | 只需 JAR                         |
| 适用场景     | 开发、测试                     | 发布、分发                       |

## 下一步

现在你已经理解了 Box3JS 的核心设计理念和基本 API 用法。接下来：

- **学 API 细节**: 看 [API 功能速查](../api/README.md) — 按"我想做什么"查找对应 API
- **学事件系统**: 看 [教程三：事件系统与实体操控](../tutorial/03-events-entities.md)
- **学客户端**: 看 [客户端 API 文档](../api/client.md) — 按键监听、屏幕 UI、客户端音效
- **懂原理**: 看 [运行原理](architecture.md) — Rhino 引擎、作用域、构建管线、网络通信
- **选技术**: 看 [JS vs Java 对比](js-vs-java.md) — Box3JS 与原生模组怎么选
- **常见问题**: 看 [FAQ](faq.md)
- **实战菜谱**: 看 [代码片段与菜谱](recipes.md) — 复制即用的常见功能实现
