#!/bin/bash

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
INIT_SQL_PATH=${INIT_PATH}/sql
echo "INIT_SQL_PATH: ${INIT_SQL_PATH}"


if [ $(systemctl status mysqld | grep running | wc -l) -eq 1 ]; then

  mysql -uroot -p''$num1'' <<EOF
CREATE USER 'datasophon'@'%' IDENTIFIED BY 'datasophon';
ALTER USER 'datasophon'@'%' IDENTIFIED BY 'datasophon' PASSWORD EXPIRE NEVER;
ALTER USER 'datasophon'@'%' IDENTIFIED WITH mysql_native_password BY 'datasophon';
GRANT ALL PRIVILEGES ON *.* TO 'datasophon'@'%';
FLUSH PRIVILEGES;
CREATE DATABASE IF NOT EXISTS datasophon DEFAULT CHARACTER SET utf8;
EOF
  echo " init user datasophon finished."
  echo " init database datasophon finished."

else
  systemctl start mysqld
  systemctl enable mysqld

fi
echo " init db_datasophon finished."
echo "Done."
