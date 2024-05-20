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

import com.datasophon.api.service.ClusterAlertHistoryService;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterAlertHistory;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("cluster/alert/history")
public class ClusterAlertHistoryController {
    
    @Autowired
    private ClusterAlertHistoryService clusterAlertHistoryService;
    
    /**
     * 列表
     */
    @RequestMapping("/getAlertList")
    public Result getAlertList(Integer serviceInstanceId) {
        return clusterAlertHistoryService.getAlertList(serviceInstanceId);
    }
    
    /**
     * 列表
     */
    @RequestMapping("/getAllAlertList")
    public Result getAllAlertList(Integer clusterId, Integer page, Integer pageSize) {
        return clusterAlertHistoryService.getAllAlertList(clusterId, page, pageSize);
    }
    
    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public Result info(@PathVariable("id") Integer id) {
        ClusterAlertHistory clusterAlertHistory = clusterAlertHistoryService.getById(id);
        
        return Result.success().put("clusterAlertHistory", clusterAlertHistory);
    }
    
    /**
     * 保存
     */
    @RequestMapping("/save")
    public Result save(@RequestBody String alertMessage) {
        clusterAlertHistoryService.saveAlertHistory(alertMessage);
        return Result.success();
    }
    
    /**
     * 修改
     */
    @RequestMapping("/update")
    public Result update(@RequestBody ClusterAlertHistory clusterAlertHistory) {
        
        clusterAlertHistoryService.updateById(clusterAlertHistory);
        
        return Result.success();
    }
    
    /**
     * 删除
     */
    @RequestMapping("/delete")
    public Result delete(@RequestBody Integer[] ids) {
        clusterAlertHistoryService.removeByIds(Arrays.asList(ids));
        
        return Result.success();
    }
    
}
