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

package com.datasophon.api.utils;

import com.datasophon.common.Constants;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class HttpUtils {
    
    public static String getClientIpAddress(HttpServletRequest request) {
        String clientIp = request.getHeader(Constants.HTTP_X_FORWARDED_FOR);
        
        if (StringUtils.isNotEmpty(clientIp) && !clientIp.equalsIgnoreCase(Constants.HTTP_HEADER_UNKNOWN)) {
            int index = clientIp.indexOf(Constants.COMMA);
            if (index != -1) {
                return clientIp.substring(0, index);
            } else {
                return clientIp;
            }
        }
        
        clientIp = request.getHeader(Constants.HTTP_X_REAL_IP);
        if (StringUtils.isNotEmpty(clientIp) && !clientIp.equalsIgnoreCase(Constants.HTTP_HEADER_UNKNOWN)) {
            return clientIp;
        }
        
        return request.getRemoteAddr();
    }
}
