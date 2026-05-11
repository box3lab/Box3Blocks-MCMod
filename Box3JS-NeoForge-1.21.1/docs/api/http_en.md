# HTTP API

Box3JS provides HTTP request capabilities via the global `http` object, supporting all HTTP methods, timeout, custom headers, auto-parsing, binary uploads, and both synchronous and asynchronous calling modes.

> **Synchronous requests** block the server tick — avoid long-running requests in high-frequency callbacks. **Async requests** (`async: true`) are non-blocking and deliver results via callbacks.

## `http.fetch(url, options?)`

Sends an HTTP request and returns `GameHttpFetchResponse`.

| Parameter | Type | Description |
|-----------|------|-------------|
| `url` | `string` | The request URL |
| `options` | `GameHttpFetchRequestOptions` | Optional configuration |

## GameHttpFetchRequestOptions

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `method` | `string` | `"GET"` | HTTP method: GET / POST / PUT / DELETE / PATCH / HEAD / OPTIONS |
| `headers` | `object` | `{}` | Request headers as key-value pairs |
| `body` | `string \| ArrayBuffer` | — | Request body (text or binary) |
| `timeout` | `number` | `10000` | Timeout in milliseconds |
| `responseType` | `string` | — | Auto-parse: `"json"` / `"text"` / `"arrayBuffer"` |
| `maxBodySize` | `number` | `0` | Max response body bytes, `0` = no limit. Exceeding part is truncated, `resp.truncated = true` |
| `async` | `boolean` | `false` | Set to `true` for non-blocking async request. Must provide `onResponse` / `onError` callbacks |
| `onResponse` | `function` | — | Callback on async success, receives `GameHttpFetchResponse` |
| `onError` | `function` | — | Callback on async failure, receives error message string |

> When `responseType` is set, the parsed result is available via `resp.data` — no need to call `resp.json()` manually.
>
> In async mode `fetch()` returns `null`. Results are delivered via callbacks.

## GameHttpFetchResponse

### Properties

| Property | Type | Description |
|----------|------|-------------|
| `status` | `number` | HTTP status code |
| `statusText` | `string` | Status text description |
| `ok` | `boolean` | Whether the request was successful (status 200-299) |
| `errorMessage` | `string` | Error message (only set on failure) |
| `headers` | `object` | Response headers as key-value pairs |
| `data` | `any` | Auto-parsed result (requires `responseType`) |
| `truncated` | `boolean` | Whether the response body was truncated due to `maxBodySize` |

### Methods

#### `getHeader(name)`

Returns a single response header value, or `null` if absent.

```js
const ct = resp.getHeader("Content-Type");
```

#### `json()`

Parses the response body as JSON. Returns `null` on parse failure.

#### `text()`

Returns the response body as text.

#### `arrayBuffer()`

Returns the response body as a byte array.

#### `close()`

Closes the connection (no-op in synchronous implementation, provided for API compatibility).

## Examples

```js
// GET request
const resp = http.fetch("https://api.example.com/data");

// GET with auto-parse JSON
const data = http.fetch("https://api.example.com/data", { responseType: "json" }).data;

// POST JSON
const resp2 = http.fetch("https://api.example.com/submit", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ name: "test", score: 100 })
});
const result = resp2.json();

// PUT update
http.fetch("https://api.example.com/item/1", {
  method: "PUT",
  body: JSON.stringify({ value: 99 })
});

// DELETE
http.fetch("https://api.example.com/item/1", { method: "DELETE" });

// PATCH partial update
http.fetch("https://api.example.com/item/1", {
  method: "PATCH",
  body: JSON.stringify({ status: "active" })
});

// Custom timeout
http.fetch("https://slow-server.com/api", { timeout: 5000 });

// Read response headers
console.log(resp.getHeader("Content-Type"));

// Binary upload (ArrayBuffer)
const bytes = new ArrayBuffer(4);
http.fetch("https://api.example.com/upload", { method: "PUT", body: bytes });

// Error handling
const resp4 = http.fetch("https://invalid.example.com");
if (!resp4.ok) {
  console.log("Request failed:", resp4.errorMessage);
}

// Async request (non-blocking)
http.fetch("https://api.example.com/data", {
  async: true,
  responseType: "json",
  onResponse: function(resp) {
    console.log("Async response:", resp.status, resp.data);
  },
  onError: function(err) {
    console.log("Async failed:", err);
  }
});
console.log("Request sent, code continues immediately");
```
