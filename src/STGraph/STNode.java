package STGraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Eddie on 2018/7/23.
 */
public class STNode {
    String group;

    public Set<String> remainingGroups;

    public Map<String, STNode> directChildrenGroups;

    public Map<String, STNode> directAfterGroups;

    public STNode(String group) {
        this.group = group;
        directChildrenGroups = new HashMap<>();
        directAfterGroups = new HashMap<>();
        remainingGroups = new HashSet<>();
    }
}
