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
import com.datasophon.api.utils.ProcessUtils;
import com.datasophon.api.utils.SpringTool;
import com.datasophon.common.Constants;
import com.datasophon.common.cache.CacheUtils;
import com.datasophon.common.model.ServiceConfig;
import com.datasophon.common.model.ServiceRoleInfo;
import com.datasophon.common.utils.HostUtils;
import com.datasophon.common.utils.PlaceholderUtils;
import com.datasophon.dao.entity.ClusterInfoEntity;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

public class ZkServerHandlerStrategy implements ServiceRoleStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(ZkServerHandlerStrategy.class);
    
    @Override
    public void handler(Integer clusterId, List<String> hosts, String serviceName) {
        // 保存zkUrls到全局变量
        Map<String, String> globalVariables = GlobalVariables.get(clusterId);
        String join = String.join(":2181,", hosts);
        String zkUrls = join + ":2181";
        ProcessUtils.generateClusterVariable(globalVariables, clusterId, serviceName, "${zkUrls}", zkUrls);
        // 保存hbaseZkUrls到全局变量
        String hbaseZkUrls = String.join(",", hosts);
        ProcessUtils.generateClusterVariable(globalVariables, clusterId, serviceName, "${zkHostsUrl}", hbaseZkUrls);
    }
    
    @Override
    public void handlerConfig(Integer clusterId, List<ServiceConfig> list, String serviceName) {
        Map<String, String> globalVariables = GlobalVariables.get(clusterId);
        ClusterInfoEntity clusterInfo = ProcessUtils.getClusterInfo(clusterId);
        boolean enableKerberos = false;
        Map<String, ServiceConfig> map = ProcessUtils.translateToMap(list);
        
        for (ServiceConfig config : list) {
            if ("enableKerberos".equals(config.getName())) {
                if ((Boolean) config.getValue()) {
                    enableKerberos = true;
                    ProcessUtils.generateClusterVariable(globalVariables, clusterId, serviceName,
                            "${enableZOOKEEPERKerberos}",
                            "true");
                } else {
                    ProcessUtils.generateClusterVariable(globalVariables, clusterId, serviceName,
                            "${enableZOOKEEPERKerberos}",
                            "false");
                }
            }
        }
        
        String key = clusterInfo.getClusterFrame() + Constants.UNDERLINE + "ZOOKEEPER" + Constants.CONFIG;
        List<ServiceConfig> configs = ServiceConfigMap.get(key);
        ArrayList<ServiceConfig> kbConfigs = new ArrayList<>();
        if (enableKerberos) {
            for (ServiceConfig serviceConfig : configs) {
                if (serviceConfig.isConfigWithKerberos()) {
                    if (map.containsKey(serviceConfig.getName())) {
                        ServiceConfig config = map.get(serviceConfig.getName());
                        config.setRequired(true);
                        config.setHidden(false);
                        String value = PlaceholderUtils.replacePlaceholders((String) serviceConfig.getValue(),
                                globalVariables, Constants.REGEX_VARIABLE);
                        logger.info("the value is {}", value);
                        config.setValue(value);
                    } else {
                        serviceConfig.setRequired(true);
                        serviceConfig.setHidden(false);
                        String value = PlaceholderUtils.replacePlaceholders((String) serviceConfig.getValue(),
                                globalVariables, Constants.REGEX_VARIABLE);
                        serviceConfig.setValue(value);
                        kbConfigs.add(serviceConfig);
                    }
                }
            }
        } else {
            for (ServiceConfig serviceConfig : configs) {
                if (serviceConfig.isConfigWithKerberos()) {
                    if (map.containsKey(serviceConfig.getName())) {
                        list.remove(map.get(serviceConfig.getName()));
                    }
                }
            }
        }
        list.addAll(kbConfigs);
    }
    
    /**
     *
     * @param clusterId
     * @param list
     */
    @Override
    public void getConfig(Integer clusterId, List<ServiceConfig> list) {
        // add server.x config
        ClusterInfoService clusterInfoService = SpringTool.getApplicationContext().getBean(ClusterInfoService.class);
        ClusterInfoEntity clusterInfo = clusterInfoService.getById(clusterId);
        
        String hostMapKey = clusterInfo.getClusterCode() + Constants.UNDERLINE + Constants.SERVICE_ROLE_HOST_MAPPING;
        HashMap<String, List<String>> hostMap = (HashMap<String, List<String>>) CacheUtils.get(hostMapKey);
        
        if (Objects.nonNull(hostMap)) {
            List<String> zkServers = hostMap.get("ZkServer");
            
            Map<String, ServiceConfig> map = ProcessUtils.translateToMap(list);
            
            Integer myid = 1;
            for (String server : zkServers) {
                ServiceConfig serviceConfig = new ServiceConfig();
                serviceConfig.setName("server." + myid);
                serviceConfig.setLabel("server." + myid);
                serviceConfig.setValue(HostUtils.getIp(server) + ":2888:3888");
                serviceConfig.setHidden(false);
                serviceConfig.setRequired(true);
                serviceConfig.setType("input");
                serviceConfig.setDefaultValue("");
                serviceConfig.setConfigType("zkserver");
                if (map.containsKey("server." + myid)) {
                    logger.info("set zk server {}", myid);
                    ServiceConfig config = map.get("server." + myid);
                    BeanUtils.copyProperties(serviceConfig, config);
                } else {
                    logger.info("add zk server.x config");
                    list.add(serviceConfig);
                }
                CacheUtils.put("zkserver_" + server, myid);
                myid++;
            }
        }
    }
    
    @Override
    public void handlerServiceRoleInfo(ServiceRoleInfo serviceRoleInfo, String hostname) {
        
    }
    
    @Override
    public void handlerServiceRoleCheck(ClusterServiceRoleInstanceEntity roleInstanceEntity,
                                        Map<String, ClusterServiceRoleInstanceEntity> map) {
        
    }
}
