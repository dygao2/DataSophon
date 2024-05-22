### 1、构建安装包

```shell
mkdir openldap-2.4.44-22.el7
cd openldap-2.4.44-22.el7
touch add-memberof.ldif
touch base.ldif
touch changeDomain.ldif
touch control_openldap.sh
touch refint1.ldif
touch refint2.ldif
cd ..
chmod -R 755 ./openldap-2.4.44-22.el7
tar czf openldap-2.4.44-22.el7.tar.gz openldap-2.4.44-22.el7
md5sum openldap-2.4.44-22.el7.tar.gz
echo '146e20ea21a85be1182f88627f69b7e8' > openldap-2.4.44-22.el7.tar.gz.md5
cp ./openldap-2.4.44-22.el7.tar.gz ./openldap-2.4.44-22.el7.tar.gz.md5 /opt/datasophon/DDP/packages/
```

add-memberof.ldif

```shell
dn: cn=module{0},cn=config
cn: modulle{0}
objectClass: olcModuleList
objectclass: top
olcModuleload: memberof.la
olcModulePath: /usr/lib64/openldap
 
dn: olcOverlay={0}memberof,olcDatabase={2}hdb,cn=config
objectClass: olcConfig
objectClass: olcMemberOf
objectClass: olcOverlayConfig
objectClass: top
olcOverlay: memberof
olcMemberOfDangling: ignore
olcMemberOfRefInt: TRUE
olcMemberOfGroupOC: groupOfUniqueNames
olcMemberOfMemberAD: uniqueMember
```

base.ldif

```shell
dn: dc=ldap,dc=com
dc: ldap
objectClass: top
objectClass: domain

dn: cn=root,dc=ldap,dc=com
objectClass: organizationalRole
cn: root
description: LDAP root

dn: ou=People,dc=ldap,dc=com
objectClass: organizationalUnit
ou: People

dn: ou=Group,dc=ldap,dc=com
objectClass: organizationalUnit
ou: Group
```

control_openldap.sh

```shell
#!/bin/bash

start_slapd() {
    systemctl start slapd
}

stop_slapd() {
    systemctl stop slapd
}

status_slapd() {
    if systemctl is-active --quiet slapd; then
        echo "OpenLDAP is running."
        exit 0
    else
        echo "OpenLDAP is not running."
        exit 1
    fi
}

case "$1" in
    start)
        start_slapd
        ;;
    stop)
        stop_slapd
        ;;
    status)
        status_slapd
        ;;
    *)
        echo "Usage: $0 {start|stop|status}"
        exit 1
        ;;
esac

exit 0

```

refint1.ldif

```shell
dn: cn=module{0},cn=config
add: olcmoduleload
olcmoduleload: refint
```

refint2.ldif

```shell
dn: olcOverlay=refint,olcDatabase={2}hdb,cn=config
objectClass: olcConfig
objectClass: olcOverlayConfig
objectClass: olcRefintConfig
objectClass: top
olcOverlay: refint
olcRefintAttribute: memberof uniqueMember  manager owner
```

### 2、元数据文件

```shell
cd /opt/apps/datasophon-manager-1.2.0/conf/meta/DDP-1.2.0
mkdir OPENLDAP
cd OPENLDAP
touch service_ddl.json
```

service_ddl.json

```shell
{
  "name": "OPENLDAP",
  "label": "Openldap",
  "description": "开放式轻量级目录访问协议",
  "version": "2.4.44-22.el7",
  "sortNum": 33,
  "dependencies": [],
  "packageName": "openldap-2.4.44-22.el7.tar.gz",
  "decompressPackageName": "openldap-2.4.44-22.el7",
  "roles": [
    {
      "name": "OpenldapServer",
      "label": "OpenldapServer",
      "roleType": "master",
      "cardinality": "1",
      "sortNum": 1,
      "logFile": "/var/log/slapd/slapd.log",
      "jmxPort": "",
      "startRunner": {
        "timeout": "60",
        "program": "control_openldap.sh",
        "args": [
          "start"
        ]
      },
      "stopRunner": {
        "timeout": "60",
        "program": "control_openldap.sh",
        "args": [
          "stop"
        ]
      },
      "statusRunner": {
        "timeout": "60",
        "program": "control_openldap.sh",
        "args": [
          "status"
        ]
      }
    }
  ],
  "configWriter": {
    "generators": [
      {
        "filename": "changeDomain.ldif",
        "configFormat": "custom",
        "outputDirectory": "",
        "templateName": "changeDomain.flt",
        "includeParams": [
          "olcRootDN",
          "olcSuffix"
        ]
      }
    ]
  },
  "parameters": [
    {
      "name": "olcRootDN",
      "label": "olcRootDN",
      "description": "具有对LDAP执行所有管理活动的无限制访问权限的用户的根专有名称（DN）条目，如root用户。",
      "required": true,
      "configType": "map",
      "type": "input",
      "value": "cn=root,dc=ldap,dc=com",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": "cn=root,dc=ldap,dc=com"
    },
    {
      "name": "olcSuffix",
      "label": "olcSuffix",
      "description": "数据库后缀，它是LDAP服务器提供信息的域名。简单来说，更改为自己的域名。",
      "required": true,
      "configType": "map",
      "type": "input",
      "value": "dc=ldap,dc=com",
      "configurableInWizard": true,
      "hidden": true,
      "defaultValue": "dc=ldap,dc=com"
    }
  ]
}
```

### 3、新增worker策略

新增 com.datasophon.worker.strategy.OpenldapHandlerStrategy

```shell
package com.datasophon.worker.strategy;

import com.datasophon.common.Constants;
import com.datasophon.common.command.ServiceRoleOperateCommand;
import com.datasophon.common.enums.CommandType;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.ShellUtils;
import com.datasophon.worker.handler.ServiceHandler;

import java.sql.SQLException;

public class OpenldapHandlerStrategy extends AbstractHandlerStrategy implements ServiceRoleStrategy {

    public OpenldapHandlerStrategy(String serviceName, String serviceRoleName) {
        super(serviceName, serviceRoleName);
    }

    @Override
    public ExecResult handler(ServiceRoleOperateCommand command) throws SQLException, ClassNotFoundException {
        ServiceHandler serviceHandler = new ServiceHandler(command.getServiceName(), command.getServiceRoleName());
        String workPath = Constants.INSTALL_PATH + Constants.SLASH + command.getDecompressPackageName();

        if (command.getCommandType().equals(CommandType.INSTALL_SERVICE)) {
            // 安装服务
            ShellUtils.exceShell("yum -y install openldap compat-openldap openldap-clients openldap-servers openldap-servers-sql openldap-devel migrationtools");
            logger.info("yum install openldap success");

            // 配置OpenLDAP数据库
            logger.info("start config database");
            ShellUtils.exceShell("cp /usr/share/openldap-servers/DB_CONFIG.example /var/lib/ldap/DB_CONFIG");
            ShellUtils.exceShell("chown ldap:ldap -R /var/lib/ldap");
            ShellUtils.exceShell("chmod 700 -R /var/lib/ldap");
            logger.info("config database success");

            // 启动
            ShellUtils.exceShell("systemctl enable slapd");
            ShellUtils.exceShell("systemctl start slapd");

            // 配置域名等
            ShellUtils.exceShell("slappasswd -s 123456 |sed -e 's#{SSHA}#olcRootPW: {SSHA}#g' >> " + workPath + "/changeDomain.ldif");
            ShellUtils.exceShell("ldapmodify -Y EXTERNAL -H ldapi:/// -f " + workPath + "/changeDomain.ldif");

            // 导入基本Schema
            logger.info("import database schema");
            ShellUtils.exceShell("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/cosine.ldif");
            ShellUtils.exceShell("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/core.ldif");
            ShellUtils.exceShell("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/collective.ldif");
            ShellUtils.exceShell("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/corba.ldif");
            ShellUtils.exceShell("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/duaconf.ldif");
            ShellUtils.exceShell("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/dyngroup.ldif");
            ShellUtils.exceShell("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/inetorgperson.ldif");
            ShellUtils.exceShell("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/java.ldif");
            ShellUtils.exceShell("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/misc.ldif");
            ShellUtils.exceShell("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/nis.ldif");
            ShellUtils.exceShell("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/openldap.ldif");
            ShellUtils.exceShell("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/pmi.ldif");
            ShellUtils.exceShell("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/ppolicy.ldif");
            logger.info("import success");

            // 开启memberof支持
            ShellUtils.exceShell(" ldapadd -Q -Y EXTERNAL -H ldapi:/// -f " + workPath + "/add-memberof.ldif");
            ShellUtils.exceShell(" ldapadd -Q -Y EXTERNAL -H ldapi:/// -f " + workPath + "/refint1.ldif");
            ShellUtils.exceShell(" ldapadd -Q -Y EXTERNAL -H ldapi:/// -f " + workPath + "/refint2.ldif");

            // 开启日志
            ShellUtils.exceShell("echo \"local4.* /var/log/slapd/slapd.log\" >> /etc/rsyslog.conf");
            ShellUtils.exceShell("systemctl restart rsyslog");

            // 添加基础用户
            ShellUtils.exceShell("ldapadd -x -D cn=root,dc=ldap,dc=com -w 123456 -f " + workPath + "/base.ldif");
        }

        ExecResult startResult = serviceHandler.start(command.getStartRunner(), command.getStatusRunner(),
                command.getDecompressPackageName(), command.getRunAs());
        return startResult;
    }
}

```

com.datasophon.worker.strategy.ServiceRoleStrategyContext 新增代码

```shell
map.put("OpenldapServer", new OpenldapHandlerStrategy("OPENLDAP", "OpenldapServer"));
```

新增com.datasophon.api.strategy.OpenldapHandlerStrategy

```java
package com.datasophon.api.strategy;

import cn.hutool.core.util.ObjUtil;
import com.datasophon.api.load.GlobalVariables;
import com.datasophon.api.utils.ProcessUtils;
import com.datasophon.common.model.ServiceConfig;
import com.datasophon.common.model.ServiceRoleInfo;
import com.datasophon.dao.entity.ClusterServiceRoleInstanceEntity;

import java.util.List;
import java.util.Map;

public class OpenldapHandlerStrategy implements ServiceRoleStrategy {
    @Override
    public void handler(Integer clusterId, List<String> hosts) {
        Map<String, String> globalVariables = GlobalVariables.get(clusterId);
        if (!globalVariables.containsKey("${openldapAddr}") || ObjUtil.isNull(globalVariables.get("${openldapAddr}"))) {
            if (!hosts.isEmpty()) {
                ProcessUtils.generateClusterVariable(globalVariables, clusterId, "${openldapAddr}", "ldap://" + hosts.get(0) + ":389");
            }
        }
    }

    @Override
    public void handlerConfig(Integer clusterId, List<ServiceConfig> list) {

    }

    @Override
    public void getConfig(Integer clusterId, List<ServiceConfig> list) {

    }

    @Override
    public void handlerServiceRoleInfo(ServiceRoleInfo serviceRoleInfo, String hostname) {

    }

    @Override
    public void handlerServiceRoleCheck(ClusterServiceRoleInstanceEntity roleInstanceEntity, Map<String, ClusterServiceRoleInstanceEntity> map) {

    }
}

```

修改com.datasophon.api.strategy.ServiceRoleStrategyContext

```java
map.put("OpenldapServer", new OpenldapHandlerStrategy());
```

打包部署

### 4、重启

各节点worker重启

```shell
sh /opt/datasophon/datasophon-worker/bin/datasophon-worker.sh restart worker debug
```

主节点重启api

```shell
sh /opt/apps/datasophon-manager-1.2.0/bin/datasophon-api.sh restart api debug
```

### 5、服务器openldap服务完全清除命令

```shell
systemctl stop slapd
systemctl disable slapd
yum -y remove openldap-servers openldap-clients
rm -rf /var/lib/ldap
rm -rf /etc/openldap/slapd.d
rm -rf /opt/datasophon/openldap-2.4.44-22.el7
rm -rf /opt/datasophon/openldap
```

### 5、用户操作命令

#### 5.1 添加用户

```shell
dn: uid=user1,ou=People,dc=ldap,dc=com
objectClass: top
objectClass: person
objectClass: organizationalPerson
objectClass: inetOrgPerson
objectClass: shadowAccount
objectClass: posixAccount
uid: user1
cn: User1
sn: User1
givenName: User
userPassword: {SSHA}zXiU4p/UEy20i054J4Jzu+pSJ/C+N9XE
shadowMax: 90
shadowWarning: 7
loginShell: /bin/bash
uidNumber: 1001
gidNumber: 1001
homeDirectory: /home/user1

dn: uid=user2,ou=People,dc=ldap,dc=com
objectClass: top
objectClass: person
objectClass: organizationalPerson
objectClass: inetOrgPerson
objectClass: shadowAccount
objectClass: posixAccount
uid: user2
cn: User2
sn: User2
givenName: User
userPassword: {SSHA}zXiU4p/UEy20i054J4Jzu+pSJ/C+N9XE
shadowMax: 90
shadowWarning: 7
loginShell: /bin/bash
uidNumber: 1002
gidNumber: 1002
homeDirectory: /home/user2
```

```shell
ldapadd -x -D "cn=root,dc=ldap,dc=com" -W -f ./create.ldif
```

#### 5.2 删除用户

```shell
dn: uid=user1,ou=People,dc=ldap,dc=com
changetype: delete

dn: uid=user2,ou=People,dc=ldap,dc=com
changetype: delete
```

```shell
ldapmodify -x -D "cn=root,dc=ldap,dc=com" -W -f ./delete.ldif
```

