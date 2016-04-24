package com.chenxu.jsonparser;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/23 0023.
 */
public class Map2BeanUtil {

    public static void transformMapOrList2Bean(Object mapOrList, Object bean) {//mapOrList must be a map,cannot be a list
        if (mapOrList == null || bean == null) {
        } else {
            try {
                if (mapOrList instanceof Map<?, ?>) {
                    Map<String, Object> map = (Map<String, Object>) mapOrList;
                    Class clazz = bean.getClass();
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        Field field = clazz.getDeclaredField(key);
                        field.setAccessible(true);
                        if (value instanceof Map<?, ?>) {
                            Class type = field.getType();
                            Object subbean = type.newInstance();
                            Map<String, Object> submap = (Map<String, Object>) value;
                            transformMapOrList2Bean(submap, subbean);
                            field.set(bean, subbean);
                        } else if (value instanceof List<?>) {
                            Class cls = field.getType();
                            if (cls == List.class) {
                                cls = ArrayList.class;
                            }
                            String typeString = "";
                            System.out.println("type:" + cls);
                            Type genericType = field.getGenericType();
                            System.out.println("genericType:" + genericType);
                            typeString=getGenericClassString(genericType.toString());
//                            if (genericType instanceof ParameterizedType) {
//                                ParameterizedType parameterizedType = (ParameterizedType) genericType;
//                                typeString = parameterizedType.getActualTypeArguments()[0].toString();
//                                System.out.println("typeString:" + typeString);
//                            } else {
//                            }
                            LogUtil.ii("typeString:" + typeString);
                            Object subbean = cls.newInstance();
                            List<Object> sublist = (List<Object>) value;
                            transformList2Bean(sublist, subbean, typeString);
                            field.set(bean, subbean);

                        } else {
                            field.set(bean, value);
                        }
                    }

                } else if (mapOrList instanceof List<?>) {
                    //******NOT FUNCTIONAL
                    List<Object> list = (List<Object>) mapOrList;
                    List<Object> listBean = (List<Object>) bean;
                    transformList2Bean(list, listBean, "");
                    //*******NOT FUNCTIONAL
                } else {
                    Object object = mapOrList;
                    bean = object;
                }
            } catch (Exception e) {
                e.printStackTrace();
//                throw new RuntimeException(e.getLocalizedMessage());
            }
        }
    }

    public static void transformList2Bean(Object alist, Object bean, String itemClassString) {
        List<Object> list = (List<Object>) alist;
        List<Object> listBean = (List<Object>) bean;
        System.out.println("itemClassString:" + itemClassString);
        try {
            for (int i = 0; i < list.size(); i++) {
                Object object = list.get(i);
                if (object instanceof Map<?, ?>) {
                    Class itemClass = Class.forName(itemClassString);
                    Object subbean = itemClass.newInstance();
                    Map<String, Object> map = (Map<String, Object>) object;
                    transformMapOrList2Bean(map, subbean);
                    listBean.add(subbean);
                } else if (object instanceof List<?>) {
                    List<Object> subbean = new ArrayList<>();
                    List<Object> lst = (List<Object>) object;
                    String subClassString = getGenericClassString(itemClassString);
                    System.out.println("subClassString:" + subClassString);
                    transformList2Bean(lst, subbean, subClassString);
                    listBean.add(subbean);
                } else {
                    listBean.add(object);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
//            throw new RuntimeException(e.getLocalizedMessage());
        }

    }

    public static String getGenericClassString(String className) {
        int firstIndexOfLeftBrace = className.indexOf("<");
        int lastIndexOfRightBrace = className.lastIndexOf(">");
        String genericClassName = className.substring(firstIndexOfLeftBrace + 1, lastIndexOfRightBrace);
        System.out.println("genericClassName:" + genericClassName);
        return genericClassName;
    }


}
