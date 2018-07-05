package IntelMessage;

import java.io.*;
import java.util.*;

/**
 * In the entity map, the key the file name of the entity which indicates the framework
 * The value is a set of entities in that framework
 */
public class EntityMap {
    public static EntityMap getInstance() {
        if (instance == null) {
            instance = new EntityMap();
        }

        return instance;
    }

    private static EntityMap instance = null;

    private EntityMap() {
        entityMap = new HashMap<>();
    }

    private Map<String, List<String>> entityMap;

    public void loadEntity(String entityFilePath) throws FileNotFoundException {
        File entityFile = new File(entityFilePath);
        loadEntity(entityFile);
    }

    public void loadEntity(File entityFile) throws FileNotFoundException {
        if (!entityFile.exists()) {
            throw new FileNotFoundException("entity file " + entityFile.getAbsolutePath() + " does not exist");
        }
        List<String> entities = new ArrayList<>();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(entityFile));
            String line;
            while ((line = br.readLine()) != null) {
                entities.add(line.trim());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        entityMap.put(entityFile.getAbsolutePath(), entities);
    }

    public List<String> getEntityList(String filePath) {
        List<String> res = entityMap.get(filePath);
        if (res == null) {
            try {
                loadEntity(filePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return entityMap.get(filePath);
    }
}
