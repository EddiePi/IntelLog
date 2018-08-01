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
        String graphPath = "/Users/Eddie/gitRepo/log-preprocessor/data/mr-data/mr-map-graph.json";
        STNode root = builder.buildGraph();
        //GsonSerializer.writeJSON(root, graphPath);
    }

}