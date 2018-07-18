package STGraph;

import java.util.Iterator;

/**
 * Created by Eddie on 2018/7/16.
 */
public class NullIterator implements Iterator {
    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Object next() {
        return null;
    }
}
