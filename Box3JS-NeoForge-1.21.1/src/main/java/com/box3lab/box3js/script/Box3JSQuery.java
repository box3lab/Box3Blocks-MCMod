package com.box3lab.box3js.script;

import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

class Box3JSQuery {

    private final MinecraftServer server;
    private final Box3ScriptEngine engine;

    Box3JSQuery(MinecraftServer server, Box3ScriptEngine engine) {
        this.server = server;
        this.engine = engine;
    }

    List<Box3JSEntity> querySelectorAll(String selector) {
        List<Box3JSEntity> result = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Box3JSEntity e = new Box3JSEntity(player, server, engine);
            if (matchesSelector(e, selector)) result.add(e);
        }
        return result;
    }

    Box3JSEntity querySelector(String selector) {
        List<Box3JSEntity> all = querySelectorAll(selector);
        return all.isEmpty() ? null : all.get(0);
    }

    private static boolean matchesSelector(Box3JSEntity entity, String selector) {
        if (selector.equals("*") || selector.equals("player")) return entity.isPlayer();
        if (selector.startsWith("#")) return selector.substring(1).equals(entity.getId());
        if (selector.startsWith(".")) return entity.hasTag(selector.substring(1));
        return false;
    }

    Object raycast(GameVector3 origin, GameVector3 direction) {
        return raycast(origin, direction, 5.0);
    }

    Object raycast(GameVector3 origin, GameVector3 direction, double maxDistance) {
        ServerLevel level = server.overworld();
        Vec3 start = new Vec3(origin.x, origin.y, origin.z);
        double len = Math.sqrt(direction.x * direction.x + direction.y * direction.y + direction.z * direction.z);
        if (len < 0.0001) {
            NativeObject result = new NativeObject();
            ScriptableObject.putProperty(result, "hit", false);
            return result;
        }
        Vec3 dir = new Vec3(direction.x / len, direction.y / len, direction.z / len);
        Vec3 end = start.add(dir.scale(maxDistance));
        ClipContext ctx = new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, net.minecraft.world.phys.shapes.CollisionContext.empty());
        BlockHitResult blockHit = level.clip(ctx);
        AABB searchBox = new AABB(start, end).inflate(1.0);
        Entity closestEntity = null;
        Vec3 entityHitPos = null;
        double closestEntDistSqr = maxDistance * maxDistance;
        for (Entity e : level.getEntities((Entity) null, searchBox, e2 -> true)) {
            var hit = e.getBoundingBox().clip(start, end);
            if (hit.isPresent()) {
                double dSqr = start.distanceToSqr(hit.get());
                if (dSqr < closestEntDistSqr) {
                    closestEntDistSqr = dSqr;
                    closestEntity = e;
                    entityHitPos = hit.get();
                }
            }
        }
        double blockDistSqr = blockHit.getType() != HitResult.Type.MISS
            ? start.distanceToSqr(blockHit.getLocation()) : Double.MAX_VALUE;
        NativeObject result = new NativeObject();
        if (closestEntity != null && closestEntDistSqr < blockDistSqr) {
            ScriptableObject.putProperty(result, "hit", true);
            ScriptableObject.putProperty(result, "x", entityHitPos.x);
            ScriptableObject.putProperty(result, "y", entityHitPos.y);
            ScriptableObject.putProperty(result, "z", entityHitPos.z);
            ScriptableObject.putProperty(result, "normalX", 0);
            ScriptableObject.putProperty(result, "normalY", 0);
            ScriptableObject.putProperty(result, "normalZ", 0);
            ScriptableObject.putProperty(result, "distance", Math.sqrt(closestEntDistSqr));
            ScriptableObject.putProperty(result, "entity", new Box3JSEntity(closestEntity, server, engine));
        } else if (blockHit.getType() != HitResult.Type.MISS) {
            Vec3 pos = blockHit.getLocation();
            ScriptableObject.putProperty(result, "hit", true);
            ScriptableObject.putProperty(result, "x", pos.x);
            ScriptableObject.putProperty(result, "y", pos.y);
            ScriptableObject.putProperty(result, "z", pos.z);
            Direction face = blockHit.getDirection();
            ScriptableObject.putProperty(result, "normalX", face.getStepX());
            ScriptableObject.putProperty(result, "normalY", face.getStepY());
            ScriptableObject.putProperty(result, "normalZ", face.getStepZ());
            ScriptableObject.putProperty(result, "distance", Math.sqrt(blockDistSqr));
            ScriptableObject.putProperty(result, "entity", null);
            BlockPos bp = blockHit.getBlockPos();
            ScriptableObject.putProperty(result, "voxel", engine.getVoxelsBinding().getId(level.getBlockState(bp)));
        } else {
            ScriptableObject.putProperty(result, "hit", false);
        }
        return result;
    }

    List<Box3JSEntity> entitiesInArea(GameVector3 pos1, GameVector3 pos2) {
        AABB aabb = new AABB(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z);
        List<Box3JSEntity> result = new ArrayList<>();
        for (Entity e : server.overworld().getEntities((Entity) null, aabb, e2 -> true)) {
            result.add(new Box3JSEntity(e, server, engine));
        }
        return result;
    }

    List<Box3JSEntity> entitiesInRadius(double x, double y, double z, double radius) {
        AABB aabb = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
        List<Box3JSEntity> result = new ArrayList<>();
        for (Entity e : server.overworld().getEntities((Entity) null, aabb, e2 -> true)) {
            result.add(new Box3JSEntity(e, server, engine));
        }
        return result;
    }

    List<Box3JSEntity> entitiesInRadius(GameVector3 pos, double radius) {
        return entitiesInRadius(pos.x, pos.y, pos.z, radius);
    }

    String getBiome(int x, int y, int z) {
        Holder<Biome> biome = server.overworld().getBiome(new BlockPos(x, y, z));
        var key = biome.unwrapKey();
        return key.map(k -> k.location().toString()).orElse("unknown");
    }

    String getBiome(GameVector3 pos) {
        return getBiome((int) pos.x, (int) pos.y, (int) pos.z);
    }
}
