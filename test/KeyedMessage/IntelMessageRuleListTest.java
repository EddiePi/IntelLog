package KeyedMessage;

import IntelMessage.IntelMessageList;
import IntelMessage.IntelMessageRule;
import Log.LogPreprocessor;
import NPL.FrequencyCalculator;
import org.junit.Before;
import org.junit.Test;
import spell.LCSMap;
import spell.LCSObject;
import utils.ObjectSerializer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Eddie on 2018/5/21.
 */
public class IntelMessageRuleListTest {
    IntelMessageList intelMessageList = null;
    List<LCSObject> lcsObjectList;

    FrequencyCalculator fc;
    LCSMap map;

    @Before
    public void setUp() throws Exception {
        fc = new FrequencyCalculator();
        map = new LCSMap();

        //File f = new File("/Users/Eddie/gitRepo/log-preprocessor/data/yarn-only-content.log");
        File f = new File("/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-only-content.log");
        FileReader fr;
        BufferedReader br;
        try {
            fr = new FileReader(f);
            br = new BufferedReader(fr);

            String line;
            while((line = br.readLine()) != null) {
                map.insert(line);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        lcsObjectList = map.getAllLCSObjects();
    }

    @Test
    public void buildIntelMessages() throws Exception {
        intelMessageList = (IntelMessageList) ObjectSerializer.deserialize("/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-intel-message.obj");
        if (intelMessageList == null) {
            intelMessageList = new IntelMessageList();
            intelMessageList.buildIntelMessages(map);
            ObjectSerializer.serialize(intelMessageList, "/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-intel-message.obj");
        }
        intelMessageList.report();
    }

    @Test
    public void findPreposition() throws Exception {
        buildIntelMessages();
        File f = new File("/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-only-content.log");
        List<String> inputLog = new ArrayList<>();
        //File f = new File("/home/eddie/log-preprocessor/data/spark-only-content.log");
        FileReader fr;
        BufferedReader br;
        int maxLine = 100;
        try {
            fr = new FileReader(f);
            br = new BufferedReader(fr);

            String line;
            for (int i = 0; i < maxLine; i++) {
                if ((line = br.readLine()) == null) {
                    break;
                }
                inputLog.add(LogPreprocessor.preProcess(line.toLowerCase()));
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (String line: inputLog) {
            for(IntelMessageRule rule: intelMessageList.intelMessageRules){
                Pattern pattern = Pattern.compile(rule.keyedMessageRule.regex);
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    System.out.printf("log: %s\nregex: %s\n", line, pattern.toString());
                    System.out.printf("POS: %s\n", rule.POSSequence.toString());
                    int tokenIndex;
                    for (tokenIndex = 0; tokenIndex < rule.POSSequence.size(); tokenIndex++) {
                        String token = rule.POSSequence.get(tokenIndex);
                        String[] lineSplit = line.split("\\s+");
                        String location = "";
                        if (token.equals("TO")) {
                            if (tokenIndex + 1 < lineSplit.length) {
                                location = lineSplit[tokenIndex + 1];
                                System.out.printf("location: %s\n", location);
                            }
                        }
                    }
                    break;
                }

            }
            System.out.println();

        }
    }
}