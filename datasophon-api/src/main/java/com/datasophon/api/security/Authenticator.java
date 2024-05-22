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

package com.datasophon.api.security;

import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.UserInfoEntity;

import javax.servlet.http.HttpServletRequest;

public interface Authenticator {
    
    /**
     * Verifying legality via username and password
     *
     * @param username user name
     * @param password user password
     * @param extra    extra info
     * @return result object
     */
    Result authenticate(String username, String password, String extra);
    
    /**
     * Get authenticated user
     *
     * @param request http servlet request
     * @return user
     */
    UserInfoEntity getAuthUser(HttpServletRequest request);
}
