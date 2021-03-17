package org.sunbird.common;

import java.io.File;
import java.net.URLDecoder;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import net.sf.junidecode.Junidecode;

public class Slug {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-\\.]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern DUPDASH = Pattern.compile("-+");

    public static void main(String[] args) {
        System.out.println(makeSlug("Cov-e*r+I/ma.ge.png", true));
    }

    public static File createSlugFile(File f) {
        try {
            String name = f.getName();
            String slug = Slug.makeSlug(name, true);
            if (!StringUtils.equals(name, slug)) {
                String path = FilenameUtils.getFullPath(f.getAbsolutePath());
                String newName = path + File.separator + slug;
                File newFile = new File(newName);
                f.renameTo(newFile);
                f = newFile;
                return newFile;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return f;
    }

    public static String makeSlug(String input) {
        // Default is slugging without transliteration
        return makeSlug(input, false);
    }

    public static String makeSlug(String input, boolean transliterate) {
        String origInput = input;
        // Validate the input
        if (input == null)
            throw new IllegalArgumentException("Input is null");
        // Remove extra spaces
        input = input.trim();
        // Remove URL encoding
        input = urlDecode(input);
        // If transliterate is required
        if (transliterate) {
            // Tranlisterate & cleanup
            String transliterated = transliterate(input);
            //transliterated = removeDuplicateChars(transliterated);
            input = transliterated;
        }
        // Replace all whitespace with dashes
        input = WHITESPACE.matcher(input).replaceAll("-");
        // Remove all accent chars
        input = Normalizer.normalize(input, Form.NFD);
        // Remove all non-latin special characters
        input = NONLATIN.matcher(input).replaceAll("");
        // Remove any consecutive dashes
        input = normalizeDashes(input);
        // Validate before returning
        validateResult(input, origInput);
        // Slug is always lowercase
        return input.toLowerCase(Locale.ENGLISH);
    }

    private static void validateResult(String input, String origInput) {
        // Check if we are not left with a blank
        if (input.length() == 0)
            throw new IllegalArgumentException("Failed to cleanup the input " + origInput);
        // Check if we are not left with a blank file name or extension
        // #686 - remove the validation check for names starting and ending with "."
//        if (input.startsWith("."))
//            throw new IllegalArgumentException("Failed to cleanup the file name " + origInput);
//        if (input.endsWith("."))
//            throw new IllegalArgumentException("Failed to cleanup the file extension " + origInput);
    }

    public static String transliterate(String input) {
        return Junidecode.unidecode(input);
    }

    public static String urlDecode(String input) {
        try {
            input = URLDecoder.decode(input, "UTF-8");
        } catch (Exception ex) {
            // Suppress
        }
        return input;
    }

    public static String removeDuplicateChars(String text) {
        StringBuilder ret = new StringBuilder(text.length());
        if (text.length() == 0) {
            return "";
        }
        ret.append(text.charAt(0));
        for (int i = 1; i < text.length(); i++) {
            if (text.charAt(i) != text.charAt(i - 1)) {
                ret.append(text.charAt(i));
            }
        }
        return ret.toString();
    }

    public static String normalizeDashes(String text) {
        String clean = DUPDASH.matcher(text).replaceAll("-");
        // Special case that only dashes remain
        if (clean.equals("-") || clean.equals("--"))
            return "";
        int startIdx = (clean.startsWith("-") ? 1 : 0);
        int endIdx = (clean.endsWith("-") ? 1 : 0);
        clean = clean.substring(startIdx, (clean.length() - endIdx));
        return clean;
    }
}
