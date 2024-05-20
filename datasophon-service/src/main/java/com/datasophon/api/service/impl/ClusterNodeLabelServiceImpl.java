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
import com.datasophon.api.exceptions.BusinessException;
import com.datasophon.api.master.ActorUtils;
import com.datasophon.api.service.ClusterInfoService;
import com.datasophon.api.service.ClusterNodeLabelService;
import com.datasophon.api.service.ClusterServiceRoleInstanceService;
import com.datasophon.api.service.host.ClusterHostService;
import com.datasophon.api.utils.PackageUtils;
import com.datasophon.common.Constants;
import com.datasophon.common.command.ExecuteCmdCommand;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterHostDO;
import com.datasophon.dao.entity.ClusterInfoEntity;
import com.datasophon.dao.entity.ClusterNodeLabelEntity;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceEntity;
import com.datasophon.dao.mapper.ClusterNodeLabelMapper;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import akka.actor.ActorSelection;
import akka.pattern.Patterns;
import akka.util.Timeout;

@Service("clusterNodeLabelService")
@Transactional
public class ClusterNodeLabelServiceImpl extends ServiceImpl<ClusterNodeLabelMapper, ClusterNodeLabelEntity>
        implements
            ClusterNodeLabelService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClusterNodeLabelServiceImpl.class);
    
    @Autowired
    private ClusterHostService hostService;
    
    @Autowired
    private ClusterServiceRoleInstanceService roleInstanceService;
    
    @Autowired
    private ClusterInfoService clusterInfoService;
    
    @Override
    public Result saveNodeLabel(Integer clusterId, String nodeLabel) {
        if (repeatNodeLable(clusterId, nodeLabel)) {
            return Result.error(Status.REPEAT_NODE_LABEL.getMsg());
        }
        ClusterNodeLabelEntity nodeLabelEntity = new ClusterNodeLabelEntity();
        nodeLabelEntity.setClusterId(clusterId);
        nodeLabelEntity.setNodeLabel(nodeLabel);
        this.save(nodeLabelEntity);
        // refresh to yarn
        if (!refreshToYarn(clusterId, "-addToClusterNodeLabels", nodeLabel)) {
            throw new BusinessException(
                    Status.ADD_YARN_NODE_LABEL_FAILED.getMsg() + ",maybe you need to enable yarn node labels");
        }
        return Result.success();
    }
    
    private boolean refreshToYarn(Integer clusterId, String type, String nodeLabel) {
        ClusterInfoEntity clusterInfo = clusterInfoService.getById(clusterId);
        List<ClusterServiceRoleInstanceEntity> roleList =
                roleInstanceService.getServiceRoleInstanceListByClusterIdAndRoleName(clusterId, "ResourceManager");
        if (roleList.size() > 0) {
            String hostname = roleList.get(0).getHostname();
            ActorSelection execCmdActor = ActorUtils.actorSystem
                    .actorSelection("akka.tcp://datasophon@" + hostname + ":2552/user/worker/executeCmdActor");
            ExecuteCmdCommand command = new ExecuteCmdCommand();
            Timeout timeout = new Timeout(Duration.create(180, TimeUnit.SECONDS));
            ArrayList<String> commands = new ArrayList<>();
            commands.add(Constants.INSTALL_PATH + Constants.SLASH
                    + PackageUtils.getServiceDcPackageName(clusterInfo.getClusterFrame(), "YARN") + "/bin/yarn");
            commands.add("rmadmin");
            commands.add(type);
            commands.add("\"" + nodeLabel + "\"");
            command.setCommands(commands);
            Future<Object> execFuture = Patterns.ask(execCmdActor, command, timeout);
            try {
                ExecResult execResult = (ExecResult) Await.result(execFuture, timeout.duration());
                if (execResult.getExecResult()) {
                    logger.info("add yarn node label success at {}", hostname);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.info("add yarn node label failed");
            return false;
        }
        return true;
    }
    
    @Override
    public Result deleteNodeLabel(Integer nodeLabelId) {
        ClusterNodeLabelEntity nodeLabelEntity = this.getById(nodeLabelId);
        
        if (nodeLabelInUse(nodeLabelEntity.getNodeLabel())) {
            return Result.error(Status.NODE_LABEL_IS_USING.getMsg());
        }
        this.removeById(nodeLabelId);
        if (!refreshToYarn(nodeLabelEntity.getClusterId(), "-removeFromClusterNodeLabels",
                nodeLabelEntity.getNodeLabel())) {
            throw new BusinessException(Status.REMOVE_YARN_NODE_LABEL_FAILED.getMsg());
        }
        return Result.success();
    }
    
    @Override
    public Result assignNodeLabel(Integer nodeLabelId, String hostIds) {
        ClusterNodeLabelEntity nodeLabelEntity = this.getById(nodeLabelId);
        List<String> ids = Arrays.asList(hostIds.split(","));
        hostService.updateBatchNodeLabel(ids, nodeLabelEntity.getNodeLabel());
        
        List<ClusterHostDO> list = hostService.getHostListByIds(ids);
        String assignNodeLabel = list.stream().map(e -> e.getHostname() + "=" + nodeLabelEntity.getNodeLabel())
                .collect(Collectors.joining(" "));
        logger.info("assign node label {}", assignNodeLabel);
        // sync to yarn
        // refresh to yarn
        if (!refreshToYarn(nodeLabelEntity.getClusterId(), "-replaceLabelsOnNode", assignNodeLabel)) {
            throw new BusinessException(Status.ASSIGN_YARN_NODE_LABEL_FAILED.getMsg());
        }
        return Result.success();
    }
    
    @Override
    public List<ClusterNodeLabelEntity> queryClusterNodeLabel(Integer clusterId) {
        return this.list(new QueryWrapper<ClusterNodeLabelEntity>().eq(Constants.CLUSTER_ID, clusterId));
    }
    
    @Override
    public void createDefaultNodeLabel(Integer clusterId) {
        ClusterNodeLabelEntity nodeLabelEntity = new ClusterNodeLabelEntity();
        nodeLabelEntity.setNodeLabel("default");
        nodeLabelEntity.setClusterId(clusterId);
        this.save(nodeLabelEntity);
    }
    
    private boolean nodeLabelInUse(String nodeLabel) {
        List<ClusterHostDO> list = hostService.list(new QueryWrapper<ClusterHostDO>()
                .eq(Constants.NODE_LABEL, nodeLabel));
        if (list.size() > 0) {
            return true;
        }
        return false;
    }
    
    private boolean repeatNodeLable(Integer clusterId, String nodeLabel) {
        List<ClusterNodeLabelEntity> list = this.list(new QueryWrapper<ClusterNodeLabelEntity>()
                .eq(Constants.CLUSTER_ID, clusterId)
                .eq(Constants.NODE_LABEL, nodeLabel));
        if (list.size() > 0) {
            return true;
        }
        return false;
    }
}
