package org.sunbird.taxonomy.content.concrete.processor;

import org.apache.commons.io.FileUtils;
import org.sunbird.common.exception.ClientException;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.concrete.processor.AssetsLicenseValidatorProcessor;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.util.ECRFConversionUtility;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Unit Test Cases for {@link AssetsLicenseValidatorProcessor#process(Plugin)}
 *
 * @see AssetsLicenseValidatorProcessor
 */
public class AssetsLicenseValidatorProcessorTest {

    private final static File tmpFolder = new File("/data/ContentBundleTest/local");

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void initializeTempFolder() {
        try {
            FileUtils.deleteQuietly(tmpFolder);
            FileUtils.touch(tmpFolder);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public String getFileString(String fileName) {
        String fileString = "";
        File file = new File(getClass().getResource("/Contents/" + fileName).getFile());
        try {
            fileString = FileUtils.readFileToString(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileString;
    }

    @Test
    public void licenseValidationForYoutubeMediaInEcmlWithSupportedLicense() {
        try {
            ECRFConversionUtility fixture = new ECRFConversionUtility();
            String strContent = getFileString("testEcmlMediaYoutube/index.ecml");
            Plugin plugin = fixture.getECRF(strContent);
            PipelineRequestorClient.getPipeline("assetsLicenseValidatorProcessor", tmpFolder.getPath(), "TestEcmlContent")
                    .execute(plugin);
        } catch (Exception e) {
            assertEquals(ContentErrorMessageConstants.LICENSE_NOT_SUPPORTED, e.getMessage());
        }
    }

    @Test
    public void licenseValidationForYoutubeMediaInEcmlWithUnsupportedLicense() {
        exception.expect(ClientException.class);
        exception.expectMessage(ContentErrorMessageConstants.LICENSE_NOT_SUPPORTED);
        try {
            ECRFConversionUtility fixture = new ECRFConversionUtility();
            String strContent = getFileString("testEcmlMediaYoutube/index_with_unsupported_youtube_license.ecml");
            Plugin plugin = fixture.getECRF(strContent);
            PipelineRequestorClient.getPipeline("assetsLicenseValidatorProcessor", tmpFolder.getPath(), "TestEcmlContent")
                    .execute(plugin);
        } catch (Exception e) {
            assertEquals(ContentErrorMessageConstants.LICENSE_NOT_SUPPORTED, e.getMessage());
            throw e;
        }
    }

}
