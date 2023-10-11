#!/bin/bash


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

XDG_FOLDER_NAME=xdg-utils
XDG_TAR_NAME=xdg-utils.tar.gz
rpm -qa | grep xdg-utils
if [ "$?" == "0" ]; then
echo "rpm -qa | grep xdg-utils exists"
else
   yum -y install xdg-utils
   rpm -qa | grep xdg-utils
   if [ "$?" == "0" ]; then
   echo "xdg-utils install successfully"
   fi
fi

