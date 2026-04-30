# Box3 API → MC 映射文档

## 概览

本文档记录了 Box3 平台 JS API 与 Minecraft (NeoForge 1.21.1) 之间的映射关系和实现状态。

- **✅ Box3 API** — Box3 原有 API，已接入 MC
- **⬆ MC 扩展** — 非 Box3 原有，利用 MC 特性新增

### 运行时

| 项目 | 说明 |
|---|---|
| 引擎 | Mozilla Rhino 1.7.14 |
| 作用域 | 服务端脚本（S- 脚本） |
| Tick | `ServerTickEvent.Post` |

### console (⬆ MC 扩展)

| API | 说明 |
|---|---|
| `console.log(...args)` | 标准日志输出 `[Box3JS] [项目名] msg` |
| `console.debug(...args)` | 调试日志 `[Box3JS][DEBUG] [项目名] msg` |
| `console.warn(...args)` | 警告日志 `[Box3JS][WARN] [项目名] msg` |
| `console.error(...args)` | 错误日志（stderr）`[Box3JS][ERROR] [项目名] msg` |
| `console.assert(assertion, ...args)` | 断言失败时调用 `error()` |
| `console.clear()` | 清空控制台 |

### sleep (⬆ MC 扩展)

| API | 说明 |
|---|---|
| `sleep(ms)` | 阻塞当前线程指定毫秒数 |

---

## world (GameWorld)

### 世界属性

| API | 类型 | MC 映射 | 说明 |
|---|---|---|---|
| `world.projectName()` | ✅ | `MinecraftServer.getMotd()` | |
| `world.currentTick()` | ✅ | `MinecraftServer.getTickCount()` | |
| `world.rainDensity` | ✅ | `getRainLevel()` / `setRaining()` | get/set，0.0–1.0 |
| `world.thunderDensity` | ⬆ | `getThunderLevel()` / `setThundering()` | get/set，0.0–1.0 |
| `world.clearWeather()` | ⬆ | `setRaining(false)` + `setThundering(false)` | 清除所有天气 |

### 时间

| API | 类型 | MC 映射 | 说明 |
|---|---|---|---|
| `world.getTime()` | ✅ | `ServerLevel.getDayTime()` | |
| `world.setTime(tick)` | ✅ | `ServerLevel.setDayTime(tick)` | |
| `world.timeScale` | ✅ | `GameRules.RULE_DAYLIGHT` | get/set，0=停止 1=正常 |

### 难度 / 出生点

| API | 类型 | MC 映射 | 说明 |
|---|---|---|---|
| `world.difficulty` | ✅ | `getDifficulty()` / `setDifficulty()` | get 返回名称字符串；set 接受字符串或 0-3 |
| `world.spawnPoint` | ⬆ | `ServerLevel.getSharedSpawnPos()` | 只读 GameVector3 |
| `world.setWorldSpawn(pos)` | ⬆ | `ServerLevel.setDefaultSpawnPos()` | |

### 实体生成

| API | 类型 | MC 映射 | 说明 |
|---|---|---|---|
| `world.spawnEntity(type, pos)` | ✅ | `EntityType.create()` + `addFreshEntity()` | type 为命名空间 ID 字符串，返回 Box3JSEntity |

### 事件

| API | 类型 | 回调参数 |
|---|---|---|
| `world.onTick(handler)` | ✅ | `()` |
| `world.onPlayerJoin(handler)` | ✅ | `(entity)` |
| `world.onPlayerLeave(handler)` | ✅ | `(entity)` |
| `world.onVoxelDestroy(handler)` | ✅ | `(entity, x, y, z, voxel, tick)` |
| `world.onVoxelContact(handler)` | ✅ | `(entity, voxel, x, y, z, axis, force, tick)` |
| `world.onInteract(handler)` | ✅ | `(entity, target, tick)` |
| `world.onChat(handler)` | ✅ | `(entity, message, tick)` |
| `world.onFluidEnter(handler)` | ✅ | `(entity, fluid, x, y, z, tick)` |
| `world.onFluidLeave(handler)` | ✅ | `(entity, fluid, x, y, z, tick)` |
| `world.onEntityContact(handler)` | ✅ | `(entity, other, tick)` |
| `world.onEntitySeparate(handler)` | ✅ | `(entity, other, tick)` |
| `world.onBlockPlace(handler)` | ⬆ | `(entity, x, y, z, voxel, voxelId, tick)` |
| `world.onEntityDeath(handler)` | ⬆ | `(entity, killer, tick)` |
| `world.onPlayerRespawn(handler)` | ⬆ | `(entity)` |
| `world.onBlockActivate(handler)` | ⬆ | `(entity, x, y, z, voxel, tick)` |
| `world.onEntityDamage(handler)` | ⬆ | `(entity, amount, source, attacker, tick)` |

### 查询 / 聊天

| API | 类型 | 说明 |
|---|---|---|
| `world.querySelector(selector)` | ✅ | `"*"` `"#uuid"` `".tag"` |
| `world.querySelectorAll(selector)` | ✅ | 同上，返回数组 |
| `world.say(message)` | ✅ | 全服广播 |

### 计时器（⬆ MC 扩展）

| API | 类型 | 说明 |
|---|---|---|
| `world.setTimeout(handler, ticks)` | ⬆ | 延迟执行，返回 timer ID |
| `world.setInterval(handler, ticks)` | ⬆ | 重复执行，返回 timer ID |
| `world.clearTimeout(id)` | ⬆ | 取消 timeout |
| `world.clearInterval(id)` | ⬆ | 取消 interval |

### 项目间消息传递（⬆ MC 扩展）

| API | 类型 | 说明 |
|---|---|---|
| `world.message.send(target, data)` | ⬆ | 精确路由到目标项目；`"*"` 广播给所有其他项目 |
| `world.message.on(handler)` | ⬆ | 接收其他项目发来的消息；`handler(from, data)` |

### 命令 / 查询（⬆ MC 扩展）

| API | 类型 | 说明 |
|---|---|---|
| `world.runCommand(cmd)` | ⬆ | 以服务器身份执行命令 |
| `world.query.raycast(origin, dir)` | ⬆ | 射线检测，默认 5 格 |
| `world.query.raycast(origin, dir, dist)` | ⬆ | 射线检测，自定义距离 |
| `world.query.entitiesInArea(pos1, pos2)` | ⬆ | 返回 AABB 区域内所有实体 |
| `world.query.biome(x, y, z)` | ⬆ | 获取生物群系命名空间 ID |
| `world.effect.explode(x, y, z, power)` | ⬆ | 创建爆炸 |
| `world.effect.explode(x, y, z, power, fire)` | ⬆ | 创建爆炸（可引火） |
| `world.sound.playAll(path, x, y, z, vol, pitch)` | ⬆ | 在坐标播放音效给所有玩家 |
---

## entity (GameEntity)

| API | 类型 | 说明 |
|---|---|---|
| `.id` | ✅ | UUID 字符串，只读 |
| `.isPlayer()` | ✅ | |
| `.entityType` | ✅ | 命名空间 ID，只读 |
| `.position` | ✅ | LiveVec3；`.set(x,y,z)` 传送 |
| `.velocity` | ✅ | LiveVec3；`.set(x,y,z)` 修改 |
| `.bounds` | ✅ | 包围盒半尺寸，只读 |
| `.meshInvisible` | ✅ | 同步 `Entity.setInvisible()` |
| `.addTag(tag)` | ✅ | |
| `.hasTag(tag)` | ✅ | |
| `.removeTag(tag)` | ✅ | |
| `.sound(path)` | ✅ | 固定 NOTE_BLOCK_PLING |
| `.hp` | ✅ | LivingEntity 同步 |
| `.maxHp` | ✅ | LivingEntity 同步 |
| `.destroyed` | ✅ | `Entity.isRemoved()`，只读 |
| `.hurt(amount)` | ✅ | |
| `.heal(amount)` | ✅ | |
| `.destroy()` | ✅ | 触发 onDestroy → discard |
| `.remove()` | ⬆ | `discard()` 不触发 onDestroy |
| `.onDestroy(handler)` | ✅ | |
| `.setFire(ticks)` | ⬆ | 点燃实体 |
| `.clearFire()` | ⬆ | 扑灭火焰 |
| `.lookAt(x, y, z)` | ⬆ | 实体面朝指定坐标 |
| `.navigateTo(x, y, z, speed)` | ⬆ | 寻路步行到目标（PathfinderMob） |
| `.setAI(enabled)` | ⬆ | 开关实体 AI |
| `.equipment.set(slot, itemId)` | ⬆ | 给生物穿装备；slot: mainhand/offhand/head/chest/legs/feet |
| `.effect.add(effectId, dur, amp)` | ⬆ | 给任意 LivingEntity 添加药水效果 |
| `.setTarget(entity)` | ⬆ | 设置怪物攻击目标（Mob.setTarget） |
| `.getTarget()` | ⬆ | 获取怪物当前攻击目标 |
| `.clearTarget()` | ⬆ | 清除攻击目标 |
| `.isGlowing()` / `.setGlowing(v)` | ⬆ | 发光效果 |
| `.getNameTag()` / `.setNameTag(n)` | ⬆ | 自定义名称 |
| `.getOnGround()` | ⬆ | 是否在地面 |
| `.getEyePosition()` | ⬆ | 视线高度 GameVector3 |
| `.isInvulnerable()` / `.setInvulnerable(v)` | ⬆ | 无敌状态 |
| `entity.任意字段` | ✅ | 自定义属性，实体生命周期内有效 |

---

## player (GamePlayerEntity)

### 基本信息 / 外观

| API | 类型 | 说明 |
|---|---|---|
| `.name` | ✅ | 只读 |
| `.userId` | ✅ | UUID，只读 |
| `.invisible` | ✅ | get/set |
| `.scale` | ✅ | 只读 |

### 移动

| API | 类型 | 说明 |
|---|---|---|
| `.walkSpeed` | ✅ | `MOVEMENT_SPEED` attribute |
| `.runSpeed` | ✅ | walkSpeed × 1.3 |
| `.jumpPower` | ✅ | `JUMP_STRENGTH` attribute |
| `.moveState` | ✅ | FLYING/SWIM/JUMP/FALL/GROUND |
| `.walkState` | ✅ | CROUCH/RUN/WALK/NONE |

### 飞行 / 游戏模式

| API | 类型 | 说明 |
|---|---|---|
| `.canFly` | ✅ | `PlayerAbilities.mayfly` |
| `.spectator` | ✅ | 只读 |
| `.flySpeed` | ✅ | `PlayerAbilities.flyingSpeed` |
| `.disableFly` | ✅ | set true 时立即禁用飞行 |
| `.gameMode` | ✅ | get 返回名称；set 接受字符串或 0-3 |

### 相机

| API | 类型 | 说明 |
|---|---|---|
| `.cameraMode` | ✅ | FPS 调用 `setCamera(null)` |
| `.cameraEntity` | ✅ | FOLLOW 调用 `setCamera(entity)` |
| `.cameraPitch` | ✅ | `getXRot()` / `setXRot()` |
| `.cameraYaw` | ✅ | `getYRot()` / `setYRot()` |
| `.facingDirection` | ✅ | 只读，`getLookAngle()` |
| `.cameraTarget` | ✅ | 只读，eye + look × 5.0 |

### 重生

| API | 类型 | 说明 |
|---|---|---|
| `.setRespawnPoint(pos)` | ✅ | `player.setRespawnPosition()` |
| `.respawn()` | ✅ | `player.respawn()`（仅死亡时有效） |

### 踢出 / 传送

| API | 类型 | 说明 |
|---|---|---|
| `.kick()` | ✅ | 默认 "Kicked" |
| `.kick(reason)` | ✅ | |
| `.teleport(pos)` | ✅ | |

### 消息

| API | 类型 | 说明 |
|---|---|---|
| `.directMessage(msg)` | ✅ | |
| `.actionBar(msg)` | ✅ | 快捷栏上方 |
| `.title(title, subtitle)` | ⬆ | 默认 fadeIn=10 stay=70 fadeOut=20 |
| `.title(t, s, fIn, stay, fOut)` | ⬆ | 完全参数 |
| `.dialog(config)` | ✅ | 简化版，返回 `{index, value}` |
| `.link(href)` | ✅ | 可点击链接 |
| `.onChat(handler)` | ✅ | 玩家级聊天回调 |

### 物品 / 效果 / 属性

| API | 类型 | 说明 |
|---|---|---|
| `.inventory.give(itemId, count)` | ⬆ | 命名空间 ID |
| `.effect.add(effectId, dur, amp)` | ⬆ | 命名空间 ID；duration 为 tick |
| `.effect.clear()` | ⬆ | `removeAllEffects()` |
| `.xp` | ⬆ | 经验等级 get/set |
| `.food` | ⬆ | 饱食度 get/set |
| `.saturation` | ⬆ | 饱和度 get/set |

### 音效

| API | 类型 | 说明 |
|---|---|---|
| `.sound(path)` | ✅ | 固定 NOTE_BLOCK_PLING |
| `.sound.play(path, vol, pitch)` | ⬆ | 播放任意 MC 音效 |

### 维度 / 物品（⬆ MC 扩展）

| API | 类型 | 说明 |
|---|---|---|
| `.dimension` | ⬆ | 维度 ID，get/set（set 可跨维度传送） |
| `.inventory.held()` | ⬆ | 主手物品 `{id, count}` |
| `.inventory.clear()` | ⬆ | 清空背包 |

### 命令（⬆ MC 扩展）

| API | 类型 | 说明 |
|---|---|---|
| `.runCommand(cmd)` | ⬆ | 以玩家身份执行命令 |

---

## 数学类型（全部 ✅）

- **GameVector3** — `new(x,y,z)`、`.set` `.add` `.sub` `.scale` `.dot` `.mag` `.sqrMag` `.normalize` `.distance` `.lerp` `.equals`、`fromPolar()`
- **GameBounds3** — `new(lo,hi)`、`.intersects` `.contains`
- **GameRGBColor** — `new(r,g,b)` (0.0–1.0)、`.lerp`、`.random()`
- **GameRGBAColor** — `new(r,g,b,a)`、`.set` `.copy` `.clone` `.add/sub/mul/div` `.addEq/subEq/mulEq/divEq` `.lerp` `.equals` `.blendEq`
- **GameQuaternion** — `new(w,x,y,z)`、`.set` `.copy` `.clone` `.add/sub/mul/div` `.inv` `.dot` `.mag` `.sqrMag` `.normalize` `.slerp` `.angle` `.getAxisAngle` `.equals` `.rotateX/Y/Z`、`.fromAxisAngle` `.fromEuler` `.rotationBetween`

---

## 枚举常量（全部 ✅）

`GameDialogType` `GameButtonType` `GameInputDirection` `GameCameraMode` `GamePlayerMoveState` `GamePlayerWalkState`

---

## voxels (GameVoxels)

| API | 说明 |
|---|---|
| `voxels.shape` | 只读 |
| `voxels.VoxelTypes` | 方块名称数组 |
| `voxels.id(name)` / `voxels.name(id)` | 名称 ↔ ID |
| `voxels.setVoxel(x,y,z, voxel, rotation?)` | 放置方块；rotation 0-3 |
| `voxels.setVoxelId(x,y,z, voxel)` | voxel 含 rotation 编码 |
| `voxels.getVoxel(x,y,z)` | 基础 ID |
| `voxels.getVoxelId(x,y,z)` | 完整 ID |
| `voxels.getVoxelRotation(x,y,z)` | 0-3 |
| `voxels.fillVoxel(x1,y1,z1, x2,y2,z2, voxel)` | ⬆ 填充矩形区域 |
| `voxels.countVoxel(x1,y1,z1, x2,y2,z2, voxel)` | ⬆ 统计区域内匹配方块数量 |

---

## storage (GameDataStorage)

| API | 说明 |
|---|---|
| `storage.key` | 空字符串 |
| `storage.getDataStorage(name)` / `getGroupStorage(name)` | 返回 GameDataStorage |
| `store.set(key, value)` / `store.get(key)` | 读写 JSON |
| `store.keys()` | 返回所有 key |
| `store.update(key, handler)` | 回调更新 |
| `store.remove(key)` / `store.increment(key, delta?)` | 删除/递加 |
| `store.list(options)` | 分页排序过滤 |
| `store.destroy()` | 删除存储 |

---

## 命令

| 命令 | 说明 |
|---|---|
| `/box3script eval <code>` | 执行 JS |
| `/box3script file <path>` | 加载执行 JS 文件 |
| `/box3script run <project>` | 运行一次项目的 app.js |
| `/box3script list` | 列出所有项目及开关状态 |
| `/box3script on <project>` | 启用项目 |
| `/box3script off <project>` | 禁用项目 |
| `/box3script reload` | 重载所有启用项目 |
| `/box3script stop` | 停止所有脚本，清空回调 |
| `/box3script create <name>` | 创建新项目目录及 `app.js` 模板 |

---

## 永不能实现的 API

| API | 原因 |
|---|---|
| `world.animate/getAnimations` | 世界关键帧动画 |
| `world.createEntity/createPlayerEntity` | 动态实体创建（用 `spawnEntity` 替代） |
| `.animation` `.setMotionControl` | Voxa 动作系统 |
| `.enable3DCursor` | 3D 光标 |
| `.boxId` `.userKey` `.querySocial` | Box3 平台账户 |
| `rtc` `analytics` `remoteChannel` `http` `defineParser` | 跨端通讯/语音/分析 |
| 全部客户端 API | 服务端无客户端上下文 |

---

## 命名空间 API (v2) — 全部为 ⬆ MC 扩展

以下 `world.*` / `player.*` / `entity.*` 命名空间 API 均为 MC 原生扩展，非 Box3 原有。

### world.scoreboard

| API | 说明 |
|---|---|
| `world.scoreboard.add(name)` | 创建 dummy 记分项 |
| `world.scoreboard.add(name, criteria)` | 创建指定标准记分项 |
| `world.scoreboard.setScore(entityOrName, obj, value)` | 设置分数 |
| `world.scoreboard.getScore(entityOrName, obj)` | 获取分数 |
| `world.scoreboard.show(slot, obj)` | 显示记分板 |
| `world.scoreboard.hide(slot)` | 清除显示槽位 |
| `world.scoreboard.remove(name)` | 删除记分项 |
| `world.scoreboard.list(name)` | 获取所有分数条目 |

### world.bossbar

| API | 说明 |
|---|---|
| `world.bossbar.show(name, text, progress, color)` | 显示/更新 Boss 血条 |
| `world.bossbar.remove(name)` | 移除 Boss 血条 |

### world.team

| API | 说明 |
|---|---|
| `world.team.create(name, color)` | 创建队伍 |
| `world.team.join(entity, teamName)` | 加入队伍 |
| `world.team.leave(entity)` | 移出队伍 |
| `world.team.remove(name)` | 删除队伍 |
| `world.team.of(entity)` | 获取队伍名称 |

### world.border

| API | 说明 |
|---|---|
| `world.border.size()` | 获取边界大小 |
| `world.border.center(x, z)` | 设置边界中心 |
| `world.border.set(size)` | 立即设置边界大小 |
| `world.border.shrink(target, sec)` | 平滑缩圈 |
| `world.border.damage(d)` | 边界外伤害 |
| `world.border.warning(blocks)` | 警告距离 |

### world.lightning

| API | 说明 |
|---|---|
| `world.lightning.strike(x, y, z)` | 召唤闪电 |
| `world.lightning.strike(x, y, z, damage)` | 召唤闪电（自定义伤害） |

### world.firework

| API | 说明 |
|---|---|
| `world.firework.launch(x, y, z, color, shape)` | 发射烟花 |

### world.particle

| API | 说明 |
|---|---|
| `world.particle.spawn(type, x, y, z, ct, dx, dy, dz, spd)` | 生成粒子 |
| `world.particle.circle(x, y, z, radius, type, count)` | 圆形粒子圈 |

### world.drop

| API | 说明 |
|---|---|
| `world.drop.item(x, y, z, itemId, count)` | 掉落物品 |

---

## 统计

| 状态 | 数量 |
|---|---|
| ✅ Box3 API | ~100 |
| ⬆ MC 扩展 | ~83 |

> 最后更新：2026-04-30
