# Tutorial 4: Advanced Game Systems

This tutorial covers scoreboards, BossBars, teams, world border, and cross-script communication.

## 4.1 Scoreboards

```js
// Create scoreboards
world.addScoreboard("kills");                    // dummy type (manual scoring)
world.addScoreboard("deaths", "deathCount");     // MC auto-tracks deaths

// Set scores
world.setScore("Steve", "kills", 5);
world.setScore(entity, "kills", 10);  // Can also use entity object

// Read scores
const kills = world.getScore("Steve", "kills");

// Display on screen sidebar
world.showScoreboard("sidebar", "kills");

// Display in tab list
world.showScoreboard("list", "deaths");

// List all scores
const scores = world.listScores("kills");
// [{name: "Steve", value: 5}, {name: "Alex", value: 3}, ...]

// Hide / remove
world.hideScoreboard("sidebar");
world.removeScoreboard("kills");
```

### Example: Playtime Leaderboard

```js
world.addScoreboard("playtime", "dummy");
world.showScoreboard("sidebar", "playtime");

// +1 every minute
setInterval(() => {
  world.querySelectorAll("*").forEach((entity) => {
    if (!entity.isPlayer()) { return; }
    const p = entity.player;
    const current = world.getScore(p.name, "playtime");
    world.setScore(p.name, "playtime", current + 1);
  });
}, 1200);

// Initialize on join
world.onPlayerJoin((entity, _tick) => {
  const p = entity.player;
  world.setScore(p.name, "playtime", 0);
  p.setPlayerListName(`§7[§f${p.name}§7]`);
});
```

### Example: Kill Counter

```js
world.addScoreboard("kills");
world.showScoreboard("sidebar", "kills");

world.onEntityDeath((entity, killer, _tick) => {
  if (killer?.isPlayer()) {
    const p = killer.player;
    const current = world.getScore(p.name, "kills");
    world.setScore(p.name, "kills", current + 1);
    p.actionBar(`§eKills: §f${current + 1}`);
  }
});
```

## 4.2 BossBar

A BossBar shows a progress bar with a title at the top of the screen, commonly used for boss fights or global countdowns.

```js
// Basic usage
world.showBossbar("my_bar", "§c§lBoss Name", 1.0, "red");

// Update
world.showBossbar("my_bar", "§c§lBoss Name §7[50%]", 0.5, "yellow");

// Remove
world.removeBossbar("my_bar");
```

Color options: `"blue"` `"green"` `"pink"` `"purple"` `"red"` `"white"` `"yellow"`

### Example: 30-Second Countdown

```js
let timeLeft = 30;
world.showBossbar("demo_timer", "§eCountdown Demo", 1.0, "green");

const timerId = setInterval(() => {
  timeLeft--;
  if (timeLeft <= 0) {
    world.removeBossbar("demo_timer");
    timerId.cancel();
    world.say("§c⏰ Time's up!");
    world.playSound("minecraft:block.note_block.pling", new GameVector3(0, 70, 0), 1.0, 0.5);
    return;
  }

  const progress = timeLeft / 30;
  let color = "red";
  if (progress > 0.5) { color = "green"; }
  else if (progress > 0.2) { color = "yellow"; }

  world.showBossbar("demo_timer", `§eCountdown: §f${timeLeft} §eseconds`, progress, color);

  if (timeLeft <= 5) {
    world.playSound("minecraft:block.note_block.pling", new GameVector3(0, 70, 0), 0.5, 2.0);
  }
}, 20);
```

Effect: a countdown bar appears at the top of the screen, a bell rings each second during the last 5 seconds, and the bar turns red when time runs out.

## 4.3 Team System

```js
// Create teams
world.createTeam("red", "red");
world.createTeam("blue", "blue");

// Player joins a team
world.joinTeam(entity, "red");
entity.player.directMessage("§cYou joined §lRed Team");
entity.player.setPlayerListName(`§c[Red] §f${entity.player.name}`);

// Get team
const team = world.getTeamOf(entity);  // "red" or null

// Leave team
world.leaveTeam(entity);

// Delete team
world.removeTeam("red");
```

### Example: Team Assignment + Particle Effects

```js
world.onChat((entity, message, _tick) => {
  const p = entity.player;

  if (message === "!team-red") {
    world.joinTeam(entity, "red");
    p.directMessage("§cYou joined §lRed Team");
    p.setPlayerListName(`§c[Red] §f${p.name}`);
    world.spawnParticle(
      "minecraft:redstone",
      p.position.x, p.position.y + 2, p.position.z,
      10, 0.3, 0.3, 0.3, 0.02
    );
    return false;
  }

  if (message === "!team-blue") {
    world.joinTeam(entity, "blue");
    p.directMessage("§9You joined §lBlue Team");
    p.setPlayerListName(`§9[Blue] §f${p.name}`);
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

## 4.4 World Border

The world border can create dynamic shrinking zones — perfect for PvP or survival gameplay.

```js
// Set border
world.setBorderCenter(0, 0);
world.borderSize = 500;
world.setBorderDamage(2);        // Damage per second outside border
world.setBorderWarning(10);      // Screen reddening warning distance

// Smooth shrink: from current size to 100 over 120 seconds
world.shrinkBorder(100, 120);

// Read current size
console.log(world.borderSize);
```

### Example: Shrinking Zone Announcement

```js
world.say("§c⚠ Border will start shrinking in 5 seconds!");
world.setBorderCenter(0, 0);
world.borderSize = 200;
world.setBorderDamage(1);
world.setBorderWarning(10);

setTimeout(() => {
  world.say("§cBorder shrinking to 50 blocks!");
  world.shrinkBorder(50, 60);
  world.playSound(
    "minecraft:entity.wither.spawn",
    new GameVector3(0, 70, 0), 0.5, 0.8
  );
}, 100);
```

## 4.5 Cross-Script Communication

Different script projects can communicate via `sendMessage` / `onMessage`.

Script A (sender):

```js
// Send to a specific project
world.sendMessage("minigame_hub", { action: "start", level: 2 });

// Broadcast to all projects
world.sendMessage("*", { action: "reload_config" });
```

Script B (receiver):

```js
world.onMessage((from: string, data: unknown) => {
  const msg = data as Record<string, unknown> | null;
  console.log(`Received message from ${from}:`, JSON.stringify(msg));

  if (msg?.action === "start") {
    startGame(Number(msg.level));
  } else if (msg?.action === "reload_config") {
    reloadConfig();
  }
});
```

## 4.6 Projectiles & Explosions

```js
// Projectile: (type, fromPos, targetPos, speed)
world.launchProjectile("minecraft:fireball", fromPos, targetPos, 2);

// Explosion: (x, y, z, power, causesFire)
world.explode(0, 100, 0, 4);         // Power 4, no fire
world.explode(0, 100, 0, 8, true);   // Power 8, causes fire
```

## 4.7 Mini-Game Design Pattern Summary

| System | Use Case | Key APIs |
|--------|----------|----------|
| Scoreboard | Kill count, points, leaderboard | `world.addScoreboard()` / `setScore()` / `showScoreboard()` |
| BossBar | Countdown, boss HP, global progress | `world.showBossbar()` / `removeBossbar()` |
| Teams | Team assignment, friendly markers, grouping | `world.createTeam()` / `joinTeam()` |
| World Border | Shrinking zone, storm circle | `world.borderSize` / `shrinkBorder()` |
| Projectiles | Boss skills, bullet patterns | `world.launchProjectile()` |
| Explosions | Destructive events, traps | `world.explode()` |
| Cross-script messaging | Inter-module communication | `world.sendMessage()` / `onMessage()` |

## Next Step

Tutorial 5 covers visual effects: particles, fireworks, lightning, sounds, and two complete mini-game examples.
