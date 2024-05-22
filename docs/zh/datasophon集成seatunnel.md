### 1、构建安装包

下载安装包
[https://www.apache.org/dyn/closer.lua/seatunnel/2.3.3/apache-seatunnel-2.3.3-bin.tar.gz](https://www.apache.org/dyn/closer.lua/seatunnel/2.3.3/apache-seatunnel-2.3.3-bin.tar.gz)

```shell
tar -zxvf apache-seatunnel-2.3.3-bin.tar.gz
mv apache-seatunnel-2.3.3-bin seatunnel-2.3.3

cd seatunnel-2.3.3
# 修改config/plugin_config文件只保留需要的连接器后安装连接器
sh bin/install-plugin.sh 2.3.3

# bin目录新增status脚本
touch ./bin/seatunnel-status.sh
chmod 755 ./bin/seatunnel-status.sh

# 打包
tar czf seatunnel-2.3.3.tar.gz seatunnel-2.3.3
md5sum seatunnel-2.3.3.tar.gz
echo '27c821b7d7ead2f99a4db2d7503fc2b5' > seatunnel-2.3.3.tar.gz.md5
```

```shell
#!/bin/bash

# 检查 SeaTunnelServer 进程是否存在
if ps -ef | grep -q "[S]eaTunnelServer"; then
    echo "SeaTunnelServer 进程正在运行."
    exit 0
else
    echo "SeaTunnelServer 进程未找到."
    exit 1
fi
```

### 2、元数据文件

api节点新增：

```shell
cd /opt/apps/datasophon-manager-1.2.0/conf/meta/DDP-1.2.0
mkdir SEATUNNEL
cd SEATUNNEL
touch service_ddl.json
```

```shell
{
  "name": "SEATUNNEL",
  "label": "Seatunnel",
  "description": "数据同步工具",
  "version": "2.3.3",
  "sortNum": 32,
  "dependencies": [],
  "packageName": "seatunnel-2.3.3.tar.gz",
  "decompressPackageName": "seatunnel-2.3.3",
  "roles": [
    {
      "name": "SeatunnelServer",
      "label": "SeatunnelServer",
      "roleType": "worker",
      "cardinality": "1+",
      "logFile": "logs/seatunnel-engine-server.log",
      "startRunner": {
        "timeout": "60",
        "program": "bin/seatunnel-cluster.sh",
        "args": [
          "-d"
        ]
      },
      "stopRunner": {
        "timeout": "60",
        "program": "bin/stop-seatunnel-cluster.sh",
        "args": []
      },
      "statusRunner": {
        "timeout": "60",
        "program": "bin/seatunnel-status.sh",
        "args": []
      }
    }
  ],
  "configWriter": {
    "generators": [
      {
        "filename": "seatunnel.yaml",
        "configFormat": "custom",
        "templateName": "seatunnel-yml.flt",
        "outputDirectory": "config",
        "includeParams": [
          "backupCount",
          "custom.checkPoint"
        ]
      },
      {
        "filename": "hazelcast.yaml",
        "configFormat": "custom",
        "templateName": "hazelcast.flt",
        "outputDirectory": "config",
        "includeParams": [
          "hosts"
        ]
      },
      {
        "filename": "hazelcast-client.yaml",
        "configFormat": "custom",
        "templateName": "hazelcast-client.flt",
        "outputDirectory": "config",
        "includeParams": [
          "hosts"
        ]
      }
    ]
  },
  "parameters": [
    {
      "name": "backupCount",
      "label": "同步备份的数量",
      "description": "同步备份的数量",
      "required": true,
      "type": "input",
      "configType": "map",
      "value": "1",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "1"
    },
    {
      "name": "custom.checkPoint",
      "label": "自定义配置检查点存储",
      "description": "自定义配置",
      "configType": "custom",
      "required": false,
      "type": "multipleWithKey",
      "value": [{"namespace":"/tmp/seatunnel/checkpoint_snapshot"},{"storage.type":"hdfs"},{"fs.defaultFS":"file:///tmp/"}],
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": [{"namespace":"/tmp/seatunnel/checkpoint_snapshot"},{"storage.type":"hdfs"},{"fs.defaultFS":"file:///tmp/"}]
    },
    {
      "name": "hosts",
      "label": "集群节点ip",
      "description": "集群节点ip",
      "required": true,
      "type": "multiple",
      "separator": ",",
      "value": [],
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": ""
    }
  ]
}

```

各worker节点新增：

```shell
cd /opt/datasophon/datasophon-worker/conf/templates
touch seatunnel-yml.flt
touch hazelcast.flt
touch hazelcast-client.flt
```

```shell
hazelcast:
  cluster-name: seatunnel
  network:
    rest-api:
      enabled: true
      endpoint-groups:
        CLUSTER_WRITE:
          enabled: true
        DATA:
          enabled: true
    join:
      tcp-ip:
        enabled: true
        member-list:
<#list itemList as item>
    <#list item.value?split(",") as host>
          - ${host}
    </#list>
</#list>
    port:
      auto-increment: false
      port: 5801
  properties:
    hazelcast.invocation.max.retry.count: 20
    hazelcast.tcp.join.port.try.count: 30
    hazelcast.logging.type: log4j2
    hazelcast.operation.generic.thread.count: 50
```

```shell
hazelcast-client:
  cluster-name: seatunnel
  properties:
    hazelcast.logging.type: log4j2
  network:
    cluster-members:
<#list itemList as item>
    <#list item.value?split(",") as host>
      - ${host}:5801
    </#list>
</#list>
```

```shell
seatunnel:
  engine:
    backup-count: ${backupCount}
    queue-type: blockingqueue
    print-execution-info-interval: 60
    print-job-metrics-info-interval: 60
    slot-service:
      dynamic-slot: true
    checkpoint:
      interval: 10000
      timeout: 60000
      max-concurrent: 1
      tolerable-failure: 2
      storage:
        type: hdfs
        max-retained: 3
        plugin-config:
          <#list itemList as item>
          ${item.name}: ${item.value}
          </#list>
```

### 3、重启

各节点worker重启

```shell
sh /opt/datasophon/datasophon-worker/bin/datasophon-worker.sh restart worker debug
```

主节点重启api

```shell
sh /opt/apps/datasophon-manager-1.2.0/bin/datasophon-api.sh restart api debug
```

### 4、页面配置样例

![image](https://github.com/datavane/datasophon/assets/62798940/e72af3f5-cbd2-41c4-9d30-988c3cfb36ee)
