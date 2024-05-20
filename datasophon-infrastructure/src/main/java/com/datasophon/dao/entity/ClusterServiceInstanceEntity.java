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

import com.datasophon.dao.enums.NeedRestart;
import com.datasophon.dao.enums.ServiceState;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_ddh_cluster_service_instance")
@Data
public class ClusterServiceInstanceEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 集群id
     */
    private Integer clusterId;
    /**
     * 服务名称
     */
    private String serviceName;
    
    private String label;
    /**
     * 服务状态 1、待安装 2：正在运行  3：存在告警 4:存在异常
     */
    private ServiceState serviceState;
    
    @TableField(exist = false)
    private Integer serviceStateCode;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 创建时间
     */
    private Date createTime;
    
    private NeedRestart needRestart;
    
    private Integer frameServiceId;
    
    @TableField(exist = false)
    private String dashboardUrl;
    
    @TableField(exist = false)
    private Integer alertNum;
    
    private Integer sortNum;
}
