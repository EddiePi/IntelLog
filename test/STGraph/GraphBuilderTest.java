package STGraph;

import org.junit.Before;
import org.junit.Test;

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
        builder.buildGraph();
    }

}