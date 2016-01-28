package com.ilimi.taxonomy.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class CustomParser  {

	
	
	/**
	 * This method read ECML file 
	 * @param file path for index.ecml
	 * @return 
	 * */
	public static Map<String,String> readECMLFile(String filePath,String saveDir){
		final Map<String,String> mediaId = new HashMap<String , String>();
		//final String saveDir1 = saveDir;
		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {

				public void startElement(String uri, String localName,String qName, 
						Attributes attributes) throws SAXException {
					if (qName.equalsIgnoreCase("media")) {
						System.out.print( "Media Id::  " + attributes.getValue("id") + " " );
						if (attributes.getValue("id")!=null) {
							if (attributes.getValue("src")!=null) {
								mediaId.put(attributes.getValue("id"),attributes.getValue("src"));
							}
						}
						System.out.print("Media src::  " +attributes.getValue("src") + " ");
						System.out.println("Media type::  " +attributes.getValue("type") + " ");
					}
				}

				public void endElement(String uri, String localName,
						String qName) throws SAXException {
					//System.out.println("End Element :" + qName);
				}
			};
			saxParser.parse(filePath, handler);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mediaId;
	}

	public static void readECMLFileDownload(String filePath,String assetFolder,Map<String,String> mediaIdURLMap){
		
			String filePath1 = filePath+"index.ecml";
			String saveDir = assetFolder+"assets";
			File file1 = new File(saveDir);
			if (!file1.exists()) {
				file1.mkdir();
			}
	        File xmlFile = new File(filePath1);
	        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder dBuilder;
	        try {
	            dBuilder = dbFactory.newDocumentBuilder();
	            Document doc = dBuilder.parse(xmlFile);
	            doc.getDocumentElement().normalize();
	            //update attribute value
	            updateAttributeValue(doc , saveDir,mediaIdURLMap);
	            //write the updated document to file or console
	            doc.getDocumentElement().normalize();
	            TransformerFactory transformerFactory = TransformerFactory.newInstance();
	            Transformer transformer = transformerFactory.newTransformer();
	            DOMSource source = new DOMSource(doc);
	            StreamResult result = new StreamResult(new File(filePath1));
	            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	            transformer.transform(source, result);
	            System.out.println("XML file updated successfully");
	             
	        } catch (SAXException e1) {
	            e1.printStackTrace();
	        }catch (ParserConfigurationException  e) {
				e.printStackTrace();
			}catch (IOException e) {
				e.printStackTrace();
			}catch (TransformerException e) {
				e.printStackTrace();
			}
	}
	
	private static String fileNameInURL[] = null;
	private static String fileNameWithExtn = null;
    @SuppressWarnings("rawtypes")
	private static void updateAttributeValue(Document doc,String saveDir,Map<String,String> mediaIdURLMap) {
        NodeList medias = doc.getElementsByTagName("media");
        Element media = null;
        for(int i=0; i<medias.getLength();i++){
        	media = (Element) medias.item(i);
        	if (mediaIdURLMap!=null && !mediaIdURLMap.isEmpty()) {
        		 Iterator<Entry<String, String>> it = mediaIdURLMap.entrySet().iterator();
        		 while (it.hasNext()) {
        		        Map.Entry pair = (Map.Entry)it.next();
        		        media.setAttribute("src", (String)pair.getValue());
        		        it.remove(); // avoids a ConcurrentModificationException
        		    }
			}else if (mediaIdURLMap==null) {
				 String src = media.getAttribute("src");
                 HttpDownloadUtility.downloadFile(src, saveDir);
                 fileNameInURL =  src.split("/");
         		fileNameWithExtn = fileNameInURL[fileNameInURL.length-1];
                 System.out.println(src);
                 media.setAttribute("src", fileNameWithExtn);
			}
           
          
        }
    }
    public static String readFile(File file){
    	String text = "";
    	try (FileInputStream fis = new FileInputStream(file);){ 
    		text = IOUtils.toString(fis, StandardCharsets.UTF_8.name()); 
    		System.out.println("String generated by reading InputStream in Java : " + text); 
    	} catch (IOException io) {
    		io.printStackTrace(); 
    	}
    	return text;
    }
   /* 
    public static void main(String[] args) {
		File file = new File("C:\\ilimi\\download\\temp\\index.ecml");
		String text = readFile(file);
		
		try {
    		File file1 = new File("C:\\ilimi\\download\\temp2\\index.ecml");
    		if (!file1.exists()) {
				file1.mkdir();
			}
			FileUtils.writeStringToFile(file1, text);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
}
