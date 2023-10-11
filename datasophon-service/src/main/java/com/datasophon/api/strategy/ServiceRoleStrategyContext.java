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

package com.datasophon.api.strategy;

import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRoleStrategyContext {

    private static final Map<String, ServiceRoleStrategy> map = new ConcurrentHashMap<>();

    static {
        map.put("NameNode", new NameNodeHandlerStrategy());
        map.put("ResourceManager", new RMHandlerStrategy());
        map.put("HiveMetaStore", new HiveMetaStroreHandlerStrategy());
        map.put("HiveServer2", new HiveServer2HandlerStrategy());
        map.put("Grafana", new GrafanaHandlerStrategy());
        map.put("ZkServer", new ZkServerHandlerStrategy());
        map.put("HistoryServer", new HistoryServerHandlerStrategy());
        map.put("TimelineServer", new TSHandlerStrategy());
        map.put("TrinoCoordinator", new TrinoHandlerStrategy());
        map.put("JournalNode", new JournalNodeHandlerStrategy());
        map.put("ZKFC", new ZKFCHandlerStrategy());
        map.put("SRFE", new FEHandlerStartegy());
        map.put("DorisFE", new FEHandlerStartegy());
        map.put("DorisFEObserver", new FEObserverHandlerStartegy());
        map.put("SRBE", new BEHandlerStartegy());
        map.put("DorisBE", new BEHandlerStartegy());
        map.put("Krb5Kdc", new Krb5KdcHandlerStrategy());
        map.put("KAdmin", new KAdminHandlerStrategy());
        map.put("RangerAdmin", new RangerAdminHandlerStrategy());
        map.put("ElasticSearch", new ElasticSearchHandlerStrategy());
        map.put("Prometheus", new PrometheusHandlerStrategy());
        map.put("AlertManager", new AlertManagerHandlerStrategy());

        map.put("RANGER", new RangerAdminHandlerStrategy());
        map.put("ZOOKEEPER", new ZkServerHandlerStrategy());
        map.put("YARN", new RMHandlerStrategy());
        map.put("HDFS", new NameNodeHandlerStrategy());
        map.put("HIVE", new HiveServer2HandlerStrategy());
        map.put("KAFKA", new KafkaHandlerStrategy());
        map.put("HBASE", new HBaseHandlerStrategy());
        map.put("FLINK", new FlinkHandlerStrategy());
    }

    public static ServiceRoleStrategy getServiceRoleHandler(String type) {
        if (StringUtils.isBlank(type)) {
            return null;
        }
        return map.get(type);
    }
}
