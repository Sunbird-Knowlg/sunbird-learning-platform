package org.ekstep.framework.mgr.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.framework.enums.FrameworkEnum;
import org.ekstep.framework.mgr.IFrameworkManager;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.springframework.stereotype.Component;

/**
 * The Class <code>FrameworkManagerImpl</code> is the implementation of
 * <code>IFrameworkManager</code> for all the operation including CRUD operation
 * and High Level Operations.
 * 
 * 
 * @author gauraw
 *
 */
@Component
public class FrameworkManagerImpl extends BaseFrameworkManager implements IFrameworkManager {

	private static final String FRAMEWORK_OBJECT_TYPE = "Framework";
	private String host = "localhost";
	private int port = 9200;
	private SearchProcessor processor = null;
	private static ObjectMapper mapper = new ObjectMapper();

	@PostConstruct
	public void init() {
		host = Platform.config.hasPath("dialcode.es_host") ? Platform.config.getString("dialcode.es_host") : host;
		port = Platform.config.hasPath("dialcode.es_port") ? Platform.config.getInt("dialcode.es_port") : port;
		processor = new SearchProcessor(host, port);
	}

	/*
	 * create framework
	 * 
	 * @param Map request
	 * 
	 */
	@Override
	public Response createFramework(Map<String, Object> request, String channelId) throws Exception {
		if (null == request)
			return ERROR("ERR_INVALID_FRAMEWORK_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);

		String code = (String) request.get("code");
		if (StringUtils.isBlank(code))
			throw new ClientException("ERR_FRAMEWORK_CODE_REQUIRED", "Unique code is mandatory for framework",
					ResponseCode.CLIENT_ERROR);

		request.put("identifier", code);

		if (validateChannel(channelId)) {
			Response response = create(request, FRAMEWORK_OBJECT_TYPE);
			if (response.getResponseCode() == ResponseCode.OK)
				generateFrameworkHierarchy(code);
			return response;
		} else {
			return ERROR("ERR_INVALID_CHANNEL_ID", "Invalid Channel Id. Channel doesn't exist.",
					ResponseCode.CLIENT_ERROR);
		}

	}

	/*
	 * Read framework by Id
	 * 
	 * @param graphId
	 * 
	 * @param frameworkId
	 * 
	 */
	// TODO : Uncomment this method
	/*
	 * @Override public Response readFramework(String frameworkId) throws Exception
	 * { return read(frameworkId, FRAMEWORK_OBJECT_TYPE,
	 * FrameworkEnum.framework.name());; }
	 */

	// TODO : Delete this method and uncomment above method
	@SuppressWarnings("unchecked")
	@Override
	public Response readFramework(String frameworkId) throws Exception {

		Response response = read(frameworkId, FRAMEWORK_OBJECT_TYPE, FrameworkEnum.framework.name());
		Map<String, Object> responseMap = (Map<String, Object>) response.get(FrameworkEnum.framework.name());
		responseMap.put("dummy_categories", getCategoriesList(frameworkId));

		List<Object> searchResult = searchFramework(frameworkId);
		if (null != searchResult && !searchResult.isEmpty()) {
			Map<String, Object> framework = (Map<String, Object>) searchResult.get(0);
			Map<String, Object> hierarchy = mapper.readValue((String) framework.get("fr_hierarchy"), Map.class);
			Object categories = hierarchy.get("categories");
			if (categories != null) {
				responseMap.put("categories", categories);
			}
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	private List<Object> getCategoriesList(String frameworkId) {
		List<Object> categories = null;
		try {
			String str = "{\"categories\":[{\"identifier\":\"" + frameworkId
					+ "_class\",\"name\":\"Class\",\"code\":\"class\",\"objectType\":\"CategoryInstance\",\"relation\":\"hasSequenceMember\",\"description\":\"Class\",\"index\":1,\"terms\":[{\"identifier\":\""
					+ frameworkId
					+ "_class_class1\",\"name\":\"Class1\",\"objectType\":\"Term\",\"relation\":\"hasSequenceMember\",\"description\":\"Term for Class 1\",\"index\":1,\"code\":\"class1\",\"children\":[],\"associations\":[{\"identifier\":\""
					+ frameworkId
					+ "_subject_english\",\"code\":\"english\",\"name\":\"English\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_math\",\"code\":\"math\",\"name\":\"Math\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_hindi\",\"code\":\"hindi\",\"name\":\"Hindi\",\"category\":\"subject\"}]},{\"identifier\":\""
					+ frameworkId
					+ "_class_class2\",\"name\":\"Class2\",\"objectType\":\"Term\",\"relation\":\"hasSequenceMember\",\"description\":\"Term for Class 2\",\"index\":2,\"code\":\"class2\",\"children\":[],\"associations\":[{\"identifier\":\""
					+ frameworkId
					+ "_subject_english\",\"code\":\"english\",\"name\":\"English\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_math\",\"code\":\"math\",\"name\":\"Math\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_hindi\",\"code\":\"hindi\",\"name\":\"Hindi\",\"category\":\"subject\"}]},{\"identifier\":\""
					+ frameworkId
					+ "_class_class3\",\"name\":\"Class3\",\"objectType\":\"Term\",\"relation\":\"hasSequenceMember\",\"description\":\"Term for Class 3\",\"index\":3,\"code\":\"class3\",\"children\":[],\"associations\":[{\"identifier\":\""
					+ frameworkId
					+ "_subject_english\",\"code\":\"english\",\"name\":\"English\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_math\",\"code\":\"math\",\"name\":\"Math\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_hindi\",\"code\":\"hindi\",\"name\":\"Hindi\",\"category\":\"subject\"}]},{\"identifier\":\""
					+ frameworkId
					+ "_class_class4\",\"name\":\"Class4\",\"objectType\":\"Term\",\"relation\":\"hasSequenceMember\",\"description\":\"Term for Class 4\",\"index\":4,\"code\":\"class4\",\"children\":[],\"associations\":[{\"identifier\":\""
					+ frameworkId
					+ "_subject_english\",\"code\":\"english\",\"name\":\"English\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_math\",\"code\":\"math\",\"name\":\"Math\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_hindi\",\"code\":\"hindi\",\"name\":\"Hindi\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_environmental-science\",\"code\":\"environmental-science\",\"name\":\"Environmental Science\",\"category\":\"subject\"}]},{\"identifier\":\""
					+ frameworkId
					+ "_class_class5\",\"name\":\"Class5\",\"objectType\":\"Term\",\"relation\":\"hasSequenceMember\",\"description\":\"Term for Class 5\",\"index\":5,\"code\":\"class5\",\"children\":[],\"associations\":[{\"identifier\":\""
					+ frameworkId
					+ "_subject_english\",\"code\":\"english\",\"name\":\"English\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_math\",\"code\":\"math\",\"name\":\"Math\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_hindi\",\"code\":\"hindi\",\"name\":\"Hindi\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_environmental-science\",\"code\":\"environmental-science\",\"name\":\"Environmental Science\",\"category\":\"subject\"}]},{\"identifier\":\""
					+ frameworkId
					+ "_class_class6\",\"name\":\"Class6\",\"objectType\":\"Term\",\"relation\":\"hasSequenceMember\",\"description\":\"Term for Class 6\",\"index\":6,\"code\":\"class6\",\"children\":[],\"associations\":[{\"identifier\":\""
					+ frameworkId
					+ "_subject_english\",\"code\":\"english\",\"name\":\"English\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_math\",\"code\":\"math\",\"name\":\"Math\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_hindi\",\"code\":\"hindi\",\"name\":\"Hindi\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_environmental-science\",\"code\":\"environmental-science\",\"name\":\"Environmental Science\",\"category\":\"subject\"}]},{\"identifier\":\""
					+ frameworkId
					+ "_class_class7\",\"name\":\"Class7\",\"objectType\":\"Term\",\"relation\":\"hasSequenceMember\",\"description\":\"Term for Class 7\",\"index\":7,\"code\":\"class7\",\"children\":[],\"associations\":[{\"identifier\":\""
					+ frameworkId
					+ "_subject_english\",\"code\":\"english\",\"name\":\"English\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_math\",\"code\":\"math\",\"name\":\"Math\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_hindi\",\"code\":\"hindi\",\"name\":\"Hindi\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_environmental-science\",\"code\":\"environmental-science\",\"name\":\"Environmental Science\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_science\",\"code\":\"science\",\"name\":\"Science\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_social-science\",\"code\":\"social-science\",\"name\":\"Social Science\",\"category\":\"subject\"}]},{\"identifier\":\""
					+ frameworkId
					+ "_class_class8\",\"name\":\"Class8\",\"objectType\":\"Term\",\"relation\":\"hasSequenceMember\",\"description\":\"Term for Class 8\",\"index\":8,\"code\":\"class8\",\"children\":[],\"associations\":[{\"identifier\":\""
					+ frameworkId
					+ "_subject_english\",\"code\":\"english\",\"name\":\"English\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_math\",\"code\":\"math\",\"name\":\"Math\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_hindi\",\"code\":\"hindi\",\"name\":\"Hindi\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_environmental-science\",\"code\":\"environmental-science\",\"name\":\"Environmental Science\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_science\",\"code\":\"science\",\"name\":\"Science\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_social-science\",\"code\":\"social-science\",\"name\":\"Social Science\",\"category\":\"subject\"}]},{\"identifier\":\""
					+ frameworkId
					+ "_class_class9\",\"name\":\"Class9\",\"objectType\":\"Term\",\"relation\":\"hasSequenceMember\",\"description\":\"Term for Class 9\",\"index\":9,\"code\":\"class9\",\"children\":[],\"associations\":[{\"identifier\":\""
					+ frameworkId
					+ "_subject_english\",\"code\":\"english\",\"name\":\"English\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_math\",\"code\":\"math\",\"name\":\"Math\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_hindi\",\"code\":\"hindi\",\"name\":\"Hindi\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_environmental-science\",\"code\":\"environmental-science\",\"name\":\"Environmental Science\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_science\",\"code\":\"science\",\"name\":\"Science\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_social-science\",\"code\":\"social-science\",\"name\":\"Social Science\",\"category\":\"subject\"}]},{\"identifier\":\""
					+ frameworkId
					+ "_class_class10\",\"name\":\"Class10\",\"objectType\":\"Term\",\"relation\":\"hasSequenceMember\",\"description\":\"Term for Class 10\",\"index\":10,\"code\":\"class10\",\"children\":[],\"associations\":[{\"identifier\":\""
					+ frameworkId
					+ "_subject_english\",\"code\":\"english\",\"name\":\"English\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_math\",\"code\":\"math\",\"name\":\"Math\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_hindi\",\"code\":\"hindi\",\"name\":\"Hindi\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_environmental-science\",\"code\":\"environmental-science\",\"name\":\"Environmental Science\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_science\",\"code\":\"science\",\"name\":\"Science\",\"category\":\"subject\"},{\"identifier\":\""
					+ frameworkId
					+ "_subject_social-science\",\"code\":\"social-science\",\"name\":\"Social Science\",\"category\":\"subject\"}]}]},{\"identifier\":\""
					+ frameworkId
					+ "_subject\",\"name\":\"Subject\",\"code\":\"subject\",\"objectType\":\"CategoryInstance\",\"relation\":\"hasSequenceMember\",\"description\":\"Subject\",\"index\":2,\"terms\":[{\"identifier\":\""
					+ frameworkId
					+ "_subject_english\",\"name\":\"English\",\"objectType\":\"Term\",\"relation\":\"hasSequenceMember\",\"description\":\"Term for English\",\"index\":1,\"code\":\"english\",\"children\":[],\"associations\":[{\"identifier\":\""
					+ frameworkId
					+ "_medium_english\",\"code\":\"english\",\"name\":\"English\",\"category\":\"medium\"}]},{\"identifier\":\""
					+ frameworkId
					+ "_subject_math\",\"name\":\"Math\",\"objectType\":\"Term\",\"relation\":\"hasSequenceMember\",\"description\":\"Term for Math\",\"index\":2,\"code\":\"math\",\"children\":[],\"associations\":[{\"identifier\":\""
					+ frameworkId
					+ "_medium_english\",\"code\":\"english\",\"name\":\"English\",\"category\":\"medium\"},{\"identifier\":\""
					+ frameworkId + "_medium_hindi\",\"code\":\"hindi\",\"name\":\"Hindi\"}]},{\"identifier\":\""
					+ frameworkId
					+ "_subject_hindi\",\"name\":\"Hindi\",\"objectType\":\"Term\",\"relation\":\"hasSequenceMember\",\"description\":\"Term for Hindi\",\"index\":3,\"code\":\"hindi\",\"children\":[],\"associations\":[{\"identifier\":\""
					+ frameworkId
					+ "_medium_hindi\",\"code\":\"hindi\",\"name\":\"Hindi\",\"category\":\"medium\"}]},{\"identifier\":\""
					+ frameworkId
					+ "_subject_environmental-science\",\"name\":\"Environmental Science\",\"objectType\":\"Term\",\"relation\":\"hasSequenceMember\",\"description\":\"Term for Environmental Science\",\"index\":4,\"code\":\"environmental-science\",\"children\":[],\"associations\":[{\"identifier\":\""
					+ frameworkId
					+ "_medium_english\",\"code\":\"english\",\"name\":\"English\",\"category\":\"medium\"},{\"identifier\":\""
					+ frameworkId
					+ "_medium_hindi\",\"code\":\"hindi\",\"name\":\"Hindi\",\"category\":\"medium\"}]},{\"identifier\":\""
					+ frameworkId
					+ "_subject_science\",\"name\":\"Science\",\"objectType\":\"Term\",\"relation\":\"hasSequenceMember\",\"description\":\"Term for Science\",\"index\":5,\"code\":\"science\",\"children\":[],\"associations\":[{\"identifier\":\""
					+ frameworkId
					+ "_medium_english\",\"code\":\"english\",\"name\":\"English\",\"category\":\"medium\"},{\"identifier\":\""
					+ frameworkId
					+ "_medium_hindi\",\"code\":\"hindi\",\"name\":\"Hindi\",\"category\":\"medium\"}]},{\"identifier\":\""
					+ frameworkId
					+ "_subject_social-science\",\"name\":\"Social Science\",\"objectType\":\"Term\",\"relation\":\"hasSequenceMember\",\"description\":\"Term for Social Science\",\"index\":6,\"code\":\"social-science\",\"children\":[],\"associations\":[{\"identifier\":\""
					+ frameworkId
					+ "_medium_english\",\"code\":\"english\",\"name\":\"English\",\"category\":\"medium\"},{\"identifier\":\""
					+ frameworkId
					+ "_medium_hindi\",\"code\":\"hindi\",\"name\":\"Hindi\",\"category\":\"medium\"}]}]},{\"identifier\":\""
					+ frameworkId
					+ "_medium\",\"name\":\"Medium\",\"code\":\"medium\",\"objectType\":\"CategoryInstance\",\"relation\":\"hasSequenceMember\",\"description\":\"Medium\",\"index\":3,\"terms\":[{\"identifier\":\""
					+ frameworkId
					+ "_medium_english\",\"name\":\"English\",\"objectType\":\"Term\",\"relation\":\"hasSequenceMember\",\"description\":\"Term for Medium\",\"index\":1,\"code\":\"english\",\"children\":[],\"associations\":[]},{\"identifier\":\""
					+ frameworkId
					+ "_medium_hindi\",\"name\":\"Hindi\",\"objectType\":\"Term\",\"relation\":\"hasSequenceMember\",\"description\":\"Term for Medium\",\"index\":2,\"code\":\"hindi\",\"children\":[],\"associations\":[]}]}]}";
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> map = mapper.readValue(str, new TypeReference<Map<String, Object>>() {
			});
			categories = (List<Object>) map.get("categories");
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return categories;

	}

	private List<Object> searchFramework(String frameworkId) throws Exception {
		List<Object> searchResult = new ArrayList<Object>();
		SearchDTO searchDto = new SearchDTO();
		searchDto.setFuzzySearch(false);

		searchDto.setProperties(setSearchProperties(frameworkId));
		searchDto.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		searchDto.setFields(getFields());
		searchDto.setLimit(1);

		searchResult = (List<Object>) processor.processSearchQuery(searchDto, false,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, false);

		return searchResult;
	}

	private List<String> getFields() {
		List<String> fields = new ArrayList<String>();
		fields.add("fr_hierarchy");
		return fields;
	}

	private List<Map> setSearchProperties(String frameworkId) {
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<>();
		property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		property.put("propertyName", "identifier");
		property.put("values", frameworkId);
		properties.add(property);

		property = new HashMap<String, Object>();
		property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		property.put("propertyName", "objectType");
		property.put("values", "Framework");
		properties.add(property);

		return properties;
	}

	/*
	 * Update Framework Details
	 * 
	 * @param frameworkId
	 * 
	 * @param Map<String,Object> map
	 * 
	 */
	@Override
	public Response updateFramework(String frameworkId, String channelId, Map<String, Object> map) throws Exception {
		Response getNodeResponse = getDataNode(GRAPH_ID, frameworkId);
		if (checkError(getNodeResponse))
			throw new ResourceNotFoundException("ERR_FRAMEWORK_NOT_FOUND",
					"Framework Not Found With Id : " + frameworkId);
		Node graphNode = (Node) getNodeResponse.get(GraphDACParams.node.name());
		String ownerChannelId = (String) graphNode.getMetadata().get("channel");
		if (!(channelId.equalsIgnoreCase(ownerChannelId))) {
			return ERROR("ERR_SERVER_ERROR_UPDATE_FRAMEWORK", "Invalid Request. Channel Id Not Matched.",
					ResponseCode.CLIENT_ERROR);
		}
		
		Response response = update(frameworkId, FRAMEWORK_OBJECT_TYPE, map);
		if (response.getResponseCode() == ResponseCode.OK)
			generateFrameworkHierarchy(frameworkId);
		return response;

	}

	/*
	 * Read list of all framework based on criteria
	 * 
	 * @param Map<String,Object> map
	 * 
	 */
	@Override
	public Response listFramework(Map<String, Object> map) throws Exception {
		if (map == null)
			return ERROR("ERR_INVALID_SEARCH_REQUEST", "Invalid Search Request", ResponseCode.CLIENT_ERROR);

		return search(map, FRAMEWORK_OBJECT_TYPE, "frameworks", null);

	}

	/*
	 * Retire Framework - will update the status From "Live" to "Retire"
	 * 
	 * @param frameworkId
	 * 
	 */

	@Override
	public Response retireFramework(String frameworkId, String channelId) throws Exception {
		Response getNodeResponse = getDataNode(GRAPH_ID, frameworkId);
		if (checkError(getNodeResponse))
			throw new ResourceNotFoundException("ERR_FRAMEWORK_NOT_FOUND",
					"Framework Not Found With Id : " + frameworkId);
		Node frameworkNode = (Node) getNodeResponse.get(GraphDACParams.node.name());

		String ownerChannelId = (String) frameworkNode.getMetadata().get("channel");

		if (!(channelId.equalsIgnoreCase(ownerChannelId))) {
			return ERROR("ERR_SERVER_ERROR_UPDATE_FRAMEWORK", "Invalid Request. Channel Id Not Matched.",
					ResponseCode.CLIENT_ERROR);
		}

		return retire(frameworkId, FRAMEWORK_OBJECT_TYPE);

	}

}