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

import com.datasophon.api.service.ClusterServiceInstanceConfigService;
import com.datasophon.api.service.ClusterServiceRoleGroupConfigService;
import com.datasophon.common.Constants;
import com.datasophon.common.model.ServiceConfig;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterServiceInstanceConfigEntity;
import com.datasophon.dao.entity.ClusterServiceRoleGroupConfig;
import com.datasophon.dao.mapper.ClusterServiceInstanceConfigMapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service("clusterServiceInstanceConfigService")
public class ClusterServiceInstanceConfigServiceImpl
        extends
            ServiceImpl<ClusterServiceInstanceConfigMapper, ClusterServiceInstanceConfigEntity>
        implements
            ClusterServiceInstanceConfigService {
    
    @Autowired
    private ClusterServiceRoleGroupConfigService roleGroupConfigService;
    
    @Override
    public Result getServiceInstanceConfig(Integer serviceInstanceId, Integer version, Integer roleGroupId,
                                           Integer page, Integer pageSize) {
        ClusterServiceRoleGroupConfig roleGroupConfig =
                roleGroupConfigService.getConfigByRoleGroupIdAndVersion(roleGroupId, version);
        if (Objects.nonNull(roleGroupConfig)) {
            String configJson = roleGroupConfig.getConfigJson();
            List<ServiceConfig> serviceConfigs = JSONObject.parseArray(configJson, ServiceConfig.class);
            return Result.success(serviceConfigs);
        }
        return Result.success();
    }
    
    @Override
    public ClusterServiceInstanceConfigEntity getServiceConfigByServiceId(Integer id) {
        return this.lambdaQuery()
                .eq(ClusterServiceInstanceConfigEntity::getServiceId, id)
                .orderByDesc(ClusterServiceInstanceConfigEntity::getConfigVersion)
                .last("limit 1")
                .one();
    }
    
    @Override
    public Result getConfigVersion(Integer serviceInstanceId, Integer roleGroupId) {
        
        List<ClusterServiceRoleGroupConfig> list =
                roleGroupConfigService.list(new QueryWrapper<ClusterServiceRoleGroupConfig>()
                        .eq(Constants.ROLE_GROUP_ID, roleGroupId)
                        .orderByDesc(Constants.CONFIG_VERSION));
        List<Integer> versions = list.stream().map(e -> e.getConfigVersion()).collect(Collectors.toList());
        return Result.success(versions);
    }
}
