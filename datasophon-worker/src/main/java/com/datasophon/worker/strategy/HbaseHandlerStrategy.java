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

public class HbaseHandlerStrategy extends AbstractHandlerStrategy implements ServiceRoleStrategy {
    
    public HbaseHandlerStrategy(String serviceName, String serviceRoleName) {
        super(serviceName, serviceRoleName);
    }
    
    @Override
    public ExecResult handler(ServiceRoleOperateCommand command) {
        ExecResult startResult = new ExecResult();
        ServiceHandler serviceHandler = new ServiceHandler(command.getServiceName(), command.getServiceRoleName());
        if (command.getEnableRangerPlugin()) {
            logger.info("start to enable  hbase plugin");
            ArrayList<String> commands = new ArrayList<>();
            commands.add("sh");
            commands.add("./enable-hbase-plugin.sh");
            if (!FileUtil.exist(Constants.INSTALL_PATH + Constants.SLASH + command.getDecompressPackageName()
                    + "/ranger-hbase-plugin/success.id")) {
                ExecResult execResult = ShellUtils.execWithStatus(Constants.INSTALL_PATH + Constants.SLASH
                        + command.getDecompressPackageName() + "/ranger-hbase-plugin", commands, 30L, logger);
                if (execResult.getExecResult()) {
                    logger.info("enable ranger hbase plugin success");
                    FileUtil.writeUtf8String("success", Constants.INSTALL_PATH + Constants.SLASH
                            + command.getDecompressPackageName() + "/ranger-hbase-plugin/success.id");
                } else {
                    logger.info("enable ranger hbase plugin failed");
                    return execResult;
                }
            }
        }
        if (command.getEnableKerberos()) {
            logger.info("start to get hbase keytab file");
            String hostname = CacheUtils.getString(Constants.HOSTNAME);
            KerberosUtils.createKeytabDir();
            if (!FileUtil.exist("/etc/security/keytab/hbase.keytab")) {
                KerberosUtils.downloadKeytabFromMaster("hbase/" + hostname, "hbase.keytab");
            }
        }
        if (command.getCommandType().equals(CommandType.INSTALL_SERVICE)) {
            String hadoopHome = PropertyUtils.getString("HADOOP_HOME");
            ShellUtils.exceShell("sudo -u hdfs " + hadoopHome + "/bin/hdfs dfs -mkdir -p /hbase");
            ShellUtils.exceShell("sudo -u hdfs " + hadoopHome + "/bin/hdfs dfs -chown hbase:hadoop /hbase");
            ShellUtils.exceShell("sudo -u hdfs " + hadoopHome + "/bin/hdfs dfs -chmod 777 /hbase");
        }
        startResult = serviceHandler.start(command.getStartRunner(), command.getStatusRunner(),
                command.getDecompressPackageName(), command.getRunAs());
        
        return startResult;
        
    }
}
