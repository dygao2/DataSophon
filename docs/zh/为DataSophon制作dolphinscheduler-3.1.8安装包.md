# 为DataSophon制作dolphinscheduler-3.1.8安装包

### DataSophon修改datasophopn-manager中conf/meat/DDP-1.2.0/DS/service_ddl.json，修改以下参数

```
"version": "3.1.8",
"packageName": "dolphinscheduler-3.1.8.tar.gz",
"decompressPackageName": "dolphinscheduler-3.1.8",
```

### 下载apache-dolphinscheduler-3.1.8-bin.tar.gz包，在服务器中解压缩

```shell
tar -xvf ./apache-dolphinscheduler-3.1.8-bin.tar.gz
```

### 修改文件名称，主要是要与上面decompressPackageName一致

```shell
mv apache-dolphinscheduler-3.1.8-bin dolphinscheduler-3.1.8
```

### 增加jmx文件夹

```shell
cp jmx dolphinscheduler-3.1.8
```

### 修改以下脚本的启动命令使jmx生效

./dolphinscheduler-3.1.8/alert-server/bin/start.sh

./dolphinscheduler-3.1.8/api-server/bin/start.sh

./dolphinscheduler-3.1.8/master-server/bin/start.sh

./dolphinscheduler-3.1.8/worker-server/bin/start.sh

```主要是JAVA_OPTS中添加了jmx内容
JAVA_OPTS=${JAVA_OPTS:-"-server -javaagent:$BIN_DIR/../../jmx/jmx_prometheus_javaagent-0.16.1.jar=12359:$BIN_DIR/../../jmx/prometheus_config.yml  -Duser.timezone=${SPRING_JACKSON_TIME_ZONE} -Xms1g -Xmx1g -Xmn512m -XX:+PrintGCDetails -Xloggc:gc.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=dump.hprof"}
```

### jmx的端口号需要和service_ddl.json中的jmx端口号一致

* api-server：12356
* master-server：12357
* worker-server：12358
* alert-server：12359

### 修改dolphinscheduler-3.1.8/bin/dolphinscheduler-daemon.sh脚本，在几乎使最下方的地方$state == "STOP"的地方增加一行exit 1

```shell
(status)
    get_server_running_status
    if [[ $state == "STOP" ]]; then
      #  font color - red
      state="[ \033[1;31m $state \033[0m ]"
      #增加一行，使得DataSophon执行脚本时可以有返回值判断状态
      exit 1
    else
      # font color - green
      state="[ \033[1;32m $state \033[0m ]"
    fi
    echo -e "$command  $state"
    ;;

  (*)
    echo $usage
    exit 1
    ;;
```

### 增加mysql依赖包，将mysql8依赖包放入到每个组件的lib中

参考ds的说明https://github.com/apache/dolphinscheduler/blob/3.1.8-release/docs/docs/zh/guide/howto/datasource-setting.md

### 打压缩包

```shell
tar -zcvf dolphinscheduler-3.1.8.tar.gz dolphinscheduler-3.1.8
```

### 生成md5文件

```shell
md5sum dolphinscheduler-3.1.8.tar.gz > dolphinscheduler-3.1.8.tar.gz.md5
```

### 将dolphinscheduler-3.1.8.tar.gz和dolphinscheduler-3.1.8.tar.gz.md5上传到DataSophon的安装包中

```shell
cp ./dolphinscheduler-3.1.8.tar.gz /opt/datasophon/DDP/packages/
cp ./dolphinscheduler-3.1.8.tar.gz.md5 /opt/datasophon/DDP/packages/
```

