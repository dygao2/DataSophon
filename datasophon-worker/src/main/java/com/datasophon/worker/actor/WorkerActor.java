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

package com.datasophon.worker.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import com.alibaba.fastjson.JSONObject;
import com.datasophon.common.model.StartWorkerMessage;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.io.IOException;

public class WorkerActor extends UntypedActor {

    private static final Logger logger = LoggerFactory.getLogger(WorkerActor.class);

    @Override
    public void preRestart(Throwable reason, Option<Object> message) {
        logger.info("worker actor restart by reason {}", reason.getMessage());
    }

    @Override
    public void preStart() throws IOException {
        ActorRef installServiceActor = getContext().actorOf(Props.create(InstallServiceActor.class),
                getActorRefName(InstallServiceActor.class));
        ActorRef configureServiceActor = getContext().actorOf(Props.create(ConfigureServiceActor.class),
                getActorRefName(ConfigureServiceActor.class));
        ActorRef startServiceActor =
                getContext().actorOf(Props.create(StartServiceActor.class), getActorRefName(StartServiceActor.class));
        ActorRef stopServiceActor =
                getContext().actorOf(Props.create(StopServiceActor.class), getActorRefName(StopServiceActor.class));
        ActorRef restartServiceActor = getContext().actorOf(Props.create(RestartServiceActor.class),
                getActorRefName(RestartServiceActor.class));
        ActorRef logActor = getContext().actorOf(Props.create(LogActor.class), getActorRefName(LogActor.class));
        ActorRef executeCmdActor =
                getContext().actorOf(Props.create(ExecuteCmdActor.class), getActorRefName(ExecuteCmdActor.class));
        ActorRef fileOperateActor =
                getContext().actorOf(Props.create(FileOperateActor.class), getActorRefName(FileOperateActor.class));
        ActorRef alertConfigActor =
                getContext().actorOf(Props.create(AlertConfigActor.class), getActorRefName(AlertConfigActor.class));
        ActorRef unixUserActor =
                getContext().actorOf(Props.create(UnixUserActor.class), getActorRefName(UnixUserActor.class));
        ActorRef unixGroupActor =
                getContext().actorOf(Props.create(UnixGroupActor.class), getActorRefName(UnixGroupActor.class));
        ActorRef kerberosActor =
                getContext().actorOf(Props.create(KerberosActor.class), getActorRefName(KerberosActor.class));
        ActorRef nMStateActor =
                getContext().actorOf(Props.create(NMStateActor.class), getActorRefName(NMStateActor.class));
        ActorRef rMStateActor =
                getContext().actorOf(Props.create(RMStateActor.class), getActorRefName(RMStateActor.class));
        getContext().watch(installServiceActor);
        getContext().watch(configureServiceActor);
        getContext().watch(startServiceActor);
        getContext().watch(stopServiceActor);
        getContext().watch(restartServiceActor);
        getContext().watch(logActor);
        getContext().watch(executeCmdActor);
        getContext().watch(fileOperateActor);
        getContext().watch(alertConfigActor);
        getContext().watch(unixUserActor);
        getContext().watch(unixGroupActor);
        getContext().watch(kerberosActor);
        getContext().watch(rMStateActor);
        getContext().watch(nMStateActor);
    }

    /** Get ActorRef name from Class name. */
    private String getActorRefName(Class clazz) {
        return StringUtils.uncapitalize(clazz.getSimpleName());
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof String) {

        } else if (message instanceof Terminated) {
            Terminated t = (Terminated) message;
            logger.info("find actor {} terminated", t.getActor());
        } else {
            unhandled(message);
        }
    }

    public static void main(String[] args) {
        String str =
                "{coreNum: 8, totalMem: 31.4189, totalDisk: 991.51,usedDisk: 9.59, diskAvail: 981.92,usedMem:5.91602,memUsedPersent:18.8295,diskUsedPersent:1.0,averageLoad:0.06}";
        StartWorkerMessage message = JSONObject.parseObject(str, StartWorkerMessage.class);
    }

}
