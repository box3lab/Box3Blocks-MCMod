# /box3script 命令参考

所有命令需要 **OP 权限等级 2**（默认管理员权限）。

---

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

### `/box3script list`

列出所有已发现的脚本项目及其启用/禁用状态。

```
/box3script list
```

输出示例：
```
项目列表:
  [开] skyrun
  [关] siege
  [关] mygame
```

### `/box3script on <project>`

启用指定项目并**立即加载执行**。加载错误会直接反馈到聊天栏。

```
/box3script on skyrun
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

停止所有脚本，重新加载所有已启用项目的 `app.js`。等价于 `stop` + 重新 `autoLoad`。加载错误会反馈到聊天栏。

```
/box3script reload
```

### `/box3script watch`

开启/关闭文件监控。开启后监控所有项目的 `dist/` 目录，`.js` 文件变化时自动热重载对应项目（2 秒防抖）。

```
/box3script watch          # 切换 开/关
/box3script watch on       # 开启
/box3script watch off      # 关闭
```

### `/box3script stop`

停止所有项目，清除全部回调、定时器和作用域。

```
/box3script stop
```

### `/box3script stop <project>`

停止指定项目，仅清除该项目的回调、定时器和作用域，**不影响其他正在运行的项目**。

```
/box3script stop siege
```

---

## 配置文件

启用/禁用状态保存在 `config/box3/scripts.json`：

```json
{
  "skyrun": true,
  "siege": false,
  "mygame": true
}
```

---

## 脚本目录结构

```
config/box3/
  ├── scripts.json        ← 项目开关配置
  ├── script/              ← 脚本目录
  │   ├── skyrun/
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
