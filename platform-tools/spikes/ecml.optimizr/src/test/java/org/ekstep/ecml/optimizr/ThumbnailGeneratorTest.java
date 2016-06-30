/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.ecml.optimizr;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import junit.framework.TestCase;

/**
 * Test cases for the thumbnail generator utility. This test case depends upon
 * the resources being available. If the resources are modified, the test cases
 * will fail.
 *
 * @author feroz
 */
public class ThumbnailGeneratorTest extends TestCase {

    /**
     * Thumbnail generation by giving file name
     */
    public void testSimpleFileName() {
        String file = getFilePath("/images/input1.png");
        boolean done = ThumbnailGenerator.generate(file);
        assertTrue(done);
        assertTrue(exists("/images/input1.thumb.png"));

        // cleanup
        deleteFile("/images/input1.thumb.png");
    }

    /**
     * Thumbnail generation by giving File object
     */
    public void testSimpleFile() {
        File file = getFile("/images/input1.png");
        boolean done = ThumbnailGenerator.generate(file);
        assertTrue(done);
        assertTrue(exists("/images/input1.thumb.png"));

        // cleanup
        deleteFile("/images/input1.thumb.png");
    }

    /**
     * Thumbnail generation by giving input and output both
     */
    public void testSimpleFileOut() {
        File file = getFile("/images/input1.png");
        File ofile = getFile("/images/", "input1.min.png");
        System.out.println(ofile.getAbsolutePath());
        boolean done = ThumbnailGenerator.generate(file, ofile);
        assertTrue(done);
        assertFalse(exists("/images/input1.thumb.png"));
        assertTrue(exists("/images/input1.min.png"));

        // cleanup
        deleteFile("/images/input1.thumb.png");
        deleteFile("/images/input1.min.png");
    }

    /**
     * Small files should not generate the thumbnail
     */
    public void testSmallFile() {
        File file = getFile("/images/small.png");
        boolean done = ThumbnailGenerator.generate(file);
        assertFalse(done);
        assertFalse(exists("/images/small.thumb.png"));
    }

    /**
     * Recursive thumbnail generation of all images in a directory
     */
    public void testDirectory() {
        File dir = getFile("/images");
        int count = ThumbnailGenerator.process(dir);
        assertEquals(count, 3);
        assertFalse(exists("/images/small.thumb.png"));
        assertTrue(exists("/images/input1.thumb.png"));
        assertTrue(exists("/images/input2.thumb.png"));
        assertTrue(exists("/images/nested/input3.thumb.png"));
    }

    /**
     * Helper method to delete a given file
     * @param name File name to delete from classpath
     */
    private void deleteFile(String name) {
        URL url = ThumbnailGenerator.class.getResource(name);
        if (url != null) {
            try {
                File file = new File(url.toURI());
                if (file.exists()) {
                    file.delete();
                }
            } catch (URISyntaxException ex) {
                // No op
            }

        }
    }

    /**
     * Helper method to locate a file in a given directory. File may not exist.
     * @param dir Directory in the classpath
     * @param name File name
     * @return File object pointinting to the given directory and file
     */
    private File getFile(String dir, String name) {
        URL url = ThumbnailGenerator.class.getResource(dir);
        String path = url.getPath();
        String fileName = path + File.separator + name;
        File file = new File(fileName);
        return file;
    }

    /**
     * Returns the File object for a given file name. Uses the class path to search the file
     * @param name File name from the classpath
     * @return File object
     */
    private File getFile(String name) {
        URL url = ThumbnailGenerator.class.getResource(name);
        File file = null;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException ex) {
            // No op
        }
        return file;
    }

    /**
     * Returns the file path string - looks for the file in the classpath
     * @param name File name in the classpath
     * @return Absolute path to the file
     */
    private String getFilePath(String name) {
        URL url = ThumbnailGenerator.class.getResource(name);
        return url.getPath();
    }

    /**
     * Returns true if the given file exists in the classpath
     * @param fileName File to search for in the classpath
     * @return True if the file exists, false otherwise
     */
    private boolean exists(String fileName) {
        try {
            File file = getFile(fileName);
            return file.exists();
        } catch (Exception ex) {
            return false;
        }
    }
}
