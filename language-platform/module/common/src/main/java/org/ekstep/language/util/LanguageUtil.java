package org.ekstep.language.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

public class LanguageUtil {

    private static String[] punctuations = new String[] { ".", ",", ";", "|", "।", "?", "!", "*" };

    public static String replacePunctuations(String text) {
        if (StringUtils.isNotBlank(text)) {
            for (String punctuation : punctuations) {
                text = text.replaceAll("\\" + punctuation + "", " ");
            }
        }
        return text;
    }

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
