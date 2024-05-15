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
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.ShellUtils;
import com.datasophon.worker.handler.ServiceHandler;
import com.datasophon.worker.utils.KerberosUtils;

import java.sql.SQLException;
import java.util.ArrayList;

import cn.hutool.core.io.FileUtil;

public class JournalNodeHandlerStrategy extends AbstractHandlerStrategy implements ServiceRoleStrategy {
    
    public JournalNodeHandlerStrategy(String serviceName, String serviceRoleName) {
        super(serviceName, serviceRoleName);
    }
    
    @Override
    public ExecResult handler(ServiceRoleOperateCommand command) throws SQLException, ClassNotFoundException {
        ExecResult startResult = new ExecResult();
        ServiceHandler serviceHandler = new ServiceHandler(command.getServiceName(), command.getServiceRoleName());
        if (command.getEnableKerberos()) {
            logger.info("start to get journalnode keytab file");
            String hostname = CacheUtils.getString(Constants.HOSTNAME);
            KerberosUtils.createKeytabDir();
            String hadoopConfDir =
                    Constants.INSTALL_PATH + Constants.SLASH + command.getDecompressPackageName() + "/etc/hadoop/";
            if (!FileUtil.exist(hadoopConfDir + "ssl-server.xml")) {
                ShellUtils.exceShell(
                        "cp " + hadoopConfDir + "ssl-server.xml.template " + hadoopConfDir + "ssl-server.xml");
            }
            if (!FileUtil.exist(hadoopConfDir + "ssl-client.xml")) {
                ShellUtils.exceShell(
                        "cp " + hadoopConfDir + "ssl-client.xml.template " + hadoopConfDir + "ssl-client.xml");
            }
            if (!FileUtil.exist("/etc/security/keytab/jn.service.keytab")) {
                KerberosUtils.downloadKeytabFromMaster("jn/" + hostname, "jn.service.keytab");
            }
            if (!FileUtil.exist("/etc/security/keytab/keystore")) {
                ArrayList<String> commands = new ArrayList<>();
                commands.add("sh");
                commands.add("keystore.sh");
                commands.add(hostname);
                ExecResult execResult = ShellUtils.execWithStatus(Constants.WORKER_SCRIPT_PATH, commands, 30L, logger);
                if (!execResult.getExecResult()) {
                    logger.info("generate keystore file failed");
                    return execResult;
                }
            }
            startResult = serviceHandler.start(command.getStartRunner(), command.getStatusRunner(),
                    command.getDecompressPackageName(), command.getRunAs());
        } else {
            startResult = serviceHandler.start(command.getStartRunner(), command.getStatusRunner(),
                    command.getDecompressPackageName(), command.getRunAs());
        }
        return startResult;
    }
}
