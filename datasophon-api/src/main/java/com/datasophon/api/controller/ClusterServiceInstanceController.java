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

import com.datasophon.api.service.ClusterServiceInstanceService;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterServiceInstanceEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("cluster/service/instance")
public class ClusterServiceInstanceController {
    
    @Autowired
    private ClusterServiceInstanceService clusterServiceInstanceService;
    
    /**
     * 列表
     */
    @RequestMapping("/list")
    public Result list(Integer clusterId) {
        return Result.success(clusterServiceInstanceService.listAll(clusterId));
    }
    
    /**
     * 获取服务角色类型列表
     */
    @RequestMapping("/getServiceRoleType")
    public Result getServiceRoleType(Integer serviceInstanceId) {
        return clusterServiceInstanceService.getServiceRoleType(serviceInstanceId);
    }
    
    /**
     * 获取服务角色类型列表
     */
    @RequestMapping("/configVersionCompare")
    public Result configVersionCompare(Integer serviceInstanceId, Integer roleGroupId) {
        return clusterServiceInstanceService.configVersionCompare(serviceInstanceId, roleGroupId);
    }
    
    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public Result info(@PathVariable("id") Integer id) {
        ClusterServiceInstanceEntity clusterServiceInstance = clusterServiceInstanceService.getById(id);
        
        return Result.success().put("clusterServiceInstance", clusterServiceInstance);
    }
    
    /**
     * 信息
     */
    @RequestMapping("/downloadClientConfig")
    public Result downloadClientConfig(Integer clusterId, String serviceName) {
        
        return clusterServiceInstanceService.downloadClientConfig(clusterId, serviceName);
    }
    
    /**
     * 保存
     */
    @RequestMapping("/save")
    public Result save(@RequestBody ClusterServiceInstanceEntity clusterServiceInstance) {
        clusterServiceInstanceService.save(clusterServiceInstance);
        
        return Result.success();
    }
    
    /**
     * 修改
     */
    @RequestMapping("/update")
    public Result update(@RequestBody ClusterServiceInstanceEntity clusterServiceInstance) {
        clusterServiceInstanceService.updateById(clusterServiceInstance);
        
        return Result.success();
    }
    
    /**
     * 删除
     */
    @RequestMapping("/delete")
    public Result delete(Integer serviceInstanceId) {
        return clusterServiceInstanceService.delServiceInstance(serviceInstanceId);
    }
    
}
