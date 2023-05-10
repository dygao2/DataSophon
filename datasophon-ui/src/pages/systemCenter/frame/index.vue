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


 * @Date: 2022-06-09 10:11:22
 * @LastEditTime: 2023-03-15 17:35:42
 * @FilePath: \ddh-ui\src\pages\securityCenter\user.vue
-->

<template>
  <div class="frame-list">
    <a-row type="flex" align="middle">
      <a-col :span="24" style="text-align: right">
        <a-button style="margin: 10px 0px 10px 10px;" type="primary" @click="createFrame({})">{{'添加机架'}}</a-button>
      </a-col>
    </a-row>
    <a-card class="card-shadow">  
      <div class="table-info steps-body">
        <a-table @change="(pagination)=>{this.tableChange(pagination)}" :columns="columns" :loading="loading" :dataSource="dataSource" rowKey="id" :pagination="pagination"></a-table>
      </div>
    </a-card>  
  </div>
</template>

<script>
import AddFrame from "./commponents/addFrame.vue";
import DeleteFrame from "./commponents/deleteFrame.vue";
import { mapGetters, mapState, mapMutations } from "vuex";

export default {
  name: "FRAME",
  data() {
    return {
      clusterId: Number(localStorage.getItem("clusterId") || -1),
      params: {},
      pagination: {
        total: 0,
        pageSize: 10,
        current: 1,
        showSizeChanger: true,
        pageSizeOptions: ["10", "20", "50", "100"],
        showTotal: (total) => `共 ${total} 条`,
      },
      username:'',
      dataSource: [],
      loading: false,
      columns: [
        {
          title: "序号",
          key: "index",
          width: 70,
          customRender: (text, row, index) => {
            return (
              <span>
                {parseInt(
                  this.pagination.current === 1
                    ? index + 1
                    : index +
                        1 +
                        this.pagination.pageSize * (this.pagination.current - 1)
                )}
              </span>
            );
          },
        },
        { title: "机架名称", key: "rack", dataIndex: "rack" },
        {
          title: "操作",
          key: "action",
          width:180,
          customRender: (text, row, index) => {
            return (
              <span class="flex-container">
                <a class="btn-opt" onClick={() => this.deleteFrame(row)}>
                  删除
                </a>
              </span>
            );
          },
        },
      ],
    };
  },
  computed: {
    ...mapGetters("account", ["user"]),
  },
  methods: {
    tableChange(pagination,key) {
      this.pagination.current = pagination.current;
      this.pagination.pageSize = pagination.pageSize
      this.getFrameList();
    },
    getVal(val, filed) {
      this.params[`${filed}`] = val.target.value;
    },
    //   查询
    onSearch(key) {
      this.pagination.current = 1;
      this.getFrameList();
    },
    createFrame(obj,key) {
      const self = this;
      let width = 520;
      let title = JSON.stringify(obj) === "{}" ? "添加机架" : "编辑机架";
      let content = (
        <AddFrame detail={obj} callBack={() => self.getFrameList()} />
      );
      this.$confirm({
        width: width,
        title: title,
        content: content,
        closable: true,
        icon: () => {
          return <div />;
        },
      });
    },
    deleteFrame(obj,key) {
      const self = this;
      let width = 400;
      let content = (
        <DeleteFrame
          sysTypeTxt="机架"
          detail={obj}
          callBack={() => self.getFrameList()}
        />
      );
      this.$confirm({
        width: width,
        title: () => {
          return (
            <div>
              <a-icon
                type="question-circle"
                style="color:#2F7FD1 !important;margin-right:10px"
              />
              提示
            </div>
          );
        },
        content,
        closable: true,
        icon: () => {
          return <div />;
        },
      });
    },
    getFrameList() {
      this.loading = true;
      let params = {
        // pageSize: this.pagination.pageSize,
        // page: this.pagination.current,
        clusterId: this.clusterId
      };
      this.$axiosPost(global.API.getRackList, params).then((res) => {
        this.loading = false;
        this.dataSource = res.data;
        this.pagination.total = res.data.length;
      });
    },
  },
  mounted() {
    this.getFrameList();
  },
};
</script>

<style lang="less" scoped>
.frame-list {
  background: #f5f7f8;
  .btn-opt {
    border-radius: 1px;
    font-size: 12px;
    color: #0264c8;
    letter-spacing: 0;
    font-weight: 400;
    margin: 0 5px;
  }
}
</style>
