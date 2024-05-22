/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.datasophon.worker.handler;

import com.datasophon.common.Constants;
import com.datasophon.common.cache.CacheUtils;
import com.datasophon.common.command.InstallServiceRoleCommand;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.FileUtils;
import com.datasophon.common.utils.PropertyUtils;
import com.datasophon.common.utils.ShellUtils;
import com.datasophon.worker.strategy.resource.AppendLineStrategy;
import com.datasophon.worker.strategy.resource.DownloadStrategy;
import com.datasophon.worker.strategy.resource.EmptyStrategy;
import com.datasophon.worker.strategy.resource.LinkStrategy;
import com.datasophon.worker.strategy.resource.ReplaceStrategy;
import com.datasophon.worker.strategy.resource.ResourceStrategy;
import com.datasophon.worker.strategy.resource.ShellStrategy;
import com.datasophon.worker.utils.TaskConstants;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.lang.Console;
import cn.hutool.http.HttpUtil;

@Slf4j
@Data
public class InstallServiceHandler {
    
    private static final String HADOOP = "hadoop";
    
    private String frameCode;
    
    private String serviceName;
    
    private String serviceRoleName;
    
    private Logger logger;
    
    public InstallServiceHandler(String frameCode, String serviceName, String serviceRoleName) {
        this.frameCode = frameCode;
        this.serviceName = serviceName;
        this.serviceRoleName = serviceRoleName;
        String loggerName = String.format("%s-%s-%s-%s", TaskConstants.TASK_LOG_LOGGER_NAME, frameCode, serviceName,
                serviceRoleName);
        logger = LoggerFactory.getLogger(loggerName);
    }
    
    public ExecResult install(InstallServiceRoleCommand command) {
        ExecResult execResult = new ExecResult();
        try {
            String destDir = Constants.INSTALL_PATH + Constants.SLASH + "DDP/packages" + Constants.SLASH;
            String packageName = command.getPackageName();
            String packagePath = destDir + packageName;
            String decompressPackageName = command.getDecompressPackageName();
            
            Boolean needDownLoad =
                    !Objects.equals(PropertyUtils.getString(Constants.MASTER_HOST), CacheUtils.get(Constants.HOSTNAME))
                            && isNeedDownloadPkg(packagePath, command.getPackageMd5());
            
            if (Boolean.TRUE.equals(needDownLoad)) {
                downloadPkg(packageName, packagePath);
            }
            
            boolean result = decompressPkg(packageName, decompressPackageName, destDir);
            if (result) {
                if (CollUtil.isNotEmpty(command.getResourceStrategies())) {
                    for (Map<String, Object> strategy : command.getResourceStrategies()) {
                        String type = (String) strategy.get(ResourceStrategy.TYPE_KEY);
                        ResourceStrategy rs;
                        switch (type) {
                            case ReplaceStrategy.REPLACE_TYPE:
                                rs = BeanUtil.mapToBean(strategy, ReplaceStrategy.class, true,
                                        CopyOptions.create().ignoreError());
                                break;
                            case DownloadStrategy.DOWNLOAD_TYPE:
                                rs = BeanUtil.mapToBean(strategy, DownloadStrategy.class, true,
                                        CopyOptions.create().ignoreError());
                                break;
                            case AppendLineStrategy.APPEND_LINE_TYPE:
                                rs = BeanUtil.mapToBean(strategy, AppendLineStrategy.class, true,
                                        CopyOptions.create().ignoreError());
                                break;
                            case LinkStrategy.LINK_TYPE:
                                rs = BeanUtil.mapToBean(strategy, LinkStrategy.class, true,
                                        CopyOptions.create().ignoreError());
                                break;
                            case ShellStrategy.SHELL_TYPE:
                                rs = BeanUtil.mapToBean(strategy, ShellStrategy.class, true,
                                        CopyOptions.create().ignoreError());
                                break;
                            default:
                                rs = new EmptyStrategy();
                        }
                        rs.setFrameCode(frameCode);
                        rs.setService(serviceName);
                        rs.setServiceRole(serviceRoleName);
                        rs.setBasePath(Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName);
                        rs.exec();
                    }
                }
                
                if (Objects.nonNull(command.getRunAs())) {
                    ExecResult chownResult = ShellUtils.exceShell(
                            " chown -R " + command.getRunAs().getUser() + ":" + command.getRunAs().getGroup() + " "
                                    + Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName);
                    logger.info("chown {} {}", decompressPackageName, chownResult.getExecResult() ? "success" : "fail");
                }
                ExecResult chmodResult = ShellUtils
                        .exceShell(" chmod -R 775 " + Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName);
                logger.info("chmod {} {}", decompressPackageName, chmodResult.getExecResult() ? "success" : "fail");
                if (decompressPackageName.contains(Constants.PROMETHEUS)) {
                    String alertPath = Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName
                            + Constants.SLASH + "alert_rules";
                    ShellUtils.exceShell("sed -i \"s/clusterIdValue/" + PropertyUtils.getString("clusterId")
                            + "/g\" `grep clusterIdValue -rl " + alertPath + "`");
                }
                if (decompressPackageName.contains(HADOOP)) {
                    changeHadoopInstallPathPerm(decompressPackageName);
                }
            }
            execResult.setExecResult(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            execResult.setExecOut(e.getMessage());
        }
        return execResult;
    }
    
    private Boolean isNeedDownloadPkg(String packagePath, String packageMd5) {
        boolean needDownLoad = true;
        logger.info("Remote package md5 is {}", packageMd5);
        if (FileUtil.exist(packagePath)) {
            // check md5
            String md5 = FileUtils.md5(new File(packagePath));
            
            logger.info("Local md5 is {}", md5);
            
            if (StringUtils.isNotBlank(md5) && packageMd5.trim().equals(md5.trim())) {
                needDownLoad = false;
            }
        }
        return needDownLoad;
    }
    
    private void downloadPkg(String packageName, String packagePath) {
        String masterHost = PropertyUtils.getString(Constants.MASTER_HOST);
        String masterPort = PropertyUtils.getString(Constants.MASTER_WEB_PORT);
        String downloadUrl = "http://" + masterHost + ":" + masterPort
                + "/ddh/service/install/downloadPackage?packageName=" + packageName;
        
        logger.info("download url is {}", downloadUrl);
        
        HttpUtil.downloadFile(downloadUrl, FileUtil.file(packagePath), new StreamProgress() {
            
            @Override
            public void start() {
                Console.log("start to install。。。。");
            }
            
            @Override
            public void progress(long progressSize, long l1) {
                Console.log("installed：{} / {} ", FileUtil.readableFileSize(progressSize),
                        FileUtil.readableFileSize(l1));
            }
            
            @Override
            public void finish() {
                Console.log("install success！");
            }
        });
        logger.info("download package {} success", packageName);
    }
    
    private boolean decompressPkg(String packageName, String decompressPackageName, String destDir) {
        boolean decompressResult = true;
        if (!FileUtil.exist(Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName)) {
            return decompressTarGz(destDir + packageName, Constants.INSTALL_PATH);
        }
        return decompressResult;
    }
    
    public boolean decompressTarGz(String sourceTarGzFile, String targetDir) {
        logger.info("Start to use tar -zxvf to decompress {}", sourceTarGzFile);
        ArrayList<String> command = new ArrayList<>();
        command.add("tar");
        command.add("-zxvf");
        command.add(sourceTarGzFile);
        command.add("-C");
        command.add(targetDir);
        ExecResult execResult = ShellUtils.execWithStatus(targetDir, command, 120, logger);
        return execResult.getExecResult();
    }
    
    private void changeHadoopInstallPathPerm(String decompressPackageName) {
        ShellUtils.exceShell(
                " chown -R  root:hadoop " + Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName);
        ShellUtils.exceShell(" chmod 775 " + Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName);
        ShellUtils.exceShell(
                " chmod -R 775 " + Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName + "/etc");
        ShellUtils.exceShell(" chmod 6050 " + Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName
                + "/bin/container-executor");
        ShellUtils.exceShell(" chmod 400 " + Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName
                + "/etc/hadoop/container-executor.cfg");
        ShellUtils.exceShell(" chown -R yarn:hadoop " + Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName
                + "/logs/userlogs");
        ShellUtils.exceShell(
                " chmod 775 " + Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName + "/logs/userlogs");
        ShellUtils.exceShell(" ln -s " + Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName + " "
                + Constants.INSTALL_PATH + Constants.SLASH + "hadoop");
    }
}
