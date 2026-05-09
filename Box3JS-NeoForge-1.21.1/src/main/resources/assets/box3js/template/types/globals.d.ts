// ── §1 @zh 数学类型 @en Math Types ──

/**
 * @zh 三维向量，所有坐标使用世界坐标（方块坐标，非像素）。
 * @en A 3‑dimensional vector with double‑precision components.
 * All coordinates are in world space (block coordinates, not pixels).
 */
declare class GameVector3 {
  /** @zh X 分量（东‑西） @en X component (east‑west) */
  x: number;
  /** @zh Y 分量（上‑下） @en Y component (up‑down) */
  y: number;
  /** @zh Z 分量（南‑北） @en Z component (north‑south) */
  z: number;

  /** @zh 创建一个零向量 (0, 0, 0)。 @en Creates a zero vector at origin. */
  constructor();

  /**
   * @zh 创建一个指定坐标的向量。
   * @en Creates a vector with the given coordinates.
   * @param x - @zh X 坐标 @en X coordinate
   * @param y - @zh Y 坐标 @en Y coordinate
   * @param z - @zh Z 坐标 @en Z coordinate
   */
  constructor(x: number, y: number, z: number);

  /**
   * @zh 设置向量的 X / Y / Z 分量（会改变调用者自身）。
   * @en Sets all three components in‑place (mutates the vector).
   * @returns @zh 调用者本身 @en this vector
   */
  set(x: number, y: number, z: number): GameVector3;

  /** @zh 原地复制 v 的值。 @en Copies values from v in‑place. */
  copy(v: GameVector3): GameVector3;

  /** @zh 深拷贝。 @en Returns a new independent copy. */
  clone(): GameVector3;

  /**
   * @zh 向量加法：this + v。
   * @en Vector addition: this + v.
   * @returns @zh 一个新向量 @en a new vector
   */
  add(v: GameVector3): GameVector3;

  /**
   * @zh 向量减法：this - v。
   * @en Vector subtraction: this - v.
   * @returns @zh 一个新向量 @en a new vector
   */
  sub(v: GameVector3): GameVector3;

  /** @zh 逐分量乘法（返回新对象）。 @en Component‑wise multiplication (returns new vector). */
  mul(v: GameVector3): GameVector3;

  /** @zh 逐分量除法（返回新对象，除以 0 得 0）。 @en Component‑wise division (divide‑by‑zero → 0). */
  div(v: GameVector3): GameVector3;

  /**
   * @zh 标量乘法：每个分量乘以 n。
   * @en Scalar multiplication: each component multiplied by n.
   * @returns @zh 一个新向量 @en a new vector
   */
  scale(n: number): GameVector3;

  /** @zh 原地缩放。 @en Scale in‑place. */
  scaleEq(n: number): GameVector3;

  /** @zh 反向向量（返回新对象）。 @en Negation (returns new vector). */
  neg(): GameVector3;

  /** @zh 原地反向。 @en Negation in‑place. */
  negEq(): GameVector3;

  /** @zh 原地加法。 @en Addition in‑place. */
  addEq(v: GameVector3): GameVector3;

  /** @zh 原地减法。 @en Subtraction in‑place. */
  subEq(v: GameVector3): GameVector3;

  /** @zh 原地乘法。 @en Multiplication in‑place. */
  mulEq(v: GameVector3): GameVector3;

  /** @zh 原地除法（除以 0 跳过该分量）。 @en Division in‑place (divide‑by‑zero skips that component). */
  divEq(v: GameVector3): GameVector3;

  /** @zh 点积（内积）：this · v。 @en Dot (inner) product: this · v. */
  dot(v: GameVector3): number;

  /** @zh 叉积：this × v。 @en Cross product. */
  cross(v: GameVector3): GameVector3;

  /** @zh 向量长度（模）。 @en Magnitude (length) of this vector. */
  mag(): number;

  /** @zh 向量长度的平方（比 `mag()` 更快）。 @en Squared magnitude — faster than `mag()` when comparing distances. */
  sqrMag(): number;

  /**
   * @zh 单位化：返回方向相同、长度为 1 的新向量。零向量会返回 (0,0,0)。
   * @en Normalizes this vector; returns a unit vector in the same direction.
   * @zh Zero vector returns (0,0,0).
   */
  normalize(): GameVector3;

  /** @zh 计算 this 与 v 之间的欧几里得距离。 @en Euclidean distance between this and v. */
  distance(v: GameVector3): number;

  /** @zh 到 v 的平方距离（比 distance() 快，适合排序/比较）。 @en Squared distance to v — faster than distance() for comparisons. */
  sqrDistance(v: GameVector3): number;

  /**
   * @zh 线性插值：在 this 和 v 之间按比率 n 插值。
   * @en Linear interpolation between this and v by ratio n.
   * @param n - @zh 插值比率（0=this，1=v） @en interpolation factor (0=this, 1=v)
   */
  lerp(v: GameVector3, n: number): GameVector3;

  /** @zh 匀速移向目标点（步长 maxDelta，到目标后返回 target 的拷贝）。 @en Moves toward target by at most maxDelta. Returns copy of target when reached. */
  moveTowards(target: GameVector3, maxDelta: number): GameVector3;

  /** @zh 指向 v 的方向向量（已单位化）。 @en Direction vector pointing toward v (normalized). */
  towards(v: GameVector3): GameVector3;

  /** @zh this 与 v 之间的夹角（弧度）。 @en Angle between this and v in radians. */
  angle(v: GameVector3): number;

  /** @zh 近似相等检查（容差 1e‑6）。 @en Approximate equality within 1e‑6 tolerance. */
  equals(v: GameVector3): boolean;

  /** @zh 精确相等检查（分量完全相等）。 @en Exact component‑wise equality. */
  exactEquals(v: GameVector3): boolean;

  /** @zh 逐分量取较大值（返回新对象）。 @en Component‑wise max. */
  max(v: GameVector3): GameVector3;

  /** @zh 逐分量取较小值（返回新对象）。 @en Component‑wise min. */
  min(v: GameVector3): GameVector3;

  /** @zh 是否为零向量（容差 1e‑6）。 @en True if all components are within 1e‑6 of zero. */
  isZero(): boolean;

  /** @zh 逐分量向下取整（常用于方块坐标）。 @en Component‑wise floor (useful for block coordinates). */
  floor(): GameVector3;

  /** @zh 逐分量向上取整。 @en Component‑wise ceil. */
  ceil(): GameVector3;

  /** @zh 限制向量长度不超过 max（超长时返回归一化后缩放到 max 的新向量）。 @en Clamps length to max, returning a new vector. */
  clampLength(max: number): GameVector3;

  /**
   * @zh 从球坐标创建向量。
   * @en Creates a vector from spherical coordinates.
   * @param mag - @zh 半径 @en radius (magnitude)
   * @param phi - @zh 方位角（弧度，绕 Y 轴水平旋转） @en azimuth angle (radians, horizontal rotation around Y)
   * @param theta - @zh 仰角（弧度，从水平面起算） @en elevation angle (radians, from horizontal plane)
   */
  static fromPolar(mag: number, phi: number, theta: number): GameVector3;

  /** @zh 返回 "(x, y, z)" 格式的字符串表示。 @en Returns a string in "(x, y, z)" format. */
  toString(): string;
}

/**
 * @zh 三维轴对齐包围盒（AABB），由两个对角顶点 lo（最小角）和 hi（最大角）定义。
 * @en Axis‑aligned 3‑dimensional bounding box,
 * defined by two opposing corners: lo (minimum corner) and hi (maximum corner).
 */
declare class GameBounds3 {
  /** @zh 最小角（三个分量均为最小值）。 @en Lower/minimum corner. */
  lo: GameVector3;
  /** @zh 最大角（三个分量均为最大值）。 @en Upper/maximum corner. */
  hi: GameVector3;

  /** @zh 用两个对角顶点构造包围盒。 @en Constructs bounds from two opposing corners. */
  constructor(lo: GameVector3, hi: GameVector3);

  /** @zh 原地设置所有边界。 @en Sets all boundaries in‑place. */
  set(
    lox: number,
    loy: number,
    loz: number,
    hix: number,
    hiy: number,
    hiz: number,
  ): GameBounds3;

  /** @zh 原地复制 b 的值。 @en Copies values from b in‑place. */
  copy(b: GameBounds3): GameBounds3;

  /** @zh 判断当前包围盒是否与 other 相交。 @en Returns true if this bounds intersects with other. */
  intersects(other: GameBounds3): boolean;

  /** @zh 计算交集包围盒（无交集返回 null）。 @en Returns the intersection bounds, or null if they don't overlap. */
  intersect(other: GameBounds3): GameBounds3 | null;

  /** @zh 判断点 v 是否位于包围盒内部（含边界）。 @en Returns true if point v is inside (or on the boundary of) this bounds. */
  contains(v: GameVector3): boolean;

  /** @zh 判断是否完全包含另一个包围盒。 @en Whether this bounds fully contains b. */
  containsBounds(b: GameBounds3): boolean;

  /** @zh 返回包围盒中心点。 @en Returns the center point of the bounds. */
  center(): GameVector3;

  /** @zh 返回包围盒尺寸 (hi‑lo)。 @en Returns the size (hi‑lo) as a vector. */
  size(): GameVector3;

  /** @zh 扩展包围盒，各方向扩大 delta（返回新对象）。 @en Returns a new bounds expanded by delta on all sides. */
  expand(delta: number): GameBounds3;

  /** @zh 原地扩展包围盒。 @en Expands the bounds in‑place. */
  expandEq(delta: number): GameBounds3;

  /** @zh 扩展包围盒以包含点 v（原地）。 @en Grows the bounds to include point v in‑place. */
  growToInclude(v: GameVector3): GameBounds3;

  /** @zh 包围盒上距离 v 最近的点（v 在内部时返回投影到表面的点）。 @en Closest point on the bounds surface to v. */
  closestPoint(v: GameVector3): GameVector3;

  /** @zh 平移包围盒（返回新对象）。 @en Translates the bounds by offset (returns new object). */
  move(offset: GameVector3): GameBounds3;

  /** @zh 原地平移包围盒。 @en Translates the bounds in‑place. */
  moveEq(offset: GameVector3): GameBounds3;

  /** @zh 从 GameVector3 数组创建最小包围盒。 @en Creates bounds from an array of GameVector3. */
  static fromPoints(points: GameVector3[]): GameBounds3 | null;

  toString(): string;
}

// ──────────────────────────────────────────────

/**
 * @zh RGB 颜色，三个通道，每通道 0.0–1.0。
 * @en An RGB color with three channels ranging from 0.0 to 1.0.
 */
declare class GameRGBColor {
  /** @zh 红色通道（0.0–1.0）。 @en Red channel. */
  r: number;
  /** @zh 绿色通道（0.0–1.0）。 @en Green channel. */
  g: number;
  /** @zh 蓝色通道（0.0–1.0）。 @en Blue channel. */
  b: number;

  /** @zh 用指定的 R / G / B 值创建颜色。 @en Creates a color with the given R/G/B values. */
  constructor(r: number, g: number, b: number);

  /** @zh 原地设置所有通道。 @en Sets all three channels in‑place. */
  set(r: number, g: number, b: number): GameRGBColor;

  /** @zh 原地复制另一个颜色的值。 @en Copies values from another color in‑place. */
  copy(o: GameRGBColor): GameRGBColor;

  /** @zh 深拷贝。 @en Returns a new independent copy. */
  clone(): GameRGBColor;

  /** @zh 逐通道加法（返回新对象）。 @en Channel‑wise addition (returns new object). */
  add(o: GameRGBColor): GameRGBColor;

  /** @zh 逐通道减法（返回新对象）。 @en Channel‑wise subtraction (returns new object). */
  sub(o: GameRGBColor): GameRGBColor;

  /** @zh 逐通道乘法（返回新对象）。 @en Channel‑wise multiplication (returns new object). */
  mul(o: GameRGBColor): GameRGBColor;

  /** @zh 逐通道除法（返回新对象，除以 0 得 0）。 @en Channel‑wise division (divide‑by‑zero → 0). */
  div(o: GameRGBColor): GameRGBColor;

  /** @zh 原地加法。 @en Addition in‑place. */
  addEq(o: GameRGBColor): GameRGBColor;

  /** @zh 原地减法。 @en Subtraction in‑place. */
  subEq(o: GameRGBColor): GameRGBColor;

  /** @zh 原地乘法。 @en Multiplication in‑place. */
  mulEq(o: GameRGBColor): GameRGBColor;

  /** @zh 原地除法（除以 0 跳过该通道）。 @en Division in‑place (divide‑by‑zero skips that channel). */
  divEq(o: GameRGBColor): GameRGBColor;

  /** @zh 标量乘法（返回新对象，常用于亮度调整）。 @en Scalar multiply — brighten (n>1) or darken (n<1). */
  scale(n: number): GameRGBColor;

  /** @zh 原地标量乘法。 @en Scalar multiply in‑place. */
  scaleEq(n: number): GameRGBColor;

  /** @zh 在 this 和 o 之间线性插值。 @en Linear interpolation between this and o by ratio n. */
  lerp(o: GameRGBColor, n: number): GameRGBColor;

  /** @zh 近似相等检查（容差 1e‑6）。 @en Approximate equality within 1e‑6 tolerance. */
  equals(o: GameRGBColor): boolean;

  /** @zh 转为 "rgba(r,g,b,1.0)" 格式字符串。 @en Converts to an rgba CSS string. */
  toRGBA(): string;

  /** @zh 生成一个随机 RGB 颜色（每个通道 0–1）。 @en Generates a random RGB color (each channel 0–1). */
  static random(): GameRGBColor;

  toString(): string;
}

// ──────────────────────────────────────────────

/**
 * @zh RGBA 颜色，四个通道，每通道 0.0–1.0。
 * @en An RGBA color; all four channels range from 0.0 to 1.0.
 */
declare class GameRGBAColor {
  /** @zh 红色通道（0.0–1.0）。 @en Red channel. */
  r: number;
  /** @zh 绿色通道（0.0–1.0）。 @en Green channel. */
  g: number;
  /** @zh 蓝色通道（0.0–1.0）。 @en Blue channel. */
  b: number;
  /** @zh Alpha（不透明度），范围 0.0–1.0。 @en Alpha (opacity), range 0.0–1.0. */
  a: number;

  constructor(r: number, g: number, b: number, a: number);

  /** @zh 原地设置所有四个通道。 @en Sets all four channels in‑place. */
  set(r: number, g: number, b: number, a: number): GameRGBAColor;

  /** @zh 原地复制另一个颜色的值。 @en Copies values from another RGBA color in‑place. */
  copy(c: GameRGBAColor): GameRGBAColor;

  /** @zh 深拷贝。 @en Returns a new independent copy. */
  clone(): GameRGBAColor;

  /** @zh 逐通道加法（返回新对象）。 @en Channel‑wise addition (returns new object). */
  add(rgba: GameRGBAColor): GameRGBAColor;

  /** @zh 逐通道减法（返回新对象）。 @en Channel‑wise subtraction (returns new object). */
  sub(rgba: GameRGBAColor): GameRGBAColor;

  /** @zh 逐通道乘法（返回新对象）。 @en Channel‑wise multiplication (returns new object). */
  mul(rgba: GameRGBAColor): GameRGBAColor;

  /** @zh 逐通道除法（返回新对象，除以 0 得 0）。 @en Channel‑wise division (returns new object; divide‑by‑zero → 0). */
  div(rgba: GameRGBAColor): GameRGBAColor;

  /** @zh 原地加法。 @en Addition in‑place. */
  addEq(rgba: GameRGBAColor): GameRGBAColor;

  /** @zh 原地减法。 @en Subtraction in‑place. */
  subEq(rgba: GameRGBAColor): GameRGBAColor;

  /** @zh 原地乘法。 @en Multiplication in‑place. */
  mulEq(rgba: GameRGBAColor): GameRGBAColor;

  /** @zh 原地除法（除以 0 跳过该通道）。 @en Division in‑place (divide‑by‑zero skips that channel). */
  divEq(rgba: GameRGBAColor): GameRGBAColor;

  /** @zh 标量乘法（返回新对象，包括 alpha 通道）。 @en Scalar multiply — all channels including alpha. */
  scale(n: number): GameRGBAColor;

  /** @zh 原地标量乘法。 @en Scalar multiply in‑place. */
  scaleEq(n: number): GameRGBAColor;

  /** @zh 线性插值。 @en Linear interpolation between this and rgba by ratio n. */
  lerp(rgba: GameRGBAColor, n: number): GameRGBAColor;

  /** @zh 近似相等检查（容差 1e‑6）。 @en Approximate equality within 1e‑6 tolerance. */
  equals(rgba: GameRGBAColor): boolean;

  /**
   * @zh Alpha 混合：将自身 RGBA 颜色混合到 RGB 背景上。
   * @en Blends this RGBA color onto an RGB background, returning the displayed RGB.
   */
  blendEq(rgb: GameRGBColor): GameRGBColor;

  toString(): string;
}

// ──────────────────────────────────────────────

/**
 * @zh 四元数，用于三维旋转。单位四元数（w²+x²+y²+z²=1）表示纯旋转。
 * @en A quaternion used for 3‑dimensional rotation.
 * Unit quaternions represent pure rotations.
 */
declare class GameQuaternion {
  /** @zh 实部（标量分量）。 @en Real (scalar) component. */
  w: number;
  /** @zh 虚部 X 分量。 @en Imaginary X component. */
  x: number;
  /** @zh 虚部 Y 分量。 @en Imaginary Y component. */
  y: number;
  /** @zh 虚部 Z 分量。 @en Imaginary Z component. */
  z: number;

  /** @zh 创建单位四元数 (1, 0, 0, 0)。 @en Creates an identity quaternion. */
  constructor();

  /** @zh 用指定的 w/x/y/z 分量创建四元数。 @en Creates a quaternion with the given w/x/y/z components. */
  constructor(w: number, x: number, y: number, z: number);

  /** @zh 原地设置所有分量。 @en Sets all components in‑place. */
  set(w: number, x: number, y: number, z: number): GameQuaternion;

  /** @zh 原地复制。 @en Copies values from another quaternion in‑place. */
  copy(v: GameQuaternion): GameQuaternion;

  /** @zh 深拷贝。 @en Returns a new independent copy. */
  clone(): GameQuaternion;

  /** @zh 逐分量加法。 @en Component‑wise addition. */
  add(v: GameQuaternion): GameQuaternion;

  /** @zh 逐分量减法。 @en Component‑wise subtraction. */
  sub(v: GameQuaternion): GameQuaternion;

  /**
   * @zh 四元数乘法（汉密尔顿积）：this × q。注意乘法不满足交换律。
   * @en Hamilton product: this × q. Multiplication is NOT commutative.
   */
  mul(q: GameQuaternion): GameQuaternion;

  /** @zh 共轭四元数（对单位四元数等价于逆）。 @en Conjugate of this quaternion (equals inverse for unit quaternions). */
  inv(): GameQuaternion;

  /** @zh 除法：this × q⁻¹。 @en Division: this × q⁻¹. */
  div(q: GameQuaternion): GameQuaternion;

  /** @zh 点积：this · q。 @en Dot product. */
  dot(q: GameQuaternion): number;

  /** @zh 模长（范数）。 @en Magnitude (norm). */
  mag(): number;

  /** @zh 模长平方。 @en Squared magnitude. */
  sqrMag(): number;

  /** @zh 单位化：返回模长为 1 的新四元数。 @en Normalizes this quaternion; returns a unit quaternion. */
  normalize(): GameQuaternion;

  /**
   * @zh 球面线性插值（Slerp）：在 this 和 q 之间平滑旋转。
   * @en Spherical linear interpolation — smooth rotation between this and q.
   * @param t - @zh 插值比率（0=this，1=q） @en interpolation factor (0=this, 1=q)
   */
  slerp(q: GameQuaternion, t: number): GameQuaternion;

  /** @zh 返回 this 和 q 之间的角度（弧度）。 @en Angular difference between this and q (in radians). */
  angle(q: GameQuaternion): number;

  /**
   * @zh 返回四元数对应的轴‑角表示。
   * @en Decomposes this quaternion into axis‑angle representation.
   * @returns @zh 包含 `angle` 和 `axis` 字段的对象 @en object with `angle` and `axis` fields
   */
  getAxisAngle(): AxisAngle;

  // ── @zh 旋转操作 @en Rotation operations ──

  /** @zh 绕 X 轴旋转（在左侧乘以旋转四元数）。 @en Rotate around X axis. */
  rotateX(rad: number): GameQuaternion;
  /** @zh 绕 Y 轴旋转。 @en Rotate around Y axis. */
  rotateY(rad: number): GameQuaternion;
  /** @zh 绕 Z 轴旋转。 @en Rotate around Z axis. */
  rotateZ(rad: number): GameQuaternion;

  /** @zh 用此单位四元数旋转向量 v。 @en Rotates vector v by this unit quaternion. */
  rotateVector(v: GameVector3): GameVector3;

  /** @zh 转为 YZX 欧拉角（弧度），x/y/z 分别对应绕 X/Y/Z 轴的旋转角。 @en Decomposes into YZX Euler angles (radians). */
  toEuler(): GameVector3;

  // ── @zh 静态构造器 @en Static constructors ──

  /** @zh 从轴‑角表示创建四元数。 @en Create from axis‑angle representation. */
  static fromAxisAngle(axis: GameVector3, rad: number): GameQuaternion;

  /** @zh 从欧拉角创建四元数（YZX 旋转顺序：Y → Z → X）。 @en Create from Euler angles (YZX rotation order: Y → Z → X). */
  static fromEuler(x: number, y: number, z: number): GameQuaternion;

  /** @zh 计算从向量 a 旋转到向量 b 的最短弧四元数。 @en Shortest‑arc quaternion rotating from vector a to vector b. */
  static rotationBetween(a: GameVector3, b: GameVector3): GameQuaternion;

  /** @zh 构造从 from 看向 to 的朝向四元数（up 为上方向）。 @en Builds a look‑at quaternion orienting -Z from `from` toward `to`. */
  static lookAt(from: GameVector3, to: GameVector3, up: GameVector3): GameQuaternion;

  /** @zh 近似相等检查（容差 1e‑6）。 @en Approximate equality check within 1e‑6 tolerance. */
  equals(v: GameQuaternion): boolean;

  toString(): string;
}

/**
 * @zh 轴‑角表示，由 `getAxisAngle()` 返回。
 * @en Axis‑angle representation returned by `getAxisAngle()`.
 */
interface AxisAngle {
  /** @zh 旋转角度 (弧度) @en rotation angle in radians */
  angle: number;
  /** @zh 旋转轴 (单位向量) @en rotation axis (unit vector) */
  axis: GameVector3;
}

/**
 * @zh 事件处理器令牌，由 `world.onXxx()` 返回。
 * 调用 `cancel()` 取消监听后不可恢复，需重新注册。
 * @en Event handler token returned by `world.onXxx()`.
 * Once cancelled via `cancel()`, it cannot be resumed — re-register instead.
 */
declare class GameEventHandlerToken {
  /** @zh 取消事件监听 (不可逆) @en Cancels the event listener (irreversible). */
  cancel(): void;

  /**
   * @zh 尝试恢复已取消的监听 (会抛出 UnsupportedOperationException)。
   * @en Attempts to resume a cancelled listener — always throws UnsupportedOperationException.
   * @throws UnsupportedOperationException 始终抛出 / always thrown
   */
  resume(): void;

  /** @zh 返回 true 表示监听仍处于活跃状态 @en Returns true if the listener is still active. */
  active(): boolean;
}

/**
 * @zh `onTick` 回调的参数类型。
 * @en The info object passed to `onTick` handlers.
 */
interface TickInfo {
  /** @zh 当前 tick 数 @en Current tick count. */
  tick: number;
  /** @zh 上一 tick 数 @en Previous tick count. */
  prevTick: number;
  /** @zh 自启动以来的毫秒数 @en Milliseconds elapsed since server start. */
  elapsedTimeMS: number;
  /** @zh 跳过的 tick 数 (MC 下始终为 0) @en Number of skipped ticks (always 0 in MC). */
  skip: number;
}

// ── §2 @zh 持久化存储 @en Persistent Storage ──

/**
 * @zh JSON 可序列化的值类型，用作 `GameDataStorage<T>` 的默认类型参数。
 * @en Represents any JSON‑serializable value.
 * Used as the default type parameter for `GameDataStorage<T>`.
 */
type JSONValue = string | number | boolean | null | JSONValue[] | { [key: string]: JSONValue };

/**
 * @zh 数据存储空间（键值持久化），通过 `storage.getDataStorage<T>("name")` 获取。
 * T 指定后所有读写操作自动获得类型检查。
 *
 * @en A data‑storage namespace — persistent key‑value store backed by JSON files.
 * Obtain via `storage.getDataStorage<T>("name")`; once T is set, all read/write operations are type‑checked.
 *
 * @example
 * ```ts
 * const coins = storage.getDataStorage<number>("coins");
 * const balance = coins.get(userId);  // number | null
 * coins.set(userId, 100);             // value must be number
 * ```
 */
interface GameDataStorage<T = JSONValue> {
  /**
   * @zh 获取存储空间名称 (只读)。
   * @en Returns the read‑only namespace name.
   */
  readonly key: string;

  /**
   * @zh 存入一个键值对。值必须是可 JSON 序列化的类型。
   * @en Stores a key‑value pair. Value must be JSON‑serializable.
   * @param key - 键 / key
   * @param value - 值 / value (typed to T)
   */
  set(key: string, value: T): void;

  /**
   * @zh 读取键对应的值, 不存在则返回 null。
   * @en Retrieves the value for a key, or null if it does not exist.
   * @returns 存储的值, 或 null
   */
  get(key: string): T | null;

  /**
   * @zh 获取当前存储空间中的所有键。
   * @en Lists all keys in this storage namespace.
   */
  keys(): string[];

  /**
   * @zh 原子更新: 取出当前值, 用 handler(currentValue) 的结果覆盖。
   * @en Atomically updates a value using a callback.
   * @param key - 键 / key
   * @param handler - (prevValue) => newValue / callback receiving the old value, returning the new one
   * @remarks 如果键不存在, 不会创建新条目 (遵循 Box3 规范)。
   *          If the key does not exist, nothing happens (per Box3 spec).
   *
   * @example
   * const list = storage.getDataStorage<string[]>("list");
   * list.update("items", (prev) => prev.concat("newItem"));
   */
  update(key: string, handler: (prevValue: T) => T): void;

  /**
   * @zh 删除键, 返回旧值 (不存在则返回 null)。
   * @en Removes a key and returns its previous value, or null.
   * @returns 被删除的旧值 / the previous value, or null
   */
  remove(key: string): T | null;

  /**
   * @zh 原子递增 (delta 默认为 1)。
   * @en Atomically increments a numeric value by delta (default 1).
   * @param key - 键 / key
   * @param delta - 增量 (可选, 默认 1) / increment amount (optional, default 1)
   * @returns 递增后的新值 / the new value after incrementing
   * @remarks 键不存在时从 0 + delta 开始。
   *          If the key doesn't exist, starts from 0 + delta.
   */
  increment(key: string, delta?: number): number;

  /**
   * @zh 分页查询存储条目。
   * @en Paginated query of stored entries.
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
  }): QueryList<T>;

  /**
   * @zh 销毁该存储空间 (删除对应 JSON 文件)。
   * @en Destroys this storage namespace (deletes the backing JSON file).
   */
  destroy(): void;
}

/**
 * @zh 分页查询结果，由 `GameDataStorage.list()` 返回。
 * @en Paginated query result returned by `GameDataStorage.list()`.
 */
interface QueryList<T = JSONValue> {
  /** @zh 是否已到达最后一页 @en Whether the last page has been reached. */
  isLastPage: boolean;

  /**
   * @zh 获取当前页的条目数组。
   * @en Returns the entries for the current page.
   */
  getCurrentPage(): ReturnValue<T>[];

  /**
   * @zh 移动到下一页。
   * @en Advances the cursor to the next page.
   */
  nextPage(): void;
}

/**
 * @zh 单个存储条目，包含元数据。
 * @en A single stored entry with metadata.
 */
interface ReturnValue<T = JSONValue> {
  /** @zh 键名 @en key name */
  key: string;
  /** @zh 值 @en stored value */
  value: T;
  /** @zh 更新时间 (Unix 毫秒) @en last‑modified timestamp (Unix ms) */
  updateTime: number;
  /** @zh 创建时间 (Unix 毫秒) @en creation timestamp (Unix ms) */
  createTime: number;
  /** @zh 版本标识符 (可用于乐观锁) @en version identifier (usable for optimistic locking) */
  version: string;
}

/**
 * @zh 全局存储入口 — 脚本中通过 `storage` 访问。
 * 项目间数据隔离：每个项目自动使用项目名作为存储文件前缀。
 * 跨项目共享：`getGroupStorage` 使用 `__shared__/` 命名空间，所有项目访问同一数据。
 *
 * @en Global storage entry point — accessed via `storage` in scripts.
 * Per‑project isolation: each project's storage is automatically prefixed with the project name.
 * Cross‑project sharing: `getGroupStorage` uses a `__shared__/` namespace visible to all projects.
 */
interface GameStorage {
  /** @zh 始终返回空字符串 (MC 本地存储无 key, 只读) @en Always returns "" for MC local storage, readonly. */
  readonly key: string;

  /**
   * @zh 打开或创建指定名称的数据存储空间 (项目隔离)。
   * @en Opens or creates a named data‑storage namespace (per‑project isolated).
   * @param name - 命名空间 (可含 "/" 作为目录分隔) / namespace (may contain "/" as directory separator)
   * @remarks 不同项目使用同一 name 会访问不同文件。
   *          Different projects using the same name access different files.
   *
   * @example
   * const coins = storage.getDataStorage<number>("coins");
   * const balance = coins.get(userId);  // number | null
   */
  getDataStorage<T = JSONValue>(name: string): GameDataStorage<T>;

  /**
   * @zh 获取跨项目共享存储 — 所有项目通过同一 name 读写同一份数据。
   * @en Shared cross‑project storage — all projects read/write the same data by name.
   * @param name - 命名空间 / namespace
   * @remarks 底层使用 `__shared__/` 前缀, 适合全服排行榜、全局配置等场景。
   *          Uses `__shared__/` prefix internally; suitable for global leaderboards, shared config, etc.
   */
  getGroupStorage<T = JSONValue>(name: string): GameDataStorage<T>;
}

/**
 * @zh SQL 查询结果，支持迭代和 thenable 模式。
 * SELECT 查询返回行数据，INSERT/UPDATE/DELETE 返回受影响行数。
 *
 * @en SQL query result — supports iteration and thenable pattern.
 * SELECT queries return row data; INSERT/UPDATE/DELETE return affected row count.
 *
 * @example
 * ```ts
 * // 获取所有行 / Get all rows
 * const rows = db.sql("SELECT * FROM players").rows;
 *
 * // 逐行迭代 / Iterate row by row
 * const result = db.sql("SELECT * FROM players");
 * let row;
 * while (!(row = result.next()).done) {
 *   console.log(row.value.name);
 * }
 *
 * // Thenable 模式 / Thenable pattern
 * db.sql("SELECT * FROM players").then((rows) => console.log(rows.length));
 * ```
 */
interface GameQueryResult<T = Record<string, any>> {
  /** @zh 所有行 @en All rows */
  readonly rows: T[];

  /** @zh 第一行，无结果时返回 null @en First row, or null if empty */
  readonly firstRow: T | null;

  /** @zh 列名数组 @en Column name array */
  readonly columnNames: string[];

  /** @zh 列数 @en Column count */
  readonly columnCount: number;

  /** @zh 行数 (SELECT) @en Row count (for SELECT queries) */
  readonly rowCount: number;

  /**
   * @zh 受影响行数 (INSERT/UPDATE/DELETE)，SELECT 查询返回 -1。
   * @en Affected row count for INSERT/UPDATE/DELETE; -1 for SELECT queries.
   */
  readonly affectedRows: number;

  /** @zh 是否为查询 (SELECT) @en Whether this is a query (SELECT) */
  readonly isQuery: boolean;

  /**
   * @zh 返回下一行: `{done: boolean, value: T}`。
   * @en Returns the next row as `{done: boolean, value: T}`.
   */
  next(): { done: boolean; value: T };

  /** @zh 重置内部游标到第一行 @en Resets internal cursor to first row */
  reset(): void;

  /**
   * @zh Thenable 支持 — resolve 接收所有行数组。
   * @en Thenable support — resolve receives the full row array.
   */
  then(resolve: (rows: T[]) => void, reject?: (err: string) => void): void;
}

/**
 * @zh SQLite 数据库，自动按项目隔离到 `config/box3/data/<project>.db`。
 * 通过全局 `db` 访问，支持 `?` 占位符和 tagged template 两种调用约定。
 *
 * @en SQLite database — auto-isolated per project at `config/box3/data/<project>.db`.
 * Access via global `db`, supports both `?` placeholder and tagged template calling conventions.
 *
 * @example
 * ```ts
 * // 常规查询 / Regular query
 * db.sql("SELECT * FROM players WHERE score > ?", 100);
 *
 * // Tagged template (TS 编译后) / Tagged template (after TS compilation)
 * db.sql`SELECT * FROM players WHERE id = ${playerId}`;
 *
 * // INSERT / UPDATE / DELETE
 * db.sql("INSERT INTO log (name, msg) VALUES (?, ?)", "Steve", "hello");
 * db.sql("DELETE FROM temp WHERE created < ?", Date.now() - 86400000);
 * ```
 */
interface GameDatabase {
  /**
   * @zh 执行 SQL 查询或更新。
   * @en Executes a SQL query or update.
   *
   * @param sql - SQL 字符串 (含 ? 占位符) 或字符串数组 (模板字面量)。
   *             SQL string (with ? placeholders) or string array (template literal).
   * @param params - 参数值 (number | string | boolean | null | Uint8Array)
   *                 Parameter values to bind.
   * @returns 查询结果 / query result
   *
   * @example
   * // 指定行类型获得完整类型检查 / Specify row type for full type-checking
   * const rows = db.sql<PlayerRow>("SELECT * FROM players").rows;
   * // rows: PlayerRow[]
   *
   * // 不指定则默认为 Record<string, any> / Defaults to Record<string, any>
   * const rows = db.sql("SELECT * FROM players").rows;
   * // rows: Record<string, any>[]
   */
  sql<T = Record<string, any>>(
    sql: string | readonly string[],
    ...params: (number | string | boolean | null | Uint8Array)[]
  ): GameQueryResult<T>;
}

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
   * @param amount - 伤害值 (半心) / damage amount in half‑hearts
   */
  hurt(amount: number): void;

  /**
   * @zh 治疗实体。
   * @en Heals the entity.
   * @param amount - 治疗量 (半心) / healing amount in half‑hearts
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
   * @param attributeId - 属性 ID (如 "minecraft:generic.max_health")
   * @returns 当前属性值, 不支持的实体返回 0
   */
  getAttribute(attributeId: string): number;

  /**
   * @zh 设置实体属性基础值。
   * @en Sets the base value of a registered entity attribute.
   * @param attributeId - 属性 ID (如 "minecraft:generic.movement_speed")
   * @param value - 新基础值 / new base value
   * @remarks 仅对 LivingEntity 有效。Only works on living entities.
   */
  setAttribute(attributeId: string, value: number): void;

  // ── @zh 装备 @en Equipment ──

  /**
   * @zh 给生物设置装备。
   * @en Equips an item onto a mob's equipment slot.
   * @param slot - 槽位名称 / slot name:
   *   "mainhand", "offhand", "head"/"helmet"/"helm",
   *   "chest"/"chestplate", "legs"/"leggings", "feet"/"boots"
   * @param itemId - 物品 ID (如 "minecraft:diamond_sword")
   */
  setEquipment(slot: string, itemId: string): void;

  /**
   * @zh 设置装备掉落概率。
   * @en Sets the drop chance for an equipment slot.
   * @param slot - 槽位名称 或 "all" / slot name or "all" for every slot
   * @param chance - 掉落概率 (0‑1) / drop chance (0–1)
   */
  setDropChance(slot: string, chance: number): void;

  // ── @zh 导航 & AI @en Navigation & AI ──

  /**
   * @zh 让生物导航到指定坐标。
   * @en Orders a pathfinder mob to navigate to the given coordinates.
   * @param x, y, z - 目标坐标
   * @param speed - 移动速度倍率
   * @returns 路径计算成功返回 true, 非 PathfinderMob 返回 false
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
type GamePlayerEntity = GameEntity & { player: GamePlayer };

// ── §4 @zh 玩家 @en Player ──

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
   * @returns 用户选择结果 { index, value }
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
   * @zh 给予玩家自定义物品 (基于 resourcepacks/box3js-items/items.json 配置)。
   * @en Gives a custom item defined in the resource pack's items.json.
   * Items are vanilla paper with custom_model_data + name/lore/food components.
   * @param id - 自定义物品 ID (如 "arena_trophy")
   * @param count - 数量 (1‑64)
   */
  giveCustomItem(id: string, count: number): void;

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
}

// ── §5 @zh 世界 API @en World ──

/**
 * @zh 世界控制与事件 — 脚本中通过 `world` 访问。
 * @en World control & events — accessed via `world` in scripts.
 */
interface GameWorld {
  // ── @zh 世界属性 @en World properties ──

  /** @zh 服务器 MOTD @en Server MOTD string. */
  projectName(): string;

  /** @zh 服务器 MOTD (可读写, 同 projectName) @en Server MOTD (read/write, alias of projectName). */
  serverId: string;

  /** @zh 当前服务端 tick 计数 @en Current server tick count. */
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
   * @param name - 规则名 / rule name (see setGameRule for the list)
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
   * @returns 生成的实体包装, 失败返回 null
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

  // ── @zh 自定义物品 @en Custom Items ──

  /**
   * @zh 从资源包加载自定义物品配置 (基于数据组件, 无需 DeferredRegister, 无注册表同步问题)。
   * @en Loads custom item definitions from a resource pack's items.json.
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
   * @returns 是否成功加入黑名单
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
    x: number, y: number, z: number,
    color: GameRGBColor,
    count: number,
    dx: number, dy: number, dz: number,
    speed: number,
  ): void;
  /** @zh 彩色粒子，GameVector3 重载。 @en Colored dust particle, GameVector3 overload. */
  spawnParticle(
    pos: GameVector3,
    color: GameRGBColor,
    count: number,
    dx: number, dy: number, dz: number,
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
    x: number, y: number, z: number,
    colors: GameRGBColor[],
    shape: string,
  ): void;
  /** @zh 彩色烟花，GameVector3 + GameRGBColor[] 重载。 @en Colored firework, GameVector3 overload. */
  launchFirework(
    pos: GameVector3,
    colors: GameRGBColor[],
    shape: string,
  ): void;

  // ── @zh 闪电 @en Lightning ──

  /**
   * @zh 在指定位置召唤闪电。
   * @en Summons a lightning bolt at the given position.
   * @param x, y, z - 位置
   * @param damage - 伤害值 (可选, 仅对实体造成)
   * @returns 是否成功
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
   * @returns 定时器 ID (可用于 clearTimeout)
   */
  setTimeout(handler: () => void, ticks: number): number;

  /**
   * @zh 设置循环定时回调。
   * @en Schedules a recurring interval callback.
   * @param handler - 回调函数
   * @param ticks - 间隔 tick 数
   * @returns 定时器 ID (可用于 clearInterval)
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
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
   */
  onTick(handler: (info: TickInfo) => void): GameEventHandlerToken;

  /**
   * @zh 注册玩家加入回调。
   * @en Registers a callback invoked when a player joins the server.
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
   */
  onPlayerJoin(
    handler: (entity: GamePlayerEntity, tick: number) => void,
  ): GameEventHandlerToken;

  /**
   * @zh 注册玩家离开回调。
   * @en Registers a callback invoked when a player leaves the server.
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
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
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
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
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
   */
  onPlayerRespawn(
    handler: (entity: GamePlayerEntity, tick: number) => void,
  ): GameEventHandlerToken;

  /**
   * @zh 注册方块右键激活回调。
   * @en Registers a callback invoked when a player right‑clicks a block.
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
   * @zh 注册方块破坏回调。
   * @en Registers a callback invoked when a player breaks a block.
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
   * @zh 注册方块放置回调。
   * @en Registers a callback invoked when a player places a block.
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
   * @zh 注册方块接触回调 (玩家移动到新方块时触发)。
   * @en Registers a callback invoked when a player's block position changes.
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
   * @zh 注册实体交互回调 (玩家右键实体)。
   * @en Registers a callback invoked when a player right‑clicks an entity.
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
   * @zh 注册实体死亡回调。
   * @en Registers a callback invoked when an entity dies.
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
   * @zh 注册实体受伤回调。
   * @en Registers a callback invoked when an entity takes damage.
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
   * @zh 注册流体进入回调 (玩家进入水/熔岩)。
   * @en Registers a callback invoked when a player enters a fluid.
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
   * @zh 注册流体离开回调 (玩家离开水/熔岩)。
   * @en Registers a callback invoked when a player leaves a fluid.
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
   * @zh 注册实体接触回调 (两个实体碰撞)。
   * @en Registers a callback invoked when two entities come into contact.
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
   */
  onEntityContact(
    handler: (entityA: GameEntity, entityB: GameEntity, tick: number) => void,
  ): GameEventHandlerToken;

  /**
   * @zh 注册实体分离回调 (两个实体不再碰撞)。
   * @en Registers a callback invoked when two entities separate after contact.
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
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
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
   */
  onButtonPressed(
    handler: (entity: GamePlayerEntity, button: string, tick: number) => void,
  ): GameEventHandlerToken;

  /**
   * @zh 注册跨项目消息回调。
   * @en Registers a callback for messages from other script projects.
   * @returns GameEventHandlerToken — 调用 .cancel() 取消
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

// ── §6 @zh 方块操作 @en Voxels ──

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
   * @returns 数字 ID, 未知方块的返回 0 (air)
   */
  id(name: string): number;

  /**
   * @zh 将数字 ID 转为方块名称。
   * @en Resolves a numeric ID back to a block name string.
   * @returns ResourceLocation 字符串, 未知 ID 返回 "air"
   */
  name(id: number): string;

  // ── @zh 读取 @en Read ──

  /**
   * @zh 获取方块数字 ID (不含旋转信息的基础 ID)。
   * @en Returns the base numeric block ID at the given position (without rotation encoding).
   * @returns 基础方块 ID, 空气返回 0 / base block ID, 0 for air
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
   * @returns 含旋转编码的完整方块 ID, 删除/空气返回 0
   */
  setVoxel(x: number, y: number, z: number, voxel: string | number): number;
  setVoxel(pos: GameVector3, voxel: string | number): number;

  /**
   * @zh 放置方块并指定旋转。返回含旋转编码的完整 ID。
   * @en Places a block with explicit rotation.
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
   * @param x, y, z - 刷怪笼坐标 / spawner coordinates
   * @param entityType - 实体类型 ID (如 "minecraft:zombie")
   */
  setSpawner(x: number, y: number, z: number, entityType: string): void;
  setSpawner(pos: GameVector3, entityType: string): void;
}

// ── §7 @zh 控制台 @en Console ──

/**
 * @zh 服务端控制台输出 — 脚本中通过 `console` 访问。
 * 输出格式：`[Box3JS] [projectName] <message>`，通过 System.out / System.err 输出到服务端控制台。
 *
 * @en Server console output — accessed via `console` in scripts.
 * Output format: `[Box3JS] [projectName] <message>`, written via System.out / System.err.
 */
interface GameConsole {
  /** @zh 普通日志 @en Info‑level log. */
  log(...args: unknown[]): void;

  /** @zh 调试日志 (前缀 [DEBUG]) @en Debug‑level log (prefixed with [DEBUG]). */
  debug(...args: unknown[]): void;

  /** @zh 警告日志 (前缀 [WARN]) @en Warning‑level log (prefixed with [WARN]). */
  warn(...args: unknown[]): void;

  /**
   * @zh 错误日志 (输出到 stderr, 前缀 [ERROR])。
   * @en Error‑level log (written to stderr, prefixed with [ERROR]).
   */
  error(...args: unknown[]): void;

  /**
   * @zh 清除控制台 (发送 ANSI 清屏序列)。
   * @en Clears the console output (sends ANSI clear‑screen sequence).
   */
  clear(): void;

  /**
   * @zh 断言: 条件为 false 时输出错误。
   * @en Asserts a condition; logs an error message if the condition is false.
   * @param condition - 要测试的条件 / the condition to test
   * @param args - 失败时输出的额外参数 / additional values to log on failure
   */
  assert(condition: boolean, ...args: unknown[]): void;
}

// ── §8 @zh 运行时枚举常量 @en Runtime Enum Constants ──

/**
 * @zh 按钮类型常量 — 用于 `world.onButtonPressed()` 的 `button` 参数。
 * @en Button type constants for the `button` parameter of `world.onButtonPressed()`.
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
 * @zh 相机模式常量 — `player.cameraMode` 的取值。
 * @en Camera mode constants for the `player.cameraMode` property.
 */
declare const GameCameraMode: {
  readonly FOLLOW: "FOLLOW";
  readonly FPS: "FPS";
};

/**
 * @zh 玩家移动状态常量 — `player.moveState` 的可能返回值。
 * @en Player movement state constants — possible return values of `player.moveState`.
 */
declare const GamePlayerMoveState: {
  readonly FLYING: "FLYING";
  readonly GROUND: "GROUND";
  readonly SWIM: "SWIM";
  readonly FALL: "FALL";
  readonly JUMP: "JUMP";
};

/**
 * @zh 玩家行走状态常量 — `player.walkState` 的可能返回值。
 * @en Player walk state constants — possible return values of `player.walkState`.
 */
declare const GamePlayerWalkState: {
  readonly NONE: "NONE";
  readonly CROUCH: "CROUCH";
  readonly WALK: "WALK";
  readonly RUN: "RUN";
};

// ── §9 @zh 全局声明 @en Global Declarations ──

/** @zh 世界控制与事件 API @en World control & events */
declare const world: GameWorld;

/** @zh 方块读写 API @en Block read & write */
declare const voxels: GameVoxels;

/** @zh 持久化存储 API @en Persistent key‑value storage */
declare const storage: GameStorage;

/**
 * @zh SQLite 数据库 API。
 *
 * **前置条件：** 需要安装 `minecraft-sqlite-jdbc` 模组来提供 JDBC 驱动。
 * 未安装时，调用 `db.sql()` 会抛出带明确提示的错误，不影响其它 API（`world`、`storage`、`voxels` 等）。
 *
 * @en SQLite database API.
 *
 * **Prerequisite:** Install the `minecraft-sqlite-jdbc` mod to provide the JDBC driver.
 * If missing, `db.sql()` throws a clear error without affecting other APIs.
 *
 * @see https://modrinth.com/plugin/minecraft-sqlite-jdbc
 */
declare const db: GameDatabase;

/** @zh 服务端控制台输出 @en Server console output */
declare const console: GameConsole;
