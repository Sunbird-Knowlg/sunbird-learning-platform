package org.ekstep.sync.tool.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.ekstep.common.Platform;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.jasypt.hibernate4.type.EncryptedLongAsStringType;
import org.springframework.stereotype.Component;

@Component
public class ElasticSearchConnector {

	private static String indexName;
	private static String documentType;

	@PostConstruct
	private void init() throws Exception {
		indexName = Platform.config.hasPath("search.index.name") ? Platform.config.getString("search.index.name")
				: CompositeSearchConstants.COMPOSITE_SEARCH_INDEX;
		documentType = "_doc";
		ElasticSearchUtil.initialiseESClient(indexName, Platform.config.getString("search.es_conn_info"));
		createIndexIfNotExist();
	}

	@PreDestroy
	public void shutdown() {
	}
	
	public void createIndexIfNotExist() throws IOException {
		String settings = "{\"max_ngram_diff\":\"29\",\"mapping\":{\"total_fields\":{\"limit\":\"1600\"},\"single_type\":true},\"analysis\":{\"filter\":{\"mynGram\":{\"token_chars\":[\"letter\",\"digit\",\"whitespace\",\"punctuation\",\"symbol\"],\"min_gram\":\"1\",\"type\":\"nGram\",\"max_gram\":\"30\"},\"synonym_filter\":{\"type\":\"synonym\",\"synonyms_path\":\"analysis/compositesearch_synonym.txt\"}},\"analyzer\":{\"cs_index_analyzer\":{\"filter\":[\"lowercase\",\"mynGram\"],\"type\":\"custom\",\"tokenizer\":\"standard\"},\"keylower\":{\"filter\":\"lowercase\",\"tokenizer\":\"keyword\"},\"cs_search_analyzer\":{\"filter\":[\"standard\",\"lowercase\"],\"type\":\"custom\",\"tokenizer\":\"standard\"},\"cs_search_syn_analyzer\":{\"filter\":[\"standard\",\"lowercase\",\"synonym_filter\"],\"type\":\"custom\",\"tokenizer\":\"standard\"}}}}";
		if (Platform.config.hasPath("search.settings"))
			settings = Platform.config.getString("search.settings");
		String mappings = "{\"dynamic_templates\":[{\"nested\":{\"match_mapping_type\":\"object\",\"mapping\":{\"type\":\"nested\",\"fields\":{\"type\":\"nested\"}}}},{\"longs\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}}}},{\"booleans\":{\"match_mapping_type\":\"boolean\",\"mapping\":{\"type\":\"boolean\",\"fields\":{\"raw\":{\"type\":\"boolean\"}}}}},{\"doubles\":{\"match_mapping_type\":\"double\",\"mapping\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}}}},{\"dates\":{\"match_mapping_type\":\"date\",\"mapping\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}}}},{\"strings\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"text\",\"copy_to\":\"all_fields\",\"fields\":{\"raw\":{\"type\":\"text\",\"fielddata\":true,\"analyzer\":\"keylower\"},\"synonym\":{\"type\":\"text\",\"fielddata\":true,\"analyzer\":\"cs_search_syn_analyzer\"}},\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"}}}],\"properties\":{\"fw_hierarchy\":{\"type\":\"text\",\"index\":false},\"screenshots\":{\"type\":\"text\",\"index\":false},\"body\":{\"type\":\"text\",\"index\":false},\"appIcon\":{\"type\":\"text\",\"index\":false},\"all_fields\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"fielddata\":true,\"analyzer\":\"keylower\"},\"synonym\":{\"type\":\"text\",\"fielddata\":true,\"analyzer\":\"cs_search_syn_analyzer\"}},\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"}}}";
		if (Platform.config.hasPath("search.mappings"))
			mappings = Platform.config.getString("search.mappings");
		ElasticSearchUtil.addIndex(indexName, documentType, settings, mappings);
	}

	public void bulkImport(Map<String, Object> messages) throws Exception {
		ElasticSearchUtil.bulkIndexWithIndexId(indexName, documentType, messages);
	}
	
	public void bulkImportAutoID(List<Map<String, Object>> messages) throws Exception {
		ElasticSearchUtil.bulkIndexWithAutoGenerateIndexId(indexName, documentType, messages);
	}
}
