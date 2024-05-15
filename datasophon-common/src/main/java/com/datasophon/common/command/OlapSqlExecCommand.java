package com.datasophon.common.command;

import java.io.Serializable;

import lombok.Data;

@Data
public class OlapSqlExecCommand implements Serializable {
    
    private OlapOpsType opsType;
    
    private String feMaster;
    
    private String hostName;
}
