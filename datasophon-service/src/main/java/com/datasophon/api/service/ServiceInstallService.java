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

package com.datasophon.api.service;

import com.datasophon.common.model.HostServiceRoleMapping;
import com.datasophon.common.model.ServiceConfig;
import com.datasophon.common.model.ServiceRoleHostMapping;
import com.datasophon.common.utils.Result;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

public interface ServiceInstallService {
    
    Result getServiceConfigOption(Integer clusterId, String serviceName);
    
    Result saveServiceRoleHostMapping(Integer clusterId, List<ServiceRoleHostMapping> list);
    
    Result saveServiceConfig(Integer clusterId, String serviceName, List<ServiceConfig> configJson,
                             Integer roleGroupId);
    
    Result saveHostServiceRoleMapping(Integer clusterId, List<HostServiceRoleMapping> list);
    
    Result getServiceRoleDeployOverview(Integer clusterId);
    
    Result startInstallService(Integer clusterId, List<String> commandIds);
    
    void downloadPackage(String packageName, HttpServletResponse response) throws IOException;
    
    void downloadResource(String frameCode, String serviceRoleName,
                          String resource, HttpServletResponse response) throws IOException;
    
    Result getServiceRoleHostMapping(Integer clusterId);
    
    Result checkServiceDependency(Integer clusterId, String serviceIds);
}
