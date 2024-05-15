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

package com.datasophon.dao.model;

import java.util.List;

import lombok.Data;

/**
 *
 * 第三方框架组件
 *
 *
 * @author zhenqin
 */
@Data
public class ParcelInfoVO {
    
    /**
     * Parcel Remote URL
     */
    String url;
    
    /**
     * Parcel 名称
     */
    String parcelName;
    
    /**
     * hash 256 验证
     */
    String hash;
    
    /**
     * md5 验证
     */
    String md5;
    
    /**
     * 依赖的 DDP 版本
     */
    String depends;
    
    /**
     * 支持的 DDP Frame 框架版本
     */
    String meta;
    
    /**
     * 内部包含的组件
     */
    List<ComponentVO> components;
    
    /**
     * 最后修改时间
     */
    long lastUpdated;
    
}
