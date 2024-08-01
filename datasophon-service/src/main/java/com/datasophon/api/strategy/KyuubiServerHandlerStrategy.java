/*
 *
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
 *
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KyuubiServerHandlerStrategy extends ServiceHandlerAbstract implements ServiceRoleStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(KyuubiServerHandlerStrategy.class);
    private static final String ENABLE_KERBEROS = "enableKerberos";
    
    @Override
    public void handler(Integer clusterId, List<String> hosts, String serviceName) {
        
    }
    
    @Override
    public void handlerConfig(Integer clusterId, List<ServiceConfig> list, String serviceName) {
        
        Map<String, String> globalVariables = GlobalVariables.get(clusterId);
        boolean enableKerberos = false;
        Map<String, ServiceConfig> map = ProcessUtils.translateToMap(list);
        ClusterInfoEntity clusterInfo = ProcessUtils.getClusterInfo(clusterId);
        // todo: 判断kerberos的逻辑应该抽取到公共方法中
        for (ServiceConfig config : list) {
            if (ENABLE_KERBEROS.equals(config.getName())) {
                enableKerberos =
                        isEnableKerberos(
                                clusterId, globalVariables, enableKerberos, config, "KYUUBI");
            }
        }
        String key = clusterInfo.getClusterFrame() + Constants.UNDERLINE + "KYUUBI" + Constants.CONFIG;
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
    
}
