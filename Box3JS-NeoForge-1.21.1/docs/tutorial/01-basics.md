# 教程一：从零开始

本教程将带你创建第一个 Box3JS 脚本，逐步掌握控制台输出、聊天命令和基础 API。

## 前置要求

- 服务端已安装 Box3JS 模组
- 了解基础的 JavaScript/TypeScript 语法

## 1.1 创建项目

在游戏内执行：

```
/box3script create mytutorial
```

这会在 `config/box3/script/mytutorial/` 下创建一个 TypeScript 项目模板。如果你想用纯 JavaScript，直接把 `src/app.ts` 当 JS 写即可——构建工具不会检查类型。

目录结构：

```
config/box3/script/mytutorial/
├── src/
│   └── app.ts          ← 入口文件，代码写在这里
├── types/
│   └── globals.d.ts    ← API 类型声明（只读）
├── build.mjs           ← 构建脚本
└── package.json
```

每次改完代码后，在 `mytutorial/` 目录下执行：

```bash
node build.mjs
```

构建成功后，在游戏内开启脚本：

```
/box3script on mytutorial
```

## 1.2 第一个脚本

打开 `src/app.ts`，清空内容，写入：

```js
// 脚本加载时执行
console.log("[MyTutorial] Hello, Box3JS!");

// 服务器启动后执行
world.onPlayerJoin((entity, tick) => {
  entity.player.directMessage("§a欢迎来到服务器！");
});
```

构建并开启脚本后，玩家加入就会看到欢迎消息。

## 1.3 控制台输出

`console` 对象有 4 个级别：

```js
console.log("普通日志"); // [Box3JS] [mytutorial] 普通日志
console.debug("调试信息"); // [Box3JS] [mytutorial] [DEBUG] 调试信息
console.warn("警告"); // [Box3JS] [mytutorial] [WARN] 警告
console.error("错误"); // [Box3JS] [mytutorial] [ERROR] 错误
```

输出会显示在服务端控制台，格式为 `[Box3JS] [项目名] message`。

## 1.4 简单聊天命令

让我们写一个聊天命令系统：

```js
world.onChat((entity, message, tick) => {
  const player = entity.player;

  // 用 switch 处理不同命令
  switch (message) {
    case "!hello":
      player.directMessage("§e你好，" + player.name + "！");
      return false; // 取消原始消息

    case "!time":
      player.directMessage("§e当前游戏时间: §f" + world.time);
      return false;

    case "!pos":
      const pos = player.position;
      player.directMessage(
        "§e你的位置: §f" +
          Math.floor(pos.x) +
          ", " +
          Math.floor(pos.y) +
          ", " +
          Math.floor(pos.z),
      );
      return false;

    case "!online":
      const count = world.querySelectorAll("*").length;
      player.directMessage("§e在线玩家: §f" + count + " 人");
      return false;
  }

  return true; // 不是命令的消息正常发送
});
```

**关键点：** `onChat` 回调返回 `false` 会阻止消息在聊天栏显示，返回 `true` 则正常发送。

## 1.5 定时任务

```js
// 每 5 分钟广播一次
world.setInterval(() => {
  const online = world.querySelectorAll("*").length;
  if (online > 0) {
    world.say("§6[服务器] §f当前在线 " + online + " 人");
  }
}, 6000); // 6000 ticks = 5 分钟

// 30 秒后执行一次
world.setTimeout(() => {
  world.say("§6[服务器] §f已运行 30 秒");
}, 600); // 600 ticks = 30 秒
```

**Ticks 换算：** 20 ticks = 1 秒。`setInterval(fn, 20)` = 每秒执行一次。

## 1.6 世界属性

```js
// 时间控制
world.time = 6000; // 正午 (0=日出, 6000=正午, 12000=日落, 18000=午夜)
world.timeScale = 0; // 暂停时间
world.timeScale = 1; // 恢复

// 天气
world.rainDensity = 1.0; // 下雨
world.thunderDensity = 0.5; // 雷暴
world.clearWeather(); // 晴天

// 难度
world.difficulty = "hard"; // peaceful / easy / normal / hard

// 游戏规则
world.setGameRule("keepInventory", true); // 死亡不掉落
world.setGameRule("doFireTick", false); // 火焰不蔓延
```

## 1.7 广播与消息类型

```js
// 全服广播 (聊天栏)
world.say("§6[公告] §f服务器将在 5 分钟后重启！");

// 单独发送 (仅该玩家可见)
player.directMessage("§a这是一个私密消息");

// 动作栏 (快捷栏上方)
player.actionBar("§e当前在线: " + world.querySelectorAll("*").length);

// 屏幕标题
player.title("§6§lBOSS名称", "§7这是一个危险的敌人");

// 完整标题参数: title, subtitle, fadeIn, stay, fadeOut (单位: ticks)
player.title("§4§l警告", "§c你正在进入危险区域", 10, 60, 10);
```

## 1.8 检查清单

把你的 `app.ts` 整理一下，最终应该看起来像这样：

```js
// ═══════════════════════════════════
//  MyTutorial — 基础示例脚本
// ═══════════════════════════════════

console.log("[MyTutorial] 脚本已加载");

// 欢迎消息
world.onPlayerJoin((entity, tick) => {
  entity.player.directMessage("§a欢迎！输入 !help 查看命令");
});

// 定时公告
world.setInterval(() => {
  const online = world.querySelectorAll("*").length;
  if (online > 0) world.say("§7在线: " + online + " 人");
}, 6000);

// 聊天命令
world.onChat((entity, message, tick) => {
  const p = entity.player;

  switch (message) {
    case "!help":
      p.directMessage("§e命令: §f!hello !time !pos !online !day !clear");
      return false;
    case "!hello":
      p.directMessage("§e你好，" + p.name + "！");
      return false;
    case "!time":
      p.directMessage("§e时间: §f" + world.time);
      return false;
    case "!pos":
      const pos = p.position;
      p.directMessage(
        "§e位置: §f" +
          Math.floor(pos.x) +
          " " +
          Math.floor(pos.y) +
          " " +
          Math.floor(pos.z),
      );
      return false;
    case "!online":
      p.directMessage("§e在线: §f" + world.querySelectorAll("*").length);
      return false;
    case "!day":
      world.time = 1000;
      world.say("§e" + p.name + " §f将时间设为白天");
      return false;
    case "!clear":
      world.clearWeather();
      world.say("§e" + p.name + " §f清除了天气");
      return false;
  }
  return true;
});
```

## 下一步

教程二将介绍玩家操作：传送、物品给予、生命值、经验值、飞行等。
