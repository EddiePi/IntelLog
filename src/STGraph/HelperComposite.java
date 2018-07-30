package STGraph;

import IntelMessage.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * A HelperComposite stands for an entity group
 */
public class HelperComposite extends STComponent {
    Logger logger = LogManager.getLogger(this.getClass());

    // groupName to parallel HelperComposite
    //Map<String, HelperComposite> groupedParallelComposite;
    // this field is used to handle real incoming logs, and needs to be reset for each log file
    transient Map<String, LogComposite> groupToLogMap;
    // this array is to construct rule relationship, and needs to be reset for each log file
    transient Set<String> ruleUsage;

    // record all the rules that have been seen
    // TEST ONLY
    transient Set<String> seenRules = new HashSet<>();

    // record the relationship between each group
    public ArrayList<ArrayList<HierarchyType>>  groupRelationship;
    // record the relationship between each IntelLogRule
    public ArrayList<ArrayList<HierarchyType>> ruleRelationship;

    // group name to index map
    public Map<String, Integer> groupNameToIndexMap;

    // index to group name map
    public Map<Integer, String> indexToGroupNameMap;

    // rule name to index map
    public Map<String, Integer> ruleToIndexMap;

    // index to rule name map
    public Map<Integer, String> indexToRuleMap;

    Map<String, Set<IntelMessageRule>> groupToRules;

    public HelperComposite(String groupName, IntelMessageRuleList ruleList) {

        this.groupToRules = ruleList.buildGroupToRuleMap();
        this.groupKey = groupName;
        indexToGroupNameMap = new HashMap<>();
        groupNameToIndexMap = new HashMap<>();
        ruleToIndexMap = new HashMap<>();
        indexToRuleMap = new HashMap<>();
        ruleUsage = new HashSet<>();
        int groupSize = groupToRules.size();
        // initialize the group relationship matrix
        groupRelationship = new ArrayList<>(groupSize);
        for (int i = 0; i < groupSize; i++) {
            ArrayList<HierarchyType> row = new ArrayList<>(groupSize);
            for (int j = 0; j < groupSize; j++) {
                row.add(HierarchyType.NOT_ASSIGNED);
            }
            groupRelationship.add(row);
        }
        int groupIndex = 0;
        for (Map.Entry<String, Set<IntelMessageRule>> entry: groupToRules.entrySet()) {
            indexToGroupNameMap.put(groupIndex, entry.getKey());
            groupNameToIndexMap.put(entry.getKey(), groupIndex);
            groupIndex++;

        }
        // initialize the rule relationship matrix
        int ruleIndex = 0;
        for (IntelMessageRule rule: ruleList.intelMessageRules) {
            if (!ruleToIndexMap.containsKey(rule.originalLogKey)) {
                ruleToIndexMap.put(rule.originalLogKey, ruleIndex);
                indexToRuleMap.put(ruleIndex, rule.originalLogKey);
                ruleIndex++;
            }
        }
        int ruleSize = ruleToIndexMap.size();
        ruleRelationship = new ArrayList<>(ruleSize);
        for (int i = 0; i < ruleSize; i++) {
            ArrayList<HierarchyType> row = new ArrayList<>(ruleSize);
            for (int j = 0; j < ruleSize; j++) {
                row.add(HierarchyType.NOT_ASSIGNED);
            }
            ruleRelationship.add(row);
        }
        reset();
    }

    /**
     * This method is called after each log file (or request) is finished
     */
    public void reset() {
        super.resetTimestamp();
        this.groupToLogMap = new HashMap<>();
        ruleUsage.clear();
    }


    private boolean hasGroup(IntelMessage message) {
        return message.ruleRef.entityGroups.size() > 0;
    }

    private void addLog(String groupName, IntelMessage message) {
        LogComposite logComposite = groupToLogMap.computeIfAbsent(groupName, k -> new LogComposite());
        logComposite.addIntelMessage(message);

        // update rule relationship
        String logKey = message.ruleRef.originalLogKey;
        seenRules.add(logKey);
        if (!ruleUsage.contains(logKey)) {
            updateRuleRelationship(logKey);
        }
    }

    private void addNewChildGroup(String childGroupName) {
        boolean hasGroup = indexToGroupNameMap.values().contains(childGroupName);
        if (!hasGroup) {
            int index = indexToGroupNameMap.size();
            indexToGroupNameMap.put(index, childGroupName);
            int oldSize = groupRelationship.size();
            for (ArrayList<HierarchyType> row: groupRelationship) {
                row.add(HierarchyType.NOT_ASSIGNED);
            }
            ArrayList<HierarchyType> newRow = new ArrayList<>(oldSize + 1);
            for (int i = 0; i < oldSize + 1; i++) {
                newRow.add(HierarchyType.NOT_ASSIGNED);
            }
            groupRelationship.add(newRow);
        }
    }


    public void addIntelMessage(IntelMessage message) {
        super.addIntelMessage(message);
        if (!hasGroup(message)) {
            String keyAsGroupName = message.getRuleRef().originalLogKey;
            addLog(keyAsGroupName, message);
            addNewChildGroup(keyAsGroupName);
        } else {
            for (String group: message.ruleRef.entityGroups) {
                addLog(group, message);
            }
        }
    }

    public void updateRelationship() {
        //logger.debug("updating entity relationship");
        int groupSize = groupRelationship.size();
        for (int rowIndex = 0; rowIndex < groupSize; rowIndex++) {
            for (int columnIndex = rowIndex; columnIndex < groupSize; columnIndex++) {
                if (columnIndex == rowIndex) {
                    groupRelationship.get(rowIndex).set(columnIndex, HierarchyType.SELF);
                } else {
                    setRelationship(rowIndex, columnIndex);
                }
            }
        }
        reset();
        //logger.debug("updated entity relationship");
    }

    private void updateRuleRelationship(String newRule) {
        int newRuleIndex = ruleToIndexMap.get(newRule);
        ruleRelationship.get(newRuleIndex).set(newRuleIndex, HierarchyType.SELF);
        for (String existingRule: ruleUsage) {
            int existingIndex = ruleToIndexMap.get(existingRule);
            HierarchyType curType = ruleRelationship.get(newRuleIndex).get(existingIndex);
            if (curType == HierarchyType.NOT_ASSIGNED) {
                ruleRelationship.get(newRuleIndex).set(existingIndex, HierarchyType.AFTER);
                ruleRelationship.get(existingIndex).set(newRuleIndex, HierarchyType.BEFORE);
            } else if (curType == HierarchyType.BEFORE) {
                ruleRelationship.get(newRuleIndex).set(existingIndex, HierarchyType.PARALLEL);
                ruleRelationship.get(existingIndex).set(newRuleIndex, HierarchyType.PARALLEL);
            }
        }
        ruleUsage.add(newRule);
    }

    private void setRelationship(int rowIndex, int columnIndex) {
        String rowGroup = indexToGroupNameMap.get(rowIndex);
        String columnGroup = indexToGroupNameMap.get(columnIndex);
        LogComposite rowComposite = groupToLogMap.get(rowGroup);
        LogComposite columnComposite = groupToLogMap.get(columnGroup);
        if (rowComposite == null || columnComposite == null) {
            return;
        }
        HierarchyType rowToColumnType = groupRelationship.get(rowIndex).get(columnIndex);

        long rowStart = rowComposite.startTimestamp;
        long rowEnd = rowComposite.endTimestamp;
        long columnStart = columnComposite.startTimestamp;
        long columnEnd = columnComposite.endTimestamp;
        HierarchyType typeToAssign;
        HierarchyType typeToAssignColumn;
        if (rowEnd <= columnStart) {
            typeToAssign = HierarchyType.BEFORE;
            typeToAssignColumn = HierarchyType.AFTER;
        } else if (rowStart >= columnEnd) {
            typeToAssign = HierarchyType.AFTER;
            typeToAssignColumn = HierarchyType.BEFORE;
        } else if (rowStart >= columnStart && rowEnd <= columnEnd) {
            typeToAssign = HierarchyType.IS_CHILD;
            typeToAssignColumn = HierarchyType.IS_PARENT;
        } else if (rowStart <= columnStart && rowEnd >= columnEnd) {
            typeToAssign = HierarchyType.IS_PARENT;
            typeToAssignColumn = HierarchyType.IS_CHILD;
        } else {
            typeToAssign = HierarchyType.PARALLEL;
            typeToAssignColumn = HierarchyType.PARALLEL;
        }

        if (rowToColumnType == HierarchyType.NOT_ASSIGNED) {
            groupRelationship.get(rowIndex).set(columnIndex, typeToAssign);
            groupRelationship.get(columnIndex).set(rowIndex, typeToAssignColumn);
        } else if (typeToAssign == HierarchyType.PARALLEL) {
            groupRelationship.get(rowIndex).set(columnIndex, HierarchyType.PARALLEL);
            groupRelationship.get(columnIndex).set(rowIndex, HierarchyType.PARALLEL);
        } if (typeToAssign == HierarchyType.IS_CHILD && rowToColumnType == HierarchyType.IS_PARENT ||
                typeToAssign == HierarchyType.IS_PARENT && rowToColumnType == HierarchyType.IS_CHILD) {
            groupRelationship.get(rowIndex).set(columnIndex, HierarchyType.SEQUENTIAL);
            groupRelationship.get(columnIndex).set(rowIndex, HierarchyType.SEQUENTIAL);
        }
    }

    public void reportRelationship() {
        int groupSize = indexToGroupNameMap.size();
        System.out.print("\t");
        for (int i = 0; i < groupSize; i++) {
            System.out.print(indexToGroupNameMap.get(i) + "\t");
        }
        System.out.println();
        for (int i = 0; i < groupSize; i++) {
            System.out.print(indexToGroupNameMap.get(i) + "\t");
            for (int j = 0; j < groupSize; j++) {
                System.out.print(groupRelationship.get(i).get(j) + "\t");
            }
            System.out.println();
        }
    }

    public Set<String> getCommonRelationship() {
        int groupSize = indexToGroupNameMap.size();
        String groupName;
        Set<String> groupToReport = new HashSet<>();
        for (int i = 0; i < groupSize; i++) {
            groupName = indexToGroupNameMap.get(i);

            Set<IntelMessageRule> groupedRuleSet = groupToRules.get(groupName);
            if (groupedRuleSet != null && groupedRuleSet.size() > 1) {
                groupToReport.add(groupName);
            }
        }

        return groupToReport;
    }

    public void reportCommonRelationship() {
        System.out.print("******** Reporting common relationship ***********\n");
        Set<String> groupToReport = getCommonRelationship();
        int groupSize = indexToGroupNameMap.size();
        String groupName;
        System.out.println();

        for (int i = 0; i < groupSize; i++) {
            String group = indexToGroupNameMap.get(i);
            if (groupToReport.contains(group))
            System.out.print("\t" + group);
        }
        System.out.println();

        String columnGroupName;
        for (int i = 0; i < groupSize; i++) {
            groupName = indexToGroupNameMap.get(i);
            if (groupToReport.contains(groupName)) {
                System.out.print(groupName + "\t");
                for (int j = 0; j < groupSize; j++) {
                    columnGroupName = indexToGroupNameMap.get(j);
                    if (groupToReport.contains(columnGroupName)) {
                        System.out.print(groupRelationship.get(i).get(j) + "\t");
                    }
                }
                System.out.println();
            }
        }
    }

    public void reportCommonRuleRelationship() {
        System.out.print("******** Reporting common rule ***********\n");
        Set<String> commonRelationship = getCommonRelationship();
        Set<String> rulesToReport = new HashSet<>();
        for (Map.Entry<String, Set<IntelMessageRule>> entry: groupToRules.entrySet()) {
            if (commonRelationship.contains(entry.getKey())) {
                for(IntelMessageRule rule: entry.getValue()) {
                    rulesToReport.add(rule.originalLogKey);
                }
            }
        }
        int ruleSize = indexToRuleMap.size();
        for (int i = 0; i < ruleSize; i++) {
            String rule = indexToRuleMap.get(i);
            if (rulesToReport.contains(rule))
            System.out.print("\t" + rule);
        }
        System.out.println();
        String rowKey;
        String columnKey;
        for (int i = 0; i < ruleSize; i++) {
            rowKey = indexToRuleMap.get(i);
            if (rulesToReport.contains(rowKey)) {
                System.out.print(rowKey + "\t");
                for (int j = 0; j < ruleSize; j++) {
                    columnKey = indexToRuleMap.get(j);
                    if (rulesToReport.contains(columnKey)) {
                        System.out.print(ruleRelationship.get(i).get(j) + "\t");
                    }
                }
                System.out.println();
            }
        }

    }
}
