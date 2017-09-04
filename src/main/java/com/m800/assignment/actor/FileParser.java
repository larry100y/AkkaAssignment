package com.m800.assignment.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static com.m800.assignment.actor.FileScanner.FINISHED;

public class FileParser extends AbstractActor{

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);
    private final ActorRef aggregator;

    public FileParser(ActorRef aggregator) {
        this.aggregator = aggregator;
    }

    public static Props props(ActorRef aggregator){
        return Props.create(FileParser.class, () -> new FileParser(aggregator));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ParseFile.class, p -> {
                    aggregator.tell(new StartOfFile(p.file.toString()), getSelf());

                    // for line in lines
                    try(Stream<String> stream = Files.lines(Paths.get(p.file.toString()))){
                        stream.forEach(l -> {
                            aggregator.tell(new Line(l), getSelf());
                        });
                    }

                    // EOF
                    aggregator.tell(EOF, getSelf());

                    getSender().tell(FINISHED, getSelf());
                })
                .build();
    }

    public static final class ParseFile {
        final File file;

        public ParseFile(File file) {
            this.file = file;
        }
    }

    public static final class StartOfFile {
        final String filename;

        public StartOfFile(String filename) {
            this.filename = filename;
        }
    }

    public static final class Line {
        final String text;

        public Line(String text) {
            this.text = text;
        }
    }

    public static final String EOF = "EOF";



}
