package com.ilimi.dac.impl.entity.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.dac.enums.AuditHistoryConstants;

@Component("auditHistoryEsDao")
public class AuditHistoryEsDao {

	/** The Logger */
	private static ILogger LOGGER = PlatformLogManager.getLogger();
	
	/** The Object Mapper */
	private ObjectMapper mapper = new ObjectMapper();

	/** The ElasticSearchUtil */
	private ElasticSearchUtil es = new ElasticSearchUtil();

	/** The SearchProcessor */
	private SearchProcessor processor = new SearchProcessor();
	
	public void save(Map<String, Object> entity_map) throws IOException {
			createIndex();
			addDocument(entity_map);
	}
	
	public List<Object> search(SearchDTO search) {
		List<Object>  result= new ArrayList<Object>();
			try {
				LOGGER.log("sending search request to search processor" ,search);
				result = (List<Object>) processor.processSearchAuditHistory(search, false, AuditHistoryConstants.AUDIT_HISTORY_INDEX);
				LOGGER.log("result from search processor" , result);
			} catch (Exception e) {
				LOGGER.log("error while processing the search request", e.getMessage(), e);
				e.printStackTrace();
			}
		return result;
	}
	
	public void createIndex() throws IOException {
		String settings = "{ \"settings\": {   \"index\": {     \"index\": \""+AuditHistoryConstants.AUDIT_HISTORY_INDEX+"\",     \"type\": \""+AuditHistoryConstants.AUDIT_HISTORY_INDEX_TYPE+"\",     \"analysis\": {       \"analyzer\": {         \"ah_index_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"lowercase\",             \"mynGram\"           ]         },         \"ah_search_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",             \"symbol\"           ]         }       }     }   } }}";
		String mappings = "{ \""+AuditHistoryConstants.AUDIT_HISTORY_INDEX_TYPE+"\" : {    \"dynamic_templates\": [      {        \"longs\": {          \"match_mapping_type\": \"long\",          \"mapping\": {            \"type\": \"long\",            fields: {              \"raw\": {                \"type\": \"long\"              }            }          }        }      },      {        \"booleans\": {          \"match_mapping_type\": \"boolean\",          \"mapping\": {            \"type\": \"boolean\",            fields: {              \"raw\": {                \"type\": \"boolean\"              }            }          }        }      },{        \"doubles\": {          \"match_mapping_type\": \"double\",          \"mapping\": {            \"type\": \"double\",            fields: {              \"raw\": {                \"type\": \"double\"              }            }          }        }      },	  {        \"dates\": {          \"match_mapping_type\": \"date\",          \"mapping\": {            \"type\": \"date\",            fields: {              \"raw\": {                \"type\": \"date\"              }            }          }        }      },      {        \"strings\": {          \"match_mapping_type\": \"string\",          \"mapping\": {            \"type\": \"string\",            \"copy_to\": \"all_fields\",            \"analyzer\": \"ah_index_analyzer\",            \"search_analyzer\": \"ah_search_analyzer\",            fields: {              \"raw\": {                \"type\": \"string\",                \"analyzer\": \"keylower\"              }            }          }        }      }    ],    \"properties\": {      \"all_fields\": {        \"type\": \"string\",        \"analyzer\": \"ah_index_analyzer\",        \"search_analyzer\": \"ah_search_analyzer\",        fields: {          \"raw\": {            \"type\": \"string\",            \"analyzer\": \"keylower\"          }        }      }    }  }}";
		LOGGER.log("Creating Audit History Index : " , AuditHistoryConstants.AUDIT_HISTORY_INDEX);
		es.addIndex(AuditHistoryConstants.AUDIT_HISTORY_INDEX, AuditHistoryConstants.AUDIT_HISTORY_INDEX_TYPE,
				settings, mappings);
	}

	public void addDocument(Map<String, Object> request) throws IOException {
		String document = null;
		LOGGER.log("Checking if document is empty : " + request);
		if(!request.isEmpty()){
			document = mapper.writeValueAsString(request);
			LOGGER.log("converting request map tp string : " + document);
		}
		if(StringUtils.isNotBlank(document)){
			es.addDocument(AuditHistoryConstants.AUDIT_HISTORY_INDEX, AuditHistoryConstants.AUDIT_HISTORY_INDEX_TYPE, document);
			LOGGER.log("Adding document to Audit History Index : " , document);
		}
	}

	public void delete(String query) throws IOException {
		LOGGER.log("deleting Audit History Index : " , AuditHistoryConstants.AUDIT_HISTORY_INDEX);
		es.deleteDocumentsByQuery(query.toString(), AuditHistoryConstants.AUDIT_HISTORY_INDEX, AuditHistoryConstants.AUDIT_HISTORY_INDEX_TYPE);
		LOGGER.log("Documents deleted from Audit History Index");
	}
	
	@PreDestroy
	public void shutdown(){
		LOGGER.log("shuting down elastic search instance" , es);
		es.finalize();
	}
}
