package com.ilimi.taxonomy.mgr.impl;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.common.Identifier;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.enums.SuggestionConstants;
import com.ilimi.taxonomy.enums.SuggestionErrorCodeConstants;
import com.ilimi.taxonomy.mgr.ISuggestionManager;

@Component
public class SuggestionManager extends BaseManager implements ISuggestionManager {

	/** The ElasticSearchUtil */
	private ElasticSearchUtil es = new ElasticSearchUtil();

	/** The ControllerUtil */
	private ControllerUtil util = new ControllerUtil();

	/** The Class Logger. */
	private static LogHelper LOGGER = LogHelper.getInstance(SuggestionManager.class.getName());

	private ObjectMapper mapper = new ObjectMapper();

	Response response = new Response();
	
	@Override
	public Response createSuggestion(Map<String, Object> request) {
		String suggestionId = null;
		try {
			String identifier = (String) request.get("identifier");
			Node node = util.getNode(SuggestionConstants.GRAPH_ID, identifier);
			if (StringUtils.equalsIgnoreCase(identifier, node.getIdentifier())) {
				suggestionId = saveSuggestion(request);
				response = setResponse(suggestionId);
			} else {
				throw new ClientException(SuggestionErrorCodeConstants.invalid_content_id.name(),
						"Content_Id doesnt exists | Invalid Content_id");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;

	}

	private Response setResponse(String suggestionId) {
		response.setParams(response.getParams());
		response.getResult().put("suggestion_id", suggestionId);
		response.setResponseCode(response.getResponseCode());
		return response;
	}

	public String saveSuggestion(Map<String, Object> entity_map) throws IOException {
		createIndex();
		String identifier = addDocument(entity_map);
		return identifier;
	}

	public void createIndex() throws IOException {
		String settings = "{ \"settings\": {   \"index\": {     \"index\": \"" + SuggestionConstants.SUGGESTION_INDEX
				+ "\",     \"type\": \"" + SuggestionConstants.SUGGESTION_INDEX_TYPE
				+ "\",     \"analysis\": {       \"analyzer\": {         \"sg_index_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"lowercase\",             \"mynGram\"           ]         },         \"sg_search_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",             \"symbol\"           ]         }       }     }   } }}";
		String mappings = "{ \"" + SuggestionConstants.SUGGESTION_INDEX_TYPE
				+ "\" : {    \"dynamic_templates\": [      {        \"longs\": {          \"match_mapping_type\": \"long\",          \"mapping\": {            \"type\": \"long\",            fields: {              \"raw\": {                \"type\": \"long\"              }            }          }        }      },      {        \"booleans\": {          \"match_mapping_type\": \"boolean\",          \"mapping\": {            \"type\": \"boolean\",            fields: {              \"raw\": {                \"type\": \"boolean\"              }            }          }        }      },{        \"doubles\": {          \"match_mapping_type\": \"double\",          \"mapping\": {            \"type\": \"double\",            fields: {              \"raw\": {                \"type\": \"double\"              }            }          }        }      },	  {        \"dates\": {          \"match_mapping_type\": \"date\",          \"mapping\": {            \"type\": \"date\",            fields: {              \"raw\": {                \"type\": \"date\"              }            }          }        }      },      {        \"strings\": {          \"match_mapping_type\": \"string\",          \"mapping\": {            \"type\": \"string\",            \"copy_to\": \"all_fields\",            \"analyzer\": \"sg_index_analyzer\",            \"search_analyzer\": \"sg_search_analyzer\",            fields: {              \"raw\": {                \"type\": \"string\",                \"analyzer\": \"keylower\"              }            }          }        }      }    ],    \"properties\": {      \"all_fields\": {        \"type\": \"string\",        \"analyzer\": \"sg_index_analyzer\",        \"search_analyzer\": \"sg_search_analyzer\",        fields: {          \"raw\": {            \"type\": \"string\",            \"analyzer\": \"keylower\"          }        }      }    }  }}";
		LOGGER.info("Creating Suggestion Index : " + SuggestionConstants.SUGGESTION_INDEX);
		es.addIndex(SuggestionConstants.SUGGESTION_INDEX, SuggestionConstants.SUGGESTION_INDEX_TYPE, settings,
				mappings);
	}

	public String addDocument(Map<String, Object> request) throws IOException {
		String suggestionId = "sg_" + Identifier.getUniqueIdFromTimestamp();
		String document = null;
		if (StringUtils.isNoneBlank(suggestionId)) {
			request.put("suggestionId", suggestionId);
			LOGGER.info("Checking if document is empty : " + request);
			if (!request.isEmpty()) {
				document = mapper.writeValueAsString(request);
				LOGGER.info("converting request map tp string : " + document);
			}
			if (StringUtils.isNotBlank(document)) {
				es.addDocument(SuggestionConstants.SUGGESTION_INDEX, SuggestionConstants.SUGGESTION_INDEX_TYPE,
						document);
				LOGGER.info("Adding document to Suggetion Index : " + document);
			}
		}
		return suggestionId;
	}
}
