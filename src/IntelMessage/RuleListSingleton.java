package IntelMessage;

import Main.BuilderConf;
import utils.GsonSerializer;

/**
 * Created by Eddie on 2018/7/31.
 */
public class RuleListSingleton {

    private static RuleListSingleton instance;

    private static IntelMessageRuleList intelMessageRuleList;

    public static RuleListSingleton getInstance() {
        if (instance == null) {
            instance = new RuleListSingleton();
        }

        return instance;
    }

    private RuleListSingleton() {
        BuilderConf conf = BuilderConf.getInstance();
        String intelRulePath = conf.getStringOrDefault("intel-rule.file.path", "../conf/intel-log-rule.json");
        intelMessageRuleList = GsonSerializer.readJSON(IntelMessageRuleList.class, intelRulePath);
    }

    public IntelMessageRuleList getIntelMessageRuleList() {
        return intelMessageRuleList;
    }
}
