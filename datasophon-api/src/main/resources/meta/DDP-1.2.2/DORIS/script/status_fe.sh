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

curdir=`dirname "$0"`
curdir=`cd "$curdir"; pwd`
PID_DIR=`cd "$curdir"; pwd`
pid=$PID_DIR/fe.pid

function get_json(){
  echo "${1//\"/}" | sed "s/.*$2:\([^,}]*\).*/\1/"
}
status(){
  if [ -f $pid ]; then
    ARGET_PID=`cat $pid`
    echo "pid is $ARGET_PID"
    kill -0 $ARGET_PID
    if [ $? -eq 0 ]
    then
      # 发送GET请求到指定的URL
      response=$(curl -s  http://localhost:18030/api/bootstrap)
      # 检查返回值是否为200
      code=$(get_json "${response}" "code")
      if [ $code -eq 0 ]; then
          echo "http request success, return value is：$response"
          echo "FE is OK"
      else
          echo "http request failed, return value is：$response"
          echo "$command  is not ready"
          exit 1
      fi
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
case $startStop in
  (status)
	  status
	;;
  (*)
    echo $usage
    exit 1
    ;;
esac


echo "End $startStop $command."
