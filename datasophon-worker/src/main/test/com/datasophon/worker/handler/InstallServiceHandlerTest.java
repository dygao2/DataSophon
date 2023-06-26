package com.datasophon.worker.handler;

import org.junit.Before;
import org.junit.Test;

public class InstallServiceHandlerTest {

    private InstallServiceHandler installServiceHandlerUnderTest;

    @Before
    public void setUp() {
        installServiceHandlerUnderTest = new InstallServiceHandler("HDFS","NameNode");
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
