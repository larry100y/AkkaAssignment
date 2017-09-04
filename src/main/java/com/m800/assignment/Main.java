package com.m800.assignment;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.m800.assignment.actor.Aggregator;
import com.m800.assignment.actor.FileParser;
import com.m800.assignment.actor.FileScanner;

public class Main {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("assignment");

        String dirPath = System.getProperty("user.dir");
        System.out.println("Current Directory: " + dirPath);

        try {
            ActorRef aggregator = system.actorOf(Aggregator.props(), "aggregator");
            ActorRef parser = system.actorOf(FileParser.props(aggregator), "parser");
            ActorRef scanner = system.actorOf(FileScanner.props(parser), "scanner");

            scanner.tell(new FileScanner.CheckDir(dirPath), ActorRef.noSender());

            while(!scanner.isTerminated()){

            }
        } finally {
            system.terminate();
        }
    }
}
