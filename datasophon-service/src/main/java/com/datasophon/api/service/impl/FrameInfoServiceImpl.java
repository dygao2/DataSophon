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

import com.datasophon.api.service.FrameInfoService;
import com.datasophon.api.service.FrameServiceService;
import com.datasophon.common.utils.CollectionUtils;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.FrameInfoEntity;
import com.datasophon.dao.entity.FrameServiceEntity;
import com.datasophon.dao.mapper.FrameInfoMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service("frameInfoService")
public class FrameInfoServiceImpl extends ServiceImpl<FrameInfoMapper, FrameInfoEntity> implements FrameInfoService {
    
    @Autowired
    private FrameServiceService frameServiceService;
    
    @Override
    public Result getAllClusterFrame() {
        List<FrameInfoEntity> frameInfoEntities = this.list();
        if (CollectionUtils.isEmpty(frameInfoEntities)) {
            return Result.success();
        }
        
        Set<Integer> frameInfoIds = frameInfoEntities.stream().map(FrameInfoEntity::getId).collect(Collectors.toSet());
        Map<Integer, List<FrameServiceEntity>> frameServiceGroupBys = frameServiceService.lambdaQuery()
                .select(FrameServiceEntity::getId, FrameServiceEntity::getFrameId, FrameServiceEntity::getFrameCode,
                        FrameServiceEntity::getServiceName, FrameServiceEntity::getServiceVersion,
                        FrameServiceEntity::getServiceDesc)
                .in(FrameServiceEntity::getFrameId, frameInfoIds)
                .list()
                .stream()
                .collect(Collectors.groupingBy(FrameServiceEntity::getFrameId));
        frameInfoEntities.forEach(f -> f.setFrameServiceList(frameServiceGroupBys.get(f.getId())));
        
        return Result.success(frameInfoEntities);
    }
}
