package com.box3lab.box3js.script;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * HTTP response value object shared by server and client HTTP APIs.
 * Exposed to JS via {@code http.fetch()}.
 */
public class Box3JSResponse {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final int status;
    private final String statusText;
    private final Map<String, List<String>> headers;
    private final byte[] body;
    private final boolean ok;
    private final String errorMessage;
    private final boolean truncated;
    private Object parsedBody;

    public Box3JSResponse(HttpResponse<byte[]> r, String responseType, long maxBodySize) {
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
                case "json" -> this.parsedBody = json();
                case "text" -> this.parsedBody = text();
                case "arrayBuffer" -> this.parsedBody = arrayBuffer();
            }
        }
    }

    private Box3JSResponse(int status, String statusText, String error) {
        this.status = status;
        this.statusText = statusText;
        this.headers = Collections.emptyMap();
        this.body = null;
        this.ok = false;
        this.errorMessage = error;
        this.truncated = false;
    }

    public static Box3JSResponse timeout() {
        return new Box3JSResponse(408, "Request Timeout", "Request timed out");
    }

    public static Box3JSResponse error(String msg) {
        return new Box3JSResponse(0, "Error", msg);
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
        String raw = new String(body, StandardCharsets.UTF_8);
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
        return new String(body, StandardCharsets.UTF_8);
    }

    public byte[] arrayBuffer() {
        return body != null ? body.clone() : new byte[0];
    }

    public String getErrorMessage() {
        return errorMessage != null ? errorMessage : "";
    }

    public void close() {}
}
