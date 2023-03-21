package com.github.martinfrank.chessapp;

public class ColorConverter {

    public static int red(int argb){
        return (argb & 0xFF0000 ) >> 16;
    }

    public static int green(int argb){
        return  (argb & 0x00FF00) >> 8;
    }

    public static int blue(int argb){
        return argb & 0x0000FF;
    }

    public static int rgb(int red, int green, int blue) {
        int r = (red << 16);
        int g = (green << 8);
        int b = blue;
        return r + g + b;
    }
}
