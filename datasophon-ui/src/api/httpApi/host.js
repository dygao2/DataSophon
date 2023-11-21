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
  reStartDispatcherHostAgent: path + '/host/install/reStartDispatcherHostAgent', // 主机agent分发重试
  dispatcherHostAgentList: path + '/host/install/dispatcherHostAgentList', // 主机agent分发进度列表
  rehostCheck: path+ '/host/install/rehostCheck', // 重试主机环境校验
  analysisHostList: path + '/host/install/analysisHostList', // 解析主机列表
  hostCheckCompleted: path + '/host/install/hostCheckCompleted', // 查询主机环境校验是否完成
  dispatcherHostAgentCompleted: path + '/host/install/dispatcherHostAgentCompleted', // 查询主机agent分发是否完成
  getRack: path + '/api/cluster/host/getRack', // 查询机架
  updateRack: path + '/api/cluster/host/update', // 分配机架
  deleteRack: path + '/api/cluster/host/delete', // 分配机架
  getRoleListByHostname: path + '/api/cluster/host/getRoleListByHostname', // 根据主机查询角色列表
  generateHostAgentCommand: path + '/host/install/generateHostAgentCommand', // 主机 Worker 管理
  generateHostServiceCommand: path + '/host/install/generateHostServiceCommand', // 主机 Worker Service 管理
}
