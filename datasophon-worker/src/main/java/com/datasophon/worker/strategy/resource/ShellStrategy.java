package com.datasophon.worker.strategy.resource;

import com.datasophon.common.Constants;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.ShellUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

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
            log.info(" {} result {} ", command, result.getExecResult()? "success": "fail");
        }
    }
}
