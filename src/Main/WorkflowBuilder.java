package Main;

import IntelMessage.IntelMessageRule;
import IntelMessage.IntelMessageRuleList;
import IntelMessage.LogFormatter.AbstractFormatter;
import IntelMessage.LogFormatter.SparkFormatter;
import STGraph.STComposite;
import utils.GsonSerializer;

import java.util.Map;
import java.util.Set;

/**
 * Created by Eddie on 2018/7/16.
 */
public class WorkflowBuilder {
    public static void main(String[] args) {
        BuilderConf conf =  BuilderConf.getInstance();
        String intelRuleFilePath = conf.getStringOrDefault("intel-rule.file.path", "../conf/intel-log-rule.json");
        String logFileRootPath = conf.getStringOrDefault("log-root.file.path", "../conf/log-root/");
        IntelMessageRuleList intelMessageRuleList = GsonSerializer.readJSON(IntelMessageRuleList.class, intelRuleFilePath);
        Map<String, Set<IntelMessageRule>> groupToRuleMap = intelMessageRuleList.buildGroupToRuleMap();





        System.out.print("test");
    }
}
