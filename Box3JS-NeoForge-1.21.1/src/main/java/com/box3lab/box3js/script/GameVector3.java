package com.box3lab.box3js.script;

public class GameVector3 {

    public double x, y, z;

    public GameVector3() { this(0, 0, 0); }

    public GameVector3(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }

    public GameVector3 set(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z; return this;
    }

    public GameVector3 add(GameVector3 v) {
        return new GameVector3(x + v.x, y + v.y, z + v.z);
    }

    public GameVector3 sub(GameVector3 v) {
        return new GameVector3(x - v.x, y - v.y, z - v.z);
    }

    public GameVector3 scale(double n) {
        return new GameVector3(x * n, y * n, z * n);
    }

    public double dot(GameVector3 v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public double mag() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double sqrMag() { return x * x + y * y + z * z; }

    public GameVector3 normalize() {
        double m = mag();
        return m == 0 ? new GameVector3(0, 0, 0) : scale(1.0 / m);
    }

    public double distance(GameVector3 v) {
        double dx = x - v.x, dy = y - v.y, dz = z - v.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public GameVector3 lerp(GameVector3 v, double n) {
        return new GameVector3(x + (v.x - x) * n, y + (v.y - y) * n, z + (v.z - z) * n);
    }

    public boolean equals(GameVector3 v) {
        return x == v.x && y == v.y && z == v.z;
    }

    public static GameVector3 fromPolar(double mag, double phi, double theta) {
        return new GameVector3(
                mag * Math.cos(phi) * Math.cos(theta),
                mag * Math.sin(theta),
                mag * Math.sin(phi) * Math.cos(theta)
        );
    }

    @Override
    public String toString() {
        return "GameVector3(" + x + ", " + y + ", " + z + ")";
    }
}
