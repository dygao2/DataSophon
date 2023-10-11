#!/bin/bash

if [ $UID -ne 0 ]; then
  echo Non root user. Please run as root.
  exit 1
fi
if [ -L $0 ]; then
  BASE_DIR=$(dirname $(readlink $0))
else
  BASE_DIR=$(dirname $0)
fi
BBASE_PATH=$(
  cd ${BASE_DIR}
  pwd
)
INIT_PATH=$(dirname "${BASE_PATH}")
echo "INIT_PATH: ${INIT_PATH}"

ETC_HOST=/etc/hosts
source /etc/profile
sed -i '/modify java policy start/,/modify java policy end/d' ${JAVA_HOME}/jre/lib/security/java.policy
sed -i '/grant {/a\//modify java policy end' ${JAVA_HOME}/jre/lib/security/java.policy
sed -i '/modify java policy end/i\//modify java policy start' ${JAVA_HOME}/jre/lib/security/java.policy
sed -i '/modify java policy end/i\permission javax.management.MBeanTrustPermission "register";' ${JAVA_HOME}/jre/lib/security/java.policy
echo "init-java-policy.sh finished."
echo "Done."
