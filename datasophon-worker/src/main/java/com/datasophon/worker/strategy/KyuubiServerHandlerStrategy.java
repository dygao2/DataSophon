package com.datasophon.worker.strategy;

import com.datasophon.common.Constants;
import com.datasophon.common.cache.CacheUtils;
import com.datasophon.common.command.ServiceRoleOperateCommand;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.worker.handler.ServiceHandler;
import com.datasophon.worker.utils.KerberosUtils;

import java.sql.SQLException;

import cn.hutool.core.io.FileUtil;

/**
 * @author thomasgx
 * @date 2023年10月20日  19:02
 */
public class KyuubiServerHandlerStrategy extends AbstractHandlerStrategy
        implements
            ServiceRoleStrategy {

    private static final String KEYTAB_NAME = "kyuubi.service.keytab";
    private static final String KEYTAB_PATH = "/etc/security/keytab/" + KEYTAB_NAME;

    public KyuubiServerHandlerStrategy(String serviceName, String serviceRoleName) {
        super(serviceName, serviceRoleName);
    }

    @Override
    public ExecResult handler(ServiceRoleOperateCommand command) throws SQLException, ClassNotFoundException {
        ExecResult startResult;
        if (command.getEnableKerberos()) {
            logger.info("start to get kyuubi keytab file");
            String hostname = CacheUtils.getString(Constants.HOSTNAME);
            KerberosUtils.createKeytabDir();
            if (!FileUtil.exist(KEYTAB_PATH)) {
                KerberosUtils.downloadKeytabFromMaster("kyuubi/" + hostname, KEYTAB_NAME);
            }
        }

        ServiceHandler serviceHandler = new ServiceHandler(command.getServiceName(),
                command.getServiceRoleName());
        startResult = serviceHandler.start(command.getStartRunner(), command.getStatusRunner(),
                command.getDecompressPackageName(), command.getRunAs());
        return startResult;
    }
}
