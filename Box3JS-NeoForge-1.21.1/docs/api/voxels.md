# voxels — 方块操作 API

`voxels` 提供纯方块层面的读写操作。不涉及实体逻辑。

---

## 方块信息

### voxels.shape

✅ Box3 API | 只读 `GameVector3`。世界尺寸（仅在部分 Box3 环境中有效）。

### voxels.VoxelTypes

✅ Box3 API | 只读字符串数组。所有已注册方块名称列表。

---

## 名称 ↔ ID

### voxels.id(name)

✅ Box3 API | 方块名称 → 内部 ID。`name` 为带命名空间的字符串（如 `"minecraft:stone"`）。

### voxels.name(id)

✅ Box3 API | 内部 ID → 方块名称。

```js
var stoneId = voxels.id("minecraft:stone");  // 获取 ID
var name = voxels.name(stoneId);             // "minecraft:stone"
```

---

## 放置方块

### voxels.setVoxel(x, y, z, voxel)

✅ Box3 API | 在指定坐标放置方块。`voxel` 参数接受：
- 字符串：命名空间 ID，如 `"minecraft:glass"`
- 数字：内部方块 ID（含 rotation 编码）

返回新放置方块的内部 ID。

### voxels.setVoxel(pos, voxel)

⬆ GameVector3 重载。

### voxels.setVoxel(x, y, z, voxel, rotation)

✅ Box3 API | 放置方块并指定旋转方向。`rotation` 为 0–3，控制朝向（类似 `BlockState` 的旋转）。

### voxels.setVoxel(pos, voxel, rotation)

⬆ GameVector3 重载。

```js
// 用字符串放置
voxels.setVoxel(0, 100, 0, "minecraft:glass");
voxels.setVoxel(new GameVector3(0, 100, 0), "minecraft:gold_block");

// 指定旋转
voxels.setVoxel(0, 100, 0, "minecraft:oak_stairs", 2);
voxels.setVoxel(new GameVector3(0, 100, 0), "minecraft:oak_stairs", 2);
```

### voxels.setVoxelId(x, y, z, voxelId)

✅ Box3 API | 放置方块，`voxelId` 为已编码 rotation 的内部 ID。

### voxels.setVoxelId(pos, voxelId)

⬆ GameVector3 重载。

### voxels.fillVoxel(x1, y1, z1, x2, y2, z2, voxel)

⬆ MC 扩展 | 在矩形区域内填充方块。坐标两端点会被自动排序（无需保证 x1≤x2）。

### voxels.fillVoxel(pos1, pos2, voxel)

⬆ GameVector3 重载。

```js
// 填充一个 5×1×5 的平台
voxels.fillVoxel(-2, 100, -2, 2, 100, 2, "minecraft:white_concrete");
voxels.fillVoxel(new GameVector3(-2, 100, -2), new GameVector3(2, 100, 2), "minecraft:white_concrete");

// 清除区域
voxels.fillVoxel(new GameVector3(-5, 100, -5), new GameVector3(5, 110, 5), "minecraft:air");
```

---

## 读取方块

### voxels.getVoxel(x, y, z)

✅ Box3 API | 返回方块的基础 ID（不含 rotation 信息）。

### voxels.getVoxel(pos)

⬆ GameVector3 重载。

### voxels.getVoxelId(x, y, z)

✅ Box3 API | 返回完整 ID（含 rotation 编码位）。

### voxels.getVoxelId(pos)

⬆ GameVector3 重载。

### voxels.getVoxelName(x, y, z)

✅ Box3 API | 返回方块的命名空间 ID 字符串。

### voxels.getVoxelName(pos)

⬆ GameVector3 重载。

### voxels.getVoxelRotation(x, y, z)

✅ Box3 API | 返回方块的 rotation 值（0–3）。

### voxels.getVoxelRotation(pos)

⬆ GameVector3 重载。

```js
var id = voxels.getVoxel(0, 100, 0);        // 基础 ID
var fullId = voxels.getVoxelId(0, 100, 0);   // 含 rotation 的完整 ID
var name = voxels.getVoxelName(0, 100, 0);   // "minecraft:stone"
var rot = voxels.getVoxelRotation(0, 100, 0); // 0-3

// GameVector3 重载
var id = voxels.getVoxel(entity.position);
var name = voxels.getVoxelName(new GameVector3(0, 100, 0));
```

### voxels.countVoxel(x1, y1, z1, x2, y2, z2, voxel)

⬆ MC 扩展 | 统计区域内匹配方块的个数。`voxel` 可以是字符串或数字 ID。

### voxels.countVoxel(pos1, pos2, voxel)

⬆ GameVector3 重载。

```js
// 统计区域内有多少个钻石块
var count = voxels.countVoxel(-10, 50, -10, 10, 80, 10, "minecraft:diamond_block");
var count = voxels.countVoxel(new GameVector3(-10, 50, -10), new GameVector3(10, 80, 10), "minecraft:diamond_block");
```

---

## 刷怪笼

### voxels.setSpawner(x, y, z, entityType)

⬆ MC 扩展 | 设置坐标处刷怪笼的刷出类型。只有该坐标是 `minecraft:spawner` 时才有效。

### voxels.setSpawner(pos, entityType)

⬆ GameVector3 重载。

```js
voxels.setVoxel(0, 100, 0, "minecraft:spawner");
voxels.setSpawner(0, 100, 0, "minecraft:zombie");
voxels.setSpawner(new GameVector3(0, 100, 0), "minecraft:skeleton");
```

---

## 常用方块 ID 参考

```js
// 建筑方块
"minecraft:stone"      "minecraft:stone_bricks"    "minecraft:cobblestone"
"minecraft:glass"      "minecraft:white_concrete"  "minecraft:oak_planks"
"minecraft:obsidian"   "minecraft:bedrock"         "minecraft:gold_block"
"minecraft:diamond_block"  "minecraft:beacon"

// 装饰
"minecraft:sea_lantern"   "minecraft:glowstone"
"minecraft:white_stained_glass"  "minecraft:cyan_terracotta"

// 特殊
"minecraft:air"            // 清除方块
"minecraft:spawner"        // 刷怪笼
"minecraft:water"          // 水
"minecraft:lava"           // 岩浆
```

---

## Box3 API 列表

| API | 类型 |
|---|---|
| `shape` | ✅ Box3 |
| `VoxelTypes` | ✅ Box3 |
| `id()` / `name()` | ✅ Box3 |
| `setVoxel()` | ✅ Box3 |
| `setVoxelId()` | ✅ Box3 |
| `getVoxel()` / `getVoxelId()` / `getVoxelName()` | ✅ Box3 |
| `getVoxelRotation()` | ✅ Box3 |

## MC 扩展列表

| API | 类型 |
|---|---|
| `fillVoxel()` | ⬆ MC |
| `countVoxel()` | ⬆ MC |
| `setSpawner()` | ⬆ MC |
