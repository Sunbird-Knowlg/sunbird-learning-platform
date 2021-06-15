package org.ekstep.common.util;

import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;

public class HTTPUrlUtil {

	private static long sizeLimit = 50000000;
	
	public static Map<String, Object> getMetadata(String fileUrl){
/*		URLConnection conn = null;
		Map<String, Object> metadata = new HashMap<>();*/
	    try {
/*	    	URL url = new URL(fileUrl);
	        conn = (HttpURLConnection)url.openConnection();
	        if(conn instanceof HttpURLConnection) {
	            ((HttpURLConnection)conn).setRequestMethod("HEAD");
	        }
	        conn.getInputStream();
	        metadata.put("size", conn.getContentLengthLong());
	        metadata.put("type", conn.getContentType());
	        return metadata;*/
			return CommonCloudStore.getMetadata(fileUrl);

	    } catch(UnknownHostException e) {
	    		throw new ClientException("ERR_UNKNOWN_HOST", "Invalid url."); 
	    	} catch(FileNotFoundException e) {
	    		throw new ClientException("ERR_FILE_NOT_FOUND", "File not found.");
	    } catch (Exception e) {
	    		throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(),
					"Something Went Wrong While Processing Your Request. Please Try Again After Sometime!");
	    } /*finally {
	        if(conn instanceof HttpURLConnection) {
	            ((HttpURLConnection)conn).disconnect();
	        }
	    }*/

	}
	
	public static boolean isValidSize(long size) {
        return sizeLimit>=size;
    }
}
