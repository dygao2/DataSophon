/*
/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */



import paths from '@/api/baseUrl'// 后台服务地址

let path = paths.path() + '/ddh'
export default {
  getServiceList: path + '/api/frame/service/list', // 选择服务的列表
  deleteService: path + '/api/frame/service/delete', // 删除框架服务
  getServiceConfigOption: path + '/service/install/getServiceConfigOption', // 查询服务配置
  getServiceRoleList: path + '/api/frame/service/role/getServiceRoleList', // 查询服务对应的服务角色 
  getAllHost: path + '/api/cluster/host/all', // 查询集群所有主机  
  saveServiceRoleHostMapping: path + '/service/install/saveServiceRoleHostMapping', // 保存服务角色与主机对应关系 
  getNonMasterRoleList: path + '/api/frame/service/role/getNonMasterRoleList', // 查询服务对应的非Master角色 
  saveServiceConfig: path + '/service/install/saveServiceConfig', // 保存服务配置 
  startExecuteCommand: path + '/api/cluster/service/command/startExecuteCommand', // 启动执行指令  
  generateCommand: path + '/api/cluster/service/command/generateCommand', // 生成服务操作指令  
  getServiceCommandlist: path + '/api/cluster/service/command/getServiceCommandlist', // 查询服务安装指令列表1  
  getServiceHostList: path + '/api/cluster/service/command/host/list', // 查询服务安装对应主机列表  
  getServiceRoleOrderList: path + '/api/cluster/service/command/host/command/list', // 查询主机上服务角色指令列表3
  getLog: path + '/cluster/service/role/instance/getLog', // 服务实例-查看日志
  getHostCommandLog: path + '/api/cluster/service/command/host/command/getHostCommandLog', // 查询主机上服务角色指令3日志
  getQueueList: path + '/cluster/yarn/queue/list', // 队列列表
  getCapacityList: path + '/cluster/queue/capacity/list', // 容量队列列表
  saveQueue: path + '/cluster/yarn/queue/save', // 队列保存
  deleteQueue: path + '/cluster/yarn/queue/delete', // 队列删除
  updateQueue: path + '/cluster/yarn/queue/update', // 更新队列
  refreshQueues: path + '/cluster/yarn/queue/refreshQueues', // 刷新队列到Yarn
  refreshQueuesYARN : path + '/cluster/queue/capacity/refreshToYarn'
}
