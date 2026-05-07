# 教程四：高级游戏系统

本教程涵盖 BossBar、粒子/烟花/闪电、世界边界、抛射物、爆炸等视觉效果，以及跨脚本通信。

## 4.1 BossBar 血条

BossBar 在屏幕上方显示一个带标题的进度条，常用于 Boss 战或全局倒计时。

```js
// 基本用法
world.showBossbar("boss_hp", "§c§l远古巨龙", 1.0, "red");

// 更新进度
world.showBossbar("boss_hp", "§c§l远古巨龙 §7[50%]", 0.5, "yellow");

// 移除
world.removeBossbar("boss_hp");
```

颜色选项：`"blue"`、`"green"`、`"pink"`、`"purple"`、`"red"`、`"white"`、`"yellow"`。

### 实战：Boss 血量同步

```js
const bossBarId = "dragon_boss";

world.onEntityDamage((entity, amount, source, attacker, tick) => {
  if (!entity.hasTag("boss")) return;

  const hpPercent = entity.hp / entity.maxHp;
  if (hpPercent <= 0) {
    world.removeBossbar(bossBarId);
    return;
  }

  let color = "green";
  if (hpPercent < 0.3) color = "red";
  else if (hpPercent < 0.6) color = "yellow";

  world.showBossbar(
    bossBarId,
    `§c§lBoss §f${entity.nameTag} §7[${Math.ceil(entity.hp)}/${entity.maxHp}]`,
    hpPercent,
    color,
  );
});
```

### 实战：全局倒计时

```js
let timeLeft = 300; // 5 分钟

const timerId = world.setInterval(() => {
  timeLeft--;

  if (timeLeft <= 0) {
    world.removeBossbar("countdown");
    world.clearInterval(timerId);
    world.say("§c时间到！");
    return;
  }

  const progress = timeLeft / 300;
  const mins = Math.floor(timeLeft / 60);
  const secs = timeLeft % 60;
  const color = progress > 0.3 ? "green" : progress > 0.1 ? "yellow" : "red";

  world.showBossbar(
    "countdown",
    `§e剩余时间: §f${mins}:${secs.toString().padStart(2, "0")}`,
    progress,
    color,
  );
}, 20); // 每秒更新
```

## 4.2 粒子效果

```js
// 单点粒子: (类型, x, y, z, 数量, dx, dy, dz, 速度)
world.spawnParticle("minecraft:flame", 0, 100, 0, 20, 0.5, 0.5, 0.5, 0.05);

// 圆形粒子圈: (x, y, z, 半径, 类型, 数量)
world.spawnParticleCircle(0, 100, 0, 3.0, "minecraft:happy_villager", 30);

// 常用粒子:
// minecraft:flame           火焰
// minecraft:cloud           烟雾
// minecraft:happy_villager  绿色粒子
// minecraft:witch           紫色粒子
// minecraft:portal          传送门
// minecraft:end_rod         末地烛光
// minecraft:heart           爱心
// minecraft:note            音符
// minecraft:dragon_breath   龙息
// minecraft:angry_villager  愤怒粒子
```

### 实战：Boss 出场特效

```js
function bossSpawnEffect(pos) {
  // 螺旋上升粒子
  for (let i = 0; i < 40; i++) {
    const angle = (i / 40) * Math.PI * 4;
    const radius = 2;
    const px = pos.x + Math.cos(angle) * radius;
    const pz = pos.z + Math.sin(angle) * radius;
    const py = pos.y + i * 0.2;

    world.setTimeout(() => {
      world.spawnParticle("minecraft:portal", px, py, pz, 3, 0, 0, 0, 0);
    }, i * 2);
  }

  // 地面圆形粒子
  world.spawnParticleCircle(pos.x, pos.y, pos.z, 3, "minecraft:end_rod", 50);
}
```

## 4.3 烟花与闪电

```js
// 闪电 (x, y, z, 伤害)
world.strikeLightning(0, 100, 0);        // 默认伤害
world.strikeLightning(0, 100, 0, 10);    // 10 点伤害

// 烟花 (x, y, z, 颜色, 形状)
world.launchFirework(0, 100, 0, "gold", "large_ball");
world.launchFirework(pos, "red", "star");
```

烟花形状：`"ball"`、`"large_ball"`、`"star"`、`"creeper"`、`"burst"`

烟花颜色：`"red"`、`"blue"`、`"green"`、`"yellow"`、`"gold"`、`"white"`、`"aqua"`、`"pink"`、`"purple"`

### 实战：击杀烟花

```js
world.onEntityDeath((entity, killer, tick) => {
  if (!killer || !killer.isPlayer()) return;

  const pos = entity.position;

  // Boss 击杀特效
  if (entity.hasTag("boss")) {
    world.strikeLightning(pos, 0); // 无伤害闪电，纯视觉效果
    world.setTimeout(() => world.launchFirework(pos.x, pos.y + 2, pos.z, "gold", "large_ball"), 5);
    world.setTimeout(() => world.launchFirework(pos.x, pos.y + 2, pos.z, "red", "star"), 10);
    world.setTimeout(() => world.launchFirework(pos.x, pos.y + 2, pos.z, "purple", "burst"), 15);
    world.say("§6" + killer.player.name + " §f击败了 §c" + entity.nameTag + "§f！");
  }
});
```

## 4.4 世界边界

世界边界可以制造动态缩圈效果，配合 PvP 或生存玩法。

```js
// 设置边界
world.setBorderCenter(0, 0);
world.borderSize = 500;
world.setBorderDamage(2);       // 边界外每秒伤害
world.setBorderWarning(10);     // 屏幕变红预警距离

// 平滑缩圈到 100 格，耗时 120 秒
world.shrinkBorder(100, 120);

// 读取当前大小
console.log(world.borderSize);
```

### 实战：缩圈公告

```js
function startShrinkCycle() {
  const stages = [
    { size: 300, delay: 600, duration: 60 },
    { size: 150, delay: 2400, duration: 90 },
    { size: 50, delay: 4800, duration: 120 },
  ];

  world.setBorderCenter(0, 0);
  world.borderSize = 500;
  world.setBorderDamage(1);
  world.setBorderWarning(10);

  world.say("§c边界将在 30 秒后开始缩小！");

  stages.forEach((stage) => {
    world.setTimeout(() => {
      world.say(`§c边界缩小至 ${stage.size} 格！`);
      world.shrinkBorder(stage.size, stage.duration);
    }, stage.delay);
  });
}
```

## 4.5 抛射物与爆炸

```js
// 抛射物: (类型, 起点x, y, z, 目标x, y, z, 速度)
const proj = world.launchProjectile("minecraft:fireball", 0, 100, 0, 10, 100, 10, 2);
// 也可用 pos 重载
world.launchProjectile("minecraft:arrow", pos, targetPos, 3);

// 爆炸: (x, y, z, 威力, 是否引火)
world.explode(0, 100, 0, 4);        // 威力 4，不引火
world.explode(0, 100, 0, 8, true);  // 威力 8，引火
```

### 实战：Boss 技能——火球连射

```js
function bossFireballAttack(boss) {
  const players = world.querySelectorAll("*");
  if (players.length === 0) return;

  // 向每个玩家发射火球
  players.forEach((player, i) => {
    world.setTimeout(() => {
      const bossPos = boss.position;
      const targetPos = player.position;
      world.launchProjectile(
        "minecraft:fireball",
        bossPos.x, bossPos.y + 1, bossPos.z,
        targetPos.x, targetPos.y, targetPos.z,
        1.5,
      );
    }, i * 200); // 间隔 200 ticks
  });
}

// 每 10 秒攻击一次
world.setInterval(() => {
  const bosses = world.querySelectorAll(".boss");
  bosses.forEach((boss) => bossFireballAttack(boss));
}, 200);
```

## 4.6 音效

```js
// 全局音效（所有玩家听到）
world.playSound("minecraft:block.note_block.pling", 0, 100, 0, 1.0, 1.5);

// 仅某个玩家听到
player.playSound("minecraft:block.note_block.pling", 1.0, 1.5);

// 常用音效:
// minecraft:block.note_block.pling         铃铛
// minecraft:entity.experience_orb.pickup    经验球
// minecraft:entity.player.levelup           升级
// minecraft:entity.ender_dragon.growl       末影龙吼
// minecraft:entity.wither.spawn             凋零生成
// minecraft:entity.lightning_bolt.thunder   雷鸣
// minecraft:block.anvil.land                铁砧落地
```

## 4.7 跨脚本通信

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
world.onMessage((from, data) => {
  console.log("收到来自 " + from + " 的消息:", data);

  if (data.action === "start") {
    startGame(data.level);
  } else if (data.action === "reload_config") {
    reloadConfig();
  }
});
```

## 4.8 射线检测

```js
// 从实体眼睛位置向下检测
const down = new GameVector3(0, -1, 0);
const result = world.raycast(player.position, down, 50);

if (result.hit) {
  console.log("命中方块:", result.voxel, "距离:", result.distance);
  if (result.entity) {
    console.log("命中实体:", result.entity.entityType);
  }
}
```

返回值：`{ hit, x, y, z, normalX, normalY, normalZ, distance, entity, voxel }`

## 4.9 完整示例：PvP 竞技场

一个完整的队伍 PvP 小游戏，整合了事件、BossBar、粒子、烟花、边界缩圈、抛射物等系统。

```js
// ═══════════════════════════════════════════
//  PvP 竞技场 — 完整示例
// ═══════════════════════════════════════════

console.log("[PvPArena] 脚本已加载");

// ── 配置 ──
const ARENA_CENTER = new GameVector3(0, 70, 0);
const ARENA_RADIUS = 80;
const GAME_DURATION = 300; // 300 秒
const SHRINK_START = 120;  // 120 秒后开始缩圈
const MAX_PLAYERS = 16;

// ── 游戏状态 ──
let gameState = "waiting"; // waiting | starting | playing | ending
let gameTimer = null;
let lobbyTimer = null;
let playersReady = 0;
let redSpawn = new GameVector3(-20, 70, 0);
let blueSpawn = new GameVector3(20, 70, 0);

// ── 初始化 ──
world.setGameRule("keepInventory", false);
world.setGameRule("doMobSpawning", false);
world.clearWeather();
world.time = 6000; // 正午
world.timeScale = 0; // 冻结时间

// 创建计分板
world.addScoreboard("pvp_kills");
world.addScoreboard("pvp_score");
world.showScoreboard("sidebar", "pvp_score");

// 创建队伍
world.createTeam("red", "red");
world.createTeam("blue", "blue");

// ── 聊天命令 ──
world.onChat((entity, message, tick) => {
  const p = entity.player;

  switch (message) {
    case "!join":
      if (gameState !== "waiting") {
        p.directMessage("§c游戏已开始，无法加入");
        return false;
      }
      if (playersReady >= MAX_PLAYERS) {
        p.directMessage("§c竞技场已满");
        return false;
      }
      playersReady++;
      p.directMessage("§a你已加入竞技场！当前 " + playersReady + "/" + MAX_PLAYERS + " 人");

      // 当足够人数后开始倒计时
      if (playersReady >= 2 && gameState === "waiting") {
        startLobbyCountdown();
      }
      return false;

    case "!leave":
      if (gameState === "waiting") {
        playersReady = Math.max(0, playersReady - 1);
        p.directMessage("§7你已退出竞技场");
      }
      return false;

    case "!pvp":
      p.directMessage("§e── PvP 竞技场帮助 ──");
      p.directMessage("§f!join  §7- 加入竞技场");
      p.directMessage("§f!leave §7- 退出等待");
      p.directMessage("§f当前状态: " + gameState + " | 玩家: " + playersReady);
      return false;
  }
  return true;
});

// ── 大厅倒计时 ──
function startLobbyCountdown() {
  gameState = "starting";
  let countdown = 30;

  lobbyTimer = world.setInterval(() => {
    countdown--;

    if (countdown <= 0) {
      world.clearInterval(lobbyTimer);
      startGame();
    } else if (countdown <= 5) {
      world.say("§e游戏将在 §c" + countdown + " §e秒后开始！");
      world.playSound("minecraft:block.note_block.pling", ARENA_CENTER, 1.0, 1.5);
    } else if (countdown % 10 === 0) {
      world.say("§7游戏将在 " + countdown + " 秒后开始...");
    }
  }, 20);
}

// ── 游戏开始 ──
function startGame() {
  gameState = "playing";
  world.timeScale = 1;

  const allPlayers = world.querySelectorAll("*");

  // 分配队伍
  allPlayers.forEach((entity, i) => {
    const p = entity.player;

    // 清空背包
    p.clearInventory();
    p.hp = 20;
    p.maxHp = 20;
    p.food = 20;

    if (i % 2 === 0) {
      world.joinTeam(entity, "red");
      p.teleport(redSpawn);
      p.directMessage("§c你加入了 §l红队");
      p.setPlayerListName("§c[红] §f" + p.name);
      // 红队装备
      p.giveItem("minecraft:iron_sword", 1);
      p.giveItem("minecraft:bow", 1);
      p.giveItem("minecraft:arrow", 32);
      p.giveItem("minecraft:golden_apple", 3);
    } else {
      world.joinTeam(entity, "blue");
      p.teleport(blueSpawn);
      p.directMessage("§9你加入了 §l蓝队");
      p.setPlayerListName("§9[蓝] §f" + p.name);
      // 蓝队装备
      p.giveItem("minecraft:iron_sword", 1);
      p.giveItem("minecraft:crossbow", 1);
      p.giveItem("minecraft:arrow", 32);
      p.giveItem("minecraft:golden_apple", 3);
    }

    p.giveItem("minecraft:cooked_beef", 16);
    p.directMessage("§7竞技场半径: " + ARENA_RADIUS + " 格");

    // 出场粒子效果
    const pos = p.position;
    world.spawnParticleCircle(pos.x, pos.y, pos.z, 1.5, "minecraft:happy_villager", 20);
    world.playSound("minecraft:entity.player.levelup", pos, 1.0, 1.0);
  });

  // 边界初始化
  world.setBorderCenter(ARENA_CENTER.x, ARENA_CENTER.z);
  world.borderSize = ARENA_RADIUS * 2;
  world.setBorderDamage(1);
  world.setBorderWarning(5);

  // 全局公告
  world.say("§c§l⚔ 竞技场开始！击杀敌人获取积分 ⚔");
  world.playSound("minecraft:entity.ender_dragon.growl", ARENA_CENTER, 1.0, 1.0);

  // 倒计时显示
  let timeRemaining = GAME_DURATION;
  const gameTimerId = world.setInterval(() => {
    timeRemaining--;

    const progress = timeRemaining / GAME_DURATION;
    const mins = Math.floor(timeRemaining / 60);
    const secs = timeRemaining % 60;
    const color = progress > 0.3 ? "green" : progress > 0.1 ? "yellow" : "red";

    world.showBossbar(
      "pvp_timer",
      `§e战斗剩余: §f${mins}:${secs.toString().padStart(2, "0")}`,
      progress,
      color,
    );

    // 缩圈触发
    if (timeRemaining === SHRINK_START) {
      world.say("§c边界开始缩小！向中心移动！");
      world.shrinkBorder(20, 60);
      world.playSound("minecraft:block.note_block.bass", ARENA_CENTER, 1.0, 1.0);
    }

    if (timeRemaining === 60) {
      world.say("§c最后一分钟！");
    }

    if (timeRemaining === 30) {
      // 向中心召唤闪电
      world.strikeLightning(ARENA_CENTER.x, ARENA_CENTER.y, ARENA_CENTER.z, 0);
    }

    if (timeRemaining <= 0) {
      world.clearInterval(gameTimerId);
      endGame();
    }
  }, 20);
  gameTimer = gameTimerId;

  // 定时奖励空投
  world.setInterval(() => {
    if (gameState !== "playing") return;

    const angle = Math.random() * Math.PI * 2;
    const dist = Math.random() * ARENA_RADIUS * 0.6;
    const dropX = ARENA_CENTER.x + Math.cos(angle) * dist;
    const dropZ = ARENA_CENTER.z + Math.sin(angle) * dist;

    // 空投降落特效
    world.strikeLightning(dropX, ARENA_CENTER.y + 30, dropZ, 0);
    world.setTimeout(() => {
      world.dropItem(dropX, ARENA_CENTER.y + 1, dropZ, "minecraft:ender_pearl", 2);
      world.dropItem(dropX, ARENA_CENTER.y + 1, dropZ, "minecraft:golden_apple", 2);
      world.launchFirework(dropX, ARENA_CENTER.y + 3, dropZ, "yellow", "ball");
      world.say("§e☄ 空投已降落！");
    }, 20);
  }, 1200); // 每 60 秒
}

// ── 击杀计分 ──
world.onEntityDeath((entity, killer, tick) => {
  if (gameState !== "playing") return;

  // 玩家击杀
  if (killer && killer.isPlayer() && entity.isPlayer()) {
    const kp = killer.player;
    const team = world.getTeamOf(killer);

    // 增加击杀数
    const currentKills = world.getScore(kp.name, "pvp_kills");
    world.setScore(kp.name, "pvp_kills", currentKills + 1);

    // 团队分数
    const teamScore = world.getScore(team, "pvp_score");
    world.setScore(team, "pvp_score", teamScore + 1);

    // 个人奖励
    kp.addExperienceLevels(2);
    kp.playSound("minecraft:entity.player.levelup", 1.0, 1.0);

    // 击杀特效
    const pos = entity.position;
    world.spawnParticleCircle(pos.x, pos.y, pos.z, 2, "minecraft:angry_villager", 15);
    world.launchFirework(pos.x, pos.y + 1, pos.z, "red", "star");

    // 全局击杀播报
    const killedTeam = world.getTeamOf(entity);
    if (killedTeam !== team) {
      world.say(
        `§c[${team}] §f${kp.name} §7击杀了 §f[${killedTeam}] ${entity.player.name} §7(${currentKills + 1} 杀)`
      );
    }
  }
});

// ── 死亡处理 ──
world.onPlayerRespawn((entity, tick) => {
  if (gameState !== "playing") return;

  const team = world.getTeamOf(entity);
  const p = entity.player;

  if (team === "red") {
    p.teleport(redSpawn);
  } else if (team === "blue") {
    p.teleport(blueSpawn);
  }

  // 重生后补装备
  p.giveItem("minecraft:iron_sword", 1);
  p.giveItem("minecraft:cooked_beef", 4);
  p.addEffect("minecraft:regeneration", 100, 1, true);
  p.addEffect("minecraft:resistance", 100, 2, true); // 短暂无敌
});

// ── 玩家离开处理 ──
world.onPlayerLeave((entity, tick) => {
  if (gameState === "waiting" || gameState === "starting") {
    playersReady = Math.max(0, playersReady - 1);
  }
});

// ── 游戏结束 ──
function endGame() {
  gameState = "ending";
  world.removeBossbar("pvp_timer");

  // 统计分数
  const redScore = world.getScore("red", "pvp_score");
  const blueScore = world.getScore("blue", "pvp_score");

  let winner = "";
  let color = "";

  if (redScore > blueScore) {
    winner = "红队";
    color = "c";
  } else if (blueScore > redScore) {
    winner = "蓝队";
    color = "9";
  } else {
    winner = "";
    color = "e";
  }

  // 胜利公告
  const allPlayers = world.querySelectorAll("*");
  allPlayers.forEach((entity) => {
    const p = entity.player;
    p.title(`§${color}§l${winner ? winner + " 获胜！" : "平局！"}`, "§7竞技场结束", 10, 80, 10);
    p.playSound(
      "minecraft:ui.toast.challenge_complete",
      1.0,
      1.0,
    );
  });

  if (winner) {
    world.say(
      `§${color}§l🏆 ${winner} §f以 §e${Math.max(redScore, blueScore)} §f分获胜！`
    );
  } else {
    world.say(`§e§l🤝 平局！双方各得 §f${redScore} §e分`);
  }

  world.say("§7红队: " + redScore + " 分 | 蓝队: " + blueScore + " 分");

  // 烟花庆祝
  for (let i = 0; i < 10; i++) {
    world.setTimeout(() => {
      const colors = ["red", "gold", "green", "blue", "purple"];
      const shapes = ["ball", "large_ball", "star", "burst"];
      const c = colors[Math.floor(Math.random() * colors.length)];
      const s = shapes[Math.floor(Math.random() * shapes.length)];
      world.launchFirework(
        ARENA_CENTER.x + (Math.random() - 0.5) * 20,
        ARENA_CENTER.y + Math.random() * 5,
        ARENA_CENTER.z + (Math.random() - 0.5) * 20,
        c,
        s,
      );
    }, i * 400);
  }

  // 30 秒后重置
  world.setTimeout(() => {
    resetGame();
  }, 600);
}

function resetGame() {
  gameState = "waiting";
  playersReady = 0;

  world.removeScoreboard("pvp_kills");
  world.removeScoreboard("pvp_score");
  world.addScoreboard("pvp_kills");
  world.addScoreboard("pvp_score");
  world.hideScoreboard("sidebar");
  world.showScoreboard("sidebar", "pvp_score");

  world.removeTeam("red");
  world.removeTeam("blue");
  world.createTeam("red", "red");
  world.createTeam("blue", "blue");

  world.clearWeather();
  world.time = 6000;

  // 恢复边界
  world.setBorderCenter(0, 0);
  world.borderSize = 60000000;

  world.say("§a竞技场已重置，输入 §f!join §a加入下一局");
}
```

## 4.10 小游戏设计模式总结

| 系统 | 用途 | 关键 API |
|------|------|----------|
| BossBar | 倒计时、Boss 血量、全局进度 | `world.showBossbar()` / `removeBossbar()` |
| 记分板 | 击杀数、积分、排行榜 | `world.addScoreboard()` / `setScore()` / `showScoreboard()` |
| 队伍 | 分队、友好标记 | `world.createTeam()` / `joinTeam()` |
| 世界边界 | 缩圈、毒圈 | `world.borderSize` / `shrinkBorder()` |
| 粒子 | 出/退场特效、区域标记 | `world.spawnParticle()` / `spawnParticleCircle()` |
| 烟花 | 庆祝、击杀特效 | `world.launchFirework()` |
| 闪电 | 警告、空投标记 | `world.strikeLightning()` |
| 音效 | 提示、氛围 | `world.playSound()` / `player.playSound()` |
| 定时器 | 倒计时、阶段推进、定时事件 | `world.setInterval()` / `setTimeout()` |
| 跨脚本消息 | 模块间通信 | `world.sendMessage()` / `onMessage()` |

## 下一步

## 下一步

教程五收集了更多独立实用示例：聊天命令、传送系统、防破坏、波次刷怪、赛跑检查点、捉迷藏、计分板应用等。

更多 API 细节请参考 `docs/api/` 中的完整 API 文档。
