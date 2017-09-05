package com.m800.assignment.actor;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.commons.io.FileUtils;
import scala.concurrent.duration.FiniteDuration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Manager extends AbstractActor{

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    private final Set<String> filePathSet=new HashSet<>();
    private final String resultFilePath;
    private boolean isWriteToFile = true; //For test
    private ActorRef originator;

    public Manager(String resultFilePath) {
        this.resultFilePath = resultFilePath;
    }

    public Manager(String resultFilePath, boolean isWriteToFile) {
        this.resultFilePath = resultFilePath;
        this.isWriteToFile = isWriteToFile;
    }

    public static Props props(String resultFilePath){
        return Props.create(Manager.class, resultFilePath);
    }

    public static Props props(String resultFilePath, boolean isWriteToFile){
        return Props.create(Manager.class, resultFilePath, isWriteToFile);
    }

    /**
     * Message: Specify the directory for text file searching and where the result is located.
     */
    public static final class LaunchTask{
        final String dir;

        public LaunchTask(String dir) {
            this.dir = dir;
        }
    }

    /**
     * Message: text file list from the scanner
     */
    public static final class ReportParserList{
        final Set<String> filePathSet;

        public ReportParserList(Set<String> filePathSet) {
            this.filePathSet = filePathSet;
        }
    }

    /**
     * Message: word count result from aggregator
     */
    public static final class ReportResult{
        public final String filePath;
        public final int wordCount;

        public ReportResult(String filePath, int wordCount) {
            this.filePath = filePath;
            this.wordCount = wordCount;
        }
    }

    /**
     * Message: timeout event
     */
    public static final class Timeout{

    }

    /**
     * Message: Send word count to the actor who created this manager actor. For test only.
     */
    public static final class ReportWordCount{
        public final int wordCount;

        public ReportWordCount(int wordCount) {
            this.wordCount = wordCount;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(LaunchTask.class, this::onLaunchTask)
                .match(ReportParserList.class, this::onReportParserList)
                .match(ReportResult.class, this::onReportResult)
                .match(Timeout.class, this::onTimeout)
                .build();
    }

    /**
     * Create a scanner and pass the directory. <br/>
     * Then schedule a timeout event.
     *
     * @param launchTaskMsg
     */
    private void onLaunchTask(LaunchTask launchTaskMsg){
        ActorRef scanner = getContext().actorOf(FileScanner.props(launchTaskMsg.dir, getSelf()), "scanner");
        scanner.tell(new FileScanner.Scan(), getSelf());

        getContext().getSystem().scheduler().scheduleOnce(
                new FiniteDuration(10L, TimeUnit.SECONDS),
                getSelf(),
                new Timeout(),
                getContext().dispatcher(),
                getSelf()
        );

        this.originator = getSender();
    }

    private void onReportParserList(ReportParserList msg){
        filePathSet.addAll(msg.filePathSet);
    }

    /**
     * Append word count result to a designated file. <br/>
     * When all the text files are parsed, kill the manager actor.
     *
     * @param msg
     */
    private void onReportResult(ReportResult msg){
        File resultFile = new File(resultFilePath);
        String content =
                        "File: " + msg.filePath + "\n"
                        + "Word Count: " + msg.wordCount + "\n"
                        + "************\n";

        System.out.println("************");
        System.out.println(content);

        try {
            if(isWriteToFile){
                FileUtils.writeStringToFile(resultFile, content, true);
            }else{
                this.originator.tell(new ReportWordCount(msg.wordCount), getSelf());
            }
            if(filePathSet.contains(msg.filePath)){
                filePathSet.remove(msg.filePath);
                logger.info("File {} is parsed.", msg.filePath);
            }
            if(filePathSet.size() == 0){
                getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
                //isFinished = true;
                logger.info("All parsings are done.");
            }
        } catch (IOException e) {
            //e.printStackTrace();
            logger.info("Could not write result to file.");
        }
    }

    private void onTimeout(Timeout msg){
        getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
    }


}
