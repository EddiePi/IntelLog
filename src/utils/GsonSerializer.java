package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

/**
 * Created by Eddie on 2018/6/22.
 */
public class GsonSerializer {
    static Gson gson = new Gson();

    public static <T> void writeJSON(T object, String path) {
        FileWriter fw;
        try {
            fw = new FileWriter(path);
            fw.write(gson.toJson(object));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> T readJSON(Class<T> c, String path) {
        BufferedReader br;
        T res = null;
        try {
            br = new BufferedReader(new FileReader(path));
            res = gson.fromJson(br, c);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
}
