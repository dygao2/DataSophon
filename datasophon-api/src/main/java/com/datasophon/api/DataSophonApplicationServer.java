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

package com.datasophon.api;

import com.datasophon.api.master.ActorUtils;
import com.datasophon.common.Constants;
import com.datasophon.common.cache.CacheUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

import javax.annotation.PostConstruct;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

import cn.hutool.extra.spring.EnableSpringUtil;

@SpringBootApplication
@ServletComponentScan
@ComponentScan("com.datasophon")
@MapperScan("com.datasophon.dao")
@EnableSpringUtil
public class DataSophonApplicationServer extends SpringBootServletInitializer {
    
    public static void main(String[] args) {
        SpringApplication.run(DataSophonApplicationServer.class, args);
        // add shutdown hook， close and shutdown resources
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdown();
        }));
    }
    
    @PostConstruct
    public void run() throws UnknownHostException, NoSuchAlgorithmException {
        String hostName = InetAddress.getLocalHost().getHostName();
        CacheUtils.put(Constants.HOSTNAME, hostName);
        ActorUtils.init();
    }
    
    /**
     * Master 关闭时调用
     */
    public static void shutdown() {
        ActorUtils.shutdown();
    }
}
