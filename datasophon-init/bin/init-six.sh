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
SIX_FOLDER_NAME=six-1.15.0
SIX_TAR_NAME=six-1.15.0.tar.gz
tar -zxvf ${PACKAGES_PATH}/${SIX_TAR_NAME} -C ${PACKAGES_PATH}
cd ${PACKAGES_PATH}/${SIX_FOLDER_NAME}
python3 setup.py install
cd ${INIT_BIN_PATH}
echo "six-1.15.0 install successfully"
