package com.m800.assignment.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.StringTokenizer;

public class Aggregator extends AbstractActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    private final ActorRef manager;
    private int count = 0;
    private final String filePath;

    public Aggregator(String filePath, ActorRef manager) {
        this.manager = manager;
        this.filePath = filePath;
    }

    public static Props props(String filePath, ActorRef manager){
        return Props.create(Aggregator.class, filePath, manager);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FileParser.StartOfFile.class, s -> {

                })
                .match(FileParser.Line.class, l -> {
                    countWord(l.text);
                })
                .match(FileParser.EndOfFile.class, this::onEndOfFile)
                .build();
    }

    private void onEndOfFile(FileParser.EndOfFile msg){
        logger.info("File Name: {}, Word Count: {}", filePath, count);
        manager.tell(new Manager.ReportResult(filePath, count), getSelf());
    }

    private void countWord(String text){
        StringTokenizer tokenizer = new StringTokenizer(text);
        while(tokenizer.hasMoreTokens()){
            count++;
            tokenizer.nextToken();
        }
    }
}
