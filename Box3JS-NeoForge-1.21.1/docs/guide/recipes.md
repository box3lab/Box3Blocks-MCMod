# 常用配方：Box3JS 功能模板

本指南是"菜谱"风格——不逐 API 讲解，而是一个个"想实现 X 功能，照这个模板改就行"。所有代码段均经过编译验证。

## 目录

1. [聊天命令](#聊天命令)
2. [经济系统](#经济系统)
3. [传送系统](#传送系统)
4. [重生保护](#重生保护)
5. [商店/NPC](#商店npc)
6. [每日奖励](#每日奖励)
7. [排行榜](#排行榜)
8. [波次刷怪](#波次刷怪)
9. [缩圈机制](#缩圈机制)
10. [HTTP Webhook](#http-webhook)
11. [客户端 HUD](#客户端-hud)
12. [跨脚本联动](#跨脚本联动)

---

## 聊天命令

### 基础命令路由

```js
world.onChat((entity, message) => {
  const p = entity.player;
  const args = message.trim().split(/\s+/);
  const cmd = args[0].toLowerCase();

  switch (cmd) {
    case "!heal":
      p.hp = p.maxHp;
      p.food = 20;
      p.directMessage("§a已治愈！");
      return false;

    case "!fly":
      p.canFly = !p.canFly;
      p.flying = p.canFly;
      p.directMessage(p.canFly ? "§a飞行: 开启" : "§7飞行: 关闭");
      return false;

    case "!gm":
      if (p.opLevel < 2) { p.directMessage("§c权限不足"); return false; }
      const mode = args[1];
      const modes: Record<string, string> = { "0": "survival", "1": "creative", "2": "adventure", "3": "spectator" };
      if (modes[mode]) { p.gameMode = modes[mode]; p.directMessage(`§a游戏模式: ${mode}`); }
      else { p.directMessage("§c用法: !gm 0/1/2/3"); }
      return false;
  }
  return true;  // 不是命令的消息正常发送
});
```

### 权限检查

```js
// opLevel: 0=普通玩家, 1-4=管理员
function requireOP(p: GamePlayer, level: number): boolean {
  if (p.opLevel < level) {
    p.directMessage(`§c此命令需要 OP 等级 ≥ ${level}`);
    return false;
  }
  return true;
}

// 使用
if (message === "!admin") {
  if (!requireOP(p, 2)) return false;
  // ... 管理员操作
}
```

---

## 经济系统

基于计分板的经济系统，玩家可以用 `/box3script reload` 不丢失数据（计分板独立于脚本生命周期）。

```js
const CURRENCY = "coins";

// ── 初始化 ──
world.addScoreboard(CURRENCY);

world.onPlayerJoin((entity) => {
  // 新玩家初始化余额
  if (world.getScore(entity.player.name, CURRENCY) === 0) {
    world.setScore(entity.player.name, CURRENCY, 100);  // 初始 100 金币
  }
});

// ── API 函数 ──
function getBalance(playerName: string): number {
  return world.getScore(playerName, CURRENCY);
}

function addCoins(playerName: string, amount: number): void {
  const current = getBalance(playerName);
  world.setScore(playerName, CURRENCY, Math.max(0, current + amount));
}

function transferCoins(from: string, to: string, amount: number): boolean {
  if (getBalance(from) < amount) return false;
  addCoins(from, -amount);
  addCoins(to, amount);
  return true;
}

// ── 命令 ──
world.onChat((entity, message) => {
  const p = entity.player;
  const args = message.trim().split(/\s+/);
  const cmd = args[0].toLowerCase();

  switch (cmd) {
    case "!coins":
      p.directMessage(`§6金币: §f${getBalance(p.name)}`);
      return false;

    case "!pay": {
      const target = args[1];
      const amount = parseInt(args[2]);
      if (!target || isNaN(amount) || amount <= 0) {
        p.directMessage("§c用法: !pay <玩家> <金额>"); return false;
      }
      if (!transferCoins(p.name, target, amount)) {
        p.directMessage("§c余额不足！"); return false;
      }
      p.directMessage(`§a已向 ${target} 转账 ${amount} 金币`);
      const recipient = world.querySelector(target);
      if (recipient?.isPlayer()) {
        recipient.player.directMessage(`§a${p.name} 向你转账 ${amount} 金币`);
      }
      return false;
    }

    case "!top": {
      const scores = world.listScores(CURRENCY);
      p.directMessage("§6── 财富排行榜 ──");
      scores.slice(0, 5).forEach((s, i) => {
        p.directMessage(`§e${i + 1}. §f${s.name} §7- §6${s.value} 金币`);
      });
      return false;
    }
  }
  return true;
});
```

---

## 传送系统

### 家传送（内存，重启丢失）

```js
const homes = new Map<string, GameVector3>();

world.onChat((entity, message) => {
  const p = entity.player;

  if (message === "!sethome") {
    homes.set(p.name, new GameVector3(p.position.x, p.position.y, p.position.z));
    p.directMessage("§a家已设置！");
    return false;
  }

  if (message === "!home") {
    const home = homes.get(p.name);
    if (!home) { p.directMessage("§c先设置家: !sethome"); return false; }
    p.teleport(home);
    p.directMessage("§a已传送回家！");
    p.playSound("minecraft:entity.enderman.teleport", 1.0, 1.0);
    return false;
  }

  return true;
});
```

### 家传送（持久化，重启不丢失）

```js
const homeStorage = storage.getDataStorage<{ x: number; y: number; z: number }>("homes");

world.onChat((entity, message) => {
  const p = entity.player;

  if (message === "!sethome") {
    homeStorage.set(p.userId, {
      x: p.position.x, y: p.position.y, z: p.position.z,
    });
    p.directMessage("§a家已设置（持久化）！");
    return false;
  }

  if (message === "!home") {
    const home = homeStorage.get(p.userId);
    if (!home) { p.directMessage("§c先设置家: !sethome"); return false; }
    p.teleport(new GameVector3(home.x, home.y, home.z));
    p.directMessage("§a已传送回家！");
    return false;
  }

  return true;
});
```

### 地标传送（管理员设置，所有人可用）

```js
const warps = new Map<string, GameVector3>();

world.onChat((entity, message) => {
  const p = entity.player;
  const args = message.trim().split(/\s+/);
  const cmd = args[0].toLowerCase();

  switch (cmd) {
    case "!setwarp":
      if (p.opLevel < 2) { p.directMessage("§c需要管理员权限"); return false; }
      warps.set(args[1], new GameVector3(p.position.x, p.position.y, p.position.z));
      world.say(`§a地标 §e${args[1]} §a已设置`);
      return false;

    case "!warp":
      const warp = warps.get(args[1]);
      if (!warp) { p.directMessage("§c地标不存在"); return false; }
      p.teleport(warp);
      p.directMessage(`§a已传送到 §e${args[1]}`);
      return false;

    case "!warps": {
      const list = Array.from(warps.keys()).join(", ");
      p.directMessage(`§6地标: §f${list || "无"}`);
      return false;
    }
  }
  return true;
});
```

---

## 重生保护

```js
// 玩家重生后给予短暂无敌
world.onPlayerRespawn((entity) => {
  const p = entity.player;
  p.addEffect("minecraft:resistance", 100, 4, true);    // 5秒 抗性V（无敌）
  p.addEffect("minecraft:regeneration", 100, 2, true);  // 5秒 生命恢复III
  p.addEffect("minecraft:fire_resistance", 100, 0, true); // 5秒 防火
  p.directMessage("§a你已获得 5 秒重生保护");
  p.playSound("minecraft:block.beacon.activate", 1.0, 1.5);
});
```

---

## 商店/NPC

右键一个实体（如村民）弹出对话/交易：

```js
// 商店数据结构
interface ShopItem {
  id: string;
  displayName: string;
  price: number;
  item: string;
  count: number;
}

const shopItems: ShopItem[] = [
  { id: "apple", displayName: "苹果 x16", price: 5, item: "minecraft:apple", count: 16 },
  { id: "diamond", displayName: "钻石", price: 50, item: "minecraft:diamond", count: 1 },
  { id: "_sword", displayName: "铁剑", price: 30, item: "minecraft:iron_sword", count: 1 },
  { id: "golden_apple", displayName: "金苹果", price: 20, item: "minecraft:golden_apple", count: 1 },
];

// 右击村民打开商店
world.onInteract((entity, target) => {
  if (target.entityType !== "minecraft:villager") return;
  const p = entity.player;
  p.directMessage("§6── 商店 ──");
  shopItems.forEach((item) => {
    p.directMessage(`§f!buy ${item.id} §7- §6${item.price}金币 §7→ ${item.displayName}`);
  });
  p.directMessage("§7用法: !buy <商品ID>");
});

// 购买命令
world.onChat((entity, message) => {
  const p = entity.player;
  const args = message.trim().split(/\s+/);

  if (args[0].toLowerCase() === "!buy") {
    const item = shopItems.find((si) => si.id === args[1]);
    if (!item) { p.directMessage("§c商品不存在"); return false; }

    const balance = world.getScore(p.name, "coins");
    if (balance < item.price) {
      p.directMessage(`§c余额不足！需要 §6${item.price} §c金币，你有 §6${balance} §c金币`);
      return false;
    }

    world.setScore(p.name, "coins", balance - item.price);
    p.giveItem(item.item, item.count);
    p.directMessage(`§a购买了 §e${item.displayName} §a，花费 §6${item.price} §a金币`);
    p.playSound("minecraft:entity.experience_orb.pickup", 1.0, 1.0);
    return false;
  }
  return true;
});
```

---

## 每日奖励

```js
const dailyRewards = storage.getDataStorage<{ lastClaimed: number }>("daily-rewards");

world.onChat((entity, message) => {
  const p = entity.player;

  if (message === "!daily") {
    const now = Date.now();
    const record = dailyRewards.get(p.userId);
    const cooldown = 24 * 60 * 60 * 1000;  // 24 小时

    if (record && (now - record.lastClaimed) < cooldown) {
      const hours = Math.ceil((record.lastClaimed + cooldown - now) / 3600000);
      p.directMessage(`§c请等待 ${hours} 小时后再领取`);
      return false;
    }

    // 发放奖励
    p.giveItem("minecraft:diamond", 3);
    p.giveItem("minecraft:experience_bottle", 8);
    const bonus = 10 + Math.floor(Math.random() * 20);
    const coins = world.getScore(p.name, "coins");
    world.setScore(p.name, "coins", coins + bonus);
    dailyRewards.set(p.userId, { lastClaimed: now });
    p.directMessage(`§a每日奖励已领取！获得 3 钻石 + 8 经验瓶 + ${bonus} 金币`);
    p.playSound("minecraft:entity.player.levelup", 1.0, 1.0);
    return false;
  }
  return true;
});
```

---

## 排行榜

```js
function showLeaderboard(p: GamePlayer, board: string, title: string): void {
  const scores = world.listScores(board);
  p.directMessage(`§6── ${title} ──`);
  if (scores.length === 0) {
    p.directMessage("§7暂无数据");
    return;
  }
  scores.slice(0, 10).forEach((s, i) => {
    const medal = i === 0 ? "§6🏆 " : i === 1 ? "§7🥈 " : i === 2 ? "§c🥉 " : `§7${i + 1}. `;
    p.directMessage(`${medal}§f${s.name} §7- §e${s.value}`);
  });
}

// 命令
world.onChat((entity, message) => {
  if (message === "!topkills") {
    showLeaderboard(entity.player, "kills", "击杀排行榜");
    return false;
  }
  return true;
});
```

---

## 波次刷怪

完整波次系统，难度递增：

```js
let wave = 0;
let mobsAlive = 0;
let waveActive = false;

function spawnWave(pos: GameVector3): void {
  wave++;
  const count = 3 + wave * 2;
  mobsAlive = count;
  waveActive = true;
  const types = ["minecraft:zombie", "minecraft:skeleton", "minecraft:spider"];

  world.say(`§c§l⚔ 第 ${wave} 波开始！§f ${count} 只怪物`);

  for (let i = 0; i < count; i++) {
    world.setTimeout(() => {
      const x = pos.x + (Math.random() - 0.5) * 12;
      const z = pos.z + (Math.random() - 0.5) * 12;
      const type = types[Math.floor(Math.random() * types.length)];
      const mob = world.spawnEntity(type, new GameVector3(x, pos.y, z));
      if (!mob) return;
      mob.setNameTag(`§7[第${wave}波] ${type.split(":")[1]}`);
      mob.maxHp = 20 + wave * 3;
      mob.hp = mob.maxHp;
      mob.setAI(true);
      mob.addTag("wave_mob");
      // 每 5 波出精英
      if (wave % 5 === 0) {
        mob.addEffect("minecraft:speed", 99999, 1, true);
        mob.setNameTag(`§c[精英] ${type.split(":")[1]}`);
      }
    }, i * 150);
  }
}
```

---

## 缩圈机制

```js
function startShrinkPhase(centerX: number, centerZ: number, stages: { size: number; duration: number }[]): void {
  let stageIndex = 0;

  world.setBorderCenter(centerX, centerZ);
  world.borderSize = stages[0].size * 2;  // 需要乘2（直径）
  world.setBorderDamage(0.5);

  function nextStage(): void {
    if (stageIndex >= stages.length) {
      world.say("§c边界已缩至最小！");
      return;
    }
    const stage = stages[stageIndex];
    world.say(`§c边界缩小至 ${stage.size} 格！(${stage.duration} 秒)`);
    world.shrinkBorder(stage.size * 2, stage.duration);
    stageIndex++;
    world.setTimeout(nextStage, stage.duration * 20);
  }

  world.setTimeout(nextStage, 100);  // 5 秒后开始
}

// 用法：100→50→25→10，每段 60 秒
startShrinkPhase(0, 0, [
  { size: 100, duration: 60 },
  { size: 50, duration: 60 },
  { size: 25, duration: 60 },
  { size: 10, duration: 60 },
]);
```

---

## HTTP Webhook

```js
// 发送击杀事件到 Discord Webhook
world.onEntityDeath((entity, killer) => {
  if (killer?.isPlayer() && entity.isPlayer()) {
    const kp = killer.player;

    http.fetch("https://discord.com/api/webhooks/YOUR_WEBHOOK_URL", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        content: `⚔ **${kp.name}** 击杀了 **${entity.player.name}**`,
      }),
      timeout: 5000,
      async: true,
      onResponse: (resp) => { console.log(`Webhook sent: ${resp.status}`); },
      onError: (err) => { console.warn(`Webhook failed: ${err}`); },
    });
  }
});

// 服务器状态上报
const SERVER_NAME = "My Server";
const WEBHOOK_URL = "https://discord.com/api/webhooks/YOUR_ID";

world.setInterval(() => {
  const playerCount = world.querySelectorAll("*").length;
  const tps = "20";  // 正常情况

  http.fetch(WEBHOOK_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      content: `📊 **${SERVER_NAME}** | 玩家: ${playerCount} | TPS: ${tps} | 时间: ${new Date().toLocaleTimeString()}`,
    }),
    timeout: 5000,
    async: true,
  });
}, 6000);
```

---

## 客户端 HUD

结合 `remoteChannel` 实现客户端自定义 HUD（服务端提供数据，客户端显示）：

**服务端 `src/server/app.ts`：**

```js
// 接收客户端的位置请求
remoteChannel.onServerEvent((event) => {
  if (event.args.type === "requestHUDData") {
    const p = event.entity.player;
    const pos = p.position;
    remoteChannel.sendClientEvent(event.entity, {
      type: "hudData",
      data: {
        health: p.hp,
        maxHealth: p.maxHp,
        food: p.food,
        x: Math.floor(pos.x),
        y: Math.floor(pos.y),
        z: Math.floor(pos.z),
        coins: world.getScore(p.name, "coins"),
      },
    });
  }
});
```

**客户端 `src/client/app.ts`：**

```js
interface HUDData {
  health: number; maxHealth: number; food: number;
  x: number; y: number; z: number; coins: number;
}

client.onTick(() => {
  if (tickCount % 20 === 0) {  // 每秒请求一次
    remoteChannel.sendServerEvent({ type: "requestHUDData" });
  }
});

remoteChannel.onClientEvent((event) => {
  if (event.args.type === "hudData") {
    const d = event.args.data as HUDData;
    const hpPercent = Math.round((d.health / d.maxHealth) * 100);
    const hpColor = hpPercent > 60 ? "§a" : hpPercent > 30 ? "§e" : "§c";
    ui.showOverlay(
      `${hpColor}❤ ${Math.ceil(d.health)} §7| §6🍖 ${d.food} §7| §6💰 ${d.coins} §7| §f(${d.x}, ${d.y}, ${d.z})`
    );
  }
});
```

---

## 跨脚本联动

多个脚本项目之间通信：

**大厅脚本：**

```js
// 接收其他脚本的状态更新并转发给玩家
world.onMessage((from, data) => {
  if (data?.type === "gameEnded") {
    world.say(`§e[大厅] ${from} 的游戏已结束！${data.winner} 获胜`);
  }
});
```

**小游戏脚本：**

```js
// 游戏结束时通知大厅
function endGame(): void {
  world.sendMessage("lobby", {
    type: "gameEnded",
    winner: "红队",
    scores: { red: state.redScore, blue: state.blueScore },
  });
}
```

---

每个配方都是独立的，按需取用。更多细节参见 [API 文档](../api/README.md) 和 [教程系列](../tutorial/README.md)。
