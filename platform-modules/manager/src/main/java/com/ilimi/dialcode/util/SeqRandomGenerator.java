package com.ilimi.dialcode.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class SeqRandomGenerator {

	private static final String[] alphabet = new String[] { "0","1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B",
			"C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N","O", "P", "Q", "R", "S", "T", "U", "V", "W",
			"X", "Y", "Z" };
	private static final String stripChars = "0";
	private static final BigDecimal largePrimeNumber = new BigDecimal(1679979167);

	public static Set<String> generate(double startIndex, double count) {
		Set<String> codes = new HashSet<String>();
		int totalChars = alphabet.length;
		Double length = Math.sqrt(totalChars);
		BigDecimal exponent = BigDecimal.valueOf(totalChars);
		exponent = exponent.pow(length.intValue());
		for (double i = startIndex; i < startIndex + count; i++) {
			BigDecimal number = new BigDecimal(i);
			BigDecimal num = number.multiply(largePrimeNumber).remainder(exponent);
			String code = baseN(num, totalChars);
			codes.add(code);
		}
		return codes;
	}

	private static String baseN(BigDecimal num, int base) {
		if (num.doubleValue() == 0) {
			return "0";
		}
		double div = Math.floor(num.doubleValue() / base);
		return StringUtils.stripStart(baseN(new BigDecimal(div), base), stripChars)
				+ alphabet[num.remainder(new BigDecimal(base)).intValue()];
	}
	
	/*public static void main(String[] args) {
		int count = 103;
		Map<Integer,String> codeMap=new HashMap<Integer,String>();
		Set<String> codes=generate(103,100);
		for(String code:codes){
			codeMap.put(count++, code);
			
		}
		System.out.println("Map Size:"+codeMap.size());
		System.out.println("codeMap:::"+codeMap);
		
	}*/
}
