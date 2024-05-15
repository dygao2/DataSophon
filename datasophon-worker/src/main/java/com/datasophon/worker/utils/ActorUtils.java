package com.datasophon.worker.utils;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.util.Timeout;

public class ActorUtils {
    
    private static ActorSystem actorSystem;
    
    public static void setActorSystem(ActorSystem actorSystem) {
        ActorUtils.actorSystem = actorSystem;
    }
    
    public static ActorRef getRemoteActor(String hostname, String actorName) {
        String actorPath = "akka.tcp://datasophon@" + hostname + ":2551/user/" + actorName;
        ActorSelection actorSelection = actorSystem.actorSelection(actorPath);
        Timeout timeout = new Timeout(Duration.create(30, TimeUnit.SECONDS));
        Future<ActorRef> future = actorSelection.resolveOne(timeout);
        ActorRef actorRef = null;
        try {
            actorRef = Await.result(future, Duration.create(30, TimeUnit.SECONDS));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return actorRef;
    }
}
