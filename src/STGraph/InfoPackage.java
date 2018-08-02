package STGraph;

import IntelMessage.IntelMessageRule;

import java.util.*;

/**
 * Created by Eddie on 2018/8/2.
 */
public class InfoPackage {

    private static InfoPackage instance;

    public static InfoPackage getInstance() {
        if (instance == null) {
            instance = new InfoPackage();
        }
        return instance;
    }

    public Set<String> commonGroup;

    public Set<IntelMessageRule> criticalRules;

    public ArrayList<ArrayList<HierarchyType>> ruleRelationship;

    // rule name to index map
    public Map<String, Integer> ruleToIndexMap;

    private InfoPackage() {
        criticalRules = new HashSet<>();
    }
}
