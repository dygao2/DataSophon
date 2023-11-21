#!/bin/bash

# example: sh init-ssh-gen-key.sh

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
INIT_BIN_PATH=${INIT_PATH}/bin
echo "INIT_BIN_PATH: ${INIT_BIN_PATH}"
INIT_SBIN_PATH=${INIT_PATH}/sbin
echo "INIT_SBIN_PATH: ${INIT_SBIN_PATH}"
PACKAGES_PATH=${INIT_PATH}/packages
echo "PACKAGES_PATH: ${PACKAGES_PATH}"

sh ${INIT_BIN_PATH}/init-sshpackage.sh
sh ${INIT_BIN_PATH}/init-expect.sh

removeSSHAsk() {
  echo "begin removeSSHAsk....."
  sed -i '/^#.*UseDNS no/s/^#//g' /etc/ssh/sshd_config
  sed -i '/^#.*StrictHostKeyChecking ask/s/^#//g' /etc/ssh/ssh_config
  sed -i '/StrictHostKeyChecking ask/s/ask/no/g' /etc/ssh/ssh_config
}


keygenConfig(){
# ssh-keygen -t rsa
sshPath=/root/.ssh
if [ ! -r "${sshPath}/id_rsa.pub" ]; then
echo "id_rsa.pub nonexistent  creating......"
/usr/bin/expect <<-EOF
set timeout 10
spawn ssh-keygen -t rsa
expect {
        "Enter file in which to save the key (/root/.ssh/id_rsa): " { send "\r"; exp_continue }
        "Overwrite (y/n)? " { send "y\r"; exp_continue }
        "Enter passphrase (empty for no passphrase): " { send "\r"; exp_continue }
        "Enter same passphrase again: " { send "\r" }
        }
		expect eof
	EOF

wait
echo "Finish ssh-keygen -t rsa."
else
echo "id_rsa.pub exists"
fi
}

configAuthorizedKeys(){
    user=root
    sshDir="/${user}/.ssh"
    cat ${sshDir}/id_rsa.pub >> ${sshDir}/authorized_keys
}
removeSSHAsk
keygenConfig
configAuthorizedKeys

echo "init-ssh-gen-key.sh finished."
echo "Done."




