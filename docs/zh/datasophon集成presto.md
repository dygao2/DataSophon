### 1、打包安装包

解压安装包，可以在这里对解压后的文件做修改，更改文件名，这里的文件名是和后面的配置文件对应的：

```shell
tar -zxvf presto-server-0.283.tar.gz
mv presto-server-0.283 presto-0.283
```

将文件打包，注意这里的压缩包名也和后面配置文件对应：

```shell
tar czf presto-0.283.tar.gz presto-0.283
```

编写md5文件：

```shell
md5sum presto-0.283.tar.gz
echo '84666ba9ef9b9024fa7c385af0823101' > presto-0.283.tar.gz.md5
```

将两个文件拷贝进对应文件夹中：

```shell
cp ./presto-0.283.tar.gz ./presto-0.283.tar.gz.md5 /opt/datasophon/DDP/packages
```

### 2、编写presto元数据

```shell
cd /opt/apps/datasophon/datasophon-manager-1.1.2/conf/meta/DDP-1.1.2
mkdir PRESTO
cd PRESTO
vim service_ddl.json
```

```shell
{
  "name": "PRESTO",
  "label": "Presto",
  "description": "分布式SQL交互式查询引擎",
  "version": "0.283",
  "sortNum": 21,
  "dependencies": [],
  "packageName": "presto-0.283.tar.gz",
  "decompressPackageName": "presto-0.283",
  "roles": [
    {
      "name": "PrestoCoordinator",
      "label": "PrestoCoordinator",
      "roleType": "master",
      "cardinality": "1",
      "jmxPort": 8087,
      "logFile": "data/var/log/server.log",
      "startRunner": {
        "timeout": "60",
        "program": "bin/launcher",
        "args": [
          "start"
        ]
      },
      "stopRunner": {
        "timeout": "600",
        "program": "bin/launcher",
        "args": [
          "stop"
        ]
      },
      "statusRunner": {
        "timeout": "60",
        "program": "bin/launcher",
        "args": [
          "status"
        ]
      },
      "restartRunner": {
        "timeout": "60",
        "program": "bin/launcher",
        "args": [
          "restart"
        ]
      },
      "externalLink": {
        "name": "Presto UI",
        "label": "Presto UI",
        "url": "http://${host}:7777"
      }
    },
    {
      "name": "PrestoWorker",
      "label": "PrestoWorker",
      "roleType": "worker",
      "cardinality": "1+",
      "jmxPort": 8089,
      "logFile": "data/var/log/server.log",
      "startRunner": {
        "timeout": "60",
        "program": "bin/launcher",
        "args": [
          "start"
        ]
      },
      "stopRunner": {
        "timeout": "600",
        "program": "bin/launcher",
        "args": [
          "stop"
        ]
      },
      "statusRunner": {
        "timeout": "60",
        "program": "bin/launcher",
        "args": [
          "status"
        ]
      },
      "restartRunner": {
        "timeout": "60",
        "program": "bin/launcher",
        "args": [
          "restart"
        ]
      }
    }
  ],
  "configWriter": {
    "generators": [
      {
        "filename": "config.properties",
        "configFormat": "properties",
        "outputDirectory": "etc",
        "includeParams": [
          "coordinator",
          "http-server.http.port",
          "query.max-memory-per-node",
          "query.max-memory",
          "discovery.uri",
          "custom.config.properties"
        ]
      },
      {
        "filename": "jvm.config",
        "configFormat": "custom",
        "outputDirectory": "etc",
        "templateName": "presto.jvm.config.ftl",
        "includeParams": [
          "prestoHeapSize"
        ]
      },
      {
        "filename": "node.properties",
        "configFormat": "properties",
        "outputDirectory": "etc",
        "includeParams": [
          "node.data-dir",
          "node.environment"
        ]
      },
      {
        "filename": "hive.properties",
        "configFormat": "properties",
        "outputDirectory": "etc/catalog",
        "includeParams": [
          "custom.hive.properties"
        ]
      }
    ]
  },
  "parameters": [
    {
      "name": "coordinator",
      "label": "coordinator",
      "description": "coordinator",
      "required": true,
      "type": "input",
      "value": "false",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "false"
    },
    {
      "name": "prestoHeapSize",
      "label": "Presto最大堆内存",
      "description": "Presto最大堆内存",
      "configType": "map",
      "required": true,
      "minValue": 0,
      "maxValue": 64,
      "type": "slider",
      "value": "",
      "unit": "GB",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "32"
    },
    {
      "name": "http-server.http.port",
      "label": "Presto Http端口",
      "description": "",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "7777"
    },
    {
      "name": "discovery.uri",
      "label": "服务发现地址",
      "description": "",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "http://${coordinatorHost}:7777"
    },
    {
      "name": "query.max-memory-per-node",
      "label": "每个查询在单个节点可使用最大内存",
      "description": "",
      "required": true,
      "type": "input",
      "minValue": 0,
      "maxValue": "30",
      "value": "",
      "unit": "GB",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "1GB"
    },
    {
      "name": "query.max-memory",
      "label": "总共可使用最大内存",
      "description": "若query.max-memory-per-node = 30GB则query.max-memory = <30GB *节点数>",
      "required": true,
      "type": "input",
      "minValue": 0,
      "maxValue": "30",
      "value": "",
      "unit": "GB",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "30GB"
    },
    {
      "name": "node.data-dir",
      "label": "日志存储地址",
      "description": "",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "${INSTALL_PATH}/presto-0.283/data"
    },
    {
      "name": "node.environment",
      "label": "集群环境名称",
      "description": "",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "production"
    },
    {
      "name": "custom.config.properties",
      "label": "自定义配置config.properties",
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
      "name": "custom.hive.properties",
      "label": "自定义配置hive.properties",
      "description": "自定义配置",
      "configType": "custom",
      "required": false,
      "type": "multipleWithKey",
      "value": [{"connector.name":"hive-hadoop2"},{"hive.metastore.uri":"thrift://${metastoreHost}:9083"},{"hive.config.resources":"${INSTALL_PATH}/hadoop-3.3.3/etc/hadoop/core-site.xml,${INSTALL_PATH}/hadoop-3.3.3/etc/hadoop/hdfs-site.xml"}],
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": [{"connector.name":"hive-hadoop2"},{"hive.metastore.uri":"thrift://${metastoreHost}:9083"},{"hive.config.resources":"${INSTALL_PATH}/hadoop-3.3.3/etc/hadoop/core-site.xml,${INSTALL_PATH}/hadoop-3.3.3/etc/hadoop/hdfs-site.xml"}]
    }
  ]
}
```

```shell
cd /opt/datasophon/datasophon-worker/conf/templates
vim presto.jvm.config.ftl
```

```shell
-server
-Xmx${prestoHeapSize}G
-XX:-UseBiasedLocking
-XX:G1HeapRegionSize=32M
-XX:+ExplicitGCInvokesConcurrent
-XX:+ExitOnOutOfMemoryError
-XX:+HeapDumpOnOutOfMemoryError
-XX:-OmitStackTraceInFastThrow
-XX:ReservedCodeCacheSize=512M
-XX:PerMethodRecompilationCutoff=10000
-XX:PerBytecodeRecompilationCutoff=10000
-Djdk.attach.allowAttachSelf=true
-Djdk.nio.maxCachedBufferSize=2000000
-XX:+UnlockDiagnosticVMOptions
-XX:+UseAESCTRIntrinsics
```

### 3、修改worker源码，重新打包worker包

修改 datasophon-worker/src/main/java/com/datasophon/worker/handler/ConfigureServiceHandler.java
新增代码

![image](https://github.com/datavane/datasophon/assets/62798940/0fbf7d09-e351-4789-9aff-f911610e117f)

```shell
if ("PrestoCoordinator".equals(serviceRoleName) && "coordinator".equals(config.getName())) {
    logger.info("Start config presto coordinator");
    config.setValue("true");
    ServiceConfig serviceConfig = new ServiceConfig();
    serviceConfig.setName("node-scheduler.include-coordinator");
    serviceConfig.setValue("false");
    ServiceConfig serviceConfig1 = new ServiceConfig();
    serviceConfig1.setName("discovery-server.enabled");
    serviceConfig1.setValue("true");
    customConfList.add(serviceConfig);
    customConfList.add(serviceConfig1);
}
```

将重新打包的 datasophon-worker-1.1.2.jar 文件替换到每个worker节点的 /opt/datasophon/datasophon-worker/lib
1.2.0版本worker包名为datasophon-worker-1.1.3.jar，需要上传后改名

### 4、重启

各节点worker重启

```shell
sh /opt/datasophon/datasophon-worker/bin/datasophon-worker.sh restart worker
```

主节点重启api

```shell
sh /opt/apps/datasophon/datasophon-manager-1.1.2/bin/datasophon-api.sh restart api
```

此时可以看到mysql元数据库中 t_ddh_frame_service 和 t_ddh_frame_service_role 两个表已经添加了presto的元数据。
搭建需要注意一点节点不能既是master又是worker

### 5、集成监控

#### 5.1 presto安装目录创建jmx配置文件

```shell
pwd
/opt/datasophon/presto
mkdir jmx
cd jmx
vim prometheus_config.yml
```

```shell
---
lowercaseOutputLabelNames: true
lowercaseOutputName: true
whitelistObjectNames: ["java.lang:type=OperatingSystem"]
blacklistObjectNames: []
rules:
  - pattern: 'java.lang<type=OperatingSystem><>(committed_virtual_memory|free_physical_memory|free_swap_space|total_physical_memory|total_swap_space)_size:'
    name: os_$1_bytes
    type: GAUGE
    attrNameSnakeCase: true
  - pattern: 'java.lang<type=OperatingSystem><>((?!process_cpu_time)\w+):'
    name: os_$1
    type: GAUGE
    attrNameSnakeCase: true
```

将 jmx_prometheus_javaagent-0.16.1.jar 放入jmx文件夹

![image](https://github.com/datavane/datasophon/assets/62798940/16b9dd5d-8957-45b6-b0fc-163e47d49a25)

#### 5.2 修改presto启动脚本 /opt/datasophon/presto/bin/launcher.py

![image](https://github.com/datavane/datasophon/assets/62798940/820fda3d-860d-4817-a687-ffa37cf5f6a3)

```shell
#!/usr/bin/env python

import errno
import os
import platform
import subprocess
import sys
import traceback

from fcntl import flock, LOCK_EX, LOCK_NB
from optparse import OptionParser
from os import O_RDWR, O_CREAT, O_WRONLY, O_APPEND
from os.path import basename, dirname, exists, realpath
from os.path import join as pathjoin
from signal import SIGTERM, SIGKILL
from stat import S_ISLNK
from time import sleep

COMMANDS = ['run', 'start', 'stop', 'restart', 'kill', 'status']

LSB_NOT_RUNNING = 3
LSB_STATUS_UNKNOWN = 4


def find_install_path(f):
    """Find canonical parent of bin/launcher.py"""
    if basename(f) != 'launcher.py':
        raise Exception("Expected file '%s' to be 'launcher.py' not '%s'" % (f, basename(f)))
    p = realpath(dirname(f))
    if basename(p) != 'bin':
        raise Exception("Expected file '%s' directory to be 'bin' not '%s" % (f, basename(p)))
    return dirname(p)


def makedirs(p):
    """Create directory and all intermediate ones"""
    try:
        os.makedirs(p)
    except OSError as e:
        if e.errno != errno.EEXIST:
            raise


def load_properties(f):
    """Load key/value pairs from a file"""
    properties = {}
    for line in load_lines(f):
        k, v = line.split('=', 1)
        properties[k.strip()] = v.strip()
    return properties


def load_lines(f):
    """Load lines from a file, ignoring blank or comment lines"""
    lines = []
    for line in open(f, 'r').readlines():
        line = line.strip()
        if len(line) > 0 and not line.startswith('#'):
            lines.append(line)
    return lines


def try_lock(f):
    """Try to open an exclusive lock (inheritable) on a file"""
    try:
        flock(f, LOCK_EX | LOCK_NB)
        return True
    except (IOError, OSError):  # IOError in Python 2, OSError in Python 3.
        return False


def open_pidfile(f, mode):
    """Open file in read/write mode (without truncating it)"""
    fd = os.open(f, O_RDWR | O_CREAT, mode)
    if hasattr(os, 'set_inheritable'):
        # See https://docs.python.org/3/library/os.html#inheritance-of-file-descriptors
        # Since Python 3.4
        os.set_inheritable(fd, True)
    return os.fdopen(fd, 'r+')


class Process:
    def __init__(self, path):
        makedirs(dirname(path))
        self.path = path
        self.pid_file = open_pidfile(path, 0o600)
        self.refresh()

    def refresh(self):
        self.locked = try_lock(self.pid_file)

    def clear_pid(self):
        assert self.locked, 'pid file not locked by us'
        self.pid_file.seek(0)
        self.pid_file.truncate()

    def write_pid(self, pid):
        self.clear_pid()
        self.pid_file.write(str(pid) + '\n')
        self.pid_file.flush()

    def alive(self):
        self.refresh()
        if self.locked:
            return False

        pid = self.read_pid()
        try:
            os.kill(pid, 0)
            return True
        except OSError as e:
            raise Exception('Signaling pid %s failed: %s' % (pid, e))

    def read_pid(self):
        assert not self.locked, 'pid file is locked by us'
        self.pid_file.seek(0)
        line = self.pid_file.readline().strip()
        if len(line) == 0:
            raise Exception("Pid file '%s' is empty" % self.path)

        try:
            pid = int(line)
        except ValueError:
            raise Exception("Pid file '%s' contains garbage: %s" % (self.path, line))
        if pid <= 0:
            raise Exception("Pid file '%s' contains an invalid pid: %s" % (self.path, pid))
        return pid


def redirect_stdin_to_devnull():
    """Redirect stdin to /dev/null"""
    fd = os.open(os.devnull, O_RDWR)
    os.dup2(fd, sys.stdin.fileno())
    os.close(fd)


def open_append(f):
    """Open a raw file descriptor in append mode"""
    # noinspection PyTypeChecker
    return os.open(f, O_WRONLY | O_APPEND | O_CREAT, 0o644)


def redirect_output(fd):
    """Redirect stdout and stderr to a file descriptor"""
    os.dup2(fd, sys.stdout.fileno())
    os.dup2(fd, sys.stderr.fileno())


def symlink_exists(p):
    """Check if symlink exists and raise if another type of file exists"""
    try:
        st = os.lstat(p)
        if not S_ISLNK(st.st_mode):
            raise Exception('Path exists and is not a symlink: %s' % p)
        return True
    except OSError as e:
        if e.errno != errno.ENOENT:
            raise
    return False


def create_symlink(source, target):
    """Create a symlink, removing the target first if it is a symlink"""
    if symlink_exists(target):
        os.remove(target)
    if exists(source):
        os.symlink(source, target)


def create_app_symlinks(options):
    """
    Symlink the 'etc' and 'plugin' directory into the data directory.

    This is needed to support programs that reference 'etc/xyz' from within
    their config files: log.levels-file=etc/log.properties
    """
    if options.etc_dir != pathjoin(options.data_dir, 'etc'):
        create_symlink(
            options.etc_dir,
            pathjoin(options.data_dir, 'etc'))

    if options.install_path != options.data_dir:
        create_symlink(
            pathjoin(options.install_path, 'plugin'),
            pathjoin(options.data_dir, 'plugin'))


def build_java_execution(options, daemon):
    if not exists(options.config_path):
        raise Exception('Config file is missing: %s' % options.config_path)
    if not exists(options.jvm_config):
        raise Exception('JVM config file is missing: %s' % options.jvm_config)
    if not exists(options.launcher_config):
        raise Exception('Launcher config file is missing: %s' % options.launcher_config)
    if options.log_levels_set and not exists(options.log_levels):
        raise Exception('Log levels file is missing: %s' % options.log_levels)

    with open(os.devnull, 'w') as devnull:
        try:
            subprocess.check_call(['java', '-version'], stdout=devnull, stderr=devnull)
        except (OSError, subprocess.CalledProcessError):
            raise Exception('Java is not installed')

    properties = options.properties.copy()

    if exists(options.log_levels):
        properties['log.levels-file'] = options.log_levels

    if daemon:
        properties['log.output-file'] = options.server_log
        properties['log.enable-console'] = 'false'

    jvm_properties = load_lines(options.jvm_config)
    launcher_properties = load_properties(options.launcher_config)

    try:
        main_class = launcher_properties['main-class']
    except KeyError:
        raise Exception("Launcher config is missing 'main-class' property")

    properties['config'] = options.config_path

    system_properties = ['-D%s=%s' % i for i in properties.items()]
    classpath = pathjoin(options.install_path, 'lib', '*')

    command = ['java', '-cp', classpath]
    command += jvm_properties + options.jvm_options + system_properties
    config_properties = {}
    if exists(options.config_path):
        config_properties = load_properties(options.config_path)
        if config_properties['coordinator'] == 'true':
            print 'coordinator true'
            command += ['-javaagent:/opt/datasophon/presto/jmx/jmx_prometheus_javaagent-0.16.1.jar=7778:/opt/datasophon/presto/jmx/prometheus_config.yml']
        else:
            command += ['-javaagent:/opt/datasophon/presto/jmx/jmx_prometheus_javaagent-0.16.1.jar=7779:/opt/datasophon/presto/jmx/prometheus_config.yml']
    command += [main_class]
    if options.verbose:
        print(command)
        print("")

    env = os.environ.copy()

    # set process name: https://github.com/airlift/procname
    process_name = launcher_properties.get('process-name', '')
    if len(process_name) > 0:
        system = platform.system() + '-' + platform.machine()
        shim = pathjoin(options.install_path, 'bin', 'procname', system, 'libprocname.so')
        if exists(shim):
            env['LD_PRELOAD'] = (env.get('LD_PRELOAD', '') + ':' + shim).strip()
            env['PROCNAME'] = process_name

    return command, env


def run(process, options):
    if process.alive():
        print('Already running as %s' % process.read_pid())
        return

    create_app_symlinks(options)
    args, env = build_java_execution(options, False)

    makedirs(options.data_dir)
    os.chdir(options.data_dir)

    process.write_pid(os.getpid())

    redirect_stdin_to_devnull()

    os.execvpe(args[0], args, env)


def start(process, options):
    if process.alive():
        print('Already running as %s' % process.read_pid())
        return

    create_app_symlinks(options)
    args, env = build_java_execution(options, True)

    makedirs(dirname(options.launcher_log))
    log = open_append(options.launcher_log)

    makedirs(options.data_dir)
    os.chdir(options.data_dir)

    pid = os.fork()
    if pid > 0:
        process.write_pid(pid)
        print('Started as %s' % pid)
        return

    os.setsid()

    redirect_stdin_to_devnull()
    redirect_output(log)
    os.close(log)

    os.execvpe(args[0], args, env)


def terminate(process, signal, message):
    if not process.alive():
        print('Not running')
        return

    pid = process.read_pid()

    while True:
        try:
            os.kill(pid, signal)
        except OSError as e:
            if e.errno != errno.ESRCH:
                raise Exception('Signaling pid %s failed: %s' % (pid, e))

        if not process.alive():
            process.clear_pid()
            break

        sleep(0.1)

    print('%s %s' % (message, pid))


def stop(process):
    terminate(process, SIGTERM, 'Stopped')


def kill(process):
    terminate(process, SIGKILL, 'Killed')


def status(process):
    if not process.alive():
        print('Not running')
        sys.exit(LSB_NOT_RUNNING)
    print('Running as %s' % process.read_pid())


def handle_command(command, options):
    process = Process(options.pid_file)
    if command == 'run':
        run(process, options)
    elif command == 'start':
        start(process, options)
    elif command == 'stop':
        stop(process)
    elif command == 'restart':
        stop(process)
        start(process, options)
    elif command == 'kill':
        kill(process)
    elif command == 'status':
        status(process)
    else:
        raise AssertionError('Unhandled command: ' + command)


def create_parser():
    commands = 'Commands: ' + ', '.join(COMMANDS)
    parser = OptionParser(prog='launcher', usage='usage: %prog [options] command', description=commands)
    parser.add_option('-v', '--verbose', action='store_true', default=False, help='Run verbosely')
    parser.add_option('--etc-dir', metavar='DIR', help='Defaults to INSTALL_PATH/etc')
    parser.add_option('--launcher-config', metavar='FILE', help='Defaults to INSTALL_PATH/bin/launcher.properties')
    parser.add_option('--node-config', metavar='FILE', help='Defaults to ETC_DIR/node.properties')
    parser.add_option('--jvm-config', metavar='FILE', help='Defaults to ETC_DIR/jvm.config')
    parser.add_option('--config', metavar='FILE', help='Defaults to ETC_DIR/config.properties')
    parser.add_option('--log-levels-file', metavar='FILE', help='Defaults to ETC_DIR/log.properties')
    parser.add_option('--data-dir', metavar='DIR', help='Defaults to INSTALL_PATH')
    parser.add_option('--pid-file', metavar='FILE', help='Defaults to DATA_DIR/var/run/launcher.pid')
    parser.add_option('--launcher-log-file', metavar='FILE', help='Defaults to DATA_DIR/var/log/launcher.log (only in daemon mode)')
    parser.add_option('--server-log-file', metavar='FILE', help='Defaults to DATA_DIR/var/log/server.log (only in daemon mode)')
    parser.add_option('-J', action='append', metavar='OPT', dest='jvm_options', help='Set a JVM option')
    parser.add_option('-D', action='append', metavar='NAME=VALUE', dest='properties', help='Set a Java system property')
    return parser


def parse_properties(parser, args):
    properties = {}
    for arg in args:
        if '=' not in arg:
            parser.error('property is malformed: %s' % arg)
        key, value = [i.strip() for i in arg.split('=', 1)]
        if key == 'config':
            parser.error('cannot specify config using -D option (use --config)')
        if key == 'log.output-file':
            parser.error('cannot specify server log using -D option (use --server-log-file)')
        if key == 'log.levels-file':
            parser.error('cannot specify log levels using -D option (use --log-levels-file)')
        properties[key] = value
    return properties


def print_options(options):
    if options.verbose:
        for i in sorted(vars(options)):
            print("%-15s = %s" % (i, getattr(options, i)))
        print("")


class Options:
    pass


def main():
    parser = create_parser()

    (options, args) = parser.parse_args()

    if len(args) != 1:
        if len(args) == 0:
            parser.error('command name not specified')
        else:
            parser.error('too many arguments')
    command = args[0]

    if command not in COMMANDS:
        parser.error('unsupported command: %s' % command)

    try:
        install_path = find_install_path(sys.argv[0])
    except Exception as e:
        print('ERROR: %s' % e)
        sys.exit(LSB_STATUS_UNKNOWN)

    o = Options()
    o.verbose = options.verbose
    o.install_path = install_path
    o.launcher_config = realpath(options.launcher_config or pathjoin(o.install_path, 'bin/launcher.properties'))
    o.etc_dir = realpath(options.etc_dir or pathjoin(o.install_path, 'etc'))
    o.node_config = realpath(options.node_config or pathjoin(o.etc_dir, 'node.properties'))
    o.jvm_config = realpath(options.jvm_config or pathjoin(o.etc_dir, 'jvm.config'))
    o.config_path = realpath(options.config or pathjoin(o.etc_dir, 'config.properties'))
    o.log_levels = realpath(options.log_levels_file or pathjoin(o.etc_dir, 'log.properties'))
    o.log_levels_set = bool(options.log_levels_file)
    o.jvm_options = options.jvm_options or []

    if options.node_config and not exists(o.node_config):
        parser.error('Node config file is missing: %s' % o.node_config)

    node_properties = {}
    if exists(o.node_config):
        node_properties = load_properties(o.node_config)

    data_dir = node_properties.get('node.data-dir')
    o.data_dir = realpath(options.data_dir or data_dir or o.install_path)

    o.pid_file = realpath(options.pid_file or pathjoin(o.data_dir, 'var/run/launcher.pid'))
    o.launcher_log = realpath(options.launcher_log_file or pathjoin(o.data_dir, 'var/log/launcher.log'))
    o.server_log = realpath(options.server_log_file or pathjoin(o.data_dir, 'var/log/server.log'))

    o.properties = parse_properties(parser, options.properties or {})
    for k, v in node_properties.items():
        if k not in o.properties:
            o.properties[k] = v

    if o.verbose:
        print_options(o)

    try:
        handle_command(command, o)
    except SystemExit:
        raise
    except Exception as e:
        if o.verbose:
            traceback.print_exc()
        else:
            print('ERROR: %s' % e)
        sys.exit(LSB_STATUS_UNKNOWN)


if __name__ == '__main__':
    main()

```

#### 5.3 修改Prometheus配置文件

```shell
vim /opt/datasophon/prometheus/prometheus.yml
```

新增presto配置

```shell
- job_name: 'prestocoordinator'
  file_sd_configs:
   - files:
     - configs/prestocoordinator.json
- job_name: 'prestoworker'
  file_sd_configs:
   - files:
     - configs/prestoworker.json
```

在 /opt/datasophon/prometheus/configs 目录新增 prestocoordinator.json 和 prestoworker.json 配置文件

```shell
[
 {
  "targets":["hadoop1:7778"]
 }
]
```

```shell
[
 {
  "targets":["hadoop2:7779","hadoop3:7779"]
 }
]
```

重启prometheus,访问webui可看到采集过来的指标
[http://hadoop1:9090/targets](http://hadoop1:9090/targets)

![image](https://github.com/datavane/datasophon/assets/62798940/f93a3ad1-64c6-463c-b989-c7c7af93cd82)

#### 5.4 绘制grafana

打开grafana ui

![image](https://github.com/datavane/datasophon/assets/62798940/369c0997-5a5e-44ce-bcc8-5163360b240c)

将下面json粘贴进去

```shell
{
  "annotations": {
    "list": [
      {
        "$$hashKey": "object:7978",
        "builtIn": 1,
        "datasource": {
          "type": "datasource",
          "uid": "grafana"
        },
        "enable": true,
        "hide": true,
        "iconColor": "rgba(0, 211, 255, 1)",
        "name": "Annotations & Alerts",
        "target": {
          "limit": 100,
          "matchAny": false,
          "tags": [],
          "type": "dashboard"
        },
        "type": "dashboard"
      }
    ]
  },
  "description": "",
  "editable": true,
  "fiscalYearStartMonth": 0,
  "gnetId": 10866,
  "graphTooltip": 0,
  "id": 42,
  "links": [],
  "liveNow": false,
  "panels": [
    {
      "datasource": {
        "type": "prometheus",
        "uid": "hj6gjW44z"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "thresholds"
          },
          "mappings": [
            {
              "options": {
                "match": "null",
                "result": {
                  "text": "N/A"
                }
              },
              "type": "special"
            }
          ],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              }
            ]
          },
          "unit": "dateTimeAsIso"
        },
        "overrides": []
      },
      "gridPos": {
        "h": 4,
        "w": 6,
        "x": 0,
        "y": 0
      },
      "id": 16,
      "links": [],
      "maxDataPoints": 100,
      "options": {
        "colorMode": "value",
        "graphMode": "none",
        "justifyMode": "auto",
        "orientation": "horizontal",
        "reduceOptions": {
          "calcs": [
            "lastNotNull"
          ],
          "fields": "",
          "values": false
        },
        "text": {
          "valueSize": 38
        },
        "textMode": "auto"
      },
      "pluginVersion": "9.1.6",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "hj6gjW44z"
          },
          "editorMode": "code",
          "expr": "process_start_time_seconds{job=\"prestocoordinator\"}*1000",
          "legendFormat": "__auto",
          "range": true,
          "refId": "A"
        }
      ],
      "title": "PrestoCoordinator启动时间",
      "type": "stat"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "hj6gjW44z"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "thresholds"
          },
          "mappings": [
            {
              "options": {
                "match": "null",
                "result": {
                  "text": "N/A"
                }
              },
              "type": "special"
            }
          ],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              }
            ]
          },
          "unit": "s"
        },
        "overrides": []
      },
      "gridPos": {
        "h": 4,
        "w": 4,
        "x": 6,
        "y": 0
      },
      "id": 34,
      "links": [],
      "maxDataPoints": 100,
      "options": {
        "colorMode": "value",
        "graphMode": "none",
        "justifyMode": "auto",
        "orientation": "horizontal",
        "reduceOptions": {
          "calcs": [
            "lastNotNull"
          ],
          "fields": "",
          "values": false
        },
        "text": {
          "valueSize": 38
        },
        "textMode": "auto"
      },
      "pluginVersion": "9.1.6",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "hj6gjW44z"
          },
          "editorMode": "code",
          "expr": "time() - process_start_time_seconds{job=\"prestocoordinator\"}",
          "interval": "",
          "legendFormat": "",
          "range": true,
          "refId": "A"
        }
      ],
      "title": "PrestoCoordinator运行时长",
      "type": "stat"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "hj6gjW44z"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "thresholds"
          },
          "mappings": [
            {
              "options": {
                "match": "null",
                "result": {
                  "text": "N/A"
                }
              },
              "type": "special"
            }
          ],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              }
            ]
          },
          "unit": "bytes"
        },
        "overrides": []
      },
      "gridPos": {
        "h": 4,
        "w": 4,
        "x": 10,
        "y": 0
      },
      "id": 20,
      "links": [],
      "maxDataPoints": 100,
      "options": {
        "colorMode": "value",
        "graphMode": "none",
        "justifyMode": "auto",
        "orientation": "horizontal",
        "reduceOptions": {
          "calcs": [
            "lastNotNull"
          ],
          "fields": "",
          "values": false
        },
        "text": {
          "valueSize": 38
        },
        "textMode": "auto"
      },
      "pluginVersion": "9.1.6",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "hj6gjW44z"
          },
          "editorMode": "code",
          "expr": "jvm_memory_bytes_max{job=\"prestocoordinator\",area=\"heap\"}",
          "legendFormat": "__auto",
          "range": true,
          "refId": "A"
        }
      ],
      "title": "Presto最大堆内存",
      "type": "stat"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "hj6gjW44z"
      },
      "fieldConfig": {
        "defaults": {
          "mappings": [],
          "max": 100,
          "min": 0,
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          },
          "unit": "%"
        },
        "overrides": []
      },
      "gridPos": {
        "h": 4,
        "w": 4,
        "x": 14,
        "y": 0
      },
      "id": 28,
      "options": {
        "orientation": "auto",
        "reduceOptions": {
          "calcs": [
            "lastNotNull"
          ],
          "fields": "",
          "values": false
        },
        "showThresholdLabels": false,
        "showThresholdMarkers": true
      },
      "pluginVersion": "9.1.6",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "hj6gjW44z"
          },
          "editorMode": "code",
          "expr": "jvm_memory_bytes_used{area=\"heap\",job=\"prestocoordinator\"}*100/jvm_memory_bytes_max{area=\"heap\",job=\"prestocoordinator\"}",
          "legendFormat": "__auto",
          "range": true,
          "refId": "A"
        }
      ],
      "title": "PrestoCoordinator堆内存使用率",
      "type": "gauge"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "hj6gjW44z"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "thresholds"
          },
          "mappings": [
            {
              "options": {
                "match": "null",
                "result": {
                  "text": "N/A"
                }
              },
              "type": "special"
            }
          ],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          },
          "unit": "none"
        },
        "overrides": []
      },
      "gridPos": {
        "h": 4,
        "w": 4,
        "x": 18,
        "y": 0
      },
      "id": 24,
      "links": [],
      "maxDataPoints": 100,
      "options": {
        "colorMode": "value",
        "graphMode": "none",
        "justifyMode": "auto",
        "orientation": "horizontal",
        "reduceOptions": {
          "calcs": [
            "lastNotNull"
          ],
          "fields": "",
          "values": false
        },
        "text": {
          "valueSize": 38
        },
        "textMode": "auto"
      },
      "pluginVersion": "9.1.6",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "hj6gjW44z"
          },
          "editorMode": "code",
          "expr": "sum(up{job=\"prestoworker\"})",
          "legendFormat": "__auto",
          "range": true,
          "refId": "A"
        }
      ],
      "title": "在线Worker数",
      "type": "stat"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "hj6gjW44z"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "palette-classic"
          },
          "custom": {
            "axisCenteredZero": false,
            "axisColorMode": "text",
            "axisLabel": "",
            "axisPlacement": "auto",
            "barAlignment": 0,
            "drawStyle": "line",
            "fillOpacity": 10,
            "gradientMode": "none",
            "hideFrom": {
              "legend": false,
              "tooltip": false,
              "viz": false
            },
            "lineInterpolation": "linear",
            "lineWidth": 1,
            "pointSize": 5,
            "scaleDistribution": {
              "type": "linear"
            },
            "showPoints": "never",
            "spanNulls": false,
            "stacking": {
              "group": "A",
              "mode": "none"
            },
            "thresholdsStyle": {
              "mode": "off"
            }
          },
          "links": [],
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          },
          "unit": "bytes"
        },
        "overrides": [
          {
            "matcher": {
              "id": "byName",
              "options": "Usage %"
            },
            "properties": [
              {
                "id": "custom.drawStyle",
                "value": "bars"
              },
              {
                "id": "custom.fillOpacity",
                "value": 100
              },
              {
                "id": "color",
                "value": {
                  "fixedColor": "#6d1f62",
                  "mode": "fixed"
                }
              },
              {
                "id": "custom.lineWidth",
                "value": 0
              },
              {
                "id": "unit",
                "value": "percentunit"
              },
              {
                "id": "min",
                "value": 0
              },
              {
                "id": "max",
                "value": 1
              }
            ]
          }
        ]
      },
      "gridPos": {
        "h": 9,
        "w": 12,
        "x": 0,
        "y": 4
      },
      "id": 18,
      "links": [],
      "options": {
        "legend": {
          "calcs": [
            "mean",
            "max"
          ],
          "displayMode": "table",
          "placement": "bottom",
          "showLegend": true
        },
        "tooltip": {
          "mode": "multi",
          "sort": "none"
        }
      },
      "pluginVersion": "9.1.6",
      "repeat": "memarea",
      "repeatDirection": "h",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "hj6gjW44z"
          },
          "editorMode": "code",
          "expr": "jvm_memory_bytes_used{area=\"heap\",job=\"prestocoordinator\"}",
          "legendFormat": "已用内存",
          "range": true,
          "refId": "A"
        },
        {
          "datasource": {
            "type": "prometheus",
            "uid": "hj6gjW44z"
          },
          "editorMode": "code",
          "expr": " jvm_memory_bytes_max{area=\"heap\",job=\"prestocoordinator\"}",
          "hide": false,
          "legendFormat": "总内存",
          "range": true,
          "refId": "B"
        },
        {
          "datasource": {
            "type": "prometheus",
            "uid": "hj6gjW44z"
          },
          "editorMode": "code",
          "expr": "jvm_memory_bytes_used{area=\"heap\",job=\"prestocoordinator\"} / jvm_memory_bytes_max >= 0",
          "hide": false,
          "legendFormat": "使用率",
          "range": true,
          "refId": "C"
        }
      ],
      "title": "PrestoCoordinator堆内存使用趋势",
      "type": "timeseries"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "hj6gjW44z"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "palette-classic"
          },
          "custom": {
            "axisCenteredZero": false,
            "axisColorMode": "text",
            "axisLabel": "",
            "axisPlacement": "auto",
            "barAlignment": 0,
            "drawStyle": "line",
            "fillOpacity": 10,
            "gradientMode": "none",
            "hideFrom": {
              "legend": false,
              "tooltip": false,
              "viz": false
            },
            "lineInterpolation": "linear",
            "lineWidth": 1,
            "pointSize": 5,
            "scaleDistribution": {
              "type": "linear"
            },
            "showPoints": "never",
            "spanNulls": false,
            "stacking": {
              "group": "A",
              "mode": "none"
            },
            "thresholdsStyle": {
              "mode": "off"
            }
          },
          "links": [],
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          },
          "unit": "s"
        },
        "overrides": []
      },
      "gridPos": {
        "h": 9,
        "w": 12,
        "x": 12,
        "y": 4
      },
      "id": 26,
      "links": [],
      "options": {
        "legend": {
          "calcs": [
            "lastNotNull",
            "max",
            "min"
          ],
          "displayMode": "table",
          "placement": "bottom",
          "showLegend": true
        },
        "tooltip": {
          "mode": "multi",
          "sort": "none"
        }
      },
      "pluginVersion": "9.1.6",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "hj6gjW44z"
          },
          "editorMode": "code",
          "expr": "increase(jvm_gc_collection_seconds_sum{job=\"prestocoordinator\"}[$__interval])",
          "format": "time_series",
          "interval": "60s",
          "intervalFactor": 1,
          "legendFormat": "{{gc}}",
          "metric": "jvm_gc_collection_seconds_sum",
          "range": true,
          "refId": "A",
          "step": 10
        }
      ],
      "title": "PrestoCoordinator GC时间趋势图",
      "type": "timeseries"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "hj6gjW44z"
      },
      "description": "",
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "thresholds"
          },
          "custom": {
            "align": "auto",
            "displayMode": "color-text",
            "inspect": false
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              }
            ]
          },
          "unit": "percent"
        },
        "overrides": [
          {
            "matcher": {
              "id": "byName",
              "options": "instance"
            },
            "properties": [
              {
                "id": "displayName",
                "value": "PrestoWorker"
              },
              {
                "id": "unit",
                "value": "short"
              },
              {
                "id": "decimals",
                "value": 2
              },
              {
                "id": "custom.align"
              }
            ]
          },
          {
            "matcher": {
              "id": "byName",
              "options": "Value"
            },
            "properties": [
              {
                "id": "displayName",
                "value": "堆内存使用率"
              },
              {
                "id": "custom.align",
                "value": "left"
              },
              {
                "id": "custom.displayMode",
                "value": "lcd-gauge"
              },
              {
                "id": "min",
                "value": 1
              },
              {
                "id": "max",
                "value": 100
              },
              {
                "id": "thresholds",
                "value": {
                  "mode": "absolute",
                  "steps": [
                    {
                      "color": "green",
                      "value": null
                    },
                    {
                      "color": "yellow",
                      "value": 80
                    },
                    {
                      "color": "red",
                      "value": 90
                    }
                  ]
                }
              },
              {
                "id": "color",
                "value": {
                  "mode": "continuous-GrYlRd"
                }
              }
            ]
          },
          {
            "matcher": {
              "id": "byName",
              "options": "area"
            },
            "properties": [
              {
                "id": "custom.hidden",
                "value": true
              }
            ]
          },
          {
            "matcher": {
              "id": "byName",
              "options": "job"
            },
            "properties": [
              {
                "id": "custom.hidden",
                "value": true
              }
            ]
          },
          {
            "matcher": {
              "id": "byName",
              "options": "Time"
            },
            "properties": [
              {
                "id": "custom.hidden",
                "value": true
              }
            ]
          }
        ]
      },
      "gridPos": {
        "h": 12,
        "w": 12,
        "x": 0,
        "y": 13
      },
      "id": 36,
      "interval": "",
      "links": [],
      "options": {
        "footer": {
          "enablePagination": true,
          "fields": "",
          "reducer": [
            "sum"
          ],
          "show": false
        },
        "frameIndex": 0,
        "showHeader": true,
        "sortBy": [
          {
            "desc": true,
            "displayName": "PrestoWorker"
          }
        ]
      },
      "pluginVersion": "9.1.6",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "hj6gjW44z"
          },
          "editorMode": "code",
          "expr": "jvm_memory_bytes_used{area=\"heap\",job=\"prestoworker\"}*100/jvm_memory_bytes_max{area=\"heap\",job=\"prestoworker\"}",
          "format": "table",
          "instant": true,
          "interval": "",
          "intervalFactor": 1,
          "legendFormat": "broker",
          "refId": "A"
        }
      ],
      "title": "PrestoWorker内存使用率",
      "transformations": [],
      "type": "table"
    }
  ],
  "refresh": "5s",
  "schemaVersion": 37,
  "style": "dark",
  "tags": [],
  "templating": {
    "list": [
      {
        "current": {
          "isNone": true,
          "selected": false,
          "text": "None",
          "value": ""
        },
        "datasource": {
          "type": "prometheus",
          "uid": "hj6gjW44z"
        },
        "definition": "label_values(up{job=\"trino\"},instance)",
        "hide": 0,
        "includeAll": false,
        "label": "节点",
        "multi": false,
        "name": "node",
        "options": [],
        "query": {
          "query": "label_values(up{job=\"trino\"},instance)",
          "refId": "Prometheus-node-Variable-Query"
        },
        "refresh": 1,
        "regex": "",
        "skipUrlSync": false,
        "sort": 1,
        "tagValuesQuery": "",
        "tagsQuery": "",
        "type": "query",
        "useTags": false
      }
    ]
  },
  "time": {
    "from": "now-6h",
    "to": "now"
  },
  "timepicker": {
    "refresh_intervals": [
      "5s",
      "10s",
      "30s",
      "1m",
      "5m",
      "15m",
      "30m",
      "1h",
      "2h",
      "1d"
    ],
    "time_options": [
      "5m",
      "15m",
      "1h",
      "6h",
      "12h",
      "24h",
      "2d",
      "7d",
      "30d"
    ]
  },
  "timezone": "",
  "title": "Presto",
  "uid": "7Iy7ibMIz",
  "version": 13,
  "weekStart": ""
}
```

#### 5.5 添加dotasophon presto模块总览

在grafana中复制面板链接

![image](https://github.com/datavane/datasophon/assets/62798940/02443af5-90ff-4dc8-9cbd-d42fee7b2ca4)

打开datasophon mysql t_ddh_cluster_service_dashboard表，添加presto面板
注意复制的面板连接后面要拼上&kiosk，如下图：

![image](https://github.com/datavane/datasophon/assets/62798940/977f9796-00ca-4016-82a9-d0f663659a00)

集成好的监控长这样

![image](https://github.com/datavane/datasophon/assets/62798940/d15fcc17-16bf-4604-acf8-014f29ae7713)

#### 5.6 集成告警

在 /opt/datasophon/prometheus/alert_rules 目录中添加presto告警配置文件 presto.yml

```shell
groups:
- name: PRESTO
  # rules：定义规则
  rules:
  # alert：告警规则的名称
  - alert: PrestoCoordinator进程存活
    expr: up{job="prestocoordinator"} != 1
    for: 15s
    labels:
      # severity: 指定告警级别。有三种等级，分别为warning、critical和emergency。严重等级依次递增。
      severity: exception
      clusterId: 1
      serviceRoleName: PrestoCoordinator
    annotations:
      # summary描述告警的概要信息
      # description用于描述告警的详细信息。
      summary: 重新启动
      description: "{{ $labels.job }}的{{ $labels.instance }}实例产生告警"
  - alert: PrestoWorker进程存活
    expr: up{job="prestoworker"} != 1
    for: 15s
    labels:
      # severity: 指定告警级别。有三种等级，分别为warning、critical和emergency。严重等级依次递增。
      severity: exception
      clusterId: 1
      serviceRoleName: PrestoWorker
    annotations:
      # summary描述告警的概要信息
      # description用于描述告警的详细信息。
      summary: 重新启动
      description: "{{ $labels.job }}的{{ $labels.instance }}实例产生告警"
```

重启prometheus，可以在UI上看到已经添加了告警

![image](https://github.com/datavane/datasophon/assets/62798940/75709858-b641-425c-b87f-f838a5dea1fc)
