/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author feroz
 */
public class JsonMergeTest {

    public JsonMergeTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Merge "source" into "target". If fields have equal name, merge them
     * recursively. Null values in source will remove the field from the target.
     * Override target values with source values Keys not supplied in source
     * will remain unchanged in target
     *
     * @return the merged object (target).
     */
    public static JsonObject deepMerge(JsonObject source, JsonObject target) throws Exception {

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
                        deepMerge(sourceVal.getAsJsonObject(), targetVal.getAsJsonObject());
                    } else {
                        // overwrite target value
                        target.add(key, value);
                    }
                } else {
                    // should we overwrite with null or remove the key?
                    target.add(key, value);
                    //target.remove(key);
                }
            }
        }
        return target;
    }
    
    public static JsonArray arrayMerge(JsonElement source, JsonElement target) throws Exception {
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
        System.out.println(targetArray);
        return targetArray;
    }

    @Test
    public void testMerge() throws Exception {
        JsonParser parser = new JsonParser();
        JsonObject a = null;
        JsonObject b = null;
        a = parser.parse("{offer: {issue1: null, issue2: null}, accept: true, reject: [hello]}").getAsJsonObject();
        b = parser.parse("{offer: {issue2: value2}, reject: world}").getAsJsonObject();
        System.out.println(deepMerge(a, b));
        // prints:
        // {"offer":{},"accept":true}
        a = parser.parse("{offer: {issue1: value1}, accept: true, reject: null}").getAsJsonObject();
        b = parser.parse("{offer: {issue2: value2}, reject: false}").getAsJsonObject();
        System.out.println(deepMerge(a, b));
        // prints:
        // {"offer":{"issue2":"value2","issue1":"value1"},"accept":true}

    }
}
