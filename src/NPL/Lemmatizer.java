package NPL;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import utils.LogUtil;

import java.io.*;
import java.util.List;

/**
 * Created by Eddie on 2018/2/22.
 */
public class Lemmatizer {
    private static Lemmatizer instance = new Lemmatizer();

    public static Lemmatizer getInstance() {
        return instance;
    }

    static DictionaryLemmatizer lemmatizer;

    private Lemmatizer () {
        try {
            File modelFile = new File("./lib/en-lemmatizer.dict");
            if (!modelFile.exists()) {
                modelFile = new File("../lib/en-lemmatizer.dict");
            }
            if (!modelFile.exists()) {
                System.out.println("model does not exist. exit!");
                throw new FileNotFoundException(modelFile.getAbsolutePath());
            }
            InputStream modelIn = new FileInputStream(modelFile);
            lemmatizer = new DictionaryLemmatizer(modelIn);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] lemmatize (String[] tokens, String[] POSs) {
        String[] lemmatized = lemmatizer.lemmatize(tokens, POSs);
        String[] res = new String[lemmatized.length];
        for (int i = 0; i < lemmatized.length; i++) {
            if (lemmatized[i].equals("O")) {
                res[i] = tokens[i];
            } else {
                res[i] = lemmatized[i];
            }
        }
        return res;
    }

    public static String mayBeLemmatizeToSingular(String word) {
        String res;
        String[] wordInSeq = new String[1];
        String[] posInSeq = new String[1];
        String[] splited = word.split("[\\._\\-]+");
        wordInSeq[0] = splited[splited.length - 1].trim();
        posInSeq[0] = "NNS";
        String lemmatizedWord = Lemmatizer.getInstance().lemmatize(wordInSeq, posInSeq)[0];
        if (splited.length <= 1) {
            res = lemmatizedWord;
        } else {
            //splited[splited.length - 1] = lemmatizedWord;
            //res = LogUtil.spliceSequence(splited);
            int replaceStartIndex = word.lastIndexOf(wordInSeq[0]);
            res = word.substring(0, replaceStartIndex) + lemmatizedWord;
        }
        return res;
    }
}
