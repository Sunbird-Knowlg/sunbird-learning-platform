/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.utils;

import java.util.Locale;
import org.apache.commons.lang3.RandomStringUtils;

/**
 *
 * @author feroz
 */
public class Identifier {
    public static String nextIdentifier() {
        String identifier = RandomStringUtils.random(10, true, true);
        return identifier.toLowerCase(Locale.ENGLISH);
    }
}
