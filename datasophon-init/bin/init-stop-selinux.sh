#!/bin/bash
# example: sh init-stop-firewall.sh
if [ $UID -ne 0 ]; then
  echo Non root user. Please run as root.
  exit 1
fi
if [ -L $0 ]
then
    BASE_DIR=`dirname $(readlink $0)`
else
    BASE_DIR=`dirname $0`
fi
BASE_PATH=$(cd ${BASE_DIR}; pwd)
INIT_PATH=$(dirname "${BASE_PATH}")
echo "INIT_PATH: ${INIT_PATH}"
INIT_BIN_PATH=${INIT_PATH}/bin
echo "INIT_BIN_PATH: ${INIT_BIN_PATH}"
INIT_SBIN_PATH=${INIT_PATH}/sbin
echo "INIT_SBIN_PATH: ${INIT_SBIN_PATH}"
PACKAGES_PATH=${INIT_PATH}/packages
echo "PACKAGES_PATH: ${PACKAGES_PATH}"


SELINUX_STATUS=`getenforce`
SELINUX_CONFIG_PATH="/etc/selinux/config"
if [[ ${SELINUX_STATUS} == "Enforcing" ]]
then
    echo "Closing SELINUX."
    setenforce 0
    echo "Disabling SELINUX."
    sed -i "s/SELINUX=enforcing/SELINUX=disabled/g" ${SELINUX_CONFIG_PATH}
    echo "SELINUX closed."
else
    echo "SELINUX closed."
fi

echo "init-stop-selinux.sh finished."
echo "Done."
