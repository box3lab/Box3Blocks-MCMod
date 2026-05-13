/// <reference path="../shared.d.ts" />
/// <reference path="entity.d.ts" />

// ── §6 @zh 世界 API @en World ──

/**
 * @zh 世界控制与事件 — 脚本中通过 `world` 访问。
 * @en World control & events — accessed via `world` in scripts.
 */
interface GameWorld {
  // ── @zh 世界属性 @en World properties ──

  /** @zh 当前脚本项目名称 (只读方法) @en Current script project name, readonly method. */
  projectName(): string;

  /** @zh 服务器 MOTD/标识符 (可读写) @en Server MOTD/identifier, read/write. */
  serverId: string;

  /** @zh 当前服务端 tick 计数 (只读方法) @en Current server tick count, readonly method. */
  currentTick(): number;

  /**
   * @zh 降雨强度 (0‑1)。
   * @en Rain density (0–1).
   */
  rainDensity: number;

  /**
   * @zh 雷暴强度 (0‑1)。
   * @en Thunder density (0–1).
   */
  thunderDensity: number;

  /** @zh 清除天气 (晴天) @en Clears weather to clear skies. */
  clearWeather(): void;

  // ── @zh 时间 @en Time ──

  /**
   * @zh 当前游戏内时间 (tick, 0‑24000)。
   * @en Current in‑game time in ticks (0–24000).
   */
  time: number;

  /**
   * @zh 时间流速 (1=正常, 0=停止)。
   * @en Time scale (1 = normal, 0 = frozen).
   */
  timeScale: number;

  /**
   * @zh 设置游戏内时间 (tick, 0‑24000)。
   * @en Sets the in-game time in ticks.
   * @param time - 0=黎明, 6000=正午, 12000=黄昏, 18000=午夜
   */
  setTime(time: number): void;

  // ── @zh 难度 @en Difficulty ──

  /**
   * @zh 当前难度。
   * @en Current difficulty ("peaceful" | "easy" | "normal" | "hard").
   */
  difficulty: string;

  // ── @zh 出生点 @en Spawn ──

  /**
   * @zh 世界出生点坐标。
   * @en World spawn point coordinates.
   */
  readonly spawnPoint: GameVector3;

  /**
   * @zh 设置世界出生点。
   * @en Sets the world spawn point.
   */
  setWorldSpawn(pos: GameVector3): void;

  // ── @zh 游戏规则 (MC 扩展) @en Game Rules (MC extension) ──

  /**
   * @zh 读取游戏规则。
   * @en Reads a game‑rule value.
   * @param name - @zh 规则名 @en rule name (see setGameRule for the list)
   */
  getGameRule(name: string): boolean | null;

  /**
   * @zh 设置游戏规则。
   * @en Sets a game rule.
   * @param name - supported: doDaylightCycle | doWeatherCycle | keepInventory |
   *               doMobSpawning | doFireTick | mobGriefing | doImmediateRespawn
   * @param value - boolean or string "true"/"false"
   */
  setGameRule(name: string, value: boolean | string): void;

  // ── @zh 音效属性 @en Sound Properties ──

  /** @zh 环境音效路径 (每 200 tick 在世界出生点自动播放, 0.3 音量) @en Ambient sound — auto-plays at world spawn every 200 ticks at 0.3 volume. */
  ambientSound: string;

  /** @zh 玩家加入音效路径 (玩家加入时自动播放) @en Player join sound — auto-plays when a player joins. */
  playerJoinSound: string;

  /** @zh 玩家离开音效路径 (玩家离开时自动播放) @en Player leave sound — auto-plays when a player leaves. */
  playerLeaveSound: string;

  /** @zh 方块放置音效路径 (放置方块时自动播放) @en Block place sound — auto-plays when a block is placed. */
  placeVoxelSound: string;

  /** @zh 方块破坏音效路径 (破坏方块时自动播放) @en Block break sound — auto-plays when a block is broken. */
  breakVoxelSound: string;

  // ── @zh 实体生成 @en Entity Spawning ──

  /**
   * @zh 在指定位置生成实体。
   * @en Spawns an entity at the given position.
   * @param type - 实体类型 ID (如 "minecraft:zombie")
   * @param pos - 生成坐标
   * @returns @zh 生成的实体包装，失败返回 null @en The spawned entity wrapper, or null on failure
   */
  spawnEntity(type: string, pos: GameVector3): GameEntity | null;

  /**
   * @zh 使用完整配置对象生成实体。
   * @en Spawns an entity with a full configuration object.
   *
   * @example
   * @zh ```ts
   * @en // 生成一个固定在空中的发光僵尸
   * const entity = world.createEntity({
   *   type: "minecraft:zombie",
   *   position: new GameVector3(100, 70, 100),
   *   fixed: true,
   *   hp: 40,
   *   maxHp: 40,
   *   tags: ["boss"],
   * });
   * ```
   *
   * @param config - @zh 实体配置对象 @en entity configuration object
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

  // ── @zh 消息 & 声音 @en Broadcasting ──

  /**
   * @zh 向全服广播消息。
   * @en Sends a chat message to all players.
   */
  say(message: string): void;

  // ── @zh 结构 & 成就 @en Structure & Advancement ──

  /**
   * @zh 在指定位置放置数据包中的 .nbt 结构。
   * @en Places an .nbt structure from current datapacks at the given position.
   * Structure must exist under data/<namespace>/structure/<id>.nbt
   */
  placeStructure(x: number, y: number, z: number, structureId: string): void;
  placeStructure(pos: GameVector3, structureId: string): void;

  /**
   * @zh 为指定玩家授予成就/进度。
   * @en Grants a datapack advancement to a player by name.
   */
  grantAdvancement(playerName: string, advancementId: string): void;

  /**
   * @zh 按物品名搜索配方 ID 列表。
   * @en Searches recipe IDs matching a filter string.
   * @param filter - 搜索关键词 (匹配配方 ID)
   */
  listRecipes(filter: string): string[];

  /**
   * @zh 移除指定 ID 的配方 (黑名单机制, 服务器重载后需重新移除)。
   * @en Removes a recipe by ID (blacklisted; re‑apply after server reload).
   * @param recipeId - 配方 ID, 例如 "minecraft:iron_pickaxe"
   * @returns @zh 是否成功加入黑名单 @en Whether the recipe was successfully blacklisted
   */
  removeRecipe(recipeId: string): boolean;

  /**
   * @zh 清除所有配方黑名单, 恢复全部原始配方。
   * @en Clears the recipe blacklist and restores all original recipes.
   */
  clearRecipes(): void;

  /**
   * @zh 在指定位置向全服播放声音。
   * @en Plays a sound for all players at a location.
   * @param path - 声音 ID
   * @param x, y, z - 声源坐标
   * @param volume - 音量 (0‑1)
   * @param pitch - 音高 (0.5‑2)
   */
  playSound(
    path: string,
    x: number,
    y: number,
    z: number,
    volume: number,
    pitch: number,
  ): void;
  playSound(
    path: string,
    pos: GameVector3,
    volume: number,
    pitch: number,
  ): void;

  // ── @zh 命令 @en Command ──

  /**
   * @zh 以服务端身份执行命令。
   * @en Executes a Minecraft command as the server.
   */
  runCommand(cmd: string): void;

  // ── @zh 实体查询 @en Entity Queries ──

  /**
   * @zh 查询所有匹配选择器的实体 (目前仅限玩家)。
   * @en Selects all entities matching a selector (currently only players).
   * @param selector - "*" (所有玩家) | "#uuid" | ".tag"
   */
  querySelectorAll(selector: string): GameEntity[];

  /**
   * @zh 查询第一个匹配的实体 (或 null)。
   * @en Selects the first matching entity, or null.
   */
  querySelector(selector: string): GameEntity | null;

  /**
   * @zh 查询指定区域内的所有实体。
   * @en Returns all entities inside an AABB defined by two corners.
   */
  entitiesInArea(pos1: GameVector3, pos2: GameVector3): GameEntity[];

  /**
   * @zh 查询指定半径内的所有实体。
   * @en Returns all entities within a radius around a point.
   */
  entitiesInRadius(
    x: number,
    y: number,
    z: number,
    radius: number,
  ): GameEntity[];
  entitiesInRadius(pos: GameVector3, radius: number): GameEntity[];

  // ── @zh 搜索与音效 @en Search & Sound ──

  /**
   * @zh 播放音效 (简写或完整配置)。
   * @en Plays a sound (string shorthand or full config object).
   * @param config - 音效路径字符串 或 { path, position, volume, pitch }
   */
  sound(
    config:
      | string
      | {
          path: string;
          position?: GameVector3;
          volume?: number;
          pitch?: number;
        },
  ): void;

  /**
   * @zh 查询包围盒内的所有实体。
   * @en Returns all entities inside a GameBounds3.
   */
  searchBox(bounds: GameBounds3): GameEntity[];

  // ── @zh 射线检测 @en Raycast ──

  /**
   * @zh 从起点向指定方向发射射线，返回碰撞结果。
   * @en Casts a ray and returns hit information.
   *
   * @example
   * @zh ```ts
   * @en // 检测玩家视线前方 10 格内是否有方块或实体
   * const hit = world.raycast(player.eyePosition, player.facingDirection, 10);
   * if (hit.hit) {
   *   if (hit.entity) {
   *     world.say(`命中实体: ${hit.entity.entityType}`);
   *   } else if (hit.voxel !== undefined) {
   *     world.say(`命中方块: ${voxels.name(hit.voxel)}`);
   *   }
   * }
   * ```
   *
   * @param origin - @zh 起点 @en ray origin
   * @param direction - @zh 方向向量（自动归一化） @en direction vector (auto-normalized)
   * @param maxDistance - @zh 最大距离（可选，默认 5） @en max distance (optional, default 5)
   * @returns @zh 碰撞结果 @en hit result
   */
  raycast(
    origin: GameVector3,
    direction: GameVector3,
    maxDistance?: number,
  ): RaycastResult;

  // ── @zh 生物群系 @en Biome ──

  /**
   * @zh 获取指定位置的生物群系 ID。
   * @en Returns the biome identifier at the given position.
   */
  getBiome(x: number, y: number, z: number): string;
  getBiome(pos: GameVector3): string;

  // ── @zh 爆炸 @en Explosion ──

  /**
   * @zh 在指定位置制造爆炸。
   * @en Creates an explosion at the given position.
   * @param x, y, z - 爆炸中心
   * @param power - 爆炸强度
   * @param fire - 是否产生火焰 (可选, 默认 false)
   */
  explode(x: number, y: number, z: number, power: number, fire?: boolean): void;
  explode(pos: GameVector3, power: number, fire?: boolean): void;

  // ── @zh 粒子 @en Particles ──

  /**
   * @zh 在指定位置生成粒子。
   * @en Spawns particles at a given location.
   *
   * @example
   * @zh ```ts
   * @en // 在玩家位置生成火焰粒子
   * world.spawnParticle("minecraft:flame", player.position, 10, 0.5, 0.5, 0.5, 0);
   *
   * // 在指定坐标生成末影粒子
   * world.spawnParticle("minecraft:portal", 100, 64, 100, 20, 1, 1, 1, 0.1);
   * ```
   *
   * @param type - @zh 粒子 ID（如 "minecraft:flame"） @en Particle ID (e.g. "minecraft:flame")
   * @param x - @zh X 坐标 @en X coordinate
   * @param y - @zh Y 坐标 @en Y coordinate
   * @param z - @zh Z 坐标 @en Z coordinate
   * @param count - @zh 数量 @en Count
   * @param dx - @zh X 扩散范围 @en X spread
   * @param dy - @zh Y 扩散范围 @en Y spread
   * @param dz - @zh Z 扩散范围 @en Z spread
   * @param speed - @zh 粒子速度 @en Particle speed
   */
  spawnParticle(
    type: string,
    x: number,
    y: number,
    z: number,
    count: number,
    dx: number,
    dy: number,
    dz: number,
    speed: number,
  ): void;
  /** @zh GameVector3 重载。 @en GameVector3 overload. */
  spawnParticle(
    type: string,
    pos: GameVector3,
    count: number,
    dx: number,
    dy: number,
    dz: number,
    speed: number,
  ): void;

  /** @zh 彩色粒子 (DustParticleOptions)。 @en Colored dust particle. */
  spawnParticle(
    x: number,
    y: number,
    z: number,
    color: GameRGBColor,
    count: number,
    dx: number,
    dy: number,
    dz: number,
    speed: number,
  ): void;
  /** @zh 彩色粒子，GameVector3 重载。 @en Colored dust particle, GameVector3 overload. */
  spawnParticle(
    pos: GameVector3,
    color: GameRGBColor,
    count: number,
    dx: number,
    dy: number,
    dz: number,
    speed: number,
  ): void;

  /**
   * @zh 在指定圆环上生成粒子。
   * @en Spawns particles in a circle.
   * @param x, y, z - 圆心
   * @param radius - 半径
   * @param type - 粒子 ID
   * @param count - 数量
   */
  spawnParticleCircle(
    x: number,
    y: number,
    z: number,
    radius: number,
    type: string,
    count: number,
  ): void;
  spawnParticleCircle(
    pos: GameVector3,
    radius: number,
    type: string,
    count: number,
  ): void;

  // ── @zh 烟花 @en Fireworks ──

  /**
   * @zh 在指定位置发射烟花。
   * @en Launches a firework rocket.
   * @param x, y, z - 发射位置
   * @param color - 颜色名称: "red" | "blue" | "green" | "yellow" | "gold" | "white" | "aqua" | "pink" | "purple"
   * @param shape - 形状: "ball" | "large_ball" | "star" | "creeper" | "burst"
   */
  launchFirework(
    x: number,
    y: number,
    z: number,
    color: string,
    shape: string,
  ): void;
  launchFirework(pos: GameVector3, color: string, shape: string): void;

  /** @zh 彩色烟花，GameRGBColor 数组。 @en Colored firework with GameRGBColor array. */
  launchFirework(
    x: number,
    y: number,
    z: number,
    colors: GameRGBColor[],
    shape: string,
  ): void;
  /** @zh 彩色烟花，GameVector3 + GameRGBColor[] 重载。 @en Colored firework, GameVector3 overload. */
  launchFirework(pos: GameVector3, colors: GameRGBColor[], shape: string): void;

  // ── @zh 闪电 @en Lightning ──

  /**
   * @zh 在指定位置召唤闪电。
   * @en Summons a lightning bolt at the given position.
   * @param x, y, z - 位置
   * @param damage - 伤害值 (可选, 仅对实体造成)
   * @returns @zh 是否成功 @en Whether the lightning was successfully summoned
   */
  strikeLightning(x: number, y: number, z: number, damage?: number): boolean;
  strikeLightning(pos: GameVector3, damage?: number): boolean;

  // ── @zh 掉落物 @en Drop Item ──

  /**
   * @zh 在指定位置生成掉落物。
   * @en Drops an item stack at the given position.
   * @param x, y, z - 位置
   * @param itemId - 物品 ID
   * @param count - 数量
   */
  dropItem(
    x: number,
    y: number,
    z: number,
    itemId: string,
    count: number,
  ): void;
  dropItem(pos: GameVector3, itemId: string, count: number): void;

  // ── @zh 弹射物 @en Projectile ──

  /**
   * @zh 从起点向目标发射弹射物。
   * @en Launches a projectile from origin toward a target.
   * @param type - 弹射物类型 (如 "minecraft:arrow")
   * @param x, y, z - 发射位置
   * @param tx, ty, tz - 目标位置
   * @param speed - 速度
   * @returns @zh 弹射物实体，失败返回 null @en The projectile entity, or null on failure
   */
  launchProjectile(
    type: string,
    x: number,
    y: number,
    z: number,
    tx: number,
    ty: number,
    tz: number,
    speed: number,
  ): GameEntity | null;
  launchProjectile(
    type: string,
    pos: GameVector3,
    target: GameVector3,
    speed: number,
  ): GameEntity | null;

  // ── @zh 计分板 @en Scoreboard ──

  /**
   * @zh 添加计分板目标 (默认 dummy 标准)。
   * @en Adds a scoreboard objective (default dummy criteria).
   */
  addScoreboard(name: string): void;

  /**
   * @zh 添加计分板目标 (自定义标准)。
   * @en Adds a scoreboard objective with a custom criteria.
   */
  addScoreboard(name: string, criteria: string): void;

  /** @zh 移除计分板目标 @en Removes a scoreboard objective. */
  removeScoreboard(name: string): void;

  /**
   * @zh 设置实体/名称的分数。
   * @en Sets the score of an entity or name for a given objective.
   */
  setScore(
    entityOrName: string | GameEntity,
    objectiveName: string,
    value: number,
  ): void;

  /**
   * @zh 获取分数。
   * @en Gets the score of an entity or name for a given objective.
   */
  getScore(entityOrName: string | GameEntity, objectiveName: string): number;

  /**
   * @zh 在指定显示位置展示计分板。
   * @en Displays a scoreboard objective in a display slot.
   * @param slot - "sidebar" | "list" | "belowname"
   */
  showScoreboard(slot: string, objectiveName: string): void;

  /**
   * @zh 从显示位置隐藏计分板。
   * @en Hides a scoreboard from a display slot.
   */
  hideScoreboard(slot: string): void;

  /**
   * @zh 列出计分板上所有玩家的分数。
   * @en Lists all player scores for a given objective.
   * @returns Array<{ name: string, value: number }>
   */
  listScores(objectiveName: string): Array<{ name: string; value: number }>;

  // ── @zh Boss 血条 @en Boss Bar ──

  /**
   * @zh 显示或更新 Boss 血条。
   * @en Shows or updates a boss bar.
   * @param name - 血条 ID
   * @param text - 显示文字
   * @param progress - 进度 (0‑1)
   * @param color - 颜色: "red" | "blue" | "green" | "yellow" | "purple" | "pink" | "white"
   */
  showBossbar(
    name: string,
    text: string,
    progress: number,
    color: string,
  ): void;

  /** @zh 移除 Boss 血条 @en Removes a boss bar by ID. */
  removeBossbar(name: string): void;

  // ── @zh 队伍 @en Teams ──

  /**
   * @zh 创建一个队伍。
   * @en Creates a scoreboard team.
   * @param name - 队伍名
   * @param color - 颜色 (如 "aqua", "red", "blue" 等)
   */
  createTeam(name: string, color: string): void;

  /** @zh 删除队伍 @en Removes a team. */
  removeTeam(name: string): void;

  /**
   * @zh 将实体/名称加入队伍。
   * @en Adds an entity or name to a team.
   */
  joinTeam(entityOrName: string | GameEntity, teamName: string): void;

  /**
   * @zh 将实体/名称移出队伍。
   * @en Removes an entity or name from its current team.
   */
  leaveTeam(entityOrName: string | GameEntity): void;

  /**
   * @zh 获取实体/名称所在的队伍名 (不在任何队伍返回 null)。
   * @en Returns the team name of an entity or name, or null.
   */
  getTeamOf(entityOrName: string | GameEntity): string | null;

  // ── @zh 世界边界 @en World Border ──

  /** @zh 当前边界大小 @en Current world border size. */
  borderSize: number;

  /**
   * @zh 设置边界中心。
   * @en Sets the world border center.
   */
  setBorderCenter(x: number, z: number): void;

  /**
   * @zh 缩放边界到目标大小 (带动画)。
   * @en Shrinks/grows the world border to a target size over time.
   * @param targetSize - 目标大小
   * @param seconds - 动画秒数
   */
  shrinkBorder(targetSize: number, seconds: number): void;

  /**
   * @zh 边界伤害 (每秒造成的伤害值)。
   * @en World border damage per block per second.
   */
  setBorderDamage(damage: number): void;

  /**
   * @zh 边界警告距离 (方块数)。
   * @en World border warning distance in blocks.
   */
  setBorderWarning(blocks: number): void;

  // ── @zh 定时器 @en Timers ──

  /**
   * @zh 设置一次性延时回调。
   * @en Schedules a one‑shot delayed callback.
   * @param handler - 回调函数
   * @param ticks - 延迟 tick 数
   * @returns @zh 定时器 ID（可用于 clearTimeout） @en Timer ID (can be used with clearTimeout)
   */
  setTimeout(handler: () => void, ticks: number): number;

  /**
   * @zh 设置循环定时回调。
   * @en Schedules a recurring interval callback.
   * @param handler - 回调函数
   * @param ticks - 间隔 tick 数
   * @returns @zh 定时器 ID（可用于 clearInterval） @en Timer ID (can be used with clearInterval)
   */
  setInterval(handler: () => void, ticks: number): number;

  /** @zh 取消 setTimeout @en Clears a timeout by ID. */
  clearTimeout(id: number): void;

  /** @zh 取消 setInterval @en Clears an interval by ID. */
  clearInterval(id: number): void;

  // ── @zh 项目间消息 @en Cross‑project Messaging ──

  /**
   * @zh 向另一个项目发送消息。
   * @en Sends a message to another script project.
   * @param target - 目标项目名 (不含路径)
   * @param data - 数据 (任意 JSON 可序列化的值)
   */
  sendMessage(target: string, data: unknown): void;

  // ── @zh 事件注册 @en Event Registration ──
  // @zh 所有 onXxx() 返回 GameEventHandlerToken, 调用 .cancel() 取消监听。 @en All onXxx() return GameEventHandlerToken; call .cancel() to unregister.

  /**
   * @zh 注册每 tick 回调 (每秒 20 次)。
   * @en Registers a callback invoked every tick (20 times/sec).
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onTick(handler: (info: TickInfo) => void): GameEventHandlerToken;

  /**
   * @zh 注册玩家加入回调。
   * @en Registers a callback invoked when a player joins the server.
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onPlayerJoin(
    handler: (entity: GamePlayerEntity, tick: number) => void,
  ): GameEventHandlerToken;

  /**
   * @zh 注册玩家离开回调。
   * @en Registers a callback invoked when a player leaves the server.
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onPlayerLeave(
    handler: (entity: GamePlayerEntity, tick: number) => void,
  ): GameEventHandlerToken;

  /**
   * @zh 注册聊天消息回调 (包括 /me 消息)。
   * @en Registers a callback for chat messages (including /me).
   * @param handler - (entity, message, tick) => boolean|void
   *                 返回 false 可取消聊天消息发送。
   *                 Return false to cancel sending this chat message.
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onChat(
    handler: (
      entity: GamePlayerEntity,
      message: string,
      tick: number,
    ) => boolean | void,
  ): GameEventHandlerToken;

  /**
   * @zh 注册玩家重生回调。
   * @en Registers a callback invoked when a player respawns.
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onPlayerRespawn(
    handler: (entity: GamePlayerEntity, tick: number) => void,
  ): GameEventHandlerToken;

  /**
   * @zh 注册方块右键激活回调。
   * @en Registers a callback invoked when a player right‑clicks a block.
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onBlockActivate(
    handler: (
      entity: GamePlayerEntity,
      x: number,
      y: number,
      z: number,
      voxel: string,
      tick: number,
    ) => void,
  ): GameEventHandlerToken;

  /**
   * @zh 注册方块破坏回调。
   * @en Registers a callback invoked when a player breaks a block.
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onVoxelDestroy(
    handler: (
      entity: GamePlayerEntity,
      x: number,
      y: number,
      z: number,
      voxel: string,
      tick: number,
    ) => void,
  ): GameEventHandlerToken;

  /**
   * @zh 注册方块放置回调。
   * @en Registers a callback invoked when a player places a block.
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onBlockPlace(
    handler: (
      entity: GamePlayerEntity,
      x: number,
      y: number,
      z: number,
      voxel: string,
      voxelId: number,
      tick: number,
    ) => void,
  ): GameEventHandlerToken;

  /**
   * @zh 注册方块接触回调 (玩家移动到新方块时触发)。
   * @en Registers a callback invoked when a player's block position changes.
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onVoxelContact(
    handler: (
      entity: GamePlayerEntity,
      voxelId: number,
      x: number,
      y: number,
      z: number,
      contactType: number,
      force: number,
      tick: number,
    ) => void,
  ): GameEventHandlerToken;

  /**
   * @zh 注册实体交互回调 (玩家右键实体)。
   * @en Registers a callback invoked when a player right‑clicks an entity.
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onInteract(
    handler: (
      entity: GamePlayerEntity,
      target: GameEntity,
      tick: number,
    ) => void,
  ): GameEventHandlerToken;

  /**
   * @zh 注册实体死亡回调。
   * @en Registers a callback invoked when an entity dies.
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onEntityDeath(
    handler: (
      entity: GameEntity,
      killer: GameEntity | null,
      tick: number,
    ) => void,
  ): GameEventHandlerToken;

  /**
   * @zh 注册实体受伤回调。
   * @en Registers a callback invoked when an entity takes damage.
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onEntityDamage(
    handler: (
      entity: GameEntity,
      amount: number,
      source: string,
      attacker: GameEntity | null,
      tick: number,
    ) => void,
  ): GameEventHandlerToken;

  /**
   * @zh 注册流体进入回调 (玩家进入水/熔岩)。
   * @en Registers a callback invoked when a player enters a fluid.
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onFluidEnter(
    handler: (
      entity: GamePlayerEntity,
      fluid: string,
      x: number,
      y: number,
      z: number,
      tick: number,
    ) => void,
  ): GameEventHandlerToken;

  /**
   * @zh 注册流体离开回调 (玩家离开水/熔岩)。
   * @en Registers a callback invoked when a player leaves a fluid.
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onFluidLeave(
    handler: (
      entity: GamePlayerEntity,
      fluid: string,
      x: number,
      y: number,
      z: number,
      tick: number,
    ) => void,
  ): GameEventHandlerToken;

  /**
   * @zh 注册实体接触回调 (两个实体碰撞)。
   * @en Registers a callback invoked when two entities come into contact.
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onEntityContact(
    handler: (entityA: GameEntity, entityB: GameEntity, tick: number) => void,
  ): GameEventHandlerToken;

  /**
   * @zh 注册实体分离回调 (两个实体不再碰撞)。
   * @en Registers a callback invoked when two entities separate after contact.
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onEntitySeparate(
    handler: (entityA: GameEntity, entityB: GameEntity, tick: number) => void,
  ): GameEventHandlerToken;

  /**
   * @zh 注册按钮按下回调 — 当玩家按下指定按钮时触发。
   * @en Registers a callback for button presses from any player.
   * @param handler — `(entity, button, tick) => void`
   *
   * `button` 参数值是 {@link GameButtonType} 中的字符串常量之一：
   * WALK / RUN / CROUCH / JUMP / FLY / ACTION0 / ACTION1
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onButtonPressed(
    handler: (entity: GamePlayerEntity, button: string, tick: number) => void,
  ): GameEventHandlerToken;

  /**
   * @zh 注册跨项目消息回调。
   * @en Registers a callback for messages from other script projects.
   * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to unsubscribe
   */
  onMessage(
    handler: (sender: string, data: unknown) => void,
  ): GameEventHandlerToken;
}

/**
 * @zh `world.raycast()` 返回结果。
 * @en Return type of `world.raycast()`.
 */
interface RaycastResult {
  /** @zh 是否命中 @en True if something was hit. */
  hit: boolean;
  /** @zh 命中点 X 坐标 @en Hit point X coordinate. */
  x: number;
  /** @zh 命中点 Y 坐标 @en Hit point Y coordinate. */
  y: number;
  /** @zh 命中点 Z 坐标 @en Hit point Z coordinate. */
  z: number;
  /** @zh 表面法线 X 分量 @en Surface normal X component. */
  normalX: number;
  /** @zh 表面法线 Y 分量 @en Surface normal Y component. */
  normalY: number;
  /** @zh 表面法线 Z 分量 @en Surface normal Z component. */
  normalZ: number;
  /** @zh 命中距离 @en Distance from origin to hit point. */
  distance: number;
  /** @zh 命中的方块 ID (命中方块时为数字) @en Hit block ID (number when a block was hit). */
  voxel?: number;
  /** @zh 命中的实体 (命中实体时) @en The entity that was hit (when an entity was hit). */
  entity?: GameEntity;
}
