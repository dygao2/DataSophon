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

import com.datasophon.api.service.AlertGroupService;
import com.datasophon.api.service.FrameServiceRoleService;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.AlertGroupEntity;
import com.datasophon.dao.entity.FrameServiceRoleEntity;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/frame/service/role")
public class FrameServiceRoleController {
    
    @Autowired
    private FrameServiceRoleService frameServiceRoleService;
    
    @Autowired
    private AlertGroupService alertGroupService;
    
    /**
     * 查询服务对应的角色列表
     */
    @RequestMapping("/getServiceRoleList")
    public Result getServiceRoleOfMaster(Integer clusterId, String serviceIds, Integer serviceRoleType) {
        return frameServiceRoleService.getServiceRoleList(clusterId, serviceIds, serviceRoleType);
    }
    
    @RequestMapping("/getNonMasterRoleList")
    public Result getNonMasterRoleList(Integer clusterId, String serviceIds) {
        return frameServiceRoleService.getNonMasterRoleList(clusterId, serviceIds);
    }
    
    @RequestMapping("/getServiceRoleByServiceName")
    public Result getServiceRoleByServiceName(Integer clusterId, Integer alertGroupId) {
        AlertGroupEntity alertGroupEntity = alertGroupService.getById(alertGroupId);
        return frameServiceRoleService.getServiceRoleByServiceName(clusterId, alertGroupEntity.getAlertGroupCategory());
    }
    
    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public Result info(@PathVariable("id") Integer id) {
        FrameServiceRoleEntity frameServiceRole = frameServiceRoleService.getById(id);
        
        return Result.success().put("frameServiceRole", frameServiceRole);
    }
    
    /**
     * 保存
     */
    @RequestMapping("/save")
    public Result save(@RequestBody FrameServiceRoleEntity frameServiceRole) {
        frameServiceRoleService.save(frameServiceRole);
        
        return Result.success();
    }
    
    /**
     * 修改
     */
    @RequestMapping("/update")
    public Result update(@RequestBody FrameServiceRoleEntity frameServiceRole) {
        frameServiceRoleService.updateById(frameServiceRole);
        
        return Result.success();
    }
    
    /**
     * 删除
     */
    @RequestMapping("/delete")
    public Result delete(@RequestBody Integer[] ids) {
        frameServiceRoleService.removeByIds(Arrays.asList(ids));
        
        return Result.success();
    }
    
}
