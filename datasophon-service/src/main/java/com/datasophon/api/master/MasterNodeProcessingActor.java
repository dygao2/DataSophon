package com.datasophon.api.master;

import com.datasophon.common.command.OlapSqlExecCommand;
import com.datasophon.common.utils.ExecResult;
import com.datasophon.common.utils.OlapUtils;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;
import cn.hutool.json.JSONUtil;

public class MasterNodeProcessingActor extends UntypedActor {
    
    private static final Logger logger = LoggerFactory.getLogger(MasterNodeProcessingActor.class);
    
    @Override
    public void onReceive(Object message) throws Throwable {
        logger.info("MasterNodeProcessingActor receive message: " + JSONUtil.toJsonStr(message));
        if (message instanceof OlapSqlExecCommand) {
            OlapSqlExecCommand command = (OlapSqlExecCommand) message;
            ExecResult execResult = new ExecResult();
            String tip = command.getOpsType().getDesc();
            switch (command.getOpsType()) {
                case ADD_BE:
                    execResult = OlapUtils.addBackend(command.getFeMaster(), command.getHostName());
                    break;
                case ADD_FE_FOLLOWER:
                    execResult = OlapUtils.addFollower(command.getFeMaster(), command.getHostName());
                    break;
                case ADD_FE_OBSERVER:
                    execResult = OlapUtils.addObserver(command.getFeMaster(), command.getHostName());
                    break;
            }
            if (execResult.getExecResult()) {
                logger.info(command.getHostName() + " " + tip + " added success");
            } else {
                logger.info(command.getHostName() + " " + tip + " added failed");
            }
            int tryTimes = 0;
            while (!execResult.getExecResult() && tryTimes < 3) {
                try {
                    TimeUnit.SECONDS.sleep(10L);
                    switch (command.getOpsType()) {
                        case ADD_BE:
                            execResult = OlapUtils.addBackendBySqlClient(command.getFeMaster(), command.getHostName());
                            break;
                        case ADD_FE_FOLLOWER:
                            execResult = OlapUtils.addFollowerBySqlClient(command.getFeMaster(), command.getHostName());
                            break;
                        case ADD_FE_OBSERVER:
                            execResult = OlapUtils.addObserverBySqlClient(command.getFeMaster(), command.getHostName());
                            break;
                    }
                    if (execResult.getExecResult()) {
                        logger.info(command.getHostName() + " " + tip + " added success");
                        break;
                    } else {
                        logger.info(command.getHostName() + " " + tip + " added failed");
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
