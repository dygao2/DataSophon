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

package com.datasophon.api.master;

import com.datasophon.api.master.handler.service.ServiceConfigureHandler;
import com.datasophon.api.service.ClusterInfoService;
import com.datasophon.api.service.ClusterServiceRoleInstanceService;
import com.datasophon.api.service.host.ClusterHostService;
import com.datasophon.api.utils.PackageUtils;
import com.datasophon.api.utils.ProcessUtils;
import com.datasophon.api.utils.SpringTool;
import com.datasophon.common.command.GenerateRackPropCommand;
import com.datasophon.common.model.Generators;
import com.datasophon.common.model.ServiceConfig;
import com.datasophon.common.model.ServiceRoleInfo;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.dao.entity.ClusterHostDO;
import com.datasophon.dao.entity.ClusterInfoEntity;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.toolkit.Constants;

import akka.actor.UntypedActor;

public class RackActor extends UntypedActor {
    
    private static final Logger logger = LoggerFactory.getLogger(RackActor.class);
    
    @Override
    public void onReceive(Object msg) throws Throwable {
        if (msg instanceof GenerateRackPropCommand) {
            GenerateRackPropCommand command = (GenerateRackPropCommand) msg;
            
            ClusterServiceRoleInstanceService roleInstanceService =
                    SpringTool.getApplicationContext().getBean(ClusterServiceRoleInstanceService.class);
            ClusterHostService hostService = SpringTool.getApplicationContext().getBean(ClusterHostService.class);
            ClusterInfoService clusterInfoService =
                    SpringTool.getApplicationContext().getBean(ClusterInfoService.class);
            // update rack table
            List<ClusterServiceRoleInstanceEntity> roleList = roleInstanceService
                    .getServiceRoleInstanceListByClusterIdAndRoleName(command.getClusterId(), "NameNode");
            ClusterInfoEntity clusterInfo = clusterInfoService.getById(command.getClusterId());
            // build config file map
            HashMap<Generators, List<ServiceConfig>> configFileMap = new HashMap<>();
            Generators generators = new Generators();
            generators.setFilename("rack.properties");
            generators.setOutputDirectory("etc/hadoop");
            generators.setConfigFormat("properties2");
            
            ArrayList<ServiceConfig> serviceConfigs = new ArrayList<>();
            List<ClusterHostDO> hostList = hostService.list();
            for (ClusterHostDO clusterHostDO : hostList) {
                ServiceConfig serviceConfig = ProcessUtils.createServiceConfig(clusterHostDO.getIp(),
                        Constants.SLASH + clusterHostDO.getRack(), "input");
                serviceConfigs.add(serviceConfig);
            }
            configFileMap.put(generators, serviceConfigs);
            for (ClusterServiceRoleInstanceEntity roleInstanceEntity : roleList) {
                // generate rack.properties
                ServiceRoleInfo serviceRoleInfo = new ServiceRoleInfo();
                serviceRoleInfo.setName("NameNode");
                serviceRoleInfo.setParentName("HDFS");
                serviceRoleInfo.setConfigFileMap(configFileMap);
                serviceRoleInfo.setDecompressPackageName(
                        PackageUtils.getServiceDcPackageName(clusterInfo.getClusterFrame(), "HDFS"));
                serviceRoleInfo.setHostname(roleInstanceEntity.getHostname());
                ServiceConfigureHandler configureHandler = new ServiceConfigureHandler();
                ExecResult execResult = configureHandler.handlerRequest(serviceRoleInfo);
                if (!execResult.getExecResult()) {
                    logger.error("generate rack.properties failed");
                }
            }
        }
    }
}
