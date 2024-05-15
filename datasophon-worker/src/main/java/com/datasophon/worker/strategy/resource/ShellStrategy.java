package com.datasophon.worker.strategy.resource;

import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.ShellUtils;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
public class ShellStrategy extends ResourceStrategy {
    
    public static final String SHELL_TYPE = "sh";
    
    private List<List<String>> commands;
    
    @Override
    public void exec() {
        for (List<String> command : commands) {
            ExecResult result = ShellUtils.execWithStatus(basePath, command, 60L);
            log.info(" {} result {} ", command, result.getExecResult() ? "success" : "fail");
        }
    }
}
