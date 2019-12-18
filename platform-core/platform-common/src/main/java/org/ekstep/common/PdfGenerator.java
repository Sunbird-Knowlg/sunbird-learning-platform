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
    /**
     * Converts a Html String to a Pdf file
     *
     * @param htmlString
     * @return
     */

    private static final String TEMP_FILE_LOCATION = "/tmp/";

    public static File convertHtmlStringToPdfFile(String htmlString) {
        File file = new File(TEMP_FILE_LOCATION + getPdfFileName() + ".pdf");
        OutputStream os = null;
        InputStream is = null;
        try {
            if (file.createNewFile()) {
                os = new FileOutputStream(file);
                Document document = new Document();
                PdfWriter writer = PdfWriter.getInstance(document, os);
                document.open();
                is = new ByteArrayInputStream(htmlString.getBytes());
                XMLWorkerHelper.getInstance().parseXHtml(writer, document, is);
                document.close();
            }
            return file;
        } catch (Exception e) {

            return null;
        } finally {
            try {
                if (null != os)
                    os.close();
                if(null != is)
                    is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static File convertHtmlFileToPdfFile(File htmlFile) {
        File file = new File(TEMP_FILE_LOCATION + getPdfFileName() + ".pdf");
        OutputStream os = null;
        InputStream is = null;
        try {
            if (file.createNewFile()) {
                os = new FileOutputStream(file);
                Document document = new Document();
                PdfWriter writer = PdfWriter.getInstance(document, os);
                document.open();
                is = new FileInputStream(htmlFile);
                XMLWorkerHelper.getInstance().parseXHtml(writer, document, is);
                document.close();
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (null != os)
                    os.close();
                if(null != is)
                    is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String getPdfFileName() {
        return "pdf_" + System.currentTimeMillis();
    }


}
