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

import com.datasophon.api.master.ActorUtils;
import com.datasophon.api.master.PrometheusActor;
import com.datasophon.api.service.AlertGroupService;
import com.datasophon.api.service.ClusterAlertQuotaService;
import com.datasophon.common.Constants;
import com.datasophon.common.command.GenerateAlertConfigCommand;
import com.datasophon.common.model.AlertItem;
import com.datasophon.common.model.Generators;
import com.datasophon.common.utils.CollectionUtils;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.AlertGroupEntity;
import com.datasophon.dao.entity.ClusterAlertQuota;
import com.datasophon.dao.enums.QuotaState;
import com.datasophon.dao.mapper.ClusterAlertQuotaMapper;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.additional.query.impl.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import akka.actor.ActorRef;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

@Service("clusterAlertQuotaService")
public class ClusterAlertQuotaServiceImpl extends ServiceImpl<ClusterAlertQuotaMapper, ClusterAlertQuota>
        implements
            ClusterAlertQuotaService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClusterAlertQuotaServiceImpl.class);
    @Autowired
    AlertGroupService alertGroupService;
    
    @Override
    public Result getAlertQuotaList(Integer clusterId, Integer alertGroupId, String quotaName, Integer page,
                                    Integer pageSize) {
        Integer offset = (page - 1) * pageSize;
        
        LambdaQueryChainWrapper<ClusterAlertQuota> wrapper = this.lambdaQuery()
                .eq(alertGroupId != null, ClusterAlertQuota::getAlertGroupId, alertGroupId)
                .like(StringUtils.isNotBlank(quotaName), ClusterAlertQuota::getAlertQuotaName, quotaName);
        int count = wrapper.count() == null ? 0 : wrapper.count();
        List<ClusterAlertQuota> alertQuotaList = wrapper.last("limit " + offset + "," + pageSize).list();
        if (CollectionUtils.isEmpty(alertQuotaList)) {
            return Result.successEmptyCount();
        }
        // 查询通知组
        Set<Integer> alertQuotaIdList =
                alertQuotaList.stream().map(ClusterAlertQuota::getAlertGroupId).collect(Collectors.toSet());
        Collection<AlertGroupEntity> alertGroupEntityList = alertGroupService.listByIds(alertQuotaIdList);
        if (CollectionUtils.isNotEmpty(alertGroupEntityList)) {
            Map<Integer, AlertGroupEntity> idMap = alertGroupEntityList.stream()
                    .collect(Collectors.toMap(AlertGroupEntity::getId, a -> a, (a1, a2) -> a1));
            alertQuotaList.forEach(a -> {
                AlertGroupEntity alertGroupEntity = idMap.get(a.getAlertGroupId());
                if (Objects.nonNull(alertGroupEntity)) {
                    a.setAlertGroupName(alertGroupEntity.getAlertGroupName());
                }
                a.setQuotaStateCode(a.getQuotaState().getValue());
            });
        }
        return Result.success(alertQuotaList).put(Constants.TOTAL, count);
    }
    
    private void alertRuleFile(Integer clusterId, Collection<ClusterAlertQuota> alertQuotaList) {
        HashMap<String, List<ClusterAlertQuota>> map = new HashMap<>();
        for (ClusterAlertQuota alertQuota : alertQuotaList) {
            if (!map.containsKey(alertQuota.getServiceCategory())) {
                // list all started alert quota
                List<ClusterAlertQuota> quotaList = this.lambdaQuery()
                        .eq(ClusterAlertQuota::getServiceCategory, alertQuota.getServiceCategory())
                        .eq(ClusterAlertQuota::getQuotaState, QuotaState.RUNNING)
                        .list();
                quotaList.add(alertQuota);
                map.put(alertQuota.getServiceCategory(), quotaList);
            } else {
                List<ClusterAlertQuota> quotaList = map.get(alertQuota.getServiceCategory());
                
                quotaList.add(alertQuota);
            }
            alertQuota.setQuotaState(QuotaState.RUNNING);
        }
        
        if (alertQuotaList.size() > 0) {
            logger.info("start alert size is {}", alertQuotaList.size());
            this.updateBatchById(alertQuotaList);
        }
        HashMap<Generators, List<AlertItem>> configFileMap = new HashMap<>();
        for (Map.Entry<String, List<ClusterAlertQuota>> entry : map.entrySet()) {
            String category = entry.getKey();
            List<ClusterAlertQuota> alerts = entry.getValue();
            // alerts duplicate removal
            List<ClusterAlertQuota> alertList = alerts.stream()
                    .collect(Collectors.collectingAndThen(Collectors.toCollection(
                            () -> new TreeSet<>(Comparator.comparing(ClusterAlertQuota::getAlertQuotaName))),
                            ArrayList::new));
            
            Generators generators = new Generators();
            generators.setFilename(category.toLowerCase() + ".yml");
            generators.setConfigFormat("prometheus");
            generators.setOutputDirectory("alert_rules");
            ArrayList<AlertItem> alertItems = new ArrayList<>();
            for (ClusterAlertQuota clusterAlertQuota : alertList) {
                AlertItem alertItem = new AlertItem();
                alertItem.setAlertName(clusterAlertQuota.getAlertQuotaName());
                alertItem.setAlertExpr(clusterAlertQuota.getAlertExpr() + " " + clusterAlertQuota.getCompareMethod()
                        + " " + clusterAlertQuota.getAlertThreshold());
                alertItem.setClusterId(clusterId);
                alertItem.setServiceRoleName(clusterAlertQuota.getServiceRoleName());
                alertItem.setAlertLevel(clusterAlertQuota.getAlertLevel().getDesc());
                alertItem.setAlertAdvice(clusterAlertQuota.getAlertAdvice());
                alertItem.setTriggerDuration(clusterAlertQuota.getTriggerDuration());
                alertItems.add(alertItem);
            }
            configFileMap.put(generators, alertItems);
        }
        ActorRef prometheusActor =
                ActorUtils.getLocalActor(PrometheusActor.class, ActorUtils.getActorRefName(PrometheusActor.class));
        GenerateAlertConfigCommand alertConfigCommand = new GenerateAlertConfigCommand();
        alertConfigCommand.setClusterId(clusterId);
        alertConfigCommand.setConfigFileMap(configFileMap);
        prometheusActor.tell(alertConfigCommand, ActorRef.noSender());
    }
    
    @Override
    public void start(Integer clusterId, String alertQuotaIds) {
        List<String> ids = Arrays.asList(alertQuotaIds.split(","));
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        
        Collection<ClusterAlertQuota> alertQuotaList = this.listByIds(ids);
        
        alertRuleFile(clusterId, alertQuotaList);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void stop(Integer clusterId, String alertQuotaIds) {
        List<String> ids = Arrays.asList(alertQuotaIds.split(StrUtil.COMMA));
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        
        Set<String> categories = new HashSet<>(ids.size());
        // 1、修改禁用状态 & 更新
        Collection<ClusterAlertQuota> alertQuotas = this.listByIds(ids);
        alertQuotas.forEach(q -> {
            q.setQuotaState(QuotaState.STOPPED);
            categories.add(q.getServiceCategory());
        });
        this.updateBatchById(alertQuotas);
        
        // 2、查询需要重新生成 alert rule file 的告警指标
        Collection<ClusterAlertQuota> clusterAlertQuotas = this.lambdaQuery()
                .eq(ClusterAlertQuota::getQuotaState, QuotaState.RUNNING)
                .in(ClusterAlertQuota::getServiceCategory, categories)
                .list();
        if (CollUtil.isEmpty(clusterAlertQuotas)) {
            return;
        }
        
        alertRuleFile(clusterId, clusterAlertQuotas);
    }
    
    @Override
    public void saveAlertQuota(ClusterAlertQuota clusterAlertQuota) {
        clusterAlertQuota.setQuotaState(QuotaState.STOPPED);
        clusterAlertQuota.setCreateTime(new Date());
        AlertGroupEntity alertGroupEntity = alertGroupService.getById(clusterAlertQuota.getAlertGroupId());
        clusterAlertQuota.setServiceCategory(alertGroupEntity.getAlertGroupCategory());
        this.save(clusterAlertQuota);
    }
    
    @Override
    public List<ClusterAlertQuota> listAlertQuotaByServiceName(String serviceName) {
        return this.list(new QueryWrapper<ClusterAlertQuota>().eq(Constants.SERVICE_CATEGORY, serviceName));
    }
}
