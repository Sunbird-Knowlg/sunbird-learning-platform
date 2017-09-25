/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.functions;

import java.util.HashMap;
import java.util.Map;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.SimpleJtwigFunction;

/**
 * Provides a lookup function to the transformation. The lookup is most useful
 * in scenarios where key=value mappings are involved, such as replacing a
 * language name with its code, or a concept name with its identifier.
 *
 * Params: 
 *  lang - Transformation key to lookup 
 *  langs - Lookup table (while loading the lookups)
 *
 * Usage: 
 *  {{ lookup(lang, 'langs') }}
 *
 * @author feroz
 */
public class LookupFunction extends SimpleJtwigFunction {

    Map<String, Map<String, String>> lookupTables = new HashMap();

    @Override
    public String name() {
        return "lookup";
    }

    public void addLookupTable(String lookupTableName, Map<String, String> lookupData) {
        lookupTables.put(lookupTableName, lookupData);
    }

    @Override
    public Object execute(FunctionRequest functionRequest) {
        String key = (String) functionRequest.get(0);
        String lookupTable = (String) functionRequest.get(1);
        String val = null;

        if (lookupTables.containsKey(lookupTable)) {
            val = lookupTables.get(lookupTable).get(key);
        }

        return val;
    }
}
