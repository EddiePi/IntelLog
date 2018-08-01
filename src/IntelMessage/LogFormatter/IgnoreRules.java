package IntelMessage.LogFormatter;

import Main.BuilderConf;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Eddie on 2018/7/31.
 */
public class IgnoreRules {

    private static IgnoreRules instance;

    public List<String> ignoreRules;

    public static IgnoreRules getInstance() {
        if (instance == null) {
            instance = new IgnoreRules();
        }
        return instance;
    }

    private IgnoreRules() {
        BuilderConf conf = BuilderConf.getInstance();
        String ignoreRulePath = conf.getStringOrDefault("ignore-rule.path", null);
        ignoreRules = new ArrayList<>();
        if (ignoreRulePath == null) {
            return;
        }
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(ignoreRulePath));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.trim().length() == 0) {
                    continue;
                }
                ignoreRules.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
