hazelcast-client:
  cluster-name: seatunnel
  properties:
    hazelcast.logging.type: log4j2
  network:
    cluster-members:
<#list itemList as item>
    <#list item.value?split(",") as host>
      - ${host}:5801
    </#list>
</#list>