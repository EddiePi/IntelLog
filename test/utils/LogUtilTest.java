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

    @Test
    public void idToTypeAndValue() throws Exception {
        String[] res;
        res = LogUtil.idToTypeAndValue("task 19");
        System.out.printf("type: %s, value: %s\n", res[0], res[1]);
        res = LogUtil.idToTypeAndValue("BlockManagerId(1, 3, 4, 5)");
        System.out.printf("type: %s, value: %s\n", res[0], res[1]);
        res = LogUtil.idToTypeAndValue("fetcher # 13");
        System.out.printf("type: %s, value: %s\n", res[0], res[1]);
        res = LogUtil.idToTypeAndValue("attempt_1_123_342");
        System.out.printf("type: %s, value: %s\n", res[0], res[1]);
    }

}