package com.datasophon.api.utils;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecuteShellScriptUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(ExecuteShellScriptUtils.class);
    public static int executeShellScript(String shellScriptPath,
                                         String params) throws IOException, InterruptedException {
        logger.info("start executeShellScript:{}", shellScriptPath);
        ProcessBuilder pb = new ProcessBuilder("sh", shellScriptPath, params);
        // pb.inheritIO();
        Process p = pb.start();
        /*
         * BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream())); String line; while ((line = reader.readLine()) != null){ System.out.println(line); }
         */
        return p.waitFor();
    }
}
