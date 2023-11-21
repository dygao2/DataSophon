#!/bin/bash
ip="$1"
# example: bash init-xxxxxxxx.sh  119.21.12.34
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

echo "Depending on the performance of your machine, it may take between 15-20 minutes to initialize the private YUM source. Please do not log out."
#如果文件不存在
if [ ! -d "/data/private-yum-library" ]; then
  echo "没有发现private-yum-library 离线yum源，请确认是否正确配置离线yum源......."
fi
cat >/etc/yum.repos.d/dataSophon.repo <<EOF
[dataSophon-base]
name=dataSophon-base
baseurl=file:///data/private-yum-library/repo/
gpgcheck=1
enable=1
gpgkey=file:///data/private-yum-library/repo/RPM-GPG-KEY-openEuler

#初始化只包含从iso镜像中拷贝出来的基础repo源，如不能满足需求可以再次基础上配置epol扩展包YUM源
#[dataSophon-epel]
#name=dataSophon-epel
#baseurl=file:///data/private-yum-library/epel/
#enabled=1
#gpgcheck=0

EOF
yum clean all
yum makecache
echo "init-private-yum-library-openEuler.sh finished."
echo "Done."

cat /etc/httpd/conf/httpd.conf | grep 'Listen 8000'
if [ $? -eq 0 ]; then
  echo "httpd port modified successfully" >>${initLogDir}/installSingle_$(date +%Y%m%d).log
  systemctl stop httpd
  systemctl start httpd

else
  echo "init httpd begin."
  yum install httpd
  cat /etc/httpd/conf/httpd.conf | grep 'Listen 8000'
  sed -i 's/Listen 80/Listen 8000/g' /etc/httpd/conf/httpd.conf
  sed -i '/ServerName yum.dataSophon.cn:8000/d' /etc/httpd/conf/httpd.conf
  sed -i '/#ServerName/a ServerName yum.dataSophon.cn:8000' /etc/httpd/conf/httpd.conf 
  systemctl start httpd
  systemctl enable httpd
  echo "init httpd finished."
fi

unlink /var/www/html/repo
ln -s /data/private-yum-library/repo /var/www/html/
#unlink  /var/www/html/epol
#ln -s /data/datasophon-init/private-yum-library/epol /var/www/html/
sed -i '/#modify yum mapping hosts start/,/#modify yum mapping hosts end/d' /etc/hosts
modifyYumHosts() {
  echo "#modify yum mapping hosts start" >>/etc/hosts
  echo "${ip} yum.dataSophon.cn" >>/etc/hosts
  echo "#modify yum mapping hosts end" >>/etc/hosts
  source /etc/profile
  source /root/.bash_profile
}
modifyYumHosts

rm /etc/yum.repos.d/dataSophon.repo

cat >/etc/yum.repos.d/dataSophon.repo <<EOF
[dataSophon-base]
name=dataSophon-base
baseurl=http://yum.dataSophon.cn:8000/repo/
gpgcheck=0
enable=1

#初始化只包含从iso镜像中拷贝出来的基础repo源，如不能满足需求可以再次基础上配置epol扩展包YUM源
#[dataSophon-epel]
#name=dataSophon-epel
#baseurl=http://yum.dataSophon.cn:8000/epel/
#enabled=1
#gpgcheck=0
EOF
yum clean all
yum makecache
echo "init-private-yum-library-openEuler.sh finished."
echo "Done."
