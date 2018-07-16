package IntelMessage;

import KeyedMessage.KeyedMessageRule;
import NPL.Lemmatizer;
import NPL.PartOfSpeech;
import NPL.SDToCheck;
import NPL.StructureParser;
import edu.stanford.nlp.trees.TypedDependency;
import spell.LCSMap;
import spell.LCSObject;
import utils.EntityInLog;
import utils.LogUtil;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Eddie on 2018/5/21.
 */
public class IntelMessageRuleList implements Serializable {


    public List<IntelMessageRule> intelMessageRules;


    transient SDToCheck sdToCheck = SDToCheck.getInstance();

    public IntelMessageRuleList() {
        intelMessageRules = new ArrayList<>();
    }

    @Deprecated
    public void buildIntelMessages(LCSMap lcsMap) {
        for (LCSObject lcsObject: lcsMap.getAllLCSObjects()) {
            IntelMessageRule newIntelRule = new IntelMessageRule();
            newIntelRule.tokenSequence = Arrays.asList(lcsObject.getLCSseq());
            String[] POSArr = lcsObject.getPOSseq();
            if (POSArr != null) {
                newIntelRule.POSSequence = Arrays.asList(POSArr);
            }
            KeyedMessageRule newRule = new KeyedMessageRule();
            newRule.buildRule(lcsObject);
            newIntelRule.keyedMessageRule = newRule;

            intelMessageRules.add(newIntelRule);
        }
    }

    private List<Operation> buildOperations(String keySeq[], String[] POSSeq, Collection<TypedDependency> deps, List<String> entityList) {
        List<Operation> operationList = new ArrayList<>();
        for (TypedDependency td: deps) {
            // process the object relationship
            if (sdToCheck.dependencies.contains(td.reln().toString())) {
                String orgGov = td.gov().word().toLowerCase();
                String orgDep = td.dep().word().toLowerCase();
                if (td.gov().word() == null || td.dep().word() == null) {
                    System.out.printf("null gov or dep. gov: %s, dep: %s\n", td.gov().word(), td.dep().word());
                    continue;
                }
                EntityInLog targetEntity;
                int orgDepIndex = td.dep().index() - 1;
                // fine the entity corresponding to the word
                //TODO: we should handle camel case in LogUtil, too
                targetEntity = LogUtil.findEntity(keySeq, orgDepIndex, entityList);

                // lemmatize all words in the entity and verb
                String lemmatizedEntity;
                if (targetEntity.isCamel) {
                    lemmatizedEntity = targetEntity.entity;
                } else {
                    int entityLength = targetEntity.endPosition - targetEntity.startPosition;
                    String[] entitySeq = new String[entityLength];
                    //String[] entityPosSeq = new String[entityLength];
                    for (int i = 0; i < entityLength; i++) {
                        entitySeq[i] = keySeq[i + targetEntity.startPosition];
                        //entityPosSeq[i] = POSSeq[i + targetEntity.startPosition];
                    }
                    String[] lemmatizedEntitySeq = new String[entitySeq.length];
                    for (int i = 0; i < entitySeq.length; i++) {
                        lemmatizedEntitySeq[i] = Lemmatizer.mayBeLemmatizeToSingular(entitySeq[i]);
                    }
                    lemmatizedEntity = LogUtil.spliceSequence(lemmatizedEntitySeq);
                }


                boolean foundOperation = false;
                //find if the entity already exist in the list
                for (Operation operation: operationList) {
                    // if we already have the predicate
                    if (operation.predicate.equals(td.gov().word())) {
                        // if the td is an object relationship, we search whether the predicate has an object
                        if (sdToCheck.objectRelation.contains(td.reln().toString())) {
                            System.out.printf("obj reln found\n");
                            if (operation.object == null) {
                                operation.object = lemmatizedEntity;
                                foundOperation = true;
                            }
                        } else if (sdToCheck.subjectRelation.contains(td.reln().toString())) {
                            // if the td is a subject relationship, we search whether the predicate has a subject
                            if (operation.subject == null) {
                                operation.subject = lemmatizedEntity;
                                foundOperation = true;
                            }
                        }
                    }
                }

                if (!foundOperation) {
                    Operation newOp = new Operation();
                    newOp.predicate = td.gov().word();
                    System.out.printf("new operation.\npredicate: %s\n", newOp.predicate);
                    if (sdToCheck.objectRelation.contains(td.reln().toString())) {
                        System.out.printf("new obj: %s\n",lemmatizedEntity);
                        newOp.object = lemmatizedEntity;
                        operationList.add(newOp);
                    } else if (sdToCheck.subjectRelation.contains(td.reln().toString())) {
                        System.out.printf("new sub: %s\n",lemmatizedEntity);
                        newOp.subject = lemmatizedEntity;
                        operationList.add(newOp);
                    }
                    //TODO handle other kind of relation
                }
                System.out.print("\n");

            }
        }
        return operationList;
    }

    private List<String> buildPOLSequence(String[] keySeq, String[] POSSeq, String sampleLog) {
        int keyLength = keySeq.length;
        String[] logSeq = sampleLog.split("\\s+");
        int logSeqLength = logSeq.length;
        String[] POLSequence = new String[logSeqLength];
        List<Pattern> locationPatterns = LocationREList.getInstance().locationREs;
        // since a multiple words in a log can be identified as one '*'
        // we need to keep a separate index for the log.
        // logIndex is always less than or equals to index
        int logIndex = 0;
        int keyIndex = 0;

        while (logIndex < logSeqLength) {
            String keyWord = keySeq[keyIndex];
            String logWord = logSeq[logIndex];
            if (!keyWord.contains("*") && !LogUtil.isKeyWordVariable(keyWord)) {
                POLSequence[logIndex] = "CON";
            } else if (keyWord.matches("container_d\\+.*") ||
                    keyWord.matches("application_d\\+.*") ||
                    keyWord.matches("appattempt_d\\+.*") ||
                    keyWord.matches("attempt_d\\+.*") ||
                    keyWord.matches("BlockManagerId\\*")) {
                POLSequence[logIndex] = "ID";
                // add logIndex until reaching the end of the identifier (include)
                if (logIndex + 1 < logSeqLength) {
                    if (logSeq[logIndex + 1].contains("(")) {
                        logIndex++;
                        while (logIndex < logSeqLength) {
                            POLSequence[logIndex] = "_ID";
                            if (logSeq[logIndex].contains(")")) {
                                break;
                            } else {
                                logIndex++;
                            }
                        }
                        logIndex--;
                    }
                }
            } else {

                // 1. location (LOC) has the highest priority.
                // if a word is categorized as location, don't check whether it is ID nor VAL
                for (Pattern pattern: locationPatterns) {
                    Matcher matcher = pattern.matcher(logWord);
                    if (matcher.matches()) {
                        POLSequence[logIndex] = "LOC";
                        // handle the case like 'ip : port'
                        if (logIndex + 2 < logSeqLength) {
                            if (logSeq[logIndex + 1].equals(":") &&
                                    logSeq[logIndex + 2].matches("\\d{1,5}")) {
                                POLSequence[logIndex + 1] = "_LOC";
                                POLSequence[logIndex + 2] = "_LOC";
                                logIndex += 2;
                                keyIndex += 2;
                            }
                        }
                        break;
                    }
                }
                if (logWord.matches("//\\w+.*") && logIndex - 2 >= 0) {
                    if (logSeq[logIndex - 1].equals(":")) {
                        POLSequence[logIndex] = "_LOC";
                        POLSequence[logIndex - 1] = "_LOC";
                        POLSequence[logIndex - 2] = "LOC";
                    }
                }
                // 2. value (VAL)
                // handle cases such as 5 MB and 12 ms
                if (POLSequence[logIndex] == null) {
                    if (logWord.toLowerCase().matches("\\d+(\\.\\d+)?(byte|bytes|kb|mb|gb|ms|s|second).*")) {
                        POLSequence[logIndex] = "VAL";
                    } else if (logWord.matches("\\d+(\\.\\d+)?") && logIndex + 1 < logSeqLength) {
                        if (logSeq[logIndex + 1].toLowerCase().matches("(byte|bytes|kb|mb|gb|ms|s|second).*")) {
                            POLSequence[logIndex] = "VAL";
                        }
                    }
                }

                // 3. identifier (ID)
                if (POLSequence[logIndex] == null) {
                    if (logSeq[logIndex].matches(".*(([a-zA-Z]+\\d+)|(\\d+[a-zA-Z]+)).*") && PartOfSpeech.NONE.contains(POSSeq[keyIndex])) {
                        POLSequence[logIndex] = "ID";
                    } else if (logSeq[logIndex].matches("\\d+(\\.\\d+)?")) {
                        if (keyIndex > 0 && PartOfSpeech.NONE.contains(POSSeq[keyIndex - 1])) {
                            // we classify the noun as part of ID too.
                            POLSequence[logIndex - 1] = "ID";
                            POLSequence[logIndex] = "_ID";
                        } else if(keyIndex > 1 &&
                                POSSeq[keyIndex - 1].contains("#") &&
                                PartOfSpeech.NONE.contains(POSSeq[keyIndex - 2])) {
                            POLSequence[logIndex - 2] = "ID";
                            POLSequence[logIndex - 1] = "_ID";
                            POLSequence[logIndex] = "_ID";
                        } else {
                            POLSequence[logIndex] = "VAL";
                        }
                    } else {
                        POLSequence[logIndex] = "VAL";
                    }
                }
            }

            // the length of keySeq may not equal to the length of logSeq
            // we should align the index after each word scanned
            boolean aligned = false;
            if (keySeq[keyIndex].equals(logSeq[logIndex])) {
                keyIndex++;
                logIndex++;
            } else if (keyIndex + 1 < keyLength && logIndex + 1 < logSeqLength) {
                if (keySeq[keyIndex + 1].equals(logSeq[logIndex + 1])) {
                    keyIndex++;
                    logIndex++;
                } else {
                    int keySearchLength = 1;
                    while (keyIndex + keySearchLength < keyLength && keySearchLength <= 3) {
                        int logSearchLength = 1;
                        while (logIndex + logSearchLength < logSeqLength) {
                            if (keySeq[keyIndex + keySearchLength].equals(logSeq[logIndex + logSearchLength])) {
                                keyIndex += keySearchLength;
                                logIndex += logSearchLength;
                                aligned = true;
                                break;
                            }
                            logSearchLength++;
                        }
                        if (aligned) {
                            break;
                        }
                        keySearchLength++;
                    }
                }
            } else {
                break;
            }
        }

        return Arrays.asList(POLSequence);
    }


    public void buildIntelMessagesRule(String keyTagFilePath, String entityFilePath, String sampleFilePath) {
        File keyTagFile = new File(keyTagFilePath);
        File entityFile = new File(entityFilePath);
        File sampleFile = new File(sampleFilePath);
        buildIntelMessagesRule(keyTagFile, entityFile, sampleFile);
    }

    public void buildIntelMessagesRule(File keyTagFile, File entityFile, File sampleFile) {
        if (!keyTagFile.exists() || !entityFile.exists() || !sampleFile.exists()) {
            System.out.print("key-tag file, entity file or sample file does not exist. Abort build intel message rules");
        }
        List<String> entityList = EntityMap.getInstance().getEntityList(entityFile.getAbsolutePath());
        List<String> sampleLogList = LogUtil.loadFile(sampleFile);
        StructureParser parser = new StructureParser();
        parser.loadKeyTagFile(keyTagFile);
        parser.parse();
        // index is for each log key
        for (int index = 0; index < parser.parsedKeyTagList.size(); index++) {
            IntelMessageRule newIntelRule = new IntelMessageRule();
            // get the log sequence and POS sequence
            String[] keySeq = parser.rawKeyList.get(index).split("\\s+");
            String[] POSSeq = parser.rawPOSList.get(index).split("\\s+");
            newIntelRule.tokenSequence = Arrays.asList(keySeq);
            newIntelRule.POSSequence = Arrays.asList(POSSeq);
            newIntelRule.originalLogKey = LogUtil.spliceSequence(keySeq);
            newIntelRule.entities = LogUtil.findAllEntities(keySeq, entityList);

            // TEST
            System.out.printf("building %s\n", Arrays.toString(keySeq));

            // build the operations
            // this step replaces one word to phrases in the entity set.
            // this step lemmatize noun and verb
            // this step process camel case words
            List<Operation> operations = buildOperations(keySeq, POSSeq, parser.parsedKeyTagList.get(index), entityList);
            newIntelRule.operations = operations;
            System.out.print("operations built\n");


            // build the KeyedMessageRule
            KeyedMessageRule newRule = new KeyedMessageRule();
            newRule.buildRule(keySeq, POSSeq, entityList);
            newIntelRule.keyedMessageRule = newRule;
            System.out.print("keyed message rule built\n");

            //build the LOS sequence
            String sampleLog = sampleLogList.get(index);
            newIntelRule.POLSequence = buildPOLSequence(keySeq, POSSeq, sampleLog);
            System.out.print("POLSeq built\n");



            intelMessageRules.add(newIntelRule);
        }
    }

    public void report() {
        int index;
        int size = intelMessageRules.size();
        for (index = 0; index < size; index++) {
            System.out.printf("index: %d\n", index);
            System.out.printf("log key: %s\n", intelMessageRules.get(index).originalLogKey);
            System.out.printf("POL seq: %s\n", intelMessageRules.get(index).POLSequence.toString());
            System.out.printf("rule: %s\n", intelMessageRules.get(index).keyedMessageRule.regex);
            System.out.printf("token seq: %s\n", intelMessageRules.get(index).tokenSequence.toString());
            System.out.printf("POS seq: %s\n", intelMessageRules.get(index).POSSequence.toString());
            System.out.printf("entities: %s\n", intelMessageRules.get(index).entities.toString());
            System.out.printf("Operations: %s\n\n", intelMessageRules.get(index).operations);
        }
    }

}
