package com.chenxu.jsonparser;

import android.text.TextUtils;

import java.util.Stack;

/**
 * Created by chenxu on 2016/4/5.
 */
public class JsonParser {
    public enum TOKEN_TYPE {OBJECT_START,OBJECT_END,ARRAY_START,ARRAY_END,KEY,COLON,NULL,TRUE,FALSE,NUMBER,STRING,COMMA,END};
    private String jsonString;
    private int index=0;
    private Object parsedObject;
    private Stack<TOKEN_TYPE> typeStack=new Stack<>();
    private Stack<Object> objectStack=new Stack<>();
    private static JsonParser instance=null;

    public void parse(){
        if (TextUtils.isEmpty(jsonString)){
            LogUtil.ii("json string is empty");
        } else {
            for (;;){
                TOKEN_TYPE tokenType = getNextToken();
                if (tokenType== TOKEN_TYPE.END){
                    break;
                }
            }
        }
    }

    private TOKEN_TYPE getNextToken(){
        TOKEN_TYPE type = getNextTokenType();
        skipSpace();
        return type;
    }

    private TOKEN_TYPE getNextTokenType(){
        skipSpace();
        TOKEN_TYPE type = TOKEN_TYPE.END;
        if (index<jsonString.length()){
            char c = jsonString.charAt(index);
            switch (c){
                case '{':{
                    TOKEN_TYPE prevType = TOKEN_TYPE.END;
                    if (typeStack.size()>=1){
                        prevType=typeStack.peek();
                        if (prevType== TOKEN_TYPE.COLON||prevType== TOKEN_TYPE.END||prevType== TOKEN_TYPE.ARRAY_START||prevType== TOKEN_TYPE.COMMA){
                            type= TOKEN_TYPE.OBJECT_START;
                        } else {
                            dumpError();
                        }
                    } else {
                        type= TOKEN_TYPE.OBJECT_START;
                    }
                    break;
                }
                case '}': {
                    if (typeStack.size()>=1){
                        TOKEN_TYPE prevType = typeStack.peek();
                        if (prevType== TOKEN_TYPE.OBJECT_END||prevType== TOKEN_TYPE.ARRAY_END||prevType== TOKEN_TYPE.STRING||
                                prevType== TOKEN_TYPE.NUMBER||prevType== TOKEN_TYPE.TRUE||prevType== TOKEN_TYPE.FALSE||
                                prevType== TOKEN_TYPE.NULL){
                            type= TOKEN_TYPE.OBJECT_END;
                        } else {
                            dumpError();
                        }
                    } else {
                        dumpError();
                    }
                }
                    break;
                case '[':{
                    if (typeStack.size()>=1){
                        TOKEN_TYPE prevType = typeStack.peek();
                        if (prevType== TOKEN_TYPE.END||prevType== TOKEN_TYPE.COLON||prevType== TOKEN_TYPE.COMMA||
                                prevType== TOKEN_TYPE.ARRAY_START){
                            type= TOKEN_TYPE.ARRAY_START;
                        } else {
                            dumpError();
                        }
                    } else {
                        type= TOKEN_TYPE.ARRAY_START;
                    }
                }
                    break;
                case ']':
                {
                    if (typeStack.size()>=1){
                        TOKEN_TYPE prevType = typeStack.peek();
                        if (prevType== TOKEN_TYPE.STRING||prevType== TOKEN_TYPE.NUMBER||prevType== TOKEN_TYPE.NULL||prevType== TOKEN_TYPE.TRUE||
                                prevType== TOKEN_TYPE.FALSE||prevType== TOKEN_TYPE.OBJECT_END||prevType== TOKEN_TYPE.ARRAY_END){
                            type= TOKEN_TYPE.ARRAY_END;
                        } else {
                            dumpError();
                        }
                    } else {
                        dumpError();
                    }
                }
                    break;
                case '\"':
                {
                    if (typeStack.size()>=1){
                        TOKEN_TYPE prevType = typeStack.peek();
                        if (prevType== TOKEN_TYPE.OBJECT_START||prevType== TOKEN_TYPE.COMMA){
                            type= TOKEN_TYPE.KEY;
                        } else if (prevType== TOKEN_TYPE.COLON||prevType== TOKEN_TYPE.ARRAY_START){
                            type= TOKEN_TYPE.STRING;
                        } else {
                            dumpError();
                        }
                    } else {
                        dumpError();
                    }
                }
                    break;
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:

                default:
                    dumpError();
            }
        }
        return type;
    }

    private void dumpError(){
        String errorString="index at "+index+" error";
        LogUtil.ii(errorString);
        throw new RuntimeException(errorString);
    }

    private void skipSpace(){
        while (index<jsonString.length()){
            char c = jsonString.charAt(index);
            if (Character.isWhitespace(c)){
                ++index;
            }
        }

    }

    private JsonParser() {
    }

    private JsonParser(String jsonString){
        this.jsonString=jsonString;
    }

    public static synchronized JsonParser getInstance(){
        if (instance==null){
            instance=new JsonParser();
        }
        return instance;
    }

    public static synchronized JsonParser getInstance(String jsonString){
        if (instance==null){
            instance=new JsonParser(jsonString);
        }
        return instance;
    }

    public String getJsonString() {
        return jsonString;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }

}
