package org.ekstep.common;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class PdfGenerator {

    private PdfGenerator() {

    }

    private static final String TEMP_FILE_LOCATION = Platform.config.hasPath("lp.assessment.tmp_file_location") ? Platform.config.getString("lp.assessment.tmp_file_location") : "/tmp/";
    private static final String PDF_EXTENSION = ".pdf";

    /**
     * Converts a Html String to a Pdf file
     *
     * @param htmlString
     * @return
     */
    public static File convertHtmlStringToPdfFile(String htmlString, String pdfFileName) {
        try {
            return convertFile(pdfFileName, getInputStream(htmlString));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File convertHtmlFileToPdfFile(File htmlFile, String pdfFileName) {
        try {
            return convertFile(pdfFileName, getInputStream(htmlFile));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static File convertFile(String pdfFileName, InputStream is) throws Exception {
        File pdfFile = new File(TEMP_FILE_LOCATION + pdfFileName + PDF_EXTENSION);
        Document document = null;
        OutputStream os = null;
        try {
            if (pdfFile.createNewFile()) {
                document = new Document();
                os = new FileOutputStream(pdfFile);
                PdfWriter writer = PdfWriter.getInstance(document, os);
                document.open();
                XMLWorkerHelper.getInstance().parseXHtml(writer, document, is);
            }
            return pdfFile;
        } catch (Exception e) {
            throw e;
        } finally {
            if (null != document)
                document.close();
            if (null != is)
                is.close();
            if (null != os)
                os.close();
        }
    }

    /**
     * This method takes input html string or file object and returns an InputStream
     *
     * @param object
     * @return
     */
    private static InputStream getInputStream(Object object) throws Exception {
        if (object instanceof String) {
            return new ByteArrayInputStream(((String) object).getBytes());
        } else if (object instanceof File) {
            return new FileInputStream((File) object);
        }
        return null;
    }

}
