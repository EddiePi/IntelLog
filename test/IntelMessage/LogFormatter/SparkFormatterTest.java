package IntelMessage.LogFormatter;

import IntelMessage.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * Created by Eddie on 2018/7/9.
 */
public class SparkFormatterTest {

    SparkFormatter sparkFormatter;
    String testString;
    String logFilePath = "/Users/Eddie/gitRepo/log-preprocessor/data/mr-data/mr-log-sample.txt";
    String ruleFilePath = "/Users/Eddie/gitRepo/log-preprocessor/data/mr-data/mr-intel-log.json";
    Logger logger = LogManager.getLogger(this.getClass());

    @Before
    public void setUp() throws Exception {
        sparkFormatter = new SparkFormatter(logFilePath, ruleFilePath);
        testString = "2017-08-14 09:11:14.908 INFO SecurityManager: Changing modify acls groups to:";
    }

    @Test
    public void format() throws Exception {
        IntelMessage message = sparkFormatter.format(testString);
    }

    @Test
    public void getIntelMessageRule() throws Exception {
        BufferedReader br;
        IntelMessage message;
        try {
            br = new BufferedReader(new FileReader(logFilePath));
            String line;
            while ((line = br.readLine()) != null) {
                String content = sparkFormatter.replacePunctuation(line);

//                IntelMessageRule rule = sparkFormatter.getIntelMessageRule(content);
//                IntelMessageRule rule = sparkFormatter.getIntelMessageRuleFromRegex(content);
//                System.out.println(rule.toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void buildCompleteMessage() throws Exception {
        BufferedReader br;
        IntelMessage message;
        try {
            br = new BufferedReader(new FileReader(logFilePath));
            String line;
            while ((line = br.readLine()) != null) {
                message = new IntelMessage();
                String content = sparkFormatter.replacePunctuation(line);
                message.setOriginalLog(content);
                logger.debug(content);
//                IntelMessageRule rule = sparkFormatter.getIntelMessageRule(content);

                sparkFormatter.buildCompleteMessage(message);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}