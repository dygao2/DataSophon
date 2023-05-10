package com.datasophon.api.master;

import akka.actor.UntypedActor;
import com.datasophon.common.command.OlapOpsType;
import com.datasophon.common.command.OlapSqlExecCommand;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.OlapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class MasterNodeProcessingActor extends UntypedActor {

    private static final Logger logger = LoggerFactory.getLogger(MasterNodeProcessingActor.class);

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof OlapSqlExecCommand) {
            OlapSqlExecCommand command = (OlapSqlExecCommand) message;
            ExecResult execResult = OlapOpsType.ADD_BE.equals(command.getOpsType())
                    ? OlapUtils.addBackendBySqlClient(command.getFeMaster(), command.getHostName())
                    : OlapUtils.addFollowerBySqlClient(command.getFeMaster(), command.getHostName());
            String tip = OlapOpsType.ADD_BE.equals(command.getOpsType()) ? "backend" : "follower";
            if (execResult.getExecResult()) {
                logger.info(command.getHostName() + " " + tip + " be added success");
            } else {
                logger.info(command.getHostName() + " " + tip + " be added failed");
            }
            int tryTimes = 0;
            while (!execResult.getExecResult() && tryTimes < 3) {
                try {
                    TimeUnit.SECONDS.sleep(10L);
                    execResult = OlapOpsType.ADD_BE.equals(command.getOpsType())
                            ? OlapUtils.addBackendBySqlClient(command.getFeMaster(), command.getHostName())
                            : OlapUtils.addFollowerBySqlClient(command.getFeMaster(), command.getHostName());
                    if (execResult.getExecResult()) {
                        logger.info(command.getHostName() + " " + tip + " be added success");
                        break;
                    } else {
                        logger.info(command.getHostName() + " " + tip + " be added failed");
                    }
                    tryTimes++;
                } catch (InterruptedException e) {
                    logger.info("The SR operate be sleep operation failed");
                }
            }
        } else {
            unhandled(message);
        }
    }
}
