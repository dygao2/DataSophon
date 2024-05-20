package com.datasophon.worker.strategy.resource;

import com.datasophon.common.Constants;
import com.datasophon.common.utils.FileUtils;
import com.datasophon.common.utils.PropertyUtils;

import java.io.File;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpUtil;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
public class DownloadStrategy extends ResourceStrategy {
    
    public static final String DOWNLOAD_TYPE = "download";
    
    private String from;
    
    private String to;
    
    private String md5;
    
    @Override
    public void exec() {
        File file = new File(basePath + Constants.SLASH + to);
        if (file.exists() && FileUtils.md5(file).equals(md5)) {
            log.info("resource {}  existed", to);
            return;
        }
        
        log.info("start to download resource : {}", from);
        
        String masterHost = PropertyUtils.getString(Constants.MASTER_HOST);
        String masterPort = PropertyUtils.getString(Constants.MASTER_WEB_PORT);
        String params = HttpUtil.toParams(MapUtil.<String, Object>builder("frameCode", frameCode)
                .put("serviceRoleName", serviceRole)
                .put("resource", from)
                .build());
        
        String url = "http://" + masterHost + ":" + masterPort
                + "/ddh/service/install/downloadResource?" + params;
        HttpUtil.downloadFile(url, file, 300);
        
        log.info("end to download resource {} to {} ", from, to);
    }
}
