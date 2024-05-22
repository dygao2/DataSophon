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

import com.datasophon.api.service.AlertGroupService;
import com.datasophon.api.service.ClusterAlertGroupMapService;
import com.datasophon.api.service.ClusterAlertQuotaService;
import com.datasophon.common.Constants;
import com.datasophon.common.utils.CollectionUtils;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.AlertGroupEntity;
import com.datasophon.dao.entity.ClusterAlertGroupMap;
import com.datasophon.dao.entity.ClusterAlertQuota;
import com.datasophon.dao.mapper.AlertGroupMapper;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.additional.query.impl.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service("alertGroupService")
public class AlertGroupServiceImpl extends ServiceImpl<AlertGroupMapper, AlertGroupEntity>
        implements
            AlertGroupService {
    
    @Autowired
    private ClusterAlertGroupMapService alertGroupMapService;
    
    @Autowired
    private ClusterAlertQuotaService quotaService;
    
    @Override
    public Result getAlertGroupList(Integer clusterId, String alertGroupName, Integer page, Integer pageSize) {
        Integer offset = (page - 1) * pageSize;
        
        List<ClusterAlertGroupMap> alertGroupMapList =
                alertGroupMapService.list(new QueryWrapper<ClusterAlertGroupMap>().eq(Constants.CLUSTER_ID, clusterId));
        if (CollectionUtils.isEmpty(alertGroupMapList)) {
            return Result.successEmptyCount();
        }
        
        List<Integer> groupIds =
                alertGroupMapList.stream().map(ClusterAlertGroupMap::getAlertGroupId).collect(Collectors.toList());
        LambdaQueryChainWrapper<AlertGroupEntity> wrapper = this.lambdaQuery()
                .in(AlertGroupEntity::getId, groupIds)
                .like(StringUtils.isNotBlank(alertGroupName), AlertGroupEntity::getAlertGroupName, alertGroupName);
        int count = wrapper.count() == null ? 0 : wrapper.count();
        List<AlertGroupEntity> alertGroupList = wrapper.last("limit " + offset + "," + pageSize).list();
        if (CollectionUtils.isEmpty(alertGroupList)) {
            return Result.successEmptyCount();
        }
        
        Set<Integer> alertGroupIdList =
                alertGroupList.stream().map(AlertGroupEntity::getId).collect(Collectors.toSet());
        // 查询告警组下告警指标个数
        List<ClusterAlertQuota> clusQuotaList =
                quotaService.lambdaQuery().in(ClusterAlertQuota::getAlertGroupId, alertGroupIdList).list();
        if (CollectionUtils.isNotEmpty(clusQuotaList)) {
            Map<Integer, List<ClusterAlertQuota>> alertGroupByGroupId =
                    clusQuotaList.stream().collect(Collectors.groupingBy(ClusterAlertQuota::getAlertGroupId));
            alertGroupList.forEach(a -> {
                List<ClusterAlertQuota> tmpQuotaList = alertGroupByGroupId.get(a.getId());
                int quotaCnt = CollectionUtils.isEmpty(tmpQuotaList) ? 0 : tmpQuotaList.size();
                a.setAlertQuotaNum(quotaCnt);
            });
        }
        
        return Result.success(alertGroupList).put(Constants.TOTAL, count);
    }
    
    @Override
    public Result saveAlertGroup(AlertGroupEntity alertGroup) {
        this.save(alertGroup);
        ClusterAlertGroupMap clusterAlertGroupMap = new ClusterAlertGroupMap();
        clusterAlertGroupMap.setAlertGroupId(alertGroup.getId());
        clusterAlertGroupMap.setClusterId(alertGroup.getClusterId());
        alertGroupMapService.save(clusterAlertGroupMap);
        return Result.success();
    }
}
