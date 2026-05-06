# Math Types

All ✅ Box3 API. The following data types are available globally in JS.

## GameVector3

3D vector for positions, directions, velocities, etc.

### Construction

```js
var v = new GameVector3(0, 100, 0); // x, y, z
```

### Properties

```js
v.x = 10; // read/write
v.y = 20;
v.z = 30;
```

### Methods

| Method           | Returns       | Description                   |
| ---------------- | ------------- | ----------------------------- |
| `v.set(x, y, z)` | `GameVector3` | Set components, returns self  |
| `v.add(w)`       | `GameVector3` | Addition, returns new vector  |
| `v.sub(w)`       | `GameVector3` | Subtraction                   |
| `v.scale(s)`     | `GameVector3` | Scalar multiplication         |
| `v.dot(w)`       | `number`      | Dot product                   |
| `v.mag()`        | `number`      | Vector magnitude              |
| `v.sqrMag()`     | `number`      | Squared magnitude (faster)    |
| `v.normalize()`  | `GameVector3` | Normalize, returns new vector |
| `v.distance(w)`  | `number`      | Distance between two points   |
| `v.lerp(w, t)`   | `GameVector3` | Linear interpolation, t 0–1   |
| `v.equals(w)`    | `boolean`     | Component-wise equality       |

### Static Methods

```js
var v = GameVector3.fromPolar(mag, phi, theta); // spherical → vector
```

```js
var pos = new GameVector3(0, 100, 0);
var target = new GameVector3(10, 100, 10);

// Distance between two points
var dist = pos.distance(target); // ~14.14

// Direction vector
var dir = target.sub(pos).normalize();

// Teleport
entity.position.set(0, 100, 0);
```

## GameBounds3

Axis-aligned bounding box (AABB).

### Construction

```js
var bounds = new GameBounds3(
  new GameVector3(-1, 0, -1), // lower bound (lo)
  new GameVector3(1, 2, 1), // upper bound (hi)
);
```

### Methods

| Method                     | Returns   | Description                                  |
| -------------------------- | --------- | -------------------------------------------- |
| `bounds.intersects(other)` | `boolean` | Whether it intersects another bounding box   |
| `bounds.contains(point)`   | `boolean` | Whether the point is inside the bounding box |

## GameRGBColor

RGB color, components range 0.0–1.0.

### Construction

```js
var red = new GameRGBColor(1, 0, 0);
var blue = new GameRGBColor(0, 0, 1);
var gray = new GameRGBColor(0.5, 0.5, 0.5);
```

### Properties

```js
color.r = 0.5; // read/write
color.g = 0.8;
color.b = 0.2;
```

### Methods

| Method         | Returns        | Description          |
| -------------- | -------------- | -------------------- |
| `c.lerp(d, t)` | `GameRGBColor` | Linear interpolation |

### Static Methods

```js
var randomColor = GameRGBColor.random(); // random color
```

## GameRGBAColor

Color with alpha channel, components range 0.0–1.0.

### Construction

```js
var semiRed = new GameRGBAColor(1, 0, 0, 0.5);
```

### Methods

```js
var a = new GameRGBAColor(1, 0, 0, 1);
var b = new GameRGBAColor(0, 1, 0, 0.5);

var c = a.add(b); // component-wise addition
var d = a.sub(b); // component-wise subtraction
var e = a.mul(b); // component-wise multiplication
var f = a.div(b); // component-wise division

a.addEq(b); // in-place addition (a += b)
a.subEq(b); // in-place subtraction
a.mulEq(b); // in-place multiplication
a.divEq(b); // in-place division

a.blendEq(b); // blend

a.set(0.5, 0.5, 0.5, 1); // set components
var result = new GameRGBAColor(0, 0, 0, 0);
result.copy(a); // shallow copy from a
var clone = a.clone(); // deep copy

var lerped = a.lerp(b, 0.5); // interpolation
var eq = a.equals(b); // comparison
```

## GameQuaternion

Quaternion for 3D rotations.

### Construction

```js
var q = new GameQuaternion(0, 0, 0, 1); // w, x, y, z
```

### Methods

| Method                                            | Description                           |
| ------------------------------------------------- | ------------------------------------- |
| `q.set(w, x, y, z)`                               | Set components                        |
| `q.copy(other)`                                   | Shallow copy                          |
| `q.clone()`                                       | Deep copy                             |
| `q.add(p)` / `q.sub(p)` / `q.mul(p)` / `q.div(p)` | Arithmetic                            |
| `q.inv()`                                         | Inverse quaternion                    |
| `q.dot(p)`                                        | Dot product                           |
| `q.mag()` / `q.sqrMag()`                          | Magnitude                             |
| `q.normalize()`                                   | Normalize                             |
| `q.slerp(p, t)`                                   | Spherical linear interpolation        |
| `q.angle(p)`                                      | Angle to another quaternion (radians) |
| `q.getAxisAngle()`                                | Get rotation axis and angle           |
| `q.rotateX(a)` / `q.rotateY(a)` / `q.rotateZ(a)`  | Rotate around axis                    |
| `q.equals(p)`                                     | Comparison                            |

### Static Methods

```js
var q1 = GameQuaternion.fromAxisAngle(axis, angle);
var q2 = GameQuaternion.fromEuler(x, y, z);
var q3 = GameQuaternion.rotationBetween(fromVec, toVec);
```
