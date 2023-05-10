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
 * @Date: 2022-05-24 10:22:10
 * @LastEditTime: 2022-06-14 11:30:14
 * @FilePath: \ddh-ui\src\App.vue
-->
<template>
  <a-config-provider :locale="locale" :get-popup-container="popContainer">
    <router-view/>
  </a-config-provider>
</template>

<script>
import {enquireScreen} from './utils/util'
import {mapState, mapMutations} from 'vuex'
import themeUtil from '@/utils/themeUtil';
import {getI18nKey} from '@/utils/routerUtil'

export default {
  name: 'App',
  data() {
    return {
      locale: {}
    }
  },
  created () {
    this.setHtmlTitle()
    this.setLanguage(this.lang)
    enquireScreen(isMobile => this.setDevice(isMobile))
  },
  mounted() {
    this.setWeekModeTheme(this.weekMode)
  },
  watch: {
    weekMode(val) {
      this.setWeekModeTheme(val)
    },
    lang(val) {
      this.setLanguage(val)
      this.setHtmlTitle()
    },
    $route() {
      this.setHtmlTitle()
    },
    'theme.mode': function(val) {
      let closeMessage = this.$message.loading(`您选择了主题模式 ${val}, 正在切换...`)
      themeUtil.changeThemeColor(this.theme.color, val).then(closeMessage)
    },
    'theme.color': function(val) {
      let closeMessage = this.$message.loading(`您选择了主题色 ${val}, 正在切换...`)
      themeUtil.changeThemeColor(val, this.theme.mode).then(closeMessage)
    },
    'layout': function() {
      window.dispatchEvent(new Event('resize'))
    }
  },
  computed: {
    ...mapState('setting', ['layout', 'theme', 'weekMode', 'lang'])
  },
  methods: {
    ...mapMutations('setting', ['setDevice']),
    setWeekModeTheme(weekMode) {
      if (weekMode) {
        document.body.classList.add('week-mode')
      } else {
        document.body.classList.remove('week-mode')
      }
    },
    setLanguage(lang) {
      this.$i18n.locale = lang
      switch (lang) {
      case 'CN':
        this.locale = require('ant-design-vue/es/locale-provider/zh_CN').default
        break
      case 'HK':
        this.locale = require('ant-design-vue/es/locale-provider/zh_TW').default
        break
      case 'US':
      default:
        this.locale = require('ant-design-vue/es/locale-provider/en_US').default
        break
      }
    },
    setHtmlTitle() {
      const route = this.$route
      const key = route.path === '/' ? 'home.name' : getI18nKey(route.matched[route.matched.length - 1].path)
      document.title = this.$t(key)
    },
    popContainer() {
      return document.getElementById("popContainer")
    }
  }
}
</script>

<style lang="less" scoped>
  #id{
  }
</style>
