package STGraph;

import IntelMessage.IntelMessage;
import IntelMessage.IntelMessageRuleList;
import IntelMessage.LogFormatter.AbstractFormatter;
import Main.BuilderConf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.GsonSerializer;
import utils.RootPathReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * <code>GraphBuilder</code> combines <code>AbstractFormatter</code> and <code>IntelMessageRuleList</code>.
 * Using information from both real logs and rules to build STGraph.
 * Currently, the <code>GraphBuilder</code> only supports the entity hierarchy.
 * A <code>GraphBuilder</code> is only responsible for building one STGraph root.
 */
public class GraphBuilder {
    Logger logger = LogManager.getLogger();
    BuilderConf conf = BuilderConf.getInstance();
    String logRootPath;
    String intelRulePath;
    String logFormatterClassName;
    Class<? extends AbstractFormatter> formatterClazz;
    Constructor<?> formatterConstructor;
    AbstractFormatter formatter;
    RootPathReader rootPathReader;
    IntelMessageRuleList intelMessageRuleList;

    public GraphBuilder() {
        logRootPath = conf.getStringOrDefault("log-root.file.path", "../conf/log-root/");
        intelRulePath = conf.getStringOrDefault("intel-rule.file.path", "../conf/intel-log-rule.json");
        logFormatterClassName = conf.getStringOrDefault("log-formatter.class.name", "IntelMessage.LogFormatter.SparkFormatter");
        rootPathReader = new RootPathReader(logRootPath);
        intelMessageRuleList = GsonSerializer.readJSON(IntelMessageRuleList.class, intelRulePath);
        try {
            formatterClazz = (Class<? extends AbstractFormatter>) Class.forName(logFormatterClassName);
            formatterConstructor = formatterClazz.getDeclaredConstructor(File.class, IntelMessageRuleList.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public STComposite build() {
        File logFile;
        STComposite root = new STComposite("root", intelMessageRuleList.buildGroupToRuleMap());
        while ((logFile = rootPathReader.nextFile()) != null) {
            if (logFile.getName().startsWith(".")) {
                continue;
            }
            try {
                formatter = (AbstractFormatter) formatterConstructor.newInstance(logFile, intelMessageRuleList);
                formatter.setLogFile(logFile);
                formatter.setRuleList(intelMessageRuleList);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            root.resetTimestamp();
            IntelMessage message;
            while((message = formatter.syncGetIntelMessage()) != null) {
                root.addIntelMessage(message);
            }
            logger.debug("added log file: " + logFile.getAbsolutePath());
            root.updateRelationship();
        }
        return root;
    }
}
