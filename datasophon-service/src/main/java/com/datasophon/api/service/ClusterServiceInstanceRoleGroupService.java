/*
 *
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
 *
 */

package com.datasophon.api.service;

import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterServiceInstanceRoleGroup;
import com.datasophon.dao.entity.ClusterServiceRoleGroupConfig;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

public interface ClusterServiceInstanceRoleGroupService
        extends
            IService<ClusterServiceInstanceRoleGroup> {
    
    ClusterServiceInstanceRoleGroup getRoleGroupByServiceInstanceId(Integer id);
    
    Result saveRoleGroup(Integer serviceInstanceId, Integer roleGroupId, String roleGroupName);
    
    Result bind(String roleInstanceIds, Integer roleGroupId);
    
    ClusterServiceRoleGroupConfig getRoleGroupConfigByServiceId(Integer id);
    
    Result rename(Integer roleGroupId, String roleGroupName);
    
    Result deleteRoleGroup(Integer roleGroupId);
    
    List<ClusterServiceInstanceRoleGroup> listRoleGroupByServiceInstanceId(
                                                                           Integer serviceInstanceId);
    
    void updateToNeedRestart(Integer roleGroupId);
}
