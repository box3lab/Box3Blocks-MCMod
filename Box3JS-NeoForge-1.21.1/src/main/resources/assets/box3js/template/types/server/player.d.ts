/// <reference path="../shared.d.ts" />
/// <reference path="entity.d.ts" />

// ── §5 @zh 玩家 @en Player ──

/**
 * @zh 玩家扩展接口，通过 `entity.player` 访问。
 * @en Player‑specific interface — accessed via `entity.player`.
 */
interface GamePlayer {
  // ── @zh 身份 @en Identity ──

  /** @zh 玩家名 (只读) @en Player display name, readonly. */
  readonly name: string;
  /** @zh 玩家 UUID (与 entity.id 相同, 只读) @en Player UUID (same as entity.id), readonly. */
  readonly userId: string;

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
   * @zh 包围盒半尺寸 (只读)。
   * @en Bounding‑box half‑extents, readonly.
   */
  readonly bounds: GameVector3;

  /**
   * @zh 是否在地面上 (只读)。
   * @en True if the player is standing on a block, readonly.
   */
  readonly onGround: boolean;

  // ── @zh 外观 @en Appearance ──

  /**
   * @zh 是否隐身。
   * @en Whether the player is invisible.
   */
  invisible: boolean;

  /**
   * @zh 模型缩放比例 (MC 原生, 非 Box3 scale)。
   * @en Player model scale (Minecraft native, not Box3 scale).
   */
  readonly scale: number;

  // ── @zh 移动 @en Movement ──

  /** @zh 行走速度 (基础值) @en Walk speed (base attribute value). */
  walkSpeed: number;

  /**
   * @zh 疾跑速度 (≈ walkSpeed × 1.3)。
   * @en Run/sprint speed (≈ walkSpeed × 1.3).
   */
  runSpeed: number;

  /**
   * @zh 跳跃力度。
   * @en Jump power (jump strength attribute).
   */
  jumpPower: number;

  /**
   * @zh 当前移动状态。
   * @en Current movement state.
   * @returns "FLYING" | "GROUND" | "SWIM" | "FALL" | "JUMP"
   */
  readonly moveState: string;

  /**
   * @zh 当前行走状态。
   * @en Current walk state.
   * @returns "NONE" | "CROUCH" | "WALK" | "RUN"
   */
  readonly walkState: string;

  // ── @zh 跳跃 / 潜行 / 游泳 @en Jump / Sneak / Swim ──

  /**
   * @zh 是否允许跳跃 (默认 true, false 时清除跳跃力)。
   * @en Whether jumping is enabled (default true; when false, jump strength is zeroed).
   */
  enableJump: boolean;

  /** @zh 潜行速度 (默认 0.0, MC 下无独立潜行速度) @en Crouch speed (stored as custom prop). */
  crouchSpeed: number;

  /** @zh 游泳速度 (映射到 WATER_MOVEMENT_EFFICIENCY 属性) @en Swim speed (maps to WATER_MOVEMENT_EFFICIENCY attribute). */
  swimSpeed: number;

  // ── @zh 飞行 & 碰撞 @en Flying & Collision ──

  /** @zh 是否允许飞行 @en Whether flight is enabled. */
  canFly: boolean;

  /** @zh 是否正在飞行 @en Whether the player is currently flying. */
  flying: boolean;

  /** @zh 飞行速度 @en Flying speed. */
  flySpeed: number;

  /**
   * @zh 碰撞开关 (通过队伍碰撞规则实现)。
   * @en Collision toggle (implemented via team collision rules).
   */
  collision: boolean;

  /** @zh 是否为观察者模式 @en Whether the player is in spectator mode. */
  readonly spectator: boolean;

  /** @zh 是否禁用飞行 (不允许且自动关闭飞行) @en Whether flying is disabled entirely. */
  disableFly: boolean;

  // ── @zh 游戏模式 @en Game Mode ──

  /**
   * @zh 游戏模式字符串 (如 "survival", "creative", "adventure", "spectator")。
   * @en Game mode as a string (e.g. "survival", "creative", "adventure", "spectator").
   * 也可以接受数字 (0=survival, 1=creative, 2=adventure, 3=spectator)。
   */
  gameMode: string | number;

  /**
   * @zh 当前维度 ID (如 "minecraft:overworld")。
   * @en Current dimension identifier.
   */
  dimension: string;

  // ── @zh 相机 @en Camera ──

  /**
   * @zh 相机模式。
   * @en Camera mode.
   * @default "FPS"
   */
  cameraMode: string;

  /**
   * @zh 相机跟随的实体 (在 FOLLOW 模式下)。
   * @en The entity the camera follows (when in FOLLOW mode).
   */
  cameraEntity: GameEntity | null;

  /** @zh 相机俯仰角 @en Camera pitch (vertical rotation). */
  cameraPitch: number;

  /** @zh 相机偏航角 @en Camera yaw (horizontal rotation). */
  cameraYaw: number;

  /**
   * @zh 玩家面朝方向 (单位向量)。
   * @en Direction the player is facing (unit vector).
   */
  readonly facingDirection: GameVector3;

  /**
   * @zh 玩家视线前方 5 格处的目标点。
   * @en A point 5 blocks ahead of the player's eyes (look‑at target).
   */
  readonly cameraTarget: GameVector3;

  // ── @zh 生命 @en Vital stats ──

  /** @zh 饥饿值 (0‑20) @en Food level (0–20). */
  food: number;

  /** @zh 饱和度 (0‑20) @en Saturation level (0–20). */
  saturation: number;

  /** @zh 当前生命值 @en Current health. */
  hp: number;
  /** @zh 最大生命值 @en Maximum health. */
  maxHp: number;

  // ── @zh 经验 @en Experience ──

  /** @zh 经验等级 (与 /xp 命令相同) @en Experience level (same as /xp command). */
  xp: number;

  /** @zh 增加经验等级 @en Adds experience levels to the player. */
  addExperienceLevels(levels: number): void;

  // ── @zh 传送 @en Teleport ──

  /**
   * @zh 将玩家传送到指定坐标。
   * @en Teleports the player to the given coordinates.
   */
  teleport(pos: GameVector3): void;

  // ── @zh 重生 @en Respawn ──

  /**
   * @zh 是否已死亡。
   * @en Whether the player is dead or dying.
   */
  readonly dead: boolean;

  /**
   * @zh 重生点坐标 (可读写)。
   * @en Spawn point coordinates (readable & writable).
   */
  spawnPoint: GameVector3;

  /**
   * @zh 设置重生点。
   * @en Sets the player's respawn point.
   */
  setRespawnPoint(pos: GameVector3): void;

  /**
   * @zh 强制重生 (仅在死亡状态下有效)。
   * @en Forces a respawn (only works when dead).
   */
  respawn(): void;

  // ── @zh 踢出 @en Kick ──

  /** @zh 踢出玩家 (默认理由 "Kicked") @en Kicks the player with default reason. */
  kick(): void;
  /** @zh 踢出玩家 (自定义理由) @en Kicks the player with a custom reason. */
  kick(reason: string): void;

  // ── @zh 消息 @en Messaging ──

  /**
   * @zh 发送仅该玩家可见的聊天消息。
   * @en Sends a chat message visible only to this player.
   */
  directMessage(msg: string): void;

  /** @zh 发送带颜色的聊天消息。 @en Sends a colored chat message. */
  directMessage(msg: string, color: GameRGBColor): void;

  /**
   * @zh 在动作栏 (快捷栏上方) 显示文字。
   * @en Displays text in the action bar (above the hotbar).
   */
  actionBar(message: string): void;

  /**
   * @zh 显示屏幕标题。
   * @en Displays a screen title.
   * @param title - 主标题
   * @param subtitle - 副标题
   * @param fadeIn - 淡入 tick (可选, 默认 10)
   * @param stay - 停留 tick (可选, 默认 70)
   * @param fadeOut - 淡出 tick (可选, 默认 20)
   */
  title(
    title: string,
    subtitle: string,
    fadeIn?: number,
    stay?: number,
    fadeOut?: number,
  ): void;

  /**
   * @zh 弹出对话面板 (简化版, MC 目前仅发送文本)。
   * @en Shows a dialog panel — simplified; currently just sends text in MC.
   * @param config.content - 对话内容
   * @param config.options - 选项数组
   * @returns @zh 用户选择结果 { index, value } @en User selection result { index, value }
   */
  dialog(config: { content?: string; options?: string[] }): {
    index: number;
    value: string;
  };

  // ── @zh 链接 @en Link ──

  /**
   * @zh 向玩家发送可点击的 URL 链接。
   * @en Sends a clickable URL link to the player.
   */
  link(href: string): void;

  // ── @zh 计分板名称 @en Tab list name ──

  /**
   * @zh 设置玩家在 TAB 列表中的显示名称 (支持颜色代码)。
   * @en Sets the player's display name in the tab list (supports color codes).
   */
  setPlayerListName(name: string): void;

  // ── @zh 朝向 @en Look direction ──

  /**
   * @zh 让玩家看向指定坐标。
   * @en Makes the player look at a point in space.
   */
  lookAt(x: number, y: number, z: number): void;
  lookAt(pos: GameVector3): void;

  // ── @zh 执行命令 @en Command ──

  /**
   * @zh 以玩家身份执行 Minecraft 命令。
   * @en Executes a Minecraft command as this player.
   */
  runCommand(cmd: string): void;

  // ── @zh 物品栏 @en Inventory ──

  /**
   * @zh 给予玩家物品。
   * @en Gives an item to the player.
   *
   * @example
   * @zh ```ts
   * @en player.giveItem("minecraft:diamond", 10);
   * player.giveItem("minecraft:diamond_sword", 1);
   * ```
   *
   * @param itemId - @zh 物品 ID（如 "minecraft:diamond"） @en Item ID (e.g. "minecraft:diamond")
   * @param count - @zh 数量 (1–64) @en Count (1–64)
   */
  giveItem(itemId: string, count: number): void;

  /**
   * @zh 给予玩家附魔物品。
   * @en Gives an enchanted item to the player.
   * @param itemId - 物品 ID
   * @param count - 数量
   * @param enchants - 附魔对象 (如 { "minecraft:sharpness": 5 })
   */
  giveEnchantedItem(
    itemId: string,
    count: number,
    enchants: Record<string, number>,
  ): void;

  /**
   * @zh 给予玩家带自定义名称和描述的命名物品。
   * @en Gives an item with a custom name and lore.
   * @param itemId - 物品 ID
   * @param count - 数量
   * @param customName - 自定义名称
   * @param lore - 描述文字数组
   */
  giveNamedItem(
    itemId: string,
    count: number,
    customName: string,
    lore: string[],
  ): void;

  /**
   * @zh 获取手持物品信息。
   * @en Returns info about the currently held item.
   * @returns { id: string, count: number }
   */
  getHeldItem(): { id: string; count: number };

  /** @zh 清空背包 @en Clears the player's inventory. */
  clearInventory(): void;

  /** @zh 管理员权限等级 (0-4)。0=普通玩家, 4=最高权限 @en Server operator permission level (0–4). */
  opLevel: number;

  // ── @zh 效果 @en Effects ──

  /**
   * @zh 添加状态效果。
   * @en Applies a status effect.
   * @param effectId - 效果 ID (如 "minecraft:speed")
   * @param duration - 持续时间 (tick)
   * @param amplifier - 等级 (0 = 一级)
   * @param hideParticles - 是否隐藏粒子 (可选, 默认 false)
   */
  addEffect(
    effectId: string,
    duration: number,
    amplifier: number,
    hideParticles?: boolean,
  ): void;

  /** @zh 清除所有状态效果 @en Removes all status effects. */
  clearEffects(): void;

  // ── @zh 声音 @en Sound ──

  /**
   * @zh 向该玩家播放声音。
   * @en Plays a sound for this player only.
   * @param path - 声音 ID (如 "minecraft:block.note_block.pling")
   * @param volume - 音量 (0‑1)
   * @param pitch - 音高 (0.5‑2)
   */
  playSound(path: string, volume: number, pitch: number): void;

  // ── @zh 聊天 @en Chat ──

  /**
   * @zh 为该玩家注册聊天处理器 (覆盖全局 onChat)。
   * @en Registers a per‑player chat handler (overrides global onChat for this player).
   * @returns GameEventHandlerToken
   */
  onChat(
    handler: (
      entity: GamePlayerEntity,
      message: string,
      tick: number,
    ) => boolean | void,
  ): GameEventHandlerToken;

  // ── @zh 成就 @en Advancements ──

  /**
   * @zh 授予该玩家一个成就/进度。
   * @en Grants an advancement to this player by resource location (e.g. "minecraft:story/mine_stone").
   */
  grantAdvancement(advancementId: string): void;

  /**
   * @zh 撤销该玩家的一个成就/进度。
   * @en Revokes an advancement from this player.
   */
  revokeAdvancement(advancementId: string): void;

  // ── @zh 客户端 Mod 检测 @en Client Mod Detection ──

  /**
   * @zh 检查该玩家的客户端是否安装了 Box3JS mod。
   * @en Returns true if this player's client has the Box3JS mod installed.
   * @remarks 用于在调用 `remoteChannel.sendClientEvent()` 前检测，避免向未安装的客户端发送。
   *          Use before calling `remoteChannel.sendClientEvent()` to avoid sending to unsupported clients.
   */
  hasBox3JSClientMod(): boolean;
}
