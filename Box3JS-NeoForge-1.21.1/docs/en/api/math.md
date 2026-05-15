---
---

# Math Types

The following data types are globally available in JS.

## GameVector3

A 3D vector with double-precision components. Used for position, direction, velocity, etc.

### Constructor

```js
var v = new GameVector3(); // Zero vector (0, 0, 0)
var v = new GameVector3(x, y, z); // Specified coordinates
```

### Properties

| Property | Type     | Description                           |
| -------- | -------- | ------------------------------------- |
| `v.x`    | `number` | X component (east/west), read/write   |
| `v.y`    | `number` | Y component (up/down), read/write     |
| `v.z`    | `number` | Z component (north/south), read/write |

### Instance Methods

#### Mutating (return this)

| Method           | Returns       | Description                                                           |
| ---------------- | ------------- | --------------------------------------------------------------------- |
| `v.set(x, y, z)` | `GameVector3` | Set all components                                                    |
| `v.copy(w)`      | `GameVector3` | Copy all components from `w`                                          |
| `v.addEq(w)`     | `GameVector3` | In-place addition: `v += w`                                           |
| `v.subEq(w)`     | `GameVector3` | In-place subtraction: `v -= w`                                        |
| `v.mulEq(w)`     | `GameVector3` | In-place component-wise multiplication                                |
| `v.divEq(w)`     | `GameVector3` | In-place component-wise division; divide-by-zero skips that component |
| `v.scaleEq(n)`   | `GameVector3` | In-place scalar multiplication: `v.x *= n` …                          |
| `v.negEq()`      | `GameVector3` | In-place negation: `v = -v`                                           |

#### Creating New Vectors (does not mutate)

| Method                            | Returns       | Description                                                     |
| --------------------------------- | ------------- | --------------------------------------------------------------- |
| `v.clone()`                       | `GameVector3` | Deep copy — independent vector with same values                 |
| `v.add(w)`                        | `GameVector3` | Vector addition: `v + w`                                        |
| `v.sub(w)`                        | `GameVector3` | Vector subtraction: `v - w`                                     |
| `v.mul(w)`                        | `GameVector3` | Component-wise multiplication                                   |
| `v.div(w)`                        | `GameVector3` | Component-wise division; divide-by-zero → 0                     |
| `v.scale(n)`                      | `GameVector3` | Scalar multiplication: each component × `n`                     |
| `v.cross(w)`                      | `GameVector3` | Cross product: `v × w`                                          |
| `v.normalize()`                   | `GameVector3` | Unit vector; zero vector returns `(0,0,0)`                      |
| `v.lerp(w, t)`                    | `GameVector3` | Linear interpolation: `t=0` → this, `t=1` → `w`                 |
| `v.towards(w)`                    | `GameVector3` | Direction vector pointing toward `w` (normalized)               |
| `v.max(w)`                        | `GameVector3` | Component-wise maximum                                          |
| `v.min(w)`                        | `GameVector3` | Component-wise minimum                                          |
| `v.neg()`                         | `GameVector3` | Negation: `-v`                                                  |
| `v.moveTowards(target, maxDelta)` | `GameVector3` | Move toward target by at most `maxDelta` distance               |
| `v.floor()`                       | `GameVector3` | Component-wise floor                                            |
| `v.ceil()`                        | `GameVector3` | Component-wise ceiling                                          |
| `v.clampLength(max)`              | `GameVector3` | Clamp magnitude to `max`, scale down proportionally if exceeded |

#### Numeric Computations

| Method             | Returns  | Description                                        |
| ------------------ | -------- | -------------------------------------------------- |
| `v.dot(w)`         | `number` | Dot (inner) product: `v · w`                       |
| `v.mag()`          | `number` | Magnitude (length)                                 |
| `v.sqrMag()`       | `number` | Squared magnitude — faster than `mag()`            |
| `v.distance(w)`    | `number` | Euclidean distance to `w`                          |
| `v.angle(w)`       | `number` | Angle between `v` and `w` (radians, 0–π)           |
| `v.sqrDistance(w)` | `number` | Squared distance to `w` — faster than `distance()` |

#### Comparison

| Method             | Returns   | Description                                                   |
| ------------------ | --------- | ------------------------------------------------------------- |
| `v.equals(w)`      | `boolean` | Approximate equality, tolerance 1e-6                          |
| `v.exactEquals(w)` | `boolean` | Exact equality — components strictly equal                    |
| `v.isZero()`       | `boolean` | Whether this is (approximately) a zero vector, tolerance 1e-6 |

```js
var pos = new GameVector3(0, 100, 0);
var target = new GameVector3(10, 100, 10);

// Distance
var dist = pos.distance(target); // ~14.14

// Direction vector
var dir = target.sub(pos).normalize();

// Angle
var angle = pos.angle(target); // radians

// Comparison
var a = new GameVector3(1, 2, 3);
var b = new GameVector3(1.0000001, 2.0000001, 3.0000001);
a.equals(b); // true (within tolerance)
a.exactEquals(b); // false

// Teleport entity (LiveVec3)
entity.position.set(0, 100, 0);
```

### Static Methods

```js
// Spherical coordinates → vector
var v = GameVector3.fromPolar(mag, phi, theta);
// mag:   radius
// phi:   azimuth angle (radians, horizontal rotation around Y)
// theta: elevation angle (radians, from horizontal plane)
```

### toString

```js
var v = new GameVector3(1, 2, 3);
v.toString(); // "GameVector3(1.0, 2.0, 3.0)"
```

## GameBounds3

Axis-aligned bounding box (AABB), defined by two opposing corners: `lo` (minimum corner) and `hi` (maximum corner).

### Constructor

```js
var bounds = new GameBounds3(
  new GameVector3(-1, 0, -1), // lo (min corner)
  new GameVector3(1, 2, 1), // hi (max corner)
);
```

### Properties

| Property    | Type          | Description                |
| ----------- | ------------- | -------------------------- |
| `bounds.lo` | `GameVector3` | Minimum corner, read/write |
| `bounds.hi` | `GameVector3` | Maximum corner, read/write |

### Instance Methods

| Method                                     | Returns               | Description                                                |
| ------------------------------------------ | --------------------- | ---------------------------------------------------------- |
| `bounds.set(lox, loy, loz, hix, hiy, hiz)` | `GameBounds3`         | Set all boundaries in-place, returns this                  |
| `bounds.copy(b)`                           | `GameBounds3`         | Copy values from `b` in-place, returns this                |
| `bounds.intersects(other)`                 | `boolean`             | Whether this intersects `other`                            |
| `bounds.intersect(other)`                  | `GameBounds3 \| null` | Intersection bounds, or `null` if no overlap               |
| `bounds.contains(v)`                       | `boolean`             | Whether point `v` is inside (inclusive)                    |
| `bounds.containsBounds(b)`                 | `boolean`             | Whether this fully contains `b`                            |
| `bounds.center()`                          | `GameVector3`         | Center point of the bounds                                 |
| `bounds.size()`                            | `GameVector3`         | Size of the bounds (width, height, depth)                  |
| `bounds.expand(delta)`                     | `GameBounds3`         | Expand all faces outward by `delta`, returns new bounds    |
| `bounds.expandEq(delta)`                   | `GameBounds3`         | In-place expand all faces outward by `delta`, returns this |
| `bounds.growToInclude(v)`                  | `GameBounds3`         | In-place grow to include point `v`, returns this           |
| `bounds.closestPoint(v)`                   | `GameVector3`         | Closest point on the bounds to point `v`                   |
| `bounds.move(offset)`                      | `GameBounds3`         | Translate by `offset`, returns new bounds                  |
| `bounds.moveEq(offset)`                    | `GameBounds3`         | In-place translate by `offset`, returns this               |

### Static Methods

```js
// Create minimal bounds from an array of GameVector3
var points = [new GameVector3(0, 0, 0), new GameVector3(5, 10, 3)];
var box = GameBounds3.fromPoints(points); // returns GameBounds3 or null
```

### toString

```js
bounds.toString(); // "GameBounds3(GameVector3(-1.0, 0.0, -1.0), GameVector3(1.0, 2.0, 1.0))"
```

```js
// Query entities within bounds
var entities = world.searchBox(bounds);

// Check if point is inside
if (bounds.contains(player.position)) {
  // Player is inside the area
}
```

## GameRGBColor

An RGB color with three channels ranging from 0.0 to 1.0.

### Constructor

```js
var red = new GameRGBColor(1, 0, 0);
var blue = new GameRGBColor(0, 0, 1);
var gray = new GameRGBColor(0.5, 0.5, 0.5);
```

### Properties

| Property  | Type     | Description                     |
| --------- | -------- | ------------------------------- |
| `color.r` | `number` | Red channel (0–1), read/write   |
| `color.g` | `number` | Green channel (0–1), read/write |
| `color.b` | `number` | Blue channel (0–1), read/write  |

### Instance Methods

#### Mutating (return this)

| Method           | Returns        | Description                                          |
| ---------------- | -------------- | ---------------------------------------------------- |
| `c.set(r, g, b)` | `GameRGBColor` | Set all channels                                     |
| `c.copy(o)`      | `GameRGBColor` | Copy all channels from another color                 |
| `c.addEq(o)`     | `GameRGBColor` | In-place addition: `c += o`                          |
| `c.subEq(o)`     | `GameRGBColor` | In-place subtraction: `c -= o`                       |
| `c.mulEq(o)`     | `GameRGBColor` | In-place channel-wise multiplication                 |
| `c.divEq(o)`     | `GameRGBColor` | In-place channel-wise division; divide-by-zero skips |
| `c.scaleEq(n)`   | `GameRGBColor` | In-place scalar multiplication: each channel × `n`   |

#### Creating New Colors (does not mutate)

| Method         | Returns        | Description                                     |
| -------------- | -------------- | ----------------------------------------------- |
| `c.clone()`    | `GameRGBColor` | Deep copy                                       |
| `c.add(o)`     | `GameRGBColor` | Channel-wise addition                           |
| `c.sub(o)`     | `GameRGBColor` | Channel-wise subtraction                        |
| `c.mul(o)`     | `GameRGBColor` | Channel-wise multiplication                     |
| `c.div(o)`     | `GameRGBColor` | Channel-wise division; divide-by-zero → 0       |
| `c.lerp(o, t)` | `GameRGBColor` | Linear interpolation: `t=0` → this, `t=1` → `o` |
| `c.scale(n)`   | `GameRGBColor` | Scalar multiplication: each channel × `n`       |
| `c.equals(o)`  | `boolean`      | Approximate equality, tolerance 1e-6            |
| `c.toRGBA()`   | `string`       | CSS format string: `"rgba(r,g,b,1.0)"`          |

### Static Methods

```js
var randomColor = GameRGBColor.random(); // Each channel 0–1 random
```

### toString

```js
new GameRGBColor(1, 0.5, 0).toString(); // "GameRGBColor(1.0, 0.5, 0.0)"
```

## GameRGBAColor

An RGBA color with four channels ranging from 0.0 to 1.0.

### Constructor

```js
var semiRed = new GameRGBAColor(1, 0, 0, 0.5);
var opaque = new GameRGBAColor(0, 1, 0, 1.0);
```

### Properties

| Property  | Type     | Description                     |
| --------- | -------- | ------------------------------- |
| `color.r` | `number` | Red channel (0–1), read/write   |
| `color.g` | `number` | Green channel (0–1), read/write |
| `color.b` | `number` | Blue channel (0–1), read/write  |
| `color.a` | `number` | Alpha opacity (0–1), read/write |

### Instance Methods

#### Mutating (return this)

| Method              | Returns         | Description                                          |
| ------------------- | --------------- | ---------------------------------------------------- |
| `c.set(r, g, b, a)` | `GameRGBAColor` | Set all four channels                                |
| `c.copy(o)`         | `GameRGBAColor` | Copy all channels from another RGBA color            |
| `c.addEq(o)`        | `GameRGBAColor` | In-place addition                                    |
| `c.subEq(o)`        | `GameRGBAColor` | In-place subtraction                                 |
| `c.mulEq(o)`        | `GameRGBAColor` | In-place channel-wise multiplication                 |
| `c.divEq(o)`        | `GameRGBAColor` | In-place channel-wise division; divide-by-zero skips |
| `c.scaleEq(n)`      | `GameRGBAColor` | In-place scalar multiplication: each channel × `n`   |

#### Creating New Colors (does not mutate)

| Method           | Returns         | Description                                               |
| ---------------- | --------------- | --------------------------------------------------------- |
| `c.clone()`      | `GameRGBAColor` | Deep copy                                                 |
| `c.add(o)`       | `GameRGBAColor` | Channel-wise addition                                     |
| `c.sub(o)`       | `GameRGBAColor` | Channel-wise subtraction                                  |
| `c.mul(o)`       | `GameRGBAColor` | Channel-wise multiplication                               |
| `c.div(o)`       | `GameRGBAColor` | Channel-wise division; divide-by-zero → 0                 |
| `c.lerp(o, t)`   | `GameRGBAColor` | Linear interpolation                                      |
| `c.scale(n)`     | `GameRGBAColor` | Scalar multiplication: each channel × `n`                 |
| `c.equals(o)`    | `boolean`       | Approximate equality, tolerance 1e-6                      |
| `c.blendEq(rgb)` | `GameRGBColor`  | Alpha-blend onto an RGB background, returns displayed RGB |

### toString

```js
new GameRGBAColor(1, 0, 0, 0.5).toString(); // "GameRGBAColor(1.0, 0.0, 0.0, 0.5)"
```

```js
// Alpha blending
var fg = new GameRGBAColor(1, 0, 0, 0.5); // Semi-transparent red
var bg = new GameRGBColor(1, 1, 1); // White background
var result = fg.blendEq(bg); // Blended RGB color
```

## GameQuaternion

A quaternion used for 3D rotation. Unit quaternions (magnitude=1) represent pure rotations.

### Constructor

```js
var q = new GameQuaternion(); // Identity (1, 0, 0, 0)
var q = new GameQuaternion(w, x, y, z); // Specified components
```

### Properties

| Property | Type     | Description                         |
| -------- | -------- | ----------------------------------- |
| `q.w`    | `number` | Real (scalar) component, read/write |
| `q.x`    | `number` | Imaginary X component, read/write   |
| `q.y`    | `number` | Imaginary Y component, read/write   |
| `q.z`    | `number` | Imaginary Z component, read/write   |

### Instance Methods

#### Mutating (return this)

| Method              | Returns          | Description                  |
| ------------------- | ---------------- | ---------------------------- |
| `q.set(w, x, y, z)` | `GameQuaternion` | Set all components           |
| `q.copy(p)`         | `GameQuaternion` | Copy all components from `p` |

#### Creating New Quaternions (does not mutate)

| Method          | Returns          | Description                                     |
| --------------- | ---------------- | ----------------------------------------------- |
| `q.clone()`     | `GameQuaternion` | Deep copy                                       |
| `q.add(p)`      | `GameQuaternion` | Component-wise addition                         |
| `q.sub(p)`      | `GameQuaternion` | Component-wise subtraction                      |
| `q.mul(p)`      | `GameQuaternion` | Hamilton product: `q × p` (NOT commutative)     |
| `q.div(p)`      | `GameQuaternion` | Division: `q × p⁻¹`                             |
| `q.inv()`       | `GameQuaternion` | Conjugate (equals inverse for unit quaternions) |
| `q.normalize()` | `GameQuaternion` | Normalize, returns unit quaternion              |

#### Interpolation

| Method          | Returns          | Description                                               |
| --------------- | ---------------- | --------------------------------------------------------- |
| `q.slerp(p, t)` | `GameQuaternion` | Spherical linear interpolation: `t=0` → this, `t=1` → `p` |

#### Numeric Computations

| Method        | Returns   | Description                           |
| ------------- | --------- | ------------------------------------- |
| `q.dot(p)`    | `number`  | Dot product                           |
| `q.mag()`     | `number`  | Magnitude (norm)                      |
| `q.sqrMag()`  | `number`  | Squared magnitude                     |
| `q.angle(p)`  | `number`  | Angular difference from `p` (radians) |
| `q.equals(p)` | `boolean` | Approximate equality, tolerance 1e-6  |

#### Rotation Operations (rotate around local axes, returns new quaternion)

| Method              | Returns          | Description                                                         |
| ------------------- | ---------------- | ------------------------------------------------------------------- |
| `q.rotateX(rad)`    | `GameQuaternion` | Rotate around X axis                                                |
| `q.rotateY(rad)`    | `GameQuaternion` | Rotate around Y axis                                                |
| `q.rotateZ(rad)`    | `GameQuaternion` | Rotate around Z axis                                                |
| `q.rotateVector(v)` | `GameVector3`    | Rotate vector `v` by this quaternion                                |
| `q.toEuler()`       | `GameVector3`    | Convert to Euler angles (YZX order), returns `(x, y, z)` in radians |

#### Axis-Angle Decomposition

```js
var result = q.getAxisAngle();
// result.angle — rotation angle (radians)
// result.axis  — rotation axis (unit GameVector3)
```

### Static Methods

```js
// Create from axis-angle representation
var q1 = GameQuaternion.fromAxisAngle(axis, rad);
// axis: GameVector3 (auto-normalized)
// rad:  rotation angle (radians)

// Create from Euler angles (YZX rotation order: Y → Z → X)
var q2 = GameQuaternion.fromEuler(x, y, z);
// x, y, z: rotation around each axis in radians

// Shortest-arc quaternion rotating from vector a to b
var q3 = GameQuaternion.rotationBetween(fromVec, toVec);

// Create quaternion from look-at direction (from → to)
var q4 = GameQuaternion.lookAt(from, to, up);
// from: GameVector3 — observer position
// to:   GameVector3 — target point
// up:   GameVector3 — up direction (default (0,1,0))
```

### toString

```js
q.toString(); // "GameQuaternion(0.707, 0.0, 0.707, 0.0)"
```
