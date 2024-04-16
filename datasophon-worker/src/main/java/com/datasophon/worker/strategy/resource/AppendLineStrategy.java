package com.datasophon.worker.strategy.resource;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.datasophon.common.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class AppendLineStrategy extends ResourceStrategy {

    public static final String APPEND_LINE_TYPE = "append_line";

    private String source;

    private Integer line;

    private String text;

    @Override
    public void exec() {
        File file = new File(basePath + Constants.SLASH + source);
        if (file.exists() && ObjectUtil.isNotNull(line)) {
            List<String> lines = FileUtil.readLines(file, Charset.defaultCharset());
            if (lines.size() >= line) {
                String lineText = lines.get(line - 1);
                if (!lineText.equals(text)) {
                    lines.add(line - 1, text);
                }
                FileUtil.writeLines(lines, file, Charset.defaultCharset(), false);
            }
        }
    }
}
