package com.box3lab.box3js.script;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.function.Supplier;

/**
 * Console backend exposed to server-side scripts.
 */
public class Box3JSConsole {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Supplier<String> projectName;

    public Box3JSConsole(Supplier<String> projectName) {
        this.projectName = projectName;
    }

    private String format(Object... args) {
        StringBuilder sb = new StringBuilder();
        String project = projectName.get();
        if (project != null && !project.isEmpty()) {
            sb.append('[').append(project).append("] ");
        }
        for (Object arg : args) {
            sb.append(arg).append(' ');
        }
        return sb.toString().trim();
    }

    public void log(Object... args) {
        LOGGER.info("[Box3JS] {}", format(args));
    }

    public void debug(Object... args) {
        LOGGER.debug("[Box3JS] {}", format(args));
    }

    public void warn(Object... args) {
        LOGGER.warn("[Box3JS] {}", format(args));
    }

    public void error(Object... args) {
        LOGGER.error("[Box3JS] {}", format(args));
    }

    public void clear() {
        // Server logs cannot be cleared reliably; keep console.clear() as a safe no-op.
    }
}
