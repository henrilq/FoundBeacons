package com.a60circuits.foundbeacons.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zoz on 28/05/2016.
 */
public class CacheVariable {
    private Map<String, Object> map = new ConcurrentHashMap<>();

    public static CacheVariable getInstance(){
        return CacheVariableHolder.instance;
    }

    public static void put(String key, Object value){
        getInstance().map.put(key, value);
    }

    public static boolean getBoolean(String key){
        return getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defaultValue){
        boolean res = defaultValue;
        Object obj = getInstance().map.get(key);
        if(obj != null){
            res = (boolean)getInstance().map.get(key);
        }
        return res;
    }

    public static Object get(String key){
        return getInstance().map.get(key);
    }

    private static class CacheVariableHolder{
        private static CacheVariable instance = new CacheVariable();
    }
}
