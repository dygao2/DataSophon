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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import com.datasophon.api.service.ClusterGroupService;
import com.datasophon.api.service.ClusterInfoService;
import com.datasophon.api.service.ClusterServiceCommandService;
import com.datasophon.api.service.ClusterServiceRoleInstanceService;
import com.datasophon.api.service.ClusterUserService;
import com.datasophon.api.service.host.ClusterHostService;
import com.datasophon.api.utils.ProcessUtils;
import com.datasophon.api.utils.SpringTool;
import com.datasophon.common.Constants;
import com.datasophon.common.cache.CacheUtils;
import com.datasophon.common.command.GenerateHostPrometheusConfig;
import com.datasophon.common.enums.CommandType;
import com.datasophon.common.enums.InstallState;
import com.datasophon.common.model.HostInfo;
import com.datasophon.common.model.StartWorkerMessage;
import com.datasophon.common.model.WorkerServiceMessage;
import com.datasophon.common.utils.CollectionUtils;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterGroup;
import com.datasophon.dao.entity.ClusterHostDO;
import com.datasophon.dao.entity.ClusterInfoEntity;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceEntity;
import com.datasophon.dao.entity.ClusterUser;
import com.datasophon.dao.enums.ServiceRoleState;
import com.datasophon.domain.host.enums.MANAGED;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import cn.hutool.core.util.ObjectUtil;

public class WorkerStartActor extends UntypedActor {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkerStartActor.class);
    
    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof StartWorkerMessage) {
            StartWorkerMessage msg = (StartWorkerMessage) message;
            String hostname = msg.getHostname();
            Integer clusterId = msg.getClusterId();
            logger.info("Receive message when worker first start :{}", hostname);
            
            ClusterHostService clusterHostService =
                    SpringTool.getApplicationContext().getBean(ClusterHostService.class);
            ClusterInfoService clusterInfoService =
                    SpringTool.getApplicationContext().getBean(ClusterInfoService.class);
            
            // is managed?
            ClusterHostDO hostEntity = clusterHostService.getClusterHostByHostname(hostname);
            ClusterInfoEntity cluster = clusterInfoService.getById(clusterId);
            logger.info("Host install set to 100%");
            if (CacheUtils.constainsKey(cluster.getClusterCode() + Constants.HOST_MAP)) {
                HashMap<String, HostInfo> map =
                        (HashMap<String, HostInfo>) CacheUtils.get(cluster.getClusterCode() + Constants.HOST_MAP);
                HostInfo hostInfo = map.get(hostname);
                if (Objects.nonNull(hostInfo)) {
                    hostInfo.setProgress(Constants.ONE_HUNDRRD);
                    hostInfo.setInstallState(InstallState.SUCCESS);
                    hostInfo.setInstallStateCode(InstallState.SUCCESS.getValue());
                    hostInfo.setManaged(true);
                }
            }
            if (ObjectUtil.isNull(hostEntity)) {
                // save to db
                ProcessUtils.saveHostInstallInfo(msg, cluster.getClusterCode(), clusterHostService);
                logger.info("Host install save to database");
                // sync cluster user and group
                // syncClusterUserAndGroup(clusterId, hostname);
            } else {
                hostEntity.setCpuArchitecture(msg.getCpuArchitecture());
                hostEntity.setManaged(MANAGED.YES);
                clusterHostService.updateById(hostEntity);
            }
            
            // add to prometheus
            ActorRef prometheusActor =
                    ActorUtils.getLocalActor(PrometheusActor.class, ActorUtils.getActorRefName(PrometheusActor.class));
            GenerateHostPrometheusConfig prometheusConfigCommand = new GenerateHostPrometheusConfig();
            prometheusConfigCommand.setClusterId(cluster.getId());
            prometheusActor.tell(prometheusConfigCommand, getSelf());
            
            // tell to worker what need to start
            autoAddServiceOperatorNeeded(msg.getHostname(), cluster.getId(), CommandType.START_SERVICE, false);
        } else if (message instanceof WorkerServiceMessage) {
            WorkerServiceMessage msg = (WorkerServiceMessage) message;
            // tell to worker what need to start/stop
            autoAddServiceOperatorNeeded(msg.getHostname(), msg.getClusterId(), msg.getCommandType(), true);
        }
    }
    
    /**
     * Automatically start/stop services that need to be started
     *
     * @param clusterId
     */
    private void autoAddServiceOperatorNeeded(String hostname, Integer clusterId, CommandType commandType,
                                              boolean needRestart) {
        ClusterServiceRoleInstanceService roleInstanceService =
                SpringTool.getApplicationContext().getBean(ClusterServiceRoleInstanceService.class);
        ClusterServiceCommandService serviceCommandService =
                SpringTool.getApplicationContext().getBean(ClusterServiceCommandService.class);
        
        List<ClusterServiceRoleInstanceEntity> serviceRoleList = null;
        // 启动服务
        if (CommandType.START_SERVICE.equals(commandType)) {
            serviceRoleList = roleInstanceService
                    .listStoppedServiceRoleListByHostnameAndClusterId(hostname, clusterId);
            // 重启时重刷服务配置以支持磁盘故障等问题
            if (needRestart) {
                roleInstanceService.updateToNeedRestartByHost(hostname);
            }
        }
        
        // 停止运行状态的服务
        if (commandType.STOP_SERVICE.equals(commandType)) {
            serviceRoleList = roleInstanceService
                    .getServiceRoleListByHostnameAndClusterId(hostname, clusterId).stream()
                    .filter(roleInstance -> (!ServiceRoleState.STOP.equals(roleInstance.getServiceRoleState()) &&
                            !ServiceRoleState.DECOMMISSIONED.equals(roleInstance.getServiceRoleState())))
                    .collect(toList());
        }
        
        if (CollectionUtils.isEmpty(serviceRoleList)) {
            logger.info("No services need to start at host {}.", hostname);
            return;
        }
        
        Map<Integer, List<String>> serviceRoleMap = serviceRoleList.stream()
                .collect(
                        groupingBy(
                                ClusterServiceRoleInstanceEntity::getServiceId,
                                mapping(i -> String.valueOf(i.getId()), toList())));
        Result result =
                serviceCommandService.generateServiceRoleCommands(clusterId, commandType, serviceRoleMap);
        if (result.getCode() == 200) {
            logger.info("Auto-start services successful");
        } else {
            logger.info("Some service auto-start failed, please check logs of the services that failed to start.");
        }
    }
    
    private void syncClusterUserAndGroup(Integer clusterId, String hostname) {
        ClusterGroupService clusterGroupService = SpringTool.getApplicationContext().getBean(ClusterGroupService.class);
        ClusterUserService clusterUserService = SpringTool.getApplicationContext().getBean(ClusterUserService.class);
        
        List<ClusterGroup> userGroupList = clusterGroupService.listAllUserGroup(clusterId);
        for (ClusterGroup clusterGroup : userGroupList) {
            String groupName = clusterGroup.getGroupName();
            clusterGroupService.createUnixGroupOnHost(hostname, groupName);
        }
        List<ClusterUser> userList = clusterUserService.listAllUser(clusterId);
        for (ClusterUser clusterUser : userList) {
            clusterUserService.createUnixUserOnHost(clusterUser, hostname);
        }
    }
    
}
