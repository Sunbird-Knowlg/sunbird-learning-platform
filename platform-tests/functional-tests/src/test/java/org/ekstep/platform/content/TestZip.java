package org.ekstep.platform.content;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.ekstep.platform.domain.BaseTest;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.lingala.zip4j.core.ZipFile;

public class TestZip extends BaseTest {
	
	int rn = generateRandomInt(2000, 2500);
	String nodeId;
	String downloadUrl = "https://ekstep-public.s3-ap-southeast-1.amazonaws.com/ecar_files/Test_QA_2222_1468397934500.ecar";
	String artifactUrl = "https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/1468397880051_Test_QA_2222.zip";

	File[] fol1 = new File("/Users/purnima/Desktop/Test1").listFiles();
	File[] fol2 = new File("/Users/purnima/Desktop/Test2").listFiles();
	
	static ClassLoader classLoader = TestZip.class.getClassLoader();
	static URL url = classLoader.getResource("UploadFiles/DownloadedFiles");
	static File downloadPath;
	
	@BeforeClass
	public static void setup() throws URISyntaxException{
		downloadPath = new File(url.toURI().getPath());
	}	
	
	/*@AfterClass
	public static void end() throws IOException{
		FileUtils.cleanDirectory(downloadPath);

	}*/
	@Test @SuppressWarnings("unused")
	
	public void unzip(){
		
		//compareFiles(fol1, fol2);
		try{
			
			String ecarName = "ecar_"+rn+"";
			String uploadFile = "upload_"+rn+"";
			
			FileUtils.copyURLToFile(new URL(artifactUrl), new File(downloadPath+"/"+uploadFile+".zip"));
			String uploadSource = downloadPath+"/"+uploadFile+".zip";
			
			FileUtils.copyURLToFile(new URL(downloadUrl), new File(downloadPath+"/"+ecarName+".zip"));		
			String source = downloadPath+"/"+ecarName+".zip";
			
			File Destination = new File(downloadPath+"/"+ecarName+"");
			String Dest = Destination.getPath();
			System.out.println(Dest);
			try {
				
				// Extracting the uploaded file using artifact url
				ZipFile zipUploaded = new ZipFile(uploadSource);
				zipUploaded.extractAll(Dest);
				
				// Downloaded from artifact url
				File uploadAssetsPath = new File(Dest+"/assets");
				File[] uploadListFiles = uploadAssetsPath.listFiles();
				
				// Extracting the 
				ZipFile zip = new ZipFile(source);
				zip.extractAll(Dest);
				
				String folderName = "Test_QA_2222";
				String dirName = Dest+"/"+folderName;
				
				File fileName = new File(dirName);
				File[] listofFiles = fileName.listFiles();
				
				for(File file : listofFiles){
					
					if(file.isFile()){
						String fPath = file.getAbsolutePath();
						String fName = file.getName();
						System.out.println(fName);
						
						if (fName.endsWith(".zip")|| fName.endsWith(".rar")){
						ZipFile ecarZip = new ZipFile(fPath);
						ecarZip.extractAll(dirName);
						File assetsPath = new File(dirName+"/assets");
						File[] extractedAssets = assetsPath.listFiles();						
						if (assetsPath.exists()){
							
							int assetCount = assetsPath.listFiles().length;
							System.out.println(assetCount);
							
							int uploadAssetsCount = uploadAssetsPath.listFiles().length;
							System.out.println(uploadAssetsCount);
							Assert.assertEquals(assetCount, uploadAssetsCount);
							compareFiles(uploadListFiles, extractedAssets);
							}
						}
					}		
				}
				File manifest = new File(Dest+"/manifest.json");
				JSONParser parser = new JSONParser();
				
				Object obj = parser.parse(new FileReader(manifest));
				JSONObject js = (JSONObject) obj;

				JSONObject arc = (JSONObject) js.get("archive");
				JSONArray items = (JSONArray)arc.get("items");
				
		        @SuppressWarnings("rawtypes")
				Iterator i = items.iterator();
		        while(i.hasNext()) {
		        	try {
					JSONObject item = (JSONObject) i.next();
					String name = (String) item.get("name");		        
					String mimeType = (String) item.get("mimeType");
					String status = (String) item.get("status");
					String code = (String) item.get("code");
					String osID = (String) item.get("osId");
					String contentType = (String) item.get("contentType");
					String mediaType = (String) item.get("mediaType");
					String description = (String) item.get("description");
			        }
			        	catch(JSONException jse){
			        		jse.printStackTrace();
			        	}
		        	}
				}				
				catch (Exception x){
				x.printStackTrace();	
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	
	}

			
	public String compareFiles(File[] uploadListFiles, File[] extractedAssets)
	{
	    String filesincommon = "";
	    String filesnotpresent = "";
	    boolean final_status = true;
	    for (int i = 0; i < uploadListFiles.length; i++) {
	        boolean status = false;
	        for (int k = 0; k < extractedAssets.length; k++) {
	            if (uploadListFiles[i].getName().equalsIgnoreCase(extractedAssets[k].getName())) {
	                filesincommon = uploadListFiles[i].getName() + "," + filesincommon;
	                //System.out.println("Common files are: "+filesincommon);
	                status = true;
	                break;
	            }
	        }
	        if (!status) {
	            final_status = false;
	            filesnotpresent = uploadListFiles[i].getName() + "," + filesnotpresent;
	        }
	    }
	    if (final_status) {
	        System.out.println("Files are same");
	        return "success";
	    } else {
	    	System.out.println("Files are missing");
	        System.out.println(filesnotpresent);
	        return filesnotpresent;
	    }
	}
}
