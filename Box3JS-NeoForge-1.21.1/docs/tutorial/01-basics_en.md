# Tutorial 1: 5-Minute Quick Start

Get from zero to your first running Box3JS script — no Minecraft modding experience needed, just JavaScript knowledge.

## Prerequisites

- Box3JS mod installed on the server
- Basic JavaScript/TypeScript syntax

## Step 1: Create a Project

Run one command in-game:

```
/box3script create hello
```

This generates a complete TypeScript project under `config/box3/script/hello/`. The file `src/app.ts` is where you write your code.

## Step 2: Build

Open a terminal and enter the project directory:

```bash
cd config/box3/script/hello
npm install && npm run build
```

`npm install` only needs to run once. After that, just `npm run build` after each code change.

## Step 3: Write Your First Script

Open `src/app.ts`, clear the contents, and write:

```js
console.log("Hello, Box3JS!");

world.onPlayerJoin((entity) => {
  entity.player.directMessage("§aWelcome to the server!");
});
```

**That's it** — no imports, no initialization. `world` and `console` are globals provided by the mod.

## Step 4: Start

Back in-game:

```
/box3script start hello
```

Now when a player joins, they'll receive "§aWelcome to the server!" in green. The server console will output `[Box3JS] [hello] Hello, Box3JS!`.

## Step 5: Edit + Hot Reload

Try changing the welcome message to:

```js
entity.player.directMessage("§6Hello, " + entity.player.name + "!");
```

Save, run `npm run build`, then in-game:

```
/box3script reload hello
```

No server restart required — changes take effect immediately.

---

These 5 steps form the complete dev cycle: **edit code → build → reload**. The rest of this tutorial dives into everything you can build.

## Message System

Before writing chat commands, you need to know how to send messages to players.

### Four Message Types

```js
// 1. Server broadcast — chat, everyone sees it
world.say("Attention everyone!");

// 2. Private message — chat, only the target player sees it
player.directMessage("Only you can see this message");

// 3. Action bar — small text above the hotbar
player.actionBar("Tip above the hotbar");

// 4. Screen title — large text in the center of the screen
player.title("§6§lMain Title", "§7Subtitle");
// With timing: (title, subtitle, fadeInTicks, stayTicks, fadeOutTicks)
player.title("§c§lBOSS", "Ancient Dragon", 10, 60, 10);
```

| Method | Location | Visibility |
|--------|----------|------------|
| `world.say()` | Chat | Server-wide |
| `player.directMessage()` | Chat | Single player |
| `player.actionBar()` | Above hotbar | Single player |
| `player.title()` | Screen center | Single player |

### console Logging

`console` outputs to the server console in the format `[Box3JS] [projectName] message`:

```js
console.log("Info");     // [Box3JS] [hello] Info
console.debug("Debug");  // [Box3JS] [hello] [DEBUG] Debug
console.warn("Warning"); // [Box3JS] [hello] [WARN] Warning
console.error("Error");  // [Box3JS] [hello] [ERROR] Error
```

## Chat Command System

Use `world.onChat` to intercept chat messages and implement custom commands:

```js
world.onChat((entity, message) => {
  const p = entity.player;

  switch (message) {
    case "!help":
      p.directMessage("§6── Commands ──");
      p.directMessage("§f!hello  §7- Greet");
      p.directMessage("§f!time   §7- Check time");
      p.directMessage("§f!pos    §7- Check position");
      p.directMessage("§f!day    §7- Set to daytime");
      p.directMessage("§f!clear  §7- Clear weather");
      return false;  // ★ Return false to suppress the chat message

    case "!hello":
      p.directMessage(`§eHello, ${p.name}!`);
      return false;

    case "!time":
      p.directMessage(`§eCurrent game time: §f${world.time}`);
      return false;

    case "!pos": {
      const pos = p.position;
      p.directMessage(
        `§eYour position: §f${Math.floor(pos.x)}, ${Math.floor(pos.y)}, ${Math.floor(pos.z)}`
      );
      return false;
    }

    case "!day":
      world.time = 1000;
      world.say(`§e${p.name} §fset the time to day`);
      return false;

    case "!clear":
      world.clearWeather();
      world.say(`§e${p.name} §fcleared the weather`);
      return false;
  }
  return true;  // Non-command messages pass through normally
});
```

**Key rule:** Returning `false` from the callback suppresses the chat message. Return `true` to let it through.

## Welcome Message with Effects

Plain text is boring. Add some visual flair:

```js
world.onPlayerJoin((entity) => {
  const p = entity.player;

  // Screen title
  p.title("§6§lWelcome!", "§7Type §f!help §7for commands", 5, 70, 10);

  // Particle circle + sound
  const pos = p.position;
  world.spawnParticleCircle(pos.x, pos.y, pos.z, 1.5, "minecraft:happy_villager", 15);
  world.playSound("minecraft:block.note_block.pling", pos, 1.0, 1.5);
});
```

Effect: when a player joins, they see a screen title, hear a bell chime, and green particles circle around them.

## Timers

```js
// Broadcast player count every 5 minutes
world.setInterval(() => {
  const count = world.querySelectorAll("*").length;
  if (count > 0) world.say(`§7Online: §f${count} §7players`);
}, 6000);  // 6000 ticks = 5 minutes

// Run once after 30 seconds
world.setTimeout(() => {
  world.say("§6Server has been running for 30 seconds");
}, 600);  // 600 ticks = 30 seconds
```

**Tick conversion:** 20 ticks = 1 second

| Duration | Ticks |
|----------|-------|
| 1 second | 20 |
| 5 seconds | 100 |
| 30 seconds | 600 |
| 1 minute | 1200 |
| 5 minutes | 6000 |

## World Properties

```js
// Time
world.time = 6000;      // Noon (0=sunrise, 6000=noon, 12000=sunset, 18000=midnight)

// Weather
world.rainDensity = 1.0;    // Full rain
world.thunderDensity = 0.5; // Thunderstorm
world.clearWeather();        // Clear skies

// Difficulty
world.difficulty = "hard";  // peaceful / easy / normal / hard

// Game rules
world.setGameRule("keepInventory", true);  // Keep inventory on death
world.setGameRule("doFireTick", false);    // Fire doesn't spread
world.setGameRule("doMobSpawning", false); // Disable mob spawning
```

## Complete Example

Putting everything together in one script:

```js
// ═══════════════════════════════════
//  Hello — Box3JS Starter Script
// ═══════════════════════════════════

console.log("[Hello] Script loaded");

// ── Welcome effects ──
world.onPlayerJoin((entity) => {
  const p = entity.player;
  p.title("§6§lWelcome to the server!", "§7Type §f!help §7for commands", 5, 70, 10);
  const pos = p.position;
  world.spawnParticleCircle(pos.x, pos.y, pos.z, 1.5, "minecraft:happy_villager", 15);
  world.playSound("minecraft:block.note_block.pling", pos, 1.0, 1.5);
});

// ── Periodic announcement ──
world.setInterval(() => {
  const count = world.querySelectorAll("*").length;
  if (count > 0) world.say(`§7Online: §f${count} §7players`);
}, 6000);

// ── Chat commands ──
world.onChat((entity, message) => {
  const p = entity.player;
  switch (message) {
    case "!help":
      p.directMessage("§6Commands: §f!hello !time !pos !online !day !clear");
      return false;
    case "!hello":
      p.directMessage(`§eHello, ${p.name}!`);
      return false;
    case "!time":
      p.directMessage(`§eTime: §f${world.time}`);
      return false;
    case "!pos": {
      const pos = p.position;
      p.directMessage(`§ePosition: §f${Math.floor(pos.x)} ${Math.floor(pos.y)} ${Math.floor(pos.z)}`);
      return false;
    }
    case "!online":
      p.directMessage(`§eOnline: §f${world.querySelectorAll("*").length}`);
      return false;
    case "!day":
      world.time = 1000;
      world.say(`§e${p.name} §fset the time to day`);
      return false;
    case "!clear":
      world.clearWeather();
      world.say(`§e${p.name} §fcleared the weather`);
      return false;
  }
  return true;
});
```

## Tips

### Dev Cycle

```
Edit code → npm run build → /box3script reload hello → Test
```

Enable file watching for auto hot-reload (no manual reload needed):

```
/box3script watch
```

### Sandbox Mode (Safe Testing)

With sandbox enabled, all world modifications by the script are tracked and can be rolled back with one command:

```
/box3script sandbox hello    # Enable
# ... test your script ...
/box3script sandbox hello    # Disable → all changes rolled back
```

### Debugging

When something goes wrong, check in this order:
1. Check the server console for errors (`console.log` output appears here)
2. Verify the script is loaded: run `/box3script` and check if the project shows `◉` (loaded and running)
3. Verify the build succeeded: `npm run build` should complete without errors
4. If syntax is fine but logic isn't working, check that event callbacks are registered correctly

## Next Step

[Tutorial 2: Players & Items](../tutorial/02-player-items_en.md) — teleport, items, enchantments, potion effects, game modes.
