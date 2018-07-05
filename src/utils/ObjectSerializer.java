package utils;

import java.io.*;
import java.lang.reflect.Type;

/**
 * Created by Eddie on 2018/5/22.
 */
public class ObjectSerializer {

    public static void serialize(Serializable obj, String path) {
        try
        {
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(obj);
            out.close();
            fileOut.close();
        } catch(IOException i) {
            i.printStackTrace();
        }
    }

    public static Serializable deserialize(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        Serializable obj = null;
        try
        {
            FileInputStream fileIn = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            obj = (Serializable) in.readObject();
            in.close();
            fileIn.close();
        }catch(IOException i)
        {
            i.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return obj;
    }


}
