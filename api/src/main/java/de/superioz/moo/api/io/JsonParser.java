package de.superioz.moo.api.io;

import lombok.Getter;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Created on 27.09.2016.
 */
@Getter
public class JsonParser {

    private LinkedHashMap<String, Object> pathList;
    private JSONObject object;

    public JsonParser(JSONObject object, String root) {
        this.object = object;
        this.pathList = new LinkedHashMap<>();

        if(object != null) {
            readObject(object, root);
        }
    }

    /**
     * Reads a json object and put paths into map (recursive methods, jey!)
     *
     * @param object   The object
     * @param jsonPath The jsonpath
     */
    private void readObject(JSONObject object, String jsonPath) {
        Iterator keysItr = object.keySet().iterator();
        String parentPath = jsonPath;
        while(keysItr.hasNext()){
            String key = (String) keysItr.next();
            Object value = object.get(key);
            jsonPath = parentPath + "." + key;

            if(value instanceof JSONObject) {
                readObject((JSONObject) value, jsonPath);
            }
            else { // is a value
                this.pathList.put(jsonPath, value);
            }
        }
    }

}