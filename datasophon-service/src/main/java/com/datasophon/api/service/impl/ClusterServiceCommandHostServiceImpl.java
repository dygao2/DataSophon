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

import com.datasophon.api.service.ClusterServiceCommandHostCommandService;
import com.datasophon.api.service.ClusterServiceCommandHostService;
import com.datasophon.common.Constants;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterServiceCommandHostEntity;
import com.datasophon.dao.enums.CommandState;
import com.datasophon.dao.mapper.ClusterServiceCommandHostMapper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.additional.query.impl.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service("clusterServiceCommandHostService")
public class ClusterServiceCommandHostServiceImpl
        extends
            ServiceImpl<ClusterServiceCommandHostMapper, ClusterServiceCommandHostEntity>
        implements
            ClusterServiceCommandHostService {
    
    @Autowired
    private ClusterServiceCommandHostCommandService hostCommandService;
    
    @Autowired
    private ClusterServiceCommandHostMapper hostMapper;
    
    @Override
    public Result getCommandHostList(Integer clusterId, String commandId, Integer page, Integer pageSize) {
        Integer offset = (page - 1) * pageSize;
        
        LambdaQueryChainWrapper<ClusterServiceCommandHostEntity> wrapper = this.lambdaQuery()
                .eq(ClusterServiceCommandHostEntity::getCommandId, commandId);
        int total = wrapper.count();
        List<ClusterServiceCommandHostEntity> list = wrapper
                .orderByDesc(ClusterServiceCommandHostEntity::getCreateTime)
                .last("limit " + offset + "," + pageSize)
                .list();
        for (ClusterServiceCommandHostEntity commandHostEntity : list) {
            commandHostEntity.setCommandStateCode(commandHostEntity.getCommandState().getValue());
        }
        
        return Result.success(list).put(Constants.TOTAL, total);
    }
    
    @Override
    public Integer getCommandHostSizeByCommandId(String commandId) {
        return this.lambdaQuery().eq(ClusterServiceCommandHostEntity::getCommandId, commandId).count();
    }
    
    @Override
    public Integer getCommandHostTotalProgressByCommandId(String commandId) {
        return hostMapper.getCommandHostTotalProgressByCommandId(commandId);
    }
    
    @Override
    public List<ClusterServiceCommandHostEntity> findFailedCommandHost(String commandId) {
        return this.lambdaQuery()
                .eq(ClusterServiceCommandHostEntity::getCommandId, commandId)
                .eq(ClusterServiceCommandHostEntity::getCommandState, CommandState.FAILED)
                .list();
    }
    
    @Override
    public List<ClusterServiceCommandHostEntity> findCanceledCommandHost(String commandId) {
        return this.lambdaQuery()
                .eq(ClusterServiceCommandHostEntity::getCommandId, commandId)
                .eq(ClusterServiceCommandHostEntity::getCommandState, CommandState.CANCEL)
                .list();
    }
    
}
