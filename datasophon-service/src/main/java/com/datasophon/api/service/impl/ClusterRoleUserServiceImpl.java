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

package com.datasophon.api.service.impl;

import com.datasophon.api.service.ClusterRoleUserService;
import com.datasophon.common.Constants;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterRoleUserEntity;
import com.datasophon.dao.entity.UserInfoEntity;
import com.datasophon.dao.enums.UserType;
import com.datasophon.dao.mapper.ClusterRoleUserMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service("clusterRoleUserService")
public class ClusterRoleUserServiceImpl extends ServiceImpl<ClusterRoleUserMapper, ClusterRoleUserEntity>
        implements
            ClusterRoleUserService {
    
    @Autowired
    private ClusterRoleUserMapper clusterRoleUserMapper;
    
    @Override
    public boolean isClusterManager(Integer userId, String clusterId) {
        List<ClusterRoleUserEntity> list = this.list(new QueryWrapper<ClusterRoleUserEntity>()
                .eq(Constants.DETAILS_USER_ID, userId)
                .eq(Constants.CLUSTER_ID, clusterId));
        if (Objects.nonNull(list) && list.size() == 1) {
            return true;
        }
        return false;
    }
    
    @Override
    public Result saveClusterManager(Integer clusterId, String userIds) {
        // 首先删除原有管理员
        this.remove(new QueryWrapper<ClusterRoleUserEntity>().eq(Constants.CLUSTER_ID, clusterId));
        if (StringUtils.isEmpty(userIds)) {
            // userIds 为空,表示取消授权
            return Result.success();
        }
        ArrayList<ClusterRoleUserEntity> list = new ArrayList<>();
        for (String userId : userIds.split(",")) {
            Integer id = Integer.parseInt(userId);
            ClusterRoleUserEntity clusterRoleUserEntity = new ClusterRoleUserEntity();
            clusterRoleUserEntity.setClusterId(clusterId);
            clusterRoleUserEntity.setUserId(id);
            clusterRoleUserEntity.setUserType(UserType.CLUSTER_MANAGER);
            list.add(clusterRoleUserEntity);
        }
        this.saveBatch(list);
        return Result.success();
    }
    
    @Override
    public List<UserInfoEntity> getAllClusterManagerByClusterId(Integer clusterId) {
        return clusterRoleUserMapper.getAllClusterManagerByClusterId(clusterId);
    }
}
