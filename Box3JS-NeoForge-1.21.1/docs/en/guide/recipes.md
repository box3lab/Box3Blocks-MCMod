---
---

# Common Recipes

"Want to implement X? Copy this template and tweak." All code is build-verified.

## Chat Commands

### Basic Command Router

```js
world.onChat((entity, message) => {
  const p = entity.player;
  const args = message.trim().split(/\s+/);
  const cmd = args[0].toLowerCase();

  switch (cmd) {
    case "!heal":
      p.hp = p.maxHp;
      p.food = 20;
      p.directMessage("§aHealed!");
      return false;

    case "!fly":
      p.canFly = !p.canFly;
      p.flying = p.canFly;
      p.directMessage(p.canFly ? "§aFlight: ON" : "§7Flight: OFF");
      return false;

    case "!gm":
      if (p.opLevel < 2) { p.directMessage("§cInsufficient permission"); return false; }
      const mode = args[1];
      const modes: Record<string, string> = { "0": "survival", "1": "creative", "2": "adventure", "3": "spectator" };
      if (modes[mode]) { p.gameMode = modes[mode]; p.directMessage(`§aGame mode: ${mode}`); }
      else { p.directMessage("§cUsage: !gm 0/1/2/3"); }
      return false;
  }
  return true;  // Non-command messages pass through
});
```

### Permission Check

```js
// opLevel: 0=normal, 1-4=admin
function requireOP(p: GamePlayer, level: number): boolean {
  if (p.opLevel < level) {
    p.directMessage(`§cThis command requires OP level ≥ ${level}`);
    return false;
  }
  return true;
}

// Usage
if (message === "!admin") {
  if (!requireOP(p, 2)) return false;
  // ... admin operations
}
```

## Economy System

Scoreboard-based economy. Data persists through `/box3script reload` (scoreboards are independent of script lifecycle).

```js
const CURRENCY = "coins";

// ── Initialization ──
world.addScoreboard(CURRENCY);

world.onPlayerJoin((entity) => {
  // Initialize new players with starting balance
  if (world.getScore(entity.player.name, CURRENCY) === 0) {
    world.setScore(entity.player.name, CURRENCY, 100);  // Start with 100 coins
  }
});

// ── API functions ──
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

// ── Commands ──
world.onChat((entity, message) => {
  const p = entity.player;
  const args = message.trim().split(/\s+/);
  const cmd = args[0].toLowerCase();

  switch (cmd) {
    case "!coins":
      p.directMessage(`§6Coins: §f${getBalance(p.name)}`);
      return false;

    case "!pay": {
      const target = args[1];
      const amount = parseInt(args[2]);
      if (!target || isNaN(amount) || amount <= 0) {
        p.directMessage("§cUsage: !pay <player> <amount>"); return false;
      }
      if (!transferCoins(p.name, target, amount)) {
        p.directMessage("§cInsufficient balance!"); return false;
      }
      p.directMessage(`§aSent ${amount} coins to ${target}`);
      const recipient = world.querySelector(target);
      if (recipient?.isPlayer()) {
        recipient.player.directMessage(`§a${p.name} sent you ${amount} coins`);
      }
      return false;
    }

    case "!top": {
      const scores = world.listScores(CURRENCY);
      p.directMessage("§6── Wealth Leaderboard ──");
      scores.slice(0, 5).forEach((s, i) => {
        p.directMessage(`§e${i + 1}. §f${s.name} §7- §6${s.value} coins`);
      });
      return false;
    }
  }
  return true;
});
```

## Teleport System

### Home TP (in-memory, lost on restart)

```js
const homes = new Map<string, GameVector3>();

world.onChat((entity, message) => {
  const p = entity.player;

  if (message === "!sethome") {
    homes.set(p.name, new GameVector3(p.position.x, p.position.y, p.position.z));
    p.directMessage("§aHome set!");
    return false;
  }

  if (message === "!home") {
    const home = homes.get(p.name);
    if (!home) { p.directMessage("§cSet home first: !sethome"); return false; }
    p.teleport(home);
    p.directMessage("§aTeleported home!");
    p.playSound("minecraft:entity.enderman.teleport", 1.0, 1.0);
    return false;
  }

  return true;
});
```

### Home TP (persistent, survives restarts)

```js
const homeStorage = storage.getDataStorage<{ x: number; y: number; z: number }>("homes");

world.onChat((entity, message) => {
  const p = entity.player;

  if (message === "!sethome") {
    homeStorage.set(p.userId, {
      x: p.position.x, y: p.position.y, z: p.position.z,
    });
    p.directMessage("§aHome set (persistent)!");
    return false;
  }

  if (message === "!home") {
    const home = homeStorage.get(p.userId);
    if (!home) { p.directMessage("§cSet home first: !sethome"); return false; }
    p.teleport(new GameVector3(home.x, home.y, home.z));
    p.directMessage("§aTeleported home!");
    return false;
  }

  return true;
});
```

### Warp Points (admin sets, everyone uses)

```js
const warps = new Map<string, GameVector3>();

world.onChat((entity, message) => {
  const p = entity.player;
  const args = message.trim().split(/\s+/);
  const cmd = args[0].toLowerCase();

  switch (cmd) {
    case "!setwarp":
      if (p.opLevel < 2) { p.directMessage("§cAdmin only"); return false; }
      warps.set(args[1], new GameVector3(p.position.x, p.position.y, p.position.z));
      world.say(`§aWarp §e${args[1]} §aset`);
      return false;

    case "!warp":
      const warp = warps.get(args[1]);
      if (!warp) { p.directMessage("§cWarp not found"); return false; }
      p.teleport(warp);
      p.directMessage(`§aTeleported to §e${args[1]}`);
      return false;

    case "!warps": {
      const list = Array.from(warps.keys()).join(", ");
      p.directMessage(`§6Warps: §f${list || "none"}`);
      return false;
    }
  }
  return true;
});
```

## Respawn Protection

```js
// Give brief invulnerability after respawn
world.onPlayerRespawn((entity) => {
  const p = entity.player;
  p.addEffect("minecraft:resistance", 100, 4, true); // 5s Resistance V (near-invulnerable)
  p.addEffect("minecraft:regeneration", 100, 2, true); // 5s Regen III
  p.addEffect("minecraft:fire_resistance", 100, 0, true); // 5s Fire Resistance
  p.directMessage("§a5 seconds of respawn protection");
  p.playSound("minecraft:block.beacon.activate", 1.0, 1.5);
});
```

## Shop / NPC

Right-click an entity (e.g. villager) to open a dialog/shop:

```js
// Shop data structure
interface ShopItem {
  id: string;
  displayName: string;
  price: number;
  item: string;
  count: number;
}

const shopItems: ShopItem[] = [
  { id: "apple", displayName: "Apple x16", price: 5, item: "minecraft:apple", count: 16 },
  { id: "diamond", displayName: "Diamond", price: 50, item: "minecraft:diamond", count: 1 },
  { id: "_sword", displayName: "Iron Sword", price: 30, item: "minecraft:iron_sword", count: 1 },
  { id: "golden_apple", displayName: "Golden Apple", price: 20, item: "minecraft:golden_apple", count: 1 },
];

// Right-click villager to open shop
world.onInteract((entity, target) => {
  if (target.entityType !== "minecraft:villager") return;
  const p = entity.player;
  p.directMessage("§6── Shop ──");
  shopItems.forEach((item) => {
    p.directMessage(`§f!buy ${item.id} §7- §6${item.price} coins §7→ ${item.displayName}`);
  });
  p.directMessage("§7Usage: !buy <itemId>");
});

// Buy command
world.onChat((entity, message) => {
  const p = entity.player;
  const args = message.trim().split(/\s+/);

  if (args[0].toLowerCase() === "!buy") {
    const item = shopItems.find((si) => si.id === args[1]);
    if (!item) { p.directMessage("§cItem not found"); return false; }

    const balance = world.getScore(p.name, "coins");
    if (balance < item.price) {
      p.directMessage(`§cInsufficient funds! Need §6${item.price} §c, have §6${balance}`);
      return false;
    }

    world.setScore(p.name, "coins", balance - item.price);
    p.giveItem(item.item, item.count);
    p.directMessage(`§aBought §e${item.displayName} §afor §6${item.price} §acoins`);
    p.playSound("minecraft:entity.experience_orb.pickup", 1.0, 1.0);
    return false;
  }
  return true;
});
```

## Daily Rewards

```js
const dailyRewards =
  storage.getDataStorage < { lastClaimed: number } > "daily-rewards";

world.onChat((entity, message) => {
  const p = entity.player;

  if (message === "!daily") {
    const now = Date.now();
    const record = dailyRewards.get(p.userId);
    const cooldown = 24 * 60 * 60 * 1000; // 24 hours

    if (record && now - record.lastClaimed < cooldown) {
      const hours = Math.ceil((record.lastClaimed + cooldown - now) / 3600000);
      p.directMessage(`§cPlease wait ${hours} hours before claiming again`);
      return false;
    }

    // Grant rewards
    p.giveItem("minecraft:diamond", 3);
    p.giveItem("minecraft:experience_bottle", 8);
    const bonus = 10 + Math.floor(Math.random() * 20);
    const coins = world.getScore(p.name, "coins");
    world.setScore(p.name, "coins", coins + bonus);
    dailyRewards.set(p.userId, { lastClaimed: now });
    p.directMessage(
      `§aDaily reward claimed! 3 diamonds + 8 XP bottles + ${bonus} coins`,
    );
    p.playSound("minecraft:entity.player.levelup", 1.0, 1.0);
    return false;
  }
  return true;
});
```

## Leaderboards

```js
function showLeaderboard(p: GamePlayer, board: string, title: string): void {
  const scores = world.listScores(board);
  p.directMessage(`§6── ${title} ──`);
  if (scores.length === 0) {
    p.directMessage("§7No data yet");
    return;
  }
  scores.slice(0, 10).forEach((s, i) => {
    const medal = i === 0 ? "§6🏆 " : i === 1 ? "§7🥈 " : i === 2 ? "§c🥉 " : `§7${i + 1}. `;
    p.directMessage(`${medal}§f${s.name} §7- §e${s.value}`);
  });
}

// Command
world.onChat((entity, message) => {
  if (message === "!topkills") {
    showLeaderboard(entity.player, "kills", "Kill Leaderboard");
    return false;
  }
  return true;
});
```

## Wave Spawning

Complete wave system with scaling difficulty:

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

  world.say(`§c§l⚔ Wave ${wave} begins! §f${count} mobs`);

  for (let i = 0; i < count; i++) {
    setTimeout(() => {
      const x = pos.x + (Math.random() - 0.5) * 12;
      const z = pos.z + (Math.random() - 0.5) * 12;
      const type = types[Math.floor(Math.random() * types.length)];
      const mob = world.spawnEntity(type, new GameVector3(x, pos.y, z));
      if (!mob) return;
      mob.setNameTag(`§7[Wave ${wave}] ${type.split(":")[1]}`);
      mob.maxHp = 20 + wave * 3;
      mob.hp = mob.maxHp;
      mob.setAI(true);
      mob.addTag("wave_mob");
      // Elites every 5 waves
      if (wave % 5 === 0) {
        mob.addEffect("minecraft:speed", 99999, 1, true);
        mob.setNameTag(`§c[Elite] ${type.split(":")[1]}`);
      }
    }, i * 150);
  }
}
```

## Shrinking Zone

```js
function startShrinkPhase(centerX: number, centerZ: number, stages: { size: number; duration: number }[]): void {
  let stageIndex = 0;

  world.setBorderCenter(centerX, centerZ);
  world.borderSize = stages[0].size * 2;  // Multiply by 2 (diameter)
  world.setBorderDamage(0.5);

  function nextStage(): void {
    if (stageIndex >= stages.length) {
      world.say("§cBorder at minimum size!");
      return;
    }
    const stage = stages[stageIndex];
    world.say(`§cBorder shrinking to ${stage.size} blocks! (${stage.duration}s)`);
    world.shrinkBorder(stage.size * 2, stage.duration);
    stageIndex++;
    setTimeout(nextStage, stage.duration * 20);
  }

  setTimeout(nextStage, 100);  // Start after 5 seconds
}

// Usage: 100→50→25→10, 60s per stage
startShrinkPhase(0, 0, [
  { size: 100, duration: 60 },
  { size: 50, duration: 60 },
  { size: 25, duration: 60 },
  { size: 10, duration: 60 },
]);
```

## HTTP Webhook

```js
// Send kill events to Discord Webhook
world.onEntityDeath((entity, killer) => {
  if (killer?.isPlayer() && entity.isPlayer()) {
    const kp = killer.player;

    http.fetch("https://discord.com/api/webhooks/YOUR_WEBHOOK_URL", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        content: `⚔ **${kp.name}** eliminated **${entity.player.name}**`,
      }),
      timeout: 5000,
      async: true,
      onResponse: (resp) => {
        console.log(`Webhook sent: ${resp.status}`);
      },
      onError: (err) => {
        console.warn(`Webhook failed: ${err}`);
      },
    });
  }
});

// Periodic server status report
const SERVER_NAME = "My Server";
const WEBHOOK_URL = "https://discord.com/api/webhooks/YOUR_ID";

setInterval(() => {
  const playerCount = world.querySelectorAll("*").length;

  http.fetch(WEBHOOK_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      content: `📊 **${SERVER_NAME}** | Players: ${playerCount} | Time: ${new Date().toLocaleTimeString()}`,
    }),
    timeout: 5000,
    async: true,
  });
}, 6000);
```

## Client HUD

Combine `remoteChannel` for a custom client-side HUD (server provides data, client displays it):

**Server `src/server/app.ts`:**

```js
// Respond to client's HUD data requests
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

**Client `src/client/app.ts`:**

```js
interface HUDData {
  health: number; maxHealth: number; food: number;
  x: number; y: number; z: number; coins: number;
}

client.onTick(() => {
  if (tickCount % 20 === 0) {  // Request once per second
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

## Cross-Script Integration

Multiple script projects communicating:

**Lobby script:**

```js
// Receive status updates from other scripts and forward to players
world.onMessage((from, data) => {
  if (data?.type === "gameEnded") {
    world.say(`§e[Lobby] Game on ${from} ended! ${data.winner} won`);
  }
});
```

**Minigame script:**

```js
// Notify the lobby when the game ends
function endGame(): void {
  world.sendMessage("lobby", {
    type: "gameEnded",
    winner: "Red Team",
    scores: { red: state.redScore, blue: state.blueScore },
  });
}
```

Each recipe is self-contained — grab what you need. See the [API reference](../api/README.md) and [tutorials](../tutorial/README.md) for more detail.
