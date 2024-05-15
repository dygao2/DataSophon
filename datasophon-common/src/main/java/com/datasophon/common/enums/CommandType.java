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

package com.datasophon.common.enums;

import com.datasophon.common.Constants;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CommandType {
    
    // 命令类型1：安装服务 2：启动服务 3：停止服务 4：重启服务 5：更新配置后启动 6：更新配置后重启
    INSTALL_SERVICE(1, "INSTALL", "安装"),
    START_SERVICE(2, "START", "启动"),
    STOP_SERVICE(3, "STOP", "停止"),
    RESTART_SERVICE(4, "RESTART", "重启"),
    START_WITH_CONFIG(5, "START_WITH_CONFIG", ""),
    RESTART_WITH_CONFIG(6, "RESTART_WITH_CONFIG", "znDesc");
    
    private int value;
    
    private String desc;
    
    private String cnDesc;
    
    CommandType(int value, String desc, String cnDesc) {
        this.value = value;
        this.desc = desc;
        this.cnDesc = cnDesc;
    }
    
    public int getValue() {
        return value;
    }
    
    public void setValue(int value) {
        this.value = value;
    }
    
    @JsonValue
    public String getDesc() {
        return desc;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    public String getCnDesc() {
        return cnDesc;
    }
    
    public void setZnDesc(String cnDesc) {
        this.cnDesc = cnDesc;
    }
    
    public String getCommandName(String language) {
        if (Constants.CN.equals(language)) {
            return this.cnDesc;
        } else {
            return this.desc;
        }
    }
    
    @Override
    public String toString() {
        return this.desc;
    }
    
}
