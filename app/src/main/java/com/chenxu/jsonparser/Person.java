package com.chenxu.jsonparser;

/**
 * Created by Administrator on 2016/4/23 0023.
 */
public class Person {
    private String name;
    private float age;

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

    public float getAge() {
        return age;
    }

    public void setAge(float age) {
        this.age = age;
    }
}
