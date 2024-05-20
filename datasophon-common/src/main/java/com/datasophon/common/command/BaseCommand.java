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

import com.datasophon.common.enums.ServiceRoleType;
import com.datasophon.common.model.ServiceRoleRunner;

import java.io.Serializable;

import lombok.Data;

@Data
public class BaseCommand implements Serializable {
    
    private static final long serialVersionUID = -1495156573211152639L;
    
    private String frameCode;
    
    private String serviceName;
    
    private String serviceRoleName;
    
    private ServiceRoleType serviceRoleType;
    
    private String hostCommandId;
    
    private String packageName;
    
    private Integer clusterId;
    
    private ServiceRoleRunner startRunner;
    
    private ServiceRoleRunner stopRunner;
    
    private ServiceRoleRunner statusRunner;
    
    private ServiceRoleRunner restartRunner;
}
