package com.datasophon.worker.strategy.resource;


import com.datasophon.common.Constants;
import com.datasophon.common.utils.ShellUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
public class LinkStrategy extends ResourceStrategy {

    public static final String LINK_TYPE = "link";

    private String source;

    private String target;

    @Override
    public void exec() {
        String realTarget = basePath + Constants.SLASH + target;
        ShellUtils.exceShell("ln -s " + source + " " + realTarget);
        log.info("Create symbolic dir: {} to {}", source, realTarget);
    }
}
