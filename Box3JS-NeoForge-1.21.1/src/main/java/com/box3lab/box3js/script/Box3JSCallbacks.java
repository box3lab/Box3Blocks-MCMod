package com.box3lab.box3js.script;

@FunctionalInterface
interface PlayerJoinCallback {
    void onJoin(Box3JSEntity entity, long tick);
}

@FunctionalInterface
interface PlayerLeaveCallback {
    void onLeave(Box3JSEntity entity, long tick);
}

@FunctionalInterface
interface VoxelDestroyCallback {
    void onDestroy(Box3JSEntity entity, int x, int y, int z, String voxel, long tick);
}

@FunctionalInterface
interface VoxelContactCallback {
    void onContact(Box3JSEntity entity, int voxel, int x, int y, int z, int axis, double force, long tick);
}

@FunctionalInterface
interface InteractCallback {
    void onInteract(Box3JSEntity entity, Box3JSEntity target, long tick);
}

@FunctionalInterface
interface ChatCallback {
    void onChat(Box3JSEntity entity, String message, long tick);
}

@FunctionalInterface
interface FluidEnterCallback {
    void onEnter(Box3JSEntity entity, String fluid, int x, int y, int z, long tick);
}

@FunctionalInterface
interface FluidLeaveCallback {
    void onLeave(Box3JSEntity entity, String fluid, int x, int y, int z, long tick);
}

@FunctionalInterface
interface EntityContactCallback {
    void onContact(Box3JSEntity entity, Box3JSEntity other, long tick);
}

@FunctionalInterface
interface EntitySeparateCallback {
    void onSeparate(Box3JSEntity entity, Box3JSEntity other, long tick);
}

@FunctionalInterface
interface BlockPlaceCallback {
    void onPlace(Box3JSEntity entity, int x, int y, int z, String voxel, int voxelId, long tick);
}

@FunctionalInterface
interface EntityDeathCallback {
    void onDeath(Box3JSEntity entity, Box3JSEntity killer, long tick);
}

@FunctionalInterface
interface PlayerRespawnCallback {
    void onRespawn(Box3JSEntity entity, long tick);
}

@FunctionalInterface
interface BlockActivateCallback {
    void onActivate(Box3JSEntity entity, int x, int y, int z, String voxel, long tick);
}

@FunctionalInterface
interface EntityDamageCallback {
    void onDamage(Box3JSEntity entity, double amount, String source, Box3JSEntity attacker, long tick);
}

@FunctionalInterface
interface ButtonPressedCallback {
    void onButtonPressed(Box3JSEntity entity, String button, long tick);
}

@FunctionalInterface
interface MessageCallback {
    void onMessage(String from, Object data);
}
