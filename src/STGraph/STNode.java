package STGraph;

import IntelMessage.IntelMessage;
import IntelMessage.IntelMessageRule;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Eddie on 2018/7/23.
 */
public class STNode implements Serializable{
    public String group;

    public Set<SignatureRelationship> signatureSet;

    public transient Set<String> remainingGroups;

    public Map<String, STNode> directChildrenGroups;

    public Map<String, STNode> directAfterGroups;

    public STNode(String group) {
        this.group = group;
        directChildrenGroups = new HashMap<>();
        directAfterGroups = new HashMap<>();
        remainingGroups = new HashSet<>();
        signatureSet = new HashSet<>();

        // none-sig set init
        SignatureRelationship noneRelationship = new SignatureRelationship();
        noneRelationship.idSignature.add("NONE");
        signatureSet.add(noneRelationship);
    }

    public void updateSignatureSet(IntelMessageRule rule) {
        Set<String> curSignature = SignatureRelationship.getSignatureSet(rule);
        if (curSignature == null) {
            return;
        }
        if (curSignature.size() == 0) {
            for (SignatureRelationship relationship: signatureSet) {
                if (relationship.idSignature.contains("NONE") && relationship.idSignature.size() == 1) {
                    relationship.addIncludedRule(rule);
                    return;
                }
            }
        }
        boolean found = false;
        for (SignatureRelationship relationship: signatureSet) {
            if (relationship.idSignature.containsAll(curSignature)) {
                relationship.addIncludedRule(rule);
                found = true;
                continue;
            }
            if (curSignature.containsAll(relationship.idSignature)) {
                relationship.idSignature = curSignature;
                relationship.addIncludedRule(rule);
                found = true;
                continue;
            }
            for (String group: curSignature) {
                if (relationship.idSignature.contains(group)) {
                    relationship.addIncludedRule(rule);
                    break;
                }
            }
        }
        if (!found) {
            SignatureRelationship newRelationship = new SignatureRelationship();
            newRelationship.addIncludedRule(rule);
            newRelationship.idSignature = curSignature;
            signatureSet.add(newRelationship);
        }
    }
}
