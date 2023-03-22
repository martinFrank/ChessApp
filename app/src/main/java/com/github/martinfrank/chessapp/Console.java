package com.github.martinfrank.chessapp;

public class Console {

    private String text = "";

    public void add(String s) {
        text = s+"\n"+text;
    }

    public String getText() {
        return text;
    }
}
