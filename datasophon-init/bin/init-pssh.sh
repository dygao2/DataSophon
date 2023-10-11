#!/bin/bash
ios=$1
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
PSSH_FOLDER_NAME=pssh
PSSH_TAR_NAME=pssh.tar.gz

if [ "${ios}" == "openEuler" ]; then
  tar -zxvf ${PACKAGES_PATH}/${PSSH_TAR_NAME} -C ${PACKAGES_PATH}
  rpm -ivh ${PACKAGES_PATH}/${PSSH_FOLDER_NAME}/pssh-2.3.4-1.el9.noarch.rpm
  rpm -qa | grep pssh
  if [ "$?" == "0" ]; then
    echo "pssh-2.3.4-1.el9.noarch.rpm install successfully"
  fi
fi

if [ "${ios}" == "centos8" ]; then
  tar -zxvf ${PACKAGES_PATH}/${PSSH_TAR_NAME} -C ${PACKAGES_PATH}
  rpm -ivh ${PACKAGES_PATH}/${PSSH_FOLDER_NAME}/pssh-2.3.1-29.el8.noarch.rpm
  rpm -qa | grep pssh
  if [ "$?" == "0" ]; then
    echo "pssh-2.3.1-29.el8.noarch.rpm install successfully"
  fi
fi

if [ "${ios}" == "centos7" ]; then
  tar -zxvf ${PACKAGES_PATH}/${PSSH_TAR_NAME} -C ${PACKAGES_PATH}
  rpm -ivh ${PACKAGES_PATH}/${PSSH_FOLDER_NAME}/pssh-2.3.1-5.el7.noarch.rpm
  rpm -qa | grep pssh
  if [ "$?" == "0" ]; then
    echo "pssh-2.3.1-5.el7.noarch.rpm install successfully"
  fi
fi
