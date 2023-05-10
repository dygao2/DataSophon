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

module.exports = {
  title: 'DataSophon',
  description: 'DataSophon',
  base: '/vue-antd-admin-docs/',
  head: [
    ['link', { rel: 'icon', href: '/favicon.ico' }]
  ],
  themeConfig: {
    logo: '/logo.png',
    repo: 'iczer/vue-antd-admin',
    docsDir: 'docs',
    editLinks: true,
    editLinkText: '在 Github 上帮助我们编辑此页',
    nav: [
      {text: '指南', link: '/'},
      {text: '配置', link: '/develop/layout'},
      {text: '主题', link: '/advance/theme'},
    ],
    lastUpdated: 'Last Updated',
    sidebar: [
      {
        title: '开始',
        collapsable: false,
        children: [
          '/start/use', '/start/faq'
        ]
      },
      {
        title: '开发',
        collapsable: false,
        children: [
          '/develop/layout', '/develop/router', '/develop/page', '/develop/theme', '/develop/service', '/develop/mock'
        ]
      },
      {
        title: '进阶',
        collapsable: false,
        children: [
          '/advance/i18n', '/advance/async', '/advance/authority', '/advance/login', '/advance/guard', '/advance/interceptors',
          '/advance/api'
        ]
      },
      {
        title: '其它',
        collapsable: false,
        children: [
          '/other/upgrade', '/other/community'
        ]
      }
    ],
    nextLinks: true,
    prevLinks: true,
  },
  plugins: ['@vuepress/back-to-top', require('./plugins/alert')],
  markdown: {
    lineNumbers: true
  }
}
