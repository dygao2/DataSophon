# Datasophon1.2.1集成Dinky1.0.1

Dinky 下载地址: https://github.com/DataLinkDC/dinky/releases/tag/v1.0.1

## 1.下载Dinky

```
这里以Flink1.16版本为例
wget https://github.com/DataLinkDC/dinky/releases/download/v1.0.1/dinky-release-1.16-1.0.1.tar.gz

mv dinky-release-1.16-1.0.1.tar.gz  /opt/datasophon/DDP/packages
cd /opt/datasophon/DDP/packages

md5sum dinky-release-1.16-1.0.1.tar.gz
56b6d1fdd2c356b4f794ef1a9e514898 dinky-release-1.16-1.0.1.tar.gz

echo 56b6d1fdd2c356b4f794ef1a9e514898 > dinky-release-1.16-1.0.1.tar.gz.md5
```

## 2.准备服务配置模板

在每个节点的datasophon-worker配置目录下添加配置模板

```
cd  /opt/datasophon/datasophon-worker/conf/templates
vim dinky.ftl

spring:
  datasource:
    url: ${databaseUrl}
    username: ${username}
    password: ${password}
    driver-class-name: com.mysql.cj.jdbc.Driver

分发ftl(如果在当前节点安装dinky的话就不用分发)
scp dinky.ftl datasophon02:/opt/datasophon/datasophon-worker/conf/templates
scp dinky.ftl datasophon03:/opt/datasophon/datasophon-worker/conf/templates

重启所有work节点
sh  /opt/datasophon/datasophon-worker/bin/datasophon-worker.sh restart worker
```

## 3.准备配置文件service_ddl.json

进入datasophon-manager-1.2.1中

```
cd /opt/datasophon-manager-1.2.1/conf/meta/DDP-1.2.1

mkdir DINKY && cd DINKY

vim service_ddl.json
```

```
{
  "name": "DINKY",
  "label": "Dinky",
  "description": "流处理极速开发框架,流批一体&湖仓一体的云原生平台,一站式计算平台",
  "version": "1.0.1",
  "sortNum": 19,
  "dependencies":[],
  "packageName": "dinky-release-1.16-1.0.1.tar.gz",
  "decompressPackageName": "dinky-release-1.16-1.0.1",
  "roles": [
    {
      "name": "Dinky",
      "label": "Dinky",
      "roleType": "master",
      "cardinality": "1",
      "logFile": "logs/dinky.log",
      "jmxPort": 10087,
      "startRunner": {
        "timeout": "60",
        "program": "auto.sh",
        "args": [
          "startWithJmx",
          "1.16"
        ]
      },
      "stopRunner": {
        "timeout": "600",
        "program": "auto.sh",
        "args": [
          "stop"
        ]
      },
      "statusRunner": {
        "timeout": "60",
        "program": "auto.sh",
        "args": [
          "status"
        ]
      },
      "restartRunner": {
        "timeout": "60",
        "program": "auto.sh",
        "args": [
          "restart",
          "1.16"
        ]
      },
      "externalLink": {
        "name": "Dinky Ui",
        "label": "Dinky Ui",
        "url": "http://${host}:${serverPort}"
      }
    }
  ],
  "configWriter": {
    "generators": [
      {
        "filename": "application-mysql.yml",
        "configFormat": "custom",
        "outputDirectory": "config",
        "templateName": "dinky.ftl",
        "includeParams": [
          "databaseUrl",
          "username",
          "password",
          "serverPort"
        ]
      }
    ]
  },
  "parameters": [
    {
      "name": "databaseUrl",
      "label": "Dinky数据库地址",
      "description": "",
      "configType": "map",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "jdbc:mysql://${apiHost}:3306/dinky?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
    },
    {
      "name": "username",
      "label": "Dinky数据库用户名",
      "description": "",
      "configType": "map",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "root"
    },
    {
      "name": "password",
      "label": "Dinky数据库密码",
      "description": "",
      "configType": "map",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "123456"
    },
    {
      "name": "serverPort",
      "label": "Dinky服务端口",
      "description": "",
      "configType": "map",
      "required": true,
      "type": "input",
      "value": "",
      "configurableInWizard": true,
      "hidden": false,
      "defaultValue": "8888"
    }

  ]
}
```

**重启datasophon-manager的api**

```
sh /opt/datasophon-manager-1.2.1/bin/datasophon-api.sh restart api
```

## 4.安装Dinky

![f8532b699ebdd66fa94f2a04dc451f5b](https://github.com/javaht/datasophon/assets/54611681/327729d6-ed75-4442-b273-6460e781d8ee)

### 4.1需要我们手动创建数据库并且运行sql

```
mysql -u root -p -e "create database dinky"

mysql -u root -p -D dinky < /opt/datasophon/dinky/sql/dinky-mysql.sql
```

## 5.Dinky集成grafana监控

### 5.1 datasophon1.2.1默认存在，可跳过

```
cd /opt/datasophon/prometheus

vim prometheus.yml 检查是否有dinky配置文件  如果没有添加

  - job_name: 'dinky'
    file_sd_configs:
     - files:
       - configs/dinky.json


 cd /opt/datasophon/prometheus/configs
 vim dinky.json  检查是否有dinky的配置文件  如果没有添加

[
 {
  "targets":["datasophon01:10087"]
 }
]
然后重启Prometheus服务(以上配置文件1.2.1默认存在，不存在添加后重启prometheus)
```

### 5.2 Grafana 配置

通过下图展示的url进去grafana配置图表，默认登陆账户密码：admin ：admin

![2c74b17dc990845b18cfb7abf7f8312c](https://github.com/javaht/datasophon/assets/54611681/e151c3c1-eedb-4ce1-86b6-4427403ff3b4)

### 5.3 创建Grafana 模板文件

vim dinky.json

```
{
  "annotations": {
    "list": [
      {
        "builtIn": 1,
        "datasource": {
          "type": "grafana",
          "uid": "-- Grafana --"
        },
        "enable": true,
        "hide": true,
        "iconColor": "rgba(0, 211, 255, 1)",
        "name": "Annotations & Alerts",
        "target": {
          "limit": 100,
          "matchAny": false,
          "tags": [],
          "type": "dashboard"
        },
        "type": "dashboard"
      }
    ]
  },
  "editable": true,
  "fiscalYearStartMonth": 0,
  "graphTooltip": 0,
  "id": 33,
  "links": [],
  "liveNow": false,
  "panels": [
    {
      "datasource": {
        "type": "prometheus",
        "uid": "hj6gjW44z"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "thresholds"
          },
          "mappings": [
            {
              "options": {
                "0": {
                  "text": "下线"
                },
                "1": {
                  "text": "正常"
                }
              },
              "type": "value"
            },
            {
              "options": {
                "match": "null",
                "result": {
                  "text": "下线"
                }
              },
              "type": "special"
            }
          ],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "#d44a3a",
                "value": null
              },
              {
                "color": "#e24d42",
                "value": 0
              },
              {
                "color": "#299c46",
                "value": 1
              }
            ]
          },
          "unit": "none"
        },
        "overrides": []
      },
      "gridPos": {
        "h": 4,
        "w": 4,
        "x": 0,
        "y": 0
      },
      "hideTimeOverride": false,
      "id": 8,
      "links": [
        {
          "targetBlank": true,
          "title": "Tomcat dashboard",
          "url": "/d/chanjarster-tomcat-dashboard/tomcat-dashboard?$__url_time_range&$__all_variables"
        }
      ],
      "maxDataPoints": 100,
      "options": {
        "colorMode": "value",
        "graphMode": "none",
        "justifyMode": "auto",
        "orientation": "horizontal",
        "reduceOptions": {
          "calcs": [
            "lastNotNull"
          ],
          "fields": "",
          "values": false
        },
        "text": {
          "valueSize": 38
        },
        "textMode": "auto"
      },
      "pluginVersion": "9.1.6",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "hj6gjW44z"
          },
          "editorMode": "code",
          "expr": "up{job=\"dinky\"}",
          "format": "time_series",
          "instant": true,
          "interval": "",
          "intervalFactor": 1,
          "legendFormat": "",
          "refId": "A"
        }
      ],
      "title": "Dinky状态",
      "type": "stat"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "hj6gjW44z"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "thresholds"
          },
          "mappings": [
            {
              "options": {
                "match": "null",
                "result": {
                  "text": "N/A"
                }
              },
              "type": "special"
            }
          ],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              }
            ]
          },
          "unit": "dateTimeAsIso"
        },
        "overrides": []
      },
      "gridPos": {
        "h": 4,
        "w": 8,
        "x": 4,
        "y": 0
      },
      "id": 2,
      "links": [],
      "maxDataPoints": 100,
      "options": {
        "colorMode": "value",
        "graphMode": "none",
        "justifyMode": "auto",
        "orientation": "horizontal",
        "reduceOptions": {
          "calcs": [
            "lastNotNull"
          ],
          "fields": "",
          "values": false
        },
        "text": {
          "valueSize": 38
        },
        "textMode": "auto"
      },
      "pluginVersion": "9.1.6",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "hj6gjW44z"
          },
          "editorMode": "code",
          "expr": "process_start_time_seconds{job=\"dinky\"}*1000",
          "legendFormat": "__auto",
          "range": true,
          "refId": "A"
        }
      ],
      "title": "Dinky启动时间",
      "type": "stat"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "hj6gjW44z"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "thresholds"
          },
          "mappings": [
            {
              "options": {
                "match": "null",
                "result": {
                  "text": "N/A"
                }
              },
              "type": "special"
            }
          ],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              }
            ]
          },
          "unit": "s"
        },
        "overrides": []
      },
      "gridPos": {
        "h": 4,
        "w": 4,
        "x": 12,
        "y": 0
      },
      "id": 4,
      "links": [],
      "maxDataPoints": 100,
      "options": {
        "colorMode": "value",
        "graphMode": "none",
        "justifyMode": "auto",
        "orientation": "horizontal",
        "reduceOptions": {
          "calcs": [
            "lastNotNull"
          ],
          "fields": "",
          "values": false
        },
        "text": {
          "valueSize": 38
        },
        "textMode": "auto"
      },
      "pluginVersion": "9.1.6",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "hj6gjW44z"
          },
          "editorMode": "code",
          "expr": "time() - process_start_time_seconds{job=\"dinky\"}",
          "interval": "",
          "legendFormat": "",
          "range": true,
          "refId": "A"
        }
      ],
      "title": "Dinky运行时长",
      "type": "stat"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "hj6gjW44z"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "thresholds"
          },
          "mappings": [
            {
              "options": {
                "match": "null",
                "result": {
                  "text": "N/A"
                }
              },
              "type": "special"
            }
          ],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              }
            ]
          },
          "unit": "bytes"
        },
        "overrides": []
      },
      "gridPos": {
        "h": 4,
        "w": 4,
        "x": 16,
        "y": 0
      },
      "id": 6,
      "links": [],
      "maxDataPoints": 100,
      "options": {
        "colorMode": "value",
        "graphMode": "none",
        "justifyMode": "auto",
        "orientation": "horizontal",
        "reduceOptions": {
          "calcs": [
            "lastNotNull"
          ],
          "fields": "",
          "values": false
        },
        "text": {
          "valueSize": 38
        },
        "textMode": "auto"
      },
      "pluginVersion": "9.1.6",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "hj6gjW44z"
          },
          "editorMode": "code",
          "expr": "jvm_memory_bytes_max{job=\"dinky\",area=\"heap\"}",
          "legendFormat": "__auto",
          "range": true,
          "refId": "A"
        }
      ],
      "title": "Dinky堆内存",
      "type": "stat"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "hj6gjW44z"
      },
      "fieldConfig": {
        "defaults": {
          "mappings": [],
          "max": 100,
          "min": 0,
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          },
          "unit": "%"
        },
        "overrides": []
      },
      "gridPos": {
        "h": 4,
        "w": 4,
        "x": 20,
        "y": 0
      },
      "id": 10,
      "options": {
        "orientation": "auto",
        "reduceOptions": {
          "calcs": [
            "lastNotNull"
          ],
          "fields": "",
          "values": false
        },
        "showThresholdLabels": false,
        "showThresholdMarkers": true
      },
      "pluginVersion": "9.1.6",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "hj6gjW44z"
          },
          "editorMode": "code",
          "expr": "jvm_memory_bytes_used{area=\"heap\",job=\"dinky\"}*100/jvm_memory_bytes_max{area=\"heap\",job=\"dinky\"}",
          "legendFormat": "__auto",
          "range": true,
          "refId": "A"
        }
      ],
      "title": "Dinky堆内存使用率",
      "type": "gauge"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "hj6gjW44z"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "palette-classic"
          },
          "custom": {
            "axisCenteredZero": false,
            "axisColorMode": "text",
            "axisLabel": "",
            "axisPlacement": "auto",
            "barAlignment": 0,
            "drawStyle": "line",
            "fillOpacity": 10,
            "gradientMode": "none",
            "hideFrom": {
              "legend": false,
              "tooltip": false,
              "viz": false
            },
            "lineInterpolation": "linear",
            "lineWidth": 1,
            "pointSize": 5,
            "scaleDistribution": {
              "type": "linear"
            },
            "showPoints": "never",
            "spanNulls": false,
            "stacking": {
              "group": "A",
              "mode": "none"
            },
            "thresholdsStyle": {
              "mode": "off"
            }
          },
          "links": [],
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          },
          "unit": "bytes"
        },
        "overrides": [
          {
            "matcher": {
              "id": "byName",
              "options": "Usage %"
            },
            "properties": [
              {
                "id": "custom.drawStyle",
                "value": "bars"
              },
              {
                "id": "custom.fillOpacity",
                "value": 100
              },
              {
                "id": "color",
                "value": {
                  "fixedColor": "#6d1f62",
                  "mode": "fixed"
                }
              },
              {
                "id": "custom.lineWidth",
                "value": 0
              },
              {
                "id": "unit",
                "value": "percentunit"
              },
              {
                "id": "min",
                "value": 0
              },
              {
                "id": "max",
                "value": 1
              }
            ]
          }
        ]
      },
      "gridPos": {
        "h": 10,
        "w": 12,
        "x": 0,
        "y": 4
      },
      "id": 12,
      "links": [],
      "options": {
        "legend": {
          "calcs": [
            "mean",
            "max"
          ],
          "displayMode": "table",
          "placement": "bottom",
          "showLegend": true
        },
        "tooltip": {
          "mode": "multi",
          "sort": "none"
        }
      },
      "pluginVersion": "9.1.6",
      "repeat": "memarea",
      "repeatDirection": "h",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "hj6gjW44z"
          },
          "editorMode": "code",
          "expr": "jvm_memory_bytes_used{area=\"heap\",job=\"dinky\"}",
          "legendFormat": "已用内存",
          "range": true,
          "refId": "A"
        },
        {
          "datasource": {
            "type": "prometheus",
            "uid": "hj6gjW44z"
          },
          "editorMode": "code",
          "expr": " jvm_memory_bytes_max{area=\"heap\",job=\"dlink\"}",
          "hide": false,
          "legendFormat": "总内存",
          "range": true,
          "refId": "B"
        },
        {
          "datasource": {
            "type": "prometheus",
            "uid": "hj6gjW44z"
          },
          "editorMode": "code",
          "expr": "jvm_memory_bytes_used{area=\"heap\",job=\"dlink\"} / jvm_memory_bytes_max >= 0",
          "hide": false,
          "legendFormat": "使用率",
          "range": true,
          "refId": "C"
        }
      ],
      "title": "Dinky堆内存使用趋势",
      "type": "timeseries"
    },
    {
      "datasource": {
        "type": "prometheus",
        "uid": "hj6gjW44z"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "palette-classic"
          },
          "custom": {
            "axisCenteredZero": false,
            "axisColorMode": "text",
            "axisLabel": "",
            "axisPlacement": "auto",
            "barAlignment": 0,
            "drawStyle": "line",
            "fillOpacity": 10,
            "gradientMode": "none",
            "hideFrom": {
              "legend": false,
              "tooltip": false,
              "viz": false
            },
            "lineInterpolation": "linear",
            "lineWidth": 1,
            "pointSize": 5,
            "scaleDistribution": {
              "type": "linear"
            },
            "showPoints": "never",
            "spanNulls": false,
            "stacking": {
              "group": "A",
              "mode": "none"
            },
            "thresholdsStyle": {
              "mode": "off"
            }
          },
          "links": [],
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          },
          "unit": "s"
        },
        "overrides": []
      },
      "gridPos": {
        "h": 10,
        "w": 12,
        "x": 12,
        "y": 4
      },
      "id": 14,
      "links": [],
      "options": {
        "legend": {
          "calcs": [
            "lastNotNull",
            "max",
            "min"
          ],
          "displayMode": "table",
          "placement": "bottom",
          "showLegend": true
        },
        "tooltip": {
          "mode": "multi",
          "sort": "none"
        }
      },
      "pluginVersion": "9.1.6",
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "hj6gjW44z"
          },
          "editorMode": "code",
          "expr": "increase(jvm_gc_collection_seconds_sum{job=\"dinky\"}[$__interval])",
          "format": "time_series",
          "interval": "60s",
          "intervalFactor": 1,
          "legendFormat": "{{gc}}",
          "metric": "jvm_gc_collection_seconds_sum",
          "range": true,
          "refId": "A",
          "step": 10
        }
      ],
      "title": "Dinky GC时间趋势图",
      "type": "timeseries"
    }
  ],
  "schemaVersion": 37,
  "style": "dark",
  "tags": [],
  "templating": {
    "list": []
  },
  "time": {
    "from": "now-6h",
    "to": "now"
  },
  "timepicker": {},
  "timezone": "",
  "title": "Dinky",
  "uid": "9qU9T1OVk",
  "version": 4,
  "weekStart": ""
}
```

### 5.4 导入创建的模板文件

![dee4fd4731a54399166c890f385697dc](https://github.com/javaht/datasophon/assets/54611681/9a18cabf-65aa-4ba5-a6e1-b22c24b3cecd)

![12260a17477d4ade7c3c612e77f2af1c](https://github.com/javaht/datasophon/assets/54611681/3c371705-0fc9-4a73-a3d4-25903c10fcdb)

**查看datasophon数据库中t_ddh_cluster_service_dashboard表中是否原就存在dinky 如果不存在添加**

```
19 DINKY http://${grafanaHost}:3000/d/9qU9T1OVk/dinky?kiosk&refresh=1m
```

![3845420da980622d83c4f9d0a3ec91bb](https://github.com/javaht/datasophon/assets/54611681/ab82db4d-7821-45ab-8ffd-b877d281fa3c)

回到datasophon的dinky服务，刷新即可在总览看到详细监控信息

![eee3f6bd3deb64a0fa99490143c2a7c0](https://github.com/javaht/datasophon/assets/54611681/e5f4bea4-876c-4ba6-b8fb-1ca074326691)

