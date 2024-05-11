export JAVA_HOME=/data/apps/jdk1.8.0_341
CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
export JAVA_HOME CLASSPATH

export KYUUBI_HOME=/data/apps/kyuubi
export SPARK_HOME=/data/apps/spark
export PYSPARK_ALLOW_INSECURE_GATEWAY=1
export HIVE_HOME=/data/apps/hive
export KAFKA_HOME=/data/apps/kafka
export HBASE_HOME=/data/apps/hbase
export HBASE_PID_PATH_MK=/data/apps/hbase/pid
export FLINK_HOME=/data/apps/flink
export HADOOP_HOME=/data/apps/hadoop
export HADOOP_CONF_DIR=/data/apps/hadoop/etc/hadoop
export PATH=$PATH:$JAVA_HOME/bin:$SPARK_HOME/bin:$HADOOP_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$KAFKA_HOME/bin:$HBASE_HOME/bin
export HADOOP_CLASSPATH=`hadoop classpath`

