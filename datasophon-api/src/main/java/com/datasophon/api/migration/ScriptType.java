/*
 *
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
 *
 */

package com.datasophon.api.migration;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

@Getter
public enum ScriptType {
    
    UPGRADE("V"),
    
    ROLLBACK("R");
    
    private final String prefix;
    
    ScriptType(String prefix) {
        this.prefix = prefix;
    }
    
    public static ScriptType of(String version) {
        if (StringUtils.isBlank(version)) {
            return null;
        }
        if (version.startsWith(UPGRADE.getPrefix())) {
            return UPGRADE;
        } else if (version.startsWith(ROLLBACK.getPrefix())) {
            return ROLLBACK;
        } else {
            return null;
        }
    }
}
