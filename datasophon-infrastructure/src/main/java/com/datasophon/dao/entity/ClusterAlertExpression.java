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
@TableName("t_ddh_cluster_alert_expression")
public class ClusterAlertExpression implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 自增 ID
     */
    @TableId
    private Long id;
    /**
     * 指标名称
     */
    private String name;
    /**
     * 监控指标表达式
     */
    private String expr;
    /**
     * 服务类别
     */
    private String serviceCategory;
    /**
     * 阈值类型  BOOL  INT  FLOAT  
     */
    private String valueType;
    /**
     * 是否预定义
     */
    private String isPredefined;
    /**
     * 表达式状态
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
    
}
