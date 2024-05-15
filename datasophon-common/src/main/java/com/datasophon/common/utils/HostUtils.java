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

package com.datasophon.common.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.net.InetAddresses;

/**
 * 读取hosts文件
 *
 * @author gaodayu
 */
public enum HostUtils {
    ;
    
    public static final Pattern HOST_NAME_STR = Pattern.compile("[0-9a-zA-Z-.]{1,64}");
    
    public static boolean checkIP(String ipStr) {
        return InetAddresses.isInetAddress(ipStr);
    }
    
    private static void checkIPThrow(String ipStr, Map<String, String> ipHost) {
        if (!checkIP(ipStr)) {
            throw new RuntimeException("Invalid IP in file /etc/hosts, IP：" + ipStr);
        }
        if (ipHost.containsKey(ipStr)) {
            throw new RuntimeException("Duplicate ip in file /etc/hosts, IP：" + ipStr);
        }
    }
    
    public static boolean checkHostname(String hostname) {
        if (!HOST_NAME_STR.matcher(hostname).matches()) {
            return false;
        }
        return !hostname.startsWith("-") && !hostname.endsWith("-");
    }
    
    private static void validHostname(String hostname) {
        if (!checkHostname(hostname)) {
            throw new RuntimeException("Invalid hostname in file /etc/hosts, hostname：" + hostname);
        }
    }
    
    public static String findIp(String hostname) {
        validHostname(hostname);
        String ip = getIp(hostname);
        return ip;
    }
    
    public static String getHostName(String hostOrIp) {
        try {
            InetAddress byName = InetAddress.getByName(hostOrIp);
            return byName.getCanonicalHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static String getIp(String hostName) {
        try {
            InetAddress byName = InetAddress.getByName(hostName);
            return byName.getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static String getLocalIp() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            return ip.getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static String getLocalHostName() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            return ip.getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    
}
