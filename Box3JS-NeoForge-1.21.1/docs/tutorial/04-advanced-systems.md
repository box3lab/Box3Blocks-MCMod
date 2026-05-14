# 教程四：高级游戏系统

本教程涵盖计分板、BossBar、队伍、世界边界、跨脚本通信等游戏系统。

## 4.1 计分板

```js
// 创建计分板
world.addScoreboard("kills");                    // dummy 类型（手动计分）
world.addScoreboard("deaths", "deathCount");     // MC 自动统计死亡

// 设置分数
world.setScore("Steve", "kills", 5);
world.setScore(entity, "kills", 10);  // 也可以用实体对象

// 读取
const kills = world.getScore("Steve", "kills");

// 显示在屏幕右侧
world.showScoreboard("sidebar", "kills");

// 显示在 Tab 列表
world.showScoreboard("list", "deaths");

// 列出所有分数
const scores = world.listScores("kills");
// [{name: "Steve", value: 5}, {name: "Alex", value: 3}, ...]

// 隐藏/删除
world.hideScoreboard("sidebar");
world.removeScoreboard("kills");
```

### 实战：在线时长排行

```js
world.addScoreboard("playtime", "dummy");
world.showScoreboard("sidebar", "playtime");

// 每分钟 +1
setInterval(() => {
  world.querySelectorAll("*").forEach((entity) => {
    if (!entity.isPlayer()) { return; }
    const p = entity.player;
    const current = world.getScore(p.name, "playtime");
    world.setScore(p.name, "playtime", current + 1);
  });
}, 1200);

// 玩家加入初始化
world.onPlayerJoin((entity, _tick) => {
  const p = entity.player;
  world.setScore(p.name, "playtime", 0);
  p.setPlayerListName(`§7[§f${p.name}§7]`);
});
```

### 实战：击杀计数

```js
world.addScoreboard("kills");
world.showScoreboard("sidebar", "kills");

world.onEntityDeath((entity, killer, _tick) => {
  if (killer?.isPlayer()) {
    const p = killer.player;
    const current = world.getScore(p.name, "kills");
    world.setScore(p.name, "kills", current + 1);
    p.actionBar(`§e击杀: §f${current + 1}`);
  }
});
```

## 4.2 BossBar

BossBar 在屏幕上方显示一个带标题的进度条，常用于 Boss 战或全局倒计时。

```js
// 基本用法
world.showBossbar("my_bar", "§c§lBoss Name", 1.0, "red");

// 更新
world.showBossbar("my_bar", "§c§lBoss Name §7[50%]", 0.5, "yellow");

// 移除
world.removeBossbar("my_bar");
```

颜色选项：`"blue"` `"green"` `"pink"` `"purple"` `"red"` `"white"` `"yellow"`

### 实战：30 秒倒计时

```js
let timeLeft = 30;
world.showBossbar("demo_timer", "§e倒计时演示", 1.0, "green");

const timerId = setInterval(() => {
  timeLeft--;
  if (timeLeft <= 0) {
    world.removeBossbar("demo_timer");
    timerId.cancel();
    world.say("§c⏰ 时间到！");
    world.playSound("minecraft:block.note_block.pling", new GameVector3(0, 70, 0), 1.0, 0.5);
    return;
  }

  const progress = timeLeft / 30;
  let color = "red";
  if (progress > 0.5) { color = "green"; }
  else if (progress > 0.2) { color = "yellow"; }

  world.showBossbar("demo_timer", `§e倒计时: §f${timeLeft} §e秒`, progress, color);

  if (timeLeft <= 5) {
    world.playSound("minecraft:block.note_block.pling", new GameVector3(0, 70, 0), 0.5, 2.0);
  }
}, 20);
```

效果：屏幕顶部出现倒计时条，最后 5 秒每秒响铃，时间到变红并播放音效。

## 4.3 队伍系统

```js
// 创建队伍
world.createTeam("red", "red");
world.createTeam("blue", "blue");

// 玩家加入队伍
world.joinTeam(entity, "red");
entity.player.directMessage("§c你加入了 §l红队");
entity.player.setPlayerListName(`§c[红] §f${entity.player.name}`);

// 获取队伍
const team = world.getTeamOf(entity);  // "red" 或 null

// 移出队伍
world.leaveTeam(entity);

// 删除队伍
world.removeTeam("red");
```

### 实战：队伍分配 + 粒子效果

```js
world.onChat((entity, message, _tick) => {
  const p = entity.player;

  if (message === "!team-red") {
    world.joinTeam(entity, "red");
    p.directMessage("§c你加入了 §l红队");
    p.setPlayerListName(`§c[红] §f${p.name}`);
    world.spawnParticle(
      "minecraft:redstone",
      p.position.x, p.position.y + 2, p.position.z,
      10, 0.3, 0.3, 0.3, 0.02
    );
    return false;
  }

  if (message === "!team-blue") {
    world.joinTeam(entity, "blue");
    p.directMessage("§9你加入了 §l蓝队");
    p.setPlayerListName(`§9[蓝] §f${p.name}`);
    world.spawnParticle(
      "minecraft:soul_fire_flame",
      p.position.x, p.position.y + 2, p.position.z,
      10, 0.3, 0.3, 0.3, 0.02
    );
    return false;
  }
  return true;
});
```

## 4.4 世界边界

世界边界可以制造动态缩圈效果，配合 PvP 或生存玩法。

```js
// 设置边界
world.setBorderCenter(0, 0);
world.borderSize = 500;
world.setBorderDamage(2);        // 边界外每秒伤害
world.setBorderWarning(10);      // 屏幕变红预警距离

// 平滑缩圈：从当前大小缩到 100，耗时 120 秒
world.shrinkBorder(100, 120);

// 读取当前大小
console.log(world.borderSize);
```

### 实战：缩圈公告

```js
world.say("§c⚠ 边界将在 5 秒后开始缩小！");
world.setBorderCenter(0, 0);
world.borderSize = 200;
world.setBorderDamage(1);
world.setBorderWarning(10);

setTimeout(() => {
  world.say("§c边界缩小至 50 格！");
  world.shrinkBorder(50, 60);
  world.playSound(
    "minecraft:entity.wither.spawn",
    new GameVector3(0, 70, 0), 0.5, 0.8
  );
}, 100);
```

## 4.5 跨脚本通信

不同脚本项目之间可以通过 `sendMessage` / `onMessage` 通信。

脚本 A（发送方）：

```js
// 发送给指定项目
world.sendMessage("minigame_hub", { action: "start", level: 2 });

// 广播给所有项目
world.sendMessage("*", { action: "reload_config" });
```

脚本 B（接收方）：

```js
world.onMessage((from: string, data: unknown) => {
  const msg = data as Record<string, unknown> | null;
  console.log(`收到来自 ${from} 的消息:`, JSON.stringify(msg));

  if (msg?.action === "start") {
    startGame(Number(msg.level));
  } else if (msg?.action === "reload_config") {
    reloadConfig();
  }
});
```

## 4.6 抛射物与爆炸

```js
// 抛射物: (类型, 起点, 目标, 速度)
world.launchProjectile("minecraft:fireball", fromPos, targetPos, 2);

// 爆炸: (x, y, z, 威力, 是否引火)
world.explode(0, 100, 0, 4);         // 威力 4，不引火
world.explode(0, 100, 0, 8, true);   // 威力 8，引火
```

## 4.7 小游戏设计模式总结

| 系统 | 用途 | 关键 API |
|------|------|----------|
| 计分板 | 击杀数、积分、排行榜 | `world.addScoreboard()` / `setScore()` / `showScoreboard()` |
| BossBar | 倒计时、Boss 血量、全局进度 | `world.showBossbar()` / `removeBossbar()` |
| 队伍 | 分队、友好标记、对战分组 | `world.createTeam()` / `joinTeam()` |
| 世界边界 | 缩圈、毒圈 | `world.borderSize` / `shrinkBorder()` |
| 抛射物 | Boss 技能、弹幕 | `world.launchProjectile()` |
| 爆炸 | 破坏性事件、陷阱 | `world.explode()` |
| 跨脚本消息 | 模块间通信 | `world.sendMessage()` / `onMessage()` |

## 下一步

教程五将介绍可视化特效：粒子、烟花、闪电、音效，以及两个完整的小游戏示例。
