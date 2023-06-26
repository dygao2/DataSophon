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

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.lang.Console;
import cn.hutool.http.HttpUtil;
import com.datasophon.common.Constants;
import com.datasophon.common.model.RunAs;
import com.datasophon.common.utils.CompressUtils;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.FileUtils;
import com.datasophon.common.utils.PropertyUtils;
import com.datasophon.common.utils.ShellUtils;
import com.datasophon.worker.utils.TaskConstants;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Data
public class InstallServiceHandler {


    private static final String HADOOP = "hadoop";

    private String serviceName;

    private String serviceRoleName;

    private Logger logger;

    public InstallServiceHandler(String serviceName, String serviceRoleName) {
        this.serviceName = serviceName;
        this.serviceRoleName = serviceRoleName;
        String loggerName = String.format("%s-%s-%s", TaskConstants.TASK_LOG_LOGGER_NAME, serviceName, serviceRoleName);
        logger = LoggerFactory.getLogger(loggerName);
    }

    public ExecResult install(String packageName, String decompressPackageName, String packageMd5, RunAs runAs) {
        ExecResult execResult = new ExecResult();
        try {
            String destDir = Constants.INSTALL_PATH + Constants.SLASH + "DDP/packages" + Constants.SLASH;
            String packagePath = destDir + packageName;

            Boolean needDownLoad = isNeedDownloadPkg(packagePath, packageMd5);

            if (Boolean.TRUE.equals(needDownLoad)) {
                downloadPkg(packageName, packagePath);
            }

            boolean result = decompressPkg(packageName, decompressPackageName, runAs, packagePath);
            execResult.setExecResult(result);
        } catch (Exception e) {
            execResult.setExecOut(e.getMessage());
            e.printStackTrace();
        }
        return execResult;
    }

    private Boolean isNeedDownloadPkg(String packagePath, String packageMd5) {
        Boolean needDownLoad = true;
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
                Console.log("installed：{}", FileUtil.readableFileSize(progressSize));
            }

            @Override
            public void finish() {
                Console.log("install success！");
            }
        });
        logger.info("download package {} success", packageName);
    }

    private boolean decompressPkg(String packageName, String decompressPackageName, RunAs runAs, String packagePath) {
        if (!FileUtil.exist(Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName)) {
            Boolean decompressResult = CompressUtils.decompressTarGz(packagePath, Constants.INSTALL_PATH);
            if (Boolean.TRUE.equals(decompressResult)) {
                if (Objects.nonNull(runAs)) {
                    ShellUtils.exceShell(" chown -R " + runAs.getUser() + ":" + runAs.getGroup() + " "
                            + Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName);
                }
                ShellUtils
                        .exceShell(" chmod -R 775 " + Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName);
                if (decompressPackageName.contains(Constants.PROMETHEUS)) {
                    String alertPath = Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName
                            + Constants.SLASH + "alert_rules";
                    ShellUtils.exceShell("sed -i \"s/clusterIdValue/" + PropertyUtils.getString("clusterId")
                            + "/g\" `grep clusterIdValue -rl " + alertPath + "`");
                }
                if (decompressPackageName.contains(HADOOP)) {
                    changeHadoopInstallPathPerm(decompressPackageName);
                }
                return true;
            } else {
                logger.warn("install package {} failed", packageName);
                return false;
            }
        } else {
            return true;
        }
    }

    private void changeHadoopInstallPathPerm(String decompressPackageName) {
        ShellUtils.exceShell(
                " chown -R  root:hadoop " + Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName);
        ShellUtils.exceShell(" chmod 755 " + Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName);
        ShellUtils.exceShell(
                " chmod -R 755 " + Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName + "/etc");
        ShellUtils.exceShell(" chmod 6050 " + Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName
                + "/bin/container-executor");
        ShellUtils.exceShell(" chmod 400 " + Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName
                + "/etc/hadoop/container-executor.cfg");
        ShellUtils.exceShell(" chown -R yarn:hadoop " + Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName
                + "/logs/userlogs");
        ShellUtils.exceShell(
                " chmod 775 " + Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName + "/logs/userlogs");
    }
}
