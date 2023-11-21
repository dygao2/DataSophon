#!/bin/bash
# init-ntp-slave.sh
# example: sh init-ntp-slave.sh 192.168.1.11
NTP_SERVER_IP=$1
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

timedatectl set-timezone Asia/Shanghai
rpm -qa | grep chrony-
if [ "$?" == "0" ]; then
  echo "ntp-chrony exists"
else
  yum -y install chrony
fi

cat >/etc/chrony.conf <<EOF
   
server ${NTP_SERVER_IP} iburst    

driftfile /var/lib/chrony/drift

makestep 1.0 3

rtcsync

allow all

local stratum 10

keyfile /etc/chrony.keys

leapsectz right/UTC

logdir /var/log/chrony

EOF

echo "${NTP_SERVER_IP}"
systemctl enable chronyd
systemctl restart chronyd

echo "init-ntp-chrony-slave.sh finished."

echo "Done."
