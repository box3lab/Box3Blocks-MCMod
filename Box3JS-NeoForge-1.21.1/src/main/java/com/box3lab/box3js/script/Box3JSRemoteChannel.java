package com.box3lab.box3js.script;

import com.box3lab.box3js.Box3JSNetwork;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.mozilla.javascript.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-side remote event channel exposed as {@code remoteChannel} global.
 *
 * <p>Bridges JS {@code sendClientEvent / broadcastClientEvent / onServerEvent}
 * calls to NeoForge custom payloads.  JSON-serialises event data so any
 * Rhino value can cross the network boundary.
 */
public class Box3JSRemoteChannel {

    private final Box3ScriptEngine engine;

    private static final ResourceLocation SERVER_EVENT_ID =
            ResourceLocation.fromNamespaceAndPath("box3js", "server_event");

    Box3JSRemoteChannel(Box3ScriptEngine engine) {
        this.engine = engine;
    }

    // ── sendClientEvent(entities, event) ──

    public void sendClientEvent(Object entities, Object event) {
        String project = engine.getCurrentProject();
        long tick = engine.getCurrentTick();
        String json = stringify(event);
        if (json == null) return;

        var payload = new Box3JSNetwork.ServerEventPayload(project, tick, json);

        for (ServerPlayer sp : resolvePlayers(entities)) {
            if (NetworkRegistry.hasChannel(
                    sp.connection.getConnection(),
                    ConnectionProtocol.PLAY,
                    SERVER_EVENT_ID)) {
                PacketDistributor.sendToPlayer(sp, payload);
            }
        }
    }

    // ── broadcastClientEvent(event) ──

    public void broadcastClientEvent(Object event) {
        String project = engine.getCurrentProject();
        long tick = engine.getCurrentTick();
        String json = stringify(event);
        if (json == null) return;

        var payload = new Box3JSNetwork.ServerEventPayload(project, tick, json);

        for (ServerPlayer sp : engine.getServer().getPlayerList().getPlayers()) {
            if (NetworkRegistry.hasChannel(
                    sp.connection.getConnection(),
                    ConnectionProtocol.PLAY,
                    SERVER_EVENT_ID)) {
                PacketDistributor.sendToPlayer(sp, payload);
            }
        }
    }

    // ── onServerEvent(handler) ──

    public Object onServerEvent(Function handler) {
        String project = engine.getCurrentProject();
        Function stored = engine.bus.addServerEventHandler(project, handler);
        return new GameEventHandlerToken(() ->
                engine.bus.removeServerEventHandler(project, stored));
    }

    // ── Helpers ──

    private String stringify(Object value) {
        Context cx = Context.enter();
        try {
            return Box3ScriptUtils.stringify(cx, engine.getScope(), value);
        } finally {
            Context.exit();
        }
    }

    /**
     * Called by the engine when a {@code ClientEventPayload} arrives from a client.
     */
    void fireFromClient(Box3JSPlayer entity, long tick, String eventJson) {
        Object args = parse(eventJson);
        if (args == null) return;

        String project = engine.getCurrentProject();
        Context cx = Context.enter();
        try {
            Scriptable eventObj = cx.newObject(engine.getScope());
            ScriptableObject.putProperty(eventObj, "tick", tick);
            ScriptableObject.putProperty(eventObj, "entity",
                    Context.javaToJS(entity, engine.getScope()));
            ScriptableObject.putProperty(eventObj, "args", args);

            for (Function handler : engine.bus.getServerEventHandlers(project)) {
                try {
                    handler.call(cx, engine.getScope(), engine.getScope(),
                            new Object[]{eventObj});
                } catch (Exception e) {
                    engine.reportError("ServerEventHandler: " + e.getMessage());
                }
            }
        } finally {
            Context.exit();
        }
    }

    private Object parse(String json) {
        Context cx = Context.enter();
        try {
            Scriptable scope = engine.getScope();
            scope.put("_arg", scope, json);
            Object result = cx.evaluateString(scope,
                    "JSON.parse(_arg)", "json", 1, null);
            scope.delete("_arg");
            return result;
        } finally {
            Context.exit();
        }
    }

    @SuppressWarnings("unchecked")
    private List<ServerPlayer> resolvePlayers(Object arg) {
        List<ServerPlayer> result = new ArrayList<>();
        if (arg instanceof NativeArray na) {
            for (Object item : na) {
                if (item instanceof Box3JSPlayer gpe && gpe.getPlayer() != null)
                    result.add(gpe.getPlayer());
            }
        } else if (arg instanceof Box3JSPlayer gpe && gpe.getPlayer() != null) {
            result.add(gpe.getPlayer());
        }
        return result;
    }
}
