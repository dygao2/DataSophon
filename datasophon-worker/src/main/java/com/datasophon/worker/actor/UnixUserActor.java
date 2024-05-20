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

import com.datasophon.common.command.remote.CreateUnixUserCommand;
import com.datasophon.common.command.remote.DelUnixUserCommand;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.worker.utils.UnixUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

public class UnixUserActor extends UntypedActor {
    
    private static final Logger logger = LoggerFactory.getLogger(UnixUserActor.class);
    
    @Override
    public void onReceive(Object msg) throws Throwable {
        if (msg instanceof CreateUnixUserCommand) {
            CreateUnixUserCommand command = (CreateUnixUserCommand) msg;
            ExecResult execResult =
                    UnixUtils.createUnixUser(command.getUsername(), command.getMainGroup(), command.getOtherGroups());
            logger.info("create unix user {}", execResult.getExecResult() ? "success" : "failed");
            getSender().tell(execResult, getSelf());
        } else if (msg instanceof DelUnixUserCommand) {
            DelUnixUserCommand command = (DelUnixUserCommand) msg;
            ExecResult execResult = UnixUtils.delUnixUser(command.getUsername());
            logger.info("del unix user {}", execResult.getExecResult() ? "success" : "failed");
            getSender().tell(execResult, getSelf());
        } else {
            unhandled(msg);
        }
    }
}
