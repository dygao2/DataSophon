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
import com.datasophon.api.load.GlobalVariables;
import com.datasophon.api.service.ClusterAlertHistoryService;
import com.datasophon.api.service.ClusterInfoService;
import com.datasophon.api.service.ClusterServiceDashboardService;
import com.datasophon.api.service.ClusterServiceInstanceRoleGroupService;
import com.datasophon.api.service.ClusterServiceInstanceService;
import com.datasophon.api.service.ClusterServiceRoleGroupConfigService;
import com.datasophon.api.service.ClusterServiceRoleInstanceService;
import com.datasophon.api.service.ClusterServiceRoleInstanceWebuisService;
import com.datasophon.api.service.ClusterVariableService;
import com.datasophon.api.service.FrameServiceRoleService;
import com.datasophon.common.Constants;
import com.datasophon.common.model.SimpleServiceConfig;
import com.datasophon.common.utils.CollectionUtils;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterAlertHistory;
import com.datasophon.dao.entity.ClusterServiceDashboard;
import com.datasophon.dao.entity.ClusterServiceInstanceEntity;
import com.datasophon.dao.entity.ClusterServiceInstanceRoleGroup;
import com.datasophon.dao.entity.ClusterServiceRoleGroupConfig;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceEntity;
import com.datasophon.dao.entity.ClusterVariable;
import com.datasophon.dao.entity.FrameServiceRoleEntity;
import com.datasophon.dao.enums.NeedRestart;
import com.datasophon.dao.enums.ServiceRoleState;
import com.datasophon.dao.enums.ServiceState;
import com.datasophon.dao.mapper.ClusterServiceInstanceMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service("clusterServiceInstanceService")
@Transactional
public class ClusterServiceInstanceServiceImpl
        extends
            ServiceImpl<ClusterServiceInstanceMapper, ClusterServiceInstanceEntity>
        implements
            ClusterServiceInstanceService {
    
    @Value("${server.servlet.context-path}")
    private String contextPath;
    
    @Autowired
    private ClusterServiceInstanceMapper serviceInstanceMapper;
    
    @Autowired
    private ClusterServiceRoleInstanceService roleInstanceService;
    
    @Autowired
    private ClusterServiceDashboardService dashboardService;
    
    @Autowired
    private ClusterInfoService clusterInfoService;
    
    @Autowired
    private ClusterAlertHistoryService alertHistoryService;
    
    @Autowired
    private FrameServiceRoleService frameServiceRoleService;
    
    @Autowired
    private ClusterServiceRoleGroupConfigService roleGroupConfigService;
    
    @Autowired
    private ClusterServiceInstanceRoleGroupService roleGroupService;
    
    @Autowired
    private ClusterServiceRoleInstanceWebuisService webuisService;
    
    @Autowired
    private ClusterVariableService variableService;
    
    @Override
    public ClusterServiceInstanceEntity getServiceInstanceByClusterIdAndServiceName(Integer clusterId,
                                                                                    String serviceName) {
        return this.getOne(new QueryWrapper<ClusterServiceInstanceEntity>()
                .eq(Constants.CLUSTER_ID, clusterId)
                .eq(Constants.SERVICE_NAME, serviceName));
    }
    
    @Override
    public String getServiceConfigByClusterIdAndServiceName(Integer clusterId, String serviceName) {
        return serviceInstanceMapper.getServiceConfigByClusterIdAndServiceName(clusterId, serviceName);
    }
    
    @Override
    public List<ClusterServiceInstanceEntity> listAll(Integer clusterId) {
        Map<String, String> globalVariables = GlobalVariables.get(clusterId);
        List<ClusterServiceInstanceEntity> list = this.list(new QueryWrapper<ClusterServiceInstanceEntity>()
                .eq(Constants.CLUSTER_ID, clusterId).orderByAsc(Constants.SORT_NUM));
        for (ClusterServiceInstanceEntity serviceInstance : list) {
            serviceInstance.setServiceStateCode(serviceInstance.getServiceState().getValue());
            boolean needUpdate = false;
            // 查询dashboard
            ClusterServiceDashboard dashboard = dashboardService.getOne(new QueryWrapper<ClusterServiceDashboard>()
                    .eq(Constants.SERVICE_NAME, serviceInstance.getServiceName()));
            if (Objects.nonNull(dashboard) && StringUtils.hasText(dashboard.getDashboardUrl())) {
                serviceInstance.setDashboardUrl(dashboardService.getDashboardUrl(clusterId, dashboard));
            }
            // 查询告警数量
            int alertNum = alertHistoryService.count(new QueryWrapper<ClusterAlertHistory>()
                    .eq(Constants.SERVICE_INSTANCE_ID, serviceInstance.getId()).eq(Constants.IS_ENABLED, 1));
            serviceInstance.setAlertNum(alertNum);
            List<ClusterServiceRoleInstanceEntity> totalRoleList = roleInstanceService.lambdaQuery()
                    .eq(ClusterServiceRoleInstanceEntity::getServiceId, serviceInstance.getId())
                    .list();
            if (Objects.nonNull(totalRoleList) && totalRoleList.isEmpty()) {
                serviceInstance.setServiceState(ServiceState.WAIT_INSTALL);
                needUpdate = true;
            }
            
            // 查询停止状态角色
            List<ClusterServiceRoleInstanceEntity> roleList = roleInstanceService.lambdaQuery()
                    .eq(ClusterServiceRoleInstanceEntity::getServiceId, serviceInstance.getId())
                    .eq(ClusterServiceRoleInstanceEntity::getServiceRoleState, ServiceRoleState.STOP)
                    .list();
            if (Objects.nonNull(roleList) && !roleList.isEmpty()) {
                if (!ServiceState.EXISTS_EXCEPTION.equals(serviceInstance.getServiceState())) {
                    serviceInstance.setServiceState(ServiceState.EXISTS_EXCEPTION);
                    needUpdate = true;
                }
            } else {
                if (!ServiceState.RUNNING.equals(serviceInstance.getServiceState())
                        && serviceInstance.getServiceState() != ServiceState.WAIT_INSTALL
                        && serviceInstance.getServiceState() != ServiceState.EXISTS_ALARM) {
                    serviceInstance.setServiceState(ServiceState.RUNNING);
                    needUpdate = true;
                }
            }
            // 查询告警状态角色
            List<ClusterServiceRoleInstanceEntity> alarmRoleList = roleInstanceService.lambdaQuery()
                    .eq(ClusterServiceRoleInstanceEntity::getServiceId, serviceInstance.getId())
                    .eq(ClusterServiceRoleInstanceEntity::getServiceRoleState, ServiceRoleState.EXISTS_ALARM)
                    .list();
            if (Objects.nonNull(alarmRoleList) && !alarmRoleList.isEmpty()) {
                if (!ServiceState.EXISTS_ALARM.equals(serviceInstance.getServiceState())
                        && !ServiceState.EXISTS_EXCEPTION.equals(serviceInstance.getServiceState())) {
                    serviceInstance.setServiceState(ServiceState.EXISTS_ALARM);
                    needUpdate = true;
                }
            } else {
                if (serviceInstance.getServiceState() == ServiceState.EXISTS_ALARM) {
                    serviceInstance.setServiceState(ServiceState.RUNNING);
                    needUpdate = true;
                }
            }
            
            // 查询是否进行了配置更新
            List<ClusterServiceRoleInstanceEntity> obsoleteRoleList =
                    roleInstanceService.getObsoleteService(serviceInstance.getId());
            if (Objects.nonNull(obsoleteRoleList) && obsoleteRoleList.isEmpty()
                    && serviceInstance.getNeedRestart() == NeedRestart.YES) {
                serviceInstance.setNeedRestart(NeedRestart.NO);
                needUpdate = true;
            }
            if (needUpdate) {
                this.updateById(serviceInstance);
            }
        }
        return list;
    }
    
    @Override
    public Result downloadClientConfig(Integer clusterId, String serviceName) {
        
        return null;
    }
    
    @Override
    public Result getServiceRoleType(Integer serviceInstanceId) {
        ClusterServiceInstanceEntity serviceInstanceEntity = this.getById(serviceInstanceId);
        Integer frameServiceId = serviceInstanceEntity.getFrameServiceId();
        List<FrameServiceRoleEntity> list = frameServiceRoleService.getAllServiceRoleList(frameServiceId);
        return Result.success(list);
    }
    
    @Override
    public Result configVersionCompare(Integer serviceInstanceId, Integer roleGroupId) {
        List<ClusterServiceRoleGroupConfig> list =
                roleGroupConfigService.list(new QueryWrapper<ClusterServiceRoleGroupConfig>()
                        .eq(Constants.ROLE_GROUP_ID, roleGroupId)
                        .orderByDesc(Constants.CONFIG_VERSION).last("limit 2"));
        HashMap<String, List<SimpleServiceConfig>> map = new HashMap<>();
        if (Objects.nonNull(list) && list.size() == 2) {
            ClusterServiceRoleGroupConfig newConfig = list.get(0);
            ClusterServiceRoleGroupConfig oldConfig = list.get(1);
            String newConfigJson = newConfig.getConfigJson();
            List<SimpleServiceConfig> newSimpleServiceConfigs =
                    JSONArray.parseArray(newConfigJson, SimpleServiceConfig.class);
            
            String oldConfigJson = oldConfig.getConfigJson();
            List<SimpleServiceConfig> oldSimpleServiceConfigs =
                    JSONArray.parseArray(oldConfigJson, SimpleServiceConfig.class);
            map.put("newConfig", newSimpleServiceConfigs);
            map.put("oldConfig", oldSimpleServiceConfigs);
            
        } else if (list.size() == 1) {
            ClusterServiceRoleGroupConfig newConfig = list.get(0);
            String newConfigJson = newConfig.getConfigJson();
            List<SimpleServiceConfig> newSimpleServiceConfigs =
                    JSONArray.parseArray(newConfigJson, SimpleServiceConfig.class);
            map.put("newConfig", newSimpleServiceConfigs);
            map.put("oldConfig", newSimpleServiceConfigs);
        }
        return Result.success(map);
    }
    
    @Override
    public Result delServiceInstance(Integer serviceInstanceId) {
        if (hasRunningRoleInstance(serviceInstanceId)) {
            return Result.error(Status.EXIT_RUNNING_ROLE_INSTANCE.getMsg());
        }
        List<ClusterServiceInstanceRoleGroup> roleGroups =
                roleGroupService.listRoleGroupByServiceInstanceId(serviceInstanceId);
        List<Integer> roleGroupIds =
                roleGroups.stream().map(ClusterServiceInstanceRoleGroup::getId).collect(Collectors.toList());
        List<ClusterServiceRoleGroupConfig> roleGroupConfigList =
                roleGroupConfigService.listRoleGroupConfigsByRoleGroupIds(roleGroupIds);
        List<ClusterServiceRoleInstanceEntity> roleInstanceList =
                roleInstanceService.getServiceRoleInstanceListByServiceId(serviceInstanceId);
        
        // del role group
        roleGroupService.removeByIds(roleGroupIds);
        // del role group config
        roleGroupConfigService
                .removeByIds(roleGroupConfigList.stream().map(ClusterServiceRoleGroupConfig::getId)
                        .collect(Collectors.toList()));
        // del service role instance
        if (!roleInstanceList.isEmpty()) {
            List<String> roleInsIds =
                    roleInstanceList.stream().map(e -> e.getId().toString()).collect(Collectors.toList());
            roleInstanceService.deleteServiceRole(roleInsIds);
        }
        // del web uis
        webuisService.removeByServiceInsId(serviceInstanceId);
        
        // del service instance
        this.removeById(serviceInstanceId);
        // del variable
        roleGroups.forEach(roleGroup -> {
            List<ClusterVariable> variables =
                    variableService.getVariables(roleGroup.getClusterId(), roleGroup.getServiceName());
            if (CollectionUtils.isNotEmpty(variables)) {
                Map<String, String> variablesMap = GlobalVariables.get(roleGroup.getClusterId());
                variables.forEach(var -> variablesMap.remove(var.getVariableName()));
                variableService
                        .removeByIds(variables.stream().map(ClusterVariable::getId).collect(Collectors.toList()));
            }
        });
        return Result.success();
    }
    
    @Override
    public List<ClusterServiceInstanceEntity> listRunningServiceInstance(Integer clusterId) {
        return this.list(new QueryWrapper<ClusterServiceInstanceEntity>()
                .eq(Constants.CLUSTER_ID, clusterId)
                .eq(Constants.SERVICE_STATE, ServiceState.RUNNING));
    }
    
    public boolean hasRunningRoleInstance(Integer serviceInstanceId) {
        List<ClusterServiceRoleInstanceEntity> list =
                roleInstanceService.getRunningServiceRoleInstanceListByServiceId(serviceInstanceId);
        return !list.isEmpty();
    }
}
