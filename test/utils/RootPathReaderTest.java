package utils;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

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

}