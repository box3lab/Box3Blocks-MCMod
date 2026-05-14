package com.box3lab.box3js.script;

import org.mozilla.javascript.Context;

/**
 * Shared Rhino helpers for server and client script engines.
 */
public final class Box3Rhino {

    private Box3Rhino() {}

    @SuppressWarnings("deprecation")
    public static Context enterInterpretedContext() {
        Context cx = Context.enter();
        cx.setOptimizationLevel(-1);
        return cx;
    }
}
