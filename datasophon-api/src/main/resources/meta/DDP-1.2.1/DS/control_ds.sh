#!/usr/bin/env bash
current_path=$(cd `dirname $0`;pwd)
command=$1
shift
option=$1
cd $current_path
state=""
function prepareEnv() {
    process_name=$1
    source /etc/profile
    if [ -n "$HADOOP_HOME" ]
        then
        echo "sync hive conf "
        cp $HADOOP_HOME/etc/hadoop/core-site.xml $current_path/$process_name/conf/
        cp $HADOOP_HOME/etc/hadoop/hdfs-site.xml $current_path/$process_name/conf/
    fi
}

function start() {

   process_name=$1
   prepareEnv $process_name
   execStatus $process_name
   if [ $state -eq 0 ]
   then
    echo "ERROR: $process_name is running"
    exit 1
   fi
   $current_path/bin/dolphinscheduler-daemon.sh start $process_name
}

function stop() {
   process_name=$1
   execStatus $process_name
   if [ $state -eq 1 ]
   then
    echo "ERROR: $process_name is not running"
    exit 1
   fi
   $current_path/bin/dolphinscheduler-daemon.sh stop $process_name
}

function status() {
  process_name=$1
  execStatus $process_name
  if [ $state -eq 1 ]
  then
    echo "INFO: $process_name is not running"
    exit 1
  elif [ $state -eq 0 ]
  then
    echo "INFO: $process_name is running"
    exit 0
  fi
}
function execStatus() {
  process_name=$1
  status=` $current_path/bin/dolphinscheduler-daemon.sh status $process_name | grep -v Begin | grep -v End | cut -d " " -f5`
  if [[ $status=="STOP"  ]]
  then
     state=1
  fi
  if [[ $status=="RUNNING"  ]]
  then
     state=0
  fi
}


function restart() {
    stop $1
    start $1
}



case $command in
start)
  start $option
  ;;
stop)
  stop $option
  ;;
status)
  status $option
  ;;
restart)
  restart $option
  ;;
esac
