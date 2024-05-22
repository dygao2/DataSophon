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

package com.datasophon.api.migration;

import com.datasophon.common.utils.FileUtils;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.ibatis.jdbc.SQL;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;

@Slf4j
@Component
public class DatabaseMigration {
    
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    private static final String MIGRATION_HOME = "conf/db/migration";
    
    private static final String MIGRATION_TABLE_NAME = "migration_history";
    
    public static final String SPLIT = "__";
    
    private static final String TABLE_CREATE_SQL = "CREATE TABLE `migration_history`  (" +
            "  `version` varchar(128) NOT NULL," +
            "  `execute_user` varchar(128) NOT NULL," +
            "  `execute_date` timestamp NOT NULL," +
            "  `success` tinyint(1) NOT NULL," +
            "  PRIMARY KEY (`version`)" +
            ");";
    
    @Value("${spring.datasource.url}")
    private String url;
    
    @Value("${spring.datasource.username}")
    private String username;
    
    @Value("${spring.datasource.password}")
    private String password;
    
    private final JdbcTemplate jdbcTemplate;
    
    public DatabaseMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public void migration() {
        prepareMigrationTable();
        Set<Migration> migrations = getMigrations();
        if (CollectionUtils.isEmpty(migrations)) {
            printCurrentVersion();
            TreeSet<Migration> allMigrations = getAllMigrations();
            if (CollUtil.isEmpty(allMigrations)) {
                log.info("No migration required ");
            } else {
                log.info("No migration required , current database version is " + allMigrations.last().getVersion());
            }
            
            return;
        }
        RuntimeException exception = doMigrations((TreeSet<Migration>) migrations);
        printCurrentVersion();
        if (exception != null) {
            throw exception;
        }
    }
    
    private Set<Migration> getMigrations() {
        TreeSet<Migration> migrations = getAllMigrations();
        if (migrations.isEmpty()) {
            log.warn("No migration file was found!");
            return Collections.emptySet();
        }
        // query migration histories
        TreeSet<Migration> migrationHistories = new TreeSet<>(
                jdbcTemplate.query("select * from migration_history", new BeanPropertyRowMapper<>(Migration.class)));
        // filter the migrations that need to do
        Set<Migration> migrationsToDo;
        if (CollectionUtils.isEmpty(migrationHistories)) {
            return migrations;
        } else {
            Migration lastMigration = migrationHistories.last();
            migrationsToDo = migrations.tailSet(lastMigration, !lastMigration.isSuccess());
        }
        return migrationsToDo;
    }
    
    private RuntimeException doMigrations(TreeSet<Migration> migrations) {
        for (Migration migration : migrations) {
            log.info("start migration, version: " + migration.getVersion());
            migration.setExecuteUser(username);
            migration.setExecuteDate(new Date());
            migration.setSuccess(doMigration(migration));
            upsertMigration(migration);
            if (!migration.isSuccess()) {
                return new RuntimeException("Migration break at version  " + migration.getVersion());
            } else {
                log.info("Migration success! version: " + migration.getVersion());
            }
        }
        log.info("The migration is complete , The latest database version is " + migrations.last().getVersion());
        return null;
    }
    
    private boolean doMigration(Migration migration) {
        
        Resource ddlFile = migration.getUpgradeDDLFile(), dmlFile = migration.getUpgradeDMLFile();
        if (runScript(ddlFile, true) && runScript(dmlFile, true)) {
            return true;
        }
        log.error("Migration failure! version: " + migration.getVersion() + ". A rollback is about to be performed");
        Resource rollbackFile = migration.getRollbackFile();
        if (rollbackFile != null) {
            runScript(rollbackFile, false);
            log.info("The rollback script (" + rollbackFile.getFilename() + ") is successfully executed");
        } else {
            log.warn("The rollback script does not exist. Skip execution");
        }
        return false;
    }
    
    private void prepareMigrationTable() {
        List<String> tables = jdbcTemplate.queryForList("SHOW TABLES", String.class);
        if (!tables.contains(MIGRATION_TABLE_NAME)) {
            jdbcTemplate.execute(TABLE_CREATE_SQL);
        }
    }
    
    private synchronized TreeSet<Migration> getAllMigrations() {
        TreeSet<Migration> allMigrations = new TreeSet<>();
        // load migration files
        Resource[] resources;
        try {
            resources = new PathMatchingResourcePatternResolver()
                    .getResources(ResourceUtils.CLASSPATH_URL_PREFIX + "db/migration/**/*.sql");
        } catch (IOException e) {
            return allMigrations;
        }
        File home = new File(FileUtils.concatPath(System.getProperty("user.dir"), MIGRATION_HOME));
        if (home.exists() && home.isDirectory()) {
            List<File> sqlFiles =
                    FileUtil.loopFiles(home, pathname -> "sql".equals(FileUtil.getSuffix(pathname.getName())));
            if (sqlFiles != null) {
                resources = sqlFiles.stream().map(FileSystemResource::new).toArray(Resource[]::new);
            }
        }
        Map<String, List<Resource>> resourceMap = Arrays.stream(resources)
                .filter(Migration::isMigrationFile)
                .collect(Collectors.groupingBy(
                        r -> Objects.requireNonNull(r.getFilename()).substring(1, r.getFilename().indexOf(SPLIT))));
        
        for (Map.Entry<String, List<Resource>> entries : resourceMap.entrySet()) {
            Resource ddl = null, dml = null, rollback = null;
            for (Resource resource : entries.getValue()) {
                String fileName = FileUtil.mainName(Objects.requireNonNull(resource.getFilename()));
                if (fileName.startsWith(ScriptType.UPGRADE.getPrefix())) {
                    String[] extracts = fileName.split(SPLIT);
                    if ("DDL".equals(extracts[1])) {
                        ddl = resource;
                    } else if ("DML".equals(extracts[1])) {
                        dml = resource;
                    }
                } else if (fileName.startsWith(ScriptType.ROLLBACK.getPrefix())) {
                    rollback = resource;
                }
            }
            if (ddl != null || dml != null) {
                Migration migration = new Migration(entries.getKey(), ddl, dml, rollback);
                if (!allMigrations.add(migration)) {
                    throw new RuntimeException("Duplicate version " + migration.getVersion());
                }
            }
        }
        return allMigrations;
    }
    
    private void upsertMigration(Migration migration) {
        SQL query = new SQL();
        query.SELECT("*").FROM(MIGRATION_TABLE_NAME)
                .WHERE("`version` = '" + migration.getVersion() + "'");
        List<Migration> migrations = jdbcTemplate.query(query.toString(), new BeanPropertyRowMapper<>(Migration.class));
        SQL sql = new SQL();
        if (!CollectionUtils.isEmpty(migrations) && !migrations.get(0).isSuccess()) {
            sql.UPDATE(MIGRATION_TABLE_NAME)
                    .SET("success = " + String.format("'%s'", (migration.isSuccess() ? "1" : "0")),
                            "execute_date = "
                                    + String.format("'%s'", DateFormatUtils.format(new Date(), DEFAULT_DATE_FORMAT)))
                    .WHERE("`version` = " + String.format("'%s'", migration.getVersion()));
        } else {
            sql.INSERT_INTO(MIGRATION_TABLE_NAME)
                    .INTO_VALUES(String.format("'%s'", migration.getVersion()),
                            String.format("'%s'", migration.getExecuteUser()),
                            String.format("'%s'",
                                    DateFormatUtils.format(migration.getExecuteDate(), DEFAULT_DATE_FORMAT)),
                            String.format("'%s'", migration.isSuccess() ? "1" : "0"));
        }
        jdbcTemplate.execute(sql.toString());
    }
    
    private boolean runScript(Resource resource, boolean stopOnError) {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            ScriptRunner scriptRunner = new ScriptRunner(connection);
            scriptRunner.setAutoCommit(false);
            scriptRunner.setStopOnError(stopOnError);
            scriptRunner.setSendFullScript(false);
            LogWriter logWriter = new LogWriter(System.out);
            if (!stopOnError) {
                scriptRunner.setErrorLogWriter(logWriter);
            }
            scriptRunner.setLogWriter(logWriter);
            scriptRunner.runScript(new InputStreamReader(resource.getInputStream()));
            return true;
        } catch (Exception e) {
            log.error("Script execute failed! " + resource.getFilename(), e);
            return false;
        }
    }
    
    private void printCurrentVersion() {
        String currentVersion = null, lastVersion = null;
        TreeSet<Migration> migrations = queryMigrationHistory();
        if (!CollectionUtils.isEmpty(migrations)) {
            Migration last = migrations.last();
            while (last != null && !last.isSuccess()) {
                last = migrations.lower(last);
            }
            if (last != null) {
                currentVersion = last.getVersion();
            }
        }
        
        TreeSet<Migration> allMigrations = getAllMigrations();
        if (CollUtil.isEmpty(allMigrations)) {
            return;
        }
        lastVersion = allMigrations.last().getVersion();
        log.info("Last Script Version {}, Current Database Version {}", lastVersion, currentVersion);
    }
    
    private TreeSet<Migration> queryMigrationHistory() {
        return new TreeSet<>(
                jdbcTemplate.query("select * from migration_history", new BeanPropertyRowMapper<>(Migration.class)));
    }
    
    static class LogWriter extends PrintWriter {
        
        public LogWriter(OutputStream out) {
            super(out);
        }
        
        @Override
        public void write(String s) {
            log.info(s);
        }
    }
    
}
