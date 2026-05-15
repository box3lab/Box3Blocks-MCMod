# 数学类型

以下数据类型在 JS 中全局可用。

## GameVector3

三维向量，用于位置、方向、速度等。所有分量使用 `double` 精度。

### 构造

```js
var v = new GameVector3(); // 零向量 (0, 0, 0)
var v = new GameVector3(x, y, z); // 指定坐标
```

### 属性

| 属性  | 类型     | 说明                      |
| ----- | -------- | ------------------------- |
| `v.x` | `number` | X 分量 (东西方向)，可读写 |
| `v.y` | `number` | Y 分量 (上下方向)，可读写 |
| `v.z` | `number` | Z 分量 (南北方向)，可读写 |

### 实例方法

#### 原地修改 (返回 this)

| 方法             | 返回值        | 说明                              |
| ---------------- | ------------- | --------------------------------- |
| `v.set(x, y, z)` | `GameVector3` | 设置所有分量                      |
| `v.copy(w)`      | `GameVector3` | 从 `w` 复制所有分量               |
| `v.addEq(w)`     | `GameVector3` | 原地加法：`v += w`                |
| `v.subEq(w)`     | `GameVector3` | 原地减法：`v -= w`                |
| `v.mulEq(w)`     | `GameVector3` | 原地逐分量乘法：`v.x *= w.x` …    |
| `v.divEq(w)`     | `GameVector3` | 原地逐分量除法，除以 0 跳过该分量 |
| `v.scaleEq(n)`   | `GameVector3` | 原地标量乘法：`v.x *= n` …        |
| `v.negEq()`      | `GameVector3` | 原地取反：`v = -v`                |

#### 创建新向量 (不修改自身)

| 方法                              | 返回值        | 说明                                 |
| --------------------------------- | ------------- | ------------------------------------ |
| `v.clone()`                       | `GameVector3` | 深拷贝，返回相同值的独立新向量       |
| `v.add(w)`                        | `GameVector3` | 向量加法：`v + w`                    |
| `v.sub(w)`                        | `GameVector3` | 向量减法：`v - w`                    |
| `v.mul(w)`                        | `GameVector3` | 逐分量乘法                           |
| `v.div(w)`                        | `GameVector3` | 逐分量除法，除以 0 得 0              |
| `v.scale(n)`                      | `GameVector3` | 标量乘法：每个分量乘以 `n`           |
| `v.cross(w)`                      | `GameVector3` | 叉积：`v × w`                        |
| `v.normalize()`                   | `GameVector3` | 单位化，零向量返回 `(0,0,0)`         |
| `v.lerp(w, t)`                    | `GameVector3` | 线性插值：`t=0` 为自身，`t=1` 为 `w` |
| `v.towards(w)`                    | `GameVector3` | 指向 `w` 的方向向量 (已单位化)       |
| `v.max(w)`                        | `GameVector3` | 逐分量取较大值                       |
| `v.min(w)`                        | `GameVector3` | 逐分量取较小值                       |
| `v.neg()`                         | `GameVector3` | 取反：`-v`                           |
| `v.moveTowards(target, maxDelta)` | `GameVector3` | 向目标移动不超过 `maxDelta` 距离     |
| `v.floor()`                       | `GameVector3` | 逐分量向下取整                       |
| `v.ceil()`                        | `GameVector3` | 逐分量向上取整                       |
| `v.clampLength(max)`              | `GameVector3` | 限制长度至 `max`，超长则等比缩放     |

#### 数值计算

| 方法               | 返回值   | 说明                                    |
| ------------------ | -------- | --------------------------------------- |
| `v.dot(w)`         | `number` | 点积 (内积)：`v · w`                    |
| `v.mag()`          | `number` | 向量长度 (模)                           |
| `v.sqrMag()`       | `number` | 长度平方，比 `mag()` 更快               |
| `v.distance(w)`    | `number` | 与 `w` 的欧几里得距离                   |
| `v.angle(w)`       | `number` | 与 `w` 的夹角 (弧度, 0–π)               |
| `v.sqrDistance(w)` | `number` | 与 `w` 的距离平方，比 `distance()` 更快 |

#### 比较

| 方法               | 返回值    | 说明                            |
| ------------------ | --------- | ------------------------------- |
| `v.equals(w)`      | `boolean` | 近似相等，容差 1e-6             |
| `v.exactEquals(w)` | `boolean` | 精确相等，分量完全一致          |
| `v.isZero()`       | `boolean` | 是否为 (接近) 零向量，容差 1e-6 |

```js
var pos = new GameVector3(0, 100, 0);
var target = new GameVector3(10, 100, 10);

// 计算距离
var dist = pos.distance(target); // ~14.14

// 方向向量
var dir = target.sub(pos).normalize();

// 夹角
var angle = pos.angle(target); // 弧度

// 比较
var a = new GameVector3(1, 2, 3);
var b = new GameVector3(1.0000001, 2.0000001, 3.0000001);
a.equals(b); // true (容差内)
a.exactEquals(b); // false

// 传送实体 (LiveVec3)
entity.position.set(0, 100, 0);
```

### 静态方法

```js
// 球坐标 → 向量
var v = GameVector3.fromPolar(mag, phi, theta);
// mag:   半径
// phi:   方位角 (弧度, 绕 Y 轴水平旋转)
// theta: 仰角 (弧度, 从水平面起算)
```

### toString

```js
var v = new GameVector3(1, 2, 3);
v.toString(); // "GameVector3(1.0, 2.0, 3.0)"
```

## GameBounds3

轴对齐包围盒 (AABB)，由两个对角顶点 `lo` (最小角) 和 `hi` (最大角) 定义。

### 构造

```js
var bounds = new GameBounds3(
  new GameVector3(-1, 0, -1), // lo (最小角)
  new GameVector3(1, 2, 1), // hi (最大角)
);
```

### 属性

| 属性        | 类型          | 说明                                |
| ----------- | ------------- | ----------------------------------- |
| `bounds.lo` | `GameVector3` | 最小角 (三个分量均为最小值)，可读写 |
| `bounds.hi` | `GameVector3` | 最大角 (三个分量均为最大值)，可读写 |

### 实例方法

| 方法                                       | 返回值                | 说明                               |
| ------------------------------------------ | --------------------- | ---------------------------------- |
| `bounds.set(lox, loy, loz, hix, hiy, hiz)` | `GameBounds3`         | 原地设置所有边界，返回自身         |
| `bounds.copy(b)`                           | `GameBounds3`         | 原地复制 `b` 的值，返回自身        |
| `bounds.intersects(other)`                 | `boolean`             | 是否与 `other` 相交                |
| `bounds.intersect(other)`                  | `GameBounds3 \| null` | 计算交集包围盒，不相交返回 `null`  |
| `bounds.contains(v)`                       | `boolean`             | 点 `v` 是否在包围盒内 (含边界)     |
| `bounds.containsBounds(b)`                 | `boolean`             | 是否完全包含另一个包围盒 `b`       |
| `bounds.center()`                          | `GameVector3`         | 包围盒中心点                       |
| `bounds.size()`                            | `GameVector3`         | 包围盒尺寸 (宽, 高, 深)            |
| `bounds.expand(delta)`                     | `GameBounds3`         | 各面向外扩展 `delta`，返回新包围盒 |
| `bounds.expandEq(delta)`                   | `GameBounds3`         | 原地各面向外扩展 `delta`，返回自身 |
| `bounds.growToInclude(v)`                  | `GameBounds3`         | 原地扩展以包含点 `v`，返回自身     |
| `bounds.closestPoint(v)`                   | `GameVector3`         | 包围盒上离点 `v` 最近的点          |
| `bounds.move(offset)`                      | `GameBounds3`         | 平移 `offset`，返回新包围盒        |
| `bounds.moveEq(offset)`                    | `GameBounds3`         | 原地平移 `offset`，返回自身        |

### 静态方法

```js
// 从 GameVector3 数组创建最小包围盒
var points = [new GameVector3(0, 0, 0), new GameVector3(5, 10, 3)];
var box = GameBounds3.fromPoints(points); // 返回 GameBounds3 或 null
```

### toString

```js
bounds.toString(); // "GameBounds3(GameVector3(-1.0, 0.0, -1.0), GameVector3(1.0, 2.0, 1.0))"
```

```js
// 查询区域内实体
var entities = world.searchBox(bounds);

// 检测点是否在区域内
if (bounds.contains(player.position)) {
  // 玩家在区域内
}
```

## GameRGBColor

RGB 颜色，三个通道范围 0.0–1.0。

### 构造

```js
var red = new GameRGBColor(1, 0, 0);
var blue = new GameRGBColor(0, 0, 1);
var gray = new GameRGBColor(0.5, 0.5, 0.5);
```

### 属性

| 属性      | 类型     | 说明                   |
| --------- | -------- | ---------------------- |
| `color.r` | `number` | 红色通道 (0–1)，可读写 |
| `color.g` | `number` | 绿色通道 (0–1)，可读写 |
| `color.b` | `number` | 蓝色通道 (0–1)，可读写 |

### 实例方法

#### 原地修改 (返回 this)

| 方法             | 返回值         | 说明                              |
| ---------------- | -------------- | --------------------------------- |
| `c.set(r, g, b)` | `GameRGBColor` | 设置所有通道                      |
| `c.copy(o)`      | `GameRGBColor` | 从另一个颜色复制所有通道          |
| `c.addEq(o)`     | `GameRGBColor` | 原地加法：`c += o`                |
| `c.subEq(o)`     | `GameRGBColor` | 原地减法：`c -= o`                |
| `c.mulEq(o)`     | `GameRGBColor` | 原地逐通道乘法                    |
| `c.divEq(o)`     | `GameRGBColor` | 原地逐通道除法，除以 0 跳过该通道 |
| `c.scaleEq(n)`   | `GameRGBColor` | 原地标量乘法：每个通道乘以 `n`    |

#### 创建新颜色 (不修改自身)

| 方法           | 返回值         | 说明                                 |
| -------------- | -------------- | ------------------------------------ |
| `c.clone()`    | `GameRGBColor` | 深拷贝                               |
| `c.add(o)`     | `GameRGBColor` | 逐通道加法                           |
| `c.sub(o)`     | `GameRGBColor` | 逐通道减法                           |
| `c.mul(o)`     | `GameRGBColor` | 逐通道乘法                           |
| `c.div(o)`     | `GameRGBColor` | 逐通道除法，除以 0 得 0              |
| `c.lerp(o, t)` | `GameRGBColor` | 线性插值：`t=0` 为自身，`t=1` 为 `o` |
| `c.scale(n)`   | `GameRGBColor` | 标量乘法：每个通道乘以 `n`           |
| `c.equals(o)`  | `boolean`      | 近似相等，容差 1e-6                  |
| `c.toRGBA()`   | `string`       | 转为 CSS 格式：`"rgba(r,g,b,1.0)"`   |

### 静态方法

```js
var randomColor = GameRGBColor.random(); // 每个通道 0–1 随机值
```

### toString

```js
new GameRGBColor(1, 0.5, 0).toString(); // "GameRGBColor(1.0, 0.5, 0.0)"
```

## GameRGBAColor

带 Alpha 通道的颜色，四个分量范围 0.0–1.0。

### 构造

```js
var semiRed = new GameRGBAColor(1, 0, 0, 0.5);
var opaque = new GameRGBAColor(0, 1, 0, 1.0);
```

### 属性

| 属性      | 类型     | 说明                         |
| --------- | -------- | ---------------------------- |
| `color.r` | `number` | 红色通道 (0–1)，可读写       |
| `color.g` | `number` | 绿色通道 (0–1)，可读写       |
| `color.b` | `number` | 蓝色通道 (0–1)，可读写       |
| `color.a` | `number` | Alpha 不透明度 (0–1)，可读写 |

### 实例方法

#### 原地修改 (返回 this)

| 方法                | 返回值          | 说明                              |
| ------------------- | --------------- | --------------------------------- |
| `c.set(r, g, b, a)` | `GameRGBAColor` | 设置所有四个通道                  |
| `c.copy(o)`         | `GameRGBAColor` | 从另一个 RGBA 颜色复制所有通道    |
| `c.addEq(o)`        | `GameRGBAColor` | 原地加法                          |
| `c.subEq(o)`        | `GameRGBAColor` | 原地减法                          |
| `c.mulEq(o)`        | `GameRGBAColor` | 原地逐通道乘法                    |
| `c.divEq(o)`        | `GameRGBAColor` | 原地逐通道除法，除以 0 跳过该通道 |
| `c.scaleEq(n)`      | `GameRGBAColor` | 原地标量乘法：每个通道乘以 `n`    |

#### 创建新颜色 (不修改自身)

| 方法             | 返回值          | 说明                                  |
| ---------------- | --------------- | ------------------------------------- |
| `c.clone()`      | `GameRGBAColor` | 深拷贝                                |
| `c.add(o)`       | `GameRGBAColor` | 逐通道加法                            |
| `c.sub(o)`       | `GameRGBAColor` | 逐通道减法                            |
| `c.mul(o)`       | `GameRGBAColor` | 逐通道乘法                            |
| `c.div(o)`       | `GameRGBAColor` | 逐通道除法，除以 0 得 0               |
| `c.lerp(o, t)`   | `GameRGBAColor` | 线性插值                              |
| `c.scale(n)`     | `GameRGBAColor` | 标量乘法：每个通道乘以 `n`            |
| `c.equals(o)`    | `boolean`       | 近似相等，容差 1e-6                   |
| `c.blendEq(rgb)` | `GameRGBColor`  | Alpha 混合到 RGB 背景上，返回最终 RGB |

### toString

```js
new GameRGBAColor(1, 0, 0, 0.5).toString(); // "GameRGBAColor(1.0, 0.0, 0.0, 0.5)"
```

```js
// Alpha 混合
var fg = new GameRGBAColor(1, 0, 0, 0.5); // 半透明红
var bg = new GameRGBColor(1, 1, 1); // 白色背景
var result = fg.blendEq(bg); // 得到混合后的 RGB 颜色
```

## GameQuaternion

四元数，用于 3D 旋转。单位四元数 (模长=1) 表示纯旋转。

### 构造

```js
var q = new GameQuaternion(); // 单位四元数 (1, 0, 0, 0)
var q = new GameQuaternion(w, x, y, z); // 指定分量
```

### 属性

| 属性  | 类型     | 说明                    |
| ----- | -------- | ----------------------- |
| `q.w` | `number` | 实部 (标量分量)，可读写 |
| `q.x` | `number` | 虚部 X 分量，可读写     |
| `q.y` | `number` | 虚部 Y 分量，可读写     |
| `q.z` | `number` | 虚部 Z 分量，可读写     |

### 实例方法

#### 原地修改 (返回 this)

| 方法                | 返回值           | 说明                |
| ------------------- | ---------------- | ------------------- |
| `q.set(w, x, y, z)` | `GameQuaternion` | 设置所有分量        |
| `q.copy(p)`         | `GameQuaternion` | 从 `p` 复制所有分量 |

#### 创建新四元数 (不修改自身)

| 方法            | 返回值           | 说明                                     |
| --------------- | ---------------- | ---------------------------------------- |
| `q.clone()`     | `GameQuaternion` | 深拷贝                                   |
| `q.add(p)`      | `GameQuaternion` | 逐分量加法                               |
| `q.sub(p)`      | `GameQuaternion` | 逐分量减法                               |
| `q.mul(p)`      | `GameQuaternion` | 汉密尔顿积：`q × p` (不可交换)           |
| `q.div(p)`      | `GameQuaternion` | 除法：`q × p⁻¹`                          |
| `q.inv()`       | `GameQuaternion` | 共轭 (对单位四元数等价于逆)              |
| `q.normalize()` | `GameQuaternion` | 单位化，返回模长为 1 的新四元数          |
| `q.slerp(p, t)` | `GameQuaternion` | 球面线性插值：`t=0` 为自身，`t=1` 为 `p` |

#### 数值计算

| 方法          | 返回值    | 说明                   |
| ------------- | --------- | ---------------------- |
| `q.dot(p)`    | `number`  | 点积                   |
| `q.mag()`     | `number`  | 模长 (范数)            |
| `q.sqrMag()`  | `number`  | 模长平方               |
| `q.angle(p)`  | `number`  | 与 `p` 的角度差 (弧度) |
| `q.equals(p)` | `boolean` | 近似相等，容差 1e-6    |

#### 旋转操作 (绕自身坐标系旋转，返回新四元数)

| 方法                | 返回值           | 说明                                         |
| ------------------- | ---------------- | -------------------------------------------- |
| `q.rotateX(rad)`    | `GameQuaternion` | 绕 X 轴旋转                                  |
| `q.rotateY(rad)`    | `GameQuaternion` | 绕 Y 轴旋转                                  |
| `q.rotateZ(rad)`    | `GameQuaternion` | 绕 Z 轴旋转                                  |
| `q.rotateVector(v)` | `GameVector3`    | 用此四元数旋转向量 `v`                       |
| `q.toEuler()`       | `GameVector3`    | 转为欧拉角 (YZX 顺序)，返回 `(x, y, z)` 弧度 |

#### 轴角分解

```js
var result = q.getAxisAngle();
// result.angle — 旋转角度 (弧度)
// result.axis  — 旋转轴 (单位 GameVector3)
```

### 静态方法

```js
// 从轴-角表示创建
var q1 = GameQuaternion.fromAxisAngle(axis, rad);
// axis: GameVector3 (自动归一化)
// rad:  旋转角度 (弧度)

// 从欧拉角创建 (YZX 旋转顺序: Y → Z → X)
var q2 = GameQuaternion.fromEuler(x, y, z);
// x, y, z: 绕各轴旋转的弧度

// 从向量 a 旋转到向量 b 的最短弧
var q3 = GameQuaternion.rotationBetween(fromVec, toVec);

// 从观察方向创建四元数 (从 from 看向 to)
var q4 = GameQuaternion.lookAt(from, to, up);
// from: GameVector3 — 观察者位置
// to:   GameVector3 — 目标点
// up:   GameVector3 — 上方向 (默认 (0,1,0))
```

### toString

```js
q.toString(); // "GameQuaternion(0.707, 0.0, 0.707, 0.0)"
```
