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

package com.datasophon.api.controller;

import com.datasophon.api.service.ClusterServiceDashboardService;
import com.datasophon.common.utils.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("cluster/service/dashboard")
public class ClusterServiceDashboardController {
    
    @Autowired
    private ClusterServiceDashboardService clusterServiceDashboardService;
    
    /**
     * 列表
     */
    @RequestMapping("/getDashboardUrl")
    public Result getDashboardUrl(Integer clusterId) {
        
        return clusterServiceDashboardService.getDashboardUrl(clusterId);
    }
    
}
