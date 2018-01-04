package org.ekstep.dialcode.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.dialcode.model.DialCodesBatch;

public class SeqRandomGenerator {

	private static final String[] alphabet = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C",
			"D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",
			"Z" };
	private static final String stripChars = "0";
	private static final Double length = 6.0;
	private static final BigDecimal largePrimeNumber = new BigDecimal(1679979167);

	public static DialCodesBatch generate(double startIndex, double count) {
		DialCodesBatch dialCodesBatch = new DialCodesBatch();
		Map<Double, String> codes = new HashMap<Double, String>();
		int totalChars = alphabet.length;
		BigDecimal exponent = BigDecimal.valueOf(totalChars);
		exponent = exponent.pow(length.intValue());
		double codesCount = 0;
		double lastIndex = startIndex;
		while (codesCount < count) {
			BigDecimal number = new BigDecimal(lastIndex);
			BigDecimal num = number.multiply(largePrimeNumber).remainder(exponent);
			String code = baseN(num, totalChars);
			if (code.length() == length) {
				codesCount += 1;
				codes.put(lastIndex, code);
			}
			lastIndex += 1;
		}
		dialCodesBatch.setDialCodes(codes);
		dialCodesBatch.setMaxIndex(lastIndex);
		return dialCodesBatch;
	}

	private static String baseN(BigDecimal num, int base) {
		if (num.doubleValue() == 0) {
			return "0";
		}
		double div = Math.floor(num.doubleValue() / base);
		String val = baseN(new BigDecimal(div), base);
		return StringUtils.stripStart(val, stripChars) + alphabet[num.remainder(new BigDecimal(base)).intValue()];
	}

}
