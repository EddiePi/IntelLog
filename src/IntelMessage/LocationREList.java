package IntelMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Eddie on 2018/6/19.
 */
public class LocationREList {

    public static LocationREList getInstance() {
        if (instance == null) {
            instance = new LocationREList();
        }
        return instance;
    }

    private static LocationREList instance = null;

    public List<Pattern> locationREs;

    private LocationREList() {
        locationREs = new ArrayList<>();
        // file://foo/bar@10.0.101.001:12345
        //locationREs.add(Pattern.compile(".*?(?<loc>[\\w\\.:/]+@\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(:\\d{1,5})?).*?"));
        // user@host
        locationREs.add(Pattern.compile(".*?(?<loc>\\w+@\\w+(:\\d{1,5})).*?"));
        // user@192.168.32.1:12345
        locationREs.add(Pattern.compile(".*?(?<loc>\\w+@\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(:\\d{1,5})?).*?"));
        // 192.168.0.1:12345
        locationREs.add(Pattern.compile(".*?(?<loc>\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(:\\d{1,5})?).*?"));
        // foo/bar, /foo/bar, foo/bar/
        locationREs.add(Pattern.compile(".*?(?<loc>/\\w+(/\\w+)+/?).*?"));
        // 192.168.32.1
        locationREs.add(Pattern.compile(".*?(?<loc>\\w+@\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}).*?"));
    }

}
