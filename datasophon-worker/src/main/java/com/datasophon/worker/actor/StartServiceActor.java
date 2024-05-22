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

import com.datasophon.common.command.ServiceRoleOperateCommand;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.worker.handler.ServiceHandler;
import com.datasophon.worker.strategy.ServiceRoleStrategy;
import com.datasophon.worker.strategy.ServiceRoleStrategyContext;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

public class StartServiceActor extends UntypedActor {
    
    private static final Logger logger = LoggerFactory.getLogger(StartServiceActor.class);
    
    @Override
    public void onReceive(Object msg) throws Throwable {
        if (msg instanceof ServiceRoleOperateCommand) {
            ServiceRoleOperateCommand command = (ServiceRoleOperateCommand) msg;
            logger.info("start to start service role {}", command.getServiceRoleName());
            ExecResult startResult = new ExecResult();
            ServiceHandler serviceHandler = new ServiceHandler(command.getServiceName(), command.getServiceRoleName());
            
            ServiceRoleStrategy serviceRoleHandler =
                    ServiceRoleStrategyContext.getServiceRoleHandler(command.getServiceRoleName());
            if (Objects.nonNull(serviceRoleHandler)) {
                startResult = serviceRoleHandler.handler(command);
            } else {
                startResult = serviceHandler.start(command.getStartRunner(), command.getStatusRunner(),
                        command.getDecompressPackageName(), command.getRunAs());
            }
            
            getSender().tell(startResult, getSelf());
            logger.info("service role {} start result {}", command.getServiceRoleName(),
                    startResult.getExecResult() ? "success" : "failed");
        } else {
            unhandled(msg);
        }
        
    }
}
