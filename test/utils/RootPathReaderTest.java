package utils;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
        Map<String, Integer> testMap = new HashMap<>();
        testMap.compute("test1", (k, v) -> (v == null) ? 1 : v + 1);
        testMap.compute("test1", (k, v) -> (v == null) ? 1 : v + 1);
        testMap.compute("test1", (k, v) -> (v == null) ? 1 : v + 1);
        testMap.compute("test2", (k, v) -> (v == null) ? 1 : v + 1);

        System.out.print(testMap.toString());

    }

}