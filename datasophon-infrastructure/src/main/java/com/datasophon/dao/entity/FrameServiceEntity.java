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

import lombok.Data;
import lombok.experimental.Accessors;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_ddh_frame_service")
@Data
@Accessors(chain = true)
public class FrameServiceEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 框架id
     */
    private Integer frameId;
    /**
     * 服务名称
     */
    private String serviceName;
    
    private String label;
    /**
     * 服务版本
     */
    private String serviceVersion;
    /**
     * 服务描述
     */
    private String serviceDesc;
    
    private String packageName;
    
    private String dependencies;
    
    private String serviceJson;
    
    private String serviceJsonMd5;
    
    private String serviceConfig;
    
    private String frameCode;
    
    private String configFileJson;
    
    private String configFileJsonMd5;
    
    private String decompressPackageName;
    
    @TableField(exist = false)
    private Boolean installed;
    
    private Integer sortNum;
    
}
