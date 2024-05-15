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
import com.datasophon.api.master.ActorUtils;
import com.datasophon.api.service.ClusterAlertHistoryService;
import com.datasophon.api.service.ClusterInfoService;
import com.datasophon.api.service.ClusterServiceCommandService;
import com.datasophon.api.service.ClusterServiceInstanceRoleGroupService;
import com.datasophon.api.service.ClusterServiceRoleInstanceService;
import com.datasophon.api.service.ClusterServiceRoleInstanceWebuisService;
import com.datasophon.api.service.FrameServiceRoleService;
import com.datasophon.api.service.FrameServiceService;
import com.datasophon.api.utils.ProcessUtils;
import com.datasophon.common.Constants;
import com.datasophon.common.command.GetLogCommand;
import com.datasophon.common.enums.CommandType;
import com.datasophon.common.utils.CollectionUtils;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.PlaceholderUtils;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterInfoEntity;
import com.datasophon.dao.entity.ClusterServiceInstanceRoleGroup;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceEntity;
import com.datasophon.dao.entity.FrameServiceEntity;
import com.datasophon.dao.entity.FrameServiceRoleEntity;
import com.datasophon.dao.enums.NeedRestart;
import com.datasophon.dao.enums.RoleType;
import com.datasophon.dao.enums.ServiceRoleState;
import com.datasophon.dao.mapper.ClusterServiceRoleInstanceMapper;

import org.apache.commons.lang3.StringUtils;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.additional.query.impl.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import akka.actor.ActorSelection;
import akka.pattern.Patterns;
import akka.util.Timeout;

@Service("clusterServiceRoleInstanceService")
public class ClusterServiceRoleInstanceServiceImpl
        extends
            ServiceImpl<ClusterServiceRoleInstanceMapper, ClusterServiceRoleInstanceEntity>
        implements
            ClusterServiceRoleInstanceService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClusterServiceRoleInstanceServiceImpl.class);
    
    @Autowired
    ClusterInfoService clusterInfoService;
    
    @Autowired
    FrameServiceRoleService frameServiceRoleService;
    
    @Autowired
    FrameServiceService frameService;
    
    @Autowired
    ClusterServiceRoleInstanceService roleInstanceService;
    
    @Autowired
    private ClusterServiceCommandService commandService;
    
    @Autowired
    private ClusterServiceInstanceRoleGroupService roleGroupService;
    
    @Autowired
    private ClusterServiceRoleInstanceMapper roleInstanceMapper;
    
    @Autowired
    private ClusterAlertHistoryService alertHistoryService;
    
    @Autowired
    private ClusterServiceRoleInstanceWebuisService webuisService;
    
    @Override
    public List<ClusterServiceRoleInstanceEntity> listStoppedServiceRoleListByHostnameAndClusterId(String hostname,
                                                                                                   Integer clusterId) {
        return this.lambdaQuery()
                .eq(ClusterServiceRoleInstanceEntity::getClusterId, clusterId)
                .eq(ClusterServiceRoleInstanceEntity::getHostname, hostname)
                .eq(ClusterServiceRoleInstanceEntity::getServiceRoleState, ServiceRoleState.STOP)
                .list();
    }
    
    @Override
    public List<ClusterServiceRoleInstanceEntity> getServiceRoleListByHostnameAndClusterId(String hostname,
                                                                                           Integer clusterId) {
        return this.list(new QueryWrapper<ClusterServiceRoleInstanceEntity>()
                .eq(Constants.CLUSTER_ID, clusterId)
                .eq(Constants.HOSTNAME, hostname));
    }
    
    @Override
    public List<ClusterServiceRoleInstanceEntity> getServiceRoleInstanceListByServiceIdAndRoleState(Integer serviceId,
                                                                                                    ServiceRoleState stop) {
        return this.lambdaQuery()
                .eq(ClusterServiceRoleInstanceEntity::getServiceId, serviceId)
                .eq(ClusterServiceRoleInstanceEntity::getServiceRoleState, stop)
                .list();
    }
    
    @Override
    public ClusterServiceRoleInstanceEntity getOneServiceRole(String name, String hostname, Integer id) {
        List<ClusterServiceRoleInstanceEntity> list = this.list(new QueryWrapper<ClusterServiceRoleInstanceEntity>()
                .eq(Constants.SERVICE_ROLE_NAME, name)
                .eq(StringUtils.isNotBlank(hostname), Constants.HOSTNAME, hostname)
                .eq(Constants.CLUSTER_ID, id));
        if (Objects.nonNull(list) && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }
    
    @Override
    public Result listAll(Integer serviceInstanceId, String hostname, Integer serviceRoleState, String serviceRoleName,
                          Integer roleGroupId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        
        LambdaQueryChainWrapper<ClusterServiceRoleInstanceEntity> wrapper = this.lambdaQuery()
                .eq(ClusterServiceRoleInstanceEntity::getServiceId, serviceInstanceId)
                .eq(Objects.nonNull(serviceRoleState), ClusterServiceRoleInstanceEntity::getServiceRoleState,
                        serviceRoleState)
                .eq(StringUtils.isNotBlank(serviceRoleName), ClusterServiceRoleInstanceEntity::getServiceRoleName,
                        serviceRoleName)
                .eq(Objects.nonNull(roleGroupId), ClusterServiceRoleInstanceEntity::getRoleGroupId, roleGroupId)
                .like(StringUtils.isNotBlank(hostname), ClusterServiceRoleInstanceEntity::getHostname, hostname);
        int count = wrapper.count() == null ? 0 : wrapper.count();
        List<ClusterServiceRoleInstanceEntity> cluServiceRoleInstList = wrapper
                .last("limit " + offset + "," + pageSize)
                .list();
        if (CollectionUtils.isEmpty(cluServiceRoleInstList)) {
            return Result.successEmptyCount();
        }
        
        for (ClusterServiceRoleInstanceEntity roleInstanceEntity : cluServiceRoleInstList) {
            ClusterServiceInstanceRoleGroup roleGroup = roleGroupService.getById(roleInstanceEntity.getRoleGroupId());
            if (Objects.nonNull(roleGroup)) {
                roleInstanceEntity.setRoleGroupName(roleGroup.getRoleGroupName());
            }
            roleInstanceEntity.setServiceRoleStateCode(roleInstanceEntity.getServiceRoleState().getValue());
        }
        
        return Result.success(cluServiceRoleInstList).put(Constants.TOTAL, count);
    }
    
    @Override
    public Result getLog(Integer serviceRoleInstanceId) throws Exception {
        ClusterServiceRoleInstanceEntity roleInstance = this.getById(serviceRoleInstanceId);
        ClusterInfoEntity clusterInfo = clusterInfoService.getById(roleInstance.getClusterId());
        FrameServiceRoleEntity serviceRole = frameServiceRoleService.getServiceRoleByFrameCodeAndServiceRoleName(
                clusterInfo.getClusterFrame(), roleInstance.getServiceRoleName());
        Map<String, String> globalVariables = GlobalVariables.get(roleInstance.getClusterId());
        if (serviceRole.getServiceRoleType() == RoleType.CLIENT) {
            return Result.success("client does not have any log");
        }
        FrameServiceEntity frameServiceEntity = frameService.getById(serviceRole.getServiceId());
        String logFile = serviceRole.getLogFile();
        if (StringUtils.isNotBlank(logFile)) {
            logFile = PlaceholderUtils.replacePlaceholders(logFile, globalVariables, Constants.REGEX_VARIABLE);
            logger.info("logFile is {}", logFile);
        }
        GetLogCommand command = new GetLogCommand();
        command.setLogFile(logFile);
        command.setDecompressPackageName(frameServiceEntity.getDecompressPackageName());
        logger.info("start to get {} log from {}", serviceRole.getServiceRoleName(), roleInstance.getHostname());
        
        ActorSelection configActor = ActorUtils.actorSystem
                .actorSelection("akka.tcp://datasophon@" + roleInstance.getHostname() + ":2552/user/worker/logActor");
        Timeout timeout = new Timeout(Duration.create(60, TimeUnit.SECONDS));
        Future<Object> logFuture = Patterns.ask(configActor, command, timeout);
        ExecResult logResult = (ExecResult) Await.result(logFuture, timeout.duration());
        if (Objects.nonNull(logResult) && logResult.getExecResult()) {
            return Result.success(logResult.getExecOut());
        }
        return Result.success();
    }
    
    @Override
    public List<ClusterServiceRoleInstanceEntity> getServiceRoleInstanceListByServiceId(int id) {
        return this.lambdaQuery().eq(ClusterServiceRoleInstanceEntity::getServiceId, id).list();
    }
    
    @Override
    public List<ClusterServiceRoleInstanceEntity> getServiceRoleInstanceListByClusterId(int clusterId) {
        return this.lambdaQuery().eq(ClusterServiceRoleInstanceEntity::getClusterId, clusterId).list();
    }
    
    @Override
    public Result deleteServiceRole(List<String> idList) {
        Collection<ClusterServiceRoleInstanceEntity> list = this.listByIds(idList);
        // is there a running instance
        boolean flag = false;
        ArrayList<Integer> needRemoveList = new ArrayList<>();
        for (ClusterServiceRoleInstanceEntity instance : list) {
            if (instance.getServiceRoleState() == ServiceRoleState.RUNNING) {
                flag = true;
            } else {
                needRemoveList.add(instance.getId());
            }
        }
        if (!needRemoveList.isEmpty()) {
            alertHistoryService.removeAlertByRoleInstanceIds(needRemoveList);
            this.removeByIds(needRemoveList);
            // delete if there is a webui
            webuisService.removeByRoleInsIds(needRemoveList);
            
        }
        return flag ? Result.error(Status.EXIT_RUNNING_INSTANCES.getMsg()) : Result.success();
    }
    
    @Override
    public List<ClusterServiceRoleInstanceEntity> getServiceRoleInstanceListByClusterIdAndRoleName(Integer clusterId,
                                                                                                   String roleName) {
        return this.list(new QueryWrapper<ClusterServiceRoleInstanceEntity>()
                .eq(Constants.CLUSTER_ID, clusterId).eq(Constants.SERVICE_ROLE_NAME, roleName));
    }
    
    @Override
    public List<ClusterServiceRoleInstanceEntity> getRunningServiceRoleInstanceListByServiceId(Integer serviceInstanceId) {
        return this.lambdaQuery()
                .eq(ClusterServiceRoleInstanceEntity::getServiceId, serviceInstanceId)
                .eq(ClusterServiceRoleInstanceEntity::getServiceRoleState, ServiceRoleState.RUNNING)
                .list();
    }
    
    @Override
    public Result restartObsoleteService(Integer roleGroupId) {
        ClusterServiceInstanceRoleGroup roleGroup = roleGroupService.getById(roleGroupId);
        List<ClusterServiceRoleInstanceEntity> list = this.list(new QueryWrapper<ClusterServiceRoleInstanceEntity>()
                .eq(Constants.ROLE_GROUP_ID, roleGroupId)
                .eq(Constants.NEET_RESTART, NeedRestart.YES));
        if (Objects.nonNull(list) && !list.isEmpty()) {
            List<String> ids = list.stream().map(e -> e.getId() + "").collect(Collectors.toList());
            commandService.generateServiceRoleCommand(roleGroup.getClusterId(), CommandType.RESTART_SERVICE,
                    roleGroup.getServiceInstanceId(), ids);
        } else {
            return Result.error(Status.ROLE_GROUP_HAS_NO_OUTDATED_SERVICE.getMsg());
        }
        return Result.success();
    }
    
    @Override
    public Result decommissionNode(String serviceRoleInstanceIds, String serviceName) throws Exception {
        TreeSet<String> hosts = new TreeSet<>();
        Integer serviceInstanceId = null;
        String serviceRoleName = "";
        for (String str : serviceRoleInstanceIds.split(",")) {
            int serviceRoleInstanceId = Integer.parseInt(str);
            ClusterServiceRoleInstanceEntity roleInstanceEntity = this.getById(serviceRoleInstanceId);
            if ("DataNode".equals(roleInstanceEntity.getServiceRoleName())
                    || "NodeManager".equals(roleInstanceEntity.getServiceRoleName())) {
                hosts.add(roleInstanceEntity.getHostname());
                serviceInstanceId = roleInstanceEntity.getServiceId();
                serviceRoleName = roleInstanceEntity.getServiceRoleName();
                roleInstanceEntity.setServiceRoleState(ServiceRoleState.DECOMMISSIONING);
                this.updateById(roleInstanceEntity);
            }
        }
        // 查询已退役节点
        List<ClusterServiceRoleInstanceEntity> list = this.lambdaQuery()
                .eq(ClusterServiceRoleInstanceEntity::getServiceRoleState, ServiceRoleState.DECOMMISSIONING)
                .in(ClusterServiceRoleInstanceEntity::getId, serviceRoleInstanceIds)
                .list();
        // 添加已退役节点到黑名单
        for (ClusterServiceRoleInstanceEntity roleInstanceEntity : list) {
            hosts.add(roleInstanceEntity.getHostname());
        }
        String type = "blacklist";
        String roleName = "NameNode";
        if ("nodemanager".equalsIgnoreCase(serviceRoleName)) {
            type = "nmexclude";
            roleName = "ResourceManager";
        }
        if (!hosts.isEmpty()) {
            ProcessUtils.hdfsEcMethond(serviceInstanceId, this, hosts, "blacklist", roleName);
        }
        return Result.success();
    }
    
    @Override
    public void updateToNeedRestart(Integer roleGroupId) {
        roleInstanceMapper.updateToNeedRestart(roleGroupId);
    }
    
    @Override
    public void updateToNeedRestartByHost(String hostName) {
        roleInstanceMapper.updateToNeedRestartByHost(hostName);
    }
    
    @Override
    public List<ClusterServiceRoleInstanceEntity> getObsoleteService(Integer serviceInstanceId) {
        return this.lambdaQuery()
                .eq(ClusterServiceRoleInstanceEntity::getServiceId, serviceInstanceId)
                .eq(ClusterServiceRoleInstanceEntity::getNeedRestart, NeedRestart.YES)
                .list();
    }
    
    @Override
    public List<ClusterServiceRoleInstanceEntity> getStoppedRoleInstanceOnHost(Integer clusterId, String hostname,
                                                                               ServiceRoleState state) {
        return roleInstanceService.list(new QueryWrapper<ClusterServiceRoleInstanceEntity>()
                .eq(Constants.CLUSTER_ID, clusterId)
                .eq(Constants.HOSTNAME, hostname)
                .eq(Constants.SERVICE_ROLE_STATE, state));
    }
    
    @Override
    public void reomveRoleInstance(Integer serviceInstanceId) {
        this.lambdaUpdate()
                .eq(ClusterServiceRoleInstanceEntity::getServiceId, serviceInstanceId)
                .eq(ClusterServiceRoleInstanceEntity::getServiceRoleState, ServiceRoleState.STOP)
                .remove();
    }
    
    @Override
    public ClusterServiceRoleInstanceEntity getKAdminRoleIns(Integer clusterId) {
        return this.getOne(new QueryWrapper<ClusterServiceRoleInstanceEntity>()
                .eq(Constants.CLUSTER_ID, clusterId)
                .eq(Constants.SERVICE_ROLE_NAME, "KAdmin"));
    }
    
    @Override
    public List<ClusterServiceRoleInstanceEntity> listServiceRoleByName(String name) {
        return this.list(new QueryWrapper<ClusterServiceRoleInstanceEntity>()
                .eq(Constants.SERVICE_ROLE_NAME, name));
    }
    
    @Override
    public ClusterServiceRoleInstanceEntity getServiceRoleInsByHostAndName(String hostName, String serviceRoleName) {
        return this.getOne(new QueryWrapper<ClusterServiceRoleInstanceEntity>()
                .eq(Constants.HOSTNAME, hostName)
                .eq(Constants.SERVICE_ROLE_NAME, serviceRoleName));
    }
    
    @Override
    public List<ClusterServiceRoleInstanceEntity> listRoleIns(String hostname, String serviceName) {
        return this.lambdaQuery()
                .eq(ClusterServiceRoleInstanceEntity::getHostname, hostname)
                .eq(ClusterServiceRoleInstanceEntity::getServiceName, serviceName).list();
    }
}
