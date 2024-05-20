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

import com.datasophon.api.enums.Status;
import com.datasophon.api.exceptions.ServiceException;
import com.datasophon.api.master.ActorUtils;
import com.datasophon.api.service.ClusterGroupService;
import com.datasophon.api.service.ClusterUserGroupService;
import com.datasophon.api.service.ClusterUserService;
import com.datasophon.api.service.host.ClusterHostService;
import com.datasophon.common.Constants;
import com.datasophon.common.command.remote.CreateUnixUserCommand;
import com.datasophon.common.command.remote.DelUnixUserCommand;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterGroup;
import com.datasophon.dao.entity.ClusterHostDO;
import com.datasophon.dao.entity.ClusterUser;
import com.datasophon.dao.entity.ClusterUserGroup;
import com.datasophon.dao.mapper.ClusterUserMapper;

import org.apache.commons.lang3.StringUtils;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import akka.actor.ActorSelection;
import akka.pattern.Patterns;
import akka.util.Timeout;

@Service("clusterUserService")
@Transactional
public class ClusterUserServiceImpl extends ServiceImpl<ClusterUserMapper, ClusterUser> implements ClusterUserService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClusterUserServiceImpl.class);
    @Autowired
    private ClusterGroupService groupService;
    
    @Autowired
    private ClusterHostService hostService;
    
    @Autowired
    private ClusterUserGroupService userGroupService;
    
    @Override
    public Result create(Integer clusterId, String username, Integer mainGroupId, String groupIds) {
        
        if (hasRepeatUserName(clusterId, username)) {
            return Result.error(Status.DUPLICATE_USER_NAME.getMsg());
        }
        List<ClusterHostDO> hostList = hostService.getHostListByClusterId(clusterId);
        
        ClusterUser clusterUser = new ClusterUser();
        clusterUser.setUsername(username);
        clusterUser.setClusterId(clusterId);
        this.save(clusterUser);
        buildClusterUserGroup(clusterId, clusterUser.getId(), mainGroupId, 1);
        
        String otherGroup = null;
        if (StringUtils.isNotBlank(groupIds)) {
            List<Integer> otherGroupIds =
                    Arrays.stream(groupIds.split(",")).map(e -> Integer.parseInt(e)).collect(Collectors.toList());
            for (Integer id : otherGroupIds) {
                buildClusterUserGroup(clusterId, clusterUser.getId(), id, 2);
            }
            Collection<ClusterGroup> clusterGroups = groupService.listByIds(otherGroupIds);
            otherGroup = clusterGroups.stream().map(e -> e.getGroupName()).collect(Collectors.joining(","));
        }
        
        ClusterGroup mainGroup = groupService.getById(mainGroupId);
        // sync to all hosts
        for (ClusterHostDO clusterHost : hostList) {
            ActorSelection unixUserActor = ActorUtils.actorSystem.actorSelection(
                    "akka.tcp://datasophon@" + clusterHost.getHostname() + ":2552/user/worker/unixUserActor");
            
            CreateUnixUserCommand createUnixUserCommand = new CreateUnixUserCommand();
            createUnixUserCommand.setUsername(username);
            createUnixUserCommand.setMainGroup(mainGroup.getGroupName());
            createUnixUserCommand.setOtherGroups(otherGroup);
            
            Timeout timeout = new Timeout(Duration.create(180, TimeUnit.SECONDS));
            Future<Object> execFuture = Patterns.ask(unixUserActor, createUnixUserCommand, timeout);
            ExecResult execResult = null;
            try {
                execResult = (ExecResult) Await.result(execFuture, timeout.duration());
                if (execResult.getExecResult()) {
                    logger.info("create unix user {} success at {}", username, clusterHost.getHostname());
                } else {
                    logger.info(execResult.getExecOut());
                    throw new ServiceException(500,
                            "create unix user " + username + " failed at " + clusterHost.getHostname());
                }
            } catch (Exception e) {
                throw new ServiceException(500,
                        "create unix user " + username + " failed at " + clusterHost.getHostname());
            }
        }
        return Result.success();
    }
    
    private void buildClusterUserGroup(Integer clusterId, Integer userId, Integer groupId, Integer userGroupType) {
        ClusterUserGroup clusterUserGroup = new ClusterUserGroup();
        clusterUserGroup.setUserId(userId);
        clusterUserGroup.setGroupId(groupId);
        clusterUserGroup.setClusterId(clusterId);
        clusterUserGroup.setUserGroupType(userGroupType);
        userGroupService.save(clusterUserGroup);
    }
    
    private boolean hasRepeatUserName(Integer clusterId, String username) {
        List<ClusterUser> list = this.list(new QueryWrapper<ClusterUser>()
                .eq(Constants.CLUSTER_ID, clusterId)
                .eq(Constants.USERNAME, username));
        if (list.size() > 0) {
            return true;
        }
        return false;
    }
    
    @Override
    public Result listPage(Integer clusterId, String username, Integer page, Integer pageSize) {
        Integer offset = (page - 1) * pageSize;
        List<ClusterUser> list = this.list(new QueryWrapper<ClusterUser>()
                .like(StringUtils.isNotBlank(username), Constants.USERNAME, username)
                .eq(Constants.CLUSTER_ID, clusterId)
                .last("limit " + offset + "," + pageSize));
        for (ClusterUser clusterUser : list) {
            ClusterGroup mainGroup = userGroupService.queryMainGroup(clusterUser.getId());
            List<ClusterGroup> otherGroupList = userGroupService.listOtherGroups(clusterUser.getId());
            if (Objects.nonNull(otherGroupList) && !otherGroupList.isEmpty()) {
                String otherGroups =
                        otherGroupList.stream().map(e -> e.getGroupName()).collect(Collectors.joining(","));
                clusterUser.setOtherGroups(otherGroups);
            }
            clusterUser.setMainGroup(mainGroup.getGroupName());
        }
        int total = this.count(new QueryWrapper<ClusterUser>()
                .like(StringUtils.isNotBlank(username), Constants.USERNAME, username)
                .eq(Constants.CLUSTER_ID, clusterId));
        return Result.success(list).put(Constants.TOTAL, total);
    }
    
    @Override
    public Result deleteClusterUser(Integer id) {
        ClusterUser clusterUser = this.getById(id);
        // delete user and group
        userGroupService.deleteByUser(id);
        List<ClusterHostDO> hostList = hostService.getHostListByClusterId(clusterUser.getClusterId());
        // sync to all hosts
        for (ClusterHostDO clusterHost : hostList) {
            ActorSelection unixUserActor = ActorUtils.actorSystem.actorSelection(
                    "akka.tcp://datasophon@" + clusterHost.getHostname() + ":2552/user/worker/unixUserActor");
            DelUnixUserCommand createUnixUserCommand = new DelUnixUserCommand();
            Timeout timeout = new Timeout(Duration.create(180, TimeUnit.SECONDS));
            createUnixUserCommand.setUsername(clusterUser.getUsername());
            Future<Object> execFuture = Patterns.ask(unixUserActor, createUnixUserCommand, timeout);
            ExecResult execResult = null;
            try {
                execResult = (ExecResult) Await.result(execFuture, timeout.duration());
                if (execResult.getExecResult()) {
                    logger.info("del unix user success at {}", clusterHost.getHostname());
                } else {
                    logger.info("del unix user failed at {}", clusterHost.getHostname());
                }
            } catch (Exception e) {
                logger.info("del unix user failed at {}", clusterHost.getHostname());
            }
        }
        this.removeById(id);
        return Result.success();
    }
    
    @Override
    public List<ClusterUser> listAllUser(Integer clusterId) {
        return this.lambdaQuery().eq(ClusterUser::getClusterId, clusterId).list();
    }
    
    @Override
    public void createUnixUserOnHost(ClusterUser clusterUser, String hostname) {
        String username = clusterUser.getUsername();
        ClusterGroup mainGroup = userGroupService.queryMainGroup(clusterUser.getId());
        List<ClusterGroup> otherGroupList = userGroupService.listOtherGroups(clusterUser.getId());
        String otherGroup = "";
        if (Objects.nonNull(otherGroupList) && !otherGroupList.isEmpty()) {
            otherGroup = otherGroupList.stream().map(e -> e.getGroupName()).collect(Collectors.joining(","));
        }
        ActorSelection unixUserActor = ActorUtils.actorSystem
                .actorSelection("akka.tcp://datasophon@" + hostname + ":2552/user/worker/unixUserActor");
        
        CreateUnixUserCommand createUnixUserCommand = new CreateUnixUserCommand();
        createUnixUserCommand.setUsername(clusterUser.getUsername());
        createUnixUserCommand.setMainGroup(mainGroup.getGroupName());
        createUnixUserCommand.setOtherGroups(otherGroup);
        
        Timeout timeout = new Timeout(Duration.create(180, TimeUnit.SECONDS));
        Future<Object> execFuture = Patterns.ask(unixUserActor, createUnixUserCommand, timeout);
        ExecResult execResult = null;
        try {
            execResult = (ExecResult) Await.result(execFuture, timeout.duration());
            if (execResult.getExecResult()) {
                logger.info("create unix user {} success at {}", username, hostname);
            } else {
                logger.info(execResult.getExecOut());
                throw new ServiceException(500, "create unix user " + username + " failed at " + hostname);
            }
        } catch (Exception e) {
            throw new ServiceException(500, "create unix user " + username + " failed at " + hostname);
        }
        
    }
}
