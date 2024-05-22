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

import com.datasophon.api.service.ClusterVariableService;
import com.datasophon.common.Constants;
import com.datasophon.dao.entity.ClusterVariable;
import com.datasophon.dao.mapper.ClusterVariableMapper;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service("clusterVariableService")
public class ClusterVariableServiceImpl extends ServiceImpl<ClusterVariableMapper, ClusterVariable>
        implements
            ClusterVariableService {
    
    @Override
    public ClusterVariable getVariableByVariableName(String variableName, Integer clusterId) {
        List<ClusterVariable> list = this.list(new QueryWrapper<ClusterVariable>()
                .eq(Constants.VARIABLE_NAME, variableName).eq(Constants.CLUSTER_ID, clusterId));
        if (Objects.nonNull(list) && list.size() >= 1) {
            return list.get(0);
        }
        return null;
    }
    
    @Override
    public List<ClusterVariable> getVariables(Integer clusterId, String serviceName) {
        return this.list(new LambdaQueryWrapper<ClusterVariable>()
                .eq(ClusterVariable::getClusterId, clusterId)
                .eq(ClusterVariable::getServiceName, serviceName));
    }
}
