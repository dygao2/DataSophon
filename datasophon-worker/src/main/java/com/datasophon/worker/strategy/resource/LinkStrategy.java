package com.datasophon.worker.strategy.resource;

import com.datasophon.common.Constants;
import com.datasophon.common.utils.ShellUtils;

import java.io.File;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.io.FileUtil;

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
        File sourceFile = new File(source);
        File targetFile = new File(realTarget);
        if (!targetFile.exists() && sourceFile.exists()) {
            // 先创建文件夹
            FileUtil.mkdir(targetFile.getParent());
            ShellUtils.exceShell("ln -s " + source + " " + realTarget);
            log.info("Create symbolic dir: {} to {}", source, realTarget);
        }
    }
}
