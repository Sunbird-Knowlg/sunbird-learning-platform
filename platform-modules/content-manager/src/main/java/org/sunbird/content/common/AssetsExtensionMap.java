package org.sunbird.content.common;

import java.util.HashMap;
import java.util.Map;

import org.sunbird.content.enums.ContentWorkflowPipelineParams;

public class AssetsExtensionMap {
	
	private static Map<String, String> whiteListedExtension = new HashMap<String, String>();

	private static Map<String, String> blackListedExtension = new HashMap<String, String>();
	
	static {
		whiteListedExtension.put("", "");
		whiteListedExtension.put(ContentWorkflowPipelineParams.zip.name(), ContentWorkflowPipelineParams.ECML_Archive.name());
		whiteListedExtension.put(ContentWorkflowPipelineParams.zip.name(), ContentWorkflowPipelineParams.HTML_Archive.name());
		whiteListedExtension.put(ContentWorkflowPipelineParams.apk.name(), ContentWorkflowPipelineParams.Android_Package.name());
		whiteListedExtension.put(ContentWorkflowPipelineParams.zip.name(), ContentWorkflowPipelineParams.Content_Archive.name());
		whiteListedExtension.put(ContentWorkflowPipelineParams.zip.name(), ContentWorkflowPipelineParams.Collection.name());
		whiteListedExtension.put(ContentWorkflowPipelineParams.zip.name(), ContentWorkflowPipelineParams.Content_Package.name());
		whiteListedExtension.put(ContentWorkflowPipelineParams.jpeg.name(), ContentWorkflowPipelineParams.JPEG_Image.name());
		whiteListedExtension.put(ContentWorkflowPipelineParams.jpg.name(), ContentWorkflowPipelineParams.JPG_Image.name());
		whiteListedExtension.put(ContentWorkflowPipelineParams.png.name(), ContentWorkflowPipelineParams.PNG_Image.name());
		whiteListedExtension.put(ContentWorkflowPipelineParams.tiff.name(), ContentWorkflowPipelineParams.TIFF_Image.name());
		whiteListedExtension.put(ContentWorkflowPipelineParams.bmp.name(), ContentWorkflowPipelineParams.BMP_Image.name());
		whiteListedExtension.put(ContentWorkflowPipelineParams.gif.name(), ContentWorkflowPipelineParams.GIF_Image.name());
		whiteListedExtension.put(ContentWorkflowPipelineParams.svg.name(), ContentWorkflowPipelineParams.SVG_XML_Image.name());
		whiteListedExtension.put(ContentWorkflowPipelineParams.avi.name(), ContentWorkflowPipelineParams.AVI_Video.name());
		whiteListedExtension.put(ContentWorkflowPipelineParams.mpeg.name(), ContentWorkflowPipelineParams.MPEG_Video.name());
		whiteListedExtension.put(ContentWorkflowPipelineParams.qtff.name(), ContentWorkflowPipelineParams.QUICK_TIME_Video.name());
		whiteListedExtension.put(ContentWorkflowPipelineParams.mp4.name(), ContentWorkflowPipelineParams.MP4_File.name());
		whiteListedExtension.put(ContentWorkflowPipelineParams.ogg.name(), ContentWorkflowPipelineParams.OGG_File.name());
		whiteListedExtension.put(ContentWorkflowPipelineParams.webm.name(), ContentWorkflowPipelineParams.WEBM_File.name());
		whiteListedExtension.put(ContentWorkflowPipelineParams.mp3.name(), ContentWorkflowPipelineParams.MP3_Audio.name());
		whiteListedExtension.put(ContentWorkflowPipelineParams.xwav.name(), ContentWorkflowPipelineParams.X_WAV_Audio.name());
		whiteListedExtension.put("video/3gpp", ContentWorkflowPipelineParams.III_gpp_Video.name());
		
		blackListedExtension.put("", "");
	}
	
	public static boolean isWhiteListedExtension(String mimeType) {
		return whiteListedExtension.containsKey(mimeType);
	}
	
	public static boolean isBlackListedExtension(String mimeType) {
		return blackListedExtension.containsKey(mimeType);
	}
	
	public static boolean isValidExtension(String mimeType) {
		return (whiteListedExtension.containsKey(mimeType) ||
				blackListedExtension.containsKey(mimeType));
	}
	
	public static boolean isAllowedExtension(String mimeType) {
		return (whiteListedExtension.containsKey(mimeType) &&
				!blackListedExtension.containsKey(mimeType));
	}

}
