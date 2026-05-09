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

    /** Rotates a vector by this unit quaternion. */
    public GameVector3 rotateVector(GameVector3 v) {
        // v' = v*(w²-|r|²) + 2*r*(r·v) + 2*w*(r × v)
        GameVector3 r = new GameVector3(x, y, z);
        double w2mr2 = w * w - (x * x + y * y + z * z);
        double dot2 = 2 * r.dot(v);
        GameVector3 term1 = v.scale(w2mr2);
        GameVector3 term2 = r.scale(dot2);
        GameVector3 term3 = r.cross(v).scale(2 * w);
        return new GameVector3(
            term1.x + term2.x + term3.x,
            term1.y + term2.y + term3.y,
            term1.z + term2.z + term3.z
        );
    }

    /** Decomposes this quaternion into YZX Euler angles (radians).
     *  Returns a GameVector3 where x/y/z correspond to rotation angles around the X/Y/Z axes. */
    public GameVector3 toEuler() {
        double m10 = 2 * (x * y + w * z);
        double m00 = 1 - 2 * (y * y + z * z);
        double m20 = 2 * (x * z - w * y);
        double m11 = 1 - 2 * (x * x + z * z);
        double m12 = 2 * (y * z - w * x);

        double ez = Math.asin(Math.max(-1, Math.min(1, m10)));
        double ey, ex;
        double cosZ = Math.cos(ez);
        if (Math.abs(cosZ) < 1e-6) {
            ex = 0;
            ey = Math.atan2(-m20, m00);
        } else {
            ex = Math.atan2(-m12, m11);
            ey = Math.atan2(-m20, m00);
        }
        return new GameVector3(ex, ey, ez);
    }

    /** Builds a look-at quaternion rotating the -Z direction toward (to-from). */
    public static GameQuaternion lookAt(GameVector3 from, GameVector3 to, GameVector3 up) {
        GameVector3 fwd = to.sub(from).normalize();
        GameVector3 right = fwd.cross(up).normalize();
        if (right.mag() < 0.001) {
            right = new GameVector3(1, 0, 0).cross(fwd).normalize();
            if (right.mag() < 0.001)
                right = new GameVector3(0, 1, 0).cross(fwd).normalize();
        }
        GameVector3 upCorr = right.cross(fwd).normalize();

        double m00 = right.x, m01 = upCorr.x, m02 = -fwd.x;
        double m10 = right.y, m11 = upCorr.y, m12 = -fwd.y;
        double m20 = right.z, m21 = upCorr.z, m22 = -fwd.z;

        double trace = m00 + m11 + m22;
        double w, x, y, z;
        if (trace > 0) {
            double s = Math.sqrt(trace + 1) * 2;
            w = 0.25 * s;
            x = (m21 - m12) / s;
            y = (m02 - m20) / s;
            z = (m10 - m01) / s;
        } else if (m00 > m11 && m00 > m22) {
            double s = Math.sqrt(1 + m00 - m11 - m22) * 2;
            w = (m21 - m12) / s;
            x = 0.25 * s;
            y = (m01 + m10) / s;
            z = (m02 + m20) / s;
        } else if (m11 > m22) {
            double s = Math.sqrt(1 + m11 - m00 - m22) * 2;
            w = (m02 - m20) / s;
            x = (m01 + m10) / s;
            y = 0.25 * s;
            z = (m12 + m21) / s;
        } else {
            double s = Math.sqrt(1 + m22 - m00 - m11) * 2;
            w = (m10 - m01) / s;
            x = (m02 + m20) / s;
            y = (m12 + m21) / s;
            z = 0.25 * s;
        }
        return new GameQuaternion(w, x, y, z);
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
