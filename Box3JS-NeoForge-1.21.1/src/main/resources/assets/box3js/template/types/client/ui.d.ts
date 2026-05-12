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
}
