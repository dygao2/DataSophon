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

SRPM_FOLDER_NAME=python-srpm-macros

SRPM_TAR_NAME=python-srpm-macros.tar.gz
rpm -qa | grep python-srpm-macros
if [ "$?" == "0" ]; then
echo "python-srpm-macros exists"
else
   yum -y install python-srpm-macros
   rpm -qa | grep python-srpm-macros
   if [ "$?" == "0" ]; then
   echo "python-srpm-macros install successfully"
   fi
fi

