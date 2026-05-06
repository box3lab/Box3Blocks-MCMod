# /box3script 命令参考

所有命令需要 **OP 权限等级 2**（默认管理员权限）。所有 `<project>` 参数均支持 **Tab 自动补全**。

## 命令列表

### `/box3script create <name>`

创建新的 TypeScript 脚本项目。在 `config/box3/script/<name>/` 下生成完整的 TS 脚手架。创建后默认**禁用**。

```
/box3script create mygame
```

生成的文件结构：

```
config/box3/script/
  └── mygame/
      ├── .gitignore
      ├── package.json          ← 依赖（esbuild、Babel、TypeScript）
      ├── tsconfig.json
      ├── build.mjs             ← 构建脚本
      ├── types/
      │   └── globals.d.ts      ← Box3JS 类型声明
      └── src/
          └── app.ts            ← 入口（含 Hello World 示例）
```

创建后需要手动安装依赖和构建：

```bash
cd config/box3/script/mygame
npm install
npm run build          # 输出 dist/app.js
```

### `/box3script`

直接输入不带参数，列出所有项目及启用/禁用/沙盒状态。

```
/box3script
```

输出示例：

```
=== Projects ===
  [ON] [SANDBOX]  colorzone
  [ON]  demo
  [OFF]  siege
```

### `/box3script on <project>`

启用指定项目并**立即加载执行**。加载错误会直接反馈到聊天栏。

```
/box3script on mygame
```

### `/box3script on all`

一键启用所有项目。

```
/box3script on all
```

### `/box3script off <project>`

禁用指定项目。下次服务端重启时不再自动执行。

```
/box3script off siege
```

### `/box3script off all`

一键禁用所有项目。

```
/box3script off all
```

### `/box3script reload`

停止所有脚本，重新加载所有已启用项目的 `app.js`。加载错误会反馈到聊天栏。

```
/box3script reload
```

### `/box3script reload <project>`

重新加载指定项目（先停止再启动）。未启用的项目会自动设为启用后启动。

```
/box3script reload mygame
```

### `/box3script watch`

开启/关闭文件监控。开启后监控所有项目的 `dist/` 目录，`.js` 文件变化时自动热重载对应项目。

```
/box3script watch          # 切换 开/关
/box3script watch on       # 开启
/box3script watch off      # 关闭
```

### `/box3script sandbox <project>`

切换沙盒模式。开启后自动追踪该项目所有的方块修改、实体/玩家/世界状态变更。**沙盒持久化**——`/box3script stop` 和 `/box3script reload` 不会清除沙盒状态，仅手动再次执行此命令才会关闭沙盒并回滚全部修改。关闭时在聊天栏显示恢复摘要。

```
/box3script sandbox mygame    # 切换 开/关
```

**追踪内容：**

| 类别 | 追踪项                                                                                 |
| ---- | -------------------------------------------------------------------------------------- |
| 方块 | `setVoxel`/`setVoxelId`/`fillVoxel` 修改（上限 500 万块）                              |
| 实体 | HP、AI、隐身、发光、无敌、着火、药水效果、标签、名称、装备、掉落率、属性               |
| 玩家 | 游戏模式、飞行能力、速度、跳跃力、经验、饱食度、物品栏、护甲、药水、位置、维度、重生点 |
| 世界 | 天气、时间、难度、游戏规则、世界边界                                                   |

典型工作流：

```
/box3script sandbox mygame    # 开启沙盒
/box3script on mygame         # 加载脚本
# ... 测试、观察结果 ...
/box3script stop mygame       # 停止脚本，不改世界
# ... 修改代码、npm run build ...
/box3script on mygame         # 再次测试
# ... 满意后关闭沙盒回滚 ...
/box3script sandbox mygame    # 关闭沙盒 → 回滚 + 显示摘要
```

> **注意：** 沙盒仅追踪通过脚本 API 修改的方块（`setVoxel`/`setVoxelId`/`fillVoxel`）。直接用镐子挖的方块不受影响。追踪上限 500 万块，达到 90% 时控制台日志警告。

### `/box3script stop`

停止所有项目，清除全部回调、定时器和作用域。**已开启沙盒的项目会自动保留沙盒追踪状态**，不会被回滚。

```
/box3script stop
```

### `/box3script stop <project>`

停止指定项目，仅清除该项目的回调、定时器和作用域，**不影响其他正在运行的项目**。沙盒项目会保留追踪状态，不会回滚。

```
/box3script stop siege
```

## 配置文件

启用/禁用状态保存在 `config/box3/scripts.json`：

```json
{
  "mygame": true,
  "siege": false,
  "mygame": true
}
```

## 脚本目录结构

```
config/box3/
  ├── scripts.json        ← 项目开关配置
  ├── script/              ← 脚本目录
  │   ├── mygame/
  │   │   ├── package.json
  │   │   ├── src/app.ts
  │   │   └── dist/app.js  ← 编译产物
  │   └── mygame/
  │       ├── package.json
  │       ├── src/app.ts
  │       └── dist/app.js
  └── storage/             ← 存储数据目录 (storage API)
      └── ...
```
