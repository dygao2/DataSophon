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

package com.datasophon.api.load;

import com.datasophon.common.model.ServiceRoleInfo;

import java.util.HashMap;

public class ServiceRoleMap {
    
    private static HashMap<String, ServiceRoleInfo> map = new HashMap<String, ServiceRoleInfo>();
    
    public static void put(String key, ServiceRoleInfo value) {
        map.put(key, value);
    }
    
    public static ServiceRoleInfo get(String key) {
        return map.get(key);
    }
    
    public static boolean exists(String key) {
        return map.containsKey(key);
    }
}
