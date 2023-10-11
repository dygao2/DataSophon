#!/bin/bash

num1="$1"
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
#mysql -udatasophon -p'datasophon' <${INIT_SQL_PATH}/V1.1.0_DDL.sql;
#mysql -udatasophon -p'datasophon' <${INIT_SQL_PATH}/V1.1.0_DML.sql;

mysql -udatasophon -p'datasophon' <<EOF
source ${INIT_SQL_PATH}/V1.1.0_DML.sql;
EOF
