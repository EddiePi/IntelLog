package NPL;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import utils.LogUtil;

import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
/**
 * Created by Eddie on 2018/5/22.
 */
public class StructureParser {
    String grammar;
    String[] options = { "-maxLength", "80", "-retainTmpSubcategories" };
    LexicalizedParser lp;
    TreebankLanguagePack tlp;
    GrammaticalStructureFactory gsf;
    List<String> unformattedKeyTagList;
    // one entry is one log key
    public List<String> rawKeyList;
    // one entry is a sequence of POS separated by '\s'
    public List<String> rawPOSList;
    public List<Collection<TypedDependency>> parsedKeyTagList;


    public StructureParser() {
        grammar = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
        lp = LexicalizedParser.loadModel(grammar, options);
        tlp = lp.getOp().langpack();
        tlp.setGenerateOriginalDependencies(true);
        gsf = tlp.grammaticalStructureFactory();
        unformattedKeyTagList = new ArrayList<>();
        parsedKeyTagList = new ArrayList<>();
        rawKeyList = new ArrayList<>();
        rawPOSList = new ArrayList<>();
    }

    public void test() {
        List<TaggedWord> sentence3 = formatTaggedSentence("Started_VBN daemon_NN with_IN process_NN name_NN :_: *_NN");
        Tree parse = lp.parse(sentence3);
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        Collection<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
        System.out.println(tdl);
    }

    public void loadKeyTagFile(String filePath) {
        loadKeyTagFile(new File(filePath));
    }

    public void loadKeyTagFile(File file) {
//        FileReader fr;
//        BufferedReader br;
//        try {
//            fr = new FileReader(file);
//            br = new BufferedReader(fr);
//            String line = null;
//            while ((line = br.readLine()) != null) {
//                unformattedKeyTagList.add(line);
//            }
//        } catch (FileNotFoundException e){
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        unformattedKeyTagList = LogUtil.loadFile(file);
    }

    public void parse() {
        if (unformattedKeyTagList.isEmpty()) {
            System.out.print("key-tag list is not loaded");
            return;
        }
        List<TaggedWord> sentence;
        Tree parse;
        GrammaticalStructure gs;
        Collection<TypedDependency> tdl;
        for (String keyTag: unformattedKeyTagList) {
            sentence = formatTaggedSentence(keyTag);
            parse = lp.parse(sentence);
            gs = gsf.newGrammaticalStructure(parse);
            tdl = gs.typedDependenciesCCprocessed();
            parsedKeyTagList.add(tdl);
        }
    }

    public List<TaggedWord> formatTaggedSentence(String taggedSentence) {
        List<TaggedWord> sentence = new ArrayList<>();
        String taggedWords[] = taggedSentence.trim().split("\\s+");
        int length = taggedWords.length;
        String[] words = new String[length];
        String[] tags = new String[length];
        String rawKey = "";
        String rawPOS = "";
        for (int index = 0; index < length; index++) {
            String taggedWord = taggedWords[index];
            int underIndex = taggedWord.lastIndexOf("_");
            String word = taggedWord.substring(0, underIndex);
            String tag = taggedWord.substring(underIndex + 1);
            words[index] = word;
            tags[index] = tag;
        }
        for (int i = 0; i < length; i++) {
            sentence.add(new TaggedWord(words[i], tags[i]));
            rawKey += words[i] + " ";
            rawPOS += tags[i] + " ";
        }
        rawKeyList.add(rawKey.trim());
        rawPOSList.add(rawPOS.trim());
        return sentence;
    }
}
