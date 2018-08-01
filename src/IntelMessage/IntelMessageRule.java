package IntelMessage;

import KeyedMessage.KeyedMessageRule;
import edu.stanford.nlp.trees.TypedDependency;
import spell.LCSMap;
import spell.LCSObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * We do not include the original message. This class is used to build the model.
 * This message include:
 * 1) A log message
 * 2) A token and a POS sequence for the log message
 * 3) Entities in the log message
 * 4) Location information
 * 5) operation extracted by structure parser
 * 6) ... a relationship between other object
 */
public class IntelMessageRule implements Serializable {
    public String originalLogKey;
    public KeyedMessageRule keyedMessageRule;
    public List<String> tokenSequence;
    public List<String> POSSequence;
    // the length of POLSequence equals to real log content
    public List<String> POLSequence;
    public List<String> entities;
    public List<Operation> operations;
    public Set<String> entityGroups;

    // this is to build sub-routine
    public IntelMessage exampleMessage = null;


    @Override
    public String toString() {
        String res;
        res = "Log Key: " + originalLogKey + "\n";
        res += "Keyed Message Rule: " + keyedMessageRule.toString() + "\n";
        res += "tokenSeq: " + tokenSequence.toString() + "\n";
        res += "POSSeq: " + POSSequence.toString() + "\n";
        res += "POLSeq: " + POLSequence.toString() + "\n";
        res += "Entities: " + entities.toString() + "\n";
        res += "Operations: " + operations.toString() + "\n";

        return res;
    }


}
