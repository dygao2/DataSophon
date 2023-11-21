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

PYTHON36_DEVEL_FOLDER_NAME=python36-devel

PYTHON36_DEVEL_TAR_NAME=python36-devel.tar.gz
rpm -qa | grep python3
if [ "$?" == "0" ]; then
  echo "rpm -qa | grep python36-devel exists"
else
  yum -y install python36-devel
  rpm -qa | grep python3
  if [ "$?" == "0" ]; then
    echo "python36-devel install successfully"
  fi
fi
