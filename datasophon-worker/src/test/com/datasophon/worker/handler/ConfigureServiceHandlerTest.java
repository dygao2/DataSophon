package com.datasophon.worker.handler;

import com.datasophon.common.model.Generators;
import com.datasophon.common.model.ServiceConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigureServiceHandlerTest {

    private ConfigureServiceHandler configureServiceHandlerUnderTest;

    @BeforeAll
    public void setUp() {
        configureServiceHandlerUnderTest = new ConfigureServiceHandler("HDFS", "NameNode");
    }

    @Test
    public void testConfigure() {
        // Setup
        final Map<Generators, List<ServiceConfig>> configFileMap = new HashMap<>();

        // Run the test
//        final ExecResult result = configureServiceHandlerUnderTest.configure(configFileMap, "decompressPackageName", 0,
//                "serviceRoleName");

        // Verify the results
    }
}
