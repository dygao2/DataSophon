export JAVA_HOME=/usr/local/jdk1.8.0_333
CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
export JAVA_HOME CLASSPATH

export KYUUBI_HOME=/opt/datasophon/kyuubi-1.7.3
export SPARK_HOME=/opt/datasophon/spark-3.1.3
export PYSPARK_ALLOW_INSECURE_GATEWAY=1
export HIVE_HOME=/opt/datasophon/hive-3.1.0

export KAFKA_HOME=/opt/datasophon/kafka-2.4.1
export HBASE_HOME=/opt/datasophon/hbase-2.4.16
export FLINK_HOME=/opt/datasophon/flink-1.15.2
export HADOOP_HOME=/opt/datasophon/hadoop-3.3.3
export HADOOP_CONF_DIR=/opt/datasophon/hadoop-3.3.3/etc/hadoop
export PATH=$PATH:$JAVA_HOME/bin:$SPARK_HOME/bin:$HADOOP_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$KAFKA_HOME/bin:$HBASE_HOME/bin


