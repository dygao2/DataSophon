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

hostname=$1
modifyHostname() {
    echo "${hostname}" >/etc/hostname
    echo "HOSTNAME=${hostname}" >/etc/sysconfig/network
    echo "NOZEROCONF=yes" >>/etc/sysconfig/network
    hostnamectl set-hostname ${hostname}
    hostnamectl set-hostname --static ${hostname}
}

modifyHostname

echo "init-hostname.sh finished."
