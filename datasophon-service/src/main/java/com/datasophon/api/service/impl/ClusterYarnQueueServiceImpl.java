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
import com.datasophon.api.master.handler.service.ServiceConfigureHandler;
import com.datasophon.api.service.ClusterServiceRoleInstanceService;
import com.datasophon.api.service.ClusterYarnQueueService;
import com.datasophon.common.Constants;
import com.datasophon.common.command.ExecuteCmdCommand;
import com.datasophon.common.model.Generators;
import com.datasophon.common.model.ServiceConfig;
import com.datasophon.common.model.ServiceRoleInfo;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceEntity;
import com.datasophon.dao.entity.ClusterYarnQueue;
import com.datasophon.dao.mapper.ClusterYarnQueueMapper;

import org.apache.commons.lang3.StringUtils;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import akka.actor.ActorSelection;
import akka.pattern.Patterns;
import akka.util.Timeout;
import cn.hutool.core.bean.BeanUtil;

@Service("clusterYarnQueueService")
public class ClusterYarnQueueServiceImpl extends ServiceImpl<ClusterYarnQueueMapper, ClusterYarnQueue>
        implements
            ClusterYarnQueueService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClusterYarnQueueServiceImpl.class);
    
    @Autowired
    private ClusterServiceRoleInstanceService roleInstanceService;
    
    @Override
    public Result listByPage(Integer clusterId, Integer page, Integer pageSize) {
        Integer offset = (page - 1) * pageSize;
        List<ClusterYarnQueue> list = this.list(new QueryWrapper<ClusterYarnQueue>()
                .eq(Constants.CLUSTER_ID, clusterId)
                .orderByDesc(Constants.CREATE_TIME)
                .last("limit " + offset + "," + pageSize));
        int count = this.count(new QueryWrapper<ClusterYarnQueue>()
                .eq(Constants.CLUSTER_ID, clusterId));
        for (ClusterYarnQueue clusterYarnQueue : list) {
            String minResources = clusterYarnQueue.getMinCore() + "Core," + clusterYarnQueue.getMinMem() + "GB";
            String maxResources = clusterYarnQueue.getMaxCore() + "Core," + clusterYarnQueue.getMaxMem() + "GB";
            clusterYarnQueue.setMinResources(minResources);
            clusterYarnQueue.setMaxResources(maxResources);
        }
        return Result.success(list).put(Constants.TOTAL, count);
    }
    
    @Override
    public Result refreshQueues(Integer clusterId) throws Exception {
        List<ClusterYarnQueue> list = this.list(new QueryWrapper<ClusterYarnQueue>()
                .eq(Constants.CLUSTER_ID, clusterId));
        // 查询resourcemanager节点
        List<ClusterServiceRoleInstanceEntity> roleList =
                roleInstanceService.getServiceRoleInstanceListByClusterIdAndRoleName(clusterId, "ResourceManager");
        
        // 构建configfilemap
        HashMap<Generators, List<ServiceConfig>> configFileMap = new HashMap<>();
        Generators generators = new Generators();
        generators.setFilename("fair-scheduler.xml");
        generators.setOutputDirectory("etc/hadoop");
        generators.setConfigFormat("custom");
        generators.setTemplateName("fair-scheduler.ftl");
        
        ArrayList<ServiceConfig> serviceConfigs = new ArrayList<>();
        ServiceConfig config = new ServiceConfig();
        ArrayList<JSONObject> queueList = new ArrayList<>();
        for (ClusterYarnQueue clusterYarnQueue : list) {
            JSONObject queue = new JSONObject();
            Integer minMem = clusterYarnQueue.getMinMem() * 1024;
            Integer maxMem = clusterYarnQueue.getMaxMem() * 1024;
            clusterYarnQueue.setMinResources(minMem + "mb," + clusterYarnQueue.getMinCore() + "vcores");
            clusterYarnQueue.setMaxResources(maxMem + "mb," + clusterYarnQueue.getMaxCore() + "vcores");
            BeanUtil.copyProperties(clusterYarnQueue, queue, false);
            queueList.add(queue);
        }
        config.setName("queueList");
        config.setValue(queueList);
        config.setConfigType("map");
        config.setRequired(true);
        serviceConfigs.add(config);
        
        configFileMap.put(generators, serviceConfigs);
        String hostname = "";
        for (ClusterServiceRoleInstanceEntity roleInstanceEntity : roleList) {
            // 调用指令刷新yarn队列配置
            ServiceRoleInfo serviceRoleInfo = new ServiceRoleInfo();
            serviceRoleInfo.setName("ResourceManager");
            serviceRoleInfo.setParentName("YARN");
            serviceRoleInfo.setConfigFileMap(configFileMap);
            serviceRoleInfo.setDecompressPackageName("hadoop");
            serviceRoleInfo.setHostname(roleInstanceEntity.getHostname());
            ServiceConfigureHandler configureHandler = new ServiceConfigureHandler();
            ExecResult execResult = configureHandler.handlerRequest(serviceRoleInfo);
            if (!execResult.getExecResult()) {
                return Result.error(Status.FAILED_REFRESH_THE_QUEUE_TO_YARN.getMsg());
            }
            if (StringUtils.isBlank(hostname)) {
                hostname = roleInstanceEntity.getHostname();
            }
        }
        ActorSelection execCmdActor = ActorUtils.actorSystem
                .actorSelection("akka.tcp://datasophon@" + hostname + ":2552/user/worker/executeCmdActor");
        ExecuteCmdCommand command = new ExecuteCmdCommand();
        Timeout timeout = new Timeout(Duration.create(180, TimeUnit.SECONDS));
        ArrayList<String> commands = new ArrayList<>();
        commands.add(Constants.INSTALL_PATH + "/hadoop/bin/yarn");
        commands.add("rmadmin");
        commands.add("-refreshQueues");
        command.setCommands(commands);
        Future<Object> execFuture = Patterns.ask(execCmdActor, command, timeout);
        ExecResult execResult = (ExecResult) Await.result(execFuture, timeout.duration());
        if (execResult.getExecResult()) {
            logger.info("yarn dfsadmin -refreshQueues success at {}", hostname);
        } else {
            logger.info(execResult.getExecOut());
            return Result.error(Status.FAILED_REFRESH_THE_QUEUE_TO_YARN.getMsg());
        }
        return Result.success();
    }
}
