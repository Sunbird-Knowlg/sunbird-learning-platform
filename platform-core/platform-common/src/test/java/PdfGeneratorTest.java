import org.ekstep.common.PdfGenerator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class PdfGeneratorTest {
    private File htmlFile;
    private String htmlString;
    private String malFormedHtmlString;
    private File htmlNonExistentFile;
    private File pdfFile;
    private static final String PDF_EXTENSION = "pdf";

    @Before
    public void createInputFile() {
        htmlFile = new File(System.getProperty("user.dir") + "/src/test/resources/test.html");
        htmlNonExistentFile = new File("test.html");
        htmlString = "<table style=\"width:100%\">\n" +
                "    <caption>Monthly savings</caption>\n" +
                "    <tr>\n" +
                "        <th>Month</th>\n" +
                "        <th>Savings</th>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "        <td>January</td>\n" +
                "        <td>$100</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "        <td>February</td>\n" +
                "        <td>$50</td>\n" +
                "    </tr>\n" +
                "</table>";
        malFormedHtmlString = "<table style=\"width:100%\">\n" +
                "    <caption>Monthly savings</caption>\n" +
                "    <tr>\n" +
                "        <th>Month</th>\n" +
                "        <th>Savings</th>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "        <td>January</td>\n" +
                "        <td>$100</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "        <td>February</td>\n" +
                "        <td>$50</td>\n" +
                "    </tr>\n" +
                "</";
    }

    @After
    public void deleteOutputFile() {
        if (pdfFile != null && pdfFile.exists())
            pdfFile.delete();
        htmlString = null;
        malFormedHtmlString = null;
    }

    @Test
    public void convertHtmlStringToPdfFile() {
        pdfFile = PdfGenerator.convertHtmlStringToPdfFile(htmlString);
        Assert.assertNotNull(pdfFile);
        Assert.assertEquals(PDF_EXTENSION, pdfFile.getName().split("\\.")[1]);

    }

    @Test
    public void convertHtmlFileToPdfFile() {
        pdfFile = PdfGenerator.convertHtmlFileToPdfFile(htmlFile);
        Assert.assertNotNull(pdfFile);
        Assert.assertEquals(PDF_EXTENSION, pdfFile.getName().split("\\.")[1]);
    }

    @Test
    public void convertHtmlStringToPdfFileMalformedHtml() {
        pdfFile = PdfGenerator.convertHtmlStringToPdfFile(malFormedHtmlString);
        Assert.assertNull(pdfFile);
    }

    @Test
    public void convertHtmlFileToPdfFileDoesntExist() {
        pdfFile = PdfGenerator.convertHtmlFileToPdfFile(htmlNonExistentFile);
        Assert.assertNull(pdfFile);
    }

}
