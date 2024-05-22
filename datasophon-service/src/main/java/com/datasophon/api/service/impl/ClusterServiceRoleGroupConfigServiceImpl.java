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

package com.datasophon.api.service.impl;

import com.datasophon.api.service.ClusterServiceRoleGroupConfigService;
import com.datasophon.common.Constants;
import com.datasophon.dao.entity.ClusterServiceRoleGroupConfig;
import com.datasophon.dao.mapper.ClusterServiceRoleGroupConfigMapper;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service("clusterServiceRoleGroupConfigService")
public class ClusterServiceRoleGroupConfigServiceImpl
        extends
            ServiceImpl<ClusterServiceRoleGroupConfigMapper, ClusterServiceRoleGroupConfig>
        implements
            ClusterServiceRoleGroupConfigService {
    
    @Override
    public ClusterServiceRoleGroupConfig getConfigByRoleGroupId(Integer roleGroupId) {
        return this.getOne(new QueryWrapper<ClusterServiceRoleGroupConfig>()
                .eq(Constants.ROLE_GROUP_ID, roleGroupId).orderByDesc(Constants.CONFIG_VERSION).last("limit 1"));
    }
    
    @Override
    public ClusterServiceRoleGroupConfig getConfigByRoleGroupIdAndVersion(Integer roleGroupId, Integer version) {
        return this.getOne(new QueryWrapper<ClusterServiceRoleGroupConfig>()
                .eq(Constants.ROLE_GROUP_ID, roleGroupId).eq(Constants.CONFIG_VERSION, version));
    }
    
    @Override
    public void removeAllByRoleGroupId(Integer roleGroupId) {
        this.remove(new QueryWrapper<ClusterServiceRoleGroupConfig>()
                .eq(Constants.ROLE_GROUP_ID, roleGroupId));
    }
    
    @Override
    public List<ClusterServiceRoleGroupConfig> listRoleGroupConfigsByRoleGroupIds(List<Integer> roleGroupIds) {
        List<ClusterServiceRoleGroupConfig> list = this.list(new QueryWrapper<ClusterServiceRoleGroupConfig>()
                .in(Constants.ROLE_GROUP_ID, roleGroupIds));
        return list;
    }
}
