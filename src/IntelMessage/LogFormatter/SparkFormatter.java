package IntelMessage.LogFormatter;

import IntelMessage.IntelMessage;
import IntelMessage.IntelMessageRuleList;
import IntelMessage.IntelMessageRule;
import spell.ForceReplaceMap;
import utils.LogUtil;

import java.io.File;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Eddie on 2018/7/9.
 */
public class SparkFormatter extends AbstractFormatter {

    Map<String, String> punctReplaceMap;

    public SparkFormatter(String logFilePath, String ruleFilePath) {
        super(logFilePath, ruleFilePath);
        punctReplaceMap = ForceReplaceMap.getInstance().punctReplaceMap;
    }

    public SparkFormatter(File logFile, IntelMessageRuleList intelMessageRuleList) {
        super(logFile, intelMessageRuleList);
        punctReplaceMap = ForceReplaceMap.getInstance().punctReplaceMap;
    }


    /**
     * parse the spark log message
     * @param logMessage from spark log
     * @return IntelMessage which contains 1) timestamp; 2) original log (punctuation with space); 3) container id if there is <code>IntelMessageRule</code> that matches this log message; or null if there is no match
     */
    @Override
    protected IntelMessage format(String logMessage) {
        if (!isOriginalFormat(logMessage)) {
            return null;
        }
        IntelMessage res = new IntelMessage();
        Long timestamp = parseTimestamp(logMessage);
        // we also have to add space to some punctuations on the content
        String content = extractContent(logMessage);
        if (content.length() == 0) {
            return null;
        }
        content = replacePunctuation(content);
        while(content.charAt(content.length() - 1) == '.') {
            content = content.substring(0, content.length() - 1);
        }
        res.setTimestamp(timestamp);
        res.setOriginalLog(content);
        String containerId = parseContainerIdFromPath(logFile.getAbsolutePath());
        if (containerId != null) {
            res.addIdentifier("container", containerId);
        }
        return res;
    }

    public String replacePunctuation(String content) {
        for (Map.Entry<String, String> strToRplc: punctReplaceMap.entrySet()) {
            String origin = strToRplc.getKey();
            String target = strToRplc.getValue();
            content = content.replaceAll(origin, target);
        }
        content = LogUtil.spliceSequence(content.split("\\s+"));
        return content;
    }

    // public for test
    private boolean isOriginalFormat(String logMessage) {
        Pattern pattern = Pattern.compile("([\\d]{2}|[\\d]{4})[/\\-][\\d]{2}[/\\-][\\d]{2}\\s[\\d]{2}:[\\d]{2}:[\\d]{2}([,\\.][\\d]{3})?.*");
        Matcher matcher = pattern.matcher(logMessage);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

    // public for test
    private Long parseTimestamp(String logMessage) {
        String[] words = logMessage.split("\\s+");
        words[0] = words[0].replace('/', '-');
        String year = words[0].split("-")[0];
        if (year.length() < 4) {
            words[0] = "20" + words[0];
        }
        Long timestamp = Timestamp.valueOf(words[0] + " " + words[1].replace(',', '.')).getTime();
        return timestamp;
    }

    // public for test
    private String extractContent(String logMessage) {
        int contentStartIndex;
        int spaceCount = 0;
        String content = "";
        if (logFile.getName().equals("stderr")) {
            for (contentStartIndex = 0; contentStartIndex < logMessage.length(); contentStartIndex++) {
                if (logMessage.charAt(contentStartIndex) == ' ' || logMessage.charAt(contentStartIndex) == '\t') {
                    spaceCount++;
                }
                if (spaceCount == 4 && logFile.getName().equals("stderr")) {
                    break;
                }
            }
            content = logMessage.substring(contentStartIndex + 1);
        } else if (logFile.getName().equals("syslog")) {
            Pattern pattern = Pattern.compile("([\\d]{2}|[\\d]{4})[/\\-][\\d]{2}[/\\-][\\d]{2}\\s[\\d]{2}:[\\d]{2}:[\\d]{2}([,\\.][\\d]{3})? [A-Z]+ \\[[a-zA-Z\\s]+\\] (?<classcontent>.*)");
            Matcher matcher = pattern.matcher(logMessage);
            if (matcher.matches()) {
                String classContent = matcher.group("classcontent");
                contentStartIndex = classContent.indexOf(":");
                content = classContent.substring(contentStartIndex + 2).trim();
            }
        }
        return content;
    }

    private String parseContainerIdFromPath(String path) {
        String[] dirs = path.split("/");
        for (int i = 0; i < dirs.length; i++) {
            if (dirs[i].matches("container_\\d+_\\d+_\\d+_\\d+]")) {
                return dirs[i];
            }
        }
        return null;
    }
}
