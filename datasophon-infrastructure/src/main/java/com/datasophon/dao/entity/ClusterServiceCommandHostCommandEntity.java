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

package com.datasophon.dao.entity;

import com.datasophon.dao.enums.CommandState;
import com.datasophon.dao.enums.RoleType;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_ddh_cluster_service_command_host_command")
@Data
public class ClusterServiceCommandHostCommandEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */
    private String hostCommandId;
    /**
     * 指令名称
     */
    private String commandName;
    /**
     * 指令状态 1、正在运行2：成功3：失败
     */
    private CommandState commandState;
    
    @TableField(exist = false)
    private Integer commandStateCode;
    /**
     * 指令进度
     */
    private Integer commandProgress;
    /**
     * 主机id
     */
    private String commandHostId;
    
    private String commandId;
    
    private String hostname;
    /**
     * 服务角色名称
     */
    private String serviceRoleName;
    
    private RoleType serviceRoleType;
    
    private String resultMsg;
    
    private Date createTime;
    
    private Integer commandType;
    
}
