---
layout: home

hero:
  name: "Box3JS"
  text: "Minecraft JS/TS 脚本引擎"
  tagline: 神奇代码岛同款编程体验，用 JS/TS 在 Minecraft 里创造小游戏
  actions:
    - theme: brand
      text: 快速开始
      link: /guide/getting-started

features:
  - icon: 🎮
    title: 服务端 & 客户端双端脚本
    details: 服务端：世界操作、实体、合成配方。客户端：键盘输入、屏幕 UI、音效、SQLite 存储、HTTP 请求。
  - icon: 📦
    title: TypeScript 优先
    details: 17 个全局对象全部提供 DTS 类型定义。内置 esbuild + Babel 构建管线，将现代 TS 转译为 Rhino 兼容的 ES5。
  - icon: 🔄
    title: 热重载
    details: 修改脚本即时生效，无需重启服务端。文件监听器在保存时自动重载。
  - icon: 🌐
    title: 双向通信
    details: remoteChannel 实现服务端↔客户端事件消息传递。服务端广播至所有玩家，客户端独立回复。
  - icon: 🗄️
    title: 双端存储 & 数据库
    details: 服务端和客户端均支持 JSON 文件持久化与 SQLite。分页查询、原子更新、计数器、标签模板查询。
  - icon: 🧩
    title: 自定义方块 & 物品
    details: 方块纹理、物品模型、装备、音效、创造标签页——全部通过 JSON 配置注册（独立/JAR 模式）。
  - icon: 📚
    title: 完善文档
    details: 50+ 页面，涵盖 API 参考、渐进式教程、常用配方、架构深入解析和常见问题——支持中英双语。
  - icon: 🚀
    title: 独立 JAR 模式
    details: 将脚本项目编译为独立 JAR 模组，无需依赖 Box3JS 运行时——放入 mods 文件夹即可使用。

---

## 快速开始

```bash
# 游戏内：创建新项目
/box3script create mygame

# 构建并监听
cd config/box3/script/mygame
npm install
npm run build -- --watch

# TypeScript 类型检查
npm run check
```

```ts
// src/server/app.ts — 你的第一个脚本
world.onChat((entity, message) => {
  if (message === "!hello") {
    entity.player.directMessage(`你好，${entity.player.name}！`);
    return false;
  }
  return true;
});
```

[查看完整文档 →](/guide/getting-started)

## 版本信息

| 组件 | 版本 |
|------|------|
| Minecraft | 1.21.1 |
| 模组加载器 | NeoForge |
| Java | 21 |
| JS 引擎 | Mozilla Rhino 1.9.1 (ES5) |
| TypeScript | 通过 Babel → ES5 |

