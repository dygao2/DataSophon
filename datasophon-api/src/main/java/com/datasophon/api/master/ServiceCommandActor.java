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

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.datasophon.api.service.ClusterAlertQuotaService;
import com.datasophon.api.service.ClusterInfoService;
import com.datasophon.api.service.ClusterServiceCommandHostCommandService;
import com.datasophon.api.service.ClusterServiceCommandHostService;
import com.datasophon.api.service.ClusterServiceCommandService;
import com.datasophon.api.utils.SpringTool;
import com.datasophon.common.Constants;
import com.datasophon.common.command.GeneratePrometheusConfigCommand;
import com.datasophon.common.command.GenerateSRPromConfigCommand;
import com.datasophon.common.command.HdfsEcCommand;
import com.datasophon.common.model.UpdateCommandHostMessage;
import com.datasophon.dao.entity.ClusterAlertQuota;
import com.datasophon.dao.entity.ClusterInfoEntity;
import com.datasophon.dao.entity.ClusterServiceCommandEntity;
import com.datasophon.dao.entity.ClusterServiceCommandHostCommandEntity;
import com.datasophon.dao.entity.ClusterServiceCommandHostEntity;
import com.datasophon.dao.enums.ClusterState;
import com.datasophon.dao.enums.CommandState;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceCommandActor extends UntypedActor {

    private static final Logger logger = LoggerFactory.getLogger(ServiceCommandActor.class);

    private static final String STARROCKS = "starrocks";

    private static final String DORIS = "doris";

    private static final String HDFS = "hdfs";

    @Override
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
        logger.info("service command actor restart because {}", reason.getMessage());
        super.preRestart(reason, message);
    }

    @Override
    public void onReceive(Object msg) throws Throwable {
        if (msg instanceof UpdateCommandHostMessage) {
            UpdateCommandHostMessage message = (UpdateCommandHostMessage) msg;

            ClusterInfoService clusterInfoService =
                    SpringTool.getApplicationContext().getBean(ClusterInfoService.class);
            ClusterServiceCommandHostCommandService service =
                    SpringTool.getApplicationContext().getBean(ClusterServiceCommandHostCommandService.class);
            ClusterServiceCommandHostService commandHostService =
                    SpringTool.getApplicationContext().getBean(ClusterServiceCommandHostService.class);
            ClusterServiceCommandService commandService =
                    SpringTool.getApplicationContext().getBean(ClusterServiceCommandService.class);

            ClusterServiceCommandHostEntity commandHost =
                    commandHostService.getOne(new QueryWrapper<ClusterServiceCommandHostEntity>()
                            .eq(Constants.COMMAND_HOST_ID, message.getCommandHostId()));
            Integer size = service.getHostCommandSizeByHostnameAndCommandHostId(message.getHostname(),
                    message.getCommandHostId());
            Integer totalProgress = service.getHostCommandTotalProgressByHostnameAndCommandHostId(message.getHostname(),
                    message.getCommandHostId());
            Integer progress = totalProgress / size;
            commandHost.setCommandProgress(progress);

            if (progress == 100) {
                List<ClusterServiceCommandHostCommandEntity> list =
                        service.findFailedHostCommand(message.getHostname(), message.getCommandHostId());
                if (list.size() > 0) {
                    commandHost.setCommandState(CommandState.FAILED);
                } else {
                    commandHost.setCommandState(CommandState.SUCCESS);
                }
                List<ClusterServiceCommandHostCommandEntity> cancelList =
                        service.findCanceledHostCommand(message.getHostname(), message.getCommandHostId());
                if (cancelList.size() > 0) {
                    commandHost.setCommandState(CommandState.CANCEL);
                }
            }
            commandHostService.update(commandHost, new QueryWrapper<ClusterServiceCommandHostEntity>()
                    .eq(Constants.COMMAND_HOST_ID, message.getCommandHostId()));
            Integer size1 = commandHostService.getCommandHostSizeByCommandId(message.getCommandId());
            Integer totalProgress1 = commandHostService.getCommandHostTotalProgressByCommandId(message.getCommandId());
            Integer progress1 = totalProgress1 / size1;
            ClusterServiceCommandEntity command = commandService.lambdaQuery()
                    .eq(ClusterServiceCommandEntity::getCommandId, message.getCommandId()).one();
            command.setCommandProgress(progress1);
            if (progress1 == 100) {
                command.setCommandState(CommandState.SUCCESS);
                command.setEndTime(new Date());
                // update cluster state
                if (command.getCommandType() == 1) {
                    ClusterInfoEntity clusterInfo = clusterInfoService.getById(command.getClusterId());
                    if (clusterInfo.getClusterState().equals(ClusterState.NEED_CONFIG)) {
                        clusterInfo.setClusterState(ClusterState.RUNNING);
                        clusterInfoService.updateById(clusterInfo);
                    }
                    String serviceName = command.getServiceName();
                    if (HDFS.equals(serviceName.toLowerCase())) {
                        ActorRef hdfsECActor = ActorUtils.getLocalActor(HdfsECActor.class,
                                ActorUtils.getActorRefName(HdfsECActor.class));
                        HdfsEcCommand hdfsEcCommand = new HdfsEcCommand();
                        hdfsEcCommand.setServiceInstanceId(command.getServiceInstanceId());
                        hdfsECActor.tell(hdfsEcCommand, getSelf());
                        //update web ui

                    }
                    logger.info("start to generate prometheus config");
                    ActorRef prometheusActor = ActorUtils.getLocalActor(PrometheusActor.class,
                            ActorUtils.getActorRefName(PrometheusActor.class));
                    if (STARROCKS.equals(serviceName.toLowerCase()) || DORIS.equals(serviceName.toLowerCase())) {
                        GenerateSRPromConfigCommand prometheusConfigCommand = new GenerateSRPromConfigCommand();
                        prometheusConfigCommand.setServiceInstanceId(command.getServiceInstanceId());
                        prometheusConfigCommand.setClusterFrame(clusterInfo.getClusterFrame());
                        prometheusConfigCommand.setClusterId(clusterInfo.getId());
                        prometheusConfigCommand.setFilename(serviceName.toLowerCase() + ".json");
                        prometheusActor.tell(prometheusConfigCommand, getSelf());
                    } else {
                        GeneratePrometheusConfigCommand prometheusConfigCommand = new GeneratePrometheusConfigCommand();
                        prometheusConfigCommand.setServiceInstanceId(command.getServiceInstanceId());
                        prometheusConfigCommand.setClusterFrame(clusterInfo.getClusterFrame());
                        prometheusConfigCommand.setClusterId(clusterInfo.getId());
                        prometheusActor.tell(prometheusConfigCommand, getSelf());
                    }
                    ClusterAlertQuotaService alertQuotaService =
                            SpringTool.getApplicationContext().getBean(ClusterAlertQuotaService.class);
                    List<ClusterAlertQuota> list = alertQuotaService.listAlertQuotaByServiceName(serviceName);
                    List<Integer> ids = list.stream().map(e -> e.getId()).collect(Collectors.toList());
                    String alertQuotaIds = StringUtils.join(ids, ",");
                    alertQuotaService.start(clusterInfo.getId(), alertQuotaIds);
                }
                List<ClusterServiceCommandHostEntity> list =
                        commandHostService.findFailedCommandHost(message.getCommandId());
                if (list.size() > 0) {
                    command.setCommandState(CommandState.FAILED);
                    command.setEndTime(new Date());
                }

                List<ClusterServiceCommandHostEntity> cancelList =
                        commandHostService.findCanceledCommandHost(message.getCommandId());
                if (cancelList.size() > 0) {
                    command.setCommandState(CommandState.CANCEL);
                    command.setEndTime(new Date());
                }
            }
            commandService.lambdaUpdate().eq(ClusterServiceCommandEntity::getCommandId, command.getCommandId())
                    .update(command);
        }
    }

}
