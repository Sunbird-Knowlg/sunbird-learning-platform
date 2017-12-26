package com.ilimi.dialcode.util;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class SeqRandomGenerator {

	private static final String[] alphabet = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C",
			"D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",
			"Z" };
	private static final String stripChars = "0";
	private static final Double length = 6.0;
	private static final BigDecimal largePrimeNumber = new BigDecimal(1679979167);

	public static Set<String> generate(double startIndex, double count) {
		Set<String> codes = new HashSet<String>();
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
				codes.add(code);
			}
			lastIndex += 1;
		}
		return codes;
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
