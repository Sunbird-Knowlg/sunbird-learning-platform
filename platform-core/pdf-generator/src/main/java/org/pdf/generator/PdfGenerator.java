package org.pdf.generator;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.ekstep.common.Platform;

public class PdfGenerator {

    private PdfGenerator() {}

    private static final String TEMP_FILE_LOCATION = Platform.config.hasPath("lp.assessment.tmp_file_location") ? 
    		Platform.config.getString("lp.assessment.tmp_file_location") : 
    			"/tmp/";
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
        		if(pdfFile.exists())
        			pdfFile.delete();
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
    
    public static void main(String[] args) {
    	
    	String s = "<header>\n" + 
    			"	<style type=\"text/css\">\n" + 
    			"		.questions-paper {\n" + 
    			"			padding: 50px;\n" + 
    			"		}\n" + 
    			"		.question-title{\n" + 
    			"			\n" + 
    			"		}\n" + 
    			"		#abc {\n" + 
    			"			margin-bottom: 100px;\n" + 
    			"			margin-top: 100px;\n" + 
    			"			color: red;\n" + 
    			"		}\n" + 
    			"		.mcq-option {\n" + 
    			"			padding-left: 20px;\n" + 
    			"			line-height: 10px;\n" + 
    			"		}\n" + 
    			"		.mcq-option p:before {\n" + 
    			"		  content: '\\2022';\n" + 
    			"		  margin-right: 8px;\n" + 
    			"		}\n" + 
    			"		.answer{\n" + 
    			"			padding-left: 20px;\n" + 
    			"		}\n" + 
    			"		.answer p:before {\n" + 
    			"			content: '\\2023';\n" + 
    			"		  	margin-right: 8px;\n" + 
    			"		}\n" + 
    			"		.question-header {\n" + 
    			"			text-align: center;\n" + 
    			"\n" + 
    			"		}\n" + 
    			"		.answer-sheet{\n" + 
    			"			page-break-before: always;\n" + 
    			"		}\n" + 
    			"	</style>\n" + 
    			"</header>\n" + 
    			"<div class=\"questions-paper\">\n" + 
    			"	<div class='mcq-vertical cheveron-helper'>\n" + 
    			"		<div class=\"question-sheet\">\n" + 
    			"			<div class=\"question-header\">\n" + 
    			"				<h3>Question Sheet</h3>\n" + 
    			"			</div>\n" + 
    			"			<div class='question-title' id=\"abc\" align=\"margin-bottom:100; margin-top:100;\">\n" + 
    			"				<p>Which of the following sets of tests would be LEAST urgently indicated for a patient presenting with intraocular pressures of R 22mmHg and L 46mmHg?</p>\n" + 
    			"			</div>\n" + 
    			"			<div class='mcq-options'>\n" + 
    			"				<div data-simple-choice-interaction data-response-variable='responseValue' class='mcq-option'>\n" + 
    			"					<p>Visual field examination and dilated optic nerve head assessment</p>\n" + 
    			"				</div>\n" + 
    			"				<div data-simple-choice-interaction data-response-variable='responseValue' class='mcq-option'>\n" + 
    			"					<p>Examination of the irides and anterior chamber assessment for cells and flare</p>\n" + 
    			"				</div>\n" + 
    			"				<div data-simple-choice-interaction data-response-variable='responseValue' class='mcq-option'>\n" + 
    			"					<p>Gonioscopy and assessment of the lens capsule</p>\n" + 
    			"				</div>\n" + 
    			"				<div data-simple-choice-interaction data-response-variable='responseValue' class='mcq-option'>\n" + 
    			"					<p>Assessment of the corneal endothelium and iris transillumination</p>\n" + 
    			"				</div>\n" + 
    			"			</div>\n" + 
    			"			<div class='question-title'>\n" + 
    			"				<p>The primary cause of blindness in Australia and New Zealand for people over the age of 55 years is</p>\n" + 
    			"			</div>\n" + 
    			"			<div class='mcq-options'>\n" + 
    			"				<div data-simple-choice-interaction data-response-variable='responseValue' class='mcq-option'>\n" + 
    			"					<p>Cataract</p>\n" + 
    			"				</div>\n" + 
    			"				<div data-simple-choice-interaction data-response-variable='responseValue' class='mcq-option'>\n" + 
    			"					<p>Glaucoma</p>\n" + 
    			"				</div>\n" + 
    			"				<div data-simple-choice-interaction data-response-variable='responseValue' class='mcq-option'>\n" + 
    			"					<p>Macular degeneration</p>\n" + 
    			"				</div>\n" + 
    			"				<div data-simple-choice-interaction data-response-variable='responseValue' class='mcq-option'>\n" + 
    			"					<p>Diabetic retinopathy</p>\n" + 
    			"				</div>\n" + 
    			"			</div>\n" + 
    			"		</div>\n" + 
    			"		<div class=\"answer-sheet\">\n" + 
    			"			<div class=\"question-header\">\n" + 
    			"				<h3>Answer Sheet</h3>\n" + 
    			"			</div>\n" + 
    			"			<div class='question-title'>\n" + 
    			"				<p>Which of the following sets of tests would be LEAST urgently indicated for a patient presenting with intraocular pressures of R 22mmHg and L 46mmHg?</p>\n" + 
    			"			</div>\n" + 
    			"			<div class=\"answer\">\n" + 
    			"				<p>Gonioscopy and assessment of the lens capsule</p>\n" + 
    			"			</div>\n" + 
    			"			<div class='question-title'>\n" + 
    			"				<p>The primary cause of blindness in Australia and New Zealand for people over the age of 55 years is</p>\n" + 
    			"			</div>\n" + 
    			"			<div class=\"answer\">\n" + 
    			"				<p>Diabetic retinopathy</p>\n" + 
    			"			</div>\n" + 
    			"		</div>\n" + 
    			"	</div>\n" + 
    			"</div>";
    	File f = convertHtmlStringToPdfFile(s, "abc");
    	System.out.println(f.getPath());
    }

}
