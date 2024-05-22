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

import cn.hutool.core.io.FileUtil;

public class HistoryServerHandlerStrategy extends AbstractHandlerStrategy implements ServiceRoleStrategy {
    
    public HistoryServerHandlerStrategy(String serviceName, String serviceRoleName) {
        super(serviceName, serviceRoleName);
    }
    
    @Override
    public ExecResult handler(ServiceRoleOperateCommand command) {
        ServiceHandler serviceHandler = new ServiceHandler(command.getServiceName(), command.getServiceRoleName());
        if (command.getEnableKerberos()) {
            logger.info("start to get jobhistoryserver keytab file");
            String hostname = CacheUtils.getString(Constants.HOSTNAME);
            KerberosUtils.createKeytabDir();
            if (!FileUtil.exist("/etc/security/keytab/jhs.service.keytab")) {
                KerberosUtils.downloadKeytabFromMaster("jhs/" + hostname, "jhs.service.keytab");
            }
        }
        String hadoopHome = Constants.INSTALL_PATH + Constants.SLASH + command.getDecompressPackageName();
        if (command.getCommandType().equals(CommandType.INSTALL_SERVICE)) {
            // create tmp
            ShellUtils.exceShell("sudo -u hdfs " + hadoopHome + "/bin/hdfs dfs -mkdir -p /user/yarn/yarn-logs");
            ShellUtils.exceShell("sudo -u hdfs " + hadoopHome + "/bin/hdfs dfs -mkdir /tmp");
            ShellUtils.exceShell("sudo -u hdfs " + hadoopHome + "/bin/hdfs dfs -chmod 777 /tmp");
            ShellUtils
                    .exceShell("sudo -u hdfs " + hadoopHome + "/bin/hdfs dfs -chown yarn:hadoop /user/yarn/yarn-logs");
        }
        return serviceHandler.start(command.getStartRunner(), command.getStatusRunner(),
                command.getDecompressPackageName(), command.getRunAs());
    }
}
