package utils;

import NPL.Lemmatizer;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Eddie on 2018/6/6.
 */
public class LogUtil {

    /**
     *
     * @param logSeq
     * @param wordIndex
     * @param entityList
     * @return
     */
    public static EntityInLog findEntity(String[] logSeq, Integer wordIndex, List<String> entityList) {
        String log = "";
        EntityInLog targetEntity = new EntityInLog();
        String indexedWord;
        String lemmatizedWord;

        // splice the words in to a sentence
        for (String word: logSeq) {
            log += word + " ";
        }
        log = log.trim().toLowerCase();
        // if the word is '*', we subtract it by 1
        if ((logSeq[wordIndex].equals("*") || isKeyWordVariable(logSeq[wordIndex])) && wordIndex > 0) {
            do {
                wordIndex--;
            } while (!logSeq[wordIndex].matches(".*[a-zA-Z]+.*") && wordIndex > 0);
        }
        indexedWord = logSeq[wordIndex].toLowerCase();
        lemmatizedWord = Lemmatizer.mayBeLemmatizeToSingular(indexedWord);

        int entityIndex = -1;
        int entityLength;

        // TEST
//        if (indexedWord.equals("driver")) {
//            System.out.print("stop\n");
//        }

        // we go through the entity set
        for (String entity: entityList) {
            // if the entity has multiple words, we splice it and test whether it appears in the logSeq
            String[] words = entity.split("\\s+");
            if (words.length > 0) {
                String spliced = "";
                for (String word: words) {
                    spliced += word;
                }
                spliced = spliced.toLowerCase();
                if (spliced.equals(indexedWord)) {
                    targetEntity.entity = entity;
                    targetEntity.startPosition = wordIndex;
                    targetEntity.endPosition = wordIndex + 1;
                    targetEntity.isCamel = true;
                    break;
                }
            }

            entityLength = entity.split("\\s+").length;
            String subLog = subPhrase(logSeq, wordIndex - entityLength + 1, wordIndex + entityLength);
            entityIndex = subLog.indexOf(entity);

            if (entityIndex >= 0 && (entity.contains(indexedWord) || entity.contains(lemmatizedWord))) {
                // if the log sequence contains the entity && the entity contains the word, we splice the phrase
                targetEntity.entity = entity;
                targetEntity.startPosition = fineWordIndex(subLog, entityIndex) + Math.max(0, wordIndex - entityLength + 1);
                targetEntity.endPosition = targetEntity.startPosition + entityLength;
                targetEntity.isCamel = false;
                break;

            }
        }
        return targetEntity;
    }

    public static List<String> findAllEntities(String[] keySeq, List<String> entityList) {
        List<String> res = new ArrayList<>();
        int keyLength = keySeq.length;
        for (int startIndex = 0; startIndex < keyLength; startIndex++) {
            if (isCamelFormat(keySeq[startIndex])) {
                String[] splitedCamel = splitCamelWord(keySeq[startIndex]);
                String words = spliceSequence(splitedCamel);
                res.add(words);
                continue;
            }
            for (String entity: entityList) {
                String[] entitySeq = entity.split("\\s+");
                int entityLength = entitySeq.length;
                if (startIndex + entityLength > keyLength) {
                    continue;
                }
                int entityIndex;
                for (entityIndex = 0; entityIndex < entityLength; entityIndex++) {
                    if (keySeq[startIndex + entityIndex].toLowerCase().equals(entitySeq[entityIndex])) {
                        continue;
                    } else {
                        break;
                    }
                }
                if (entityIndex == entityLength) {
                    res.add(entity);
                    startIndex += entityLength - 1;
                    break;
                }
            }
        }

        return res;
    }

    public static boolean isCamelFormat(String word) {
        if (word.matches(".*[a-z]+[A-Z]+.*")) {
            return true;
        }
        return false;
    }

    public static String[] splitCamelWord(String camelWord) {
        List<String> wordList = new ArrayList<>();
        int length = camelWord.length();
        int wordStart = 0;
        for (int i = 1; i < length; i++) {
            char curChar = camelWord.charAt(i);
            if (curChar >= 'A' && curChar <= 'Z') {
                wordList.add(camelWord.substring(wordStart, i).toLowerCase());
                wordStart = i;
            }
        }
        wordList.add(camelWord.substring(wordStart).toLowerCase());

        String[] res = new String[wordList.size()];
        return wordList.toArray(res);
    }

    public static String subPhrase (String[] sequence, int start, int end) {
        // sanity check
        int length = sequence.length;
        String res = "";
        start = Math.max(0, start);
        end = Math.min(length, end);
        if (start < length && end >= start) {
            for (int index = start; index < end; index++) {
                res += sequence[index] + " ";
            }
            res = res.trim();
            return res;

        } else {
            System.out.print("index out of boundary");
            return null;
        }
    }

    public static boolean isSubPhrase(String shorter, String longer) {
        String[] shorterSeq = shorter.split("\\s+");
        String[] longerSeq = longer.split("\\s+");
        int shorterLen = shorterSeq.length;
        int longerLen = longerSeq.length;
        if (shorterLen > longerLen) {
            return false;
        }

        for (int i = 0; i <= longerLen - shorterLen; i++) {
            int j;
            for (j = 0; j < shorterLen; j++) {
                if (shorterSeq[j].equals(longerSeq[i + j])) {
                    continue;
                } else {
                    break;
                }
            }
            if (j == shorterLen) {
                return true;
            }
        }
        return false;
    }

    public static String spliceSequence(String[] seq) {
        String res = "";
        for(int i = 0; i < seq.length; i++) {
            res += seq[i] + " ";
        }
        res = res.trim();
        return res;
    }

    /**
     * splice the sequence from the start index (inclusive) to the end index (exclusive)
     * @param seq
     * @param start
     * @param end
     * @return the spliced string
     */
    public static String spliceSequence(String[] seq, int start, int end) {
        String res = "";
        if (start >= 0 && start <= end && end <= seq.length) {
            for (int i = start; i < end; i++) {
                res += seq[i] + " ";
            }
            res = res.trim();
        }
        return res;
    }

    /**
     * provide the string a index, return the position of the word where the index is
     *
     * @param str
     * @param charIndex
     * @return
     */
    public static Integer fineWordIndex(String str, int charIndex) {
        int endIndex = Math.min(str.length(), charIndex);
        boolean lastSpace = false;
        Integer wordIndex = 0;
        for (int i = 0; i < endIndex; i++) {
            if (str.charAt(i) == ' ') {
                if (!lastSpace) {
                    wordIndex++;
                }
                lastSpace = true;
            } else {
                lastSpace = false;
            }
        }
        return wordIndex;
    }

    public static List<String> loadFile(File file) {
        List<String> res = new ArrayList<>();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                res.add(line.trim());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static Boolean isKeyWordVariable(String keyWord) {
        Boolean res = false;
        if (keyWord.contains("*")) {
            res = true;
        }
        if (keyWord.matches("container_d\\+.*") ||
                keyWord.matches("application_d\\+.*") ||
                keyWord.matches("appattempt_d\\+.*") ||
                keyWord.matches("attempt_d\\+.*") ||
                keyWord.matches("BlockManagerId\\*")) {
            res = true;
        }
        return res;
    }

    public static String[] idToTypeAndValue(String id) {

        String[] res = new String[2];
        int length = id.length();
        if (length == 0) {
            return res;
        }
        if (id.charAt(0) == '\'') {
            id = id.substring(1, length);
            length = id.length();
        }
        if (id.charAt(length - 1) == '\'') {
            id = id.substring(0, length - 1);
            length = id.length();
        }

        int typeEndIndex;
        int valueStartIndex;
        char curChar;
        for (typeEndIndex = 0; typeEndIndex < length; typeEndIndex++) {
            curChar = id.charAt(typeEndIndex);
            if (curChar >= 'a' && curChar <= 'z' || curChar >= 'A' && curChar <= 'Z' || curChar == ' ') {
                continue;
            } else {
                break;
            }
        }
        valueStartIndex = typeEndIndex;
        if (valueStartIndex < length) {
            curChar = id.charAt(valueStartIndex);
            if (curChar != '(' && curChar != '[' && curChar != '{' && !(curChar <= '9' && curChar >= '0')) {
                valueStartIndex++;
            }
        }
        String idType = id.substring(0, typeEndIndex).trim();
        String idValue = id.substring(valueStartIndex).trim();
        res[0] = idType.toLowerCase();
        res[1] = idValue.toLowerCase();

        return res;
    }

    public static String extractAppFromPath(String str) {
        String[] parts = str.split("/");
        String appStr = null;
        String containerStr = null;
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].matches("application_\\d+_\\d+")) {
                appStr = parts[i];
                break;
            }
            if (parts[i].matches("container_\\d+_\\d+_\\d+_\\d+")) {
                containerStr = parts[i];
                break;
            }
        }
        if (appStr != null) {
            return appStr;
        }
        if (containerStr != null) {
            parts = containerStr.split("_");
            StringBuilder builder = new StringBuilder("");
            builder.append("application_").append(parts[1] + "_").append(parts[2]);
            return builder.toString();
        }
        return null;

    }
}
