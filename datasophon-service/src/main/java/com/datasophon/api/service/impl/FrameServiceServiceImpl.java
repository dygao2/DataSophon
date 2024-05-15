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

import com.datasophon.api.service.ClusterInfoService;
import com.datasophon.api.service.ClusterServiceInstanceService;
import com.datasophon.api.service.FrameServiceService;
import com.datasophon.common.Constants;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterInfoEntity;
import com.datasophon.dao.entity.ClusterServiceInstanceEntity;
import com.datasophon.dao.entity.FrameInfoEntity;
import com.datasophon.dao.entity.FrameServiceEntity;
import com.datasophon.dao.enums.ServiceState;
import com.datasophon.dao.mapper.FrameInfoMapper;
import com.datasophon.dao.mapper.FrameServiceMapper;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service("frameServiceService")
public class FrameServiceServiceImpl extends ServiceImpl<FrameServiceMapper, FrameServiceEntity>
        implements
            FrameServiceService {
    
    @Autowired
    ClusterInfoService clusterInfoService;
    
    @Autowired
    FrameInfoMapper frameInfoMapper;
    
    @Autowired
    ClusterServiceInstanceService serviceInstanceService;
    
    @Override
    public Result getAllFrameService(Integer clusterId) {
        ClusterInfoEntity clusterInfo = clusterInfoService.getById(clusterId);
        FrameInfoEntity frameInfo = frameInfoMapper.getFrameInfoByFrameCode(clusterInfo.getClusterFrame());
        List<FrameServiceEntity> list = this.lambdaQuery()
                .eq(FrameServiceEntity::getFrameId, frameInfo.getId())
                .orderByAsc(FrameServiceEntity::getSortNum)
                .list();
        setInstalled(clusterId, list);
        return Result.success(list);
    }
    
    private void setInstalled(Integer clusterId, List<FrameServiceEntity> list) {
        for (FrameServiceEntity serviceEntity : list) {
            ClusterServiceInstanceEntity serviceInstance = serviceInstanceService
                    .getServiceInstanceByClusterIdAndServiceName(clusterId, serviceEntity.getServiceName());
            if (Objects.nonNull(serviceInstance)
                    && !serviceInstance.getServiceState().equals(ServiceState.WAIT_INSTALL)) {
                serviceEntity.setInstalled(true);
            } else {
                serviceEntity.setInstalled(false);
            }
        }
    }
    
    @Override
    public Result getServiceListByServiceIds(List<Integer> serviceIds) {
        Collection<FrameServiceEntity> list = this.listByIds(serviceIds);
        return Result.success(list);
    }
    
    @Override
    public FrameServiceEntity getServiceByFrameIdAndServiceName(Integer frameId, String serviceName) {
        return this.lambdaQuery()
                .eq(FrameServiceEntity::getFrameId, frameId)
                .eq(FrameServiceEntity::getServiceName, serviceName)
                .one();
    }
    
    @Override
    public FrameServiceEntity getServiceByFrameCodeAndServiceName(String clusterFrame, String serviceName) {
        return this.getOne(new QueryWrapper<FrameServiceEntity>()
                .eq(Constants.FRAME_CODE_1, clusterFrame)
                .eq(Constants.SERVICE_NAME, serviceName));
    }
    
    @Override
    public List<FrameServiceEntity> getAllFrameServiceByFrameCode(String clusterFrame) {
        return this.list(new QueryWrapper<FrameServiceEntity>().eq(Constants.FRAME_CODE_1, clusterFrame));
    }
    
    @Override
    public List<FrameServiceEntity> listServices(String serviceIds) {
        List<String> ids = Arrays.stream(serviceIds.split(",")).collect(Collectors.toList());
        return this.lambdaQuery().in(FrameServiceEntity::getId, ids).list();
    }
    
}
