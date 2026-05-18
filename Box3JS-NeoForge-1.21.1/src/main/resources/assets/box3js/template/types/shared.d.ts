// ── §1 @zh 数学类型（服务端 & 客户端共享） @en Math Types (shared between server & client) ──

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
  /** @zh 返回 "(lo, hi)" 格式的字符串表示。 @en Returns a string representation in "(lo, hi)" format. */
  /** @zh 返回四元数字符串表示。 @en Returns a string representation of the quaternion. */
  /** @zh 返回四元数字符串表示。 @en Returns a string representation of the quaternion. */
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

  /** @zh 返回颜色字符串表示。 @en Returns a string representation of the color. */
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

  /** @zh 返回颜色字符串表示。 @en Returns a string representation of the color. */
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

  /** @zh 返回四元数字符串表示。 @en Returns a string representation of the quaternion. */
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
  static lookAt(
    from: GameVector3,
    to: GameVector3,
    up: GameVector3,
  ): GameQuaternion;

  /** @zh 近似相等检查（容差 1e‑6）。 @en Approximate equality check within 1e‑6 tolerance. */
  equals(v: GameQuaternion): boolean;

  /** @zh 返回四元数字符串表示。 @en Returns a string representation of the quaternion. */
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

// ── §2 @zh 持久化存储（服务端 & 客户端共享） @en Persistent Storage (shared between server & client) ──

/**
 * @zh JSON 可序列化的值类型，用作 `GameDataStorage<T>` 的默认类型参数。
 * @en Represents any JSON‑serializable value.
 * Used as the default type parameter for `GameDataStorage<T>`.
 */
type JSONValue =
  | string
  | number
  | boolean
  | null
  | JSONValue[]
  | { [key: string]: JSONValue };

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
   * @param key - @zh 键 @en key
   * @param value - @zh 值 @en value (typed to T)
   */
  set(key: string, value: T): void;

  /**
   * @zh 读取键对应的值, 不存在则返回 null。
   * @en Retrieves the value for a key, or null if it does not exist.
   * @returns @zh 存储的值, 或 null @en The stored value, or null
   */
  get(key: string): T | null;

  /**
   * @zh 获取当前存储空间中的所有键。
   * @en Lists all keys in this storage namespace.
   */
  keys(): string[];

  /**
   * @zh 删除键, 返回旧值 (不存在则返回 null)。
   * @en Removes a key and returns its previous value, or null.
   * @returns @zh 被删除的旧值, 或 null @en The previous value, or null
   */
  remove(key: string): T | null;

  /**
   * @zh 销毁该存储空间 (删除对应 JSON 文件)。
   * @en Destroys this storage namespace (deletes the backing JSON file).
   */
  destroy(): void;
}

/**
 * @zh 全局存储入口 — 脚本中通过 `storage` 访问。
 * 项目间数据隔离：每个项目自动使用项目名作为存储文件前缀。
 *
 * @en Global storage entry point — accessed via `storage` in scripts.
 * Per‑project isolation: each project's storage is automatically prefixed with the project name.
 */
interface GameStorage {
  /** @zh 始终返回空字符串 (MC 本地存储无 key, 只读) @en Always returns "" for MC local storage, readonly. */
  readonly key: string;

  /**
   * @zh 打开或创建指定名称的数据存储空间 (项目隔离)。
   * @en Opens or creates a named data‑storage namespace (per‑project isolated).
   * @param name - @zh 命名空间（可含 "/" 作为目录分隔） @en namespace (may contain "/" as directory separator)
   * @remarks 不同项目使用同一 name 会访问不同文件。
   *          Different projects using the same name access different files.
   *
   * @example
   * const coins = storage.getDataStorage<number>("coins");
   * const balance = coins.get(userId);  // number | null
   */
  getDataStorage<T = JSONValue>(name: string): GameDataStorage<T>;
}

// ── §2b @zh 持久化存储扩展（服务端 & 客户端共享） @en Storage Extensions (shared between server & client) ──

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

// Declaration merging: augment GameDataStorage with shared advanced methods
interface GameDataStorage<T = JSONValue> {
  /**
   * @zh 原子更新: 取出当前值, 用 handler(currentValue) 的结果覆盖。
   * @en Atomically updates a value using a callback.
   * @param key - @zh 键 @en key
   * @param handler - @zh (prevValue) => newValue @en callback receiving the old value, returning the new one
   * @remarks 如果键不存在, 不会创建新条目 (遵循 Box3 规范)。
   *          If the key does not exist, nothing happens (per Box3 spec).
   */
  update(key: string, handler: (prevValue: T) => T): void;

  /**
   * @zh 原子递增 (delta 默认为 1)。
   * @en Atomically increments a numeric value by delta (default 1).
   * @param key - @zh 键 @en key
   * @param delta - @zh 增量（可选，默认 1） @en increment amount (optional, default 1)
   * @returns @zh 递增后的新值 @en The new value after incrementing
   * @remarks 键不存在时从 0 + delta 开始。
   *          If the key doesn't exist, starts from 0 + delta.
   */
  increment(key: string, delta?: number): number;

  /**
   * @zh 分页查询存储条目。
   * @en Paginated query of stored entries.
   * @param options - @zh 查询选项 @en query options
   * @param options.cursor - @zh 起始游标（页码） @en starting cursor (page number * pageSize)
   * @param options.pageSize - @zh 每页条目数（1‑100，默认 100） @en items per page (1–100, default 100)
   * @param options.ascending - @zh 是否升序排列 @en sort ascending if true
   * @param options.max - @zh 值的上限过滤 @en maximum value filter
   * @param options.min - @zh 值的下限过滤 @en minimum value filter
   * @param options.constraintTarget - @zh 排序/过滤的目标路径（如 "a.b.c"） @en nested path for sorting/filtering
   * @returns @zh 分页结果对象 @en paginated query result
   */
  list(options?: {
    cursor?: number;
    pageSize?: number;
    ascending?: boolean;
    max?: number;
    min?: number;
    constraintTarget?: string;
  }): QueryList<T>;
}

// ── §3 @zh 控制台（服务端 & 客户端共享） @en Console (shared between server & client) ──

/**
 * @zh 控制台输出 — 服务端和客户端脚本中均通过 `console` 访问。
 * 服务端输出到 System.out / System.err；客户端输出到客户端日志。
 *
 * @en Console output — accessed via `console` in both server and client scripts.
 * Server writes to System.out / System.err; client writes to client log.
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
   * @param condition - @zh 要测试的条件 @en the condition to test
   * @param args - @zh 失败时输出的额外参数 @en additional values to log on failure
   */
  assert(condition: boolean, ...args: unknown[]): void;
}

// ── §4 @zh 远程事件通道 @en Remote Event Channel ──

/**
 * @zh 远程事件通道 — 在服务端和客户端脚本中均通过 `remoteChannel` 访问。
 *
 * 事件数据通过 JSON 序列化传输，支持任意可序列化的类型。
 *
 * **服务端方法**（在 `server.d.ts` 中声明）：
 * `sendClientEvent()` / `broadcastClientEvent()` / `onServerEvent()`
 *
 * **客户端方法**（在 `client.d.ts` 中声明）：
 * `sendServerEvent()` / `onClientEvent()`
 *
 * @en Remote event channel — accessed via `remoteChannel` in both server and client scripts.
 *
 * Event data is serialized via JSON and supports any serializable type.
 *
 * **Server‑side methods** (declared in `server.d.ts`):
 * `sendClientEvent()` / `broadcastClientEvent()` / `onServerEvent()`
 *
 * **Client‑side methods** (declared in `client.d.ts`):
 * `sendServerEvent()` / `onClientEvent()`
 */
interface RemoteChannel {}

// ── §5 @zh SQLite 数据库（服务端 & 客户端共享） @en SQLite Database (shared between server & client) ──

/**
 * @zh SQL 查询结果，支持迭代和 thenable 模式。
 * SELECT 查询返回行数据，INSERT/UPDATE/DELETE 返回受影响行数。
 *
 * @en SQL query result — supports iteration and thenable pattern.
 * SELECT queries return row data; INSERT/UPDATE/DELETE return affected row count.
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
 * @zh SQLite 数据库，服务端存储在 `config/box3/data/<project>.db`，
 * 客户端存储在 `<gameDir>/config/box3/client-db/<project>.db`。
 * 通过全局 `db` 访问，支持 `?` 占位符和 tagged template 两种调用约定。
 *
 * @en SQLite database — server stores at `config/box3/data/<project>.db`,
 * client stores at `<gameDir>/config/box3/client-db/<project>.db`.
 * Access via global `db`, supports both `?` placeholder and tagged template calling conventions.
 */
interface GameDatabase {
  /**
   * @zh 检查 SQLite JDBC 驱动（minecraft-sqlite-jdbc 模组）是否可用。
   * 不可用时调用 {@link sql} 会返回空的错误结果而非抛出异常。
   * @en Returns true if the SQLite JDBC driver (minecraft-sqlite-jdbc mod) is available.
   * When unavailable, {@link sql} returns an empty error result instead of throwing.
   */
  isAvailable(): boolean;

  /**
   * @zh 执行 SQL 查询或更新。
   * @en Executes a SQL query or update.
   *
   * @param sql - @zh SQL 字符串（含 ? 占位符）或字符串数组（模板字面量） @en SQL string (with ? placeholders) or string array (template literal)
   * @param params - @zh 参数值 @en Parameter values to bind (number | string | boolean | null | Uint8Array)
   * @returns @zh 查询结果 @en query result
   */
  sql<T = Record<string, any>>(
    sql: string | readonly string[],
    ...params: (number | string | boolean | null | Uint8Array)[]
  ): GameQueryResult<T>;
}

// ── §6 @zh HTTP 请求（服务端 & 客户端共享） @en HTTP Request (shared between server & client) ──

/** @zh HTTP 请求头 @en HTTP request headers */
type GameHttpFetchHeaders = {
  [name: string]: string | string[];
};

/**
 * @zh HTTP 请求选项
 * @en HTTP request options
 */
type GameHttpFetchRequestOptions = {
  /** @zh 请求超时时间，单位为毫秒（默认 10000） @en Request timeout in milliseconds (default 10000) */
  timeout?: number;
  /** @zh 请求方法（默认 GET） @en Request method (default GET) */
  method?: "OPTIONS" | "GET" | "HEAD" | "PUT" | "POST" | "DELETE" | "PATCH";
  /** @zh 请求头 @en Request headers */
  headers?: GameHttpFetchHeaders;
  /** @zh 请求体（字符串或 ArrayBuffer） @en Request body (string or ArrayBuffer) */
  body?: string | ArrayBuffer;
  /**
   * @zh 自动解析响应体（"json" | "text" | "arrayBuffer"），结果见 resp.data
   * @en Auto-parse response body ("json" | "text" | "arrayBuffer"), result in resp.data
   */
  responseType?: "json" | "text" | "arrayBuffer";
  /**
   * @zh 响应体最大字节数（0 表示不限制）。超出截断，resp.truncated 设为 true
   * @en Max response body bytes (0 = no limit). Exceeding content is truncated, resp.truncated is set to true
   */
  maxBodySize?: number;
  /**
   * @zh 设为 true 启用异步请求（不阻塞）。必须同时提供 onResponse / onError 回调
   * @en Set to true for async request (non-blocking). Must provide onResponse / onError callbacks
   */
  async?: boolean;
  /**
   * @zh 异步请求成功时的回调，参数为 GameHttpFetchResponse
   * @en Callback on async request success, receives GameHttpFetchResponse
   */
  onResponse?: (resp: GameHttpFetchResponse) => void;
  /**
   * @zh 异步请求失败时的回调，参数为错误信息字符串
   * @en Callback on async request error, receives error message string
   */
  onError?: (err: string) => void;
};

/**
 * @zh HTTP 请求响应
 * @en HTTP request response
 */
declare class GameHttpFetchResponse {
  /** @zh 状态码 @en Status code */
  readonly status: number;
  /** @zh 状态码描述 @en Status text description */
  readonly statusText: string;
  /** @zh 是否请求成功（状态码 200-299） @en Whether the request was successful (status 200-299) */
  readonly ok: boolean;
  /** @zh 错误信息（仅在请求失败时有值） @en Error message (only set on failure) */
  readonly errorMessage: string;
  /** @zh 所有响应头（键值对） @en All response headers (key-value map) */
  readonly headers: GameHttpFetchHeaders;
  /**
   * @zh 自动解析的结果（设置了 responseType 时） @en Auto-parsed result (when responseType was set)
   */
  readonly data: any;
  /**
   * @zh 响应体是否因超过 maxBodySize 被截断 @en Whether the response body was truncated due to maxBodySize
   */
  readonly truncated: boolean;

  /**
   * @zh 获取指定响应头的值
   * @en Get a single response header value
   * @param name - @zh 响应头名称（大小写不敏感） @en Header name (case-insensitive)
   * @returns @zh 响应头值，不存在返回 null @en Header value, or null if absent
   */
  getHeader(name: string): string | null;

  /**
   * @zh 将响应体解析为 JSON 对象
   * @en Parse the response body as a JSON object
   * @returns @zh 解析后的对象，解析失败返回 null @en Parsed object, or null on parse failure
   */
  json(): any;

  /**
   * @zh 返回响应体的文本内容
   * @en Return the response body as text
   */
  text(): string;

  /**
   * @zh 返回响应体的字节数组
   * @en Return the response body as a byte array
   */
  arrayBuffer(): ArrayBuffer;

  /**
   * @zh 关闭连接
   * @en Close the connection
   */
  close(): void;

  private constructor();
}

/**
 * @zh HTTP 请求 API
 * @en HTTP request API
 */
declare class GameHttpAPI {
  /**
   * @zh 发送 HTTP 请求
   * @en Send an HTTP request
   *
   * @param url - @zh 请求地址 @en The request URL
   * @param options - @zh 请求配置。设置 `async: true` + `onResponse`/`onError` 回调进行异步请求 @en Request options
   * @returns @zh 同步返回结果，异步返回 null @en Response for sync, null for async
   */
  fetch(
    url: string,
    options?: GameHttpFetchRequestOptions,
  ): GameHttpFetchResponse;

  private constructor();
}

// ── §7 @zh 全局声明（服务端 & 客户端共享） @en Global Declarations (shared between server & client) ──

/** @zh 持久化存储 API @en Persistent key‑value storage */
declare const storage: GameStorage;

/** @zh 控制台输出 @en Console output */
declare const console: GameConsole;

/** @zh 远程事件通道（服务端 ↔ 客户端通信） @en Remote event channel (server ↔ client communication) */
declare const remoteChannel: RemoteChannel;

/**
 * @zh SQLite 数据库 API。
 *
 * **前置条件：** 需要安装 `minecraft-sqlite-jdbc` 模组来提供 JDBC 驱动。
 * 未安装时，调用 `db.sql()` 会抛出带明确提示的错误。
 *
 * @en SQLite database API.
 *
 * **Prerequisite:** Install the `minecraft-sqlite-jdbc` mod to provide the JDBC driver.
 * If missing, `db.sql()` throws a clear error.
 */
declare const db: GameDatabase;

/** @zh HTTP 请求 API @en HTTP request API */
declare const http: GameHttpAPI;

// ── §8 @zh 定时器（全局函数） @en Timers (global functions) ──

/**
 * @zh 设置一次性延时回调。Rhino 引擎不提供浏览器内置的 setTimeout，由 Box3JS 提供。
 * @en Schedules a one‑shot delayed callback. Rhino does not provide the browser built‑in setTimeout; Box3JS supplies it.
 * @param handler - @zh 回调函数 @en callback function
 * @param ticks - @zh 延迟 tick 数（20 ticks = 1 秒） @en delay in ticks (20 ticks = 1 second)
 * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to cancel
 *
 * @example
 * const token = setTimeout(() => {
 *   world.say("3 seconds passed");
 * }, 60);
 * // token.cancel(); // cancel before it fires
 */
declare function setTimeout(
  handler: () => void,
  ticks: number,
): GameEventHandlerToken;

/**
 * @zh 设置循环定时回调。Rhino 引擎不提供浏览器内置的 setInterval，由 Box3JS 提供。
 * @en Schedules a recurring interval callback. Rhino does not provide the browser built‑in setInterval; Box3JS supplies it.
 * @param handler - @zh 回调函数 @en callback function
 * @param ticks - @zh 间隔 tick 数（20 ticks = 1 秒） @en interval in ticks (20 ticks = 1 second)
 * @returns @zh GameEventHandlerToken — 调用 .cancel() 取消 @en GameEventHandlerToken — call .cancel() to cancel
 *
 * @example
 * const token = setInterval(() => {
 *   world.say("Every 5 seconds");
 * }, 100);
 * // token.cancel(); // stop the interval
 */
declare function setInterval(
  handler: () => void,
  ticks: number,
): GameEventHandlerToken;
