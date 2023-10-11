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

MACROS_FOLDER_NAME=python-rpm-macros

MACROS_TAR_NAME=python-rpm-macros.tar.gz
rpm -qa | grep python-rpm-macros
if [ "$?" == "0" ]; then
  echo "python-rpm-macros exists"
else
  yum -y install python-rpm-macros
  rpm -qa | grep python-rpm-macros
  if [ "$?" == "0" ]; then
    echo "python-rpm-macros install successfully"
  fi
fi
