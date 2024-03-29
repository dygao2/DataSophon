#!/bin/bash

num1="$1"
ios=$2
if [ $UID -ne 0 ]; then
  echo Non root user. Please run as root.
  exit 1
fi
if [ -L $0 ]; then
  BASE_DIR=$(dirname $(readlink $0))
else
  BASE_DIR=$(dirname $0)
fi
BASE_PATH=$(
  cd ${BASE_DIR}
  pwd
)
INIT_PATH=$(dirname "${BASE_PATH}")
echo "INIT_PATH: ${INIT_PATH}"
INIT_BIN_PATH=${INIT_PATH}/bin
echo "INIT_BIN_PATH: ${INIT_BIN_PATH}"
INIT_SBIN_PATH=${INIT_PATH}/sbin
echo "INIT_SBIN_PATH: ${INIT_SBIN_PATH}"
PACKAGES_PATH=${INIT_PATH}/packages
echo "PACKAGES_PATH: ${PACKAGES_PATH}"

mariadb_rpm=$(rpm -qa | grep mariadb)
if [[ "$?" == "0" ]]; then
  echo "exist mariadb"
  rpm -qa | grep mariadb | xargs rpm -e --nodeps
fi
mysql_rpm=$(rpm -qa | grep mysql)
if [[ "$?" == "0" ]]; then
  echo "exist mysql"
  echo "开始卸载已存在的 mysql..............."
  systemctl stop mysqld
  rpm -qa | grep mysql | xargs rpm -e
  rm -rf /var/lib/mysql
  rm -rf /usr/sbin/mysqld
  rm -rf /usr/local/mysql
  rm -rf /etc/my.cnf
  rm -rf /var/log/mysqld.log
  rm -rf /var/log/mysql.log
fi

rpm -qa | grep zlib-devel
if [ "$?" == "0" ]; then
  echo "zlib-devel exists"
else
  yum -y install zlib-devel
  rpm -qa | grep zlib-devel
  if [ "$?" == "0" ]; then
    echo "zlib-devel install successfully"
  fi
fi

rpm -qa | grep bzip2-devel
if [ "$?" == "0" ]; then
  echo "bzip2-devel exists"
else
  yum -y install bzip2-devel
  rpm -qa | grep bzip2-devel
  if [ "$?" == "0" ]; then
    echo "bzip2-devel install successfully"
  fi
fi

rpm -qa | grep openssl-devel
if [ "$?" == "0" ]; then
  echo "openssl-devel exists"
else
  yum -y install openssl-devel
  rpm -qa | grep openssl-devel
  if [ "$?" == "0" ]; then
    echo "openssl-devel install successfully"
  fi
fi

rpm -qa | grep ncurses-devel
if [ "$?" == "0" ]; then
  echo "ncurses-devel exists"
else
  yum -y install ncurses-devel
  rpm -qa | grep ncurses-devel
  if [ "$?" == "0" ]; then
    echo "ncurses-devel install successfully"
  fi
fi

if [ "${ios}" == "centos7" ]; then
  rpm -qa | grep libaio
  if [ "$?" == "0" ]; then
    echo "libaio exists"
  else
    yum -y install libaio
    rpm -qa | grep libaio
    if [ "$?" == "0" ]; then
      echo "libaio install successfully"
    fi
  fi
fi

MYSQL_FOLDER_NAME=mysql-community-8.0.28.el8.x86_64
MYSQL_TAR_NAME=mysql-community-8.0.28.el8.x86_64.tar.gz

echo "mysql start install mysql-community-8.0.28-1.el8.x86_64........."
tar -zxvf ${PACKAGES_PATH}/${MYSQL_TAR_NAME} -C ${PACKAGES_PATH}
rpm -ivh ${PACKAGES_PATH}/${MYSQL_FOLDER_NAME}/mysql-community-common-8.0.28-1.el8.x86_64.rpm
rpm -ivh ${PACKAGES_PATH}/${MYSQL_FOLDER_NAME}/mysql-community-client-plugins-8.0.28-1.el8.x86_64.rpm
rpm -ivh ${PACKAGES_PATH}/${MYSQL_FOLDER_NAME}/mysql-community-libs-8.0.28-1.el8.x86_64.rpm
rpm -ivh ${PACKAGES_PATH}/${MYSQL_FOLDER_NAME}/mysql-community-devel-8.0.28-1.el8.x86_64.rpm
rpm -ivh ${PACKAGES_PATH}/${MYSQL_FOLDER_NAME}/mysql-community-client-8.0.28-1.el8.x86_64.rpm
rpm -ivh ${PACKAGES_PATH}/${MYSQL_FOLDER_NAME}/mysql-community-icu-data-files-8.0.28-1.el8.x86_64.rpm
rpm -ivh ${PACKAGES_PATH}/${MYSQL_FOLDER_NAME}/mysql-community-server-8.0.28-1.el8.x86_64.rpm

mysqld --initialize --user=mysql
systemctl start mysqld
systemctl enable mysqld
sleep 2
echo "${num1}"

if [ $(systemctl status mysqld | grep running | wc -l) -eq 1 ]; then
  echo "mysql在运行"
  tmp_passwd=$(grep 'temporary password' /var/log/mysqld.log | awk '{print $NF}')
  echo "临时密码：${tmp_passwd}"
  #mysql -uroot -p"${tmp_passwd}" --connect-expired-password -e "ALTER USER 'root'@'%' IDENTIFIED BY '${num1}';"
  #mysql -uroot -p"${num1}" -e "ALTER USER 'root'@'%' IDENTIFIED BY '${num1}' PASSWORD EXPIRE NEVER;"
  #mysql -uroot -p"${num1}" -e "ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY '${num1}';"
  #mysql -uroot -p"${num1}" -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;"
  #mysql -uroot -p"${num1}" -e "FLUSH PRIVILEGES;"

  /usr/bin/mysqladmin -uroot -p''$tmp_passwd'' password ''$num1''
  mysql -uroot -p''$num1'' -e "update mysql.user set host='%' where user ='root';"
  mysql -uroot -p''$num1'' -e "FLUSH PRIVILEGES;"
  mysql -uroot -p''$num1'' -e "ALTER USER 'root'@'%' IDENTIFIED BY '$num1' PASSWORD EXPIRE NEVER;"
  echo "num1：'$num1'"
  mysql -uroot -p''$num1'' -e "ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY '$num1';"
  mysql -uroot -p''$num1'' -e "FLUSH PRIVILEGES;"

  cat >/etc/my.cnf <<EOF
[mysqld]    

character_set_server=utf8mb4

collation_server=utf8mb4_general_ci

default-storage-engine=INNODB 

explicit_defaults_for_timestamp=true

max_connections=3600

EOF

  systemctl restart mysqld
  echo "install mysql-community-8.0.28-1.el8.x86_64  finished........."
else
  echo "####################################################################"
  echo "mysql install finished & but service startup failed & checkup /var/log/mysqld.log"
fi
