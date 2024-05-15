package com.datasophon.domain.host.model;

import com.datasophon.domain.host.enums.HostState;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class DynamicHostProp {
    
    /**
     * 已用内存
     */
    private Integer usedMem;
    /**
     * 已用磁盘
     */
    private Integer usedDisk;
    /**
     * 平均负载
     */
    private String averageLoad;
    /**
     * 检测时间
     */
    private Date checkTime;
    /**
     * 1:正常运行 2：断线 3、存在告警
     */
    private HostState hostState;
    /**
     * 1:受管 2：断线
     */
    private Integer managed;
    
    private List<ServiceRole> serviceRoles;
}
