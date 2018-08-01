package IntelMessage.LogFormatter;

import IntelMessage.*;
import com.sun.istack.internal.Nullable;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import spell.ForceReplaceMap;
import utils.GsonSerializer;
import org.apache.logging.log4j.*;
import utils.LogUtil;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Eddie on 2018/7/9.
 */
public abstract class AbstractFormatter {
    private static Queue<IntelMessage> parsedIntelMessageQueue = new LinkedBlockingQueue<IntelMessage>();
    private static Logger logger = LogManager.getLogger(AbstractFormatter.class);

    protected File logFile;
    private LogReaderRunnable logReader;
    private Thread logReaderThread;
    private List<IntelMessageRule> ruleList;
    private BufferedReader br;
    private List<String> ignoreRuleSet;

    public AbstractFormatter(String filePath, String ruleFilePath) {
        try {
            setLogFile(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        IntelMessageRuleList intelMessageRuleList = RuleListSingleton.getInstance().getIntelMessageRuleList();
        ruleList = intelMessageRuleList.intelMessageRules;
        ignoreRuleSet = IgnoreRules.getInstance().ignoreRules;
    }

    public AbstractFormatter(File logFile, IntelMessageRuleList intelMessageRuleList) {
        try {
            setLogFile(logFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ruleList = intelMessageRuleList.intelMessageRules;
        ignoreRuleSet = IgnoreRules.getInstance().ignoreRules;
    }

    /**
     * The turn the complete IntelMessage
     *
     * @return IntelMessage
     */
    public IntelMessage asyncGetIntelMessage() {
        return parsedIntelMessageQueue.poll();
    }

    public IntelMessage syncGetIntelMessage() {
        IntelMessage newMessage = null;
        try {
            String line;
                while ((line = br.readLine()) != null) {
                    //logger.debug("parsing log: " + line);
                    newMessage = format(line);
//                    if (line.matches(".*about to shuffle.*")) {
//                        System.out.print("STOP");
//                    }
                    boolean ignore = false;
                    for (String subStr: ignoreRuleSet) {
                        if (line.contains(subStr)) {
                            ignore = true;
                            break;
                        }
                    }
                    if (ignore) {
                        continue;
                    }
                    if (newMessage == null) {
                        continue;
                    }
                    newMessage = buildCompleteMessage(newMessage);
                    if (newMessage == null) {
                        logger.warn("log message: " + line + " has no matched rule. Please check existing rules or add new rules");
                        continue;
                    } else {
                        break;
                    }
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newMessage;
    }

    private class LogReaderRunnable implements Runnable {

        File logFile;
        boolean isRunning = true;

        public LogReaderRunnable(File logFile) {
            this.logFile = logFile;
        }


        @Override
        public void run() {
            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(logFile));
                String line;
                while (isRunning) {
                    while ((line = br.readLine()) != null) {
                        IntelMessage newMessage = format(line);
                        if (newMessage == null) {
                            continue;
                        }
                        newMessage = buildCompleteMessage(newMessage);
                        if (newMessage == null) {
                            logger.warn("log message: " + line + " has no matched rule. Please check existing rules or add new rules");
                            continue;
                        } else {
                            parsedIntelMessageQueue.offer(newMessage);
                        }
                    }
                    Thread.sleep(1000);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void stop() {
            isRunning = false;
        }
    }

    /**
     * This abstract method uses template design pattern.
     * The format method is written for a specific log format.
     * @param logMessage
     * @return IntelMessage which contains only the 1) timestamp; 2) original log content; 3) additional identifiers
     */
    protected abstract IntelMessage format(String logMessage);

    /**
     * this method should be called after <code>format</code>
     * this method includes finding the corresponding IntelMessageRule
     * public for test
     * @param message
     * @return
     */
    public IntelMessage buildCompleteMessage(IntelMessage message) {
        IntelMessageRule rule = getIntelMessageRule(message.getOriginalLog());
        if (rule == null) {
            rule = getIntelMessageRuleFromRegex(message.getOriginalLog());
        }
        if (rule == null) {
            return null;
        }
        message.ruleRef = rule;
        // build the identifiers, value and location in one pass
        String[] contentSeq  = message.getOriginalLog().split("\\s+");
        String[] POLSeq = new String[rule.POLSequence.size()];
        rule.POLSequence.toArray(POLSeq);
        int seqLength = contentSeq.length;
        int index = 0;
        int positionEnd;
        while (index < seqLength) {
            String currentPOL = POLSeq[index];
            String endPOL = "";
            if (currentPOL.equals("CON")) {
                index++;
                continue;
            } else if (currentPOL.equals("LOC")) {
                endPOL = "_LOC";
            } else if (currentPOL.equals("VAL")) {
                endPOL = "_VAL";
            } else if (currentPOL.equals("ID")) {
                endPOL = "_ID";
            }
            positionEnd = index;
            while (positionEnd < seqLength - 1) {
                if (POLSeq[positionEnd + 1].equals(endPOL)) {
                    positionEnd++;
                } else {
                    break;
                }
            }
            //TEST
//            if (contentSeq.length >= 3 && contentSeq[0].equals("Task") && contentSeq[2].equals("done")) {
//                System.out.print("stop");
//            }
            String splicedVariable = LogUtil.spliceSequence(contentSeq, index, positionEnd + 1);
            if (currentPOL.equals("LOC")) {
                message.addLocation(splicedVariable);
            } else if (currentPOL.equals("VAL")) {
                message.addValue(splicedVariable);
            } else if (currentPOL.equals("ID")) {
                String[] name_value = LogUtil.idToTypeAndValue(splicedVariable);
                message.addIdentifier(name_value[0], name_value[1]);
            }
            index = positionEnd + 1;
        }
        // TEST
//        if (message.originalLog.contains("about to shuffle")) {
//            System.out.print("STOP");
//        }
        if (rule.exampleMessage == null) {
            rule.exampleMessage = message;
        }
        return message;
    }

    public void setLogFilePath(String filePath) {
        logFile = new File(filePath);
    }

    /**
     * this method is only called in <code>buildCompleteMessage</code>
     * @param logContent
     * @return <code>IntelMessageRule</code> if it is found; otherwise, return <code>null</code>
     */
    @Nullable
    private IntelMessageRule getIntelMessageRuleFromRegex(String logContent) {
        if (logContent.trim().length() == 0) {
            return null;
        }
        IntelMessageRule matchedRule = null;

        String replacedLog = logContent.toLowerCase();

        for (IntelMessageRule rule: ruleList) {
            Pattern pattern = Pattern.compile(rule.keyedMessageRule.regex);
            Matcher matcher = pattern.matcher(replacedLog);
            if (matcher.matches()) {
                matchedRule = rule;
                break;
            }
        }

        return matchedRule;
    }


    /**
     * this method is only called in <code>buildCompleteMessage</code>
     * @param logContent
     * @return <code>IntelMessageRule</code> if it is found; otherwise, return <code>null</code>
     */
    @Nullable
    private IntelMessageRule getIntelMessageRule(String logContent) {
        if (logContent.trim().length() == 0) {
            return null;
        }

        String replacedLog = logContent;
        for (Map.Entry<String, String> strToRplc: ForceReplaceMap.getInstance().idReplaceMap.entrySet()) {
            String origin = strToRplc.getKey();
            String target = strToRplc.getValue();
            replacedLog = replacedLog.replaceAll(origin, target);
        }

        // this is the sequence of the original log content with punctuation replaced
        String contentSeq[] = logContent.trim().split("\\s+");
        String replacedSeq[] = replacedLog.trim().split("\\s+");
        IntelMessageRule bestMatch = null;
        int bestMatchLength = 0;
        //Find LCS of all existing LCSObjects and determine if they're a match as described in the paper
        for(IntelMessageRule rule: ruleList) {
            List<String> tokenSeq = rule.tokenSequence;
            List<String> POLSeq = rule.POLSequence;
            if (POLSeq.size() == contentSeq.length) {
                int count = 0;
                int tokenIndex = 0;
                int seqIndex = 0;
                int tokenLength = tokenSeq.size();
                int replacedLength = replacedSeq.length;
                while (tokenIndex < tokenLength && seqIndex < replacedLength) {
                    if (tokenSeq.get(tokenIndex).equals("*")) {
                        count++;
                        // if we encounter a "*", we should find the next matched token
                        if (tokenIndex < tokenLength - 1) {
                            tokenIndex++;
                            String nextToken = tokenSeq.get(tokenIndex);
                            seqIndex++;
                            while (seqIndex < replacedLength) {
                                if (replacedSeq[seqIndex].equals(nextToken)) {
                                    break;
                                }
                                seqIndex++;
                            }
                        } else {
                            break;
                        }
                    } else if (tokenSeq.get(tokenIndex).equals(replacedSeq[seqIndex])) {
                        count++;
                        tokenIndex++;
                        seqIndex++;
                    } else {
                        seqIndex++;
                    }
                }
                if (count == tokenLength) {
                    bestMatch = rule;
                    break;
                }
                if (count > bestMatchLength && count > tokenLength / 1.7) {
                    bestMatch = rule;
                    bestMatchLength = count;
                }
            }
        }

        return bestMatch;
    }

    public void start() {
        logReader = new LogReaderRunnable(logFile);
        logReaderThread = new Thread(logReader);
        logReaderThread.start();
    }

    public void stop() {
        logReader.stop();
    }

    public void setLogFile(String filePath) throws FileNotFoundException {
        setLogFile(new File(filePath));

    }

    public void setLogFile(File file) throws FileNotFoundException {
        this.logFile = file;
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        try {
            br = new BufferedReader(new FileReader(logFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setRuleList(IntelMessageRuleList ruleList) {
        this.ruleList = ruleList.intelMessageRules;
    }

    public void setRuleList(String rulePath) {
        IntelMessageRuleList intelMessageRuleList = GsonSerializer.readJSON(IntelMessageRuleList.class, rulePath);
        this.ruleList = intelMessageRuleList.intelMessageRules;
    }
}
