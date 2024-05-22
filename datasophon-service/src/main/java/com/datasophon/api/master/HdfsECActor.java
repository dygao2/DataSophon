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

import com.datasophon.api.service.ClusterServiceRoleInstanceService;
import com.datasophon.api.utils.ProcessUtils;
import com.datasophon.api.utils.SpringTool;
import com.datasophon.common.command.HdfsEcCommand;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceEntity;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

/**
 * Used to manage hdfs capacity expansion and reduction
 */
public class HdfsECActor extends UntypedActor {
    
    private static final Logger logger = LoggerFactory.getLogger(HdfsECActor.class);
    
    @Override
    public void onReceive(Object msg) throws Throwable {
        if (msg instanceof HdfsEcCommand) {
            HdfsEcCommand hdfsEcCommand = (HdfsEcCommand) msg;
            ClusterServiceRoleInstanceService roleInstanceService =
                    SpringTool.getApplicationContext().getBean(ClusterServiceRoleInstanceService.class);
            // list datanode
            List<ClusterServiceRoleInstanceEntity> datanodes = roleInstanceService.lambdaQuery()
                    .eq(ClusterServiceRoleInstanceEntity::getServiceId, hdfsEcCommand.getServiceInstanceId())
                    .eq(ClusterServiceRoleInstanceEntity::getServiceRoleName, "DataNode")
                    .list();
            TreeSet<String> list =
                    datanodes.stream().map(e -> e.getHostname()).collect(Collectors.toCollection(TreeSet::new));
            ProcessUtils.hdfsEcMethond(hdfsEcCommand.getServiceInstanceId(), roleInstanceService, list, "whitelist",
                    "NameNode");
        } else {
            unhandled(msg);
        }
    }
    
}
