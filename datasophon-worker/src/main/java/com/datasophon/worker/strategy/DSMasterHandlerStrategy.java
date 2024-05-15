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

package com.datasophon.worker.strategy;

import com.datasophon.common.Constants;
import com.datasophon.common.command.ServiceRoleOperateCommand;
import com.datasophon.common.enums.CommandType;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.ShellUtils;
import com.datasophon.worker.handler.ServiceHandler;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.db.DbUtil;
import cn.hutool.db.ds.simple.SimpleDataSource;
import cn.hutool.db.handler.RsHandler;
import cn.hutool.db.sql.SqlExecutor;

public class DSMasterHandlerStrategy extends AbstractHandlerStrategy implements ServiceRoleStrategy {
    
    public DSMasterHandlerStrategy(String serviceName, String serviceRoleName) {
        super(serviceName, serviceRoleName);
    }
    
    @Override
    public ExecResult handler(ServiceRoleOperateCommand command) {
        ServiceHandler serviceHandler = new ServiceHandler(command.getServiceName(), command.getServiceRoleName());
        String workPath = Constants.INSTALL_PATH + Constants.SLASH + command.getDecompressPackageName();
        if (command.getCommandType().equals(CommandType.INSTALL_SERVICE)) {
            // 判断数据库是否已经初始化
            boolean ready = true;
            logger.info("check if DolphinScheduler database is ready ");
            // 读取ds元数据库地址和账密
            File envFile = new File(workPath + "/bin/env/dolphinscheduler_env.sh");
            if (envFile.exists()) {
                List<String> lines = FileReader.create(envFile).readLines();
                Optional<String> optionalUrl =
                        lines.stream().filter(line -> line.contains("SPRING_DATASOURCE_URL")).findFirst();
                Optional<String> optionaUsername =
                        lines.stream().filter(line -> line.contains("SPRING_DATASOURCE_USERNAME")).findFirst();
                Optional<String> optionalPassword =
                        lines.stream().filter(line -> line.contains("SPRING_DATASOURCE_PASSWORD")).findFirst();
                
                if (optionalUrl.isPresent() && optionaUsername.isPresent() && optionalPassword.isPresent()) {
                    Connection con = null;
                    try {
                        con = DbUtil.use(new SimpleDataSource(getValue(optionalUrl.get()),
                                getValue(optionaUsername.get()), getValue(optionalPassword.get()))).getConnection();
                        List<String> entityList = SqlExecutor.query(con, "SHOW TABLES", new RsHandler<List<String>>() {
                            
                            @Override
                            public List<String> handle(ResultSet rs) throws SQLException {
                                final List<String> result = new ArrayList<>();
                                while (rs.next()) {
                                    result.add(rs.getString(1));
                                }
                                return result;
                            }
                        });
                        if (entityList.stream().noneMatch(s -> s.equals("t_escheduler_version")
                                || s.equals("t_ds_version") || s.equals("t_escheduler_queue"))) {
                            ready = false;
                        }
                    } catch (SQLException sqle) {
                        logger.error(sqle.getMessage(), sqle);
                    } finally {
                        DbUtil.close(con);
                    }
                }
            }
            
            if (!ready) {
                ArrayList<String> commands = new ArrayList<>();
                commands.add("bash");
                commands.add("tools/bin/upgrade-schema.sh");
                ExecResult execResult = ShellUtils.execWithStatus(workPath, commands, 180L, logger);
                if (execResult.getExecResult()) {
                    logger.info("DolphinScheduler database init success");
                } else {
                    logger.error("DolphinScheduler database init failed");
                    return execResult;
                }
            }
        }
        
        return serviceHandler.start(command.getStartRunner(), command.getStatusRunner(),
                command.getDecompressPackageName(), command.getRunAs());
    }
    
    private String getValue(String line) {
        String value = line.substring(line.indexOf("=") + 1);
        if (value.startsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
    
}
