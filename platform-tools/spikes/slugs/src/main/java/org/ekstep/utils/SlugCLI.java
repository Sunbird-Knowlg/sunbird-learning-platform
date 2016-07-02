package org.ekstep.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class SlugCLI 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        
        // s = "résumé"
        List<String> names = new ArrayList();
        names.add("हाथी और भालू की शादी");
        names.add("Nia's Stories");
        names.add("### ### ^^^ Hello,     / % world ###");
        names.add("ಕನ್ನಡ  ರೈಮ್ಸ್"); //Kannada rhymes
        names.add("ಮೊಲ  ಮತ್ತು ಆಮೆಯ ಕಥೆ ");// story of hare and turtle   
        names.add("1hi.png");
        
        for (String name : names) {
            String slug = Slug.makeSlug(name, true);
            System.out.println(name + "  --->  " + slug);
        }

        int count = 10000000;
        if (args.length > 0) {
            String countStr = args[0];
            count = Integer.parseInt(countStr);
        }
                
        Map<String, String> hashes = new HashMap();
        
        for (int i = 0; i < count; i++) {
            String identifier = Identifier.nextIdentifier();
            //System.out.println(identifier);
            hashes.put(identifier, identifier);
        }
        
        System.out.println("------------");
        System.out.println("Total = " + count + ", Unique = " + hashes.size() + ", Clashes = " + (count - hashes.size()));
    }
}
