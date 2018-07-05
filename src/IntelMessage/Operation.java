package IntelMessage;

/**
 * Created by Eddie on 2018/6/5.
 */
public class Operation {
    public String subject;
    public String object;
    public String predicate;

    @Override
    public String toString() {
        String res;
        res = "subject: " + subject + " predicate: " + predicate + " object: " + object;

        return res;
    }
}
