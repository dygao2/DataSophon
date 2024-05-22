package com.datasophon.api.master;

import java.util.HashMap;

public class CancelCommandMap {
    
    private static HashMap<String, String> map = new HashMap<String, String>();
    
    public static void put(String key, String value) {
        map.put(key, value);
    }
    
    public static String get(String key) {
        return map.get(key);
    }
    
    public static boolean exists(String key) {
        return map.containsKey(key);
    }
}
