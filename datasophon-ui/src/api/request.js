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



import Vue from 'vue'
import axios from 'axios'
// import '@/api/interceptors'

// post数据处理
const handleParams = function(data) {
  const params = new FormData()
  for(var key in data) {
    params.append(key, data[key])
  }
  return params
}

const axiosGet = function(url, params = {}) {
  return new Promise((resolve, reject) => {
    axios({
      method: 'get',
      url: url,
      params: params,
    }).then(res => {
      resolve(res.data)
    }).catch(error => {
      reject(error)
    })
  })
}

const axiosPost = function(url, params = {}) {
  return new Promise((resolve, reject) => {
    axios({
      method: 'post',
      url: url,
      data: handleParams(params),
      ContentType:"application/json;charset=UTF-8"
    }).then(res => {
      resolve(res.data)
    }).catch(error => {
      reject(error)
    })
  })
}
const axiosJsonPost = function(url, params = {}) {
  return new Promise((resolve, reject) => {
    axios({
      method: 'post',
      url: url,
      data: params,
    }).then(res => {
      resolve(res.data)
    }).catch(error => {
      reject(error)
    })
  })
}

// 文件上传，params为form-data
const axiosPostUpload = function(url, params = {}) {
  return new Promise((resolve, reject) => {
    axios({
      method: 'post',
      url: url,
      data: params
    }).then(res => {
      resolve(res.data)
    }).catch(error => {
      reject(error)
    })
  })
}

Vue.prototype.$axiosGet = axiosGet// get请求
Vue.prototype.$axiosPost = axiosPost// post请求
Vue.prototype.$axiosPostUpload = axiosPostUpload// 文件上传-post请求
Vue.prototype.$axiosJsonPost = axiosJsonPost// jsonpost请求
