/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.utils;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 *
 * @author feroz
 */
public class JsonUtil {
    
    public enum NullStrategy {
        Remove, Merge, Overwrite
    }
    
    /**
     * Merge "source" into "target". If fields have equal name, merge them
     * recursively. Null values in source will remove the field from the target.
     * Override target values with source values Keys not supplied in source
     * will remain unchanged in target
     *
     * @return the merged object (target).
     */
    public static JsonObject deepMerge(JsonObject source, JsonObject target, NullStrategy nullStrategy) {

        for (Map.Entry<String, JsonElement> sourceEntry : source.entrySet()) {
            String key = sourceEntry.getKey();
            JsonElement value = sourceEntry.getValue();
            if (!target.has(key)) {
                //target does not have the same key, so perhaps it should be added to target
                if (!value.isJsonNull()) //well, only add if the source value is not null
                {
                    target.add(key, value);
                }
            } else {
                if (!value.isJsonNull()) {
                    JsonElement sourceVal = value;
                    JsonElement targetVal = target.get(key);
                    
                    if (sourceVal.isJsonArray() || targetVal.isJsonArray()) {
                        //array - append
                        JsonArray array = arrayMerge(sourceVal, targetVal);
                        target.add(key, array);
                    }
                    else if (value.isJsonObject()) {
                        //source value is json object, start deep merge
                        deepMerge(sourceVal.getAsJsonObject(), targetVal.getAsJsonObject(), nullStrategy);
                    } else {
                        // overwrite target value
                        target.add(key, value);
                    }
                } else {
                    // should we overwrite with null or remove the key?
                    switch (nullStrategy) {
                        case Remove: target.remove(key); break;
                        case Overwrite: target.add(key, value); break;
                        case Merge: break;
                    }
                }
            }
        }
        return target;
    }
    
    public static JsonArray arrayMerge(JsonElement source, JsonElement target) {
        JsonArray targetArray = null;
        
        if (target.isJsonArray()) {
            targetArray = target.getAsJsonArray();
        }
        else {
            targetArray = new JsonArray();
            targetArray.add(target);
        }
            
        if (source.isJsonArray()) {
            // merge into the target array
            targetArray.addAll(source.getAsJsonArray());
        }
        else {
            // source is not an array, just append
            targetArray.add(source);
        }
        
        // Remove any inadvertant nulls that may have crept into the arrays
        for (Iterator<JsonElement> it = targetArray.iterator(); it.hasNext();) {
            JsonElement next = it.next();
            if (next.isJsonNull()) it.remove();
        }
        
        return targetArray;
    }
    
    public static String escape(String input) {
		return StringEscapeUtils.escapeJson(input);
    }
    
    public static String getFromObject(JsonObject object, String property) {
        JsonElement element = object.get(property);
        if ((element != null) && (! element.isJsonNull())) return element.getAsString();
        else return "";
    }
    
    public static JsonObject wrap(JsonElement object, String key) {
        JsonObject request = new JsonObject();
        JsonObject wrapper = new JsonObject();
        
        // request/key/object
        request.add(key, object);
        wrapper.add("request", request);
        return wrapper;
    }
}
