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
import com.datasophon.common.model.Generators;
import com.datasophon.common.model.RunAs;
import com.datasophon.common.model.ServiceConfig;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class InstallServiceRoleCommand extends BaseCommand implements Serializable {
    
    private static final long serialVersionUID = -8610024764701745463L;
    
    private Map<Generators, List<ServiceConfig>> configFileMap;
    
    private Long deliveryId;
    
    private Integer normalSize;
    
    private String packageMd5;
    
    private String decompressPackageName;
    
    private RunAs runAs;
    
    private ServiceRoleType serviceRoleType;
    
    private List<Map<String, Object>> resourceStrategies;
    
}
