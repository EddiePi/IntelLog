package STGraph;

import IntelMessage.*;
import IntelMessage.LogFormatter.AbstractFormatter;
import Main.BuilderConf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.RootPathReader;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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
    String logFormatterClassName;
    boolean useCommonGroup;

    Constructor<?> formatterConstructor;
    AbstractFormatter formatter;
    RootPathReader rootPathReader;
    IntelMessageRuleList intelMessageRuleList;

    HelperComposite helper = null;

    // fields about building the graph
    Stack<STNode> buildingNodesStack;

    // record the relationship between each group
    ArrayList<ArrayList<HierarchyType>>  groupRelationship;
    // group name to index map
    Map<Integer, String> indexToGroupNameMap;
    Map<String, Integer> groupNameToIndexMap;

    // common group from the helper
    Set<String> commonGroup = null;


    public GraphBuilder() {
        Class<? extends AbstractFormatter> formatterClazz;
        logRootPath = conf.getStringOrDefault("log-root.file.path", "../conf/log-root/");
        logFormatterClassName = conf.getStringOrDefault("log-formatter.class.name", "IntelMessage.LogFormatter.SparkFormatter");
        useCommonGroup = conf.getBooleanOrDefault("st-graph.common-group", true);
        rootPathReader = new RootPathReader(logRootPath);
        intelMessageRuleList = RuleListSingleton.getInstance().getIntelMessageRuleList();
        try {
            formatterClazz = (Class<? extends AbstractFormatter>) Class.forName(logFormatterClassName);
            formatterConstructor = formatterClazz.getDeclaredConstructor(File.class, IntelMessageRuleList.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        buildingNodesStack = new Stack<>();
    }

    public Set<String> getCommonGroup() {
        if (commonGroup == null) {
            logger.warn("Common Group has not been built yet.");
        }
        return commonGroup;
    }

    public HelperComposite buildMatrix() {
        File logFile;
        helper = new HelperComposite("root", intelMessageRuleList);
        while ((logFile = rootPathReader.nextFile()) != null) {

            if (logFile.getName().startsWith(".")) {
                continue;
            }
            try {
                formatter = (AbstractFormatter) formatterConstructor.newInstance(logFile, intelMessageRuleList);
                //formatter.setLogFile(logFile);
                //formatter.setRuleList(intelMessageRuleList);


            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            helper.resetTimestamp();
            IntelMessage message;
            while((message = formatter.syncGetIntelMessage()) != null) {
                helper.addIntelMessage(message);
            }
            logger.debug("added log file: " + logFile.getAbsolutePath());
            helper.updateRelationship();
//            // this field is for mr log
//            String curAppName;
//            curAppName = LogUtil.extractAppFromPath(logFile.getAbsolutePath());
//            if (prevAppName == null || !prevAppName.equals(curAppName)) {
//                helper.resetTimestamp();
//                prevAppName = curAppName;
//                helper.reset();
//            }
        }

        this.groupRelationship = helper.groupRelationship;
        this.indexToGroupNameMap = helper.indexToGroupNameMap;
        this.groupNameToIndexMap = helper.groupNameToIndexMap;
        this.commonGroup = helper.getCommonGroup();

        return helper;
    }

    public STNode buildGraph() {
        STNode root = new STNode("ROOT");
        if (helper == null) {
            buildMatrix();
        }
        root.remainingGroups.addAll(useCommonGroup ? helper.getCommonGroup() : helper.groupToRules.keySet());

        buildingNodesStack.push(root);
        while (!buildingNodesStack.empty()) {
            STNode curNode = buildingNodesStack.pop();
            buildSTNode(curNode);
            // push after nodes into stack
            for (STNode afterNode: curNode.directAfterGroups.values()) {
                buildingNodesStack.push(afterNode);
            }

            // push child nodes into stack
            for (STNode childNode: curNode.directChildrenGroups.values()) {
                buildingNodesStack.push(childNode);
            }
        }
        return root;
    }

    void buildSTNode(STNode node) {
        Set<String> childGroupCandidates = new HashSet<>();
        Set<String> afterGroupCandidates = new HashSet<>();
        // build the child candidates
        if (!node.group.equals("ROOT")) {
            List<HierarchyType> nodeRow = groupRelationship.get(groupNameToIndexMap.get(node.group));
            for (int i = 0; i < nodeRow.size(); i ++) {
                String groupToCompare = indexToGroupNameMap.get(i);
                if (node.remainingGroups.contains(groupToCompare)) {
                    if (nodeRow.get(i) == HierarchyType.BEFORE) {
                        afterGroupCandidates.add(groupToCompare);
                    }
                    if (nodeRow.get(i) == HierarchyType.IS_PARENT) {
                        childGroupCandidates.add(groupToCompare);
                    }
                }
            }
        } else {
            childGroupCandidates.addAll(node.remainingGroups);
        }

        for (int i = 0; i < groupRelationship.size(); i++) {
            String groupInRow = indexToGroupNameMap.get(i);
            if (!node.remainingGroups.contains(groupInRow)) {
                continue;
            }
            for (int j = 0; j < groupRelationship.size(); j++) {
                String groupInColumn = indexToGroupNameMap.get(j);
                if (!node.remainingGroups.contains(groupInColumn)) {
                    continue;
                }
                if (i == j) {
                    continue;
                }

                HierarchyType curType = groupRelationship.get(i).get(j);
                if (curType == HierarchyType.IS_CHILD) {
                    childGroupCandidates.remove(groupInRow);
                    afterGroupCandidates.remove(groupInRow);
                }
                if (curType == HierarchyType.AFTER) {
                    afterGroupCandidates.remove(groupInRow);
                    childGroupCandidates.remove(groupInRow);
                }
            }
        }

        Set<String> remainingInSub = new HashSet<>(node.remainingGroups);

        remainingInSub.removeAll(childGroupCandidates);
        remainingInSub.removeAll(afterGroupCandidates);

        for (String group: childGroupCandidates) {
            STNode newChild = new STNode(group);
            newChild.remainingGroups.addAll(remainingInSub);
            node.directChildrenGroups.put(group, newChild);
        }

        for (String group: afterGroupCandidates) {
            STNode newAfter = new STNode(group);
            newAfter.remainingGroups.addAll(remainingInSub);
            node.directAfterGroups.put(group, newAfter);
        }

        // assign corresponding IntelMessageRule to the note
        // and find the signature group
        Set<String> commonGroup = helper.getCommonGroup();
        if (commonGroup.contains(node.group)) {
            for (IntelMessageRule rule : helper.groupToRules.get(node.group)) {
                node.updateSignatureSet(rule);
            }
        }
    }
}
