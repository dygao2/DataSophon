package com.datasophon.worker.strategy;

import cn.hutool.core.io.FileUtil;
import com.datasophon.common.Constants;
import com.datasophon.common.cache.CacheUtils;
import com.datasophon.common.command.ServiceRoleOperateCommand;
import com.datasophon.common.enums.CommandType;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.ShellUtils;
import com.datasophon.worker.handler.ServiceHandler;
import com.datasophon.worker.utils.KerberosUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;

public class JournalNodeHandlerStrategy implements ServiceRoleStrategy {

    private static final Logger logger = LoggerFactory.getLogger(JournalNodeHandlerStrategy.class);


    @Override
    public ExecResult handler(ServiceRoleOperateCommand command) throws SQLException, ClassNotFoundException {
        ExecResult startResult = new ExecResult();
        ServiceHandler serviceHandler = new ServiceHandler();
        if(command.getEnableKerberos()){
            logger.info("start to get journalnode keytab file");
            String hostname = CacheUtils.getString(Constants.HOSTNAME);
            KerberosUtils.createKeytabDir();
            if(!FileUtil.exist("/etc/security/keytab/jn.service.keytab")){
                KerberosUtils.downloadKeytabFromMaster("jn/" + hostname, "jn.service.keytab");
            }
            startResult = serviceHandler.start(command.getStartRunner(), command.getStatusRunner(), command.getDecompressPackageName(), command.getRunAs());
        }else {
            startResult = serviceHandler.start(command.getStartRunner(), command.getStatusRunner(), command.getDecompressPackageName(), command.getRunAs());
        }
        return startResult;
    }
}
