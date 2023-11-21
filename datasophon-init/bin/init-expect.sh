#!/bin/bash

# example: sh init-expect.sh
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

EXPECT_FOLDER_NAME=expect
EXPECT_TAR_NAME=expect.tar.gz
#存在python3-pexpect-4.3.1-3 导致判断不准确，直接安装
#rpm -qa | grep expect-5
#if [ "$?" == "0" ]; then
#  echo "expect exists"
#else
#  yum -y install expect
#  rpm -qa | grep expect-5.45
#  if [ "$?" == "0" ]; then
#    echo "expect exists"
#    echo "init-expect.sh finished."
#    echo "Done."
#  fi
#fi

yum -y install expect
echo "init-expect.sh finished."
echo "Done."
