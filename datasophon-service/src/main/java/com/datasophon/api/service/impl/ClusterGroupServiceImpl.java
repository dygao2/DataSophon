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
import com.datasophon.api.service.host.ClusterHostService;
import com.datasophon.api.utils.ProcessUtils;
import com.datasophon.common.Constants;
import com.datasophon.common.command.remote.CreateUnixGroupCommand;
import com.datasophon.common.command.remote.DelUnixGroupCommand;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterGroup;
import com.datasophon.dao.entity.ClusterHostDO;
import com.datasophon.dao.entity.ClusterUser;
import com.datasophon.dao.mapper.ClusterGroupMapper;

import org.apache.commons.lang3.StringUtils;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

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

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;

@Service("clusterGroupService")
@Transactional
public class ClusterGroupServiceImpl extends ServiceImpl<ClusterGroupMapper, ClusterGroup>
        implements
            ClusterGroupService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClusterGroupServiceImpl.class);
    
    @Autowired
    private ClusterHostService hostService;
    
    @Autowired
    private ClusterUserGroupService userGroupService;
    
    @Override
    public Result saveClusterGroup(Integer clusterId, String groupName) {
        if (hasRepeatGroupName(clusterId, groupName)) {
            return Result.error(Status.GROUP_NAME_DUPLICATION.getMsg());
        }
        ClusterGroup clusterGroup = new ClusterGroup();
        clusterGroup.setClusterId(clusterId);
        clusterGroup.setGroupName(groupName);
        this.save(clusterGroup);
        
        List<ClusterHostDO> hostList = hostService.getHostListByClusterId(clusterId);
        for (ClusterHostDO clusterHost : hostList) {
            ActorRef unixGroupActor = ActorUtils.getRemoteActor(clusterHost.getHostname(), "unixGroupActor");
            CreateUnixGroupCommand createUnixGroupCommand = new CreateUnixGroupCommand();
            createUnixGroupCommand.setGroupName(groupName);
            Timeout timeout = new Timeout(Duration.create(180, TimeUnit.SECONDS));
            Future<Object> execFuture = Patterns.ask(unixGroupActor, createUnixGroupCommand, timeout);
            ExecResult execResult = null;
            try {
                execResult = (ExecResult) Await.result(execFuture, timeout.duration());
                if (execResult.getExecResult()) {
                    logger.info("create unix group success at {}", clusterHost.getHostname());
                } else {
                    logger.info(execResult.getExecOut());
                    throw new ServiceException(500,
                            "create unix group " + groupName + " failed at " + clusterHost.getHostname());
                }
            } catch (Exception e) {
                throw new ServiceException(500,
                        "create unix group " + groupName + " failed at " + clusterHost.getHostname());
            }
        }
        
        return Result.success();
    }
    
    private boolean hasRepeatGroupName(Integer clusterId, String groupName) {
        List<ClusterGroup> list = this.list(new QueryWrapper<ClusterGroup>()
                .eq(Constants.CLUSTER_ID, clusterId)
                .eq(Constants.GROUP_NAME, groupName));
        if (list.size() > 0) {
            return true;
        }
        return false;
    }
    
    @Override
    public void refreshUserGroupToHost(Integer clusterId) {
        List<ClusterHostDO> hostList = hostService.getHostListByClusterId(clusterId);
        List<ClusterGroup> groupList = this.list();
        for (ClusterGroup clusterGroup : groupList) {
            ProcessUtils.syncUserGroupToHosts(hostList, clusterGroup.getGroupName(), "groupadd");
        }
    }
    
    @Override
    public Result deleteUserGroup(Integer id) {
        ClusterGroup clusterGroup = this.getById(id);
        Integer num = userGroupService.countGroupUserNum(id);
        if (num > 0) {
            return Result.error(Status.USER_GROUP_TIPS_ONE.getMsg());
        }
        this.removeById(id);
        List<ClusterHostDO> hostList = hostService.getHostListByClusterId(clusterGroup.getClusterId());
        for (ClusterHostDO clusterHost : hostList) {
            ActorRef unixGroupActor = ActorUtils.getRemoteActor(clusterHost.getHostname(), "unixGroupActor");
            DelUnixGroupCommand delUnixGroupCommand = new DelUnixGroupCommand();
            delUnixGroupCommand.setGroupName(clusterGroup.getGroupName());
            Timeout timeout = new Timeout(Duration.create(180, TimeUnit.SECONDS));
            Future<Object> execFuture = Patterns.ask(unixGroupActor, delUnixGroupCommand, timeout);
            ExecResult execResult = null;
            try {
                execResult = (ExecResult) Await.result(execFuture, timeout.duration());
                if (execResult.getExecResult()) {
                    logger.info("del unix group success at {}", clusterHost.getHostname());
                } else {
                    logger.info("del unix group failed at {}", clusterHost.getHostname());
                }
            } catch (Exception e) {
                logger.info("del unix group failed at {}", clusterHost.getHostname());
            }
        }
        return Result.success();
    }
    
    @Override
    public Result listPage(String groupName, Integer clusterId, Integer page, Integer pageSize) {
        Integer offset = (page - 1) * pageSize;
        List<ClusterGroup> list = this.list(new QueryWrapper<ClusterGroup>()
                .like(StringUtils.isNotBlank(groupName), Constants.GROUP_NAME, groupName)
                .eq(Constants.CLUSTER_ID, clusterId)
                .last("limit " + offset + "," + pageSize));
        for (ClusterGroup clusterGroup : list) {
            List<ClusterUser> clusterUserList = userGroupService.listClusterUsers(clusterGroup.getId());
            if (Objects.nonNull(clusterUserList) && !clusterUserList.isEmpty()) {
                String clusterUsers =
                        clusterUserList.stream().map(e -> e.getUsername()).collect(Collectors.joining(","));
                clusterGroup.setClusterUsers(clusterUsers);
            }
        }
        int total = this.count(new QueryWrapper<ClusterGroup>()
                .like(StringUtils.isNotBlank(groupName), Constants.GROUP_NAME, groupName)
                .eq(Constants.CLUSTER_ID, clusterId));
        return Result.success(list).put(Constants.TOTAL, total);
    }
    
    @Override
    public List<ClusterGroup> listAllUserGroup(Integer clusterId) {
        return this.lambdaQuery().eq(ClusterGroup::getClusterId, clusterId).list();
    }
    
    @Override
    public void createUnixGroupOnHost(String hostname, String groupName) {
        ActorRef unixGroupActor = ActorUtils.getRemoteActor(hostname, "unixGroupActor");
        createUnixGroup(hostname, unixGroupActor, groupName);
    }
    
    private void createUnixGroup(String hostname, ActorRef unixGroupActor, String groupName) {
        CreateUnixGroupCommand createUnixGroupCommand = new CreateUnixGroupCommand();
        createUnixGroupCommand.setGroupName(groupName);
        Timeout timeout = new Timeout(Duration.create(180, TimeUnit.SECONDS));
        Future<Object> execFuture = Patterns.ask(unixGroupActor, createUnixGroupCommand, timeout);
        ExecResult execResult = null;
        try {
            execResult = (ExecResult) Await.result(execFuture, timeout.duration());
            if (execResult.getExecResult()) {
                logger.info("create unix group success at {}", hostname);
            } else {
                logger.info(execResult.getExecOut());
                throw new ServiceException(500, "create unix group " + groupName + " failed at " + hostname);
            }
        } catch (Exception e) {
            throw new ServiceException(500, "create unix group " + groupName + " failed at " + hostname);
        }
    }
}
