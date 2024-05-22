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
import com.datasophon.api.service.FrameServiceRoleService;
import com.datasophon.api.service.FrameServiceService;
import com.datasophon.common.Constants;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterServiceInstanceEntity;
import com.datasophon.dao.entity.FrameServiceEntity;
import com.datasophon.dao.entity.FrameServiceRoleEntity;

import java.io.File;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import cn.hutool.core.io.FileUtil;

@Slf4j
@RestController
@RequestMapping("api/frame/service")
public class FrameServiceController {
    
    @Autowired
    private FrameServiceService frameVersionServiceService;
    
    @Autowired
    private FrameServiceRoleService frameServiceRoleService;
    
    @Autowired
    private ClusterServiceInstanceService clusterServiceInstanceService;
    
    /**
     * 列表
     */
    @RequestMapping("/list")
    public Result list(Integer clusterId) {
        return frameVersionServiceService.getAllFrameService(clusterId);
    }
    
    /**
     * 根据servce id列表查询服务
     */
    @RequestMapping("/getServiceListByServiceIds")
    public Result getServiceListByServiceIds(List<Integer> serviceIds) {
        return frameVersionServiceService.getServiceListByServiceIds(serviceIds);
    }
    
    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public Result info(@PathVariable("id") Integer id) {
        FrameServiceEntity frameVersionService = frameVersionServiceService.getById(id);
        
        return Result.success().put("frameVersionService", frameVersionService);
    }
    
    /**
     * 保存
     */
    @RequestMapping("/save")
    public Result save(@RequestBody FrameServiceEntity frameVersionService) {
        frameVersionServiceService.save(frameVersionService);
        
        return Result.success();
    }
    
    /**
     * 修改
     */
    @RequestMapping("/update")
    public Result update(@RequestBody FrameServiceEntity frameVersionService) {
        frameVersionServiceService.updateById(frameVersionService);
        
        return Result.success();
    }
    
    /**
     * 删除服务组件
     */
    @RequestMapping("/delete/{id}")
    public Result delete(@PathVariable("id") Integer id) {
        final FrameServiceEntity serviceEntity = frameVersionServiceService.getById(id);
        if (serviceEntity == null) {
            return Result.error("Service 组件不存在。");
        }
        // 已经安装为服务，无法删除
        final List<ClusterServiceInstanceEntity> roleEntities = clusterServiceInstanceService.list(
                Wrappers.<ClusterServiceInstanceEntity>lambdaQuery()
                        .eq(ClusterServiceInstanceEntity::getFrameServiceId, serviceEntity.getId()));
        if (roleEntities != null && !roleEntities.isEmpty()) {
            return Result.error("Service 组件正在使用中。");
        }
        
        // delete /DDP/packages 下的软件包
        File targetPackageFile = new File(Constants.MASTER_MANAGE_PACKAGE_PATH, serviceEntity.getPackageName());
        FileUtil.del(targetPackageFile);
        log.info("delete package file to: {}", targetPackageFile.getAbsolutePath());
        File targetPackageFileMd5 =
                new File(Constants.MASTER_MANAGE_PACKAGE_PATH, serviceEntity.getPackageName() + ".md5");
        FileUtil.del(targetPackageFileMd5);
        log.info("delete package md5 file to: {}", targetPackageFileMd5.getAbsolutePath());
        
        // 删除配置
        frameServiceRoleService.remove(Wrappers.<FrameServiceRoleEntity>lambdaQuery()
                .eq(FrameServiceRoleEntity::getServiceId, id));
        // 删除主服务
        frameVersionServiceService.removeById(id);
        return Result.success();
    }
    
}
