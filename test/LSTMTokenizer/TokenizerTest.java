package LSTMTokenizer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TokenizerTest {

    Tokenizer tokenizer = new Tokenizer();
    String logRoot = "/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-test/buggy";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void tokenizeAll() {
        tokenizer.tokenizeAll(logRoot);
    }
}