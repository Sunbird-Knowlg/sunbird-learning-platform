package org.ekstep.language.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ParserUtil {
	
	
	public static String getFormattedDateTime(long dateTime) {
		String dateTimeString = "";
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(dateTime);
			dateTimeString = formatter.format(cal.getTime());
		} catch (Exception e) {
			dateTimeString = "";
		}
		return dateTimeString;
	}
}
