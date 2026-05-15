---
---

# entity — Entity API

`entity` represents any entity in the Minecraft world (mobs, animals, items, players).

Use `entity.player` to get the corresponding `player` object (non-null only when the entity is a player).

## Basic Identity

### entity.id

Readonly. The entity's UUID string (e.g. `"550e8400-e29b-41d4-a716-446655440000"`).

### entity.isPlayer()

Returns `true` if the entity is a player. When true, `entity.player` is guaranteed non-null.

### entity.entityType

Readonly. Returns the entity's namespace ID string (e.g. `"minecraft:zombie"`).

```js
var all = world.querySelectorAll("*");
for (var i = 0; i < all.length; i++) {
  var e = all[i];
  console.log(e.id + " -> " + e.entityType + " -> isPlayer: " + e.isPlayer());
}
```

## Position & Movement

### entity.position

Readonly `GameVector3`. This is a **LiveVec3**: reading syncs to the entity's current coordinates; calling `.set(x,y,z)` **directly teleports** the entity.

```js
var pos = entity.position;
console.log(pos.x, pos.y, pos.z);

// Teleport
entity.position.set(0, 100, 0);
```

### entity.velocity

Readonly `GameVector3`. **LiveVec3**: reading syncs to current velocity; `.set(x,y,z)` directly sets the velocity vector.

```js
entity.velocity.set(0, 1, 0);  // Launch upward
entity.velocity.set(2, 0, 2);  // Horizontal velocity
```

### entity.bounds

Readonly `GameVector3`. The entity's bounding box **half-extents**:
- `x` = width / 2
- `y` = height / 2
- `z` = width / 2

### entity.onGround

⬆ MC Extension | Readonly. Whether the entity is standing on a block.

```js
if (entity.onGround) {
  // On the ground
}
```

### entity.eyePosition

⬆ MC Extension | Readonly `GameVector3`. Eye position (raycast origin).

```js
var eye = entity.eyePosition;
```

## Health

### entity.hp

Gets/sets current health. For non-LivingEntity, returns/stores a custom property.

### entity.maxHp

Gets/sets maximum health. If current health exceeds the new maximum, it is clamped.

```js
var zombie = world.spawnEntity("minecraft:zombie", new GameVector3(0, 100, 0));
zombie.maxHp = 100;
zombie.hp = 100;
```

### entity.hurt(amount)

Deals `amount` generic damage to the entity (triggers damage events).

### entity.heal(amount)

Heals the entity by `amount` (capped at maxHp).

```js
zombie.hurt(10);  // Deal 10 damage
zombie.heal(5);   // Heal 5
```

### entity.invulnerable

⬆ MC Extension | Gets/sets whether the entity is invulnerable (immune to damage).

```js
entity.invulnerable = true;
console.log(entity.invulnerable);
```

### entity.destroyed

Readonly. Whether the entity has been removed/destroyed.

## Physics

The following properties control entity physics behavior.

### entity.collides

Gets/sets whether the entity participates in collisions. Default `true`. When `false`, disables physics for LivingEntity (setNoPhysics).

```js
entity.collides = false;  // No-collision ghost
```

### entity.fixed

Gets/sets whether the entity is fixed in place. Default `false`. When `true`, disables gravity and zeros velocity each tick.

```js
entity.fixed = true;  // Stationary decoration, no gravity
```

### entity.gravity

Gets/sets whether the entity is affected by gravity. Default `true`. When `false`, disables gravity (setNoGravity).

```js
entity.gravity = false;  // Floats without gravity
```

### entity.friction

Gets/sets friction coefficient (custom property, default `0.0`). Scripts can read this to implement custom friction logic.

### entity.mass

Gets/sets mass (custom property, default `1.0`). Scripts can read this for custom physics calculations.

### entity.restitution

Gets/sets restitution / bounciness (custom property, default `0.0`). Scripts can read this for custom collision response.

```js
// Create a bouncy ball
var ball = world.createEntity({
  type: "minecraft:slime",
  position: new GameVector3(0, 100, 0),
  gravity: true,
  collides: true,
  restitution: 0.8,
  mass: 0.5
});
```

## Appearance

### entity.meshInvisible

Controls entity invisibility.

```js
entity.meshInvisible = true;  // Invisible
console.log(entity.meshInvisible);
```

### entity.glowing

⬆ MC Extension | Gets/sets the glow outline effect (similar to spectral arrow).

```js
entity.glowing = true;
console.log(entity.glowing);
```

### entity.setGlowColor(color)

⬆ MC Extension | Sets the glow outline color via team color, mapping RGB to the nearest `ChatFormatting` (16 colors).

```js
entity.glowing = true;
entity.setGlowColor(new GameRGBColor(1, 0, 0));  // Red glow
entity.setGlowColor(new GameRGBColor(0, 0, 1));  // Blue glow
```

### entity.setText(text)

⬆ MC Extension | Sets the text content of a text display entity (only effective on `minecraft:text_display` entities).

### entity.setTextColor(color)

⬆ MC Extension | Sets the text color for text display entities.

### entity.setTextBackgroundColor(color)

⬆ MC Extension | Sets the background color for text display entities. `GameRGBAColor` can be used for semi-transparent backgrounds.

```js
// Create a text display entity
var textEntity = world.createEntity("minecraft:text_display", pos);
textEntity.setText("Hello, World!");
textEntity.setTextColor(new GameRGBColor(1, 1, 1));             // White text
textEntity.setTextBackgroundColor(new GameRGBAColor(0, 0, 0, 0.5)); // Semi-transparent black background
```

### entity.nameTag

⬆ MC Extension | Gets/sets the entity's custom display name (supports color codes). Empty string = no name.

```js
entity.nameTag = "§cBoss Mob";
console.log(entity.nameTag);        // Property access
entity.setNameTag("§eGuard");       // Method access
```

## Tag System

 Tags are string markers attached to entities (backed by Minecraft scoreboard tags), used for classification and queries.

### entity.addTag(tag)

Adds a tag.

### entity.hasTag(tag)

Checks whether the entity has the specified tag.

### entity.removeTag(tag)

Removes a tag.

### entity.tags()

Returns all tags as a string array.

```js
entity.addTag("boss");
entity.addTag("red_team");

if (entity.hasTag("boss")) {
  entity.maxHp = 200;
}

// Get all tags
var allTags = entity.tags();
for (var i = 0; i < allTags.length; i++) {
  console.log(allTags[i]);
}

// Query by tag
var bosses = world.querySelectorAll(".boss");
```

## Fire

### entity.setFire(ticks)

⬆ MC Extension | Sets the entity on fire for the given number of ticks. 20 ticks = 1 second.

### entity.clearFire()

⬆ MC Extension | Extinguishes any fire on the entity.

```js
entity.setFire(100);   // Ignite for 5 seconds
entity.clearFire();    // Extinguish immediately
```

## AI & Navigation

### entity.setAI(enabled)

⬆ MC Extension | Enables/disables the entity's AI (Mob only). When disabled, the entity won't move or attack.

```js
entity.setAI(false);  // Freeze entity
```

### entity.setTarget(target)

⬆ MC Extension | Sets the mob's attack target (Mob only). The mob will pathfind to and attack it.

### entity.getTarget()

⬆ MC Extension | Returns the current attack target, or `null`.

### entity.clearTarget()

⬆ MC Extension | Clears the attack target, stopping pursuit.

```js
var boss = world.spawnEntity("minecraft:skeleton", new GameVector3(0, 100, 0));
var target = world.querySelectorAll("*")[0];
boss.setTarget(target);
// ...
boss.clearTarget();
```

### entity.navigateTo(x, y, z, speed)

⬆ MC Extension | Orders a pathfinder mob to navigate to the given coordinates. Returns `true` if path calculation succeeded.

### entity.navigateTo(pos, speed)

⬆ GameVector3 overload.

```js
entity.navigateTo(10, 100, 10, 1.0);
entity.navigateTo(target.position, 1.0);
```

### entity.lookAt(x, y, z)

⬆ MC Extension | Makes the entity look at the given coordinates.

### entity.lookAt(pos)

⬆ GameVector3 overload.

```js
entity.lookAt(0, 100, -10);
entity.lookAt(target.position);
```

## Status Effects

All ⬆ MC Extension.

### entity.addEffect(effectId, duration, amplifier)

Applies a status effect. `duration` in ticks (20 ticks = 1 second), `amplifier` starts at 0 (0 = level I).

### entity.addEffect(effectId, duration, amplifier, hideParticles)

Applies an effect, optionally hiding particles.

```js
entity.addEffect("minecraft:speed", 600, 2);                    // Speed III, 30 seconds
entity.addEffect("minecraft:strength", 99999, 1, true);         // Permanent Strength II, no particles
entity.addEffect("minecraft:glowing", 200, 0);                  // Glowing 10 seconds

// Common effects:
// minecraft:speed, minecraft:slowness, minecraft:strength
// minecraft:weakness, minecraft:regeneration, minecraft:poison
// minecraft:jump_boost, minecraft:slow_falling, minecraft:invisibility
// minecraft:glowing, minecraft:levitation, minecraft:fire_resistance
```

## Equipment

All ⬆ MC Extension.

### entity.setEquipment(slot, itemId)

Equips an item onto a mob. **Slot values:**

| slot | Description |
|------|-------------|
| `"mainhand"` | Main hand |
| `"offhand"` | Off hand |
| `"head"`, `"helmet"`, `"helm"` | Helmet |
| `"chest"`, `"chestplate"` | Chestplate |
| `"legs"`, `"leggings"` | Leggings |
| `"feet"`, `"boots"` | Boots |

```js
entity.setEquipment("mainhand", "minecraft:diamond_sword");
entity.setEquipment("head", "minecraft:iron_helmet");
entity.setEquipment("chest", "minecraft:iron_chestplate");
entity.setEquipment("feet", "minecraft:leather_boots");
```

### entity.setDropChance(slot, chance)

Sets the drop chance for an equipment slot, range 0.0–1.0. Use `"all"` for `slot` to set all slots at once (both hands + four armor slots).

```js
entity.setDropChance("mainhand", 0.5);  // 50% chance to drop main hand item
entity.setDropChance("all", 0);         // Drop nothing
```

## Attributes

All ⬆ MC Extension.

### entity.getAttribute(attributeId)

Returns the current attribute value. Returns 0 for non-LivingEntity.

### entity.setAttribute(attributeId, value)

Sets the base attribute value. Only works on LivingEntity.

```js
var attack = entity.getAttribute("minecraft:generic.attack_damage");
entity.setAttribute("minecraft:generic.attack_damage", 10);
entity.setAttribute("minecraft:generic.max_health", 100);
entity.setAttribute("minecraft:generic.movement_speed", 0.5);
entity.setAttribute("minecraft:generic.knockback_resistance", 1.0);
entity.setAttribute("minecraft:generic.armor", 10);
```

::: tip
`maxHp` / `hp` / `walkSpeed` / `jumpPower` and other Box3 convenience properties use these attributes internally. Prefer the convenience properties; use `setAttribute` only for attributes not exposed as properties.
:::

## Lifecycle

### entity.destroy()

Destroys the entity. If a callback was registered via `setOnDestroy()`, it will be invoked.

### entity.setOnDestroy(handler)

Registers a callback called when the entity is destroyed. `handler` receives one argument `(entity)`.

```js
entity.setOnDestroy(function(e) {
  console.log("Entity " + e.id + " destroyed");
});
```

### entity.setPersistent(v)

⬆ MC Extension | When `true`, prevents the mob from despawning naturally (Mob only). Write-only method, no getter.

```js
var boss = world.spawnEntity(
  "minecraft:wither_skeleton",
  new GameVector3(0, 100, 0),
);
boss.setPersistent(true);  // Won't despawn
boss.setNameTag("§c§lWither Guard");
boss.setOnDestroy(function(e) {
  world.say("Boss defeated!");
});
```

## Custom Properties

You can store arbitrary JS data directly on the entity. The data lives as long as the entity exists.

Custom properties are stored under the entity's UUID in a `ConcurrentHashMap` and persist until the entity is removed.

```js
entity.myCustomField = "hello";
entity.spawnTick = world.currentTick();
entity.killCount = 0;

console.log(entity.myCustomField);
```
