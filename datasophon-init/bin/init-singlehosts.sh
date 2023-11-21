#!/bin/bash

hostAllInfoPath="$1"
initAllHostNums="$2"

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
ETC_HOST=/etc/hosts
sed -i '/#modify etc hosts start/,/#modify etc hosts end/d' /etc/hosts
modifyHosts() {
  cat /etc/hosts | grep yum
  if [ $? -eq 0 ]; then
    sed -i '/#modify yum mapping hosts start/i\#modify etc hosts start' /etc/hosts
    while read line || [[ -n ${line} ]]; do
      ip=$(echo $line | cut -d " " -f1)
      port=$(echo $line | cut -d " " -f3)
      hostname=$(echo $line | cut -d " " -f4)
      echo "ip: ${ip}"
      echo "port: ${port}"
      echo "resule:${ip} ${hostname}"
      sed -i '/#modify yum mapping hosts start/i '${ip}' '${hostname}'' /etc/hosts
    done <${hostAllInfoPath}
    sed -i '/#modify yum mapping hosts start/i\#modify etc hosts end' /etc/hosts
  else
    echo "#modify etc hosts start" >>/etc/hosts
    while read line || [[ -n ${line} ]]; do
      ip=$(echo $line | cut -d " " -f1)
      port=$(echo $line | cut -d " " -f3)
      hostname=$(echo $line | cut -d " " -f4)
      echo "ip: ${ip}"
      echo "port: ${port}"
      echo "${ip}  ${hostname}" >>/etc/hosts
    done <${hostAllInfoPath}
    echo "#modify etc hosts end" >>/etc/hosts
  fi
}

modifyHosts
sed -i 's/^[^#].*[0-9]-[0-9]/#&/g' /etc/hosts

echo "init-singlehosts.sh finished."
echo "Done."
