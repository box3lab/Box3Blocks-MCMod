package com.box3lab.box3js.script;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;

public class Box3ScriptUtils {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Regex helpers backed by {@code java.util.regex.Pattern} and {@code java.util.regex.Matcher}.
     * Rhino can't load {@code NativeRegExp} (Minecraft classloader isolation), so we delegate
     * to the JDK's own regex engine via Rhino's Java interop.
     *
     * <p>The Babel build step converts every regex literal into a call to one of these helpers,
     * so changing the implementation here fixes regex for all existing scripts.</p>
     */
    public static final String REGEX_HELPERS_JS =
        "(function(){" +
        "var Pattern = java.util.regex.Pattern;" +
        "var Matcher = java.util.regex.Matcher;" +
        "function flagsToBits(f){" +
        "  var bits=0;" +
        "  if(!f)return bits;" +
        "  if(f.indexOf('i')>=0)bits|=Pattern.CASE_INSENSITIVE;" +
        "  if(f.indexOf('m')>=0)bits|=Pattern.MULTILINE;" +
        "  if(f.indexOf('s')>=0)bits|=Pattern.DOTALL;" +
        "  return bits;" +
        "}" +
        "__regexTest=function(p,f,s){" +
        "  return Pattern.compile(p,flagsToBits(f)).matcher(s).find();" +
        "};" +
        "__regexExec=function(p,f,s){" +
        "  var m=Pattern.compile(p,flagsToBits(f)).matcher(s);" +
        "  if(!m.find())return null;" +
        "  var gc=m.groupCount();" +
        "  var r=[m.group()];" +
        "  for(var i=1;i<=gc;i++)r.push(m.group(i));" +
        "  r.index=m.start();" +
        "  r.input=s;" +
        "  return r;" +
        "};" +
        "__regexMatch=function(s,p,f){" +
        "  return __regexExec(p,f,s);" +
        "};" +
        "__regexSplit=function(s,p,f){" +
        "  var arr=Pattern.compile(p,flagsToBits(f)).split(s);" +
        "  var r=[];" +
        "  for(var i=0;i<arr.length;i++)r.push(String(arr[i]));" +
        "  return r;" +
        "};" +
        "__regexReplace=function(s,p,f,rp){" +
        "  var m=Pattern.compile(p,flagsToBits(f)).matcher(s);" +
        "  if(typeof rp==='function'){" +
        "    var sb=new java.lang.StringBuffer();" +
        "    while(m.find()){" +
        "      var rep=String(rp(m.group()));" +
        "      m.appendReplacement(sb,Matcher.quoteReplacement(rep));" +
        "    }" +
        "    m.appendTail(sb);" +
        "    return sb.toString();" +
        "  }" +
        "  return m.replaceAll(String(rp||''));" +
        "};" +
        "})();";

    /**
     * Shared JS snippet that creates the {@code console} object by forwarding
     * all method calls to the Java {@code _jConsole} backend via {@code .apply()}.
     * Used by both the server and client Rhino engines.
     */
    public static final String CONSOLE_INIT_JS =
        "console = {" +
        "  log: function() { return _jConsole.log.apply(_jConsole, arguments); }," +
        "  debug: function() { return _jConsole.debug.apply(_jConsole, arguments); }," +
        "  warn: function() { return _jConsole.warn.apply(_jConsole, arguments); }," +
        "  error: function() { return _jConsole.error.apply(_jConsole, arguments); }," +
        "  clear: function() { return _jConsole.clear.apply(_jConsole, arguments); }," +
        "  assert: function(a) {" +
        "    if (!a) {" +
        "      var b = [];" +
        "      for (var i = 1; i < arguments.length; i++) b.push(arguments[i]);" +
        "      _jConsole.error(b.length ? b : ['Assertion failed']);" +
        "    }" +
        "  }" +
        "};";

    public static Item lookupItem(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return null;
        return BuiltInRegistries.ITEM.getOptional(rl).orElse(null);
    }

    public static EntityType<?> lookupEntityType(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return null;
        return BuiltInRegistries.ENTITY_TYPE.getOptional(rl).orElse(null);
    }

    public static Block lookupBlock(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return null;
        return BuiltInRegistries.BLOCK.getOptional(rl).orElse(null);
    }

    public static Holder<MobEffect> lookupMobEffect(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return null;
        return BuiltInRegistries.MOB_EFFECT.getHolder(rl).orElse(null);
    }

    public static Holder<SoundEvent> lookupSoundEvent(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return null;
        return BuiltInRegistries.SOUND_EVENT.getHolder(rl).orElse(null);
    }

    public static ParticleOptions lookupParticle(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return null;
        var type = BuiltInRegistries.PARTICLE_TYPE.getOptional(rl);
        if (type.isEmpty()) return null;
        try {
            return (ParticleOptions) type.get();
        } catch (ClassCastException ignored) {
            return null;
        }
    }

    public static Holder<Attribute> lookupAttribute(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return null;
        return BuiltInRegistries.ATTRIBUTE.getHolder(rl).orElse(null);
    }

    public static boolean coerceBool(Object v) {
        return v instanceof Boolean b ? b : Boolean.parseBoolean(v.toString());
    }

    static String resolveScoreName(Object entityOrName) {
        if (entityOrName instanceof String s) return s;
        if (entityOrName instanceof Box3JSEntity e) return e.getEntity().getScoreboardName();
        if (entityOrName instanceof ServerPlayer sp) return sp.getScoreboardName();
        return null;
    }

    public static void lookAt(Entity entity, double x, double y, double z) {
        double dx = x - entity.getX();
        double dy = y - entity.getEyeY();
        double dz = z - entity.getZ();
        double hd = Math.sqrt(dx * dx + dz * dz);
        entity.setYRot((float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
        entity.setXRot((float) (-Math.toDegrees(Math.atan2(dy, hd))));
    }

    /**
     * JSON-stringify a Rhino value. Shared by server (RemoteChannel) and client engine.
     */
    public static String stringify(Context cx, Scriptable scope, Object value) {
        try {
            scope.put("_arg", scope, value);
            Object result = cx.evaluateString(scope,
                    "JSON.stringify(_arg)", "json", 1, null);
            scope.delete("_arg");
            return result instanceof String s ? s : null;
        } catch (Exception e) {
            LOGGER.error("Failed to stringify value", e);
            return null;
        }
    }
}
