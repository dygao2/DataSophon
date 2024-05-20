## datasophon集成paimon

### Paimon：

#### 目前支持Flink  1.14及以上版本

#### 目前支持 Hive 3.1、2.3、2.2、2.1 和 2.1-cdh-6.3。支持 Hive Read 的 MR 和 Tez 执行引擎，以及 Hive Write 的 MR 执行引擎（beeline也不支持hive write）

#### 目前支持 Spark 3.4、3.3、3.2 和 3.1

快照仓库，根据自己需要选择集成的组件和版本：https://repository.apache.org/content/groups/snapshots/org/apache/paimon/

- paimon-flink-1.16-0.7-20231201.002224-11.jar
- paimon-hive-connector-3.1-0.7-20231201.002224-11.jar
- paimon-spark-3.2-0.7-20231201.002224-11.jar

### 当前Hadoop：hadoop-3.3.3

### Flink：flink-1.16.3集成paimon

### 解压flink-1.16.3安装包

- 

```shell
tar -zxvf flink-1.16.3-bin-scala_2.12.tgz（这里可以用原datasophon的1.15版本或者是参照官网的步骤定制需要的flink版本，如果用的原flink版本或者已经升级过，直接解压packages中的包即可，如果同时需要升级flink版本，注意保持和service_ddl.json中 decompressPackageName 一致）
mv flink-1.16.3-bin-scala_2.12.tgz flink-1.16.3
```

### 拷贝相关的包到lib目录

- 

```shell
cp paimon-flink-1.16-0.7-20231201.002224-11.jar  /opt/datasophon/flink-1.16.3/lib
cp flink-sql-connector-hive-3.1.2_2.12-1.16.2.jar  /opt/datasophon/flink-1.16.3/lib
****解决乱七八糟的类找不到的问题：cp /opt/datasophon/hadoop-3.3.3/share/hadoop/mapreduce/hadoop-mapreduce-client-core-3.3.3.jar /opt/datasophon/flink-1.16.3/lib
```

### 检查环境变量

```shell
cat /etc/profile.d/datasophon-env.sh
	xxxx
	...
	export FLINK_HOME=/opt/datasophon/flink-1.16.3
	export HADOOP_CLASSPATH=`hadoop classpath`
检查环境变量是否跟自己flink版本一致，如有修改最后记得source一下并分发到各节点

```

### 打包压缩并生成 md5

```shell
tar -czf  flink-1.16.3.tar.gz  flink-1.16.3
md5sum flink-1.16.3.tar.gz | awk '{print $1}' >flink-1.16.3.tar.gz.md5
```

### 重启

各节点worker重启

```shell
sh /opt/datasophon/datasophon-worker/bin/datasophon-worker.sh restart worker
```

主节点重启api

```shell
sh /opt/module/datasophon-manager-1.2.0/bin/datasophon-api.sh restart api
```

### 测试：以 Yarn-Session模式为例

```shell
vim /opt/datasophon/flink-1.16.3/conf/sql-client-init.sql
```

```shell
CREATE CATALOG fs_catalog WITH (
    'type' = 'paimon',
    'warehouse' = 'hdfs://ddp1:8020/paimon/fs'
);

CREATE CATALOG hive_catalog WITH (
    'type' = 'paimon',
    'metastore' = 'hive',
'uri' = 'thrift://ddp1:9083',
'hive-conf-dir' = '/opt/datasophon/hive/conf',
    'warehouse' = 'hdfs://ddp1:8020/paimon/hive'
);

use catalog fs_catalog;
SET 'sql-client.execution.result-mode' = 'tableau';
```

```shell
/opt/module/flink-1.17.0/bin/yarn-session.sh -d
/opt/module/flink-1.17.0/bin/sql-client.sh -s yarn-session
```

效果如下即为集成成功

![](C:\Users\Hasee\Desktop\paimon-flink.png)

![](C:\Users\Hasee\Desktop\paimon-catalogs.png)

### Hive：hive-3.1.0集成paimon

解压hive-3.1.0安装包

```shell
cp paimon-hive-connector-3.1-0.7-20231201.002224-11.jar /opt/datasophon/hive-3.1.0/auxlibs

（不推荐用add jar，MR 引擎运行 join 语句会报异常）
```

### 重新打包覆盖，并重启worker和manager api步骤同上

### 测试：

```sql
SET hive.metastore.warehouse.dir=hdfs://ddp1:8020/user/hive/warehouse;
CREATE TABLE test.test_paimon(
                       a INT COMMENT 'The a field',
                       b STRING COMMENT 'The b field'
)
    STORED BY 'org.apache.paimon.hive.PaimonStorageHandler';

DESC FORMATTED test.test_paimon;
INSERT INTO TABLE test.test_paimon(a,b) VALUES (666,'paimon集成hive成功');
SELECT *
FROM test.test_paimon;
```

效果如下即为成功：

![](C:\Users\Hasee\Desktop\paimon-hive.png)

![image-20231203185833490](C:\Users\Hasee\AppData\Roaming\Typora\typora-user-images\image-20231203185833490.png)

### Spark：spark-3.2.2集成paimon

```shell
cp paimon-spark-3.2-0.7-20231201.002224-11.jar /opt/datasophon/spark-3.2.2/jars 
```

剩余步骤同flink集成，不做赘述

注：启动spark-sql时，指定Catalog。切换到catalog后，Spark现有的表将无法直接访问，可以使用spark_catalog.${database_name}.${table_name}来访问Spark表。注册catalog可以启动时指定，也可以配置在spark-defaults.conf中

测试：

```shell
spark-sql \
    --conf spark.sql.catalog.fs=org.apache.paimon.spark.SparkCatalog \
    --conf spark.sql.catalog.fs.warehouse=hdfs://ddp1:8020/spark/paimon/fs
```

```shell
spark-sql \
    --conf spark.sql.catalog.hive=org.apache.paimon.spark.SparkCatalog \
    --conf spark.sql.catalog.hive.warehouse=hdfs://ddp1:8020/spark/paimon/hive \
    --conf spark.sql.catalog.hive.metastore=hive \
    --conf spark.sql.catalog.hive.uri=thrift://ddp1:9083
```

效果如下：

![image-20231203192751501](C:\Users\Hasee\AppData\Roaming\Typora\typora-user-images\image-20231203192751501.png)

### 集成paimon注意点：

如果使用的是 Hive3，请禁用 Hive ACID：

```sql
hive.strict.managed.tables=false
hive.create.as.insert.only=false
metastore.create.as.acid=false
```

使用hive Catalog通过alter table更改不兼容的列类型时，参见 HIVE-17832。需要配置

```sql
hive.metastore.disallow.inknown.col.type.changes=false
```

