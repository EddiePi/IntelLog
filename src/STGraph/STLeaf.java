package STGraph;

import IntelMessage.IntelMessage;

import java.util.Iterator;

/**
 * Created by Eddie on 2018/7/16.
 */
public class STLeaf extends STComponent {
    IntelMessage intelMessage;

    public STLeaf(IntelMessage message) {
        this.intelMessage = message;
        startTimestamp = message.getTimestamp();
        endTimestamp = startTimestamp;
        groupKey = intelMessage.ruleRef.originalLogKey;
        groupLevel = GroupLevel.LOG;
    }
}
