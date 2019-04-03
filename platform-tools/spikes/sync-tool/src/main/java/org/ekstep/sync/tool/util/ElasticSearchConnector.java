package org.ekstep.sync.tool.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.google.gson.Gson;
import org.ekstep.common.Platform;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.springframework.stereotype.Component;

@Component
public class ElasticSearchConnector {

	private static String indexName;
	private static String documentType;
	private static Gson gson = new Gson();

	@PostConstruct
	private void init() throws Exception {
		indexName = Platform.config.hasPath("search.index.name") ? Platform.config.getString("search.index.name")
				: CompositeSearchConstants.COMPOSITE_SEARCH_INDEX;
		documentType = Platform.config.hasPath("search.document.type") ? Platform.config.getString("search.document.type")
				: CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE;
		ElasticSearchUtil.initialiseESClient(indexName, Platform.config.getString("search.es_conn_info"));
		createIndexIfNotExist();
	}

	@PreDestroy
	public void shutdown() {
	}
	
	public void createIndexIfNotExist() throws IOException {
		ElasticSearchUtil.addIndex(indexName, documentType, getESIndexConfig("settings"), getESIndexConfig("mappings"));
	}

	public void bulkImport(Map<String, Object> messages) throws Exception {
		ElasticSearchUtil.bulkIndexWithIndexId(indexName, documentType, messages);
	}
	
	public void bulkImportAutoID(List<Map<String, Object>> messages) throws Exception {
		ElasticSearchUtil.bulkIndexWithAutoGenerateIndexId(indexName, documentType, messages);
	}

	/**
	 *
	 * @param fileName
	 * @return
	 */
	private InputStream getResourceFile(String fileName) {
        return getClass().getClassLoader().getResourceAsStream(fileName);
	}

	/**
	 *
	 * @param propertyName
	 * @return
	 */
	private String getESIndexConfig(String propertyName) {
		try (InputStreamReader reader = new InputStreamReader(getResourceFile(propertyName + ".json"))) {
			Object obj = gson.fromJson(reader, Object.class);
			return gson.toJson(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
