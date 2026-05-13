# Box3 API vs Box3JS 实现对比

本文档详细对比官方 Box3 平台 API 与 Box3JS 模组（NeoForge 1.21.1）的实现差异。仅涉及**服务端 API**，因为客户端 API（ClientUI、ClientAudio、ClientMedia 等）在 Box3JS 中完全不可用——Minecraft 模组运行在服务端，没有 Box3 平台的客户端渲染环境。

> **图例**: ✅ 已实现 | ⚠️ 部分实现 | ❌ 未实现 | ⬆ 独有扩展

---

## 目录

1. [GameWorld (world)](#1-gameworld-world)
2. [GameEntity (entity)](#2-gameentity-entity)
3. [GamePlayerEntity (entity.player)](#3-gameplayerentity-entityplayer)
4. [GameVoxels (voxels)](#4-gamevoxels-voxels)
5. [GameDataStorage (storage)](#5-gamedatastorage-storage)
6. [Math 类型](#6-math-类型)
7. [其他服务端 API](#7-其他服务端-api)
8. [客户端 API（不适用）](#8-客户端-api不适用)
9. [Box3JS 独有 MC 扩展](#9-box3js-独有-mc-扩展)
10. [总结](#10-总结)

---

## 1. GameWorld (world)

### 1.1 基础属性

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `world.projectName()` (只读方法) | `world.projectName` (属性) | ✅ | 返回当前脚本项目名；服务器 MOTD 使用 `world.serverId` |
| `world.serverId` (属性) | `world.serverId` (读写属性) | ✅ | 一致。映射到服务端 MOTD（get/set） |
| `world.currentTick()` (只读方法) | `world.currentTick` (属性) | ✅ | 返回服务器总 tick 数 |
| `world.url` (只读属性) | — | ❌ | 未实现。MC 无地图 URL 概念 |

### 1.2 天气

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `world.rainDensity` (属性 0-1) | `world.rainDensity` (属性) | ✅ | 一致 |
| — | `world.thunderDensity` (属性) | ⬆ | MC 扩展。Box3 无打雷概念 |
| — | `world.clearWeather()` | ⬆ | MC 扩展。清除雨和雷 |
| `world.maxFog` | — | ❌ | |
| `world.fogColor` | — | ❌ | |
| `world.fogStartDistance` | — | ❌ | |
| `world.fogHeightOffset` | — | ❌ | |
| `world.fogUniformDensity` | — | ❌ | |
| `world.fogHeightFalloff` | — | ❌ | |
| `world.rainSpeed` | — | ❌ | |
| `world.rainColor` | — | ❌ | |
| `world.rainDirection` | — | ❌ | |
| `world.rainInterference` | — | ❌ | |
| `world.rainSizeLo/Hi` | — | ❌ | |
| `world.snowColor` | — | ❌ | Box3 有独立雪花系统，MC 降雪依附于原版天气 |
| `world.snowTexture` | — | ❌ | |
| `world.snowDensity` | — | ❌ | |
| `world.snowFallSpeed` | — | ❌ | |
| `world.snowSpinSpeed` | — | ❌ | |
| `world.snowSizeLo/Hi` | — | ❌ | |
| `world.lightMode` | — | ❌ | Box3 的手动/自然光照模式 |
| `world.sunFrequency/Phase/Direction/Light` | — | ❌ | |
| `world.skyLeftLight/RightLight/...` | — | ❌ | Box3 六个方向环境光 |
| `world.lunarPhase` | — | ❌ | |

**原因**: Box3 拥有独立的天气/光照渲染引擎，可以精细控制雾、雨、雪、光照参数。MC 的天气和光照系统由原版引擎控制，模组无法在不安装客户端 mod 的情况下改变这些视觉效果。要实现这些需要客户端侧渲染 hook，超出了服务端脚本引擎的范围。

### 1.3 时间

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `world.time` (属性) | `world.time` (属性, 本质是 getTime/setTime) | ✅ | 一致。Box3 一天 = 24000 tick |
| `world.setTime(tick)` | `world.setTime(tick)` | ✅ | 便捷方法，一致 |
| `world.timeScale` (属性 0-1) | `world.timeScale` (属性) | ✅ | 一致。底层操作 doDaylightCycle 规则 |
| — | `world.setTime(tick)` | ⬆ | 等同于 `world.time = tick` |

### 1.4 难度

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `world.difficulty` (属性) | `world.difficulty` (属性) | ✅ | 一致。get 返回名称字符串，set 接受名称或数字 0-3 |

### 1.5 出生点

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `world.spawnPoint` (只读属性) | `world.spawnPoint` (只读属性) | ✅ | 一致，返回 GameVector3 |
| `world.setWorldSpawn(pos)` | `world.setWorldSpawn(pos)` | ✅ | 一致 |

### 1.6 游戏规则

Box3 **没有**游戏规则 API。Box3JS 完全为 MC 扩展：

| Box3JS API | 说明 |
|------------|------|
| `world.getGameRule(name)` | MC 扩展。获取游戏规则布尔值 |
| `world.setGameRule(name, value)` | MC 扩展。支持 7 种规则：doDaylightCycle, doWeatherCycle, keepInventory, doMobSpawning, doFireTick, mobGriefing, doImmediateRespawn |

### 1.7 物理系统

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `world.useOBB` | — | ❌ | OBB 碰撞检测 |
| `world.gravity` | — | ❌ | 世界重力 |
| `world.airFriction` | — | ❌ | 空气阻力 |
| `world.addCollisionFilter(a, b)` | — | ❌ | 碰撞过滤 |
| `world.removeCollisionFilter(a, b)` | — | ❌ | |
| `world.clearCollisionFilters()` | — | ❌ | |
| `world.collisionFilters()` | — | ❌ | |
| `world.testSelector(sel, ent)` | — | ❌ | |

**原因**: MC 的物理系统由原版引擎控制，重力/碰撞/摩擦力是全局常量，无法通过脚本动态修改。

### 1.8 实体生成

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `world.createEntity(config)` | `world.createEntity(config)` | ✅ | 一致。接受 NativeObject 配置对象，支持 type/position/velocity/fixed/gravity/friction/mass/restitution/collides/meshInvisible/hp/maxHp/tags 字段 |
| `world.spawnEntity(type, pos)` | `world.spawnEntity(type, pos)` | ⬆ | 便捷方法。简化版生成，仅接受类型+位置 |
| `world.entityQuota()` | — | ❌ | 实体配额查询 |

### 1.9 查询

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `world.querySelector(selector)` | `world.querySelector(selector)` | ✅ | 一致 |
| `world.querySelectorAll(selector)` | `world.querySelectorAll(selector)` | ✅ | 一致 |
| `world.searchBox(bounds)` | `world.searchBox(bounds)` | ✅ | 一致。接受 GameBounds3，内部委托 entitiesInArea |
| `world.raycast(origin, dir, options?)` | `world.raycast(origin, dir)` / `world.raycast(origin, dir, maxDist)` | ⚠️ | Box3JS 无 options 对象（无 ignoreFluid/ignoreVoxel/ignoreEntities 等），仅支持 maxDistance |
| — | `world.entitiesInArea(pos1, pos2)` | ⬆ | MC 扩展 |
| — | `world.entitiesInRadius(x, y, z, r)` / `world.entitiesInRadius(pos, r)` | ⬆ | MC 扩展 |
| — | `world.getBiome(x, y, z)` / `world.getBiome(pos)` | ⬆ | MC 扩展。返回生物群系 ID（如 "minecraft:plains"） |

**Raycast 返回值差异**:
- Box3 返回: `{origin, direction, distance, hit, hitEntity, hitPosition, hitVoxel, voxelIndex, normal}`
- Box3JS 返回: `{hit, x, y, z, normalX, normalY, normalZ, distance, entity, voxel}`
- 差异: 字段命名不同 (`normal` → `normalX/Y/Z`, `hitEntity` → `entity`)，无 `origin/direction/hitPosition/voxelIndex`

### 1.10 区域系统 (GameZone)

Box3 的区域系统完全未实现：

| Box3 API | 状态 |
|----------|------|
| `world.addZone(config)` | ❌ |
| `world.removeZone(trigger)` | ❌ |
| `world.zones()` | ❌ |
| `zone.onEnter / zone.onLeave` | ❌ |
| `zone.entities` | ❌ |
| `zone.remove()` | ❌ |

Zone 可以设置局部天气/光照/力场参数，这在 MC 服务端无法实现（需要客户端渲染支持）。

### 1.11 聊天

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `world.say(message)` | `world.say(message)` | ✅ | 一致。全服广播 |
| `world.createTempChat(chatId?)` | — | ❌ | 临时聊天频道 |
| `world.destroyTempChat(chatId)` | — | ❌ | |
| `world.addTempChatPlayer(chatId, player)` | — | ❌ | |
| `world.removeTempChatPlayer(chatId, player)` | — | ❌ | |
| `world.getTempChats()` | — | ❌ | |
| `world.getTempChatUsers(chatId)` | — | ❌ | |

### 1.12 事件回调

| Box3 API | Box3JS 实现 | 状态 | 回调签名差异 |
|----------|-------------|------|-------------|
| `world.onTick(fn)` | `world.onTick(fn)` → GameEventHandlerToken | ✅ | Box3 传入 `{tick, prevTick, elapsedTimeMS, skip}`；Box3JS 已对齐传入 NativeObject |
| `world.onPlayerJoin(fn)` | `world.onPlayerJoin(fn)` → GameEventHandlerToken | ✅ | Box3 传入 `{entity, tick}`；Box3JS 传入 `(entity, tick)` |
| `world.onPlayerLeave(fn)` | `world.onPlayerLeave(fn)` → GameEventHandlerToken | ✅ | 同上 |
| `world.onChat(fn)` | `world.onChat(fn)` → GameEventHandlerToken | ✅ | Box3 传入 `{entity, message, tick}`；Box3JS 传入 `(entity, message, tick)` — 展开参数 |
| `world.onInteract(fn)` | `world.onInteract(fn)` → GameEventHandlerToken | ✅ | Box3 传入 `{entity, targetEntity, tick}`；Box3JS 传入 `(entity, target, tick)` — 展开参数 |
| `world.onEntityContact(fn)` | `world.onEntityContact(fn)` → GameEventHandlerToken | ✅ | Box3 传入 `{axis, entity, force, other, tick}`；Box3JS 传入 `(entity, other, tick)` — 缺少 axis/force |
| `world.onEntitySeparate(fn)` | `world.onEntitySeparate(fn)` → GameEventHandlerToken | ✅ | 同上 |
| `world.onVoxelContact(fn)` | `world.onVoxelContact(fn)` → GameEventHandlerToken | ✅ | Box3 传入 `{axis, entity, force, voxel, tick, x, y, z}`；Box3JS 传入 `(entity, voxelId, x, y, z, contactType, force, tick)` |
| `world.onFluidEnter(fn)` | `world.onFluidEnter(fn)` → GameEventHandlerToken | ✅ | Box3 传入 `{entity, voxel, tick}`；Box3JS 传入 `(entity, fluid, x, y, z, tick)` |
| `world.onFluidLeave(fn)` | `world.onFluidLeave(fn)` → GameEventHandlerToken | ✅ | 同上 |
| `world.onEntityCreate(fn)` | — | ❌ | |
| `world.onEntityDestroy(fn)` | — | ❌ | 实体层面有 `entity.onDestroy` 属性 |
| `world.onClick(fn)` | — | ❌ | Box3 鼠标点击事件，MC 无对应 |
| `world.onPress(fn)` | `world.onButtonPressed(fn)` → GameEventHandlerToken | ⚠️ | Box3 传入 `{entity, button, raycast, tick, position, pressed}`；Box3JS 传入 `(entity, button, tick)` — 简化版 |
| `world.onRelease(fn)` | — | ❌ | 按键释放事件 |
| `world.onTakeDamage(fn)` | `world.onEntityDamage(fn)` → GameEventHandlerToken | ⚠️ | Box3 传入 `{entity, attacker, damage, damageType, tick}`；Box3JS 传入 `(entity, amount, source, attacker, tick)` — 展开参数 |
| `world.onDie(fn)` | `world.onEntityDeath(fn)` → GameEventHandlerToken | ⚠️ | Box3 传入 `{entity, attacker, damageType, tick}`；Box3JS 传入 `(entity, killer, tick)`，killer 可能为 null |
| `world.onRespawn(fn)` | `world.onPlayerRespawn(fn)` → GameEventHandlerToken | ⚠️ | 命名不同（Respawn → PlayerRespawn），Box3 传入 `{entity, tick}`；Box3JS 传入 `(entity, tick)` |
| `world.onPlayerPurchaseSuccess(fn)` | — | ❌ | MC 无商城系统 |
| — | `world.onVoxelDestroy(fn)` → GameEventHandlerToken | ⬆ | MC 扩展。`(entity, x, y, z, voxel, tick)` |
| — | `world.onBlockPlace(fn)` → GameEventHandlerToken | ⬆ | MC 扩展。`(entity, x, y, z, voxel, voxelId, tick)` |
| — | `world.onBlockActivate(fn)` → GameEventHandlerToken | ⬆ | MC 扩展。`(entity, x, y, z, voxel, tick)` |
| — | `world.onEntityDamage(fn)` → GameEventHandlerToken | ⬆ | MC 扩展。`(entity, amount, source, attacker, tick)` |
| — | `world.onMessage(fn)` → GameEventHandlerToken | ⬆ | MC 扩展。跨脚本消息 |

**关键差异总结**:
1. 所有事件注册**已返回 GameEventHandlerToken**，支持 `.cancel()` 和 `.active()`
2. Box3 事件回调接收**事件对象**，Box3JS 回调接收**展开的参数列表**（JS 展开参数风格）
3. `onTick` 已对齐传入 `{tick, prevTick, elapsedTimeMS, skip}`；`onPlayerJoin/Leave/Respawn` 已添加 tick 参数
4. Box3 的 `onVoxelContact` 每个碰撞帧触发（持续），Box3JS 仅在进入/离开时触发

### 1.13 音效

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `world.ambientSound` (属性) | `world.ambientSound` (属性) | ✅ | 一致。存储音效路径字符串 |
| `world.playerJoinSound` (属性) | `world.playerJoinSound` (属性) | ✅ | 一致 |
| `world.playerLeaveSound` (属性) | `world.playerLeaveSound` (属性) | ✅ | 一致 |
| `world.placeVoxelSound` (属性) | `world.placeVoxelSound` (属性) | ✅ | 一致 |
| `world.breakVoxelSound` (属性) | `world.breakVoxelSound` (属性) | ✅ | 一致 |
| `world.sound(config)` | `world.sound(config)` | ✅ | 一致。接受 String 路径或 `{path, position, volume, pitch}` 对象。内部委托 playSound |
| — | `world.playSound(path, x, y, z, volume, pitch)` | ⬆ | MC 扩展。展开参数版本 |

### 1.14 计时器

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `world.setTimeout(fn, ticks)` | `world.setTimeout(fn, ticks)` | ✅ | 一致 |
| `world.setInterval(fn, ticks)` | `world.setInterval(fn, ticks)` | ✅ | 一致 |
| `world.clearTimeout(id)` | `world.clearTimeout(id)` | ✅ | 一致 |
| `world.clearInterval(id)` | `world.clearInterval(id)` | ✅ | 一致 |

**注意**: Box3 的 setTimeout/setInterval 在官方文档中标注为 ，但 Box3JS 在 `world` 全局对象上直接提供，用法一致。

### 1.15 视觉效果

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `world.strikeLightning(x, y, z)` | `world.strikeLightning(x, y, z)` | ✅ | 一致。额外支持 damage 参数 |
| `world.strikeLightning(pos)` | `world.strikeLightning(pos)` | ✅ | GameVector3 重载 |
| — | `world.strikeLightning(x, y, z, damage)` | ⬆ | 传自定义伤害值 |
| `world.launchFirework(x, y, z, color, shape)` | `world.launchFirework(x, y, z, color, shape)` | ✅ | 颜色和形状枚举值一致 |
| `world.launchFirework(pos, color, shape)` | `world.launchFirework(pos, color, shape)` | ✅ | GameVector3 重载 |
| `world.spawnParticle(type, x, y, z, count, dx, dy, dz, speed)` | `world.spawnParticle(type, x, y, z, count, dx, dy, dz, speed)` | ✅ | 一致 |
| `world.spawnParticle(type, pos, count, dx, dy, dz, speed)` | `world.spawnParticle(type, pos, count, dx, dy, dz, speed)` | ✅ | GameVector3 重载 |
| — | `world.spawnParticleCircle(x, y, z, radius, type, count)` | ⬆ | MC 扩展。圆形粒子圈 |
| — | `world.spawnParticleCircle(pos, radius, type, count)` | ⬆ | GameVector3 重载 |
| — | `world.spawnParticle(x, y, z, color, count, dx, dy, dz, speed)` | ⬆ | MC 扩展。使用 GameRGBColor 生成彩色 dust 粒子 |
| — | `world.spawnParticle(pos, color, count, dx, dy, dz, speed)` | ⬆ | GameVector3 + GameRGBColor 重载 |
| — | `world.launchFirework(x, y, z, colors, shape)` | ⬆ | MC 扩展。colors 为 GameRGBColor[] 数组 |
| — | `world.launchFirework(pos, colors, shape)` | ⬆ | GameVector3 + GameRGBColor[] 重载 |

### 1.16 物品与抛射物

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `world.dropItem(x, y, z, itemId, count)` | `world.dropItem(x, y, z, itemId, count)` | ✅ | 一致 |
| `world.dropItem(pos, itemId, count)` | `world.dropItem(pos, itemId, count)` | ✅ | GameVector3 重载 |
| `world.launchProjectile(type, x, y, z, tx, ty, tz, speed)` | `world.launchProjectile(type, x, y, z, tx, ty, tz, speed)` | ✅ | 一致 |
| `world.launchProjectile(type, pos, target, speed)` | `world.launchProjectile(type, pos, target, speed)` | ✅ | GameVector3 重载 |

### 1.17 爆炸

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `world.explode(x, y, z, power)` | `world.explode(x, y, z, power)` | ✅ | 一致 |
| `world.explode(pos, power)` | `world.explode(pos, power)` | ✅ | GameVector3 重载 |
| — | `world.explode(x, y, z, power, fire)` | ⬆ | MC 扩展。fire=true 可引燃方块 |

### 1.18 动画

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `world.animate(keyframes, playback?)` | — | ❌ | 世界级关键帧动画 |
| `world.getAnimations()` | — | ❌ | |
| `world.getEntityAnimations()` | — | ❌ | |
| `world.getPlayerAnimations()` | — | ❌ | |

### 1.19 传送

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `world.teleport(mapId, players, serverId?)` | — | ❌ | 地图组间传送。MC 无地图组概念 |

### 1.20 跨脚本消息

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| — | `world.sendMessage(target, data)` | ⬆ | MC 扩展。target 为 `"*"` 或项目名 |
| — | `world.onMessage(fn)` | ⬆ | MC 扩展。回调 `(from, data)` |

### 1.21 控制台命令

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| — | `world.runCommand(cmd)` | ⬆ | MC 扩展。以控制台身份执行命令 |

### 1.22 记分板 (全部 MC 扩展)

Box3 无记分板系统。

| Box3JS API | 说明 |
|------------|------|
| `world.addScoreboard(name)` | 创建 dummy 类型记分项 |
| `world.addScoreboard(name, criteria)` | 指定标准（dummy/deathCount 等） |
| `world.removeScoreboard(name)` | 删除记分项 |
| `world.setScore(entityOrName, objective, value)` | 设置分数 |
| `world.getScore(entityOrName, objective)` | 获取分数 |
| `world.showScoreboard(slot, objective)` | 在 sidebar/list/belowname 显示 |
| `world.hideScoreboard(slot)` | 隐藏槽位 |
| `world.listScores(objective)` | 列出所有条目 |

### 1.23 Boss 血条 (全部 MC 扩展)

Box3 无 Bossbar 系统。

| Box3JS API | 说明 |
|------------|------|
| `world.showBossbar(name, text, progress, color)` | 显示或更新血条 |
| `world.removeBossbar(name)` | 移除血条 |

### 1.24 队伍 (全部 MC 扩展)

Box3 无队伍系统。

| Box3JS API | 说明 |
|------------|------|
| `world.createTeam(name, color)` | 创建队伍 |
| `world.removeTeam(name)` | 删除队伍 |
| `world.joinTeam(entity, teamName)` | 加入队伍 |
| `world.leaveTeam(entity)` | 离开队伍 |
| `world.getTeamOf(entity)` | 查询队伍 |

### 1.25 世界边界 (全部 MC 扩展)

Box3 无世界边界概念。

| Box3JS API | 说明 |
|------------|------|
| `world.borderSize` (属性) | 获取/设置边界大小 |
| `world.setBorderCenter(x, z)` | 设置边界中心 |
| `world.shrinkBorder(targetSize, seconds)` | 平滑缩圈 |
| `world.setBorderDamage(damage)` | 边界外伤害值 |
| `world.setBorderWarning(blocks)` | 警告距离 |

### 1.26 合成管理 (全部 MC 扩展)

| Box3JS API | 说明 |
|------------|------|
| `world.listRecipes(filter)` | 搜索匹配 filter 的合成配方 ID 列表 |
| `world.removeRecipe(recipeId)` | 禁用指定配方（加入黑名单） |
| `world.clearRecipes()` | 清除黑名单，恢复所有原始配方 |

### 1.28 结构与成就 (全部 MC 扩展)

| Box3JS API | 说明 |
|------------|------|
| `world.placeStructure(x, y, z, structureId)` | 在指定位置放置数据包结构模板 (NBT) |
| `world.placeStructure(pos, structureId)` | GameVector3 重载 |
| `world.grantAdvancement(playerName, advancementId)` | 按玩家名称授予进度 |

### 1.29 数据库 (db 全局对象)

Box3 平台无独立数据库 API。Box3JS 通过 `db` 全局对象提供 SQLite 能力。

| Box3JS API | 说明 |
|------------|------|
| `db.sql(sql, ...params)` | 执行 SQL 查询/更新，返回 `GameQueryResult` |
| `db.sql\`SELECT * FROM t WHERE id = ${id}\`` | Tagged template 语法，自动参数化 |

**GameQueryResult**: `rows` (数组), `firstRow`, `columnNames`, `columnCount`, `rowCount`, `affectedRows`, `isQuery`
**GameQueryResult 方法**: `next()`, `reset()`, `then(resolve, reject?)`

每个脚本项目拥有独立的数据库文件 `config/box3/data/<project>.db`。

---

## 2. GameEntity (entity)

### 2.1 身份标识

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `entity.id` (只读属性) | `entity.id` (只读属性) | ✅ | 一致。返回 UUID 字符串 |
| `entity.isPlayer` (只读属性) | `entity.isPlayer()` (方法) | ⚠️ | Box3 为属性，Box3JS 为方法 |
| `entity.player` (只读属性) | `entity.player` (只读属性) | ✅ | 一致。非玩家返回 null |
| `entity.entityType` (只读属性) | `entity.entityType` (只读属性) | ✅ | 一致。返回命名空间 ID（如 "minecraft:zombie"） |

### 2.2 标签

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `entity.addTag(tag)` | `entity.addTag(tag)` | ✅ | 一致 |
| `entity.hasTag(tag)` | `entity.hasTag(tag)` | ✅ | 一致 |
| `entity.removeTag(tag)` | `entity.removeTag(tag)` | ✅ | 一致 |
| `entity.tags()` (返回数组) | `entity.tags()` | ✅ | 一致。返回所有标签的字符串数组 |

### 2.3 位置/速度/包围盒

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `entity.position` (属性) | `entity.position` (属性) | ✅ | 一致。LiveVec3 — 赋值即传送 |
| `entity.velocity` (属性) | `entity.velocity` (属性) | ✅ | 一致。赋值即设置速度 |
| `entity.bounds` (只读属性) | `entity.bounds` (只读属性) | ✅ | 一致。返回半尺寸 (half-extents) |
| — | `entity.eyePosition` (只读属性) | ⬆ | MC 扩展。返回眼睛位置的 GameVector3 |
| — | `entity.onGround` (只读属性) | ⬆ | MC 扩展。是否着地 |

### 2.4 外观

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `entity.mesh` | — | ❌ | Box3 自定义模型 |
| `entity.meshColor` | — | ❌ | |
| `entity.meshScale` | — | ❌ | |
| `entity.meshOrientation` | — | ❌ | |
| `entity.meshMetalness` | — | ❌ | |
| `entity.meshEmissive` | — | ❌ | |
| `entity.meshShininess` | — | ❌ | |
| `entity.anchor` | — | ❌ | |
| `entity.anchorOffset` | — | ❌ | |
| `entity.meshInvisible` (属性) | `entity.meshInvisible` (属性) | ✅ | 一致。底层设置 MC 隐身 |
| — | `entity.nameTag` (属性) | ⬆ | MC 扩展。获取/设置实体名牌 |
| — | `entity.glowing` (属性) | ⬆ | MC 扩展。获取/设置发光效果 |
| — | `entity.invulnerable` (属性) | ⬆ | MC 扩展。获取/设置无敌状态 |
| — | `entity.fire` (setFire/clearFire) | ⬆ | MC 扩展。设置/清除着火 |

### 2.5 物理属性

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `entity.collides` (属性) | `entity.collides` (属性) | ✅ | 一致。false 时对 LivingEntity 设置 noGravity |
| `entity.fixed` (属性) | `entity.fixed` (属性) | ✅ | 一致。true 时设置 noGravity 并锁定位置 |
| `entity.friction` (属性) | `entity.friction` (属性) | ✅ | 一致。存储为自定义属性（脚本自行利用） |
| `entity.gravity` (属性) | `entity.gravity` (属性) | ✅ | 一致。false 时设置 noGravity(true) |
| `entity.mass` (属性) | `entity.mass` (属性) | ✅ | 一致。存储为自定义属性，默认 1.0 |
| `entity.restitution` (属性) | `entity.restitution` (属性) | ✅ | 一致。存储为自定义属性，默认 0.0 |
| `entity.contactForce` | — | ❌ | |
| `entity.entityContacts` (只读) | — | ❌ | |
| `entity.voxelContacts` (只读) | — | ❌ | |
| `entity.fluidContacts` (只读) | — | ❌ | |
| `entity.useOBB` | — | ❌ | |
| `entity.ignoreEntityGravity` | — | ❌ | |

**注意**: MC 物理由原版引擎控制，物理属性（friction/mass/restitution）存储为自定义属性供脚本读取，不会改变原版物理行为。collides/fixed/gravity 有对应 MC 副作用（noGravity）。

### 2.6 粒子

Box3 实体可独立发射粒子，Box3JS 完全未实现实体级粒子：

| Box3 API | 状态 |
|----------|------|
| `entity.particleRate` | ❌ |
| `entity.particleRateSpread` | ❌ |
| `entity.particleLimit` | ❌ |
| `entity.particleLifetime` | ❌ |
| `entity.particleLifetimeSpread` | ❌ |
| `entity.particleSize` | ❌ |
| `entity.particleSizeSpread` | ❌ |
| `entity.particleColor` | ❌ |
| `entity.particleVelocity` | ❌ |
| `entity.particleVelocitySpread` | ❌ |
| `entity.particleDamping` | ❌ |
| `entity.particleAcceleration` | ❌ |
| `entity.particleNoise` | ❌ |
| `entity.particleNoiseFrequency` | ❌ |
| `entity.particleTarget` | ❌ |
| `entity.particleTargetOffset` | ❌ |

### 2.7 音效

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `entity.chatSound` | — | ❌ | |
| `entity.hurtSound` | — | ❌ | |
| `entity.dieSound` | — | ❌ | |
| `entity.interactSound` | — | ❌ | |
| `entity.sound(config)` | — | ❌ | |

### 2.8 交互

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `entity.enableInteract` | — | ❌ | |
| `entity.interactRadius` | — | ❌ | |
| `entity.interactHint` | — | ❌ | |
| `entity.interactColor` | — | ❌ | |
| `entity.say(message)` | — | ❌ | |

### 2.9 战斗/生命

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `entity.destroyed` (只读属性) | `entity.destroyed` (只读属性) | ✅ | 一致 |
| `entity.hp` (属性) | `entity.hp` (属性) | ✅ | 一致 |
| `entity.maxHp` (属性) | `entity.maxHp` (属性) | ✅ | 一致 |
| `entity.enableDamage` | — | ❌ | |
| `entity.showHealthBar` | — | ❌ | |
| `entity.hurt(amount)` | `entity.hurt(amount)` | ✅ | 一致 |
| — | `entity.heal(amount)` | ⬆ | MC 扩展。治疗实体 |
| `entity.destroy()` | `entity.destroy()` | ✅ | 一致。触发 onDestroy 回调后移除。Box3 中 destroy() 调用后实体立即消失 |

### 2.10 实体事件

Box3 的实体级事件非常丰富：

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `entity.onClick(fn)` | — | ❌ | |
| `entity.onInteract(fn)` | — | ❌ | 可用 `world.onInteract` 替代 |
| `entity.onEntityContact(fn)` | — | ❌ | 可用 `world.onEntityContact` 替代 |
| `entity.onEntitySeparate(fn)` | — | ❌ | 可用 `world.onEntitySeparate` 替代 |
| `entity.onFluidEnter(fn)` | — | ❌ | 可用 `world.onFluidEnter` 替代 |
| `entity.onFluidLeave(fn)` | — | ❌ | 可用 `world.onFluidLeave` 替代 |
| `entity.onVoxelContact(fn)` | — | ❌ | 可用 `world.onVoxelContact` 替代 |
| `entity.onVoxelSeparate(fn)` | — | ❌ | |
| `entity.onDestroy(fn)` | `entity.onDestroy` (可赋值属性) | ⚠️ | Box3 为 `onDestroy(handler)` 方法；Box3JS 为可赋值属性 `entity.onDestroy = fn` |
| `entity.onTakeDamage(fn)` | — | ❌ | 可用 `world.onEntityDamage` 替代 |
| `entity.onDie(fn)` | — | ❌ | 可用 `world.onEntityDeath` 替代 |

**注意**: 所有 `world.onXxx()` 事件回调注册后返回 `GameEventHandlerToken`，支持 `.cancel()` / `.active()`，与 Box3 一致。实体级事件 (`entity.onDestroy`) 暂不返回 token。

### 2.11 动画

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `entity.motion` (属性) | — | ❌ | Box3 的 GameMotionController |
| `entity.animate(keyframes, playback?)` | — | ❌ | |
| `entity.getAnimations()` | — | ❌ | |

### 2.12 变换方法

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `entity.lookAt(target)` | `entity.lookAt(x, y, z)` / `entity.lookAt(pos)` | ✅ | 一致。Box3 接受多种目标类型，Box3JS 接受坐标或 GameVector3 |
| `entity.rotateLocal(quat)` | — | ❌ | 局部旋转 |
| `entity.scaleLocal(vec)` | — | ❌ | 局部缩放 |

### 2.13 MC 独有扩展 (Entity)

| Box3JS API | 说明 |
|------------|------|
| `entity.navigateTo(x, y, z, speed)` / `entity.navigateTo(pos, speed)` | 生物寻路到目标位置 |
| `entity.setTarget(targetEntity)` | 设置生物攻击目标 |
| `entity.clearTarget()` | 清除攻击目标 |
| `entity.getTarget()` | 获取当前攻击目标 |
| `entity.setAI(enabled)` | 启用/禁用生物 AI |
| `entity.addEffect(effectId, duration, amplifier)` | 添加药水效果 |
| `entity.addEffect(effectId, duration, amplifier, hideParticles)` | 添加效果（可隐藏粒子） |
| `entity.setEquipment(slot, itemId)` | 设置生物装备（mainhand/offhand/head/chest/legs/feet） |
| `entity.setDropChance(slot, chance)` / `entity.setDropChance("all", chance)` | 设置装备掉落概率 |
| `entity.setPersistent(true)` | 使生物持久化（不自然消失） |
| `entity.getAttribute(attributeId)` | 获取属性值（如 "minecraft:generic.attack_damage"） |
| `entity.setAttribute(attributeId, value)` | 设置属性值 |
| `entity.lookAt(x, y, z)` / `entity.lookAt(pos)` | 使实体看向某位置 |

---

## 3. GamePlayerEntity (entity.player)

### 3.1 基础信息

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `player.name` (只读属性) | `player.name` (只读属性) | ✅ | 一致 |
| `player.userId` (只读属性) | `player.userId` (只读属性) | ✅ | Box3 返回用户数字 ID；Box3JS 返回 UUID 字符串 |
| `player.boxId` (只读属性) | — | ❌ | 已弃用的 Box ID |
| `player.userKey` (只读属性) | — | ❌ | 已弃用的用户密钥 |
| `player.avatar` (只读属性) | — | ❌ | 头像 URL |
| `player.movementBounds` (属性) | — | ❌ | |
| `player.url` (属性) | — | ❌ | |

### 3.2 社交

| Box3 API | Box3JS 实现 | 状态 |
|----------|-------------|------|
| `player.querySocial(type)` | — | ❌ |
| `player.querySocialStatistic()` | — | ❌ |
| `player.openUserProfileDialog(userId)` | — | ❌ |

### 3.3 外观

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `player.color` | — | ❌ | |
| `player.emissive` | — | ❌ | |
| `player.invisible` (属性) | `player.invisible` (属性) | ✅ | 一致 |
| `player.showName` | — | ❌ | |
| `player.showIndicator` | — | ❌ | |
| `player.scale` (属性) | `player.scale` (只读) | ⚠️ | Box3JS 只读 |
| `player.metalness` | — | ❌ | |
| `player.shininess` | — | ❌ | |
| `player.skin` | — | ❌ | |
| `player.skinInvisible` | — | ❌ | |
| `player.setSkinByName(name)` | — | ❌ | |
| `player.resetToDefaultSkin()` | — | ❌ | |
| `player.clearSkin()` | — | ❌ | |
| `player.addWearable(config)` | — | ❌ | |
| `player.removeWearable(config)` | — | ❌ | |
| `player.wearables(bodyPart?)` | — | ❌ | |

### 3.4 相机

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `player.cameraMode` (属性) | `player.cameraMode` (属性) | ⚠️ | Box3 支持 FIXED/FOLLOW/FPS/RELATIVE 四种模式；Box3JS 仅支持 FPS/FOLLOW |
| `player.cameraEntity` (属性) | `player.cameraEntity` (属性) | ⚠️ | Box3 可设为 null/实体；Box3JS 支持但设为非 null 时自动切换到 FOLLOW |
| `player.cameraPosition` (属性) | — | ❌ | FIXED/RELATIVE 模式用 |
| `player.cameraTarget` (属性) | `player.cameraTarget` (只读属性) | ⚠️ | Box3 可读写；Box3JS 只读，返回玩家视线方向 5 格处的点 |
| `player.cameraUp` (属性) | — | ❌ | |
| `player.cameraFovY` (属性) | — | ❌ | |
| `player.enable3DCursor` | — | ❌ | |
| `player.cameraFreezedAxis` | — | ❌ | |
| `player.freezedForwardDirection` | — | ❌ | |
| `player.cameraDistance` (属性) | — | ❌ | 相机到目标距离 |
| `player.cameraPitch` (只读属性) | `player.cameraPitch` (读/写属性) | ✅ | Box3JS 可写（`setCameraPitch` 方法效果） |
| `player.cameraYaw` (只读属性) | `player.cameraYaw` (读/写属性) | ✅ | Box3JS 可写 |
| `player.setCameraPitch(v)` | `player.setCameraPitch(v)` (作为属性 setter) | ✅ | 通过属性赋值实现 |
| `player.setCameraYaw(v)` | `player.setCameraYaw(v)` (作为属性 setter) | ✅ | 同上 |
| `player.facingDirection` (只读属性) | `player.facingDirection` (只读属性) | ✅ | 一致 |

### 3.5 画面滤镜

| Box3 API | Box3JS 实现 | 状态 |
|----------|-------------|------|
| `player.colorLUT` (属性) | — | ❌ |

### 3.6 聊天/消息

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `player.directMessage(message)` | `player.directMessage(message)` | ✅ | 一致 |
| `player.dialog(config)` | `player.dialog(config)` | ⚠️ | **大幅简化**。Box3 支持 TEXT/INPUT/SELECT 三种类型 + 丰富样式配置 + 异步返回；Box3JS 仅发送系统消息并返回 `{index: 0, value: "OK"}`，不支持真正的交互对话框 |
| `player.cancelDialogs()` | — | ❌ | |
| `player.share(content)` | — | ❌ | |
| `player.onChat(handler)` | `player.onChat(handler)` | ✅ | Box3 传入 `{entity, message, tick}` 事件对象；Box3JS 直接展开参数 |
| — | `player.actionBar(message)` | ⬆ | MC 扩展。ActionBar 消息 |
| — | `player.title(title, subtitle)` | ⬆ | MC 扩展。标题/副标题 |
| — | `player.title(title, subtitle, fadeIn, stay, fadeOut)` | ⬆ | MC 扩展。带时间的标题 |

### 3.7 战斗/生命

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `player.dead` (只读属性) | `player.dead` (只读属性) | ✅ | 一致。返回 `player.isDeadOrDying()` |
| `player.spawnPoint` (属性) | `player.spawnPoint` (读写属性) | ✅ | 一致。可读可写，写入委托 setRespawnPoint |
| `player.forceRespawn()` | `player.respawn()` | ⚠️ | 方法名不同（forceRespawn → respawn） |
| `player.onRespawn(handler)` | — | ❌ | 可用 `world.onPlayerRespawn` 替代 |
| `player.hp` (属性) | `player.hp` (属性) | ✅ | 一致 |
| `player.maxHp` (属性) | `player.maxHp` (属性) | ✅ | 一致 |

### 3.8 移动/输入

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `player.gamepad` | — | ❌ | 虚拟按键图片 |
| `player.disableInputDirection` | — | ❌ | |
| `player.enableAction0` | — | ❌ | |
| `player.enableAction1` | — | ❌ | |
| `player.action0Button` (只读) | — | ❌ | 可通过 `world.onButtonPressed` 检测 ACTION0 |
| `player.action1Button` (只读) | — | ❌ | 可通过 `world.onButtonPressed` 检测 ACTION1 |
| `player.jumpButton` (只读) | — | ❌ | 可通过 `world.onButtonPressed` 检测 JUMP |
| `player.walkButton` (只读) | — | ❌ | 可通过 `world.onButtonPressed` 检测 WALK |
| `player.swapInputDirection` | — | ❌ | |
| `player.reverseInputDirection` | — | ❌ | |
| `player.canFly` (属性) | `player.canFly` (属性) | ✅ | 一致 |
| — | `player.flying` (属性) | ⬆ | MC 扩展。当前是否在飞行 |
| `player.spectator` (只读属性) | `player.spectator` (只读属性) | ✅ | 一致 |
| — | `player.collision` (属性) | ⬆ | MC 扩展。实体碰撞开关 |
| `player.enableJump` (属性) | `player.enableJump` (属性) | ✅ | 一致。false 时保存并清零跳跃强度，true 时恢复 |
| `player.enableDoubleJump` | — | ❌ | 二段跳已移除 |
| `player.walkSpeed` (属性) | `player.walkSpeed` (属性) | ✅ | 一致 |
| `player.runSpeed` (属性) | `player.runSpeed` (属性) | ✅ | 一致 |
| `player.runAcceleration` | — | ❌ | |
| `player.jumpPower` (属性) | `player.jumpPower` (属性) | ✅ | 一致 |
| `player.jumpSpeedFactor` | — | ❌ | |
| `player.jumpAccelerationFactor` | — | ❌ | |
| `player.doubleJumpPower` | — | ❌ | |
| `player.crouchSpeed` (属性) | `player.crouchSpeed` (属性) | ✅ | 一致。存储为自定义属性 |
| `player.crouchAcceleration` | — | ❌ | |
| `player.flySpeed` (属性) | `player.flySpeed` (属性) | ✅ | 一致 |
| `player.flyAcceleration` | — | ❌ | |
| `player.swimAcceleration` | — | ❌ | |
| `player.swimSpeed` (属性) | `player.swimSpeed` (属性) | ✅ | 一致。映射到 WATER_MOVEMENT_EFFICIENCY 属性 |
| `player.walkAcceleration` | — | ❌ | |
| `player.moveState` (只读属性) | `player.moveState` (只读属性) | ✅ | 一致。枚举值: FLYING/GROUND/SWIM/FALL/JUMP |
| `player.walkState` (只读属性) | `player.walkState` (只读属性) | ✅ | 一致。枚举值: NONE/CROUCH/WALK/RUN |
| `player.cameraPitch` (只读属性) | `player.cameraPitch` (读/写属性) | ✅ | 如上 |
| `player.cameraYaw` (只读属性) | `player.cameraYaw` (读/写属性) | ✅ | 如上 |
| `player.kick()` | `player.kick()` / `player.kick(reason)` | ✅ | 一致。额外支持自定义踢出原因 |

### 3.9 输入事件

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `player.onPress(handler)` | — | ❌ | 可用 `world.onButtonPressed` 替代 |
| `player.onRelease(handler)` | — | ❌ | |
| `player.onKeyDown(handler)` | — | ❌ | 键盘事件，MC 服务端无法获取 |
| `player.onKeyUp(handler)` | — | ❌ | |

### 3.10 音效

Box3 玩家有 14 种音效属性，Box3JS 仅提供播放方法：

| Box3 API | Box3JS 实现 | 状态 |
|----------|-------------|------|
| `player.music` | — | ❌ |
| `player.action0Sound` | — | ❌ |
| `player.action1Sound` | — | ❌ |
| `player.crouchSound` | — | ❌ |
| `player.jumpSound` | — | ❌ |
| `player.doubleJumpSound` | — | ❌ |
| `player.landSound` | — | ❌ |
| `player.enterWaterSound` | — | ❌ |
| `player.leaveWaterSound` | — | ❌ |
| `player.swimSound` | — | ❌ |
| `player.spawnSound` | — | ❌ |
| `player.stepSound` | — | ❌ |
| `player.startFlySound` | — | ❌ |
| `player.stopFlySound` | — | ❌ |
| `player.sound(config)` | `player.playSound(path, volume, pitch)` | ⚠️ | Box3 接受完整 Sound 对象或路径；Box3JS 展开参数 |

### 3.11 链接/跳转

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `player.link(href, options?)` | `player.link(href)` | ⚠️ | Box3JS 简化版，无 isConfirm/isNewTab 选项。通过发送可点击的聊天组件实现 |
| `player.teleport(pos)` | `player.teleport(pos)` | ✅ | 一致 |

### 3.12 商城

| Box3 API | Box3JS 实现 | 状态 |
|----------|-------------|------|
| `player.openMarketplace(productIds)` | — | ❌ |
| `player.getMiaoShells()` | — | ❌ |

### 3.13 玩家动画

| Box3 API | Box3JS 实现 | 状态 |
|----------|-------------|------|
| `player.animate(keyframes, playback?)` | — | ❌ |
| `player.getAnimations()` | — | ❌ |

### 3.14 MC 独有扩展 (Player)

| Box3JS API | 说明 |
|------------|------|
| `player.opLevel` (属性) | 获取/设置玩家 OP 权限级别 |
| `player.gameMode` (属性) | 获取/设置游戏模式（survival/creative/adventure/spectator） |
| `player.dimension` (属性) | 获取/设置玩家所在维度（如 "minecraft:overworld"） |
| `player.disableFly` (属性) | 禁用飞行（退出飞行状态并阻止重新开启） |
| `player.giveItem(itemId, count)` | 给予物品 |
| `player.giveEnchantedItem(itemId, count, enchants)` | 给予附魔物品 |
| `player.giveNamedItem(itemId, count, name, lore)` | 给予自定义名称/描述物品 |
| `player.getHeldItem()` | 获取手持物品 `{id, count}` |
| `player.clearInventory()` | 清空背包 |
| `player.addEffect(effectId, duration, amplifier)` | 添加药水效果 |
| `player.addEffect(effectId, duration, amplifier, hideParticles)` | 添加效果（可隐藏粒子） |
| `player.clearEffects()` | 清除所有效果 |
| `player.xp` (属性) | 获取/设置经验等级 |
| `player.addExperienceLevels(levels)` | 增加经验等级 |
| `player.food` (属性) | 获取/设置饥饿值 |
| `player.saturation` (属性) | 获取/设置饱和值 |
| `player.runCommand(cmd)` | 以玩家身份执行命令 |
| `player.lookAt(x, y, z)` / `player.lookAt(pos)` | 使玩家看向某位置 |
| `player.setPlayerListName(name)` | 设置 TAB 列表显示名称 |

---

## 4. GameVoxels (voxels)

Box3JS 的 Voxels 实现是所有 API 中**最完整**的。

### 4.1 属性

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `voxels.shape` (只读属性) | `voxels.shape` (只读属性) | ✅ | 一致。返回世界最大尺寸 |
| `voxels.VoxelTypes` (只读属性) | `voxels.VoxelTypes` (只读属性) | ✅ | 一致。所有可用方块名称数组 |

### 4.2 名称/ID 映射

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `voxels.id(name)` | `voxels.id(name)` | ✅ | 一致。名称→数字 ID |
| `voxels.name(id)` | `voxels.name(id)` | ✅ | 一致。数字 ID→名称 |

### 4.3 写入

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `voxels.setVoxel(x, y, z, voxel)` | `voxels.setVoxel(x, y, z, voxel)` | ✅ | 一致 |
| `voxels.setVoxel(x, y, z, voxel, rotation)` | `voxels.setVoxel(x, y, z, voxel, rotation)` | ✅ | 一致。rotation 0-3 |
| `voxels.setVoxel(pos, voxel)` | `voxels.setVoxel(pos, voxel)` | ✅ | GameVector3 重载 |
| `voxels.setVoxel(pos, voxel, rotation)` | `voxels.setVoxel(pos, voxel, rotation)` | ✅ | GameVector3 重载 |
| `voxels.setVoxelId(x, y, z, voxel)` | `voxels.setVoxelId(x, y, z, voxel)` | ✅ | 一致。ID 包含旋转编码 |
| `voxels.setVoxelId(pos, voxel)` | `voxels.setVoxelId(pos, voxel)` | ✅ | GameVector3 重载 |

**旋转编码方案一致**: `finalId = rotation * 16384 + baseId`，rotation 0=南(0°), 1=西(90°), 2=北(180°), 3=东(270°)。

### 4.4 读取

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `voxels.getVoxel(x, y, z)` | `voxels.getVoxel(x, y, z)` | ✅ | 一致。返回基础 ID（无旋转） |
| `voxels.getVoxel(pos)` | `voxels.getVoxel(pos)` | ✅ | GameVector3 重载 |
| `voxels.getVoxelId(x, y, z)` | `voxels.getVoxelId(x, y, z)` | ✅ | 一致。返回含旋转的完整 ID |
| `voxels.getVoxelId(pos)` | `voxels.getVoxelId(pos)` | ✅ | GameVector3 重载 |
| `voxels.getVoxelRotation(x, y, z)` | `voxels.getVoxelRotation(x, y, z)` | ✅ | 一致 |
| `voxels.getVoxelRotation(pos)` | `voxels.getVoxelRotation(pos)` | ✅ | GameVector3 重载 |

### 4.5 MC 独有扩展 (Voxels)

| Box3JS API | 说明 |
|------------|------|
| `voxels.getVoxelName(x, y, z)` / `voxels.getVoxelName(pos)` | 返回方块的命名空间 ID（如 "minecraft:stone"） |
| `voxels.fillVoxel(x1, y1, z1, x2, y2, z2, voxel)` / `voxels.fillVoxel(pos1, pos2, voxel)` | 批量填充方块区域 |
| `voxels.countVoxel(x1, y1, z1, x2, y2, z2, voxel)` / `voxels.countVoxel(pos1, pos2, voxel)` | 统计区域内匹配方块数量 |
| `voxels.setSpawner(x, y, z, entityType)` / `voxels.setSpawner(pos, entityType)` | 设置刷怪笼类型 |

---

## 5. GameDataStorage (storage)

### 5.1 存储空间管理

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `storage.key` (只读属性) | `storage.key` (只读属性) | ✅ | 一致。但 Box3JS 返回空字符串（根 storage） |
| `storage.getDataStorage(key)` | `storage.getDataStorage(key)` | ✅ | 一致 |
| `storage.getGroupStorage(key)` | `storage.getGroupStorage(key)` | ✅ | 一致 |

### 5.2 数据操作

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `ds.set(key, value)` | `ds.set(key, value)` | ⚠️ | **Box3 异步返回 Promise，Box3JS 同步执行** |
| `ds.get(key)` | `ds.get(key)` | ⚠️ | **Box3 异步返回 `Promise<ReturnValue>`，Box3JS 同步返回 value 或 null** |
| `ds.update(key, handler)` | `ds.update(key, handler)` | ⚠️ | **Box3 异步，Box3JS 同步** |
| `ds.remove(key)` | `ds.remove(key)` | ⚠️ | **Box3 异步，Box3JS 同步** |
| `ds.increment(key, value?)` | `ds.increment(key)` / `ds.increment(key, value)` | ⚠️ | **Box3 异步，Box3JS 同步** |
| `ds.list(options)` | `ds.list(options)` | ⚠️ | **Box3 异步，Box3JS 同步**。Box3JS 无 cursor 分页语义（cursor 仅作偏移量），不支持 constraintTarget 深层排序 |
| `ds.destroy()` | `ds.destroy()` | ⚠️ | **Box3 异步，Box3JS 同步** |
| — | `ds.keys()` | ⬆ | MC 扩展。返回所有 key 数组 |
| — | `ds.getKey()` | ⬆ | MC 扩展。返回存储空间名称 |

### 5.3 关键差异

1. **同步 vs 异步**: Box3 的 DataStorage 是异步 API（基于网络），Box3JS 是同步的本地文件存储。在 JS 中调用方式不同：Box3 需要 `await ds.get("key")`，Box3JS 直接 `ds.get("key")`。
2. **返回值**: Box3 的 `get` 返回 `ReturnValue {key, value, updateTime, createTime, version}`，Box3JS 的 `get` 直接返回 value。
3. **存储位置**: Box3 使用云端数据库，Box3JS 使用本地 JSON 文件（`config/box3/storage/`）。
4. **速率限制**: Box3 有严格的读写速率限制，Box3JS 无限制。
5. **错误处理**: Box3 有详细的错误码（400/429/500），Box3JS 静默失败。

---

## 6. Math 类型

### 6.1 GameVector3

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| 构造函数 `(x, y, z)` | ✅ | ✅ | |
| `x, y, z` 属性 | ✅ | ✅ | |
| `set(x, y, z)` | ✅ | ✅ | |
| `copy(v)` | ✅ | ✅ | |
| `clone()` | ✅ | ✅ | |
| `add(v)` | ✅ | ✅ | |
| `sub(v)` | ✅ | ✅ | |
| `mul(v)` | ✅ | ✅ | 逐分量乘法，零保护 |
| `div(v)` | ✅ | ✅ | 逐分量除法，零保护 |
| `addEq(v)` | ✅ | ✅ | 返回 this |
| `subEq(v)` | ✅ | ✅ | 返回 this |
| `mulEq(v)` | ✅ | ✅ | 返回 this |
| `divEq(v)` | ✅ | ✅ | 返回 this，零保护 |
| `dot(v)` | ✅ | ✅ | |
| `cross(v)` | ✅ | ✅ | 叉积 |
| `scale(n)` | ✅ | ✅ | |
| `lerp(v, n)` | ✅ | ✅ | |
| `towards(v)` | ✅ | ✅ | 返回归一化方向向量 |
| `mag()` | ✅ | ✅ | |
| `sqrMag()` | ✅ | ✅ | |
| `angle(v)` | ✅ | ✅ | 返回弧度 |
| `distance(v)` | ✅ | ✅ | |
| `equals(v)` | ✅ | ✅ | 使用 1e-6 容差，匹配 Box3 |
| `exactEquals(v)` | ✅ | ✅ | 精确比较 |
| `max(v)` | ✅ | ✅ | 逐分量取最大值 |
| `min(v)` | ✅ | ✅ | 逐分量取最小值 |
| `normalize()` | ✅ | ✅ | |
| `toString()` | ✅ | ✅ | |
| `fromPolar(mag, phi, theta)` (静态) | ✅ | ✅ | |

**GameVector3 完全实现**，所有 28 个 Box3 方法均已对齐。

### 6.2 GameBounds3

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| 构造函数 `(lo, hi)` | ✅ | ✅ | |
| `lo, hi` 属性 | ✅ | ✅ | |
| `set(lox, loy, loz, hix, hiy, hiz)` | ✅ | ✅ | |
| `copy(b)` | ✅ | ✅ | |
| `intersect(b)` | ✅ | ✅ | 返回新对象或 null（无交集时） |
| `intersects(b)` | ✅ | ✅ | |
| `contains(v)` | ✅ | ✅ | |
| `containsBounds(b)` | ✅ | ✅ | |
| `toString()` | ✅ | ✅ | |
| `fromPoints(...points)` (静态) | ✅ | ✅ | 接受 NativeArray |

**GameBounds3 完全实现**。

### 6.3 GameRGBColor

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| 构造函数 `(r, g, b)` | ✅ | ✅ | |
| `r, g, b` 属性 | ✅ | ✅ | |
| `set(r, g, b)` | ✅ | ✅ | |
| `copy(c)` | ✅ | ✅ | |
| `clone()` | ✅ | ✅ | |
| `add/sub/mul/div` | ✅ | ✅ | 除法有零保护 |
| `addEq/subEq/mulEq/divEq` | ✅ | ✅ | 返回 this，除法有零保护 |
| `lerp(rgb, n)` | ✅ | ✅ | |
| `equals(rgb)` | ✅ | ✅ | 使用 1e-6 容差 |
| `toRGBA()` | ✅ | ✅ | 返回 `"rgba(r,g,b,1.0)"` 字符串 |
| `toString()` | ✅ | ✅ | |
| `random()` (静态) | ✅ | ✅ | |

**GameRGBColor 完全实现**，所有 16 个 Box3 方法均已对齐。

### 6.4 GameRGBAColor

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| 构造函数 `(r, g, b, a)` | ✅ | ✅ | |
| `r, g, b, a` 属性 | ✅ | ✅ | |
| `set(r, g, b, a)` | ✅ | ✅ | |
| `copy(c)` | ✅ | ✅ | |
| `clone()` | ✅ | ✅ | |
| `add/sub/mul/div` | ✅ | ✅ | |
| `addEq/subEq/mulEq/divEq` | ✅ | ✅ | |
| `lerp(rgba, n)` | ✅ | ✅ | |
| `equals(rgba)` | ✅ | ✅ | |
| `blendEq(rgb)` | ✅ | ✅ | |
| `toString()` | ✅ | ✅ | |

**GameRGBAColor 是完全实现的**，所有 Box3 方法均有对应。

### 6.5 GameQuaternion

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| 构造函数 `(w, x, y, z)` | ✅ | ✅ | |
| `w, x, y, z` 属性 | ✅ | ✅ | |
| `set(w, x, y, z)` | ✅ | ✅ | |
| `copy(v)` | ✅ | ✅ | |
| `clone()` | ✅ | ✅ | |
| `rotateX/Y/Z(rad)` | ✅ | ✅ | |
| `add/sub` | ✅ | ✅ | |
| `mul(q)` | ✅ | ✅ | Hamilton 积 |
| `inv()` | ✅ | ✅ | |
| `div(q)` | ✅ | ✅ | |
| `dot(q)` | ✅ | ✅ | |
| `slerp(q, n)` | ✅ | ✅ | |
| `angle(q)` | ✅ | ✅ | |
| `getAxisAngle()` | ✅ | ✅ | 返回 `{angle, axis}` |
| `mag()` | ✅ | ✅ | |
| `sqrMag()` | ✅ | ✅ | |
| `equals(v)` | ✅ | ✅ | |
| `normalize()` | ✅ | ✅ | |
| `toString()` | ✅ | ✅ | |
| `fromAxisAngle(axis, rad)` (静态) | ✅ | ✅ | |
| `fromEuler(x, y, z)` (静态) | ✅ | ✅ | YZX 顺序 |
| `rotationBetween(a, b)` (静态) | ✅ | ✅ | |

**GameQuaternion 是完全实现的**。

---

## 7. 其他服务端 API

### 7.1 GameAnimation

**状态**: ❌ 完全未实现

Box3 有完整的关键帧动画系统，支持 World/Entity/Player 级别的属性动画（位置、颜色、缩放、天气参数等）。Box3JS 无动画系统。

| Box3 API | 状态 |
|----------|------|
| `world.animate(keyframes, playback?)` | ❌ |
| `entity.animate(keyframes, playback?)` | ❌ |
| `player.animate(keyframes, playback?)` | ❌ |
| `GameAnimation` 对象 (play/cancel/currentTime/playState/onFinish/...) | ❌ |

### 7.2 GameMotionController

**状态**: ❌ 完全未实现

Box3 的 Voxa 模型动画系统，用于控制自定义模型的骨骼动画。

| Box3 API | 状态 |
|----------|------|
| `entity.motion` | ❌ |
| `motion.loadByName(configs)` | ❌ |
| `motion.pause()` / `motion.resume()` | ❌ |
| `motion.setDefaultMotionByName(name)` | ❌ |
| `GameMotionHandler` (play/cancel/onFinish) | ❌ |

### 7.3 Sound

**状态**: ❌ 未实现独立 Sound 对象

Box3 的 `sound()` 方法返回 Sound 对象，支持 `setCurrentTime`/`resume`/`pause`/`stop`。Box3JS 的 `playSound` 是 fire-and-forget 模式，无法控制播放中的声音。

### 7.4 RemoteChannel (跨端通信)

**状态**: ❌ 完全未实现

Box3 的 `remoteChannel` 用于服务端↔客户端双向通信。MC 模组运行在服务端，无客户端代码，无法实现。

| Box3 API | 状态 |
|----------|------|
| `remoteChannel.sendClientEvent(entities, event)` (服务端) | ❌ |
| `remoteChannel.broadcastClientEvent(event)` (服务端) | ❌ |
| `remoteChannel.onServerEvent(handler)` (服务端) | ❌ |
| `remoteChannel.sendServerEvent(event)` (客户端) | ❌ |
| `remoteChannel.onClientEvent(handler)` (客户端) | ❌ |

**替代方案**: Box3JS 提供了 `world.sendMessage(target, data)` 和 `world.onMessage(fn)` 用于**脚本间**通信（同一服务端不同脚本项目），但这与 Box3 的跨端通信不同。

### 7.5 GameRTC (实时语音)

**状态**: ❌ 完全未实现

MC 无内置语音通信，无法实现。

### 7.6 GameHttpAPI (http)

**状态**: ✅ 已实现（同步调用）

Box3 的 `http.fetch(url, options?)` 用于服务端发起 HTTP 请求。

| Box3 API | Box3JS 实现 | 状态 | 差异说明 |
|----------|-------------|------|---------|
| `http.fetch(url, options?)` → `Promise<Response>` | `http.fetch(url, options?)` → `Response` | ⚠️ | Box3 异步返回 Promise；Box3JS 同步阻塞调用 |
| `options.method` | ✅ | ✅ | GET/POST/PUT/DELETE/PATCH/HEAD/OPTIONS |
| `options.headers` | ✅ | ✅ | 键值对 |
| `options.body` (string) | ✅ | ✅ | 文本请求体 |
| `options.body` (ArrayBuffer) | ✅ | ✅ | 二进制请求体 |
| `options.timeout` | ✅ | ✅ | 超时毫秒 |
| — | `options.responseType` | ⬆ | 自动解析：`"json"` / `"text"` / `"arrayBuffer"`，结果见 `resp.data` |
| — | `options.maxBodySize` | ⬆ | 响应体最大字节数，超出截断并标记 `resp.truncated` |
| `Response.ok` | ✅ | ✅ | 状态码 200-299 |
| `Response.status` | ✅ | ✅ | HTTP 状态码 |
| `Response.statusText` | ✅ | ✅ | 状态描述 |
| `Response.headers` | ✅ | ✅ | 响应头键值对 |
| `Response.json()` → `Promise<any>` | `Response.json()` → `any` | ⚠️ | Box3 异步；Box3JS 同步返回，解析失败返回 `null` |
| `Response.text()` → `Promise<string>` | `Response.text()` → `string` | ⚠️ | Box3 异步；Box3JS 同步返回 |
| `Response.arrayBuffer()` → `Promise<ArrayBuffer>` | `Response.arrayBuffer()` → `ArrayBuffer` | ⚠️ | Box3 异步；Box3JS 同步返回 |
| — | `Response.getHeader(name)` | ⬆ | 获取单个响应头值 |
| — | `Response.errorMessage` | ⬆ | 请求失败时的错误信息 |
| — | `Response.truncated` | ⬆ | 响应体是否因 maxBodySize 被截断 |
| — | `Response.data` | ⬆ | responseType 自动解析的结果 |
| `Response.close()` | ✅ | ✅ | 关闭连接（Box3JS 为空操作） |

> **⚠️ 重要差异：** Box3JS 的 `http.fetch()` 是**同步阻塞**调用（Rhino 引擎限制），会阻塞服务器 tick。Box3 原版是异步 Promise。请避免在高频回调（`world.onTick()` 等）中使用。

### 7.7 GameAnalytics (analytics)

**状态**: ❌ 未实现

Box3 的神策数据分析埋点。

### 7.8 GameAssetListEntry (resources)

**状态**: ❌ 未实现

Box3 的 `resources.ls(type?)` 浏览资源文件。MC 无对应资源管理 API。

### 7.9 GameEventHandlerToken

**状态**: ✅ 已实现

Box3 的事件注册方法返回 `GameEventHandlerToken`，可调用 `.cancel()` / `.active()`。Box3JS 的所有 `world.onXxx()` 方法均返回 `GameEventHandlerToken`，支持 `.cancel()` 取消注册和 `.active()` 检查状态。`.resume()` 抛出 UnsupportedOperationException（需重新注册）。

---

## 8. 客户端 API（不适用）

以下 Box3 API 全部运行在**客户端**（玩家浏览器），Box3JS 作为纯服务端模组**完全无法实现**：

| 类别 | 全局对象 | 说明 |
|------|---------|------|
| ClientUI | `ui`, `input`, `screenWidth`, `screenHeight` | 2D UI 系统（盒子、文本、图片、输入框、滚动框等） |
| ClientAudio | `Audio` | 客户端音频播放 |
| ClientMedia | `media` | 录音/播放 |
| ClientNavigator | `navigator` | 设备信息、语言 |
| ClientScreen | `screen` | 屏幕尺寸事件 |
| ClientHttp | `http` (客户端) | 客户端 HTTP 请求 |
| ClientWorld | `world` (客户端) | 3D 渲染开关 |

---

## 9. Box3JS 独有 MC 扩展

这些 API 是 Box3JS 利用 Minecraft 原生能力提供的，在 Box3 平台**不存在**：

### 9.1 世界管理
- `world.thunderDensity` — 雷暴强度
- `world.clearWeather()` — 清除天气
- `world.getGameRule/setGameRule` — 游戏规则
- `world.runCommand(cmd)` — 控制台命令
- `world.onVoxelDestroy` — 方块破坏事件
- `world.onBlockPlace` — 方块放置事件
- `world.onBlockActivate` — 方块右键事件
- `world.onBlockActivateBegin/onBlockActivateEnd` — 方块右键开始/结束（长按检测）
- `world.entitiesInArea/entitiesInRadius` — 空间实体查询
- `world.getBiome` — 生物群系查询
- `world.spawnParticleCircle` — 圆形粒子
- `world.spawnParticle/spawnFirework` GameRGBColor 重载 — RGB 彩色粒子/烟花
- `world.listRecipes/removeRecipe/clearRecipes` — 合成管理
- `world.placeStructure` — 结构放置
- `world.grantAdvancement` — 成就授予
- `world.onButtonPressed` — 按钮点击事件（石质/木质按钮）

### 9.2 实体管理
- `entity.nameTag` — 名牌
- `entity.glowing` — 发光
- `entity.invulnerable` — 无敌
- `entity.onGround` — 着地状态
- `entity.eyePosition` — 眼睛位置
- `entity.navigateTo` — 寻路
- `entity.setTarget/clearTarget/getTarget` — 战斗目标
- `entity.setAI` — AI 开关
- `entity.heal` — 治疗
- `entity.addEffect` — 药水效果
- `entity.setEquipment/setDropChance` — 装备管理
- `entity.setPersistent` — 持久化
- `entity.getAttribute/setAttribute` — 属性修改
- `entity.lookAt` — 视线方向
- `entity.setGlowColor(color)` — RGB 发光颜色（GameRGBColor → 最近 ChatFormatting）
- `entity.setText/setTextColor/setTextBackgroundColor` — 文本显示实体控制

### 9.3 玩家管理
- `player.opLevel` — OP 权限
- `player.gameMode` — 游戏模式
- `player.dimension` — 维度切换
- `player.flying` — 飞行状态
- `player.collision` — 碰撞开关
- `player.disableFly` — 禁用飞行
- `player.actionBar` — ActionBar 消息
- `player.title` — 标题/副标题
- `player.giveItem/giveEnchantedItem/giveNamedItem` — 物品给予
- `player.getHeldItem` — 手持物品
- `player.clearInventory` — 清空背包
- `player.addEffect/clearEffects` — 药水效果
- `player.xp/addExperienceLevels` — 经验管理
- `player.food/saturation` — 饥饿管理
- `player.runCommand` — 以玩家身份执行命令
- `player.lookAt` — 视线方向
- `player.setPlayerListName` — TAB 列表名称
- `player.directMessage(msg, color)` — 发送带颜色的系统消息（GameRGBColor 指定颜色）
- `player.grantAdvancement/revokeAdvancement` — 成就授予/撤销

### 9.4 系统
- `world.addScoreboard/removeScoreboard/setScore/getScore/showScoreboard/hideScoreboard/listScores` — 记分板
- `world.showBossbar/removeBossbar` — Boss 血条
- `world.createTeam/removeTeam/joinTeam/leaveTeam/getTeamOf` — 队伍管理
- `world.borderSize/setBorderCenter/shrinkBorder/setBorderDamage/setBorderWarning` — 世界边界
- `world.sendMessage/onMessage` — 跨脚本通信
- `voxels.fillVoxel/countVoxel/getVoxelName/setSpawner` — 方块批量操作

### 9.5 额外事件
- `world.onEntityDamage` — 实体受伤（Pre 阶段）
- `world.onMessage` — 跨脚本消息
- `world.onButtonPressed` — 按钮点击事件（支持石质/木质按钮长按检测）

### 9.6 数据库
- `db.sql` — SQLite 数据库操作（支持 tagged template 和参数化查询）
- `GameQueryResult` — 查询结果（rows, firstRow, columnNames, rowCount, affectedRows, isQuery）
- 每个项目独立数据库文件 `config/box3/data/<project>.db`

### 9.7 HTTP 请求
- `http.fetch(url, options?)` — 同步 HTTP 请求，支持 GET/POST/PUT/DELETE/PATCH/HEAD/OPTIONS
- `options.responseType` — 自动解析响应体（`"json"` / `"text"` / `"arrayBuffer"`），结果见 `resp.data`
- `options.maxBodySize` — 响应体大小限制，超出截断并标记 `resp.truncated`
- `Response.getHeader(name)` — 获取单个响应头值
- `Response.errorMessage` — 请求失败时的错误信息

---

## 10. 总结

### 10.1 实现统计

| 类别 | Box3 API 总数 | 已实现 | 部分实现 | 未实现 | MC 独有扩展 |
|------|--------------|--------|---------|--------|-------------|
| GameWorld | ~80 | ~30 | ~6 | ~44 | 33 |
| GameEntity | ~65 | ~21 | ~1 | ~43 | 21 |
| GamePlayerEntity | ~72 | ~27 | ~4 | ~41 | 22 |
| GameVoxels | 14 | 14 | 0 | 0 | 4 |
| GameDataStorage | 8 | 8 | 7 (同步化) | 0 | 2 |
| Math 类型 | ~100 | ~100 | 0 | 0 | 0 |
| 数据库 (db) | N/A | — | — | — | 1 |
| 其他服务端 | ~30 | 0 | 0 | ~30 | 0 |
| **总计** | **~369** | **~200** | **~18** | **~158** | **~83** |

> **2026-05 更新**: 本阶段实现约 54 个新 Box3 API（属性对齐 + math 补全 + 物理属性 + token + 回调签名 + World API 补全），Math 类型现已完全对齐。

### 10.2 核心差异模式

1. **事件回调签名**: Box3 传事件对象 → Box3JS 传展开参数；已为 onTick/onPlayerJoin/Leave/Respawn 添加 tick 参数
2. **异步存储**: Box3 的 Promise 存储 → Box3JS 的同步本地文件存储
3. **视觉/渲染 API**: Box3 有独立渲染引擎 → Box3JS 依赖 MC 原版渲染，无法控制雾/光照/雪花/粒子系统参数
4. **物理 API**: 基本物理属性（collides/fixed/gravity/friction/mass/restitution）已对齐，高级物理（接触力/OBB/碰撞过滤）不支持
5. **客户端 API**: Box3 有完整 UI/音频/媒体客户端 API → Box3JS 纯服务端，全部不可用
6. **动画系统**: Box3 有关键帧动画 → Box3JS 无
7. **事件令牌**: 已实现 GameEventHandlerToken，所有 world.onXxx() 返回 token，支持 cancel()/active()

### 10.3 Box3JS 的独特优势

Box3JS 虽然缺失大量 Box3 视觉/渲染/客户端 API，但提供了 Box3 平台**完全不具备**的 MC 原生能力：

- **完整的原版方块系统** — 数百种方块类型、红石、容器、刷怪笼
- **生物 AI/寻路/战斗** — 全套 MC 生物行为控制
- **原版物品/装备/附魔** — 完整的物品系统
- **药水效果/属性修改** — 精细的属性控制
- **计分板/Bossbar/队伍** — MC 原生的 UI 系统
- **世界边界** — 缩圈玩法
- **维度切换** — 下界/末地传送
- **游戏模式/OP 权限** — 权限管理
- **跨脚本通信** — 模块化脚本协作

### 10.4 迁移建议

从 Box3 平台迁移脚本到 Box3JS 时：

1. **视觉相关代码需重写** — 雾/光照/天气效果无法直接迁移，需使用 MC 原版机制代替
2. **回调参数需调整** — 将事件对象访问改为参数列表访问
3. **异步存储改为同步** — 移除 `await`，直接调用存储方法
4. **实体外观/物理不可用** — mesh/粒子/物理属性需删除或用 MC 代替
5. **客户端代码全废弃** — UI/音频/媒体代码无对应
6. **可充分利用 MC 扩展** — 记分板/Bossbar/生物 AI/物品系统/药水效果等是 Box3 没有的强大功能
