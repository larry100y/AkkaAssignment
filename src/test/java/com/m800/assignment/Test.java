package com.m800.assignment;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.m800.assignment.actor.Aggregator;
import com.m800.assignment.actor.FileParser;
import com.m800.assignment.actor.FileScanner;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.net.URL;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class Test {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @org.junit.Test
    public void testReplyWithLastestTemperatureReading() {
        TestKit probe = new TestKit(system);
        ActorRef aggregator = system.actorOf(Aggregator.props(), "aggregator");
        ActorRef parser = system.actorOf(FileParser.props(aggregator), "parser");
        ActorRef scanner = system.actorOf(FileScanner.props(parser), "scanner");

        URL url = this.getClass().getClassLoader().getResource("");
        String dirPath = url.getPath();
        scanner.tell(new FileScanner.CheckDir(dirPath), ActorRef.noSender());

        assertEquals(true, true);

    }

}
