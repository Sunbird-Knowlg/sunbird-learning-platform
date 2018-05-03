package org.ekstep.sync.tool.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.ekstep.common.Platform;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.springframework.stereotype.Component;

@Component
public class ElasticSearchConnector {

	private ElasticSearchUtil esUtil = new ElasticSearchUtil();
	private static String indexName;
	private static String documentType;

	@PostConstruct
	private void init() throws Exception {
		indexName = Platform.config.hasPath("search.index.name") ? Platform.config.getString("search.index.name")
				: CompositeSearchConstants.COMPOSITE_SEARCH_INDEX;
		documentType = Platform.config.hasPath("search.document.type") ? Platform.config.getString("search.index.name")
				: CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE;
		createIndexIfNotExist();
	}

	@PreDestroy
	public void shutdown() {
		if (null != esUtil)
			esUtil.finalize();
	}
	
	public void createIndexIfNotExist() throws IOException {
		String settings ="{\"analysis\":{\"analyzer\":{\"cs_index_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"standard\",\"filter\":[\"lowercase\",\"mynGram\"]},\"cs_search_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"standard\",\"filter\":[\"standard\",\"lowercase\"]},\"keylower\":{\"tokenizer\":\"keyword\",\"filter\":\"lowercase\"}},\"filter\":{\"mynGram\":{\"type\":\"nGram\",\"min_gram\":1,\"max_gram\":20,\"token_chars\":[\"letter\",\"digit\",\"whitespace\",\"punctuation\",\"symbol\"]}}}}"; 
		if (Platform.config.hasPath("search.settings"))
			settings = Platform.config.getString("search.settings");
		String mappings = "{\"dynamic_templates\":[{\"nested\":{\"match_mapping_type\":\"object\",\"mapping\":{\"type\":\"nested\",\"fields\":{\"type\":\"nested\"}}}},{\"longs\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}}}},{\"booleans\":{\"match_mapping_type\":\"boolean\",\"mapping\":{\"type\":\"boolean\",\"fields\":{\"raw\":{\"type\":\"boolean\"}}}}},{\"doubles\":{\"match_mapping_type\":\"double\",\"mapping\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}}}},{\"dates\":{\"match_mapping_type\":\"date\",\"mapping\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}}}},{\"strings\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"text\",\"copy_to\":\"all_fields\",\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"fielddata\":true,\"analyzer\":\"keylower\"}}}}}],\"properties\":{\"fw_hierarchy\":{\"type\":\"text\",\"index\":false},\"screenshots\":{\"type\":\"text\",\"index\":false},\"body\":{\"type\":\"text\",\"index\":false},\"appIcon\":{\"type\":\"text\",\"index\":false},\"all_fields\":{\"type\":\"text\",\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"fielddata\":true,\"analyzer\":\"keylower\"}}}}}";
		if (Platform.config.hasPath("search.mappings"))
			mappings = Platform.config.getString("search.mappings");
		esUtil.addIndex(indexName, documentType, settings, mappings);
	}

	public void bulkImport(Map<String, Object> messages) throws Exception {
		esUtil.bulkIndexWithIndexId(indexName, documentType, messages);
	}
	
	public void bulkImportAutoID(List<Map<String, Object>> messages) throws Exception {
		esUtil.bulkIndexWithAutoGenerateIndexId(indexName, documentType, messages);
	}
}
