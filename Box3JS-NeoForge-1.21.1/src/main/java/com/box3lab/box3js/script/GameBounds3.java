package com.box3lab.box3js.script;

import org.mozilla.javascript.NativeArray;

public class GameBounds3 {

    public GameVector3 lo, hi;

    public GameBounds3(GameVector3 lo, GameVector3 hi) {
        this.lo = lo;
        this.hi = hi;
    }

    public GameBounds3 set(double lox, double loy, double loz, double hix, double hiy, double hiz) {
        lo.x = lox; lo.y = loy; lo.z = loz;
        hi.x = hix; hi.y = hiy; hi.z = hiz;
        return this;
    }

    public GameBounds3 copy(GameBounds3 b) {
        lo.x = b.lo.x; lo.y = b.lo.y; lo.z = b.lo.z;
        hi.x = b.hi.x; hi.y = b.hi.y; hi.z = b.hi.z;
        return this;
    }

    public boolean intersects(GameBounds3 other) {
        return !(hi.x < other.lo.x || lo.x > other.hi.x ||
                 hi.y < other.lo.y || lo.y > other.hi.y ||
                 hi.z < other.lo.z || lo.z > other.hi.z);
    }

    public GameBounds3 intersect(GameBounds3 other) {
        double lx = Math.max(lo.x, other.lo.x);
        double ly = Math.max(lo.y, other.lo.y);
        double lz = Math.max(lo.z, other.lo.z);
        double hx = Math.min(hi.x, other.hi.x);
        double hy = Math.min(hi.y, other.hi.y);
        double hz = Math.min(hi.z, other.hi.z);
        if (lx > hx || ly > hy || lz > hz) return null;
        return new GameBounds3(new GameVector3(lx, ly, lz), new GameVector3(hx, hy, hz));
    }

    public boolean contains(GameVector3 v) {
        return v.x >= lo.x && v.x <= hi.x &&
               v.y >= lo.y && v.y <= hi.y &&
               v.z >= lo.z && v.z <= hi.z;
    }

    public boolean containsBounds(GameBounds3 b) {
        return contains(b.lo) && contains(b.hi);
    }

    public static GameBounds3 fromPoints(Object points) {
        if (!(points instanceof NativeArray arr)) return null;
        long len = arr.getLength();
        if (len == 0) return null;

        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < len; i++) {
            Object elem = arr.get(i);
            if (elem instanceof GameVector3 v) {
                if (v.x < minX) minX = v.x;
                if (v.y < minY) minY = v.y;
                if (v.z < minZ) minZ = v.z;
                if (v.x > maxX) maxX = v.x;
                if (v.y > maxY) maxY = v.y;
                if (v.z > maxZ) maxZ = v.z;
            }
        }

        return new GameBounds3(new GameVector3(minX, minY, minZ), new GameVector3(maxX, maxY, maxZ));
    }

    @Override
    public String toString() {
        return "GameBounds3(" + lo + ", " + hi + ")";
    }
}
