package com.m800.assignment.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class FileParser extends AbstractActor{

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    private final ActorRef manager;
    private final String filePath;

    public FileParser(String filePath, ActorRef manager) {
        this.filePath = filePath;
        this.manager = manager;
    }

    public static Props props(String filePath, ActorRef manager){
        return Props.create(FileParser.class, filePath, manager);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ParseFile.class, this::onParseFile)
                .build();
    }

    private void onParseFile(ParseFile msg){
        ActorRef aggregator = getContext().actorOf(Aggregator.props(filePath, manager), "aggregator");
        aggregator.tell(new StartOfFile(), getSelf());

        try(Stream<String> stream = Files.lines(Paths.get(filePath))){
            stream.forEach(l -> {
                aggregator.tell(new Line(l), getSelf());
            });
        } catch (IOException e) {
            logger.info("An error occurs while parsing " + "' " + filePath + "'");
        }

        aggregator.tell(new EndOfFile(), getSelf());

    }

    /**
     *
     */
    public static final class ParseFile {
        final String filePath;

        public ParseFile(String filePath) {
            this.filePath = filePath;
        }
    }

    /**
     *
     */
    public static final class StartOfFile {

    }

    /**
     *
     */
    public static final class Line {
        final String text;

        public Line(String text) {
            this.text = text;
        }
    }

    /**
     *
     */
    public static final class EndOfFile {

    }

    /**
     *
     */
    public static final class Error{

    }

}
