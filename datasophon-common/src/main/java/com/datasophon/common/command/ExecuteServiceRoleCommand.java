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

package com.datasophon.common.command;

import com.datasophon.common.enums.CommandType;
import com.datasophon.common.enums.ServiceExecuteState;
import com.datasophon.common.enums.ServiceRoleType;
import com.datasophon.common.model.DAGGraph;
import com.datasophon.common.model.ServiceNode;
import com.datasophon.common.model.ServiceRoleInfo;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ExecuteServiceRoleCommand {
    
    private Integer clusterId;
    
    private String clusterCode;
    
    private String serviceName;
    
    private List<ServiceRoleInfo> masterRoles;
    
    private ServiceRoleInfo workerRole;
    
    private ServiceRoleType serviceRoleType;
    
    private CommandType commandType;
    
    private DAGGraph<String, ServiceNode, String> dag;
    
    private Map<String, String> errorTaskList;
    private Map<String, ServiceExecuteState> activeTaskList;
    private Map<String, String> readyToSubmitTaskList;
    private Map<String, String> completeTaskList;
    
    public ExecuteServiceRoleCommand(Integer clusterId, String serviceName, List<ServiceRoleInfo> serviceRoles) {
        this.clusterId = clusterId;
        this.serviceName = serviceName;
        this.masterRoles = serviceRoles;
    }
    
    public ExecuteServiceRoleCommand() {
    }
}
