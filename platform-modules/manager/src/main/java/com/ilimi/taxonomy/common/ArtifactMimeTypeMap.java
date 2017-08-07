package com.ilimi.taxonomy.common;

import java.util.HashMap;
import java.util.Map;

public class ArtifactMimeTypeMap {
	
private static Map<String, String> artifactMimeTypeMap = new HashMap<String, String>();
	
	static {
		artifactMimeTypeMap.put("application/vnd.ekstep.ecml-archive", "application/vnd.ekstep.ecml-archive+zip");
		artifactMimeTypeMap.put("application/vnd.ekstep.html-archive", "application/vnd.ekstep.html-archive+zip");
		artifactMimeTypeMap.put("application/vnd.android.package-archive", "application/vnd.android.package-archive+zip");
		artifactMimeTypeMap.put("application/vnd.ekstep.content-archive", "application/vnd.ekstep.content-archive+zip");
		artifactMimeTypeMap.put("application/vnd.ekstep.content-collection", "application/vnd.ekstep.content-collection+zip");
		artifactMimeTypeMap.put("application/vnd.ekstep.plugin-archive", "application/vnd.ekstep.plugin-archive+zip");
		artifactMimeTypeMap.put("application/vnd.ekstep.h5p-archive", "application/vnd.ekstep.h5p-archive+zip");
		artifactMimeTypeMap.put("application/epub", "application/epub+zip");
		artifactMimeTypeMap.put("text/x-url", "text/x-url");
		artifactMimeTypeMap.put("video/youtube", "video/x-youtube");
		artifactMimeTypeMap.put("application/octet-stream", "application/octet-stream");
		artifactMimeTypeMap.put("application/msword", "application/msword");
		artifactMimeTypeMap.put("application/pdf", "application/pdf");
		artifactMimeTypeMap.put("image/jpeg", "image/jpeg");
		artifactMimeTypeMap.put("image/jpg", "image/jpg");
		artifactMimeTypeMap.put("image/png", "image/png");
		artifactMimeTypeMap.put("image/tiff", "image/tiff");
		artifactMimeTypeMap.put("image/bmp", "image/bmp");
		artifactMimeTypeMap.put("image/gif", "image/gif");
		artifactMimeTypeMap.put("image/svg+xml", "image/svg+xml");
		artifactMimeTypeMap.put("video/avi", "video/avi");
		artifactMimeTypeMap.put("video/mpeg", "video/mpeg");
		artifactMimeTypeMap.put("video/quicktime", "video/quicktime");
		artifactMimeTypeMap.put("video/3gpp", "video/3gpp");
		artifactMimeTypeMap.put("video/mpeg", "video/mpeg");
		artifactMimeTypeMap.put("video/mp4", "video/mp4");
		artifactMimeTypeMap.put("video/ogg", "video/ogg");
		artifactMimeTypeMap.put("video/webm", "video/webm");
		artifactMimeTypeMap.put("audio/mp3", "audio/mp3");
		artifactMimeTypeMap.put("audio/mp4", "audio/mp4");
		artifactMimeTypeMap.put("audio/mpeg", "audio/mpeg");
		artifactMimeTypeMap.put("audio/ogg", "audio/ogg");
		artifactMimeTypeMap.put("audio/webm", "audio/webm");
		artifactMimeTypeMap.put("audio/x-wav", "audio/x-wav");
		artifactMimeTypeMap.put("audio/wav", "audio/wa");
	}
	
	public static String getArtifactMimeType(String mimeType) {
		return artifactMimeTypeMap.get(mimeType);
	}

}
