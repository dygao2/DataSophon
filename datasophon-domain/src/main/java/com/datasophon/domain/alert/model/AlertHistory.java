package com.datasophon.domain.alert.model;

import java.util.Date;

import lombok.Data;

@Data
public class AlertHistory {
    
    /**
     * 主键
     */
    private Integer id;
    /**
     * 告警组
     */
    private String alertGroupName;
    /**
     * 告警指标
     */
    private String alertTargetName;
    /**
     * 告警详情
     */
    private String alertInfo;
    /**
     * 告警建议
     */
    private String alertAdvice;
    /**
     * 主机
     */
    private String hostname;
    /**
     * 告警级别 1：警告2：异常
     */
    private Integer alertLevel;
    /**
     * 是否处理 1:未处理2：已处理
     */
    private Integer isEnabled;
    /**
     * 集群服务角色实例id
     */
    private Integer serviceRoleInstanceId;
    /**
     * 集群服务实例id
     */
    private Integer serviceInstanceId;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 集群id
     */
    private Integer clusterId;
}
