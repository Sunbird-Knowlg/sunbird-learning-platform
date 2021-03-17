package org.sunbird.content.common;

import java.util.HashMap;
import java.util.Map;

import org.sunbird.content.enums.ContentWorkflowPipelineParams;

public class AssetsMimeTypeMap {

	private static Map<String, String> whiteListedMimeType = new HashMap<String, String>();

	private static Map<String, String> blackListedMimeType = new HashMap<String, String>();

	static {
		whiteListedMimeType.put("application/vnd.ekstep.ecml-archive", ContentWorkflowPipelineParams.ECML_Archive.name());
		whiteListedMimeType.put("application/vnd.ekstep.html-archive", ContentWorkflowPipelineParams.HTML_Archive.name());
		whiteListedMimeType.put("application/vnd.android.package-archive", ContentWorkflowPipelineParams.Android_Package.name());
		whiteListedMimeType.put("application/vnd.ekstep.content-archive", ContentWorkflowPipelineParams.Content_Archive.name());
		whiteListedMimeType.put("application/vnd.ekstep.content-collection", ContentWorkflowPipelineParams.Collection.name());
		whiteListedMimeType.put("application/octet-stream", ContentWorkflowPipelineParams.Content_Package.name());
		whiteListedMimeType.put("application/json", ContentWorkflowPipelineParams.JSON_FILE.name());
		whiteListedMimeType.put("application/javascript", ContentWorkflowPipelineParams.JS_FILE.name());
		whiteListedMimeType.put("application/xml", ContentWorkflowPipelineParams.XML_FILE.name());
		whiteListedMimeType.put("text/plain", ContentWorkflowPipelineParams.TEXT_FILE.name());
		whiteListedMimeType.put("text/html", ContentWorkflowPipelineParams.HTML_FILE.name());
		whiteListedMimeType.put("text/javascript", ContentWorkflowPipelineParams.JS_FILE.name());
		whiteListedMimeType.put("text/xml", ContentWorkflowPipelineParams.JS_FILE.name());
		whiteListedMimeType.put("text/css", ContentWorkflowPipelineParams.CSS_FILE.name());
		whiteListedMimeType.put("image/jpeg", ContentWorkflowPipelineParams.JPEG_Image.name());
		whiteListedMimeType.put("image/jpg", ContentWorkflowPipelineParams.JPG_Image.name());
		whiteListedMimeType.put("image/png", ContentWorkflowPipelineParams.PNG_Image.name());
		whiteListedMimeType.put("image/tiff", ContentWorkflowPipelineParams.TIFF_Image.name());
		whiteListedMimeType.put("image/bmp", ContentWorkflowPipelineParams.BMP_Image.name());
		whiteListedMimeType.put("image/gif", ContentWorkflowPipelineParams.GIF_Image.name());
		whiteListedMimeType.put("image/svg+xml", ContentWorkflowPipelineParams.SVG_XML_Image.name());
		whiteListedMimeType.put("image/x-quicktime", ContentWorkflowPipelineParams.QUICK_TIME_Image.name());
		whiteListedMimeType.put("image/x-quicktime", ContentWorkflowPipelineParams.QUICK_TIME_Image.name());
		whiteListedMimeType.put("image/x-quicktime", ContentWorkflowPipelineParams.QUICK_TIME_Image.name());
		whiteListedMimeType.put("video/avi", ContentWorkflowPipelineParams.AVI_Video.name());
		whiteListedMimeType.put("video/avi", ContentWorkflowPipelineParams.AVI_Video.name());
		whiteListedMimeType.put("video/msvideo", ContentWorkflowPipelineParams.AVI_Video.name());
		whiteListedMimeType.put("video/x-msvideo", ContentWorkflowPipelineParams.AVI_Video.name());
		whiteListedMimeType.put("video/mpeg", ContentWorkflowPipelineParams.MPEG_Video.name());
		whiteListedMimeType.put("video/quicktime", ContentWorkflowPipelineParams.QUICK_TIME_Video.name());
		whiteListedMimeType.put("video/quicktime", ContentWorkflowPipelineParams.QUICK_TIME_Video.name());
		whiteListedMimeType.put("video/x-qtc", ContentWorkflowPipelineParams.QUICK_TIME_Video.name());
		whiteListedMimeType.put("video/3gpp", ContentWorkflowPipelineParams.III_gpp_Video.name());
		whiteListedMimeType.put("video/mp4", ContentWorkflowPipelineParams.MP4_Video.name());
		whiteListedMimeType.put("video/ogg", ContentWorkflowPipelineParams.OGG_Video.name());
		whiteListedMimeType.put("video/webm", ContentWorkflowPipelineParams.WEBM_Video.name());
		whiteListedMimeType.put("video/mpeg", ContentWorkflowPipelineParams.MP3_Audio.name());
		whiteListedMimeType.put("video/x-mpeg", ContentWorkflowPipelineParams.MP3_Audio.name());
		whiteListedMimeType.put("audio/mp3", ContentWorkflowPipelineParams.MP3_Audio.name());
		whiteListedMimeType.put("audio/mpeg3", ContentWorkflowPipelineParams.MP3_Audio.name());
		whiteListedMimeType.put("audio/x-mpeg-3", ContentWorkflowPipelineParams.MP3_Audio.name());
		whiteListedMimeType.put("audio/mp4", ContentWorkflowPipelineParams.MP4_Audio.name());
		whiteListedMimeType.put("audio/mpeg", ContentWorkflowPipelineParams.MPEG_Audio.name());
		whiteListedMimeType.put("audio/ogg", ContentWorkflowPipelineParams.OGG_Audio.name());
		whiteListedMimeType.put("audio/vorbis", ContentWorkflowPipelineParams.OGG_Audio.name());
		whiteListedMimeType.put("audio/webm", ContentWorkflowPipelineParams.WEBM_Audio.name());
		whiteListedMimeType.put("audio/x-wav", ContentWorkflowPipelineParams.X_WAV_Audio.name());
		whiteListedMimeType.put("application/x-font-ttf", ContentWorkflowPipelineParams.X_FONT_TTF.name());

		blackListedMimeType.put("", "");
	}
	
	public static boolean isWhiteListedMimeType(String mimeType) {
		return whiteListedMimeType.containsKey(mimeType);
	}
	
	public static boolean isBlackListedMimeType(String mimeType) {
		return blackListedMimeType.containsKey(mimeType);
	}
	
	public static boolean isValidMimeType(String mimeType) {
		return (whiteListedMimeType.containsKey(mimeType) ||
				blackListedMimeType.containsKey(mimeType));
	}
	
	public static boolean isAllowedMimeType(String mimeType) {
		return (whiteListedMimeType.containsKey(mimeType) &&
				!blackListedMimeType.containsKey(mimeType));
	}

}
