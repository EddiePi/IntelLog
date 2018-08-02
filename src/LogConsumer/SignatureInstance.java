package LogConsumer;

import IntelMessage.*;
import STGraph.SignatureRelationship;

import java.util.*;

/**
 * Created by Eddie on 2018/8/1.
 */
public class SignatureInstance {

    SignatureRelationship templateMessageRules;
    public Set<String> signature;
    List<IntelMessage> consumedMessage;
    Map<String, Integer> consumedCount;

    public SignatureInstance (SignatureRelationship signatureRules) {
        templateMessageRules = signatureRules;
        consumedMessage = new ArrayList<>();
        signature = new HashSet<>();
        consumedCount = new HashMap<>();
        for (IntelMessageRule rule: templateMessageRules.includedRules) {
            consumedCount.put(rule.originalLogKey, 0);
        }
    }

    public void addMessage(IntelMessage message, Set<String> ids) {
        consumedMessage.add(message);
        consumedCount.computeIfPresent(message.ruleRef.originalLogKey, (k, v) -> v + 1);
        if (ids.containsAll(signature)) {
            signature.addAll(ids);
        }
    }

    //TODO: tihs method is used to do auto anomaly check
    public boolean checkMessages() {
        boolean isCorrect = true;
        return isCorrect;
    }
}
