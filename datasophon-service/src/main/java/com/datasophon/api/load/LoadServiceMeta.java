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

package com.datasophon.api.load;

import static com.datasophon.common.Constants.META_PATH;

import com.datasophon.api.service.ClusterInfoService;
import com.datasophon.api.service.ClusterServiceInstanceRoleGroupService;
import com.datasophon.api.service.ClusterServiceInstanceService;
import com.datasophon.api.service.ClusterServiceRoleGroupConfigService;
import com.datasophon.api.service.ClusterVariableService;
import com.datasophon.api.service.FrameInfoService;
import com.datasophon.api.service.FrameServiceRoleService;
import com.datasophon.api.service.FrameServiceService;
import com.datasophon.api.utils.CommonUtils;
import com.datasophon.api.utils.PackageUtils;
import com.datasophon.api.utils.ProcessUtils;
import com.datasophon.common.Constants;
import com.datasophon.common.model.ConfigWriter;
import com.datasophon.common.model.Generators;
import com.datasophon.common.model.ServiceConfig;
import com.datasophon.common.model.ServiceInfo;
import com.datasophon.common.model.ServiceRoleInfo;
import com.datasophon.dao.entity.ClusterInfoEntity;
import com.datasophon.dao.entity.ClusterServiceInstanceEntity;
import com.datasophon.dao.entity.ClusterServiceRoleGroupConfig;
import com.datasophon.dao.entity.ClusterVariable;
import com.datasophon.dao.entity.FrameInfoEntity;
import com.datasophon.dao.entity.FrameServiceEntity;
import com.datasophon.dao.entity.FrameServiceRoleEntity;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.crypto.SecureUtil;

@Component
public class LoadServiceMeta implements ApplicationRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(LoadServiceMeta.class);
    
    @Autowired
    private PropertyResolver propertyResolver;
    
    @Autowired
    private FrameServiceService frameServiceService;
    
    @Autowired
    private FrameInfoService frameInfoService;
    
    @Autowired
    private FrameServiceRoleService roleService;
    
    @Autowired
    private ClusterVariableService variableService;
    
    @Autowired
    private ClusterInfoService clusterInfoService;
    
    @Autowired
    private ConfigBean configBean;
    
    @Autowired
    private ClusterServiceInstanceService serviceInstanceService;
    
    @Autowired
    private ClusterServiceInstanceRoleGroupService roleGroupService;
    
    @Autowired
    private ClusterServiceRoleGroupConfigService roleGroupConfigService;
    
    private static final String HDFS = "HDFS";
    
    private static final String HADOOP = "HADOOP";
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) throws Exception {
        File[] ddps = FileUtil.ls(META_PATH);
        // load global variable, 加载 frame
        List<ClusterInfoEntity> clusters = clusterInfoService.list();
        loadGlobalVariables(clusters);
        
        for (File path : ddps) {
            List<File> files = FileUtil.loopFiles(path);
            String frameCode = path.getName();
            FrameInfoEntity frameInfo = saveClusterFrame(frameCode);
            // analysis file
            for (File file : files) {
                if (file.getName().endsWith(Constants.JSON)) {
                    String serviceName = file.getParentFile().getName();
                    String serviceDdl = FileReader.create(file).readString();
                    try {
                        parseServiceDdl(frameCode, clusters, frameInfo, serviceName, serviceDdl);
                    } catch (Exception e) {
                        logger.error("invalid service ddl file: " + serviceName, e);
                    }
                }
            }
        }
    }
    
    /**
     * 解析 DDL 并存储到 frame 库
     *
     * @param frameCode
     * @param clusters
     * @param frameInfo
     * @param serviceName
     * @param serviceDdl
     */
    public void parseServiceDdl(final String frameCode,
                                List<ClusterInfoEntity> clusters,
                                FrameInfoEntity frameInfo,
                                final String serviceName,
                                final String serviceDdl) {
        ServiceInfo serviceInfo = JSONObject.parseObject(serviceDdl, ServiceInfo.class);
        String serviceInfoMd5 = SecureUtil.md5(serviceDdl);
        
        // save service config
        List<ServiceConfig> allParameters = serviceInfo.getParameters();
        Map<String, ServiceConfig> map =
                allParameters.stream()
                        .collect(
                                Collectors.toMap(
                                        ServiceConfig::getName,
                                        serviceConfig -> serviceConfig,
                                        (v1, v2) -> v1));
        Map<Generators, List<ServiceConfig>> configFileMap = new HashMap<>();
        
        buildConfigFileMap(serviceInfo, map, configFileMap);
        
        PackageUtils.putServicePackageName(
                frameCode, serviceName, serviceInfo.getDecompressPackageName());
        
        putServiceHomeToVariable(frameCode,
                clusters, serviceName, serviceInfo.getDecompressPackageName());
        // save service and service config
        FrameServiceEntity serviceEntity =
                saveFrameService(
                        frameCode,
                        frameInfo,
                        serviceName,
                        serviceDdl,
                        serviceInfo,
                        serviceInfoMd5,
                        allParameters,
                        configFileMap);
        // save frame service role
        saveFrameServiceRole(frameCode, serviceName, serviceInfo, serviceEntity);
    }
    
    private void putServiceHomeToVariable(String frameCode,
                                          List<ClusterInfoEntity> clusters, String serviceName,
                                          String decompressPackageName) {
        for (ClusterInfoEntity cluster : clusters) {
            if (cluster.getClusterFrame().equals(frameCode)) {
                Map<String, String> globalVariables = GlobalVariables.get(cluster.getId());
                if (HDFS.equals(serviceName)) {
                    serviceName = HADOOP;
                }
                globalVariables.put(
                        "${" + serviceName + "_HOME}",
                        Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName);
            }
        }
    }
    
    private void saveFrameServiceRole(
                                      String frameCode,
                                      String serviceName,
                                      ServiceInfo serviceInfo,
                                      FrameServiceEntity serviceEntity) {
        List<ServiceRoleInfo> serviceRoles = serviceInfo.getRoles();
        
        for (ServiceRoleInfo serviceRole : serviceRoles) {
            serviceRole.setParentName(serviceName);
            String key =
                    frameCode
                            + Constants.UNDERLINE
                            + serviceInfo.getName()
                            + Constants.UNDERLINE
                            + serviceRole.getName();
            logger.info(
                    "put {} {} {} service role info into cache",
                    frameCode,
                    serviceName,
                    serviceRole.getName());
            if (StringUtils.isNotBlank(serviceRole.getJmxPort())) {
                logger.info(
                        "{} jmx port is :{} and the jmx key is: {}",
                        serviceRole.getName(),
                        serviceRole.getJmxPort(),
                        key);
                ServiceRoleJmxMap.put(key, serviceRole.getJmxPort());
            }
            ServiceRoleMap.put(key, serviceRole);
            String serviceRoleJson = JSONObject.toJSONString(serviceRole);
            String serviceRoleJsonMd5 = SecureUtil.md5(serviceRoleJson);
            // 持久化服务角色元信息至数据库
            FrameServiceRoleEntity role =
                    roleService.getServiceRoleByServiceIdAndServiceRoleName(
                            serviceEntity.getId(), serviceRole.getName());
            if (Objects.isNull(role)) {
                role = new FrameServiceRoleEntity();
                buildFrameServiceRole(
                        frameCode,
                        serviceEntity,
                        serviceRole,
                        serviceRoleJson,
                        serviceRoleJsonMd5,
                        role);
                roleService.save(role);
            } else if (!role.getServiceRoleJsonMd5().equals(serviceRoleJsonMd5)) {
                buildFrameServiceRole(
                        frameCode,
                        serviceEntity,
                        serviceRole,
                        serviceRoleJson,
                        serviceRoleJsonMd5,
                        role);
                roleService.updateById(role);
            }
        }
        logger.info("put {} {} service info into cache", frameCode, serviceName);
        ServiceInfoMap.put(frameCode + Constants.UNDERLINE + serviceName, serviceInfo);
    }
    
    private FrameServiceEntity saveFrameService(
                                                String frameCode,
                                                FrameInfoEntity frameInfo,
                                                String serviceName,
                                                String serviceDdl,
                                                ServiceInfo serviceInfo,
                                                String serviceInfoMd5,
                                                List<ServiceConfig> allParameters,
                                                Map<Generators, List<ServiceConfig>> configFileMap) {
        FrameServiceEntity serviceEntity =
                frameServiceService.getServiceByFrameIdAndServiceName(
                        frameInfo.getId(), serviceName);
        if (Objects.isNull(serviceEntity)) {
            serviceEntity = new FrameServiceEntity();
            buildServiceEntity(
                    frameCode,
                    frameInfo.getId(),
                    serviceName,
                    serviceDdl,
                    serviceInfo,
                    serviceInfoMd5,
                    serviceEntity,
                    configFileMap,
                    serviceInfo.getDecompressPackageName());
            
            frameServiceService.save(serviceEntity);
        } else if (!serviceEntity.getServiceJsonMd5().equals(serviceInfoMd5)) {
            String configMapStr = JSONObject.toJSONString(configFileMap);
            String configFileMapStrMd5 = SecureUtil.md5(configMapStr);
            if (!configFileMapStrMd5.equals(serviceEntity.getConfigFileJsonMd5())) {
                // update config
                updateServiceInstanceConfig(
                        frameCode, serviceInfo.getName(), serviceInfo.getParameters());
            }
            buildServiceEntity(
                    frameCode,
                    frameInfo.getId(),
                    serviceName,
                    serviceDdl,
                    serviceInfo,
                    serviceInfoMd5,
                    serviceEntity,
                    configFileMap,
                    serviceInfo.getDecompressPackageName());
            frameServiceService.updateById(serviceEntity);
        }
        
        ServiceConfigMap.put(
                frameCode + Constants.UNDERLINE + serviceInfo.getName() + Constants.CONFIG,
                allParameters);
        ServiceConfigFileMap.put(
                frameCode + Constants.UNDERLINE + serviceInfo.getName() + Constants.CONFIG_FILE,
                configFileMap);
        
        return serviceEntity;
    }
    
    private void buildConfigFileMap(
                                    ServiceInfo serviceInfo,
                                    Map<String, ServiceConfig> map,
                                    Map<Generators, List<ServiceConfig>> configFileMap) {
        ConfigWriter configWriter = serviceInfo.getConfigWriter();
        List<Generators> generators = configWriter.getGenerators().stream().filter(g -> {
            if (StringUtils.isNotEmpty(g.getConditionalOnProperty())) {
                return propertyResolver.getProperty(g.getConditionalOnProperty(), boolean.class, false);
            }
            return true;
        }).collect(Collectors.toList());
        for (Generators generator : generators) {
            List<ServiceConfig> list = new ArrayList<>();
            List<String> includeParams = generator.getIncludeParams();
            for (String includeParam : includeParams) {
                if (map.containsKey(includeParam)) {
                    ServiceConfig serviceConfig = map.get(includeParam);
                    ServiceConfig newConfig = new ServiceConfig();
                    BeanUtils.copyProperties(serviceConfig, newConfig);
                    list.add(newConfig);
                }
            }
            if (configFileMap.containsKey(generator)) {
                configFileMap.get(generator).addAll(list);
            } else {
                configFileMap.put(generator, list);
            }
        }
    }
    
    private FrameInfoEntity saveClusterFrame(String frameCode) {
        FrameInfoEntity frameInfo =
                frameInfoService.getOne(
                        new QueryWrapper<FrameInfoEntity>().eq("frame_code", frameCode));
        if (Objects.isNull(frameInfo)) {
            frameInfo = new FrameInfoEntity();
            frameInfo.setFrameCode(frameCode);
            frameInfoService.save(frameInfo);
        }
        return frameInfo;
    }
    
    public void loadGlobalVariables(List<ClusterInfoEntity> clusters) throws UnknownHostException {
        if (CollUtil.isNotEmpty(clusters)) {
            for (ClusterInfoEntity cluster : clusters) {
                HashMap<String, String> globalVariables = new HashMap<>();
                List<ClusterVariable> variables =
                        variableService.list(
                                new QueryWrapper<ClusterVariable>()
                                        .eq(Constants.CLUSTER_ID, cluster.getId()));
                for (ClusterVariable variable : variables) {
                    globalVariables.put(variable.getVariableName(), variable.getVariableValue());
                }
                globalVariables.put("${apiHost}", InetAddress.getLocalHost().getHostName());
                globalVariables.put("${apiPort}", configBean.getServerPort());
                globalVariables.put("${INSTALL_PATH}", Constants.INSTALL_PATH);
                
                GlobalVariables.put(cluster.getId(), globalVariables);
                
                ProcessUtils.createServiceActor(cluster);
            }
        }
    }
    
    private void updateServiceInstanceConfig(
                                             String frameCode, String serviceName, List<ServiceConfig> parameters) {
        // 查询frameCode相同的集群
        List<ClusterInfoEntity> clusters = clusterInfoService.getClusterByFrameCode(frameCode);
        // 查询集群的服务实例
        for (ClusterInfoEntity cluster : clusters) {
            ClusterServiceInstanceEntity serviceInstance =
                    serviceInstanceService.getServiceInstanceByClusterIdAndServiceName(
                            cluster.getId(), serviceName);
            if (Objects.nonNull(serviceInstance)) {
                ClusterServiceRoleGroupConfig config =
                        roleGroupService.getRoleGroupConfigByServiceId(serviceInstance.getId());
                String configJson = config.getConfigJson();
                List<ServiceConfig> serviceConfigs =
                        JSONArray.parseArray(configJson, ServiceConfig.class);
                ProcessUtils.addAll(serviceConfigs, parameters);
                // 更新服务实例的配置
                config.setConfigJson(JSONObject.toJSONString(serviceConfigs));
                roleGroupConfigService.updateById(config);
            }
        }
    }
    
    private void buildFrameServiceRole(
                                       String frameCode,
                                       FrameServiceEntity serviceEntity,
                                       ServiceRoleInfo serviceRole,
                                       String serviceRoleJson,
                                       String serviceRoleJsonMd5,
                                       FrameServiceRoleEntity role) {
        role.setServiceId(serviceEntity.getId());
        role.setServiceRoleName(serviceRole.getName());
        role.setCardinality(serviceRole.getCardinality());
        role.setFrameCode(frameCode);
        role.setServiceRoleJson(serviceRoleJson);
        role.setServiceRoleType(CommonUtils.convertRoleType(serviceRole.getRoleType().getName()));
        role.setJmxPort(serviceRole.getJmxPort());
        role.setServiceRoleJsonMd5(serviceRoleJsonMd5);
        role.setLogFile(serviceRole.getLogFile());
    }
    
    private void buildServiceEntity(
                                    String frameCode,
                                    Integer frameInfoId,
                                    String serviceName,
                                    String serviceDdl,
                                    ServiceInfo serviceInfo,
                                    String serviceInfoMd5,
                                    FrameServiceEntity serviceEntity,
                                    Map<Generators, List<ServiceConfig>> configFileMap,
                                    String decompressPackageName) {
        serviceEntity.setServiceName(serviceName);
        serviceEntity.setLabel(serviceInfo.getLabel());
        serviceEntity.setFrameId(frameInfoId);
        serviceEntity.setServiceDesc(serviceInfo.getDescription());
        serviceEntity.setServiceVersion(serviceInfo.getVersion());
        serviceEntity.setPackageName(serviceInfo.getPackageName());
        serviceEntity.setDependencies(StringUtils.join(serviceInfo.getDependencies(), ","));
        serviceEntity.setFrameCode(frameCode);
        serviceEntity.setServiceConfig(JSON.toJSONString(serviceInfo.getParameters()));
        serviceEntity.setServiceJson(serviceDdl);
        serviceEntity.setServiceJsonMd5(serviceInfoMd5);
        serviceEntity.setDecompressPackageName(decompressPackageName);
        serviceEntity.setConfigFileJson(JSONObject.toJSONString(configFileMap));
        serviceEntity.setConfigFileJsonMd5(SecureUtil.md5(serviceEntity.getConfigFileJson()));
        serviceEntity.setSortNum(serviceInfo.getSortNum());
    }
}
