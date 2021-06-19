package LSTMTokenizer;

import IntelMessage.IntelMessage;
import IntelMessage.IntelMessageRule;
import IntelMessage.IntelMessageRuleList;
import IntelMessage.LogFormatter.AbstractFormatter;
import IntelMessage.RuleListSingleton;
import LogConsumer.STInstance;
import Main.BuilderConf;
import utils.RootPathReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Tokenizer {

    IntelMessageRuleList intelMessageRuleList = RuleListSingleton.getInstance().getIntelMessageRuleList();
    Map<String, Integer> logKeyToToken = new HashMap<>();
    AbstractFormatter formatter;
    BuilderConf conf = BuilderConf.getInstance();
    Constructor<?> formatterConstructor;

    String dirPrefix = "/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-test/debug-level/logs/application_1546967335286_0003-wc-debug/container_1546967335286_0003_01_000002/";

    File inputDir;
    File outputDir;

    public Tokenizer() {
        buildKeyToTokenMap();
        initFormatter();
    }

    public void tokenizeAll (String rootDir) {
        RootPathReader rootReader = new RootPathReader(rootDir);
        File nextFile = null;
        while ((nextFile = rootReader.nextFile()) != null) {
            if (nextFile.getName().equals("stderr")) {
                File readFile = nextFile;
                if (readFile.getAbsolutePath().contains("000001")) {
                    continue;
                }
                System.out.print("tokenizing file: %s\n" + readFile.getAbsolutePath());
                File writeFile = new File(readFile.getParent() + "/log_token");
                tokenize(readFile, writeFile);
            }
        }

    }

    public void tokenize(File readFile, File writeFile) {
        IntelMessage message;
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new java.io.FileWriter(writeFile));
            formatter.setLogFile(readFile);
            while ((message = formatter.syncGetIntelMessage()) != null) {
                if (message.ruleRef == null) {
                    //System.out.printf("unexpected message: %s\n", message.originalLog);
                    continue;
                }
                Integer token = logKeyToToken.get(message.ruleRef.originalLogKey);
                String trimedLog = message.originalLog.substring(0, Math.min(10, message.originalLog.length()));
                System.out.printf("message: %s, token: %d\n", trimedLog, token);
                try {
                    writer.write(token + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            writer.close();
        } catch (IOException e) {}
    }

    void buildKeyToTokenMap() {
        int index = 0;
        for (IntelMessageRule rule: intelMessageRuleList.intelMessageRules) {
            logKeyToToken.put(rule.originalLogKey, index++);
        }
    }

    void initFormatter() {
        String logFormatterClassName = conf.getStringOrDefault("log-formatter.class.name", "IntelMessage.LogFormatter.SparkFormatter");
        Class<? extends AbstractFormatter> formatterClazz;
        try {
            formatterClazz = (Class<? extends AbstractFormatter>) Class.forName(logFormatterClassName);
            formatterConstructor = formatterClazz.getDeclaredConstructor(File.class, IntelMessageRuleList.class, AbstractFormatter.FormatterMode.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        String inputFile = dirPrefix + "stderr";

        intelMessageRuleList = RuleListSingleton.getInstance().getIntelMessageRuleList();
        try {
            formatter = (AbstractFormatter) formatterConstructor.newInstance(new File(inputFile), intelMessageRuleList, AbstractFormatter.FormatterMode.CONSUMING);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
