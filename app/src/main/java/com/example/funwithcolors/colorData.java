package com.example.funwithcolors;

public class colorData {

    int r;
    int g;
    int b;
    String name;

    public colorData(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.name = "R:"+ String.valueOf(r)+" G:"+String.valueOf(g)+" B:"+String.valueOf(b);
    }

    public String getName() {
        return name;
    }
}
