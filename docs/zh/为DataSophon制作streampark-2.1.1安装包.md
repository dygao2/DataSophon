# 为DataSophon制作streampark-2.1.1安装包.md

## 下载并解压streampark 2.1.1安装包

```shell
tar -xzvf apache-streampark_2.12-2.1.1-incubating-bin.tar.gz
```

## 修改安装包目录名称

保持和service_ddl.json中 decompressPackageName 一致

```shell
mv apache-streampark_2.12-2.1.1-incubating-bin streampark-2.1.1
```

## 修改`streampark-2.1.1/bin/streampark.sh`文件

- 在DEFAULT_OPTS(原249行)中增加prometheus_javaagent配置，如下：

```shell
DEFAULT_OPTS="""
  -ea
  -server
  -javaagent:$APP_HOME/jmx/jmx_prometheus_javaagent-0.16.1.jar=10086:$APP_HOME/jmx/prometheus_config.yml
  -Xms1024m
  -Xmx1024m
  -Xmn256m
  -XX:NewSize=100m
  -XX:+UseConcMarkSweepGC
  -XX:CMSInitiatingOccupancyFraction=70
  -XX:ThreadStackSize=512
  -Xloggc:${APP_HOME}/logs/gc.log
  """
```

- 在start函数中，`local workspace=...略`(原380行)下一行，增加 `mkdir-p $workspace`，如下

```shell
local workspace=$(echo "$conf_streampark_workspace_local" | sed 's/#.*$//g')
mkdir -p $workspace
if [[ ! -d $workspace ]]; then
  echo_r "ERROR: streampark.workspace.local: \"$workspace\" is invalid path, Please reconfigure in application.yml"
  echo_r "NOTE: \"streampark.workspace.local\" Do not set under APP_HOME($APP_HOME). Set it to a secure directory outside of APP_HOME.  "
  exit 1;
fi
if [[ ! -w $workspace ]] || [[ ! -r $workspace ]]; then
    echo_r "ERROR: streampark.workspace.local: \"$workspace\" Permission denied! "
    exit 1;
fi
```

- 修改status函数(原582行)中增加`exit 1`,如下：

```shell
status() {
  # shellcheck disable=SC2155
  # shellcheck disable=SC2006
  local PID=$(get_pid)
  if [ $PID -eq 0 ]; then
    echo_r "StreamPark is not running"
    exit 1
  else
    echo_g "StreamPark is running pid is: $PID"
  fi
}
```

## 增加jmx文件夹

```shell
cp jmx streampark-2.1.1
```

## copy mysql8驱动包至lib目录

(streampark从某个版本后把mysql驱动包移除了)

```shell
cp mysql-connector-java-8.0.28.jar streampark-2.1.1/lib/
```

## copy streampark安装包内的 mysql-schema.sql 和 mysql-data.sql 脚本出来备用

```shell
cp streampark-2.1.1/script/schema/mysql-schema.sql ./streampark_mysql-schema.sql
cp streampark-2.1.1/script/data/mysql-data.sql ./streampark_mysql-data.sql
```

## 打压缩包并生成md5

```shell
tar -czf  streampark-2.1.1.tar.gz   streampark-2.1.1
md5sum streampark-2.1.1.tar.gz | awk '{print $1}' >streampark-2.1.1.tar.gz.md5
```

