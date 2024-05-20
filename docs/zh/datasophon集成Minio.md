### 1、构建minio压缩包

下载minio安装包：[https://dl.min.io/server/minio/release/linux-amd64/minio](https://dl.min.io/server/minio/release/linux-amd64/minio)

```shell
mkdir /opt/soft/tmp/minio-8.4.3
cd /opt/soft/tmp/minio-8.4.3
# 将Minio安装包放到当前目录
mkdir bin
mkdir etc
touch ./bin/start.sh
touch ./bin/stop.sh
touch ./bin/status.sh
```

创建好的编排目录格式如下：

```shell
-bin
	-start.sh
	-stop.sh
	-status.sh
-ect
-minio
```

编写 stop.sh 和 status.sh

```shell
#!/bin/bash

echo "Stopping minio"

pid=`ps -ef | grep 'minio server' | grep -v grep | awk '{print $2}'`

if [ -n "$pid" ]

then

kill -9 $pid

fi

echo "Stop Success!"
```

```shell
#!/bin/bash

echo "Checking Minio Status"

# 使用ps命令查找Minio进程
pid=$(ps -ef | grep 'minio server' | grep -v grep | awk '{print $2}')

if [ -n "$pid" ]; then
    echo "Minio is running with PID $pid"
    exit 0
else
    echo "Minio is not running"
    exit 1
fi
```

制作minio安装包

```shell
cd /opt/soft/tmp
tar czf minio-8.4.3.tar.gz minio-8.4.3
md5sum minio-8.4.3.tar.gz
echo '8f766b89b11cbc15b46b9f620a20780f' > minio-8.4.3.tar.gz.md5
```

将安装包拷贝到各worker节点对应目录

```shell
cp ./minio-8.4.3.tar.gz ./minio-8.4.3.tar.gz.md5 /opt/datasophon/DDP/packages/
```

### 2、创建minio配置文件

```shell
cd /opt/apps/datasophon/datasophon-manager-1.1.2/conf/meta/DDP-1.1.2
mkdir MINIO
cd MINIO
touch service_ddl.json
```

```shell
{
  "name": "MINIO",
  "label": "MINIO",
  "description": "s3对象存储",
  "version": "8.4.3",
  "sortNum": 22,
  "dependencies": [],
  "packageName": "minio-8.4.3.tar.gz",
  "decompressPackageName": "minio-8.4.3",
  "roles": [
    {
      "name": "MinioService",
      "label": "MinioService",
      "roleType": "master",
      "cardinality": "1+",
      "sortNum": 1,
      "logFile": "minio.log",
      "jmxPort": 11111,
      "startRunner": {
        "timeout": "60",
        "program": "bin/start.sh",
        "args": []
      },
      "stopRunner": {
        "timeout": "60",
        "program": "bin/stop.sh",
        "args": []
      },
      "statusRunner": {
        "timeout": "60",
        "program": "bin/status.sh",
        "args": []
      },
      "externalLink": {
        "name": "minio Ui",
        "label": "minio Ui",
        "url": "http://${host}:${consolePort}"
      }
    }
  ],
  "configWriter": {
    "generators": [
      {
        "filename": "start.sh",
        "configFormat": "custom",
        "outputDirectory": "bin",
        "templateName": "minio-run.flt",
        "includeParams": [
          "MINIO_ACCESS_KEY",
          "MINIO_SECRET_KEY",
          "dataPaths",
          "apiPort",
          "consolePort"
        ]
      }
    ]
  },
  "parameters": [
    {
      "name": "MINIO_ACCESS_KEY",
      "label": "用户名",
      "description": "用户名，长度最小是5个字符",
      "required": true,
      "configType": "map",
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "minio"
    },
    {
      "name": "MINIO_SECRET_KEY",
      "label": "密码",
      "description": "密码不能设置过于简单，不然minio会启动失败，长度最小是8个字符",
      "required": true,
      "configType": "map",
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "Jd2019@123"
    },
    {
      "name": "dataPaths",
      "label": "集群配置文件目录",
      "description": "集群配置文件目录，必须根据指定格式将各部署节点配置上，按空格分隔",
      "configType": "map",
      "required": true,
      "separator": " ",
      "type": "multiple",
      "value": [
        "http://{host}:{apiPort}/data/minio/data"
      ],
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": ""
    },
    {
      "name": "apiPort",
      "label": "api访问端口",
      "description": "api访问端口",
      "required": true,
      "configType": "map",
      "type": "input",
      "value": "9000",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "9000"
    },
    {
      "name": "consolePort",
      "label": "UI访问端口",
      "description": "UI访问端口",
      "required": true,
      "configType": "map",
      "type": "input",
      "value": "9001",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "9001"
    }
  ]
}
```

各worker几点创建minio-run.flt文件

```shell
cd /opt/datasophon/datasophon-worker/conf/templates
touch minio-run.flt
```

```shell
#!/bin/bash

# 设置MinIO的配置参数
export MINIO_ROOT_USER=${MINIO_ACCESS_KEY}
export MINIO_ROOT_PASSWORD=${MINIO_SECRET_KEY}

export MINIO_PROMETHEUS_AUTH_TYPE=public   #加入这行环境变量，“public”表示Prometheus访问minio集群可以不通过身份验证

/opt/datasophon/minio/minio server --config-dir /opt/datasophon/minio/etc \
        --address "0.0.0.0:${apiPort}" --console-address ":${consolePort}" \
        ${dataPaths} > /opt/datasophon/minio/minio.log 2>&1 &
```

### 3、重启datasophon

各节点worker重启

```shell
sh /opt/datasophon/datasophon-worker/bin/datasophon-worker.sh restart worker
```

主节点重启api

```shell
sh /opt/apps/datasophon/datasophon-manager-1.1.2/bin/datasophon-api.sh restart api
```

此时可以看到mysql元数据库中 t_ddh_frame_service 和 t_ddh_frame_service_role 两个表已经添加了minio的元数据。

### 4、安装

安装配置样例

![image](https://github.com/datavane/datasophon/assets/62798940/b7ca4c46-fcb8-4c8b-b195-e2e3d32f00c2)

注意配置文件目录data文件夹必须是空的！！！

### 5、监控

```shell
vim /opt/datasophon/prometheus/prometheus.yml
# 新增配置
  - job_name: minio_job
    metrics_path: /minio/prometheus/metrics
    scheme: http
    static_configs:
    - targets: ['192.168.1.54:9000','192.168.1.55:9000','192.168.1.56:9000']
```

重启prometheus

### 6、grafana

导入模板 [https://grafana.com/grafana/dashboards/12063](https://grafana.com/grafana/dashboards/12063)
datasophon mysql表 t_ddh_cluster_service_dashboard 新增图标链接

![image](https://github.com/datavane/datasophon/assets/62798940/95067756-41b4-428d-aeb6-b4923411c314)
