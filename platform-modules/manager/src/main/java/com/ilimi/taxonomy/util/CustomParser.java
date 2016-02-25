package com.ilimi.taxonomy.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.enums.ContentErrorCodes;

public class CustomParser {

	private Document doc;

	private static String fileNameInURL[] = null;
	private static String fileNameWithExtn = null;

	/**
	 * This method read ECML file
	 * 
	 * @param file
	 *            path for index.ecml
	 * @return
	 * */

	public CustomParser() {
	}

	public CustomParser(File xmlFile) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();
			this.doc = doc;
		} catch (Exception e) {
			throw new ServerException(
					ContentErrorCodes.ERR_CONTENT_EXTRACT.name(),
					e.getMessage());
		}
	}

	public static Map<String, List<String>> readECMLFile(String filePath) {
		final Map<String, List<String>> mediaIdMap = new HashMap<String, List<String>>();
		// final String saveDir1 = saveDir;
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new DefaultHandler() {
				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException {
					if (qName.equalsIgnoreCase("media")) {
						String id = attributes.getValue("id");
						if (StringUtils.isNotBlank(id)) {
							String src = attributes.getValue("src");
							if (StringUtils.isNotBlank(src)) {
								String assetId = attributes.getValue("assetId");
								List<String> mediaValues = new ArrayList<String>();
								mediaValues.add(src);
								mediaValues.add(assetId);
								mediaIdMap.put(id, mediaValues);
							}
						}
					}
				}

				public void endElement(String uri, String localName,
						String qName) throws SAXException {
					// System.out.println("End Element :" + qName);
				}
			};
			saxParser.parse(filePath, handler);
		} catch (Exception e) {
			throw new ServerException(
					ContentErrorCodes.ERR_CONTENT_EXTRACT.name(),
					e.getMessage());
		}
		return mediaIdMap;
	}

	public void updateEcml(String filePath) {
		String filePath1 = filePath + File.separator + "index.ecml";
		String assetDir = filePath + File.separator + "assets";
		File file1 = new File(assetDir);
		if (!file1.exists()) {
			file1.mkdir();
		}
		try {
			NodeList medias = doc.getElementsByTagName("media");
			Element media = null;
			for (int i = 0; i < medias.getLength(); i++) {
				media = (Element) medias.item(i);
				String src = media.getAttribute("src");
				HttpDownloadUtility.downloadFile(src, assetDir);
				fileNameInURL = src.split("/");
				fileNameWithExtn = fileNameInURL[fileNameInURL.length - 1];
				System.out.println(src);
				media.setAttribute("src", fileNameWithExtn);
			}
			doc.getDocumentElement().normalize();
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(filePath1));
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new ServerException(
					ContentErrorCodes.ERR_CONTENT_PUBLISH.name(),
					e1.getMessage());
		}
	}

	public void updateSrcInEcml(String filePath,
			Map<String, List<String>> mediaIdURLMap) {
		try {
			doc.getDocumentElement().normalize();
			NodeList medias = doc.getElementsByTagName("media");
			Element media = null;
			for (int i = 0; i < medias.getLength(); i++) {
				media = (Element) medias.item(i);
				if (mediaIdURLMap != null && !mediaIdURLMap.isEmpty()) {
					String mediaId = media.getAttribute("id");
					if (mediaIdURLMap.containsKey(mediaId)) {
						List<String> list = mediaIdURLMap.get(mediaId);
						if (null != list && list.size() == 2) {
							String url = list.get(0);
							if (StringUtils.isNotBlank(url))
								media.setAttribute("src", url);
							String assetId = list.get(1);
							if (StringUtils.isNotBlank(assetId))
								media.setAttribute("assetId", assetId);
						}
					}
				}
			}
			doc.getDocumentElement().normalize();
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(filePath));
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new ServerException(
					ContentErrorCodes.ERR_CONTENT_EXTRACT.name(),
					e1.getMessage());
		}
	}

	/**
	 * Read any type of file
	 * 
	 * @author Rajiv
	 * @param File
	 *            type
	 * @return text, String Type
	 * */
	public static String readFile(File file) {
		String text = "";
		try (FileInputStream fis = new FileInputStream(file);) {
			text = IOUtils.toString(fis, StandardCharsets.UTF_8.name());
		} catch (IOException io) {
			io.printStackTrace();
		}
		return text;
	}

	/**
	 * This Method Copy Data and Item Json into ecml as CDATA
	 * 
	 * @author Rajiv
	 * @param filePath
	 * @param type
	 *            : items or data
	 * **/
	public void updateJsonInEcml(String filePath, List<String> listOfCtrlType) {
		for (String type : listOfCtrlType) {
			try {
				NodeList attrList = doc.getElementsByTagName("controller");
				for (int i = 0; i < attrList.getLength(); i++) {
					Element controller = (Element) attrList.item(i);
					if (controller.getAttribute("type").equalsIgnoreCase(type)) {
						controller = (Element) attrList.item(i);
						File file = new File(filePath);
						String nameOfJsonFile = controller.getAttribute("id");
						String itemJsonPath = file.getParent() + File.separator
								+ type + File.separator + nameOfJsonFile
								+ ".json";
						File jsonFile = new File(itemJsonPath);
						if (jsonFile.exists()) {
							if (isJSONValid(jsonFile)) {
								controller
										.appendChild(doc
												.createCDATASection(readFile(jsonFile)));
							} else {
								throw new ServerException(
										ContentErrorCodes.ERR_CONTENT_JSON_INVALID
												.name(), "Json invalid for"
												+ type);
							}
						}
					}
				}
				doc.getDocumentElement().normalize();
				TransformerFactory transformerFactory = TransformerFactory
						.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File(filePath));
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.transform(source, result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isJSONValid(File jsonFile) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			mapper.readTree(jsonFile);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void readJsonFileDownload(String filePath) {
		String assetDir = filePath + File.separator + "assets";
		String jsonFilePath = filePath + File.separator + "index.json";
		String jsonString = readFile(new File(jsonFilePath));
		ObjectMapper mapper = new ObjectMapper();
		List<Map<String, Object>> listOfMedia = new ArrayList<>();
		Map<String, String> idSrcMap = new HashMap<>();
		try {
			Map<String, Object> jsonMap = mapper.readValue(jsonString,
					new TypeReference<Map<String, Object>>() {
					});
			Map<String, Object> jsonThemeMap = (Map<String, Object>) jsonMap
					.get("theme");
			Map<String, Object> jsonManifestMap = (Map<String, Object>) jsonThemeMap
					.get("manifest");
			listOfMedia = (List<Map<String, Object>>) jsonManifestMap
					.get("media");
			Iterator medias = listOfMedia.iterator();
			while (medias.hasNext()) {
				Map<String, Object> media = (Map<String, Object>) medias.next();
				idSrcMap.put((String) media.get("id"),
						(String) media.get("src"));
			}
			Iterator mediaEntries = idSrcMap.entrySet().iterator();
			while (mediaEntries.hasNext()) {
				Map.Entry entry = (Map.Entry) mediaEntries.next();
				String src = (String) entry.getValue();
				HttpDownloadUtility.downloadFile(src, assetDir);
				fileNameInURL = src.split("/");
				fileNameWithExtn = fileNameInURL[fileNameInURL.length - 1];
				idSrcMap.put((String) entry.getKey(), fileNameWithExtn);
			}
			List<Map<String, Object>> updatedListOfMedia = updateJsonMedia(
					jsonMap, idSrcMap);
			if (updatedListOfMedia.isEmpty() || updatedListOfMedia != null) {
				jsonManifestMap.put("media", updatedListOfMedia);
			}
			String updatedJsonString = mapper.writeValueAsString(jsonMap);
			if (StringUtils.isNoneBlank(updatedJsonString)) {
				File file = new File(jsonFilePath);
				FileUtils.writeStringToFile(file, updatedJsonString);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServerException(
					ContentErrorCodes.ERR_CONTENT_PUBLISH.name(),
					e.getMessage());
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static List<Map<String, Object>> updateJsonMedia(
			Map<String, Object> jsonMap, Map<String, String> idSrcMap) {
		List<Map<String, Object>> listOfMedia = new ArrayList<>();
		List<Map<String, Object>> updatedListOfMedia = new ArrayList<>();
		try {
			Map<String, Object> jsonThemeMap = (Map<String, Object>) jsonMap
					.get("theme");
			Map<String, Object> jsonManifestMap = (Map<String, Object>) jsonThemeMap
					.get("manifest");
			listOfMedia = (List<Map<String, Object>>) jsonManifestMap
					.get("media");
			Iterator medias = listOfMedia.iterator();
			while (medias.hasNext()) {
				Map<String, Object> media = (Map<String, Object>) medias.next();
				String id = (String) media.get("id");
				if (StringUtils.isNoneBlank(id)) {
					media.put("src", idSrcMap.get(id));
					updatedListOfMedia.add(media);
				}
			}
			return updatedListOfMedia;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static File getZipFile(File ecarFileName,Node node) {
		String filePath = ecarFileName.getParent() + File.separator
				+ ecarFileName.getName().split("\\.")[0] + File.separator + node.getIdentifier();
		String[] extensions = new String[] { "zip" };
		List<File> files = (List<File>) FileUtils.listFiles(new File(filePath), extensions, true);
		return files.get(0);
	}
	/*public static void main(String[] args) {
		System.out.println("in");
		try {
			File file =new  File("C:\\temp\\contentPublish_4_1456399412338.ecar");
			getZipFile(file);
			System.out.println("done");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}*/

}
