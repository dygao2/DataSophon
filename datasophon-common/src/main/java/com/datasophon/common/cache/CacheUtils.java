/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.datasophon.common.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;

/**
 * Cache工具类
 */
public class CacheUtils {
    
    private static Logger logger = LoggerFactory.getLogger(CacheUtils.class);
    private static Cache<String, Object> cache = CacheUtil.newLRUCache(4096);
    
    public static Object get(String key) {
        Object data = cache.get(key);
        return data;
    }
    
    public static void put(String key, Object value) {
        cache.put(key, value);
    }
    
    public static boolean constainsKey(String key) {
        return cache.containsKey(key);
    }
    
    public static void removeKey(String key) {
        cache.remove(key);
    }
    
    public static Integer getInteger(String key) {
        Object data = cache.get(key);
        return (Integer) data;
    }
    
    public static Boolean getBoolean(String key) {
        Object data = cache.get(key);
        return (Boolean) data;
    }
    
    public static String getString(String key) {
        Object data = cache.get(key);
        return (String) data;
    }
    
}
