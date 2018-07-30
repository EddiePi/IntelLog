package spell;

import Log.LogPreprocessor;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

/**
 * Created by Eddie on 2018/4/25.
 */
public class LCSMapTest {
    LCSMap map;
    @Before
    public void setUp() throws Exception {
        map = new LCSMap();
        LogPreprocessor preprocessor;

        //File f = new File("/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-only-content.log");
        File f = new File("/Users/Eddie/gitRepo/log-preprocessor/data/mr-data/mr-only-content.log");
        //File f = new File("/home/eddie/log-preprocessor/data/spark-only-content.log");
        preprocessor = new LogPreprocessor(f);
        String line;
        while ((line = preprocessor.getNextLine()) != null) {
            map.insert(line);
        }

    }

    @Test
    public void writeResult() throws Exception {
        //String fileName="/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-log-keys.txt";
        String fileName="/Users/Eddie/gitRepo/log-preprocessor/data/mr-data/mr-log-keys.txt";
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
            for (LCSObject lcsObject: map.getAllLCSObjects()) {
                String key = lcsObject.getLCSString().trim();
                bw.write(key + "\n");
                System.out.printf("%s\n", key);
            }
            bw.close();
        } catch (IOException e) {
                e.printStackTrace();
        }
    }

}