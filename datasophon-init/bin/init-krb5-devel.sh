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
echo "${PACKAGES_PATH}"
KRB5_FOLDER_NAME=krb5-devel
KRB5_TAR_NAME=krb5-devel.tar.gz
rpm -qa | grep krb5-devel
if [ "$?" == "0" ]; then
  echo "rpm -qa | grep krb5-devel exists"
else
  yum -y install krb5-devel
  rpm -qa | grep krb5-devel
  if [ "$?" == "0" ]; then
    echo "krb5-devel install successfully"
  fi
fi
