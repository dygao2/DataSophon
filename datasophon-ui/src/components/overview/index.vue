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


 * @describe: 
 * @Date: 2022-06-21 16:31:17
 * @LastEditTime: 2022-09-26 17:07:58
 * @FilePath: \ddh-ui\src\components\overview\index.vue
-->
<template>
  <div>
    <iframe v-if="reloadIframe" id="iframe" :src="dashboardUrl" class="overview-iframe" width="100%" frameborder="0"></iframe>
  </div>
</template>
<script>
export default {
  props: {
    dashboardUrl: String,
    reloadIframe: {
      type: Boolean,
      default: true
    }
  },
  computed: {
  },
  data() {
    return {
      iframeSrc1:
        "http://172.31.96.15:3000/d/_4gf-qOZz/1-zong-lan?orgId=1&refresh=5s&fromType=et",
    };
  },
};
</script>
<style lang="less" scoped>
.overview-iframe {
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  height: 100%;
}
</style>