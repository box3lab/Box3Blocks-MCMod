package com.box3lab.box3js.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.mozilla.javascript.Function;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.*;

/**
 * Client-side HTTP fetch API exposed to JS as the global {@code http} object.
 *
 * <p>Supports both synchronous (blocking) and async (callback-based) requests.
 * Async requests use {@link HttpClient#sendAsync} and deliver callbacks via
 * {@link Minecraft#execute}.
 */
public class Box3JSClientHttp {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final HttpClient client;

    public Box3JSClientHttp() {
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public Response fetch(String url, Map<String, Object> options) {
        String method = "GET";
        Map<String, Object> headers = Collections.emptyMap();
        byte[] body = null;
        long timeoutMs = 10_000;
        String responseType = null;
        long maxBodySize = 0;
        boolean async = false;
        Function onResponse = null;
        Function onError = null;

        if (options != null) {
            if (options.get("method") instanceof String m)
                method = m.toUpperCase();
            if (options.get("responseType") instanceof String rt)
                responseType = rt;
            if (options.get("maxBodySize") instanceof Number n)
                maxBodySize = n.longValue();
            @SuppressWarnings("unchecked")
            Map<String, Object> h = (Map<String, Object>) options.get("headers");
            if (h != null)
                headers = h;
            Object rawBody = options.get("body");
            if (rawBody instanceof String s) {
                body = s.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            } else if (rawBody instanceof byte[] b) {
                body = b;
            }
            if (options.get("timeout") instanceof Number n)
                timeoutMs = n.longValue();
            if (options.get("async") instanceof Boolean b)
                async = b;
            if (options.get("onResponse") instanceof Function f)
                onResponse = f;
            if (options.get("onError") instanceof Function f)
                onError = f;
        }

        HttpRequest request;
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs));

            for (var entry : headers.entrySet()) {
                Object v = entry.getValue();
                if (v instanceof Object[] arr) {
                    for (Object item : arr)
                        builder.header(entry.getKey(), String.valueOf(item));
                } else {
                    builder.header(entry.getKey(), String.valueOf(v));
                }
            }

            HttpRequest.BodyPublisher bp;
            if (body != null && body.length > 0) {
                bp = HttpRequest.BodyPublishers.ofByteArray(body);
            } else {
                bp = HttpRequest.BodyPublishers.noBody();
            }
            builder.method(method, bp);
            request = builder.build();
        } catch (Exception e) {
            LOGGER.warn("HTTP fetch failed to build request for {}: {}", url, e.getMessage());
            if (async && onError != null) {
                final String errMsg = e.getMessage();
                final Function errCb = onError;
                Minecraft.getInstance().execute(() -> errCb.call(
                        org.mozilla.javascript.Context.getCurrentContext(),
                        errCb, errCb, new Object[]{errMsg}));
            }
            return async ? null : Response.error(e.getMessage());
        }

        if (async) {
            final String rt = responseType;
            final long mbs = maxBodySize;
            final Function onResp = onResponse;
            final Function onErr = onError;

            client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                    .thenAccept(response -> {
                        Response resp = new Response(response, rt, mbs);
                        Minecraft.getInstance().execute(() -> {
                            if (onResp != null) {
                                org.mozilla.javascript.Context cx =
                                        org.mozilla.javascript.Context.enter();
                                try {
                                    onResp.call(cx, onResp, onResp, new Object[]{resp});
                                } finally {
                                    org.mozilla.javascript.Context.exit();
                                }
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        Minecraft.getInstance().execute(() -> {
                            String msg = ex.getCause() instanceof HttpTimeoutException
                                    ? "Request timed out"
                                    : ex.getMessage();
                            if (onErr != null) {
                                org.mozilla.javascript.Context cx =
                                        org.mozilla.javascript.Context.enter();
                                try {
                                    onErr.call(cx, onErr, onErr, new Object[]{msg});
                                } finally {
                                    org.mozilla.javascript.Context.exit();
                                }
                            }
                        });
                        return null;
                    });
            return null;
        }

        try {
            HttpResponse<byte[]> response = client.send(request,
                    HttpResponse.BodyHandlers.ofByteArray());
            return new Response(response, responseType, maxBodySize);
        } catch (HttpTimeoutException e) {
            LOGGER.warn("HTTP fetch timed out after {}ms: {}", timeoutMs, url);
            return Response.timeout();
        } catch (IOException e) {
            LOGGER.warn("HTTP fetch failed for {}: {}", url, e.getMessage());
            return Response.error(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Response.error("Interrupted");
        }
    }

    public Response fetch(String url) {
        return fetch(url, null);
    }

    // ── Response ──

    public static class Response {
        private final int status;
        private final String statusText;
        private final Map<String, List<String>> headers;
        private final byte[] body;
        private final boolean ok;
        private final String errorMessage;
        private final boolean truncated;
        private Object parsedBody;

        Response(HttpResponse<byte[]> r, String responseType, long maxBodySize) {
            this.status = r.statusCode();
            this.statusText = switch (status) {
                case 200 -> "OK";
                case 201 -> "Created";
                case 204 -> "No Content";
                case 301 -> "Moved Permanently";
                case 302 -> "Found";
                case 304 -> "Not Modified";
                case 400 -> "Bad Request";
                case 401 -> "Unauthorized";
                case 403 -> "Forbidden";
                case 404 -> "Not Found";
                case 405 -> "Method Not Allowed";
                case 408 -> "Request Timeout";
                case 429 -> "Too Many Requests";
                case 500 -> "Internal Server Error";
                case 502 -> "Bad Gateway";
                case 503 -> "Service Unavailable";
                default -> "";
            };
            this.headers = r.headers().map();
            byte[] raw = r.body();
            if (maxBodySize > 0 && raw.length > maxBodySize) {
                this.body = new byte[(int) maxBodySize];
                System.arraycopy(raw, 0, this.body, 0, (int) maxBodySize);
                this.truncated = true;
            } else {
                this.body = raw;
                this.truncated = false;
            }
            this.ok = status >= 200 && status < 300;
            this.errorMessage = null;

            if (responseType != null && ok && body != null && body.length > 0) {
                switch (responseType) {
                    case "json" -> {
                        try { this.parsedBody = json(); } catch (Exception ignored) {}
                    }
                    case "text" -> this.parsedBody = text();
                    case "arrayBuffer" -> this.parsedBody = arrayBuffer();
                }
            }
        }

        private Response(int status, String statusText, String error) {
            this.status = status;
            this.statusText = statusText;
            this.headers = Collections.emptyMap();
            this.body = null;
            this.ok = false;
            this.errorMessage = error;
            this.truncated = false;
        }

        static Response timeout() {
            return new Response(408, "Request Timeout", "Request timed out");
        }

        static Response error(String msg) {
            return new Response(0, "Error", msg);
        }

        public int getStatus() { return status; }
        public String getStatusText() { return statusText; }
        public boolean getOk() { return ok; }
        public boolean getTruncated() { return truncated; }

        public Object getData() { return parsedBody; }

        public Map<String, Object> getHeaders() {
            Map<String, Object> flat = new LinkedHashMap<>();
            for (var entry : headers.entrySet()) {
                List<String> values = entry.getValue();
                if (values.size() == 1)
                    flat.put(entry.getKey(), values.get(0));
                else
                    flat.put(entry.getKey(), values.toArray(new String[0]));
            }
            return flat;
        }

        public String getHeader(String name) {
            List<String> values = headers.get(name);
            if (values == null || values.isEmpty()) return null;
            return values.get(0);
        }

        public Object json() {
            if (parsedBody instanceof Map || parsedBody instanceof List
                    || parsedBody instanceof Number || parsedBody instanceof Boolean)
                return parsedBody;
            if (body == null || body.length == 0) return null;
            String raw = new String(body, java.nio.charset.StandardCharsets.UTF_8);
            try {
                return org.mozilla.javascript.Context.getCurrentContext()
                        .evaluateString(org.mozilla.javascript.Context.getCurrentContext()
                                .initStandardObjects(), "(" + raw + ")", "json", 1, null);
            } catch (Exception e) {
                LOGGER.warn("Failed to parse HTTP response as JSON: {}", e.getMessage());
                return null;
            }
        }

        public String text() {
            if (body == null) return "";
            return new String(body, java.nio.charset.StandardCharsets.UTF_8);
        }

        public byte[] arrayBuffer() {
            return body != null ? body.clone() : new byte[0];
        }

        public String getErrorMessage() {
            return errorMessage != null ? errorMessage : "";
        }

        public void close() {}
    }
}
