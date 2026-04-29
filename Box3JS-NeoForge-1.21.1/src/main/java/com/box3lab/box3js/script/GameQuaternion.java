package com.box3lab.box3js.script;

public class GameQuaternion {

    public double w, x, y, z;

    public GameQuaternion() { this(1, 0, 0, 0); }

    public GameQuaternion(double w, double x, double y, double z) {
        this.w = w; this.x = x; this.y = y; this.z = z;
    }

    public GameQuaternion set(double w, double x, double y, double z) {
        this.w = w; this.x = x; this.y = y; this.z = z; return this;
    }

    public GameQuaternion copy(GameQuaternion v) {
        this.w = v.w; this.x = v.x; this.y = v.y; this.z = v.z; return this;
    }

    public GameQuaternion clone() {
        return new GameQuaternion(w, x, y, z);
    }

    public GameQuaternion add(GameQuaternion v) {
        return new GameQuaternion(w + v.w, x + v.x, y + v.y, z + v.z);
    }

    public GameQuaternion sub(GameQuaternion v) {
        return new GameQuaternion(w - v.w, x - v.x, y - v.y, z - v.z);
    }

    /** Hamilton product: this * q */
    public GameQuaternion mul(GameQuaternion q) {
        return new GameQuaternion(
            w * q.w - x * q.x - y * q.y - z * q.z,
            w * q.x + x * q.w + y * q.z - z * q.y,
            w * q.y - x * q.z + y * q.w + z * q.x,
            w * q.z + x * q.y - y * q.x + z * q.w
        );
    }

    /** Conjugate (inverse for unit quaternions) */
    public GameQuaternion inv() {
        return new GameQuaternion(w, -x, -y, -z);
    }

    /** Division: this * q^-1 */
    public GameQuaternion div(GameQuaternion q) {
        double denom = q.w * q.w + q.x * q.x + q.y * q.y + q.z * q.z;
        if (denom == 0) return clone();
        GameQuaternion qi = new GameQuaternion(q.w / denom, -q.x / denom, -q.y / denom, -q.z / denom);
        return mul(qi);
    }

    public double dot(GameQuaternion q) {
        return w * q.w + x * q.x + y * q.y + z * q.z;
    }

    public double mag() {
        return Math.sqrt(w * w + x * x + y * y + z * z);
    }

    public double sqrMag() {
        return w * w + x * x + y * y + z * z;
    }

    public GameQuaternion normalize() {
        double m = mag();
        return m == 0 ? new GameQuaternion(1, 0, 0, 0) : new GameQuaternion(w / m, x / m, y / m, z / m);
    }

    public GameQuaternion slerp(GameQuaternion q, double t) {
        double cosTheta = dot(q);
        GameQuaternion q2 = q;
        if (cosTheta < 0) { cosTheta = -cosTheta; q2 = new GameQuaternion(-q.w, -q.x, -q.y, -q.z); }
        if (cosTheta > 0.9995) {
            GameQuaternion r = new GameQuaternion(
                w + (q2.w - w) * t, x + (q2.x - x) * t,
                y + (q2.y - y) * t, z + (q2.z - z) * t);
            return r.normalize();
        }
        double theta = Math.acos(cosTheta);
        double sinTheta = Math.sin(theta);
        double a = Math.sin((1 - t) * theta) / sinTheta;
        double b = Math.sin(t * theta) / sinTheta;
        return new GameQuaternion(
            a * w + b * q2.w, a * x + b * q2.x,
            a * y + b * q2.y, a * z + b * q2.z);
    }

    /** Angle in radians between this and q */
    public double angle(GameQuaternion q) {
        double d = dot(q);
        if (d > 1) d = 1; if (d < -1) d = -1;
        return 2 * Math.acos(Math.abs(d));
    }

    /** Returns {angle, axis} for this quaternion */
    public AxisAngle getAxisAngle() {
        GameQuaternion q = normalize();
        double angle = 2 * Math.acos(q.w);
        double s = Math.sqrt(1 - q.w * q.w);
        GameVector3 axis;
        if (s < 1e-6) {
            axis = new GameVector3(1, 0, 0);
        } else {
            axis = new GameVector3(q.x / s, q.y / s, q.z / s);
        }
        return new AxisAngle(angle, axis);
    }

    /** Return type for getAxisAngle() — public fields accessible from JS */
    public static class AxisAngle {
        public double angle;
        public GameVector3 axis;
        AxisAngle(double angle, GameVector3 axis) { this.angle = angle; this.axis = axis; }
    }

    public boolean equals(GameQuaternion v) {
        return Math.abs(w - v.w) < 1e-6 && Math.abs(x - v.x) < 1e-6 &&
               Math.abs(y - v.y) < 1e-6 && Math.abs(z - v.z) < 1e-6;
    }

    // ---- Rotations ----

    public GameQuaternion rotateX(double rad) {
        double half = rad / 2;
        GameQuaternion rx = new GameQuaternion(Math.cos(half), Math.sin(half), 0, 0);
        return rx.mul(this);
    }

    public GameQuaternion rotateY(double rad) {
        double half = rad / 2;
        GameQuaternion ry = new GameQuaternion(Math.cos(half), 0, Math.sin(half), 0);
        return ry.mul(this);
    }

    public GameQuaternion rotateZ(double rad) {
        double half = rad / 2;
        GameQuaternion rz = new GameQuaternion(Math.cos(half), 0, 0, Math.sin(half));
        return rz.mul(this);
    }

    // ---- Static constructors ----

    public static GameQuaternion fromAxisAngle(GameVector3 axis, double rad) {
        double half = rad / 2;
        double s = Math.sin(half);
        GameVector3 n = axis.normalize();
        return new GameQuaternion(Math.cos(half), n.x * s, n.y * s, n.z * s);
    }

    /** YZX Euler order: rotate Y then Z then X */
    public static GameQuaternion fromEuler(double x, double y, double z) {
        double cx = Math.cos(x / 2), sx = Math.sin(x / 2);
        double cy = Math.cos(y / 2), sy = Math.sin(y / 2);
        double cz = Math.cos(z / 2), sz = Math.sin(z / 2);
        return new GameQuaternion(
            cy * cz * cx + sy * sz * sx,
            cy * cz * sx - sy * sz * cx,
            cy * sz * cx + sy * cz * sx,
            sy * cz * cx - cy * sz * sx
        );
    }

    /** Shortest-arc quaternion rotating from vector a to b */
    public static GameQuaternion rotationBetween(GameVector3 a, GameVector3 b) {
        GameVector3 an = a.normalize();
        GameVector3 bn = b.normalize();
        double dot = an.dot(bn);
        if (dot > 0.99999) return new GameQuaternion(1, 0, 0, 0);
        if (dot < -0.99999) {
            GameVector3 axis = Math.abs(an.x) < 0.9
                ? new GameVector3(1, 0, 0).add(an).normalize()
                : new GameVector3(0, 1, 0).add(an).normalize();
            return new GameQuaternion(0, axis.x, axis.y, axis.z);
        }
        GameVector3 axis = new GameVector3(
            an.y * bn.z - an.z * bn.y,
            an.z * bn.x - an.x * bn.z,
            an.x * bn.y - an.y * bn.x
        );
        double s = Math.sqrt((1 + dot) * 2);
        return new GameQuaternion(s / 2, axis.x / s, axis.y / s, axis.z / s);
    }

    @Override
    public String toString() {
        return "GameQuaternion(" + w + ", " + x + ", " + y + ", " + z + ")";
    }
}
