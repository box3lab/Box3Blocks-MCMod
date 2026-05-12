# Box3JS 教程

从零开始学习 Box3JS 脚本开发。每个教程约 10-15 分钟，包含可直接运行的完整代码。

## 学习路径

```
教程一          教程二          教程三              教程四              教程五
  │               │               │                   │                   │
从零开始  →   玩家操控  →   事件系统  →   高级游戏系统  →   实战小游戏
第一个脚本     物品给予      实体操控         计分板/BossBar     PvP 竞技场
聊天命令       药水效果      方块交互         队伍/边界          波次刷怪
定时任务       游戏模式      AI/巡逻          跨脚本通信         特效大全
```

## 教程列表

| # | 教程 | 你会学到 |
|---|------|---------|
| 1 | [从零开始](01-basics.md) | 创建项目 → 构建 → 第一个脚本 → 聊天命令 → 定时任务 |
| 2 | [玩家操控与物品](02-player-items.md) | 传送、飞行、物品给予、附魔、药水效果、游戏模式 |
| 3 | [事件系统与实体操控](03-events-entities.md) | 全部事件回调、生成实体、AI 控制、巡逻守卫、碰撞检测 |
| 4 | [高级游戏系统](04-advanced-systems.md) | 计分板排名、BossBar 倒计时、队伍分组、世界边界缩圈、跨脚本通信 |
| 5 | [实战小游戏](05-examples.md) | PvP 竞技场（完整可玩）、粒子特效大全、烟花秀、波次刷怪、家传送 |

## 你需要知道

- **语言：** JavaScript/TypeScript。如果你会 JS，直接用 `.ts` 文件当成 JS 写就行。
- **环境：** 所有代码运行在服务端，不需要客户端安装任何东西。
- **热重载：** 改完代码 `npm run build` 后 `/box3script reload`，无需重启服务器。
- **发布部署：** 开发完成后 `/box3script compile` 编译为独立 JAR，放入 `mods/` 目录即可运行。需要 Box3JS 作为依赖提供运行时。
- **API 速查：** 写代码时遇到"这个功能用什么 API"，翻 [API 速查表](../api/README.md) 按任务查找。

## 最简示例

如果你只想看一眼 Box3JS 长什么样：

```js
// app.ts — 聊天命令 + 定时广播
world.onChat((entity, message) => {
  if (message === "!hello") {
    entity.player.directMessage("你好，" + entity.player.name + "！");
    return false;
  }
  return true;
});

world.setInterval(() => {
  world.say("当前在线: " + world.querySelectorAll("*").length + " 人");
}, 6000);
```

`npm run build` → `/box3script start <项目名>` 即可运行。

## 完整 API 文档

| 文档 | 说明 |
|------|------|
| [world](../api/world.md) | 世界状态、事件回调、粒子、烟花、音效 |
| [entity](../api/entity.md) | 实体属性、AI、装备、效果 |
| [player](../api/player.md) | 背包、消息、飞行、传送 |
| [voxels](../api/voxels.md) | 方块读写、区域填充 |
| [storage](../api/storage.md) | JSON 数据持久化 |
| [database](../api/database.md) | SQLite 数据库 |
| [http](../api/http.md) | HTTP 网络请求 |
| [client](../api/client.md) | 客户端脚本（UI/输入/聊天/音效） |
| [registries](../api/registries.md) | 自定义方块/物品/音效 |
| [math](../api/math.md) | GameVector3、Color、Quaternion |
| [commands](../api/commands.md) | `/box3script` 命令参考 |
