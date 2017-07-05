package org.ekstep.language.util;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Bulk;
import io.searchbox.core.Bulk.Builder;
import io.searchbox.core.Delete;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchResult.Hit;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.DeleteIndex;
import io.searchbox.indices.IndicesExists;
import io.searchbox.indices.mapping.PutMapping;
import io.searchbox.indices.settings.GetSettings;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.gson.internal.LinkedTreeMap;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;

/**
 * The Class ElasticSearchUtil acts as a tool and a wrapper to work with an
 * ElasticSearch database. Supported operations include everything from creating
 * a new index to performing complex searches. This tool uses the JEST client to
 * interface with ES over HTTP
 * 
 * @author Amarnath
 */
public class ElasticSearchUtil {

	/** The client. */
	private JestClient client;

	/** The host name. */
	private String hostName;

	/** The port. */
	private String port;

	/** The default result limit. */
	private int defaultResultLimit = 10000;

	/** The batch size. */
	private int BATCH_SIZE = 1000;

	/** The connection timeout. */
	private int CONNECTION_TIMEOUT = 30;

	/** The result limit. */
	private int resultLimit = defaultResultLimit;

	/** The logger. */
	private static ILogger LOGGER = new PlatformLogger(ElasticSearchUtil.class.getName());

	/**
	 * Instantiates a new elastic search util by connecting to the ES cluster or
	 * server based on the host name and port properties. Sets the default
	 * result limit across the util
	 *
	 * @param resultSize
	 *            the result size
	 * @throws UnknownHostException
	 *             the unknown host exception
	 */
	public ElasticSearchUtil(int resultSize) throws UnknownHostException {
		super();
		initialize();
		if (resultSize < defaultResultLimit) {
			this.resultLimit = resultSize;
		}
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig.Builder(hostName + ":" + port).multiThreaded(true)
				.connTimeout(CONNECTION_TIMEOUT).build());
		client = factory.getObject();

	}

	/**
	 * Instantiates a new elastic search util by connecting to the ES cluster or
	 * server based on the host name and port properties.
	 *
	 * @throws UnknownHostException
	 *             the unknown host exception
	 */
	public ElasticSearchUtil() throws UnknownHostException {
		super();
		initialize();
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig.Builder(hostName + ":" + port).multiThreaded(true)
				.connTimeout(CONNECTION_TIMEOUT).build());
		client = factory.getObject();
	}

	/**
	 * Initialize the util.
	 */
	public void initialize() {
		hostName = PropertiesUtil.getProperty("elastic-search-host");
		port = PropertiesUtil.getProperty("elastic-search-port");
		if (PropertiesUtil.getProperty("bulk-load-batch-size") != null) {
			BATCH_SIZE = Integer.parseInt(PropertiesUtil.getProperty("bulk-load-batch-size"));
		}
		if (PropertiesUtil.getProperty("connection-timeout") != null) {
			CONNECTION_TIMEOUT = Integer.parseInt(PropertiesUtil.getProperty("connection-timeout"));
		}
	}

	/**
	 * Creates the JEST client.
	 *
	 * @return the jest client
	 */
	@SuppressWarnings("unused")
	private JestClient createClient() {
		return client;
	}

	/**
	 * Creates a document in ES using the id provided.
	 *
	 * @param indexName
	 *            the index name
	 * @param documentType
	 *            the document type
	 * @param documentId
	 *            the document id
	 * @param document
	 *            the document
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void addDocumentWithId(String indexName, String documentType, String documentId, String document)
			throws IOException {
		Index index = new Index.Builder(document).index(indexName).type(documentType).id(documentId).build();
		client.execute(index);
	}

	/**
	 * Creates an index in ES using the settings and mappings provided.
	 *
	 * @param indexName
	 *            the index name
	 * @param documentType
	 *            the document type
	 * @param settings
	 *            the settings
	 * @param mappings
	 *            the mappings
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void addIndex(String indexName, String documentType, String settings, String mappings) throws IOException {
		if (!isIndexExists(indexName)) {
			CreateIndex createIndex = new CreateIndex.Builder(indexName).settings(settings).build();
			client.execute(createIndex);

			GetSettings getSettings = new GetSettings.Builder().addIndex(indexName).build();
			client.execute(getSettings);

			PutMapping putMapping = new PutMapping.Builder(indexName, documentType, mappings).build();
			client.execute(putMapping);
		}
	}

	/**
	 * Creates a document in ES and auto-generates an id.
	 *
	 * @param indexName
	 *            the index name
	 * @param documentType
	 *            the document type
	 * @param document
	 *            the document
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void addDocument(String indexName, String documentType, String document) throws IOException {
		Index index = new Index.Builder(document).index(indexName).type(documentType).build();
		client.execute(index);
	}

	/**
	 * Checks if an index exists.
	 *
	 * @param indexName
	 *            the index name
	 * @return true, if is index exists
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public boolean isIndexExists(String indexName) throws IOException {
		return client.execute(new IndicesExists.Builder(indexName).build()).isSucceeded();
	}

	/**
	 * Deletes document using the document Id.
	 *
	 * @param indexName
	 *            the index name
	 * @param documentType
	 *            the document type
	 * @param documentId
	 *            the document id
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void deleteDocument(String indexName, String documentType, String documentId) throws IOException {
		client.execute(new Delete.Builder(documentId).index(indexName).type(documentType).build());
	}

	/**
	 * Deletes index using the index name.
	 *
	 * @param indexName
	 *            the index name
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void deleteIndex(String indexName) throws IOException {
		client.execute(new DeleteIndex.Builder(indexName).build());
	}

	/**
	 * Bulk load index documents and uses the index id provided.
	 *
	 * @param indexName
	 *            the index name
	 * @param documentType
	 *            the document type
	 * @param jsonObjects
	 *            the index documents
	 * @throws Exception
	 *             the exception
	 */
	public void bulkIndexWithIndexId(String indexName, String documentType, Map<String, String> jsonObjects)
			throws Exception {
		if (isIndexExists(indexName)) {
			if (!jsonObjects.isEmpty()) {
				int count = 0;
				Builder bulkBuilder = new Bulk.Builder().defaultIndex(indexName).defaultType(documentType);
				for (Map.Entry<String, String> entry : jsonObjects.entrySet()) {
					count++;
					bulkBuilder.addAction(new Index.Builder(entry.getValue()).id(entry.getKey()).build());
					if (count % BATCH_SIZE == 0 || (count % BATCH_SIZE < BATCH_SIZE && count == jsonObjects.size())) {
						Bulk bulk = bulkBuilder.build();
						client.execute(bulk);
						System.out.println("Bulk indexed " + BATCH_SIZE + " documents");
						bulkBuilder = new Bulk.Builder().defaultIndex(indexName).defaultType(documentType);
					}
				}
			}
		} else {
			throw new Exception("Index does not exist");
		}
	}

	/**
	 * Bulk load index documents and auto generates the index id.
	 *
	 * @param indexName
	 *            the index name
	 * @param documentType
	 *            the document type
	 * @param jsonObjects
	 *            the json objects
	 * @throws Exception
	 *             the exception
	 */
	public void bulkIndexWithAutoGenerateIndexId(String indexName, String documentType, List<String> jsonObjects)
			throws Exception {
		if (isIndexExists(indexName)) {
			if (!jsonObjects.isEmpty()) {
				int count = 0;
				Builder bulkBuilder = new Bulk.Builder().defaultIndex(indexName).defaultType(documentType);
				for (String jsonString : jsonObjects) {
					count++;
					bulkBuilder.addAction(new Index.Builder(jsonString).build());
					if (count % BATCH_SIZE == 0 || (count % BATCH_SIZE < BATCH_SIZE && count == jsonObjects.size())) {
						Bulk bulk = bulkBuilder.build();
						client.execute(bulk);
						System.out.println("Bulk indexed " + BATCH_SIZE + " documents");
						bulkBuilder = new Bulk.Builder().defaultIndex(indexName).defaultType(documentType);
					}
				}
			}
		} else {
			throw new Exception("Index does not exist");
		}
	}

	/**
	 * Performs a Text search on ES.
	 *
	 * @param objectClass
	 *            the object class
	 * @param matchCriterias
	 *            the filters
	 * @param IndexName
	 *            the index name
	 * @param IndexType
	 *            the index type
	 * @return the list
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings({ "rawtypes" })
	public List<Object> textSearch(Class objectClass, Map<String, Object> matchCriterias, String IndexName,
			String IndexType) throws IOException {
		SearchResult result = search(matchCriterias, null, IndexName, IndexType, null, false);
		return getDocumentsFromSearchResult(result, objectClass);
	}

	/**
	 * Gets the documents from search result by casting the results to the
	 * objectClass provided.
	 *
	 * @param result
	 *            the result
	 * @param objectClass
	 *            the object class of the resulting document
	 * @return the documents from search result
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Object> getDocumentsFromSearchResult(SearchResult result, Class objectClass) {
		List<Hit<Object, Void>> hits = result.getHits(objectClass);
		return getDocumentsFromHits(hits);
	}

	/**
	 * Gets the documents from hits as Objects.
	 *
	 * @param hits
	 *            the hits
	 * @return the documents from hits
	 */
	@SuppressWarnings("rawtypes")
	public List<Object> getDocumentsFromHits(List<Hit<Object, Void>> hits) {
		List<Object> documents = new ArrayList<Object>();
		for (Hit hit : hits) {
			documents.add(hit.source);
		}
		return documents;
	}

	/**
	 * Performs a Wild card search and returns list of objects casted to the
	 * objectClass.
	 *
	 * @param objectClass
	 *            the object class
	 * @param textKeyWord
	 *            the field to search for
	 * @param wordWildCard
	 *            the word wild card text
	 * @param indexName
	 *            the index name
	 * @param indexType
	 *            the index type
	 * @return the list
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings({ "rawtypes" })
	public List<Object> wildCardSearch(Class objectClass, String textKeyWord, String wordWildCard, String indexName,
			String indexType) throws IOException {
		SearchResult result = wildCardSearch(textKeyWord, wordWildCard, indexName, indexType);
		return getDocumentsFromSearchResult(result, objectClass);
	}

	/**
	 * Performs a Wild card search and returns the SearchResult object.
	 *
	 * @param textKeyWord
	 *            the field to search for
	 * @param wordWildCard
	 *            the word wild card text
	 * @param indexName
	 *            the index name
	 * @param indexType
	 *            the index type
	 * @return the search result
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public SearchResult wildCardSearch(String textKeyWord, String wordWildCard, String indexName, String indexType)
			throws IOException {
		String query = buildJsonForWildCardQuery(textKeyWord, wordWildCard);
		return search(indexName, indexType, query);
	}

	/**
	 * Performs a Text filters search and returns list of objects casted to the
	 * objectClass.
	 *
	 * @param objectClass
	 *            the object class
	 * @param searchCriteria
	 *            the search criteria
	 * @param textFiltersMap
	 *            the text filters map
	 * @param indexName
	 *            the index name
	 * @param indexType
	 *            the index type
	 * @return the list
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings({ "rawtypes" })
	public List<Object> textFiltersSearch(Class objectClass, Map<String, Object> searchCriteria,
			Map<String, Object> textFiltersMap, String indexName, String indexType) throws IOException {
		SearchResult result = search(searchCriteria, textFiltersMap, indexName, indexType, null, false);
		return getDocumentsFromSearchResult(result, objectClass);
	}

	/**
	 * Performs a Text filters search and groups together the results.
	 *
	 * @param objectClass
	 *            the object class
	 * @param searchCriteria
	 *            the search criteria
	 * @param textFiltersMap
	 *            the text filters map
	 * @param groupByList
	 *            the group by list
	 * @param indexName
	 *            the index name
	 * @param indexType
	 *            the index type
	 * @return the map
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> textFiltersGroupBySearch(Class objectClass, Map<String, Object> searchCriteria,
			Map<String, Object> textFiltersMap, List<Map<String, Object>> groupByList, String indexName,
			String indexType) throws IOException {
		SearchResult result = search(searchCriteria, textFiltersMap, indexName, indexType, groupByList, false);
		List<Object> documents = getDocumentsFromSearchResult(result, objectClass);
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("objects", documents);

		if (result.getAggregations() != null) {
			LinkedTreeMap<String, Object> aggregations = (LinkedTreeMap<String, Object>) result
					.getValue("aggregations");
			response.put("aggregations", getCountFromAggregation(aggregations, groupByList));
		}
		return response;
	}

	// TODO: Remove, duplicate method
	/**
	 * Performs a Text filters search and returns list of objects casted to the
	 * objectClass.
	 *
	 * @param objectClass
	 *            the object class
	 * @param matchCriterias
	 *            the match criterias
	 * @param textFiltersMap
	 *            the text filters map
	 * @param IndexName
	 *            the index name
	 * @param IndexType
	 *            the index type
	 * @return the list
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings({ "rawtypes" })
	public List<Object> textSearch(Class objectClass, Map<String, Object> matchCriterias,
			Map<String, Object> textFiltersMap, String IndexName, String IndexType) throws IOException {
		SearchResult result = search(matchCriterias, textFiltersMap, IndexName, IndexType, null, false);
		return getDocumentsFromSearchResult(result, objectClass);
	}

	// TODO: Remove, duplicate method
	/**
	 * Performs a Text filters search and groups together the results.
	 *
	 * @param objectClass
	 *            the object class
	 * @param matchCriterias
	 *            the match criterias
	 * @param textFiltersMap
	 *            the text filters map
	 * @param IndexName
	 *            the index name
	 * @param IndexType
	 *            the index type
	 * @param groupByList
	 *            the group by list
	 * @return the list
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings({ "rawtypes" })
	public List<Object> textSearch(Class objectClass, Map<String, Object> matchCriterias,
			Map<String, Object> textFiltersMap, String IndexName, String IndexType,
			List<Map<String, Object>> groupByList) throws IOException {
		SearchResult result = search(matchCriterias, textFiltersMap, IndexName, IndexType, groupByList, false);
		return getDocumentsFromSearchResult(result, objectClass);
	}

	/**
	 * Performs a Search on ES with the given filters, text searches and group
	 * by criteria.
	 *
	 * @param matchCriterias
	 *            the match criteria
	 * @param textFiltersMap
	 *            the text filters map
	 * @param IndexName
	 *            the index name
	 * @param IndexType
	 *            the index type
	 * @param groupBy
	 *            the group by
	 * @param isDistinct
	 *            the is distinct
	 * @return the search result
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public SearchResult search(Map<String, Object> matchCriterias, Map<String, Object> textFiltersMap, String IndexName,
			String IndexType, List<Map<String, Object>> groupBy, boolean isDistinct) throws IOException {
		String query = buildJsonForQuery(matchCriterias, textFiltersMap, groupBy, isDistinct);
		return search(IndexName, IndexType, query);
	}

	/**
	 * Performs a Search on ES using the JSON query generated and returns the
	 * Search Result.
	 *
	 * @param IndexName
	 *            the index name
	 * @param IndexType
	 *            the index type
	 * @param query
	 *            the query
	 * @return the search result
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public SearchResult search(String IndexName, String IndexType, String query) throws IOException {
		Search search = new Search.Builder(query).addIndex(IndexName).addType(IndexType)
				.setParameter("size", resultLimit).build();
		long startTime = System.currentTimeMillis();
		SearchResult result = client.execute(search);
		if (result.getErrorMessage() != null) {
			throw new IOException(result.getErrorMessage());
		}
		long endTime = System.currentTimeMillis();
		long diff = endTime - startTime;
		LOGGER.log("Time taken for search: " , diff, "INFO");
		return result;
	}

	/**
	 * Gets the grouped by count from aggregated results.
	 *
	 * @param aggregations
	 *            the aggregated results
	 * @param groupByList
	 *            the group by list
	 * @return the count from aggregation
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Object> getCountFromAggregation(LinkedTreeMap<String, Object> aggregations,
			List<Map<String, Object>> groupByList) {
		Map<String, Object> countMap = new HashMap<String, Object>();
		if (aggregations != null) {

			// Iterate the results and form the response based on the request
			// group by criteria
			for (Map<String, Object> aggregationsMap : groupByList) {
				Map<String, Object> parentCountMap = new HashMap<String, Object>();
				String groupByParent = (String) aggregationsMap.get("groupByParent");
				Map aggKeyMap = (Map) aggregations.get(groupByParent);
				List<Map<String, Double>> aggKeyList = (List<Map<String, Double>>) aggKeyMap.get("buckets");
				List<Map<String, Object>> parentGroupList = new ArrayList<Map<String, Object>>();
				for (Map aggKeyListMap : aggKeyList) {
					Map<String, Object> parentCountObject = new HashMap<String, Object>();
					parentCountObject.put("count", ((Double) aggKeyListMap.get("doc_count")).longValue());
					List<String> groupByChildList = (List<String>) aggregationsMap.get("groupByChildList");
					if (groupByChildList != null && !groupByChildList.isEmpty()) {
						Map<String, Object> groupByChildMap = new HashMap<String, Object>();
						for (String groupByChild : groupByChildList) {
							List<Map<String, Long>> childGroupsList = new ArrayList<Map<String, Long>>();
							Map aggChildKeyMap = (Map) aggKeyListMap.get(groupByChild);
							List<Map<String, Double>> aggChildKeyList = (List<Map<String, Double>>) aggChildKeyMap
									.get("buckets");
							Map<String, Long> childCountMap = new HashMap<String, Long>();
							for (Map aggChildKeyListMap : aggChildKeyList) {
								childCountMap.put((String) aggChildKeyListMap.get("key"),
										((Double) aggChildKeyListMap.get("doc_count")).longValue());
								childGroupsList.add(childCountMap);
								groupByChildMap.put(groupByChild, childCountMap);
							}
						}
						parentCountObject.putAll(groupByChildMap);
					}
					parentCountMap.put((String) aggKeyListMap.get("key"), parentCountObject);
					parentGroupList.add(parentCountMap);
				}
				countMap.put(groupByParent, parentCountMap);
			}
		}
		return countMap;
	}

	/**
	 * Gets the search result count.
	 *
	 * @param objectClass
	 *            the object class
	 * @param matchCriterias
	 *            the match criteria
	 * @param IndexName
	 *            the index name
	 * @param IndexType
	 *            the index type
	 * @param groupByList
	 *            the group by list
	 * @return the count of search
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Object> getCountOfSearch(Class objectClass, Map<String, Object> matchCriterias, String IndexName,
			String IndexType, List<Map<String, Object>> groupByList) throws IOException {
		SearchResult result = search(matchCriterias, null, IndexName, IndexType, groupByList, false);
		LinkedTreeMap<String, Object> aggregations = (LinkedTreeMap<String, Object>) result.getValue("aggregations");
		return getCountFromAggregation(aggregations, groupByList);
	}

	/**
	 * Gets the distinct search result count.
	 *
	 * @param matchCriterias
	 *            the match criterias
	 * @param IndexName
	 *            the index name
	 * @param IndexType
	 *            the index type
	 * @param groupByList
	 *            the group by list
	 * @return the distinct count of search
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Object> getDistinctCountOfSearch(Map<String, Object> matchCriterias, String IndexName,
			String IndexType, List<Map<String, Object>> groupByList) throws IOException {
		Map<String, Object> countMap = new HashMap<String, Object>();
		SearchResult result = search(matchCriterias, null, IndexName, IndexType, groupByList, true);
		LinkedTreeMap<String, Object> aggregations = (LinkedTreeMap<String, Object>) result.getValue("aggregations");
		if (aggregations != null) {
			// Iterate the results and form the response based on the request
			// group by criteria
			for (Map<String, Object> aggregationsMap : groupByList) {
				Map<String, Object> parentCountMap = new HashMap<String, Object>();
				String groupByParent = (String) aggregationsMap.get("groupBy");
				Map aggKeyMap = (Map) aggregations.get(groupByParent);
				List<Map<String, Double>> aggKeyList = (List<Map<String, Double>>) aggKeyMap.get("buckets");
				for (Map aggKeyListMap : aggKeyList) {
					String distinctKey = (String) aggregationsMap.get("distinctKey");
					Map aggChildKeyMap = (Map) aggKeyListMap.get("distinct_" + distinctKey + "s");
					Long count = ((Double) aggChildKeyMap.get("value")).longValue();
					String keyAsString = (String) aggKeyListMap.get("key_as_string");
					if (keyAsString != null) {
						parentCountMap.put(keyAsString, count);
					} else {
						parentCountMap.put((String) aggKeyListMap.get("key"), (Long) count);
					}
				}
				countMap.put(groupByParent, parentCountMap);
			}
		}
		return countMap;
	}

	/**
	 * Builds the JSON query that can be interpreted by ES from the filters,
	 * group by and text filters.
	 *
	 * @param matchCriterias
	 *            the match criterias
	 * @param textFiltersMap
	 *            the text filters map
	 * @param groupByList
	 *            the group by list
	 * @param isDistinct
	 *            the is distinct
	 * @return the string
	 * @throws JsonGenerationException
	 *             the json generation exception
	 * @throws JsonMappingException
	 *             the json mapping exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unchecked")
	public String buildJsonForQuery(Map<String, Object> matchCriterias, Map<String, Object> textFiltersMap,
			List<Map<String, Object>> groupByList, boolean isDistinct)
					throws JsonGenerationException, JsonMappingException, IOException {

		JSONBuilder builder = new JSONStringer();
		builder.object().key("query").object().key("filtered").object();

		// forms at least one match query for all partial matches
		if (matchCriterias != null) {
			builder.key("query").object().key("bool").object().key("should").array();
			for (Map.Entry<String, Object> entry : matchCriterias.entrySet()) {
				if (entry.getValue() instanceof List) {
					for (String matchText : (ArrayList<String>) entry.getValue()) {
						builder.object().key("match").object().key(entry.getKey()).value(matchText).endObject()
								.endObject();
					}
				}
			}
			builder.endArray().endObject().endObject();
		}

		// forms at least one match query for all exact matches
		if (textFiltersMap != null && !textFiltersMap.isEmpty()) {
			builder.key("filter").object().key("bool").object().key("must").array();
			for (Map.Entry<String, Object> entry : textFiltersMap.entrySet()) {
				builder.object().key("terms").object().key(entry.getKey()).array();
				ArrayList<String> termValues = (ArrayList<String>) entry.getValue();
				for (String termValue : termValues) {
					builder.value(termValue);
				}
				builder.endArray().endObject().endObject();
			}
			builder.endArray().endObject().endObject();
		}

		builder.endObject().endObject();

		// adds the group by criteria
		if (groupByList != null && !groupByList.isEmpty()) {

			// if not a distinct count aggregation
			if (!isDistinct) {
				for (Map<String, Object> groupByMap : groupByList) {
					String groupByParent = (String) groupByMap.get("groupByParent");
					List<String> groupByChildList = (List<String>) groupByMap.get("groupByChildList");
					builder.key("aggs").object().key(groupByParent).object().key("terms").object().key("field")
							.value(groupByParent).key("size").value(resultLimit).endObject();
					if (groupByChildList != null && !groupByChildList.isEmpty()) {
						builder.key("aggs").object();
						for (String childGroupBy : groupByChildList) {
							builder.key(childGroupBy).object().key("terms").object().key("field").value(childGroupBy)
									.key("size").value(resultLimit).endObject().endObject();
						}
						builder.endObject();
					}
					builder.endObject().endObject();
				}
			}
			// if it is a distinct count aggregation
			else {
				builder.key("aggs").object();
				for (Map<String, Object> groupByMap : groupByList) {
					String groupBy = (String) groupByMap.get("groupBy");
					String distinctKey = (String) groupByMap.get("distinctKey");
					builder.key(groupBy).object().key("terms").object().key("field").value(groupBy).endObject();
					builder.key("aggs").object();
					builder.key("distinct_" + distinctKey + "s").object().key("cardinality").object().key("field")
							.value(distinctKey).endObject().endObject();
					builder.endObject().endObject();
				}
				builder.endObject();
			}
		}

		builder.endObject();
		return builder.toString();
	}

	/**
	 * Builds the json query for a wild card search.
	 *
	 * @param textKeyWord
	 *            the text key word
	 * @param wordWildCard
	 *            the word wild card
	 * @return the string
	 */
	private String buildJsonForWildCardQuery(String textKeyWord, String wordWildCard) {
		JSONBuilder builder = new JSONStringer();
		builder.object().key("query").object().key("wildcard").object().key(textKeyWord).value(wordWildCard).endObject()
				.endObject().endObject();

		return builder.toString();
	}
}
