/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.functions;

import org.ekstep.tools.loader.utils.Slug;
import org.apache.commons.lang3.StringUtils;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.SimpleJtwigFunction;

/**
 * Provides a sluggify function to the transformation to convert an input string
 * into a valid, sluggified file name. Sluggification removes any special chars, 
 * transliterates the string into latin, and makes sure that the resultant value
 * is a valid URL and file name. 
 * 
 * Params: 
 *  value - Value to sluggify
 *
 * Usage: 
 *  {{ sluggify(fileName) }}
 *
 * @author feroz
 */
public class SluggifyFunction extends SimpleJtwigFunction {
    @Override
    public String name() {
        return "sluggify";
    }
    
    @Override
    public Object execute(FunctionRequest functionRequest) {
        
        String name = (String) functionRequest.get(0);
        String slug = Slug.makeSlug(name, true);
        return slug;
    }
}
