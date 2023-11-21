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


 * @Date: 2022-06-08 11:38:30
 * @LastEditTime: 2022-06-15 17:06:23
 * @FilePath: \ddh-ui\src\pages\colonyManage\commponents\authCluster.vue
-->
<template>
  <div style="padding-top: 20px">
    <a-form
      :label-col="labelCol"
      :wrapper-col="wrapperCol"
      :form="form"
      class="p0-32-10-32"
    >
      <a-form-item label="集群管理员">
          <a-select mode="multiple" v-decorator="['userIds', { rules: [{ required: false}]}]"  placeholder="请选择集群管理员">
               <a-select-option :value="item.id" v-for="(item,index) in userList" :key="index">{{item.username}}</a-select-option>
          </a-select>
      </a-form-item>
    </a-form>
    <div class="ant-modal-confirm-btns-new">
      <a-button
        style="margin-right: 10px"
        type="primary"
        @click.stop="handleSubmit"
        :loading="loading"
        >确认</a-button
      >
      <a-button @click.stop="formCancel">取消</a-button>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    detail: {
      type: Object,
      default: function () {
        return {};
      },
    },
    callBack:Function
  },
  data() {
    return {
      labelCol: {
        xs: { span: 24 },
        sm: { span: 7 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 17 },
      },
      form: this.$form.createForm(this),
      value1: "",
      loading: false,
      userList: [] //集群管理员列表
    };
  },
  watch: {},
  methods: {
    formCancel() {
      this.$destroyAll();
    },
    handleSubmit(e) {
      const _this = this
      e.preventDefault();
      this.form.validateFields((err, values) => {
        console.log(values);
        if (!err) {
          const params = {
            "userIds": values.userIds || ""
          }
          if (JSON.stringify(this.detail) !== '{}') params.clusterId = this.detail.id
          this.loading = true;
          this.$axiosPost(global.API.authCluster, params).then((res) => {
            this.loading = false;
            if (res.code === 200) {
              if (params.userIds.length > 0) {
                this.$message.success('授权成功', 2)
              }else{
                this.$message.success('取消授权成功', 2)
              }
              this.$destroyAll();
              _this.callBack();
            }
          }).catch((err) => {});
        }
      });
    },
    queryAllUser() {
      this.$axiosPost(global.API.queryAllUser, {}).then((res) => {
        if (res.code === 200) {
          this.userList = res.data
        }
      })
    }
  },
  mounted() {
    this.queryAllUser()
  },
};
</script>
<style lang="less" scoped>
</style>