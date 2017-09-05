package com.m800.assignment.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.File;
import java.util.HashSet;
import java.util.Set;


public class FileScanner extends AbstractActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);
    private final ActorRef manager;
    private final String dir;

    public FileScanner(String dir, ActorRef manager) {
        this.dir = dir;
        this.manager = manager;
    }

    public static Props props(String dir, ActorRef manager){
        return Props.create(FileScanner.class, dir, manager);
    }

    /**
     *  Message: Scan the directory for text file
     */
    public static final class Scan {

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Scan.class, this::onScan)
                .build();
    }

    private void onScan(Scan msg){
        File file = new File(this.dir);
        if(!file.isDirectory()){
            logger.info("There is no such directory: {}", dir);
            manager.tell(new Manager.NoDirectory(), getSelf());
            return;
        }
        //String[] filePaths = file.list(new SuffixFileFilter(".txt"));
        File[] files = file.listFiles();
        int parserId = 0;
        Set<String> filePathSet = new HashSet<>();
        for (File f : files){
            if(f != null && isTextFile(f)){
                String filePath = f.getAbsolutePath();
                ActorRef parser = getContext().actorOf(FileParser.props(filePath, manager), "parser-" + parserId);
                parser.tell(new FileParser.ParseFile(filePath), getSelf());
                parserId++;
                filePathSet.add(filePath);
            }
        }
        manager.tell(new Manager.ReportParserList(filePathSet), getSelf());
    }

    private boolean isTextFile(File f){
            if(f != null){
                if(f.toString().endsWith("txt")){
                    logger.info("Found text file: {}", f.toString());
                    return true;
                }
            }
            return false;
    }

}
