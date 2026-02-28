package com.box3lab.command;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;

final class Box3ExportSelectionStore {

    private static final Map<UUID, Selection> SELECTIONS = new ConcurrentHashMap<>();

    private Box3ExportSelectionStore() {
    }

    static Selection get(UUID playerId) {
        return SELECTIONS.get(playerId);
    }

    static Selection setPos1(UUID playerId, BlockPos pos) {
        Selection current = SELECTIONS.get(playerId);
        Selection updated = new Selection(pos.immutable(), current != null ? current.pos2() : null);
        SELECTIONS.put(playerId, updated);
        return updated;
    }

    static Selection setPos2(UUID playerId, BlockPos pos) {
        Selection current = SELECTIONS.get(playerId);
        Selection updated = new Selection(current != null ? current.pos1() : null, pos.immutable());
        SELECTIONS.put(playerId, updated);
        return updated;
    }

    static void clear(UUID playerId) {
        SELECTIONS.remove(playerId);
    }

    record Selection(BlockPos pos1, BlockPos pos2) {
        boolean complete() {
            return pos1 != null && pos2 != null;
        }
    }
}
