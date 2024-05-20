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

import com.datasophon.common.enums.InstallState;
import com.datasophon.common.model.HostInfo;
import com.datasophon.dao.enums.RoleType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(CommonUtils.class);
    
    private CommonUtils() {
    }
    
    public static void updateInstallState(InstallState installState, HostInfo hostInfo) {
        hostInfo.setInstallState(installState);
        hostInfo.setInstallStateCode(installState.getValue());
    }
    
    public static RoleType convertRoleType(String roleType) {
        if (roleType == null || "".equals(roleType.trim())) {
            logger.error("Convert role type failed, roleType is null.");
            return null;
        }
        try {
            return RoleType.valueOf(roleType.toUpperCase());
        } catch (Exception e) {
            logger.error("Unsupported role type:{}", roleType);
            return null;
        }
    }
}
