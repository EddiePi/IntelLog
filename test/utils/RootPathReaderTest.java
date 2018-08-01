package utils;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by Eddie on 2018/7/17.
 */
public class RootPathReaderTest {
    RootPathReader reader;
    String rootPath;
    @Before
    public void setUp() throws Exception {
        rootPath = "/Users/Eddie/gitRepo/log-preprocessor/data/spark-master/";
        reader = new RootPathReader(rootPath);
    }

    @Test
    public void nextFile() throws Exception {
        File file;
        while ((file = reader.nextFile()) != null) {
            System.out.println(file.getAbsolutePath());
        }
    }

    @Test
    public void justATest() throws Exception {
        String[] strArray = new String[3];
        strArray[0] = "token";
        strArray[1] = "apple";
        strArray[2] = "arandom string";
        Arrays.sort(strArray);
        for (String str: strArray) {
            System.out.println(str);
        }
    }

}