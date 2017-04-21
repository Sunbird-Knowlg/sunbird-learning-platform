package com.ilimi.taxonomy.mgr.impl;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Response;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.enums.SuggestionConstants;
import com.ilimi.taxonomy.mgr.ISuggestionManager;

@Component
public class SuggestionManager implements ISuggestionManager{

	/** The ElasticSearchUtil */
	private ElasticSearchUtil es = new ElasticSearchUtil();
	
	private ControllerUtil util = new ControllerUtil();
	
	/** The Class Logger. */
	private static LogHelper LOGGER = LogHelper.getInstance(SuggestionManager.class.getName());
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public Response createSuggestion(Map<String, Object> request) {
		String identifier = (String)request.get("identifier");
	    try {
			save(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public void save(Map<String, Object> entity_map) throws IOException {
		createIndex();
		addDocument(entity_map);
    }
	
	public void createIndex() throws IOException {
		String settings = "{ \"settings\": {   \"index\": {     \"index\": \""+SuggestionConstants.SUGGESTION_INDEX+"\",     \"type\": \""+SuggestionConstants.SUGGESTION_INDEX_TYPE+"\",     \"analysis\": {       \"analyzer\": {         \"sg_index_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"lowercase\",             \"mynGram\"           ]         },         \"sg_search_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",             \"symbol\"           ]         }       }     }   } }}";
		String mappings = "{ \""+SuggestionConstants.SUGGESTION_INDEX_TYPE+"\" : {    \"dynamic_templates\": [      {        \"longs\": {          \"match_mapping_type\": \"long\",          \"mapping\": {            \"type\": \"long\",            fields: {              \"raw\": {                \"type\": \"long\"              }            }          }        }      },      {        \"booleans\": {          \"match_mapping_type\": \"boolean\",          \"mapping\": {            \"type\": \"boolean\",            fields: {              \"raw\": {                \"type\": \"boolean\"              }            }          }        }      },{        \"doubles\": {          \"match_mapping_type\": \"double\",          \"mapping\": {            \"type\": \"double\",            fields: {              \"raw\": {                \"type\": \"double\"              }            }          }        }      },	  {        \"dates\": {          \"match_mapping_type\": \"date\",          \"mapping\": {            \"type\": \"date\",            fields: {              \"raw\": {                \"type\": \"date\"              }            }          }        }      },      {        \"strings\": {          \"match_mapping_type\": \"string\",          \"mapping\": {            \"type\": \"string\",            \"copy_to\": \"all_fields\",            \"analyzer\": \"sg_index_analyzer\",            \"search_analyzer\": \"sg_search_analyzer\",            fields: {              \"raw\": {                \"type\": \"string\",                \"analyzer\": \"keylower\"              }            }          }        }      }    ],    \"properties\": {      \"all_fields\": {        \"type\": \"string\",        \"analyzer\": \"sg_index_analyzer\",        \"search_analyzer\": \"sg_search_analyzer\",        fields: {          \"raw\": {            \"type\": \"string\",            \"analyzer\": \"keylower\"          }        }      }    }  }}";
		LOGGER.info("Creating Suggestion Index : " + SuggestionConstants.SUGGESTION_INDEX);
		es.addIndex(SuggestionConstants.SUGGESTION_INDEX, SuggestionConstants.SUGGESTION_INDEX_TYPE,
				settings, mappings);
	}

	public void addDocument(Map<String, Object> request) throws IOException {
		String document = null;
		LOGGER.info("Checking if document is empty : " + request);
		if(!request.isEmpty()){
			document = mapper.writeValueAsString(request);
			LOGGER.info("converting request map tp string : " + document);
		}
		if(StringUtils.isNotBlank(document)){
			es.addDocument(SuggestionConstants.SUGGESTION_INDEX, SuggestionConstants.SUGGESTION_INDEX_TYPE, document);
			LOGGER.info("Adding document to Suggetion Index : " + document);
		}
	}
}
