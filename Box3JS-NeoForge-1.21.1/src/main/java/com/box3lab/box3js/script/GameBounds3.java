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

    public GameVector3 center() {
        return new GameVector3(
            (lo.x + hi.x) / 2, (lo.y + hi.y) / 2, (lo.z + hi.z) / 2);
    }

    public GameVector3 size() {
        return new GameVector3(hi.x - lo.x, hi.y - lo.y, hi.z - lo.z);
    }

    public GameBounds3 expand(double delta) {
        return new GameBounds3(
            new GameVector3(lo.x - delta, lo.y - delta, lo.z - delta),
            new GameVector3(hi.x + delta, hi.y + delta, hi.z + delta));
    }

    public GameBounds3 expandEq(double delta) {
        lo.x -= delta; lo.y -= delta; lo.z -= delta;
        hi.x += delta; hi.y += delta; hi.z += delta;
        return this;
    }

    public GameBounds3 growToInclude(GameVector3 v) {
        if (v.x < lo.x) lo.x = v.x;
        if (v.y < lo.y) lo.y = v.y;
        if (v.z < lo.z) lo.z = v.z;
        if (v.x > hi.x) hi.x = v.x;
        if (v.y > hi.y) hi.y = v.y;
        if (v.z > hi.z) hi.z = v.z;
        return this;
    }

    public GameVector3 closestPoint(GameVector3 v) {
        return new GameVector3(
            Math.max(lo.x, Math.min(hi.x, v.x)),
            Math.max(lo.y, Math.min(hi.y, v.y)),
            Math.max(lo.z, Math.min(hi.z, v.z)));
    }

    public GameBounds3 move(GameVector3 offset) {
        return new GameBounds3(
            new GameVector3(lo.x + offset.x, lo.y + offset.y, lo.z + offset.z),
            new GameVector3(hi.x + offset.x, hi.y + offset.y, hi.z + offset.z));
    }

    public GameBounds3 moveEq(GameVector3 offset) {
        lo.x += offset.x; lo.y += offset.y; lo.z += offset.z;
        hi.x += offset.x; hi.y += offset.y; hi.z += offset.z;
        return this;
    }

    public double volume() {
        return (hi.x - lo.x) * (hi.y - lo.y) * (hi.z - lo.z);
    }

    public boolean isEmpty() {
        return hi.x <= lo.x || hi.y <= lo.y || hi.z <= lo.z;
    }

    public boolean equals(GameBounds3 b) {
        if (b == null) return false;
        return lo.x == b.lo.x && lo.y == b.lo.y && lo.z == b.lo.z &&
               hi.x == b.hi.x && hi.y == b.hi.y && hi.z == b.hi.z;
    }

    public GameBounds3 union(GameBounds3 b) {
        if (b == null) return new GameBounds3(new GameVector3(lo.x, lo.y, lo.z), new GameVector3(hi.x, hi.y, hi.z));
        return new GameBounds3(
            new GameVector3(Math.min(lo.x, b.lo.x), Math.min(lo.y, b.lo.y), Math.min(lo.z, b.lo.z)),
            new GameVector3(Math.max(hi.x, b.hi.x), Math.max(hi.y, b.hi.y), Math.max(hi.z, b.hi.z)));
    }

    public GameBounds3 inflate(double amount) {
        return new GameBounds3(
            new GameVector3(lo.x - amount, lo.y - amount, lo.z - amount),
            new GameVector3(hi.x + amount, hi.y + amount, hi.z + amount));
    }

    public GameBounds3 deflate(double amount) {
        double hw = (hi.x - lo.x) / 2;
        double hh = (hi.y - lo.y) / 2;
        double hd = (hi.z - lo.z) / 2;
        double cx = (lo.x + hi.x) / 2;
        double cy = (lo.y + hi.y) / 2;
        double cz = (lo.z + hi.z) / 2;
        double nw = Math.max(0, hw - amount);
        double nh = Math.max(0, hh - amount);
        double nd = Math.max(0, hd - amount);
        return new GameBounds3(
            new GameVector3(cx - nw, cy - nh, cz - nd),
            new GameVector3(cx + nw, cy + nh, cz + nd));
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
