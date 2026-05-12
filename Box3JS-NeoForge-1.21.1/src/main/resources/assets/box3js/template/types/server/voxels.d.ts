/// <reference path="../shared.d.ts" />

// ── §7 @zh 方块操作 @en Voxels ──

/**
 * @zh 方块读写操作 — 脚本中通过 `voxels` 访问。所有坐标使用世界方块坐标（整数）。
 * @en Voxel (block) read/write — accessed via `voxels` in scripts.
 * All coordinates are in world block space (integers).
 */
interface GameVoxels {
  // ── @zh 世界尺寸 @en World dimensions ──

  /**
   * @zh 世界最大尺寸 (x, y, z 均为世界高度)。
   * @en Maximum world dimensions (x/y/z all equal world height).
   */
  readonly shape: GameVector3;

  /**
   * @zh 所有可用的方块类型名称数组。
   * @en Array of all registered block type resource‑location strings.
   */
  readonly VoxelTypes: string[];

  // ── @zh 名称 ↔ ID 映射 @en Name–ID mapping ──

  /**
   * @zh 将方块名称转为数字 ID。
   * @en Resolves a block name (e.g. "stone" or "minecraft:stone") to its numeric ID.
   * @returns @zh 数字 ID，未知方块的返回 0（air） @en Numeric ID, 0 for unknown blocks (air)
   */
  id(name: string): number;

  /**
   * @zh 将数字 ID 转为方块名称。
   * @en Resolves a numeric ID back to a block name string.
   * @returns @zh ResourceLocation 字符串，未知 ID 返回 "air" @en ResourceLocation string, "air" for unknown IDs
   */
  name(id: number): string;

  // ── @zh 读取 @en Read ──

  /**
   * @zh 获取方块数字 ID (不含旋转信息的基础 ID)。
   * @en Returns the base numeric block ID at the given position (without rotation encoding).
   * @returns @zh 基础方块 ID，空气返回 0 @en base block ID, 0 for air
   */
  getVoxel(x: number, y: number, z: number): number;
  getVoxel(pos: GameVector3): number;

  /**
   * @zh 获取方块数字 ID (不含旋转信息的基础 ID)。
   * @en Returns the base numeric block ID (without rotation encoding).
   */
  getVoxelId(x: number, y: number, z: number): number;
  getVoxelId(pos: GameVector3): number;

  /**
   * @zh 获取方块名称 (如 "minecraft:stone")。
   * @en Returns the block name at the given position (e.g. "minecraft:stone").
   */
  getVoxelName(x: number, y: number, z: number): string;
  getVoxelName(pos: GameVector3): string;

  /**
   * @zh 获取方块旋转值 (0‑3, 对应南/西/北/东)。
   * @en Returns the block rotation: 0=South, 1=West, 2=North, 3=East.
   */
  getVoxelRotation(x: number, y: number, z: number): number;
  getVoxelRotation(pos: GameVector3): number;

  // ── @zh 写入 @en Write ──

  /**
   * @zh 放置方块 (名称或 ID)。返回含旋转编码的完整 ID。
   * @en Places a block by name or ID. Returns the full encoded ID (baseId + rotation * 16384).
   * @param voxel - 方块名称 (如 "minecraft:diamond_block") 或数字 ID
   * @returns @zh 含旋转编码的完整方块 ID，删除/空气返回 0 @en Full encoded block ID (base + rotation * 16384), 0 for remove/air
   */
  setVoxel(x: number, y: number, z: number, voxel: string | number): number;
  setVoxel(pos: GameVector3, voxel: string | number): number;

  /**
   * @zh 放置方块并指定旋转。返回含旋转编码的完整 ID。
   * @en Places a block with explicit rotation.
   * @param voxel - 方块名称或数字 ID
   * @param rotation - 旋转值 0‑3 (或字符串 "0"‑"3")
   * @returns @zh 含旋转编码的完整 ID @en Full encoded block ID (base + rotation * 16384)
   */
  setVoxel(
    x: number,
    y: number,
    z: number,
    voxel: string | number,
    rotation: number | string,
  ): number;
  setVoxel(
    pos: GameVector3,
    voxel: string | number,
    rotation: number | string,
  ): number;

  /**
   * @zh 放置已含旋转编码的完整 ID 方块。
   * @en Places a block using a rotation‑encoded full ID (from getVoxelId).
   * @param voxel - 完整编码 ID (baseId + rotation * 16384)
   */
  setVoxelId(x: number, y: number, z: number, voxel: number): number;
  setVoxelId(pos: GameVector3, voxel: number): number;

  // ── @zh 区域操作 @en Region operations ──

  /**
   * @zh 在两个对角顶点定义的区域内填充方块。
   * @en Fills a cuboid region with a block.
   * @param x1, y1, z1 - 顶点 1
   * @param x2, y2, z2 - 顶点 2
   * @param voxel - 方块名称或 ID
   */
  fillVoxel(
    x1: number,
    y1: number,
    z1: number,
    x2: number,
    y2: number,
    z2: number,
    voxel: string | number,
  ): void;
  fillVoxel(pos1: GameVector3, pos2: GameVector3, voxel: string | number): void;

  /**
   * @zh 统计区域内指定方块的数量。
   * @en Counts matching blocks within a cuboid region.
   */
  countVoxel(
    x1: number,
    y1: number,
    z1: number,
    x2: number,
    y2: number,
    z2: number,
    voxel: string | number,
  ): number;
  countVoxel(
    pos1: GameVector3,
    pos2: GameVector3,
    voxel: string | number,
  ): number;

  // ── @zh 刷怪笼 @en Spawner ──

  /**
   * @zh 设置刷怪笼的生成实体类型。
   * @en Sets the spawner entity type at the given position.
   * @param x, y, z - @zh 刷怪笼坐标 @en spawner coordinates
   * @param entityType - 实体类型 ID (如 "minecraft:zombie")
   */
  setSpawner(x: number, y: number, z: number, entityType: string): void;
  setSpawner(pos: GameVector3, entityType: string): void;
}
