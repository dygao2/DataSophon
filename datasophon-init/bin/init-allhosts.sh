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
REPAIR_PATH=$(dirname "${BASE_PATH}")
echo "REPAIR_PATH: ${REPAIR_PATH}"

sed -i '/#modify etc hosts start/,/#modify etc hosts end/d' /etc/hosts
modifyHosts() {
  cat /etc/hosts | grep yum
  if [ $? -eq 0 ]; then
    sed -i '/#modify yum mapping hosts start/i\#modify etc hosts start' /etc/hosts
    function prop {
      [ -f "${hostAllInfoPath}" ] && grep -P "^\s*[^#]?${1}=.*$" ${hostAllInfoPath} | cut -d'=' -f2
    }
    for ((i = 1; i <= ${initAllHostNums}; i++)); do
      ip=$(prop "dataSophon.ip.${i}")                      #ip
      hostname=$(prop "dataSophon.ssh.port.hostname.${i}") # password
      echo "ip: ${ip}"
      echo "${ip} ${hostname}"
      sed -i '/#modify yum mapping hosts start/i '${ip}' '${hostname}'' /etc/hosts
    done
    sed -i '/#modify yum mapping hosts start/i\#modify etc hosts end' /etc/hosts
  else
    function prop {
      [ -f "${hostAllInfoPath}" ] && grep -P "^\s*[^#]?${1}=.*$" ${hostAllInfoPath} | cut -d'=' -f2
    }
    echo "#modify etc hosts start" >>/etc/hosts
    for ((i = 1; i <= ${initAllHostNums}; i++)); do
      ip=$(prop "dataSophon.ip.${i}")                      #ip
      hostname=$(prop "dataSophon.ssh.port.hostname.${i}") # password
      echo "ip: ${ip}"
      echo "${ip} ${hostname}"
      echo "${ip}  ${hostname}" >>/etc/hosts
    done
    echo "#modify etc hosts end" >>/etc/hosts
  fi

}

modifyHosts
sed -i 's/^[^#].*[0-9]-[0-9]/#&/g' /etc/hosts

echo "init-hosts.sh finished."
echo "Done."
