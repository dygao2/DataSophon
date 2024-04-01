package com.datasophon.common.strategy.resource;


import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpUtil;
import com.datasophon.common.Constants;
import com.datasophon.common.utils.PropertyUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
public class DownloadStrategy extends ResourceStrategy {

    public static final String DOWNLOAD_TYPE = "download";

    private String from;

    private String to;


    @Override
    public void exec() {
        log.info("start to download resource : {}", from);
        String masterHost = PropertyUtils.getString(Constants.MASTER_HOST);
        String masterPort = PropertyUtils.getString(Constants.MASTER_WEB_PORT);
        String params = HttpUtil.toParams(MapUtil.<String, Object>builder("frameCode", frameCode)
                .put("serviceRoleName", serviceRole)
                .put("resource", from)
                .build());

        String url = "http://" + masterHost + ":" + masterPort
                + "/ddh/service/install/downloadResource?" + params;
        HttpUtil.downloadFile(url, new File(basePath + Constants.SLASH + to), 300);

        log.info("end to download resource {} to {} ", from, to);
    }
}
