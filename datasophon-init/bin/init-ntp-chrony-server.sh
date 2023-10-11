#!/bin/bash
#init-ntp-server.sh
#初始化ntp server

if [ $UID -ne 0 ]; then
    echo 'Non root user. Please run as root.'
    exit 1
fi

export GATEWAY_IP=0.0.0.0

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

function network() {
    local timeout=8
    local target=www.baidu.com
    local ret_code=$(curl -I -s --connect-timeout ${timeout} -m 20 ${target} -w %{http_code} | tail -n1)
    if [ "x$ret_code" = "x200" ]; then
        return 1
    else
        return 0
    fi
    return 0
}

rpm -qa | grep chrony-
if [ "$?" == "0" ]; then
    echo "ntp-chrony exists"
else
    yum -y install chrony
fi

network
if [ $? -eq 0 ]; then
    echo -e "\033[31m 由于机器没有联网，请再次确认机器时间是否设置正确！ \033[0m"
    echo -e "本机被设置为NTP服务端......"
    cat >/etc/chrony.conf <<EOF

server 127.0.0.1 iburst    

driftfile /var/lib/chrony/drift

makestep 1.0 3

rtcsync

allow all

local stratum 10

keyfile /etc/chrony.keys

leapsectz right/UTC

logdir /var/log/chrony

EOF
else
    cat >/etc/chrony.conf <<EOF

server ntp.aliyun.com iburst    

driftfile /var/lib/chrony/drift

makestep 1.0 3

rtcsync

allow all

local stratum 10

keyfile /etc/chrony.keys

leapsectz right/UTC

logdir /var/log/chrony

EOF
fi

systemctl enable chronyd
systemctl restart chronyd

echo "init-ntp-chrony-server.sh finished."

echo "Done."
