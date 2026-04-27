# 神奇代码岛 (Dao3) 贴图切块工具

## 安装依赖

npm install

## 使用时

npm run build
npm run start

## 输出文件

- bump/\*.png: 法线贴图
- color/\*.png: 材质贴图
- material/\*.png: 未知

### 命名格式

`<name>_<side>_<frame>`
其中

- name: 方块名称
- side: 方块面, 有"left" | "right" | "bottom" | "top" | "front" | "back"六面
- frame: 贴图帧, 当前方块面的动画帧数
