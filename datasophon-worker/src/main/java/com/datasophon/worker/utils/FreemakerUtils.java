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

package com.datasophon.worker.utils;

import com.datasophon.common.Constants;
import com.datasophon.common.model.AlertItem;
import com.datasophon.common.model.Generators;
import com.datasophon.common.model.ServiceConfig;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

public class FreemakerUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(FreemakerUtils.class);
    
    public static void generateConfigFile(Generators generators,
                                          List<ServiceConfig> configs,
                                          String decompressPackageName) throws IOException, TemplateException {
        generateConfigFile(generators, configs, decompressPackageName, null);
    }
    
    /**
     *
     * 支持 从附加的目录加载 模版
     *
     * @param generators
     * @param configs
     * @param decompressPackageName
     * @param extPath
     * @throws IOException
     * @throws TemplateException
     */
    public static void generateConfigFile(Generators generators,
                                          List<ServiceConfig> configs,
                                          String decompressPackageName,
                                          String extPath) throws IOException, TemplateException {
        // 1.加载模板
        // 创建核心配置对象
        Configuration config = new Configuration(Configuration.getVersion());
        // 设置加载的目录
        List<TemplateLoader> loaderList = new ArrayList<>();
        loaderList.add(new ClassTemplateLoader(FreemakerUtils.class, "/templates"));
        if (StringUtils.isNotBlank(extPath) && new File(extPath).exists()) {
            // 如果 三方的 package 中存在 templates 模版，则直接加载
            loaderList.add(new FileTemplateLoader(new File(extPath)));
        }
        config.setTemplateLoader(new MultiTemplateLoader(loaderList.toArray(new TemplateLoader[0])));
        
        Map<String, Object> data = new HashMap<>();
        // 得到模板对象
        String configFormat = generators.getConfigFormat();
        Template template = null;
        if (Constants.XML.equals(configFormat)) {
            template = config.getTemplate("xml.ftl");
        }
        if (Constants.PROPERTIES.equals(configFormat)) {
            template = config.getTemplate("properties.ftl");
        }
        if (Constants.PROPERTIES2.equals(configFormat)) {
            template = config.getTemplate("properties2.ftl");
        }
        if (Constants.PROPERTIES3.equals(configFormat)) {
            template = config.getTemplate("properties3.ftl");
        }
        if (Constants.PROMETHEUS.equals(configFormat)) {
            template = config.getTemplate("alert.yml");
        }
        if (Constants.CUSTOM.equals(configFormat)) {
            template = config.getTemplate(generators.getTemplateName());
            data = configs.stream().filter(e -> "map".equals(e.getConfigType()))
                    .collect(Collectors.toMap(key -> key.getName(), value -> value.getValue()));
            configs = configs.stream().filter(e -> !"map".equals(e.getConfigType())).collect(Collectors.toList());
        }
        logger.info("load template: {} success.", template.getSourceName());
        data.put("itemList", configs);
        // 3.产生输出
        processOut(generators, template, data, decompressPackageName);
    }
    
    public static void generatePromAlertFile(Generators generators, List<AlertItem> configs,
                                             String serviceName) throws IOException, TemplateException {
        // 创建核心配置对象
        Configuration config = new Configuration(Configuration.getVersion());
        // 设置加载的目录
        // ""代表当前包
        config.setClassForTemplateLoading(FreemakerUtils.class, "/templates");
        // 得到模板对象
        String configFormat = generators.getConfigFormat();
        Template template = null;
        
        if (Constants.PROMETHEUS.equals(configFormat)) {
            template = config.getTemplate("alert.yml");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("itemList", configs);
        data.put("serviceName", serviceName);
        // 3.产生输出
        processOut(generators, template, data, "prometheus");
    }
    
    public static void generatePromScrapeConfig(Generators generators, List<ServiceConfig> configs,
                                                String serviceName) throws IOException, TemplateException {
        // 创建核心配置对象
        Configuration config = new Configuration(Configuration.getVersion());
        // 设置加载的目录
        // ""代表当前包
        config.setClassForTemplateLoading(FreemakerUtils.class, "/templates");
        // 得到模板对象
        Template template = config.getTemplate("scrape.ftl");
        
        Map<String, Object> data = new HashMap<>();
        data.put("itemList", configs);
        // 3.产生输出
        processOut(generators, template, data, serviceName);
    }
    
    private static void processOut(Generators generators, Template template, Map<String, Object> data,
                                   String decompressPackageName) throws IOException, TemplateException {
        String packagePath = Constants.INSTALL_PATH + Constants.SLASH + decompressPackageName + Constants.SLASH;
        String outputDirectory = generators.getOutputDirectory();
        
        if (outputDirectory.contains(Constants.COMMA)) {
            for (String outPutDir : generators.getOutputDirectory().split(StrUtil.COMMA)) {
                String outputFile = packagePath + outPutDir + Constants.SLASH + generators.getFilename();
                writeToTemplate(template, data, outputFile);
            }
        } else if (outputDirectory.startsWith(Constants.SLASH)) {
            String outputFile = generators.getOutputDirectory() + Constants.SLASH + generators.getFilename();
            writeToTemplate(template, data, outputFile);
        } else {
            String outputFile =
                    packagePath + generators.getOutputDirectory() + Constants.SLASH + generators.getFilename();
            writeToTemplate(template, data, outputFile);
        }
    }
    
    private static void writeToTemplate(Template template, Map<String, Object> data,
                                        String outputFile) throws IOException, TemplateException {
        File file = new File(outputFile);
        if (!file.exists()) {
            FileUtil.mkParentDirs(file);
        }
        FileWriter out = new FileWriter(file);
        template.process(data, out);
        out.close();
    }
    
}
