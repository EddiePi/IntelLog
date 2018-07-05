package NPL;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Eddie on 2018/6/4.
 */
public class SDToCheck {

    public static SDToCheck getInstance() {
        if (instance == null) {
            instance = new SDToCheck();
        }
        return instance;
    }

    private static SDToCheck instance;

    public HashSet dependencies;

    public HashSet objectRelation;

    public HashSet subjectRelation;

    private SDToCheck() {
        dependencies = new HashSet() {{
            //add("root");
            add("conj");
            add("dobj");
            add("iobj");
            add("pobj");
            add("nsubj");
            add("nsubjpass");
        }};

        objectRelation = new HashSet() {{
            add("dobj");
            add("iobj");
            add("pobj");
            add("nsubjpass");
        }};

        subjectRelation = new HashSet() {{
           add("nsubj");
        }};
    }
}
