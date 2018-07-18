package IntelMessage;

import org.junit.Before;
import org.junit.Test;
import utils.GsonSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eddie on 2018/6/9.
 */
public class IntelMessageRuleListTest {
    IntelMessageRuleList intelMessageRuleList;
    String keyTagFilePath;
    String entityFilePath;
    String sampleFilePath;

    @Before
    public void setUp() throws Exception {
        intelMessageRuleList = new IntelMessageRuleList();
//        keyTagFilePath = "/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-keys-tags.txt";
//        entityFilePath = "/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-entity-sorted.txt";
//        sampleFilePath = "/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-log-sample.txt";
        keyTagFilePath = "/Users/Eddie/gitRepo/log-preprocessor/data/mr-data/mr-keys-tags.txt";
        entityFilePath = "/Users/Eddie/gitRepo/log-preprocessor/data/mr-data/mr-entity-sorted.txt";
        sampleFilePath = "/Users/Eddie/gitRepo/log-preprocessor/data/mr-data/mr-log-sample.txt";
    }

    @Test
    public void buildIntelMessagesRule() throws Exception {
        intelMessageRuleList.buildIntelMessagesRule(keyTagFilePath, entityFilePath, sampleFilePath);
        intelMessageRuleList.report();
        GsonSerializer.writeJSON(intelMessageRuleList, "/Users/Eddie/gitRepo/log-preprocessor/data/mr-data/mr-new-intel-log.json");
    }

    @Test
    public void loadIntelMessageRule() throws Exception {
        //BufferedReader br;
        String path = "/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-intel-log.json";
//        Gson gson = new Gson();
//        try {
//            br = new BufferedReader(new FileReader(path));
//            intelMessageRuleList = gson.fromJson(br, IntelMessageRuleList.class);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        intelMessageRuleList = GsonSerializer.readJSON(IntelMessageRuleList.class, path);
        System.out.print("stop");
    }

    @Test
    public void reverseIndex() throws Exception {
        String path = "/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-intel-log-with-group.json";
        intelMessageRuleList = GsonSerializer.readJSON(IntelMessageRuleList.class, path);
        intelMessageRuleList.buildGroupToRuleMap();
    }

    @Test
    public void report() throws Exception {
        List list = new ArrayList();
    }

}