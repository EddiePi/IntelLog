package LogConsumer;

import IntelMessage.IntelMessage;
import IntelMessage.IntelMessageRuleList;
import IntelMessage.LogFormatter.AbstractFormatter;
import IntelMessage.RuleListSingleton;
import Main.BuilderConf;
import STGraph.STNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Eddie on 2018/8/1.
 */
public class SessionConsumer {
    Logger logger = LogManager.getLogger(SessionConsumer.class);
    AbstractFormatter formatter;
    BuilderConf conf = BuilderConf.getInstance();
    Constructor<?> formatterConstructor;

    File logFile;
    IntelMessageRuleList intelMessageRuleList;

    Map<String, STNode> flatten;
    Map<String, STInstance> instanceMap;

    Set<String> commonGroups;

    String sessionId;

    public SessionConsumer (File logFile, Map<String, STNode> flatten, Set<String> commonGroups, String sessionId) {
        // initialize the formatter using reflection
        String logFormatterClassName = conf.getStringOrDefault("log-formatter.class.name", "IntelMessage.LogFormatter.SparkFormatter");
        Class<? extends AbstractFormatter> formatterClazz;
        try {
            formatterClazz = (Class<? extends AbstractFormatter>) Class.forName(logFormatterClassName);
            formatterConstructor = formatterClazz.getDeclaredConstructor(File.class, IntelMessageRuleList.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        this.logFile = logFile;
        intelMessageRuleList = RuleListSingleton.getInstance().getIntelMessageRuleList();
        try {
            formatter = (AbstractFormatter) formatterConstructor.newInstance(logFile, intelMessageRuleList);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        // init other variables
        this.flatten = flatten;
        this.commonGroups = commonGroups;
        this.sessionId = sessionId;

        instanceMap = new HashMap<>();
        for (Map.Entry<String, STNode> entry: flatten.entrySet()) {
            String key = entry.getKey();
            STNode value = entry.getValue();
            instanceMap.put(key, new STInstance(key, value.signatureSet));
        }
    }

    public void consumeSession() {
        IntelMessage message;
        while ((message = formatter.syncGetIntelMessage()) != null) {
            Set<String> curGroups = message.ruleRef.entityGroups;
            for (String group: curGroups) {
                if (commonGroups.contains(group)) {
                    STInstance instanceToAdd = instanceMap.get(group);
                    if (instanceToAdd != null) {
                        instanceToAdd.addMessage(message);
                    }
                }
            }
        }
    }

    public void report() {
        System.out.printf("session: %s\n", sessionId);
        for (Map.Entry<String, STInstance> entry: instanceMap.entrySet()) {
            System.out.printf("st instance: %s\n", entry.getValue().toString());
        }
    }
}
