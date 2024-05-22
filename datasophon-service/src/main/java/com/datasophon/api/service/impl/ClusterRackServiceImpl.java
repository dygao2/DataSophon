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

import com.datasophon.api.enums.Status;
import com.datasophon.api.service.ClusterRackService;
import com.datasophon.api.service.host.ClusterHostService;
import com.datasophon.common.Constants;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterHostDO;
import com.datasophon.dao.entity.ClusterRack;
import com.datasophon.dao.mapper.ClusterRackMapper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service("clusterRackService")
public class ClusterRackServiceImpl extends ServiceImpl<ClusterRackMapper, ClusterRack> implements ClusterRackService {
    
    @Autowired
    private ClusterHostService hostService;
    
    @Override
    public List<ClusterRack> queryClusterRack(Integer clusterId) {
        return this.list(new QueryWrapper<ClusterRack>().eq(Constants.CLUSTER_ID, clusterId));
    }
    
    @Override
    public void saveRack(Integer clusterId, String rack) {
        ClusterRack clusterRack = new ClusterRack();
        clusterRack.setRack(rack);
        clusterRack.setClusterId(clusterId);
        this.save(clusterRack);
    }
    
    @Override
    public Result deleteRack(Integer rackId) {
        ClusterRack clusterRack = this.getById(rackId);
        if (rackInUse(clusterRack)) {
            return Result.error(Status.RACK_IS_USING.getMsg());
        }
        this.removeById(rackId);
        return Result.success();
    }
    
    @Override
    public void createDefaultRack(Integer clusterId) {
        ClusterRack clusterRack = new ClusterRack();
        clusterRack.setRack("/default-rack");
        clusterRack.setClusterId(clusterId);
        this.save(clusterRack);
    }
    
    private boolean rackInUse(ClusterRack clusterRack) {
        List<ClusterHostDO> list =
                hostService.getClusterHostByRack(clusterRack.getClusterId(), clusterRack.getRack());
        if (list.size() > 0) {
            return true;
        }
        return false;
    }
    
}
