/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datasophon.worker.utils;

public class TaskConstants {
    
    private TaskConstants() {
        throw new IllegalStateException("Utility class");
    }
    
    public static final String YARN_APPLICATION_REGEX = "application_\\d+_\\d+";
    
    public static final String FLINK_APPLICATION_REGEX = "JobID \\w+";
    
    public static final String SETVALUE_REGEX = "[\\$#]\\{setValue\\(([^)]*)\\)}";
    
    /**
     * string false
     */
    public static final String STRING_FALSE = "false";
    
    /**
     * exit code kill
     */
    public static final int EXIT_CODE_KILL = 137;
    public static final String PID = "pid";
    
    /**
     * QUESTION ?
     */
    public static final String QUESTION = "?";
    
    /**
     * comma ,
     */
    public static final String COMMA = ",";
    
    /**
     * hyphen
     */
    public static final String HYPHEN = "-";
    
    /**
     * slash /
     */
    public static final String SLASH = "/";
    
    /**
     * COLON :
     */
    public static final String COLON = ":";
    
    /**
     * SPACE " "
     */
    public static final String SPACE = " ";
    
    /**
     * SINGLE_SLASH /
     */
    public static final String SINGLE_SLASH = "/";
    
    /**
     * pstree, get pud and sub pid
     */
    public static final String PSTREE = "pstree";
    
    public static final String RWXR_XR_X = "rwxr-xr-x";
    
    /**
     * task log info format
     */
    public static final String TASK_LOG_LOGGER_NAME = "TaskLogLogger";
    
    /**
     * task log logger name format
     */
    public static final String TASK_LOG_LOGGER_NAME_FORMAT = TASK_LOG_LOGGER_NAME + "-%s";
    
    /**
     * Task Logger's prefix
     */
    public static final String TASK_LOGGER_INFO_PREFIX = "TASK";
    
    /**
     * Task Logger Thread's name
     */
    public static final String TASK_APPID_LOG_FORMAT = "taskAppId";
    
    /**
     * get output log service
     */
    public static final String GET_OUTPUT_LOG_SERVICE = "-getOutputLogService";
    
}
