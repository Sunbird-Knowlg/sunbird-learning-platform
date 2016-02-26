package com.ilimi.taxonomy.mgr.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.taxonomy.enums.ContentErrorCodes;
import com.ilimi.taxonomy.util.AWSUploader;

public class BaseMimeTypeManager extends BaseManager{
	private static final String bucketName = "ekstep-public";
	private static final String folderName = "content";
	public boolean isArtifactUrlSet(Map<String, Object> contentMap) {
		return false;
	}
	
	public boolean isValidZipFile(File file) {
		return false;
	}
	
	public void uploadFile(InputStream stream) {
		
	}
	
	public String uploadFile(String folder, String filename) {
		File olderName = new File(folder + filename);
		try {
			if (null != olderName && olderName.exists() && olderName.isFile()) {
				String parentFolderName = olderName.getParent();
				File newName = new File(parentFolderName + File.separator
						+ System.currentTimeMillis() + "_" + olderName.getName());
				olderName.renameTo(newName);
				String[] url = AWSUploader.uploadFile(bucketName, folderName, newName);
				return url[1];
			}
		} catch (Exception ex) {
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_EXTRACT.name(), ex.getMessage());
		}
		return null;
	}
	
	public Map<String, Object> setArtifactUrl(Map<String, Object> contentMap, String url) {
		Map<String, Object> map = new HashMap<String, Object>();
		return map;
	}
	
	public Response getContentNode(Request request) {
		Response response = new Response();
		return response;
	}
	
	public boolean isJSONValid(String content) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			mapper.readTree(content);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public boolean isECMLValid(String content) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			dBuilder.parse(IOUtils.toInputStream(content, "UTF-8"));
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public  Map<String, List<Object>> readECMLFile(String filePath) {
		final Map<String, List<Object>> mediaIdMap = new HashMap<String, List<Object>>();
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
								List<Object> mediaValues = new ArrayList<Object>();
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
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_EXTRACT.name(),e.getMessage());
		}
		return mediaIdMap;
	}
	public  void delete(File file) throws IOException {
		if (file.isDirectory()) {
			// directory is empty, then delete it
			if (file.list().length == 0) {
				file.delete();
			} else {
				// list all the directory contents
				String files[] = file.list();
				for (String temp : files) {
					// construct the file structure
					File fileDelete = new File(file, temp);
					// recursive delete
					delete(fileDelete);
				}
				// check the directory again, if empty then delete it
				if (file.list().length == 0) {
					file.delete();
				}
			}

		} else {
			// if file, then delete it
			file.delete();
		}
	}
}
