/// <reference path="../shared.d.ts" />

// ── §3 @zh 实体 @en Entity ──

/**
 * @zh 实体包装，可用于玩家或生物。
 * 通过 `world.querySelector()`、`world.querySelectorAll()` 或事件回调获取。
 *
 * @en Entity wrapper — represents a player or mob in the world.
 * Obtained via `world.querySelector()`, `world.querySelectorAll()`, or event callbacks.
 */
interface GameEntity {
  // ── @zh 身份 @en Identity ──

  /**
   * @zh 实体 UUID (字符串格式, 只读)。
   * @en Entity UUID as a string (e.g. "550e8400-e29b-41d4-a716-446655440000"), readonly.
   */
  readonly id: string;

  /**
   * @zh 是否为玩家实体。返回 true 后 player 属性自动收窄为非 null。
   * @en True if this entity is a player. After a truthy check, `player` is narrowed to non-null.
   */
  isPlayer(): this is GamePlayerEntity;

  /**
   * @zh 实体类型标识符 (如 "minecraft:zombie", 只读)。
   * @en Entity type identifier (e.g. "minecraft:zombie"), readonly.
   */
  readonly entityType: string;

  // ── @zh 位置 & 运动 @en Position & Movement ──

  /**
   * @zh 当前坐标 (世界坐标, 只读, 可通过 .set() 修改)。
   * @en Current world‑space position. Readonly ref — mutate via .set(), cannot reassign.
   */
  readonly position: GameVector3;

  /**
   * @zh 当前速度 (运动向量, 只读, 可通过 .set() 修改)。
   * @en Current velocity (motion vector). Readonly ref — mutate via .set(), cannot reassign.
   */
  readonly velocity: GameVector3;

  /**
   * @zh 包围盒半尺寸 (x=宽/2, y=高/2, z=宽/2, 只读)。
   * @en Bounding‑box half‑extents (x=width/2, y=height/2, z=width/2), readonly.
   */
  readonly bounds: GameVector3;

  /**
   * @zh 是否在地面上 (只读)。
   * @en True if the entity is standing on a block, readonly.
   */
  readonly onGround: boolean;

  /**
   * @zh 视线起始点 (眼部位置, 只读)。
   * @en Eye position (raycast origin for the entity's view), readonly.
   */
  readonly eyePosition: GameVector3;

  // ── @zh 生命状态 @en Lifecycle ──

  /**
   * @zh 当前生命值。
   * @en Current health (HP).
   */
  hp: number;

  /**
   * @zh 最大生命值。
   * @en Maximum health.
   */
  maxHp: number;

  /**
   * @zh 实体是否已被移除/销毁 (true = 已移除, 只读)。
   * @en Whether the entity has been removed / destroyed (true = removed), readonly.
   */
  readonly destroyed: boolean;

  /**
   * @zh 设置实体着火 tick 数 (0 = 灭火)。
   * @en Sets the remaining fire ticks (0 = extinguish).
   */
  setFire(ticks: number): void;

  /** @zh 灭火 @en Extinguishes any fire on the entity. */
  clearFire(): void;

  // ── @zh 伤害 & 恢复 @en Damage & Healing ──

  /**
   * @zh 对实体造成伤害。
   * @en Deals generic damage to the entity.
   * @param amount - @zh 伤害值（半心） @en damage amount in half‑hearts
   */
  hurt(amount: number): void;

  /**
   * @zh 治疗实体。
   * @en Heals the entity.
   * @param amount - @zh 治疗量（半心） @en healing amount in half‑hearts
   */
  heal(amount: number): void;

  // ── @zh 外观 @en Appearance ──

  /**
   * @zh 是否不可见 (隐身)。
   * @en True if the entity is invisible.
   */
  meshInvisible: boolean;

  /** @zh 是否发光 (轮廓高亮) @en Whether glow outline is active. */
  glowing: boolean;

  /** @zh 设置发光颜色 (通过队伍颜色实现, 映射到最接近的 ChatFormatting)。 @en Sets glow outline color (via team color, mapped to nearest ChatFormatting). */
  setGlowColor(color: GameRGBColor): void;

  // ── @zh 文字展示实体 @en TextDisplay ──

  /** @zh 设置文字展示实体的文本 (仅 text_display 实体有效)。 @en Sets text for text display entities. */
  setText(text: string): void;
  /** @zh 设置文字展示实体的文本颜色。 @en Sets the text color for text display entities. */
  setTextColor(color: GameRGBColor): void;
  /** @zh 设置文字展示实体的背景颜色。 @en Sets the background color for text display entities. */
  setTextBackgroundColor(color: GameRGBAColor): void;

  /**
   * @zh 名称标签文本 (空字符串 = 无)。
   * @en Custom name tag text (empty string = none).
   */
  nameTag: string;
  setNameTag(name: string): void;

  // ── @zh 物理 @en Physics ──

  /**
   * @zh 是否参与碰撞 (默认 true)。
   * @en Whether the entity participates in collisions (default true).
   */
  collides: boolean;

  /**
   * @zh 是否固定 (默认 false, true 时禁用重力并每 tick 清零速度)。
   * @en Whether the entity is fixed in place (default false; disables gravity + zeros velocity each tick).
   */
  fixed: boolean;

  /**
   * @zh 是否受重力影响 (默认 true)。
   * @en Whether gravity affects the entity (default true).
   */
  gravity: boolean;

  /** @zh 摩擦系数 (默认 0.0) @en Friction coefficient. */
  friction: number;

  /** @zh 质量 (默认 1.0) @en Mass. */
  mass: number;

  /** @zh 弹性系数 (默认 0.0) @en Restitution (bounciness). */
  restitution: number;

  // ── @zh 无敌 & 持久化 @en Invulnerability & Persistence ──

  /** @zh 是否无敌 @en Whether the entity is invulnerable to damage. */
  invulnerable: boolean;

  /**
   * @zh 设置为持久化实体 (防止被自然清除)。
   * @en Marks the entity as persistent (prevents it from being despawned naturally).
   * @remarks 仅写方法, 无 getter。Write‑only method, no getter available.
   */
  setPersistent(v: boolean): void;

  // ── @zh 标签 @en Tags ──

  /** @zh 添加一个标签 @en Adds a scoreboard tag. */
  addTag(tag: string): void;

  /** @zh 移除一个标签 @en Removes a scoreboard tag. */
  removeTag(tag: string): void;

  /** @zh 检查是否拥有指定标签 @en Checks whether the entity has the given tag. */
  hasTag(tag: string): boolean;

  /** @zh 获取所有标签 @en Returns all tags as a string array. */
  tags(): string[];

  // ── @zh 效果 @en Effects ──

  /**
   * @zh 添加状态效果。
   * @en Applies a status effect to the entity.
   *
   * @example
   * @zh ```ts
   * @en // 给予实体 30 秒速度 II 效果，隐藏粒子
   * entity.addEffect("minecraft:speed", 600, 1, true);
   * // 给予实体 10 秒发光效果
   * entity.addEffect("minecraft:glowing", 200, 0);
   * ```
   *
   * @param effectId - @zh 效果 ID（如 "minecraft:speed"） @en Effect ID (e.g. "minecraft:speed")
   * @param duration - @zh 持续时间（tick） @en Duration in ticks
   * @param amplifier - @zh 等级（0 = 一级） @en Amplifier (0 = level I)
   * @param hideParticles - @zh 是否隐藏粒子（可选，默认 false） @en Whether to hide particles (optional, default false)
   */
  addEffect(
    effectId: string,
    duration: number,
    amplifier: number,
    hideParticles?: boolean,
  ): void;

  // ── @zh 属性 @en Attributes ──

  /**
   * @zh 读取实体属性值。
   * @en Reads a registered entity attribute value.
   * @param attributeId - @zh 属性 ID @en attribute ID (e.g. "minecraft:generic.max_health")
   * @returns @zh 当前属性值，不支持的实体返回 0 @en Current attribute value, 0 for unsupported entities
   */
  getAttribute(attributeId: string): number;

  /**
   * @zh 设置实体属性基础值。
   * @en Sets the base value of a registered entity attribute.
   * @param attributeId - @zh 属性 ID @en attribute ID (e.g. "minecraft:generic.movement_speed")
   * @param value - @zh 新基础值 @en new base value
   * @remarks 仅对 LivingEntity 有效。Only works on living entities.
   */
  setAttribute(attributeId: string, value: number): void;

  // ── @zh 装备 @en Equipment ──

  /**
   * @zh 给生物设置装备。
   * @en Equips an item onto a mob's equipment slot.
   * @param slot - @zh 槽位名称 @en slot name:
   *   "mainhand", "offhand", "head"/"helmet"/"helm",
   *   "chest"/"chestplate", "legs"/"leggings", "feet"/"boots"
   * @param itemId - 物品 ID (如 "minecraft:diamond_sword")
   */
  setEquipment(slot: string, itemId: string): void;

  /**
   * @zh 设置装备掉落概率。
   * @en Sets the drop chance for an equipment slot.
   * @param slot - @zh 槽位名称 或 "all"（所有槽位） @en slot name or "all" for every slot
   * @param chance - @zh 掉落概率（0‑1） @en drop chance (0–1)
   */
  setDropChance(slot: string, chance: number): void;

  // ── @zh 导航 & AI @en Navigation & AI ──

  /**
   * @zh 让生物导航到指定坐标。
   * @en Orders a pathfinder mob to navigate to the given coordinates.
   * @param x, y, z - @zh 目标坐标 @en target coordinates
   * @param speed - @zh 移动速度倍率 @en movement speed multiplier
   * @returns @zh 路径计算成功返回 true，非 PathfinderMob 返回 false @en true if pathfinding succeeded, false for non-PathfinderMob entities
   */
  navigateTo(x: number, y: number, z: number, speed: number): boolean;
  /** @zh GameVector3 重载 @en GameVector3 overload. */
  navigateTo(pos: GameVector3, speed: number): boolean;

  /**
   * @zh 设置生物的当前攻击目标。
   * @en Sets the mob's attack target (the mob will pathfind to and attack it).
   */
  setTarget(target: GameEntity): void;

  /** @zh 清除攻击目标, 停止追击 @en Clears the attack target, stopping pursuit. */
  clearTarget(): void;

  /**
   * @zh 获取当前攻击目标 (可能为 null)。
   * @en Returns the mob's current attack target, or null.
   */
  getTarget(): GameEntity | null;

  /**
   * @zh 启用或禁用生物 AI (寻路/目标等)。
   * @en Enables or disables the mob's AI (pathfinding, goals, etc.).
   */
  setAI(enabled: boolean): void;

  // ── @zh 朝向 @en Look direction ──

  /**
   * @zh 让实体看向指定坐标。
   * @en Makes the entity look at a point in space.
   */
  lookAt(x: number, y: number, z: number): void;
  lookAt(pos: GameVector3): void;

  // ── @zh 生命周期 @en Lifecycle ──

  /**
   * @zh 销毁实体 (触发 onDestroy 回调)。
   * @en Destroys the entity (triggers any registered onDestroy callback).
   */
  destroy(): void;

  setOnDestroy(handler: (entity: GameEntity) => void): void;

  // ── @zh 玩家代理 @en Player proxy ──

  /**
   * @zh 玩家接口 (仅当 isPlayer 为 true 时非 null)。
   * @en The player interface — non‑null only when isPlayer is true.
   */
  player: GamePlayer | null;
}

/**
 * @zh 玩家实体 — `GameEntity` 的子类型，保证 `player` 属性非 null。
 * @en A player entity — subtype of `GameEntity` with a guaranteed non‑null `player`.
 */
type GamePlayerEntity = GameEntity & { player: GamePlayer; hasBox3JSClient(): boolean };
