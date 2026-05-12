package com.box3lab.box3js.client;

import com.box3lab.box3js.Box3JSNetwork;
import com.box3lab.box3js.script.Box3JSQueryResult;
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
    private String currentProject = "";
    private Box3JSClientStorage storage;
    private Box3JSClientDatabase database;
    private Box3JSClientHttp http;
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
        KEY_MAP.put("left_ctrl",        InputConstants.KEY_LCONTROL);
        KEY_MAP.put("right_ctrl",       InputConstants.KEY_RCONTROL);
        KEY_MAP.put("left_alt",         InputConstants.KEY_LALT);
        KEY_MAP.put("right_alt",        InputConstants.KEY_RALT);
        KEY_MAP.put("up",              InputConstants.KEY_UP);
        KEY_MAP.put("down",            InputConstants.KEY_DOWN);
        KEY_MAP.put("left",            InputConstants.KEY_LEFT);
        KEY_MAP.put("right",           InputConstants.KEY_RIGHT);
    }

    public static Box3JSClientEngine get() { return INSTANCE; }

    private Box3JSClientEngine() {}

    // ── Initialisation ──

    private void init() {
        if (initialized) return;

        Context cx = Context.enter();
        cx.setOptimizationLevel(-1);
        try {
            scope = cx.initStandardObjects();

            // -- console --------------------------------------------------
            ScriptableObject.putProperty(scope, "_jConsole",
                    Context.javaToJS(new Box3JSClientConsole(), scope));
            cx.evaluateString(scope, """
                    var console = {
                        log: function() {
                            var msg = [];
                            for (var i = 0; i < arguments.length; i++)
                                msg.push(String(arguments[i]));
                            _jConsole.log(msg.join(' '));
                        },
                        debug: function() {
                            var msg = [];
                            for (var i = 0; i < arguments.length; i++)
                                msg.push(String(arguments[i]));
                            _jConsole.debug(msg.join(' '));
                        },
                        warn: function() {
                            var msg = [];
                            for (var i = 0; i < arguments.length; i++)
                                msg.push(String(arguments[i]));
                            _jConsole.warn(msg.join(' '));
                        },
                        error: function() {
                            var msg = [];
                            for (var i = 0; i < arguments.length; i++)
                                msg.push(String(arguments[i]));
                            _jConsole.error(msg.join(' '));
                        },
                        clear: function() {},
                        assert: function(condition) {
                            if (!condition) {
                                var msg = ['Assertion failed:'];
                                for (var i = 1; i < arguments.length; i++)
                                    msg.push(String(arguments[i]));
                                _jConsole.error(msg.join(' '));
                            }
                        }
                    };
                    """, "console-init", 1, null);

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
                        tickCallbacks.add(() -> {
                            Context cx2 = Context.enter();
                            try {
                                fn.call(cx2, scope, scope, new Object[0]);
                            } catch (Exception e) {
                                LOGGER.error("Client tick callback error", e);
                            } finally {
                                Context.exit();
                            }
                        });
                    }
                    return Undefined.instance;
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

            // -- regex helpers (pure JS, mirrored from server engine) ----
            cx.evaluateString(scope,
                "(function(){" +
                "function isSp(c){return c==' '||c=='\\t'||c=='\\n'||c=='\\r'||c=='\\f'||c=='\\v';}" +
                "function isDi(c){return c>='0'&&c<='9';}" +
                "function isWo(c){return(c>='a'&&c<='z')||(c>='A'&&c<='Z')||(c>='0'&&c<='9')||c=='_';}" +
                "function parse(p,f){" +
                "var a=[];var i=0;var ic=f.indexOf('i')>=0;" +
                "while(i<p.length){" +
                "var ch=p.charAt(i);var m;" +
                "if(ch=='\\\\'){" +
                "i++;var e=p.charAt(i);" +
                "if(e=='s')m=isSp;" +
                "else if(e=='S')m=function(c){return !isSp(c);};" +
                "else if(e=='d')m=isDi;" +
                "else if(e=='D')m=function(c){return !isDi(c);};" +
                "else if(e=='w')m=isWo;" +
                "else if(e=='W')m=function(c){return !isWo(c);};" +
                "else m=function(c){return c==e;};" +
                "i++;" +
                "}else if(ch=='.'){" +
                "m=function(c){return c!='\\n'&&c!='\\r';};i++;" +
                "}else if(ch=='['){" +
                "i++;var ne=false;if(p.charAt(i)=='^'){ne=true;i++;}" +
                "var cs='';" +
                "while(i<p.length&&p.charAt(i)!=']'){" +
                "if(p.charAt(i)=='\\\\'){" +
                "i++;var e2=p.charAt(i);" +
                "if(e2=='s')cs+=' \\t\\n\\r\\f\\v';" +
                "else if(e2=='d')cs+='0123456789';" +
                "else if(e2=='w')cs+='abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_';" +
                "else cs+=e2;" +
                "}else if(p.charAt(i)=='-'&&i+1<p.length&&p.charAt(i+1)!=']'){" +
                "var sc=p.charCodeAt(i-1);var ec=p.charCodeAt(i+1);" +
                "for(var cc=sc+1;cc<=ec;cc++)cs+=String.fromCharCode(cc);" +
                "i+=2;continue;" +
                "}else{cs+=p.charAt(i);}" +
                "i++;" +
                "}" +
                "i++;" +
                "if(ne)m=function(c){return cs.indexOf(c)<0;};" +
                "else m=function(c){return cs.indexOf(c)>=0;};" +
                "}else{" +
                "var lit=ch;var low=ic?lit.toLowerCase():lit;var up=ic?lit.toUpperCase():lit;" +
                "if(ic)m=function(c){return c==low||c==up;};" +
                "else m=function(c){return c==lit;};" +
                "i++;" +
                "}" +
                "var min=1,max=1;" +
                "if(i<p.length){" +
                "var q=p.charAt(i);" +
                "if(q=='+'){min=1;max=-1;i++;}" +
                "else if(q=='*'){min=0;max=-1;i++;}" +
                "else if(q=='?'){min=0;max=1;i++;}" +
                "}" +
                "a.push({m:m,min:min,max:max});" +
                "}" +
                "return a;" +
                "}" +
                "function matchAt(s,a,pos){" +
                "var p=pos;" +
                "for(var ai=0;ai<a.length;ai++){" +
                "var at=a[ai];var cnt=0;" +
                "while(p<s.length&&at.m(s.charAt(p))){" +
                "cnt++;p++;if(at.max>=0&&cnt>=at.max)break;" +
                "}" +
                "if(cnt<at.min)return -1;" +
                "}" +
                "return p-pos;" +
                "}" +
                "function findNext(s,a,pos){" +
                "for(var i=pos;i<s.length;i++){" +
                "var len=matchAt(s,a,i);" +
                "if(len>0)return {index:i,length:len};" +
                "}" +
                "return null;" +
                "}" +
                "function findAll(s,a){" +
                "var ms=[];var pos=0;" +
                "while(pos<s.length){" +
                "var m=findNext(s,a,pos);" +
                "if(!m)break;" +
                "ms.push(m);pos=m.index+m.length;" +
                "if(m.length===0)pos++;" +
                "}" +
                "return ms;" +
                "}" +
                "var _ref={parse:parse,findNext:findNext,findAll:findAll};" +
                "__regexSplit=function(s,p,f){" +
                "var a=_ref.parse(p,f||'');var r=[];var pos=0;" +
                "var ms=_ref.findAll(s,a);" +
                "for(var i=0;i<ms.length;i++){" +
                "var m=ms[i];r.push(s.substring(pos,m.index));" +
                "pos=m.index+m.length;" +
                "}" +
                "r.push(s.substring(pos));return r;" +
                "};" +
                "__regexMatch=function(s,p,f){" +
                "var a=_ref.parse(p,f||'');" +
                "var m=_ref.findNext(s,a,0);" +
                "if(!m)return null;" +
                "var r=[s.substring(m.index,m.index+m.length)];" +
                "r.index=m.index;r.input=s;return r;" +
                "};" +
                "__regexReplace=function(s,p,f,rp){" +
                "var a=_ref.parse(p,f||'');" +
                "var gl=(f||'').indexOf('g')>=0;" +
                "var ms=_ref.findAll(s,a);var rs='';var pos=0;" +
                "for(var i=0;i<ms.length;i++){" +
                "var m=ms[i];rs+=s.substring(pos,m.index);" +
                "if(typeof rp==='function')rs+=rp(s.substring(m.index,m.index+m.length));" +
                "else rs+=rp;" +
                "pos=m.index+m.length;if(!gl)break;" +
                "}" +
                "rs+=s.substring(pos);return rs;" +
                "};" +
                "__regexTest=function(p,f,s){" +
                "var a=_ref.parse(p,f||'');" +
                "return _ref.findNext(s,a,0)!==null;" +
                "};" +
                "__regexExec=function(p,f,s){" +
                "return __regexMatch(s,p,f);" +
                "};" +
                "})();",
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

        Context cx = Context.enter();
        cx.setOptimizationLevel(-1);
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

            Context cx = Context.enter();
            cx.setOptimizationLevel(-1);
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
        try {
            scope.put("_arg", scope, value);
            Object result = cx.evaluateString(scope,
                    "JSON.stringify(_arg)", "json", 1, null);
            scope.delete("_arg");
            return result instanceof String s ? s : null;
        } catch (Exception e) {
            LOGGER.error("Failed to stringify event", e);
            return null;
        }
    }

    // ── Console backend ──

    public static class Box3JSClientConsole {
        public void log(String msg)   { LOGGER.info("[client] {}", msg); }
        public void debug(String msg) { LOGGER.debug("[client] {}", msg); }
        public void warn(String msg)  { LOGGER.warn("[client] {}", msg); }
        public void error(String msg) { LOGGER.error("[client] {}", msg); }
    }
}
