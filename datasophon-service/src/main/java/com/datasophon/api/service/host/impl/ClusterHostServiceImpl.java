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

package com.datasophon.api.service.host.impl;

import com.datasophon.api.enums.Status;
import com.datasophon.api.master.ActorUtils;
import com.datasophon.api.master.PrometheusActor;
import com.datasophon.api.master.RackActor;
import com.datasophon.api.service.ClusterInfoService;
import com.datasophon.api.service.ClusterRackService;
import com.datasophon.api.service.ClusterServiceRoleInstanceService;
import com.datasophon.api.service.host.ClusterHostService;
import com.datasophon.api.service.host.dto.QueryHostListPageDTO;
import com.datasophon.common.Constants;
import com.datasophon.common.cache.CacheUtils;
import com.datasophon.common.command.ExecuteCmdCommand;
import com.datasophon.common.command.GenerateHostPrometheusConfig;
import com.datasophon.common.command.GenerateRackPropCommand;
import com.datasophon.common.model.HostInfo;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterHostDO;
import com.datasophon.dao.entity.ClusterInfoEntity;
import com.datasophon.dao.entity.ClusterRack;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceEntity;
import com.datasophon.dao.enums.RoleType;
import com.datasophon.dao.enums.ServiceRoleState;
import com.datasophon.dao.mapper.ClusterHostMapper;
import com.datasophon.domain.host.enums.HostState;

import org.apache.commons.lang3.StringUtils;

import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import akka.actor.ActorRef;
import cn.hutool.crypto.SecureUtil;

@Service("clusterHostService")
@Transactional
public class ClusterHostServiceImpl extends ServiceImpl<ClusterHostMapper, ClusterHostDO>
        implements
            ClusterHostService {
    
    @Autowired
    ClusterHostMapper hostMapper;
    
    @Autowired
    ClusterServiceRoleInstanceService roleInstanceService;
    
    @Autowired
    ClusterInfoService clusterInfoService;
    
    @Autowired
    ClusterRackService clusterRackService;
    
    private final String ip = "ip";
    
    @Override
    public ClusterHostDO getClusterHostByHostname(String hostname) {
        return hostMapper.getClusterHostByHostname(hostname);
    }
    
    @Override
    public Result listByPage(Integer clusterId, String hostname, String ip, String cpuArchitecture, Integer hostState,
                             String orderField, String orderType, Integer page, Integer pageSize) {
        List<QueryHostListPageDTO> hostListPageDTOS = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        List<ClusterHostDO> list =
                this.list(new QueryWrapper<ClusterHostDO>().eq(Constants.CLUSTER_ID, clusterId)
                        .eq(Constants.MANAGED, 1)
                        .eq(StringUtils.isNotBlank(cpuArchitecture), Constants.CPU_ARCHITECTURE, cpuArchitecture)
                        .eq(hostState != null, Constants.HOST_STATE, hostState)
                        .like(StringUtils.isNotBlank(ip), this.ip, ip)
                        .like(StringUtils.isNotBlank(hostname), Constants.HOSTNAME, hostname)
                        .orderByAsc("asc".equals(orderType), orderField)
                        .orderByDesc("desc".equals(orderType), orderField)
                        .last("limit " + offset + "," + pageSize));
        
        // 回显rack的名称 而不是ID
        Map<String, String> rackMap = clusterRackService.queryClusterRack(clusterId).stream()
                .collect(Collectors.toMap(obj -> obj.getId() + "", ClusterRack::getRack));
        for (ClusterHostDO clusterHostDO : list) {
            QueryHostListPageDTO queryHostListPageDTO = new QueryHostListPageDTO();
            BeanUtils.copyProperties(clusterHostDO, queryHostListPageDTO);
            // 查询主机上服务角色数
            int serviceRoleNum = roleInstanceService.count(new QueryWrapper<ClusterServiceRoleInstanceEntity>()
                    .eq(Constants.HOSTNAME, clusterHostDO.getHostname()));
            queryHostListPageDTO.setServiceRoleNum(serviceRoleNum);
            queryHostListPageDTO.setHostState(clusterHostDO.getHostState().getValue());
            queryHostListPageDTO.setRack(rackMap.getOrDefault(queryHostListPageDTO.getRack(), "/default-rack"));
            hostListPageDTOS.add(queryHostListPageDTO);
        }
        int count = this.count(new QueryWrapper<ClusterHostDO>().eq(Constants.CLUSTER_ID, clusterId)
                .eq(Constants.MANAGED, 1)
                .eq(StringUtils.isNotBlank(cpuArchitecture), Constants.CPU_ARCHITECTURE, cpuArchitecture)
                .eq(hostState != null, Constants.HOST_STATE, hostState)
                .like(StringUtils.isNotBlank(hostname), Constants.HOSTNAME, hostname));
        return Result.success(hostListPageDTOS).put(Constants.TOTAL, count);
    }
    
    @Override
    public List<ClusterHostDO> getHostListByClusterId(Integer clusterId) {
        return this.list(new QueryWrapper<ClusterHostDO>()
                .eq(Constants.CLUSTER_ID, clusterId)
                .eq(Constants.MANAGED, 1));
    }
    
    @Override
    public Result getRoleListByHostname(Integer clusterId, String hostname) {
        List<ClusterServiceRoleInstanceEntity> list =
                roleInstanceService.getServiceRoleListByHostnameAndClusterId(hostname, clusterId);
        for (ClusterServiceRoleInstanceEntity roleInstanceEntity : list) {
            roleInstanceEntity.setServiceRoleStateCode(roleInstanceEntity.getServiceRoleState().getValue());
        }
        return Result.success(list);
    }
    
    /**
     * 批量删除主机。
     * 删除主机，首先停止主机上的服务
     * 其次删除主机 worker，同时移除 Prometheus hosts
     * 然后删除主机运行的实例
     *
     * @param hostIds
     * @return
     */
    @Override
    @Transactional
    public Result deleteHosts(String hostIds) {
        // 批量移除
        String[] ids = hostIds.split(Constants.COMMA);
        for (String hostId : ids) {
            ClusterHostDO host = this.getById(hostId);
            // 获取主机上运行的服务
            List<ClusterServiceRoleInstanceEntity> listRunningService =
                    roleInstanceService.list(new QueryWrapper<ClusterServiceRoleInstanceEntity>()
                            .eq(Constants.CLUSTER_ID, host.getClusterId())
                            .eq(Constants.HOSTNAME, host.getHostname())
                            .eq(Constants.SERVICE_ROLE_STATE, ServiceRoleState.RUNNING)
                            .ne(Constants.ROLE_TYPE, RoleType.CLIENT));
            List<String> runningRoles = listRunningService.stream().map(ClusterServiceRoleInstanceEntity::getServiceRoleName)
                    .collect(Collectors.toList());
            if (!listRunningService.isEmpty()) {
                return Result.error(host.getHostname() + Status.HOST_EXIT_ONE_RUNNING_ROLE.getMsg() + runningRoles);
            }
            // 获取主机上安装的服务
            List<ClusterServiceRoleInstanceEntity> listInstalledSerivce =
                    roleInstanceService.list(new QueryWrapper<ClusterServiceRoleInstanceEntity>()
                            .eq(Constants.CLUSTER_ID, host.getClusterId())
                            .eq(Constants.HOSTNAME, host.getHostname()));
            List<String> installedRoles = listInstalledSerivce.stream().map(ClusterServiceRoleInstanceEntity::getServiceRoleName)
                    .collect(Collectors.toList());
            if (!listInstalledSerivce.isEmpty()) {
                return Result.error(host.getHostname() + Status.HOST_EXIT_ONE_INSTALLED_ROLE.getMsg() + installedRoles);
            }

            ClusterInfoEntity clusterInfo = clusterInfoService.getById(host.getClusterId());
            String clusterCode = clusterInfo.getClusterCode();
            String distributeAgentKey = clusterCode + Constants.UNDERLINE + Constants.START_DISTRIBUTE_AGENT;
            if (CacheUtils.constainsKey(distributeAgentKey + Constants.UNDERLINE + host.getHostname())) {
                CacheUtils.removeKey(distributeAgentKey + Constants.UNDERLINE + host.getHostname());
            }
            
            this.removeById(hostId);
            
            if (host.getHostState() != HostState.OFFLINE) {
                // stop the worker on this host
                ActorRef execCmdActor = ActorUtils.getRemoteActor(host.getHostname(), "executeCmdActor");
                ExecuteCmdCommand command = new ExecuteCmdCommand();
                ArrayList<String> commands = new ArrayList<>();
                commands.add("service");
                commands.add("datasophon-worker");
                commands.add("stop");
                
                command.setCommands(commands);
                execCmdActor.tell(command, ActorRef.noSender());
            }
            // remove host from prometheus
            ActorRef prometheusActor =
                    ActorUtils.getLocalActor(PrometheusActor.class, ActorUtils.getActorRefName(PrometheusActor.class));
            
            // Prometheus 移除 hosts 信息
            GenerateHostPrometheusConfig prometheusConfigCommand = new GenerateHostPrometheusConfig();
            prometheusConfigCommand.setClusterId(clusterInfo.getId());
            
            ActorUtils.actorSystem.scheduler().scheduleOnce(
                    FiniteDuration.apply(3L, TimeUnit.SECONDS),
                    prometheusActor,
                    prometheusConfigCommand,
                    ActorUtils.actorSystem.dispatcher(),
                    ActorRef.noSender());
            
            // remove the host from the cache
            Map<String, HostInfo> map =
                    (Map<String, HostInfo>) CacheUtils.get(clusterCode + Constants.HOST_MAP);
            String md5 = SecureUtil.md5(host.getHostname());
            if (Objects.nonNull(map)) {
                map.remove(host.getHostname());
            }
            if (CacheUtils.constainsKey(clusterCode + Constants.HOST_MD5)
                    && md5.equals(CacheUtils.getString(clusterCode + Constants.HOST_MD5))) {
                CacheUtils.removeKey(clusterCode + Constants.HOST_MD5);
            }
        }
        return Result.success();
    }
    
    @Override
    public Result getRack(Integer clusterId) {
        ArrayList<JSONObject> list = new ArrayList<>();
        JSONObject rack = new JSONObject();
        rack.put("rack", "/default-rack");
        list.add(rack);
        return Result.success(list);
    }
    
    @Override
    public void removeHostByClusterId(Integer clusterId) {
        this.remove(new QueryWrapper<ClusterHostDO>().eq(Constants.CLUSTER_ID, clusterId));
    }
    
    @Override
    public void updateBatchNodeLabel(List<String> hostIds, String nodeLabel) {
        List<ClusterHostDO> list = this.lambdaQuery().in(ClusterHostDO::getId, hostIds).list();
        for (ClusterHostDO clusterHostDO : list) {
            clusterHostDO.setNodeLabel(nodeLabel);
        }
        this.updateBatchById(list);
    }
    
    @Override
    public List<ClusterHostDO> getHostListByIds(List<String> ids) {
        return this.lambdaQuery().in(ClusterHostDO::getId, ids).or().in(ClusterHostDO::getHostname, ids).list();
    }
    
    @Override
    public Result assignRack(Integer clusterId, String rack, String hostIds) {
        List<String> ids = Arrays.asList(hostIds.split(","));
        List<ClusterHostDO> list = this.lambdaQuery().in(ClusterHostDO::getId, ids).list();
        for (ClusterHostDO clusterHostDO : list) {
            clusterHostDO.setRack(rack);
        }
        this.updateBatchById(list);
        // tell rack actor
        GenerateRackPropCommand command = new GenerateRackPropCommand();
        command.setClusterId(clusterId);
        ActorRef rackActor = ActorUtils.getLocalActor(RackActor.class, "rackActor");
        rackActor.tell(command, ActorRef.noSender());
        return Result.success();
    }
    
    @Override
    public List<ClusterHostDO> getClusterHostByRack(Integer clusterId, String rack) {
        return this.list(new QueryWrapper<ClusterHostDO>()
                .eq(Constants.CLUSTER_ID, clusterId)
                .eq(Constants.RACK, rack));
    }
}
