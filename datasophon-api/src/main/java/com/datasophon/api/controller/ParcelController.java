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

import com.datasophon.api.load.LoadServiceMeta;
import com.datasophon.api.service.ClusterInfoService;
import com.datasophon.api.service.FrameInfoService;
import com.datasophon.api.service.FrameServiceService;
import com.datasophon.common.Constants;
import com.datasophon.common.utils.FileUtils;
import com.datasophon.common.utils.Result;
import com.datasophon.dao.entity.ClusterInfoEntity;
import com.datasophon.dao.entity.FrameInfoEntity;
import com.datasophon.dao.entity.FrameServiceEntity;
import com.datasophon.dao.model.ComponentVO;
import com.datasophon.dao.model.ParcelInfoVO;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.util.concurrent.AtomicDouble;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.http.HttpUtil;

/**
 *
 * 远程框架管理（Parcel）管理，支持 DDP 从第三方加载框架并安装
 *
 *
 * @author zhenqin
 */
@Slf4j
@RestController
@RequestMapping("/cluster/parcel")
public class ParcelController implements DisposableBean {
    
    /**
     * 组件下载进程缓存，不会安装太多组件的，直接采用内存
     */
    final Map<String, ComponentVO> componentCache = new ConcurrentHashMap<>();
    
    /**
     * 异步操作的任务
     */
    final Map<String, CompletableFuture> asyncTaskCache = new ConcurrentHashMap<>();
    
    @Autowired
    private FrameServiceService frameServiceService;
    
    @Autowired
    private FrameInfoService frameInfoService;
    
    @Autowired
    private ClusterInfoService clusterInfoService;
    
    @Autowired
    LoadServiceMeta loadServiceMeta;
    
    /**
     * 列表
     */
    @GetMapping("/list")
    public Result list() {
        return Result.success("");
    }
    
    /**
     * 解析 URL 中的 parcel 信息
     */
    @PostMapping("/parse")
    public Result parseParcel(ParcelInfoVO info) {
        log.info(JSON.toJSONString(info));
        String url = info.getUrl();
        // 解析 URL
        if (!url.endsWith("manifest.json")) {
            if (url.endsWith("/")) {
                url = url + "manifest.json";
            } else {
                url = url + "/manifest.json";
            }
        }
        // 查询所有的 框架
        List<FrameInfoEntity> installFrames = frameInfoService.list();
        final Map<String, List<FrameInfoEntity>> frameCodeMapping =
                installFrames.stream().collect(Collectors.groupingBy(FrameInfoEntity::getFrameCode));
        
        try {
            JSONObject json = JSON.parseObject(httpGet(url));
            ParcelInfoVO parcelInfo = JSON.toJavaObject((JSONObject) json.get("parcel"), ParcelInfoVO.class);
            if (frameCodeMapping.get(parcelInfo.getMeta()) == null) {
                // 不支持的框架版本
                return Result.error("Unsupported frame: " + parcelInfo.getMeta());
            }
            parcelInfo.setUrl(url);
            parcelInfo.setLastUpdated(json.getLong("lastUpdated"));
            if (parcelInfo.getComponents() != null && !parcelInfo.getComponents().isEmpty()) {
                // 仅过滤支持的架构
                final List<ComponentVO> componentVOS = parcelInfo.getComponents(); /*
                                                                                    * .stream().filter(it -> { return SystemUtils.OS_ARCH. equalsIgnoreCase(it.getArch());
                                                                                    * }).collect(Collectors.toList());
                                                                                    */
                parcelInfo.setComponents(componentVOS);
                log.info(JSON.toJSONString(parcelInfo));
                return Result.success(parcelInfo);
            }
        } catch (Exception e) {
            log.warn("invalid parcel url.", e);
            return Result.error("Invalid DHH Parcel Endpoint, Cause: " + e.getMessage());
        }
        return Result.error("Invalid DHH Parcel Endpoint!");
    }
    
    /**
     * 下载 Parcel
     */
    @PostMapping("/download")
    public Result downloadParcel(ParcelInfoVO info) {
        log.info(JSON.toJSONString(info));
        String url = info.getUrl();
        // 解析 URL
        if (!url.endsWith("manifest.json")) {
            if (url.endsWith("/")) {
                url = url + "manifest.json";
            } else {
                url = url + "/manifest.json";
            }
        }
        try {
            List<FrameInfoEntity> installFrames = frameInfoService.list();
            final Map<String, List<FrameInfoEntity>> frameCodeMapping =
                    installFrames.stream().collect(Collectors.groupingBy(FrameInfoEntity::getFrameCode));
            
            JSONObject json = JSONObject.parseObject(httpGet(url));
            final ParcelInfoVO parcelInfo = JSON.toJavaObject((JSONObject) json.get("parcel"), ParcelInfoVO.class);
            parcelInfo.setUrl(url);
            parcelInfo.setLastUpdated(json.getLong("lastUpdated"));
            if (frameCodeMapping.get(parcelInfo.getMeta()) == null) {
                // 不支持的框架版本
                return Result.error("Unsupported frame: " + parcelInfo.getMeta());
            }
            
            if (parcelInfo.getComponents() != null && !parcelInfo.getComponents().isEmpty()) {
                final List<ComponentVO> componentVOS = parcelInfo.getComponents().stream().filter(it -> {
                    return info.getParcelName().equals(it.getName());
                }).collect(Collectors.toList());
                if (componentVOS.isEmpty()) {
                    throw new IllegalStateException("No component package: " + info.getParcelName());
                }
                final ComponentVO componentVO = componentVOS.get(0);
                final String packagePath = getParcelPath(url, componentVO.getPackageName());
                File ddhTmpDir = new File(SystemUtils.getJavaIoTmpDir(), "jdh");
                if (!ddhTmpDir.exists()) {
                    ddhTmpDir.mkdirs();
                }
                
                // 开始下载，这里需要做成带进度
                componentVO.setProcess(0.0f);
                componentVO.setStep("download");
                componentVO.setState("executing");
                // 异步下载
                final CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        log.info("download parcel: {}", packagePath);
                        // 开始下载，这里需要做成带进度
                        final AtomicDouble process = new AtomicDouble(0.0f);
                        File filePath = HttpUtil.downloadFileFromUrl(packagePath, ddhTmpDir, new StreamProgress() {
                            
                            @Override
                            public void start() {
                                log.info("start to download: {} to dir: {}", packagePath, ddhTmpDir.getAbsolutePath());
                            }
                            
                            @Override
                            public void progress(long total, long progressSize) {
                                float p = progressSize * 1.0f / total;
                                if (p > process.get()) {
                                    // 每 10% 推送一次进度
                                    float per = NumberUtil.round(process.get(), 2).floatValue();
                                    componentVO.setProcess(per);
                                    log.info("download {} new process: {}", componentVO.getPackageName(), per);
                                    process.set(per + 0.1f);
                                }
                            }
                            
                            @Override
                            public void finish() {
                            }
                        });
                        if (!StringUtils.equals(componentVO.getPackageName(), filePath.getName())) {
                            filePath = FileUtil.rename(filePath, componentVO.getPackageName(), true);
                        }
                        componentVO.setHash(filePath.getAbsolutePath());
                        // 下载成功
                        componentVO.setProcess(1.0f);
                        componentVO.setStep("download");
                        componentVO.setState("success");
                        log.info("download {} success, finish process: {}", componentVO.getPackageName(),
                                componentVO.getProcess());
                    } catch (Exception e) {
                        log.error("download parcel error!", e);
                        // 下载失败
                        componentVO.setProcess(1.0f);
                        componentVO.setStep("download");
                        componentVO.setState("fail");
                        log.info("download {} fail, finish process: {}", componentVO.getPackageName(),
                                componentVO.getProcess());
                    }
                });
                asyncTaskCache.put(componentVO.getMd5(), future);
                // 框架支持的版本
                componentVO.setMeta(parcelInfo.getMeta());
                componentCache.put(componentVO.getMd5(), componentVO);
                log.info(JSON.toJSONString(componentVO));
                return Result.success(componentVO);
            }
            return Result.error("component: " + info.getParcelName() + " not found!");
        } catch (Exception e) {
            log.warn("download parcel error!", e);
            return Result.error("download parcel error, Cause: " + e.getMessage());
        }
    }
    
    /**
     * Install Parcel
     */
    @PostMapping("/install")
    public Result installParcel(ComponentVO info) {
        // 安装：验证 md5 or hash、安装,推送到 worker 节点，并且读取 meta 信息，写入数据库
        log.info(JSON.toJSONString(info));
        ComponentVO vo = componentCache.get(info.getMd5());
        if (vo == null) {
            return Result.error("component: " + info.getPackageName() + " not found!");
        }
        
        // 应用包
        File packageFile = new File(vo.getHash());
        if (!packageFile.exists()) {
            return Result.error("component: " + info.getPackageName() + " not found!");
        }
        
        // 检验是否合法
        List<FrameInfoEntity> installFrames = frameInfoService.list();
        final Map<String, List<FrameInfoEntity>> frameCodeMapping =
                installFrames.stream().collect(Collectors.groupingBy(FrameInfoEntity::getFrameCode));
        final List<FrameInfoEntity> frameInfoEntityList = frameCodeMapping.get(vo.getMeta());
        if (frameInfoEntityList == null || frameInfoEntityList.isEmpty()) {
            // 不支持的框架版本
            return Result.error("Unsupported frame: " + vo.getMeta());
        }
        // 当前安装的框架
        final FrameInfoEntity frameInfo = frameInfoEntityList.get(0);
        
        // 是否已经安装了组件？
        List<FrameServiceEntity> installService = frameServiceService.list(
                Wrappers.<FrameServiceEntity>lambdaQuery()
                        .eq(FrameServiceEntity::getServiceName, vo.getName())
                        .eq(FrameServiceEntity::getServiceVersion, vo.getVersion()));
        if (installService.isEmpty()) {
            // 防止包名称相同，覆盖了已经安装的，也防止包名称的污染
            installService = frameServiceService.list(
                    Wrappers.<FrameServiceEntity>lambdaQuery()
                            .eq(FrameServiceEntity::getPackageName, vo.getPackageName()));
        }
        // 已经安装的服务
        if (installService != null && !installService.isEmpty()) {
            return Result.error("已经安装组件: " + vo.getName() + "-" + vo.getVersion());
        }
        
        vo.setProcess(0.0f);
        vo.setStep("install");
        vo.setState("executing");
        final CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                String packageMd5 = FileUtils.md5(packageFile);
                if (!StringUtils.equals(packageMd5, vo.getMd5())) {
                    throw new IllegalStateException("component: " + info.getPackageName() + " md5 invalid!");
                }
                // 生成 md 5 校验文件
                File packageFileMd5 = new File(packageFile.getParent(), packageFile.getName() + ".md5");
                FileUtil.writeUtf8String(packageMd5, packageFileMd5);
                // 合法，开始安装
                vo.setProcess(0.3f);
                vo.setStep("install");
                vo.setState("executing");
                
                Thread.sleep(5000);
                
                // mv 到 /DDP/packages
                File targetPackageFile = new File(Constants.MASTER_MANAGE_PACKAGE_PATH, packageFile.getName());
                FileUtil.move(packageFile, targetPackageFile, true);
                log.info("move package file to: {}", targetPackageFile.getAbsolutePath());
                File targetPackageFileMd5 = new File(Constants.MASTER_MANAGE_PACKAGE_PATH, packageFileMd5.getName());
                FileUtil.move(packageFileMd5, targetPackageFileMd5, true);
                log.info("move package md5 file to: {}", targetPackageFileMd5.getAbsolutePath());
                
                // 合法，开始安装
                vo.setProcess(0.6f);
                vo.setStep("install");
                vo.setState("executing");
                
                Thread.sleep(5000);
                
                String frameCode = frameInfo.getFrameCode();
                List<ClusterInfoEntity> clusters = clusterInfoService.list();
                // service ddl 存在的目录，读取压缩包内的 meta 文件
                String tempFileName = "/meta/service_ddl.json";
                String serviceDdl =
                        FileUtils.readTargzTextFile(targetPackageFile, tempFileName, StandardCharsets.UTF_8);
                String serviceName = vo.getName();
                loadServiceMeta.parseServiceDdl(frameCode, clusters, frameInfo, serviceName, serviceDdl);
                // 成功，安装结束
                vo.setProcess(1.0f);
                vo.setStep("install");
                vo.setState("success");
            } catch (Exception e) {
                log.error("install parcel error!", e);
                // 下载失败
                vo.setProcess(1.0f);
                vo.setStep("install");
                vo.setState("fail");
            }
        });
        asyncTaskCache.put(vo.getMd5(), future);
        // 返回安装，异步获取进度
        return Result.success(vo);
    }
    
    /**
     * 获取 Process 进度，简易方案
     */
    @GetMapping("/process")
    public Result getProcess(ComponentVO info) {
        ComponentVO vo = componentCache.get(info.getMd5());
        if (vo == null) {
            vo = new ComponentVO();
            // 错误的 ID
            vo.setProcess(0.0f);
            vo.setStep("download");
            vo.setState("success");
            log.warn("no task: {}", info.getMd5());
        }
        return Result.success(vo);
    }
    
    /**
     * http get
     * @param url
     * @return
     * @throws IOException
     */
    private String httpGet(String url) throws IOException {
        if (StringUtils.isBlank(url)) {
            throw new IllegalStateException("Invalid DDP Parcel Endpoint!");
        }
        return HttpUtil.get(url, 20000);
    }
    
    /**
     * parcel name
     * @param url
     * @param resourceName
     * @return
     */
    private String getParcelPath(String url, String resourceName) {
        final URI uri = URI.create(url);
        final Path urlParentPath = Paths.get(uri.getPath()).getParent();
        
        String urlStr = uri.toString();
        String prefix = urlStr.substring(0, urlStr.lastIndexOf(urlParentPath.toString()));
        
        URI newUrl = "/".equals(urlParentPath.toString()) ? URI.create(prefix + urlParentPath.toString() + resourceName)
                : URI.create(prefix + urlParentPath.toString() + "/" + resourceName);
        return newUrl.toString();
    }
    
    /**
     * 当 api-server 停止时，结束没有完成的任务
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        for (Map.Entry<String, CompletableFuture> entry : asyncTaskCache.entrySet()) {
            final CompletableFuture future = entry.getValue();
            try {
                if (!future.isDone()) {
                    future.cancel(true);
                }
            } catch (Exception ignore) {
            }
        }
    }
}
