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

package com.datasophon.api.service.impl;

import com.datasophon.api.load.GlobalVariables;
import com.datasophon.api.master.ActorUtils;
import com.datasophon.api.service.ClusterInfoService;
import com.datasophon.api.service.ClusterServiceCommandHostCommandService;
import com.datasophon.api.service.FrameServiceRoleService;
import com.datasophon.api.service.FrameServiceService;
import com.datasophon.common.Constants;
import com.datasophon.common.command.GetLogCommand;
import com.datasophon.common.utils.CollectionUtils;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.PlaceholderUtils;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterInfoEntity;
import com.datasophon.dao.entity.ClusterServiceCommandHostCommandEntity;
import com.datasophon.dao.entity.FrameServiceEntity;
import com.datasophon.dao.entity.FrameServiceRoleEntity;
import com.datasophon.dao.enums.CommandState;
import com.datasophon.dao.enums.RoleType;
import com.datasophon.dao.mapper.ClusterServiceCommandHostCommandMapper;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import akka.actor.ActorSelection;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

@Service("clusterServiceCommandHostCommandService")
public class ClusterServiceCommandHostCommandServiceImpl
        extends ServiceImpl<
                ClusterServiceCommandHostCommandMapper, ClusterServiceCommandHostCommandEntity>
        implements ClusterServiceCommandHostCommandService {

    private static final Logger logger =
            LoggerFactory.getLogger(ClusterServiceCommandHostCommandServiceImpl.class);

    @Autowired ClusterServiceCommandHostCommandMapper hostCommandMapper;

    @Autowired FrameServiceRoleService frameServiceRoleService;

    @Autowired FrameServiceService frameService;

    @Autowired ClusterInfoService clusterInfoService;

    @Override
    public Result getHostCommandList(
            String hostname, String commandHostId, Integer page, Integer pageSize) {
        Integer offset = (page - 1) * pageSize;

        List<ClusterServiceCommandHostCommandEntity> list =
                this.lambdaQuery()
                        .eq(ClusterServiceCommandHostCommandEntity::getCommandHostId, commandHostId)
                        .orderByDesc(ClusterServiceCommandHostCommandEntity::getCreateTime)
                        .last("limit " + offset + "," + pageSize)
                        .list();
        if (CollectionUtils.isNotEmpty(list)) {
            for (ClusterServiceCommandHostCommandEntity hostCommandEntity : list) {
                hostCommandEntity.setCommandStateCode(
                        hostCommandEntity.getCommandState().getValue());
            }
        }

        Integer total =
                this.lambdaQuery()
                        .eq(ClusterServiceCommandHostCommandEntity::getCommandHostId, commandHostId)
                        .count();
        return Result.success(list).put(Constants.TOTAL, total);
    }

    @Override
    public List<ClusterServiceCommandHostCommandEntity> getHostCommandListByCommandId(
            String commandId) {
        return this.lambdaQuery()
                .eq(ClusterServiceCommandHostCommandEntity::getCommandId, commandId)
                .list();
    }

    @Override
    public ClusterServiceCommandHostCommandEntity getByHostCommandId(String hostCommandId) {
        return this.lambdaQuery()
                .eq(ClusterServiceCommandHostCommandEntity::getHostCommandId, hostCommandId)
                .one();
    }

    @Override
    public void updateByHostCommandId(ClusterServiceCommandHostCommandEntity hostCommand) {
        this.lambdaUpdate()
                .eq(
                        ClusterServiceCommandHostCommandEntity::getHostCommandId,
                        hostCommand.getHostCommandId())
                .update(hostCommand);
    }

    @Override
    public Integer getHostCommandSizeByHostnameAndCommandHostId(
            String hostname, String commandHostId) {
        return this.lambdaQuery()
                .eq(ClusterServiceCommandHostCommandEntity::getHostname, hostname)
                .eq(ClusterServiceCommandHostCommandEntity::getCommandHostId, commandHostId)
                .count();
    }

    @Override
    public Integer getHostCommandTotalProgressByHostnameAndCommandHostId(
            String hostname, String commandHostId) {
        return hostCommandMapper.getHostCommandTotalProgressByHostnameAndCommandHostId(
                hostname, commandHostId);
    }

    @Override
    public Result getHostCommandLog(Integer clusterId, String hostCommandId) throws Exception {
        ClusterInfoEntity clusterInfo = clusterInfoService.getById(clusterId);

        ClusterServiceCommandHostCommandEntity hostCommand =
                this.lambdaQuery()
                        .eq(ClusterServiceCommandHostCommandEntity::getHostCommandId, hostCommandId)
                        .one();
        FrameServiceRoleEntity serviceRole =
                frameServiceRoleService.getServiceRoleByFrameCodeAndServiceRoleName(
                        clusterInfo.getClusterFrame(), hostCommand.getServiceRoleName());
        Map<String, String> globalVariables = GlobalVariables.get(clusterId);
        if (serviceRole.getServiceRoleType() == RoleType.CLIENT) {
            return Result.success("client does not have any log");
        }
        FrameServiceEntity frameServiceEntity = frameService.getById(serviceRole.getServiceId());
        String logFile = serviceRole.getLogFile();
        if (StringUtils.isNotBlank(logFile)) {
            logFile =
                    PlaceholderUtils.replacePlaceholders(
                            logFile, globalVariables, Constants.REGEX_VARIABLE);
            logger.info("logFile is {}", logFile);
        }
        GetLogCommand command = new GetLogCommand();
        command.setLogFile(logFile);
        command.setDecompressPackageName(frameServiceEntity.getDecompressPackageName());
        logger.info(
                "start to get {} log from {}",
                serviceRole.getServiceRoleName(),
                hostCommand.getHostname());
        ActorSelection configActor =
                ActorUtils.actorSystem.actorSelection(
                        "akka.tcp://datasophon@"
                                + hostCommand.getHostname()
                                + ":2552/user/worker/logActor");
        Timeout timeout = new Timeout(Duration.create(60, TimeUnit.SECONDS));
        Future<Object> logFuture = Patterns.ask(configActor, command, timeout);
        ExecResult logResult = (ExecResult) Await.result(logFuture, timeout.duration());
        if (Objects.nonNull(logResult) && logResult.getExecResult()) {
            return Result.success(logResult.getExecOut());
        }
        return Result.success();
    }

    @Override
    public List<ClusterServiceCommandHostCommandEntity> findFailedHostCommand(
            String hostname, String commandHostId) {
        return this.lambdaQuery()
                .eq(ClusterServiceCommandHostCommandEntity::getHostname, hostname)
                .eq(ClusterServiceCommandHostCommandEntity::getCommandHostId, commandHostId)
                .eq(ClusterServiceCommandHostCommandEntity::getCommandState, CommandState.FAILED)
                .list();
    }

    @Override
    public List<ClusterServiceCommandHostCommandEntity> findCanceledHostCommand(
            String hostname, String commandHostId) {
        return this.lambdaQuery()
                .eq(ClusterServiceCommandHostCommandEntity::getHostname, hostname)
                .eq(ClusterServiceCommandHostCommandEntity::getCommandHostId, commandHostId)
                .eq(ClusterServiceCommandHostCommandEntity::getCommandState, CommandState.CANCEL)
                .list();
    }
}
