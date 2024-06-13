export JAVA_HOME=/usr/local/jdk1.8.0_333
CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
export JAVA_HOME CLASSPATH

export KYUUBI_HOME=/opt/datasophon/kyuubi
export SPARK_HOME=/opt/datasophon/spark3
export PYSPARK_ALLOW_INSECURE_GATEWAY=1
export HIVE_HOME=/opt/datasophon/hive
export KAFKA_HOME=/opt/datasophon/kafka
export HBASE_HOME=/opt/datasophon/hbase
export HBASE_PID_PATH_MK=/opt/datasophon/hbase/pid
export FLINK_HOME=/opt/datasophon/flink
export HADOOP_HOME=/opt/datasophon/hadoop
export HADOOP_CONF_DIR=/opt/datasophon/hadoop/etc/hadoop
export PATH=$PATH:$JAVA_HOME/bin:$SPARK_HOME/bin:$HADOOP_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$KAFKA_HOME/bin:$HBASE_HOME/bin
export HADOOP_CLASSPATH=`hadoop classpath`

