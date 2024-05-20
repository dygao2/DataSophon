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
import com.datasophon.api.master.ActorUtils;
import com.datasophon.api.master.DAGBuildActor;
import com.datasophon.api.service.ClusterInfoService;
import com.datasophon.api.service.ClusterServiceCommandHostCommandService;
import com.datasophon.api.service.ClusterServiceCommandHostService;
import com.datasophon.api.service.ClusterServiceCommandService;
import com.datasophon.api.service.ClusterServiceInstanceConfigService;
import com.datasophon.api.service.ClusterServiceInstanceService;
import com.datasophon.api.service.ClusterServiceRoleInstanceService;
import com.datasophon.api.service.FrameServiceRoleService;
import com.datasophon.api.service.FrameServiceService;
import com.datasophon.api.service.host.ClusterHostService;
import com.datasophon.api.utils.ProcessUtils;
import com.datasophon.common.Constants;
import com.datasophon.common.cache.CacheUtils;
import com.datasophon.common.command.StartExecuteCommandCommand;
import com.datasophon.common.enums.CommandType;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterInfoEntity;
import com.datasophon.dao.entity.ClusterServiceCommandEntity;
import com.datasophon.dao.entity.ClusterServiceCommandHostCommandEntity;
import com.datasophon.dao.entity.ClusterServiceCommandHostEntity;
import com.datasophon.dao.entity.ClusterServiceInstanceEntity;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceEntity;
import com.datasophon.dao.entity.FrameServiceEntity;
import com.datasophon.dao.entity.FrameServiceRoleEntity;
import com.datasophon.dao.mapper.ClusterServiceCommandMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import akka.actor.ActorRef;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;

@Service("clusterServiceCommandService")
public class ClusterServiceCommandServiceImpl
        extends
            ServiceImpl<ClusterServiceCommandMapper, ClusterServiceCommandEntity>
        implements
            ClusterServiceCommandService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClusterServiceCommandServiceImpl.class);
    
    @Autowired
    private ClusterInfoService clusterInfoService;
    
    @Autowired
    private ClusterServiceCommandHostService commandHostService;
    
    @Autowired
    private ClusterServiceCommandHostCommandService hostCommandService;
    
    @Autowired
    private FrameServiceService frameServiceService;
    
    @Autowired
    private FrameServiceRoleService frameServiceRoleService;
    
    @Autowired
    private ClusterServiceCommandService commandService;
    
    @Autowired
    private ClusterHostService hostService;
    
    @Autowired
    private ClusterServiceInstanceService serviceInstanceService;
    
    @Autowired
    private ClusterServiceInstanceConfigService serviceInstanceConfigService;
    
    @Autowired
    private ClusterServiceRoleInstanceService roleInstanceService;
    
    @Override
    @Transactional
    public Result generateCommand(Integer clusterId, CommandType commandType, List<String> serviceNames) {
        ClusterInfoEntity clusterInfo = clusterInfoService.getById(clusterId);
        
        List<ClusterServiceCommandEntity> list = new ArrayList<>();
        List<ClusterServiceCommandHostEntity> commandHostList = new ArrayList<>();
        List<ClusterServiceCommandHostCommandEntity> hostCommandList = new ArrayList<>();
        List<String> commandIds = new ArrayList<String>();
        
        Map<String, List<String>> serviceRoleHostMap = (Map<String, List<String>>) CacheUtils
                .get(clusterInfo.getClusterCode() + Constants.UNDERLINE + Constants.SERVICE_ROLE_HOST_MAPPING);
        
        for (String serviceName : serviceNames) {
            // 1、生成操作指令
            ClusterServiceInstanceEntity serviceInstance =
                    serviceInstanceService.getServiceInstanceByClusterIdAndServiceName(clusterId, serviceName);
            
            ClusterServiceCommandEntity commandEntity =
                    ProcessUtils.generateCommandEntity(clusterId, commandType, serviceName);
            commandEntity.setServiceInstanceId(serviceInstance.getId());
            list.add(commandEntity);
            String commandId = commandEntity.getCommandId();
            commandIds.add(commandId);
            
            // 查询服务的服务角色
            FrameServiceEntity frameService =
                    frameServiceService.getServiceByFrameCodeAndServiceName(clusterInfo.getClusterFrame(), serviceName);
            Result result =
                    frameServiceRoleService.getServiceRoleList(clusterId, String.valueOf(frameService.getId()), null);
            List<FrameServiceRoleEntity> serviceRoleList = (List<FrameServiceRoleEntity>) result.getData();
            HashMap<String, ClusterServiceCommandHostEntity> map = new HashMap<>();
            for (FrameServiceRoleEntity serviceRole : serviceRoleList) {
                if (Objects.nonNull(serviceRoleHostMap)
                        && serviceRoleHostMap.containsKey(serviceRole.getServiceRoleName())) {
                    List<String> hosts = serviceRoleHostMap.get(serviceRole.getServiceRoleName());
                    for (String hostname : hosts) {
                        if (alreadyExistsServiceRole(serviceRole.getServiceRoleName(), hostname, clusterId)) {
                            continue;
                        } else {
                            ClusterServiceCommandHostEntity commandHost;
                            if (map.containsKey(hostname)) {
                                commandHost = map.get(hostname);
                            } else {
                                commandHost = ProcessUtils.generateCommandHostEntity(commandId, hostname);
                                commandHostList.add(commandHost);
                                map.put(hostname, commandHost);
                            }
                            // 4、生成主机操作指令
                            ClusterServiceCommandHostCommandEntity hostCommand =
                                    ProcessUtils.generateCommandHostCommandEntity(commandType, commandId,
                                            serviceRole.getServiceRoleName(), serviceRole.getServiceRoleType(),
                                            commandHost);
                            hostCommandList.add(hostCommand);
                        }
                    }
                }
            }
        }
        if (commandHostList.isEmpty()) {
            logger.warn("No service role selected");
            return Result.error(Status.NO_SERVICE_ROLE_SELECTED.getMsg());
        }
        commandService.saveBatch(list);
        commandHostService.saveBatch(commandHostList);
        hostCommandService.saveBatch(hostCommandList);
        return Result.success(String.join(",", commandIds));
    }
    
    private boolean alreadyExistsServiceRole(String serviceRoleName, String hostname, Integer clusterId) {
        ClusterServiceRoleInstanceEntity serviceRole =
                roleInstanceService.getOneServiceRole(serviceRoleName, hostname, clusterId);
        if (Objects.nonNull(serviceRole)) {
            return true;
        }
        return false;
    }
    
    @Override
    public Result getServiceCommandlist(Integer clusterId, Integer page, Integer pageSize) {
        Integer offset = (page - 1) * pageSize;
        List<ClusterServiceCommandEntity> list = this.list(new QueryWrapper<ClusterServiceCommandEntity>()
                .eq(Constants.CLUSTER_ID, clusterId)
                .orderByDesc(Constants.CREATE_TIME).last("limit " + offset + "," + pageSize));
        Integer total = this.count(new QueryWrapper<ClusterServiceCommandEntity>()
                .eq(Constants.CLUSTER_ID, clusterId));
        for (ClusterServiceCommandEntity commandEntity : list) {
            commandEntity.setCommandStateCode(commandEntity.getCommandState().getValue());
            Date createTime = commandEntity.getCreateTime();
            Date endTime = commandEntity.getEndTime();
            if (Objects.isNull(endTime)) {
                endTime = new Date();
            }
            long between = DateUtil.between(createTime, endTime, DateUnit.MS);
            String durationTime = DateUtil.formatBetween(between, BetweenFormatter.Level.SECOND);
            commandEntity.setDurationTime(durationTime);
        }
        return Result.success(list).put(Constants.TOTAL, total);
    }
    
    /**
     * 1、生成指令
     * 2、生成主机指令
     * 3、生产主机上操作指令
     *
     * @param clusterId
     * @param commandType
     * @param serviceInstanceIds
     * @return
     */
    @Override
    public Result generateServiceCommand(Integer clusterId, CommandType commandType, List<String> serviceInstanceIds) {
        List<ClusterServiceCommandEntity> list = new ArrayList<>();
        List<ClusterServiceCommandHostEntity> commandHostList = new ArrayList<>();
        List<ClusterServiceCommandHostCommandEntity> hostCommandList = new ArrayList<>();
        List<String> commandIds = new ArrayList<>();
        for (String serviceInstanceId : serviceInstanceIds) {
            int id = Integer.parseInt(serviceInstanceId);
            // 查询服务对应的服务角色实例
            List<ClusterServiceRoleInstanceEntity> roleInstanceList =
                    roleInstanceService.getServiceRoleInstanceListByServiceId(id);
            if (Objects.isNull(roleInstanceList) || roleInstanceList.isEmpty()) {
                continue;
            }
            ClusterServiceInstanceEntity serviceInstance = serviceInstanceService.getById(id);
            ClusterServiceCommandEntity commandEntity =
                    ProcessUtils.generateCommandEntity(clusterId, commandType, serviceInstance.getServiceName());
            String commandId = commandEntity.getCommandId();
            commandEntity.setServiceInstanceId(id);
            commandIds.add(commandId);
            list.add(commandEntity);
            
            HashMap<String, ClusterServiceCommandHostEntity> map = new HashMap<>();
            for (ClusterServiceRoleInstanceEntity roleInstance : roleInstanceList) {
                ClusterServiceCommandHostEntity commandHost;
                if (map.containsKey(roleInstance.getHostname())) {
                    commandHost = map.get(roleInstance.getHostname());
                } else {
                    commandHost = ProcessUtils.generateCommandHostEntity(commandId, roleInstance.getHostname());
                    commandHostList.add(commandHost);
                }
                ClusterServiceCommandHostCommandEntity hostCommand =
                        ProcessUtils.generateCommandHostCommandEntity(commandType, commandId,
                                roleInstance.getServiceRoleName(), roleInstance.getRoleType(), commandHost);
                hostCommandList.add(hostCommand);
                map.put(roleInstance.getHostname(), commandHost);
            }
        }
        if (!list.isEmpty()) {
            commandService.saveBatch(list);
            commandHostService.saveBatch(commandHostList);
            hostCommandService.saveBatch(hostCommandList);
            
            // 通知commandActor执行命令
            ActorRef dagBuildActor =
                    ActorUtils.getLocalActor(DAGBuildActor.class, ActorUtils.getActorRefName(DAGBuildActor.class));
            dagBuildActor.tell(new StartExecuteCommandCommand(commandIds, clusterId, commandType), ActorRef.noSender());
        }
        return Result.success(String.join(",", commandIds));
    }
    
    @Override
    public Result generateServiceRoleCommands(Integer clusterId, CommandType commandType,
                                              Map<Integer, List<String>> instanceIdMap) {
        Result result = null;
        for (Map.Entry<Integer, List<String>> entry : instanceIdMap.entrySet()) {
            result = generateServiceRoleCommand(clusterId, commandType, entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    @Override
    public Result generateServiceRoleCommand(Integer clusterId, CommandType commandType, Integer serviceInstanceId,
                                             List<String> serviceRoleInstanceIds) {
        List<ClusterServiceCommandEntity> list = new ArrayList<>();
        List<ClusterServiceCommandHostEntity> commandHostList = new ArrayList<>();
        List<ClusterServiceCommandHostCommandEntity> hostCommandList = new ArrayList<>();
        List<String> commandIds = new ArrayList<>();
        
        ClusterServiceInstanceEntity serviceInstance = serviceInstanceService.getById(serviceInstanceId);
        ClusterServiceCommandEntity commandEntity =
                ProcessUtils.generateCommandEntity(clusterId, commandType, serviceInstance.getServiceName());
        String commandId = commandEntity.getCommandId();
        commandEntity.setServiceInstanceId(serviceInstanceId);
        commandIds.add(commandId);
        list.add(commandEntity);
        // 查询服务对应的服务角色实例
        HashMap<String, ClusterServiceCommandHostEntity> map = new HashMap<>();
        for (String serviceRoleInstanceId : serviceRoleInstanceIds) {
            int id = Integer.parseInt(serviceRoleInstanceId);
            ClusterServiceRoleInstanceEntity roleInstance = roleInstanceService.getById(id);
            
            ClusterServiceCommandHostEntity commandHost;
            if (map.containsKey(roleInstance.getHostname())) {
                commandHost = map.get(roleInstance.getHostname());
            } else {
                commandHost = ProcessUtils.generateCommandHostEntity(commandId, roleInstance.getHostname());
                commandHostList.add(commandHost);
            }
            ClusterServiceCommandHostCommandEntity hostCommand = ProcessUtils.generateCommandHostCommandEntity(
                    commandType, commandId, roleInstance.getServiceRoleName(), roleInstance.getRoleType(), commandHost);
            hostCommandList.add(hostCommand);
            map.put(roleInstance.getHostname(), commandHost);
        }
        commandService.saveBatch(list);
        commandHostService.saveBatch(commandHostList);
        hostCommandService.saveBatch(hostCommandList);
        
        // 通知commandActor执行命令
        ActorRef dagBuildActor =
                ActorUtils.getLocalActor(DAGBuildActor.class, ActorUtils.getActorRefName(DAGBuildActor.class));
        dagBuildActor.tell(new StartExecuteCommandCommand(commandIds, clusterId, commandType), ActorRef.noSender());
        return Result.success(String.join(",", commandIds));
    }
    
    @Override
    public void startExecuteCommand(Integer clusterId, String commandType, String commandIds) {
        List<String> list = Arrays.asList(commandIds.split(","));
        CommandType command = EnumUtil.fromString(CommandType.class, commandType);
        // 通知commandActor执行命令
        ActorRef dagBuildActor =
                ActorUtils.getLocalActor(DAGBuildActor.class, ActorUtils.getActorRefName(DAGBuildActor.class));
        dagBuildActor.tell(new StartExecuteCommandCommand(list, clusterId, command), ActorRef.noSender());
    }
    
    @Override
    public void cancelCommand(String commandId) {
        // command , command host, host command状态置为取消
        
    }
    
    @Override
    public ClusterServiceCommandEntity getLastRestartCommand(Integer serviceInstanceId) {
        return this.getOne(
                new QueryWrapper<ClusterServiceCommandEntity>().eq(Constants.SERVICE_INSTANCE_ID, serviceInstanceId)
                        .eq(Constants.COMMAND_TYPE, CommandType.RESTART_SERVICE.getValue()).or()
                        .eq(Constants.COMMAND_TYPE, CommandType.INSTALL_SERVICE.getValue())
                        .orderByDesc(Constants.CREATE_TIME).last("limit 1"));
    }
    
    @Override
    public ClusterServiceCommandEntity getCommandById(String commandId) {
        return this.getOne(
                new QueryWrapper<ClusterServiceCommandEntity>().eq("command_id", commandId));
    }
}
