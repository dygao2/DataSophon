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


 * @Date: 2022-05-24 10:28:22
 * @LastEditTime: 2022-06-02 16:19:05
 * @FilePath: \ddh-ui\src\layouts\CommonLayout.vue
-->
<template>
  <div class="common-layout">
    <div class="content"><slot></slot></div>
  </div>
</template>

<script>
import {mapState} from 'vuex'

export default {
  name: 'CommonLayout',
  computed: {
    ...mapState('setting', ['footerLinks', 'copyright'])
  }
}
</script>

<style scoped lang="less">
.common-layout{
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: auto;
  background-color: @layout-body-background;
  background-image: url('../assets/img/bg.png');
  background-repeat: no-repeat;
  background-size: 100% 92%;
  .content{
    padding: 32px 0;
    flex: 1;
    @media (min-width: 768px){
      padding: 28px 0 28px;
    }
  }
}
</style>
