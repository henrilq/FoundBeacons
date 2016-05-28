package com.a60circuits.foundbeacons.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zoz on 28/05/2016.
 */
public class CacheVariable {
    private Map<String, Object> map = new HashMap<>();

    public static CacheVariable getInstance(){
        return CacheVariableHolder.instance;
    }

    public static void put(String key, Object value){
        getInstance().map.put(key, value);
    }

    public static Object get(String key){
        return getInstance().map.get(key);
    }

    private static class CacheVariableHolder{
        private static CacheVariable instance = new CacheVariable();
    }
}
