### 1、构建压缩包

官方下载安装包 alluxio-2.9.3-bin.tar.gz

```shell
tar -zxvf alluxio-2.9.3-bin.tar.gz
cd alluxio-2.9.3
vim control_alluxio.sh
cd ..
tar czf alluxio-2.9.3.tar.gz alluxio-2.9.3
mkd5sum alluxio-2.9.3.tar.gz
echo 'bf0bf449ee28d0db8da56a5dba8ecee3' > alluxio-2.9.3.tar.gz.md5
cp ./alluxio-2.9.3.tar.gz ./alluxio-2.9.3.tar.gz.md5 /opt/datasophon/DDP/packages
```

control_alluxio.sh:

```shell
#!/bin/bash

operation=$1
node_type=$2

alluxio_start="./bin/alluxio-start.sh"
alluxio_stop="./bin/alluxio-stop.sh"

check_process() {
    if ps -ef | grep -v grep | grep -q "$1"; then
        return 0  # Process exists
    else
        return 1  # Process doesn't exist
    fi
}

start_master() {
    if ! check_process "AlluxioMaster"; then
        $alluxio_start master
    fi
    if ! check_process "AlluxioJobMaster"; then
        $alluxio_start job_master
    fi
    if ! check_process "AlluxioProxy"; then
        $alluxio_start proxy
    fi
}

start_worker() {
    if ! check_process "AlluxioWorker"; then
        $alluxio_start worker
    fi
    if ! check_process "AlluxioJobWorker"; then
        $alluxio_start job_worker
    fi
    if ! check_process "AlluxioProxy"; then
        $alluxio_start proxy
    fi
}

stop_master() {
    if check_process "AlluxioProxy"; then
        $alluxio_stop proxy
    fi
    if check_process "AlluxioJobMaster"; then
        $alluxio_stop job_master
    fi
    if check_process "AlluxioMaster"; then
        $alluxio_stop master
    fi
}

stop_worker() {
    if check_process "AlluxioProxy"; then
        $alluxio_stop proxy
    fi
    if check_process "AlluxioJobWorker"; then
        $alluxio_stop job_worker
    fi
    if check_process "AlluxioWorker"; then
        $alluxio_stop worker
    fi
}

if [ "$operation" == "start" ]; then
    case "$node_type" in
        "master")
            start_master
            ;;
        "worker")
            start_worker
            ;;
        *)
            echo "Invalid node type. Please use 'master' or 'worker'."
            ;;
    esac
elif [ "$operation" == "stop" ]; then
    case "$node_type" in
        "master")
            stop_master
            ;;
        "worker")
            stop_worker
            ;;
        *)
            echo "Invalid node type. Please use 'master' or 'worker'."
            ;;
    esac
elif [ "$operation" == "status" ]; then
    case "$node_type" in
        "master")
            if check_process "AlluxioMaster"; then
                exit 0
            else
                exit 1
            fi
            ;;
        "worker")
            if check_process "AlluxioWorker"; then
                exit 0
            else
                exit 1
            fi
            ;;
        *)
            echo "Invalid node type. Please use 'master' or 'worker'."
            ;;
    esac
else
    echo "Invalid operation. Please use 'start', 'stop', or 'status'."
fi

```

### 2、配置元数据文件

```shell
cd /opt/apps/datasophon-manager-1.2.0/conf/meta/DDP-1.2.0
mkdir ALLUXIO
touch service_ddl.json
touch properties_value.flt
```

将下面两个文件放进去

servcie_ddl.json：

```shell
{
  "name": "ALLUXIO",
  "label": "ALLUXIO",
  "description": "分布式内存文件系统",
  "version": "2.9.3",
  "sortNum": 30,
  "dependencies": [
    "ZOOKEEPER"
  ],
  "packageName": "alluxio-2.9.3.tar.gz",
  "decompressPackageName": "alluxio-2.9.3",
  "roles": [
    {
      "name": "AlluxioMaster",
      "label": "AlluxioMaster",
      "roleType": "master",
      "runAs": {},
      "cardinality": "1+",
      "sortNum": 2,
      "logFile": "logs/master.log",
      "jmxPort": "",
      "startRunner": {
        "timeout": "600",
        "program": "control_alluxio.sh",
        "args": [
          "start",
          "master"
        ]
      },
      "stopRunner": {
        "timeout": "600",
        "program": "control_alluxio.sh",
        "args": [
          "stop",
          "master"
        ]
      },
      "statusRunner": {
        "timeout": "60",
        "program": "control_alluxio.sh",
        "args": [
          "status",
          "master"
        ]
      },
      "externalLink": {
        "name": "master Ui",
        "label": "master Ui",
        "url": "http://${host}:19999"
      }
    },
    {
      "name": "AlluxioWorker",
      "label": "AlluxioWorker",
      "roleType": "worker",
      "runAs": {},
      "cardinality": "1+",
      "sortNum": 1,
      "logFile": "logs/worker.log",
      "jmxPort": "",
      "startRunner": {
        "timeout": "60",
        "program": "control_alluxio.sh",
        "args": [
          "start",
          "worker"
        ]
      },
      "stopRunner": {
        "timeout": "600",
        "program": "control_alluxio.sh",
        "args": [
          "stop",
          "worker"
        ]
      },
      "statusRunner": {
        "timeout": "60",
        "program": "control_alluxio.sh",
        "args": [
          "status",
          "worker"
        ]
      }
    }
  ],
  "configWriter": {
    "generators": [
      {
        "filename": "alluxio-site.properties",
        "configFormat": "properties",
        "outputDirectory": "conf",
        "includeParams": [
          "alluxio.master.mount.table.root.ufs",
          "alluxio.underfs.hdfs.configuration",
          "alluxio.master.embedded.journal.addresses",
          "alluxio.zookeeper.enabled",
          "alluxio.zookeeper.address",
          "alluxio.master.journal.type",
          "alluxio.master.journal.folder",
          "alluxio.worker.block.heartbeat.timeout.ms",
          "alluxio.zookeeper.session.timeout",
          "custom.common.properties"
        ]
      },
      {
        "filename": "masters",
        "configFormat": "custom",
        "outputDirectory": "conf",
        "templateName": "properties_value.ftl",
        "includeParams": [
          "masters"
        ]
      },
      {
        "filename": "workers",
        "configFormat": "custom",
        "outputDirectory": "conf",
        "templateName": "properties_value.ftl",
        "includeParams": [
          "workers"
        ]
      }
    ]
  },
  "parameters": [
    {
      "name": "alluxio.master.mount.table.root.ufs",
      "label": "挂载到Alluxio根目录的底层存储URI",
      "description": "挂载到Alluxio根目录的底层存储URI",
      "required": true,
      "type": "input",
      "value": "${fs.defaultFS}/alluxio",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "${fs.defaultFS}/alluxio"
    },
    {
      "name": "alluxio.underfs.hdfs.configuration",
      "label": "hdfs配置文件路径",
      "description": "hdfs配置文件路径",
      "required": true,
      "type": "input",
      "value": "${HADOOP_HOME}/etc/hadoop/core-site.xml:${HADOOP_HOME}/etc/hadoop/hdfs-site.xml",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "${HADOOP_HOME}/etc/hadoop/core-site.xml:${HADOOP_HOME}/etc/hadoop/hdfs-site.xml"
    },
    {
      "name": "alluxio.master.embedded.journal.addresses",
      "label": "参加leading master选举的master节点集",
      "description": "参加Alluxio leading master选举的master节点集",
      "required": true,
      "type": "input",
      "value": "${host1}:19200,${host2}:19200,${host3}:19200",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": ""
    },
    {
      "name": "alluxio.zookeeper.enabled",
      "label": "启用HA模式",
      "description": "启用HA模式",
      "required": true,
      "type": "switch",
      "value": true,
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": true
    },
    {
      "name": "alluxio.zookeeper.address",
      "label": "zookeeper地址",
      "description": "zookeeper地址",
      "required": true,
      "type": "input",
      "value": "${zkUrls}",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "${zkUrls}"
    },
    {
      "name": "alluxio.master.journal.type",
      "label": "",
      "description": "",
      "required": true,
      "type": "input",
      "value": "UFS",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "UFS"
    },
    {
      "name": "alluxio.master.journal.folder",
      "label": "共享日志位置的URI",
      "description": "共享日志位置的URI",
      "required": true,
      "type": "input",
      "value": "${fs.defaultFS}/alluxio/journal/",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "${fs.defaultFS}/alluxio/journal/"
    },
    {
      "name": "alluxio.worker.block.heartbeat.timeout.ms",
      "label": "Zookeeper服务器的最小/最大session timeout",
      "description": "Zookeeper服务器的最小/最大session timeout",
      "required": true,
      "type": "input",
      "value": "300000",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "300000"
    },
    {
      "name": "alluxio.zookeeper.session.timeout",
      "label": "zookeeper连接超时时间",
      "description": "zookeeper连接超时时间",
      "required": true,
      "type": "input",
      "value": "120s",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "120s"
    },
    {
      "name": "custom.common.properties",
      "label": "自定义配置common.properties",
      "description": "自定义配置",
      "configType": "custom",
      "required": false,
      "type": "multipleWithKey",
      "value": [],
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": ""
    },
    {
      "name": "masters",
      "label": "masters",
      "description": "masters机器的IP",
      "required": true,
      "separator":"\n",
      "type": "multiple",
      "value": [],
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": ""
    },
    {
      "name": "workers",
      "label": "workers",
      "description": "workers机器的IP",
      "required": true,
      "separator":"\n",
      "type": "multiple",
      "value": [],
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": ""
    }
  ]
}
```

properties_value.flt：

```shell
<#list itemList as item>
${item.value}
</#list>
```

### 3、新增worker源码Handler

修改 com.datasophon.worker.handler.ConfigureServiceHandler
新增：

```shell
if ("AlluxioMaster".equals(serviceRoleName) && "alluxio-site.properties".equals(generators.getFilename())) {
    ServiceConfig serviceConfig = new ServiceConfig();
    serviceConfig.setName("alluxio.master.hostname");
    serviceConfig.setValue(hostName);
    customConfList.add(serviceConfig);
}
if ("AlluxioWorker".equals(serviceRoleName) && "alluxio-site.properties".equals(generators.getFilename())) {
    File alluxioFile =
            new File(Constants.INSTALL_PATH + File.separator + decompressPackageName, "conf/alluxio-site.properties");
    if (alluxioFile.exists()) {
        continue;
    }
}
```

![image](https://github.com/datavane/datasophon/assets/62798940/475ae77d-8865-457c-9699-dd4bff5e46f2)

修改 com.datasophon.worker.strategy.ServiceRoleStrategyContext：

```shell
map.put("AlluxioMaster", new AlluxioHandlerStrategy("ALLUXIO", "AlluxioMaster"));
```

创建：com.datasophon.worker.strategy.AlluxioHandlerStrategy

```shell
package com.datasophon.worker.strategy;

import com.datasophon.common.Constants;
import com.datasophon.common.command.ServiceRoleOperateCommand;
import com.datasophon.common.enums.CommandType;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.ShellUtils;
import com.datasophon.worker.handler.ServiceHandler;

import java.sql.SQLException;
import java.util.ArrayList;

public class AlluxioHandlerStrategy  extends AbstractHandlerStrategy implements ServiceRoleStrategy {

    public AlluxioHandlerStrategy(String serviceName, String serviceRoleName) {
        super(serviceName, serviceRoleName);
    }

    @Override
    public ExecResult handler(ServiceRoleOperateCommand command) throws SQLException, ClassNotFoundException {
        ServiceHandler serviceHandler = new ServiceHandler(command.getServiceName(), command.getServiceRoleName());
        String workPath = Constants.INSTALL_PATH + Constants.SLASH + command.getDecompressPackageName();

        if (command.getCommandType().equals(CommandType.INSTALL_SERVICE)) {
            ArrayList<String> commands = new ArrayList<>();

            logger.info("start format master");
            commands.add(workPath + "/bin/alluxio");
            commands.add("format");
            ShellUtils.execWithStatus(workPath, commands, 300L, logger);
            logger.info("alluxio master format success");

            commands.clear();
            commands.add(workPath + "/alluxio/bin/alluxio-start.sh");
            commands.add("all");
            ExecResult execResult = ShellUtils.execWithStatus(workPath, commands, 300L, logger);
            if (execResult.getExecResult()) {
                logger.info("alluxio start all success");
            }
        }

        ExecResult startResult = serviceHandler.start(command.getStartRunner(), command.getStatusRunner(),
                command.getDecompressPackageName(), command.getRunAs());
        return startResult;
    }
}

```

### 4、重启

各节点worker重启

```shell
sh /opt/datasophon/datasophon-worker/bin/datasophon-worker.sh restart worker debug
```

主节点重启api

```shell
sh /opt/apps/datasophon-manager-1.2.0/bin/datasophon-api.sh restart api debug
```

### 5、配置样例

![image](https://github.com/datavane/datasophon/assets/62798940/bd626fec-c581-4c22-8f36-b582afbb7ea4)
