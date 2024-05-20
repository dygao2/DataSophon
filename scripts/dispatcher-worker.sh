#!/bin/bash

# example: sh init-jdk.sh
# instal and config jdk env
workmd5="$1"
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

cp /opt/datasophon/DDP/packages/datasophon-worker.tar.gz /opt/datasophon/
checkworkmd5=$(md5sum /opt/datasophon/DDP/packages/datasophon-worker.tar.gz | awk '{print $1}')
if [ "$checkworkmd5" = "$workmd5" ]; then
  echo "md5效验通过"
else
  echo "md5效验不通过"
  exit 1
fi
tar -zxvf /opt/datasophon/datasophon-worker.tar.gz -C /opt/datasophon/
