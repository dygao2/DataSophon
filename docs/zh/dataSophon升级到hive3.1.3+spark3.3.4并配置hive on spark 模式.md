# 为DataSophon升级hive3.1.0到hive3.1.3,同时spark升级到3.3.4并集成配置hive on spark 模式

### 下载编译配置好的hive3.1.3安装包和spark3.3.4安装包（已放入百度云盘）修改以下参数

```
链接：https://pan.baidu.com/s/1Wpj8fDDuct8pwBh4IVcb_Q?pwd=soph 
提取码：soph
```

### 将hive3.1.3.tar.gz和hive3.1.3.tar.gz.md5上传到DataSophon的安装包中

```shell
cp ./hive3.1.3.tar.gz /opt/datasophon/DDP/packages/
cp ./hive3.1.3.tar.gz.md5 /opt/datasophon/DDP/packages/
```

### 将spark-3.3.4.tar.gz和spark-3.3.4.tar.gz.md5上传到DataSophon的安装包中

```shell
cp ./spark-3.3.4.tar.gz /opt/datasophon/DDP/packages/
cp ./spark-3.3.4.tar.gz.md5 /opt/datasophon/DDP/packages/
```

### hive3.1.3安装包是经过改动适配的，并在里面补充了相关兼容性jar来适配（hive on spark）模式，spark3.3.4.tar.gz安装包改动较小也可自行在网络下载

### DataSophon修改datasophopn-manager中conf/meat/DDP-1.2.1/HIVE/service_ddl.json，修改以下参数

```
"version": "3.1.3",
"packageName": "hive-3.1.3.tar.gz",
"decompressPackageName": "hive-3.1.3",
```

### DataSophon修改datasophopn-manager中conf/meat/DDP-1.2.1/SPARK3/service_ddl.json，修改以下参数

```
"version": "3.3.4",
"packageName": "spark-3.3.4.tar.gz",
"decompressPackageName": "spark-3.3.4",
```

### 在datasophon中删除已经安装的hive服务和spark服务

### 修改环境变量

```shell
vim /etc/profile.d/datasophon-env.sh
export SPARK_HOME=/opt/datasophon/spark-3.3.4
export HIVE_HOME=/opt/datasophon/hive-3.1.3
```

### 各节点worker重启

```shell
sh /opt/datasophon/datasophon-worker/bin/datasophon-worker.sh restart worker

sh bin/datasophon-worker.sh restart worker
```

### 主节点重启api

```shell
sh /opt/datasophon/datasophon-manager-1.2.1/bin/datasophon-api.sh restart api
```

### 在datasophon中安装的hive3.1.3服务和spark3.3.4服务

### 上传 spark Jar 包并更换引擎

```
所以当我们将任务提交到 Yarn 上进行调度时，可能会将该任务分配到其它节点，这就会导致任务无法正常运行，所以我们需要将 Spark 中的所有 Jar 包到 HDFS 上，并告知 Hive 其存储的位置,这样可以加快任务初始化的速度
这里使用纯净版的spark（spark-3.3.4-bin-without-hadoop）
下载地址 ： https://mirrors.tuna.tsinghua.edu.cn/apache/spark/spark-3.3.4/spark-3.3.4-bin-without-hadoop.tgz

hadoop fs -mkdir /spark-jars

将spark-3.3.4-bin-without-hadoop jars下面的全部jar包上传到HDFS上的新建目录上

hadoop fs -put ./jars/* /spark-jars
```

### 在 Hive 配置 Spark 参数

```shell 进入 Hive 的 conf 目录中，创建 配置Spark 配置文件，指定相关参数。
cd $HIVE_HOME/conf

vim spark-default.conf

添加如下配置内容：
spark.master                        yarn
spark.eventLog.enabled              true
spark.eventLog.dir                  hdfs://nameservice1/spark-logs
spark.executor.memory               4g
spark.driver.memory                 2g
spark.yarn.jars                     hdfs://nameservice1/spark-jars/*


配置文件创建完成后在 HDFS 上创建 Spark 的日志存储目录
hadoop fs -mkdir /spark-logs
```

### 在 Hive 的配置文件中指定 Spark jar 包的存放位置：

```在其中添加下列三项配置：
<!--Spark依赖位置-->
<property>
    <name>spark.yarn.jars</name>
    <value>hdfs://nameservice1/spark-jars/*</value>
</property>
  
<!--Hive执行引擎-->
<property>
    <name>hive.execution.engine</name>
    <value>spark</value>
</property>

<!--提交任务超时时间，单位ms-->
<property>
    <name>hive.spark.client.connect.timeout</name>
    <value>50000</value>
</property>

```

### 配置spark3.3.4，将hadoop相关的xml文件拷贝到spark-3.3.4的conf目录下

```shell 不想拷贝文件的话可以直接通过创建软链接的方式
cd  /opt/datasophon/spark-3.3.4/conf/
ln -s /opt/datasophon/hive-3.1.3/conf/hive-site.xml   hive-site.xml
ln -s /opt/datasophon/hadoop-3.3.3/etc/hadoop/yarn-site.xml  yarn-site.xml
ln -s /opt/datasophon/hadoop-3.3.3/etc/hadoop/core-site.xml  core-site.xml
ln -s /opt/datasophon/hadoop-3.3.3/etc/hadoop/hdfs-site.xml hdfs-site.xml
```

### 如果涉及到hdfs目录权限问题可以简单粗暴的将目录权限设置为777

```shell
hadoop fs -chmod -R 777 /spark-logs
hadoop fs -chmod -R 777 /spark-jars
```

