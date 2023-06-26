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
 * @LastEditTime: 2022-07-27 15:54:37
 * @FilePath: \ddh-ui\src\pages\colonyManage\frame.vue
-->
<template>
  <a-spin :spinning="spinning">
    <div class="frame-list card-shadow">
      <a-tabs default-active-key="1" @change="callback">
        <a-tab-pane v-for="(item, index) in frameList" :key="index+1" :tab="item.frameCode">
          <a-table :columns="loadTable()" class="release-table-custom" :dataSource="item.frameServiceList" rowKey="id" :pagination="false">

          </a-table>
        </a-tab-pane>
      </a-tabs>
    </div>
  </a-spin>
</template>

<script>
export default {
  name: "FrameList",
  data() {
    return {
      loading: false,
      spinning: false,
      frameList: [],
      tableColumns: [
        { title: "序号", key: "index", width: 80 },
        { title: "服务", key: "serviceName" },
        { title: "版本", key: "serviceVersion" },
        { title: "描述", key: "serviceDesc", ellipsis: true },
        { title: "操作", key: "action", width: 80, align: "center"  },
      ],
    };
  },
  methods: {
    callback(key) {
      console.log(key);
    },
    loadTable() {
      let that = this;
      let columns = that.tableColumns;
      return columns.map((item, index) => {
        return {
          title: item.title,
          key: item.key,
          fixed: item.fixed ? item.fixed : "",
          width: item.width ? item.width : "",
          align: item.align ? item.align : "left",
          ellipsis: item.ellipsis ? item.ellipsis : "",
          customRender: (text, record, index) => {
            if (item.key == "index") {
              return `${index + 1}`;
            } else if (item.key == "action") {
              let _this = this
              const child = _this.$createElement('a', {
                domProps: {
                  innerHTML: "删除"
                },
                on: {
                  click: function () {
                    _this.onDelete(record)
                  }
                }
              })
              return child;
            } else {
              return <span title={record[item.key]}> {record[item.key]} </span>;
            }
          },
        };
      });
    },
    getFrameList() {
      this.spinning = true;
      this.$axiosPost(global.API.getFrameList, {}).then((res) => {
        this.spinning = false;
        if (res.code === 200) {
          this.frameList = res.data;
        }
      });
    },
    onDelete(record) {
      console.log(record)
      let self = this
      this.$confirm({
        title: '确认提示',
        okText: '确认',
        cancelText: '取消',
        content:  (
          <div style="margin-top:20px">
            <div style="font-size: 16px;color: #555555;">
              {'是否确认删除 ' + record.serviceName + ' 服务？'}
            </div>
            <div style="margin-top:20px;text-align:right;padding:0 30px 30px 30px">
              <a-button
                style="margin-right:10px;"
                type="primary"
                onClick={() => {
                  self.$axiosGet(global.API.deleteService + "/" + record.id, {}).then((res) => {
                    if (res.code === 200) {
                      self.getFrameList();
                      self.$destroyAll();
                    }
                  });
                }}
              >
                确定
              </a-button>
              <a-button
                style="margin-right:10px;"
                onClick={() => self.$destroyAll() }
              >
                取消
              </a-button>
            </div>
          </div>
        ),
        okType: 'danger',
        closable: true,
      });
    }
  },
  mounted() {
    this.getFrameList();
  },
};
</script>

<style lang="less" scoped>
.frame-list {
  background: #fff;
  padding:0 20px 20px;
}
</style>
