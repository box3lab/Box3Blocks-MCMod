# 教程一：5 分钟上手 Box3JS

本教程带你从零创建第一个 Box3JS 脚本——不需要任何 Minecraft 模组开发经验，会 JavaScript 就能写。

## 前置要求

- 服务端已安装 Box3JS 模组
- 了解基础 JavaScript/TypeScript 语法

## 第一步：创建项目

在游戏内执行一条命令：

```js
/box3script create hello
```

这会在 `config/box3/script/hello/` 下生成一个完整的 TypeScript 项目。服务端玩法逻辑写在 `src/server/app.ts`；客户端 UI/输入逻辑写在 `src/client/app.ts`。

## 第二步：构建

打开终端，进入项目目录：

```bash
cd config/box3/script/hello
npm install && npm run build
```

`npm install` 只需执行一次。之后每次修改代码只需要 `npm run build`。

## 第三步：写你的第一个脚本

打开 `src/server/app.ts`，清空内容，写入：

```js
console.log("Hello, Box3JS!");

world.onPlayerJoin((entity) => {
  entity.player.directMessage("§a欢迎来到服务器！");
});
```

**这就够了**——不需要 import、不需要初始化，`world` 和 `console` 是模组提供的全局对象。

## 第四步：启动

回到游戏内：

```js
/box3script start hello
```

现在让一个玩家加入服务器，他会收到 "§a欢迎来到服务器！" 的绿色消息。服务端控制台会输出 `[Box3JS] [hello] Hello, Box3JS!`。

## 第五步：改代码 + 热重载

试着把欢迎消息改成：

```js
entity.player.directMessage(`§6你好，${entity.player.name}！`);
```

保存后执行 `npm run build`，然后在游戏内：

```js
/box3script reload hello
```

不需要重启服务器，改动立刻生效。

以上 5 步就是完整的开发循环：**改代码 → build → reload**。下文深入讲解你能用的所有能力。

## 消息系统

在写聊天命令之前，得先知道有哪些方式给玩家发消息。

### 四种消息类型

```js
// 1. 全服广播 — 聊天栏，所有人都看到
world.say("全体玩家注意！");

// 2. 私密消息 — 聊天栏，只有目标玩家看到
player.directMessage("这条消息只有你能看到");

// 3. 动作栏 — 快捷栏上方的小字
player.actionBar("快捷栏上方的提示");

// 4. 屏幕标题 — 屏幕中央大字
player.title("§6§l主标题", "§7副标题");
// 带时间的标题: (主标题, 副标题, 淡入tick, 停留tick, 淡出tick)
player.title("§c§lBOSS", "远古巨龙", 10, 60, 10);
```

| 方法                     | 位置       | 可见范围 |
| ------------------------ | ---------- | -------- |
| `world.say()`            | 聊天栏     | 全服     |
| `player.directMessage()` | 聊天栏     | 单人     |
| `player.actionBar()`     | 快捷栏上方 | 单人     |
| `player.title()`         | 屏幕中央   | 单人     |

### console 日志

`console` 输出到服务端控制台，格式为 `[Box3JS] [项目名] message`：

```js
console.log("普通日志"); // [Box3JS] [hello] 普通日志
console.debug("调试信息"); // [Box3JS] [hello] [DEBUG] 调试信息
console.warn("警告"); // [Box3JS] [hello] [WARN] 警告
console.error("错误"); // [Box3JS] [hello] [ERROR] 错误
```

## 聊天命令系统

用 `world.onChat` 拦截聊天消息，实现自定义命令：

```js
world.onChat((entity, message) => {
  const p = entity.player;

  switch (message) {
    case "!help":
      p.directMessage("§6── 命令帮助 ──");
      p.directMessage("§f!hello  §7- 打招呼");
      p.directMessage("§f!time   §7- 查看时间");
      p.directMessage("§f!pos    §7- 查看坐标");
      p.directMessage("§f!day    §7- 设为白天");
      p.directMessage("§f!clear  §7- 清除天气");
      return false; // ★ 返回 false 阻止消息显示在聊天栏

    case "!hello":
      p.directMessage(`§e你好，${p.name}！`);
      return false;

    case "!time":
      p.directMessage(`§e当前游戏时间: §f${world.time}`);
      return false;

    case "!pos": {
      const pos = p.position;
      p.directMessage(
        `§e你的位置: §f${Math.floor(pos.x)}, ${Math.floor(pos.y)}, ${Math.floor(pos.z)}`,
      );
      return false;
    }

    case "!day":
      world.time = 1000;
      world.say(`§e${p.name} §f将时间设为白天`);
      return false;

    case "!clear":
      world.clearWeather();
      world.say(`§e${p.name} §f清除了天气`);
      return false;
  }
  return true; // 不是命令的消息正常发送
});
```

**关键规则：** 回调返回 `false` 阻止该消息在聊天栏显示，返回 `true` 则正常发送。

## 带特效的欢迎消息

纯文字太无聊，加一点视觉效果：

```js
world.onPlayerJoin((entity) => {
  const p = entity.player;

  // 屏幕标题
  p.title("§6§l欢迎！", "§7输入 §f!help §7查看帮助", 5, 70, 10);

  // 粒子圈 + 音效
  const pos = p.position;
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

效果：玩家加入时屏幕出现标题、听到铃铛声、身边冒出绿色粒子圈。

## 定时任务

```js
// 每 5 分钟广播一次在线人数
setInterval(() => {
  const count = world.querySelectorAll("*").length;
  if (count > 0) world.say(`§7在线: §f${count} §7人`);
}, 6000); // 6000 ticks = 5 分钟

// 30 秒后执行一次
setTimeout(() => {
  world.say("§6服务器已运行 30 秒");
}, 600); // 600 ticks = 30 秒
```

**Tick 换算：** 20 ticks = 1 秒

| 时长   | Ticks |
| ------ | ----- |
| 1 秒   | 20    |
| 5 秒   | 100   |
| 30 秒  | 600   |
| 1 分钟 | 1200  |
| 5 分钟 | 6000  |

## 世界属性

```js
// 时间
world.time = 6000; // 正午 (0=日出, 6000=正午, 12000=日落, 18000=午夜)

// 天气
world.rainDensity = 1.0; // 满强度下雨
world.thunderDensity = 0.5; // 雷暴
world.clearWeather(); // 晴天

// 难度
world.difficulty = "hard"; // peaceful / easy / normal / hard

// 游戏规则
world.setGameRule("keepInventory", true); // 死亡不掉落
world.setGameRule("doFireTick", false); // 火焰不蔓延
world.setGameRule("doMobSpawning", false); // 禁止刷怪
```

## 完整整合示例

把以上所有内容整合到一个脚本：

```js
// ═══════════════════════════════════
//  Hello — Box3JS 入门脚本
// ═══════════════════════════════════

console.log("[Hello] 脚本已加载");

// ── 欢迎特效 ──
world.onPlayerJoin((entity) => {
  const p = entity.player;
  p.title("§6§l欢迎来到服务器！", "§7输入 §f!help §7查看命令", 5, 70, 10);
  const pos = p.position;
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

// ── 定时公告 ──
setInterval(() => {
  const count = world.querySelectorAll("*").length;
  if (count > 0) world.say(`§7在线: §f${count} §7人`);
}, 6000);

// ── 聊天命令 ──
world.onChat((entity, message) => {
  const p = entity.player;
  switch (message) {
    case "!help":
      p.directMessage("§6命令: §f!hello !time !pos !online !day !clear");
      return false;
    case "!hello":
      p.directMessage(`§e你好，${p.name}！`);
      return false;
    case "!time":
      p.directMessage(`§e时间: §f${world.time}`);
      return false;
    case "!pos": {
      const pos = p.position;
      p.directMessage(
        `§e位置: §f${Math.floor(pos.x)} ${Math.floor(pos.y)} ${Math.floor(pos.z)}`,
      );
      return false;
    }
    case "!online":
      p.directMessage(`§e在线: §f${world.querySelectorAll("*").length}`);
      return false;
    case "!day":
      world.time = 1000;
      world.say(`§e${p.name} §f将时间设为白天`);
      return false;
    case "!clear":
      world.clearWeather();
      world.say(`§e${p.name} §f清除了天气`);
      return false;
  }
  return true;
});
```

## 常用技巧

### 开发循环

```js
改代码 → npm run build → /box3script reload hello → 测试
```

开启文件监控自动热重载（无需手动 reload）：

```js
/box3script watch
```

### 沙盒模式（安全测试）

开启沙盒后，脚本对世界的所有修改都会被追踪，关闭时一键回滚：

```js
/box3script sandbox hello    # 开启
# ... 测试脚本 ...
/box3script sandbox hello    # 关闭 → 回滚所有修改
```

### 调试技巧

遇到问题时的排查顺序：

1. 检查服务端控制台是否有报错（`console.log` 输出会出现在这里）
2. 确认脚本已加载：`/box3script` 看项目是否显示为 `◉`（已加载运行中）
3. 确认 build 成功：`npm run build` 应该没有错误
4. 如果语法没问题但逻辑不生效，检查事件回调是否正确注册

## 下一步

[教程二：玩家操控与物品](../tutorial/02-player-items.md) — 传送、物品给予、药水效果、游戏模式、生命值。
