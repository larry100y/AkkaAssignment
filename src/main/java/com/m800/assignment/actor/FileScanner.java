package com.m800.assignment.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

public class FileScanner extends AbstractActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);
    private final ActorRef parser;
    private final Queue<File> queue = new LinkedList<File>();

    private boolean isAllFinished=false;

    public FileScanner(ActorRef parser) {
        this.parser = parser;
    }

    public static Props props(ActorRef parser){
        return Props.create(FileScanner.class, () -> new FileScanner(parser));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CheckDir.class, c -> {
                    checkDir(c);
                })
                .matchEquals(FINISHED, f -> {
                    if(!queue.isEmpty()){
                        parser.tell(new FileParser.ParseFile(queue.poll()), getSelf());
                    }else{
                       getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
                    }
                })
                .build();
    }

    private void checkDir(CheckDir c){
        //check dir path, list existing text file
        File file = new File(c.dir);
        if(!file.isDirectory()){
            System.out.println(">>> there is no such directory");
            return;
        }
        File[] list = file.listFiles();
        if(list.length < 1){
            System.out.println(">>> there is no file in directory");
            return;
        }

        for(File f : list){
            if(isTextFile(f)){
                // push into queue
                queue.add(f);
            }
        }

        if(!queue.isEmpty()){
            parser.tell(new FileParser.ParseFile(queue.poll()), getSelf());
        }else{
            System.out.println(">>> there is no text file in directory");
            getSelf().tell(FINISHED, getSelf());
        }
    }

    private boolean isTextFile(File f){
            if(f != null){
                if(f.toString().endsWith("txt")){
                    System.out.println("Found text file: " + f.toString());
                    return true;
                }
            }
            return false;
    }

    public static final class CheckDir {
        final String dir;

        public CheckDir(String dir) {
            this.dir = dir;
        }
    }

    public static final String FINISHED = "Finished";
}
