# datasophon集成doris-manager

# 背景：

doris-manager由doris官方提供的，一站式doris管理运维平台。可提供doris的一键升级以及指标监控，此处集成doris-manager,便于doris的管理.

# Datasophon

## datasophon-api模块

新增DORISMANAGER配置文件

    #对于组件的集成，必须添加status脚本判断组件状态，因为在安装时会判断组件是否正在运行
    在datasophon-api/src/main/resources/meta/DDP-1.1.2创建目录DORISMANAGER
    在DORISMANAGER下创建配置文件：service_ddl.json和control_dorismanager.sh脚本

### service\_ddl.json

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/jP2lR4X1236Eq8g5/img/78ccb560-722a-447f-ac6f-689350d6fd6f.png)

doris-manager与doris-webUi的前世今生：

早期doris-manager-23.7版本时，webui是集成到了manager里面的，但是selectDb1.2.x系列之后， selectDb本身又自带了webui界面，为了防止manager的webui与selectDb的webui冲突，故在manager23.7之后， manager移除webui。(webui是在selectDb中而非doris当中， selectDb是doris的商业版)

由于个人原因，早期的manager用户还是希望webui与manager一起，故此处继续将doris-webui放到manager一起集成。

doris-manager此处为了通信安全漏洞问题，升级到当前最新了24.0.0。不同于 23.x 系列的历史版本的 SSH 互信方式，Doris Manager 24.0.0 版本管控使用 Agent 方式，Agent 和 Server 之间直接使用 HTTP 协议通信，可以结合 SSL 加密数据，保证安全性。服务的整体架构如下图所示

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/jP2lR4X1236Eq8g5/img/f7036fc7-705f-4685-9cea-290c2f1a7376.png)

在此处我们将doris的webui一并添加到其中，此处将manager整体分为3个角色

DorisManagerServer

DorisManagerAgent

DorisManagerWebui

其整体的配置文件如下

    {
      "name": "DORISMANAGER",
      "label": "DorisManager",
      "description": "一站式Doris集群管理工具",
      "version": "24.0.0",
      "sortNum": 21,
      "dependencies": [],
      "packageName": "doris-manager-24.0.0.tar.gz",
      "decompressPackageName": "doris-manager-24.0.0",
      "roles": [
        {
          "name": "DorisManagerServer",
          "label": "DorisManagerServer",
          "roleType": "master",
          "cardinality": "1",
          "logFile": "webserver/log/manager_info.log",
          "startRunner": {
            "timeout": "60",
            "program": "control_dorismanager.sh",
            "args": [
              "start",
              "webserver"
            ]
          },
          "stopRunner": {
            "timeout": "60",
            "program": "control_dorismanager.sh",
            "args": [
              "stop",
              "webserver"
            ]
          },
          "statusRunner": {
            "timeout": "60",
            "program": "control_dorismanager.sh",
            "args": [
              "status",
              "webserver"
            ]
          },
          "externalLink": {
            "name": "Doris-Manager UI",
            "label": "Doris-Manager UI",
            "url": "http://${host}:8004"
          }
        },
        {
          "name": "DorisManagerAgent",
          "label": "DorisManagerAgent",
          "roleType": "worker",
          "cardinality": "1+",
          "logFile": "agent/manager-agent/log/agent.log",
          "startRunner": {
            "timeout": "60",
            "program": "control_dorismanager.sh",
            "args": [
              "start",
              "agent"
            ]
          },
          "stopRunner": {
            "timeout": "60",
            "program": "control_dorismanager.sh",
            "args": [
              "stop",
              "agent"
            ]
          },
          "statusRunner": {
            "timeout": "60",
            "program": "control_dorismanager.sh",
            "args": [
              "status",
              "agent"
            ]
          }
        },
        {
          "name": "DorisManagerWebui",
          "label": "DorisManagerWebui",
          "roleType": "master",
          "cardinality": "1",
          "logFile": "webui/webui.out",
          "startRunner": {
            "timeout": "60",
            "program": "control_dorismanager.sh",
            "args": [
              "start",
              "webui"
            ]
          },
          "stopRunner": {
            "timeout": "60",
            "program": "control_dorismanager.sh",
            "args": [
              "stop",
              "webui"
            ]
          },
          "statusRunner": {
            "timeout": "60",
            "program": "control_dorismanager.sh",
            "args": [
              "status",
              "webui"
            ]
          },
          "externalLink": {
            "name": "Doris-ManagerWebui",
            "label": "Doris-ManagerWebui",
            "url": "http://${host}:${WEBUI_PORT}"
          }
        }
      ],
      "configWriter": {
        "generators": [
          {
            "filename": "manager.conf",
            "configFormat": "custom",
            "outputDirectory": "webserver/conf/",
            "templateName": "doris_managerserver.ftl",
            "includeParams": [
              "managerPort",
              "dbType",
              "dataPath",
              "dbHost",
              "dbPort",
              "dbUser",
              "dbPass",
              "dbDbname",
              "dbUrlSuffix",
              "HttpConnectTimeout",
              "HttpSocketTimeout",
              "ListenProtocol",
              "FeMinDiskSpaceForUpgrade",
              "BeMinDiskSpaceForUpgrade",
              "custom.manager.conf"
            ]
          },
          {
            "filename": "agent.yaml",
            "configFormat": "custom",
            "outputDirectory": "agent/manager-agent/conf",
            "templateName": "doris_manageragent.ftl",
            "includeParams": [
              "agentPort"
            ]
          },
          {
            "filename": "webui.conf",
            "configFormat": "custom",
            "outputDirectory": "webui/conf",
            "templateName": "webui.ftl",
            "includeParams": [
              "WEBUI_PORT",
              "QUERY_LIMIT",
              "JAVA_OPTS",
              "DB_TYPE",
              "DATA_PATH",
              "DB_HOST",
              "DB_PORT",
              "DB_USER",
              "DB_PASS",
              "DB_DBNAME"
            ]
          }
        ]
      },
      "parameters": [
        {
          "name": "managerPort",
          "label": "MANAGER端口",
          "description": "Doris Manager Web服务组件监听的端口",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "8004",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "8004"
        },
        {
          "name": "dbType",
          "label": "元数据库类型",
          "description": "服务依赖的数据库类型: mysql,h2或者postgresql。默认为h2",
          "configType": "map",
          "required": true,
          "type": "select",
          "value": "",
          "selectValue": [
            "h2",
            "mysql",
            "postgresql"
          ],
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "h2"
        },
        {
          "name": "dataPath",
          "label": "webserver目录下的data路径",
          "description": "如果数据库类型为h2，默认的数据存储路径为webserver目录下的data路径",
          "configType": "map",
          "required": false,
          "type": "input",
          "value": "",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": ""
        },
        {
          "name": "dbHost",
          "label": "数据库地址",
          "description": "如果数据库类型为mysql，配置mysql数据库的访问地址",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "${host}",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "${host}"
        },
        {
          "name": "dbPort",
          "label": "数据库访问端口",
          "description": "配置mysql/postgresql数据库的访问端口",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "3306"
        },
        {
          "name": "dbUser",
          "label": "数据库用户名",
          "description": "配置mysql/postgresql数据库的访问用户",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "root"
        },
        {
          "name": "dbPass",
          "label": "数据库密码",
          "description": "配置数据库密码",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "123456"
        },
        {
          "name": "dbDbname",
          "label": "数据库名称",
          "description": "mysql/postgresql数据库的访问Database名称",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "dorismanager"
        },
        {
          "name": "dbUrlSuffix",
          "label": "mysql 数据连接 URL 的后缀 ",
          "description": "mysql 数据连接 URL 的后缀",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "'?useSSL=false&useUnicode=true&characterEncoding=UTF-8'",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "'?useSSL=false&useUnicode=true&characterEncoding=UTF-8'"
        },
        {
          "name": "HttpConnectTimeout",
          "label": "HTTP握手超时时间",
          "description": "配置HTTP握手超时时间 (单位为秒)。默认为30秒",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "30",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "30"
        },
        {
          "name": "HttpSocketTimeout",
          "label": "HTTP接收响应超时时间",
          "description": "HTTP接收响应超时时间 (单位为秒)。默认为60秒",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "60",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "60"
        },
        {
          "name": "ListenProtocol",
          "label": "服务监听的IP协议",
          "description": "服务监听的IP协议，支持 ALL、IPV4和IPV6，ALL 表示同时支持IPV4和IPV6",
          "configType": "map",
          "required": true,
          "type": "select",
          "value": "ALL",
          "selectValue": [
            "ALL",
            "IPV4",
            "IPV6"
          ],
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "ALL"
        },
        {
          "name": "FeMinDiskSpaceForUpgrade",
          "label": "fe最小的空余磁盘空间",
          "description": "升级时 FE 模块安装路径最小的空余磁盘空间，默认为10G",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "10",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "10"
        },
        {
          "name": "BeMinDiskSpaceForUpgrade",
          "label": "be最小的空余磁盘空间",
          "description": "升级时 BE 模块安装路径最小的空余磁盘空间，默认为10G",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "10",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "10"
        },
        {
          "name": "agentPort",
          "label": "agent端口",
          "description": "agent端口",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "8972",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "8972"
        },
        {
          "name": "WEBUI_PORT",
          "label": "webui端口",
          "description": "webui端口",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "8010",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "8010"
        },
        {
          "name": "JAVA_OPTS",
          "label": "JVM内存参数",
          "description": "JVM内存参数",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "-Xmx1024m"
        },
        {
          "name": "QUERY_LIMIT",
          "label": "webui查询默认返回行数",
          "description": "webui查询默认返回行数。功能类似于  limit 100 ,default value is 100",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "100",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "100"
        },
        {
          "name": "DB_TYPE",
          "label": "元数据库类型",
          "description": "服务依赖的数据库类型: mysql,h2或者postgresql。默认为h2",
          "configType": "map",
          "required": true,
          "type": "select",
          "value": "",
          "selectValue": [
            "h2",
            "mysql",
            "postgresql"
          ],
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "h2"
        },
        {
          "name": "DATA_PATH",
          "label": "WebUI服务元数据存放的路径",
          "description": "WebUI服务元数据存放的路径",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "./webui/data",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "./webui/data"
        },
        {
          "name": "DB_HOST",
          "label": "WebUI服务元数据库地址",
          "description": "WebUI服务元数据库地址",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "${host}"
        },
        {
          "name": "DB_PORT",
          "label": "WebUI服务元数据库端口",
          "description": "WebUI服务元数据库端口",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "3306"
        },
        {
          "name": "DB_USER",
          "label": "WebUI服务元数据库用户名",
          "description": "WebUI服务元数据库用户名",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "root"
        },
        {
          "name": "DB_PASS",
          "label": "WebUI服务元数据库用户名密码",
          "description": "WebUI服务元数据库用户名密码",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "123456"
        },
        {
          "name": "DB_DBNAME",
          "label": "WebUI服务元数据库名",
          "description": "WebUI服务元数据库名",
          "configType": "map",
          "required": true,
          "type": "input",
          "value": "",
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": "doriswebui"
        },
        {
          "name": "custom.manager.conf",
          "label": "自定义配置manager.conf",
          "description": "自定义配置",
          "configType": "custom",
          "required": false,
          "type": "multipleWithKey",
          "value": [],
          "configurableInWizard": true,
          "hidden": false,
          "defaultValue": []
        }
      ]
    }
    

### control\_dorismanager.sh

新组件的集成。必须要有start，stop和status三个命令，否则初始化安装无法通过

    #!/bin/bash
    
    usage="Usage: start.sh (start|stop|restart|status) <command>[webserver|agent|webui] "
    
    # if no args specified, show usage
    if [ $# -le 1 ]; then
      echo $usage
      exit 1
    fi
    startStop=$1
    shift
    command=$1
    
    SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
    DORISMANAGER_HOME=$SCRIPT_DIR  # 当前目录即为 doris-manager 安装目录
    MANAGER=${DORISMANAGER_HOME}/webserver
    AGENT=${DORISMANAGER_HOME}/agent/manager-agent
    WEBUI=${DORISMANAGER_HOME}/webui
    
    if [[ "$command" = "agent" ]]; then
       cmd=${AGENT}/bin
    elif [[ "$command" = "webserver" ]]; then
       cmd=${MANAGER}/bin
    elif [[ "$command" = "webui" ]]; then
       cmd=${WEBUI}/bin
    else
      echo "Error: No command named \'$command' was found."
      exit 1
    fi
    
    start(){
    	echo "execute $cmd/start.sh"
    	$cmd/start.sh
    	if [ $? -eq 0 ]
        then
    		echo "$command start success"
    	else
    		echo "$command start failed"
    		exit 1
    	fi
    }
    stop(){
      echo "execute $cmd/stop.sh"
      $cmd/stop.sh
    	if [ $? -eq 0 ]
        then
    		echo "$command stop success"
    	else
    		echo "$command stop failed"
    		exit 1
    	fi
    }
    
    status(){
      echo "execute $cmd/status.sh"
      $cmd/status.sh
    	if [ $? -eq 0 ]
        then
    		echo "$command is running"
    	else
    		echo "$command is not running"
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

## datasophon-worker模块

新增三个模板文件，分别用于配置manager-server,manaer-agent和webui

    #在datasophon-worker/src/main/resources/templates目录下新增以下三个文件
    doris_managerserver.ftl
    doris_manageragent.ftl
    doris_managerwebui.ftl

三个文件的内容分别如下

### doris\_managerserver.ftl

    # See the License for the specific language governing permissions and
    # limitations under the License.
    
    # http port
    MANAGER_PORT=${managerPort}
    
    # The deployment depends on three data service types: mysql/h2/postgresql. h2 is supported by default.
    DB_TYPE=${dbType}
    
    # h2 database data path, the default is the relative path.
    # If configuration is required, please configure the absolute path.
    DATA_PATH=
    
    # If you want to use mysql/postgresql, you need to configure the following connection information
    # mysql/postgresql connection address
    DB_HOST=${dbHost}
    
    # mysql/postgresql connection port
    DB_PORT=${dbPort}
    
    # mysql/postgresql connect access user
    DB_USER=${dbUser}
    
    # mysql/postgresql connection access user password
    DB_PASS=${dbPass}
    
    # mysql/postgresql database accessed by the service（database）
    DB_DBNAME=${dbDbname}
    
    # mysql database connect url suffix
    DB_URL_SUFFIX=${dbUrlSuffix}
    
    INSPECTION_RES_MAX_AGE=1209600
    
    # timeout for http handshake (seconds)
    HTTP_CONNECT_TIMEOUT=${HttpConnectTimeout}
    
    # timeout for http receiving response (seconds)
    HTTP_SOCKET_TIMEOUT=${HttpSocketTimeout}
    
    # make webserver process prefer to listen on IPV4, IPV6 or ALL
    LISTEN_PROTOCOL=${ListenProtocol}
    
    # weather to open swagger api (FALSE or TRUE)
    SWAGGER_ENABLE=FALSE
    
    # min disk space for FE upgrade(default 10 GB)
    FE_MIN_DISK_SPACE_FOR_UPGRADE=${FeMinDiskSpaceForUpgrade}
    
    # min disk space for BE upgrade(default 10 GB)
    BE_MIN_DISK_SPACE_FOR_UPGRADE=${BeMinDiskSpaceForUpgrade}
    
    # Default master branch managed version
    # MASTER_BRANCH_VERSION=0.0.0
    
    # job executor thread num
    # JOB_EXECUTOR_THREAD_NUM=50

### doris\_manageragent.ftl

    # Copyright 2023 SelectDB, Inc.
    #
    # Licensed under the Apache License, Version 2.0 (the "License");
    # you may not use this file except in compliance with the License.
    # You may obtain a copy of the License at
    #
    #     http://www.apache.org/licenses/LICENSE-2.0
    #
    # Unless required by applicable law or agreed to in writing, software
    # distributed under the License is distributed on an "AS IS" BASIS,
    # See the License for the specific language governing permissions and
    # limitations under the License.
    
    # Configuration file for dorisctrl agent
    
    agent:
    port: ${agentPort}
    
    #log:
    #  level: debug
    #  path: /var/log/doris-agent
    #  link_name: agent.log
    #  rotation_time: 24h
    #  max_age: 720h
    #  line_flag: false
    #  std_file_output: true
    #  formatter: text
    

### doris\_managerwebui.ftl

    # http port
    WEBUI_PORT=${WEBUI_PORT}
    
    # default query limit rows ,default value is 100
    QUERY_LIMIT=${QUERY_LIMIT}
    
    JAVA_OPTS=${JAVA_OPTS}
    
    # The deployment depends on three data service types: mysql/h2/postgresql. h2 is supported by default.
    DB_TYPE=${DB_TYPE}
    
    # h2 database data path, the default is the relative path.
    # If configuration is required, please configure the absolute path.
    DATA_PATH=${DATA_PATH}
    
    # If you want to use mysql/postgresql, you need to configure the following connection information
    # mysql/postgresql connection address
    DB_HOST=${DB_HOST}
    
    # mysql/postgresql connection port
    DB_PORT=${DB_PORT}
    
    # mysql/postgresql connect access user
    DB_USER=${DB_USER}
    
    # mysql/postgresql connection access user password
    DB_PASS=${DB_PASS}
    
    # mysql/postgresql database accessed by the service（database）
    DB_DBNAME=${DB_DBNAME}
    

# doris-manager安装包 制作

## 下载安装包 

doris-manager

x64版本下载链接： [https://selectdb-doris-1308700295.cos.ap-beijing.myqcloud.com/doris-manager/release/24.0.0-rc2/doris-manager-24.0.0-rc2-x64-bin.tar.gz](https://selectdb-doris-1308700295.cos.ap-beijing.myqcloud.com/doris-manager/release/24.0.0-rc2/doris-manager-24.0.0-rc2-x64-bin.tar.gz)

doris-webui

doris-webui在selectDb当中：https://doris-build-1308700295.cos.ap-beijing.myqcloud.com/enterprise-doris-release-output/selectdb-doris-2.0.3-b4-bin-x64.tar.gz

## 解压安装包

    tar -zxvf doris-manager-24.0.0-rc1-x64-bin.tar.gz
    mv doris-manager-24.0.0-rc1-x64-bin doris-manager-24.0.0
    
    #agent在manager当中，以tar.gz包存在，我们提前将其解压，
    cd doris-manager-24.0.0/agent
    tar -zxvf manager-agent-24.0.0-rc1-x64-bin.tar.gz
    mv manager-agent-24.0.0-rc1-x64-bin manager-agent
    
    
    

#将selectDb当中的webui也解压到doris-manager当中

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/jP2lR4X1236Eq8g5/img/0783e2bb-0a23-44ad-8ccc-512eed2b983b.png)

    scp -r selectdb-doris-2.0.3-b4-bin-x64/webui doris-manager-24.0.0

## 添加状态判断脚本

以下脚本都需要赋予可执行权限   ：chmod +x status.sh

### manager-agent

在doris-manager-24.0.0/agent/manager-agent/bin下添加agent的状态脚本，status.sh

    #!/usr/bin/env bash
    
    # YAML配置名称
    AGENT_YAML="agent.yaml"
    
    # 查看状态
    pid=$(ps -ef | grep $AGENT_YAML | grep -v grep |awk '{print $2}')
      if [ -z $pid ]; then
        echo ""
        echo "Service manager_agent is not running!"
        echo ""
        exit 1
      else
        echo ""
        echo "Service manager_agent is running. It's pid=${pid}"
        echo ""
      fi

### manager-server

在doris-manager-24.0.0/webserver/bin下添加server的状态脚本，status.sh

    #!/usr/bin/env bash
    
    # jar包名称
    SERVER_JAR="doris-manager.jar"
    
    # 查看状态
    pid=$(ps -ef | grep $SERVER_JAR | grep -v grep |awk '{print $2}')
      if [ -z $pid ]; then
        echo ""
        echo "Service ${SERVER_JAR} is not running!"
        echo ""
        exit 1
      else
        echo ""
        echo "Service ${SERVER_JAR} is running. It's pid=${pid}"
        echo ""
      fi

### webui

在doris-manager-24.0.0/webui/bin下添加webui的状态脚本，status.sh

    #!/usr/bin/env bash
    
    # jar包名称
    WEBUI_JAR="doris-webui.jar"
    
    # 查看状态
    pid=$(ps -ef | grep $WEBUI_JAR | grep -v grep |awk '{print $2}')
      if [ -z $pid ]; then
        echo ""
        echo "Service ${WEBUI_JAR} is not running!"
        echo ""
        exit 1
      else
        echo ""
        echo "Service ${WEBUI_JAR} is running. It's pid=${pid}"
        echo ""
      fi
    

## 添加全局控制脚本

在doris-manager-24.0.0下添加全局控制脚本：control\_dorismanager.sh

脚本赋予可执行权限   ：chmod +x control\_dorismanager.sh

    #!/bin/bash
    
    usage="Usage: start.sh (start|stop|restart|status) <command>[webserver|agent|webui] "
    
    # if no args specified, show usage
    if [ $# -le 1 ]; then
      echo $usage
      exit 1
    fi
    startStop=$1
    shift
    command=$1
    
    SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
    DORISMANAGER_HOME=$SCRIPT_DIR  # 当前目录即为 doris-manager 安装目录
    MANAGER=${DORISMANAGER_HOME}/webserver
    AGENT=${DORISMANAGER_HOME}/agent/manager-agent
    WEBUI=${DORISMANAGER_HOME}/webui
    
    if [[ "$command" = "agent" ]]; then
       cmd=${AGENT}/bin
    elif [[ "$command" = "webserver" ]]; then
       cmd=${MANAGER}/bin
    elif [[ "$command" = "webui" ]]; then
       cmd=${WEBUI}/bin
    else
      echo "Error: No command named \'$command' was found."
      exit 1
    fi
    
    start(){
    	echo "execute $cmd/start.sh"
    	$cmd/start.sh
    	if [ $? -eq 0 ]
        then
    		echo "$command start success"
    	else
    		echo "$command start failed"
    		exit 1
    	fi
    }
    stop(){
      echo "execute $cmd/stop.sh"
      $cmd/stop.sh
    	if [ $? -eq 0 ]
        then
    		echo "$command stop success"
    	else
    		echo "$command stop failed"
    		exit 1
    	fi
    }
    
    status(){
      echo "execute $cmd/status.sh"
      $cmd/status.sh
    	if [ $? -eq 0 ]
        then
    		echo "$command is running"
    	else
    		echo "$command is not running"
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

所有包制作完成后，目录结构如下

## 重新打包tar.gz

    # bin目录新增status脚本
    
    # 打包
    tar czf doris-manager-24.0.0.tar.gz doris-manager-24.0.0
    
    #生成md
    md5sum doris-manager-24.0.0.tar.gz | awk '{print $1}' > doris-manager-24.0.0.tar.gz.md5
    

# 组件安装

组件安装需分三步走， 第一步启用升级后的datasophon-manager， 然后所有worker节点重新启动datasophon-worker，最后可以安装新的doris-manager组件

## 刷新datasophon-manager

上传manager安装到/opt/datasophon/DDP/packages

上传 service\_ddl.json和control\_dorismanager.sh脚本到/opt/datasophon/datasophon-manager-1.1.2/conf/meta/DDP-1.1.2/DORISMANAGER

重启manager服务

    sh /opt/datasophon/datasophon-manager-1.1.2/bin/datasophon-api.sh restart api

## 刷新worker

将worker模块下的 三个ftl文件上传到/opt/datasophon/datasophon-worker/conf/templates（每台机器都需重启worker）

并重启worker服务

    sh /opt/datasophon/datasophon-worker/bin/datasophon-worker.sh restart worker

## 安装doris-manager

注意：doris-managerServer服务不要和grafana，altermanager和promethus放在同一个节点

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/jP2lR4X1236Eq8g5/img/030b9f33-a66a-4221-bdb9-19636c4fd9fc.png)

安装doris-manager默认采用h2作为元数据库， 如果想替换成mysql，需要mysql当中提前创建dorismanager和doriswebui两个库，并指定为mysql，配置如下

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/jP2lR4X1236Eq8g5/img/4fa6b3de-e4c8-410a-bcca-98dcc247b2c6.png)

安装成功后如下图所示

![image](https://alidocs.oss-cn-zhangjiakou.aliyuncs.com/res/jP2lR4X1236Eq8g5/img/75329576-fe13-4259-86fb-f4d3d612baa3.png)