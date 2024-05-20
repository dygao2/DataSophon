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
import com.datasophon.dao.entity.UserInfoEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.hutool.core.convert.Convert;

public class SecurityUtils {
    
    public static HttpServletRequest getRequest() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request;
    }
    
    public static HttpSession getSession() {
        HttpSession session = getRequest().getSession();
        return session;
    }
    
    /**
     * 获取用户
     */
    public static String getUsername() {
        String username = getAuthUser().getUsername();
        return null == username ? null : ServletUtils.urlDecode(username);
    }
    
    /**
     * 获取用户ID
     */
    public static Long getUserId() {
        return Convert.toLong(ServletUtils.getRequest().getHeader(Constants.DETAILS_USER_ID));
    }
    
    /**
     * 是否为管理员
     *
     * @param userInfoEntity 用户
     * @return 结果
     */
    public static boolean isAdmin(UserInfoEntity userInfoEntity) {
        Integer userId = userInfoEntity.getId();
        return userId != null && 1 == userId;
    }
    
    public static UserInfoEntity getAuthUser() {
        return (UserInfoEntity) getRequest().getAttribute(Constants.SESSION_USER);
    }
}
