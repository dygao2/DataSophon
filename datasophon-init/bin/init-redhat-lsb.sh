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

REDHAT_LSB_FOLDER_NAME=redhat-lsb

REDHAT_LSB_TAR_NAME=redhat-lsb.tar.gz
rpm -qa | grep redhat-lsb
if [ "$?" == "0" ]; then
  echo "redhat-lsb exists"
else
  yum -y install redhat-lsb*
  rpm -qa | grep redhat-lsb*
  if [ "$?" == "0" ]; then
    echo "redhat-lsb install successfully"
  fi
fi
