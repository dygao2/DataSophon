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
 * @Date: 2022-05-24 10:28:22
 * @LastEditTime: 2022-06-20 15:26:00
 * @FilePath: \ddh-ui\src\components\exception\ExceptionPage.vue
-->
<template>
  <div class="exception-page">
    <div class="img">
      <img :src="config[type].img" />
    </div>
    <div class="content">
      <h1>{{config[type].title}}</h1>
      <div class="desc">{{config[type].desc}}</div>
      <div class="action">
        <a-button type="primary" @click="backHome">返回首页</a-button>
      </div>
    </div>
  </div>
</template>

<script>
import Config from './typeConfig'

export default {
  name: 'ExceptionPage',
  props: ['type', 'homeRoute'],
  data () {
    return {
      config: Config
    }
  },
  methods: {
    backHome() {
      if (this.homeRoute) {
        this.$router.push('/')
      }
      this.$emit('backHome', this.type)
    }
  }
}
</script>

<style lang="less" scoped>
  .exception-page{
    border-radius: 4px;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: @base-bg-color;
    .img{
      padding-right: 52px;
      zoom: 1;
      img{
        max-width: 430px;
      }
    }
    .content{
      h1{
        color: #434e59;
        font-size: 72px;
        font-weight: 600;
        line-height: 72px;
        margin-bottom: 24px;
      }
      .desc{
        color: @text-color-second;
        font-size: 20px;
        line-height: 28px;
        margin-bottom: 16px;
      }
    }
  }

</style>
