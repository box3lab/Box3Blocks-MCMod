# JS 脚本 vs 原生 Java 模组开发对比

## 总览

| 维度                | Box3JS (JS/TS)                  | 原生 Java 模组                              |
| ------------------- | ------------------------------- | ------------------------------------------- |
| **上手门槛**        | 会 JavaScript 即可              | 需要 Java + Gradle + Minecraft 模组开发知识 |
| **开发速度**        | 改代码 → build → reload（秒级） | 改代码 → 编译 → 重启 MC（分钟级）           |
| **热重载**          | 支持（`/box3script reload`）    | 不支持，每次改代码需重启客户端/服务端       |
| **发布方式**        | `/box3script compile` 生成 JAR  | `gradlew build` 生成 JAR                    |
| **执行性能**        | 中等（Rhino 解释执行）          | 高（JIT 编译为字节码）                      |
| **API 覆盖面**      | 高层封装 API（100+ 方法）       | 完整 Minecraft/NeoForge API                 |
| **类型安全**        | TypeScript 类型声明             | Java 静态类型                               |
| **调试工具**        | console.log + 控制台输出        | IDE 断点调试                                |
| **依赖管理**        | npm（仅构建时）                 | Gradle/Maven                                |
| **客户端功能**      | 有限（UI/输入/音效/聊天）       | 完整（渲染、模型、GUI、网络协议）           |
| **自定义方块/物品** | JSON 配置 + 编译时生成          | Java 类 + 注册                              |
| **修改原版行为**    | 不支持（无 Mixin）              | 支持（Mixin/ASM/CoreMod）                   |
| **多人协作**        | JS 源码 + Git                   | Java 源码 + Git + Gradle                    |

## 开发体验对比

### Box3JS 的优势

#### 1. 极低的上手门槛

```js
// Box3JS — 5 行代码，立即生效
world.onChat((entity, message) => {
  if (message === "!heal") {
    entity.player.hp = entity.player.maxHp;
    entity.player.directMessage("§a已治愈！");
    return false;
  }
  return true;
});
```

```java
// 原生 Java 模组 — 需要 3 个文件、注册事件、处理 chat 事件
@Mod("myhealmod")
public class HealMod {
    public HealMod(IEventBus bus) {
        bus.addListener(ServerChatEvent.class, this::onChat);
    }
    private void onChat(ServerChatEvent event) {
        if (event.getMessage().getString().equals("!heal")) {
            ServerPlayer player = event.getPlayer();
            player.setHealth(player.getMaxHealth());
            player.sendSystemMessage(Component.literal("已治愈！"));
            event.setCanceled(true);
        }
    }
}
```

**不需要学 Gradle、不需要配模组开发环境、不需要等编译。** 你会 JS 就能写。

#### 2. 秒级热重载

这是 Box3JS **最大的生产力优势**。

| 操作             | Box3JS                            | Java 模组                                      |
| ---------------- | --------------------------------- | ---------------------------------------------- |
| 修改一行代码     | build(3s) + reload(1s) = **4 秒** | 编译(10-60s) + 重启MC(30-120s) = **40-180 秒** |
| 测试一个聊天命令 | 改代码 → build → 游戏内 reload    | 改代码 → 编译 → 重启MC → 进入世界              |
| 一天迭代次数     | **50+**                           | 5-10                                           |

对于玩法脚本（小游戏、RPG 机制、经济系统），热重载是**不可替代的**——玩法需要反复调参试错，等不起重启。

#### 3. 简化的 API 设计

Box3JS 的高层 API 屏蔽了 Minecraft 的复杂性：

```js
// Box3JS: 给玩家一个物品
player.giveItem("minecraft:diamond_sword", 1);

// Java: 需要创建 ItemStack、获取 Inventory、调用 add
ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
player.getInventory().add(sword);
```

```js
// Box3JS: 播放粒子
world.spawnParticle("flame", x, y, z, 0.5, 0.5, 0.5, 0, 10);

// Java: 需要构造 Vec3、获取 ServerLevel、sendParticles
Vec3 pos = new Vec3(x, y, z);
serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z,
    10, 0.5, 0.5, 0.5, 0);
```

```js
// Box3JS: 计分板一行搞定
world.addScoreboard("kills");
world.setScore("Steve", "kills", 5);

// Java: 需要操作 Scoreboard、Objective、Score 三层 API
```

#### 4. 一站式项目模板

`/box3script create` 生成完整的项目结构，包含：

- TypeScript 配置 + 类型声明
- 构建管线（Babel + esbuild）
- ESLint 代码检查
- 服务端/客户端双入口

对比 Java 模组：需要手动创建 Gradle 项目、配置 NeoForge MDG、创建 mods.toml、注册总线……新手指南通常要 50 页。

#### 5. 适合快速原型验证

在正式写 Java 模组前，用 Box3JS 快速验证玩法设计：

```js
想法 → 30分钟写Box3JS脚本 → 和朋友试玩 → 调整 → 确认玩法可行
                                                      ↓
                                         决定做完整模组 → 用 Java 重写
```

### Box3JS 的劣势

#### 1. 性能开销

Rhino 是**解释型** JS 引擎（无 JIT），单线程执行。对性能敏感的操作（如每 tick 扫描大量实体）可能成为瓶颈。

| 场景                      | Box3JS       | Java   |
| ------------------------- | ------------ | ------ |
| 聊天命令                  | 无感知       | 无感知 |
| 每 tick 遍历 100 个实体   | 可接受       | 可接受 |
| 每 tick 遍历 10000 个实体 | **可能卡顿** | 可接受 |
| 复杂数学运算（路径算法）  | **明显慢**   | 快     |
| Y=0 区块全图填充          | **很慢**     | 快     |

**经验法则**：如果 `onTick` 回调耗时超过 1ms，考虑优化或改用 Java。

#### 2. API 覆盖不完整

Box3JS 封装了 100+ 常用 API，但不是全部：

| 你想做的               | Box3JS                  | Java                         |
| ---------------------- | ----------------------- | ---------------------------- |
| 修改合成表             | ❌                      | ✅ `RecipeManager`           |
| 自定义 GUI（箱子界面） | ❌                      | ✅ `MenuProvider` / `Screen` |
| 修改生物 AI            | 部分（setAI/setTarget） | ✅ Brain/Memory 系统         |
| 自定义维度             | ❌                      | ✅ `DimensionType`           |
| 数据包/战利品表        | ❌                      | ✅ 完整支持                  |
| 网络协议               | 高层（remoteChannel）   | ✅ 底层 `CustomPayload`      |
| 修改原版类行为         | ❌                      | ✅ Mixin / ASM               |
| 渲染自定义模型         | ❌                      | ✅ 完整渲染管线              |

#### 3. 无断点调试

只能通过 `console.log` 输出调试信息，没有 IDE 断点、变量监视、堆栈追踪等现代化调试体验。复杂 bug 定位较为困难。

#### 4. 客户端功能有限

客户端脚本可以做：

- 键盘输入检测
- 屏幕 UI 显示
- 音效/音乐播放
- 聊天收发

但不能做：

- 自定义渲染（模型、粒子、GUI）
- 修改 HUD
- 自定义着色器
- 键盘/鼠标事件拦截（除了轮询和简单回调）

#### 5. ES5 限制

Rhino 1.9.1 仅支持 ES5 语法。不能使用：

- `let` / `const`（Babel 编译为 `var`）
- 箭头函数（Babel 编译为 `function`）
- `async` / `await`
- `Promise`
- `class`
- 模板字符串
- 解构赋值

但 **Babel 会把 TS 编译为 ES5**，所以你可以用 TS 写现代语法，构建后自动转换。

#### 6. 部署依赖 Box3JS

编译后的 JAR 依赖 Box3JS 作为运行时。用户需要同时安装 Box3JS + 你的 JAR。而纯 Java 模组是自包含的。

## 适用场景决策树

```text
你想做什么？
│
├─ 小游戏（PvP/跑酷/竞速）
│  └─ → Box3JS ✅ 热重载快速迭代
│
├─ 聊天命令 / 经济系统 / 领地
│  └─ → Box3JS ✅ 主要是 API 调用
│
├─ RPG 机制（技能/副本/任务）
│  └─ → Box3JS ✅ 逻辑多、需频繁调参
│
├─ 自定义事件（进服欢迎/死亡惩罚）
│  └─ → Box3JS ✅ 简单事件响应
│
├─ 服务端管理工具
│  └─ → Box3JS ✅ 快速开发
│
├─ 自定义方块/物品装饰
│  └─ → Box3JS ✅ registries JSON 配置
│
├─ 需要重型计算（路径算法/大量实体）
│  └─ → Java ⚠️ 性能要求高
│
├─ 自定义 GUI / 渲染 / 模型
│  └─ → Java ❌ Box3JS 不支持
│
├─ 修改原版机制（合成/掉落/生物行为）
│  └─ → Java ❌ 需要 Mixin
│
├─ 完整的大型模组（100+ 方块/生物/维度）
│  └─ → Java ❌ Box3JS 架构不适合
│
└─ 需要作为独立模组发布到 CurseForge/Modrinth
   └─ → 取决于复杂度
       简单玩法 → Box3JS compile JAR
       大量内容 → Java 原生模组
```

## 混合方案

最佳实践：**Box3JS 做玩法，Java 做基础设施**。

```text
┌──────────────────────────────────┐
│  Java 模组（提供底层能力）         │
│  - 自定义方块/物品注册             │
│  - 自定义实体/生物                 │
│  - Mixin 修改原版                 │
│  - 网络协议扩展                   │
└──────────┬───────────────────────┘
           │ 暴露 API 给
           ▼
┌──────────────────────────────────┐
│  Box3JS 脚本（玩法逻辑）           │
│  - 小游戏规则                     │
│  - 聊天命令                       │
│  - 事件响应                       │
│  - 经济/等级系统                  │
└──────────────────────────────────┘
```

一个真实的示例架构：

- Java 模组添加了自定义武器、自定义怪物、新维度
- Box3JS 脚本定义怪物波次规则、Boss 技能、任务触发条件
- 玩法策划可以独立修改脚本，不需要碰 Java 代码

## 总结

| 选 Box3JS                | 选 Java                          |
| ------------------------ | -------------------------------- |
| 你主要做玩法/小游戏      | 你需要修改原版机制               |
| 你需要快速迭代试错       | 你需要自定义渲染/模型            |
| 你的团队有 JS 开发者     | 你的团队主要是 Java 开发者       |
| 项目逻辑复杂但不涉及渲染 | 项目包含大量自定义方块/实体/维度 |
| 你想先验证玩法再正式开发 | 你要发布到 CurseForge/Modrinth   |
| 你需要热重载             | 你需要极致性能                   |
| 项目是服务端为主         | 项目需要客户端渲染               |

**没有谁更好，只有谁更适合当前项目。** 对于服务端玩法开发，Box3JS 的生产力优势是压倒性的——热重载 + 低门槛 + 丰富 API。对于需要修改原版机制或自定义渲染的项目，Java 是必须的。
