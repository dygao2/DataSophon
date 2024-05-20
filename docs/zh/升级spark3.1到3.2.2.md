### 1、构建压缩包

下载官方包 spark-3.2.2-bin-hadoop3.2.tgz

```shell
tar -zxvf spark-3.2.2-bin-hadoop3.2.tgz
mv spark-3.2.2-bin-hadoop3.2 spark-3.2.2

# 默认集成hudi
cp ./hudi-spark3.2-bundle_2.12-0.13.0.jar /spark-3.2.2/jars/
chown hadoop:hadoop /spark-3.2.2/jars/hudi-spark3.2-bundle_2.12-0.13.0.jar

tar czf spark-3.2.2.tar.gz spark-3.2.2
md5sum spark-3.2.2.tar.gz
echo 'eadd4bb2ce5d809ce4c8631f1e865252' > spark-3.2.2.tar.gz.md5
cp ./spark-3.2.2.tar.gz ./spark-3.2.2.tar.gz.md5 /opt/datasophon/DDP/packages/
```

### 2、修改servcie_ddl.json

```shell
{
  "name": "SPARK3",
  "label": "Spark3",
  "description": "分布式计算系统",
  "version": "3.2.2",
  "sortNum": 7,
  "dependencies":[],
  "packageName": "spark-3.2.2.tar.gz",
  "decompressPackageName": "spark-3.2.2",
  "roles": [
    {
      "name": "SparkClient3",
      "label": "SparkClient3",
      "roleType": "client",
      "cardinality": "1+",
      "logFile": "logs/hadoop-${user}-datanode-${host}.log"
    }
  ],
  "configWriter": {
    "generators": [
      {
        "filename": "spark-env.sh",
        "configFormat": "custom",
        "templateName": "spark-env.ftl",
        "outputDirectory": "conf",
        "includeParams": [
          "SPARK_DIST_CLASSPATH",
          "HADOOP_CONF_DIR",
          "YARN_CONF_DIR",
          "custom.spark.env.sh"
        ]
      },
      {
        "filename": "spark-defaults.conf",
        "configFormat": "properties2",
        "outputDirectory": "conf",
        "includeParams": [
          "custom.spark.defaults.conf"
        ]
      }
    ]
  },
  "parameters": [
    {
      "name": "SPARK_DIST_CLASSPATH",
      "label": "spark加载Classpath路径",
      "description": "",
      "required": true,
      "configType": "map",
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "$(${HADOOP_HOME}/bin/hadoop classpath)"
    },
    {
      "name": "HADOOP_CONF_DIR",
      "label": "Hadoop配置文件目录",
      "description": "",
      "configType": "map",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "${HADOOP_HOME}/etc/hadoop"
    },{
      "name": "YARN_CONF_DIR",
      "label": "Yarn配置文件目录",
      "description": "",
      "configType": "map",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "${HADOOP_HOME}/etc/hadoop"
    },
    {
      "name": "custom.spark.env.sh",
      "label": "自定义配置spark-env.sh",
      "description": "自定义配置spark-env.sh",
      "configType": "custom",
      "required": false,
      "type": "multipleWithKey",
      "value": [],
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": ""
    },
    {
      "name": "custom.spark.defaults.conf",
      "label": "自定义配置spark-defaults.conf",
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

### 3、修改环境变量

```shell
vim /etc/profile.d/datasophon-env.sh
export SPARK_HOME=/opt/datasophon/spark-3.2.2
```

各节点分发

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

单机：

```shell
sh /opt/datasophon/spark-3.2.2/bin/spark-submit --class org.apache.spark.examples.SparkPi /opt/datasophon/spark-3.2.2/examples/jars/spark-examples_2.12-3.2.2.jar 12
```

yarn:

```shell
su - hdfs
sh /opt/datasophon/spark-3.2.2/bin/spark-submit --master yarn --deploy-mode client --class org.apache.spark.examples.SparkPi /opt/datasophon/spark-3.2.2/examples/jars/spark-examples_2.12-3.2.2.jar 12
```

