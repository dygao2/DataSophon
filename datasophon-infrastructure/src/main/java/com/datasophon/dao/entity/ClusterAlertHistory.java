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

import com.datasophon.dao.enums.AlertLevel;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@Builder
@TableName("t_ddh_cluster_alert_history")
@NoArgsConstructor
@AllArgsConstructor
public class ClusterAlertHistory implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 告警组
     */
    private String alertGroupName;
    /**
     * 告警指标
     */
    private String alertTargetName;
    /**
     * 告警详情
     */
    private String alertInfo;
    /**
     * 告警建议
     */
    private String alertAdvice;
    /**
     * 主机
     */
    private String hostname;
    /**
     * 告警级别 1：警告2：异常
     */
    private AlertLevel alertLevel;
    /**
     * 是否处理 1:未处理2：已处理
     */
    private Integer isEnabled;
    /**
     * 集群服务角色实例id
     */
    private Integer serviceRoleInstanceId;
    /**
     * 集群服务实例id
     */
    private Integer serviceInstanceId;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 集群id
     */
    private Integer clusterId;
    
}
