package org.ekstep.framework.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.mgr.ConvertGraphNode;
import org.ekstep.common.slugs.Slug;
import org.ekstep.framework.enums.FrameworkEnum;
import org.ekstep.framework.mgr.IFrameworkManager;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.model.node.DefinitionDTO;
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
	private SearchProcessor processor = null;
	private static ObjectMapper mapper = new ObjectMapper();

	@PostConstruct
	public void init() {
		processor = new SearchProcessor();
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

		if (validateObject(channelId)) {
			Response response = create(request, FRAMEWORK_OBJECT_TYPE);
			if (response.getResponseCode() == ResponseCode.OK) {
				if (Platform.config.hasPath("framework.es.sync")) {
					if (Platform.config.getBoolean("framework.es.sync")) {
						generateFrameworkHierarchy(code);
					}
				}
				
			}
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
	public Response readFramework(String frameworkId, List<String> returnCategories) throws Exception {
		Response response = read(frameworkId, FRAMEWORK_OBJECT_TYPE, FrameworkEnum.framework.name());
		if (Platform.config.hasPath("framework.es.sync")) {
			if (Platform.config.getBoolean("framework.es.sync")) {
				Map<String, Object> responseMap = (Map<String, Object>) response.get(FrameworkEnum.framework.name());
				List<Object> searchResult = searchFramework(frameworkId);
				if (null != searchResult && !searchResult.isEmpty()) {
					Map<String, Object> framework = (Map<String, Object>) searchResult.get(0);
					Map<String, Object> hierarchy = mapper.readValue((String) framework.get("fw_hierarchy"), Map.class);
					if (null != hierarchy && !hierarchy.isEmpty()) {
						List<Map<String, Object>> categories = hierarchy.get("categories");
						
						String concepts = "{\"identifier\":\"xyz\",\"name\":\"Concepts\",\"code\":\"concepts\",\"description\":\"\",\"index\":"
								+ (categories.size() + 1)
								+ ",\"status\":\"Live\",\"domains\":[{\"identifier\":\"numeracy\",\"name\":\"Numeracy\",\"objectType\":\"Domain\",\"children\":[{\"identifier\":\"D5\",\"name\":\"Data Handling\",\"objectType\":\"Dimension\",\"children\":[]},{\"identifier\":\"D1\",\"name\":\"Geometry\",\"objectType\":\"Dimension\",\"children\":[]},{\"identifier\":\"testDimension1\",\"name\":\"Measurement\",\"objectType\":\"Dimension\",\"children\":[]},{\"identifier\":\"D4\",\"name\":\"Measurement\",\"objectType\":\"Dimension\",\"children\":[]},{\"identifier\":\"D2\",\"name\":\"Number sense\",\"objectType\":\"Dimension\",\"children\":[{\"identifier\":\"C6\",\"name\":\"Counting\",\"objectType\":\"Concept\",\"children\":[{\"identifier\":\"C49\",\"name\":\"Counting objects\",\"objectType\":\"Concept\",\"children\":[{\"identifier\":\"C211\",\"name\":\"Count to 20\",\"objectType\":\"Concept\",\"children\":[{\"identifier\":\"C455\",\"name\":\"Learner does not know the order of numbers.\",\"objectType\":\"Concept\",\"children\":[]},{\"identifier\":\"C456\",\"name\":\"Learner is not able to count till a specified number within a given set.\",\"objectType\":\"Concept\",\"children\":[]},{\"identifier\":\"C451\",\"name\":\"Learner is not able to match the number of objects and its numeral values.\",\"objectType\":\"Concept\",\"children\":[]},{\"identifier\":\"C460\",\"name\":\"Learner skips all even numbers while counting.\",\"objectType\":\"Concept\",\"children\":[]}]}]}]},{\"identifier\":\"C8\",\"name\":\"Place value\",\"objectType\":\"Concept\",\"children\":[{\"identifier\":\"C71\",\"name\":\"Expand a number with respect to place values\",\"objectType\":\"Concept\",\"children\":[]}]}]}]}]}";
						categories.add(mapper.readValue(concepts, Map.class));
						if (categories != null) {
							if (returnCategories != null && !returnCategories.isEmpty()) {
								responseMap.put("categories",
										categories.stream().filter(p -> returnCategories.contains(p.get("code")))
												.collect(Collectors.toList()));
							} else {
								responseMap.put("categories", categories);
							}
						}
					}
					
				}
			}
		}
		return response;
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
		fields.add("fw_hierarchy");
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
			if (Platform.config.hasPath("framework.es.sync")) {
				if (Platform.config.getBoolean("framework.es.sync")) {
					generateFrameworkHierarchy(frameworkId);
				}
			}
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
			throw new ClientException("ERR_INVALID_SEARCH_REQUEST", "Invalid Search Request");

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

	@Override
	  public Response copyFramework(String frameworkId, String channelId, Map<String, Object> request) throws Exception {
	    
	    if (null == request)
	      return ERROR("ERR_INVALID_FRAMEWORK_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);

	    String code = (String) request.get("code");
	    if (StringUtils.isBlank(code))
	      throw new ClientException("ERR_FRAMEWORK_CODE_REQUIRED", 
	          "Unique code is mandatory for framework",
	          ResponseCode.CLIENT_ERROR);

	    if(StringUtils.equals(frameworkId, code))
	      throw new ClientException("ERR_FRAMEWORKID_CODE_MATCHES", 
	          "FrameworkId and code should not be same.", 
	          ResponseCode.CLIENT_ERROR);
	    
	    if(validateObject(code)) {
			throw new ClientException("ERR_FRAMEWORK_EXISTS", 
			          "Framework with code: " + code + ", already exists.",
			          ResponseCode.CLIENT_ERROR);
		}
	    
	    if (validateObject(channelId)) {
	    		String sluggifiedFrameworkId = Slug.makeSlug(frameworkId);
	    		String sluggifiedCode = Slug.makeSlug(code);
	    		Response response = copyHierarchy(frameworkId, code, sluggifiedFrameworkId, sluggifiedCode, request);
	    		if (response.getResponseCode() == ResponseCode.OK) {
	    			if (Platform.config.hasPath("framework.es.sync")) {
					if (Platform.config.getBoolean("framework.es.sync")) {
						generateFrameworkHierarchy(code);
					}
				}
			}
	    		return response;
	    }else {
	    		return ERROR("ERR_INVALID_CHANNEL_ID", "Invalid Channel Id. Channel doesn't exist.",
	    				ResponseCode.CLIENT_ERROR);
	    }
	  }
	  
	  protected Response copyHierarchy(String existingObjectId, String clonedObjectId, String existingFrameworkId, String clonedFrameworkId, Map<String, Object> requestMap) throws Exception{
	    Response responseNode = getDataNode(GRAPH_ID, existingObjectId);
	    if (checkError(responseNode)) {
	    		throw new ResourceNotFoundException("ERR_DATA_NOT_FOUND", "Data not found with id : " + existingObjectId, ResponseCode.RESOURCE_NOT_FOUND);
	    }
	    Node node = (Node) responseNode.get(GraphDACParams.node.name());
	    String objectType = node.getObjectType();
	    DefinitionDTO definition = getDefinition(GRAPH_ID, objectType);
	    
	    Map<String, Object> request = new HashMap<>();
	    String[] fields = getFields(definition);
	    if (fields != null) {
	      for (String field : fields) {
	        request.put(field, node.getMetadata().get(field));
	      }
	    } else {
	      request.putAll(node.getMetadata());
	    }
	    if(StringUtils.equalsIgnoreCase(objectType, "Framework")) {
	    		Set<String> propertySet = requestMap.keySet();
	    		for(String property : propertySet) {
	    			if(request.containsKey(property))
	    				request.put(property, requestMap.get(property));
	    		}
	    	}
	    request.put("identifier", clonedObjectId);
	    
	    
	    Response getNodeResponse = getDataNode(GRAPH_ID, clonedObjectId);
	    if (checkError(getNodeResponse))
	      create(request, objectType);
	    else
	      update(clonedObjectId, objectType, request);
	    
	    Map<String, String> inRelDefMap = new HashMap<>();
	    Map<String, String> outRelDefMap = new HashMap<>();
	    ConvertGraphNode.getRelationDefinitionMaps(definition, inRelDefMap, outRelDefMap);
	    List<Relation> outRelations = node.getOutRelations();
	    if (null != outRelations && !outRelations.isEmpty()) {
	      for (Relation relation : outRelations) {
	    	  	String endNodeObjectType = relation.getEndNodeObjectType();
	    	  	if(StringUtils.equals(endNodeObjectType, "Framework") || 
	    	  			StringUtils.equals(endNodeObjectType, "CategoryInstance") || 
	    	  			StringUtils.equals(endNodeObjectType, "Term")) {
	    	  		String title = outRelDefMap.get(relation.getRelationType() + relation.getEndNodeObjectType());
		        String endNodeId = relation.getEndNodeId();
		        endNodeId = endNodeId.replaceFirst(existingFrameworkId, clonedFrameworkId);
		        Response res = copyHierarchy(relation.getEndNodeId(), endNodeId, existingFrameworkId, clonedFrameworkId, null);
		        
		        Map<String, Object> childObjectMap = new HashMap<>();
		        childObjectMap.put("identifier", res.get("node_id"));
		        childObjectMap.put("index", relation.getMetadata().get("IL_SEQUENCE_INDEX"));
		        
		        if(request.containsKey(title)) {
		          List<Map<String, Object>> relationshipList = (List<Map<String, Object>>)request.get(title);
		          relationshipList.add(childObjectMap);
		        }else {
		          List<Map<String, Object>> relationshipList = new ArrayList<>();
		          relationshipList.add(childObjectMap);
		          request.put(title, relationshipList);
		        }
		      }
		      update(clonedObjectId, objectType, request);
	    	  	}
	    }
	    Response response = new Response();
	    response.put("node_id", clonedObjectId);
	    return response;
	  }
	  
}