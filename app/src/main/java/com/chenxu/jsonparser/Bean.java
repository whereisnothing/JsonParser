package com.chenxu.jsonparser;

import java.util.List;

/**
 * Created by Administrator on 2016/4/23 0023.
 */
public class Bean {
    private String key1;
    private double key2;
    private boolean key3;
    private List<Person> key4;

    public void dump(){
        LogUtil.ii("key1:"+key1+" key2:"+key2+" key3:"+key3);
        if (key4!=null){
            for (int i = 0; i < key4.size(); i++) {
                Person person = key4.get(i);
                person.dump();
            }
        }
    }

    public Bean() {
    }

    public String getKey1() {
        return key1;
    }

    public void setKey1(String key1) {
        this.key1 = key1;
    }

    public double getKey2() {
        return key2;
    }

    public void setKey2(double key2) {
        this.key2 = key2;
    }

    public boolean isKey3() {
        return key3;
    }

    public void setKey3(boolean key3) {
        this.key3 = key3;
    }

    public List<Person> getKey4() {
        return key4;
    }

    public void setKey4(List<Person> key4) {
        this.key4 = key4;
    }
}
