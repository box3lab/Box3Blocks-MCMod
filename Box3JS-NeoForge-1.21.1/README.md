# Box3JS（神岛代码）-- Minecraft Mod

> **测试版（Beta）** — 本项目处于早期测试阶段，API 可能变动，可能存在未发现的缺陷。欢迎反馈问题。

[简体中文](README.md) | [English](README_en.md)

`Box3JS` 是一个 Minecraft 服务端模组，延续了神奇代码岛的代码风格。你无需编写 Java，只需使用 TypeScript 即可开发脚本。

## 特性

- **TypeScript 支持** — 项目模板内置 TS 类型声明，完整类型检查
- **Box3 API 兼容** — 实现了 Box3 平台核心 API（World / Entity / Player / Voxels / Storage）
- **MC 扩展** — 90+ Minecraft 独有功能：记分板、Bossbar、队伍、世界边界、粒子、烟花、药水等
- **热重载** — `/box3script watch` 重新加载，无需重启
- **项目管理** — 多项目隔离，独立启用/禁用，重启自动执行

## 快速开始

在游戏中（需要 OP 权限，等级 ≥ 2）：

```
/box3script create mygame
```

这会创建一个 TypeScript 脚手架项目：

```
config/box3/script/mygame/
├── .gitignore
├── package.json          ← npm 依赖（esbuild、Babel、TypeScript）
├── tsconfig.json
├── build.mjs             ← 构建脚本（esbuild → Babel → Rhino）
├── types/
│   └── globals.d.ts      ← Box3JS 完整类型声明
└── src/
    └── app.ts            ← 入口（含 Hello World 示例）
```

然后构建：

```bash
cd config/box3/script/mygame
npm install
npm run build          # 输出 dist/app.js
```

回到游戏启用：

```
/box3script on mygame
```

## 可用 API

[API 总览 →](docs/api/README.md) ([English](docs/api/README_en.md))

## 命令

[命令详细参考 →](docs/api/commands.md) ([English](docs/api/commands_en.md))

## 许可证

Apache License 2.0
