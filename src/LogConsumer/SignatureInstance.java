package LogConsumer;

import IntelMessage.*;
import STGraph.InfoPackage;
import STGraph.SignatureRelationship;

import java.util.*;

/**
 * Created by Eddie on 2018/8/1.
 */
public class SignatureInstance {

    SignatureRelationship templateMessageRules;
    String group;
    public Set<String> signature;
    List<IntelMessage> consumedMessage;
    Map<IntelMessageRule, Integer> consumedCount;

    public SignatureInstance (SignatureRelationship signatureRules, String group) {
        templateMessageRules = signatureRules;
        this.group = group;
        consumedMessage = new ArrayList<>();
        signature = new HashSet<>();
        consumedCount = new HashMap<>();
        for (IntelMessageRule rule: templateMessageRules.getIncludedRules()) {
            consumedCount.put(rule, 0);
        }
    }

    public void addMessage(IntelMessage message, Set<String> ids) {
        consumedMessage.add(message);
        consumedCount.computeIfPresent(message.ruleRef, (k, v) -> v + 1);
        if (ids.containsAll(signature)) {
            signature.addAll(ids);
        }
    }

    //TODO: this method is used to do auto anomaly check
    public boolean checkMessages() {
        boolean isCorrect = true;
        Set<IntelMessageRule> allCriticalRule = InfoPackage.getInstance().criticalRules;
        // check all critical rules
        for (Map.Entry<IntelMessageRule, Integer> entry: consumedCount.entrySet()) {
            IntelMessageRule rule = entry.getKey();
            Integer count = entry.getValue();
            if (allCriticalRule.contains(rule)) {
                if (count == 0) {
                    System.out.printf("subroutine: %s missed a log: %s\n", group, rule.originalLogKey);
                }
            }
        }
        return isCorrect;
    }
}
