# 教程五：实用示例集

本文收集了各种独立、可直接使用的小脚本示例，按场景分类。

## 5.1 聊天命令

### 弹幕颜色命令

```js
world.onChat((entity, message, tick) => {
  const p = entity.player;
  const colors = { "r": "c", "g": "a", "b": "9", "y": "e", "p": "d", "w": "f" };
  const match = message.match(/^!(\w)\s(.+)/);

  if (match && colors[match[1]]) {
    world.say(`§${colors[match[1]]}[${p.name}] §f${match[2]}`);
    return false; // 阻止原始消息
  }
  return true;
});
// 用法: !r 大家好  → 红色发送
```

### 新手帮助命令

```js
world.onChat((entity, message, tick) => {
  const p = entity.player;

  switch (message) {
    case "!help":
      p.directMessage("§6── 服务器命令帮助 ──");
      p.directMessage("§f!home  §7- 传送回家");
      p.directMessage("§f!shop  §7- 打开商店");
      p.directMessage("§f!tpa <玩家>  §7- 请求传送");
      p.directMessage("§f!ignore <玩家>  §7- 屏蔽玩家");
      p.directMessage("§f!vote  §7- 投票换图");
      return false;
  }
  return true;
});
```

### 私聊系统

```js
world.onChat((entity, message, tick) => {
  const p = entity.player;
  const match = message.match(/^!msg\s+(\S+)\s+(.+)/);

  if (match) {
    const targetName = match[1];
    const msg = match[2];
    const targets = world.querySelectorAll("*");
    let found = false;

    targets.forEach((e) => {
      if (e.player.name.toLowerCase() === targetName.toLowerCase()) {
        e.player.directMessage(`§d[${p.name} → 你] §f${msg}`);
        p.directMessage(`§d[你 → ${e.player.name}] §f${msg}`);
        found = true;
      }
    });

    if (!found) p.directMessage(`§c玩家 ${targetName} 不在线`);
    return false;
  }
  return true;
});
```

## 5.2 传送系统

### 家传送

```js
// 玩家用属性存储家坐标
world.onChat((entity, message, tick) => {
  const p = entity.player;

  switch (message) {
    case "!sethome":
      entity.homeX = entity.position.x;
      entity.homeY = entity.position.y;
      entity.homeZ = entity.position.z;
      p.directMessage("§a家已设置！输入 !home 回家");
      return false;

    case "!home":
      if (entity.homeX === undefined) {
        p.directMessage("§c你还没有设置家！先输入 !sethome");
        return false;
      }
      p.teleport(new GameVector3(entity.homeX, entity.homeY, entity.homeZ));
      p.directMessage("§a已传送回家！");
      return false;
  }
  return true;
});
```

### 坐标分享

```js
world.onChat((entity, message, tick) => {
  const p = entity.player;

  if (message === "!sharepos") {
    const pos = entity.position;
    world.say(
      `§e${p.name} §f的坐标: §a[${Math.floor(pos.x)}, ${Math.floor(pos.y)}, ${Math.floor(pos.z)}]`
    );
    return false;
  }
  return true;
});
```

### 随机传送

```js
world.onChat((entity, message, tick) => {
  const p = entity.player;

  if (message === "!rtp") {
    const range = 500;
    const x = (Math.random() - 0.5) * range * 2;
    const z = (Math.random() - 0.5) * range * 2;
    p.teleport(new GameVector3(x, 150, z));
    p.directMessage(`§a已随机传送到 (${Math.floor(x)}, ~, ${Math.floor(z)})`);
    return false;
  }
  return true;
});
```

### 传送请求 (TPA)

```js
// 存储待处理的传送请求
// tpRequest 的属性: { fromName, fromEntity }

world.onChat((entity, message, tick) => {
  const p = entity.player;
  const match = message.match(/^!tpa\s+(\S+)/);

  if (match) {
    const targetName = match[1];
    const targets = world.querySelectorAll("*");

    targets.forEach((target) => {
      if (target.player.name.toLowerCase() === targetName.toLowerCase()) {
        // 在目标上存储请求
        target.tpRequest = {
          fromName: p.name,
          fromEntity: entity,
        };
        target.player.directMessage(
          `§e${p.name} §f想传送到你这里！输入 §a!tpaccept §f接受`
        );
        p.directMessage(`§a已向 ${targetName} 发送传送请求`);
      }
    });
    return false;
  }

  if (message === "!tpaccept") {
    const req = entity.tpRequest;
    if (!req) {
      p.directMessage("§c没有待处理的传送请求");
      return false;
    }
    req.fromEntity.player.teleport(entity.position);
    req.fromEntity.player.directMessage(`§a已传送到 ${p.name} 身边`);
    p.directMessage(`§a${req.fromName} 已传送到你身边`);
    entity.tpRequest = undefined;
    return false;
  }

  return true;
});
```

## 5.3 公告与定时消息

### 定时公告轮播

```js
const announcements = [
  "§e欢迎来到服务器！输入 !help 查看帮助",
  "§b遵守服务器规则，文明游戏",
  "§a遇到问题请联系管理员",
  "§d服务器每天凌晨 4 点重启",
];

let index = 0;
world.setInterval(() => {
  const online = world.querySelectorAll("*").length;
  if (online > 0) {
    world.say(`§6[公告] §f${announcements[index]}`);
    index = (index + 1) % announcements.length;
  }
}, 6000); // 每 5 分钟一条
```

### 自动重启提醒

```js
// 每 2 小时提醒一次
world.setInterval(() => {
  world.say("§4[系统] §c服务器将在 5 分钟后自动重启！");
  world.playSound("minecraft:block.note_block.bass", new GameVector3(0, 100, 0), 1.0, 1.0);

  // 4 分钟后 1 分钟警告
  world.setTimeout(() => {
    world.say("§4[系统] §c距离重启还有 1 分钟！");
  }, 4800);

  // 5 分钟后执行重启命令
  world.setTimeout(() => {
    world.say("§4[系统] §c服务器正在重启...");
    world.runCommand("stop");
  }, 6000);
}, 144000); // 7200 秒
```

## 5.4 防破坏与保护

### 出生点保护

```js
const SPAWN = new GameVector3(0, 70, 0);
const PROTECT_RADIUS = 50;

// 阻止破坏
world.onVoxelDestroy((entity, x, y, z, voxel, tick) => {
  const dx = x - SPAWN.x;
  const dz = z - SPAWN.z;
  if (Math.sqrt(dx * dx + dz * dz) < PROTECT_RADIUS) {
    if (entity.player.opLevel < 2) {
      entity.player.directMessage("§c出生点范围禁止破坏方块！");
      // 注：事件无法阻止操作，仅作提示
    }
  }
});

// 阻止放置
world.onBlockPlace((entity, x, y, z, voxel, voxelId, tick) => {
  const dx = x - SPAWN.x;
  const dz = z - SPAWN.z;
  if (Math.sqrt(dx * dx + dz * dz) < PROTECT_RADIUS) {
    if (entity.player.opLevel < 2) {
      voxels.setVoxel(x, y, z, "minecraft:air");
      entity.player.directMessage("§c出生点范围禁止放置方块！");
    }
  }
});
```

### 禁用物品

```js
const BANNED_ITEMS = ["minecraft:tnt", "minecraft:lava_bucket", "minecraft:flint_and_steel"];

world.onBlockPlace((entity, x, y, z, voxel, voxelId, tick) => {
  if (BANNED_ITEMS.includes(voxel) && entity.player.opLevel < 2) {
    voxels.setVoxel(x, y, z, "minecraft:air");
    entity.player.directMessage(`§c物品 ${voxel} 禁止放置！`);
  }
});
```

## 5.5 实体小游戏

### 波次刷怪

```js
const SPAWN_POS = new GameVector3(0, 70, 20);
let wave = 0;
let mobsAlive = 0;

function startWave() {
  wave++;
  const count = wave * 3; // 每波增加 3 只
  mobsAlive = count;

  world.say(`§c§l⚔ 第 ${wave} 波开始！§f生成 ${count} 只僵尸`);

  for (let i = 0; i < count; i++) {
    world.setTimeout(() => {
      const x = SPAWN_POS.x + (Math.random() - 0.5) * 10;
      const z = SPAWN_POS.z + (Math.random() - 0.5) * 10;
      const zombie = world.spawnEntity("minecraft:zombie", new GameVector3(x, 70, z));
      zombie.setNameTag(`§7[第${wave}波] 僵尸`);
      zombie.maxHp = 20 + wave * 5;
      zombie.hp = zombie.maxHp;
      zombie.setAI(true);
      zombie.addTag("wave_mob");
    }, i * 200); // 逐个生成，间隔 200 ticks
  }
}

// 击杀检测
world.onEntityDeath((entity, killer, tick) => {
  if (!entity.hasTag("wave_mob")) return;
  mobsAlive--;

  if (killer && killer.isPlayer()) {
    killer.player.actionBar(`§a击杀! 剩余: ${mobsAlive}`);
  }

  if (mobsAlive <= 0) {
    world.say("§a§l✔ 第 " + wave + " 波清除！");
    world.setTimeout(() => startWave(), 200); // 10 秒后下一波
  }
});

// 命令启动
world.onChat((entity, message, tick) => {
  if (message === "!wave" && entity.player.opLevel >= 2) {
    wave = 0;
    startWave();
    return false;
  }
  return true;
});
```

### 赛跑检查点

```js
const checkpoints = [
  new GameVector3(0, 70, 50),
  new GameVector3(50, 75, 50),
  new GameVector3(50, 80, 0),
  new GameVector3(0, 85, 0),
];

world.onChat((entity, message, tick) => {
  const p = entity.player;

  if (message === "!race") {
    entity.raceCheckpoint = 0;
    entity.raceStart = world.currentTick;
    p.teleport(checkpoints[0]);
    p.giveItem("minecraft:leather_boots", 1);
    p.addEffect("minecraft:speed", 99999, 2);
    p.directMessage("§e到达每个检查点后输入 !cp 前往下一站");
    return false;
  }

  if (message === "!cp") {
    const cp = entity.raceCheckpoint || 0;
    const pos = entity.position;

    // 检查是否在检查点 5 格范围内
    const target = checkpoints[cp];
    const dx = pos.x - target.x;
    const dy = pos.y - target.y;
    const dz = pos.z - target.z;
    const dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

    if (dist > 5) {
      p.directMessage(`§c你还没有到达检查点 ${cp + 1}！距离: ${Math.floor(dist)} 格`);
      return false;
    }

    entity.raceCheckpoint = cp + 1;

    if (cp + 1 >= checkpoints.length) {
      // 完赛
      const elapsed = Math.floor((world.currentTick - entity.raceStart) / 20);
      const mins = Math.floor(elapsed / 60);
      const secs = elapsed % 60;
      world.say(
        `§6🏆 ${p.name} §f完成了赛跑！用时 §e${mins}:${secs.toString().padStart(2, "0")}`
      );
      p.playSound("minecraft:ui.toast.challenge_complete", 1.0, 1.0);
      world.launchFirework(pos.x, pos.y + 2, pos.z, "gold", "large_ball");
      p.clearEffects();
    } else {
      p.directMessage(`§a到达检查点 ${cp + 1}！下一站→`);
      p.playSound("minecraft:block.note_block.pling", 1.0, 1.5);
    }
    return false;
  }
  return true;
});
```

### 隐藏玩法 (捉迷藏)

```js
let seeker = null;

world.onChat((entity, message, tick) => {
  const p = entity.player;

  if (message === "!seek" && !seeker) {
    seeker = entity;
    p.teleport(new GameVector3(0, 70, 0));
    p.giveItem("minecraft:diamond_sword", 1);
    p.addEffect("minecraft:speed", 99999, 1);
    p.addEffect("minecraft:glowing", 99999, 0);
    p.directMessage("§c你是鬼！找到所有人！");
    world.say(`§c${p.name} 成为了鬼！快躲起来！`);
    return false;
  }

  if (message === "!hide" && entity !== seeker) {
    p.giveItem("minecraft:leather_helmet", 1);
    p.addEffect("minecraft:invisibility", 99999, 0, true);
    p.directMessage("§a你已隐藏！鬼要来找你了！");
    return false;
  }

  return true;
});

world.onEntityDeath((entity, killer, tick) => {
  if (killer === seeker && entity.isPlayer() && entity !== seeker) {
    entity.player.directMessage("§c你被鬼抓住了！");
    world.say(`§c${entity.player.name} 被鬼抓住了！`);

    const remaining = world.querySelectorAll("*").filter(
      (e) => e !== seeker && !e.player.dead
    ).length;

    if (remaining <= 0) {
      world.say("§6👻 鬼赢了！所有人都被找到了！");
      seeker = null;
    }
  }
});
```

## 5.6 物品与装备

### 彩色装备发放

```js
const ARMOR_COLORS = {
  "red":    ["minecraft:red_wool", "minecraft:red_concrete"],
  "blue":   ["minecraft:blue_wool", "minecraft:blue_concrete"],
  "green":  ["minecraft:green_wool", "minecraft:lime_concrete"],
  "yellow": ["minecraft:yellow_wool", "minecraft:yellow_concrete"],
};

world.onChat((entity, message, tick) => {
  const p = entity.player;
  const color = ARMOR_COLORS[message.replace("!", "")];

  if (color) {
    p.clearInventory();
    p.giveItem("minecraft:iron_sword", 1);
    p.giveItem("minecraft:bow", 1);
    p.giveItem("minecraft:arrow", 64);
    p.giveNamedItem("minecraft:leather_helmet", 1, `§${message[1]}头盔`, []);
    p.giveNamedItem("minecraft:leather_chestplate", 1, `§${message[1]}胸甲`, []);
    p.giveNamedItem("minecraft:leather_leggings", 1, `§${message[1]}护腿`, []);
    p.giveNamedItem("minecraft:leather_boots", 1, `§${message[1]}靴子`, []);
    p.giveItem("minecraft:golden_apple", 8);
    p.directMessage(`§a已发放 ${message} 色装备！`);
    return false;
  }
  return true;
});
// 用法: !red !blue !green !yellow
```

### 物品兑换

```js
const EXCHANGE = {
  "64 minecraft:emerald":    { id: "minecraft:diamond_sword", count: 1, name: "钻石剑" },
  "32 minecraft:diamond":    { id: "minecraft:netherite_ingot", count: 2, name: "下界合金锭" },
  "16 minecraft:gold_ingot": { id: "minecraft:ender_pearl", count: 4, name: "末影珍珠" },
};

world.onChat((entity, message, tick) => {
  const p = entity.player;

  if (message === "!shop") {
    p.directMessage("§6── 兑换商店 ──");
    for (const [cost, reward] of Object.entries(EXCHANGE)) {
      p.directMessage(`§f${cost} §7→ §f${reward.count}x ${reward.name}`);
    }
    p.directMessage("§7输入 !buy <编号> 购买");
    return false;
  }

  return true;
});
```

### 蹦极跳

```js
world.onChat((entity, message, tick) => {
  const p = entity.player;

  if (message === "!bungee") {
    const pos = entity.position;
    // 向上弹射
    entity.velocity.set(0, 4, 0);
    p.addEffect("minecraft:slow_falling", 160, 0, true);
    p.playSound("minecraft:entity.breeze.wind_burst", 1.0, 1.5);
    world.spawnParticle("minecraft:cloud", pos.x, pos.y, pos.z, 30, 1, 0.5, 1, 0.02);
    return false;
  }
  return true;
});
```

## 5.7 可视化特效

### 玩家登录/退出特效

```js
world.onPlayerJoin((entity, tick) => {
  const pos = entity.position;
  world.say(`§a[+] §e${entity.player.name} §f加入了游戏`);
  world.playSound("minecraft:block.note_block.pling", pos, 1.0, 1.5);
  world.spawnParticleCircle(pos.x, pos.y, pos.z, 2, "minecraft:happy_villager", 20);
  world.launchFirework(pos.x, pos.y + 2, pos.z, "green", "ball");
});

world.onPlayerLeave((entity, tick) => {
  const pos = entity.position;
  world.say(`§c[-] §e${entity.player.name} §f离开了游戏`);
  world.playSound("minecraft:block.note_block.bass", pos, 1.0, 1.0);
  world.spawnParticle("minecraft:cloud", pos.x, pos.y, pos.z, 15, 0.5, 0.5, 0.5, 0.02);
});
```

### 区域粒子标记

```js
// 在指定区域持续显示粒子边框
function markArea(cx, cy, cz, radius, particleType, interval) {
  return world.setInterval(() => {
    const segments = 16;
    for (let i = 0; i < segments; i++) {
      const angle1 = (i / segments) * Math.PI * 2;
      const angle2 = ((i + 0.5) / segments) * Math.PI * 2;
      world.spawnParticle(
        particleType,
        cx + Math.cos(angle1) * radius, cy, cz + Math.sin(angle1) * radius,
        1, 0, 0, 0, 0,
      );
    }
  }, interval);
}

// 用法: 在竞技场周围显示火焰环
// markArea(0, 70, 0, 10, "minecraft:flame", 10);
```

### 技能冷却流光

```js
function cooldownIndicator(player) {
  const p = player.player || player;
  const pos = player.position;

  // 脚下粒子圈
  world.spawnParticleCircle(pos.x, pos.y - 0.9, pos.z, 0.8, "minecraft:end_rod", 8);
  p.playSound("minecraft:block.note_block.bell", 0.3, 2.0);
}

// 绑定聊天命令触发的技能
world.onChat((entity, message, tick) => {
  if (message === "!skill") {
    const now = world.currentTick;

    // 5 秒冷却
    if (entity.skillCooldown && now - entity.skillCooldown < 100) {
      const remain = Math.ceil((100 - (now - entity.skillCooldown)) / 20);
      entity.player.directMessage(`§c技能冷却中... ${remain} 秒`);
      return false;
    }

    entity.skillCooldown = now;

    // 自身周围爆炸粒子
    const pos = entity.position;
    world.spawnParticleCircle(pos.x, pos.y, pos.z, 3, "minecraft:witch", 40);
    world.explode(pos.x, pos.y, pos.z, 2, false);
    entity.player.addEffect("minecraft:strength", 100, 1);
    entity.player.directMessage("§6技能释放！力量 II 持续 5 秒");

    return false;
  }
  return true;
});
```

## 5.8 计分板应用

### 在线时长排行榜

```js
world.addScoreboard("playtime", "dummy");
world.showScoreboard("sidebar", "playtime");

// 每 60 秒更新一次
world.setInterval(() => {
  const players = world.querySelectorAll("*");
  players.forEach((entity) => {
    const current = world.getScore(entity.player.name, "playtime");
    world.setScore(entity.player.name, "playtime", current + 1);
  });
}, 1200);

// 玩家加入时初始化
world.onPlayerJoin((entity, tick) => {
  world.setScore(entity.player.name, "playtime", 0);
  entity.player.setPlayerListName(
    "§7[§f" + entity.player.name + "§7]"
  );
});
```

### 死亡排行榜

```js
world.addScoreboard("deaths", "deathCount"); // MC 自动统计死亡
world.showScoreboard("sidebar", "deaths");
```

### 自定义货币系统

```js
world.addScoreboard("coins", "dummy");

world.onChat((entity, message, tick) => {
  const p = entity.player;
  const coins = () => world.getScore(p.name, "coins");

  switch (message) {
    case "!coins":
      p.directMessage("§e你的金币: §6" + coins() + " ⛀");
      return false;

    case "!daily":
      // 每日签到
      if (entity.lastDaily) {
        const dayInTicks = 24000;
        if (world.currentTick - entity.lastDaily < dayInTicks) {
          const remain = Math.ceil((dayInTicks - (world.currentTick - entity.lastDaily)) / 20 / 60);
          p.directMessage(`§c签到冷却中，还需等待 ${remain} 分钟`);
          return false;
        }
      }
      entity.lastDaily = world.currentTick;
      world.setScore(p.name, "coins", coins() + 100);
      p.directMessage("§a签到成功！+100 金币");
      p.playSound("minecraft:entity.experience_orb.pickup", 1.0, 1.0);
      return false;
  }
  return true;
});
```

## 5.9 队伍应用

### 队伍聊天前缀

```js
world.onChat((entity, message, tick) => {
  const p = entity.player;
  const team = world.getTeamOf(entity) || "";

  const teamPrefix = {
    "red": "§c[红]",
    "blue": "§9[蓝]",
  }[team] || "§7";

  // 不是命令的正常消息，加队伍前缀
  if (!message.startsWith("!")) {
    world.say(`${teamPrefix}§f${p.name}: ${message}`);
  }
  return true;
});
```

### PvP 模式切换

```js
let pvpEnabled = false;

world.onChat((entity, message, tick) => {
  if (message === "!pvp" && entity.player.opLevel >= 2) {
    pvpEnabled = !pvpEnabled;

    if (pvpEnabled) {
      world.setGameRule("doMobSpawning", false);
      world.say("§c§l⚔ PvP 模式已开启！玩家可以互相攻击！");
      world.playSound("minecraft:entity.wither.spawn", new GameVector3(0, 70, 0), 1.0, 1.0);
    } else {
      world.say("§a§l☮ PvP 模式已关闭");
    }
    return false;
  }
  return true;
});
```

## 5.10 环境控制

### 投票换天气

```js
let voteClear = 0;
let voteRain = 0;
let votedPlayers = [];

world.onChat((entity, message, tick) => {
  const p = entity.player;

  switch (message) {
    case "!voteclear":
      if (votedPlayers.includes(entity.id)) {
        p.directMessage("§c你已经投过票了！");
        return false;
      }
      voteClear++;
      votedPlayers.push(entity.id);
      world.say(`§e${p.name} §f投票 §a晴天 §7(${voteClear}/${voteRain})`);
      break;

    case "!voterain":
      if (votedPlayers.includes(entity.id)) {
        p.directMessage("§c你已经投过票了！");
        return false;
      }
      voteRain++;
      votedPlayers.push(entity.id);
      world.say(`§e${p.name} §f投票 §b雨天 §7(${voteRain}/${voteClear})`);
      break;

    default:
      return true;
  }

  const total = voteClear + voteRain;
  const online = world.querySelectorAll("*").length;

  if (total >= online) {
    if (voteClear > voteRain) {
      world.clearWeather();
      world.say("§a☀ 投票结果: 晴天！");
    } else {
      world.rainDensity = 1.0;
      world.say("§b🌧 投票结果: 雨天！");
    }
    voteClear = 0;
    voteRain = 0;
    votedPlayers = [];
  }

  return false;
});
```

### 时间段控制

```js
world.onChat((entity, message, tick) => {
  const p = entity.player;

  const times = {
    "!day":    1000,
    "!noon":   6000,
    "!night":  13000,
    "!midnight": 18000,
  };

  if (times[message]) {
    world.time = times[message];
    world.say(`§e${p.name} §f将时间设为 ${message.replace("!", "")}`);
    return false;
  }
  return true;
});
```

## 5.11 AI 敌人

### 巡逻守卫

```js
function spawnPatrol(name, startPos, waypointsArr, speed) {
  const guard = world.spawnEntity("minecraft:skeleton", startPos);
  guard.setNameTag(name);
  guard.maxHp = 50;
  guard.hp = 50;
  guard.setEquipment("mainhand", "minecraft:bow");
  guard.setEquipment("head", "minecraft:iron_helmet");
  guard.setPersistent(true);
  guard.setAI(true);

  let wpIndex = 0;
  guard.waypoints = waypointsArr;
  guard.speed = speed;

  // 巡逻循环
  const tid = world.setInterval(() => {
    if (guard.destroyed) {
      world.clearInterval(tid);
      return;
    }

    const wp = guard.waypoints[wpIndex];
    const pos = guard.position;
    const dist = Math.sqrt((pos.x - wp.x) ** 2 + (pos.y - wp.y) ** 2 + (pos.z - wp.z) ** 2);

    if (dist < 2) {
      wpIndex = (wpIndex + 1) % guard.waypoints.length;
    }

    const target = guard.waypoints[wpIndex];
    guard.navigateTo(target.x, target.y, target.z, guard.speed);

    // 附近有玩家就攻击
    const nearby = world.entitiesInRadius(pos, 8);
    nearby.forEach((e) => {
      if (e.isPlayer() && !guard.getTarget()) {
        guard.setTarget(e);
      }
    });
  }, 40); // 每 2 秒检查一次

  return guard;
}

// 用法:
const route = [
  new GameVector3(0, 70, 0),
  new GameVector3(10, 70, 0),
  new GameVector3(10, 70, 10),
  new GameVector3(0, 70, 10),
];
// spawnPatrol("§c守卫A", route[0], route, 1.0);
```

### 自爆苦力怕

```js
function spawnBomber(pos, targetPos) {
  const creeper = world.spawnEntity("minecraft:creeper", pos);
  creeper.setNameTag("§c§l自爆者");
  creeper.addEffect("minecraft:speed", 99999, 2, true);
  creeper.setAI(true);
  creeper.addTag("bomber");

  // 导航到目标
  creeper.navigateTo(targetPos.x, targetPos.y, targetPos.z, 1.2);

  // 接近目标后引爆
  const checkId = world.setInterval(() => {
    if (creeper.destroyed) {
      world.clearInterval(checkId);
      return;
    }
    const dist = Math.sqrt(
      (creeper.position.x - targetPos.x) ** 2 +
      (creeper.position.z - targetPos.z) ** 2,
    );
    if (dist < 3) {
      world.explode(creeper.position, 6, false);
      creeper.destroy();
      world.clearInterval(checkId);
    }
  }, 10);

  return creeper;
}
```

## 5.12 实用工具

### 每日重置

```js
// 计算距下次重置的 tick 数
function ticksUntilReset(hour, minute) {
  const dayTicks = 24000;
  const targetTicks = hour * 1000 + (minute / 60) * 1000; // 近似
  const currentTicks = world.time % dayTicks;
  return (targetTicks - currentTicks + dayTicks) % dayTicks || dayTicks;
}

function setupDailyReset(hour, minute, callback) {
  function schedule() {
    const delay = ticksUntilReset(hour, minute);
    world.setTimeout(() => {
      callback();
      schedule(); // 安排下一天
    }, delay);
  }
  schedule();
}

// 用法: 每天 6:00 重置
// setupDailyReset(6, 0, () => {
//   world.say("§e新的一天开始了！每日奖励已刷新");
// });
```

### 座位/坐下

```js
world.onChat((entity, message, tick) => {
  const p = entity.player;

  if (message === "!sit") {
    // 在玩家位置生成一个不可见的固定实体作为"椅子"
    const pos = entity.position;
    const chair = world.createEntity({
      type: "minecraft:area_effect_cloud",
      position: new GameVector3(pos.x, pos.y - 0.5, pos.z),
      fixed: true,
      gravity: false,
      collides: false,
      meshInvisible: true,
    });
    chair.addTag("chair");

    // 让玩家骑上去（注：具体骑乘 API 取决于你的实现）
    p.directMessage("§7你坐下了... 输入 !stand 站起来");

    // 存储椅子引用
    entity.myChair = chair;
    return false;
  }

  if (message === "!stand" && entity.myChair) {
    entity.myChair.destroy();
    entity.myChair = undefined;
    p.directMessage("§7你站起来了");
    return false;
  }

  return true;
});
```

### 欢迎礼包（仅首次）

```js
// 用一个简单的数组跟踪已领取的玩家
let claimedPlayers = [];

world.onPlayerJoin((entity, tick) => {
  const p = entity.player;

  // 显示标题欢迎
  p.title("§6§l欢迎回来", "§7" + p.name, 10, 60, 10);

  // 首次加入检测
  if (!claimedPlayers.includes(p.userId)) {
    claimedPlayers.push(p.userId);
    p.directMessage("§a首次加入！获得新手礼包！");
    p.giveItem("minecraft:stone_sword", 1);
    p.giveItem("minecraft:stone_pickaxe", 1);
    p.giveItem("minecraft:stone_axe", 1);
    p.giveItem("minecraft:stone_shovel", 1);
    p.giveItem("minecraft:bread", 32);
    p.giveItem("minecraft:torch", 16);
    p.giveNamedItem("minecraft:shield", 1, "§b新手之盾", [
      "§7只有真正的初始玩家才能拥有",
    ]);
    p.playSound("minecraft:entity.player.levelup", 1.0, 1.0);
  }
});
```

---

所有示例均可独立运行。将其整合到你的 `app.ts` 中即可使用。
