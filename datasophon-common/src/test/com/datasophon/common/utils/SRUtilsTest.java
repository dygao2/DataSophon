package com.datasophon.common.utils;

import com.datasophon.common.model.ProcInfo;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

public class SRUtilsTest {
    @Test
    public void testAddBackend() throws SQLException, ClassNotFoundException {
        OlapUtils.addBackend("ddp4","ddp5");
    }

    @Test
    public void testAddFrontend() throws SQLException, ClassNotFoundException {
        OlapUtils.addFollower("ddp4","ddp5");

    }
    @Test
    public void testShowfrontends() throws SQLException, ClassNotFoundException {
        List<ProcInfo> list = OlapUtils.showFrontends("ddp4");
        System.out.println(list.toString());
    }

    @Test
    public void testShowBackends() throws SQLException, ClassNotFoundException {
        List<ProcInfo> list = OlapUtils.showBackends("ddp4");
        System.out.println(list.toString());
    }
    @Test
    public void testQuery() throws SQLException, ClassNotFoundException {

        OlapUtils.executeQuerySql("ddp4","select * from information_schema.`columns`");

    }
}
