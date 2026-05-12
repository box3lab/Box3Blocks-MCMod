package com.box3lab.box3js.script;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.*;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import org.mozilla.javascript.Function;
import org.slf4j.Logger;

/**
 * HTTP fetch API exposed to JS as the global {@code http} object.
 *
 * <p>Supports both synchronous (blocking) and async (callback-based) requests.
 * Async requests use {@link HttpClient#sendAsync} and deliver callbacks via
 * {@link MinecraftServer#execute}.
 */
public class Box3JSHttp {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final HttpClient client;
    private final MinecraftServer server;
    private Box3ScriptEngine engine;

    public Box3JSHttp(MinecraftServer server) {
        this.server = server;
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    void setEngine(Box3ScriptEngine engine) {
        this.engine = engine;
    }

    /**
     * Performs an HTTP request.
     *
     * <p>By default the request is synchronous (blocks the server tick).
     * Pass {@code async: true} and {@code onResponse} / {@code onError} callbacks
     * for non-blocking behaviour.
     *
     * @param url     the request URL
     * @param options optional JS object with keys:
     *                method, headers, body, timeout, responseType, maxBodySize,
     *                async, onResponse, onError
     * @return Response for sync, null for async
     */
    public Box3JSResponse fetch(String url, Map<String, Object> options) {
        // ── extract common options ──
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

        // ── build request ──
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
            if (async) {
                final String errMsg = e.getMessage();
                final Function errCb = onError;
                if (errCb != null) {
                    server.execute(() -> {
                        if (engine != null) engine.callFunction(errCb, errMsg);
                    });
                }
            }
            return async ? null : Box3JSResponse.error(e.getMessage());
        }

        // ── async path ──
        if (async) {
            final String rt = responseType;
            final long mbs = maxBodySize;
            final Function onResp = onResponse;
            final Function onErr = onError;

            client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                    .thenAccept(response -> {
                        Box3JSResponse resp = new Box3JSResponse(response, rt, mbs);
                        server.execute(() -> {
                            if (onResp != null && engine != null)
                                engine.callFunction(onResp, resp);
                        });
                    })
                    .exceptionally(ex -> {
                        server.execute(() -> {
                            String msg = ex.getCause() instanceof HttpTimeoutException
                                    ? "Request timed out"
                                    : ex.getMessage();
                            if (onErr != null && engine != null)
                                engine.callFunction(onErr, msg);
                        });
                        return null;
                    });
            return null;
        }

        // ── sync path ──
        try {
            HttpResponse<byte[]> response = client.send(request,
                    HttpResponse.BodyHandlers.ofByteArray());
            return new Box3JSResponse(response, responseType, maxBodySize);
        } catch (HttpTimeoutException e) {
            LOGGER.warn("HTTP fetch timed out after {}ms: {}", timeoutMs, url);
            return Box3JSResponse.timeout();
        } catch (IOException e) {
            LOGGER.warn("HTTP fetch failed for {}: {}", url, e.getMessage());
            return Box3JSResponse.error(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Box3JSResponse.error("Interrupted");
        }
    }

    public Box3JSResponse fetch(String url) {
        return fetch(url, null);
    }

}
