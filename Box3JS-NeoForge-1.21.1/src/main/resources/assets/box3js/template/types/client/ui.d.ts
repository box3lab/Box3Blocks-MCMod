/// <reference path="../shared.d.ts" />

// ── §5 @zh 屏幕 UI @en Screen UI ──

/** @zh 通过 `ui` 访问：屏幕文字显示 @en Accessed via `ui`: on‑screen text display */
interface GameUI {
  /**
   * @zh 在动作栏（快捷栏上方）显示文字。
   * @en Displays text in the action bar (above the hotbar).
   * @param text - @zh 要显示的文字 @en text to display
   */
  showOverlay(text: string): void;

  /**
   * @zh 显示屏幕标题。
   * @en Displays a screen title.
   * @param title - @zh 主标题 @en main title
   * @param subtitle - @zh 副标题 @en subtitle
   * @param fadeIn - @zh 淡入 tick（可选，默认 10） @en fade‑in ticks (optional, default 10)
   * @param stay - @zh 停留 tick（可选，默认 70） @en stay ticks (optional, default 70)
   * @param fadeOut - @zh 淡出 tick（可选，默认 20） @en fade‑out ticks (optional, default 20)
   */
  showTitle(
    title: string,
    subtitle: string,
    fadeIn?: number,
    stay?: number,
    fadeOut?: number,
  ): void;

  /**
   * @zh 在动作栏显示文字（与 showOverlay 相同）。
   * @en Displays text in the action bar (same as showOverlay).
   * @param text - @zh 要显示的文字 @en text to display
   */
  showActionBar(text: string): void;

  /**
   * @zh 获取当前屏幕/窗口尺寸。
   * @en Gets the current screen / window dimensions.
   * @returns @zh `{ width, height, scaledWidth, scaledHeight }` — width/height 为窗口像素, scaledWidth/scaledHeight 为 GUI 缩放后尺寸 @en `{ width, height, scaledWidth, scaledHeight }` — width/height are window pixels, scaledWidth/scaledHeight are GUI-scaled dimensions
   */
  getScreenSize(): {
    width: number;
    height: number;
    scaledWidth: number;
    scaledHeight: number;
  };

  /**
   * @zh 在屏幕上绘制自定义文字（每帧绘制，直到被移除）。
   * @en Draws custom text on screen (drawn every frame until removed).
   * @param id - @zh 文字 ID（用于后续移除），不传则自动生成 @en text ID for later removal; auto-generated if omitted
   * @param x - @zh X 坐标（GUI 缩放坐标） @en X position (GUI-scaled coordinates)
   * @param y - @zh Y 坐标（GUI 缩放坐标） @en Y position (GUI-scaled coordinates)
   * @param text - @zh 要显示的文字 @en text to display
   * @param color - @zh 文字颜色 (GameRGBColor / GameRGBAColor, 默认白色) @en text colour (GameRGBColor / GameRGBAColor, default white)
   * @returns @zh 文字 ID @en the text ID
   */
  drawText(
    id: number,
    x: number,
    y: number,
    text: string,
    color?: GameRGBColor | GameRGBAColor,
  ): number;

  /**
   * @zh 移除指定 ID 的绘制文字。
   * @en Removes the drawn text with the given ID.
   * @param id - @zh 文字 ID @en text ID
   */
  removeDrawText(id: number): void;

  /**
   * @zh 清除所有通过 `drawText()` 绘制的文字。
   * @en Clears all texts drawn via `drawText()`.
   */
  clearDrawTexts(): void;

  // ── Item icon drawing ──

  /**
   * @zh 在屏幕上绘制物品图标 (每帧绘制, 直到被移除)。
   * @en Draws an item icon on screen (drawn every frame until removed).
   * @param id - @zh 图标 ID (用于后续移除), 不传则自动生成 @en icon ID for later removal; auto-generated if omitted
   * @param x - @zh X 坐标 (GUI 缩放坐标) @en X position (GUI-scaled coordinates)
   * @param y - @zh Y 坐标 (GUI 缩放坐标) @en Y position (GUI-scaled coordinates)
   * @param itemId - @zh 物品 ID (如 "minecraft:diamond") @en item ID (e.g. "minecraft:diamond")
   * @param scale - @zh 图标尺寸 (像素, 默认 16) @en icon size in pixels (default 16)
   * @returns @zh 图标 ID @en the icon ID
   */
  drawItem(
    id: number,
    x: number,
    y: number,
    itemId: string,
    scale?: number,
  ): number;

  /**
   * @zh 移除指定 ID 的绘制图标。
   * @en Removes the drawn item icon with the given ID.
   */
  removeDrawItem(id: number): void;

  // ── Rectangle drawing ──

  /**
   * @zh 在屏幕上绘制矩形 (每帧绘制, 直到被移除)。
   * @en Draws a filled rectangle on screen (drawn every frame until removed).
   * @param id - @zh 矩形 ID (用于后续移除) @en rect ID for later removal
   * @param x - @zh 左上角 X 坐标 @en top-left X
   * @param y - @zh 左上角 Y 坐标 @en top-left Y
   * @param w - @zh 宽度 (像素) @en width in pixels
   * @param h - @zh 高度 (像素) @en height in pixels
   * @param color - @zh 颜色 (GameRGBColor) @en colour (GameRGBColor)
   * @param alpha - @zh 透明度 0‑255 (可选, 默认 255) @en alpha 0–255 (optional, default 255)
   * @returns @zh 矩形 ID @en the rect ID
   */
  drawRect(
    id: number,
    x: number,
    y: number,
    w: number,
    h: number,
    color: GameRGBColor,
    alpha?: number,
  ): number;

  /**
   * @zh 移除指定 ID 的绘制矩形。
   * @en Removes the drawn rectangle with the given ID.
   */
  removeDrawRect(id: number): void;
}
