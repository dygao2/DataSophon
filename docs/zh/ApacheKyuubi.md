# ApacheKyuubi

## 概述
`
ApacheKyuubi是一个分布式多租户网关,支持Spark,Flink,Hive等计算引擎，
依赖Kyuubi我们可以更方便的对数据湖组件进行集成.
`
## 连接器说明
`
目前默认对spark做了集成,如果需要对其他引擎或者数据湖做集成可以参考:https://kyuubi.readthedocs.io/en/v1.7.3/connector/index.html
`
## 服务认证
`
ApacheKyuubi对于认证支持多种方式,默认对Kerberos做了集成,只需要在安装时打开相关选项即可，如果
需要集成其他认证模式可以参考:https://kyuubi.readthedocs.io/en/v1.7.3/security/index.html
`
## 权限集成
`
在使用Spark引擎时我们可以借助ApacheKyuubi提供的RangerAuth插件使用现有的hive权限策略实现统一的权限
管理,目前在集成时没有对这部分做集成(集成的方式是SparkExtension需要改动Spark的相关配置)，需要使用权限
可以参考:https://kyuubi.readthedocs.io/en/v1.7.3/security/authorization/spark/index.html
`
## 简单使用说明

### 这里以Spark引擎为示例:

#### HA连接：
```
beeline -u 'jdbc:hive2://zkhost:2181/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=kyuubi_ns;principal=kyuubi/_HOST@HADOOP.COM'
```
#### 指定服务器IP连接
```
beeline -u 'jdbc:hive2://serverhost:10009/;principal=kyuubi/_HOST@HADOOP.COM' -nuserName
```
##### 连接说明
```
我们在集成时默认是以HA的方式集成的,ApacheKyuubi高可用需要借助Zookeeper,因此这里的zkhost:2181是指我们的zk集群信息,serviceDiscoveryMode说明
使用zk做服务发现,zooKeeperNamespace是zk的path信息，principal是在开启了Kerberos认证时需要指定的用户身份信息(注意这里的票据信息是固定的即Server端配置的信息，
_HOST是通配)
```

#### Sql查询
```
#查询方面与Hive beeline 没有区别两者等价,语法方面可以参考对应的Spark版本,如下执行show tables语句(这里删除打印的其他日志)
0: jdbc:hive2://192.168.163.127:2181/> show tables;
+-----------+------------+--------------+
| database  | tableName  | isTemporary  |
+-----------+------------+--------------+
| default   | my_table3  | false        |
| default   | my_table4  | false        |
+-----------+------------+--------------+
2 rows selected (3.875 seconds)

#select查询
0: jdbc:hive2://192.168.163.127:2181/> select 1 as col1;
+-------+
| col1  |
+-------+
| 1     |
+-------+
```

## 其他
```
这里只列举简单的说明与基本使用方式,ApacheKyuubi的功能远不止于此,更详细的操作请参考官方文档:https://kyuubi.readthedocs.io/en/v1.7.3
```

