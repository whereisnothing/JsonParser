package com.chenxu.jsonparser;

import java.util.List;

/**
 * Created by Administrator on 2016/4/23 0023.
 */
public class Person {
    private String name;
    private int age;
    private List<Favorite> favoriteList;

    public Person() {
    }

    public void dump() {
        String nameAge = ("Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}');
        StringBuilder sb = new StringBuilder();
        if (favoriteList!=null){
            for (Favorite favorite : favoriteList) {
                sb.append(favorite.toString());
                sb.append("\n");
            }
        }
        LogUtil.ii(nameAge+sb.toString());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
