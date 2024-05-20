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

package com.datasophon.worker.actor;

import com.datasophon.common.Constants;
import com.datasophon.common.command.remote.GenerateKeytabFileCommand;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.ShellUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;
import cn.hutool.core.io.FileUtil;

public class KerberosActor extends UntypedActor {
    
    private static final Logger logger = LoggerFactory.getLogger(KerberosActor.class);
    
    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof GenerateKeytabFileCommand) {
            ExecResult execResult = new ExecResult();
            GenerateKeytabFileCommand command = (GenerateKeytabFileCommand) message;
            String keytabFilePath =
                    "/etc/security/keytab/" + command.getHostname() + Constants.SLASH + command.getKeytabName();
            logger.info("find keytab file {}", keytabFilePath);
            if (!FileUtil.exist(keytabFilePath)) {
                FileUtil.mkParentDirs(keytabFilePath);
                String addprinc =
                        "kadmin -padmin/admin -wadmin -q \"addprinc -randkey " + command.getPrincipal() + "\"";
                logger.info("add principal : {}", addprinc);
                ShellUtils.exceShell(addprinc);
                String keytabCmd = "kadmin -padmin/admin -wadmin -q \"xst -k " + keytabFilePath + " "
                        + command.getPrincipal() + "\"";
                
                logger.info("generate keytab file cmd :{}", keytabCmd);
                ShellUtils.exceShell(keytabCmd);
            }
            execResult.setExecResult(true);
            getSender().tell(execResult, getSelf());
        } else {
            unhandled(message);
        }
    }
}
