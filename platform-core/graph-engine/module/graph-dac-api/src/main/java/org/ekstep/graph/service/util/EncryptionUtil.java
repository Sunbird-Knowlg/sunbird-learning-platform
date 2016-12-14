package org.ekstep.graph.service.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.*;

public class EncryptionUtil {

	private static Logger LOGGER = LogManager.getLogger(EncryptionUtil.class.getName());

	private static final String ALGO = "AES";
	private static final byte[] keyValue = new byte[] { 'T', 'h', 'e', 'B', 'e', 's', 't', 'S', 'e', 'c', 'r', 'e', 't',
			'K', 'e', 'y' };

	@SuppressWarnings("restriction")
	public static String encrypt(String data) {
		LOGGER.debug("Data: ", data);

		String encryptedValue = "";
		try {
			Key key = generateKey();
			LOGGER.info("Key: ", key);
			Cipher c = Cipher.getInstance(ALGO);
			c.init(Cipher.ENCRYPT_MODE, key);
			byte[] encVal = c.doFinal(data.getBytes());
			encryptedValue = new BASE64Encoder().encode(encVal);
			LOGGER.info("E_Value: " + encryptedValue);
		} catch (Exception e) {
			LOGGER.error("Error! While Encrypting Data.", e);
		}
		LOGGER.debug("Returning E_DATA: ", encryptedValue);
		return encryptedValue;
	}

	@SuppressWarnings("restriction")
	public static String decrypt(String encryptedData) {
		LOGGER.debug("Encrypted Data: ", encryptedData);

		String decryptedValue = "";
		try {
			Key key = generateKey();
			LOGGER.info("Key: ", key);
			Cipher c = Cipher.getInstance(ALGO);
			c.init(Cipher.DECRYPT_MODE, key);
			byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
			byte[] decValue = c.doFinal(decordedValue);
			decryptedValue = new String(decValue);
			LOGGER.info("D_Value: " + decryptedValue);
		} catch (Exception e) {
			LOGGER.error("Error! While Decrypting Values.", e);
		}
		LOGGER.debug("Returning D_DATA: ", decryptedValue);
		return decryptedValue;
	}

	private static Key generateKey() throws Exception {
		Key key = new SecretKeySpec(keyValue, ALGO);
		return key;
	}

}
