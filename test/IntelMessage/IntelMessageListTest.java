package IntelMessage;

import org.junit.Before;
import org.junit.Test;
import utils.GsonSerializer;

import static org.junit.Assert.*;

/**
 * Created by Eddie on 2018/6/9.
 */
public class IntelMessageListTest {
    IntelMessageList intelMessageList;
    String keyTagFilePath;
    String entityFilePath;
    String sampleFilePath;

    @Before
    public void setUp() throws Exception {
        intelMessageList = new IntelMessageList();
//        keyTagFilePath = "/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-keys-tags.txt";
//        entityFilePath = "/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-entity-sorted.txt";
//        sampleFilePath = "/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-log-sample.txt";
        keyTagFilePath = "/Users/Eddie/gitRepo/log-preprocessor/data/mr-data/mr-keys-tags.txt";
        entityFilePath = "/Users/Eddie/gitRepo/log-preprocessor/data/mr-data/mr-entity-sorted.txt";
        sampleFilePath = "/Users/Eddie/gitRepo/log-preprocessor/data/mr-data/mr-log-sample.txt";
    }

    @Test
    public void buildIntelMessagesRule() throws Exception {
        intelMessageList.buildIntelMessagesRule(keyTagFilePath, entityFilePath, sampleFilePath);
        intelMessageList.report();
        GsonSerializer.writeJSON(intelMessageList, "/Users/Eddie/gitRepo/log-preprocessor/data/mr-data/mr-new-intel-log.json");
    }

    @Test
    public void report() throws Exception {
    }

}