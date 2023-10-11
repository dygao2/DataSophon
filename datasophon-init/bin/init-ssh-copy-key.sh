#!/bin/bash

# example: sh init-ssh-gen-key.sh

filePath=$1
INITAllHostNums=$2
port=$3
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

removeSSHAsk() {
  sed -i '/^#.*UseDNS no/s/^#//g' /etc/ssh/sshd_config
  sed -i '/^#.*StrictHostKeyChecking ask/s/^#//g' /etc/ssh/ssh_config
  sed -i '/StrictHostKeyChecking ask/s/ask/no/g' /etc/ssh/ssh_config
}
copyKeyConfig() {
  # authorized_keys
  # known_hosts
  # scp -r /root/.ssh/ root@192.168.216.20:/root/

  user=root
  sshDir="/${user}/.ssh"

  function prop {
    [ -f "${filePath}" ] && grep -P "^\s*[^#]?${1}=.*$" ${filePath} | cut -d'=' -f2
  }
  for ((i = 1; i <= ${INITAllHostNums}; i++)); do
    ip=$(prop "dataSophon.ip.${i}")         #ip
    pwd=$(prop "dataSophon.password.${i}")  # password
    port=$(prop "dataSophon.ssh.port.${i}") # port
    echo $ip
    echo $pwd
    echo $port
    /usr/bin/expect <<-EOF
set timeout -1
spawn scp -P${port} -r $sshDir/ $user@$ip:/${user}/
expect {
        "yes/no" { send "yes\r"; exp_continue }
        "password:" { send "$pwd\r" }
        }
		expect eof
	EOF
  done
  echo -e "\nFinish copy ssh config."
}

removeSSHAsk
copyKeyConfig

echo "init-ssh-copy-key.sh finished."
echo "Done."
