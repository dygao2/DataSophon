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

import com.datasophon.api.configuration.ConfigBean;
import com.datasophon.api.enums.Status;
import com.datasophon.api.load.GlobalVariables;
import com.datasophon.api.service.*;
import com.datasophon.api.utils.PackageUtils;
import com.datasophon.api.utils.ProcessUtils;
import com.datasophon.api.utils.SecurityUtils;
import com.datasophon.common.Constants;
import com.datasophon.common.cache.CacheUtils;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.*;
import com.datasophon.dao.enums.ClusterState;
import com.datasophon.dao.mapper.ClusterInfoMapper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import akka.actor.*;

@Service("clusterInfoService")
@Transactional
public class ClusterInfoServiceImpl extends ServiceImpl<ClusterInfoMapper, ClusterInfoEntity>
        implements
            ClusterInfoService {

    @Autowired
    private ClusterInfoMapper clusterInfoMapper;

    @Autowired
    private ClusterRoleUserService clusterUserService;

    @Autowired
    private AlertGroupService alertGroupService;

    @Autowired
    private ClusterAlertGroupMapService groupMapService;

    @Autowired
    private ConfigBean configBean;

    @Autowired
    private FrameServiceService frameServiceService;

    @Autowired
    private ClusterHostService clusterHostService;

    @Autowired
    private ClusterYarnSchedulerService yarnSchedulerService;

    @Autowired
    private ClusterNodeLabelService nodeLabelService;

    @Autowired
    private ClusterQueueCapacityService queueCapacityService;

    @Autowired
    private ClusterRackService rackService;

    @Override
    public ClusterInfoEntity getClusterByClusterCode(String clusterCode) {
        ClusterInfoEntity clusterInfoEntity = clusterInfoMapper.getClusterByClusterCode(clusterCode);
        return clusterInfoEntity;
    }

    @Override
    public Result saveCluster(ClusterInfoEntity clusterInfo) {
        List<ClusterInfoEntity> list = this
                .list(new QueryWrapper<ClusterInfoEntity>().eq(Constants.CLUSTER_CODE, clusterInfo.getClusterCode()));
        if (Objects.nonNull(list) && list.size() >= 1) {
            return Result.error(Status.CLUSTER_CODE_EXISTS.getMsg());
        }
        clusterInfo.setCreateTime(new Date());
        clusterInfo.setCreateBy(SecurityUtils.getAuthUser().getUsername());
        clusterInfo.setClusterState(ClusterState.NEED_CONFIG);
        this.save(clusterInfo);
        List<AlertGroupEntity> alertGroupList = alertGroupService.list();
        for (AlertGroupEntity alertGroupEntity : alertGroupList) {
            ClusterAlertGroupMap alertGroupMap = new ClusterAlertGroupMap();
            alertGroupMap.setAlertGroupId(alertGroupEntity.getId());
            alertGroupMap.setClusterId(clusterInfo.getId());
            groupMapService.save(alertGroupMap);
        }
        // ProcessUtils.createServiceActor(clusterInfo);

        yarnSchedulerService.createDefaultYarnScheduler(clusterInfo.getId());

        nodeLabelService.createDefaultNodeLabel(clusterInfo.getId());

        queueCapacityService.createDefaultQueue(clusterInfo.getId());

        rackService.createDefaultRack(clusterInfo.getId());

        putClusterVariable(clusterInfo);
        return Result.success();
    }

    private void putClusterVariable(ClusterInfoEntity clusterInfo) {
        HashMap<String, String> globalVariables = new HashMap<>();
        List<FrameServiceEntity> frameServiceList =
                frameServiceService.getAllFrameServiceByFrameCode(clusterInfo.getClusterFrame());
        for (FrameServiceEntity frameServiceEntity : frameServiceList) {
            globalVariables.put("${" + frameServiceEntity.getServiceName() + "_HOME}",
                    Constants.INSTALL_PATH + Constants.SLASH + frameServiceEntity.getDecompressPackageName());
        }
        globalVariables.put("${INSTALL_PATH}", Constants.INSTALL_PATH);
        globalVariables.put("${apiHost}", CacheUtils.getString("hostname"));
        globalVariables.put("${apiPort}", configBean.getServerPort());
        globalVariables.put("${HADOOP_HOME}", Constants.INSTALL_PATH + Constants.SLASH
                + PackageUtils.getServiceDcPackageName(clusterInfo.getClusterFrame(), "HDFS"));

        GlobalVariables.put(clusterInfo.getId(), globalVariables);
    }

    @Override
    public Result getClusterList() {
        List<ClusterInfoEntity> list = this.list();
        for (ClusterInfoEntity clusterInfoEntity : list) {
            List<UserInfoEntity> userList =
                    clusterUserService.getAllClusterManagerByClusterId(clusterInfoEntity.getId());
            clusterInfoEntity.setClusterManagerList(userList);
            clusterInfoEntity.setClusterStateCode(clusterInfoEntity.getClusterState().getValue());
        }
        return Result.success(list);
    }

    @Override
    public Result runningClusterList() {
        List<ClusterInfoEntity> list =
                this.list(new QueryWrapper<ClusterInfoEntity>().eq(Constants.CLUSTER_STATE, ClusterState.RUNNING));
        return Result.success(list);
    }

    @Override
    public Result updateClusterState(Integer clusterId, Integer clusterState) {
        ClusterInfoEntity clusterInfo = this.getById(clusterId);
        if (clusterState == 2) {
            clusterInfo.setClusterState(ClusterState.RUNNING);
        }
        this.updateById(clusterInfo);
        return Result.success();
    }

    @Override
    public List<ClusterInfoEntity> getClusterByFrameCode(String frameCode) {
        return this.list(new QueryWrapper<ClusterInfoEntity>().eq(Constants.CLUSTER_FRAME, frameCode));
    }

    @Override
    public Result updateCluster(ClusterInfoEntity clusterInfo) {
        // 集群编码判重
        List<ClusterInfoEntity> list = this
                .list(new QueryWrapper<ClusterInfoEntity>().eq(Constants.CLUSTER_CODE, clusterInfo.getClusterCode()));
        if (Objects.nonNull(list) && list.size() >= 1) {
            ClusterInfoEntity clusterInfoEntity = list.get(0);
            if (!clusterInfoEntity.getId().equals(clusterInfo.getId())) {
                return Result.error(Status.CLUSTER_CODE_EXISTS.getMsg());
            }
        }
        ClusterInfoEntity cluster = this.getById(clusterInfo.getId());
        if (cluster.getClusterCode() != clusterInfo.getClusterCode()) {
            ProcessUtils.createServiceActor(clusterInfo);
        }
        this.updateById(clusterInfo);
        return Result.success();
    }

    @Override
    public void deleteCluster(List<Integer> ids) {
        Integer id = ids.get(0);
        ClusterInfoEntity clusterInfo = this.getById(id);
        this.removeByIds(ids);
        // delete host
        clusterHostService.deleteHostByClusterId(id);
    }
}
