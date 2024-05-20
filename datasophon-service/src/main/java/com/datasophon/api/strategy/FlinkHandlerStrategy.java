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
import com.datasophon.api.utils.ProcessUtils;
import com.datasophon.common.Constants;
import com.datasophon.common.model.ServiceConfig;
import com.datasophon.common.model.ServiceRoleInfo;
import com.datasophon.dao.entity.ClusterInfoEntity;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FlinkHandlerStrategy extends ServiceHandlerAbstract implements ServiceRoleStrategy {
    
    @Override
    public void handler(Integer clusterId, List<String> hosts, String serviceName) {
        
    }
    
    @Override
    public void handlerConfig(Integer clusterId, List<ServiceConfig> list, String serviceName) {
        Map<String, String> globalVariables = GlobalVariables.get(clusterId);
        ClusterInfoEntity clusterInfo = ProcessUtils.getClusterInfo(clusterId);
        boolean enableJM2HA = false;
        boolean enableKerberos = false;
        Map<String, ServiceConfig> map = ProcessUtils.translateToMap(list);
        for (ServiceConfig serviceConfig : list) {
            if ("enableKerberos".equals(serviceConfig.getName())) {
                enableKerberos = isEnableKerberos(clusterId, globalVariables, enableKerberos, serviceConfig, "FLINK");
            }
        }
        
        for (ServiceConfig config : list) {
            if ("enableJMHA".equals(config.getName())) {
                enableJM2HA = isEnableHA(clusterId, globalVariables, enableJM2HA, config, "FLINK");
            }
        }
        String key = clusterInfo.getClusterFrame() + Constants.UNDERLINE + "FLINK" + Constants.CONFIG;
        List<ServiceConfig> configs = ServiceConfigMap.get(key);
        ArrayList<ServiceConfig> haConfigs = new ArrayList<>();
        if (enableJM2HA) {
            addConfigWithHA(globalVariables, map, configs, haConfigs);
        } else {
            removeConfigWithHA(list, map, configs);
        }
        
        ArrayList<ServiceConfig> kbConfigs = new ArrayList<>();
        if (enableKerberos) {
            addConfigWithKerberos(globalVariables, map, configs, kbConfigs);
        } else {
            removeConfigWithKerberos(list, map, configs);
        }
        
        list.addAll(haConfigs);
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
}
