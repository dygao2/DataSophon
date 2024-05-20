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

package com.datasophon.common.model;

import com.datasophon.common.enums.CommandType;
import com.datasophon.common.enums.ServiceRoleType;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.Data;

@Data
public class ServiceRoleInfo implements Serializable, Comparable<ServiceRoleInfo> {
    
    private Integer id;
    
    private String name;
    
    private ServiceRoleType roleType;
    
    private String cardinality;
    
    private Integer sortNum;
    
    private ServiceRoleRunner startRunner;
    
    private ServiceRoleRunner stopRunner;
    
    private ServiceRoleRunner statusRunner;
    
    private ExternalLink externalLink;
    
    private String hostname;
    
    private String hostCommandId;
    
    private Integer clusterId;
    
    private String parentName;
    
    private String frameCode;
    
    private String packageName;
    
    private String decompressPackageName;
    
    private Map<Generators, List<ServiceConfig>> configFileMap;
    
    private String logFile;
    
    private String jmxPort;
    
    private List<Map<String, Object>> resourceStrategies;
    
    private boolean isSlave = false;
    
    private CommandType commandType;
    
    private String masterHost;
    
    private Boolean enableRangerPlugin;
    
    private Integer serviceInstanceId;
    
    private RunAs runAs;
    
    @Override
    public int compareTo(ServiceRoleInfo serviceRoleInfo) {
        if (Objects.nonNull(serviceRoleInfo.getSortNum()) && Objects.nonNull(this.getSortNum())) {
            return this.sortNum - serviceRoleInfo.getSortNum();
        }
        return 0;
    }
}
