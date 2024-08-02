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
import com.datasophon.common.command.ServiceRoleOperateCommand;
import com.datasophon.common.enums.CommandType;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.ShellUtils;
import com.datasophon.worker.handler.ServiceHandler;

import java.util.ArrayList;

public class ZKFCHandlerStrategy extends AbstractHandlerStrategy implements ServiceRoleStrategy {
    
    public ZKFCHandlerStrategy(String serviceName, String serviceRoleName) {
        super(serviceName, serviceRoleName);
    }
    
    @Override
    public ExecResult handler(ServiceRoleOperateCommand command) {
        ExecResult startResult = new ExecResult();
        ServiceHandler serviceHandler = new ServiceHandler(command.getServiceName(), command.getServiceRoleName());
        String workPath = Constants.INSTALL_PATH + Constants.SLASH + command.getDecompressPackageName();
        if (!command.isSlave() && command.getCommandType().equals(CommandType.INSTALL_SERVICE)) {
            logger.info("start to execute hdfs zkfc -formatZK");
            
            ArrayList<String> commands = new ArrayList<>();
            commands.add(workPath + "/bin/hdfs");
            commands.add("zkfc");
            commands.add("-formatZK");
            commands.add("-nonInteractive");
            commands.add("-force");
            ExecResult execResult = ShellUtils.execWithStatus(workPath, commands, 300L, logger);
            if (execResult.getExecResult()) {
                logger.info("zkfc format success");
                startResult = serviceHandler.start(command.getStartRunner(), command.getStatusRunner(),
                        command.getDecompressPackageName(), command.getRunAs());
            } else {
                logger.info("zkfc format failed");
            }
        } else {
            startResult = serviceHandler.start(command.getStartRunner(), command.getStatusRunner(),
                    command.getDecompressPackageName(), command.getRunAs());
        }
        return startResult;
    }
}
