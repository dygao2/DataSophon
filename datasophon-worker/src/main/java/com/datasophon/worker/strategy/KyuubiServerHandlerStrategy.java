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
import com.datasophon.worker.handler.ServiceHandler;
import com.datasophon.worker.utils.KerberosUtils;

import java.sql.SQLException;

import cn.hutool.core.io.FileUtil;

public class KyuubiServerHandlerStrategy extends AbstractHandlerStrategy implements ServiceRoleStrategy {
    
    private static final String KEYTAB_NAME = "kyuubi.service.keytab";
    private static final String KEYTAB_PATH = "/etc/security/keytab/" + KEYTAB_NAME;
    
    public KyuubiServerHandlerStrategy(String serviceName, String serviceRoleName) {
        super(serviceName, serviceRoleName);
    }
    
    @Override
    public ExecResult handler(ServiceRoleOperateCommand command) throws SQLException, ClassNotFoundException {
        ExecResult startResult;
        if (command.getEnableKerberos()) {
            logger.info("start to get kyuubi keytab file");
            String hostname = CacheUtils.getString(Constants.HOSTNAME);
            KerberosUtils.createKeytabDir();
            if (!FileUtil.exist(KEYTAB_PATH)) {
                KerberosUtils.downloadKeytabFromMaster("kyuubi/" + hostname, KEYTAB_NAME);
            }
        }
        ServiceHandler serviceHandler = new ServiceHandler(
                command.getServiceName(),
                command.getServiceRoleName());
        startResult = serviceHandler.start(
                command.getStartRunner(),
                command.getStatusRunner(),
                command.getDecompressPackageName(),
                command.getRunAs());
        return startResult;
    }
}
