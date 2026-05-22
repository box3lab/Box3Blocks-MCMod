# Box3JS — Minecraft 脚本引擎

> **v0.1.0** — 首个公开测试版。API 可能变动，欢迎反馈。

[简体中文](Readme.md) | [English](README_en.md)

**无需 Java 知识。用 TypeScript 为 Minecraft 创造无限玩法。**

Box3JS 在 Minecraft 嵌入 Mozilla Rhino JavaScript 引擎，API 设计延续[神奇代码岛](https://dao3.fun)简洁高效的风格——告别 Gradle、告别重启、告别复杂的环境配置。PvP 竞技场、塔防、RPG 副本、派对小游戏，写 TypeScript，一键加载，即时生效。

> Box3JS 与神奇代码岛的关系？→ [关于 Box3JS](docs/guide/about-box3js.md)

```ts
// 玩家加入时：弹窗欢迎 + 头顶粒子 + 升级音效
world.onPlayerJoin((player) => {
  player.player.dialog({ content: "欢迎来到 Box3JS 服务器！" });
  const pos = new GameVector3(player.position.x, player.position.y + 1.8, player.position.z);
  world.spawnParticle("minecraft:happy_villager", pos, 10, 0.5, 0.5, 0.5, 0.1);
  world.playSound("minecraft:entity.player.levelup", pos, 1.0, 1.0);
});

// 聊天命令：!heal 回血、!firework 放烟花
world.onChat((player, message) => {
  if (message === "!heal") {
    player.hp = player.maxHp;
    player.player.actionBar("生命已恢复！");
    return false; // 不广播此消息
  }
  if (message === "!firework") {
    const pos = new GameVector3(player.position.x, player.position.y + 1, player.position.z);
    world.launchFirework(pos, "green", "large_ball");
    return false;
  }
});
```

<details>
<summary>对比：用传统 Java Mod 实现同样功能</summary>

```java
// Java — 需要 ~60 行、3 个类才能实现同等功能
@Mod.EventBusSubscriber(modid = "mymod")
public class PlayerJoinListener {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        // 发送对话框需要在客户端处理，服务端无法直接弹窗
        player.sendSystemMessage(
            Component.literal("欢迎来到服务器！").withStyle(ChatFormatting.GREEN)
        );
        // 粒子需要获取 ServerLevel、ParticleOptions
        ServerLevel level = player.serverLevel();
        Vec3 pos = player.position().add(0, 1.8, 0);
        level.sendParticles(
            ParticleTypes.HAPPY_VILLAGER,
            pos.x, pos.y, pos.z,
            10, 0.5, 0.5, 0.5, 0.1
        );
        // 音效同理
        level.playSound(
            null, player.blockPosition(),
            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS,
            1.0f, 1.0f
        );
    }
}

@Mod.EventBusSubscriber(modid = "mymod")
public class ChatListener {
    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String message = event.getMessage().getString();
        if ("!heal".equals(message)) {
            player.setHealth(player.getMaxHealth());
            player.displayClientMessage(
                Component.literal("生命已恢复！"), true
            );
            event.setCanceled(true);
        }
        if ("!firework".equals(message)) {
            // 烟花需要手动构建 FireworkRocketEntity
            FireworkRocketEntity firework = new FireworkRocketEntity(
                player.level(),
                player.getX(), player.getY() + 1, player.getZ(),
                createFireworkItem("green", FireworkRocketEntity.Shape.LARGE_BALL)
            );
            player.level().addFreshEntity(firework);
            event.setCanceled(true);
        }
    }
    // 还需要 ~20 行构建烟花物品的辅助方法...
}
```
</details>

## 为什么选择 Box3JS？

**零门槛上手** — 会 JS/TS 就能写模组。不需要 Gradle、不需要 IDE、不需要重启服务端。`/box3script create` 一键生成完整 TypeScript 项目。

**秒级热重载** — 改代码 → 构建 → 重载，改动即时生效。开启 `watch` 模式连构建步骤都省了，保存即更新。

**写起来像 Box3，跑在 Minecraft 里** — API 命名和设计延续神奇代码岛的开发体验，学过 Box3 的创作者可以无缝过渡。同时充分利用 Minecraft 原生能力：原版生物 AI、粒子效果、结构、进度、配方等。

**沙盒保护，放心测试** — 开启沙盒后所有脚本修改自动追踪，关闭沙盒完整回滚。测试时不用担心破坏地图，开发环境即生产环境。

**TypeScript 原生支持** — 完整 `.d.ts` 类型声明，中英双语 JSDoc 注释。构建管线：esbuild 打包 → Babel 转译 ES5 → 正则清理，全程自动。

**双端脚本** — 服务端处理游戏逻辑、实体 AI、方块操作；客户端处理键盘输入、屏幕 UI、聊天、音效。通过 `remoteChannel` 双向实时通信。

**数据持久化** — 内置 JSON 文件存储和 SQLite 数据库，排行榜、经济系统、玩家存档都能轻松实现。

**独立分发** — `/box3script compile` 将脚本编译为独立 JAR 模组，像普通模组一样放入 `mods/` 即可运行，无需 Box3JS。

## 安装

1. 将 `box3js-<version>.jar` 放入服务端 `mods/` 目录
2. 如需 SQLite 数据库（`db` API），同时安装 [`minecraft-sqlite-jdbc`](https://modrinth.com/mod/minecraft-sqlite-jdbc)
3. 启动服务器

## 快速开始

在聊天框输入（需 OP 权限 ≥ 2）：

```
/box3script create mygame       # 创建 TypeScript 项目
```

然后构建并启动：

```bash
cd config/box3/script/mygame
npm install && npm run build     # 安装依赖 + 构建
```

```
/box3script sandbox mygame       # 开启沙盒（推荐，测试无忧）
/box3script start mygame         # 启动脚本
```

打开 `src/server/app.ts` 写游戏逻辑，保存后 `npm run build`，再 `/box3script reload mygame` — **不需要重启服务器**。开启 `/box3script watch` 后，保存即自动重载。

> [完整入门指南 →](docs/guide/getting-started.md)

## 命令速查

| 命令 | 说明 |
|------|------|
| `/box3script` | 查看所有项目运行状态 |
| `/box3script create <name>` | 创建新 TypeScript 项目 |
| `/box3script start [project\|all]` | 启用并加载项目 |
| `/box3script stop [project\|all]` | 禁用并卸载项目 |
| `/box3script reload [project]` | 重载脚本（开发用） |
| `/box3script watch` | 切换文件监控（自动热重载） |
| `/box3script sandbox <project>` | 切换沙盒（开=追踪修改 / 关=完整回滚） |
| `/box3script compile <project>` | 编译为独立 JAR 模组 |

所有 `<project>` 参数支持 **Tab 自动补全**。[完整命令参考 →](docs/api/commands.md)

## API 速览

| 全局对象 | 用途 |
|----------|------|
| `world` | 世界事件、实体查询、粒子、烟花、闪电、音效、计分板、BossBar、队伍、边界 |
| `entity` | 实体属性、AI 寻路/攻击、装备、药水效果、属性修改 |
| `player` | 背包、飞行、游戏模式、传送、消息、经验 |
| `voxels` | 方块读写、区域填充、替换、刷怪笼 |
| `remoteChannel` | 服务端 ↔ 客户端 双向事件通道 |
| `client` · `input` · `ui` · `chat` · `audio` · `gui` | 客户端 API：生命周期、键盘、屏幕文字、聊天、音频、自定义容器 |
| `http` | HTTP 请求（同步 + 异步） |
| `storage` · `db` | JSON 持久化 / SQLite 数据库（双端） |
| `registries` | 自定义方块/物品/音效（编译 JAR 模式） |
| `GameVector3` · `GameBounds3` · `GameRGBColor` · `GameQuaternion` | 数学类型：向量、包围盒、颜色、四元数 |

[API 总览 →](docs/api/README.md) · [按功能速查 →](docs/api/README.md#功能速查---我想) · [完整文档 →](docs/README.md)

## 教程

从零到完整小游戏，每个示例均经 TypeScript 编译 + ESLint 验证：

| # | 教程 | 时长 | 学什么 |
|---|------|------|--------|
| 1 | [从零开始](docs/tutorial/01-basics.md) | 10 min | 项目搭建、第一个脚本、聊天命令、定时器 |
| 2 | [玩家操控与物品](docs/tutorial/02-player-items.md) | 15 min | 传送、飞行、物品给予、附魔、药水效果 |
| 3 | [事件系统与实体](docs/tutorial/03-events-entities.md) | 15 min | 全部事件回调、实体生成、AI 控制、巡逻守卫 |
| 4 | [高级游戏系统](docs/tutorial/04-advanced-systems.md) | 15 min | 计分板、BossBar、队伍、世界边界、跨脚本通信 |
| 5 | [实战小游戏](docs/tutorial/05-examples.md) | 20 min | PvP 竞技场、粒子与烟花、波次刷怪 |
| 6 | [客户端脚本](docs/tutorial/06-client-scripting.md) | 15 min | 键盘输入、屏幕 UI、音效音乐、本地存储、remoteChannel |

[教程总览 →](docs/tutorial/README.md)

## 示例项目

`run/config/box3/script/` 下包含多个完整游戏项目，可直接运行或作为参考：

| 项目 | 类型 | 亮点 |
|------|------|------|
| `patdef` | 塔防 | 四类塔（箭/冰/火/雷）、波次刷怪、GUI 商店、攻击光束特效 |
| `bedwar` | 团队竞技 | 双队对抗、资源生成、装备升级、陷阱、床破坏机制 |
| `coredf` | 核心防守 | 中文消息、多阶段波次、经济系统 |
| `az` | 多阶段游戏 | 复杂状态机、阶段切换、多玩法融合 |
| `colorzone` | 跑酷 + 教程 | 双向客户端通信、UI 示例、7 个功能演示 |
| `mygame` | API 测试 | 覆盖所有 Box3JS API 的功能测试用例 |

每个项目都有独立的客户端 `dist/client.js` 和服务端 `dist/server.js`，开箱即用。

## 依赖

| 功能 | 要求 |
|------|------|
| 脚本引擎核心 | 内嵌 Rhino 1.9.1，无需额外安装 |
| `db` API（SQLite） | 需 [`minecraft-sqlite-jdbc`](https://modrinth.com/mod/minecraft-sqlite-jdbc) |
| 其他所有 API | 无额外依赖 |

> 即使不装 SQLite 模组，除 `db` 外所有功能正常工作。


## 许可证

Apache License 2.0
