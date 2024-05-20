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

import com.datasophon.common.model.HostInfo;

import org.apache.sshd.client.session.ClientSession;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class DispatcherWorkerHandlerChain {
    
    private List<DispatcherWorkerHandler> handlers = new ArrayList<>();
    
    public void addHandler(DispatcherWorkerHandler handler) {
        this.handlers.add(handler);
    }
    
    public void handle(ClientSession session, HostInfo hostInfo) throws UnknownHostException {
        for (DispatcherWorkerHandler handler : handlers) {
            boolean handled = handler.handle(session, hostInfo);
            if (!handled) {
                break;
            }
        }
    }
}
