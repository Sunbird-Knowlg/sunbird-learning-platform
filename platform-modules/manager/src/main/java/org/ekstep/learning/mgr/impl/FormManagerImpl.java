package org.ekstep.learning.mgr.impl;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.dto.Response;
import org.ekstep.common.mgr.BaseManager;
import org.ekstep.learning.mgr.IFormManager;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Mahesh
 *
 */

@Component
public class FormManagerImpl extends BaseManager implements IFormManager {

	private ObjectMapper mapper = new ObjectMapper();

	public String getObjectType() {
		return "form";
	}

	@Override
	public String getIdentifier(String type, String operation, String channel) throws Exception {
		// TODO: implement it.
		return "";
	}
	
	@Override
	public Response create(Map<String, Object> map) {
		return getSuccessResponse();
	}

	@Override
	public Response update(String identifier, Map<String, Object> map) {
		return getSuccessResponse();
	}

	@Override
	public Response read(String identifier, String type, String operation) throws Exception {
		Response response = getSuccessResponse();
		String mockDefinition = "{\"properties\":[{\"code\":\"name\",\"name\":\"Name\",\"description\":\"Name of the content\",\"category\":\"General\",\"dataType\":\"Text\",\"required\":true,\"displayProperty\":\"Editable\",\"defaultValue\":\"\",\"renderingHints\":\"{'order': 1, 'inputType': 'text'}\",\"translations\":{\"en\":\"Name\"}},{\"code\":\"code\",\"name\":\"Code\",\"description\":\"Unique code for the content\",\"category\":\"General\",\"dataType\":\"Text\",\"required\":true,\"displayProperty\":\"Editable\",\"defaultValue\":\"\",\"renderingHints\":\"{ 'order': 2, 'inputType': 'text'}\",\"translations\":{\"en\":\"Code\"}},{\"code\":\"mimeType\",\"name\":\"Mime Type\",\"description\":\"\",\"category\":\"Technical\",\"dataType\":\"Select\",\"required\":true,\"displayProperty\":\"Editable\",\"range\":[\"application/vnd.ekstep.ecml-archive\",\"application/vnd.ekstep.html-archive\",\"application/vnd.android.package-archive\",\"application/vnd.ekstep.content-archive\",\"application/vnd.ekstep.content-collection\",\"application/vnd.ekstep.plugin-archive\",\"application/vnd.ekstep.h5p-archive\",\"application/epub\",\"text/x-url\",\"video/x-youtube\",\"application/octet-stream\",\"application/msword\",\"application/pdf\",\"image/jpeg\",\"image/jpg\",\"image/png\",\"image/tiff\",\"image/bmp\",\"image/gif\",\"image/svg+xml\",\"video/avi\",\"video/mpeg\",\"video/quicktime\",\"video/3gpp\",\"video/mpeg\",\"video/mp4\",\"video/ogg\",\"video/webm\",\"audio/mp3\",\"audio/mp4\",\"audio/mpeg\",\"audio/ogg\",\"audio/webm\",\"audio/x-wav\",\"audio/wav\"],\"defaultValue\":\"application/vnd.ekstep.ecml-archive\",\"renderingHints\":\"{ 'order': 4, 'inputType': 'select'}\",\"translations\":{\"en\":\"Mime Type\"}},{\"code\":\"board\",\"name\":\"Curriculum (Board)\",\"description\":\"Education Board (Like MP Board, NCERT, etc)\",\"category\":\"Pedagogy\",\"dataType\":\"Term\",\"required\":false,\"displayProperty\":\"Editable\",\"defaultValue\":\"\",\"renderingHints\":\"{'order': 5, 'inputType': 'select'}\",\"translations\":{\"en\":\"Curriculum\"}},{\"code\":\"subject\",\"name\":\"Subject\",\"description\":\"Subject of the Content to use to teach\",\"category\":\"Pedagogy\",\"dataType\":\"Term\",\"required\":false,\"displayProperty\":\"Editable\",\"defaultValue\":\"\",\"renderingHints\":\"{ 'order': 6, 'inputType': 'select'}\",\"translations\":{\"en\":\"Subject\"}},{\"code\":\"medium\",\"name\":\"Medium\",\"description\":\"Medium of instruction\",\"category\":\"Pedagogy\",\"dataType\":\"Term\",\"required\":false,\"displayProperty\":\"Editable\",\"defaultValue\":\"\",\"renderingHints\":\"{'order': 7, 'inputType': 'select'}\"}],\"relations\":[{\"code\":\"concepts\",\"name\":\"Concepts\",\"description\":\"Concepts associated with this content\",\"required\":false,\"renderingHints\":\"{'order': 3}\",\"objectType\":[\"Concept\"],\"translations\":{\"en\":\"Concepts\"}}]}";
		Map<String, Object> definition = mapper.readValue(mockDefinition, Map.class);
		Map<String, Object> mock = new HashMap<>();
		mock.put("name", "Textbook's create form.");
		mock.put("description", "Textbook's create form.");
		mock.put("objectType", "Content");
		mock.put("type", type.toLowerCase());
		mock.put("operation", operation.toLowerCase());
		mock.put("definition", definition);
		response.put(getObjectType(), mock);
		return response;
	}
}
