# entity — Entity API

`entity` represents any entity in the Minecraft world (mobs, animals, items, players). Obtain via `world.spawnEntity()`, `world.querySelector()`, `world.entitiesInRadius()`, or event callback parameters.

Use `entity.player` to get the corresponding `player` object (only valid if the entity is a player).

## Basic Properties

### entity.id

✅ Box3 API | Read-only. The entity's UUID string.

### entity.isPlayer()

✅ Box3 API | Returns `true` if this entity is a player.

### entity.entityType

✅ Box3 API | Read-only. Returns the entity's namespaced ID string.

```js
var all = world.querySelectorAll("*");
for (var i = 0; i < all.length; i++) {
  var e = all[i];
  console.log(e.id + " -> " + e.entityType + " -> isPlayer: " + e.isPlayer());
}
```

## Position & Movement

### entity.position

✅ Box3 API | Read-only `GameVector3`. Note: this is a LiveVec3 — calling `.set(x,y,z)` directly teleports the entity.

```js
var pos = entity.position;
console.log(pos.x, pos.y, pos.z);

// Teleport
entity.position.set(0, 100, 0);
```

### entity.velocity

✅ Box3 API | Read-only `GameVector3`. LiveVec3 — `.set(x,y,z)` directly modifies velocity.

```js
entity.velocity.set(0, 1, 0); // upward velocity
```

### entity.bounds

✅ Box3 API | Read-only `GameVector3`. The entity's bounding box half-size.

### entity.onGround

⬆ MC Extension | Read-only. Whether the entity is on the ground.

```js
if (entity.onGround) {
  // on ground
}
```

### entity.eyePosition

⬆ MC Extension | Read-only `GameVector3`. The entity's eye-level position.

```js
var eye = entity.eyePosition;
```

## Health

### entity.hp

✅ Box3 API | Get/set current health (LivingEntity only).

### entity.maxHp

✅ Box3 API | Get/set maximum health (LivingEntity only).

```js
var zombie = world.spawnEntity("minecraft:zombie", new GameVector3(0, 100, 0));
zombie.maxHp = 100;
zombie.hp = 100;
```

### entity.hurt(amount)

✅ Box3 API | Deal `amount` damage to the entity (triggers damage event).

### entity.heal(amount)

✅ Box3 API | Heal the entity by `amount` (capped at maxHp).

```js
zombie.hurt(10); // deal 10 damage
zombie.heal(5); // heal 5
```

### entity.invulnerable

⬆ MC Extension | Get/set whether the entity is invulnerable.

```js
entity.invulnerable = true; // immune to damage
console.log(entity.invulnerable);
```

## Appearance

### entity.meshInvisible

✅ Box3 API | Controls entity invisibility.

```js
entity.meshInvisible = true; // invisible
```

### entity.glowing

⬆ MC Extension | Get/set glowing effect (like spectral arrow effect).

```js
entity.glowing = true; // entity glows
console.log(entity.glowing);
```

### entity.nameTag

⬆ MC Extension | Get/set the entity's custom name (displayed above head).

```js
entity.nameTag = "§cBoss Monster";
console.log(entity.nameTag);
```

## Tag System

All ✅ Box3 API. Tags are string markers attached to entities for classification and querying.

### entity.addTag(tag)

Add a tag.

### entity.hasTag(tag)

Check if the entity has the given tag.

### entity.removeTag(tag)

Remove a tag.

```js
entity.addTag("boss");
entity.addTag("red_team");

if (entity.hasTag("boss")) {
  entity.maxHp = 200;
}

// Query by tag
var bosses = world.querySelectorAll(".boss");
```

## Fire

### entity.setFire(ticks)

⬆ MC Extension | Set the entity on fire for the given number of ticks. 20 ticks = 1 second.

### entity.clearFire()

⬆ MC Extension | Extinguish the entity's fire.

```js
entity.setFire(100); // ignite for 5 seconds
entity.clearFire(); // extinguish immediately
```

## AI & Navigation

### entity.setAI(enabled)

⬆ MC Extension | Enable/disable entity AI (Mob only). When disabled, the entity won't move or attack.

```js
entity.setAI(false); // freeze entity
```

### entity.setTarget(entity)

⬆ MC Extension | Set the mob's attack target (Mob only).

### entity.getTarget()

⬆ MC Extension | Get the current attack target, returns `Box3JSEntity` or null.

### entity.clearTarget()

⬆ MC Extension | Clear the attack target.

```js
var boss = world.spawnEntity("minecraft:skeleton", new GameVector3(0, 100, 0));
var target = world.querySelectorAll("*")[0];
boss.setTarget(target);
```

### entity.navigateTo(x, y, z, speed)

⬆ MC Extension | Make the entity pathfind to the target coordinates (PathfinderMob only).

### entity.navigateTo(pos, speed)

⬆ GameVector3 overload.

```js
entity.navigateTo(10, 100, 10, 1.0); // walk to target at speed 1.0
entity.navigateTo(target.position, 1.0);
```

### entity.lookAt(x, y, z)

⬆ MC Extension | Make the entity face the target coordinates.

### entity.lookAt(pos)

⬆ GameVector3 overload.

```js
entity.lookAt(0, 100, -10);
entity.lookAt(target.position);
```

## Potion Effects

All ⬆ MC Extension.

### entity.addEffect(effectId, duration, amplifier)

Add a potion effect. `duration` in ticks, `amplifier` starts at 0.

### entity.addEffect(effectId, duration, amplifier, hideParticles)

Add effect with optional particle hiding.

```js
entity.addEffect("minecraft:speed", 600, 2); // Speed III, 30 seconds
entity.addEffect("minecraft:strength", 99999, 1, true); // Permanent Strength II, no particles
entity.addEffect("minecraft:glowing", 200, 0); // Glowing 10 seconds

// Common effects:
// minecraft:speed, minecraft:slowness, minecraft:strength
// minecraft:weakness, minecraft:regeneration, minecraft:poison
// minecraft:jump_boost, minecraft:slow_falling, minecraft:invisibility
// minecraft:glowing, minecraft:levitation, minecraft:fire_resistance
```

## Equipment

All ⬆ MC Extension.

### entity.setEquipment(slot, itemId)

Equip a mob with gear.

**Slot values:** `"mainhand"`, `"offhand"`, `"head"` (helmet), `"chest"` (chestplate), `"legs"` (leggings), `"feet"` (boots)

```js
entity.setEquipment("mainhand", "minecraft:diamond_sword");
entity.setEquipment("head", "minecraft:iron_helmet");
entity.setEquipment("chest", "minecraft:iron_chestplate");
entity.setEquipment("feet", "minecraft:leather_boots");
```

### entity.setDropChance(slot, chance)

Set the drop probability for equipment in a slot, 0.0–1.0. Use `slot = "all"` to set all slots at once.

```js
entity.setDropChance("mainhand", 0.5); // 50% chance to drop mainhand item
entity.setDropChance("all", 0); // drop nothing
```

## Attributes

All ⬆ MC Extension.

### entity.getAttribute(attributeId)

Get the current value of an entity attribute.

### entity.setAttribute(attributeId, value)

Set the base value of an entity attribute.

```js
var attack = entity.getAttribute("minecraft:generic.attack_damage");
entity.setAttribute("minecraft:generic.attack_damage", 10);
entity.setAttribute("minecraft:generic.max_health", 100);
entity.setAttribute("minecraft:generic.movement_speed", 0.5);
entity.setAttribute("minecraft:generic.knockback_resistance", 1.0);
entity.setAttribute("minecraft:generic.armor", 10);
```

> Note: Box3 convenience properties like `maxHp` / `walkSpeed` / `jumpPower` use these attributes internally. Prefer convenience properties; use `setAttribute` only for attributes without dedicated wrappers.

## Lifecycle

### entity.destroy()

✅ Box3 API | Destroy the entity. Fires the `onDestroy` callback if set.

### entity.remove()

⬆ MC Extension | Directly remove the entity, **without** firing the `onDestroy` callback.

### entity.setOnDestroy(handler)

✅ Box3 API | Set a destroy callback. `handler` receives one argument `(entity)`.

### entity.destroyed

✅ Box3 API | Read-only. Whether the entity has been removed.

### entity.setPersistent(v)

⬆ MC Extension | When `true`, the mob won't despawn when far from players (Mob only).

```js
var boss = world.spawnEntity(
  "minecraft:wither_skeleton",
  new GameVector3(0, 100, 0),
);
boss.setPersistent(true); // won't despawn
boss.setOnDestroy((e) => {
  world.say("Boss defeated!");
});
```

## Custom Properties

✅ Box3 API | You can store arbitrary JS data directly on an entity. The data lives as long as the entity.

```js
entity.myCustomField = "hello";
entity.spawnTick = world.currentTick();
entity.killCount = 0;

console.log(entity.myCustomField);
```
