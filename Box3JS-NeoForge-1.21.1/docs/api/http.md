# HTTP API

Box3JS 通过全局 `http` 对象提供同步 HTTP 请求能力，通过 `method` 选项支持全部 HTTP 方法，以及超时、自定义请求头、自动解析、二进制上传。

> **注意：** 所有 HTTP 请求均为**同步调用**，会阻塞服务器 tick。请避免在 `world.onTick()` 等高频回调中执行长时间请求。

## `http.fetch(url, options?)`

发送 HTTP 请求，返回 `GameHttpFetchResponse`。

| 参数 | 类型 | 说明 |
|------|------|------|
| `url` | `string` | 请求地址 |
| `options` | `GameHttpFetchRequestOptions` | 可选配置 |

## GameHttpFetchRequestOptions

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `method` | `string` | `"GET"` | HTTP 方法：GET / POST / PUT / DELETE / PATCH / HEAD / OPTIONS |
| `headers` | `object` | `{}` | 请求头，键值对形式 |
| `body` | `string \| ArrayBuffer` | — | 请求体（文本或二进制） |
| `timeout` | `number` | `10000` | 超时时间（毫秒） |
| `responseType` | `string` | — | 自动解析：`"json"` / `"text"` / `"arrayBuffer"` |
| `maxBodySize` | `number` | `0` | 响应体最大字节数，`0` = 不限制。超出部分截断，`resp.truncated = true` |

> 设置 `responseType` 后，解析结果可直接通过 `resp.data` 获取，无需手动调 `resp.json()` 等。

## GameHttpFetchResponse

### 属性

| 属性 | 类型 | 说明 |
|------|------|------|
| `status` | `number` | HTTP 状态码 |
| `statusText` | `string` | 状态码描述文本 |
| `ok` | `boolean` | 是否成功（状态码 200-299） |
| `errorMessage` | `string` | 错误信息（仅在请求失败时有值） |
| `headers` | `object` | 响应头键值对 |
| `data` | `any` | 自动解析结果（需设置 `responseType`） |
| `truncated` | `boolean` | 响应体是否因超过 `maxBodySize` 被截断 |

### 方法

#### `getHeader(name)`

获取指定响应头的值，不存在返回 `null`。

```js
const ct = resp.getHeader("Content-Type");
```

#### `json()`

将响应体解析为 JSON 对象。解析失败返回 `null`。

#### `text()`

返回响应体的文本内容。

#### `arrayBuffer()`

返回响应体的字节数组。

#### `close()`

关闭连接（同步实现中为空操作，提供 API 兼容性）。

## 示例

```js
// GET 请求
const resp = http.fetch("https://api.example.com/data");

// GET + 自动解析 JSON
const data = http.fetch("https://api.example.com/data", { responseType: "json" }).data;

// POST JSON
const resp2 = http.fetch("https://api.example.com/submit", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ name: "test", score: 100 })
});
const result = resp2.json();

// PUT 更新
http.fetch("https://api.example.com/item/1", {
  method: "PUT",
  body: JSON.stringify({ value: 99 })
});

// DELETE 删除
http.fetch("https://api.example.com/item/1", { method: "DELETE" });

// PATCH 部分更新
http.fetch("https://api.example.com/item/1", {
  method: "PATCH",
  body: JSON.stringify({ status: "active" })
});

// 自定义超时
http.fetch("https://slow-server.com/api", { timeout: 5000 });

// 读取响应头
console.log(resp.getHeader("Content-Type"));

// 二进制上传（ArrayBuffer）
const bytes = new ArrayBuffer(4);
http.fetch("https://api.example.com/upload", { method: "PUT", body: bytes });

// 错误处理
const resp4 = http.fetch("https://invalid.example.com");
if (!resp4.ok) {
  console.log("请求失败:", resp4.errorMessage);
}
```
