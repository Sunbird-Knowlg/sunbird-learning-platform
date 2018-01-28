package org.ekstep.dialcode.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.dialcode.store.DialCodeStore;
import org.ekstep.dialcode.store.SystemConfigStore;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DialCodeGenerator {

	@Autowired
	private DialCodeStore dialCodeStore;

	@Autowired
	private SystemConfigStore systemConfigStore;

	private static String stripChars = "0";
	private static Double length = 6.0;
	private static BigDecimal largePrimeNumber = new BigDecimal(1679979167);

	/**
	 * Get Max Index from Cassandra and Set it to Cache.
	 */
	@PostConstruct
	public void init() {
		double maxIndex;
		try {
			stripChars = Platform.config.hasPath("dialcode.strip.chars")
					? Platform.config.getString("dialcode.strip.chars")
					: stripChars;
			length = Platform.config.hasPath("dialcode.length") ? Platform.config.getDouble("dialcode.length") : length;
			largePrimeNumber = Platform.config.hasPath("dialcode.large.prime_number")
					? new BigDecimal(Platform.config.getLong("dialcode.large.prime_number"))
					: largePrimeNumber;
			maxIndex = systemConfigStore.getDialCodeIndex();
			Map<String, Object> props = new HashMap<String, Object>();
			props.put("max_index", maxIndex);
			TelemetryManager.info("setting DIAL code max index value to redis.", props);
			setMaxIndexToCache(maxIndex);
		} catch (Exception e) {
			TelemetryManager.error("fialed to set max index to redis.", e);
			// TODO: Exception handling for getDialCodeIndex SystemConfig table.
			// throw new ServerException(DialCodeErrorCodes.ERR_SERVER_ERROR,
			// DialCodeErrorMessage.ERR_SERVER_ERROR);
		}
	}

	private static final String[] alphabet = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C",
			"D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",
			"Z" };

	public Map<Double, String> generate(double count, String channel, String publisher, String batchCode)
			throws Exception {
		Map<Double, String> codes = new HashMap<Double, String>();
		double startIndex = getMaxIndex();
		int totalChars = alphabet.length;
		BigDecimal exponent = BigDecimal.valueOf(totalChars);
		exponent = exponent.pow(length.intValue());
		double codesCount = 0;
		double lastIndex = startIndex;
		while (codesCount < count) {
			lastIndex = getMaxIndex();
			BigDecimal number = new BigDecimal(lastIndex);
			BigDecimal num = number.multiply(largePrimeNumber).remainder(exponent);
			String code = baseN(num, totalChars);
			if (code.length() == length) {
				try {
					dialCodeStore.save(channel, publisher, batchCode, code, lastIndex);
					codesCount += 1;
					codes.put(lastIndex, code);
				} catch (Exception e) {
					TelemetryManager.error("Error while generating DIAL code", e);
				}
			}
		}
		setMaxIndex(lastIndex);
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
		double index = RedisStoreUtil.getNodePropertyIncVal("domain", "dialcode", "max_index");
		return index;
	}

}
