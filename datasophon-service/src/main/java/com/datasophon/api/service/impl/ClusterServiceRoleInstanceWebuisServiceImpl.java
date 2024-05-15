/*
 *
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
 *
 */

package com.datasophon.api.service.impl;

import com.datasophon.api.service.ClusterServiceRoleInstanceWebuisService;
import com.datasophon.common.Constants;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceWebuis;
import com.datasophon.dao.mapper.ClusterServiceRoleInstanceWebuisMapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service("clusterServiceRoleInstanceWebuisService")
public class ClusterServiceRoleInstanceWebuisServiceImpl
        extends
            ServiceImpl<ClusterServiceRoleInstanceWebuisMapper, ClusterServiceRoleInstanceWebuis>
        implements
            ClusterServiceRoleInstanceWebuisService {
    
    private static final String ACTIVE = "(Active)";
    
    private static final String STANDBY = "(Standby)";
    
    @Override
    public Result getWebUis(Integer serviceInstanceId) {
        List<ClusterServiceRoleInstanceWebuis> list =
                this.list(
                        new QueryWrapper<ClusterServiceRoleInstanceWebuis>()
                                .eq(Constants.SERVICE_INSTANCE_ID, serviceInstanceId));
        return Result.success(list);
    }
    
    @Override
    public void removeByServiceInsId(Integer serviceInstanceId) {
        this.remove(
                new QueryWrapper<ClusterServiceRoleInstanceWebuis>()
                        .eq(Constants.SERVICE_INSTANCE_ID, serviceInstanceId));
    }
    
    @Override
    public void updateWebUiToActive(Integer roleInstanceId) {
        updateWebUiName(roleInstanceId, ACTIVE);
    }
    
    @Override
    public ClusterServiceRoleInstanceWebuis getRoleInstanceWebUi(Integer roleInstanceId) {
        return this.lambdaQuery()
                .eq(ClusterServiceRoleInstanceWebuis::getServiceRoleInstanceId, roleInstanceId)
                .one();
    }
    
    @Override
    public void removeByRoleInsIds(ArrayList<Integer> needRemoveList) {
        this.lambdaUpdate()
                .in(ClusterServiceRoleInstanceWebuis::getServiceRoleInstanceId, needRemoveList)
                .remove();
    }
    
    @Override
    public void updateWebUiToStandby(Integer roleInstanceId) {
        updateWebUiName(roleInstanceId, STANDBY);
    }
    
    @Override
    public List<ClusterServiceRoleInstanceWebuis> listWebUisByServiceInstanceId(Integer serviceInstanceId) {
        return this.list(
                new QueryWrapper<ClusterServiceRoleInstanceWebuis>()
                        .eq(Constants.SERVICE_INSTANCE_ID, serviceInstanceId));
    }
    
    private void updateWebUiName(Integer roleInstanceId, String state) {
        ClusterServiceRoleInstanceWebuis webuis =
                this.lambdaQuery()
                        .eq(
                                ClusterServiceRoleInstanceWebuis::getServiceRoleInstanceId,
                                roleInstanceId)
                        .one();
        String webuiName = webuis.getName();
        boolean needUpdate = false;
        if (webuiName.contains(ACTIVE) && STANDBY.equals(state)) {
            webuiName = webuiName.replace(ACTIVE, STANDBY);
            needUpdate = true;
        }
        if (webuiName.contains(STANDBY) && ACTIVE.equals(state)) {
            webuiName = webuiName.replace(STANDBY, ACTIVE);
            needUpdate = true;
        }
        webuis.setName(webuiName);
        if (!webuiName.contains(ACTIVE) && !webuiName.contains(STANDBY)) {
            webuis.setName(webuis.getName() + state);
            needUpdate = true;
        }
        if (needUpdate) {
            this.lambdaUpdate()
                    .eq(ClusterServiceRoleInstanceWebuis::getServiceRoleInstanceId, roleInstanceId)
                    .update(webuis);
        }
    }
}
