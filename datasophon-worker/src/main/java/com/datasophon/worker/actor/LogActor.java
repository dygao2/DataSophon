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
import com.datasophon.common.command.GetLogCommand;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.PlaceholderUtils;
import com.datasophon.common.utils.PropertyUtils;
import com.datasophon.worker.utils.FileUtils;

import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

public class LogActor extends UntypedActor {
    
    private static final Logger logger = LoggerFactory.getLogger(LogActor.class);
    
    @Override
    public void onReceive(Object msg) throws Throwable {
        if (msg instanceof GetLogCommand) {
            logger.info("get query log command");
            GetLogCommand command = (GetLogCommand) msg;
            HashMap<String, String> paramMap = new HashMap<>();
            String hostName = InetAddress.getLocalHost().getHostName();
            paramMap.put("${user}", "root");
            paramMap.put("${host}", hostName);
            String logFileName =
                    PlaceholderUtils.replacePlaceholders(command.getLogFile(), paramMap, Constants.REGEX_VARIABLE);
            
            ExecResult execResult = new ExecResult();
            String logStr = "can not find log file";
            if (logFileName.startsWith(StrUtil.SLASH) && FileUtil.exist(logFileName)) {
                logStr = FileUtils.readLastRows(logFileName, Charset.defaultCharset(), PropertyUtils.getInt("rows"));
            } else if (FileUtil.exist(Constants.INSTALL_PATH + Constants.SLASH + command.getDecompressPackageName()
                    + Constants.SLASH + logFileName)) {
                logStr = FileUtils
                        .readLastRows(
                                Constants.INSTALL_PATH + Constants.SLASH + command.getDecompressPackageName()
                                        + Constants.SLASH + logFileName,
                                Charset.defaultCharset(), PropertyUtils.getInt("rows"));
            }
            execResult.setExecResult(true);
            execResult.setExecOut(logStr);
            getSender().tell(execResult, getSelf());
        } else {
            unhandled(msg);
        }
    }
}
