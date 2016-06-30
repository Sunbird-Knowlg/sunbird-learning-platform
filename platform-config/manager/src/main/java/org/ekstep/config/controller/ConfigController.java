package org.ekstep.config.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;

@Controller
@RequestMapping("v2/config")
public class ConfigController extends BaseController {

	private static final String RESOURCE_BUNDLE = "{\"name\":\"Name\",\"code\":\"Code\",\"os\":\"Os\",\"All\":\"All\",\"Android\":\"Android\",\"iOS\":\"iOS\",\"Windows\":\"Windows\",\"minOsVersion\":\"Min Supported OS Version\",\"minGenieVersion\":\"Min Supported Genie Version\",\"minSupportedVersion\":\"Min Supported Version\",\"filter\":\"Filter Criteria (JSON)\",\"visibility\":\"Visibility\",\"Default\":\"Default\",\"Parent\":\"Parent\",\"posterImage\":\"Poster Image\",\"mimeType\":\"Mime Type\",\"application/vnd.ekstep.ecml-archive\":\"application/vnd.ekstep.ecml-archive\",\"application/vnd.ekstep.html-archive\":\"application/vnd.ekstep.html-archive\",\"application/vnd.android.package-archive\":\"application/vnd.android.package-archive\",\"application/vnd.ekstep.content-archive\":\"application/vnd.ekstep.content-archive\",\"application/vnd.ekstep.content-collection\":\"application/vnd.ekstep.content-collection\",\"application/octet-stream\":\"application/octet-stream\",\"image/jpeg\":\"image/jpeg\",\"image/jpg\":\"image/jpg\",\"image/png\":\"image/png\",\"image/tiff\":\"image/tiff\",\"image/bmp\":\"image/bmp\",\"image/gif\":\"image/gif\",\"image/svg+xml\":\"image/svg+xml\",\"video/avi\":\"video/avi\",\"video/mpeg\":\"video/mpeg\",\"video/quicktime\":\"video/quicktime\",\"video/3gpp\":\"video/3gpp\",\"video/mp4\":\"video/mp4\",\"video/ogg\":\"video/ogg\",\"video/webm\":\"video/webm\",\"audio/mp3\":\"audio/mp3\",\"audio/mp4\":\"audio/mp4\",\"audio/mpeg\":\"audio/mpeg\",\"audio/ogg\":\"audio/ogg\",\"audio/webm\":\"audio/webm\",\"audio/x-wav\":\"audio/x-wav\",\"mediaType\":\"Media Type\",\"content\":\"content\",\"collection\":\"collection\",\"image\":\"image\",\"video\":\"video\",\"audio\":\"audio\",\"ecml\":\"ecml\",\"document\":\"document\",\"pdf\":\"pdf\",\"text\":\"Story Text\",\"other\":\"other\",\"appIcon\":\"Icon\",\"grayScaleAppIcon\":\"GrayScale App Icon\",\"thumbnail\":\"Thumbnail\",\"format\":\"Format\",\"duration\":\"Duration\",\"size\":\"Download File Size (in bytes)\",\"idealScreenSize\":\"Ideal Screen Size\",\"small\":\"small\",\"normal\":\"normal\",\"large\":\"large\",\"xlarge\":\"xlarge\",\"idealScreenDensity\":\"Ideal Screen Density (dots per inch)\",\"ldpi\":\"ldpi\",\"mdpi\":\"mdpi\",\"hdpi\":\"hdpi\",\"xhdpi\":\"xhdpi\",\"xxhdpi\":\"xxhdpi\",\"xxxhdpi\":\"xxxhdpi\",\"releaseNotes\":\"Release Notes\",\"pkgVersion\":\"Package Version\",\"resources\":\"Resources\",\"Speaker\":\"Speaker\",\"Touch\":\"Touch\",\"Microphone\":\"Microphone\",\"GPS\":\"GPS\",\"Motion Sensor\":\"Motion Sensor\",\"Compass\":\"Compass\",\"downloadUrl\":\"Download Url\",\"artifactUrl\":\"Artifact Url\",\"objects\":\"Objects Used in the Content\",\"developer\":\"Developer\",\"source\":\"Source\",\"publisher\":\"Publisher\",\"owner\":\"Owner\",\"portalOwner\":\"Portal Owner\",\"attributions\":\"Attributions\",\"collaborators\":\"Collaborators\",\"voiceCredits\":\"Voice Credits\",\"soundCredits\":\"Audio/Sound Credits\",\"imageCredits\":\"Image Credits\",\"copyright\":\"Copyright\",\"license\":\"License\",\"Against DRM license\":\"Against DRM license\",\"Creative Commons Attribution (CC BY)\":\"Creative Commons Attribution (CC BY)\",\"Creative Commons Attribution-ShareAlike (CC BY-SA)\":\"Creative Commons Attribution-ShareAlike (CC BY-SA)\",\"Creative Commons Zero (CC0)\":\"Creative Commons Zero (CC0)\",\"Free Art License\":\"Free Art License\",\"Open Audio License\":\"Open Audio License\",\"Open Game License\":\"Open Game License\",\"Other\":\"Other\",\"language\":\"Language\",\"English\":\"English\",\"Hindi\":\"Hindi\",\"Assamese\":\"Assamese\",\"Bengali\":\"Bengali\",\"Gujarati\":\"Gujarati\",\"Kannada\":\"Kannada\",\"Malayalam\":\"Malayalam\",\"Marathi\":\"Marathi\",\"Nepali\":\"Nepali\",\"Oriya\":\"Oriya\",\"Punjabi\":\"Punjabi\",\"Tamil\":\"Tamil\",\"Telugu\":\"Telugu\",\"Urdu\":\"Urdu\",\"words\":\"Dictionary Words\",\"forkable\":\"Forkable\",\"translatable\":\"Translatable\",\"ageGroup\":\"Age Group\",\"<5\":\"<5\",\"5-6\":\"5-6\",\"6-7\":\"6-7\",\"7-8\":\"7-8\",\"8-10\":\"8-10\",\">10\":\">10\",\"gradeLevel\":\"Grade Level\",\"Kindergarten\":\"Kindergarten\",\"Grade 1\":\"Grade 1\",\"Grade 2\":\"Grade 2\",\"Grade 3\":\"Grade 3\",\"Grade 4\":\"Grade 4\",\"Grade 5\":\"Grade 5\",\"interactivityLevel\":\"Interactivity Level\",\"High\":\"High\",\"Medium\":\"Medium\",\"Low\":\"Low\",\"curriculum\":\"Curriculum\",\"NCERT\":\"NCERT\",\"CBSE\":\"CBSE\",\"ICSE\":\"ICSE\",\"State Curriculum\":\"State Curriculum\",\"contentType\":\"Content Type\",\"Story\":\"Story\",\"Worksheet\":\"Worksheet\",\"Game\":\"Game\",\"Simulation\":\"Simulation\",\"Puzzle\":\"Puzzle\",\"Diagnostic\":\"Diagnostic\",\"Collection\":\"Collection\",\"Asset\":\"Asset\",\"Template\":\"Template\",\"templateType\":\"Template Type\",\"story\":\"story\",\"worksheet\":\"worksheet\",\"mcq\":\"mcq\",\"ftb\":\"ftb\",\"mtf\":\"mtf\",\"recognition\":\"recognition\",\"activity\":\"activity\",\"widget\":\"widget\",\"genre\":\"Genre\",\"Picture Books\":\"Picture Books\",\"Chapter Books\":\"Chapter Books\",\"Flash Cards\":\"Flash Cards\",\"Serial Books\":\"Serial Books\",\"Alphabet Books\":\"Alphabet Books\",\"Folktales\":\"Folktales\",\"Fiction\":\"Fiction\",\"Non-Fiction\":\"Non-Fiction\",\"Poems/Rhymes\":\"Poems/Rhymes\",\"Plays\":\"Plays\",\"Comics\":\"Comics\",\"Words\":\"Words\",\"theme\":\"Theme\",\"History\":\"History\",\"Adventure\":\"Adventure\",\"Mystery\":\"Mystery\",\"Science\":\"Science\",\"Nature\":\"Nature\",\"Art\":\"Art\",\"Music\":\"Music\",\"Funny\":\"Funny\",\"Family\":\"Family\",\"Life Skills\":\"Life Skills\",\"Scary\":\"Scary\",\"School Stories\":\"School Stories\",\"Holidays\":\"Holidays\",\"Hobby\":\"Hobby\",\"Geography\":\"Geography\",\"Rural\":\"Rural\",\"Urban\":\"Urban\",\"rating\":\"User Rating\",\"rating_a\":\"Analytical Rating\",\"quality\":\"Quality\",\"popularity\":\"Popularity\",\"downloads\":\"No of downloads\",\"launchUrl\":\"Launch Url\",\"osId\":\"Operating System Id\",\"activity_class\":\"Activity Class Name\",\"scaffolding\":\"Scaffolding\",\"Tutorial\":\"Tutorial\",\"Help\":\"Help\",\"Practice\":\"Practice\",\"feedback\":\"Feedback\",\"Right/Wrong\":\"Right/Wrong\",\"Reflection\":\"Reflection\",\"Guidance\":\"Guidance\",\"Learn from Mistakes\":\"Learn from Mistakes\",\"Adaptive Feedback\":\"Adaptive Feedback\",\"Interrupts\":\"Interrupts\",\"Rich Feedback\":\"Rich Feedback\",\"feedbackType\":\"Feedback\",\"Audio\":\"Audio\",\"Visual\":\"Visual\",\"Textual\":\"Textual\",\"Tactile\":\"Tactile\",\"teachingMode\":\"Teaching Mode\",\"Abstract\":\"Abstract\",\"Concrete\":\"Concrete\",\"Pictorial\":\"Pictorial\",\"skills\":\"Skills Required\",\"Listening\":\"Listening\",\"Speaking\":\"Speaking\",\"Reading\":\"Reading\",\"Writing\":\"Writing\",\"Gestures\":\"Gestures\",\"Draw\":\"Draw\",\"keywords\":\"Keywords\",\"domain\":\"Domain\",\"numeracy\":\"numeracy\",\"literacy\":\"literacy\",\"status\":\"Status\",\"Draft\":\"Draft\",\"Review\":\"Review\",\"Redraft\":\"Redraft\",\"Flagged\":\"Flagged\",\"Live\":\"Live\",\"Retired\":\"Retired\",\"Mock\":\"Mock\",\"optStatus\":\"Optimization Status\",\"Pending\":\"Pending\",\"Processing\":\"Processing\",\"Error\":\"Error\",\"Complete\":\"Complete\",\"description\":\"Description\",\"body\":\"Body\",\"editorState\":\"Editor State\",\"data\":\"Data\",\"loadingMessage\":\"Loading Message\",\"checksum\":\"Checksum\",\"learningObjective\":\"Learning Objective\",\"createdBy\":\"Created By\",\"createdOn\":\"Created On\",\"lastUpdatedBy\":\"Last Updated By\",\"lastUpdatedOn\":\"Last Updated On\",\"lastSubmittedBy\":\"Submitted for Review By\",\"lastSubmittedOn\":\"Submitted for Review On\",\"lastPublishedBy\":\"Published By\",\"lastPublishedOn\":\"Published On\",\"version\":\"Version\",\"versionDate\":\"Version Date\",\"versionCreatedBy\":\"Version Created By\",\"collections\":\"collections\",\"concepts\":\"concepts\",\"item_sets\":\"item_sets\",\"methods\":\"methods\",\"screenshots\":\"screenshots\",\"children\":\"children\",\"tags\":\"tags\"}";
	private static final String ORDINALS = "{\"os\":[\"All\",\"Android\",\"iOS\",\"Windows\"],\"visibility\":[\"Default\",\"Parent\"],\"mimeType\":[\"application/vnd.ekstep.ecml-archive\",\"application/vnd.ekstep.html-archive\",\"application/vnd.android.package-archive\",\"application/vnd.ekstep.content-archive\",\"application/vnd.ekstep.content-collection\",\"application/octet-stream\",\"image/jpeg\",\"image/jpg\",\"image/png\",\"image/tiff\",\"image/bmp\",\"image/gif\",\"image/svg+xml\",\"video/avi\",\"video/mpeg\",\"video/quicktime\",\"video/3gpp\",\"video/mpeg\",\"video/mp4\",\"video/ogg\",\"video/webm\",\"audio/mp3\",\"audio/mp4\",\"audio/mpeg\",\"audio/ogg\",\"audio/webm\",\"audio/x-wav\"],\"mediaType\":[\"content\",\"collection\",\"image\",\"video\",\"audio\",\"voice\",\"ecml\",\"document\",\"pdf\",\"text\",\"other\"],\"idealScreenSize\":[\"small\",\"normal\",\"large\",\"xlarge\",\"other\"],\"idealScreenDensity\":[\"ldpi\",\"mdpi\",\"hdpi\",\"xhdpi\",\"xxhdpi\",\"xxxhdpi\"],\"resources\":[\"Speaker\",\"Touch\",\"Microphone\",\"GPS\",\"Motion Sensor\",\"Compass\"],\"license\":[\"Against DRM license\",\"Creative Commons Attribution (CC BY)\",\"Creative Commons Attribution-ShareAlike (CC BY-SA)\",\"Creative Commons Zero (CC0)\",\"Free Art License\",\"Open Audio License\",\"Open Game License\",\"Other\"],\"language\":[\"English\",\"Hindi\",\"Assamese\",\"Bengali\",\"Gujarati\",\"Kannada\",\"Malayalam\",\"Marathi\",\"Nepali\",\"Oriya\",\"Punjabi\",\"Tamil\",\"Telugu\",\"Urdu\",\"Other\"],\"ageGroup\":[\"<5\",\"5-6\",\"6-7\",\"7-8\",\"8-10\",\">10\",\"Other\"],\"gradeLevel\":[\"Kindergarten\",\"Grade 1\",\"Grade 2\",\"Grade 3\",\"Grade 4\",\"Grade 5\",\"Other\"],\"interactivityLevel\":[\"High\",\"Medium\",\"Low\"],\"curriculum\":[\"NCERT\",\"CBSE\",\"ICSE\",\"State Curriculum\",\"Other\"],\"contentType\":[\"Story\",\"Worksheet\",\"Game\",\"Simulation\",\"Puzzle\",\"Diagnostic\",\"Collection\",\"Asset\",\"Template\"],\"templateType\":[\"story\",\"worksheet\",\"mcq\",\"ftb\",\"mtf\",\"recognition\",\"activity\",\"widget\",\"other\"],\"genre\":[\"Picture Books\",\"Chapter Books\",\"Flash Cards\",\"Serial Books\",\"Alphabet Books\",\"Folktales\",\"Fiction\",\"Non-Fiction\",\"Poems/Rhymes\",\"Plays\",\"Comics\",\"Words\"],\"theme\":[\"History\",\"Adventure\",\"Mystery\",\"Science\",\"Nature\",\"Art\",\"Music\",\"Funny\",\"Family\",\"Life Skills\",\"Scary\",\"School Stories\",\"Holidays\",\"Hobby\",\"Geography\",\"Rural\",\"Urban\"],\"scaffolding\":[\"Tutorial\",\"Help\",\"Practice\"],\"feedback\":[\"Right/Wrong\",\"Reflection\",\"Guidance\",\"Learn from Mistakes\",\"Adaptive Feedback\",\"Interrupts\",\"Rich Feedback\"],\"feedbackType\":[\"Audio\",\"Visual\",\"Textual\",\"Tactile\"],\"teachingMode\":[\"Abstract\",\"Concrete\",\"Pictorial\"],\"skills\":[\"Listening\",\"Speaking\",\"Reading\",\"Writing\",\"Touch\",\"Gestures\",\"Draw\"],\"domain\":[\"numeracy\",\"literacy\"],\"status\":[\"Draft\",\"Review\",\"Redraft\",\"Flagged\",\"Live\",\"Retired\",\"Mock\"],\"optStatus\":[\"Pending\",\"Processing\",\"Error\",\"Complete\"]}";
	private ObjectMapper mapper = new ObjectMapper();
	
	@RequestMapping(value = "/resourcebundles", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getResourceBundles() {
		String apiId = "ekstep.config.resourebundles.list";
		try {
			Response response = new Response();
			ResponseParams params = new ResponseParams();
			params.setErr("0");
			params.setStatus(StatusType.successful.name());
			params.setErrmsg("Operation successful");
			response.setParams(params);
			Map<String, Object> map = mapper.readValue(RESOURCE_BUNDLE, new TypeReference<Map<String, Object>>() {});
			response.put("ttl", 24.0);
			Map<String, Object> resourcebundles = new HashMap<String, Object>();
			resourcebundles.put("en", map);
			resourcebundles.put("hi", map);
			resourcebundles.put("te", map);
			resourcebundles.put("ka", map);
			resourcebundles.put("ta", map);
			response.put("resourcebundles", resourcebundles);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	@RequestMapping(value = "/resourcebundles/{languageId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getResourceBundle(@PathVariable(value = "languageId") String languageId) {
		String apiId = "ekstep.config.resourebundles.find";
		try {
			Response response = new Response();
			ResponseParams params = new ResponseParams();
			params.setErr("0");
			params.setStatus(StatusType.successful.name());
			params.setErrmsg("Operation successful");
			response.setParams(params);
			Map<String, Object> map = mapper.readValue(RESOURCE_BUNDLE, new TypeReference<Map<String, Object>>() {});
			response.put("ttl", 24.0);
			response.put(languageId, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	@RequestMapping(value = "/ordinals", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> getOrdinals() {
		String apiId = "ekstep.config.ordinals.list";
		try {
			Response response = new Response();
			ResponseParams params = new ResponseParams();
			params.setErr("0");
			params.setStatus(StatusType.successful.name());
			params.setErrmsg("Operation successful");
			response.setParams(params);
			Map<String, Object> map = mapper.readValue(ORDINALS, new TypeReference<Map<String, Object>>() {});
			response.put("ttl", 24.0);
			response.put("ordinals", map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			e.printStackTrace();
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
}
