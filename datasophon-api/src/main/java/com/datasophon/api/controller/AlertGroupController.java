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
import com.datasophon.api.service.AlertGroupService;
import com.datasophon.api.service.ClusterAlertQuotaService;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.AlertGroupEntity;
import com.datasophon.dao.entity.ClusterAlertQuota;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("alert/group")
public class AlertGroupController {
    
    @Autowired
    private AlertGroupService alertGroupService;
    
    @Autowired
    private ClusterAlertQuotaService alertQuotaService;
    
    /**
     * 列表
     */
    @RequestMapping("/list")
    public Result list(Integer clusterId, String alertGroupName, Integer page, Integer pageSize) {
        return alertGroupService.getAlertGroupList(clusterId, alertGroupName, page, pageSize);
    }
    
    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public Result info(@PathVariable("id") Integer id) {
        AlertGroupEntity alertGroup = alertGroupService.getById(id);
        
        return Result.success().put("alertGroup", alertGroup);
    }
    
    /**
     * 保存
     */
    @RequestMapping("/save")
    public Result save(@RequestBody AlertGroupEntity alertGroup) {
        alertGroup.setCreateTime(new Date());
        return alertGroupService.saveAlertGroup(alertGroup);
    }
    
    /**
     * 修改
     */
    @RequestMapping("/update")
    public Result update(@RequestBody AlertGroupEntity alertGroup) {
        alertGroupService.updateById(alertGroup);
        
        return Result.success();
    }
    
    /**
     * 删除
     */
    @RequestMapping("/delete")
    public Result delete(@RequestBody Integer[] ids) {
        
        // 校验是否绑定告警指标
        List<ClusterAlertQuota> list =
                alertQuotaService.lambdaQuery().in(ClusterAlertQuota::getAlertGroupId, ids).list();
        if (list.size() > 0) {
            return Result.error(Status.ALERT_GROUP_TIPS_ONE.getMsg());
        }
        alertGroupService.removeByIds(Arrays.asList(ids));
        
        return Result.success();
    }
    
}
