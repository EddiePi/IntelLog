package STGraph;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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
    public void build() throws Exception {
        builder.build();
    }

}