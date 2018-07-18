package Main;

import utils.FileIO;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Eddie on 2018/7/16.
 */
public class BuilderConf {
    private static final BuilderConf instance = new BuilderConf();

    public static BuilderConf getInstance() {
        return instance;
    }

    Map<String, String> setting;

    private BuilderConf(){
        setting = new HashMap<>();
        getConfFromFile();
    }

    private void getConfFromFile() {
        String path;
        try {
            File confFile;
            List<String> strings;
            path = "conf/intel.conf";
            confFile = new File(path);
            if (!confFile.exists()) {
                path = "../conf/intel.conf";
            }
            strings = FileIO.read(path);
            for(String str: strings) {
                if(str.isEmpty()) {
                    continue;
                }
                if (str.trim().charAt(0) == '#') {
                    continue;
                }
                String[] result = str.split("\\s+");
                setting.put(result[0], result[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public Integer getIntegerOrDefault(String key, Integer defaultValue) {
        String valueStr = setting.get(key);
        if (valueStr == null) {
            return defaultValue;
        }
        Integer value;
        try {
            value = Integer.valueOf(valueStr);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
        return value;
    }

    public String getStringOrDefault(String key, String defaultValue) {
        String valueStr = setting.get(key);
        return valueStr != null ? valueStr : defaultValue;
    }

    public Double getDoubleOrDefault(String key, Double defaultValue) {
        String valueStr = setting.get(key);
        if (valueStr == null) {
            return defaultValue;
        }
        Double value;
        try {
            value = Double.valueOf(valueStr);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
        return value;
    }

    public Boolean getBooleanOrDefault(String key, Boolean defaultValue) {
        String valueStr = setting.get(key);
        if (valueStr == null) {
            return defaultValue;
        }
        Boolean value;
        value = Boolean.valueOf(valueStr);
        return value;
    }
}
