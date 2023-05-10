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


 * @Date: 2022-06-13 14:04:05
 * @LastEditTime: 2022-07-13 14:36:07
 * @FilePath: \ddh-ui\src\components\logs\index.vue
-->
<template>
  <div class="logs-container">
    <Editor :propsCode="logData" ref="child"/>
    <div v-if="!hideCancel" class="footer">
      <a-button class="mgr10" @click="getModal">刷新</a-button>
    </div>
  </div>
</template>
<script>

import Editor from "@/components/editor";

export default {
  name: "LogsContainer",
  components: {
    Editor
  },
  props: { logData: String, hideCancel: Boolean },
  data() {
    return {
    };
  },
  watch: {
    
  },
  computed: {
  },
  methods: {
    getModal() {
      this.$emit('getLog');
      
      this.$refs.child.handleCancel();
    },
  },
};
</script>
<style lang="less" scoped>
.logs-container {
  height: 100%;
  display: flex;
  justify-content: space-between;
  flex-direction: column;
  .footer {
    // width: 1300px;
    margin: 0 100px;
    height: 64px;
    background: rgba(242, 244, 247, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    button {
      width: 86px;
    }
    /deep/
      .ant-btn.ant-btn-loading:not(.ant-btn-circle):not(.ant-btn-circle-outline):not(.ant-btn-icon-only) {
      padding-left: 20px;
    }
  }
}
</style>