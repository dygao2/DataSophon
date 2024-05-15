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
import com.datasophon.common.utils.PropertyUtils;
import com.datasophon.common.utils.ShellUtils;
import com.datasophon.worker.handler.ServiceHandler;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

/**
 * TEZ Server 启动支持类
 *
 * @author zhenqin
 */
public class TezServerHandlerStrategy extends AbstractHandlerStrategy implements ServiceRoleStrategy {
    
    public TezServerHandlerStrategy(String serviceName, String serviceRoleName) {
        super(serviceName, serviceRoleName);
    }
    
    @Override
    public ExecResult handler(ServiceRoleOperateCommand command) {
        ServiceHandler serviceHandler = new ServiceHandler(command.getServiceName(), command.getServiceRoleName());
        String workPath = Constants.INSTALL_PATH + Constants.SLASH + command.getDecompressPackageName();
        if (command.getCommandType().equals(CommandType.INSTALL_SERVICE)) {
            final String hadoopHome = PropertyUtils.getString("HADOOP_HOME");
            final String tezLibPath = Optional.ofNullable(StringUtils.trimToNull(createEnvPath(workPath)))
                    .orElse("hdfs:///user/tez/tez.tar.gz");
            final String tezLibParentDir = new Path(URI.create(tezLibPath).getPath()).getParent().toString();
            logger.info("Start to execute hdfs dfs -mkdir {}", tezLibParentDir);
            ArrayList<String> commands = new ArrayList<>();
            commands.add("sudo");
            commands.add("-u");
            commands.add("hdfs");
            commands.add(hadoopHome + "/bin/hdfs");
            commands.add("dfs");
            commands.add("-mkdir");
            commands.add("-p");
            commands.add(tezLibParentDir);
            ExecResult execResult = ShellUtils.execWithStatus(workPath, commands, 90, logger);
            logger.info("mkdir {} output: {}", tezLibParentDir, execResult.getExecOut());
            
            // 改变文件组权限
            if (Objects.nonNull(command.getRunAs()) && StringUtils.isNotBlank(command.getRunAs().getUser())) {
                commands = new ArrayList<>();
                commands.add("sudo");
                commands.add("-u");
                commands.add("hdfs");
                commands.add(hadoopHome + "/bin/hdfs");
                commands.add("dfs");
                commands.add("-chown");
                commands.add(command.getRunAs().getUser() + ":" + command.getRunAs().getGroup());
                commands.add(tezLibParentDir);
                execResult = ShellUtils.execWithStatus(workPath, commands, 90, logger);
                logger.info("chown {} output: {}", tezLibParentDir, execResult.getExecOut());
            }
            
            logger.info("Start to execute hdfs dfs -put ./share/tez.tar.gz {}", tezLibParentDir);
            commands = new ArrayList<>();
            if (Objects.nonNull(command.getRunAs()) && StringUtils.isNotBlank(command.getRunAs().getUser())) {
                commands.add("sudo");
                commands.add("-u");
                commands.add(command.getRunAs().getUser());
            }
            commands.add(hadoopHome + "/bin/hdfs");
            commands.add("dfs");
            commands.add("-put");
            commands.add("./share/tez.tar.gz");
            commands.add(tezLibParentDir);
            execResult = ShellUtils.execWithStatus(workPath, commands, 90, logger);
            logger.info("upload tez.tar.gz to {} output: {}", tezLibParentDir, execResult.getExecOut());
        }
        ExecResult startResult = serviceHandler.start(command.getStartRunner(), command.getStatusRunner(),
                command.getDecompressPackageName(), command.getRunAs());
        return startResult;
    }
    
    /**
     * tez 的元数据
     *
     * @param workPath
     */
    String createEnvPath(final String workPath) {
        Configuration conf = new Configuration();
        try {
            final File tezSiteFile = new File(workPath, "conf/tez-site.xml");
            if (tezSiteFile.exists()) {
                conf.addResource(tezSiteFile.toURL());
                logger.info("add tez-site file: {}", tezSiteFile.getAbsolutePath());
            }
            
            // tez lib uri 启动清理
            String tezLibPath = conf.get("tez.lib.uris");
            return tezLibPath;
        } catch (Exception e) {
        }
        return null;
    }
}
