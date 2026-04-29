package com.box3lab.box3js.script;

public class GameRGBColor {

    public double r, g, b;

    public GameRGBColor(double r, double g, double b) {
        this.r = r; this.g = g; this.b = b;
    }

    public GameRGBColor lerp(GameRGBColor o, double n) {
        return new GameRGBColor(r + (o.r - r) * n, g + (o.g - g) * n, b + (o.b - b) * n);
    }

    public static GameRGBColor random() {
        return new GameRGBColor(Math.random(), Math.random(), Math.random());
    }

    @Override
    public String toString() {
        return "GameRGBColor(" + r + ", " + g + ", " + b + ")";
    }
}
