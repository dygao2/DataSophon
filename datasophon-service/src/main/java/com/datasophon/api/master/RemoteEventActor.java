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

package com.datasophon.api.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;
import akka.remote.AssociatedEvent;
import akka.remote.AssociationErrorEvent;
import akka.remote.DisassociatedEvent;

public class RemoteEventActor extends UntypedActor {
    
    private static final Logger logger = LoggerFactory.getLogger(RemoteEventActor.class);
    
    @Override
    public void onReceive(Object msg) throws Throwable {
        if (msg instanceof AssociationErrorEvent) {
            AssociationErrorEvent aee = (AssociationErrorEvent) msg;
            logger.info(aee.getLocalAddress() + "-->" + aee.getRemoteAddress() + ": " + aee.getCause());
        } else if (msg instanceof AssociatedEvent) {
            AssociatedEvent ae = (AssociatedEvent) msg;
            logger.info(ae.getLocalAddress() + "-->" + ae.getRemoteAddress() + " associated");
        } else if (msg instanceof DisassociatedEvent) {
            DisassociatedEvent de = (DisassociatedEvent) msg;
            logger.info(de.getLocalAddress() + "-->" + de.getRemoteAddress() + " disassociated");
        }
    }
}
