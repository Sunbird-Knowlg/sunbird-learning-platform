package org.ekstep.common.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.io.File;
import java.io.IOException;

public class PDFUtil {

	public static Integer getPageCount(File file) throws IOException {
		int count = 0;
		PDDocument pdDoc = null;
		try {
			pdDoc = PDDocument.load(file);
			count = pdDoc.getNumberOfPages();
		} catch (Exception e){
			e.printStackTrace();
			TelemetryManager.error("Error Encountered While Fetching Number Of Pages for PDF Content | Error is : "+e.getMessage(), e);
		}finally {
			if(null!=pdDoc) {
				try{
					pdDoc.close();
				}catch(Exception e){

				}
			}
		}
		return count;
	}
}
