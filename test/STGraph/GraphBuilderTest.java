package STGraph;

import org.junit.Before;
import org.junit.Test;
import utils.GsonSerializer;

/**
 * Created by Eddie on 2018/7/17.
 */
public class GraphBuilderTest {

    GraphBuilder builder;
    @Before
    public void setUp() throws Exception {
        builder = new GraphBuilder();
    }

    @Test
    public void buildMatrix() throws Exception {
        HelperComposite root = builder.buildMatrix();
        System.out.print("built");
        root.reportRelationship();
        System.out.println();
        root.reportCommonRelationship();
        System.out.println();
        root.reportCommonRuleRelationship();

    }

    @Test
    public void buildGraph() throws Exception {
        //String graphPath = "/Users/Eddie/gitRepo/log-preprocessor/data/mr-data/mr-map-graph.json";
        String graphPath = "/Users/Eddie/gitRepo/log-preprocessor/data/spark-data/spark-test/debug-level/spark-debug-graph.json";
        STNode root = builder.buildGraph();
        System.out.println("STOP");
        //GsonSerializer.writeJSON(root, graphPath);
    }

}