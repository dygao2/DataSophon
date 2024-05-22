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

import static com.datasophon.common.Constants.GRAFANA_PATH;

import com.datasophon.api.load.GlobalVariables;
import com.datasophon.api.service.ClusterServiceDashboardService;
import com.datasophon.common.Constants;
import com.datasophon.common.utils.PlaceholderUtils;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterServiceDashboard;
import com.datasophon.dao.mapper.ClusterServiceDashboardMapper;

import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service("clusterServiceDashboardService")
public class ClusterServiceDashboardServiceImpl
        extends
            ServiceImpl<ClusterServiceDashboardMapper, ClusterServiceDashboard>
        implements
            ClusterServiceDashboardService {
    
    @Value("${datasophon.proxy-grafana.enable:false}")
    private boolean proxy;
    
    @Value("${server.servlet.context-path}")
    private String contextPath;
    
    @Autowired
    ClusterServiceDashboardService dashboardService;
    
    @Override
    public Result getDashboardUrl(Integer clusterId) {
        ClusterServiceDashboard dashboard = dashboardService
                .getOne(new QueryWrapper<ClusterServiceDashboard>().eq(Constants.SERVICE_NAME, "TOTAL"));
        if (Objects.nonNull(dashboard) && StringUtils.hasText(dashboard.getDashboardUrl())) {
            return Result.success(getDashboardUrl(clusterId, dashboard));
        } else {
            return Result.error("缺少集群总览");
        }
    }
    
    @Override
    public String getGrafanaHost(Integer clusterId) {
        Map<String, String> globalVariables = GlobalVariables.get(clusterId);
        return globalVariables.get("${grafanaHost}") + ":3000";
    }
    
    @Override
    public String getDashboardUrl(Integer clusterId, ClusterServiceDashboard dashboard) {
        String url = dashboard.getDashboardUrl();
        if (proxy) {
            // 兼容旧记录
            if (url.startsWith("http://${grafanaHost}:3000")) {
                url = url.substring(26);
            }
            return contextPath + GRAFANA_PATH + "/" + clusterId + url;
        } else {
            Map<String, String> globalVariables = GlobalVariables.get(clusterId);
            String dashboardUrl = PlaceholderUtils.replacePlaceholders(dashboard.getDashboardUrl(), globalVariables,
                    Constants.REGEX_VARIABLE);
            return dashboardUrl;
        }
    }
}
