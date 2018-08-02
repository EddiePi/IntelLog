package LogConsumer;

import IntelMessage.IntelMessageRule;
import Main.BuilderConf;
import STGraph.STNode;
import utils.RootPathReader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Created by Eddie on 2018/7/31.
 */
public class LogAnalyzer {
    STNode root;
    Map<String, STNode> flattenGraph;
    //Set<String> commonGroup;
    BuilderConf conf = BuilderConf.getInstance();

    Map<String, SessionConsumer> consumerMap;
    //Set<IntelMessageRule> allCriticalRules;

    String sessionRootPath;
    RootPathReader rootPathReader;

    public LogAnalyzer(STNode rootNode) {
        this.root = rootNode;
        sessionRootPath = conf.getStringOrDefault("analyzer.session-root.path", "/path/to/session/root");
        rootPathReader = new RootPathReader(sessionRootPath);
        flattenRoot();

        consumerMap = new HashMap<>();
    }

    private void flattenRoot() {
        flattenGraph = new HashMap<>();
        Stack<STNode> helperStack = new Stack<>();
        STNode curNode;
        helperStack.push(root);
        while (!helperStack.empty()) {
            curNode = helperStack.pop();
            for (STNode node: curNode.directChildrenGroups.values()) {
                helperStack.push(node);
            }

            for (STNode node: curNode.directAfterGroups.values()) {
                helperStack.push(node);
            }

            flattenGraph.put(curNode.group, curNode);
        }
    }

    public void analyze() {
        File file;
        while ((file = rootPathReader.nextFile()) != null) {
            if (file.length() == 0 || file.getName().startsWith(".")) {
                continue;
            }
            // we use the absolute path of the file as the session id
            SessionConsumer consumer = new SessionConsumer(file, flattenGraph, file.getAbsolutePath());
            consumer.consumeSession();
            consumerMap.put(file.getAbsolutePath(), consumer);
        }
    }

    public void report() {
        for (SessionConsumer instance: consumerMap.values()) {
            System.out.print("***");
            instance.report();
        }
    }
}
