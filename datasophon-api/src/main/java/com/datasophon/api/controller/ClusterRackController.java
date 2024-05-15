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

import com.datasophon.api.service.ClusterRackService;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterRack;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("cluster/rack")
public class ClusterRackController {
    
    @Autowired
    private ClusterRackService clusterRackService;
    
    /**
     * 列表
     */
    @RequestMapping("/list")
    public Result list(Integer clusterId) {
        List<ClusterRack> list = clusterRackService.queryClusterRack(clusterId);
        return Result.success(list);
    }
    
    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public Result info(@PathVariable("id") Integer id) {
        ClusterRack clusterRack = clusterRackService.getById(id);
        
        return Result.success().put("clusterRack", clusterRack);
    }
    
    /**
     * 保存
     */
    @RequestMapping("/save")
    public Result save(Integer clusterId, String rack) {
        clusterRackService.saveRack(clusterId, rack);
        return Result.success();
    }
    
    /**
     * 删除
     */
    @RequestMapping("/delete")
    public Result delete(Integer clusterId, Integer rackId) {
        return clusterRackService.deleteRack(rackId);
    }
    
}
