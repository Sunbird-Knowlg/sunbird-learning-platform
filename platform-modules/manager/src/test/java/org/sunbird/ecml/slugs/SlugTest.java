
package org.sunbird.ecml.slugs;

import org.sunbird.common.Slug;

import junit.framework.TestCase;

/**
 *
 * @author feroz
 */
public class SlugTest extends TestCase {

    public void testSimple() {
        String name = "HelloWorld";
        String expected = "helloworld";
        String slug = Slug.makeSlug(name);
        assertEquals(expected, slug);

        // File name
        name = "Hello.mp3";
        expected = "hello.mp3";
        slug = Slug.makeSlug(name);
        assertEquals("File name lowercase not correct", expected, slug);

        // File name
        name = "Hello.PNG";
        expected = "hello.png";
        slug = Slug.makeSlug(name);
        assertEquals("File extension lowercase not correct", expected, slug);

        // File name
        name = "Hello.PNG###^^^";
        expected = "hello.png";
        slug = Slug.makeSlug(name);
        assertEquals("File extension lowercase not correct", expected, slug);

        // File name
        name = "हाथी.Mp3###^^^";
        expected = "haathii.mp3";
        slug = Slug.makeSlug(name, true);
        assertEquals("Transliterate file name is not correct", expected, slug);

        // File name
        // #TODO: need to fix this. The code changes not done correctly.
//        name = "^^&^###@.Mp3###^^^";
//        try {
//            slug = Slug.makeSlug(name, true);
//            System.out.println("slug=" + slug);
//            fail("Invalid file name did not throw error");
//        }
//        catch (IllegalArgumentException ex) {}

        // File extension
//        name = "^^&^###@hello.###^^^";
//        try {
//            slug = Slug.makeSlug(name, true);
//            System.out.println("slug=" + slug);
//            fail("Invalid file extension did not throw error");
//        }
//        catch (IllegalArgumentException ex) {}

        // CSS File name
        name = "keyboard.css###^^^";
        expected = "keyboard.css";
        slug = Slug.makeSlug(name, true);
        assertEquals("File extension not correctly handled", expected, slug);
    }

    public void testSpaces() {
        // Single space
        String name = "Hello World";
        String expected = "hello-world";
        String slug = Slug.makeSlug(name);
        assertEquals("Single space not sluggified correctly", expected, slug);

        // Consecutive spaces
        name = "Hello      World";
        expected = "hello-world";
        slug = Slug.makeSlug(name);
        assertEquals("Consecutive spaces not sluggified correctly", expected, slug);

        // Consecutive spaces
        name = "Hello      Wor ld";
        expected = "hello-wor-ld";
        slug = Slug.makeSlug(name);
        assertEquals("Multiple spaces not sluggified correctly", expected, slug);

        // Leading and trailing spaces
        name = "   Hello      Wor ld   ";
        expected = "hello-wor-ld";
        slug = Slug.makeSlug(name);
        assertEquals("Leading and trailing spaces not sluggified correctly", expected, slug);
    }

    public void testSpecialChars() {
        // Simple special characters
        String name = "Nia's Stories";
        String expected = "nias-stories";
        String slug = Slug.makeSlug(name);
        assertEquals("Simple special chars not sluggified correctly", expected, slug);

        // Consecutive special characters
        name = "### ### ^^^ Hello,     / % world ###";
        expected = "hello-world";
        slug = Slug.makeSlug(name);
        assertEquals("Consecutive special chars and spaces not sluggified correctly", expected, slug);

        // Combination of special characters and spaces
        name = "He/llo      Wor ld !!!";
        expected = "hello-wor-ld";
        slug = Slug.makeSlug(name);
        assertEquals("Multiple special chars not sluggified correctly", expected, slug);

        // Pipe character should also be removed
        name = "Hello|World";
        expected = "helloworld";
        slug = Slug.makeSlug(name);
        assertEquals("Pipe character not sluggified correctly", expected, slug);
    }

    public void testUrlChars() {
        // Single URL encoded character
        String name = "Hello%20World!!";
        String expected = "hello-world";
        String slug = Slug.makeSlug(name);
        assertEquals("URL encoding not sluggified correctly", expected, slug);

        // Consecutive spaces and URL encoded characters
        name = "Hello!!!  %20    World";
        expected = "hello-world";
        slug = Slug.makeSlug(name);
        assertEquals("Consecutive special chars and spaces not sluggified correctly", expected, slug);

        // Consecutive URL encoded characters
        name = "Hello!!!  %20%20%20    World";
        expected = "hello-world";
        slug = Slug.makeSlug(name);
        assertEquals("Consecutive url encoded chars and spaces not sluggified correctly", expected, slug);

        // Consecutive spaces
        name = "He/llo  %20    Wor ld";
        expected = "hello-wor-ld";
        slug = Slug.makeSlug(name);
        assertEquals("Multiple special chars not sluggified correctly", expected, slug);
    }

    public void testAccentChars() {
        // Accent chars with other special chars
        String name = "Há, é, í, ó, úllo%20World!!";
        String expected = "ha-e-i-o-ullo-world";
        String slug = Slug.makeSlug(name);
        assertEquals("Accent char snot sluggified correctly", expected, slug);

        // Accent chars
        name = "á, é, í, ó, ú";
        expected = "a-e-i-o-u";
        slug = Slug.makeSlug(name);
        assertEquals("Accent chars special chars and spaces not sluggified correctly", expected, slug);

        // Consecutive accent chars
        name = "áéíóú";
        expected = "aeiou";
        slug = Slug.makeSlug(name);
        assertEquals("Multiple accent chars not sluggified correctly", expected, slug);
    }

    public void testTransliteration() {
        // Hindi
        String name = "हाथी और भालू की शादी";
        String expected = "haathii-aur-bhaaluu-kii-shaadii";
        String slug = Slug.makeSlug(name, true);
        assertEquals("Hindi sample transliteration correctly", expected, slug);

        // Kannada
        name = "ಕನ್ನಡ  ರೈಮ್ಸ್";;
        expected = "knndd-raims";
        slug = Slug.makeSlug(name, true);
        assertEquals("Accent chars special chars and spaces not sluggified correctly", expected, slug);

        // Consecutive accent chars
        name = "ಮೊಲ  ಮತ್ತು ಆಮೆಯ ಕಥೆ";;
        expected = "mol-mttu-aamey-kthe";
        slug = Slug.makeSlug(name, true);
        assertEquals("Multiple accent chars not sluggified correctly", expected, slug);
    }

    public void testGarbage() {

        String name = "            ";
        try {
            String slug = Slug.makeSlug(name, true);
            System.out.println("slug=" + slug);
            fail("Only spaces did not throw error");
        }
        catch (IllegalArgumentException ex) {}

        name = "   ,/^^^%%%*** ####         ";
        try {
            String slug = Slug.makeSlug(name, true);
            System.out.println("slug=" + slug);
            fail("Junk characters did not throw error");
        }
        catch (IllegalArgumentException ex) {}

        name = "";
        try {
            String slug = Slug.makeSlug(name, true);
            System.out.println("slug=" + slug);
            fail("Empty input did not throw error");
        }
        catch (IllegalArgumentException ex) {}

        name = null;
        try {
            String slug = Slug.makeSlug(name, true);
            System.out.println("slug=" + slug);
            fail("Empty input did not throw error");
        }
        catch (IllegalArgumentException ex) {}
    }
}
