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
import com.datasophon.common.utils.PropertyUtils;
import com.datasophon.common.utils.ShellUtils;
import com.datasophon.worker.handler.ServiceHandler;
import com.datasophon.worker.utils.KerberosUtils;

import java.util.ArrayList;

import cn.hutool.core.io.FileUtil;

public class HiveServer2HandlerStrategy extends AbstractHandlerStrategy implements ServiceRoleStrategy {
    
    public HiveServer2HandlerStrategy(String serviceName, String serviceRoleName) {
        super(serviceName, serviceRoleName);
    }
    
    @Override
    public ExecResult handler(ServiceRoleOperateCommand command) {
        ExecResult startResult = new ExecResult();
        final String workPath = Constants.INSTALL_PATH + Constants.SLASH + command.getDecompressPackageName();
        ServiceHandler serviceHandler = new ServiceHandler(command.getServiceName(), command.getServiceRoleName());
        if (command.getEnableRangerPlugin()) {
            logger.info("start to enable hive hdfs plugin");
            ArrayList<String> commands = new ArrayList<>();
            commands.add("sh");
            commands.add("./enable-hive-plugin.sh");
            if (!FileUtil.exist(Constants.INSTALL_PATH + Constants.SLASH + command.getDecompressPackageName()
                    + "/ranger-hive-plugin/success.id")) {
                ExecResult execResult = ShellUtils.execWithStatus(Constants.INSTALL_PATH + Constants.SLASH
                        + command.getDecompressPackageName() + "/ranger-hive-plugin", commands, 30L, logger);
                if (execResult.getExecResult()) {
                    logger.info("enable ranger hive plugin success");
                    FileUtil.writeUtf8String("success", Constants.INSTALL_PATH + Constants.SLASH
                            + command.getDecompressPackageName() + "/ranger-hive-plugin/success.id");
                } else {
                    logger.info("enable ranger hive plugin failed");
                    return execResult;
                }
            }
        }
        logger.info("command is slave : {}", command.isSlave());
        if (command.getCommandType().equals(CommandType.INSTALL_SERVICE) && !command.isSlave()) {
            // init hive database
            logger.info("start to init hive schema");
            ArrayList<String> commands = new ArrayList<>();
            commands.add("bin/schematool");
            commands.add("-dbType");
            commands.add("mysql");
            commands.add("-initSchema");
            ExecResult execResult = ShellUtils.execWithStatus(
                    Constants.INSTALL_PATH + Constants.SLASH + command.getDecompressPackageName(), commands, 60L,
                    logger);
            if (execResult.getExecResult()) {
                logger.info("init hive schema success");
            } else {
                logger.info("init hive schema failed");
                return execResult;
            }
        }
        if (command.getEnableKerberos()) {
            logger.info("start to get hive keytab file");
            String hostname = CacheUtils.getString(Constants.HOSTNAME);
            KerberosUtils.createKeytabDir();
            if (!FileUtil.exist("/etc/security/keytab/hive.service.keytab")) {
                KerberosUtils.downloadKeytabFromMaster("hive/" + hostname, "hive.service.keytab");
            }
        }
        if (command.getCommandType().equals(CommandType.INSTALL_SERVICE)) {
            String hadoopHome = PropertyUtils.getString("HADOOP_HOME");
            ShellUtils.exceShell("sudo -u hdfs " + hadoopHome + "/bin/hdfs dfs -mkdir -p /user/hive/warehouse");
            ShellUtils.exceShell("sudo -u hdfs " + hadoopHome + "/bin/hdfs dfs -mkdir -p /tmp/hive");
            ShellUtils.exceShell(
                    "sudo -u hdfs " + hadoopHome + "/bin/hdfs dfs -mkdir -p /tmp/hadoop-yarn/staging/history/done");
            ShellUtils.exceShell("sudo -u hdfs " + hadoopHome + "/bin/hdfs dfs -chown -R hdfs:hadoop /tmp");
            ShellUtils.exceShell("sudo -u hdfs " + hadoopHome + "/bin/hdfs dfs -chmod -R 775 /tmp");
            ShellUtils
                    .exceShell("sudo -u hdfs " + hadoopHome + "/bin/hdfs dfs -chown hive:hadoop /user/hive/warehouse");
            ShellUtils.exceShell("sudo -u hdfs " + hadoopHome + "/bin/hdfs dfs -chown hive:hadoop /tmp/hive");
            ShellUtils.exceShell("sudo -u hdfs " + hadoopHome + "/bin/hdfs dfs -chmod 777 /tmp/hive");
            
            // 存在 tez 则创建软连接
            final String tezHomePath = Constants.INSTALL_PATH + Constants.SLASH + "tez";
            if (FileUtil.exist(tezHomePath)) {
                ShellUtils.exceShell("ln -s " + tezHomePath + "/conf/tez-site.xml " + workPath + "/conf/tez-site.xml");
            }
        }
        
        startResult = serviceHandler.start(command.getStartRunner(), command.getStatusRunner(),
                command.getDecompressPackageName(), command.getRunAs());
        return startResult;
    }
}
