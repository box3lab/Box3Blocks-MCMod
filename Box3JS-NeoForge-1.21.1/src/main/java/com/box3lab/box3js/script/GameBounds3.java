package com.box3lab.box3js.script;

public class GameBounds3 {

    public GameVector3 lo, hi;

    public GameBounds3(GameVector3 lo, GameVector3 hi) {
        this.lo = lo;
        this.hi = hi;
    }

    public boolean intersects(GameBounds3 other) {
        return !(hi.x < other.lo.x || lo.x > other.hi.x ||
                 hi.y < other.lo.y || lo.y > other.hi.y ||
                 hi.z < other.lo.z || lo.z > other.hi.z);
    }

    public boolean contains(GameVector3 v) {
        return v.x >= lo.x && v.x <= hi.x &&
               v.y >= lo.y && v.y <= hi.y &&
               v.z >= lo.z && v.z <= hi.z;
    }

    @Override
    public String toString() {
        return "GameBounds3(" + lo + ", " + hi + ")";
    }
}
