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

package com.datasophon.api.master.handler.host;

import com.datasophon.api.load.ConfigBean;
import com.datasophon.api.utils.CommonUtils;
import com.datasophon.api.utils.MessageResolverUtils;
import com.datasophon.api.utils.MinaUtils;
import com.datasophon.api.utils.SpringTool;
import com.datasophon.common.Constants;
import com.datasophon.common.enums.InstallState;
import com.datasophon.common.model.HostInfo;

import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.client.session.ClientSession;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartWorkerHandler implements DispatcherWorkerHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(StartWorkerHandler.class);
    
    private Integer clusterId;
    
    private String clusterFrame;
    
    public StartWorkerHandler(Integer clusterId, String clusterFrame) {
        this.clusterId = clusterId;
        this.clusterFrame = clusterFrame;
    }
    
    @Override
    public boolean handle(ClientSession session, HostInfo hostInfo) throws UnknownHostException {
        ConfigBean configBean = SpringTool.getApplicationContext().getBean(ConfigBean.class);
        String installPath = Constants.INSTALL_PATH;
        String localHostName = InetAddress.getLocalHost().getHostName();
        String updateCommonPropertiesResult = MinaUtils.execCmdWithResult(session,
                Constants.UPDATE_COMMON_CMD +
                        localHostName +
                        Constants.SPACE +
                        configBean.getServerPort() +
                        Constants.SPACE +
                        this.clusterFrame +
                        Constants.SPACE +
                        this.clusterId +
                        Constants.SPACE +
                        Constants.INSTALL_PATH);
        if (StringUtils.isBlank(updateCommonPropertiesResult) || "failed".equals(updateCommonPropertiesResult)) {
            logger.error("common.properties update failed");
            hostInfo.setErrMsg("common.properties update failed");
            hostInfo.setMessage(MessageResolverUtils.getMessage("modify.configuration.file.fail"));
            CommonUtils.updateInstallState(InstallState.FAILED, hostInfo);
        } else {
            // Initialize environment
            MinaUtils.execCmdWithResult(session, "ulimit -n 102400");
            MinaUtils.execCmdWithResult(session, "sysctl -w vm.max_map_count=2000000");
            // Set startup and self start
            MinaUtils.execCmdWithResult(session,
                    "\\cp " + installPath + "/datasophon-worker/script/datasophon-worker /etc/rc.d/init.d/");
            MinaUtils.execCmdWithResult(session, "chmod +x /etc/rc.d/init.d/datasophon-worker");
            MinaUtils.execCmdWithResult(session, "chkconfig --add datasophon-worker");
            MinaUtils.execCmdWithResult(session,
                    "\\cp " + installPath + "/datasophon-worker/script/datasophon-env.sh /etc/profile.d/");
            MinaUtils.execCmdWithResult(session, "source /etc/profile.d/datasophon-env.sh");
            hostInfo.setMessage(MessageResolverUtils.getMessage("start.host.management.agent"));
            MinaUtils.execCmdWithResult(session, "service datasophon-worker restart");
            hostInfo.setProgress(75);
            hostInfo.setCreateTime(new Date());
        }
        
        logger.info("end dispatcher host agent :{}", hostInfo.getHostname());
        return true;
    }
}
