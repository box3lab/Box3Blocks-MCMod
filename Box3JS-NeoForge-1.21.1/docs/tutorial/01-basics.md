# 教程一：从零开始

本教程将带你创建第一个 Box3JS 脚本，逐步掌握控制台输出、欢迎特效、聊天命令和定时任务。

## 前置要求

- 服务端已安装 Box3JS 模组
- 了解基础的 JavaScript/TypeScript 语法

## 1.1 创建项目

在游戏内执行：

```
/box3script create mytutorial
```

这会在 `config/box3/script/mytutorial/` 下创建一个 TypeScript 项目模板。如果你想用纯 JavaScript，直接把 `src/app.ts` 当 JS 写即可——构建工具不会阻止你。

目录结构：

```
config/box3/script/mytutorial/
├── src/
│   └── app.ts          ← 入口文件，代码写在这里
├── types/
│   └── globals.d.ts    ← API 类型声明（只读参考）
├── build.mjs           ← 构建脚本
├── package.json
└── tsconfig.json
```

每次改完代码后，在 `mytutorial/` 目录下执行：

```bash
npm install && npm run build
```

构建成功后，在游戏内开启脚本：

```
/box3script start mytutorial
```

## 1.2 第一个脚本

打开 `src/app.ts`，清空内容，写入：

```js
console.log("[MyTutorial] Hello, Box3JS!");

world.onPlayerJoin((entity, tick) => {
  entity.player.directMessage("§a欢迎来到服务器！");
});
```

构建并开启脚本后，玩家加入就会看到欢迎消息。

`console` 对象有 4 个级别：

```js
console.log("普通日志");   // [Box3JS] [mytutorial] 普通日志
console.debug("调试信息"); // [Box3JS] [mytutorial] [DEBUG] 调试信息
console.warn("警告");      // [Box3JS] [mytutorial] [WARN] 警告
console.error("错误");     // [Box3JS] [mytutorial] [ERROR] 错误
```

输出会显示在服务端控制台，格式为 `[Box3JS] [项目名] message`。

## 1.3 带特效的欢迎消息

纯文字欢迎太无聊了，加一点视觉效果：

```js
world.onPlayerJoin((entity, _tick) => {
  const p = entity.player;

  // 屏幕标题欢迎
  p.title("§6§l欢迎来到服务器！", "§7输入 §f!help §7查看命令", 5, 70, 10);

  // 动作栏提示
  p.actionBar(`§a欢迎 ${p.name} §a| 在线: §f${world.querySelectorAll("*").length}`);

  // 出生粒子特效 — 绿色粒子圈
  const pos = p.position;
  world.spawnParticleCircle(
    pos.x, pos.y, pos.z, 1.5,
    "minecraft:happy_villager", 15
  );
  // 音效
  world.playSound("minecraft:block.note_block.pling", pos, 1.0, 1.5);
});
```

效果：玩家加入时看到标题、听到铃铛声、身边冒出绿色粒子圈。

## 1.4 聊天命令系统

用 `world.onChat` 拦截聊天消息实现命令：

```js
world.onChat((entity, message, _tick) => {
  const p = entity.player;

  switch (message) {
    case "!help":
      p.directMessage("§6── 服务器命令 ──");
      p.directMessage("§f!hello  §7- 打招呼");
      p.directMessage("§f!time   §7- 查看游戏时间");
      p.directMessage("§f!pos    §7- 查看坐标");
      p.directMessage("§f!online §7- 在线人数");
      p.directMessage("§f!day    §7- 设为白天");
      p.directMessage("§f!clear  §7- 清除天气");
      return false;  // 阻止原始消息显示在聊天栏

    case "!hello":
      p.directMessage(`§e你好，${p.name}！`);
      return false;

    case "!time":
      p.directMessage(`§e当前游戏时间: §f${world.time}`);
      return false;

    case "!pos": {
      const pos = p.position;
      p.directMessage(
        `§e你的位置: §f${
          Math.floor(pos.x)}, ${
          Math.floor(pos.y)}, ${
          Math.floor(pos.z)}`
      );
      return false;
    }

    case "!online":
      p.directMessage(
        `§e在线玩家: §f${world.querySelectorAll("*").length} 人`
      );
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
  return true;  // 不是命令的消息正常发送
});
```

**关键点：** 回调返回 `false` 会阻止消息在聊天栏显示，返回 `true` 则正常发送。

## 1.5 定时任务

```js
// 每 5 分钟广播一次 (6000 ticks)
world.setInterval(() => {
  const online = world.querySelectorAll("*").length;
  if (online > 0) {
    world.say(`§7[服务器] 当前在线: §f${online} §7人`);
  }
}, 6000);

// 30 秒后执行一次 (600 ticks)
world.setTimeout(() => {
  world.say("§6[服务器] §f已运行 30 秒");
}, 600);
```

**Ticks 换算：** 20 ticks = 1 秒。`setInterval(fn, 20)` = 每秒执行一次。

## 1.6 世界属性

```js
// 时间控制
world.time = 6000;      // 正午 (0=日出, 6000=正午, 12000=日落, 18000=午夜)
world.timeScale = 0;    // 暂停时间
world.timeScale = 1;    // 恢复

// 天气
world.rainDensity = 1.0;
world.thunderDensity = 0.5;
world.clearWeather();   // 晴天

// 难度
world.difficulty = "hard";  // peaceful / easy / normal / hard

// 游戏规则
world.setGameRule("keepInventory", true);  // 死亡不掉落
world.setGameRule("doFireTick", false);    // 火焰不蔓延
world.setGameRule("doMobSpawning", false); // 禁止刷怪
```

## 1.7 消息类型汇总

```js
world.say("全体可见");              // 全服广播（聊天栏）

player.directMessage("仅你可见");    // 私密消息（聊天栏）

player.actionBar("快捷栏上方");      // 动作栏（快捷栏上方）

player.title("§6§lBOSS名称", "§7副标题");  // 屏幕标题
player.title("主标题", "副标题", 10, 60, 10);  // fadeIn, stay, fadeOut (ticks)
```

## 1.8 完整示例

把以上整合起来：

```js
// ═══════════════════════════════════
//  MyTutorial — 入门示例
// ═══════════════════════════════════

console.log("[MyTutorial] 脚本已加载");

// 欢迎特效
world.onPlayerJoin((entity, _tick) => {
  const p = entity.player;
  p.title("§6§l欢迎来到服务器！", "§7输入 §f!help §7查看命令", 5, 70, 10);
  const pos = p.position;
  world.spawnParticleCircle(pos.x, pos.y, pos.z, 1.5, "minecraft:happy_villager", 15);
  world.playSound("minecraft:block.note_block.pling", pos, 1.0, 1.5);
});

// 定时公告
world.setInterval(() => {
  const online = world.querySelectorAll("*").length;
  if (online > 0) world.say(`§7在线: §f${online} §7人`);
}, 6000);

// 聊天命令
world.onChat((entity, message, _tick) => {
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
      p.directMessage(`§e位置: §f${Math.floor(pos.x)} ${Math.floor(pos.y)} ${Math.floor(pos.z)}`);
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

## 下一步

教程二将介绍玩家操控：传送、物品给予、药水效果、游戏模式、生命值和经验。
