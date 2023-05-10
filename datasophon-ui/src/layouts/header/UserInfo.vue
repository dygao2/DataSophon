<!--
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


 * @Date: 2022-06-09 17:05:39
 * @LastEditTime: 2022-06-09 17:20:58
 * @FilePath: \ddh-ui\src\layouts\header\UserInfo.vue
-->
<template>
  <div class="user-info">
    <a-descriptions class="user-warp" title="" :bordered="true" :column='1'>
      <a-descriptions-item label="用户名称">{{user.username || '-'}}</a-descriptions-item>
      <a-descriptions-item label="邮箱地址">{{user.email}}</a-descriptions-item>
      <a-descriptions-item label="手机号码">{{user.phone}}</a-descriptions-item>
    </a-descriptions>
  </div>
</template>
<script>

export default {
  data() {
    return {
      user: {}
    };
  },
  methods: {},
  mounted () {
    const user = localStorage.getItem(process.env.VUE_APP_USER_KEY)
    this.user = JSON.parse(user)
  }
};
</script>
<style lang="less" scoped>
.user-info {
  padding: 0 32px;
  .user-warp {
    margin: 16px 0;
  }
}
</style>