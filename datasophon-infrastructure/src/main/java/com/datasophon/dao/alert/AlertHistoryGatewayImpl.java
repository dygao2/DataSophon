package com.datasophon.dao.alert;

import com.datasophon.dao.entity.ClusterAlertHistory;
import com.datasophon.dao.enums.AlertLevel;
import com.datasophon.dao.mapper.ClusterAlertHistoryMapper;
import com.datasophon.domain.alert.gateway.AlertHistoryGateway;
import com.datasophon.domain.alert.model.AlertHistory;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

@Component
public class AlertHistoryGatewayImpl implements AlertHistoryGateway {
    
    @Autowired
    private ClusterAlertHistoryMapper alertHistoryMapper;
    
    @Override
    public boolean hasEnabledAlertHistory(String alertname, int clusterId, String hostname) {
        LambdaQueryWrapper<ClusterAlertHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClusterAlertHistory::getAlertTargetName, alertname)
                .eq(ClusterAlertHistory::getClusterId, clusterId)
                .eq(ClusterAlertHistory::getHostname, hostname)
                .eq(ClusterAlertHistory::getIsEnabled, 1);
        ClusterAlertHistory clusterAlertHistory = alertHistoryMapper.selectOne(queryWrapper);
        if (Objects.nonNull(clusterAlertHistory)) {
            return true;
        }
        return false;
    }
    
    @Override
    public AlertHistory getEnabledAlertHistory(String alertname, int clusterId, String hostname) {
        LambdaQueryWrapper<ClusterAlertHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClusterAlertHistory::getAlertTargetName, alertname)
                .eq(ClusterAlertHistory::getClusterId, clusterId)
                .eq(ClusterAlertHistory::getHostname, hostname)
                .eq(ClusterAlertHistory::getIsEnabled, 1);
        ClusterAlertHistory clusterAlertHistory = alertHistoryMapper.selectOne(queryWrapper);
        if (Objects.nonNull(clusterAlertHistory)) {
            AlertHistory alertHistory = new AlertHistory();
            BeanUtils.copyProperties(clusterAlertHistory, alertHistory);
            alertHistory.setAlertLevel(clusterAlertHistory.getAlertLevel().getValue());
            return alertHistory;
        }
        return null;
    }
    
    @Override
    public void updateAlertHistoryToDisabled(Integer id) {
        ClusterAlertHistory clusterAlertHistory = alertHistoryMapper.selectById(id);
        clusterAlertHistory.setIsEnabled(2);
        alertHistoryMapper.updateById(clusterAlertHistory);
    }
    
    @Override
    public boolean nodeHasWarnAlertList(String hostname, String serviceRoleName, Integer id) {
        LambdaQueryWrapper<ClusterAlertHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClusterAlertHistory::getHostname, hostname)
                .eq(ClusterAlertHistory::getAlertGroupName, serviceRoleName.toLowerCase())
                .eq(ClusterAlertHistory::getIsEnabled, 1)
                .eq(ClusterAlertHistory::getAlertLevel, AlertLevel.WARN)
                .ne(ClusterAlertHistory::getId, id);
        List<ClusterAlertHistory> clusterAlertHistories = alertHistoryMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(clusterAlertHistories)) {
            return false;
        }
        return true;
    }
}
