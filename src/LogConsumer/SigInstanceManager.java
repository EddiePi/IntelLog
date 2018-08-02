package LogConsumer;

import IntelMessage.IntelMessage;
import STGraph.SignatureRelationship;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Eddie on 2018/8/1.
 */
public class SigInstanceManager {
    SignatureRelationship relationship;

    Map<Set<String>, SignatureInstance> instanceMap;

    public SigInstanceManager(SignatureRelationship relationship) {
        this.relationship = relationship;
        instanceMap = new HashMap<>();
    }

    public void addMessage(IntelMessage message) {
        Set<String> extractedId = extractIds(message);
        boolean found = false;
        for (Map.Entry<Set<String>, SignatureInstance> entry: instanceMap.entrySet()) {
            if (extractedId.containsAll(entry.getKey())) {
                entry.getKey().addAll(extractedId);
                entry.getValue().addMessage(message, extractedId);
                found = true;
                break;
            }
        }
        if (!found) {
            SignatureInstance instance = new SignatureInstance(relationship);
            instance.addMessage(message, extractedId);
            instanceMap.put(extractedId, instance);
        }
    }

    private Set<String> extractIds(IntelMessage message) {
        String id;
        Set<String> extractedId = new HashSet<>();
        Map<String, String> messageIdSet = message.getIdentifiers();
        if (messageIdSet.size() == 0) {
            extractedId.add("NONE");
            return extractedId;
        }
        for (String idType: relationship.idSignature) {
            id = messageIdSet.get(idType);
            if (id != null) {
                extractedId.add(id);
            }
        }
        return extractedId;
    }
}
