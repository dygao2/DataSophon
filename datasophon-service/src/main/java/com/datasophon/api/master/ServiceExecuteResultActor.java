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

import com.datasophon.api.utils.ProcessUtils;
import com.datasophon.common.command.SubmitActiveTaskNodeCommand;
import com.datasophon.common.enums.ServiceExecuteState;
import com.datasophon.common.enums.ServiceRoleType;
import com.datasophon.common.model.DAGGraph;
import com.datasophon.common.model.ServiceExecuteResultMessage;
import com.datasophon.common.model.ServiceNode;
import com.datasophon.common.model.ServiceRoleInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class ServiceExecuteResultActor extends UntypedActor {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceExecuteResultActor.class);
    
    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof ServiceExecuteResultMessage) {
            ServiceExecuteResultMessage result = (ServiceExecuteResultMessage) message;
            
            DAGGraph<String, ServiceNode, String> dag = result.getDag();
            Map<String, ServiceExecuteState> activeTaskList = result.getActiveTaskList();
            Map<String, String> errorTaskList = result.getErrorTaskList();
            Map<String, String> readyToSubmitTaskList = result.getReadyToSubmitTaskList();
            Map<String, String> completeTaskList = result.getCompleteTaskList();
            ActorRef submitTaskNodeActor = ActorUtils.getLocalActor(SubmitTaskNodeActor.class,
                    ActorUtils.getActorRefName(SubmitTaskNodeActor.class));
            String node = result.getServiceName();
            ServiceNode serviceNode = dag.getNode(node);
            if (result.getServiceRoleType().equals(ServiceRoleType.MASTER)) {
                if (result.getServiceExecuteState().equals(ServiceExecuteState.ERROR)) {
                    // move to error list
                    errorTaskList.put(node, "");
                    activeTaskList.remove(node);
                    readyToSubmitTaskList.remove(node);
                    completeTaskList.put(node, "");
                    // cancel all next node
                    logger.info("{} master roles failed , cancel all next node by commandId {}", node,
                            serviceNode.getCommandId());
                    List<String> commandIds = new ArrayList<String>();
                    commandIds.add(serviceNode.getCommandId());
                    listCancelCommand(dag, node, commandIds);
                    ProcessUtils.updateCommandStateToFailed(commandIds);
                } else if (result.getServiceExecuteState().equals(ServiceExecuteState.SUCCESS)) {
                    // submit worker node
                    ServiceNode serviceNode2 = dag.getNode(node);
                    List<ServiceRoleInfo> elseRoles = serviceNode2.getElseRoles();
                    if (!elseRoles.isEmpty()) {
                        logger.info("start to submit worker/client roles");
                        for (ServiceRoleInfo elseRole : serviceNode2.getElseRoles()) {
                            ActorRef serviceActor = ActorUtils.getLocalActor(WorkerServiceActor.class,
                                    result.getClusterCode() + "-serviceActor-" + node + "-" + elseRole.getHostname());
                            ProcessUtils.buildExecuteServiceRoleCommand(
                                    result.getClusterId(),
                                    result.getCommandType(),
                                    result.getClusterCode(),
                                    dag,
                                    activeTaskList,
                                    errorTaskList,
                                    readyToSubmitTaskList,
                                    completeTaskList,
                                    node,
                                    serviceNode2.getElseRoles(),
                                    elseRole,
                                    serviceActor,
                                    ServiceRoleType.WORKER);
                        }
                        
                    } else {
                        activeTaskList.remove(node);
                        readyToSubmitTaskList.remove(node);
                    }
                    logger.info("start to submit next node");
                    tellToSubmitActiveTaskNode(result, dag, activeTaskList, errorTaskList, readyToSubmitTaskList,
                            completeTaskList, submitTaskNodeActor, node);
                }
            }
        } else {
            unhandled(message);
        }
    }
    
    public void listCancelCommand(DAGGraph<String, ServiceNode, String> dag, String node, List<String> commandIds) {
        if (dag.getSubsequentNodes(node).size() == 0) {
            return;
        }
        Set<String> subsequentNodes = dag.getSubsequentNodes(node);
        for (String subsequentNode : subsequentNodes) {
            commandIds.add(dag.getNode(subsequentNode).getCommandId());
            listCancelCommand(dag, subsequentNode, commandIds);
        }
    }
    
    private void tellToSubmitActiveTaskNode(ServiceExecuteResultMessage result,
                                            DAGGraph<String, ServiceNode, String> dag,
                                            Map<String, ServiceExecuteState> activeTaskList,
                                            Map<String, String> errorTaskList,
                                            Map<String, String> readyToSubmitTaskList,
                                            Map<String, String> completeTaskList,
                                            ActorRef submitTaskNodeActor,
                                            String node) {
        Set<String> subsequentNodes = dag.getSubsequentNodes(node);
        logger.info("{}'s subsequent nodes is {}", node, subsequentNodes.toString());
        for (String subsequentNode : subsequentNodes) {
            readyToSubmitTaskList.put(subsequentNode, "");
        }
        SubmitActiveTaskNodeCommand submitActiveTaskNodeCommand = new SubmitActiveTaskNodeCommand();
        submitActiveTaskNodeCommand.setCommandType(result.getCommandType());
        submitActiveTaskNodeCommand.setDag(dag);
        submitActiveTaskNodeCommand.setClusterId(result.getClusterId());
        submitActiveTaskNodeCommand.setActiveTaskList(activeTaskList);
        submitActiveTaskNodeCommand.setErrorTaskList(errorTaskList);
        submitActiveTaskNodeCommand.setReadyToSubmitTaskList(readyToSubmitTaskList);
        submitActiveTaskNodeCommand.setCompleteTaskList(completeTaskList);
        
        submitActiveTaskNodeCommand.setClusterCode(result.getClusterCode());
        
        submitTaskNodeActor.tell(submitActiveTaskNodeCommand, getSelf());
    }
    
}
