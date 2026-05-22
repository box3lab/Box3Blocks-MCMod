/// <reference path="../shared.d.ts" />
/// <reference path="entity.d.ts" />

// ── §2 @zh 世界 @en World ──

/**
 * @zh 世界接口，通过 `world` 访问。
 * @en World interface — accessed via `world`.
 */
interface GameWorld {
  // ── @zh 基本信息 @en Identity ──

  /** @zh 项目名称 (只读方法) @en Project name, call as method. */
  projectName(): string;

  /** @zh 当前 tick 数 (只读方法) @en Current server tick, call as method. */
  currentTick(): number;

  /** @zh 服务器 MOTD / 标识 @en Server MOTD / identifier. */
  serverId: string;

  // ── @zh 天气 @en Weather ──

  /** @zh 降雨密度 (0‑1) @en Rain density (0–1). */
  rainDensity: number;

  /** @zh 雷暴密度 (0‑1) @en Thunder density (0–1). */
  thunderDensity: number;

  /** @zh 清除所有天气 @en Clears rain and thunder. */
  clearWeather(): void;

  // ── @zh 时间 @en Time ──

  /** @zh 世界时间 (tick) @en World day time in ticks. */
  time: number;

  /**
   * @zh 设置世界时间。
   * @en Sets the world day time.
   */
  setTime(tick: number): void;

  /** @zh 时间流速 (1.0 = 正常, 0 = 冻结) @en Time scale (1.0 = normal, 0 = frozen). */
  timeScale: number;

  // ── @zh 难度 @en Difficulty ──

  /** @zh 游戏难度 ("peaceful" / "easy" / "normal" / "hard") @en Game difficulty string. */
  difficulty: string;

  // ── @zh 游戏规则 @en Game Rules ──

  /**
   * @zh 获取游戏规则值。
   * @en Gets a game rule value.
   * @param name - 规则名 (如 "doDaylightCycle")
   * @returns 布尔值或 null
   */
  getGameRule(name: string): boolean | null;

  /**
   * @zh 设置游戏规则值。
   * @en Sets a game rule value.
   * @param name - 规则名
   * @param value - 新值 (布尔)
   */
  setGameRule(name: string, value: boolean): void;

  // ── @zh 出生点 @en Spawn ──

  /** @zh 世界出生点 (只读) @en World spawn point, readonly. */
  readonly spawnPoint: GameVector3;

  /**
   * @zh 设置世界出生点。
   * @en Sets the world spawn point.
   */
  setWorldSpawn(pos: GameVector3): void;

  // ── @zh 实体生成 @en Entity Spawning ──

  /**
   * @zh 在指定坐标生成实体。
   * @en Spawns an entity at the given position.
   * @returns GameEntity 或 null
   */
  spawnEntity(type: string, pos: GameVector3): GameEntity | null;

  /**
   * @zh 按配置对象创建实体。
   * @en Creates an entity from a config object.
   * @param config - { type, position, velocity?, fixed?, gravity?, friction?, mass?, restitution?, collides?, meshInvisible?, hp?, maxHp?, tags? }
   */
  createEntity(config: {
    type?: string;
    position?: GameVector3;
    velocity?: GameVector3;
    fixed?: boolean;
    gravity?: boolean;
    friction?: number;
    mass?: number;
    restitution?: number;
    collides?: boolean;
    meshInvisible?: boolean;
    hp?: number;
    maxHp?: number;
    tags?: string[];
  }): GameEntity | null;

  // ── @zh 实体查询 @en Entity Query ──

  /**
   * @zh 查询所有匹配选择器的实体。
   * @en Queries all entities matching the selector.
   */
  querySelectorAll(selector: string): GameEntity[];

  /**
   * @zh 查询第一个匹配选择器的实体。
   * @en Queries the first entity matching the selector.
   */
  querySelector(selector: string): GameEntity | null;

  /**
   * @zh 在矩形区域内搜索实体。
   * @en Searches entities within a bounding box.
   */
  searchBox(bounds: GameBounds3): GameEntity[];

  /**
   * @zh 获取区域内的所有实体。
   * @en Returns all entities in a rectangular area.
   */
  entitiesInArea(pos1: GameVector3, pos2: GameVector3): GameEntity[];
  entitiesInArea(bounds: GameBounds3): GameEntity[];

  /**
   * @zh 获取半径内的所有实体。
   * @en Returns all entities within a radius.
   */
  entitiesInRadius(x: number, y: number, z: number, radius: number): GameEntity[];
  entitiesInRadius(pos: GameVector3, radius: number): GameEntity[];

  /**
   * @zh 射线检测，返回命中的方块或实体。
   * @en Casts a ray and returns the hit block or entity.
   */
  raycast(origin: GameVector3, direction: GameVector3): object | null;
  raycast(origin: GameVector3, direction: GameVector3, maxDistance: number): object | null;

  /**
   * @zh 获取指定坐标的生物群系。
   * @en Gets the biome at the given coordinates.
   */
  getBiome(x: number, y: number, z: number): string;
  getBiome(pos: GameVector3): string;

  // ── @zh 聊天 & 命令 @en Chat & Command ──

  /** @zh 广播系统消息。 @en Broadcasts a system message to all players. */
  say(message: string): void;

  /** @zh 以控制台身份执行命令。 @en Executes a command as console. */
  runCommand(cmd: string): void;

  // ── @zh 音效属性 @en Sound Properties ──

  /** @zh 环境音效路径 @en Ambient sound resource path. */
  ambientSound: string;

  /** @zh 玩家加入音效 @en Player join sound. */
  playerJoinSound: string;

  /** @zh 玩家离开音效 @en Player leave sound. */
  playerLeaveSound: string;

  /** @zh 放置方块音效 @en Block place sound. */
  placeVoxelSound: string;

  /** @zh 破坏方块音效 @en Block break sound. */
  breakVoxelSound: string;

  /**
   * @zh 播放环境音效 (支持字符串路径或配置对象)。
   * @en Plays a sound (accepts a path string or a config object).
   */
  sound(cfg: string | {
    path?: string;
    position?: GameVector3;
    volume?: number;
    pitch?: number;
  }): void;

  /**
   * @zh 在指定坐标播放音效。
   * @en Plays a sound at the given position for all players.
   */
  playSound(path: string, x: number, y: number, z: number, volume: number, pitch: number): void;
  playSound(path: string, pos: GameVector3, volume: number, pitch: number): void;

  // ── @zh 计分板 @en Scoreboard ──

  /** @zh 创建计分板 (默认 criteria "dummy") @en Creates a scoreboard (default criteria "dummy"). */
  addScoreboard(name: string): void;
  addScoreboard(name: string, criteria: string): void;

  /** @zh 移除计分板 @en Removes a scoreboard. */
  removeScoreboard(name: string): void;

  /**
   * @zh 设置计分。entityOrName 可以是 GameEntity 或玩家名字符串。
   * @en Sets a score. entityOrName can be a GameEntity or player name string.
   */
  setScore(entityOrName: GameEntity | string, objectiveName: string, value: number): void;

  /**
   * @zh 获取计分。
   * @en Gets a score value.
   */
  getScore(entityOrName: GameEntity | string, objectiveName: string): number;

  /** @zh 在指定 slot 显示计分板 @en Shows a scoreboard in the given display slot. */
  showScoreboard(slot: string, objectiveName: string): void;

  /** @zh 隐藏指定 slot 的计分板 @en Hides the scoreboard in the given slot. */
  hideScoreboard(slot: string): void;

  /** @zh 列出计分板所有条目 @en Lists all entries of a scoreboard. */
  listScores(objectiveName: string): Array<{ name: string; score: number }>;

  // ── @zh Boss 条 @en Boss Bar ──

  /**
   * @zh 显示全局 Bossbar。
   * @en Shows a global bossbar.
   */
  showBossbar(name: string, text: string, progress: number, colorName: string): void;

  /**
   * @zh 移除全局 Bossbar。
   * @en Removes a global bossbar.
   */
  removeBossbar(name: string): void;

  // ── @zh 队伍 @en Team ──

  /** @zh 创建队伍 @en Creates a team. */
  createTeam(name: string, colorName: string): void;

  /** @zh 移除队伍 @en Removes a team. */
  removeTeam(name: string): void;

  /** @zh 将实体/玩家加入队伍 @en Adds an entity/player to a team. */
  joinTeam(entityOrName: GameEntity | string, teamName: string): void;

  /** @zh 将实体/玩家移出队伍 @en Removes an entity/player from their team. */
  leaveTeam(entityOrName: GameEntity | string): void;

  /** @zh 获取实体/玩家所在队伍名 @en Gets the team name of an entity/player. */
  getTeamOf(entityOrName: GameEntity | string): string;

  // ── @zh 世界边界 @en World Border ──

  /** @zh 当前边界尺寸 @en Current border size. */
  borderSize: number;

  /** @zh 设置边界中心 @en Sets the border center. */
  setBorderCenter(x: number, z: number): void;

  /**
   * @zh 收缩边界到目标大小。
   * @en Shrinks the border to the target size over time.
   */
  shrinkBorder(targetSize: number, seconds: number): void;

  /** @zh 设置边界伤害 (每方块) @en Sets border damage per block. */
  setBorderDamage(damage: number): void;

  /** @zh 设置边界警告距离 (方块) @en Sets the border warning distance in blocks. */
  setBorderWarning(blocks: number): void;

  // ── @zh 闪电 @en Lightning ──

  /** @zh 在指定坐标生成闪电。 @en Strikes lightning at the given coordinates. */
  strikeLightning(x: number, y: number, z: number): boolean;
  strikeLightning(pos: GameVector3): boolean;
  strikeLightning(x: number, y: number, z: number, damage: number): boolean;
  strikeLightning(pos: GameVector3, damage: number): boolean;

  // ── @zh 弹射物 @en Projectile ──

  /**
   * @zh 发射弹射物。
   * @en Launches a projectile from start to target.
   */
  launchProjectile(
    type: string,
    x: number, y: number, z: number,
    tx: number, ty: number, tz: number,
    speed: number,
  ): GameEntity | null;
  launchProjectile(type: string, pos: GameVector3, target: GameVector3, speed: number): GameEntity | null;

  // ── @zh 烟花 @en Firework ──

  /** @zh 发射烟花 (颜色名/字符串) @en Launches a firework with a color name. */
  launchFirework(x: number, y: number, z: number, color: string, shape: string): void;
  launchFirework(pos: GameVector3, color: string, shape: string): void;
  /** @zh 发射烟花 (GameRGBColor 数组) @en Launches a firework with RGB colors. */
  launchFirework(x: number, y: number, z: number, colors: GameRGBColor[], shape: string): void;
  launchFirework(pos: GameVector3, colors: GameRGBColor[], shape: string): void;

  // ── @zh 粒子 @en Particle ──

  /** @zh 生成粒子效果 @en Spawns particles. */
  spawnParticle(
    type: string,
    x: number, y: number, z: number,
    count: number,
    dx: number, dy: number, dz: number,
    speed: number,
  ): void;
  spawnParticle(type: string, pos: GameVector3, count: number, dx: number, dy: number, dz: number, speed: number): void;
  /** @zh 生成彩色粉尘粒子 @en Spawns colored dust particles. */
  spawnParticle(x: number, y: number, z: number, color: GameRGBColor, count: number, dx: number, dy: number, dz: number, speed: number): void;
  spawnParticle(pos: GameVector3, color: GameRGBColor, count: number, dx: number, dy: number, dz: number, speed: number): void;

  /**
   * @zh 在水平圆环上生成粒子。
   * @en Spawns particles in a horizontal circle.
   */
  spawnParticleCircle(x: number, y: number, z: number, radius: number, type: string, count: number): void;
  spawnParticleCircle(pos: GameVector3, radius: number, type: string, count: number): void;

  // ── @zh 掉落物 @en Drop Item ──

  /** @zh 在指定坐标掉落物品。 @en Drops an item stack at the given position. */
  dropItem(x: number, y: number, z: number, itemId: string, count: number): void;
  dropItem(pos: GameVector3, itemId: string, count: number): void;

  // ── @zh 爆炸 @en Explosion ──

  /** @zh 在指定坐标产生爆炸。 @en Creates an explosion at the given position. */
  explode(x: number, y: number, z: number, power: number): void;
  explode(pos: GameVector3, power: number): void;
  explode(x: number, y: number, z: number, power: number, fire: boolean): void;
  explode(pos: GameVector3, power: number, fire: boolean): void;

  // ── @zh 结构 @en Structure ──

  /**
   * @zh 在指定坐标放置结构。
   * @en Places a structure at the given position.
   */
  placeStructure(x: number, y: number, z: number, structureId: string): void;
  placeStructure(pos: GameVector3, structureId: string): void;

  // ── @zh 成就 @en Advancement ──

  /**
   * @zh 为指定玩家授予成就。
   * @en Grants an advancement to the given player.
   */
  grantAdvancement(playerName: string, advancementId: string): void;

  // ── @zh 配方 @en Recipe ──

  /** @zh 列出匹配过滤器的配方 ID @en Lists recipe IDs matching a filter. */
  listRecipes(filter: string): string[];

  /** @zh 移除配方 @en Removes a recipe by ID. */
  removeRecipe(recipeId: string): boolean;

  /** @zh 清除所有配方 @en Clears all recipes. */
  clearRecipes(): void;

  // ── @zh 跨脚本消息 @en Cross-project Message ──

  /**
   * @zh 向其他项目发送消息。
   * @en Sends a message to another project.
   */
  sendMessage(target: string, data: any): void;

  // ── @zh 事件 @en Events ──

  /**
   * @zh 每 tick 触发 (20/秒)。
   * @en Fired every server tick (20/sec).
   * @param handler - (info: { tick: number; prevTick: number; elapsedTimeMS: number; skip: number }) => void
   */
  onTick(handler: (info: { tick: number; prevTick: number; elapsedTimeMS: number; skip: number }) => void): GameEventHandlerToken;

  /** @zh 玩家加入时触发 @en Fired when a player joins. */
  onPlayerJoin(handler: (entity: GamePlayerEntity, tick: number) => void): GameEventHandlerToken;

  /** @zh 玩家离开时触发 @en Fired when a player leaves. */
  onPlayerLeave(handler: (entity: GamePlayerEntity, tick: number) => void): GameEventHandlerToken;

  /** @zh 方块被破坏时触发 @en Fired when a block is destroyed. */
  onVoxelDestroy(handler: (entity: GameEntity, x: number, y: number, z: number, voxel: string, tick: number) => void): GameEventHandlerToken;

  /** @zh 实体接触方块时触发 @en Fired when an entity touches a block. */
  onVoxelContact(handler: (entity: GameEntity, voxel: number, x: number, y: number, z: number, axis: number, force: number, tick: number) => void): GameEventHandlerToken;

  /** @zh 实体交互时触发 @en Fired when entities interact. */
  onInteract(handler: (entity: GameEntity, target: any, tick: number) => void): GameEventHandlerToken;

  /** @zh 聊天消息时触发 (返回 false 取消广播) @en Fired on chat message (return false to cancel broadcast). */
  onChat(handler: (entity: GamePlayerEntity, message: string, tick: number) => boolean | void): GameEventHandlerToken;

  /** @zh 实体进入流体时触发 @en Fired when an entity enters a fluid. */
  onFluidEnter(handler: (entity: GameEntity, fluid: string, x: number, y: number, z: number, tick: number) => void): GameEventHandlerToken;

  /** @zh 实体离开流体时触发 @en Fired when an entity leaves a fluid. */
  onFluidLeave(handler: (entity: GameEntity, fluid: string, x: number, y: number, z: number, tick: number) => void): GameEventHandlerToken;

  /** @zh 实体接触实体时触发 @en Fired when two entities begin touching. */
  onEntityContact(handler: (entity: GameEntity, other: GameEntity, tick: number) => void): GameEventHandlerToken;

  /** @zh 实体分开时触发 @en Fired when two entities separate. */
  onEntitySeparate(handler: (entity: GameEntity, other: GameEntity, tick: number) => void): GameEventHandlerToken;

  /** @zh 方块被放置时触发 @en Fired when a block is placed. */
  onBlockPlace(handler: (entity: GameEntity, x: number, y: number, z: number, voxel: string, voxelId: number, tick: number) => void): GameEventHandlerToken;

  /** @zh 实体死亡时触发 @en Fired when an entity dies. */
  onEntityDeath(handler: (entity: GameEntity, killer: GameEntity | null, tick: number) => void): GameEventHandlerToken;

  /** @zh 玩家重生时触发 @en Fired when a player respawns. */
  onPlayerRespawn(handler: (entity: GamePlayerEntity, tick: number) => void): GameEventHandlerToken;

  /** @zh 方块被激活时触发 (按钮/拉杆等) @en Fired when a block is activated (button/lever etc.). */
  onBlockActivate(handler: (entity: GameEntity, x: number, y: number, z: number, voxel: string, tick: number) => void): GameEventHandlerToken;

  /** @zh 实体受伤时触发 @en Fired when an entity takes damage. */
  onEntityDamage(handler: (entity: GameEntity, amount: number, source: string, attacker: GameEntity | null, tick: number) => void): GameEventHandlerToken;

  /** @zh 玩家按下按钮时触发 (客户端事件) @en Fired when a player presses a button (client event). */
  onButtonPressed(handler: (entity: GamePlayerEntity, button: string, tick: number) => void): GameEventHandlerToken;

  /** @zh 跨脚本消息接收 @en Receives cross-project messages. */
  onMessage(handler: (from: string, data: any) => void): GameEventHandlerToken;
}
