package S5.Utils;

import java.io.Serializable;
import java.util.HashMap;

public class Data implements Serializable {
    private HashMap<Integer,Integer> database;

    public Data() {
        this.database = new HashMap<Integer,Integer>();
    }

    public HashMap<Integer, Integer> getDatabase() {
        return database;
    }

    public void setDatabase(HashMap<Integer, Integer> database) {
        this.database = database;
    }
}
