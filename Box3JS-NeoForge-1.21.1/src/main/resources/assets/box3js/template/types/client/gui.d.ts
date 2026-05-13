/// <reference path="../shared.d.ts" />

// ── § @zh 自定义 GUI @en Custom GUI ──

/** @zh 通过 `gui` 访问：自定义容器界面 @en Accessed via `gui`: custom container GUI */
interface GameGUI {
  /**
   * @zh 打开一个脚本控制的自定义容器界面。
   * @en Opens a script-controlled custom container GUI.
   *
   * @example
   * ```ts
   * const ctrl = gui.openGUI({ title: "Shop", rows: 3, slots: { 0: "minecraft:diamond" } });
   * console.log(ctrl.getItem(0)); // { id: "minecraft:diamond", count: 1 }
   * ctrl.onSlotClick((slot) => { console.log("Clicked slot:", slot); });
   * ctrl.onClose(() => { console.log("GUI closed"); });
   * ctrl.close();
   * ```
   *
   * @param config - @zh 配置对象 @en configuration object
   * @returns @zh GuiController — 可操作容器和注册事件回调 @en GuiController — for manipulating the container and registering event callbacks
   */
  openGUI(config: GuiOpenConfig): GuiController;
}

/** @zh GUI 配置 @en GUI configuration */
interface GuiOpenConfig {
  /** @zh 标题（默认 "Container"） @en Title (default "Container") */
  title?: string;
  /** @zh 行数 1–6（默认 3） @en Number of rows 1–6 (default 3) */
  rows?: number;
  /** @zh 初始物品 `{ [slotIndex: number]: string }` @en Initial items `{ [slotIndex: number]: string }` */
  slots?: { [slot: number]: string };
}

/** @zh GUI 控制器 @en GUI controller */
interface GuiController {
  /**
   * @zh 设置指定槽位的物品。
   * @en Sets the item in the given slot.
   * @param slot - @zh 槽位索引 (0–rows×9-1) @en Slot index (0–rows×9-1)
   * @param itemId - @zh 物品 ID @en Item ID
   * @param count - @zh 数量（可选，默认 1） @en Count (optional, default 1)
   */
  setItem(slot: number, itemId: string, count?: number): void;

  /**
   * @zh 获取指定槽位的物品。
   * @en Gets the item in the given slot.
   * @param slot - @zh 槽位索引 @en Slot index
   * @returns @zh `{ id: string, count: number }` — 空槽位返回 minecraft:air, count 0 @en `{ id: string, count: number }` — empty slot returns minecraft:air, count 0
   */
  getItem(slot: number): { id: string; count: number };

  /**
   * @zh 注册槽位点击回调（仅通知，无法取消点击）。
   * @en Registers a slot click callback (notification only, cannot cancel the click).
   * @param callback - @zh 回调函数，接收被点击的槽位索引 @en Callback receiving the clicked slot index
   */
  onSlotClick(callback: (slot: number) => void): void;

  /**
   * @zh 注册关闭回调。
   * @en Registers a close callback.
   * @param callback - @zh GUI 关闭时调用的回调函数 @en Callback called when the GUI is closed
   */
  onClose(callback: () => void): void;

  /**
   * @zh 关闭 GUI。
   * @en Closes the GUI.
   */
  close(): void;
}
