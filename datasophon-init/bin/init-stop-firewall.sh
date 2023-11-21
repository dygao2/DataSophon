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
DATASOPHON_PATH=$(dirname "${INIT_PATH}")
echo "DATASOPHON_PATH: ${DATASOPHON_PATH}"
INIT_BIN_PATH=${INIT_PATH}/bin
echo "INIT_BIN_PATH: ${INIT_BIN_PATH}"
INIT_SBIN_PATH=${INIT_PATH}/sbin
echo "INIT_SBIN_PATH: ${INIT_SBIN_PATH}"
PACKAGES_PATH=${INIT_PATH}/packages
echo "PACKAGES_PATH: ${PACKAGES_PATH}"
FIREWALL_STATUS=`firewall-cmd --state`
if [[ ${FIREWALL_STATUS} == "running" ]]
then
    echo "Closing firewall."
    systemctl stop firewalld.service
    systemctl disable firewalld.service
    echo "Firewall closed."
else
    echo "Firewall closed."
fi
echo "init-stop-firewall.sh finished."
echo "Done."
