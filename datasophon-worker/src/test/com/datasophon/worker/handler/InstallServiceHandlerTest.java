package com.datasophon.worker.handler;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class InstallServiceHandlerTest {

    private InstallServiceHandler installServiceHandlerUnderTest;

    @BeforeAll
    public void setUp() {
        installServiceHandlerUnderTest = new InstallServiceHandler("DDP-1.2.2", "HDFS", "NameNode");
    }

    @Test
    public void testInstall() {
        // Setup
        // Run the test
//        final ExecResult result = installServiceHandlerUnderTest.install("packageName", "decompressPackageName",
//                "packageMd5", "runAs");

        // Verify the results
    }
}
