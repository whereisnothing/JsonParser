package com.chenxu.jsonparser;

import android.app.Application;
import android.test.ApplicationTestCase;

import java.util.Stack;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public void testJsonParser(){
        JsonParser parser = JsonParser.getInstance(getContext());
        parser.loadFileNameFromAssetsFolder("test.txt");
        Object object = parser.parse();
    }
//    public void testUnicode(){
//        String s = "0063";
//        for (int i = 0; i < s.length(); i++) {
//            char c = s.charAt(i);
//        }
//        char ch = Character.forDigit(99, 10);
////        ch= (char) Integer.parseInt("0164",16);
////        LogUtil.ii("converted ch:"+ch);
//        String[] stringArray=new String[]{"2.5", "1.2e2", "352", "3.42E3"};
//        for (int i = 0; i < stringArray.length; i++) {
//            String str = stringArray[i];
//            float f = Float.parseFloat(str);
//            LogUtil.ii(str+":"+f);
//        }
//    }
//    public void testStack(){
//        Stack<String> stack = new Stack<>();
//        stack.push("one");
//        stack.push("two");
//        stack.push("three");
//        for (int i = 0; i < stack.size(); i++) {
//            LogUtil.ii("index:"+i+":"+stack.get(i));
//        }
//    }
    public ApplicationTest() {
        super(Application.class);
    }
}