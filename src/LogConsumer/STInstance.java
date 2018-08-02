package LogConsumer;

import IntelMessage.IntelMessage;
import IntelMessage.IntelMessageRule;
import STGraph.STNode;
import STGraph.SignatureRelationship;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Eddie on 2018/8/1.
 */
public class STInstance {
    public String group;

    public Set<SignatureRelationship> signatureSet;

    Map<SignatureRelationship, SigInstanceManager> signatureInstanceMap;

    public STInstance(String group, Set<SignatureRelationship> signatureSet) {
        this.group = group;
        this.signatureSet = signatureSet;
        signatureInstanceMap = new HashMap<>();
        for (SignatureRelationship relationship: signatureSet) {
            signatureInstanceMap.put(relationship, new SigInstanceManager(relationship, group));
        }
    }

    public void addMessage(IntelMessage message) {
        for (Map.Entry<SignatureRelationship, SigInstanceManager> entry: signatureInstanceMap.entrySet()) {
            SignatureRelationship key = entry.getKey();
            SigInstanceManager value = entry.getValue();
            for (IntelMessageRule rule: key.getIncludedRules()) {
                if (rule.equals(message.ruleRef)) {
                    value.addMessage(message);
                }
            }
        }
    }

    public void report() {
        StringBuilder builder = new StringBuilder("");
        System.out.printf("group: %s\n", group);
        for (SigInstanceManager manager: signatureInstanceMap.values()) {
            manager.report();
        }

    }
}
