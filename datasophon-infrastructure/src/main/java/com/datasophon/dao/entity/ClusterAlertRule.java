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

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("t_ddh_cluster_alert_rule")
public class ClusterAlertRule implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 自增 ID
     */
    @TableId
    private Long id;
    /**
     * 表达式 ID
     */
    private Long expressionId;
    /**
     * 是否预定义
     */
    private String isPredefined;
    /**
     * 比较方式 如 大于 小于 等于 等
     */
    private String compareMethod;
    /**
     * 阈值
     */
    private String thresholdValue;
    /**
     * 持续时长
     */
    private Long persistentTime;
    /**
     * 告警策略：单次，连续
     */
    private String strategy;
    /**
     * 连续告警时 间隔时长
     */
    private Long repeatInterval;
    /**
     * 告警级别
     */
    private String alertLevel;
    /**
     * 告警描述
     */
    private String alertDesc;
    /**
     * 接收组 ID
     */
    private Long receiverGroupId;
    /**
     * 状态
     */
    private String state;
    /**
     * 是否删除
     */
    private String isDelete;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date updateTime;
    /**
     * 集群id
     */
    private Integer clusterId;
    
}
