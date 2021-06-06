package database.registry;

import java.util.HashMap;
import java.util.Map;

public class BasicTypeRegistry {

    /*  1-java тип,2-sql тип */
    private static Map<String,String> types = new HashMap<>();

    public static Map<String, String> getTypes() {
        return types;
    }

    public static void setTypes() {
        types.put("java.lang.String","VARCHAR");
        types.put("java.lang.Long","BIGINT");
        types.put("java.lang.Integer","BIGINT");
        types.put("java.lang.Boolean","BOOLEAN");
    }

}
