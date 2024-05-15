/*
 *
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
 *
 */

package com.datasophon.api.master;

import com.datasophon.api.enums.Status;
import com.datasophon.api.utils.MinaUtils;
import com.datasophon.common.command.HostCheckCommand;
import com.datasophon.common.model.CheckResult;
import com.datasophon.common.model.HostInfo;
import com.datasophon.common.utils.HostUtils;

import org.apache.sshd.client.session.ClientSession;

import scala.Option;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;
import cn.hutool.core.util.ObjectUtil;

public class HostConnectActor extends UntypedActor {
    
    private static final Logger logger = LoggerFactory.getLogger(HostConnectActor.class);
    
    @Override
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
        logger.info("or restart because {}", reason.getMessage());
        super.preRestart(reason, message);
    }
    
    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof HostCheckCommand) {
            String localIp = HostUtils.getLocalIp();
            String localHostName = HostUtils.getLocalHostName();
            logger.info("datasophon manager install hostname and ip :" + localHostName + ",", localIp);
            HostCheckCommand hostCheckCommand = (HostCheckCommand) message;
            HostInfo hostInfo = hostCheckCommand.getHostInfo();
            logger.info("start host check:{}", hostInfo.getHostname());
            if (hostInfo.getIp().equals(localIp)) {
                logger.info("datasophon manager node doesn't need to be checked");
                hostInfo.setCheckResult(
                        new CheckResult(
                                Status.CHECK_HOST_SUCCESS.getCode(),
                                Status.CHECK_HOST_SUCCESS.getMsg()));
            } else {
                ClientSession session =
                        MinaUtils.openConnection(
                                hostInfo.getHostname(), hostInfo.getSshPort(), hostInfo.getSshUser());
                if (ObjectUtil.isNotNull(session)) {
                    hostInfo.setCheckResult(
                            new CheckResult(
                                    Status.CHECK_HOST_SUCCESS.getCode(),
                                    Status.CHECK_HOST_SUCCESS.getMsg()));
                } else {
                    hostInfo.setCheckResult(
                            new CheckResult(
                                    Status.CONNECTION_FAILED.getCode(),
                                    Status.CONNECTION_FAILED.getMsg()));
                }
                MinaUtils.closeConnection(session);
            }
            logger.info("end host check:{}", hostInfo.getHostname());
        } else {
            unhandled(message);
        }
    }
}
