export JVMFLAGS="-Xms${zkHeapSize}G -Xmx${zkHeapSize}G   <#if zkSecurity??>${zkSecurity}</#if> $JVMFLAGS"
export SERVER_JVMFLAGS="<#if zkSecurity??>${zkSecurity}</#if> -Djava.security.krb5.conf=/etc/krb5.conf"
export CLIENT_JVMFLAGS="$CLIENT_JVMFLAGS <#if zkSecurity??>${zkSecurity}</#if> -Djava.security.krb5.conf=/etc/krb5.conf -Dzookeeper.server.principal=zookeeper/${hostname}@${zkRealm}"