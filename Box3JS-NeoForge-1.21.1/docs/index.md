---
layout: home

hero:
  name: "Box3JS"
  text: "Minecraft TypeScript 脚本引擎"
  tagline: 不写 Java，不用 Gradle，不用重启。写 TypeScript，热重载秒级生效。
  actions:
    - theme: brand
      text: 快速开始
      link: /guide/getting-started
    - theme: alt
      text: 为什么选择 Box3JS？
      link: /guide/about-box3js

features:
  - icon: ⚡
    title: TypeScript，零 Java
    details: 所有 API 均提供完整 DTS 类型定义 + 双语 JSDoc。esbuild + Babel 构建管线自动转译现代 TS 到 ES5。无需 JDK、Gradle、IDE 配置。
  - icon: 🔄
    title: 秒级热重载
    details: 修改 → 保存 → 即时生效，不需要重启服务端。内置文件监听，保存即自动重载。
  - icon: 🎮
    title: Box3 兼容 API
    details: 延续神奇代码岛的简洁 API 风格。World、Entity、Player、Voxels、Storage、Database、HTTP、remoteChannel——全部全局注入，无需 import。
  - icon: 🧩
    title: 110+ MC 扩展
    details: 记分板、Bossbar、队伍、世界边界、粒子、烟花、闪电、药水、自定义方块/物品/音效注册……覆盖常见玩法需求。
  - icon: 🖥️
    title: 服务端 & 客户端双端脚本
    details: 服务端处理游戏逻辑和世界操作，客户端处理键盘、HUD、音效、自定义 GUI。通过 remoteChannel 双向实时通信。
  - icon: 🛡️
    title: 沙盒保护
    details: 开启沙盒自动追踪脚本所有改动，关闭沙盒完整回滚。放心测试，不破坏地图。
  - icon: 📦
    title: 独立 JAR 分发
    details: /box3script compile 一键编译为独立模组，放入 mods/ 即可运行，无需 Box3JS 依赖。
  - icon: 🗄️
    title: 内置持久化
    details: 服务端和客户端均支持 JSON 文件存储和 SQLite 数据库。排行榜、玩家存档、经济系统——全部内置。

---

## 快速开始

```bash
/box3script create mygame       # 创建 TypeScript 项目
```

```bash
cd config/box3/script/mygame
npm install && npm run build     # 安装依赖 + 构建
```

```bash
/box3script sandbox mygame       # 开启沙盒（推荐）
/box3script start mygame         # 启动脚本
```

打开 `src/server/app.ts` 写游戏逻辑，保存后 `/box3script reload mygame`。开启 `/box3script watch` 后保存即自动重载。

[完整文档 →](/guide/getting-started)

