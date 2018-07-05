package utils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eddie on 2018/6/6.
 */
public class LogUtilTest {
    String[] sampleLogSeq;
    int wordIndex;
    ArrayList<String> sampleEntityList;
    @Before
    public void setUp() throws Exception {
        sampleLogSeq = new String[]{"this", "is", "a", "sample", "log", "*", "hello", "world"};
        wordIndex = 5;
        sampleEntityList = new ArrayList<String>(){{
            add("a sample log");
            add("hello world");
            add("hello");
        }};
    }

    @Test
    public void findEntity() throws Exception {
        EntityInLog entityInLog;
        entityInLog = LogUtil.findEntity(sampleLogSeq, wordIndex, sampleEntityList);
    }

    @Test
    public void findAllEntities() throws Exception {
        List<String> res = LogUtil.findAllEntities(sampleLogSeq, sampleEntityList);
        System.out.println(res.toString());
    }

    @Test
    public void subPhrase() throws Exception {
    }

}