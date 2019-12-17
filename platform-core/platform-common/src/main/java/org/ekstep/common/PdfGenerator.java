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
     * @param htmlString
     * @return
     */
    public static File convertHtmlStringToPdfFile(String htmlString) {
        File file = new File("/data/" + getPdfFileName() + ".pdf");
        try {
            file.createNewFile();
            OutputStream os = new FileOutputStream(file);
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, os);
            document.open();
            InputStream is = new ByteArrayInputStream(htmlString.getBytes());
            XMLWorkerHelper.getInstance().parseXHtml(writer, document, is);
            document.close();
            os.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File convertHtmlFileToPdfFile(File htmlFile) {
        File file = new File("/data/" + getPdfFileName() + ".pdf");
        try{
            file.createNewFile();
            Document document = new Document();
            OutputStream os = new FileOutputStream(file);
            PdfWriter writer = PdfWriter.getInstance(document, os);
            document.open();
            InputStream is = new FileInputStream(htmlFile);
            XMLWorkerHelper.getInstance().parseXHtml(writer, document, is);
            document.close();
            os.close();
            return file;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getPdfFileName() {
        return "pdf_" + System.currentTimeMillis();
    }


}
