# voxels — Block Operations API

`voxels` provides pure block-level read/write operations. No entity logic is involved.

## Block Info

### voxels.shape

✅ Box3 API | Read-only `GameVector3`. World dimensions (valid in some Box3 environments).

### voxels.VoxelTypes

✅ Box3 API | Read-only string array. All registered block names.

## Name ↔ ID

### voxels.id(name)

✅ Box3 API | Block name → internal ID. `name` is a namespaced string (e.g. `"minecraft:stone"`).

### voxels.name(id)

✅ Box3 API | Internal ID → block name.

```js
var stoneId = voxels.id("minecraft:stone"); // get ID
var name = voxels.name(stoneId); // "minecraft:stone"
```

## Placing Blocks

### voxels.setVoxel(x, y, z, voxel)

✅ Box3 API | Place a block at the given coordinates. `voxel` accepts:

- String: namespaced ID, e.g. `"minecraft:glass"`
- Number: internal block ID (rotation encoded)

Returns the internal ID of the newly placed block.

### voxels.setVoxel(pos, voxel)

⬆ GameVector3 overload.

### voxels.setVoxel(x, y, z, voxel, rotation)

✅ Box3 API | Place a block with rotation. `rotation` is 0–3, controlling orientation (like `BlockState` rotation).

### voxels.setVoxel(pos, voxel, rotation)

⬆ GameVector3 overload.

```js
// Place by string
voxels.setVoxel(0, 100, 0, "minecraft:glass");
voxels.setVoxel(new GameVector3(0, 100, 0), "minecraft:gold_block");

// With rotation
voxels.setVoxel(0, 100, 0, "minecraft:oak_stairs", 2);
voxels.setVoxel(new GameVector3(0, 100, 0), "minecraft:oak_stairs", 2);
```

### voxels.setVoxelId(x, y, z, voxelId)

✅ Box3 API | Place a block, `voxelId` is the internal ID with encoded rotation.

### voxels.setVoxelId(pos, voxelId)

⬆ GameVector3 overload.

### voxels.fillVoxel(x1, y1, z1, x2, y2, z2, voxel)

⬆ MC Extension | Fill a rectangular region with a block. Corner coordinates are auto-sorted (no need to ensure x1 ≤ x2).

### voxels.fillVoxel(pos1, pos2, voxel)

⬆ GameVector3 overload.

```js
// Fill a 5×1×5 platform
voxels.fillVoxel(-2, 100, -2, 2, 100, 2, "minecraft:white_concrete");
voxels.fillVoxel(
  new GameVector3(-2, 100, -2),
  new GameVector3(2, 100, 2),
  "minecraft:white_concrete",
);

// Clear a region
voxels.fillVoxel(
  new GameVector3(-5, 100, -5),
  new GameVector3(5, 110, 5),
  "minecraft:air",
);
```

## Reading Blocks

### voxels.getVoxel(x, y, z)

✅ Box3 API | Returns the block's base ID (without rotation info).

### voxels.getVoxel(pos)

⬆ GameVector3 overload.

### voxels.getVoxelId(x, y, z)

✅ Box3 API | Returns the full ID (with rotation bits encoded).

### voxels.getVoxelId(pos)

⬆ GameVector3 overload.

### voxels.getVoxelName(x, y, z)

✅ Box3 API | Returns the block's namespaced ID string.

### voxels.getVoxelName(pos)

⬆ GameVector3 overload.

### voxels.getVoxelRotation(x, y, z)

✅ Box3 API | Returns the block's rotation value (0–3).

### voxels.getVoxelRotation(pos)

⬆ GameVector3 overload.

```js
var id = voxels.getVoxel(0, 100, 0); // base ID
var fullId = voxels.getVoxelId(0, 100, 0); // full ID with rotation
var name = voxels.getVoxelName(0, 100, 0); // "minecraft:stone"
var rot = voxels.getVoxelRotation(0, 100, 0); // 0-3

// GameVector3 overloads
var id = voxels.getVoxel(entity.position);
var name = voxels.getVoxelName(new GameVector3(0, 100, 0));
```

### voxels.countVoxel(x1, y1, z1, x2, y2, z2, voxel)

⬆ MC Extension | Count matching blocks in a region. `voxel` can be a string or numeric ID.

### voxels.countVoxel(pos1, pos2, voxel)

⬆ GameVector3 overload.

```js
// Count how many diamond blocks are in the region
var count = voxels.countVoxel(
  -10,
  50,
  -10,
  10,
  80,
  10,
  "minecraft:diamond_block",
);
var count = voxels.countVoxel(
  new GameVector3(-10, 50, -10),
  new GameVector3(10, 80, 10),
  "minecraft:diamond_block",
);
```

## Spawner Control

### voxels.setSpawner(x, y, z, entityType)

⬆ MC Extension | Set the spawn type of the spawner at the given coordinates. Only effective if that block is `minecraft:spawner`.

### voxels.setSpawner(pos, entityType)

⬆ GameVector3 overload.

```js
voxels.setVoxel(0, 100, 0, "minecraft:spawner");
voxels.setSpawner(0, 100, 0, "minecraft:zombie");
voxels.setSpawner(new GameVector3(0, 100, 0), "minecraft:skeleton");
```
