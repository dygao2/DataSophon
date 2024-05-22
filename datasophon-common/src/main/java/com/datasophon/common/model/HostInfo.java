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

import com.datasophon.common.enums.InstallState;

import java.util.Date;

import lombok.Data;

@Data
public class HostInfo {
    
    private String hostname;
    
    private String ip;
    /**
     * 是否受管
     */
    private boolean managed;
    
    /**
     * 检测结果
     */
    private CheckResult checkResult;
    
    private String sshUser;
    
    private Integer sshPort;
    /**
     * 安装进度
     */
    private Integer progress;
    
    private String clusterCode;
    
    /**
     * 安装状态1:正在安装 2：安装成功 3：安装失败
     */
    private InstallState installState;
    
    private Integer installStateCode;
    
    private String errMsg;
    
    private String message;
    
    private Date createTime;
    
    private String cpuArchitecture;
    
}
