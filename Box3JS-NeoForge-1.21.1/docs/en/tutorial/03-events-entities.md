---
---

# Tutorial 3: Events & Entities

This tutorial dives into event callbacks, block interactions, entity spawning, AI, and combat events.

## 3.1 Event Callbacks Overview

All events are registered via `world.onXxx(handler)` and return a `GameEventHandlerToken`.

| Registration Method | Callback Parameters | When It Fires |
|---------------------|---------------------|---------------|
| `world.onTick(fn)` | `(info)` | Every tick (20/sec) |
| `world.onPlayerJoin(fn)` | `(entity, tick)` | Player joins |
| `world.onPlayerLeave(fn)` | `(entity, tick)` | Player leaves |
| `world.onChat(fn)` | `(entity, message, tick)` | Chat message |
| `world.onBlockActivate(fn)` | `(entity, x, y, z, voxel, tick)` | Right-click block |
| `world.onVoxelDestroy(fn)` | `(entity, x, y, z, voxel, tick)` | Block broken |
| `world.onBlockPlace(fn)` | `(entity, x, y, z, voxel, voxelId, tick)` | Block placed |
| `world.onInteract(fn)` | `(entity, target, tick)` | Right-click entity |
| `world.onEntityDeath(fn)` | `(entity, killer, tick)` | Entity dies |
| `world.onEntityDamage(fn)` | `(entity, amount, source, attacker, tick)` | Entity damaged |
| `world.onPlayerRespawn(fn)` | `(entity, tick)` | Player respawns |
| `world.onButtonPressed(fn)` | `(entity, button, tick)` | Button pressed |
| `world.onMessage(fn)` | `(from, data)` | Cross-script message |

### Token Operations

```js
const token = world.onTick((info) => {
  console.log(`Tick: ${info.tick}`);
});

token.cancel();       // Unsubscribe
token.active();       // Check if still active
```

## 3.2 Block Interaction Events

```js
// ── Right-click block detection ──
world.onBlockActivate((entity, x, y, z, voxel, _tick) => {
  if (voxel === "minecraft:chest") {
    const p = entity.player;
    p.actionBar(`§eOpened chest @ (${x}, ${y}, ${z})`);
  }
  if (voxel === "minecraft:crafting_table") {
    entity.player.playSound("minecraft:block.wood.place", 0.5, 1.0);
  }
});

// ── Block break logging ──
world.onVoxelDestroy((entity, x, y, z, voxel, _tick) => {
  if (voxel !== "minecraft:air" && voxel !== "minecraft:grass_block") {
    console.log(`[Demo] ${entity.player.name} broke ${voxel} @ (${x},${y},${z})`);
  }
});

// ── Block TNT placement ──
world.onBlockPlace((entity, x, y, z, voxel, _voxelId, _tick) => {
  if (voxel === "minecraft:tnt" && entity.player.opLevel < 2) {
    voxels.setVoxel(x, y, z, "minecraft:air");  // Replace with air
    entity.player.directMessage("§cTNT placement is forbidden!");
    entity.player.playSound("minecraft:block.note_block.bass", 1.0, 0.5);
  }
});
```

## 3.3 Entity Damage & Death

```js
// ── Death rewards + Boss effects ──
world.onEntityDeath((entity, killer, _tick) => {
  if (killer?.isPlayer()) {
    const p = killer.player;
    const pos = entity.position;

    // Kill particles
    world.spawnParticle(
      "minecraft:angry_villager",
      pos.x, pos.y + 1, pos.z,
      10, 0.3, 0.3, 0.3, 0.05
    );

    // Boss kill special reward
    if (entity.hasTag("boss")) {
      p.addExperienceLevels(5);
      world.dropItem(pos, "minecraft:diamond", 3);
      world.dropItem(pos, "minecraft:emerald", 5);
      world.say(
        `§6${p.name} §fdefeated §c${
          entity.nameTag || entity.entityType}§f!`
      );
      world.launchFirework(pos.x, pos.y + 2, pos.z, "gold", "large_ball");
    }
  }
});

// ── Damage indicator ──
world.onEntityDamage((entity, amount, _source, attacker, _tick) => {
  if (attacker?.isPlayer()) {
    attacker.player.actionBar(
      `§cDealt ${amount} damage → ${entity.nameTag || entity.entityType}`
    );
  }
});
```

## 3.4 Right-Click Entity

```js
world.onInteract((entity, target, _tick) => {
  const p = entity.player;

  if (target.entityType === "minecraft:villager") {
    p.directMessage("§eThis villager is busy and doesn't want to talk...");
    // Angry particles
    world.spawnParticle(
      "minecraft:angry_villager",
      target.position.x, target.position.y + 2, target.position.z,
      3, 0.2, 0.2, 0.2, 0
    );
  }
});
```

## 3.5 Entity Spawning & Configuration

```js
// ── Spawn an elite zombie ──
const boss = world.spawnEntity(
  "minecraft:zombie",
  new GameVector3(x, y, z)
);
if (!boss) return;  // spawnEntity may return null

boss.setNameTag("§c§lElite Zombie");
boss.maxHp = 100;
boss.hp = 100;
boss.addTag("boss");
boss.setAI(true);
boss.addEffect("minecraft:resistance", 99999, 0, true);
boss.addEffect("minecraft:speed", 99999, 1, true);

// Equipment
boss.setEquipment("mainhand", "minecraft:iron_sword");
boss.setEquipment("head", "minecraft:iron_helmet");
// Slots: mainhand / offhand / head / chest / legs / feet

boss.setDropChance("mainhand", 0.3);  // 30% drop chance for held item
boss.setDropChance("all", 0);          // No drops at all
```

### Spawning with Full Configuration

```js
const entity = world.createEntity({
  type: "minecraft:skeleton",
  position: new GameVector3(0, 100, 0),
  velocity: new GameVector3(0, 0.5, 0),
  fixed: false,
  gravity: true,
  friction: 0.5,
  collides: true,
  hp: 40,
  maxHp: 40,
  tags: ["elite", "undead"],
});
if (!entity) return;  // createEntity may return null

entity.setEquipment("mainhand", "minecraft:bow");
// entity.setTarget(targetEntity);   // Set attack target (requires entity reference)
entity.clearTarget();                 // Clear target
entity.navigateTo(10, 100, 10, 0.5); // Navigate to position
entity.setPersistent(true);           // Persistent (won't be unloaded)

// Death callback
entity.setOnDestroy(() => {
  console.log("Elite skeleton defeated");
});
```

## 3.6 Patrol Guard (Full Example)

The following code spawns a skeleton guard that patrols between waypoints and attacks nearby players:

```js
function createPatrol(
  name: string,
  startPos: GameVector3,
  waypoints: GameVector3[],
  speed: number
): GameEntity | null {
  const guard = world.spawnEntity("minecraft:skeleton", startPos);
  if (!guard) { return null; }

  guard.setNameTag(name);
  guard.maxHp = 50;
  guard.hp = 50;
  guard.setEquipment("mainhand", "minecraft:bow");
  guard.setEquipment("head", "minecraft:iron_helmet");
  guard.setAI(true);

  let wpIndex = 0;
  const tid = setInterval(() => {
    if (guard.destroyed) {
      tid.cancel();
      return;
    }
    // Reached current waypoint → move to next
    const wp = waypoints[wpIndex];
    const pos = guard.position;
    const dx = pos.x - wp.x;
    const dy = pos.y - wp.y;
    const dz = pos.z - wp.z;
    const dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
    if (dist < 2) {
      wpIndex = (wpIndex + 1) % waypoints.length;
    }
    guard.navigateTo(
      waypoints[wpIndex].x, waypoints[wpIndex].y, waypoints[wpIndex].z,
      speed
    );
    // Attack nearby players
    const nearby = world.entitiesInRadius(pos, 8);
    nearby.forEach((e) => {
      if (e.isPlayer() && !guard.getTarget()) {
        guard.setTarget(e);
      }
    });
  }, 40);  // Update navigation every 2 seconds

  return guard;
}

// Usage:
const route = [
  new GameVector3(0, 70, 0),
  new GameVector3(10, 70, 0),
  new GameVector3(10, 70, 10),
  new GameVector3(0, 70, 10),
];
void createPatrol("§ePatrol Guard", route[0], route, 0.8);
```

## 3.7 Entity Tags & Collisions

```js
entity.addTag("boss");
entity.removeTag("elite");
if (entity.hasTag("boss")) {
  // Special boss handling
}
const tags = entity.tags();  // ["boss", "undead"]

// Entity collision
world.onEntityContact((entityA, entityB, _tick) => {
  if (entityA.isPlayer() && entityB.hasTag("boss")) {
    entityA.player.actionBar("§cWatch out — Boss!");
  }
});

world.onEntitySeparate((entityA, entityB, _tick) => {
  // Two entities separated
});
```

## 3.8 Common Entity Types

```js
minecraft:zombie      Zombie
minecraft:skeleton    Skeleton
minecraft:creeper     Creeper
minecraft:spider      Spider
minecraft:witch       Witch
minecraft:villager    Villager
minecraft:iron_golem  Iron Golem
minecraft:slime       Slime
minecraft:wither      Wither
minecraft:ender_dragon Ender Dragon
minecraft:area_effect_cloud  Effect cloud (useful for position markers)
```

## Next Step

Tutorial 4 covers advanced game systems: scoreboards, BossBars, teams, world border, and cross-script communication.
