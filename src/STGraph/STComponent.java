package STGraph;

import IntelMessage.IntelMessage;
import IntelMessage.IntelMessageRule;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by Eddie on 2018/7/5.
 */
public abstract class STComponent {

    public String groupKey;
    public GroupLevel groupLevel;

    // these two fields vary when parsing each file and should be determined at runtime
    public long startTimestamp = -1;
    public long endTimestamp = -1;

    /**
     * update the timestamp according to the new <code>IntelMessage</code>
     * @param message
     */
    public void addIntelMessage(IntelMessage message) {
        long messageTimestamp = message.getTimestamp();
        if (startTimestamp != -1) {
            startTimestamp = Math.min(messageTimestamp, startTimestamp);
        } else {
            startTimestamp = messageTimestamp;
        }

        if (endTimestamp != -1) {
            endTimestamp = Math.max(messageTimestamp, endTimestamp);
        } else {
            endTimestamp = messageTimestamp;
        }
    }

    public void resetTimestamp() {
        startTimestamp = -1;
        endTimestamp = -1;
    }
}
