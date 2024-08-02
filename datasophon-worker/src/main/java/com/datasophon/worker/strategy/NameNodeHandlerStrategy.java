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

package com.datasophon.worker.strategy;

import com.datasophon.common.Constants;
import com.datasophon.common.cache.CacheUtils;
import com.datasophon.common.command.ServiceRoleOperateCommand;
import com.datasophon.common.enums.CommandType;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.ShellUtils;
import com.datasophon.worker.handler.ServiceHandler;
import com.datasophon.worker.utils.KerberosUtils;

import java.util.ArrayList;

import cn.hutool.core.io.FileUtil;

public class NameNodeHandlerStrategy extends AbstractHandlerStrategy implements ServiceRoleStrategy {
    
    public NameNodeHandlerStrategy(String serviceName, String serviceRoleName) {
        super(serviceName, serviceRoleName);
    }
    
    @Override
    public ExecResult handler(ServiceRoleOperateCommand command) {
        ServiceHandler serviceHandler = new ServiceHandler(command.getServiceName(), command.getServiceRoleName());
        String workPath = Constants.INSTALL_PATH + Constants.SLASH + command.getDecompressPackageName();
        if (command.getCommandType().equals(CommandType.INSTALL_SERVICE)) {
            if (command.isSlave()) {
                // 执行hdfs namenode -bootstrapStandby
                logger.info("Start to execute hdfs namenode -bootstrapStandby");
                ArrayList<String> commands = new ArrayList<>();
                commands.add(workPath + "/bin/hdfs");
                commands.add("namenode");
                commands.add("-bootstrapStandby");
                commands.add("-nonInteractive");
                commands.add("-force");
                ExecResult execResult = ShellUtils.execWithStatus(workPath, commands, 30L, logger);
                if (execResult.getExecResult()) {
                    logger.info("Namenode standby success");
                } else {
                    logger.error("Namenode standby failed");
                    return execResult;
                }
            } else {
                logger.info("Start to execute format namenode");
                ArrayList<String> commands = new ArrayList<>();
                commands.add(workPath + "/bin/hdfs");
                commands.add("namenode");
                commands.add("-format");
                commands.add("-nonInteractive");
                commands.add("-force");
                commands.add("smhadoop");
                // 清空namenode元数据
                FileUtil.del("/data/dfs/nn/current");
                ExecResult execResult = ShellUtils.execWithStatus(workPath, commands, 180L, logger);
                if (execResult.getExecResult()) {
                    logger.info("Namenode format success");
                } else {
                    logger.error("Namenode format failed");
                    return execResult;
                }
            }
        }
        if (command.getEnableRangerPlugin()) {
            logger.info("Start to enable ranger hdfs plugin");
            ArrayList<String> commands = new ArrayList<>();
            commands.add("sh");
            commands.add(workPath + "/ranger-hdfs-plugin/enable-hdfs-plugin.sh");
            if (!FileUtil.exist(workPath + "/ranger-hdfs-plugin/success.id")) {
                ExecResult execResult =
                        ShellUtils.execWithStatus(workPath + "/ranger-hdfs-plugin", commands, 30L, logger);
                if (execResult.getExecResult()) {
                    logger.info("Enable ranger hdfs plugin success");
                    // 写入ranger plugin集成成功标识
                    FileUtil.writeUtf8String("success", workPath + "/ranger-hdfs-plugin/success.id");
                } else {
                    logger.info("Enable ranger hdfs plugin failed");
                    return execResult;
                }
            }
        }
        if (command.getEnableKerberos()) {
            logger.info("Start to get namenode keytab file");
            String hostname = CacheUtils.getString(Constants.HOSTNAME);
            KerberosUtils.createKeytabDir();
            if (!FileUtil.exist("/etc/security/keytab/nn.service.keytab")) {
                KerberosUtils.downloadKeytabFromMaster("nn/" + hostname, "nn.service.keytab");
            }
            if (!FileUtil.exist("/etc/security/keytab/spnego.service.keytab")) {
                KerberosUtils.downloadKeytabFromMaster("HTTP/" + hostname, "spnego.service.keytab");
            }
        }
        ExecResult startResult = serviceHandler.start(command.getStartRunner(), command.getStatusRunner(),
                command.getDecompressPackageName(), command.getRunAs());
        
        return startResult;
    }
    
}
