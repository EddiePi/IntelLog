package LogConsumer;

import STGraph.GraphBuilder;
import STGraph.STNode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Eddie on 2018/8/1.
 */
public class LogAnalyzerTest {

    LogAnalyzer analyzer;
    GraphBuilder builder;
    @Before
    public void setUp() throws Exception {
        builder = new GraphBuilder();
        STNode root = builder.buildGraph();
        analyzer = new LogAnalyzer(root, builder.getCommonGroup());
    }

    @Test
    public void analyze() throws Exception {
    }

    @Test
    public void report() throws Exception {
        analyzer.analyze();
        analyzer.report();
    }

}