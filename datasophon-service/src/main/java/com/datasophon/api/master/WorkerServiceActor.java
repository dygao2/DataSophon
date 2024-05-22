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

import com.datasophon.api.master.handler.service.ServiceHandler;
import com.datasophon.api.master.handler.service.ServiceStopHandler;
import com.datasophon.api.service.ClusterServiceRoleGroupConfigService;
import com.datasophon.api.service.ClusterServiceRoleInstanceService;
import com.datasophon.api.utils.ProcessUtils;
import com.datasophon.api.utils.SpringTool;
import com.datasophon.common.cache.CacheUtils;
import com.datasophon.common.command.ExecuteServiceRoleCommand;
import com.datasophon.common.enums.CommandType;
import com.datasophon.common.model.Generators;
import com.datasophon.common.model.ServiceConfig;
import com.datasophon.common.model.ServiceRoleInfo;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.dao.entity.ClusterServiceRoleGroupConfig;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceEntity;
import com.datasophon.dao.enums.NeedRestart;
import com.datasophon.dao.enums.ServiceRoleState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

public class WorkerServiceActor extends UntypedActor {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkerServiceActor.class);
    
    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof ExecuteServiceRoleCommand) {
            ExecuteServiceRoleCommand executeServiceRoleCommand = (ExecuteServiceRoleCommand) message;
            
            ClusterServiceRoleGroupConfigService roleGroupConfigService =
                    SpringTool.getApplicationContext().getBean(ClusterServiceRoleGroupConfigService.class);
            ClusterServiceRoleInstanceService roleInstanceService =
                    SpringTool.getApplicationContext().getBean(ClusterServiceRoleInstanceService.class);
            
            ServiceRoleInfo serviceRoleInfo = executeServiceRoleCommand.getWorkerRole();
            ExecResult execResult = new ExecResult();
            Integer serviceInstanceId = serviceRoleInfo.getServiceInstanceId();
            ClusterServiceRoleInstanceEntity serviceRoleInstance = roleInstanceService.getOneServiceRole(
                    serviceRoleInfo.getName(),
                    serviceRoleInfo.getHostname(),
                    serviceRoleInfo.getClusterId());
            Map<Generators, List<ServiceConfig>> configFileMap = new HashMap<>();
            boolean needReConfig = false;
            if (executeServiceRoleCommand.getCommandType() == CommandType.INSTALL_SERVICE) {
                Integer roleGroupId = (Integer) CacheUtils.get("UseRoleGroup_" + serviceInstanceId);
                ClusterServiceRoleGroupConfig config = roleGroupConfigService.getConfigByRoleGroupId(roleGroupId);
                ProcessUtils.generateConfigFileMap(configFileMap, config, serviceRoleInfo.getClusterId());
            } else if (serviceRoleInstance.getNeedRestart() == NeedRestart.YES) {
                ClusterServiceRoleGroupConfig config =
                        roleGroupConfigService.getConfigByRoleGroupId(serviceRoleInstance.getRoleGroupId());
                ProcessUtils.generateConfigFileMap(configFileMap, config, serviceRoleInfo.getClusterId());
                needReConfig = true;
            }
            serviceRoleInfo.setConfigFileMap(configFileMap);
            serviceRoleInfo.setEnableRangerPlugin(false);
            switch (executeServiceRoleCommand.getCommandType()) {
                case INSTALL_SERVICE:
                    try {
                        logger.info("start to install {} int host {}", serviceRoleInfo.getName(),
                                serviceRoleInfo.getHostname());
                        execResult = ProcessUtils.startInstallService(serviceRoleInfo);
                        if (Objects.nonNull(execResult) && execResult.getExecResult()) {
                            // install success
                            ProcessUtils.saveServiceInstallInfo(serviceRoleInfo);
                            logger.info("{} install success in {}", serviceRoleInfo.getName(),
                                    serviceRoleInfo.getHostname());
                        }
                    } catch (Exception e) {
                        logger.info("{} install failed in {}", serviceRoleInfo.getName(),
                                serviceRoleInfo.getHostname());
                        logger.error(ProcessUtils.getExceptionMessage(e));
                    }
                    break;
                case START_SERVICE:
                    try {
                        logger.info("start  {} in host {}", serviceRoleInfo.getName(), serviceRoleInfo.getHostname());
                        execResult = ProcessUtils.startService(serviceRoleInfo, needReConfig);
                        if (Objects.nonNull(execResult) && execResult.getExecResult()) {
                            // 更新角色实例状态为正在运行
                            ProcessUtils.updateServiceRoleState(CommandType.START_SERVICE,
                                    serviceRoleInfo.getName(),
                                    serviceRoleInfo.getHostname(),
                                    executeServiceRoleCommand.getClusterId(),
                                    ServiceRoleState.RUNNING);
                        }
                    } catch (Exception e) {
                        logger.error(ProcessUtils.getExceptionMessage(e));
                    }
                    break;
                case STOP_SERVICE:
                    try {
                        logger.info("stop {} in host {}", serviceRoleInfo.getName(), serviceRoleInfo.getHostname());
                        ServiceHandler serviceStopHandler = new ServiceStopHandler();
                        execResult = serviceStopHandler.handlerRequest(serviceRoleInfo);
                        if (Objects.nonNull(execResult) && execResult.getExecResult()) {// 执行成功
                            // 更新角色实例状态为停止
                            ProcessUtils.updateServiceRoleState(CommandType.STOP_SERVICE,
                                    serviceRoleInfo.getName(),
                                    serviceRoleInfo.getHostname(),
                                    executeServiceRoleCommand.getClusterId(),
                                    ServiceRoleState.STOP);
                        }
                    } catch (Exception e) {
                        logger.error(ProcessUtils.getExceptionMessage(e));
                    }
                    break;
                case RESTART_SERVICE:
                    try {
                        logger.info("restart {} in host {}", serviceRoleInfo.getName(), serviceRoleInfo.getHostname());
                        execResult = ProcessUtils.restartService(serviceRoleInfo, needReConfig);
                        if (Objects.nonNull(execResult) && execResult.getExecResult()) {
                            // 更新角色实例状态为正在运行
                            ProcessUtils.updateServiceRoleState(CommandType.RESTART_SERVICE, serviceRoleInfo.getName(),
                                    serviceRoleInfo.getHostname(), executeServiceRoleCommand.getClusterId(),
                                    ServiceRoleState.RUNNING);
                        }
                    } catch (Exception e) {
                        logger.error(ProcessUtils.getExceptionMessage(e));
                    }
                    break;
                default:
                    break;
            }
            ProcessUtils.handleCommandResult(serviceRoleInfo.getHostCommandId(), execResult.getExecResult(),
                    execResult.getExecOut());
        } else {
            unhandled(message);
        }
    }
    
}
