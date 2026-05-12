# Tutorial 2: Players & Items

This tutorial covers player properties, teleportation, giving items, potion effects, game modes, and more.

## 2.1 Teleport & Flight

```js
world.onChat((entity, message, _tick) => {
  const p = entity.player;

  // ── Random teleport ──
  if (message === "!tp") {
    p.teleport(new GameVector3(
      (Math.random() - 0.5) * 100, 80, (Math.random() - 0.5) * 100
    ));
    p.directMessage("§aRandomly teleported!");
    p.playSound("minecraft:entity.enderman.teleport", 1.0, 1.0);
    return false;
  }

  // ── Toggle flight ──
  if (message === "!fly") {
    p.canFly = !p.canFly;
    p.flying = p.canFly;
    p.directMessage(p.canFly ? "§aFlight: ON" : "§7Flight: OFF");
    p.playSound("minecraft:entity.experience_orb.pickup", 1.0, 1.0);
    return false;
  }
  return true;
});
```

### Movement Properties

```js
p.walkSpeed = 0.25;   // Walk speed (default ~0.1)
p.runSpeed = 0.26;    // Sprint speed
p.jumpPower = 0.6;    // Jump strength (default ~0.42)
p.swimSpeed = 0.3;    // Swim speed
p.flySpeed = 0.15;    // Flight speed
p.enableJump = false; // Disable jumping

// Teleport
p.teleport(new GameVector3(0, 100, 0));
```

## 2.2 Game Modes

```js
p.gameMode = "creative";  // Creative
p.gameMode = "survival";   // Survival
p.gameMode = "adventure";  // Adventure
p.gameMode = "spectator";  // Spectator
p.gameMode = 1;            // Also by number: 0=survival, 1=creative, 2=adventure, 3=spectator

// Cross-dimension teleport
p.dimension = "minecraft:the_nether";  // Nether
p.teleport(new GameVector3(0, 70, 0));
p.dimension = "minecraft:overworld";   // Overworld
p.dimension = "minecraft:the_end";     // The End
```

## 2.3 Potion Effects

```js
// Apply effect: (effectId, durationTicks, amplifier, hideParticles)
p.addEffect("minecraft:speed", 600, 1, true);          // 30s Speed II
p.addEffect("minecraft:jump_boost", 600, 1, true);     // 30s Jump Boost II
p.addEffect("minecraft:regeneration", 200, 1, true);   // 10s Regeneration II
p.addEffect("minecraft:resistance", 200, 0, true);     // 10s Resistance I
p.addEffect("minecraft:strength", 100, 1, true);       // 5s Strength II
p.addEffect("minecraft:glowing", 200, 0, false);       // 10s Glowing (particles visible)
p.addEffect("minecraft:invisibility", 200, 0, true);   // 10s Invisibility

// Clear all effects
p.clearEffects();
```

Example: type `!buffs` to get a full set of buffs:

```js
world.onChat((entity, message, _tick) => {
  const p = entity.player;

  if (message === "!buffs") {
    p.addEffect("minecraft:speed", 600, 1, true);
    p.addEffect("minecraft:jump_boost", 600, 1, true);
    p.addEffect("minecraft:regeneration", 200, 1, true);
    p.addEffect("minecraft:resistance", 200, 0, true);
    p.directMessage("§dBuffs applied! 30s Speed+Jump, 10s Regen+Resistance");
    p.playSound("minecraft:entity.witch.throw", 1.0, 1.2);
    return false;
  }
  return true;
});
```

## 2.4 Health & Hunger

```js
p.hp = 20;           // Current health (10 hearts)
p.maxHp = 40;        // Max health (20 hearts)
p.food = 20;         // Food level
p.saturation = 10;   // Saturation

// Full heal
p.hp = p.maxHp;
p.food = 20;
p.saturation = 10;
```

## 2.5 Giving Items

```js
// Basic items
p.giveItem("minecraft:diamond_sword", 1);
p.giveItem("minecraft:golden_apple", 8);
p.giveItem("minecraft:arrow", 64);

// Enchanted items
p.giveEnchantedItem("minecraft:diamond_sword", 1, {
  "minecraft:sharpness": 5,
  "minecraft:fire_aspect": 2,
  "minecraft:unbreaking": 3,
});

// Named items with custom name and lore
p.giveNamedItem("minecraft:netherite_sword", 1, "§c§lBlazing Blade", [
  "§7Bound: Flame Power",
  "§ePassive: Attacks inflict burning",
]);

p.giveNamedItem("minecraft:gold_ingot", 1, "§6§lVictory Medal", [
  "§7Proof of challenge completion",
  "§7§oOnly the worthy may hold it",
]);

// Get held item
const held = p.getHeldItem();
if (held.id !== "minecraft:air") {
  p.directMessage(`You are holding: ${held.id} x${held.count}`);
}

// Clear inventory (including armor and offhand)
p.clearInventory();
```

Example: type `!kit` to receive a full set of gear:

```js
world.onChat((entity, message, _tick) => {
  const p = entity.player;

  if (message === "!kit") {
    p.clearInventory();
    p.giveItem("minecraft:diamond_sword", 1);
    p.giveItem("minecraft:diamond_pickaxe", 1);
    p.giveItem("minecraft:golden_apple", 8);
    p.giveItem("minecraft:arrow", 64);
    p.giveItem("minecraft:bow", 1);
    p.giveEnchantedItem("minecraft:diamond_sword", 1, {
      "minecraft:sharpness": 5,
      "minecraft:fire_aspect": 2,
      "minecraft:unbreaking": 3,
    });
    p.directMessage("§aGear issued!");
    p.playSound("minecraft:entity.player.levelup", 1.0, 1.0);
    return false;
  }
  return true;
});
```

## 2.6 XP, Sounds, Titles

```js
// Experience
p.xp = 10;                    // Set to level 10
p.addExperienceLevels(5);     // Add 5 levels

// Play sound (only this player hears it)
p.playSound("minecraft:block.note_block.pling", 1.0, 1.5);
p.playSound("minecraft:entity.player.levelup", 1.0, 1.0);
p.playSound("minecraft:entity.ender_dragon.growl", 1.0, 0.8);

// Screen title
p.title("§c§lBOSS Incoming", "§7Ancient Dragon · HP 200/200", 10, 60, 10);
```

Common sounds:
- `minecraft:block.note_block.pling` — Bell chime
- `minecraft:entity.experience_orb.pickup` — XP orb
- `minecraft:entity.player.levelup` — Level up
- `minecraft:entity.ender_dragon.growl` — Dragon roar
- `minecraft:entity.witch.throw` — Potion throw

## 2.7 Kick & Admin

```js
p.kick("You have been removed from the game");

p.opLevel = 4;             // Maximum permission (equivalent to /op)
console.log(p.opLevel);    // 0=normal, 1-4=admin level

p.runCommand("say Hello everyone"); // Run command as the player
```

## 2.8 Complete Example: Starter Kit

```js
world.onPlayerJoin((entity, _tick) => {
  const p = entity.player;

  // Welcome title + particles
  p.title("§6§lWelcome to the server!", "§7Prepare for adventure", 10, 60, 10);
  const pos = p.position;
  world.spawnParticleCircle(pos.x, pos.y, pos.z, 1.5, "minecraft:happy_villager", 20);
  world.playSound("minecraft:entity.player.levelup", pos, 1.0, 1.0);

  // Starter kit
  p.giveItem("minecraft:stone_sword", 1);
  p.giveItem("minecraft:stone_pickaxe", 1);
  p.giveItem("minecraft:stone_axe", 1);
  p.giveItem("minecraft:stone_shovel", 1);
  p.giveItem("minecraft:bread", 32);
  p.giveItem("minecraft:torch", 16);

  // Named special item
  p.giveNamedItem("minecraft:shield", 1, "§b§lBeginner's Shield", [
    "§7A shield only true beginners can wield",
    "§7§oIt doesn't look very sturdy...",
  ]);

  p.directMessage("§aYou've received the starter kit! Type !help for commands");
});
```

## Next Step

Tutorial 3 covers the event system and entity manipulation: block interactions, entity spawning, death events, equipment, and AI.
