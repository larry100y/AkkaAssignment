package com.m800.assignment.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    /*
    private void checkDir(Scan c){
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
    */

    private void onScan(Scan msg){
        File file = new File(this.dir);
        if(!file.isDirectory()){
            logger.info("there is no such directory");
            return; //TODO
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
