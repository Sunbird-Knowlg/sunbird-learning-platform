package org.ekstep.dialcode.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ServerException;
import org.ekstep.dialcode.common.DialCodeErrorCodes;
import org.ekstep.dialcode.common.DialCodeErrorMessage;
import org.ekstep.dialcode.enums.DialCodeEnum;
import org.ekstep.dialcode.store.DialCodeStore;
import org.ekstep.dialcode.store.SystemConfigStore;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SeqRandomGenerator {

	@Autowired
	private DialCodeStore dialCodeStore;

	@Autowired
	private SystemConfigStore systemConfigStore;

	/**
	 * Get Max Index from Cassandra and Set it to Cache.
	 */
	@PostConstruct
	public void initMaxIndex() {
		double maxIndex;
		try {
			maxIndex = systemConfigStore.getDialCodeIndex();
			setMaxIndexToCache(maxIndex);
		} catch (Exception e) {
			throw new ServerException(DialCodeErrorCodes.ERR_SERVER_ERROR, DialCodeErrorMessage.ERR_SERVER_ERROR);
		}
	}

	private static final String[] alphabet = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C",
			"D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",
			"Z" };

	private static final String stripChars = Platform.config.getString("dialcode.strip.chars");
	private static final Double length = Platform.config.getDouble("dialcode.length");
	private static final BigDecimal largePrimeNumber = new BigDecimal(
			Platform.config.getInt("dialcode.large.prime_number"));

	public Map<Double, String> generate(double count, Map<String, Object> map) throws Exception {
		Map<Double, String> codes = new HashMap<Double, String>();
		double startIndex = getMaxIndex();
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
				try {
					dialCodeStore.save((String) map.get(DialCodeEnum.channel.name()),
							(String) map.get(DialCodeEnum.publisher.name()),
							(String) map.get(DialCodeEnum.batchCode.name()), code, lastIndex);
					codesCount += 1;
					codes.put(lastIndex, code);
				} catch (Exception e) {

				}
				if (codesCount == count) {
					setMaxIndex(lastIndex);
				}
			}
			if (codesCount < count)
				lastIndex = getMaxIndex();
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

	private void setMaxIndex(double maxIndex) throws Exception {
		systemConfigStore.setDialCodeIndex(maxIndex);
	}

	/**
	 * @param maxIndex
	 */
	public static void setMaxIndexToCache(Double maxIndex) {
		RedisStoreUtil.saveNodeProperty("domain", "dialcode", "max_index", maxIndex.toString());
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private Double getMaxIndex() throws Exception {
		String indexStr = RedisStoreUtil.getNodeProperty("domain", "dialcode", "max_index");
		if (StringUtils.isNotBlank(indexStr)) {
			double index = Double.parseDouble(indexStr);
			++index;
			setMaxIndexToCache(index);
			return index;
		} else {
			double maxIndex = systemConfigStore.getDialCodeIndex();
			return maxIndex;
		}
	}

}
