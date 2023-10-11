#!/bin/bash

# example: sh init-system-conf.sh
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


# vi /etc/systemd/system.conf
# DefaultLimitNOFILE=102400000
# DefaultLimitNPROC=102400000
sed -i '/DefaultLimitNOFILE=/d' /etc/systemd/system.conf
sed -i '/DefaultLimitNPROC=/d' /etc/systemd/system.conf

cat << EOF >> /etc/systemd/system.conf
DefaultLimitNOFILE=1024000
DefaultLimitNPROC=1024000
EOF

# vi /etc/security/limits.conf
#*            soft    fsize           unlimited
#*            hard    fsize           unlimited
#*            soft    cpu             unlimited
#*            hard    cpu             unlimited
#*            soft    as              unlimited
#*            hard    as              unlimited
#*            soft    nofile          10240000
#*            hard    nofile          10240000
#*            soft    nproc           10240000
#*            hard    nproc           10240000

sed -i '/*            soft    fsize/d' /etc/security/limits.conf
sed -i '/*            hard    fsize/d' /etc/security/limits.conf
sed -i '/*            soft    cpu/d' /etc/security/limits.conf
sed -i '/*            hard    cpu/d' /etc/security/limits.conf
sed -i '/*            soft    as/d' /etc/security/limits.conf
sed -i '/*            hard    as/d' /etc/security/limits.conf
sed -i '/*            soft    nofile/d' /etc/security/limits.conf
sed -i '/*            hard    nofile/d' /etc/security/limits.conf
sed -i '/*            soft    nproc/d' /etc/security/limits.conf
sed -i '/*            hard    nproc/d' /etc/security/limits.conf

cat << EOF >> /etc/security/limits.conf
*            soft    fsize           unlimited
*            hard    fsize           unlimited
*            soft    cpu             unlimited
*            hard    cpu             unlimited
*            soft    as              unlimited
*            hard    as              unlimited
*            soft    nofile          1048576
*            hard    nofile          1048576
*            soft    nproc           unlimited
*            hard    nproc           unlimited
EOF

# vi /etc/security/limits.d/20-nproc.conf
#*          soft    nproc     102400000
#root       soft    nproc     unlimited

cat << EOF > /etc/security/limits.d/20-nproc.conf
# Default limit for number of user's processes to prevent
# accidental fork bombs.
# See rhbz #432903 for reasoning.

*          soft    nproc     unlimited
root       soft    nproc     unlimited
EOF

echo "kernel.pid_max=1000000" >> /etc/sysctl.conf
sysctl -p

echo "init-system-conf finished."
echo "Done."
