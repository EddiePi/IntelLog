package STGraph;

import IntelMessage.IntelMessage;
import IntelMessage.IntelMessageRule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.LogUtil;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Eddie on 2018/7/31.
 */
public class SignatureRelationship implements Serializable {

    static Logger logger = LogManager.getLogger();
    public Set<String> idSignature;

    private Set<IntelMessageRule> includedRules;

    // critical rules are rules which are in every session
    private Set<IntelMessageRule> criticalRules;

    public SignatureRelationship() {
        idSignature = new HashSet<>();
        includedRules = new HashSet<>();
        criticalRules = new HashSet<>();
    }

    public static Set<String> getSignatureSet (IntelMessage message) {
        Set<String> idTypes = new HashSet<>();
        if (message.identifiers.size() == 0) {
            return idTypes;
        }

        for (String str: message.identifiers.keySet()) {
            idTypes.add(str);
        }

        return idTypes;
    }

    public static Set<String> getSignatureSet(IntelMessageRule rule) {
        if (rule.exampleMessage == null) {
            logger.warn("rule: " + rule.originalLogKey + " does not has an example message");
            return null;
        }
        return getSignatureSet(rule.exampleMessage);
    }

    public void addIncludedRule(IntelMessageRule rule) {
        includedRules.add(rule);
        if (InfoPackage.getInstance().criticalRules.contains(rule)) {
            criticalRules.add(rule);
        }
    }

    public Set<IntelMessageRule> getIncludedRules() {
        return includedRules;
    }

    public Set<IntelMessageRule> getCriticalRules() {
        return criticalRules;
    }
}
