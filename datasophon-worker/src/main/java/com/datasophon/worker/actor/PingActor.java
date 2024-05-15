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

import com.datasophon.common.command.PingCommand;
import com.datasophon.common.utils.ExecResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

/**
 * 发送 ping，返回 pong
 *
 * @author zhenqin
 */
public class PingActor extends UntypedActor {
    
    private static final Logger logger = LoggerFactory.getLogger(PingActor.class);
    
    @Override
    public void onReceive(Object msg) throws Throwable {
        if (msg instanceof PingCommand) {
            PingCommand command = (PingCommand) msg;
            ExecResult execResult = new ExecResult();
            execResult.setExecResult(true);
            execResult.setExecOut("pong");
            getSender().tell(execResult, getSelf());
        } else {
            unhandled(msg);
        }
    }
}
