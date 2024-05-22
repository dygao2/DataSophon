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
import com.datasophon.api.utils.ProcessUtils;
import com.datasophon.common.model.ProcInfo;
import com.datasophon.common.model.ServiceConfig;
import com.datasophon.common.model.ServiceRoleInfo;
import com.datasophon.common.utils.OlapUtils;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceEntity;
import com.datasophon.dao.enums.AlertLevel;
import com.datasophon.dao.enums.ServiceRoleState;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BEHandlerStartegy implements ServiceRoleStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(BEHandlerStartegy.class);
    
    @Override
    public void handler(Integer clusterId, List<String> hosts, String serviceName) {
        
    }
    
    @Override
    public void handlerConfig(Integer clusterId, List<ServiceConfig> list, String serviceName) {
        
    }
    
    @Override
    public void getConfig(Integer clusterId, List<ServiceConfig> list) {
        
    }
    
    @Override
    public void handlerServiceRoleInfo(ServiceRoleInfo serviceRoleInfo, String hostname) {
        Map<String, String> globalVariables = GlobalVariables.get(serviceRoleInfo.getClusterId());
        String feMaster = globalVariables.get("${feMaster}");
        logger.info("fe master is {}", feMaster);
        serviceRoleInfo.setMasterHost(feMaster);
    }
    
    @Override
    public void handlerServiceRoleCheck(ClusterServiceRoleInstanceEntity roleInstanceEntity,
                                        Map<String, ClusterServiceRoleInstanceEntity> map) {
        Map<String, String> globalVariables = GlobalVariables.get(roleInstanceEntity.getClusterId());
        String feMaster = globalVariables.get("${feMaster}");
        if (roleInstanceEntity.getHostname().equals(feMaster)
                && roleInstanceEntity.getServiceRoleState() == ServiceRoleState.RUNNING) {
            try {
                List<ProcInfo> backends = OlapUtils.showBackends(feMaster);
                resolveProcInfoAlert(roleInstanceEntity.getServiceRoleName(), backends, map);
            } catch (Exception e) {
                
            }
            
        }
        
    }
    
    private void resolveProcInfoAlert(String serviceRoleName, List<ProcInfo> frontends,
                                      Map<String, ClusterServiceRoleInstanceEntity> map) {
        for (ProcInfo frontend : frontends) {
            ClusterServiceRoleInstanceEntity roleInstanceEntity = map.get(frontend.getHostName() + serviceRoleName);
            if (!frontend.getAlive()) {
                String alertTargetName = serviceRoleName + " Not Add To Cluster";
                logger.info("{} at host {} is not add to cluster", serviceRoleName, frontend.getHostName());
                String alertAdvice = "The errmsg is " + frontend.getErrMsg();
                ProcessUtils.saveAlert(roleInstanceEntity, alertTargetName, AlertLevel.WARN, alertAdvice);
            } else {
                ProcessUtils.recoverAlert(roleInstanceEntity);
            }
        }
    }
}
