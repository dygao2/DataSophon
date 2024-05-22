# Kerberos增加用户

1. 添加用户

```shell
#添加lzy系统用户
[root@master bin]# adduser lzy
#登录kadmin添加lzy/master@HADOOP.COM用户，并导出keytab
[root@master bin]# kadmin.local
Authenticating as principal kylin/admin@HADOOP.COM with password.
kadmin.local:  add
add_policy     add_principal  addpol         addprinc
kadmin.local:  addprinc lzy/master@HADOOP.COM
WARNING: no policy specified for lzy/master@HADOOP.COM; defaulting to no policy
Enter password for principal "lzy/master@HADOOP.COM":
Re-enter password for principal "lzy/master@HADOOP.COM":
Principal "lzy/master@HADOOP.COM" created.
kadmin.local:  ktadd -k  /root/lzy.keytab -norandkey lzy/master@HADOOP.COM
Entry for principal lzy/master@HADOOP.COM with kvno 1, encryption type aes256-cts-hmac-sha1-96 added to keytab WRFILE:/root/lzy.keytab.
Entry for principal lzy/master@HADOOP.COM with kvno 1, encryption type aes128-cts-hmac-sha1-96 added to keytab WRFILE:/root/lzy.keytab.
Entry for principal lzy/master@HADOOP.COM with kvno 1, encryption type des3-cbc-sha1 added to keytab WRFILE:/root/lzy.keytab.
Entry for principal lzy/master@HADOOP.COM with kvno 1, encryption type arcfour-hmac added to keytab WRFILE:/root/lzy.keytab.
Entry for principal lzy/master@HADOOP.COM with kvno 1, encryption type des-hmac-sha1 added to keytab WRFILE:/root/lzy.keytab.
Entry for principal lzy/master@HADOOP.COM with kvno 1, encryption type des-cbc-md5 added to keytab WRFILE:/root/lzy.keytab.
```

2. hdfs授权

需要启用访问控制列表dfs.namenode.acls.enabled

```
#查看hdfs的权限
[root@master bin]# kinit -kt /etc/security/keytabs/hdfs.keytab hdfs/master@HADOOP.COM
[root@master bin]# hadoop fs -getfacl /
# file: /
# owner: hdfs
# group: supergroup
user::rwx
group::r-x
mask::rwx
other::r-x
#设置权限，-R是递归授权，rwx是权限，最后设置路径
[root@master bin]# hadoop fs -setfacl -R -m user:lzy:rwx /
```

3. hive和impala

用hive管理员登录

```shell
[root@master ~]# kinit -kt /etc/security/keytabs/hive.keytab hive/master@HADOOP.COM
[root@master ~]# hive
hive> show roles;
FAILED: SemanticException The current builtin authorization in Hive is incomplete and disabled.
hive> set hive.security.authorization.task.factory = org.apache.hadoop.hive.ql.parse.authorization.HiveAuthorizationTaskFactoryImpl;
OK
admin
public
Time taken: 1.36 seconds, Fetched: 2 row(s)
hive> grant role admin to user lzy;
hive> exit;

#验证授权是否成功
[root@master ~]# kinit -kt /root/lzy.keytab lzy/master@HADOOP.COM
[root@master bin]# hive
hive> show databases;
OK
default
Time taken: 0.685 seconds, Fetched: 1 row(s)
```

2. hbase

```shell
 [root@master ~]# kinit -kt /etc/security/keytabs/hbase.keytab hbase/master@HADOOP.COM
[root@master ~]# hbase shell
hbase(main):001:0> grant 'lzy','RWXCA'
0 row(s) in 0.6650 seconds

hbase(main):002:0> exit
[root@master ~]# kinit -kt /root/lzy.keytab lzy/master@HADOOP.COM
[root@master ~]# hbase shell
hbase(main):001:0> list
TABLE
TEXT
1 row(s) in 0.1890 seconds

=> ["TEXT"]
hbase(main):002:0> create 'Student','StuInfo','Grades'
0 row(s) in 4.7490 seconds

=> Hbase::Table - Student
hbase(main):003:0> list
TABLE
Student
TEXT
2 row(s) in 0.0210 seconds

=> ["Student", "TEXT"]
hbase(main):004:0> exit
```

3. kafka

```shell
#增加kylin用户
./kafka-configs.sh --zookeeper work02:2181/kafka --alter --add-config 'SCRAM-SHA-256=[password=123456],SCRAM-SHA-512=[password=123456]' --entity-type users --entity-name lzy
#修改kafka_client_jaas.conf
KafkaClient {
com.sun.security.auth.module.Krb5LoginModule required
useTicketCache=true;
};

Client {
com.sun.security.auth.module.Krb5LoginModule required
useKeyTab=true
keyTab="D:\company\kerberos\master\keytabs\lzy.keytab"
storeKey=true
useTicketCache=false
principal="lzy/master@HADOOP.COM"
serviceName=kafka;
};
```

