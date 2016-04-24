package com.chenxu.jsonparser;

/**
 * Created by Administrator on 2016/4/23 0023.
 */
public class Person {
    private String name;
    private int age;

    public Person() {
    }

    public void dump() {
        LogUtil.ii("Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}');
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
