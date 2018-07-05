package NPL;

import edu.stanford.nlp.trees.TypedDependency;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;


/**
 * Created by Eddie on 2018/5/22.
 */
public class StructureParserTest {
    StructureParser parser;
    HashSet<String> SDSet;

    @Before
    public void setUp() throws Exception {
        parser = new StructureParser();
        //parser.loadKeyTagFile("/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-keys-tags.txt");
        parser.loadKeyTagFile("/Users/Eddie/gitRepo/log-preprocessor/data/mr-data/mr-keys-tags-test.txt");
        SDSet = SDToCheck.getInstance().dependencies;
    }

    @Test
    public void test1() throws Exception {
        parser.test();
    }

    @Test
    public void parse() throws Exception {
        parser.parse();
        int index = 0;
        for (Collection<TypedDependency> tdc: parser.parsedKeyTagList) {
            // tdc contains all relationship in one log key
            System.out.printf("%s\n", parser.rawKeyList.get(index++));
            System.out.printf("%s\n", tdc);
            for (TypedDependency td: tdc) {
                //System.out.printf("relationship: %s, gov: %s, gindex: %d, dep: %s, dindex: %d\n", td.reln(), td.gov().word(), td.gov().index(), td.dep().word(), td.dep().index());
                if (true) {
                //if (SDSet.contains(td.reln().toString())) {
                    System.out.printf("relationship: %s, gov: %s, gindex: %d, dep: %s, dindex: %d\n", td.reln(), td.gov().word(), td.gov().index(), td.dep().word(), td.dep().index());
                }
            }
            System.out.println();
        }
    }

}