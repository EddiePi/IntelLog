package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Eddie on 2018/6/22.
 */
public class GsonSerializer {
    static Gson gson = new Gson();

    public static void writeJSON(Object object, String path) {
        FileWriter fw;
        try {
            fw = new FileWriter(path);
            fw.write(gson.toJson(object));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object readJSON(String path) {
        BufferedReader br;
        Object res = null;
        try {
            br = new BufferedReader(new FileReader(path));
            res = gson.fromJson(br, Object.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
}
