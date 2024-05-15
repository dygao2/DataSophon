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

package com.datasophon.common.model;

import lombok.Data;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Title: prometheus指标属性（这里只反序列化了需要的属性，用户可根据需要做增减）
 */
@Data
public class PromMetricInfo {
    
    /**
     * prometheus指标名称
     */
    @JsonProperty(value = "__name__")
    @JSONField(name = "__name__")
    private String name;
    
    /**
     * prometheus实例名称
     */
    private String instance;
    
    /**
     * prometheus任务名称
     */
    private String job;
    
}
