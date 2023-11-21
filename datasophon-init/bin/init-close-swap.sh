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

#swapoff /dev/centos/swap

# /etc/fstab
# /dev/mapper/centos-swap swap                    swap    defaults        0 0
sed -ri 's/.*swap.*/#&/' /etc/fstab

echo 0 >/proc/sys/vm/swappiness

SWAP_STR=$(cat /etc/sysctl.conf | grep vm.swappiness)
if [ -z "${SWAP_STR}" ]; then
    echo "vm.swappiness=0" >>/etc/sysctl.conf
else
    sed -i '/vm.swappiness/d' /etc/sysctl.conf
    echo "vm.swappiness=0" >>/etc/sysctl.conf
fi

sysctl vm.swappiness=0
swapoff -a && swapon -a
sysctl -p

echo "Swap is closed."
echo "init-close-swap.sh finished."
echo "Done."
