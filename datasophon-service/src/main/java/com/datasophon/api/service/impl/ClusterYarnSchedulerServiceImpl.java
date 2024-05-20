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

import com.datasophon.api.service.ClusterYarnSchedulerService;
import com.datasophon.common.Constants;
import com.datasophon.dao.entity.ClusterYarnScheduler;
import com.datasophon.dao.mapper.ClusterYarnSchedulerMapper;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service("clusterYarnSchedulerService")
public class ClusterYarnSchedulerServiceImpl extends ServiceImpl<ClusterYarnSchedulerMapper, ClusterYarnScheduler>
        implements
            ClusterYarnSchedulerService {
    
    @Override
    public ClusterYarnScheduler getScheduler(Integer clusterId) {
        return this.getOne(new QueryWrapper<ClusterYarnScheduler>().eq(Constants.CLUSTER_ID, clusterId));
    }
    
    @Override
    public void createDefaultYarnScheduler(Integer clusterId) {
        ClusterYarnScheduler scheduler = new ClusterYarnScheduler();
        scheduler.setScheduler("fair");
        scheduler.setClusterId(clusterId);
        scheduler.setInUse(1);
        this.save(scheduler);
    }
}
