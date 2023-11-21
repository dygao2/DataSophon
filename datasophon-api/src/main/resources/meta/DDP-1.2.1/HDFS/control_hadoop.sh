#!/bin/bash
#
#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

usage="Usage: start.sh (start|stop|restart) <command> "

# if no args specified, show usage
if [ $# -le 1 ]; then
  echo $usage
  exit 1
fi
startStop=$1
shift
command=$1

SH_DIR=`dirname $0`
echo $SH_DIR
ident=$SH_DIR/ident.id
export LOG_DIR=$SH_DIR/logs
export PID_DIR=$SH_DIR/pid
pid=$PID_DIR/hadoop-root-$command.pid

if [[ "$command" = "namenode"  ||  "$command" = "datanode" || "$command" = "secondarynamenode" ||  "$command" = "journalnode" || "$command" = "zkfc" ]]; then
   cmd=$SH_DIR/bin/hdfs
elif [[ "$command" = "resourcemanager" || "$command" = "nodemanager" ]]; then
   cmd=$SH_DIR/bin/yarn
elif [[ "$command" = "historyserver" ]]; then
   cmd=$SH_DIR/bin/mapred
else
  echo "Error: No command named \'$command' was found."
  exit 1
fi

start(){
	echo "execute $cmd --daemon start $command"
	$cmd --daemon start $command
	if [ $? -eq 0 ]
    then
		echo "$command start success"
		if [ $command = "namenode" ]
	  then
		  echo "true" > $ident
		fi
	else
		echo "$command start failed"
		exit 1
	fi
}
stop(){
	$cmd --daemon stop $command
	if [ $? -eq 0 ]
    then
		echo "$command stop success"
	else
		echo "$command stop failed"
		exit 1
	fi
}
status(){
  if [ -f $pid ]; then
    ARGET_PID=`cat $pid`
    kill -0 $ARGET_PID
    if [ $? -eq 0 ]
    then
      echo "$command is  running "
    else
      echo "$command  is not running"
      exit 1
    fi
  else
    echo "$command  pid file is not exists"
    exit 1
	fi
}
restart(){
	stop
	sleep 10
	start
}
case $startStop in
  (start)
    start
    ;;
  (stop)
    stop
      ;;
  (status)
	  status
	;;
  (restart)
	  restart
      ;;
  (*)
    echo $usage
    exit 1
    ;;
esac


echo "End $startStop $command."