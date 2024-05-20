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

import com.datasophon.domain.host.enums.HostState;
import com.datasophon.domain.host.enums.MANAGED;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_ddh_cluster_host")
@Data
public class ClusterHostDO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 主机名
     */
    private String hostname;
    /**
     * IP
     */
    private String ip;
    /**
     * 机架
     */
    private String rack;
    /**
     * 核数
     */
    private Integer coreNum;
    /**
     * 总内存
     */
    private Integer totalMem;
    /**
     * 总磁盘
     */
    private Integer totalDisk;
    /**
     * 已用内存
     */
    private Integer usedMem;
    /**
     * 已用磁盘
     */
    private Integer usedDisk;
    /**
     * 平均负载
     */
    private String averageLoad;
    /**
     * 检测时间
     */
    private Date checkTime;
    /**
     * 集群id
     */
    private Integer clusterId;
    /**
     * 1:正常运行 2：断线 3、存在告警
     */
    private HostState hostState;
    /**
     * 1:受管 2：断线
     */
    private MANAGED managed;
    
    private String cpuArchitecture;
    
    private String nodeLabel;
    
    @TableField(exist = false)
    private Integer serviceRoleNum;
    
}
