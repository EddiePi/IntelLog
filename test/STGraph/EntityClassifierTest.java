package STGraph;

import IntelMessage.IntelMessageRule;
import IntelMessage.IntelMessageRuleList;
import IntelMessage.RuleListSingleton;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import utils.GsonSerializer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by Eddie on 2018/7/6.
 */
public class EntityClassifierTest {

    EntityClassifier classifier;
    String entityFilePath;

    @Before
    public void setUp() throws Exception {
        classifier = new EntityClassifier();
        //entityFilePath = "/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-entity-sorted.txt";
        //entityFilePath = "/Users/Eddie/gitRepo/log-preprocessor/data/mr-data/mr-entity-sorted.txt";
        entityFilePath = "/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-test/debug-level/spark-entity-all-sorted.txt";

    }

    @Test
    public void buildEntityGroup() throws Exception {
        classifier.buildEntityGroup(entityFilePath);
        classifier.reverseIndex();
        classifier.report();
    }

    @Test
    public void assignGroup() throws Exception {
        String readingRuleListFile = "/Users/Eddie/gitRepo/log-preprocessor/data/mr-data/mr-intel-log.json";
        //String writingRuleListFile = "/Users/Eddie/gitRepo/log-preprocessor/data/mr-data/mr-intel-log-with-group.json";
        String writingRuleListFile = "/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-test/debug-level/spark-intel-log-debug-all-with-group.json";
        IntelMessageRuleList ruleList = RuleListSingleton.getInstance().getIntelMessageRuleList();
        classifier.buildEntityGroup(entityFilePath);
        Map<String, Set<String>> wordToGroupMap = classifier.wordToGroupMap;
        for (IntelMessageRule rule: ruleList.intelMessageRules) {
            if (rule.entityGroups == null) {
                rule.entityGroups = new HashSet<>();
            }
            for (String entity: rule.entities) {
                Set<String> group = wordToGroupMap.get(entity);
                if (group != null) {
                    rule.entityGroups.addAll(group);
                }
            }
        }
        GsonSerializer.writeJSON(ruleList, writingRuleListFile);
    }
}