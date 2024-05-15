package com.datasophon.worker.strategy.resource;

import com.datasophon.common.Constants;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import cn.hutool.core.io.FileUtil;

@EqualsAndHashCode(callSuper = true)
@Data
public class ReplaceStrategy extends ResourceStrategy {
    
    public static final String REPLACE_TYPE = "replace";
    
    private String source;
    
    private String regex;
    
    private String replacement;
    
    @Override
    public void exec() {
        File file = new File(basePath + Constants.SLASH + source);
        if (file.exists()) {
            List<String> lines = FileUtil.readLines(file, Charset.defaultCharset())
                    .stream().map(line -> line.replaceAll(regex, replacement)).collect(Collectors.toList());
            FileUtil.writeLines(lines, file, Charset.defaultCharset(), false);
        }
    }
}
