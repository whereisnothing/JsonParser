package com.chenxu.jsonparser;

/**
 * Created by Administrator on 2016/4/24 0024.
 */
public class Favorite {
    private String name;
    private String color;

    @Override
    public String toString() {
        return "Favorite{" +
                "name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }

    public Favorite() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
