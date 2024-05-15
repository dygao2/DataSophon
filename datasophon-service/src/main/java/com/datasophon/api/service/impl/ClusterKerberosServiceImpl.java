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

package com.datasophon.api.service.impl;

import com.datasophon.api.load.GlobalVariables;
import com.datasophon.api.service.ClusterKerberosService;
import com.datasophon.api.service.ClusterServiceRoleInstanceService;
import com.datasophon.common.Constants;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.ShellUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.io.FileUtil;

@Service("clusterKerberosService")
@Transactional
public class ClusterKerberosServiceImpl implements ClusterKerberosService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClusterKerberosServiceImpl.class);
    
    private static final String SSHUSER = "SSHUSER";
    
    private static final String KEYTAB_PATH = "/etc/security/keytab";
    
    @Autowired
    private ClusterServiceRoleInstanceService roleInstanceService;
    
    @Override
    public void downloadKeytab(
                               Integer clusterId,
                               String principal,
                               String keytabName,
                               String hostname,
                               HttpServletResponse response) throws IOException {
        String keytabFilePath =
                KEYTAB_PATH + Constants.SLASH + hostname + Constants.SLASH + keytabName;
        File file = new File(keytabFilePath);
        if (!file.exists()) {
            generateKeytabFile(clusterId, keytabFilePath, principal);
        }
        FileInputStream inputStream = new FileInputStream(file);
        response.reset();
        response.setContentType("application/octet-stream");
        response.addHeader("Content-Length", "" + file.length());
        response.setHeader("Content-Disposition", "attachment;filename=" + keytabName);
        OutputStream out = response.getOutputStream();
        try {
            int length = 0;
            byte[] buffer = new byte[1024];
            while ((length = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        } finally {
            inputStream.close();
            out.flush();
            out.close();
        }
    }
    
    @Override
    public void uploadKeytab(MultipartFile file, String hostname, String keytabFileName) throws IOException {
        String keytabFilePath =
                KEYTAB_PATH + Constants.SLASH + hostname + Constants.SLASH + keytabFileName;
        file.transferTo(new File(keytabFilePath));
    }
    
    private void generateKeytabFile(
                                    Integer clusterId,
                                    String keytabFilePath,
                                    String principal) {
        Map<String, String> globalVariables = GlobalVariables.get(clusterId);
        String kadminPrincipal = globalVariables.get("${kadminPrincipal}");
        String kadminPassword = globalVariables.get("${kadminPassword}");
        String listPrinc = "kadmin -p" + kadminPrincipal + " -w" + kadminPassword + " -q \"listprincs\"";
        ExecResult execResult = ShellUtils.exceShell(listPrinc);
        String execOut = execResult.getExecOut();
        if (!execOut.contains(principal)) {
            String addprinc = "kadmin -p" + kadminPrincipal + " -w" + kadminPassword + " -q \"addprinc -randkey "
                    + principal + "\"";
            logger.info("add principal cmd is : {}", addprinc);
            ShellUtils.exceShell(addprinc);
        }
        if (!FileUtil.exist(keytabFilePath)) {
            FileUtil.mkParentDirs(keytabFilePath);
        }
        String keytabCmd =
                "kadmin -p" + kadminPrincipal + " -w" + kadminPassword + " -q \"xst -k " + keytabFilePath + " "
                        + principal + "\"";
        logger.info("generate keytab file cmd is : {}", keytabCmd);
        ShellUtils.exceShell(keytabCmd);
        
    }
}
