package org.sunbird.taxonomy.content.common;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.gson.Gson;

public class BaseTestUtil {

	public static void writeStringToFile(String fileName, String data, boolean append) throws IOException {
		try {
			File file = new File(BaseTestUtil.class.getResource("/Contents/" + fileName).getFile());
			FileUtils.writeStringToFile(file, data, append);
		} catch (IOException e) {
			throw new IOException("Error: While writing XML string to file.", e);
		}
	}

	public static String getFileString(String fileName) {
		String fileString = "";
		File file = new File(BaseTestUtil.class.getResource("/Contents/" + fileName).getFile());
		try {
			fileString = FileUtils.readFileToString(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileString;
	}

	public static boolean isValidXmlString(String xml) {
		boolean isValid = false;
		DocumentBuilderFactory factory = null;
		DocumentBuilder builder = null;
		Document document = null;
		try {
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			document = builder.parse(new InputSource(new StringReader(xml)));
			document.getDocumentElement().normalize();
			isValid = true;
		} catch (ParserConfigurationException e) {
			isValid = false;
		} catch (SAXException e) {
			isValid = false;
		} catch (IOException e) {
			isValid = false;
		} finally {
			if (document != null) {
				document = null;
			}
		}
		return isValid;
	}

	public static boolean isValidJsonString(String json) {
		boolean isValid = false;
		Gson gson = new Gson();
		try {
			gson.fromJson(json, Object.class);
			isValid = true;
		} catch (com.google.gson.JsonSyntaxException ex) {
			isValid = false;
		}
		return isValid;
	}
}
