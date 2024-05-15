package com.datasophon.domain.host.model;

import java.util.Date;

import lombok.Data;

@Data
public class Host {
    
    /**
     * 主机名
     */
    private String hostname;
    /**
     * IP
     */
    private String ip;
    /**
     * 机架
     */
    private String rack;
    /**
     * 核数
     */
    private Integer coreNum;
    /**
     * 总内存
     */
    private Integer totalMem;
    /**
     * 总磁盘
     */
    private Integer totalDisk;
    
    private DynamicHostProp dynamicHostProp;
    /**
     * 集群id
     */
    private Integer clusterId;
    
    private String cpuArchitecture;
    
    private String nodeLabel;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
}
