export JVMFLAGS="-Xms${zkHeapSize}G -Xmx${zkHeapSize}G   <#if zkSecurity??>${zkSecurity}</#if> $JVMFLAGS"
<#if zkSecurity??>
export SERVER_JVMFLAGS="${zkSecurity} -Djava.security.krb5.conf=/etc/krb5.conf"
export CLIENT_JVMFLAGS="$CLIENT_JVMFLAGS ${zkSecurity} -Djava.security.krb5.conf=/etc/krb5.conf -Dzookeeper.server.principal=zookeeper/${hostname}@${zkRealm}"
</#if>