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

package com.datasophon.api.strategy;

import com.datasophon.api.load.GlobalVariables;
import com.datasophon.api.load.ServiceConfigMap;
import com.datasophon.api.service.ClusterInfoService;
import com.datasophon.api.service.ClusterServiceInstanceService;
import com.datasophon.api.service.ClusterServiceRoleGroupConfigService;
import com.datasophon.api.service.ClusterServiceRoleInstanceService;
import com.datasophon.api.service.ServiceInstallService;
import com.datasophon.api.utils.ProcessUtils;
import com.datasophon.api.utils.SpringTool;
import com.datasophon.common.Constants;
import com.datasophon.common.model.ServiceConfig;
import com.datasophon.common.model.ServiceRoleInfo;
import com.datasophon.dao.entity.ClusterInfoEntity;
import com.datasophon.dao.entity.ClusterServiceInstanceEntity;
import com.datasophon.dao.entity.ClusterServiceRoleGroupConfig;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

public class RangerAdminHandlerStrategy extends ServiceHandlerAbstract implements ServiceRoleStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(RangerAdminHandlerStrategy.class);
    
    @Override
    public void handler(Integer clusterId, List<String> hosts, String serviceName) {
        Map<String, String> globalVariables = GlobalVariables.get(clusterId);
        if (hosts.size() == 1) {
            String rangerAdminUrl = "http://" + hosts.get(0) + ":6080";
            logger.info("rangerAdminUrl is {}", rangerAdminUrl);
            ProcessUtils.generateClusterVariable(globalVariables, clusterId, serviceName, "${rangerAdminUrl}",
                    rangerAdminUrl);
        }
    }
    
    @Override
    public void handlerConfig(Integer clusterId, List<ServiceConfig> list, String serviceName) {
        Map<String, String> globalVariables = GlobalVariables.get(clusterId);
        ClusterInfoEntity clusterInfo = ProcessUtils.getClusterInfo(clusterId);
        boolean enableKerberos = false;
        Map<String, ServiceConfig> map = ProcessUtils.translateToMap(list);
        // enable ranger plugin
        for (ServiceConfig config : list) {
            if ("enableHDFSPlugin".equals(config.getName()) && ((Boolean) config.getValue()).booleanValue()) {
                logger.info("enableHdfsPlugin");
                ProcessUtils.generateClusterVariable(globalVariables, clusterId, serviceName, "${enableHDFSPlugin}",
                        "true");
                enableRangerPlugin(clusterId, "HDFS", "NameNode");
            }
            if ("enableHIVEPlugin".equals(config.getName()) && ((Boolean) config.getValue()).booleanValue()) {
                logger.info("enableHivePlugin");
                ProcessUtils.generateClusterVariable(globalVariables, clusterId, serviceName, "${enableHIVEPlugin}",
                        "true");
                enableRangerPlugin(clusterId, "HIVE", "HiveServer2");
            }
            if ("enableHBASEPlugin".equals(config.getName()) && ((Boolean) config.getValue()).booleanValue()) {
                logger.info("enableHbasePlugin");
                ProcessUtils.generateClusterVariable(globalVariables, clusterId, serviceName, "${enableHBASEPlugin}",
                        "true");
                enableRangerPlugin(clusterId, "HBASE", "HbaseMaster");
            }
            if (config.getName().contains("Plugin") && !(Boolean) config.getValue()) {
                String configName = config.getName();
                ProcessUtils.generateClusterVariable(globalVariables, clusterId, serviceName, "${" + configName + "}",
                        "false");
            }
            if ("enableKerberos".equals(config.getName())) {
                enableKerberos = isEnableKerberos(clusterId, globalVariables, enableKerberos, config, "RANGER");
            }
        }
        String key = clusterInfo.getClusterFrame() + Constants.UNDERLINE + "RANGER" + Constants.CONFIG;
        List<ServiceConfig> configs = ServiceConfigMap.get(key);
        ArrayList<ServiceConfig> kbConfigs = new ArrayList<>();
        if (enableKerberos) {
            addConfigWithKerberos(globalVariables, map, configs, kbConfigs);
        } else {
            removeConfigWithKerberos(list, map, configs);
        }
        list.addAll(kbConfigs);
    }
    
    @Override
    public void getConfig(Integer clusterId, List<ServiceConfig> list) {
        
    }
    
    @Override
    public void handlerServiceRoleInfo(ServiceRoleInfo serviceRoleInfo, String hostname) {
        
    }
    
    @Override
    public void handlerServiceRoleCheck(ClusterServiceRoleInstanceEntity roleInstanceEntity,
                                        Map<String, ClusterServiceRoleInstanceEntity> map) {
        
    }
    
    private void enableRangerPlugin(Integer clusterId, String serviceName, String serviceRoleName) {
        ClusterServiceInstanceService serviceInstanceService =
                SpringTool.getApplicationContext().getBean(ClusterServiceInstanceService.class);
        ClusterServiceRoleInstanceService roleInstanceService =
                SpringTool.getApplicationContext().getBean(ClusterServiceRoleInstanceService.class);
        ClusterServiceRoleGroupConfigService roleGroupConfigService =
                SpringTool.getApplicationContext().getBean(ClusterServiceRoleGroupConfigService.class);
        ClusterInfoService clusterInfoService = SpringTool.getApplicationContext().getBean(ClusterInfoService.class);
        ServiceInstallService serviceInstallService =
                SpringTool.getApplicationContext().getBean(ServiceInstallService.class);
        ClusterInfoEntity clusterInfo = clusterInfoService.getById(clusterId);
        Map<String, String> globalVariables = GlobalVariables.get(clusterId);
        ClusterServiceInstanceEntity serviceInstance =
                serviceInstanceService.getServiceInstanceByClusterIdAndServiceName(clusterId, serviceName);
        // 查询角色组id
        List<ClusterServiceRoleInstanceEntity> roleList =
                roleInstanceService.getServiceRoleInstanceListByClusterIdAndRoleName(clusterId, serviceRoleName);
        
        if (Objects.nonNull(roleList) && roleList.size() > 0) {
            Integer roleGroupId = roleList.get(0).getRoleGroupId();
            
            ClusterServiceRoleGroupConfig config = roleGroupConfigService.getConfigByRoleGroupId(roleGroupId);
            List<ServiceConfig> serviceConfigs = JSONObject.parseArray(config.getConfigJson(), ServiceConfig.class);
            Map<String, ServiceConfig> map = serviceConfigs.stream()
                    .collect(Collectors.toMap(ServiceConfig::getName, serviceConfig -> serviceConfig, (v1, v2) -> v1));
            
            String key = clusterInfo.getClusterFrame() + Constants.UNDERLINE + serviceName + Constants.CONFIG;
            List<ServiceConfig> configs = ServiceConfigMap.get(key);
            for (ServiceConfig parameter : configs) {
                String name = parameter.getName();
                if (map.containsKey(name)) {
                    parameter = map.get(name);
                }
                
                if ("permission".equals(parameter.getConfigType())) {
                    parameter.setHidden(false);
                    parameter.setRequired(true);
                }
                if ("dfs.permissions.enabled".equals(parameter.getName())) {
                    parameter.setHidden(false);
                    parameter.setRequired(true);
                    parameter.setValue(true);
                    
                }
                if ("rangerAdminUrl".equals(parameter.getName())) {
                    parameter.setHidden(false);
                    parameter.setRequired(true);
                    parameter.setValue(globalVariables.get("${rangerAdminUrl}"));
                }
                if (!map.containsKey(name)) {
                    logger.info("put config {} into service {}", name, serviceRoleName);
                    serviceConfigs.add(parameter);
                }
            }
            logger.info("Update hdfs enable ranger plugin");
            serviceInstallService.saveServiceConfig(clusterId, serviceInstance.getServiceName(), serviceConfigs,
                    roleGroupId);
        }
    }
}
