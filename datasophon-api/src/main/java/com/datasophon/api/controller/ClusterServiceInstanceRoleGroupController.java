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

import com.datasophon.api.service.ClusterServiceInstanceRoleGroupService;
import com.datasophon.common.Constants;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterServiceInstanceRoleGroup;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@RestController
@RequestMapping("cluster/service/instance/role/group")
public class ClusterServiceInstanceRoleGroupController {
    
    @Autowired
    private ClusterServiceInstanceRoleGroupService clusterServiceInstanceRoleGroupService;
    
    /**
     * 列表
     */
    @RequestMapping("/list")
    public Result list(Integer serviceInstanceId) {
        List<ClusterServiceInstanceRoleGroup> list =
                clusterServiceInstanceRoleGroupService.list(new QueryWrapper<ClusterServiceInstanceRoleGroup>()
                        .eq(Constants.SERVICE_INSTANCE_ID, serviceInstanceId));
        return Result.success(list);
    }
    
    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public Result info(@PathVariable("id") Integer id) {
        ClusterServiceInstanceRoleGroup clusterServiceInstanceRoleGroup =
                clusterServiceInstanceRoleGroupService.getById(id);
        
        return Result.success().put("clusterServiceInstanceRoleGroup", clusterServiceInstanceRoleGroup);
    }
    
    /**
     * 保存
     */
    @RequestMapping("/save")
    public Result save(Integer serviceInstanceId, Integer roleGroupId, String roleGroupName) {
        clusterServiceInstanceRoleGroupService.saveRoleGroup(serviceInstanceId, roleGroupId, roleGroupName);
        return Result.success();
    }
    
    /**
     * 分配角色组
     */
    @RequestMapping("/bind")
    public Result bind(String roleInstanceIds, Integer roleGroupId) {
        return clusterServiceInstanceRoleGroupService.bind(roleInstanceIds, roleGroupId);
    }
    
    /**
     * 修改
     */
    @RequestMapping("/rename")
    public Result update(Integer roleGroupId, String roleGroupName) {
        
        return clusterServiceInstanceRoleGroupService.rename(roleGroupId, roleGroupName);
        
    }
    
    /**
     * 删除
     */
    @RequestMapping("/delete")
    public Result delete(Integer roleGroupId) {
        // clusterServiceInstanceRoleGroupService.removeByIds(Arrays.asList(ids));
        
        return clusterServiceInstanceRoleGroupService.deleteRoleGroup(roleGroupId);
    }
    
}
