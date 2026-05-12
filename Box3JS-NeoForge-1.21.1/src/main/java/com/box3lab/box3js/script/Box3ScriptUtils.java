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
     * Pure-JS regex helpers shared between server and client Rhino engines.
     * Rhino can't load NativeRegExp in the MC classloader, so we provide
     * a minimal regex implementation in pure JavaScript.
     */
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

    public static final String REGEX_HELPERS_JS =
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
        "})();";

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
