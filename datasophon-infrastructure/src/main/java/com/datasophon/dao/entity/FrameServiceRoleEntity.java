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

import com.datasophon.dao.enums.RoleType;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_ddh_frame_service_role")
@Data
@Accessors(chain = true)
public class FrameServiceRoleEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 服务id
     */
    private Integer serviceId;
    /**
     * 角色名称
     */
    private String serviceRoleName;
    /**
     * 角色类型 1:master2:worker3:client
     */
    private RoleType serviceRoleType;
    /**
     * 1  1+
     */
    private String cardinality;
    
    private String serviceRoleJson;
    
    private String serviceRoleJsonMd5;
    
    private String frameCode;
    
    private String jmxPort;
    
    @TableField(exist = false)
    private List<String> hosts;
    
    private String logFile;
    
}
