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

package com.datasophon.api.configuration;

import com.datasophon.api.migration.DatabaseMigration;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatabaseMigrationAware implements ApplicationContextAware, Ordered {
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        String migrationEnable = applicationContext.getEnvironment().getProperty("datasophon.migration.enable");
        log.info("Database Migration enable is {}", migrationEnable);
        if (migrationEnable == null || "false".equals(migrationEnable)) {
            return;
        }
        DatabaseMigration databaseMigration = applicationContext.getBean(DatabaseMigration.class);
        try {
            databaseMigration.migration();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public int getOrder() {
        return 0;
    }
    
}
