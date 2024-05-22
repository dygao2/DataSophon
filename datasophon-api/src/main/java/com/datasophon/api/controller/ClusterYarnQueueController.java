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

import com.datasophon.api.enums.Status;
import com.datasophon.api.service.ClusterYarnQueueService;
import com.datasophon.common.Constants;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterYarnQueue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@RestController
@RequestMapping("cluster/yarn/queue")
public class ClusterYarnQueueController {
    
    @Autowired
    private ClusterYarnQueueService clusterYarnQueueService;
    
    /**
     * 列表
     */
    @RequestMapping("/list")
    public Result list(Integer clusterId, Integer page, Integer pageSize) {
        return clusterYarnQueueService.listByPage(clusterId, page, pageSize);
    }
    
    /**
     * 刷新队列
     */
    @RequestMapping("/refreshQueues")
    public Result refreshQueues(Integer clusterId) throws Exception {
        return clusterYarnQueueService.refreshQueues(clusterId);
    }
    
    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public Result info(@PathVariable("id") Integer id) {
        ClusterYarnQueue clusterYarnQueue = clusterYarnQueueService.getById(id);
        
        return Result.success().put("clusterYarnQueue", clusterYarnQueue);
    }
    
    /**
     * 保存
     */
    @RequestMapping("/save")
    public Result save(@RequestBody ClusterYarnQueue clusterYarnQueue) {
        List<ClusterYarnQueue> list = clusterYarnQueueService
                .list(new QueryWrapper<ClusterYarnQueue>().eq(Constants.QUEUE_NAME, clusterYarnQueue.getQueueName()));
        if (Objects.nonNull(list) && list.size() == 1) {
            return Result.error(Status.QUEUE_NAME_ALREADY_EXISTS.getMsg());
        }
        clusterYarnQueue.setCreateTime(new Date());
        clusterYarnQueueService.save(clusterYarnQueue);
        
        return Result.success();
    }
    
    /**
     * 修改
     */
    @RequestMapping("/update")
    public Result update(@RequestBody ClusterYarnQueue clusterYarnQueue) {
        
        clusterYarnQueueService.updateById(clusterYarnQueue);
        
        return Result.success();
    }
    
    /**
     * 删除
     */
    @RequestMapping("/delete")
    public Result delete(@RequestBody Integer[] ids) {
        clusterYarnQueueService.removeByIds(Arrays.asList(ids));
        
        return Result.success();
    }
    
}
