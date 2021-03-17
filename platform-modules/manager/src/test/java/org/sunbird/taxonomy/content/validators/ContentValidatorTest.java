package org.sunbird.taxonomy.content.validators;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.validator.ContentValidator;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.engine.mgr.impl.NodeManagerImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

public class ContentValidatorTest {

    ContentValidator validator = new ContentValidator();

    @Autowired
    NodeManagerImpl node;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    public static File valid_content_package_file = new File("src/test/resources/Contents/Verbs.zip");
    public static File invalid_content_package_file = new File("src/test/resources/Contents/Verbs.jar");
    public static File invalid_content_package_structure = new File("src/test/resources/Contents/packageValidators.zip");
    public static File invalid_package_structure = new File("src/test/resources/Contents/content_validator_01.zip");
    public static File invalid_package_size = new File("src/test/resources/Contents/packageSize_validator.zip");
    public static File invalid_package_mimetype = new File("src/test/resources/Contents/Verbs");
    public static File invalid_package = new File("src/test/resources/Contents/package_validator.zip");
    public static File invalid_mimeType = new File("src/test/resources/Contents/sampleTest.epub");
    public static File valid_mimeType = new File("src/test/resources/Contents/sample.epub");
    public static File valid_plugin_structure = new File("src/test/resources/Contents/Custom_Plugin.zip");

    //checks for given input is zip file with index.ecml/index.json present at the root folder with proper package structure
    @Test
    public void validContentPackage() {
        try {
            Boolean result = validator.isValidContentPackage(valid_content_package_file);
            assertEquals(result, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //input is a file with not a zip extension
    @Test
    public void invalidContentPackage() {
        exception.expect(ClientException.class);
        exception.expectMessage(ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_FILE_MIME_TYPE_ERROR);
        validator.isValidContentPackage(invalid_content_package_file);
    }

    //input is zip file without index.ecml/index.json
    @Test
    public void invalidContentPackageStructure_01() {
        exception.expect(ClientException.class);
        exception.expectMessage(ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_STRUCTURE_ERROR);
        validator.isValidContentPackage(invalid_content_package_structure);
    }

    //input is zip file with index.ecml & index.json
    @Test
    public void invalidContentPackageStructure_02() {
        exception.expect(ClientException.class);
        exception.expectMessage(ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_STRUCTURE_ERROR);
        validator.isValidContentPackage(invalid_package_structure);
    }

    //input is zip file with filesize greater than 50mb
    @Test
    public void invalidContentPackageSize_03() {
        exception.expect(ClientException.class);
        exception.expectMessage(ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_SIZE_ERROR);
        validator.isValidContentPackage(invalid_package_size);
    }

    //input is zip file with another zip inside
    @Test
    public void invalidContentPackageStructure_04() {
        exception.expect(ClientException.class);
        exception.expectMessage(ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_STRUCTURE_ERROR);
        validator.isValidContentPackage(invalid_package);
    }

    @Test
    public void validContentNodeTest() {
        Node node = new Node();
        node.setGraphId("En");
        node.setIdentifier("org.sunbird.jul03.collection.test02");
        node.setNodeType("DATA_NODE");
        node.setObjectType("Content");
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("name", "test");
        metadata.put("code", "org.sunbird.test");
        metadata.put("mimeType", "application/vnd.ekstep.content-collection");
        node.setMetadata(metadata);
        Boolean isValid = validator.isValidContentNode(node);
        assertEquals(true, isValid);
    }

    @Test
    public void inValidContentNodeTest() {
        exception.expect(ClientException.class);
        exception.expectMessage(ContentErrorMessageConstants.MISSING_REQUIRED_FIELDS);
        Node node = new Node();
        node.setGraphId("En");
        node.setIdentifier("org.sunbird.jul03.msword.test02");
        node.setNodeType("DATA_NODE");
        node.setObjectType("Content");
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("name", "test");
        metadata.put("code", "org.sunbird.test");
        metadata.put("mimeType", "application/msword");
        node.setMetadata(metadata);
        validator.isValidContentNode(node);
    }

    @Test
    public void invalidMimeTypeCheck() {
        exception.expect(ClientException.class);
        exception.expectMessage(ContentErrorMessageConstants.INVALID_UPLOADED_FILE_EXTENSION_ERROR);
        validator.exceptionChecks("application/epub", invalid_mimeType);
    }

    @Test
    public void validMimeTypeCheck() {
        Boolean isValid = validator.exceptionChecks("application/epub", valid_mimeType);
        assertEquals(true, isValid);
    }


    @Test
    public void testValidPDFUrl() {
        Boolean isValid = validator.isValidUrl("https://ekstep-public-prod.s3-ap-south-1.amazonaws" +
                ".com/assets/do_312468653843972096217603/1_being-a-school-leader-in-india.pdf", "application/pdf");
        assertEquals(true, isValid);
    }



    @Test
    public void testValidEpubUrl() {
        Boolean isValid = validator.isValidUrl("https://ntpstagingall.blob.core.windows.net/ntp-content-staging/content/do_11273189844413644811/artifact/index.epub",
                "application/epub");
        assertEquals(true, isValid);
    }

}