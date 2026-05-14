package com.box3lab.box3js.client;

import com.box3lab.box3js.Box3JSNetwork;
import com.box3lab.box3js.script.Box3JSQueryResult;
import com.box3lab.box3js.script.Box3Rhino;
import com.box3lab.box3js.script.GameBounds3;
import com.box3lab.box3js.script.GameEventHandlerToken;
import com.box3lab.box3js.script.GameQuaternion;
import com.box3lab.box3js.script.GameRGBAColor;
import com.box3lab.box3js.script.GameRGBColor;
import com.box3lab.box3js.script.GameVector3;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mozilla.javascript.*;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.multiplayer.ServerData;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Singleton client-side Rhino engine.
 *
 * <p>Evaluates scripts received from the server via {@code ClientScriptPayload}
 * and exposes a {@code client} global for per-frame callbacks and UI overlays.
 *
 * <p>This class is only ever loaded on the physical client, because the sole
 * code path that references it is the payload-handler lambda registered in
 * {@code Box3JS.java}.  The JVM resolves lambda-method symbolic references
 * lazily, so the server classloader never touches this package.
 */
public class Box3JSClientEngine {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Box3JSClientEngine INSTANCE = new Box3JSClientEngine();

    // ── Rhino scope ──

    private ScriptableObject scope;
    private boolean initialized;

    // ── Callbacks ──

    private final List<Runnable> tickCallbacks = new CopyOnWriteArrayList<>();
    private final Map<String, List<Function>> clientEventHandlers = new ConcurrentHashMap<>();
    private final Map<String, List<Function>> keyPressHandlers = new ConcurrentHashMap<>();
    private final List<Function> chatMessageHandlers = new CopyOnWriteArrayList<>();
    private volatile boolean tickRegistered;
    private volatile boolean keyRegistered;
    private volatile boolean chatRegistered;
    private volatile boolean renderRegistered;
    private volatile boolean mouseRegistered;
    private final Map<Integer, DrawTextEntry> drawTexts = new ConcurrentHashMap<>();
    private final AtomicInteger drawTextIdCounter = new AtomicInteger(0);
    private final List<Function> mouseClickHandlers = new CopyOnWriteArrayList<>();
    private String currentProject = "";
    private Box3JSClientStorage storage;
    private Box3JSClientDatabase database;
    private Box3JSClientHttp http;
    private Box3JSGuiProxy activeGuiProxy;
    private volatile boolean dbWarningShown;

    private static final Map<String, Integer> KEY_MAP = new HashMap<>();
    static {
        // Letters a-z
        for (char c = 'a'; c <= 'z'; c++) {
            KEY_MAP.put(String.valueOf(c), InputConstants.KEY_A + (c - 'a'));
        }
        // Numbers 0-9
        for (int i = 0; i <= 9; i++) {
            KEY_MAP.put(String.valueOf(i), InputConstants.KEY_0 + i);
        }
        // Function keys f1-f12
        for (int i = 1; i <= 12; i++) {
            KEY_MAP.put("f" + i, InputConstants.KEY_F1 + (i - 1));
        }
        // Special keys
        KEY_MAP.put("space",           InputConstants.KEY_SPACE);
        KEY_MAP.put("enter",           InputConstants.KEY_RETURN);
        KEY_MAP.put("escape",          InputConstants.KEY_ESCAPE);
        KEY_MAP.put("tab",             InputConstants.KEY_TAB);
        KEY_MAP.put("backspace",       InputConstants.KEY_BACKSPACE);
        KEY_MAP.put("delete",          InputConstants.KEY_DELETE);
        KEY_MAP.put("left_shift",       InputConstants.KEY_LSHIFT);
        KEY_MAP.put("right_shift",      InputConstants.KEY_RSHIFT);
        KEY_MAP.put("shift",            InputConstants.KEY_LSHIFT);
        KEY_MAP.put("left_ctrl",        InputConstants.KEY_LCONTROL);
        KEY_MAP.put("right_ctrl",       InputConstants.KEY_RCONTROL);
        KEY_MAP.put("ctrl",             InputConstants.KEY_LCONTROL);
        KEY_MAP.put("left_alt",         InputConstants.KEY_LALT);
        KEY_MAP.put("right_alt",        InputConstants.KEY_RALT);
        KEY_MAP.put("alt",              InputConstants.KEY_LALT);
        KEY_MAP.put("up",              InputConstants.KEY_UP);
        KEY_MAP.put("down",            InputConstants.KEY_DOWN);
        KEY_MAP.put("left",            InputConstants.KEY_LEFT);
        KEY_MAP.put("right",           InputConstants.KEY_RIGHT);
    }

    public static Box3JSClientEngine get() { return INSTANCE; }

    private Box3JSClientEngine() {}

    public Box3JSGuiProxy getActiveGuiProxy() { return activeGuiProxy; }

    // ── Initialisation ──

    private void init() {
        if (initialized) return;

        Context cx = Box3Rhino.enterInterpretedContext();
        try {
            scope = cx.initStandardObjects();

            // -- console --------------------------------------------------
            ScriptableObject.putProperty(scope, "_jConsole",
                    Context.javaToJS(new Box3JSConsole(), scope));
            cx.evaluateString(scope, com.box3lab.box3js.script.Box3ScriptUtils.CONSOLE_INIT_JS,
                    "console-init", 1, null);

            // -- math types (same bindings as server engine) ---------------
            ScriptableObject.putProperty(scope, "GameVector3",
                    new NativeJavaClass(scope, GameVector3.class));
            ScriptableObject.putProperty(scope, "GameBounds3",
                    new NativeJavaClass(scope, GameBounds3.class));
            ScriptableObject.putProperty(scope, "GameRGBColor",
                    new NativeJavaClass(scope, GameRGBColor.class));
            ScriptableObject.putProperty(scope, "GameRGBAColor",
                    new NativeJavaClass(scope, GameRGBAColor.class));
            ScriptableObject.putProperty(scope, "GameQuaternion",
                    new NativeJavaClass(scope, GameQuaternion.class));
            ScriptableObject.putProperty(scope, "GameEventHandlerToken",
                    new NativeJavaClass(scope, GameEventHandlerToken.class));
            ScriptableObject.putProperty(scope, "Box3JSQueryResult",
                    new NativeJavaClass(scope, Box3JSQueryResult.class));

            // -- client global (lifecycle + server-bound actions) ----------
            ScriptableObject clientObj = (ScriptableObject) cx.newObject(scope);

            // client.onTick(callback)
            ScriptableObject.putProperty(clientObj, "onTick", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    if (args.length > 0 && args[0] instanceof Function fn) {
                        Runnable callback = () -> {
                            Context cx2 = Context.enter();
                            try {
                                fn.call(cx2, scope, scope, new Object[0]);
                            } catch (Exception e) {
                                LOGGER.error("Client tick callback error", e);
                            } finally {
                                Context.exit();
                            }
                        };
                        tickCallbacks.add(callback);
                        return new GameEventHandlerToken(() -> tickCallbacks.remove(callback));
                    }
                    return Undefined.instance;
                }
            });

            // client.getFPS()
            ScriptableObject.putProperty(clientObj, "getFPS", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    return Minecraft.getInstance().getFps();
                }
            });

            // client.getPlayer()
            ScriptableObject.putProperty(clientObj, "getPlayer", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    var player = Minecraft.getInstance().player;
                    if (player == null) return null;
                    Scriptable obj = cx.newObject(scope);
                    ScriptableObject.putProperty(obj, "name", player.getName().getString());
                    ScriptableObject.putProperty(obj, "uuid", player.getUUID().toString());
                    ScriptableObject.putProperty(obj, "health", player.getHealth());
                    ScriptableObject.putProperty(obj, "maxHealth", player.getMaxHealth());
                    ScriptableObject.putProperty(obj, "food", player.getFoodData().getFoodLevel());
                    ScriptableObject.putProperty(obj, "saturation", player.getFoodData().getSaturationLevel());
                    ScriptableObject.putProperty(obj, "xp", player.experienceLevel);
                    ScriptableObject.putProperty(obj, "dimension", player.level().dimension().location().toString());
                    var pos = player.position();
                    Scriptable objPos = cx.newObject(scope);
                    ScriptableObject.putProperty(objPos, "x", pos.x);
                    ScriptableObject.putProperty(objPos, "y", pos.y);
                    ScriptableObject.putProperty(objPos, "z", pos.z);
                    ScriptableObject.putProperty(obj, "position", objPos);
                    return obj;
                }
            });

            // client.getLookingAt()
            ScriptableObject.putProperty(clientObj, "getLookingAt", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    var hit = Minecraft.getInstance().hitResult;
                    if (hit == null || hit.getType() == HitResult.Type.MISS) return null;
                    Scriptable obj = cx.newObject(scope);
                    ScriptableObject.putProperty(obj, "type", hit.getType().name().toLowerCase());
                    var loc = hit.getLocation();
                    Scriptable objPos = cx.newObject(scope);
                    ScriptableObject.putProperty(objPos, "x", loc.x);
                    ScriptableObject.putProperty(objPos, "y", loc.y);
                    ScriptableObject.putProperty(objPos, "z", loc.z);
                    ScriptableObject.putProperty(obj, "position", objPos);
                    if (hit instanceof EntityHitResult ehr) {
                        Entity target = ehr.getEntity();
                        Scriptable objEnt = cx.newObject(scope);
                        ScriptableObject.putProperty(objEnt, "name", target.getName().getString());
                        ScriptableObject.putProperty(objEnt, "uuid", target.getUUID().toString());
                        ScriptableObject.putProperty(objEnt, "type", target.getType().getDescriptionId());
                        ScriptableObject.putProperty(obj, "entity", objEnt);
                    }
                    if (hit instanceof BlockHitResult bhr) {
                        var blockPos = bhr.getBlockPos();
                        Scriptable objBlockPos = cx.newObject(scope);
                        ScriptableObject.putProperty(objBlockPos, "x", blockPos.getX());
                        ScriptableObject.putProperty(objBlockPos, "y", blockPos.getY());
                        ScriptableObject.putProperty(objBlockPos, "z", blockPos.getZ());
                        ScriptableObject.putProperty(obj, "blockPos", objBlockPos);
                        ScriptableObject.putProperty(obj, "direction", bhr.getDirection().getName());
                    }
                    return obj;
                }
            });

            // client.getServerInfo()
            ScriptableObject.putProperty(clientObj, "getServerInfo", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    var serverData = Minecraft.getInstance().getCurrentServer();
                    if (serverData == null) {
                        // Singleplayer — return local info
                        Scriptable obj = cx.newObject(scope);
                        ScriptableObject.putProperty(obj, "ip", "localhost");
                        ScriptableObject.putProperty(obj, "name", "Singleplayer");
                        ScriptableObject.putProperty(obj, "isLocal", true);
                        return obj;
                    }
                    Scriptable obj = cx.newObject(scope);
                    ScriptableObject.putProperty(obj, "ip", serverData.ip);
                    ScriptableObject.putProperty(obj, "name", serverData.name);
                    ScriptableObject.putProperty(obj, "isLocal", false);
                    ScriptableObject.putProperty(obj, "playerCount", serverData.players != null ? serverData.players.online() : -1);
                    ScriptableObject.putProperty(obj, "maxPlayers", serverData.players != null ? serverData.players.max() : -1);
                    return obj;
                }
            });

            ScriptableObject.putProperty(scope, "client", clientObj);

            // -- audio global (sound playback) ------------------------------
            ScriptableObject audioObj = (ScriptableObject) cx.newObject(scope);

            // audio.playSound(path, volume, pitch)
            ScriptableObject.putProperty(audioObj, "playSound", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    if (args.length < 1) return Undefined.instance;
                    String path = args[0].toString();
                    float volume = args.length > 1 && args[1] instanceof Number n ? n.floatValue() : 1f;
                    float pitch = args.length > 2 && args[2] instanceof Number n ? n.floatValue() : 1f;
                    Minecraft.getInstance().execute(() -> {
                        var player = Minecraft.getInstance().player;
                        if (player == null) return;
                        var rl = net.minecraft.resources.ResourceLocation.tryParse(path);
                        if (rl == null) return;
                        var holder = BuiltInRegistries.SOUND_EVENT.getHolder(rl);
                        holder.ifPresent(h -> player.playNotifySound(h.value(), SoundSource.PLAYERS, volume, pitch));
                    });
                    return Undefined.instance;
                }
            });

            // audio.playMusic(path, volume, pitch)
            ScriptableObject.putProperty(audioObj, "playMusic", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    if (args.length < 1) return Undefined.instance;
                    String path = args[0].toString();
                    float volume = args.length > 1 && args[1] instanceof Number n ? n.floatValue() : 1f;
                    float pitch = args.length > 2 && args[2] instanceof Number n ? n.floatValue() : 1f;
                    Minecraft.getInstance().execute(() -> {
                        var player = Minecraft.getInstance().player;
                        if (player == null) return;
                        var rl = net.minecraft.resources.ResourceLocation.tryParse(path);
                        if (rl == null) return;
                        var holder = BuiltInRegistries.SOUND_EVENT.getHolder(rl);
                        holder.ifPresent(h -> player.playNotifySound(h.value(), SoundSource.MUSIC, volume, pitch));
                    });
                    return Undefined.instance;
                }
            });

            // audio.stopAll()
            ScriptableObject.putProperty(audioObj, "stopAll", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    Minecraft.getInstance().execute(() -> {
                        Minecraft.getInstance().getSoundManager().stop();
                    });
                    return Undefined.instance;
                }
            });

            // audio.getVolume(category)
            ScriptableObject.putProperty(audioObj, "getVolume", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    if (args.length < 1) return 0f;
                    SoundSource src = mapSoundCategory(args[0].toString());
                    if (src == null) return 0f;
                    return Minecraft.getInstance().options.getSoundSourceVolume(src);
                }
            });

            // audio.setVolume(category, value)
            ScriptableObject.putProperty(audioObj, "setVolume", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    if (args.length < 2) return Undefined.instance;
                    SoundSource src = mapSoundCategory(args[0].toString());
                    if (src == null) return Undefined.instance;
                    float value = args[1] instanceof Number n ? n.floatValue() : 1f;
                    Minecraft.getInstance().execute(() -> {
                        var options = Minecraft.getInstance().options;
                        options.getSoundSourceOptionInstance(src).set((double) value);
                        options.save();
                    });
                    return Undefined.instance;
                }
            });

            ScriptableObject.putProperty(scope, "audio", audioObj);

            // -- input global (keyboard) -----------------------------------
            ScriptableObject inputObj = (ScriptableObject) cx.newObject(scope);

            // input.isKeyDown(key)
            ScriptableObject.putProperty(inputObj, "isKeyDown", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    if (args.length < 1) return false;
                    String key = args[0].toString().toLowerCase();
                    Integer code = KEY_MAP.get(key);
                    if (code == null) return false;
                    long window = Minecraft.getInstance().getWindow().getWindow();
                    return InputConstants.isKeyDown(window, code);
                }
            });

            // input.onKeyPress(key, callback)
            ScriptableObject.putProperty(inputObj, "onKeyPress", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    if (args.length < 2 || !(args[0] instanceof String key)
                            || !(args[1] instanceof Function fn))
                        return Undefined.instance;
                    String lc = key.toLowerCase();
                    keyPressHandlers.computeIfAbsent(lc,
                            k -> new CopyOnWriteArrayList<>()).add(fn);
                    registerKeyListener();
                    return new GameEventHandlerToken(() -> {
                        List<Function> list = keyPressHandlers.get(lc);
                        if (list != null) list.remove(fn);
                    });
                }
            });

            // input.getMouseX()
            ScriptableObject.putProperty(inputObj, "getMouseX", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    return Minecraft.getInstance().mouseHandler.xpos();
                }
            });

            // input.getMouseY()
            ScriptableObject.putProperty(inputObj, "getMouseY", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    return Minecraft.getInstance().mouseHandler.ypos();
                }
            });

            // input.onMouseClick(callback) — returns GameEventHandlerToken
            ScriptableObject.putProperty(inputObj, "onMouseClick", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    if (args.length < 1 || !(args[0] instanceof Function fn))
                        return Undefined.instance;
                    mouseClickHandlers.add(fn);
                    registerMouseListener();
                    return new GameEventHandlerToken(() -> mouseClickHandlers.remove(fn));
                }
            });

            ScriptableObject.putProperty(scope, "input", inputObj);

            // -- ui global (screen overlays) --------------------------------
            ScriptableObject uiObj = (ScriptableObject) cx.newObject(scope);

            // ui.showOverlay(text)
            ScriptableObject.putProperty(uiObj, "showOverlay", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    if (args.length > 0 && args[0] instanceof String text) {
                        Minecraft.getInstance().execute(() -> {
                            var player = Minecraft.getInstance().player;
                            if (player != null) {
                                player.displayClientMessage(
                                        Component.literal(text), true);
                            }
                        });
                    }
                    return Undefined.instance;
                }
            });

            // ui.showTitle(title, subtitle, fadeIn?, stay?, fadeOut?)
            ScriptableObject.putProperty(uiObj, "showTitle", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    if (args.length < 2) return Undefined.instance;
                    String title = args[0] instanceof String s ? s : args[0].toString();
                    String subtitle = args[1] instanceof String s ? s : args[1].toString();
                    int fadeIn = args.length > 2 && args[2] instanceof Number n ? n.intValue() : 10;
                    int stay  = args.length > 3 && args[3] instanceof Number n ? n.intValue() : 70;
                    int fadeOut = args.length > 4 && args[4] instanceof Number n ? n.intValue() : 20;
                    Minecraft.getInstance().execute(() -> {
                        var conn = Minecraft.getInstance().getConnection();
                        if (conn != null) {
                            conn.setTitlesAnimation(
                                    new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
                            conn.setTitleText(
                                    new ClientboundSetTitleTextPacket(Component.literal(title)));
                            if (!subtitle.isEmpty()) {
                                conn.setSubtitleText(
                                        new ClientboundSetSubtitleTextPacket(Component.literal(subtitle)));
                            }
                        }
                    });
                    return Undefined.instance;
                }
            });

            // ui.showActionBar(text)
            ScriptableObject.putProperty(uiObj, "showActionBar", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    if (args.length > 0) {
                        String text = args[0] instanceof String s ? s : args[0].toString();
                        Minecraft.getInstance().execute(() -> {
                            var player = Minecraft.getInstance().player;
                            if (player != null) {
                                player.displayClientMessage(Component.literal(text), true);
                            }
                        });
                    }
                    return Undefined.instance;
                }
            });

            // ui.getScreenSize()
            ScriptableObject.putProperty(uiObj, "getScreenSize", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    var window = Minecraft.getInstance().getWindow();
                    Scriptable obj = cx.newObject(scope);
                    ScriptableObject.putProperty(obj, "width", window.getScreenWidth());
                    ScriptableObject.putProperty(obj, "height", window.getScreenHeight());
                    ScriptableObject.putProperty(obj, "scaledWidth", window.getGuiScaledWidth());
                    ScriptableObject.putProperty(obj, "scaledHeight", window.getGuiScaledHeight());
                    return obj;
                }
            });

            // ui.drawText(id, x, y, text, color?)
            ScriptableObject.putProperty(uiObj, "drawText", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    if (args.length < 4) return -1;
                    int id = args[0] instanceof Number n ? n.intValue() : drawTextIdCounter.incrementAndGet();
                    int x = ((Number) args[1]).intValue();
                    int y = ((Number) args[2]).intValue();
                    String text = args[3].toString();
                    int color = 0xFFFFFFFF;
                    if (args.length > 4 && args[4] instanceof NativeObject c) {
                        int r = ((Number) c.get("r", c)).intValue();
                        int g = ((Number) c.get("g", c)).intValue();
                        int b = ((Number) c.get("b", c)).intValue();
                        color = 0xFF000000 | (r << 16) | (g << 8) | b;
                    }
                    drawTexts.put(id, new DrawTextEntry(x, y, text, color));
                    registerRenderListener();
                    return id;
                }
            });

            // ui.removeDrawText(id)
            ScriptableObject.putProperty(uiObj, "removeDrawText", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    if (args.length < 1) return Undefined.instance;
                    int id = ((Number) args[0]).intValue();
                    drawTexts.remove(id);
                    return Undefined.instance;
                }
            });

            // ui.clearDrawTexts()
            ScriptableObject.putProperty(uiObj, "clearDrawTexts", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    drawTexts.clear();
                    return Undefined.instance;
                }
            });

            ScriptableObject.putProperty(scope, "ui", uiObj);

            // -- chat global (messaging) ------------------------------------
            ScriptableObject chatObj = (ScriptableObject) cx.newObject(scope);

            // chat.sendMessage(text)
            ScriptableObject.putProperty(chatObj, "sendMessage", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    if (args.length < 1) return Undefined.instance;
                    String text = args[0].toString();
                    Minecraft.getInstance().execute(() -> {
                        var conn = Minecraft.getInstance().getConnection();
                        if (conn != null) conn.sendChat(text);
                    });
                    return Undefined.instance;
                }
            });

            // chat.sendCommand(cmd)
            ScriptableObject.putProperty(chatObj, "sendCommand", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    if (args.length < 1) return Undefined.instance;
                    String cmd = args[0].toString();
                    Minecraft.getInstance().execute(() -> {
                        var conn = Minecraft.getInstance().getConnection();
                        if (conn != null) conn.sendCommand(cmd);
                    });
                    return Undefined.instance;
                }
            });

            // chat.onMessage(handler) — returns GameEventHandlerToken
            ScriptableObject.putProperty(chatObj, "onMessage", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    if (args.length < 1 || !(args[0] instanceof Function fn))
                        return Undefined.instance;
                    chatMessageHandlers.add(fn);
                    registerChatListener();
                    return new GameEventHandlerToken(() -> chatMessageHandlers.remove(fn));
                }
            });

            ScriptableObject.putProperty(scope, "chat", chatObj);

            // -- remoteChannel global ---------------------------------------
            ScriptableObject rcObj = (ScriptableObject) cx.newObject(scope);

            // remoteChannel.sendServerEvent(event)
            ScriptableObject.putProperty(rcObj, "sendServerEvent", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    if (args.length > 0) {
                        String json = stringify(cx, scope, args[0]);
                        if (json != null) {
                            PacketDistributor.sendToServer(
                                    new Box3JSNetwork.ClientEventPayload(currentProject, json));
                        }
                    }
                    return Undefined.instance;
                }
            });

            // remoteChannel.onClientEvent(handler)
            ScriptableObject.putProperty(rcObj, "onClientEvent", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    if (args.length > 0 && args[0] instanceof Function fn) {
                        String project = currentProject;
                        clientEventHandlers.computeIfAbsent(project,
                                k -> new CopyOnWriteArrayList<>()).add(fn);
                        return new GameEventHandlerToken(() -> {
                            List<Function> list = clientEventHandlers.get(project);
                            if (list != null) list.remove(fn);
                        });
                    }
                    return Undefined.instance;
                }
            });

            ScriptableObject.putProperty(scope, "remoteChannel", rcObj);

            // -- storage global ------------------------------------------
            storage = new Box3JSClientStorage(
                    Minecraft.getInstance().gameDirectory, currentProject);
            ScriptableObject.putProperty(scope, "storage",
                    Context.javaToJS(storage, scope));

            // -- db global ------------------------------------------------
            database = new Box3JSClientDatabase(
                    Minecraft.getInstance().gameDirectory);
            ScriptableObject dbObj = (ScriptableObject) cx.newObject(scope);
            ScriptableObject.putProperty(dbObj, "isAvailable", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    return Box3JSClientDatabase.isAvailable();
                }
            });
            ScriptableObject.putProperty(dbObj, "sql", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    try {
                        return database.sql(args);
                    } catch (RuntimeException e) {
                        if (!dbWarningShown) {
                            dbWarningShown = true;
                            String hint = e.getMessage() != null ? e.getMessage()
                                    : "db API unavailable — missing SQLite driver";
                            LOGGER.warn("db.sql() failed: {}", hint);
                            Minecraft.getInstance().execute(() -> {
                                var player = Minecraft.getInstance().player;
                                if (player != null) {
                                    player.displayClientMessage(
                                            Component.literal("§c[Box3JS] db unavailable — install minecraft-sqlite-jdbc mod"), false);
                                }
                            });
                        }
                        // Return empty safe result instead of crashing
                        return new Box3JSQueryResult(0);
                    }
                }
            });
            ScriptableObject.putProperty(scope, "db", dbObj);

            // -- http global ----------------------------------------------
            http = new Box3JSClientHttp();
            ScriptableObject httpObj = (ScriptableObject) cx.newObject(scope);
            ScriptableObject.putProperty(httpObj, "fetch", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    if (args.length < 1) return null;
                    String url = args[0].toString();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> options = args.length > 1 && args[1] instanceof Map
                            ? (Map<String, Object>) args[1] : null;
                    return http.fetch(url, options);
                }
            });
            ScriptableObject.putProperty(scope, "http", httpObj);

            // -- gui global -------------------------------------------------
            ScriptableObject guiObj = (ScriptableObject) cx.newObject(scope);

            ScriptableObject.putProperty(guiObj, "openGUI", new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope,
                                   Scriptable thisObj, Object[] args) {
                    if (args.length < 1 || !(args[0] instanceof NativeObject config))
                        return Undefined.instance;

                    String title = "Container";
                    if (config.containsKey("title")) title = config.get("title").toString();
                    int rows = 3;
                    if (config.containsKey("rows"))
                        rows = Math.max(1, Math.min(6, ((Number) config.get("rows")).intValue()));

                    // Build slotsJson manually
                    String slotsJson = "";
                    if (config.containsKey("slots") && config.get("slots") instanceof NativeObject slots
                            && slots.keySet().size() > 0) {
                        StringBuilder sb = new StringBuilder("{");
                        boolean first = true;
                        for (Object key : slots.keySet()) {
                            Object val = slots.get(key);
                            if (val == null || val == UniqueTag.NOT_FOUND) continue;
                            if (!first) sb.append(",");
                            first = false;
                            sb.append("\"").append(key).append("\":\"");
                            sb.append(val.toString().replace("\\", "\\\\").replace("\"", "\\\""));
                            sb.append("\"");
                        }
                        sb.append("}");
                        slotsJson = sb.toString();
                    }

                    Box3JSGuiProxy proxy = new Box3JSGuiProxy();
                    activeGuiProxy = proxy;

                    PacketDistributor.sendToServer(
                        new Box3JSNetwork.GUIServerboundPayload(0, title, rows, slotsJson,
                            0, "", 0, false, false));

                    return Context.javaToJS(proxy, scope);
                }
            });

            ScriptableObject.putProperty(scope, "gui", guiObj);

            // -- regex helpers (shared pure JS, from Box3ScriptUtils) ---------
            cx.evaluateString(scope, com.box3lab.box3js.script.Box3ScriptUtils.REGEX_HELPERS_JS,
                "regex-helpers", 1, null);

        } finally {
            Context.exit();
        }
        initialized = true;
    }

    // ── Script loading ──

    /**
     * Called from the network payload handler (netty thread, client-side only).
     */
    public void loadScript(String projectName, String scriptSource) {
        if (!initialized) init();

        Context cx = Box3Rhino.enterInterpretedContext();
        try {
            if (!projectName.equals(this.currentProject)) {
                this.currentProject = projectName;
                storage = new Box3JSClientStorage(
                        Minecraft.getInstance().gameDirectory, projectName);
                ScriptableObject.putProperty(scope, "storage",
                        Context.javaToJS(storage, scope));
                if (database != null) database.setProjectName(projectName);
            }

            // Register tick handler once, from the render thread.
            if (!tickRegistered) {
                Minecraft.getInstance().execute(() -> {
                    if (!tickRegistered) {
                        NeoForge.EVENT_BUS.addListener(
                                ClientTickEvent.Post.class,
                                event -> fireTick()
                        );
                        tickRegistered = true;
                        LOGGER.debug("Client tick handler registered");
                    }
                });
            }

            cx.evaluateString(scope, scriptSource,
                    "client/" + projectName, 1, null);
            LOGGER.info("Client script '{}' loaded", projectName);
        } catch (Exception e) {
            LOGGER.error("Failed to load client script '{}': {}",
                    projectName, e.getMessage());
        } finally {
            Context.exit();
        }
    }

    // ── Tick dispatch ──

    private void fireTick() {
        for (Runnable cb : tickCallbacks) {
            try {
                cb.run();
            } catch (Exception e) {
                LOGGER.error("Client tick callback error", e);
            }
        }
    }

    private void registerKeyListener() {
        if (keyRegistered) return;
        Minecraft.getInstance().execute(() -> {
            if (keyRegistered) return;
            NeoForge.EVENT_BUS.addListener(InputEvent.Key.class, event -> {
                if (event.getAction() != InputConstants.PRESS) return;
                int code = event.getKey();
                for (var entry : keyPressHandlers.entrySet()) {
                    Integer mapped = KEY_MAP.get(entry.getKey());
                    if (mapped != null && mapped == code) {
                        for (Function fn : entry.getValue()) {
                            Context cx = Context.enter();
                            try {
                                fn.call(cx, scope, scope, new Object[0]);
                            } catch (Exception e) {
                                LOGGER.error("Key press handler error", e);
                            } finally {
                                Context.exit();
                            }
                        }
                    }
                }
            });
            keyRegistered = true;
        });
    }

    private void registerChatListener() {
        if (chatRegistered) return;
        Minecraft.getInstance().execute(() -> {
            if (chatRegistered) return;
            NeoForge.EVENT_BUS.addListener(ClientChatReceivedEvent.class, event -> {
                if (chatMessageHandlers.isEmpty()) return;
                String message = event.getMessage().getString();
                String sender = event.getSender() != null
                        ? event.getSender().toString() : "";
                boolean isSystem = event.isSystem();
                for (Function fn : chatMessageHandlers) {
                    Context cx = Context.enter();
                    try {
                        Object result = fn.call(cx, scope, scope,
                                new Object[]{message, sender, isSystem});
                        if (result instanceof Boolean b && !b) {
                            event.setCanceled(true);
                        }
                    } catch (Exception e) {
                        LOGGER.error("Chat message handler error", e);
                    } finally {
                        Context.exit();
                    }
                }
            });
            chatRegistered = true;
        });
    }

    // ── Remote event dispatch ──

    /**
     * Called from the network payload handler when a {@code ServerEventPayload}
     * arrives (server → client). Dispatches to the render thread.
     */
    public void fireClientEvent(String projectName, long tick, String eventJson) {
        Minecraft.getInstance().execute(() -> {
            List<Function> handlers = clientEventHandlers.getOrDefault(projectName, List.of());
            if (handlers.isEmpty()) return;

            Context cx = Box3Rhino.enterInterpretedContext();
            try {
                scope.put("_arg", scope, eventJson);
                Object args = cx.evaluateString(scope,
                        "JSON.parse(_arg)", "json", 1, null);
                scope.delete("_arg");

                Scriptable eventObj = cx.newObject(scope);
                ScriptableObject.putProperty(eventObj, "tick", tick);
                ScriptableObject.putProperty(eventObj, "args", args);

                for (Function handler : handlers) {
                    try {
                        handler.call(cx, scope, scope, new Object[]{eventObj});
                    } catch (Exception e) {
                        LOGGER.error("Client event handler error: {}", e.getMessage());
                    }
                }
            } finally {
                Context.exit();
            }
        });
    }

    private static SoundSource mapSoundCategory(String name) {
        return switch (name.toLowerCase()) {
            case "master"  -> SoundSource.MASTER;
            case "music"   -> SoundSource.MUSIC;
            case "record"  -> SoundSource.RECORDS;
            case "weather" -> SoundSource.WEATHER;
            case "block"   -> SoundSource.BLOCKS;
            case "hostile" -> SoundSource.HOSTILE;
            case "neutral" -> SoundSource.NEUTRAL;
            case "player"  -> SoundSource.PLAYERS;
            case "ambient" -> SoundSource.AMBIENT;
            case "voice"   -> SoundSource.VOICE;
            default -> null;
        };
    }

    private static String stringify(Context cx, Scriptable scope, Object value) {
        return com.box3lab.box3js.script.Box3ScriptUtils.stringify(cx, scope, value);
    }

    // ── Render overlay for drawText ──

    private void registerRenderListener() {
        if (renderRegistered) return;
        Minecraft.getInstance().execute(() -> {
            if (renderRegistered) return;
            NeoForge.EVENT_BUS.addListener(RenderGuiEvent.Post.class, event -> {
                if (drawTexts.isEmpty()) return;
                GuiGraphics gfx = event.getGuiGraphics();
                var font = Minecraft.getInstance().font;
                for (DrawTextEntry entry : drawTexts.values()) {
                    gfx.drawString(font, entry.text(), entry.x(), entry.y(), entry.color());
                }
            });
            renderRegistered = true;
        });
    }

    // ── Mouse click listener ──

    private void registerMouseListener() {
        if (mouseRegistered) return;
        Minecraft.getInstance().execute(() -> {
            if (mouseRegistered) return;
            NeoForge.EVENT_BUS.addListener(InputEvent.MouseButton.class, event -> {
                if (mouseClickHandlers.isEmpty()) return;
                int button = event.getButton();
                int action = event.getAction();
                double x = Minecraft.getInstance().mouseHandler.xpos();
                double y = Minecraft.getInstance().mouseHandler.ypos();
                for (Function fn : mouseClickHandlers) {
                    Context cx = Context.enter();
                    try {
                        fn.call(cx, scope, scope, new Object[]{button, action, x, y});
                    } catch (Exception e) {
                        LOGGER.error("Mouse click handler error", e);
                    } finally {
                        Context.exit();
                    }
                }
            });
            mouseRegistered = true;
        });
    }

    // ── Draw text entry ──

    private record DrawTextEntry(int x, int y, String text, int color) {}

    // ── Console backend ──

    public static class Box3JSConsole {
        private void log(String level, Object... args) {
            StringBuilder sb = new StringBuilder();
            for (Object a : args) sb.append(a).append(' ');
            String msg = sb.toString().trim();
            switch (level) {
                case "debug" -> LOGGER.debug("[client] {}", msg);
                case "warn"  -> LOGGER.warn("[client] {}", msg);
                case "error" -> LOGGER.error("[client] {}", msg);
                default      -> LOGGER.info("[client] {}", msg);
            }
        }
        public void log(Object... args)   { log("info", args); }
        public void debug(Object... args) { log("debug", args); }
        public void warn(Object... args)  { log("warn", args); }
        public void error(Object... args) { log("error", args); }
        public void clear() {}
    }
}
