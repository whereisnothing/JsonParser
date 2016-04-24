package com.chenxu.jsonparser;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by chenxu on 2016/4/5.
 */
public class JsonParser {

    private static final char COMMA = ',';

    public enum TOKEN_TYPE {OBJECT_START, OBJECT_END, ARRAY_START, ARRAY_END, KEY, COLON, NULL, TRUE, FALSE, NUMBER, STRING, COMMA, END}

    ;
    private String jsonString;
    private int index = 0;
    private Object parsedObject;
    private Stack<TOKEN_TYPE> typeStack = new Stack<>();
    private Stack<Object> objectStack = new Stack<>();
    private static JsonParser instance = null;
    private Context context;

    public Object parse() {
        if (TextUtils.isEmpty(jsonString)) {
            LogUtil.ii("json string is empty");
            return null;
        } else {
            for (; ; ) {
                TOKEN_TYPE tokenType = getNextToken();
                if (tokenType == TOKEN_TYPE.END) {
                    break;
                }
            }
            Object object = objectStack.peek();
            LogUtil.ii("object:" + object);
            return object;
        }
    }

    public static String objectToString(Object o){
        StringBuilder sb = new StringBuilder();
        if (o == null){
            sb.append("null");
        } else if (o instanceof Map<?,?>){
            Map<String,Object> map = (Map<String, Object>) o;
            sb.append("{");
            int i = 0;
            for(Map.Entry<String,Object> entry : map.entrySet()){
                if (i!=0){
                    sb.append(",");
                }
                String key = entry.getKey();
                sb.append("\""+key+"\"");
                sb.append(":");
                sb.append(objectToString(entry.getValue()));
                ++i;
            }
            sb.append("}");
        } else if (o instanceof List<?>){
            List<Object> list = (List<Object>) o;
            sb.append("[");
            int i = 0;
            for (int j = 0; j < list.size(); j++) {
                if (i!=0){
                    sb.append(",");
                }
                sb.append(objectToString(list.get(j)));
                ++i;
            }
            sb.append("]");
        } else if (o instanceof Integer){
            sb.append(String.valueOf(o));
        } else if (o instanceof Float){
            sb.append(String.valueOf(o));
        } else if (o instanceof Double){
            sb.append(String.valueOf(o));
        } else if (o instanceof Boolean){
            sb.append(String.valueOf(o));
        } else if (o instanceof String){
            sb.append("\""+String.valueOf(o)+"\"");
        } else {
            sb.append("");
        }
        return sb.toString();
    }

    public void loadFileNameFromAssetsFolder(String fileNameInAssetsFolder) {
        try {
            InputStream inputStream = null;
            inputStream = context.getResources().getAssets().open(fileNameInAssetsFolder);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(" ");
            }
            this.jsonString = stringBuilder.toString();
            reader.close();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void loadFilePath(String filePath) {
        try {
            InputStream inputStream = null;
            inputStream = new FileInputStream(filePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder stringBuilder = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(" ");
            }
            this.jsonString = stringBuilder.toString();
            reader.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private boolean isInteger(String s){
        if (TextUtils.isEmpty(s)){
            return false;
        } else {
            return s.matches("[-|+]?\\d{1,}");
        }
    }

    private TOKEN_TYPE getNextToken() {
        TOKEN_TYPE type = getNextTokenType();
        skipSpace();
        int idx = index;
        switch (type) {
            case OBJECT_START: {
                ++index;
                typeStack.push(type);
                Map<String, Object> object = new HashMap<>();
                objectStack.push(object);
            }
            break;
            case OBJECT_END: {
                ++index;
                while (typeStack.pop() != TOKEN_TYPE.OBJECT_START) {
                }
                TOKEN_TYPE nearestWrapperType = getNearestWrapperType();
                if (typeStack.size() >= 1) {
                    TOKEN_TYPE prevType = typeStack.peek();
                    if (prevType == TOKEN_TYPE.COLON) {
                        typeStack.pop();
                        if (typeStack.pop() == TOKEN_TYPE.KEY) {
                            Object value = objectStack.pop();
                            String key = (String) objectStack.pop();
                            Map<String, Object> map = (Map<String, Object>) objectStack.peek();
                            map.put(key, value);
                        }
                    } else if (nearestWrapperType == TOKEN_TYPE.ARRAY_START) {

                        Map<String, Object> map = (Map<String, Object>) objectStack.pop();
                        List<Object> list = (List<Object>) objectStack.peek();
                        list.add(map);
                    } else {
                        dumpError();
                    }
                }
                break;
            }
            case ARRAY_START: {
                ++index;
                typeStack.push(type);
                List<Object> list = new ArrayList<>();
                objectStack.push(list);
                break;
            }
            case ARRAY_END: {
                ++index;
                while (typeStack.pop() != TOKEN_TYPE.ARRAY_START) {

                }
                TOKEN_TYPE nearestWrapperType = getNearestWrapperType();
                if (typeStack.size()>=1){
                    TOKEN_TYPE prevType = typeStack.peek();
                    if (prevType == TOKEN_TYPE.COLON) {
                        typeStack.pop();
                        typeStack.pop();
                        List<Object> value = (List<Object>) objectStack.pop();
                        String key = (String) objectStack.pop();
                        Map<String, Object> map = (Map<String, Object>) objectStack.peek();
                        map.put(key, value);
                    }  else if (nearestWrapperType == TOKEN_TYPE.ARRAY_START) {
                        Object object = objectStack.pop();
                        List<Object> list = (List<Object>) objectStack.peek();
                        list.add(object);
                    } else {
                        dumpError();
                    }

                }
                break;
            }
            case KEY: {
                String key = getKey();
                typeStack.push(TOKEN_TYPE.KEY);
                objectStack.push(key);
                break;
            }
            case STRING: {
                String string = getString();
                TOKEN_TYPE prevType = typeStack.peek();
                if (prevType == TOKEN_TYPE.COLON) {
                    typeStack.pop();
                    typeStack.pop();
//                    dumpIndexAndTypeStackAndObjectStack();
                    String key = (String) objectStack.pop();
                    Map<String, Object> map = (Map<String, Object>) objectStack.peek();
                    map.put(key, string);
                } else if (prevType == TOKEN_TYPE.ARRAY_START) {
                    List<Object> list = (List<Object>) objectStack.peek();
                    list.add(string);
                } else {
                    dumpError();
                }
                break;
            }
            case NUMBER: {
                String numberString = getNumberString();
                numberString=numberString.replaceAll("\\s","");
//                LogUtil.ii("acquired number:" + numberString);
                Object number = 0;
                try {
                    if(isInteger(numberString)){
                        number= Integer.parseInt(numberString);
                    } else {
                        number = Double.parseDouble(numberString);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    dumpError();
                    break;
                }
                TOKEN_TYPE prevType = typeStack.peek();
//                LogUtil.ii("number's prev type:" + prevType);
                if (prevType == TOKEN_TYPE.COLON) {
                    typeStack.pop();
                    typeStack.pop();
                    String key = (String) objectStack.pop();
                    Map<String, Object> map = (Map<String, Object>) objectStack.peek();
                    map.put(key, number);
                } else if (prevType == TOKEN_TYPE.ARRAY_START) {
                    List<Object> list = (List<Object>) objectStack.peek();
                    list.add(number);
                } else {
                    dumpError();
                }
                break;
            }
            case TRUE: {
                skipSpace();
                if (index + 3 < jsonString.length()) {
                    String substring = jsonString.substring(index, index + "true".length());
                    if ("true".equals(substring)) {

                    } else {
                        dumpError();
                    }
                } else {
                    dumpError();
                }
                index += 4;
                if (typeStack.size() >= 1) {
                    TOKEN_TYPE prevType = typeStack.peek();
                    TOKEN_TYPE nearestWrapperType = getNearestWrapperType();
                    if (prevType == TOKEN_TYPE.ARRAY_START) {
                        List<Object> list = (List<Object>) objectStack.peek();
                        list.add(true);
                    } else if (nearestWrapperType == TOKEN_TYPE.ARRAY_START) {
                        List<Object> list = (List<Object>) objectStack.peek();
                        list.add(true);
                    } else if (prevType == TOKEN_TYPE.COLON && nearestWrapperType == TOKEN_TYPE.OBJECT_START) {
                        typeStack.pop();
                        typeStack.pop();
                        String key = (String) objectStack.pop();
                        Map<String, Object> map = (Map<String, Object>) objectStack.peek();
                        map.put(key, true);
                    } else {
                        dumpError();
                    }
                } else {
                    dumpError();
                }

                break;
            }
            case FALSE: {
                skipSpace();
                if (index + 4 < jsonString.length()) {
                    String substring = jsonString.substring(index, index + "false".length());
                    if ("false".equals(substring)) {

                    } else {
                        dumpError();
                    }
                } else {
                    dumpError();
                }
                index += 5;
                if (typeStack.size() >= 1) {
                    TOKEN_TYPE prevType = typeStack.peek();
                    TOKEN_TYPE nearestWrapperType = getNearestWrapperType();
                    if (prevType == TOKEN_TYPE.ARRAY_START) {
                        List<Object> list = (List<Object>) objectStack.peek();
                        list.add(false);
                    } else if (nearestWrapperType == TOKEN_TYPE.ARRAY_START) {
                        List<Object> list = (List<Object>) objectStack.peek();
                        list.add(false);
                    } else if (prevType == TOKEN_TYPE.COLON && nearestWrapperType == TOKEN_TYPE.OBJECT_START) {
                        typeStack.pop();
                        typeStack.pop();
                        String key = (String) objectStack.pop();
                        Map<String, Object> map = (Map<String, Object>) objectStack.peek();
                        map.put(key, false);
                    } else {
                        dumpError();
                    }
                } else {
                    dumpError();
                }

                break;
            }
            case NULL: {
                skipSpace();
                if (index + 3 < jsonString.length()) {
                    String substring = jsonString.substring(index, index + "null".length());
//                    LogUtil.ii("case NULL substring:" + substring);
                    if ("null".equals(substring)) {

                    } else {
                        dumpError();
                    }
                } else {
                    dumpError();
                }
                index += 4;
//                dumpIndexAndTypeStackAndObjectStack();
                if (typeStack.size() >= 1) {
                    TOKEN_TYPE prevType = typeStack.peek();
                    TOKEN_TYPE nearestWrapperType = getNearestWrapperType();
//                    LogUtil.ii("nearestWrapperType:"+nearestWrapperType);
                    if (prevType == TOKEN_TYPE.ARRAY_START) {
                        List<Object> list = (List<Object>) objectStack.peek();
                        list.add(null);
                    } else if (nearestWrapperType == TOKEN_TYPE.ARRAY_START) {
                        List<Object> list = (List<Object>) objectStack.peek();
                        list.add(null);
                    } else if (prevType == TOKEN_TYPE.COLON && nearestWrapperType == TOKEN_TYPE.OBJECT_START) {
                        typeStack.pop();
                        typeStack.pop();
                        String key = (String) objectStack.pop();
                        Map<String, Object> map = (Map<String, Object>) objectStack.peek();
                        map.put(key, null);
                    } else {
                        dumpError();
                    }
                } else {
                    dumpError();
                }

                break;
            }
            case COLON: {
                skipSpace();
                ++index;
                typeStack.push(TOKEN_TYPE.COLON);
                break;
            }
            case COMMA: {
                skipSpace();
                ++index;
//                typeStack.push(TOKEN_TYPE.COMMA);
                break;
            }
            case END:
                break;
            default:
                break;
        }
        return type;
    }

    private void dumpIndexAndTypeStackAndObjectStack() {
        LogUtil.ii("index:" + index);
        for (int i = 0; i < typeStack.size(); i++) {
            LogUtil.ii(i + ":" + typeStack.get(i));
        }
        for (int i = 0; i < objectStack.size(); i++) {
            LogUtil.ii(i+":"+objectStack.get(i));
        }
    }

    private String getNumberString() {
        StringBuilder sb = new StringBuilder();
        skipSpace();
        boolean hasMetWithMinusSign = false;
        boolean hasMetWithE = false;
        boolean hasMetWithDot = false;
        while (index < jsonString.length()) {
            char c = jsonString.charAt(index);
            char ch = Character.toUpperCase(c);
            if (c == '-') {
                if (hasMetWithMinusSign == true) {
                    break;
                }
                hasMetWithMinusSign = true;
                sb.append(c);
            } else if (ch == 'E') {
                if (hasMetWithE == true) {
                    break;
                }
                hasMetWithE = true;
                sb.append(c);
            } else if (c == '.') {
                if (hasMetWithDot == true) {
                    break;
                }
                hasMetWithDot = true;
                sb.append(c);
            } else if (Character.isDigit(c)) {
                sb.append(c);
            } else if (Character.isWhitespace(c)){
                char lastNonemptyCharacter = getLastNonemptyCharacter();
                if ('-'==lastNonemptyCharacter){

                } else {
                    break;
                }
            }
            else {
                break;
            }
            ++index;
        }
        return sb.toString();
    }

    private char getLastNonemptyCharacter(){
        int idx = index-1;
        while (isIndexValid(idx)){
            char c = jsonString.charAt(idx);
            if (!Character.isWhitespace(c)){
                return c;
            }
            --idx;
        }
        return ' ';
    }

    private String getKey() {
        StringBuilder sb = new StringBuilder();
        skipSpace();
        boolean isPastStart = false;
        while (index < jsonString.length()) {
            char c = jsonString.charAt(index);
            if (c == '"') {
                if (isPastStart) {
                    index += 1;
                    break;
                } else {
                    isPastStart = true;
                    ++index;
                }
            } else if (c == '\\') {
                if (isIndexValid(index + 1)) {
                    char nextChar = jsonString.charAt(index + 1);
                    if (nextChar == 'u') {
                        String fourHexDigitString = getFourHexDigits(index + 2);
                        if (TextUtils.isEmpty(fourHexDigitString)) {
                            dumpError();
                        } else {
                            char ch = getCharFromFourHexDigitString(fourHexDigitString);
                            sb.append(ch);
                            index += 6;
                        }
                    } else if (nextChar == '"') {
                        sb.append(nextChar);
                        index += 2;
                    } else {
                        sb.append(c);
                        index += 2;
                    }
                } else {
                    sb.append(c);
                    index += 1;
                }
            } else {
                sb.append(c);
                index += 1;
            }
        }
        return sb.toString();
    }

    private String getString() {
        return getKey();
    }

    private char getCharFromFourHexDigitString(String str) {
        char c;
        try {
            c = (char) Integer.parseInt(str, 16);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return ' ';
        }
        return c;
    }

    private String getFourHexDigits(int startIndex) {
        StringBuilder sb = new StringBuilder(4);
        skipSpace();
        if (startIndex + 3 < jsonString.length()) {
            for (int i = startIndex; i < startIndex + 4; i++) {
                char c = jsonString.charAt(i);
                if (isCharacterHexDigit(c)) {
                    sb.append(c);
                } else {
                    dumpError();
                    return "";
                }
            }
        } else {
            dumpError();
            return "";
        }
        return sb.toString();
    }

    private boolean isCharacterHexDigit(char c) {
        char upperChar = Character.toUpperCase(c);
        return (upperChar >= '0' && upperChar <= '9') || (upperChar >= 'A' && upperChar <= 'F');
    }

    private boolean isIndexValid(int index) {
        return index >= 0 && index < jsonString.length();
    }

    private TOKEN_TYPE getNextTokenType() {
        skipSpace();
        TOKEN_TYPE type = TOKEN_TYPE.END;
        if (index < jsonString.length()) {
            char c = jsonString.charAt(index);
            switch (c) {
                case '{': {
                    TOKEN_TYPE prevType = TOKEN_TYPE.END;
                    if (typeStack.size() >= 1) {
                        prevType = typeStack.peek();
                        if (prevType == TOKEN_TYPE.COLON || prevType == TOKEN_TYPE.END || prevType == TOKEN_TYPE.ARRAY_START) {
                            type = TOKEN_TYPE.OBJECT_START;
                        } else {
                            dumpError();
                        }
                    } else {
                        type = TOKEN_TYPE.OBJECT_START;
                    }
                    break;
                }
                case '}': {
                    type = TOKEN_TYPE.OBJECT_END;
                    TOKEN_TYPE nearestWrapperType = getNearestWrapperType();
                    if (nearestWrapperType==TOKEN_TYPE.OBJECT_START){

                    } else {
                        dumpError();
                    }
//                    if (typeStack.size() >= 1) {
//                        TOKEN_TYPE prevType = typeStack.peek();
//                        if (prevType == TOKEN_TYPE.OBJECT_END || prevType == TOKEN_TYPE.ARRAY_END || prevType == TOKEN_TYPE.STRING ||
//                                prevType == TOKEN_TYPE.NUMBER || prevType == TOKEN_TYPE.TRUE || prevType == TOKEN_TYPE.FALSE ||
//                                prevType == TOKEN_TYPE.NULL) {
//                            type = TOKEN_TYPE.OBJECT_END;
//                        } else {
//                            dumpError();
//                        }
//                    } else {
//                        dumpError();
//                    }
                }
                break;
                case '[': {
                    if (typeStack.size() >= 1) {
                        TOKEN_TYPE prevType = typeStack.peek();
                        if (prevType == TOKEN_TYPE.END || prevType == TOKEN_TYPE.COLON ||
                                prevType == TOKEN_TYPE.ARRAY_START) {
                            type = TOKEN_TYPE.ARRAY_START;
                        } else {
                            dumpError();
                        }
                    } else {
                        type = TOKEN_TYPE.ARRAY_START;
                    }
                }
                break;
                case ']': {
                    type = TOKEN_TYPE.ARRAY_END;
                    TOKEN_TYPE nearestWrapperType = getNearestWrapperType();
                    if (nearestWrapperType==TOKEN_TYPE.ARRAY_START){

                    } else {
                        dumpError();
                    }
//                    if (typeStack.size() >= 1) {
//                        TOKEN_TYPE prevType = typeStack.peek();
//                        if (prevType == TOKEN_TYPE.STRING || prevType == TOKEN_TYPE.NUMBER || prevType == TOKEN_TYPE.NULL || prevType == TOKEN_TYPE.TRUE ||
//                                prevType == TOKEN_TYPE.FALSE || prevType == TOKEN_TYPE.OBJECT_END || prevType == TOKEN_TYPE.ARRAY_END) {
//                            type = TOKEN_TYPE.ARRAY_END;
//                        } else {
//                            dumpError();
//                        }
//                    } else {
//                        dumpError();
//                    }
                }
                break;
                case '\"': {
                    if (typeStack.size() >= 1) {
                        TOKEN_TYPE prevType = typeStack.peek();
                        TOKEN_TYPE nearestWrapperType = getNearestWrapperType();
                        char lastNonemptyCharacter = getLastNonemptyCharacter();
                        if (prevType == TOKEN_TYPE.OBJECT_START&&'{'==lastNonemptyCharacter) {
                            type = TOKEN_TYPE.KEY;
                        } else if (prevType==TOKEN_TYPE.ARRAY_START&&'['==lastNonemptyCharacter){
                            type=TOKEN_TYPE.STRING;
                        }
                        else if (prevType == TOKEN_TYPE.COLON) {
                            type = TOKEN_TYPE.STRING;
                        } else if (nearestWrapperType == TOKEN_TYPE.OBJECT_START&&COMMA==lastNonemptyCharacter) {
                            type = TOKEN_TYPE.KEY;
                        } else if (nearestWrapperType == TOKEN_TYPE.ARRAY_START&&COMMA==lastNonemptyCharacter) {
                            type = TOKEN_TYPE.STRING;
                        } else {
                            dumpError();
                        }
                    } else {
                        dumpError();
                    }
                }
                break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '-': {
                    if (typeStack.size() >= 1) {
                        TOKEN_TYPE prevType = typeStack.peek();
                        TOKEN_TYPE nearestWrapperType = getNearestWrapperType();
                        char lastNonemptyCharacter = getLastNonemptyCharacter();
//                        LogUtil.ii("index:"+index+" prevType:"+prevType+" nearestWrapperType:"+nearestWrapperType+"lastNonemptyCharacter:"+lastNonemptyCharacter);
                        if (prevType == TOKEN_TYPE.ARRAY_START&&'['==lastNonemptyCharacter) {
                            type = TOKEN_TYPE.NUMBER;
                        } else if (nearestWrapperType == TOKEN_TYPE.ARRAY_START&&COMMA==lastNonemptyCharacter) {
                            type = TOKEN_TYPE.NUMBER;
                        } else if (prevType == TOKEN_TYPE.COLON) {
                            if (nearestWrapperType == TOKEN_TYPE.OBJECT_START) {
                                type = TOKEN_TYPE.NUMBER;
                            } else {
                                dumpError();
                            }
                        } else {
                            dumpError();
                        }

                    } else {
                        dumpError();
                    }
                }
                break;
                case 'n': {
                    if (typeStack.size() >= 1) {
                        TOKEN_TYPE prevType = typeStack.peek();
                        TOKEN_TYPE nearestWrapperType = getNearestWrapperType();
                        char lastNonemptyCharacter = getLastNonemptyCharacter();
                        if (prevType == TOKEN_TYPE.ARRAY_START&&'['==lastNonemptyCharacter) {
                            type = TOKEN_TYPE.NULL;

                        } else if (nearestWrapperType == TOKEN_TYPE.ARRAY_START&&COMMA==lastNonemptyCharacter) {
                            type = TOKEN_TYPE.NULL;
                        } else if (prevType == TOKEN_TYPE.COLON && nearestWrapperType == TOKEN_TYPE.OBJECT_START) {
                            type = TOKEN_TYPE.NULL;
                        } else {
                            dumpError();
                        }
                    } else {
                        dumpError();
                    }
                }
                break;
                case 't': {
                    if (typeStack.size() >= 1) {
                        TOKEN_TYPE prevType = typeStack.peek();
                        TOKEN_TYPE nearestWrapperType = getNearestWrapperType();
                        char lastNonemptyCharacter = getLastNonemptyCharacter();
                        if (prevType == TOKEN_TYPE.ARRAY_START&&'['==lastNonemptyCharacter) {
                            type = TOKEN_TYPE.TRUE;

                        } else if (nearestWrapperType == TOKEN_TYPE.ARRAY_START&&COMMA==lastNonemptyCharacter) {
                            type = TOKEN_TYPE.TRUE;
                        } else if (prevType == TOKEN_TYPE.COLON && nearestWrapperType == TOKEN_TYPE.OBJECT_START) {
                            type = TOKEN_TYPE.TRUE;
                        } else {
                            dumpError();
                        }
                    } else {
                        dumpError();
                    }
                }
                break;
                case 'f': {
                    if (typeStack.size() >= 1) {
                        TOKEN_TYPE prevType = typeStack.peek();
                        TOKEN_TYPE nearestWrapperType = getNearestWrapperType();
                        char lastNonemptyCharacter = getLastNonemptyCharacter();
                        if (prevType == TOKEN_TYPE.ARRAY_START&&'['==lastNonemptyCharacter) {
                            type = TOKEN_TYPE.FALSE;

                        } else if (nearestWrapperType == TOKEN_TYPE.ARRAY_START&&COMMA==lastNonemptyCharacter) {
                            type = TOKEN_TYPE.FALSE;
                        } else if (prevType == TOKEN_TYPE.COLON && nearestWrapperType == TOKEN_TYPE.OBJECT_START) {
                            type = TOKEN_TYPE.FALSE;
                        } else {
                            dumpError();
                        }
                    } else {
                        dumpError();
                    }
                }
                break;
                case ':': {
                    if (typeStack.size() >= 1) {
                        TOKEN_TYPE prevType = typeStack.peek();
                        if (prevType == TOKEN_TYPE.KEY) {
                            type = TOKEN_TYPE.COLON;

                        } else {
                            dumpError();
                        }
                    } else {
                        dumpError();
                    }
                }
                break;
                case ',': {
                    type = TOKEN_TYPE.COMMA;
                    TOKEN_TYPE nearestWrapperType = getNearestWrapperType();
                    if (nearestWrapperType==TOKEN_TYPE.ARRAY_START||nearestWrapperType==TOKEN_TYPE.OBJECT_START){

                    } else {
                        dumpError();
                    }
//                    if (typeStack.size() >= 1) {
//                        TOKEN_TYPE prevType = typeStack.peek();
//                        TOKEN_TYPE nearestWrapperType = getNearestWrapperType();
//                        if (nearestWrapperType == TOKEN_TYPE.ARRAY_START) {
//                            if (prevType == TOKEN_TYPE.NULL || prevType == TOKEN_TYPE.TRUE || prevType == TOKEN_TYPE.FALSE || prevType == TOKEN_TYPE.NUMBER ||
//                                    prevType == TOKEN_TYPE.STRING || prevType == TOKEN_TYPE.OBJECT_END || prevType == TOKEN_TYPE.ARRAY_END) {
//                                type = TOKEN_TYPE.COMMA;
//
//                            } else {
//                                dumpError();
//                            }
//
//                        } else if (nearestWrapperType == TOKEN_TYPE.OBJECT_START) {
//                            if (typeStack.size() >= 2) {
//                                TOKEN_TYPE prevPrevType = typeStack.get(typeStack.size() - 2);
//                                if (prevType == TOKEN_TYPE.STRING && prevPrevType == TOKEN_TYPE.COLON) {
//                                    type = TOKEN_TYPE.COMMA;
//                                } else if (prevType == TOKEN_TYPE.OBJECT_END) {
//                                    type = TOKEN_TYPE.COMMA;
//                                } else if (prevType == TOKEN_TYPE.ARRAY_END) {
//                                    type = TOKEN_TYPE.COMMA;
//                                } else {
//                                    dumpError();
//                                }
//                            } else {
//                                dumpError();
//                            }
//                        } else {
//                            dumpError();
//                        }
//                    } else {
//                        dumpError();
//                    }
                }
                break;
                default: {
                    type = TOKEN_TYPE.END;
                }
                break;
            }
        }

        return type;
    }

    private TOKEN_TYPE getNearestWrapperType() {
        TOKEN_TYPE nearestWrapperType = TOKEN_TYPE.END;
        int beginEndTypeCount = 0;
        for (int i = typeStack.size() - 1; i >= 0; i--) {
            TOKEN_TYPE tempType = typeStack.get(i);
            if (tempType == TOKEN_TYPE.OBJECT_START || tempType == TOKEN_TYPE.ARRAY_START) {
                ++beginEndTypeCount;
                if (isOdd(beginEndTypeCount)) {
                    nearestWrapperType = tempType;
                    break;
                }
            } else if (tempType == TOKEN_TYPE.OBJECT_END || tempType == TOKEN_TYPE.ARRAY_END) {
                ++beginEndTypeCount;
            }
        }
        return nearestWrapperType;
    }

    private boolean isOdd(int i) {
        return (i & 1) == 1;
    }

    private void dumpError() {
        String errorString = "index at " + index + " error";
        LogUtil.ii(errorString);
        throw new RuntimeException(errorString);
    }

    private void skipSpace() {
        while (index < jsonString.length()) {
            char c = jsonString.charAt(index);
            if (Character.isWhitespace(c)) {
                ++index;
            } else {
                break;
            }
        }

    }

    private JsonParser(Context context) {
        this.context = context;
    }

    private JsonParser(Context context, String jsonString) {
        this.context = context;
        this.jsonString = jsonString;
    }

    public static synchronized JsonParser getInstance(Context context) {
        if (instance == null) {
            instance = new JsonParser(context);
        }
        return instance;
    }

    public static synchronized JsonParser getInstance(Context context, String jsonString) {
        if (instance == null) {
            instance = new JsonParser(context, jsonString);
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
