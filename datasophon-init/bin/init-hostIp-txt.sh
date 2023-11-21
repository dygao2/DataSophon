#!/bin/bash

# example: sh init-expect.sh
#TXT for initializing the cluster IP address list

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

function prop {
  [ -f "${hostAllInfoPath}" ] && grep -P "^\s*[^#]?${1}=.*$" ${hostAllInfoPath} | cut -d'=' -f2
}
for ((i = 1; i <= ${initAllHostNums}; i++)); do
  ip=$(prop "dataSophon.ip.${i}")        #ip
  pwd=$(prop "dataSophon.password.${i}") # password
  port=$(prop "dataSophon.ssh.port.${i}")
  echo "ip: ${ip}"
  echo "port: ${port}"
  echo -e "root@${ip}:${port}" >>${INIT_BIN_PATH}/tmp_scp_host_info.txt
done

echo "init-hostIp-txt.sh finished."
echo "Done."
