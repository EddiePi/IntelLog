package STGraph;

import IntelMessage.EntityMap;
import utils.LogUtil;

import java.util.*;

/**
 * Created by Eddie on 2018/7/5.
 */
public class EntityClassifier {

    private EntityMap entityMap = EntityMap.getInstance();
    public Map<String, Set<String>> wordToGroupMap;
    private Map<String, Set<String>> groupToWordMap;

    public EntityClassifier() {
        wordToGroupMap = new HashMap<>();
        groupToWordMap = new HashMap<>();
    }

    public Map<String, List<String>> buildEntityGroup(String entityFilePath) {
        Map<String, List<String>> res = new HashMap<>();
        List<String> allEntities = entityMap.getEntityList(entityFilePath);
        int entityNum = allEntities.size();

        // we reverse the order of entity in short-first order.
        String[] ascendingEntities = new String[entityNum];
        for (int i = 0; i < entityNum; i++) {
            ascendingEntities[entityNum - 1 - i] = allEntities.get(i);
        }

        // this array keeps track of whether an entity is already used.
        int[] added = new int[entityNum];
        for (int i = 0; i < entityNum; i++) {
            String[] shortSeq = ascendingEntities[i].split("\\s+");
            for (int j = i; j < entityNum; j++) {
                String[] longSeq = ascendingEntities[j].split("\\s+");
                if (shortSeq.length == 1 && longSeq.length == 1) {
                    continue;
                } else {
                    findCommonAndGroup(shortSeq, longSeq, added, i, j);
                }

            }
        }

        for (int i = 0; i < added.length; i++) {
            if (added[i] == 0) {
                Set<String> group = new HashSet<>();
                group.add(ascendingEntities[i]);
                wordToGroupMap.put(ascendingEntities[i], group);
            }
        }
        return res;
    }

    /**
     * take a shorter phrase and a longer phrase. Find whether they have a common sub-string.
     *
     * @param shortSeq
     * @param longSeq
     */
    private void findCommonAndGroup (String[] shortSeq, String[] longSeq, int[] added, int shortIndex, int longIndex) {
        if (shortSeq.length == 1) {
            if (added[shortIndex] == 0) {
                updatePhraseGroup(shortSeq[0], shortSeq[0], added, shortIndex);
            }
            for (String word: longSeq) {
                if (shortSeq[0].equals(word)) {
                    updatePhraseGroup(LogUtil.spliceSequence(longSeq), shortSeq[0], added, longIndex);
                    break;
                }
            }
        } else {
            // if we are in the 'else' part, it means there is no one-word group in both the short entity and the long entity
            // in this case, we assign the longest common string as the group of this two entity.
            int comparingLength;
            boolean found = false;
            String longPhrase = LogUtil.spliceSequence(longSeq);
            String shortPhrase = LogUtil.spliceSequence(shortSeq);
            for (comparingLength = shortSeq.length; comparingLength >= 1; comparingLength--) {
                for (int sIndex = 0; sIndex + comparingLength <= shortSeq.length; sIndex++) {
                    String maybeCommonPhrase = LogUtil.spliceSequence(shortSeq, sIndex, sIndex + comparingLength);
                    if (LogUtil.isSubPhrase(maybeCommonPhrase, longPhrase)) {
                        found = true;
                        updatePhraseGroup(shortPhrase, maybeCommonPhrase, added, shortIndex);
                        updatePhraseGroup(longPhrase, maybeCommonPhrase, added, longIndex);

                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
        }
    }

    private void updatePhraseGroup(String phrase, String groupName, int[] added, int index) {
        Set<String> entitySet = wordToGroupMap.get(phrase);
        if (entitySet == null) {
            entitySet = new HashSet<>();
            wordToGroupMap.put(phrase, entitySet);
        }
        if (hasShorterGroupName(phrase, groupName)) {
            return;
        }
        List<String> longerEntityList = getLongerGroupName(phrase, groupName);
        entitySet.removeAll(longerEntityList);
        //entitySet = wordToGroupMap.get(groupName);
        entitySet.add(groupName);
        added[index] += 1;
    }

    private boolean hasShorterGroupName(String phrase, String groupName) {
        Set<String> entitySet = wordToGroupMap.get(phrase);
        for (String entity: entitySet) {
            if (LogUtil.isSubPhrase(entity, groupName)) {
                return true;
            }
        }
        return false;
    }

    private List<String> getLongerGroupName(String phrase, String groupName) {
        Set<String> entitySet = wordToGroupMap.get(phrase);
        List<String> longerList = new ArrayList<>();
        if (entitySet == null) {
            return longerList;
        }
        for (String entity: entitySet) {
            if (LogUtil.isSubPhrase(groupName, entity)) {
                longerList.add(entity);
            }
        }
        return longerList;
    }



    public void reverseIndex() {
        for (Map.Entry<String, Set<String>> entry: wordToGroupMap.entrySet()) {
            String key = entry.getKey();
            Set<String> value = entry.getValue();

            for (String entity : value) {
                Set<String> entitySet = groupToWordMap.get(entity);
                if (entitySet == null) {
                    entitySet = new HashSet<>();
                    groupToWordMap.put(entity, entitySet);
                }
                //entitySet = groupToWordMap.get(entity);
                entitySet.add(key);
            }
        }
    }

    public void report() {
        System.out.println("word to group");
        for (Map.Entry<String, Set<String>> entry: wordToGroupMap.entrySet()) {
            System.out.printf("entity: %s , group: %s\n", entry.getKey(), entry.getValue().toString());
        }

        System.out.println("\ngroup to word");
        for (Map.Entry<String, Set<String>> entry: groupToWordMap.entrySet()) {
            System.out.printf("group: %s , entities: %s\n", entry.getKey(), entry.getValue().toString());
        }
    }
}
