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

import store from '@/store'

function changeRouter(resData, clusterId) {
  let menuData = JSON.parse(localStorage.getItem('menuData'))
  menuData.forEach((item) => {
    if (item.path === "service-manage") {
      item.children = [];
      resData.map((serviceItem) => {
        item.children.push({
          name: serviceItem.serviceName,
          label: serviceItem.label,
          meta: {
            notAlive: true,
            isCluster: 'isCluster',
            params: {
              serviceId: serviceItem.id
            },
            obj: serviceItem,
            authority: {
              permission: "*",
            },
            permission: [{
              permission: "*",
            },
            {
              permission: "*",
            },
            ],
          },
          fullPath: `/service-manage/service-list/${serviceItem.id}`,
          path: `service-list/${serviceItem.id}`,
          component: () => import("@/pages/serviceManage/index"),
        });
      });
    }
  });
  store.commit('setting/setClusterId', clusterId)
  store.commit('setting/setMenuData', menuData)
  store.commit('setting/setIsCluster', 'isCluster')
  // 首次进入拿到菜单等数据 然后再去做刷新的操作
  setTimeout(() => {
    store.dispatch('setting/getRunningClusterList')
  }, global.intervalTime)
}
export {
  changeRouter
}