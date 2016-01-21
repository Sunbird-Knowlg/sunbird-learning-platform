package org.ekstep.language.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.language.common.LanguageMap;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageObjectTypes;

import com.ilimi.common.dto.NodeDTO;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.RequestParams;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.Sort;
import com.ilimi.graph.dac.model.TagCriterion;
import com.ilimi.graph.engine.router.GraphEngineManagers;

public class WordUtil extends BaseManager {

	private ObjectMapper mapper = new ObjectMapper();
	private static Logger LOGGER = LogManager.getLogger(WordUtil.class
			.getName());

	@SuppressWarnings("unchecked")
	protected Request getRequest(Map<String, Object> requestMap) {
		Request request = new Request();
		if (null != requestMap && !requestMap.isEmpty()) {
			String id = (String) requestMap.get("id");
			String ver = (String) requestMap.get("ver");
			String ts = (String) requestMap.get("ts");
			request.setId(id);
			request.setVer(ver);
			request.setTs(ts);
			Object reqParams = requestMap.get("params");
			if (null != reqParams) {
				try {
					RequestParams params = (RequestParams) mapper.convertValue(
							reqParams, RequestParams.class);
					request.setParams(params);
				} catch (Exception e) {
				}
			}
			Object requestObj = requestMap.get("request");
			if (null != requestObj) {
				try {
					String strRequest = mapper.writeValueAsString(requestObj);
					Map<String, Object> map = mapper.readValue(strRequest,
							Map.class);
					if (null != map && !map.isEmpty())
						request.setRequest(map);
				} catch (Exception e) {
				}
			}
		}
		return request;
	}

	private void setLimit(Request request, SearchCriteria sc) {
		Integer limit = null;
		try {
			Object obj = request.get(PARAM_LIMIT);
			if (obj instanceof String)
				limit = Integer.parseInt((String) obj);
			else
				limit = (Integer) request.get(PARAM_LIMIT);
		} catch (Exception e) {
		}
		if (null == limit || limit.intValue() <= 0)
			limit = DEFAULT_LIMIT;
		sc.setResultSize(limit);
	}

	@SuppressWarnings("rawtypes")
	private List getList(ObjectMapper mapper, Object object, String propName) {
		if (null != object) {
			try {
				String strObject = mapper.writeValueAsString(object);
				List list = mapper.readValue(strObject.toString(), List.class);
				return list;
			} catch (Exception e) {
				List<String> list = new ArrayList<String>();
				list.add(object.toString());
				return list;
			}
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getWordIdentifier(String languageId, String word) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		LinkedHashMap<String, List> lemmaMap = new LinkedHashMap<String, List>();
		lemmaMap.put("lemma", getList(mapper, word, null));
		map.put("request", lemmaMap);
		Request request = getRequest(map);
		try {
			Response response = list(languageId,
					LanguageObjectTypes.Word.name(), request);
			LOGGER.info("Search | Response: " + response);
			List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("words");
			//Dummy line
			list.get(0);
			
			
		} catch (Exception e) {
			LOGGER.error("Search | Exception: " + e.getMessage(), e);
			e.printStackTrace();
		}
		return "1";
	}

	@SuppressWarnings("unchecked")
	public Response list(String languageId, String objectType, Request request) {
		if (StringUtils.isBlank(languageId)
				|| !LanguageMap.containsLanguage(languageId))
			throw new ClientException(
					LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(),
					"Invalid Language Id");
		SearchCriteria sc = new SearchCriteria();
		sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
		sc.setObjectType(objectType);
		sc.sort(new Sort(SystemProperties.IL_UNIQUE_ID.name(), Sort.SORT_ASC));
		setLimit(request, sc);

		List<Filter> filters = new ArrayList<Filter>();
		List<String> statusList = new ArrayList<String>();
		Object statusParam = request.get(PARAM_STATUS);
		if (null != statusParam) {
			statusList = getList(mapper, statusParam, PARAM_STATUS);
			if (null != statusList && !statusList.isEmpty())
				filters.add(new Filter(PARAM_STATUS, SearchConditions.OP_IN,
						statusList));
		}
		if (null != request.getRequest() && !request.getRequest().isEmpty()) {
			for (Entry<String, Object> entry : request.getRequest().entrySet()) {
				if (!StringUtils.equalsIgnoreCase(PARAM_FIELDS, entry.getKey())
						&& !StringUtils.equalsIgnoreCase(PARAM_LIMIT,
								entry.getKey())
						&& !StringUtils.equalsIgnoreCase(PARAM_STATUS,
								entry.getKey())
						&& !StringUtils.equalsIgnoreCase("word-lists",
								entry.getKey())
						&& !StringUtils.equalsIgnoreCase(PARAM_TAGS,
								entry.getKey())) {
					List<String> list = getList(mapper, entry.getValue(),
							entry.getKey());
					if (null != list && !list.isEmpty()) {
						filters.add(new Filter(entry.getKey(),
								SearchConditions.OP_IN, list));
					}
				} else if (StringUtils.equalsIgnoreCase(PARAM_TAGS,
						entry.getKey())) {
					List<String> tags = getList(mapper, entry.getValue(),
							entry.getKey());
					if (null != tags && !tags.isEmpty()) {
						TagCriterion tc = new TagCriterion(tags);
						sc.setTag(tc);
					}
				}
			}
		}
		if (null != filters && !filters.isEmpty()) {
			MetadataCriterion mc = MetadataCriterion.create(filters);
			sc.addMetadata(mc);
		}
		Request req = getRequest(languageId,
				GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.search_criteria.name(), sc);
		req.put(GraphDACParams.get_tags.name(), true);
		Response listRes = getResponse(req, LOGGER);
		if (checkError(listRes))
			return listRes;
		else {
			List<Node> nodes = (List<Node>) listRes
					.get(GraphDACParams.node_list.name());
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			if (null != nodes && !nodes.isEmpty()) {
				String[] fields = getFields(request);
				for (Node node : nodes) {
					Map<String, Object> map = convertGraphNode(node,
							languageId, fields);
					if (null != map && !map.isEmpty()) {
						list.add(map);
					}
				}
			}
			Response response = copyResponse(listRes);
			response.put("words", list);
			return response;
		}
	}

	@SuppressWarnings("unchecked")
	private String[] getFields(Request request) {
		Object objFields = request.get(PARAM_FIELDS);
		List<String> fields = getList(mapper, objFields, PARAM_FIELDS);
		if (null != fields && !fields.isEmpty()) {
			String[] arr = new String[fields.size()];
			for (int i = 0; i < fields.size(); i++)
				arr[i] = fields.get(i);
			return arr;
		}
		return null;
	}

	private Map<String, Object> convertGraphNode(Node node, String languageId,
			String[] fields) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (null != node) {
			if (null != node.getMetadata() && !node.getMetadata().isEmpty()) {
				if (null == fields || fields.length <= 0)
					map.putAll(node.getMetadata());
				else {
					for (String field : fields) {
						if (node.getMetadata().containsKey(field))
							map.put(field, node.getMetadata().get(field));
					}
				}
			}
			addRelationsData(node, map);
			if (null != node.getTags() && !node.getTags().isEmpty()) {
				map.put("tags", node.getTags());
			}
			map.put("identifier", node.getIdentifier());
			map.put("language", LanguageMap.getLanguage(languageId));
		}
		return map;
	}

	private void addRelationsData(Node node, Map<String, Object> map) {
		List<Map<String, Object>> synonyms = new ArrayList<Map<String, Object>>();
		List<NodeDTO> antonyms = new ArrayList<NodeDTO>();
		List<NodeDTO> hypernyms = new ArrayList<NodeDTO>();
		List<NodeDTO> hyponyms = new ArrayList<NodeDTO>();
		List<NodeDTO> homonyms = new ArrayList<NodeDTO>();
		List<NodeDTO> meronyms = new ArrayList<NodeDTO>();
		getInRelationsData(node, synonyms, antonyms, hypernyms, hyponyms,
				homonyms, meronyms);
		getOutRelationsData(node, synonyms, antonyms, hypernyms, hyponyms,
				homonyms, meronyms);
		if (!synonyms.isEmpty())
			map.put("synonyms", synonyms);
		if (!antonyms.isEmpty())
			map.put("antonyms", antonyms);
		if (!hypernyms.isEmpty())
			map.put("hypernyms", hypernyms);
		if (!hyponyms.isEmpty())
			map.put("hyponyms", hyponyms);
		if (!homonyms.isEmpty())
			map.put("homonyms", homonyms);
		if (!meronyms.isEmpty())
			map.put("meronyms", meronyms);
	}

	private void getInRelationsData(Node node,
			List<Map<String, Object>> synonyms, List<NodeDTO> antonyms,
			List<NodeDTO> hypernyms, List<NodeDTO> hyponyms,
			List<NodeDTO> homonyms, List<NodeDTO> meronyms) {
		if (null != node.getInRelations() && !node.getInRelations().isEmpty()) {
			for (Relation inRel : node.getInRelations()) {
				if (StringUtils.equalsIgnoreCase(
						RelationTypes.SYNONYM.relationName(),
						inRel.getRelationType())) {
					if (null != inRel.getStartNodeMetadata()
							&& !inRel.getStartNodeMetadata().isEmpty()) {
						inRel.getStartNodeMetadata().remove(
								SystemProperties.IL_FUNC_OBJECT_TYPE.name());
						inRel.getStartNodeMetadata().remove(
								SystemProperties.IL_SYS_NODE_TYPE.name());
						inRel.getStartNodeMetadata().remove(
								SystemProperties.IL_UNIQUE_ID.name());
						synonyms.add(inRel.getStartNodeMetadata());
					}
				} else if (StringUtils.equalsIgnoreCase(
						RelationTypes.ANTONYM.relationName(),
						inRel.getRelationType())) {
					antonyms.add(new NodeDTO(inRel.getStartNodeId(), inRel
							.getStartNodeName(),
							inRel.getStartNodeObjectType(), inRel
									.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(
						RelationTypes.HYPERNYM.relationName(),
						inRel.getRelationType())) {
					hypernyms.add(new NodeDTO(inRel.getStartNodeId(), inRel
							.getStartNodeName(),
							inRel.getStartNodeObjectType(), inRel
									.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(
						RelationTypes.HYPONYM.relationName(),
						inRel.getRelationType())) {
					hyponyms.add(new NodeDTO(inRel.getStartNodeId(), inRel
							.getStartNodeName(),
							inRel.getStartNodeObjectType(), inRel
									.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(
						RelationTypes.MERONYM.relationName(),
						inRel.getRelationType())) {
					meronyms.add(new NodeDTO(inRel.getStartNodeId(), inRel
							.getStartNodeName(),
							inRel.getStartNodeObjectType(), inRel
									.getRelationType()));
				}
			}
		}
	}

	private void getOutRelationsData(Node node,
			List<Map<String, Object>> synonyms, List<NodeDTO> antonyms,
			List<NodeDTO> hypernyms, List<NodeDTO> hyponyms,
			List<NodeDTO> homonyms, List<NodeDTO> meronyms) {
		if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
			for (Relation outRel : node.getOutRelations()) {
				if (StringUtils.equalsIgnoreCase(
						RelationTypes.SYNONYM.relationName(),
						outRel.getRelationType())) {
					if (null != outRel.getEndNodeMetadata()
							&& !outRel.getEndNodeMetadata().isEmpty()) {
						outRel.getEndNodeMetadata().remove(
								SystemProperties.IL_FUNC_OBJECT_TYPE.name());
						outRel.getEndNodeMetadata().remove(
								SystemProperties.IL_SYS_NODE_TYPE.name());
						outRel.getEndNodeMetadata().remove(
								SystemProperties.IL_UNIQUE_ID.name());
						synonyms.add(outRel.getEndNodeMetadata());
					}
				} else if (StringUtils.equalsIgnoreCase(
						RelationTypes.ANTONYM.relationName(),
						outRel.getRelationType())) {
					antonyms.add(new NodeDTO(outRel.getEndNodeId(), outRel
							.getEndNodeName(), outRel.getEndNodeObjectType(),
							outRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(
						RelationTypes.HYPERNYM.relationName(),
						outRel.getRelationType())) {
					hypernyms.add(new NodeDTO(outRel.getEndNodeId(), outRel
							.getEndNodeName(), outRel.getEndNodeObjectType(),
							outRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(
						RelationTypes.HYPONYM.relationName(),
						outRel.getRelationType())) {
					hyponyms.add(new NodeDTO(outRel.getEndNodeId(), outRel
							.getEndNodeName(), outRel.getEndNodeObjectType(),
							outRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(
						RelationTypes.MERONYM.relationName(),
						outRel.getRelationType())) {
					meronyms.add(new NodeDTO(outRel.getEndNodeId(), outRel
							.getEndNodeName(), outRel.getEndNodeObjectType(),
							outRel.getRelationType()));
				}
			}
		}
	}

}
