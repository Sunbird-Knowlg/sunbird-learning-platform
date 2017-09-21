/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.service;

import com.google.gson.JsonObject;
import java.util.Map;

/**
 *
 * @author feroz
 */
public class Record {
    private String key;
    private Map<String, String> csvData;
    private JsonObject jsonData;
    
    public Record(String akey) {
        if (akey == null) this.key = "";
        else this.key = akey.trim();
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the csvData
     */
    public Map<String, String> getCsvData() {
        return csvData;
    }

    /**
     * @param csvData the csvData to set
     */
    public void setCsvData(Map<String, String> csvData) {
        this.csvData = csvData;
    }

    /**
     * @return the jsonData
     */
    public JsonObject getJsonData() {
        return jsonData;
    }

    /**
     * @param jsonData the jsonData to set
     */
    public void setJsonData(JsonObject jsonData) {
        this.jsonData = jsonData;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(key);
        builder.append(jsonData);
        return builder.toString();
    }
}
