### 1、构建安装包

下载redis tar包 redis-7.2.3.tar.gz

```shell
tar -zxvf redis-7.2.3.tar.gz
cd redis-7.2.3
# 编译
make && make install
# 复制安装路径编译文件
mkdir /opt/soft/redis-7.2.3
cd /opt/soft/redis-7.2.3
cp /usr/local/bin/redis* /opt/soft/redis-7.2.3
rm -rf /usr/local/bin/redis*

# 创建自定义文件及文件夹，目录结构为
cluster			# 自定义
	-conf			# 自定义					 
  -log 			# 自定义
  -pid 			# 自定义
control_redis.sh
redis-benchmark
redis-check-aof
redis-check-rdb
redis-cli
redis-cluster.sh		# 自定义
redis-sentinel
redis-server

tar czf redis-7.2.3.tar.gz redis-7.2.3
md5sum redis-7.2.3.tar.gz
echo 'd20743bd570ab78efaf0a9aa3b28caf5' > redis-7.2.3.tar.gz.md5
cp ./redis-7.2.3.tar.gz ./redis-7.2.3.tar.gz.md5 /opt/datasophon/DDP/packages/
```

### 2、元数据文件

**api节点元数据：**

```shell
cd /opt/apps/datasophon-manager-1.2.0/conf/meta/DDP-1.2.0
mkdir REDIS
cd REDIS	
touch service_ddl.json
```

service_ddl.json：

```shell
{
  "name": "REDIS",
  "label": "REDIS",
  "description": "交互式数据分析notebook",
  "version": "7.2.3",
  "sortNum": 37,
  "dependencies": [],
  "packageName": "redis-7.2.3.tar.gz",
  "decompressPackageName": "redis-7.2.3",
  "roles": [
    {
      "name": "RedisMaster",
      "label": "RedisMaster",
      "roleType": "master",
      "cardinality": "1+",
      "runAs": {},
      "logFile": "cluster/log/cluster-master.log",
      "startRunner": {
        "timeout": "60",
        "program": "control_redis.sh",
        "args": [
          "start",
          "master"
        ]
      },
      "stopRunner": {
        "timeout": "600",
        "program": "control_redis.sh",
        "args": [
          "stop",
          "master"
        ]
      },
      "statusRunner": {
        "timeout": "60",
        "program": "control_redis.sh",
        "args": [
          "status",
          "master"
        ]
      }
    },
    {
      "name": "RedisWorker",
      "label": "RedisWorker",
      "roleType": "worker",
      "cardinality": "1+",
      "runAs": {},
      "logFile": "cluster/log/cluster-slave.log",
      "startRunner": {
        "timeout": "60",
        "program": "control_redis.sh",
        "args": [
          "start",
          "slave"
        ]
      },
      "stopRunner": {
        "timeout": "600",
        "program": "control_redis.sh",
        "args": [
          "stop",
          "slave"
        ]
      },
      "statusRunner": {
        "timeout": "60",
        "program": "control_redis.sh",
        "args": [
          "status",
          "slave"
        ]
      }
    }
  ],
  "configWriter": {
    "generators": [
      {
        "filename": "control_redis.sh",
        "configFormat": "custom",
        "outputDirectory": "",
        "templateName": "redis-control.ftl",
        "includeParams": [
          "redisMasterPort",
          "redisSlavePort"
        ]
      },
      {
        "filename": "redis-master.conf",
        "configFormat": "custom",
        "outputDirectory": "cluster/conf",
        "templateName": "redis-master.ftl",
        "includeParams": [
          "redisMasterPort",
          "custom.master.conf"
        ]
      },
      {
        "filename": "redis-slave.conf",
        "configFormat": "custom",
        "outputDirectory": "cluster/conf",
        "templateName": "redis-slave.ftl",
        "includeParams": [
          "redisSlavePort",
          "custom.slave.conf"
        ]
      },
      {
        "filename": "redis-cluster.sh",
        "configFormat": "custom",
        "outputDirectory": "",
        "templateName": "redis-cluster.ftl",
        "includeParams": [
          "RedisMasterAddr",
          "RedisSlaveAddr"
        ]
      }
    ]
  },
  "parameters": [
    {
      "name": "redisMasterPort",
      "label": "master节点端口号",
      "description": "",
      "configType": "map",
      "required": true,
      "type": "input",
      "value": "7000",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "7000"
    },
    {
      "name": "redisSlavePort",
      "label": "worker节点端口号",
      "description": "",
      "configType": "map",
      "required": true,
      "type": "input",
      "value": "7001",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "7001"
    },
    {
      "name": "custom.master.conf",
      "label": "自定义master配置",
      "description": "自定义master配置",
      "configType": "custom",
      "required": false,
      "type": "multipleWithKey",
      "value": [],
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": ""
    },
    {
      "name": "custom.slave.conf",
      "label": "自定义worker配置",
      "description": "自定义worker配置",
      "configType": "custom",
      "required": false,
      "type": "multipleWithKey",
      "value": [],
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": ""
    },
    {
      "name": "RedisMasterAddr",
      "label": "RedisMasterAddr",
      "description": "",
      "configType": "map",
      "required": false,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": ""
    },
    {
      "name": "RedisSlaveAddr",
      "label": "RedisSlaveAddr",
      "description": "",
      "configType": "map",
      "required": false,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": ""
    }
  ]
}
```

**各worker节点元数据：**

```shell
cd /opt/datasophon/datasophon-worker/conf/templates
touch redis-cluster.ftl
touch redis-control.ftl
touch redis-master.ftl
touch redis-slave.ftl
```

redis-cluster.ftl：

```shell
#!/bin/bash

# 在这里替换为你的Redis Master和Worker节点的主机和端口（空格分隔）
REDIS_MASTERS="${RedisMasterAddr}"
REDIS_WORKERS="${RedisSlaveAddr}"
COMMAND_CREATE_CLUSTER="echo yes | /opt/datasophon/redis/redis-cli --cluster create "
COMMAND_ADD_NODE="/opt/datasophon/redis/redis-cli --cluster add-node "

CONTROL_SCRIPT="/opt/datasophon/redis/control_redis.sh"

create_cluster_command() {
    local master_list="$1"
    local create_command="$COMMAND_CREATE_CLUSTER"
    for master in $master_list; do
        create_command+=" $master"
    done
    echo "$create_command"
}

add_node_command() {
    local new_master="$1"
    local new_worker="$2"
    local add_node_command="$COMMAND_ADD_NODE $new_master $new_worker --cluster-slave"
    echo "$add_node_command"
}

check_redis_nodes() {
    IFS=' ' read -ra MASTER_NODES <<< "$REDIS_MASTERS"
    IFS=' ' read -ra WORKER_NODES <<< "$REDIS_WORKERS"

    local CHECK_SCRIPT

    # 检测Master节点状态
    for master in "<#noparse>${MASTER_NODES[@]}</#noparse>"; do
        CHECK_SCRIPT="$CONTROL_SCRIPT status master"
        if ! ssh "$(echo "$master" | cut -d ":" -f 1)" "$CHECK_SCRIPT"; then
            echo "Redis master node $master is not running."
            return 1
        fi
    done

    # 检测Worker节点状态
    for worker in "<#noparse>${WORKER_NODES[@]}</#noparse>"; do
        CHECK_SCRIPT="$CONTROL_SCRIPT status slave"
        if ! ssh "$(echo "$worker" | cut -d ":" -f 1)" "$CHECK_SCRIPT $(echo "$worker" | cut -d ":" -f 2)"; then
            echo "Redis worker node $worker is not running."
            return 1
        fi
    done

    return 0
}

main() {
    if check_redis_nodes; then
        # 如果所有节点都正常启动，执行创建集群命令
        CREATE_CLUSTER_COMMAND=$(create_cluster_command "$REDIS_MASTERS")
        echo "Executing command: $CREATE_CLUSTER_COMMAND"
        eval "$CREATE_CLUSTER_COMMAND"

        # 检查上一条命令执行状态
        if [ $? -eq 0 ]; then
            echo "Create cluster command executed successfully."
        else
            echo "Error: Create cluster command failed."
            return 1
        fi

        # 执行添加节点命令
        FIRST_MASTER="<#noparse>${MASTER_NODES[0]}</#noparse>"
        for worker in "<#noparse>${WORKER_NODES[@]}</#noparse>"; do
            ADD_NODE_COMMAND=$(add_node_command "$worker" "$FIRST_MASTER")
            echo "Executing command: $ADD_NODE_COMMAND"
            eval "$ADD_NODE_COMMAND"

            # 检查上一条命令执行状态
            if [ $? -eq 0 ]; then
                echo "Add node command executed successfully."
            else
                echo "Error: Add node command failed."
                return 1
            fi
        done
    else
        echo "Not all Redis nodes are running. Cluster commands will not be executed."
    fi
}


# 执行主函数
main
```

redis-control.ftl：

```shell
#!/bin/bash

# 定义启动和停止命令
START_MASTER="/opt/datasophon/redis/redis-server /opt/datasophon/redis/cluster/conf/redis-master.conf"
START_SLAVE="/opt/datasophon/redis/redis-server /opt/datasophon/redis/cluster/conf/redis-slave.conf"
STOP_MASTER="/opt/datasophon/redis/redis-cli -p ${redisMasterPort} shutdown"
STOP_SLAVE="/opt/datasophon/redis/redis-cli -p ${redisSlavePort} shutdown"
STATUS_MASTER="/opt/datasophon/redis/redis-cli -p ${redisMasterPort} ping"
STATUS_SLAVE="/opt/datasophon/redis/redis-cli -p ${redisSlavePort} ping"

# 启动Master
start_master() {
    echo "Starting Redis Master..."
    $START_MASTER
}

# 启动Slave
start_slave() {
    echo "Starting Redis Slave..."
    $START_SLAVE
}

# 停止Master
stop_master() {
    echo "Stopping Redis Master..."
    $STOP_MASTER
}

# 停止Slave
stop_slave() {
    echo "Stopping Redis Slave..."
    $STOP_SLAVE
}

# 检查状态并根据返回值决定退出码
check_status() {
    echo "Checking Redis status..."
    status=$($1)  # 使用传递的命令获取状态
    if [ "$status" == "PONG" ]; then
        echo "Redis is running."
        exit 0
    else
        echo "Redis is not running."
        exit 1
    fi
}

# 执行操作
case $1 in
    start)
        case $2 in
            master)
                start_master
                ;;
            slave)
                start_slave
                ;;
            *)
                echo "Invalid second parameter. Usage: $0 start {master|slave}"
                exit 1
                ;;
        esac
        ;;
    stop)
        case $2 in
            master)
                stop_master
                ;;
            slave)
                stop_slave
                ;;
            *)
                echo "Invalid second parameter. Usage: $0 stop {master|slave}"
                exit 1
                ;;
        esac
        ;;
    status)
        case $2 in
            master)
                check_status "$STATUS_MASTER"
                ;;
            slave)
                check_status "$STATUS_SLAVE"
                ;;
            *)
                echo "Invalid second parameter. Usage: $0 status {master|slave}"
                exit 1
                ;;
        esac
        ;;
    *)
        echo "Invalid first parameter. Usage: $0 {start|stop|status} {master|slave}"
        exit 1
        ;;
esac
```

redis-master.ftl：

```shell
bind 0.0.0.0
daemonize yes
protected-mode no
port ${redisMasterPort}
logfile "/opt/datasophon/redis/cluster/log/cluster-master.log"
pidfile /opt/datasophon/redis/cluster/pid/cluster-master.pid
dir /opt/datasophon/redis/cluster
dbfilename dump-master.rdb
appendonly yes
appendfilename "appendonly-master.aof"

cluster-enabled yes
cluster-config-file /opt/datasophon/redis/cluster/conf/nodes-master.conf
cluster-node-timeout 5000

<#list itemList as item>
${item.name} ${item.value}
</#list>
```

redis-slave.ftl：

```shell
bind 0.0.0.0
daemonize yes
protected-mode no
port ${redisSlavePort}
logfile "/opt/datasophon/redis/cluster/log/cluster-slave.log"
pidfile /opt/datasophon/redis/cluster/pid/cluster-slave.pid
dir /opt/datasophon/redis/cluster
dbfilename dump-slave.rdb
appendonly yes
appendfilename "appendonly-slave.aof"

cluster-enabled yes
cluster-config-file /opt/datasophon/redis/cluster/conf/nodes-slave.conf
cluster-node-timeout 5000

<#list itemList as item>
${item.name} ${item.value}
</#list>
```

### 3、修改源码

com.datasophon.api.strategy.RedisHandlerStrategy

```java
package com.datasophon.api.strategy;

import com.datasophon.api.load.GlobalVariables;
import com.datasophon.api.utils.ProcessUtils;
import com.datasophon.common.Constants;
import com.datasophon.common.cache.CacheUtils;
import com.datasophon.common.model.ServiceConfig;
import com.datasophon.common.model.ServiceRoleInfo;
import com.datasophon.dao.entity.ClusterInfoEntity;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RedisHandlerStrategy extends ServiceHandlerAbstract implements ServiceRoleStrategy {

    @Override
    public void handler(Integer clusterId, List<String> hosts) {

    }

    @Override
    public void handlerConfig(Integer clusterId, List<ServiceConfig> list) {

    }

    @Override
    public void getConfig(Integer clusterId, List<ServiceConfig> list) {
        Map<String, String> globalVariables = GlobalVariables.get(clusterId);
        String masterPort = globalVariables.get("${redisMasterPort}");
        String slavePort = globalVariables.get("${redisSlavePort}");

        ClusterInfoEntity clusterInfo = ProcessUtils.getClusterInfo(clusterId);
        String hostMapKey =
                clusterInfo.getClusterCode()
                        + Constants.UNDERLINE
                        + Constants.SERVICE_ROLE_HOST_MAPPING;
        HashMap<String, List<String>> map = (HashMap<String, List<String>>) CacheUtils.get(hostMapKey);
        List<String> masterHostList = map.get("RedisMaster");
        List<String> workerHostList = map.get("RedisWorker");

        for (ServiceConfig serviceConfig : list) {
            if ("RedisMasterAddr".equals(serviceConfig.getName())) {
                String masterAddr = masterHostList.stream()
                        .map(t -> t + ":" + masterPort)
                        .collect(Collectors.joining(" "));
                serviceConfig.setRequired(true);
                serviceConfig.setValue(masterAddr);
            } else if ("RedisSlaveAddr".equals(serviceConfig.getName())) {
                String workerAddr = workerHostList.stream()
                        .map(t -> t + ":" + slavePort)
                        .collect(Collectors.joining(" "));
                serviceConfig.setRequired(true);
                serviceConfig.setValue(workerAddr);
            }
        }
    }

    @Override
    public void handlerServiceRoleInfo(ServiceRoleInfo serviceRoleInfo, String hostname) {

    }

    @Override
    public void handlerServiceRoleCheck(ClusterServiceRoleInstanceEntity roleInstanceEntity, Map<String, ClusterServiceRoleInstanceEntity> map) {

    }
}
```

com.datasophon.api.strategy.ServiceRoleStrategyContext

```java
map.put("REDIS", new RedisHandlerStrategy());
```

com.datasophon.worker.strategy.RedisHandlerStrategy

```java
package com.datasophon.worker.strategy;

import com.datasophon.common.Constants;
import com.datasophon.common.command.ServiceRoleOperateCommand;
import com.datasophon.common.enums.CommandType;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.ShellUtils;
import com.datasophon.worker.handler.ServiceHandler;

import java.sql.SQLException;

public class RedisHandlerStrategy extends AbstractHandlerStrategy implements ServiceRoleStrategy {

    public RedisHandlerStrategy(String serviceName, String serviceRoleName) {
        super(serviceName, serviceRoleName);
    }

    @Override
    public ExecResult handler(ServiceRoleOperateCommand command) throws SQLException, ClassNotFoundException {
        ServiceHandler serviceHandler = new ServiceHandler(command.getServiceName(), command.getServiceRoleName());
        String workPath = Constants.INSTALL_PATH + Constants.SLASH + command.getDecompressPackageName();
        ExecResult startResult;

        if (command.getCommandType().equals(CommandType.INSTALL_SERVICE)) {
            startResult = serviceHandler.start(command.getStartRunner(), command.getStatusRunner(),
                    command.getDecompressPackageName(), command.getRunAs());
            ShellUtils.exceShell("bash " + workPath + "/redis-cluster.sh");
        }

        startResult = serviceHandler.start(command.getStartRunner(), command.getStatusRunner(),
                command.getDecompressPackageName(), command.getRunAs());
        return startResult;
    }
}
```

com.datasophon.worker.strategy.ServiceRoleStrategyContext

```java
map.put("RedisMaster", new RedisHandlerStrategy("REDIS", "RedisMaster"));
map.put("RedisWorker", new RedisHandlerStrategy("REDIS", "RedisWorker"));
```

**打包部署**

### 4、重启

各节点worker重启

```shell
sh /opt/datasophon/datasophon-worker/bin/datasophon-worker.sh restart worker debug
```

主节点重启api

```shell
sh /opt/apps/datasophon-manager-1.2.0/bin/datasophon-api.sh restart api debug
```

