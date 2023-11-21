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

SSH_FOLDER_NAME=ssh
SSH_TAR_NAME=ssh.tar.gz

ssh_rpm=$(rpm -qa | grep openssh)
if [[ "$?" == "0" ]]; then
    echo "ssh exists"
else
    yum -y install openssh
    echo "ssh-install finished."
fi

echo "init-sshpackage.sh finished."
echo "Done."
