### 1、构建安装包

下载：
[https://dlcdn.apache.org/flume/1.11.0/apache-flume-1.11.0-bin.tar.gz](https://dlcdn.apache.org/flume/1.11.0/apache-flume-1.11.0-bin.tar.gz)

构建安装包

```shell
tar -zxvf apache-flume-1.11.0-bin.tar.gz
mv apache-flume-1.11.0-bin flume-1.11.0
tar czf flume-1.11.0.tar.gz flume-1.11.0
md5sum flume-1.11.0.tar.gz
echo '0a96bbd5dca673835503fa2ac5ba5335' > flume-1.11.0.tar.gz.md5
# 将安装包拷贝到各worker节点目录
cp ./flume-1.11.0.tar.gz ./flume-1.11.0.tar.gz.md5 /opt/datasophon/DDP/packages/
```

### 2、新增元数据文件

```shell
cd /opt/apps/datasophon-manager-1.2.0/conf/meta/DDP-1.2.0
mkdir FLUME
cd FLUME
vim service_ddl.json
```

service_ddl.json：

```shell
{
  "name": "FLUME",
  "label": "Flume",
  "description": "分布式的海量日志采集、聚合和传输的系统",
  "version": "1.11.0",
  "sortNum": 34,
  "dependencies":[],
  "packageName": "flume-1.11.0.tar.gz",
  "decompressPackageName": "flume-1.11.0",
  "runAs":"root",
  "roles": [
    {
      "name": "FlumeClient",
      "label": "FlumeClient",
      "roleType": "client",
      "cardinality": "1+",
      "logFile": "flume.log"
    }
  ],
  "configWriter": {
    "generators": []
  },
  "parameters": []
}
```

### 3、重启api

```shell
sh /opt/apps/datasophon-manager-1.2.0/bin/datasophon-api.sh restart api
```

### 4、安装测试

datasophon页面搭建flume服务

```shell
yum -y install nc
cd /opt/datasophon/flume-1.11.0/
vim netcat.conf
```

```shell
a1.sources = r1
a1.sinks = k1
a1.channels = c1

a1.sources.r1.type = netcat
a1.sources.r1.bind = localhost
a1.sources.r1.port = 44444

a1.sinks.k1.type = logger

a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000
a1.channels.c1.transactionCapacity = 100

a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```

```shell
# 开启flume监听任务
bin/flume-ng agent -n a1 -c conf/ -f ./netcat.conf -Dflume.root.logger=INFO,console
# nc发送数据
nc localhost 44444
# 查看日志 flume.log
```

