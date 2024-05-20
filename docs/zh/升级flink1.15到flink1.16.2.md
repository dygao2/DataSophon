### 1、构建压缩包

下载flink官方包 flink-1.16.2-bin-scala_2.12.tgz

```shell
tar -zxvf flink-1.16.2-bin-scala_2.12.tgz
tar czf flink-1.16.2.tar.gz flink-1.16.2

# 默认支持hudi
cp ./hudi-flink1.16-bundle-0.13.0.jar /flink-1.16.2/lib

md5sum flink-1.16.2.tar.gz
echo '8d6c243ebc9bf58d3ee3e45e5c6509f4' > flink-1.16.2.tar.gz.md5
cp ./flink-1.16.2.tar.gz ./flink-1.16.2.tar.gz.md5 /opt/datasophon/DDP/packages/
```

### 2、修改service_ddl.json

```shell
vim /opt/apps/datasophon-manager-1.2.0/conf/meta/DDP-1.2.0/FLINK/service_ddl.json
```

```shell
{
  "name": "FLINK",
  "label": "Flink",
  "description": "实时计算引擎",
  "version": "1.16.2",
  "sortNum": 6,
  "dependencies":[],
  "packageName": "flink-1.16.2.tar.gz",
  "decompressPackageName": "flink-1.16.2",
  "runAs":"root",
  "roles": [
    {
      "name": "FlinkClient",
      "label": "FlinkClient",
      "roleType": "client",
      "cardinality": "1+",
      "logFile": "logs/flink.log"
    }
  ],
  "configWriter": {
    "generators": [
      {
        "filename": "flink-conf.yaml",
        "configFormat": "custom",
        "templateName": "properties3.ftl",
        "outputDirectory": "conf",
        "includeParams": [
          "jobmanager.memory.heap.size",
          "taskmanager.memory.flink.size",
          "high-availability",
          "high-availability.storageDir",
          "high-availability.zookeeper.quorum",
          "high-availability.zookeeper.client.acl",
          "high-availability.zookeeper.path.root",
          "custom.flink.conf.yaml",
          "classloader.check-leaked-classloader"
        ]
      }
    ]
  },
  "parameters": [
    {
      "name": "jobmanager.memory.heap.size",
      "label": "jobmanager堆内存大小",
      "description": "",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "1600m"
    },
    {
      "name": "taskmanager.memory.flink.size",
      "label": "taskmanager堆内存大小",
      "description": "",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "1280m"
    },
    {
      "name": "enableJMHA",
      "label": "开启JobManager高可用",
      "description": "",
      "required": true,
      "type": "switch",
      "value": false,
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": false
    },
    {
      "name": "high-availability",
      "label": "使用zookeeper搭建高可用",
      "description": "使用zookeeper搭建高可用",
      "configWithHA": true,
      "required": false,
      "type": "input",
      "value": "zookeeper",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": "zookeeper"
    },{
      "name": "high-availability.storageDir",
      "label": "元数据存储HDFS目录",
      "description": "存储JobManager的元数据到HDFS",
      "configWithHA": true,
      "required": false,
      "type": "input",
      "value": "hdfs://nameservice1/flink/ha/",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": "hdfs://nameservice1/flink/ha/"
    },{
      "name": "high-availability.zookeeper.quorum",
      "label": "ZK集群地址",
      "description": "配置ZK集群地址",
      "configWithHA": true,
      "required": false,
      "type": "input",
      "value": "${zkUrls}",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": ""
    },
    {
      "name": "high-availability.zookeeper.path.root",
      "label": "ZK元数据目录",
      "description": "配置ZK元数据目录",
      "configWithHA": true,
      "required": false,
      "type": "input",
      "value": "/flink",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": "/flink"
    },
    {
      "name": "high-availability.zookeeper.client.acl",
      "label": "high-availability.zookeeper.client.acl",
      "description": "默认是 open，如果zookeeper security启用了更改成creator",
      "configWithHA": true,
      "required": false,
      "type": "input",
      "value": "open",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": "open"
    },
    {
      "name": "custom.flink.conf.yaml",
      "label": "自定义配置flink-conf.yaml",
      "description": "自定义配置",
      "configType": "custom",
      "required": false,
      "type": "multipleWithKey",
      "value": [],
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": ""
    },
    {
      "name": "classloader.check-leaked-classloader",
      "label": "禁用classloader.check",
      "description": "禁用classloader.check",
      "required": true,
      "type": "switch",
      "value": false,
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": false
    }
  ]
}
```

### 3、修改环境变量

```shell
vim /etc/profile.d/datasophon-env.sh
export FLINK_HOME=/opt/datasophon/flink-1.16.2
export HADOOP_CLASSPATH=`hadoop classpath`
source /etc/profile.d/datasophon-env.sh
```

各节点同样操作

### 4、重启

各节点worker重启

```shell
sh /opt/datasophon/datasophon-worker/bin/datasophon-worker.sh restart worker
```

主节点重启api

```shell
sh /opt/apps/datasophon-manager-1.2.0/bin/datasophon-api.sh restart api
```

### 5、测试

```shell
flink run -d -t yarn-per-job $FLINK_HOME/examples/streaming/WordCount.jar
```

```shell
flink run-application -t yarn-application $FLINK_HOME/examples/streaming/TopSpeedWindowing.jar
```

