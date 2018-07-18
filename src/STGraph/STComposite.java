package STGraph;

import IntelMessage.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Created by Eddie on 2018/7/16.
 */
public class STComposite extends STComponent {
    Logger logger = LogManager.getLogger(this.getClass());

    // groupName to STComponent
    Map<String, STComposite> groupedComposite;

    // this field is used to handle real incoming logs, and needs to be reset for each log file
    Map<String, LogComposite> groupToLogMap;

    // record the relationship between each group
    ArrayList<ArrayList<HierarchyType>>  groupRelationship;
    // group name to index map
    Map<Integer, String> groupIndexMap;

    Map<String, Set<IntelMessageRule>> groupToRules;

    public STComposite(String groupName, Map<String, Set<IntelMessageRule>> groupToRules) {
        groupedComposite = new HashMap<>();
        this.groupToRules = groupToRules;
        this.groupKey = groupName;
        groupIndexMap = new HashMap<>();
        int groupSize = groupToRules.size();
        // initialize the relationship matrix
        groupRelationship = new ArrayList<>(groupSize);
        for (int i = 0; i < groupSize; i++) {
            ArrayList<HierarchyType> row = new ArrayList<>(groupSize);
            for (int j = 0; j < groupSize; j++) {
                row.add(HierarchyType.NOT_ASSIGNED);
            }
            groupRelationship.add(row);
        }
        int index = 0;
        for (String childGroupName: groupToRules.keySet()) {
            groupIndexMap.put(index, childGroupName);
            index++;
        }
        reset();
    }

    /**
     * This method is called after each log file (or request) is finished
     */
    public void reset() {
        super.resetTimestamp();
        this.groupToLogMap = new HashMap<>();
    }

    @Override
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

    private boolean hasGroup(IntelMessage message) {
        return message.ruleRef.entityGroups.size() > 0;
    }

    private void addLog(String groupName, IntelMessage message) {
        LogComposite logComposite = groupToLogMap.computeIfAbsent(groupName, k -> new LogComposite());
        logComposite.addIntelMessage(message);
    }

    private void addNewChildGroup(String childGroupName) {
        boolean hasGroup = groupIndexMap.values().contains(childGroupName);
        if (!hasGroup) {
            int index = groupIndexMap.size();
            groupIndexMap.put(index, childGroupName);
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

    public void updateRelationship() {
        logger.debug("updating entity relationship");
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
        logger.debug("updated entity relationship");
    }

    private void setRelationship(int rowIndex, int columnIndex) {
        String rowGroup = groupIndexMap.get(rowIndex);
        String columnGroup = groupIndexMap.get(columnIndex);
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
}
