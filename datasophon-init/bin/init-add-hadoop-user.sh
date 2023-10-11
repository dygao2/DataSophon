#!/bin/bash

# example: sh init-ssh-gen-key.sh

filePath=$1
GROUP=hadoop
USER=hadoop

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
egrep "^$GROUP" /etc/group >&/dev/null
if [ $? -ne 0 ]; then
    groupadd $GROUP
    echo "Successfully added GROUP: hadoop"
fi
egrep "^$USER" /etc/passwd >&/dev/null
if [ $? -ne 0 ]; then
    useradd -g ${USER} ${USER}
    echo "Successfully added USER: hadoop passwd: hadoop"
fi

echo "init-add-hadoop-user.sh."
echo "Done."
