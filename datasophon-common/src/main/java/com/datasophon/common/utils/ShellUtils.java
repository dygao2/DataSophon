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

import com.datasophon.common.Constants;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShellUtils {
    
    private static ProcessBuilder processBuilder = new ProcessBuilder();
    
    private static final Logger logger = LoggerFactory.getLogger(ShellUtils.class);
    
    public static Process exec(List<String> command) {
        Process process = null;
        try {
            String[] commands = new String[command.size()];
            command.toArray(commands);
            processBuilder.command(commands);
            process = processBuilder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return process;
    }
    
    /**
     * @param pathOrCommand 脚本路径或者命令
     * @return
     */
    public static ExecResult exceShell(String pathOrCommand) {
        ExecResult result = new ExecResult();
        StringBuffer stringBuffer = new StringBuffer();
        try {
            // 执行脚本
            Process ps = Runtime.getRuntime().exec(new String[]{"sh", "-c", pathOrCommand});
            // 只能接收脚本echo打印的数据，并且是echo打印的最后一次数据
            BufferedInputStream in = new BufferedInputStream(ps.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                stringBuffer.append(line);
                stringBuffer.append(System.lineSeparator());
            }
            in.close();
            br.close();
            String execOut = stringBuffer.toString();
            int exitValue = ps.waitFor();
            if (0 == exitValue) {
                logger.info("{} command exec out is : {} {}", pathOrCommand, System.lineSeparator(), execOut);
                result.setExecResult(true);
                result.setExecOut(execOut);
            } else {
                result.setExecOut("call shell failed. error code is :" + exitValue);
                logger.error("{} command exec out is : {} {}", pathOrCommand, System.lineSeparator(), execOut);
            }
            
        } catch (Exception e) {
            result.setExecOut(e.getMessage());
            logger.error(e.getMessage(), e);
        }
        return result;
    }
    
    // 获取cpu架构 arm或x86
    public static String getCpuArchitecture() {
        try {
            Process ps = Runtime.getRuntime().exec("arch");
            StringBuffer stringBuffer = new StringBuffer();
            int exitValue = ps.waitFor();
            if (0 == exitValue) {
                // 只能接收脚本echo打印的数据，并且是echo打印的最后一次数据
                BufferedInputStream in = new BufferedInputStream(ps.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = br.readLine()) != null) {
                    logger.info("脚本返回的数据如下： " + line);
                    stringBuffer.append(line);
                }
                in.close();
                br.close();
                return stringBuffer.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static ExecResult execWithStatus(String workPath, List<String> command, long timeout) {
        Process process = null;
        ExecResult result = new ExecResult();
        try {
            processBuilder.directory(new File(workPath));
            processBuilder.command(command);
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            getOutput(process);
            boolean execResult = process.waitFor(timeout, TimeUnit.SECONDS);
            if (execResult && process.exitValue() == 0) {
                logger.info("script execute success");
                result.setExecResult(true);
                result.setExecOut("script execute success");
            } else {
                result.setExecOut("script execute failed");
            }
            return result;
        } catch (Exception e) {
            result.setExecErrOut(e.getMessage());
            logger.error(e.getMessage(), e);
        }
        return result;
    }
    
    public static ExecResult execWithStatus(String workPath, List<String> command, long timeout, Logger logger) {
        Process process = null;
        ExecResult result = new ExecResult();
        try {
            processBuilder.directory(new File(workPath));
            processBuilder.command(command);
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            getOutput(process, logger);
            boolean execResult = process.waitFor(timeout, TimeUnit.SECONDS);
            if (execResult && process.exitValue() == 0) {
                logger.info("script execute success");
                result.setExecResult(true);
                result.setExecOut("script execute success");
            } else {
                result.setExecOut("script execute failed");
            }
            return result;
        } catch (Exception e) {
            result.setExecErrOut(e.getMessage());
            logger.error(e.getMessage(), e);
        }
        return result;
    }
    
    public static void getOutput(Process process, Logger logger) {
        
        ExecutorService getOutputLogService = Executors.newSingleThreadExecutor();
        
        getOutputLogService.submit(() -> {
            BufferedReader inReader = null;
            try {
                inReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                StringBuffer stringBuffer = new StringBuffer();
                while ((line = inReader.readLine()) != null) {
                    stringBuffer.append(line);
                    stringBuffer.append(System.lineSeparator());
                }
                logger.info(stringBuffer.toString());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                closeQuietly(inReader);
            }
            BufferedReader errorReader = null;
            try {
                errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                StringBuffer stringBuffer = new StringBuffer();
                while ((line = errorReader.readLine()) != null) {
                    stringBuffer.append(line);
                    stringBuffer.append(System.lineSeparator());
                }
                logger.error(stringBuffer.toString());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                closeQuietly(errorReader);
            }
        });
        getOutputLogService.shutdown();
    }
    
    public static void getOutput(Process process) {
        
        ExecutorService getOutputLogService = Executors.newSingleThreadExecutor();
        
        getOutputLogService.submit(() -> {
            BufferedReader inReader = null;
            try {
                inReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                StringBuffer stringBuffer = new StringBuffer();
                while ((line = inReader.readLine()) != null) {
                    stringBuffer.append(line);
                    stringBuffer.append(System.lineSeparator());
                }
                logger.trace(stringBuffer.toString());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                closeQuietly(inReader);
            }
        });
        getOutputLogService.shutdown();
    }
    
    public static String getError(Process process) {
        String errput = null;
        BufferedReader reader = null;
        try {
            if (process != null) {
                StringBuffer stringBuffer = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while (reader.read() != -1) {
                    stringBuffer.append("\n" + reader.readLine());
                }
                errput = stringBuffer.toString();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        closeQuietly(reader);
        return errput;
    }
    
    public static void closeQuietly(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
        }
    }
    
    public static void destroy(Process process) {
        if (process != null) {
            process.destroyForcibly();
        }
    }
    
    public static void addChmod(String path, String chmod) {
        ArrayList<String> command = new ArrayList<>();
        command.add("chmod");
        command.add("-R");
        command.add(chmod);
        command.add(path);
        execWithStatus(Constants.INSTALL_PATH, command, 60, logger);
    }
    
    public static void addChown(String path, String user, String group) {
        ArrayList<String> command = new ArrayList<>();
        command.add("chown");
        command.add("-R");
        command.add(user + ":" + group);
        command.add(path);
        execWithStatus(Constants.INSTALL_PATH, command, 60, logger);
    }
}
