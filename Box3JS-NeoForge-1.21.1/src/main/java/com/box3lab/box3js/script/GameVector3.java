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

    public GameVector3 copy(GameVector3 v) {
        this.x = v.x; this.y = v.y; this.z = v.z; return this;
    }

    public GameVector3 clone() {
        return new GameVector3(x, y, z);
    }

    public GameVector3 add(GameVector3 v) {
        return new GameVector3(x + v.x, y + v.y, z + v.z);
    }

    public GameVector3 sub(GameVector3 v) {
        return new GameVector3(x - v.x, y - v.y, z - v.z);
    }

    public GameVector3 mul(GameVector3 v) {
        return new GameVector3(x * v.x, y * v.y, z * v.z);
    }

    public GameVector3 div(GameVector3 v) {
        return new GameVector3(
            v.x == 0 ? 0 : x / v.x,
            v.y == 0 ? 0 : y / v.y,
            v.z == 0 ? 0 : z / v.z);
    }

    public GameVector3 scale(double n) {
        return new GameVector3(x * n, y * n, z * n);
    }

    public GameVector3 scaleEq(double n) {
        x *= n; y *= n; z *= n; return this;
    }

    public GameVector3 neg() {
        return new GameVector3(-x, -y, -z);
    }

    public GameVector3 negEq() {
        x = -x; y = -y; z = -z; return this;
    }

    public GameVector3 addEq(GameVector3 v) {
        x += v.x; y += v.y; z += v.z; return this;
    }

    public GameVector3 subEq(GameVector3 v) {
        x -= v.x; y -= v.y; z -= v.z; return this;
    }

    public GameVector3 mulEq(GameVector3 v) {
        x *= v.x; y *= v.y; z *= v.z; return this;
    }

    public GameVector3 divEq(GameVector3 v) {
        x = v.x == 0 ? 0 : x / v.x;
        y = v.y == 0 ? 0 : y / v.y;
        z = v.z == 0 ? 0 : z / v.z;
        return this;
    }

    public double dot(GameVector3 v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public GameVector3 cross(GameVector3 v) {
        return new GameVector3(
            y * v.z - z * v.y,
            z * v.x - x * v.z,
            x * v.y - y * v.x);
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

    public double sqrDistance(GameVector3 v) {
        double dx = x - v.x, dy = y - v.y, dz = z - v.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public GameVector3 lerp(GameVector3 v, double n) {
        return new GameVector3(x + (v.x - x) * n, y + (v.y - y) * n, z + (v.z - z) * n);
    }

    public GameVector3 moveTowards(GameVector3 target, double maxDelta) {
        double dx = target.x - x, dy = target.y - y, dz = target.z - z;
        double d2 = dx * dx + dy * dy + dz * dz;
        if (d2 <= maxDelta * maxDelta || d2 == 0)
            return new GameVector3(target.x, target.y, target.z);
        double t = maxDelta / Math.sqrt(d2);
        return new GameVector3(x + dx * t, y + dy * t, z + dz * t);
    }

    public GameVector3 towards(GameVector3 v) {
        return sub(v).normalize();
    }

    public double angle(GameVector3 v) {
        double m = mag() * v.mag();
        if (m == 0) return 0;
        double c = dot(v) / m;
        return Math.acos(Math.max(-1, Math.min(1, c)));
    }

    public boolean equals(GameVector3 v) {
        if (v == null) return false;
        return Math.abs(x - v.x) < 1e-6 && Math.abs(y - v.y) < 1e-6 && Math.abs(z - v.z) < 1e-6;
    }

    public boolean exactEquals(GameVector3 v) {
        if (v == null) return false;
        return x == v.x && y == v.y && z == v.z;
    }

    public GameVector3 max(GameVector3 v) {
        return new GameVector3(Math.max(x, v.x), Math.max(y, v.y), Math.max(z, v.z));
    }

    public GameVector3 min(GameVector3 v) {
        return new GameVector3(Math.min(x, v.x), Math.min(y, v.y), Math.min(z, v.z));
    }

    public boolean isZero() {
        return Math.abs(x) < 1e-6 && Math.abs(y) < 1e-6 && Math.abs(z) < 1e-6;
    }

    public GameVector3 floor() {
        return new GameVector3(Math.floor(x), Math.floor(y), Math.floor(z));
    }

    public GameVector3 ceil() {
        return new GameVector3(Math.ceil(x), Math.ceil(y), Math.ceil(z));
    }

    public GameVector3 clampLength(double max) {
        double m2 = x * x + y * y + z * z;
        if (m2 > max * max) {
            double s = max / Math.sqrt(m2);
            return new GameVector3(x * s, y * s, z * s);
        }
        return clone();
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
