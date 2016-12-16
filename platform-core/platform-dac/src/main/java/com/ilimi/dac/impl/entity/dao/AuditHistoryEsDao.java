package com.ilimi.dac.impl.entity.dao;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.dac.enums.AuditHistoryConstants;
import com.ilimi.dac.impl.entity.AuditHistoryEntity;

@Component("auditHistoryEsDao")
public class AuditHistoryEsDao {

	/** The Logger */
	private static LogHelper LOGGER = LogHelper.getInstance(AuditHistoryEsDao.class.getName());
	
	/** The Object Mapper */
	private ObjectMapper mapper = new ObjectMapper();

	/** The ElasticSearchUtil */
	private ElasticSearchUtil es = new ElasticSearchUtil();

	/** The SearchProcessor */
	private SearchProcessor processor = new SearchProcessor();
	
	/** The Settings Map for ElasticSeacrh */
	private static String settings = "{ \"settings\": {\"index\": {\"index\": \""+ AuditHistoryConstants.AUDIT_HISTORY_INDEX + "\",\"type\": \""+ AuditHistoryConstants.AUDIT_HISTORY_INDEX_TYPE+ "\",\"analysis\": {\"analyzer\": {\"ah_index_analyzer\": {\"type\": \"custom\",\"tokenizer\": \"standard\",\"filter\": [\"lowercase\",\"mynGram\"]},\"ah_search_analyzer\": {\"type\": \"custom\",\"tokenizer\": \"standard\",\"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",             \"symbol\"           ]         }       }     }   } }}";

	/** The Mappings for ElasticSearch */
	private static String mappings = "{ \"" + AuditHistoryConstants.AUDIT_HISTORY_INDEX_TYPE
			+ "\" : {\"dynamic_templates\": [{\"longs\": {\"match_mapping_type\": \"long\",\"mapping\": {\"type\": \"long\",fields: {\"raw\": {\"type\": \"long\"}}}}},{\"booleans\": {\"match_mapping_type\": \"boolean\",\"mapping\": {\"type\": \"boolean\",fields: {\"raw\": {\"type\": \"boolean\"}}}}},{\"doubles\": {\"match_mapping_type\": \"double\",\"mapping\": {\"type\": \"double\",fields: {              \"raw\": {                \"type\": \"double\"              }            }          }        }      },	  {        \"dates\": {          \"match_mapping_type\": \"date\",          \"mapping\": {            \"type\": \"date\",            fields: {              \"raw\": {                \"type\": \"date\"              }            }          }        }      },      {        \"strings\": {          \"match_mapping_type\": \"string\",          \"mapping\": {            \"type\": \"string\",            \"copy_to\": \"all_fields\",            \"analyzer\": \"ah_index_analyzer\",            \"search_analyzer\": \"ah_search_analyzer\",            fields: {              \"raw\": {                \"type\": \"string\",                \"analyzer\": \"keylower\"              }            }          }        }      }    ],    \"properties\": {      \"all_fields\": {        \"type\": \"string\",        \"analyzer\": \"ah_index_analyzer\",        \"search_analyzer\": \"ah_search_analyzer\",        fields: {          \"raw\": {            \"type\": \"string\",            \"analyzer\": \"keylower\"          }        }      }    }  }}";

	
	public void save(Map<String, Object> entity_map) throws IOException {
			createIndex();
			addDocument(entity_map);
	}

	public Map<String, Object> search(SearchDTO search) {
		Map<String,Object>  result= new HashMap<String,Object>();
			try {
				result = processor.processSearch(search, true);
				System.out.println(result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		return result;
	}
	
	public void createIndex() throws IOException {
			es.addIndex(AuditHistoryConstants.AUDIT_HISTORY_INDEX, AuditHistoryConstants.AUDIT_HISTORY_INDEX_TYPE,
					settings, mappings);
	}

	public void addDocument(Map<String, Object> request) throws IOException {
		String document = null;
		if(!request.isEmpty()){
			document = mapper.writeValueAsString(request);
		}
		if(StringUtils.isNotBlank(document)){
			es.addDocument(AuditHistoryConstants.AUDIT_HISTORY_INDEX, AuditHistoryConstants.AUDIT_HISTORY_INDEX_TYPE, document);
		}
	}
}
