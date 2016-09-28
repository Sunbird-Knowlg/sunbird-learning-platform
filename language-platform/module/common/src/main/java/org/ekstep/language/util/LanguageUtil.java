package org.ekstep.language.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

/**
 * The Class LanguageUtil contains common utilities used by the language platform.
 * 
 * @author Rayulu
 * 
 */
public class LanguageUtil {

    /** The special characters. */
    private static String[] punctuations = new String[] { ".", ",", ";", "|", "।", "?", "!", "*", "'", "\"", "(", ")", "’", "`", "!", ":" };

    /**
     * Replaces the special characters from a given text.
     *
     * @param text the text
     * @return the string
     */
    public static String replacePunctuations(String text) {
        if (StringUtils.isNotBlank(text)) {
            for (String punctuation : punctuations) {
                text = text.replaceAll("\\" + punctuation + "", " ");
            }
        }
        return text;
    }

    /**
     * Replaces special characters and tokenizes the given text.
     *
     * @param text the text
     * @return the tokens
     */
    public static List<String> getTokens(String text) {
        List<String> tokens = new ArrayList<String>();
        text = replacePunctuations(text);
        if (StringUtils.isNotBlank(text)) {
            StringTokenizer st = new StringTokenizer(text);
            while (st.hasMoreTokens()) {
                tokens.add(st.nextToken().trim());
            }
        }
        return tokens;
    }
}
