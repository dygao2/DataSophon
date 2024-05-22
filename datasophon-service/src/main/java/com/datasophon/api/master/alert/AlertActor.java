package com.datasophon.api.master.alert;

import com.datasophon.api.service.ClusterAlertHistoryService;
import com.datasophon.api.service.ClusterServiceInstanceService;
import com.datasophon.api.service.ClusterServiceRoleInstanceService;
import com.datasophon.api.service.host.ClusterHostService;
import com.datasophon.dao.entity.ClusterAlertHistory;
import com.datasophon.dao.entity.ClusterHostDO;
import com.datasophon.dao.entity.ClusterServiceInstanceEntity;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceEntity;
import com.datasophon.dao.enums.AlertLevel;
import com.datasophon.dao.enums.ServiceRoleState;
import com.datasophon.dao.enums.ServiceState;
import com.datasophon.domain.alert.gateway.AlertHistoryGateway;
import com.datasophon.domain.alert.model.AlertHistory;
import com.datasophon.domain.alert.model.AlertLabels;
import com.datasophon.domain.alert.model.AlertMessage;
import com.datasophon.domain.alert.model.Alerts;
import com.datasophon.domain.host.enums.HostState;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.alibaba.fastjson.JSONObject;

import akka.actor.UntypedActor;
import cn.hutool.extra.spring.SpringUtil;

public class AlertActor extends UntypedActor {
    
    private static final String FIRING = "firing";
    
    private static final String NODE = "node";
    
    private static final String WARNING = "warning";
    
    private static final String EXCEPTION = "exception";
    
    private static final String RESOLVED = "resolved";
    
    @Override
    public void onReceive(Object msg) throws Throwable {
        if (msg instanceof String) {
            String alertMessage = (String) msg;
            AlertMessage alertMes = JSONObject.parseObject(alertMessage, AlertMessage.class);
            AlertHistoryGateway alertHistoryGateway = SpringUtil.getBean(AlertHistoryGateway.class);
            ClusterHostService hostService = SpringUtil.getBean(ClusterHostService.class);
            ClusterAlertHistoryService alertHistoryService = SpringUtil.getBean(ClusterAlertHistoryService.class);
            ClusterServiceInstanceService serviceInstanceService =
                    SpringUtil.getBean(ClusterServiceInstanceService.class);
            ClusterServiceRoleInstanceService roleInstanceService =
                    SpringUtil.getBean(ClusterServiceRoleInstanceService.class);
            
            List<Alerts> alerts = alertMes.getAlerts();
            for (Alerts alertInfo : alerts) {
                AlertLabels labels = alertInfo.getLabels();
                String alertname = labels.getAlertname();
                int clusterId = labels.getClusterId();
                String instance = labels.getInstance();
                String status = alertInfo.getStatus();
                String hostname = instance.split(":")[0];
                String serviceRoleName = labels.getServiceRoleName();
                if (FIRING.equals(status)) {
                    Boolean hasEnabledAlertHistory =
                            alertHistoryGateway.hasEnabledAlertHistory(alertname, clusterId, hostname);
                    // 查询服务实例，服务角色实例
                    if (NODE.equals(serviceRoleName)) {
                        ClusterHostDO clusterHost = hostService.getClusterHostByHostname(hostname);
                        clusterHost.setHostState(
                                EXCEPTION.equals(labels.getSeverity()) ? HostState.OFFLINE : HostState.EXISTS_ALARM);
                        if (!hasEnabledAlertHistory) {
                            ClusterAlertHistory clusterAlertHistory = ClusterAlertHistory.builder()
                                    .clusterId(clusterId)
                                    .alertGroupName(labels.getJob())
                                    .alertTargetName(alertname)
                                    .createTime(new Date())
                                    .updateTime(new Date())
                                    .alertLevel(WARNING.equals(labels.getSeverity()) ? AlertLevel.WARN
                                            : AlertLevel.EXCEPTION)
                                    .alertInfo(alertInfo.getAnnotations().getDescription())
                                    .alertAdvice(alertInfo.getAnnotations().getSummary())
                                    .hostname(hostname)
                                    .isEnabled(1)
                                    .build();
                            alertHistoryService.save(clusterAlertHistory);
                        }
                        hostService.updateById(clusterHost);
                    } else {
                        ClusterServiceRoleInstanceEntity roleInstance =
                                roleInstanceService.getOneServiceRole(serviceRoleName, hostname, clusterId);
                        if (Objects.nonNull(roleInstance)) {
                            ClusterServiceInstanceEntity serviceInstance =
                                    serviceInstanceService.getById(roleInstance.getServiceId());
                            serviceInstance.setServiceState(ServiceState.EXISTS_ALARM);
                            roleInstance.setServiceRoleState(ServiceRoleState.EXISTS_ALARM);
                            if (!hasEnabledAlertHistory) {
                                ClusterAlertHistory clusterAlertHistory = ClusterAlertHistory.builder()
                                        .clusterId(clusterId)
                                        .alertGroupName(labels.getJob())
                                        .alertTargetName(alertname)
                                        .serviceInstanceId(serviceInstance.getId())
                                        .serviceRoleInstanceId(roleInstance.getId())
                                        .createTime(new Date())
                                        .updateTime(new Date())
                                        .alertLevel(WARNING.equals(labels.getSeverity()) ? AlertLevel.WARN
                                                : AlertLevel.EXCEPTION)
                                        .alertInfo(alertInfo.getAnnotations().getDescription())
                                        .alertAdvice(alertInfo.getAnnotations().getSummary())
                                        .hostname(hostname)
                                        .isEnabled(1)
                                        .build();
                                
                                alertHistoryService.save(clusterAlertHistory);
                            }
                            if (EXCEPTION.equals(labels.getSeverity())) {
                                serviceInstance.setServiceState(ServiceState.EXISTS_EXCEPTION);
                                roleInstance.setServiceRoleState(ServiceRoleState.STOP);
                            }
                            serviceInstanceService.updateById(serviceInstance);
                            roleInstanceService.updateById(roleInstance);
                        }
                    }
                    
                }
                if (RESOLVED.equals(status)) {
                    AlertHistory alertHistory =
                            alertHistoryGateway.getEnabledAlertHistory(alertname, clusterId, hostname);
                    if (Objects.nonNull(alertHistory)) {
                        boolean nodeHasWarnAlertList = alertHistoryGateway.nodeHasWarnAlertList(hostname,
                                serviceRoleName, alertHistory.getId());
                        
                        if (EXCEPTION.equals(labels.getSeverity())) {// 异常告警处理
                            if (NODE.equals(serviceRoleName)) {
                                // 置为正常
                                ClusterHostDO clusterHost = hostService.getClusterHostByHostname(hostname);
                                clusterHost.setHostState(
                                        nodeHasWarnAlertList ? HostState.EXISTS_ALARM : HostState.RUNNING);
                                hostService.updateById(clusterHost);
                            } else {
                                // 查询服务角色实例
                                ClusterServiceRoleInstanceEntity roleInstance = roleInstanceService
                                        .getOneServiceRole(labels.getServiceRoleName(), hostname, clusterId);
                                if (roleInstance.getServiceRoleState() != ServiceRoleState.RUNNING) {
                                    roleInstance.setServiceRoleState(ServiceRoleState.RUNNING);
                                    if (nodeHasWarnAlertList) {
                                        roleInstance.setServiceRoleState(ServiceRoleState.EXISTS_ALARM);
                                    }
                                    roleInstanceService.updateById(roleInstance);
                                }
                            }
                        } else {
                            // 警告告警处理
                            if (NODE.equals(serviceRoleName)) {
                                // 置为正常
                                ClusterHostDO clusterHost = hostService.getClusterHostByHostname(hostname);
                                clusterHost.setHostState(
                                        nodeHasWarnAlertList ? HostState.EXISTS_ALARM : HostState.RUNNING);
                                hostService.updateById(clusterHost);
                            } else {
                                // 查询服务角色实例
                                ClusterServiceRoleInstanceEntity roleInstance = roleInstanceService
                                        .getOneServiceRole(labels.getServiceRoleName(), hostname, clusterId);
                                if (roleInstance.getServiceRoleState() != ServiceRoleState.RUNNING) {
                                    if (nodeHasWarnAlertList) {
                                        roleInstance.setServiceRoleState(ServiceRoleState.EXISTS_ALARM);
                                    } else {
                                        roleInstance.setServiceRoleState(ServiceRoleState.RUNNING);
                                    }
                                    roleInstanceService.updateById(roleInstance);
                                }
                            }
                        }
                        alertHistoryGateway.updateAlertHistoryToDisabled(alertHistory.getId());
                    }
                }
            }
        } else {
            unhandled(msg);
        }
    }
}
