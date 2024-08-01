#!/bin/sh
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
shift

echo "Begin $startStop $command......"

source /etc/profile

SCRIPT="$0"
# SCRIPT may be an arbitrarily deep series of symlinks. Loop until we have the concrete path.
while [ -h "$SCRIPT" ] ; do
  ls=`ls -ld "$SCRIPT"`
  # Drop everything prior to ->
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    SCRIPT="$link"
  else
    SCRIPT=`dirname "$SCRIPT"`/"$link"
  fi
done

# some Java parameters
JAVA=`which java 2>/dev/null`
if [[ $JAVA_HOME != "" ]]; then
    JAVA=$JAVA_HOME/bin/java
fi
if test -z "$JAVA"; then
    echo "No java found in the PATH. Please set JAVA_HOME."
    exit 1
fi

BIN_DIR=`dirname "$SCRIPT"`/..
BIN_DIR=`cd "$BIN_DIR"; pwd`
export DDH_HOME=$BIN_DIR

export JAVA_HOME=$JAVA_HOME
#export JAVA_HOME=/opt/soft/jdk
export HOSTNAME=`hostname`

export DDH_PID_DIR=$DDH_HOME/pid
export DDH_LOG_DIR=$DDH_HOME/logs
export DDH_CONF_DIR=$DDH_HOME/conf
export DDH_LIB_JARS=$DDH_HOME/lib/*

export DDH_OPTS="-server $DDH_OPTS"
export STOP_TIMEOUT=5

if [ ! -d "$DDH_LOG_DIR" ]; then
  mkdir $DDH_LOG_DIR
fi

log=$DDH_LOG_DIR/$command-$HOSTNAME.out
pid=$DDH_PID_DIR/$command.pid

cd $DDH_HOME

if [ "$command" = "api" ]; then
  CLASS=com.datasophon.api.DataSophonApplicationServer
  JMX="-javaagent:$DDH_HOME/jmx/jmx_prometheus_javaagent-0.16.1.jar=8586:$DDH_HOME/jmx/jmx_exporter_config.yaml"
  HEAP_OPTS="-Xms1g -Xmx1g -Xmn512m"
  export DDH_OPTS="$HEAP_OPTS $DDH_OPTS $JMX"
else
  echo "Error: No command named \`$command' was found."
  exit 1
fi

case $startStop in
  (start)
    [ -w "$DDH_PID_DIR" ] ||  mkdir -p "$DDH_PID_DIR"

    if [ -f $pid ]; then
      if kill -0 `cat $pid` > /dev/null 2>&1; then
        echo $command running as process `cat $pid`.  Stop it first.
        exit 1
      fi
    fi

    echo starting $command, logging to $log

    exec_command="$DDH_OPTS -Dspring.profiles.active=config -classpath $DDH_CONF_DIR:$DDH_LIB_JARS $CLASS"

    echo "nohup $JAVA $exec_command > $log 2>&1 &"
    nohup $JAVA $exec_command > $log 2>&1 &
    echo $! > $pid
    ;;

  (stop)
      if [ -f $pid ]; then
        TARGET_PID=`cat $pid`
        if kill -0 $TARGET_PID > /dev/null 2>&1; then
          echo stopping $command
          kill $TARGET_PID
          sleep $STOP_TIMEOUT
          if kill -0 $TARGET_PID > /dev/null 2>&1; then
            echo "$command did not stop gracefully after $STOP_TIMEOUT seconds: killing with kill -9"
            kill -9 $TARGET_PID
          fi
        else
          echo no $command to stop
        fi
        rm -f $pid
      else
        echo no $command to stop
      fi
      ;;
  (restart)
      if [ -f $pid ]; then
        TARGET_PID=`cat $pid`
        if kill -0 $TARGET_PID > /dev/null 2>&1; then
          echo stopping $command
          kill $TARGET_PID
          sleep $STOP_TIMEOUT
          if kill -0 $TARGET_PID > /dev/null 2>&1; then
            echo "$command did not stop gracefully after $STOP_TIMEOUT seconds: killing with kill -9"
            kill -9 $TARGET_PID
          fi
        else
          echo no $command to stop
        fi
        rm -f $pid
      else
        echo no $command to stop
      fi
      sleep 2s
      [ -w "$DDH_PID_DIR" ] ||  mkdir -p "$DDH_PID_DIR"
      if [ -f $pid ]; then
          if kill -0 `cat $pid` > /dev/null 2>&1; then
            echo $command running as process `cat $pid`.  Stop it first.
            exit 1
          fi
      fi
      echo starting $command, logging to $log

      exec_command="$DDH_OPTS -Dspring.profiles.active=config -classpath $DDH_CONF_DIR:$DDH_LIB_JARS $CLASS"

      echo "nohup $JAVA $exec_command > $log 2>&1 &"
      nohup $JAVA $exec_command > $log 2>&1 &
      echo $! > $pid
      ;;
  (*)
    echo $usage
    exit 1
    ;;

esac

echo "End $startStop $command."