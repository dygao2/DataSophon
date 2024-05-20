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
import com.datasophon.common.cache.CacheUtils;
import com.datasophon.common.utils.PropertyUtils;
import com.datasophon.common.utils.ShellUtils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.lang.Console;
import cn.hutool.http.HttpUtil;

public class KerberosUtils {
    
    public static void downloadKeytabFromMaster(String principal, String keytabName) {
        String masterHost = PropertyUtils.getString(Constants.MASTER_HOST);
        String masterPort = PropertyUtils.getString(Constants.MASTER_WEB_PORT);
        Integer clusterId = PropertyUtils.getInt("clusterId");
        String hostname = CacheUtils.getString("hostname");
        
        // get kerberos keytab
        String downloadUrl =
                "http://" + masterHost + ":" + masterPort + "/ddh/cluster/kerberos/downloadKeytab?clusterId="
                        + clusterId + "&principal=" + principal + "&keytabName=" + keytabName + "&hostname=" + hostname;
        
        String dest = "/etc/security/keytab/";
        HttpUtil.downloadFile(downloadUrl, FileUtil.file(dest), new StreamProgress() {
            
            @Override
            public void start() {
                Console.log("start to install。。。。");
            }
            
            @Override
            public void progress(long progressSize, long l1) {
                Console.log("installed：{}", FileUtil.readableFileSize(progressSize));
            }
            
            @Override
            public void finish() {
                Console.log("install success！");
            }
        });
    }
    
    public static void createKeytabDir() {
        if (!FileUtil.exist("/etc/security/keytab/")) {
            FileUtil.mkdir("/etc/security/keytab/");
        }
        ShellUtils.exceShell("chown -R root:hadoop /etc/security/keytab/");
        ShellUtils.exceShell("chmod -R 770 /etc/security/keytab/");
    }
}
