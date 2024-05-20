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

package com.datasophon.dao.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClusterState {
    
    DELETING(4, "删除中"),
    
    STOP(3, "停止"),
    
    RUNNING(2, "正在运行"),
    NEED_CONFIG(1, "待配置");
    
    @EnumValue
    private int value;
    
    private String desc;
    
    ClusterState(int value, String desc) {
        this.value = value;
        this.desc = desc;
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
    
    public static ClusterState of(int value) {
        return Arrays.stream(values()).filter(state -> state.getValue() == value).findAny().orElse(null);
    }
    
    @Override
    public String toString() {
        return this.desc;
    }
}
