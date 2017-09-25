/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.functions;

import org.apache.commons.lang3.StringUtils;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.SimpleJtwigFunction;

/**
 * Provides a split function to the transformation to convert a delimited value
 * into an array of strings. 
 * 
 * Params: 
 *  value - Value to split during the transformation
 *  delim - Delimiter to use (optional, default is comma)
 *
 * Usage: 
 *  {{ split(concepts) }}
 *  {{ split(concepts, '@') }}
 *
 * @author feroz
 */
public class SplitFunction extends SimpleJtwigFunction {
    @Override
    public String name() {
        return "split";
    }
    
    @Override
    public Object execute(FunctionRequest functionRequest) {
        
        String key = (String) functionRequest.get(0);
        String delim = ","; // default is comma
        
        // Optionally, second argument is the delimiter
        if (functionRequest.getNumberOfArguments() > 1) delim = (String) functionRequest.get(1);
        String[] split = StringUtils.split(key, delim);
        
        return split;
    }
}
