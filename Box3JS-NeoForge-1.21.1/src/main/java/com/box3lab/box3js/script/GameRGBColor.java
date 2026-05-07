package com.box3lab.box3js.script;

public class GameRGBColor {

    public double r, g, b;

    public GameRGBColor(double r, double g, double b) {
        this.r = r; this.g = g; this.b = b;
    }

    public GameRGBColor set(double r, double g, double b) {
        this.r = r; this.g = g; this.b = b; return this;
    }

    public GameRGBColor copy(GameRGBColor o) {
        this.r = o.r; this.g = o.g; this.b = o.b; return this;
    }

    public GameRGBColor clone() {
        return new GameRGBColor(r, g, b);
    }

    public GameRGBColor add(GameRGBColor o) {
        return new GameRGBColor(r + o.r, g + o.g, b + o.b);
    }

    public GameRGBColor sub(GameRGBColor o) {
        return new GameRGBColor(r - o.r, g - o.g, b - o.b);
    }

    public GameRGBColor mul(GameRGBColor o) {
        return new GameRGBColor(r * o.r, g * o.g, b * o.b);
    }

    public GameRGBColor div(GameRGBColor o) {
        return new GameRGBColor(
            o.r == 0 ? 0 : r / o.r,
            o.g == 0 ? 0 : g / o.g,
            o.b == 0 ? 0 : b / o.b);
    }

    public GameRGBColor addEq(GameRGBColor o) {
        r += o.r; g += o.g; b += o.b; return this;
    }

    public GameRGBColor subEq(GameRGBColor o) {
        r -= o.r; g -= o.g; b -= o.b; return this;
    }

    public GameRGBColor mulEq(GameRGBColor o) {
        r *= o.r; g *= o.g; b *= o.b; return this;
    }

    public GameRGBColor divEq(GameRGBColor o) {
        r = o.r == 0 ? 0 : r / o.r;
        g = o.g == 0 ? 0 : g / o.g;
        b = o.b == 0 ? 0 : b / o.b;
        return this;
    }

    public GameRGBColor lerp(GameRGBColor o, double n) {
        return new GameRGBColor(r + (o.r - r) * n, g + (o.g - g) * n, b + (o.b - b) * n);
    }

    public boolean equals(GameRGBColor o) {
        if (o == null) return false;
        return Math.abs(r - o.r) < 1e-6 && Math.abs(g - o.g) < 1e-6 && Math.abs(b - o.b) < 1e-6;
    }

    public String toRGBA() {
        int ri = Math.max(0, Math.min(255, (int) (r * 255)));
        int gi = Math.max(0, Math.min(255, (int) (g * 255)));
        int bi = Math.max(0, Math.min(255, (int) (b * 255)));
        return String.format("rgba(%d,%d,%d,1.0)", ri, gi, bi);
    }

    public static GameRGBColor random() {
        return new GameRGBColor(Math.random(), Math.random(), Math.random());
    }

    @Override
    public String toString() {
        return "GameRGBColor(" + r + ", " + g + ", " + b + ")";
    }
}
