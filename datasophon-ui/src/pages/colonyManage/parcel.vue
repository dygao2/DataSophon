<!--
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
 -->
<template>
    <a-spin :spinning="spinning">
        <div class="frame-list card-shadow">
            <a-form ref="formRef" layout="horizontal">
                <a-form-item label="内置存储库">
                  <a-input style="width: 60%; margin-right: 8px" :value="ddhParcelPath" :disabled="true" />
                </a-form-item>
                <a-divider style="border-color: #7cb305" dashed />
                <a-form-item
                    v-for="parcel in parcelList"
                    :key="parcel.parcelId"
                    :label="parcel.parcelName">
                    <a-input-search
                      v-model="parcel.parcelPath"
                      placeholder="please input parcel url"
                      enter-button
                      @search="onSearch(parcel)"
                      style="width: 60%; margin-right: 8px"
                    /> 
                    <a-card v-if="parcel.components != undefined && parcel.components.length > 0" style="width: 58%;">
                        <div v-for="comp in parcel.components" :key="comp.name" style="margin-top: 20px;">
                            <a-row>
                              <a-col :span="12">{{ comp.label }}-{{ comp.version }}  - {{ comp.description }}</a-col>
                              <a-col :span="12">
                                 <a-button v-if="comp.state == undefined" type="dashed" style="margin-left: 80px" @click="handleDownload(comp, parcel.parcelPath)">下载</a-button>
                                 <a-button v-else-if="comp.state == 'success' && comp.step == 'download'" type="dashed" style="margin-left: 80px" @click="handleInstall(comp, parcel.parcelPath)" danger>安装</a-button>
                              </a-col>
                            </a-row>
                            <a-row v-if="comp.state != undefined">
                              <a-col :span="12">
                              <!-- 下载，安装：验证、安装 -->
                                <p v-if="comp.state == undefined">
                                    <a-progress :percent="comp.process" :strokeWidth="5" size="small" :format="percent => formatState(percent, comp)" />
                                </p>
                                <p v-else-if="comp.state == 'executing'">
                                    <a-progress :percent="comp.process" :strokeWidth="5" size="small" :format="percent => formatState(percent, comp)"/>
                                </p>
                                <p v-else-if="comp.state == 'success'">
                                    <a-progress :percent="comp.process" :strokeWidth="5" size="small" :format="percent => formatState(percent, comp)"/>
                                </p>
                                <p v-else-if="comp.state == 'fail'">
                                    <a-progress :percent="comp.process" status="exception" :strokeWidth="5" size="small" :format="percent => formatState(percent, comp)"/>
                                </p>
                              </a-col>
                              <a-col :span="12"></a-col>
                            </a-row>
                        </div>
                    </a-card>
                </a-form-item>
            </a-form>
        </div>
    </a-spin>
</template>
<script>
export default {
    name: "ParcelList",
    data() {
        return {
            loading: false,
            spinning: false,
            ddhParcelPath: "file:///opt/datasophon/DDP/packages",
            formState: {
                name: "XXXXXX"
            },
            parcelList: [],
            taskObj: {},
            parcelProcess: {
                open: false,
                data: "",
                name: "",
                state: undefined,
                taskId: undefined,
                process: 0,
                rolllogThread: undefined
            },
        };
    },
    methods: {
        getParcelList() {
            this.parcelList = [];
            /*
            this.spinning = true;
            this.$axiosPost(global.API.getParcelList, {}).then((res) => {
                this.spinning = false;
                if (res.code === 200) {
                    this.parcelList = res.data;
                }
            });
            */
            this.parcelList.push(
            {
                parcelId: 234,
                parcelName: "添加第三方存储库",
                parcelPath: "",
                parcelFit: 1,
                frame: "DDP-1.0.0",
                components: []
            }
            );
        },

        onSearch(parcel){
            if(parcel.parcelPath == "") {
                this.$message.warning('请输入 Parcel 存储库地址。')
                return;
            }
            console.log(parcel);
            this.$axiosPost(global.API.getParcelParse, {url: parcel.parcelPath }).then((res) => {
                if (res.code === 200) {
                    parcel.components = res.data.components;
                }
            });
        },
        formatState(percent, comp) {
            console.log(comp)
            if(this.taskObj && comp.step == 'download') {
                if(comp.state == 'executing') {
                    return "正在下载：" + percent + "%";
                } else if(comp.state == 'success') {
                    return "下载成功";
                } else {
                    return "下载失败";
                }
            } else if (this.taskObj && comp.step == 'install') {
                if (comp.state == 'executing') {
                    return "正在安装：" + percent + "%";
                } else if (comp.state == 'success') {
                    return "安装成功";
                } else {
                    return "安装失败";
                }
            }
            return percent + "%";
        },
        handleDownload(comp, url) {
            if(this.taskObj && this.taskObj.state == 'executing') {
                this.$message.warning('一个操作正在进行, 请稍后操作。')
                return;
            }
            console.log(comp);
            this.$axiosPost(global.API.downloadComponent, { url: url, parcelName: comp.name }).then((res) => {
                if (res.code === 200) {
                    comp.md5 = res.data.md5;
                    comp.process = (res.data.process * 100);
                    comp.state = res.data.state;
                    comp.step = res.data.step;

                    this.parcelProcess.open = true;
                    this.viewTaskLog(comp)
                }
            });
        },
        handleInstall(comp, url) {
            if (this.taskObj && this.taskObj.state == 'executing') {
                this.$message.warning('一个操作正在进行, 请稍后操作。')
                return;
            }
            console.log(comp);
            this.$axiosPost(global.API.installComponent, { md5: comp.md5, packageName: comp.name }).then((res) => {
                if (res.code === 200) {
                    comp.process = (res.data.process * 100);
                    comp.state = res.data.state;
                    comp.step = res.data.step;

                    this.parcelProcess.open = true;
                    this.viewTaskLog(comp)
                }
            });
        },

        viewTaskLog(row) {
            this.taskObj = row
            this.parcelProcess.state = "executing"
            //滚动查看日志
            const _this = this;
            this.$axiosGet(global.API.getParcelProcess, { md5: row.md5}).then(response => {
                if (response.code === 200) {
                    row.state = response.data.state
                    row.process = (response.data.process * 100)

                    if (this.parcelProcess.rolllogThread != undefined) {
                        clearTimeout(this.parcelProcess.rolllogThread);
                        this.parcelProcess.rolllogThread = undefined
                    }

                    if (response.data.process >= 100 && response.data.state != 'executing') {
                        this.parcelProcess.open = false;
                    }

                    // 窗口在打开着，并且进度小于 100, 任务未完成，一直获取
                    if (response.data.process <= 100 && response.data.state == 'executing' && this.parcelProcess.open) {
                        this.parcelProcess.rolllogThread = setTimeout(() => { this.viewTaskLog(row) }, 3000);
                    }
                }
            })
        },
    },
    mounted() {
        this.parcelProcess.open = false;
        this.getParcelList();
    },
};
</script>

<style lang="less" scoped>
.frame-list {
    background: #fff;
    padding: 20px 20px;
}
</style>
