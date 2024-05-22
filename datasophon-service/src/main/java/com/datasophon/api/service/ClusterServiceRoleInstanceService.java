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

import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceEntity;
import com.datasophon.dao.enums.ServiceRoleState;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 集群服务角色实例表
 *
 * @author gaodayu
 * @email gaodayu2022@163.com
 * @date 2022-04-24 16:25:17
 */
public interface ClusterServiceRoleInstanceService extends IService<ClusterServiceRoleInstanceEntity> {
    
    List<ClusterServiceRoleInstanceEntity> listStoppedServiceRoleListByHostnameAndClusterId(String hostname,
                                                                                            Integer clusterId);
    
    List<ClusterServiceRoleInstanceEntity> getServiceRoleListByHostnameAndClusterId(String hostname, Integer clusterId);
    
    List<ClusterServiceRoleInstanceEntity> getServiceRoleInstanceListByServiceIdAndRoleState(Integer id,
                                                                                             ServiceRoleState stop);
    
    ClusterServiceRoleInstanceEntity getOneServiceRole(String serviceRoleName, String hostname, Integer clusterId);
    
    Result listAll(Integer serviceInstanceId, String hostname, Integer serviceRoleState, String serviceRoleName,
                   Integer roleGroupId, Integer page, Integer pageSize);
    
    Result getLog(Integer serviceRoleInstanceId) throws Exception;
    
    List<ClusterServiceRoleInstanceEntity> getServiceRoleInstanceListByServiceId(int id);
    
    List<ClusterServiceRoleInstanceEntity> getServiceRoleInstanceListByClusterId(int clusterId);
    
    Result deleteServiceRole(List<String> idList);
    
    List<ClusterServiceRoleInstanceEntity> getServiceRoleInstanceListByClusterIdAndRoleName(Integer clusterId,
                                                                                            String roleName);
    
    List<ClusterServiceRoleInstanceEntity> getRunningServiceRoleInstanceListByServiceId(Integer serviceInstanceId);
    
    Result restartObsoleteService(Integer roleGroupId);
    
    Result decommissionNode(String serviceRoleInstanceIds, String serviceName) throws Exception;
    
    void updateToNeedRestart(Integer roleGroupId);
    
    void updateToNeedRestartByHost(String hostName);
    
    List<ClusterServiceRoleInstanceEntity> getObsoleteService(Integer id);
    
    List<ClusterServiceRoleInstanceEntity> getStoppedRoleInstanceOnHost(Integer clusterId, String hostname,
                                                                        ServiceRoleState state);
    
    void reomveRoleInstance(Integer serviceInstanceId);
    
    ClusterServiceRoleInstanceEntity getKAdminRoleIns(Integer clusterId);
    
    List<ClusterServiceRoleInstanceEntity> listServiceRoleByName(String alertManager);
    
    ClusterServiceRoleInstanceEntity getServiceRoleInsByHostAndName(String hostName, String serviceRoleName);
    
    List<ClusterServiceRoleInstanceEntity> listRoleIns(String hostname, String serviceName);
    
}
