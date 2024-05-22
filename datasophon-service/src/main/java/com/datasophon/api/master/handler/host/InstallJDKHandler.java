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

package com.datasophon.api.master.handler.host;

import com.datasophon.api.utils.MessageResolverUtils;
import com.datasophon.api.utils.MinaUtils;
import com.datasophon.common.Constants;
import com.datasophon.common.model.HostInfo;

import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.client.session.ClientSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstallJDKHandler implements DispatcherWorkerHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(InstallJDKHandler.class);
    
    @Override
    public boolean handle(ClientSession session, HostInfo hostInfo) {
        hostInfo.setProgress(60);
        String arch = MinaUtils.execCmdWithResult(session, "arch");
        String testResult = MinaUtils.execCmdWithResult(session, "test -d /usr/local/jdk1.8.0_333");
        boolean exists = true;
        if (StringUtils.isNotBlank(testResult) && "failed".equals(testResult)) {
            exists = false;
        }
        if ("x86_64".equals(arch)) {
            if (!exists) {
                hostInfo.setMessage(MessageResolverUtils.getMessage("start.install.jdk"));
                MinaUtils.uploadFile(session, "/usr/local",
                        Constants.MASTER_MANAGE_PACKAGE_PATH + Constants.SLASH + Constants.X86JDK);
                MinaUtils.execCmdWithResult(session, "tar -zxvf /usr/local/jdk-8u333-linux-x64.tar.gz -C /usr/local/");
            }
        }
        if ("aarch64".equals(arch)) {
            if (!exists) {
                hostInfo.setMessage(MessageResolverUtils.getMessage("start.install.jdk"));
                MinaUtils.uploadFile(session, "/usr/local",
                        Constants.MASTER_MANAGE_PACKAGE_PATH + Constants.SLASH + Constants.ARMJDK);
                MinaUtils.execCmdWithResult(session,
                        "tar -zxvf /usr/local/jdk-8u333-linux-aarch64.tar.gz -C /usr/local/");
            }
        }
        return true;
    }
}
