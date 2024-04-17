seatunnel:
  engine:
    backup-count: ${backupCount}
    queue-type: blockingqueue
    print-execution-info-interval: 60
    print-job-metrics-info-interval: 60
    slot-service:
      dynamic-slot: true
    checkpoint:
      interval: 10000
      timeout: 60000
      max-concurrent: 1
      tolerable-failure: 2
      storage:
        type: hdfs
        max-retained: 3
        plugin-config:
          <#list itemList as item>
          ${item.name}: ${item.value}
          </#list>