package org.ekstep.graph.service.util;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.ekstep.telemetry.logger.PlatformLogger;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class EncryptionUtil {

	private static final String ALGO = "AES";
	private static final byte[] keyValue = new byte[] { 'T', 'h', 'e', 'B', 'e', 's', 't', 'S', 'e', 'c', 'r', 'e', 't',
			'K', 'e', 'y' };

	@SuppressWarnings("restriction")
	public static String encrypt(String data) {
		PlatformLogger.log("Data: ", data);

		String encryptedValue = "";
		try {
			Key key = generateKey();
			PlatformLogger.log("Key: ", key);
			Cipher c = Cipher.getInstance(ALGO);
			c.init(Cipher.ENCRYPT_MODE, key);
			byte[] encVal = c.doFinal(data.getBytes());
			encryptedValue = new BASE64Encoder().encode(encVal);
			PlatformLogger.log("E_Value: " + encryptedValue);
		} catch (Exception e) {
			PlatformLogger.log("Error! While Encrypting Data.", null, e);
		}
		PlatformLogger.log("Returning E_DATA: ", encryptedValue);
		return encryptedValue;
	}

	@SuppressWarnings("restriction")
	public static String decrypt(String encryptedData) {
		PlatformLogger.log("Encrypted Data: ", encryptedData);

		String decryptedValue = "";
		try {
			Key key = generateKey();
			PlatformLogger.log("Key: ", key);
			Cipher c = Cipher.getInstance(ALGO);
			c.init(Cipher.DECRYPT_MODE, key);
			byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
			byte[] decValue = c.doFinal(decordedValue);
			decryptedValue = new String(decValue);
			PlatformLogger.log("D_Value: " + decryptedValue);
		} catch (Exception e) {
			PlatformLogger.log("Error! While Decrypting Values.", null, e);
		}
		PlatformLogger.log("Returning D_DATA: ", decryptedValue);
		return decryptedValue;
	}

	private static Key generateKey() throws Exception {
		Key key = new SecretKeySpec(keyValue, ALGO);
		return key;
	}

}
