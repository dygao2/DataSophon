#!/bin/bash
ip="$1"
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

rm -rf /etc/yum.repos.d/backup
mkdir -p /etc/yum.repos.d/backup
mv `find /etc/yum.repos.d/ -name "*.repo"` /etc/yum.repos.d/backup

sed -i '/#modify yum mapping hosts start/,/#modify yum mapping hosts end/d' /etc/hosts
modifyYumHosts(){
echo "#modify yum mapping hosts start" >> /etc/hosts
echo "${ip} yum.dataSophon.cn" >> /etc/hosts
echo "#modify yum mapping hosts end" >> /etc/hosts
}
modifyYumHosts
cat > /etc/yum.repos.d/dataSophon.repo << EOF
#centos8的基础repo包在iso镜像文件的BaseOS和AppStream下
[dataSophon-base]
name=dataSophon-base
baseurl=http://yum.dataSophon.cn:8000/centos/8/x86_64/BaseOS/
gpgcheck=0
enable=1

[dataSophon-AppStream]
name=dataSophon-AppStream
baseurl=http://yum.dataSophon.cn:8000/centos/8/x86_64/AppStream/
gpgcheck=0
enabled=1

#初始化只包含从iso镜像中拷贝出来的基础repo源，如不能满足需求可以再次基础上配置epol扩展包YUM源
#[dataSophon-epel]
#name=dataSophon-epel
#baseurl=http://yum.dataSophon.cn:8000/epel/8/x86_64/
#enabled=1
#gpgcheck=0
EOF

yum clean all

yum makecache
echo "init-yum-hosts-mapping.sh finished."
echo "Done."
echo "yum repolist:"
echo "$(yum repolist)"
