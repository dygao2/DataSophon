### 1、上传安装包

将编译好的安装包hbase-2.5.6.tar.gz和md5文件hbase-2.5.6.tar.gz.md5放到安装包目录/opt/datasophon/DDP/packages下

```shell
scp hbase-2.5.6.tar.gz hbase-2.5.6.tar.gz.md5 /opt/datasophon/DDP/packages
```

### 2、修改源码HBASE的service_ddl.json，重新打包api包

代码路径是datasophon\datasophon-api\src\main\resources\meta\DDP-1.2.0\HBASE\service_ddl.json

```shell
{
  "name": "HBASE",
  "label": "HBase",
  "description": "分布式列式海量存储数据库",
  "version": "2.5.6",
  "sortNum": 8,
  "dependencies":["HDFS"],
  "packageName": "hbase-2.5.6.tar.gz",
  "decompressPackageName": "hbase-2.5.6",
  "runAs":"root",
  "roles": [
    {
      "name": "HbaseMaster",
      "label": "HbaseMaster",
      "roleType": "master",
      "runAs": {
        "user": "hbase",
        "group": "hadoop"
      },
      "cardinality": "1+",
      "logFile": "logs/hbase-hbase-master-${host}.log",
      "jmxPort": 16100,
      "startRunner": {
        "timeout": "60",
        "program": "bin/hbase-daemon.sh",
        "args": [
          "start",
          "master"
        ]
      },
      "stopRunner": {
        "timeout": "600",
        "program": "bin/hbase-daemon.sh",
        "args": [
          "stop",
          "master"
        ]
      },
      "statusRunner": {
        "timeout": "60",
        "program": "bin/hbase-daemon.sh",
        "args": [
          "status",
          "master"
        ]
      },
      "restartRunner": {
        "timeout": "60",
        "program": "bin/hbase-daemon.sh",
        "args": [
          "restart",
          "master"
        ]
      },
      "externalLink": {
        "name": "HbaseMaster Ui",
        "label": "HbaseMaster Ui",
        "url": "http://${host}:16010"
      }
    },
    {
      "name": "RegionServer",
      "label": "RegionServer",
      "roleType": "worker",
      "runAs": {
        "user": "hbase",
        "group": "hadoop"
      },
      "cardinality": "1+",
      "logFile": "logs/hbase-hbase-regionserver-${host}.log",
      "jmxPort": 16101,
      "startRunner": {
        "timeout": "60",
        "program": "bin/hbase-daemon.sh",
        "args": [
          "start",
          "regionserver"
        ]
      },
      "stopRunner": {
        "timeout": "600",
        "program": "bin/hbase-daemon.sh",
        "args": [
          "stop",
          "regionserver"
        ]
      },
      "statusRunner": {
        "timeout": "60",
        "program": "bin/hbase-daemon.sh",
        "args": [
          "status","regionserver"
        ]
      },
      "restartRunner": {
        "timeout": "60",
        "program": "bin/hbase-daemon.sh",
        "args": [
          "restart",
          "regionserver"
      ]
    }
    }
  ],
  "configWriter": {
    "generators": [
      {
        "filename": "hbase-site.xml",
        "configFormat": "xml",
        "outputDirectory": "conf",
        "includeParams": [
          "hbase.cluster.distributed",
          "hbase.rootdir",
          "hbase.zookeeper.quorum",
          "hbase.wal.provider",
          "hbase.regionserver.wal.codec",
          "phoenix.schema.isNamespaceMappingEnabled",
          "hbase.io.compress.snappy.codec",
          "hbase.table.sanity.checks",
          "hbase.unsafe.stream.capability.enforce",
          "hbase.security.authentication",
          "hbase.rpc.engine",
          "hbase.coprocessor.region.classes",
          "hbase.master.kerberos.principal",
          "hbase.master.keytab.file",
          "hbase.regionserver.kerberos.principal",
          "hbase.regionserver.keytab.file",
          "custom.hbase.site.xml"
        ]
      },
      {
        "filename": "install.properties",
        "configFormat": "custom",
        "outputDirectory": "ranger-hbase-plugin",
        "templateName": "ranger-hbase.ftl",
        "includeParams": [
          "rangerAdminUrl",
          "hbaseHome"
        ]
      },
      {
        "filename": "zk-jaas.conf",
        "configFormat": "custom",
        "outputDirectory": "conf",
        "templateName": "zk-jaas.ftl",
        "includeParams": [
          "hostname"
        ]
      },
      {
        "filename": "hbase-env.sh",
        "configFormat": "custom",
        "outputDirectory": "conf",
        "templateName": "hbase-env.ftl",
        "includeParams": [
          "hbasePidDir",
          "hbaseSecurity",
          "custom.hbase.env.sh"
        ]
      }
    ]
  },
  "parameters": [
    {
      "name": "hbase.cluster.distributed",
      "label": "hbase.cluster.distributed",
      "description": "hbase.cluster.distributed",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "true"
    },
    {
      "name": "hbase.rootdir",
      "label": "hbase.rootdir",
      "description": "hbase.rootdir",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "/hbase"
    },
    {
      "name": "hbase.zookeeper.quorum",
      "label": "hbase.zookeeper.quorum",
      "description": "hbase.zookeeper.quorum",
      "required": true,
      "type": "input",
      "value": "${zkHostsUrl}",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": ""
    },
    {
      "name": "hbase.wal.provider",
      "label": "hbase.wal.provider",
      "description": "hbase.wal.provider",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "filesystem"
    },
    {
      "name": "hbase.regionserver.wal.codec",
      "label": "hbase.regionserver.wal.codec",
      "description": "hbase.regionserver.wal.codec",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "org.apache.hadoop.hbase.regionserver.wal.IndexedWALEditCodec"
    },
    {
      "name": "phoenix.schema.isNamespaceMappingEnabled",
      "label": "phoenix.schema.isNamespaceMappingEnabled",
      "description": "phoenix.schema.isNamespaceMappingEnabled",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "true"
    },
    {
      "name": "hbase.io.compress.snappy.codec",
      "label": "hbase.io.compress.snappy.codec",
      "description": "hbase.io.compress.snappy.codec",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "org.apache.hadoop.hbase.io.compress.xerial.SnappyCodec"
    },
    {
      "name": "hbase.table.sanity.checks",
      "label": "hbase.table.sanity.checks",
      "description": "hbase.table.sanity.checks",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "false"
    },
    {
      "name": "hbase.unsafe.stream.capability.enforce",
      "label": "hbase.unsafe.stream.capability.enforce",
      "description": "hbase.unsafe.stream.capability.enforce",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "false"
    },
    {
      "name": "hbasePidDir",
      "label": "HBase PID DIR",
      "description": "",
      "configType": "map",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "${HBASE_HOME}/pid"
    },
    {
      "name": "hbase.security.authorization",
      "label": "启用Ranger权限",
      "description": "",
      "configType": "permission",
      "required": false,
      "type": "switch",
      "value": true,
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": true
    },
    {
      "name": "hbase.coprocessor.master.classes",
      "label": "RangerHbaseMaster认证类",
      "description": "",
      "configType": "permission",
      "required": false,
      "type": "input",
      "value": "org.apache.ranger.authorization.hbase.RangerAuthorizationCoprocessor",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": "org.apache.ranger.authorization.hbase.RangerAuthorizationCoprocessor"
    },
    {
      "name": "hbase.coprocessor.region.classes",
      "label": "RangerHbaseRegion认证类",
      "description": "",
      "configType": "permission",
      "required": false,
      "type": "input",
      "value": "org.apache.ranger.authorization.hbase.RangerAuthorizationCoprocessor",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": "org.apache.ranger.authorization.hbase.RangerAuthorizationCoprocessor"
    },
    {
      "name": "rangerAdminUrl",
      "label": "Ranger访问地址",
      "description": "",
      "required": false,
      "configType": "map",
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": ""
    },
    {
      "name": "enableKerberos",
      "label": "开启Kerberos认证",
      "description": "开启Kerberos认证",
      "required": false,
      "type": "switch",
      "value": false,
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": false
    },
    {
      "name": "hbase.security.authentication",
      "label": "hbase.security.authentication",
      "description": "",
      "configWithKerberos": true,
      "required": false,
      "type": "input",
      "value": "kerberos",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": "kerberos"
    },
    {
      "name": "hbase.rpc.engine",
      "label": "HBase rpc安全通信",
      "description": "",
      "configWithKerberos": true,
      "required": false,
      "type": "input",
      "value": "org.apache.hadoop.hbase.ipc.SecureRpcEngine",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": "org.apache.hadoop.hbase.ipc.SecureRpcEngine"
    },
    {
      "name": "hbase.coprocessor.region.classes",
      "label": "hbase.coprocessor.region.classes",
      "description": "",
      "configWithKerberos": true,
      "required": false,
      "type": "input",
      "value": "org.apache.hadoop.hbase.security.token.TokenProvider",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": "org.apache.hadoop.hbase.security.token.TokenProvider"
    },
    {
      "name": "hbase.master.kerberos.principal",
      "label": "HMaster kerberos安全凭据",
      "description": "",
      "configWithKerberos": true,
      "required": false,
      "type": "input",
      "value": "hbase/_HOST@HADOOP.COM",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": "hbase/_HOST@HADOOP.COM"
    },
    {
      "name": "hbase.master.keytab.file",
      "label": "HMasterkeytab文件位置",
      "description": "",
      "configWithKerberos": true,
      "required": false,
      "type": "input",
      "value": "/etc/security/keytab/hbase.keytab",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": "/etc/security/keytab/hbase.keytab"
    },
    {
      "name": "hbase.regionserver.kerberos.principal",
      "label": "RegionServer kerberos安全凭据",
      "description": "",
      "configWithKerberos": true,
      "required": false,
      "type": "input",
      "value": "hbase/_HOST@HADOOP.COM",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": "hbase/_HOST@HADOOP.COM"
    },
    {
      "name": "hbase.regionserver.keytab.file",
      "label": "RegionServer keytab文件位置",
      "description": "",
      "configWithKerberos": true,
      "required": false,
      "type": "input",
      "value": "/etc/security/keytab/hbase.keytab",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": "/etc/security/keytab/hbase.keytab"
    },
    {
      "name": "hostname",
      "label": "hostname",
      "description": "",
      "configType": "map",
      "configWithKerberos": true,
      "required": false,
      "type": "input",
      "value": "${host}",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": "${host}"
    },
    {
      "name": "hbaseSecurity",
      "label": "hbaseSecurity",
      "description": "",
      "configType": "map",
      "configWithKerberos": true,
      "required": false,
      "type": "input",
      "value": "-Djava.security.auth.login.config=${HBASE_HOME}/conf/zk-jaas.conf",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": "-Djava.security.auth.login.config=${HBASE_HOME}/conf/zk-jaas.conf"
    },
    {
      "name": "hbaseHome",
      "label": "HBASE_HOME",
      "description": "HBase的安装目录",
      "required": true,
      "configType": "map",
      "type": "input",
      "value": "${HBASE_HOME}",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "${HBASE_HOME}"
    },
    {
      "name": "custom.hbase.site.xml",
      "label": "自定义配置hbase-site.xml",
      "description": "自定义配置",
      "configType": "custom",
      "required": false,
      "type": "multipleWithKey",
      "value":[],
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": ""
    },
    {
      "name": "custom.hbase.env.sh",
      "label": "自定义配置hbase-env.sh",
      "description": "自定义配置",
      "configType": "custom",
      "required": false,
      "type": "multipleWithKey",
      "value": [],
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": ""
    }
  ]
}
```

### 3、修改源码的datasophon-env.sh和hbase-env.ftl，重新打包worker包

修改datasophon-env.sh,代码路径是datasophon\datasophon-worker\src\main\resources\script\datasophon-env.sh

```shell
export JAVA_HOME=/usr/local/jdk1.8.0_333
CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
export JAVA_HOME CLASSPATH

export SPARK_HOME=/opt/datasophon/spark-3.1.3
export PYSPARK_ALLOW_INSECURE_GATEWAY=1
export HIVE_HOME=/opt/datasophon/hive-3.1.0

export KAFKA_HOME=/opt/datasophon/kafka-2.4.1
export HBASE_HOME=/opt/datasophon/hbase-2.5.6
export HBASE_PID_PATH_MK=/opt/datasophon/hbase-2.5.6/pid
export FLINK_HOME=/opt/datasophon/flink-1.15.2
export HADOOP_HOME=/opt/datasophon/hadoop-3.3.3
export HADOOP_CONF_DIR=/opt/datasophon/hadoop-3.3.3/etc/hadoop
export PATH=$PATH:$JAVA_HOME/bin:$SPARK_HOME/bin:$HADOOP_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$KAFKA_HOME/bin:$HBASE_HOME/bin

```

修改hbase-env.ftl，代码路径是datasophon\datasophon-worker\src\main\resources\templates\hbase-env.ftl

```shell
#!/usr/bin/env bash
#
#/**
# * Licensed to the Apache Software Foundation (ASF) under one
# * or more contributor license agreements.  See the NOTICE file
# * distributed with this work for additional information
# * regarding copyright ownership.  The ASF licenses this file
# * to you under the Apache License, Version 2.0 (the
# * "License"); you may not use this file except in compliance
# * with the License.  You may obtain a copy of the License at
# *
# *     http://www.apache.org/licenses/LICENSE-2.0
# *
# * Unless required by applicable law or agreed to in writing, software
# * distributed under the License is distributed on an "AS IS" BASIS,
# * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# * See the License for the specific language governing permissions and
# * limitations under the License.
# */

# Set environment variables here.

# This script sets variables multiple times over the course of starting an hbase process,
# so try to keep things idempotent unless you want to take an even deeper look
# into the startup scripts (bin/hbase, etc.)

# The java implementation to use.  Java 1.8+ required.
export JAVA_HOME=/usr/local/jdk1.8.0_333

# Extra Java CLASSPATH elements.  Optional.
# export HBASE_CLASSPATH=

# The maximum amount of heap to use. Default is left to JVM default.
# export HBASE_HEAPSIZE=1G

# Uncomment below if you intend to use off heap cache. For example, to allocate 8G of 
# offheap, set the value to "8G".
# export HBASE_OFFHEAPSIZE=1G

# Extra Java runtime options.
# Below are what we set by default.  May only work with SUN JVM.
# For more on why as well as other possible settings,
# see http://hbase.apache.org/book.html#performance
	
export HBASE_OPTS="$HBASE_OPTS -XX:+UseConcMarkSweepGC <#if hbaseSecurity??>${hbaseSecurity}</#if>"

# Uncomment one of the below three options to enable java garbage collection logging for the server-side processes.

# This enables basic gc logging to the .out file.
# export SERVER_GC_OPTS="-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps"

# This enables basic gc logging to its own file.
# If FILE-PATH is not replaced, the log file(.gc) would still be generated in the HBASE_LOG_DIR .
# export SERVER_GC_OPTS="-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:"

# This enables basic GC logging to its own file with automatic log rolling. Only applies to jdk 1.6.0_34+ and 1.7.0_2+.
# If FILE-PATH is not replaced, the log file(.gc) would still be generated in the HBASE_LOG_DIR .
# export SERVER_GC_OPTS="-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc: -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=1 -XX:GCLogFileSize=512M"

# Uncomment one of the below three options to enable java garbage collection logging for the client processes.

# This enables basic gc logging to the .out file.
# export CLIENT_GC_OPTS="-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps"

# This enables basic gc logging to its own file.
# If FILE-PATH is not replaced, the log file(.gc) would still be generated in the HBASE_LOG_DIR .
# export CLIENT_GC_OPTS="-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:"

# This enables basic GC logging to its own file with automatic log rolling. Only applies to jdk 1.6.0_34+ and 1.7.0_2+.
# If FILE-PATH is not replaced, the log file(.gc) would still be generated in the HBASE_LOG_DIR .
# export CLIENT_GC_OPTS="-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:  -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=1 -XX:GCLogFileSize=512M"

export HBASE_JMX_BASE="-Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"

export HBASE_MASTER_OPTS="$HBASE_MASTER_OPTS $HBASE_JMX_BASE  -javaagent:$HBASE_HOME/jmx/jmx_prometheus_javaagent-0.16.1.jar=16100:$HBASE_HOME/jmx/hbase_jmx_config.yaml"

export HBASE_REGIONSERVER_OPTS="$HBASE_REGIONSERVER_OPTS $HBASE_JMX_BASE -javaagent:$HBASE_HOME/jmx/jmx_prometheus_javaagent-0.16.1.jar=16101:$HBASE_HOME/jmx/hbase_jmx_config.yaml"
# See the package documentation for org.apache.hadoop.hbase.io.hfile for other configurations
# needed setting up off-heap block caching. 

# Uncomment and adjust to enable JMX exporting
# See jmxremote.password and jmxremote.access in $JRE_HOME/lib/management to configure remote password access.
# More details at: http://java.sun.com/javase/6/docs/technotes/guides/management/agent.html
# NOTE: HBase provides an alternative JMX implementation to fix the random ports issue, please see JMX
# section in HBase Reference Guide for instructions.

# export HBASE_JMX_BASE="-Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
# export HBASE_MASTER_OPTS="$HBASE_MASTER_OPTS $HBASE_JMX_BASE -Dcom.sun.management.jmxremote.port=10101"
# export HBASE_REGIONSERVER_OPTS="$HBASE_REGIONSERVER_OPTS $HBASE_JMX_BASE -Dcom.sun.management.jmxremote.port=10102"
# export HBASE_THRIFT_OPTS="$HBASE_THRIFT_OPTS $HBASE_JMX_BASE -Dcom.sun.management.jmxremote.port=10103"
# export HBASE_ZOOKEEPER_OPTS="$HBASE_ZOOKEEPER_OPTS $HBASE_JMX_BASE -Dcom.sun.management.jmxremote.port=10104"
# export HBASE_REST_OPTS="$HBASE_REST_OPTS $HBASE_JMX_BASE -Dcom.sun.management.jmxremote.port=10105"

# File naming hosts on which HRegionServers will run.  $HBASE_HOME/conf/regionservers by default.


# Uncomment and adjust to keep all the Region Server pages mapped to be memory resident
#HBASE_REGIONSERVER_MLOCK=true
#HBASE_REGIONSERVER_UID="hbase"

# File naming hosts on which backup HMaster will run.  $HBASE_HOME/conf/backup-masters by default.


# Extra ssh options.  Empty by default.
# export HBASE_SSH_OPTS="-o ConnectTimeout=1 -o SendEnv=HBASE_CONF_DIR"

# Where log files are stored.  $HBASE_HOME/logs by default.


# Enable remote JDWP debugging of major HBase processes. Meant for Core Developers 
# export HBASE_MASTER_OPTS="$HBASE_MASTER_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8070"
# export HBASE_REGIONSERVER_OPTS="$HBASE_REGIONSERVER_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8071"
# export HBASE_THRIFT_OPTS="$HBASE_THRIFT_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8072"
# export HBASE_ZOOKEEPER_OPTS="$HBASE_ZOOKEEPER_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8073"
# export HBASE_REST_OPTS="$HBASE_REST_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8074"

# A string representing this instance of hbase. $USER by default.
# export HBASE_IDENT_STRING=$USER

# The scheduling priority for daemon processes.  See 'man nice'.
# export HBASE_NICENESS=10

# The directory where pid files are stored. /tmp by default.
# export HBASE_PID_DIR=/var/hadoop/pids
export HBASE_PID_DIR=${hbasePidDir}
# Seconds to sleep between slave commands.  Unset by default.  This
# can be useful in large clusters, where, e.g., slave rsyncs can
# otherwise arrive faster than the master can service them.
# export HBASE_SLAVE_SLEEP=0.1

# Tell HBase whether it should manage it's own instance of ZooKeeper or not.
# export HBASE_MANAGES_ZK=true

# The default log rolling policy is RFA, where the log file is rolled as per the size defined for the 
# RFA appender. Please refer to the log4j.properties file to see more details on this appender.
# In case one needs to do log rolling on a date change, one should set the environment property
# HBASE_ROOT_LOGGER to DRFA".
# For example:
# HBASE_ROOT_LOGGER=INFO,DRFA
# The reason for changing default to RFA is to avoid the boundary case of filling out disk space as 
# DRFA doesn't put any cap on the log size. Please refer to HBase-5655 for more context.

# Tell HBase whether it should include Hadoop's lib when start up,
# the default value is false,means that includes Hadoop's lib.
# export HBASE_DISABLE_HADOOP_CLASSPATH_LOOKUP="true"
<#list itemList as item>
export ${item.name}=${item.value}
</#list>
```

### 4、修改service源码，重新打包api包

修改ZkServerHandlerStrategy.java,代码路径是 datasophon\datasophon-service\src\main\java\com\datasophon\api\strategy\ZkServerHandlerStrategy.java,修改hamdler方法

```java
@Override
public void handler(Integer clusterId, List<String> hosts) {
    // 保存zkUrls到全局变量
    Map<String, String> globalVariables = GlobalVariables.get(clusterId);
    String join = String.join(":2181,", hosts);
    String zkUrls = join + ":2181";
    ProcessUtils.generateClusterVariable(globalVariables, clusterId, "${zkUrls}", zkUrls);
    // 保存zkHostsUrl到全局变量
    String zkHostsUrl=String.join(",", hosts);
    ProcessUtils.generateClusterVariable(globalVariables, clusterId, "${zkHostsUrl}", zkHostsUrl);
}
```

### 5、重启DataSophonApplicationServer服务

关闭原来的DataSophonApplicationServer服务

```
datasophon-manager-1.2.0/bin/datasophon-api.sh stop api
```

删除datasophon-manager-1.2.0目录

```
rm  -rf  datasophon-manager-1.2.0/
```

将datasophon-manager-1.2.0.tar.gz重新上传和解压

```
tar -zxvf  datasophon-manager-1.2.0.tar.gz 
```

重新启动DataSophonApplicationServer服务

```
datasophon-manager-1.2.0/bin/datasophon-api.sh start api
```

### 6、 部署hbase服务

启动后DataSophonApplicationServer，在ui界面点击添加服务，选择hbase安装部署即可
