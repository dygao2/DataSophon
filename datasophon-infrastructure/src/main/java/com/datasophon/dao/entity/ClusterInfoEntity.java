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

import com.datasophon.dao.enums.ClusterState;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("t_ddh_cluster_info")
public class ClusterInfoEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * 集群编码
     */
    private String clusterCode;
    /**
     * 集群框架
     */
    private String clusterFrame;
    /**
     * 集群版本
     */
    private String frameVersion;
    /**
     * 集群状态 1:待配置 2：正在运行 3: 停止 4: 删除中 5: 已删除
     */
    private ClusterState clusterState;
    /**
     * 集群框架id
     */
    private Integer frameId;
    
    @TableField(exist = false)
    private List<UserInfoEntity> clusterManagerList;
    
    @TableField(exist = false)
    private Integer clusterStateCode;
    
}
