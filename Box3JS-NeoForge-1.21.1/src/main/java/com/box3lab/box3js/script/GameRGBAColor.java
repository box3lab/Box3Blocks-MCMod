package com.box3lab.box3js.script;

public class GameRGBAColor {

    public double r, g, b, a;

    public GameRGBAColor(double r, double g, double b, double a) {
        this.r = r; this.g = g; this.b = b; this.a = a;
    }

    public GameRGBAColor set(double r, double g, double b, double a) {
        this.r = r; this.g = g; this.b = b; this.a = a; return this;
    }

    public GameRGBAColor copy(GameRGBAColor c) {
        this.r = c.r; this.g = c.g; this.b = c.b; this.a = c.a; return this;
    }

    public GameRGBAColor clone() {
        return new GameRGBAColor(r, g, b, a);
    }

    public GameRGBAColor add(GameRGBAColor rgba) {
        return new GameRGBAColor(r + rgba.r, g + rgba.g, b + rgba.b, a + rgba.a);
    }

    public GameRGBAColor sub(GameRGBAColor rgba) {
        return new GameRGBAColor(r - rgba.r, g - rgba.g, b - rgba.b, a - rgba.a);
    }

    public GameRGBAColor mul(GameRGBAColor rgba) {
        return new GameRGBAColor(r * rgba.r, g * rgba.g, b * rgba.b, a * rgba.a);
    }

    public GameRGBAColor div(GameRGBAColor rgba) {
        return new GameRGBAColor(
            rgba.r == 0 ? 0 : r / rgba.r,
            rgba.g == 0 ? 0 : g / rgba.g,
            rgba.b == 0 ? 0 : b / rgba.b,
            rgba.a == 0 ? 0 : a / rgba.a);
    }

    // Mutating variants (return this)
    public GameRGBAColor addEq(GameRGBAColor rgba) {
        r += rgba.r; g += rgba.g; b += rgba.b; a += rgba.a; return this;
    }

    public GameRGBAColor subEq(GameRGBAColor rgba) {
        r -= rgba.r; g -= rgba.g; b -= rgba.b; a -= rgba.a; return this;
    }

    public GameRGBAColor mulEq(GameRGBAColor rgba) {
        r *= rgba.r; g *= rgba.g; b *= rgba.b; a *= rgba.a; return this;
    }

    public GameRGBAColor divEq(GameRGBAColor rgba) {
        if (rgba.r != 0) r /= rgba.r;
        if (rgba.g != 0) g /= rgba.g;
        if (rgba.b != 0) b /= rgba.b;
        if (rgba.a != 0) a /= rgba.a;
        return this;
    }

    public GameRGBAColor lerp(GameRGBAColor rgba, double n) {
        return new GameRGBAColor(
            r + (rgba.r - r) * n, g + (rgba.g - g) * n,
            b + (rgba.b - b) * n, a + (rgba.a - a) * n);
    }

    public boolean equals(GameRGBAColor rgba) {
        return Math.abs(r - rgba.r) < 1e-6 && Math.abs(g - rgba.g) < 1e-6 &&
               Math.abs(b - rgba.b) < 1e-6 && Math.abs(a - rgba.a) < 1e-6;
    }

    /** Blend this RGBA color onto an RGB background, returning the displayed GameRGBColor */
    public GameRGBColor blendEq(GameRGBColor rgb) {
        double alpha = Math.max(0, Math.min(1, a));
        return new GameRGBColor(
            r * alpha + rgb.r * (1 - alpha),
            g * alpha + rgb.g * (1 - alpha),
            b * alpha + rgb.b * (1 - alpha));
    }

    @Override
    public String toString() {
        return "GameRGBAColor(" + r + ", " + g + ", " + b + ", " + a + ")";
    }
}
