// ================================================================
//  §1  Math Types — 数学类型
// ================================================================

/**
 * 三维向量
 * A 3‑dimensional vector with double‑precision components.
 *
 * @remarks
 * 所有坐标使用世界坐标 (方块坐标, 非像素)。
 * All coordinates are in world space (block coordinates, not pixels).
 */
declare class GameVector3 {
  /** X 分量 — X component (east‑west) */
  x: number;
  /** Y 分量 — Y component (up‑down) */
  y: number;
  /** Z 分量 — Z component (north‑south) */
  z: number;

  /**
   * 创建一个零向量 (0, 0, 0)。
   * Creates a zero vector at origin.
   */
  constructor();

  /**
   * 创建一个指定坐标的向量。
   * Creates a vector with the given coordinates.
   * @param x - X 坐标 / X coordinate
   * @param y - Y 坐标 / Y coordinate
   * @param z - Z 坐标 / Z coordinate
   */
  constructor(x: number, y: number, z: number);

  /**
   * 设置向量的 X / Y / Z 分量 (会改变调用者自身)。
   * Sets all three components in‑place (mutates the vector).
   * @returns 调用者本身 / this vector
   */
  set(x: number, y: number, z: number): GameVector3;

  /** 原地复制 v 的值。Copies values from v in‑place. */
  copy(v: GameVector3): GameVector3;

  /** 深拷贝。Returns a new independent copy. */
  clone(): GameVector3;

  /**
   * 向量加法: this + v。
   * Vector addition: this + v.
   * @returns 一个新向量 / a new vector
   */
  add(v: GameVector3): GameVector3;

  /**
   * 向量减法: this - v。
   * Vector subtraction: this - v.
   * @returns 一个新向量 / a new vector
   */
  sub(v: GameVector3): GameVector3;

  /** 逐分量乘法 (返回新对象)。Component‑wise multiplication (returns new vector). */
  mul(v: GameVector3): GameVector3;

  /** 逐分量除法 (返回新对象, 除以 0 得 0)。Component‑wise division (divide‑by‑zero → 0). */
  div(v: GameVector3): GameVector3;

  /**
   * 标量乘法: 每个分量乘以 n。
   * Scalar multiplication: each component multiplied by n.
   * @returns 一个新向量 / a new vector
   */
  scale(n: number): GameVector3;

  /** 原地加法。Addition in‑place. */
  addEq(v: GameVector3): GameVector3;

  /** 原地减法。Subtraction in‑place. */
  subEq(v: GameVector3): GameVector3;

  /** 原地乘法。Multiplication in‑place. */
  mulEq(v: GameVector3): GameVector3;

  /** 原地除法 (除以 0 跳过该分量)。Division in‑place (divide‑by‑zero skips that component). */
  divEq(v: GameVector3): GameVector3;

  /**
   * 点积 (内积): this · v。
   * Dot (inner) product: this · v.
   */
  dot(v: GameVector3): number;

  /** 叉积: this × v。Cross product. */
  cross(v: GameVector3): GameVector3;

  /**
   * 向量长度 (模)。
   * Magnitude (length) of this vector.
   */
  mag(): number;

  /**
   * 向量长度的平方 (比 mag() 更快)。
   * Squared magnitude — faster than mag() when comparing distances.
   */
  sqrMag(): number;

  /**
   * 单位化: 返回方向相同、长度为 1 的新向量。
   * Normalizes this vector; returns a unit vector in the same direction.
   * 零向量会返回 (0,0,0)。
   */
  normalize(): GameVector3;

  /**
   * 计算 this 与 v 之间的欧几里得距离。
   * Euclidean distance between this and v.
   */
  distance(v: GameVector3): number;

  /**
   * 线性插值: 在 this 和 v 之间按比率 n 插值。
   * Linear interpolation between this and v by ratio n.
   * @param n - 插值比率 (0=this, 1=v) / interpolation factor
   */
  lerp(v: GameVector3, n: number): GameVector3;

  /**
   * 指向 v 的方向向量 (已单位化)。
   * Direction vector pointing toward v (normalized).
   */
  towards(v: GameVector3): GameVector3;

  /**
   * this 与 v 之间的夹角 (弧度)。
   * Angle between this and v in radians.
   */
  angle(v: GameVector3): number;

  /**
   * 近似相等检查 (容差 1e‑6)。
   * Approximate equality within 1e‑6 tolerance.
   */
  equals(v: GameVector3): boolean;

  /** 精确相等检查 (分量完全相等)。Exact component‑wise equality. */
  exactEquals(v: GameVector3): boolean;

  /** 逐分量取较大值 (返回新对象)。Component‑wise max. */
  max(v: GameVector3): GameVector3;

  /** 逐分量取较小值 (返回新对象)。Component‑wise min. */
  min(v: GameVector3): GameVector3;

  /**
   * 从球坐标创建向量。
   * Creates a vector from spherical coordinates.
   * @param mag - 半径 / radius (magnitude)
   * @param phi - 方位角 / azimuth angle (radians, horizontal rotation around Y)
   * @param theta - 仰角 / elevation angle (radians, from horizontal plane)
   */
  static fromPolar(mag: number, phi: number, theta: number): GameVector3;

  /** 返回 "(x, y, z)" 格式的字符串表示。 */
  toString(): string;
}

// ──────────────────────────────────────────────

/**
 * 三维轴对齐包围盒 (AABB)。
 * Axis‑aligned 3‑dimensional bounding box.
 *
 * @remarks
 * 由两个对角顶点 lo (最小角) 和 hi (最大角) 定义。
 * Defined by two opposing corners: lo (minimum corner) and hi (maximum corner).
 */
declare class GameBounds3 {
  /** 最小角 (三个分量均为最小值)。Lower/minimum corner. */
  lo: GameVector3;
  /** 最大角 (三个分量均为最大值)。Upper/maximum corner. */
  hi: GameVector3;

  /**
   * 用两个对角顶点构造包围盒。
   * Constructs bounds from two opposing corners.
   */
  constructor(lo: GameVector3, hi: GameVector3);

  /** 原地设置所有边界。Sets all boundaries in‑place. */
  set(
    lox: number,
    loy: number,
    loz: number,
    hix: number,
    hiy: number,
    hiz: number,
  ): GameBounds3;

  /** 原地复制 b 的值。Copies values from b in‑place. */
  copy(b: GameBounds3): GameBounds3;

  /**
   * 判断当前包围盒是否与 other 相交。
   * Returns true if this bounds intersects with other.
   */
  intersects(other: GameBounds3): boolean;

  /**
   * 计算交集包围盒 (无交集返回 null)。
   * Returns the intersection bounds, or null if they don't overlap.
   */
  intersect(other: GameBounds3): GameBounds3 | null;

  /**
   * 判断点 v 是否位于包围盒内部 (含边界)。
   * Returns true if point v is inside (or on the boundary of) this bounds.
   */
  contains(v: GameVector3): boolean;

  /** 判断是否完全包含另一个包围盒。Whether this bounds fully contains b. */
  containsBounds(b: GameBounds3): boolean;

  /** 从 GameVector3 数组创建最小包围盒。Creates bounds from an array of GameVector3. */
  static fromPoints(points: GameVector3[]): GameBounds3 | null;

  toString(): string;
}

// ──────────────────────────────────────────────

/**
 * RGB 颜色 (三个通道, 每通道 0.0‑1.0)。
 * An RGB color with three channels ranging from 0.0 to 1.0.
 */
declare class GameRGBColor {
  /** 红色通道 (0.0‑1.0)。Red channel. */
  r: number;
  /** 绿色通道 (0.0‑1.0)。Green channel. */
  g: number;
  /** 蓝色通道 (0.0‑1.0)。Blue channel. */
  b: number;

  /**
   * 用指定的 R / G / B 值创建颜色。
   * Creates a color with the given R/G/B values.
   */
  constructor(r: number, g: number, b: number);

  /** 原地设置所有通道。Sets all three channels in‑place. */
  set(r: number, g: number, b: number): GameRGBColor;

  /** 原地复制另一个颜色的值。Copies values from another color in‑place. */
  copy(o: GameRGBColor): GameRGBColor;

  /** 深拷贝。Returns a new independent copy. */
  clone(): GameRGBColor;

  /** 逐通道加法 (返回新对象)。Channel‑wise addition (returns new object). */
  add(o: GameRGBColor): GameRGBColor;

  /** 逐通道减法 (返回新对象)。Channel‑wise subtraction (returns new object). */
  sub(o: GameRGBColor): GameRGBColor;

  /** 逐通道乘法 (返回新对象)。Channel‑wise multiplication (returns new object). */
  mul(o: GameRGBColor): GameRGBColor;

  /** 逐通道除法 (返回新对象, 除以 0 得 0)。Channel‑wise division (divide‑by‑zero → 0). */
  div(o: GameRGBColor): GameRGBColor;

  /** 原地加法。Addition in‑place. */
  addEq(o: GameRGBColor): GameRGBColor;

  /** 原地减法。Subtraction in‑place. */
  subEq(o: GameRGBColor): GameRGBColor;

  /** 原地乘法。Multiplication in‑place. */
  mulEq(o: GameRGBColor): GameRGBColor;

  /** 原地除法 (除以 0 跳过该通道)。Division in‑place (divide‑by‑zero skips that channel). */
  divEq(o: GameRGBColor): GameRGBColor;

  /**
   * 在 this 和 o 之间线性插值。
   * Linear interpolation between this and o by ratio n.
   */
  lerp(o: GameRGBColor, n: number): GameRGBColor;

  /** 近似相等检查 (容差 1e‑6)。Approximate equality within 1e‑6 tolerance. */
  equals(o: GameRGBColor): boolean;

  /** 转为 "rgba(r,g,b,1.0)" 格式字符串。Converts to an rgba CSS string. */
  toRGBA(): string;

  /**
   * 生成一个随机 RGB 颜色 (每个通道 0‑1)。
   * Generates a random RGB color (each channel 0–1).
   */
  static random(): GameRGBColor;

  toString(): string;
}

// ──────────────────────────────────────────────

/**
 * RGBA 颜色 (四个通道, 每通道 0.0‑1.0)。
 * An RGBA color; all four channels range from 0.0 to 1.0.
 */
declare class GameRGBAColor {
  /** 红色通道 (0.0‑1.0)。Red channel. */
  r: number;
  /** 绿色通道 (0.0‑1.0)。Green channel. */
  g: number;
  /** 蓝色通道 (0.0‑1.0)。Blue channel. */
  b: number;
  /** Alpha (不透明度), 范围 0.0–1.0。Alpha (opacity), range 0.0–1.0. */
  a: number;

  constructor(r: number, g: number, b: number, a: number);

  /** 原地设置所有四个通道。Sets all four channels in‑place. */
  set(r: number, g: number, b: number, a: number): GameRGBAColor;

  /** 原地复制另一个颜色的值。Copies values from another RGBA color in‑place. */
  copy(c: GameRGBAColor): GameRGBAColor;

  /** 深拷贝。Returns a new independent copy. */
  clone(): GameRGBAColor;

  /** 逐通道加法 (返回新对象)。Channel‑wise addition (returns new object). */
  add(rgba: GameRGBAColor): GameRGBAColor;

  /** 逐通道减法 (返回新对象)。Channel‑wise subtraction (returns new object). */
  sub(rgba: GameRGBAColor): GameRGBAColor;

  /** 逐通道乘法 (返回新对象)。Channel‑wise multiplication (returns new object). */
  mul(rgba: GameRGBAColor): GameRGBAColor;

  /** 逐通道除法 (返回新对象, 除以 0 得 0)。Channel‑wise division (returns new object; divide‑by‑zero → 0). */
  div(rgba: GameRGBAColor): GameRGBAColor;

  /** 原地加法。Addition in‑place. */
  addEq(rgba: GameRGBAColor): GameRGBAColor;

  /** 原地减法。Subtraction in‑place. */
  subEq(rgba: GameRGBAColor): GameRGBAColor;

  /** 原地乘法。Multiplication in‑place. */
  mulEq(rgba: GameRGBAColor): GameRGBAColor;

  /** 原地除法 (除以 0 跳过该通道)。Division in‑place (divide‑by‑zero skips that channel). */
  divEq(rgba: GameRGBAColor): GameRGBAColor;

  /** 线性插值。Linear interpolation between this and rgba by ratio n. */
  lerp(rgba: GameRGBAColor, n: number): GameRGBAColor;

  /** 近似相等检查 (容差 1e‑6)。Approximate equality within 1e‑6 tolerance. */
  equals(rgba: GameRGBAColor): boolean;

  /**
   * Alpha 混合: 将自身 RGBA 颜色混合到 RGB 背景上。
   * Blends this RGBA color onto an RGB background, returning the displayed RGB.
   */
  blendEq(rgb: GameRGBColor): GameRGBColor;

  toString(): string;
}

// ──────────────────────────────────────────────

/**
 * 四元数, 用于三维旋转。
 * A quaternion used for 3‑dimensional rotation.
 *
 * @remarks
 * 单位四元数 (w²+x²+y²+z²=1) 表示纯旋转。
 * Unit quaternions represent pure rotations.
 */
declare class GameQuaternion {
  /** 实部 (标量分量)。Real (scalar) component. */
  w: number;
  /** 虚部 X 分量。Imaginary X component. */
  x: number;
  /** 虚部 Y 分量。Imaginary Y component. */
  y: number;
  /** 虚部 Z 分量。Imaginary Z component. */
  z: number;

  /** 创建单位四元数 (1, 0, 0, 0)。Creates an identity quaternion. */
  constructor();

  /** 用指定的 w/x/y/z 分量创建四元数。 */
  constructor(w: number, x: number, y: number, z: number);

  /** 原地设置所有分量。Sets all components in‑place. */
  set(w: number, x: number, y: number, z: number): GameQuaternion;

  /** 原地复制。Copies values from another quaternion in‑place. */
  copy(v: GameQuaternion): GameQuaternion;

  /** 深拷贝。Returns a new independent copy. */
  clone(): GameQuaternion;

  /** 逐分量加法。Component‑wise addition. */
  add(v: GameQuaternion): GameQuaternion;

  /** 逐分量减法。Component‑wise subtraction. */
  sub(v: GameQuaternion): GameQuaternion;

  /**
   * 四元数乘法 (汉密尔顿积): this × q。
   * Hamilton product: this × q.
   * @remarks 注意乘法不满足交换律。Multiplication is NOT commutative.
   */
  mul(q: GameQuaternion): GameQuaternion;

  /**
   * 共轭四元数 (对单位四元数等价于逆)。
   * Conjugate of this quaternion (equals inverse for unit quaternions).
   */
  inv(): GameQuaternion;

  /** 除法: this × q⁻¹。Division: this × q⁻¹. */
  div(q: GameQuaternion): GameQuaternion;

  /** 点积: this · q。Dot product. */
  dot(q: GameQuaternion): number;

  /** 模长 (范数)。Magnitude (norm). */
  mag(): number;

  /** 模长平方。Squared magnitude. */
  sqrMag(): number;

  /**
   * 单位化: 返回模长为 1 的新四元数。
   * Normalizes this quaternion; returns a unit quaternion.
   */
  normalize(): GameQuaternion;

  /**
   * 球面线性插值 (Slerp): 在 this 和 q 之间平滑旋转。
   * Spherical linear interpolation — smooth rotation between this and q.
   * @param t - 插值比率 (0=this, 1=q) / interpolation factor
   */
  slerp(q: GameQuaternion, t: number): GameQuaternion;

  /**
   * 返回 this 和 q 之间的角度 (弧度)。
   * Angular difference between this and q (in radians).
   */
  angle(q: GameQuaternion): number;

  /**
   * 返回四元数对应的轴‑角表示。
   * Decomposes this quaternion into axis‑angle representation.
   * @returns 包含 `angle` 和 `axis` 字段的对象 / object with `angle` and `axis` fields
   */
  getAxisAngle(): AxisAngle;

  // ── 旋转操作 / Rotation operations ──

  /** 绕 X 轴旋转 (在左侧乘以旋转四元数)。Rotate around X axis. */
  rotateX(rad: number): GameQuaternion;
  /** 绕 Y 轴旋转。Rotate around Y axis. */
  rotateY(rad: number): GameQuaternion;
  /** 绕 Z 轴旋转。Rotate around Z axis. */
  rotateZ(rad: number): GameQuaternion;

  // ── 静态构造器 / Static constructors ──

  /** 从轴‑角表示创建四元数。Create from axis‑angle representation. */
  static fromAxisAngle(axis: GameVector3, rad: number): GameQuaternion;

  /**
   * 从欧拉角创建四元数 (YZX 旋转顺序)。
   * Create from Euler angles (YZX rotation order: Y → Z → X).
   */
  static fromEuler(x: number, y: number, z: number): GameQuaternion;

  /**
   * 计算从向量 a 旋转到向量 b 的最短弧四元数。
   * Shortest‑arc quaternion rotating from vector a to vector b.
   */
  static rotationBetween(a: GameVector3, b: GameVector3): GameQuaternion;

  /** 近似相等检查 (容差 1e‑6)。 */
  equals(v: GameQuaternion): boolean;

  toString(): string;
}

/**
 * 轴‑角表示的返回类型 (由 getAxisAngle() 返回)。
 * Return type for quaternion.getAxisAngle().
 */
interface AxisAngle {
  /** 旋转角度 (弧度) / rotation angle in radians */
  angle: number;
  /** 旋转轴 (单位向量) / rotation axis (unit vector) */
  axis: GameVector3;
}

/**
 * 事件处理器令牌 — 由 world.onXxx() 返回。
 * Event handler token — returned by world.onXxx().
 *
 * @remarks
 * 调用 cancel() 取消监听后不可恢复, 需重新注册。
 * Once cancelled via cancel(), it cannot be resumed — re-register instead.
 */
declare class GameEventHandlerToken {
  /** 取消事件监听 (不可逆)。Cancels the event listener (irreversible). */
  cancel(): void;

  /**
   * 尝试恢复已取消的监听 (会抛出 UnsupportedOperationException)。
   * Attempts to resume a cancelled listener — always throws UnsupportedOperationException.
   * @throws UnsupportedOperationException 始终抛出 / always thrown
   */
  resume(): void;

  /** 返回 true 表示监听仍处于活跃状态。Returns true if the listener is still active. */
  active(): boolean;
}

/**
 * onTick 回调的参数类型。
 * The info object passed to onTick handlers.
 */
interface TickInfo {
  /** 当前 tick 数。Current tick count. */
  tick: number;
  /** 上一 tick 数。Previous tick count. */
  prevTick: number;
  /** 自启动以来的毫秒数。Milliseconds elapsed since server start. */
  elapsedTimeMS: number;
  /** 跳过的 tick 数 (MC 下始终为 0)。Number of skipped ticks (always 0 in MC). */
  skip: number;
}

// ================================================================
//  §2  Storage Types — 持久化存储
// ================================================================

/**
 * 数据存储空间 (键值持久化)。
 * A data‑storage namespace — persistent key‑value store backed by JSON files.
 *
 * @remarks
 * 通过 `storage.getDataStorage("name")` 获取。
 * Obtain via `storage.getDataStorage("name")`.
 */
interface GameDataStorage {
  /**
   * 获取存储空间名称 (只读)。
   * @en Returns the read‑only namespace name.
   */
  readonly key: string;

  /**
   * 存入一个键值对。值必须是可 JSON 序列化的类型。
   * Stores a key‑value pair. Value must be JSON‑serializable.
   * @param key - 键 / key
   * @param value - 值 (number | string | boolean | object | array | null) / value
   */
  set(key: string, value: unknown): void;

  /**
   * 读取键对应的值, 不存在则返回 null。
   * Retrieves the value for a key, or null if it does not exist.
   * @returns 存储的值, 或 null
   */
  get(key: string): unknown;

  /**
   * 获取当前存储空间中的所有键。
   * Lists all keys in this storage namespace.
   */
  keys(): string[];

  /**
   * 原子更新: 取出当前值, 用 handler(currentValue) 的结果覆盖。
   * Atomically updates a value using a callback.
   * @param key - 键 / key
   * @param handler - (prevValue) => newValue / callback receiving the old value, returning the new one
   * @remarks 如果键不存在, 不会创建新条目 (遵循 Box3 规范)。
   *          If the key does not exist, nothing happens (per Box3 spec).
   */
  update(key: string, handler: (prevValue: unknown) => unknown): void;

  /**
   * 删除键, 返回旧值 (不存在则返回 null)。
   * Removes a key and returns its previous value, or null.
   * @returns 被删除的旧值 / the previous value, or null
   */
  remove(key: string): unknown;

  /**
   * 原子递增 (delta 默认为 1)。
   * Atomically increments a numeric value by delta (default 1).
   * @param key - 键 / key
   * @param delta - 增量 (可选, 默认 1) / increment amount (optional, default 1)
   * @returns 递增后的新值 / the new value after incrementing
   * @remarks 键不存在时从 0 + delta 开始。
   *          If the key doesn't exist, starts from 0 + delta.
   */
  increment(key: string, delta?: number): number;

  /**
   * 分页查询存储条目。
   * Paginated query of stored entries.
   * @param options - 查询选项 / query options
   * @param options.cursor - 起始游标 (页码) / starting cursor (page number * pageSize)
   * @param options.pageSize - 每页条目数 (1‑100, 默认 100) / items per page (1–100, default 100)
   * @param options.ascending - 是否升序排列 / sort ascending if true
   * @param options.max - 值的上限过滤 / maximum value filter
   * @param options.min - 值的下限过滤 / minimum value filter
   * @param options.constraintTarget - 排序/过滤的目标路径 (如 "a.b.c") / nested path for sorting/filtering
   * @returns 分页结果对象 / paginated query result
   */
  list(options?: {
    cursor?: number;
    pageSize?: number;
    ascending?: boolean;
    max?: number;
    min?: number;
    constraintTarget?: string;
  }): QueryList;

  /**
   * 销毁该存储空间 (删除对应 JSON 文件)。
   * Destroys this storage namespace (deletes the backing JSON file).
   */
  destroy(): void;
}

/**
 * 分页查询结果 (由 GameDataStorage.list() 返回)。
 * Paginated query result returned by GameDataStorage.list().
 */
interface QueryList {
  /** 是否已到达最后一页。Whether the last page has been reached. */
  isLastPage: boolean;

  /**
   * 获取当前页的条目数组。
   * Returns the entries for the current page.
   */
  getCurrentPage(): ReturnValue[];

  /**
   * 移动到下一页。
   * Advances the cursor to the next page.
   */
  nextPage(): void;
}

/**
 * 单个存储条目 (包含元数据)。
 * A single stored entry with metadata.
 */
interface ReturnValue {
  /** 键名 / key name */
  key: string;
  /** 值 / stored value */
  value: unknown;
  /** 更新时间 (Unix 毫秒) / last‑modified timestamp (Unix ms) */
  updateTime: number;
  /** 创建时间 (Unix 毫秒) / creation timestamp (Unix ms) */
  createTime: number;
  /** 版本标识符 (可用于乐观锁) / version identifier (usable for optimistic locking) */
  version: string;
}

/**
 * 全局存储入口 — 脚本中通过 `storage` 访问。
 * Global storage entry point — accessed via `storage` in scripts.
 *
 * @remarks
 * 项目间数据隔离: 每个项目自动使用项目名作为存储文件前缀。
 * Per‑project isolation: each project's storage is automatically prefixed with the project name.
 * 跨项目共享: `getGroupStorage` 使用 `__shared__/` 命名空间, 所有项目访问同一数据。
 * Cross‑project sharing: `getGroupStorage` uses a `__shared__/` namespace visible to all projects.
 */
interface GameStorage {
  /** 始终返回空字符串 (MC 本地存储无 key, 只读)。Always returns "" for MC local storage, readonly. */
  readonly key: string;

  /**
   * 打开或创建指定名称的数据存储空间 (项目隔离)。
   * Opens or creates a named data‑storage namespace (per‑project isolated).
   * @param name - 命名空间 (可含 "/" 作为目录分隔) / namespace (may contain "/" as directory separator)
   * @remarks 不同项目使用同一 name 会访问不同文件。
   *          Different projects using the same name access different files.
   */
  getDataStorage(name: string): GameDataStorage;

  /**
   * 获取跨项目共享存储 — 所有项目通过同一 name 读写同一份数据。
   * Shared cross‑project storage — all projects read/write the same data by name.
   * @param name - 命名空间 / namespace
   * @remarks 底层使用 `__shared__/` 前缀, 适合全服排行榜、全局配置等场景。
   *          Uses `__shared__/` prefix internally; suitable for global leaderboards, shared config, etc.
   */
  getGroupStorage(name: string): GameDataStorage;
}

// ================================================================
//  §3  Entity — 实体
// ================================================================

/**
 * 实体包装, 可用于玩家或生物。
 * Entity wrapper — represents a player or mob in the world.
 *
 * @remarks
 * 通过 `world.querySelector()`, `world.querySelectorAll()` 或事件回调获取。
 * Obtained via `world.querySelector()`, `world.querySelectorAll()`, or event callbacks.
 */
interface GameEntity {
  // ── 身份 / Identity ──

  /**
   * 实体 UUID (字符串格式, 只读)。
   * Entity UUID as a string (e.g. "550e8400-e29b-41d4-a716-446655440000"), readonly.
   */
  readonly id: string;

  /**
   * 是否为玩家实体。返回 true 后 player 属性自动收窄为非 null。
   * True if this entity is a player. After a truthy check, `player` is narrowed to non-null.
   */
  isPlayer(): this is GamePlayerEntity;

  /**
   * 实体类型标识符 (如 "minecraft:zombie", 只读)。
   * Entity type identifier (e.g. "minecraft:zombie"), readonly.
   */
  readonly entityType: string;

  // ── 位置 & 运动 / Position & Movement ──

  /**
   * 当前坐标 (世界坐标, 只读, 可通过 .set() 修改)。
   * Current world‑space position. Readonly ref — mutate via .set(), cannot reassign.
   */
  readonly position: GameVector3;

  /**
   * 当前速度 (运动向量, 只读, 可通过 .set() 修改)。
   * Current velocity (motion vector). Readonly ref — mutate via .set(), cannot reassign.
   */
  readonly velocity: GameVector3;

  /**
   * 包围盒半尺寸 (x=宽/2, y=高/2, z=宽/2, 只读)。
   * Bounding‑box half‑extents (x=width/2, y=height/2, z=width/2), readonly.
   */
  readonly bounds: GameVector3;

  /**
   * 是否在地面上 (只读)。
   * True if the entity is standing on a block, readonly.
   */
  readonly onGround: boolean;

  /**
   * 视线起始点 (眼部位置, 只读)。
   * Eye position (raycast origin for the entity's view), readonly.
   */
  readonly eyePosition: GameVector3;

  // ── 生命状态 / Lifecycle ──

  /**
   * 当前生命值。
   * Current health (HP).
   */
  hp: number;

  /**
   * 最大生命值。
   * Maximum health.
   */
  maxHp: number;

  /**
   * 实体是否已被移除/销毁 (true = 已移除, 只读)。
   * Whether the entity has been removed / destroyed (true = removed), readonly.
   */
  readonly destroyed: boolean;

  /**
   * 设置实体着火 tick 数 (0 = 灭火)。
   * Sets the remaining fire ticks (0 = extinguish).
   */
  setFire(ticks: number): void;

  /** 灭火。Extinguishes any fire on the entity. */
  clearFire(): void;

  // ── 伤害 & 恢复 / Damage & Healing ──

  /**
   * 对实体造成伤害。
   * Deals generic damage to the entity.
   * @param amount - 伤害值 (半心) / damage amount in half‑hearts
   */
  hurt(amount: number): void;

  /**
   * 治疗实体。
   * Heals the entity.
   * @param amount - 治疗量 (半心) / healing amount in half‑hearts
   */
  heal(amount: number): void;

  // ── 外观 / Appearance ──

  /**
   * 是否不可见 (隐身)。
   * True if the entity is invisible.
   */
  meshInvisible: boolean;

  /** 是否发光 (轮廓高亮)。Whether glow outline is active. */
  glowing: boolean;

  /**
   * 名称标签文本 (空字符串 = 无)。
   * Custom name tag text (empty string = none).
   */
  nameTag: string;
  setNameTag(name: string): void;

  // ── 物理 / Physics ──

  /**
   * 是否参与碰撞 (默认 true)。
   * Whether the entity participates in collisions (default true).
   */
  collides: boolean;

  /**
   * 是否固定 (默认 false, true 时禁用重力并每 tick 清零速度)。
   * Whether the entity is fixed in place (default false; disables gravity + zeros velocity each tick).
   */
  fixed: boolean;

  /**
   * 是否受重力影响 (默认 true)。
   * Whether gravity affects the entity (default true).
   */
  gravity: boolean;

  /** 摩擦系数 (默认 0.0)。Friction coefficient. */
  friction: number;

  /** 质量 (默认 1.0)。Mass. */
  mass: number;

  /** 弹性系数 (默认 0.0)。Restitution (bounciness). */
  restitution: number;

  // ── 无敌 & 持久化 / Invulnerability & Persistence ──

  /** 是否无敌。Whether the entity is invulnerable to damage. */
  invulnerable: boolean;

  /**
   * 设置为持久化实体 (防止被自然清除)。
   * Marks the entity as persistent (prevents it from being despawned naturally).
   * @remarks 仅写方法, 无 getter。Write‑only method, no getter available.
   */
  setPersistent(v: boolean): void;

  // ── 标签 / Tags ──

  /** 添加一个标签。Adds a scoreboard tag. */
  addTag(tag: string): void;

  /** 移除一个标签。Removes a scoreboard tag. */
  removeTag(tag: string): void;

  /** 检查是否拥有指定标签。Checks whether the entity has the given tag. */
  hasTag(tag: string): boolean;

  /** 获取所有标签。Returns all tags as a string array. */
  tags(): string[];

  // ── 效果 / Effects ──

  /**
   * 添加状态效果。
   * Applies a status effect to the entity.
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

  // ── 属性 / Attributes ──

  /**
   * 读取实体属性值。
   * Reads a registered entity attribute value.
   * @param attributeId - 属性 ID (如 "minecraft:generic.max_health")
   * @returns 当前属性值, 不支持的实体返回 0
   */
  getAttribute(attributeId: string): number;

  /**
   * 设置实体属性基础值。
   * Sets the base value of a registered entity attribute.
   * @param attributeId - 属性 ID (如 "minecraft:generic.movement_speed")
   * @param value - 新基础值 / new base value
   * @remarks 仅对 LivingEntity 有效。Only works on living entities.
   */
  setAttribute(attributeId: string, value: number): void;

  // ── 装备 / Equipment ──

  /**
   * 给生物设置装备。
   * Equips an item onto a mob's equipment slot.
   * @param slot - 槽位名称 / slot name:
   *   "mainhand", "offhand", "head"/"helmet"/"helm",
   *   "chest"/"chestplate", "legs"/"leggings", "feet"/"boots"
   * @param itemId - 物品 ID (如 "minecraft:diamond_sword")
   */
  setEquipment(slot: string, itemId: string): void;

  /**
   * 设置装备掉落概率。
   * Sets the drop chance for an equipment slot.
   * @param slot - 槽位名称 或 "all" / slot name or "all" for every slot
   * @param chance - 掉落概率 (0‑1) / drop chance (0–1)
   */
  setDropChance(slot: string, chance: number): void;

  // ── 导航 & AI / Navigation & AI ──

  /**
   * 让生物导航到指定坐标。
   * Orders a pathfinder mob to navigate to the given coordinates.
   * @param x, y, z - 目标坐标
   * @param speed - 移动速度倍率
   * @returns 路径计算成功返回 true, 非 PathfinderMob 返回 false
   */
  navigateTo(x: number, y: number, z: number, speed: number): boolean;
  /** GameVector3 重载。GameVector3 overload. */
  navigateTo(pos: GameVector3, speed: number): boolean;

  /**
   * 设置生物的当前攻击目标。
   * Sets the mob's attack target (the mob will pathfind to and attack it).
   */
  setTarget(target: GameEntity): void;

  /** 清除攻击目标, 停止追击。Clears the attack target, stopping pursuit. */
  clearTarget(): void;

  /**
   * 获取当前攻击目标 (可能为 null)。
   * Returns the mob's current attack target, or null.
   */
  getTarget(): GameEntity | null;

  /**
   * 启用或禁用生物 AI (寻路/目标等)。
   * Enables or disables the mob's AI (pathfinding, goals, etc.).
   */
  setAI(enabled: boolean): void;

  // ── 朝向 / Look direction ──

  /**
   * 让实体看向指定坐标。
   * Makes the entity look at a point in space.
   */
  lookAt(x: number, y: number, z: number): void;
  lookAt(pos: GameVector3): void;

  // ── 生命周期 / Lifecycle ──

  /**
   * 销毁实体 (触发 onDestroy 回调)。
   * Destroys the entity (triggers any registered onDestroy callback).
   */
  destroy(): void;

  setOnDestroy(handler: (entity: GameEntity) => void): void;

  // ── 玩家代理 / Player proxy ──

  /**
   * 玩家接口 (仅当 isPlayer 为 true 时非 null)。
   * The player interface — non‑null only when isPlayer is true.
   */
  player: GamePlayer | null;
}

/**
 * 玩家实体 — GameEntity 的子类型, 保证 player 属性非 null。
 * A player entity — subtype of GameEntity with a guaranteed non‑null `player`.
 */
type GamePlayerEntity = GameEntity & { player: GamePlayer };

// ================================================================
//  §4  Player — 玩家
// ================================================================

/**
 * 玩家扩展接口 (通过 entity.player 访问)。
 * Player‑specific interface — accessed via `entity.player`.
 */
interface GamePlayer {
  // ── 身份 / Identity ──

  /** 玩家名 (只读)。Player display name, readonly. */
  readonly name: string;
  /** 玩家 UUID (与 entity.id 相同, 只读)。Player UUID (same as entity.id), readonly. */
  readonly userId: string;

  // ── 位置 & 运动 / Position & Movement ──

  /**
   * 当前坐标 (世界坐标, 只读, 可通过 .set() 修改)。
   * Current world‑space position. Readonly ref — mutate via .set(), cannot reassign.
   */
  readonly position: GameVector3;

  /**
   * 当前速度 (运动向量, 只读, 可通过 .set() 修改)。
   * Current velocity (motion vector). Readonly ref — mutate via .set(), cannot reassign.
   */
  readonly velocity: GameVector3;

  /**
   * 包围盒半尺寸 (只读)。
   * Bounding‑box half‑extents, readonly.
   */
  readonly bounds: GameVector3;

  /**
   * 是否在地面上 (只读)。
   * True if the player is standing on a block, readonly.
   */
  readonly onGround: boolean;

  // ── 外观 / Appearance ──

  /**
   * 是否隐身。
   * Whether the player is invisible.
   */
  invisible: boolean;

  /**
   * 模型缩放比例 (MC 原生, 非 Box3 scale)。
   * Player model scale (Minecraft native, not Box3 scale).
   */
  readonly scale: number;

  // ── 移动 / Movement ──

  /** 行走速度 (基础值)。Walk speed (base attribute value). */
  walkSpeed: number;

  /**
   * 疾跑速度 (≈ walkSpeed × 1.3)。
   * Run/sprint speed (≈ walkSpeed × 1.3).
   */
  runSpeed: number;

  /**
   * 跳跃力度。
   * Jump power (jump strength attribute).
   */
  jumpPower: number;

  /**
   * 当前移动状态。
   * Current movement state.
   * @returns "FLYING" | "GROUND" | "SWIM" | "FALL" | "JUMP"
   */
  readonly moveState: string;

  /**
   * 当前行走状态。
   * Current walk state.
   * @returns "NONE" | "CROUCH" | "WALK" | "RUN"
   */
  readonly walkState: string;

  // ── 跳跃 / 潜行 / 游泳 / Jump / Sneak / Swim ──

  /**
   * 是否允许跳跃 (默认 true, false 时清除跳跃力)。
   * Whether jumping is enabled (default true; when false, jump strength is zeroed).
   */
  enableJump: boolean;

  /** 潜行速度 (默认 0.0, MC 下无独立潜行速度)。Crouch speed (stored as custom prop). */
  crouchSpeed: number;

  /** 游泳速度 (映射到 WATER_MOVEMENT_EFFICIENCY 属性)。Swim speed (maps to WATER_MOVEMENT_EFFICIENCY attribute). */
  swimSpeed: number;

  // ── 飞行 & 碰撞 / Flying & Collision ──

  /** 是否允许飞行。Whether flight is enabled. */
  canFly: boolean;

  /** 是否正在飞行。Whether the player is currently flying. */
  flying: boolean;

  /** 飞行速度。Flying speed. */
  flySpeed: number;

  /**
   * 碰撞开关 (通过队伍碰撞规则实现)。
   * Collision toggle (implemented via team collision rules).
   */
  collision: boolean;

  /** 是否为观察者模式。Whether the player is in spectator mode. */
  readonly spectator: boolean;

  /** 是否禁用飞行 (不允许且自动关闭飞行)。Whether flying is disabled entirely. */
  disableFly: boolean;

  // ── 游戏模式 / Game Mode ──

  /**
   * 游戏模式字符串 (如 "survival", "creative", "adventure", "spectator")。
   * Game mode as a string (e.g. "survival", "creative", "adventure", "spectator").
   * 也可以接受数字 (0=survival, 1=creative, 2=adventure, 3=spectator)。
   */
  gameMode: string | number;

  /**
   * 当前维度 ID (如 "minecraft:overworld")。
   * Current dimension identifier.
   */
  dimension: string;

  // ── 相机 / Camera ──

  /**
   * 相机模式。
   * Camera mode.
   * @default "FPS"
   */
  cameraMode: string;

  /**
   * 相机跟随的实体 (在 FOLLOW 模式下)。
   * The entity the camera follows (when in FOLLOW mode).
   */
  cameraEntity: GameEntity | null;

  /** 相机俯仰角。Camera pitch (vertical rotation). */
  cameraPitch: number;

  /** 相机偏航角。Camera yaw (horizontal rotation). */
  cameraYaw: number;

  /**
   * 玩家面朝方向 (单位向量)。
   * Direction the player is facing (unit vector).
   */
  readonly facingDirection: GameVector3;

  /**
   * 玩家视线前方 5 格处的目标点。
   * A point 5 blocks ahead of the player's eyes (look‑at target).
   */
  readonly cameraTarget: GameVector3;

  // ── 生命 / Vital stats ──

  /** 饥饿值 (0‑20)。Food level (0–20). */
  food: number;

  /** 饱和度 (0‑20)。Saturation level (0–20). */
  saturation: number;

  /** 当前生命值。Current health. */
  hp: number;
  /** 最大生命值。Maximum health. */
  maxHp: number;

  // ── 经验 / Experience ──

  /** 经验等级 (与 /xp 命令相同)。Experience level (same as /xp command). */
  xp: number;

  /** 增加经验等级。Adds experience levels to the player. */
  addExperienceLevels(levels: number): void;

  // ── 传送 / Teleport ──

  /**
   * 将玩家传送到指定坐标。
   * Teleports the player to the given coordinates.
   */
  teleport(pos: GameVector3): void;

  // ── 重生 / Respawn ──

  /**
   * 是否已死亡。
   * Whether the player is dead or dying.
   */
  readonly dead: boolean;

  /**
   * 重生点坐标 (可读写)。
   * Spawn point coordinates (readable & writable).
   */
  spawnPoint: GameVector3;

  /**
   * 设置重生点。
   * Sets the player's respawn point.
   */
  setRespawnPoint(pos: GameVector3): void;

  /**
   * 强制重生 (仅在死亡状态下有效)。
   * Forces a respawn (only works when dead).
   */
  respawn(): void;

  // ── 踢出 / Kick ──

  /** 踢出玩家 (默认理由 "Kicked")。Kicks the player with default reason. */
  kick(): void;
  /** 踢出玩家 (自定义理由)。Kicks the player with a custom reason. */
  kick(reason: string): void;

  // ── 消息 / Messaging ──

  /**
   * 发送仅该玩家可见的聊天消息。
   * Sends a chat message visible only to this player.
   */
  directMessage(msg: string): void;

  /**
   * 在动作栏 (快捷栏上方) 显示文字。
   * Displays text in the action bar (above the hotbar).
   */
  actionBar(message: string): void;

  /**
   * 显示屏幕标题。
   * Displays a screen title.
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
   * 弹出对话面板 (简化版, MC 目前仅发送文本)。
   * Shows a dialog panel — simplified; currently just sends text in MC.
   * @param config.content - 对话内容
   * @param config.options - 选项数组
   * @returns 用户选择结果 { index, value }
   */
  dialog(config: { content?: string; options?: string[] }): {
    index: number;
    value: string;
  };

  // ── 链接 / Link ──

  /**
   * 向玩家发送可点击的 URL 链接。
   * Sends a clickable URL link to the player.
   */
  link(href: string): void;

  // ── 计分板名称 / Tab list name ──

  /**
   * 设置玩家在 TAB 列表中的显示名称 (支持颜色代码)。
   * Sets the player's display name in the tab list (supports color codes).
   */
  setPlayerListName(name: string): void;

  // ── 朝向 / Look direction ──

  /**
   * 让玩家看向指定坐标。
   * Makes the player look at a point in space.
   */
  lookAt(x: number, y: number, z: number): void;
  lookAt(pos: GameVector3): void;

  // ── 执行命令 / Command ──

  /**
   * 以玩家身份执行 Minecraft 命令。
   * Executes a Minecraft command as this player.
   */
  runCommand(cmd: string): void;

  // ── 物品栏 / Inventory ──

  /**
   * 给予玩家物品。
   * Gives an item to the player.
   * @param itemId - 物品 ID (如 "minecraft:diamond")
   * @param count - 数量 (1‑64)
   */
  giveItem(itemId: string, count: number): void;

  /**
   * 给予玩家自定义物品 (基于 resourcepacks/box3js-items/items.json 配置)。
   * Gives a custom item defined in the resource pack's items.json.
   * Items are vanilla paper with custom_model_data + name/lore/food components.
   * @param id - 自定义物品 ID (如 "arena_trophy")
   * @param count - 数量 (1‑64)
   */
  giveCustomItem(id: string, count: number): void;

  /**
   * 给予玩家附魔物品。
   * Gives an enchanted item to the player.
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
   * 给予玩家带自定义名称和描述的命名物品。
   * Gives an item with a custom name and lore.
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
   * 获取手持物品信息。
   * Returns info about the currently held item.
   * @returns { id: string, count: number }
   */
  getHeldItem(): { id: string; count: number };

  /** 清空背包。Clears the player's inventory. */
  clearInventory(): void;

  /** 管理员权限等级 (0-4)。0=普通玩家, 4=最高权限。Server operator permission level (0–4). */
  opLevel: number;

  // ── 效果 / Effects ──

  /**
   * 添加状态效果。
   * Applies a status effect.
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

  /** 清除所有状态效果。Removes all status effects. */
  clearEffects(): void;

  // ── 声音 / Sound ──

  /**
   * 向该玩家播放声音。
   * Plays a sound for this player only.
   * @param path - 声音 ID (如 "minecraft:block.note_block.pling")
   * @param volume - 音量 (0‑1)
   * @param pitch - 音高 (0.5‑2)
   */
  playSound(path: string, volume: number, pitch: number): void;

  // ── 聊天 / Chat ──

  /**
   * 为该玩家注册聊天处理器 (覆盖全局 onChat)。
   * Registers a per‑player chat handler (overrides global onChat for this player).
   * @returns GameEventHandlerToken
   */
  onChat(
    handler: (
      entity: GamePlayerEntity,
      message: string,
      tick: number,
    ) => boolean | void,
  ): GameEventHandlerToken;

  // ── 成就 / Advancements ──

  /**
   * 授予该玩家一个成就/进度。
   * Grants an advancement to this player by resource location (e.g. "minecraft:story/mine_stone").
   */
  grantAdvancement(advancementId: string): void;

  /**
   * 撤销该玩家的一个成就/进度。
   * Revokes an advancement from this player.
   */
  revokeAdvancement(advancementId: string): void;
}

// ================================================================
//  §5  World — 世界 API
// ================================================================

/**
 * 世界控制与事件 — 脚本中通过 `world` 访问。
 * World control & events — accessed via `world` in scripts.
 */
interface GameWorld {
  // ── 世界属性 / World properties ──

  /** 服务器 MOTD。Server MOTD string. */
  projectName(): string;

  /** 服务器 MOTD (可读写, 同 projectName)。Server MOTD (read/write, alias of projectName). */
  serverId: string;

  /** 当前服务端 tick 计数。Current server tick count. */
  currentTick(): number;

  /**
   * 降雨强度 (0‑1)。
   * Rain density (0–1).
   */
  rainDensity: number;

  /**
   * 雷暴强度 (0‑1)。
   * Thunder density (0–1).
   */
  thunderDensity: number;

  /** 清除天气 (晴天)。Clears weather to clear skies. */
  clearWeather(): void;

  // ── 时间 / Time ──

  /**
   * 当前游戏内时间 (tick, 0‑24000)。
   * Current in‑game time in ticks (0–24000).
   */
  time: number;

  /**
   * 时间流速 (1=正常, 0=停止)。
   * Time scale (1 = normal, 0 = frozen).
   */
  timeScale: number;

  /**
   * 设置游戏内时间 (tick, 0‑24000)。
   * Sets the in-game time in ticks.
   * @param time - 0=黎明, 6000=正午, 12000=黄昏, 18000=午夜
   */
  setTime(time: number): void;

  // ── 难度 / Difficulty ──

  /**
   * 当前难度。
   * Current difficulty ("peaceful" | "easy" | "normal" | "hard").
   */
  difficulty: string;

  // ── 出生点 / Spawn ──

  /**
   * 世界出生点坐标。
   * World spawn point coordinates.
   */
  readonly spawnPoint: GameVector3;

  /**
   * 设置世界出生点。
   * Sets the world spawn point.
   */
  setWorldSpawn(pos: GameVector3): void;

  // ── 游戏规则 (MC 扩展) / Game Rules (MC extension) ──

  /**
   * 读取游戏规则。
   * Reads a game‑rule value.
   * @param name - 规则名 / rule name (see setGameRule for the list)
   */
  getGameRule(name: string): boolean | null;

  /**
   * 设置游戏规则。
   * Sets a game rule.
   * @param name - supported: doDaylightCycle | doWeatherCycle | keepInventory |
   *               doMobSpawning | doFireTick | mobGriefing | doImmediateRespawn
   * @param value - boolean or string "true"/"false"
   */
  setGameRule(name: string, value: boolean | string): void;

  // ── 音效属性 / Sound Properties ──

  /** 环境音效路径 (每 200 tick 在世界出生点自动播放, 0.3 音量)。Ambient sound — auto-plays at world spawn every 200 ticks at 0.3 volume. */
  ambientSound: string;

  /** 玩家加入音效路径 (玩家加入时自动播放)。Player join sound — auto-plays when a player joins. */
  playerJoinSound: string;

  /** 玩家离开音效路径 (玩家离开时自动播放)。Player leave sound — auto-plays when a player leaves. */
  playerLeaveSound: string;

  /** 方块放置音效路径 (放置方块时自动播放)。Block place sound — auto-plays when a block is placed. */
  placeVoxelSound: string;

  /** 方块破坏音效路径 (破坏方块时自动播放)。Block break sound — auto-plays when a block is broken. */
  breakVoxelSound: string;

  // ── 实体生成 / Entity Spawning ──

  /**
   * 在指定位置生成实体。
   * Spawns an entity at the given position.
   * @param type - 实体类型 ID (如 "minecraft:zombie")
   * @param pos - 生成坐标
   * @returns 生成的实体包装, 失败返回 null
   */
  spawnEntity(type: string, pos: GameVector3): GameEntity | null;

  /**
   * 使用完整配置对象生成实体。
   * Spawns an entity with a full configuration object.
   * @param config - { type, position, velocity, fixed, gravity, friction, mass, restitution, collides, meshInvisible, hp, maxHp, tags }
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

  // ── 消息 & 声音 / Broadcasting ──

  /**
   * 向全服广播消息。
   * Sends a chat message to all players.
   */
  say(message: string): void;

  // ── 自定义物品 / Custom Items ──

  /**
   * 从资源包加载自定义物品配置 (基于数据组件, 无需 DeferredRegister, 无注册表同步问题)。
   * Loads custom item definitions from a resource pack's items.json.
   * Items use minecraft:paper as base with custom_model_data for model switching.
   * Models & textures must be provided via the resource pack (resourcepacks/<packName>/).
   *
   * JSON 格式使用 Minecraft 原版组件 ID 作为 key:
   *   "minecraft:custom_model_data", "minecraft:custom_name", "minecraft:lore",
   *   "minecraft:max_stack_size", "minecraft:enchantment_glint_override",
   *   "minecraft:rarity", "minecraft:food": { nutrition, saturation, can_always_eat, eat_seconds }
   *
   * @param packName - 资源包目录名 (如 "box3js-items"), 会在 resourcepacks/<packName>/items.json 查找
   */
  loadCustomItems(packName: string): void;

  // ── 结构 & 成就 / Structure & Advancement ──

  /**
   * 在指定位置放置数据包中的 .nbt 结构。
   * Places an .nbt structure from current datapacks at the given position.
   * Structure must exist under data/<namespace>/structure/<id>.nbt
   */
  placeStructure(x: number, y: number, z: number, structureId: string): void;
  placeStructure(pos: GameVector3, structureId: string): void;

  /**
   * 为指定玩家授予成就/进度。
   * Grants a datapack advancement to a player by name.
   */
  grantAdvancement(playerName: string, advancementId: string): void;

  /**
   * 按物品名搜索配方 ID 列表。
   * Searches recipe IDs matching a filter string.
   * @param filter - 搜索关键词 (匹配配方 ID)
   */
  listRecipes(filter: string): string[];

  /**
   * 移除指定 ID 的配方 (黑名单机制, 服务器重载后需重新移除)。
   * Removes a recipe by ID (blacklisted; re‑apply after server reload).
   * @param recipeId - 配方 ID, 例如 "minecraft:iron_pickaxe"
   * @returns 是否成功加入黑名单
   */
  removeRecipe(recipeId: string): boolean;

  /**
   * 清除所有配方黑名单, 恢复全部原始配方。
   * Clears the recipe blacklist and restores all original recipes.
   */
  clearRecipes(): void;

  /**
   * 在指定位置向全服播放声音。
   * Plays a sound for all players at a location.
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

  // ── 命令 / Command ──

  /**
   * 以服务端身份执行命令。
   * Executes a Minecraft command as the server.
   */
  runCommand(cmd: string): void;

  // ── 实体查询 / Entity Queries ──

  /**
   * 查询所有匹配选择器的实体 (目前仅限玩家)。
   * Selects all entities matching a selector (currently only players).
   * @param selector - "*" (所有玩家) | "#uuid" | ".tag"
   */
  querySelectorAll(selector: string): GameEntity[];

  /**
   * 查询第一个匹配的实体 (或 null)。
   * Selects the first matching entity, or null.
   */
  querySelector(selector: string): GameEntity | null;

  /**
   * 查询指定区域内的所有实体。
   * Returns all entities inside an AABB defined by two corners.
   */
  entitiesInArea(pos1: GameVector3, pos2: GameVector3): GameEntity[];

  /**
   * 查询指定半径内的所有实体。
   * Returns all entities within a radius around a point.
   */
  entitiesInRadius(
    x: number,
    y: number,
    z: number,
    radius: number,
  ): GameEntity[];
  entitiesInRadius(pos: GameVector3, radius: number): GameEntity[];

  // ── 搜索与音效 / Search & Sound ──

  /**
   * 播放音效 (简写或完整配置)。
   * Plays a sound (string shorthand or full config object).
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
   * 查询包围盒内的所有实体。
   * Returns all entities inside a GameBounds3.
   */
  searchBox(bounds: GameBounds3): GameEntity[];

  // ── 射线检测 / Raycast ──

  /**
   * 从起点向指定方向发射射线, 返回碰撞结果。
   * Casts a ray and returns hit information.
   * @param origin - 起点
   * @param direction - 方向向量 (自动归一化)
   * @param maxDistance - 最大距离 (可选, 默认 5)
   * @returns { hit, x, y, z, normalX, normalY, normalZ, distance, entity?, voxel? }
   */
  raycast(
    origin: GameVector3,
    direction: GameVector3,
    maxDistance?: number,
  ): RaycastResult;

  // ── 生物群系 / Biome ──

  /**
   * 获取指定位置的生物群系 ID。
   * Returns the biome identifier at the given position.
   */
  getBiome(x: number, y: number, z: number): string;
  getBiome(pos: GameVector3): string;

  // ── 爆炸 / Explosion ──

  /**
   * 在指定位置制造爆炸。
   * Creates an explosion at the given position.
   * @param x, y, z - 爆炸中心
   * @param power - 爆炸强度
   * @param fire - 是否产生火焰 (可选, 默认 false)
   */
  explode(x: number, y: number, z: number, power: number, fire?: boolean): void;
  explode(pos: GameVector3, power: number, fire?: boolean): void;

  // ── 粒子 / Particles ──

  /**
   * 在指定位置生成粒子。
   * Spawns particles at a given location.
   * @param type - 粒子 ID (如 "minecraft:flame")
   * @param x, y, z - 位置
   * @param count - 数量
   * @param dx - X 扩散范围
   * @param dy - Y 扩散范围
   * @param dz - Z 扩散范围
   * @param speed - 粒子速度
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
  spawnParticle(
    type: string,
    pos: GameVector3,
    count: number,
    dx: number,
    dy: number,
    dz: number,
    speed: number,
  ): void;

  /**
   * 在指定圆环上生成粒子。
   * Spawns particles in a circle.
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

  // ── 烟花 / Fireworks ──

  /**
   * 在指定位置发射烟花。
   * Launches a firework rocket.
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

  // ── 闪电 / Lightning ──

  /**
   * 在指定位置召唤闪电。
   * Summons a lightning bolt at the given position.
   * @param x, y, z - 位置
   * @param damage - 伤害值 (可选, 仅对实体造成)
   * @returns 是否成功
   */
  strikeLightning(x: number, y: number, z: number, damage?: number): boolean;
  strikeLightning(pos: GameVector3, damage?: number): boolean;

  // ── 掉落物 / Drop Item ──

  /**
   * 在指定位置生成掉落物。
   * Drops an item stack at the given position.
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

  // ── 弹射物 / Projectile ──

  /**
   * 从起点向目标发射弹射物。
   * Launches a projectile from origin toward a target.
   * @param type - 弹射物类型 (如 "minecraft:arrow")
   * @param x, y, z - 发射位置
   * @param tx, ty, tz - 目标位置
   * @param speed - 速度
   * @returns 弹射物实体, 失败返回 null
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

  // ── 计分板 / Scoreboard ──

  /**
   * 添加计分板目标 (默认 dummy 标准)。
   * Adds a scoreboard objective (default dummy criteria).
   */
  addScoreboard(name: string): void;

  /**
   * 添加计分板目标 (自定义标准)。
   * Adds a scoreboard objective with a custom criteria.
   */
  addScoreboard(name: string, criteria: string): void;

  /** 移除计分板目标。Removes a scoreboard objective. */
  removeScoreboard(name: string): void;

  /**
   * 设置实体/名称的分数。
   * Sets the score of an entity or name for a given objective.
   */
  setScore(
    entityOrName: string | GameEntity,
    objectiveName: string,
    value: number,
  ): void;

  /**
   * 获取分数。
   * Gets the score of an entity or name for a given objective.
   */
  getScore(entityOrName: string | GameEntity, objectiveName: string): number;

  /**
   * 在指定显示位置展示计分板。
   * Displays a scoreboard objective in a display slot.
   * @param slot - "sidebar" | "list" | "belowname"
   */
  showScoreboard(slot: string, objectiveName: string): void;

  /**
   * 从显示位置隐藏计分板。
   * Hides a scoreboard from a display slot.
   */
  hideScoreboard(slot: string): void;

  /**
   * 列出计分板上所有玩家的分数。
   * Lists all player scores for a given objective.
   * @returns Array<{ name: string, value: number }>
   */
  listScores(objectiveName: string): Array<{ name: string; value: number }>;

  // ── Boss 血条 / Boss Bar ──

  /**
   * 显示或更新 Boss 血条。
   * Shows or updates a boss bar.
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

  /** 移除 Boss 血条。Removes a boss bar by ID. */
  removeBossbar(name: string): void;

  // ── 队伍 / Teams ──

  /**
   * 创建一个队伍。
   * Creates a scoreboard team.
   * @param name - 队伍名
   * @param color - 颜色 (如 "aqua", "red", "blue" 等)
   */
  createTeam(name: string, color: string): void;

  /** 删除队伍。Removes a team. */
  removeTeam(name: string): void;

  /**
   * 将实体/名称加入队伍。
   * Adds an entity or name to a team.
   */
  joinTeam(entityOrName: string | GameEntity, teamName: string): void;

  /**
   * 将实体/名称移出队伍。
   * Removes an entity or name from its current team.
   */
  leaveTeam(entityOrName: string | GameEntity): void;

  /**
   * 获取实体/名称所在的队伍名 (不在任何队伍返回 null)。
   * Returns the team name of an entity or name, or null.
   */
  getTeamOf(entityOrName: string | GameEntity): string | null;

  // ── 世界边界 / World Border ──

  /** 当前边界大小。Current world border size. */
  borderSize: number;

  /**
   * 设置边界中心。
   * Sets the world border center.
   */
  setBorderCenter(x: number, z: number): void;

  /**
   * 缩放边界到目标大小 (带动画)。
   * Shrinks/grows the world border to a target size over time.
   * @param targetSize - 目标大小
   * @param seconds - 动画秒数
   */
  shrinkBorder(targetSize: number, seconds: number): void;

  /**
   * 边界伤害 (每秒造成的伤害值)。
   * World border damage per block per second.
   */
  setBorderDamage(damage: number): void;

  /**
   * 边界警告距离 (方块数)。
   * World border warning distance in blocks.
   */
  setBorderWarning(blocks: number): void;

  // ── 定时器 / Timers ──

  /**
   * 设置一次性延时回调。
   * Schedules a one‑shot delayed callback.
   * @param handler - 回调函数
   * @param ticks - 延迟 tick 数
   * @returns 定时器 ID (可用于 clearTimeout)
   */
  setTimeout(handler: () => void, ticks: number): number;

  /**
   * 设置循环定时回调。
   * Schedules a recurring interval callback.
   * @param handler - 回调函数
   * @param ticks - 间隔 tick 数
   * @returns 定时器 ID (可用于 clearInterval)
   */
  setInterval(handler: () => void, ticks: number): number;

  /** 取消 setTimeout。Clears a timeout by ID. */
  clearTimeout(id: number): void;

  /** 取消 setInterval。Clears an interval by ID. */
  clearInterval(id: number): void;

  // ── 项目间消息 / Cross‑project Messaging ──

  /**
   * 向另一个项目发送消息。
   * Sends a message to another script project.
   * @param target - 目标项目名 (不含路径)
   * @param data - 数据 (任意 JSON 可序列化的值)
   */
  sendMessage(target: string, data: unknown): void;

  // ═══════════════════════════════════════════════════
  //  事件注册 / Event Registration
  //  所有 onXxx() 返回 GameEventHandlerToken, 调用 .cancel() 取消监听。
  //  All onXxx() return GameEventHandlerToken; call .cancel() to unregister.
  // ═══════════════════════════════════════════════════

  /**
   * 注册每 tick 回调 (每秒 20 次)。
   * Registers a callback invoked every tick (20 times/sec).
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
   */
  onTick(handler: (info: TickInfo) => void): GameEventHandlerToken;

  /**
   * 注册玩家加入回调。
   * Registers a callback invoked when a player joins the server.
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
   */
  onPlayerJoin(
    handler: (entity: GamePlayerEntity, tick: number) => void,
  ): GameEventHandlerToken;

  /**
   * 注册玩家离开回调。
   * Registers a callback invoked when a player leaves the server.
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
   */
  onPlayerLeave(
    handler: (entity: GamePlayerEntity, tick: number) => void,
  ): GameEventHandlerToken;

  /**
   * 注册聊天消息回调 (包括 /me 消息)。
   * Registers a callback for chat messages (including /me).
   * @param handler - (entity, message, tick) => void
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
   */
  onChat(
    handler: (entity: GamePlayerEntity, message: string, tick: number) => void,
  ): GameEventHandlerToken;

  /**
   * 注册玩家重生回调。
   * Registers a callback invoked when a player respawns.
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
   */
  onPlayerRespawn(
    handler: (entity: GamePlayerEntity, tick: number) => void,
  ): GameEventHandlerToken;

  /**
   * 注册方块右键激活回调。
   * Registers a callback invoked when a player right‑clicks a block.
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
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
   * 注册方块破坏回调。
   * Registers a callback invoked when a player breaks a block.
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
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
   * 注册方块放置回调。
   * Registers a callback invoked when a player places a block.
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
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
   * 注册方块接触回调 (玩家移动到新方块时触发)。
   * Registers a callback invoked when a player's block position changes.
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
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
   * 注册实体交互回调 (玩家右键实体)。
   * Registers a callback invoked when a player right‑clicks an entity.
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
   */
  onInteract(
    handler: (
      entity: GamePlayerEntity,
      target: GameEntity,
      tick: number,
    ) => void,
  ): GameEventHandlerToken;

  /**
   * 注册实体死亡回调。
   * Registers a callback invoked when an entity dies.
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
   */
  onEntityDeath(
    handler: (
      entity: GameEntity,
      killer: GameEntity | null,
      tick: number,
    ) => void,
  ): GameEventHandlerToken;

  /**
   * 注册实体受伤回调。
   * Registers a callback invoked when an entity takes damage.
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
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
   * 注册流体进入回调 (玩家进入水/熔岩)。
   * Registers a callback invoked when a player enters a fluid.
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
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
   * 注册流体离开回调 (玩家离开水/熔岩)。
   * Registers a callback invoked when a player leaves a fluid.
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
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
   * 注册实体接触回调 (两个实体碰撞)。
   * Registers a callback invoked when two entities come into contact.
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
   */
  onEntityContact(
    handler: (entityA: GameEntity, entityB: GameEntity, tick: number) => void,
  ): GameEventHandlerToken;

  /**
   * 注册实体分离回调 (两个实体不再碰撞)。
   * Registers a callback invoked when two entities separate after contact.
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
   */
  onEntitySeparate(
    handler: (entityA: GameEntity, entityB: GameEntity, tick: number) => void,
  ): GameEventHandlerToken;

  /**
   * 注册按钮按下回调 — 当玩家按下指定按钮时触发。
   * Registers a callback for button presses from any player.
   * @param handler — `(entity, button, tick) => void`
   *
   * `button` 参数值是 {@link GameButtonType} 中的字符串常量之一：
   * WALK / RUN / CROUCH / JUMP / FLY / ACTION0 / ACTION1
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
   */
  onButtonPressed(
    handler: (entity: GamePlayerEntity, button: string, tick: number) => void,
  ): GameEventHandlerToken;

  /**
   * 注册跨项目消息回调。
   * Registers a callback for messages from other script projects.
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
   */
  onMessage(
    handler: (sender: string, data: unknown) => void,
  ): GameEventHandlerToken;
}

/**
 * raycast() 返回结果。
 * Return type of world.raycast().
 */
interface RaycastResult {
  /** 是否命中。True if something was hit. */
  hit: boolean;
  /** 命中点 X 坐标。Hit point X coordinate. */
  x: number;
  /** 命中点 Y 坐标。Hit point Y coordinate. */
  y: number;
  /** 命中点 Z 坐标。Hit point Z coordinate. */
  z: number;
  /** 表面法线 X 分量。Surface normal X component. */
  normalX: number;
  /** 表面法线 Y 分量。Surface normal Y component. */
  normalY: number;
  /** 表面法线 Z 分量。Surface normal Z component. */
  normalZ: number;
  /** 命中距离。Distance from origin to hit point. */
  distance: number;
  /** 命中的方块 ID (命中方块时为数字)。Hit block ID (number when a block was hit). */
  voxel?: number;
  /** 命中的实体 (命中实体时)。The entity that was hit (when an entity was hit). */
  entity?: GameEntity;
}

// ================================================================
//  §6  Voxels — 方块操作
// ================================================================

/**
 * 方块读写操作 — 脚本中通过 `voxels` 访问。
 * Voxel (block) read/write — accessed via `voxels` in scripts.
 *
 * @remarks
 * 所有坐标使用世界方块坐标 (整数)。
 * All coordinates are in world block space (integers).
 */
interface GameVoxels {
  // ── 世界尺寸 / World dimensions ──

  /**
   * 世界最大尺寸 (x, y, z 均为世界高度)。
   * Maximum world dimensions (x/y/z all equal world height).
   */
  readonly shape: GameVector3;

  /**
   * 所有可用的方块类型名称数组。
   * Array of all registered block type resource‑location strings.
   */
  readonly VoxelTypes: string[];

  // ── 名称 ↔ ID 映射 / Name–ID mapping ──

  /**
   * 将方块名称转为数字 ID。
   * Resolves a block name (e.g. "stone" or "minecraft:stone") to its numeric ID.
   * @returns 数字 ID, 未知方块的返回 0 (air)
   */
  id(name: string): number;

  /**
   * 将数字 ID 转为方块名称。
   * Resolves a numeric ID back to a block name string.
   * @returns ResourceLocation 字符串, 未知 ID 返回 "air"
   */
  name(id: number): string;

  // ── 读取 / Read ──

  /**
   * 获取方块数字 ID (不含旋转信息的基础 ID)。
   * Returns the base numeric block ID at the given position (without rotation encoding).
   * @returns 基础方块 ID, 空气返回 0 / base block ID, 0 for air
   */
  getVoxel(x: number, y: number, z: number): number;
  getVoxel(pos: GameVector3): number;

  /**
   * 获取方块数字 ID (不含旋转信息的基础 ID)。
   * Returns the base numeric block ID (without rotation encoding).
   */
  getVoxelId(x: number, y: number, z: number): number;
  getVoxelId(pos: GameVector3): number;

  /**
   * 获取方块名称 (如 "minecraft:stone")。
   * Returns the block name at the given position (e.g. "minecraft:stone").
   */
  getVoxelName(x: number, y: number, z: number): string;
  getVoxelName(pos: GameVector3): string;

  /**
   * 获取方块旋转值 (0‑3, 对应南/西/北/东)。
   * Returns the block rotation: 0=South, 1=West, 2=North, 3=East.
   */
  getVoxelRotation(x: number, y: number, z: number): number;
  getVoxelRotation(pos: GameVector3): number;

  // ── 写入 / Write ──

  /**
   * 放置方块 (名称或 ID)。返回含旋转编码的完整 ID。
   * Places a block by name or ID. Returns the full encoded ID (baseId + rotation * 16384).
   * @param voxel - 方块名称 (如 "minecraft:diamond_block") 或数字 ID
   * @returns 含旋转编码的完整方块 ID, 删除/空气返回 0
   */
  setVoxel(x: number, y: number, z: number, voxel: string | number): number;
  setVoxel(pos: GameVector3, voxel: string | number): number;

  /**
   * 放置方块并指定旋转。返回含旋转编码的完整 ID。
   * Places a block with explicit rotation.
   * @param voxel - 方块名称或数字 ID
   * @param rotation - 旋转值 0‑3 (或字符串 "0"‑"3")
   * @returns 含旋转编码的完整 ID
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
   * 放置已含旋转编码的完整 ID 方块。
   * Places a block using a rotation‑encoded full ID (from getVoxelId).
   * @param voxel - 完整编码 ID (baseId + rotation * 16384)
   */
  setVoxelId(x: number, y: number, z: number, voxel: number): number;
  setVoxelId(pos: GameVector3, voxel: number): number;

  // ── 区域操作 / Region operations ──

  /**
   * 在两个对角顶点定义的区域内填充方块。
   * Fills a cuboid region with a block.
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
   * 统计区域内指定方块的数量。
   * Counts matching blocks within a cuboid region.
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

  // ── 刷怪笼 / Spawner ──

  /**
   * 设置刷怪笼的生成实体类型。
   * Sets the spawner entity type at the given position.
   * @param x, y, z - 刷怪笼坐标 / spawner coordinates
   * @param entityType - 实体类型 ID (如 "minecraft:zombie")
   */
  setSpawner(x: number, y: number, z: number, entityType: string): void;
  setSpawner(pos: GameVector3, entityType: string): void;
}

// ================================================================
//  §7  Console — 控制台
// ================================================================

/**
 * 服务端控制台输出 — 脚本中通过 `console` 访问。
 * Server console output — accessed via `console` in scripts.
 *
 * @remarks
 * 输出格式: [Box3JS] [projectName] <message>
 * 会通过 System.out / System.err 输出到服务端控制台。
 */
interface GameConsole {
  /** 普通日志。Info‑level log. */
  log(...args: unknown[]): void;

  /** 调试日志 (前缀 [DEBUG])。Debug‑level log (prefixed with [DEBUG]). */
  debug(...args: unknown[]): void;

  /** 警告日志 (前缀 [WARN])。Warning‑level log (prefixed with [WARN]). */
  warn(...args: unknown[]): void;

  /**
   * 错误日志 (输出到 stderr, 前缀 [ERROR])。
   * Error‑level log (written to stderr, prefixed with [ERROR]).
   */
  error(...args: unknown[]): void;

  /**
   * 清除控制台 (发送 ANSI 清屏序列)。
   * Clears the console output (sends ANSI clear‑screen sequence).
   */
  clear(): void;

  /**
   * 断言: 条件为 false 时输出错误。
   * Asserts a condition; logs an error message if the condition is false.
   * @param condition - 要测试的条件 / the condition to test
   * @param args - 失败时输出的额外参数 / additional values to log on failure
   */
  assert(condition: boolean, ...args: unknown[]): void;
}

// ================================================================
//  §8  Enum Constants — 运行时枚举常量
// ================================================================

/**
 * 按钮类型 — 用于 world.onButtonPressed() 的 button 参数。
 * Button type constants for the button parameter of world.onButtonPressed().
 */
declare const GameButtonType: {
  readonly WALK: "WALK";
  readonly RUN: "RUN";
  readonly CROUCH: "CROUCH";
  readonly JUMP: "JUMP";
  readonly FLY: "FLY";
  readonly ACTION0: "ACTION0";
  readonly ACTION1: "ACTION1";
};

/**
 * 相机模式 — player.cameraMode 的取值。
 * Camera mode constants for the player.cameraMode property.
 */
declare const GameCameraMode: {
  readonly FOLLOW: "FOLLOW";
  readonly FPS: "FPS";
};

/**
 * 玩家移动状态 — player.moveState 的可能返回值。
 * Player movement state constants — possible return values of player.moveState.
 */
declare const GamePlayerMoveState: {
  readonly FLYING: "FLYING";
  readonly GROUND: "GROUND";
  readonly SWIM: "SWIM";
  readonly FALL: "FALL";
  readonly JUMP: "JUMP";
};

/**
 * 玩家行走状态 — player.walkState 的可能返回值。
 * Player walk state constants — possible return values of player.walkState.
 */
declare const GamePlayerWalkState: {
  readonly NONE: "NONE";
  readonly CROUCH: "CROUCH";
  readonly WALK: "WALK";
  readonly RUN: "RUN";
};

// ================================================================
//  §9  Global Declarations — 全局声明
// ================================================================

/** 世界控制与事件 API / World control & events */
declare const world: GameWorld;

/** 方块读写 API / Block read & write */
declare const voxels: GameVoxels;

/** 持久化存储 API / Persistent key‑value storage */
declare const storage: GameStorage;

/** 服务端控制台输出 / Server console output */
declare const console: GameConsole;

/**
 * 阻塞当前执行线程 (毫秒级)。
 * Blocks the current execution thread for the specified duration.
 *
 * @warning 会导致服务端卡顿, 谨慎使用。
 *          Will lag the server — use sparingly.
 * @param ms - 阻塞毫秒数 / sleep duration in milliseconds
 */
declare function sleep(ms: number): void;
