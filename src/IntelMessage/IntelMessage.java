package IntelMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is built for EACH matched log message
 *
 */
public class IntelMessage implements Serializable {
    public String originalLog;
    public Map<String, String> identifiers;
    public List<String> locationList;
    public List<String> valueList;
    public Long timestamp;

    /** Intel Message Rule Ref includes
     * 1) the original log key
     * 2) the entity set
     * 3) the operations
     *
     */
    public IntelMessageRule ruleRef;

    public IntelMessage() {
        identifiers = new HashMap<>();
    }


    // all getter and setter functions
    public void setOriginalLog(String log) {
        this.originalLog = log;
    }

    public String getOriginalLog() {
        return originalLog;
    }

    public void addIdentifier(String type, String value) {
        identifiers.put(type, value);
    }

    public Map<String, String> getIdentifiers() {
        if (identifiers == null) {
            identifiers = new HashMap<>();
        }
        return identifiers;
    }

    public void addLocation(String location) {
        if (locationList == null) {
            locationList = new ArrayList<>();
        }
        locationList.add(location);
    }

    public List<String> getLocationList() {
        if (locationList == null) {
            locationList = new ArrayList<>();
        }
        return locationList;
    }

    public void addValue(String value) {
        if (valueList == null) {
            valueList = new ArrayList<>();
        }
        valueList.add(value);
    }

    public List<String> getValueList() {
        if (valueList == null) {
            valueList = new ArrayList<>();
        }
        return valueList;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public IntelMessageRule getRuleRef() {
        return ruleRef;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("original log: ").append(originalLog).append("\n");


        return stringBuilder.toString();
    }

}
