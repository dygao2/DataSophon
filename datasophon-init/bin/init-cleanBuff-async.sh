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

BASE_PATH=$(
    cd ${BASE_DIR}
    pwd
)
echo "BASE_PATH: ${BASE_PATH}"

BIN_PATH=$(dirname "${BASE_PATH}")/bin
SCRIPTS_PATH=$(dirname "${BASE_PATH}")/scripts

echo "SCRIPTS_PATH: $SCRIPTS_PATH"

nohup sh ${INIT_BIN_PATH}/init-cleanBuff.sh >/dev/null 2>&1 &
#nohup sh $SCRIPTS_PATH/repair-cleanBuff.sh &
