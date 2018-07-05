package Log;

import spell.ForceReplaceMap;

import java.io.*;
import java.util.Map;

/**
 * All raw log message should be read by this Class. No matter for model construction or analysis.
 */
public class LogPreprocessor {

    File logFile;
    BufferedReader br;
    boolean EOF = false;

    public LogPreprocessor(String filePath) {
        this.logFile = new File(filePath);
        resetFilePosition();
    }

    public LogPreprocessor(File file) {
        this.logFile = file;
        resetFilePosition();
    }


    public String getNextLine() {
        String line = "";
        try {
            line = br.readLine();
            if (line == null) {
                EOF = true;
            } else {
                line = preProcess(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }

    private void resetFilePosition() {
        try {
            br = new BufferedReader(new FileReader(logFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String preProcess(String logMessage) {
        String line = logMessage;
        line = addSpace(line);
        line = removeEndingDot(line);

        return line;
    }

    private static String addSpace(String logMessage) {
        String res = logMessage;
        if (res == null) {
            System.out.print("stop\n");
        }
        Map<String, String> replaceMap = ForceReplaceMap.getInstance().punctReplaceMap;
        for (Map.Entry<String, String> strToRplc: replaceMap.entrySet()) {
            String origin = strToRplc.getKey();
            String target = strToRplc.getValue();
            res = res.replaceAll(origin, target);
            res = res.replaceAll("\\s+", " ").trim();
        }
        return res;
    }

    private static String removeEndingDot(String logMessage) {
        String line = logMessage.trim();
        if (line == null) {
            return line;
        }
        if (line.length() == 0) {
            return line;
        }

        int length;
        while (true) {
            length = line.length();
            if (line.charAt(length - 1) == '.') {
                line = line.substring(0, length - 1);
            } else {
                break;
            }
        }
        return line;
    }
}
