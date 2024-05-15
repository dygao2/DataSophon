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

package com.datasophon.api.master.handler.service;

import com.datasophon.api.load.GlobalVariables;
import com.datasophon.api.master.ActorUtils;
import com.datasophon.common.Constants;
import com.datasophon.common.cache.CacheUtils;
import com.datasophon.common.command.ServiceRoleOperateCommand;
import com.datasophon.common.enums.ServiceRoleType;
import com.datasophon.common.model.ServiceRoleInfo;
import com.datasophon.common.utils.ExecResult;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorSelection;
import akka.pattern.Patterns;
import akka.util.Timeout;

public class ServiceStartHandler extends ServiceHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceStartHandler.class);
    
    @Override
    public ExecResult handlerRequest(ServiceRoleInfo serviceRoleInfo) throws Exception {
        logger.info("start to start service {} in {}", serviceRoleInfo.getName(), serviceRoleInfo.getHostname());
        // 启动
        Map<String, String> globalVariables = GlobalVariables.get(serviceRoleInfo.getClusterId());
        ServiceRoleOperateCommand serviceRoleOperateCommand = new ServiceRoleOperateCommand();
        serviceRoleOperateCommand.setServiceName(serviceRoleInfo.getParentName());
        serviceRoleOperateCommand.setServiceRoleName(serviceRoleInfo.getName());
        serviceRoleOperateCommand.setStartRunner(serviceRoleInfo.getStartRunner());
        serviceRoleOperateCommand.setDecompressPackageName(serviceRoleInfo.getDecompressPackageName());
        serviceRoleOperateCommand.setStatusRunner(serviceRoleInfo.getStatusRunner());
        serviceRoleOperateCommand.setSlave(serviceRoleInfo.isSlave());
        serviceRoleOperateCommand.setCommandType(serviceRoleInfo.getCommandType());
        serviceRoleOperateCommand.setMasterHost(serviceRoleInfo.getMasterHost());
        serviceRoleOperateCommand.setManagerHost(CacheUtils.getString(Constants.HOSTNAME));
        
        logger.info("service master host is {}", serviceRoleInfo.getMasterHost());
        
        serviceRoleOperateCommand.setEnableRangerPlugin(serviceRoleInfo.getEnableRangerPlugin());
        serviceRoleOperateCommand.setRunAs(serviceRoleInfo.getRunAs());
        Boolean enableKerberos =
                Boolean.parseBoolean(globalVariables.get("${enable" + serviceRoleInfo.getParentName() + "Kerberos}"));
        logger.info("{} enable kerberos is {}", serviceRoleInfo.getParentName(), enableKerberos);
        serviceRoleOperateCommand.setEnableKerberos(enableKerberos);
        if (serviceRoleInfo.getRoleType() == ServiceRoleType.CLIENT) {
            
            if (serviceRoleInfo.getName().equals("FlinkClient") && enableKerberos) {
                logger.info("when serviceRoleInfo name is FlinkClient ,start to startActor!");
                ActorSelection startActors = ActorUtils.actorSystem.actorSelection(
                        "akka.tcp://datasophon@" + serviceRoleInfo.getHostname()
                                + ":2552/user/worker/startServiceActor");
                Timeout timeouts = new Timeout(Duration.create(180, TimeUnit.SECONDS));
                Await.result(Patterns.ask(startActors, serviceRoleOperateCommand, timeouts), timeouts.duration());
            }
            
            ExecResult execResult = new ExecResult();
            execResult.setExecResult(true);
            if (Objects.nonNull(getNext())) {
                return getNext().handlerRequest(serviceRoleInfo);
            }
            return execResult;
        }
        ActorSelection startActor = ActorUtils.actorSystem.actorSelection(
                "akka.tcp://datasophon@" + serviceRoleInfo.getHostname() + ":2552/user/worker/startServiceActor");
        Timeout timeout = new Timeout(Duration.create(180, TimeUnit.SECONDS));
        Future<Object> startFuture = Patterns.ask(startActor, serviceRoleOperateCommand, timeout);
        try {
            ExecResult startResult = (ExecResult) Await.result(startFuture, timeout.duration());
            if (Objects.nonNull(startResult) && startResult.getExecResult()) {
                // 角色启动成功
                if (Objects.nonNull(getNext())) {
                    return getNext().handlerRequest(serviceRoleInfo);
                }
            }
            return startResult;
        } catch (Exception e) {
            return new ExecResult();
        }
    }
}
