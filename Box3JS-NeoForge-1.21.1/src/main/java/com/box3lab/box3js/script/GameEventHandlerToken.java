package com.box3lab.box3js.script;

public class GameEventHandlerToken {

    private boolean cancelled;
    private final Runnable onCancel;

    public GameEventHandlerToken(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    public void cancel() {
        if (!cancelled) {
            cancelled = true;
            onCancel.run();
        }
    }

    public void resume() {
        throw new UnsupportedOperationException("Resume is not supported — re-register the handler instead");
    }

    public boolean active() {
        return !cancelled;
    }
}
