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

GCC_FOLDER_NAME=gcc-c++
GCC_TAR_NAME=gcc-c++.tar.gz
rpm -qa | grep gcc-c++
if [ "$?" == "0" ]; then
  echo "rpm -qa | grep gcc-c++ exists"
else
  yum -y install gcc-c++
  rpm -qa | grep gcc-c++
  if [ "$?" == "0" ]; then
    echo "gcc-c++ install successfully"
  fi
fi
