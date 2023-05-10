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
console.log(path, '请求的地址')
export default {
  login: path + '/login',
  loginOut: path + '/signOut',
  getUserList: path + '/api/user/list',// 用户列表
  addUser: path + '/api/user/save',// 添加用户
  deleteUser: path + '/api/user/delete',// 删除用户
  updateUser: path + '/api/user/update',// 更新用户
  queryAllUser: path + '/api/user/all',
  getTenant:path + '/cluster/user/list',
  getTenantGroup:path + '/cluster/group/list',
}
