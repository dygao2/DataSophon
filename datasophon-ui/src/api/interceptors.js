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

import axios from 'axios'

// axios请求拦截
axios.interceptors.request.use(
  config => {
    // 在请求之前操作
    if(config.method === 'get') {// 在ie中相同请求不会重复发送
      if(window.ActiveXObject || 'ActiveXObject' in window) {
        config.url = `${config.url}?${new Date().getTime()}`
      }
    }
    config.headers['Content-Type'] = config.ContentType?config.ContentType:'application/json;charset=UTF-8'
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

axios.interceptors.response.use(
  response => {
    // 对响应数据操作
    return response
  },
  error => {
    return Promise.reject(error)
  }
)
