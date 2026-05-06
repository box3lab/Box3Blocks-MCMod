# 数学类型

全部 ✅ Box3 API。以下数据类型在 JS 中全局可用。

## GameVector3

三维向量，用于位置、方向、速度等。

### 构造

```js
var v = new GameVector3(0, 100, 0); // x, y, z
```

### 属性

```js
v.x = 10; // 读写
v.y = 20;
v.z = 30;
```

### 方法

| 方法             | 返回值        | 说明               |
| ---------------- | ------------- | ------------------ |
| `v.set(x, y, z)` | `GameVector3` | 设置分量，返回自身 |
| `v.add(w)`       | `GameVector3` | 加法，返回新向量   |
| `v.sub(w)`       | `GameVector3` | 减法               |
| `v.scale(s)`     | `GameVector3` | 标量乘             |
| `v.dot(w)`       | `number`      | 点积               |
| `v.mag()`        | `number`      | 向量长度           |
| `v.sqrMag()`     | `number`      | 长度平方（更快）   |
| `v.normalize()`  | `GameVector3` | 归一化，返回新向量 |
| `v.distance(w)`  | `number`      | 两点距离           |
| `v.lerp(w, t)`   | `GameVector3` | 线性插值，t 0–1    |
| `v.equals(w)`    | `boolean`     | 分量相等比较       |

### 静态方法

```js
var v = GameVector3.fromPolar(mag, phi, theta); // 球坐标 → 向量
```

```js
var pos = new GameVector3(0, 100, 0);
var target = new GameVector3(10, 100, 10);

// 计算两点距离
var dist = pos.distance(target); // ~14.14

// 方向向量
var dir = target.sub(pos).normalize();

// 传送
entity.position.set(0, 100, 0);
```

## GameBounds3

轴对齐包围盒（AABB）。

### 构造

```js
var bounds = new GameBounds3(
  new GameVector3(-1, 0, -1), // 下界 (lo)
  new GameVector3(1, 2, 1), // 上界 (hi)
);
```

### 方法

| 方法                       | 返回值    | 说明                 |
| -------------------------- | --------- | -------------------- |
| `bounds.intersects(other)` | `boolean` | 与另一包围盒是否相交 |
| `bounds.contains(point)`   | `boolean` | 点是否在包围盒内     |

## GameRGBColor

RGB 颜色，分量范围 0.0–1.0。

### 构造

```js
var red = new GameRGBColor(1, 0, 0);
var blue = new GameRGBColor(0, 0, 1);
var gray = new GameRGBColor(0.5, 0.5, 0.5);
```

### 属性

```js
color.r = 0.5; // 读写
color.g = 0.8;
color.b = 0.2;
```

### 方法

| 方法           | 返回值         | 说明     |
| -------------- | -------------- | -------- |
| `c.lerp(d, t)` | `GameRGBColor` | 线性插值 |

### 静态方法

```js
var randomColor = GameRGBColor.random(); // 随机颜色
```

## GameRGBAColor

带 Alpha 通道的颜色，分量范围 0.0–1.0。

### 构造

```js
var semiRed = new GameRGBAColor(1, 0, 0, 0.5);
```

### 方法

```js
var a = new GameRGBAColor(1, 0, 0, 1);
var b = new GameRGBAColor(0, 1, 0, 0.5);

var c = a.add(b); // 分量加法
var d = a.sub(b); // 分量减法
var e = a.mul(b); // 分量乘法
var f = a.div(b); // 分量除法

a.addEq(b); // 原地加法 (a += b)
a.subEq(b); // 原地减法
a.mulEq(b); // 原地乘法
a.divEq(b); // 原地除法

a.blendEq(b); // 混合

a.set(0.5, 0.5, 0.5, 1); // 设置分量
var result = new GameRGBAColor(0, 0, 0, 0);
result.copy(a); // 浅拷贝 a
var clone = a.clone(); // 深拷贝

var lerped = a.lerp(b, 0.5); // 插值
var eq = a.equals(b); // 比较
```

## GameQuaternion

四元数，用于 3D 旋转。

### 构造

```js
var q = new GameQuaternion(0, 0, 0, 1); // w, x, y, z
```

### 方法

| 方法                                              | 说明                      |
| ------------------------------------------------- | ------------------------- |
| `q.set(w, x, y, z)`                               | 设置分量                  |
| `q.copy(other)`                                   | 浅拷贝                    |
| `q.clone()`                                       | 深拷贝                    |
| `q.add(p)` / `q.sub(p)` / `q.mul(p)` / `q.div(p)` | 算术                      |
| `q.inv()`                                         | 逆四元数                  |
| `q.dot(p)`                                        | 点积                      |
| `q.mag()` / `q.sqrMag()`                          | 模长                      |
| `q.normalize()`                                   | 归一化                    |
| `q.slerp(p, t)`                                   | 球面线性插值              |
| `q.angle(p)`                                      | 与另一四元数的夹角 (弧度) |
| `q.getAxisAngle()`                                | 获取旋转轴和角度          |
| `q.rotateX(a)` / `q.rotateY(a)` / `q.rotateZ(a)`  | 绕轴旋转                  |
| `q.equals(p)`                                     | 比较                      |

### 静态方法

```js
var q1 = GameQuaternion.fromAxisAngle(axis, angle);
var q2 = GameQuaternion.fromEuler(x, y, z);
var q3 = GameQuaternion.rotationBetween(fromVec, toVec);
```
