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
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.ShellUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnixUtils {
    
    private static final Long TIME_OUT = 60L;
    
    private static final Logger logger = LoggerFactory.getLogger(UnixUtils.class);
    
    public static ExecResult createUnixUser(String username, String mainGroup, String otherGroups) {
        ArrayList<String> commands = new ArrayList<>();
        if (isUserExists(username)) {
            commands.add("usermod");
        } else {
            commands.add("useradd");
        }
        commands.add(username);
        if (StringUtils.isNotBlank(mainGroup)) {
            commands.add("-g");
            commands.add(mainGroup);
        }
        if (StringUtils.isNotBlank(otherGroups)) {
            commands.add("-G");
            commands.add(otherGroups);
        }
        return ShellUtils.execWithStatus(Constants.INSTALL_PATH, commands, TIME_OUT);
    }
    
    public static ExecResult delUnixUser(String username) {
        ArrayList<String> commands = new ArrayList<>();
        commands.add("userdel");
        commands.add("-r");
        commands.add(username);
        return ShellUtils.execWithStatus(Constants.INSTALL_PATH, commands, TIME_OUT);
    }
    
    public static boolean isUserExists(String username) {
        ArrayList<String> commands = new ArrayList<>();
        commands.add("id");
        commands.add(username);
        ExecResult execResult = ShellUtils.execWithStatus(Constants.INSTALL_PATH, commands, TIME_OUT);
        return execResult.getExecResult();
    }
    
    public static ExecResult createUnixGroup(String groupName) {
        if (isGroupExists(groupName)) {
            ExecResult execResult = new ExecResult();
            execResult.setExecResult(true);
            return execResult;
        }
        ArrayList<String> commands = new ArrayList<>();
        commands.add("groupadd");
        commands.add(groupName);
        return ShellUtils.execWithStatus(Constants.INSTALL_PATH, commands, TIME_OUT);
    }
    
    public static ExecResult delUnixGroup(String groupName) {
        ArrayList<String> commands = new ArrayList<>();
        commands.add("groupdel");
        commands.add(groupName);
        return ShellUtils.execWithStatus(Constants.INSTALL_PATH, commands, TIME_OUT);
    }
    
    public static boolean isGroupExists(String groupName) {
        ExecResult execResult = ShellUtils.exceShell("egrep \"" + groupName + "\" /etc/group >& /dev/null");
        return execResult.getExecResult();
    }
    
}
