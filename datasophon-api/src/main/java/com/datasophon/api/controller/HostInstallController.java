/*
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
 */

package com.datasophon.api.controller;

import com.datasophon.api.security.UserPermission;
import com.datasophon.api.service.InstallService;
import com.datasophon.common.utils.Result;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("host/install")
public class HostInstallController {
    
    @Autowired
    private InstallService installService;
    
    /**
     * 获取安装步骤
     */
    @GetMapping("/getInstallStep")
    public Result getInstallStep(Integer type) {
        return installService.getInstallStep(type);
    }
    
    /**
     * 解析主机列表
     */
    @PostMapping("/analysisHostList")
    @UserPermission
    public Result analysisHostList(@RequestParam Integer clusterId,
                                   @RequestParam @NotBlank(message = "主机列表不能为空") String hosts,
                                   @RequestParam @Pattern(regexp = "(?=.*?[a-z_])[a-zA-Z0-9._\\-]{1,30}", message = "非法的SSH用户名") String sshUser,
                                   @RequestParam @NotNull(message = "SSH端口必填") @Min(value = 1, message = "非法的SSH端口") @Max(value = 65535, message = "非法的SSH端口") Integer sshPort,
                                   @RequestParam Integer page,
                                   @RequestParam Integer pageSize) {
        return installService.analysisHostList(clusterId, hosts, sshUser, sshPort, page, pageSize);
    }
    
    /**
     * 查询主机校验状态
     */
    @PostMapping("/getHostCheckStatus")
    @UserPermission
    public Result getHostCheckStatus(Integer clusterId, String sshUser, Integer sshPort) {
        return installService.getHostCheckStatus(clusterId, sshUser, sshPort);
    }
    
    /**
     * 重新进行主机环境校验
     */
    @PostMapping("/rehostCheck")
    @UserPermission
    public Result rehostCheck(Integer clusterId, String hostnames, String sshUser, Integer sshPort) {
        return installService.rehostCheck(clusterId, hostnames, sshUser, sshPort);
    }
    
    /**
     * 查询主机校验是否全部完成
     */
    @PostMapping("/hostCheckCompleted")
    @UserPermission
    public Result hostCheckCompleted(Integer clusterId) {
        return installService.hostCheckCompleted(clusterId);
    }
    
    /**
     * 主机管理agent分发安装进度列表
     */
    @PostMapping("/dispatcherHostAgentList")
    @UserPermission
    public Result dispatcherHostAgentList(Integer clusterId, Integer installStateCode, Integer page, Integer pageSize) {
        return installService.dispatcherHostAgentList(clusterId, installStateCode, page, pageSize);
    }
    
    @PostMapping("/dispatcherHostAgentCompleted")
    public Result dispatcherHostAgentCompleted(Integer clusterId) {
        return installService.dispatcherHostAgentCompleted(clusterId);
    }
    
    /**
     * 主机管理agent分发取消
     */
    @PostMapping("/cancelDispatcherHostAgent")
    public Result cancelDispatcherHostAgent(Integer clusterId, String hostname, Integer installStateCode) {
        return installService.cancelDispatcherHostAgent(clusterId, hostname, installStateCode);
    }
    
    /**
     * 主机管理agent分发安装重试
     *
     * @param clusterId
     * @param hostnames
     * @return
     */
    @PostMapping("/reStartDispatcherHostAgent")
    public Result reStartDispatcherHostAgent(Integer clusterId, String hostnames) {
        return installService.reStartDispatcherHostAgent(clusterId, hostnames);
    }
    
    /**
     * 主机管理agent操作(启动(start)、停止(stop)、重启(restart))
     * @param clusterHostIds
     * @param commandType
     * @return
     */
    @PostMapping("/generateHostAgentCommand")
    public Result generateHostAgentCommand(
                                           @RequestParam String clusterHostIds,
                                           @RequestParam String commandType) throws Exception {
        return installService.generateHostAgentCommand(clusterHostIds, commandType);
    }
    
    /**
     * 启动/停止 主机上服务启动
     * @param clusterHostIds
     * @param commandType
     * @return
     */
    @PostMapping("/generateHostServiceCommand")
    public Result generateHostServiceCommand(
                                             @RequestParam String clusterHostIds,
                                             @RequestParam String commandType) throws Exception {
        return installService.generateHostServiceCommand(clusterHostIds, commandType);
    }
    
}
