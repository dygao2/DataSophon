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

import Mock from 'mockjs'

Mock.mock(`${process.env.VUE_APP_API_BASE_URL}/routes`, 'get', () => {
  let result = {}
  result.code = 200
  result.data = [{
    router: 'root',
    children: [
      {
        router: 'dashboard',
        children: ['workplace', 'analysis'],
      },
      {
        router: 'form',
        children: ['basicForm', 'stepForm', 'advanceForm']
      },
      {
        router: 'basicForm',
        name: '验权表单',
        icon: 'file-excel',
        authority: 'queryForm'
      },
      {
        router: 'antdv',
        path: 'antdv',
        name: 'Ant Design Vue',
        icon: 'ant-design',
        link: 'https://www.antdv.com/docs/vue/introduce-cn/'
      },
      {
        router: 'document',
        path: 'document',
        name: '使用文档',
        icon: 'file-word',
        link: 'https://iczer.gitee.io/vue-antd-admin-docs/'
      }
    ]
  }]
  return result
})
