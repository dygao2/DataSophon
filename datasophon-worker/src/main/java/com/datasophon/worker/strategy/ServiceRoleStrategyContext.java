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

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRoleStrategyContext {
    
    private static final Map<String, ServiceRoleStrategy> map = new ConcurrentHashMap<>();
    
    static {
        map.put("NameNode", new NameNodeHandlerStrategy("HDFS", "NameNode"));
        map.put("ZKFC", new ZKFCHandlerStrategy("HDFS", "ZKFC"));
        map.put("JournalNode", new JournalNodeHandlerStrategy("HDFS", "JournalNode"));
        map.put("DataNode", new DataNodeHandlerStrategy("HDFS", "DataNode"));
        map.put("ResourceManager", new ResourceManagerHandlerStrategy("YARN", "ResourceManager"));
        map.put("NodeManager", new NodeManagerHandlerStrategy("YARN", "NodeManager"));
        map.put("RangerAdmin", new RangerAdminHandlerStrategy("RANGER", "RangerAdmin"));
        map.put("HiveServer2", new HiveServer2HandlerStrategy("HIVE", "HiveServer2"));
        map.put("HbaseMaster", new HbaseHandlerStrategy("HBASE", "HbaseMaster"));
        map.put("RegionServer", new HbaseHandlerStrategy("HBASE", "RegionServer"));
        map.put("Krb5Kdc", new Krb5KdcHandlerStrategy("KERBEROS", "Krb5Kdc"));
        map.put("KAdmin", new KAdminHandlerStrategy("KERBEROS", "KAdmin"));
        map.put("SRFE", new FEHandlerStrategy("STARROCKS", "SRFE"));
        map.put("DorisFE", new FEHandlerStrategy("DORIS", "DorisFE"));
        map.put("DorisFEObserver", new FEObserverHandlerStrategy("DORIS", "DorisFEObserver"));
        map.put("ZkServer", new ZkServerHandlerStrategy("ZOOKEEPER", "ZkServer"));
        map.put("KafkaBroker", new KafkaHandlerStrategy("KAFKA", "KafkaBroker"));
        map.put("SRBE", new BEHandlerStrategy("STARROCKS", "SRBE"));
        map.put("DorisBE", new BEHandlerStrategy("DORIS", "DorisBE"));
        map.put("HistoryServer", new HistoryServerHandlerStrategy("YARN", "HistoryServer"));
        
        // TEZ Server service
        map.put("TezServer", new TezServerHandlerStrategy("TEZ", "TezServer"));
        // kyuubi
        map.put("KyuubiServer", new KyuubiServerHandlerStrategy("KYUUBI", "KyuubiServer"));
        // flink
        map.put("FlinkClient", new FlinkHandlerStrategy("FLINK", "FlinkClient"));
        
        // DolphinScheduler
        map.put("MasterServer", new DSMasterHandlerStrategy("DS", "MasterServer"));
    }
    
    public static ServiceRoleStrategy getServiceRoleHandler(String type) {
        if (StringUtils.isBlank(type)) {
            return null;
        }
        return map.get(type);
    }
}
