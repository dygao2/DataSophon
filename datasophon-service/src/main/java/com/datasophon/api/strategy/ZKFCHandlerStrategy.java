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
import com.datasophon.common.model.ServiceConfig;
import com.datasophon.common.model.ServiceRoleInfo;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceEntity;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZKFCHandlerStrategy implements ServiceRoleStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(ZKFCHandlerStrategy.class);
    
    @Override
    public void handler(Integer clusterId, List<String> hosts, String serviceName) {
        Map<String, String> globalVariables = GlobalVariables.get(clusterId);
        if (hosts.size() == 2) {
            ProcessUtils.generateClusterVariable(globalVariables, clusterId, serviceName, "${ZKFC1}", hosts.get(0));
            ProcessUtils.generateClusterVariable(globalVariables, clusterId, serviceName, "${ZKFC2}", hosts.get(1));
        }
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
        if (hostname.equals(globalVariables.get("${ZKFC2}"))) {
            logger.info("set to slave zkfc");
            serviceRoleInfo.setSlave(true);
            serviceRoleInfo.setSortNum(6);
        }
    }
    
    @Override
    public void handlerServiceRoleCheck(ClusterServiceRoleInstanceEntity roleInstanceEntity,
                                        Map<String, ClusterServiceRoleInstanceEntity> map) {
        
    }
}
