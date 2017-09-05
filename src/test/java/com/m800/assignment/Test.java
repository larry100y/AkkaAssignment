package com.m800.assignment;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.m800.assignment.actor.Manager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import static org.junit.Assert.assertEquals;

public class Test {

    static ActorSystem system;


    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void tearDown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @org.junit.Test
    public void testWordCount() {
        TestKit probe = new TestKit(system);

        File testFile = new File(getClass().getClassLoader().getResource("word_count_test.txt").getFile());
        /*
        "Sometimes the IDE will not copy the 'test/resources' folder to output dir," +
        " so it cannot find the test file and test will fails."
         */
        
        String dirPath = testFile.getParent();
        System.out.println("Resource Directory: " + dirPath);

        String resultFilePath = FilenameUtils.concat(dirPath, "parse-result");
        System.out.println("Result File Path: " + resultFilePath);

        ActorRef manager = system.actorOf(Manager.props(resultFilePath, false), "manager");
        manager.tell(new Manager.LaunchTask(dirPath), probe.getRef());
        Manager.ReportWordCount response = probe.expectMsgClass(Manager.ReportWordCount.class);

        assertEquals(response.wordCount, countWord(testFile));

    }

    private static int countWord(File file) {
        try {
            int count = 0;
            String text = FileUtils.readFileToString(file);
            StringTokenizer tokenizer = new StringTokenizer(text);
            while (tokenizer.hasMoreTokens()) {
                count++;
                tokenizer.nextToken();
            }
            System.out.println("Actual word count:" + count);
            return count;
        }catch(IOException ioe){
            ioe.printStackTrace();
            return 0;
        }
    }

}
