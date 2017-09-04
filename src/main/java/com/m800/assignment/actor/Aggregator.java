package com.m800.assignment.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.StringTokenizer;

public class Aggregator extends AbstractActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    private int count = 0;

    public static Props props(){
        return Props.create(Aggregator.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FileParser.StartOfFile.class, s -> {
                    System.out.println("********************");
                    System.out.println("File Name: " + s.filename);
                })
                .match(FileParser.Line.class, l -> {
                    countWord(l.text);
                })
                .matchEquals(FileParser.EOF, e -> {
                    System.out.println("Word Count: " + count);
                    System.out.println("********************");
                    this.count = 0;
                })
                .build();
    }

    private void countWord(String text){
        StringTokenizer tokenizer = new StringTokenizer(text);
        while(tokenizer.hasMoreTokens()){
            count++;
            tokenizer.nextToken();
        }
    }
}
