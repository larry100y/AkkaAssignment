package com.m800.assignment;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.m800.assignment.actor.Manager;
import org.apache.commons.io.FilenameUtils;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        ActorSystem system = ActorSystem.create("assignment");

        String dirPath = System.getProperty("user.dir");
        System.out.println("Current Directory: " + dirPath);

        String resultFilePath = FilenameUtils.concat(dirPath, "parse-result");
        System.out.println("Result File Path: " + resultFilePath);

        try {
            ActorRef manager = system.actorOf(Manager.props(resultFilePath), "manager");
            manager.tell(new Manager.LaunchTask(dirPath), ActorRef.noSender());

            while(true){
                Thread.sleep(1000);
                if(manager.isTerminated()){ //Either timeout or parsing finished will terminate manager actor.
                    break;
                }
            }
        } finally {
            system.terminate();
        }
    }
}
