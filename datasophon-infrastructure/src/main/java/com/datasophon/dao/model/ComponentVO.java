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

package com.datasophon.dao.model;

import lombok.Data;

/**
 *
 * 第三方框架服务描述信息，安装前校验，步骤等信息
 *
 *
 * @author zhenqin
 */
@Data
public class ComponentVO {
    
    String name;
    String label;
    String version;
    String packageName;
    String hash;
    String md5;
    
    /**
     * 系统架构，x86、x86_64、aarch64、RISC-V
     */
    String arch;
    String description;
    
    /**
     * 支持的 frame 版本
     */
    String meta;
    
    /**
     * 当前阶段：下载(download)、验证(valid)、安装(install)
     */
    String step;
    
    /**
     * 当前状态，executing, success, fail
     */
    String state;
    
    /**
     * 下载进度：0 - 1 之间
     */
    float process = 0.0f;
}
