package org.ekstep.language.mgr.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.common.util.UnzipUtility;
import org.ekstep.language.common.LanguageMap;
import org.ekstep.language.common.LanguageSourceTypeMap;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.enums.Enums;
import org.ekstep.language.enums.Enums.ObjectType;
import org.ekstep.language.mgr.IImportManager;
import org.ekstep.language.models.DictionaryObject;
import org.ekstep.language.models.SynsetModel;
import org.ekstep.language.models.WordModel;
import org.ekstep.language.util.BaseLanguageManager;
import org.ekstep.language.util.ControllerUtil;
import org.ekstep.language.util.WordUtil;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.enums.ImportType;
import com.ilimi.graph.importer.InputStreamValue;
import com.ilimi.graph.importer.OutputStreamValue;

/**
 * The Class ImportManagerImpl provides implementations of various import
 * operations through which words and synsets can be imported into the platform.
 * 
 * @author Amarnath, Azhar
 * 
 */
@Component
public class ImportManagerImpl extends BaseLanguageManager implements IImportManager {

	/** The Constant CSV_SEPARATOR. */
	private static final String CSV_SEPARATOR = ",";

	/** The Constant NEW_LINE. */
	private static final String NEW_LINE = "\n";

	/** The controller util. */
	private ControllerUtil controllerUtil = new ControllerUtil();

	/** The Constant tempFileLocation. */
	private static final String tempFileLocation = "/data/temp/";

	/** The mapper. */
	private ObjectMapper mapper = new ObjectMapper();

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(IImportManager.class.getName());

	/**
	 * Gets the word list.
	 *
	 * @param wordJSONArrObj
	 *            the word JSON arr obj
	 * @return the word list
	 */
	private List<String> getWordList(Object wordJSONArrObj) {
		String JSONarrStr;
		try {
			JSONarrStr = mapper.writeValueAsString(wordJSONArrObj);
			List<String> list = mapper.readValue(JSONarrStr, new TypeReference<List<String>>() {
			});
			return list;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gets the related synsets.
	 *
	 * @param wordJSONArrObj
	 *            the word JSON arr obj
	 * @return the related synsets
	 */
	private List<Map<String, Object>> getRelatedSynsets(Object wordJSONArrObj) {
		String JSONarrStr;
		try {
			JSONarrStr = mapper.writeValueAsString(wordJSONArrObj);
			List<Map<String, Object>> list = mapper.readValue(JSONarrStr,
					new TypeReference<List<Map<String, Object>>>() {
					});
			return list;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return null;
	}

	/** The Constant KEY_NAME_IDENTIFIER. */
	private static final String KEY_NAME_IDENTIFIER = "sid";

	/** The Constant KEY_NAME_GLOSS. */
	private static final String KEY_NAME_GLOSS = "gloss";

	/** The Constant KEY_NAME_GLOSS_ENG. */
	private static final String KEY_NAME_GLOSS_ENG = "gloss_eng";

	/** The Constant KEY_NAME_EXAM_STMT. */
	private static final String KEY_NAME_EXAM_STMT = "example_stmt";

	/** The Constant KEY_NAME_POS. */
	private static final String KEY_NAME_POS = "pos";

	/** The Constant KEY_NAME_TRANSLATIONS. */
	private static final String KEY_NAME_TRANSLATIONS = "translations";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.language.mgr.IImportManager#importJSON(java.lang.String,
	 * java.io.InputStream)
	 */
	@Override
	public Response importJSON(String languageId, InputStream synsetsStreamInZIPStream) {
		if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
		if (null == synsetsStreamInZIPStream)
			throw new ClientException(LanguageErrorCodes.ERR_EMPTY_INPUT_STREAM.name(), "Input Zip object is emtpy");
		String tempFileDwn = tempFileLocation + System.currentTimeMillis() + "_temp";
		UnzipUtility unzipper = new UnzipUtility();
		StringBuilder errorMessages = new StringBuilder();
		Response importResponse = OK();
		try {
			unzipper.unzip(synsetsStreamInZIPStream, tempFileDwn);
			File zipFileDirectory = new File(tempFileDwn);
			List<String> wordList = new ArrayList<String>();
			String files[] = zipFileDirectory.list();
			Map<String, String> nodeIDcache = new HashMap<>();
			List<String> synsetIds = new ArrayList<String>();
			for (String temp : files) {
				long startTimeJSON = System.currentTimeMillis();
				// construct the file structure
				File jsonFile = new File(zipFileDirectory, temp);
				FileInputStream jsonFIS = new FileInputStream(jsonFile);
				InputStreamReader isReader = new InputStreamReader(jsonFIS, "UTF8");
				String fileName = jsonFile.getName();
				String word = fileName.substring(0, fileName.indexOf(".json"));
				wordList.add(word);
				String jsonContent = IOUtils.toString(isReader);
				System.out.println("fileName=" + fileName + ",word=" + word);
				List<Map<String, Object>> jsonObj = mapper.readValue(jsonContent,
						new TypeReference<List<Map<String, Object>>>() {
						});
				DictionaryManagerImpl manager = new DictionaryManagerImpl();
				if (null != jsonObj && !jsonObj.isEmpty()) {
					createSynsets(languageId, word, synsetIds, jsonObj, nodeIDcache, errorMessages, manager);
				} else {
					createWord(languageId, word, null, manager, nodeIDcache, errorMessages);
				}
				long stopTimeJSON = System.currentTimeMillis();
				System.out.println(word + " JSON File time taken" + (stopTimeJSON - startTimeJSON));
			}
			asyncUpdate(nodeIDcache.values(), languageId);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			errorMessages.append(", ").append(ex.getMessage());
		} finally {
			File zipFileDirectory = new File(tempFileDwn);
			if (!zipFileDirectory.exists()) {
				System.out.println("Directory does not exist.");
			} else {
				try {
					delete(zipFileDirectory);
				} catch (IOException e) {
					LOGGER.error(e.getMessage(), e);
					e.printStackTrace();
				}
			}
		}
		String errorMessageString = errorMessages.toString();
		if (!errorMessageString.isEmpty()) {
			errorMessageString = errorMessageString.substring(2);
			importResponse = ERROR(LanguageErrorCodes.SYSTEM_ERROR.name(), "Internal Error", ResponseCode.SERVER_ERROR);
			importResponse.put("errorMessage", errorMessageString);
		}
		return importResponse;
	}

	/**
	 * Async update.
	 *
	 * @param wordIds
	 *            the word ids
	 * @param languageId
	 *            the language id
	 */
	private void asyncUpdate(Collection<String> wordIds, String languageId) {
		if (null != wordIds && !wordIds.isEmpty()) {
			System.out.println("Async update | Words count: " + wordIds.size());
			List<String> nodeIds = new ArrayList<String>(wordIds);
			Map<String, Object> map = new HashMap<String, Object>();
			map = new HashMap<String, Object>();
			map.put(LanguageParams.node_ids.name(), nodeIds);
			Request request = new Request();
			request.setRequest(map);
			request.setManagerName(LanguageActorNames.ENRICH_ACTOR.name());
			request.setOperation(LanguageOperations.enrichWords.name());
			request.getContext().put(LanguageParams.language_id.name(), languageId);
			makeAsyncLanguageRequest(request, LOGGER);
		}
	}

	/**
	 * Creates the synsets.
	 *
	 * @param languageId
	 *            the language id
	 * @param word
	 *            the word
	 * @param synsetIds
	 *            the synset ids
	 * @param jsonObj
	 *            the json obj
	 * @param nodeIDcache
	 *            the node I dcache
	 * @param errorMessages
	 *            the error messages
	 * @param manager
	 *            the manager
	 */
	private void createSynsets(String languageId, String word, List<String> synsetIds,
			List<Map<String, Object>> jsonObj, Map<String, String> nodeIDcache, StringBuilder errorMessages,
			DictionaryManagerImpl manager) {
		for (Map<String, Object> synsetJSON : jsonObj) {
			String wordnetId = (String) synsetJSON.get(KEY_NAME_IDENTIFIER);
			String gloss = (String) synsetJSON.get(KEY_NAME_GLOSS);
			if (StringUtils.isNotBlank(wordnetId) && StringUtils.isNotBlank(gloss)) {
				String identifier = languageId + ':' + "S:" + wordnetId;
				if (!synsetIds.contains(identifier)) {
					String synsetNodeId = createSynset(languageId, identifier, synsetJSON);
					if (StringUtils.isEmpty(synsetNodeId)) {
						errorMessages.append(", ").append("Synset create/Update failed: " + identifier);
						continue;
					} else
						synsetIds.add(synsetNodeId);
				}
				List<String> words = getWordList(synsetJSON.get("synonyms"));
				if (null == words)
					words = new ArrayList<String>();
				if (!words.contains(word))
					words.add(word);
				for (String sWord : words) {
					if (StringUtils.isNotBlank(sWord)) {
						boolean synonymAdded = createWord(languageId, sWord, identifier, manager, nodeIDcache,
								errorMessages);
						if (!synonymAdded)
							continue;
					}
				}

				// for each synset relkations in the request, create the words
				// and the relations betwen the nodes
				List<Map<String, Object>> hypernyms = getRelatedSynsets(synsetJSON.get("hypernyms"));
				createRelations(languageId, hypernyms, identifier, RelationTypes.HYPERNYM.relationName(), nodeIDcache,
						synsetIds, manager, errorMessages, true);

				List<Map<String, Object>> hyponyms = getRelatedSynsets(synsetJSON.get("hyponyms"));
				createRelations(languageId, hyponyms, identifier, RelationTypes.HYPONYM.relationName(), nodeIDcache,
						synsetIds, manager, errorMessages, false);

				List<Map<String, Object>> holonyms = getRelatedSynsets(synsetJSON.get("holonyms"));
				createRelations(languageId, holonyms, identifier, RelationTypes.HOLONYM.relationName(), nodeIDcache,
						synsetIds, manager, errorMessages, false);

				List<Map<String, Object>> meronyms = getRelatedSynsets(synsetJSON.get("meronyms"));
				createRelations(languageId, meronyms, identifier, RelationTypes.MERONYM.relationName(), nodeIDcache,
						synsetIds, manager, errorMessages, false);

				List<Map<String, Object>> actions = getRelatedSynsets(synsetJSON.get("actions"));
				createRelations(languageId, actions, identifier, RelationTypes.ACTION.relationName(), nodeIDcache,
						synsetIds, manager, errorMessages, false);

				List<Map<String, Object>> tools = getRelatedSynsets(synsetJSON.get("tools"));
				createRelations(languageId, tools, identifier, RelationTypes.TOOL.relationName(), nodeIDcache,
						synsetIds, manager, errorMessages, false);

				List<Map<String, Object>> workers = getRelatedSynsets(synsetJSON.get("workers"));
				createRelations(languageId, workers, identifier, RelationTypes.WORKER.relationName(), nodeIDcache,
						synsetIds, manager, errorMessages, false);

				List<Map<String, Object>> objects = getRelatedSynsets(synsetJSON.get("objects"));
				createRelations(languageId, objects, identifier, RelationTypes.OBJECT.relationName(), nodeIDcache,
						synsetIds, manager, errorMessages, false);

				List<Map<String, Object>> converse = getRelatedSynsets(synsetJSON.get("converse"));
				createRelations(languageId, converse, identifier, RelationTypes.CONVERSE.relationName(), nodeIDcache,
						synsetIds, manager, errorMessages, false);
			}
		}
	}

	/**
	 * Creates the relations.
	 *
	 * @param languageId
	 *            the language id
	 * @param synsets
	 *            the synsets
	 * @param synsetId
	 *            the synset id
	 * @param relationName
	 *            the relation name
	 * @param nodeIDcache
	 *            the node I dcache
	 * @param synsetIds
	 *            the synset ids
	 * @param manager
	 *            the manager
	 * @param errorMessages
	 *            the error messages
	 * @param hypernym
	 *            the hypernym
	 */
	private void createRelations(String languageId, List<Map<String, Object>> synsets, String synsetId,
			String relationName, Map<String, String> nodeIDcache, List<String> synsetIds, DictionaryManagerImpl manager,
			StringBuilder errorMessages, boolean hypernym) {
		if (null != synsets && !synsets.isEmpty()) {
			for (Map<String, Object> synset : synsets) {
				String wordnetId = (String) synset.get(KEY_NAME_IDENTIFIER);
				String gloss = (String) synset.get(KEY_NAME_GLOSS);
				if (StringUtils.isNotBlank(wordnetId) && StringUtils.isNotBlank(gloss)) {
					String identifier = languageId + ':' + "S:" + wordnetId.trim();
					if (!synsetIds.contains(identifier)) {
						String synsetNodeId = createSynset(languageId, identifier, synset);
						if (StringUtils.isEmpty(synsetNodeId)) {
							errorMessages.append(", ").append("Synset create/Update failed: " + identifier);
							continue;
						} else
							synsetIds.add(synsetNodeId);
					}
					List<String> words = getWordList(synset.get("synonyms"));
					if (null != words && !words.isEmpty()) {
						for (String sWord : words) {
							if (StringUtils.isNotBlank(sWord)) {
								boolean synonymAdded = createWord(languageId, sWord, identifier, manager, nodeIDcache,
										errorMessages);
								if (!synonymAdded)
									continue;
							}
						}
					}
					manager.addRelation(languageId, Enums.ObjectType.Synset.name(), synsetId, relationName, identifier);
					if (hypernym)
						synsetId = identifier;
				}
			}
		}
	}

	/**
	 * Creates/updates the synset object.
	 *
	 * @param languageId
	 *            the language id
	 * @param identifier
	 *            the identifier
	 * @param synset
	 *            the synset
	 * @return the string
	 */
	private String createSynset(String languageId, String identifier, Map<String, Object> synset) {
		Node synsetNode = new Node();
		synsetNode.setGraphId(languageId);
		synsetNode.setIdentifier(identifier);
		synsetNode.setNodeType(SystemNodeTypes.DATA_NODE.name());
		synsetNode.setObjectType(Enums.ObjectType.Synset.name());
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("gloss", synset.get(KEY_NAME_GLOSS));
		metadata.put("exampleSentences", Arrays.asList(synset.get(KEY_NAME_EXAM_STMT)));
		metadata.put("pos", synset.get(KEY_NAME_POS));
		if (null != synset.get(KEY_NAME_GLOSS_ENG) && StringUtils.isNotBlank(synset.get(KEY_NAME_GLOSS_ENG).toString()))
			metadata.put("glossInEnglish", synset.get(KEY_NAME_GLOSS_ENG));
		if (null != synset.get(KEY_NAME_TRANSLATIONS)
				&& StringUtils.isNotBlank(synset.get(KEY_NAME_TRANSLATIONS).toString()))
			metadata.put("translations", synset.get(KEY_NAME_TRANSLATIONS));
		synsetNode.setMetadata(metadata);
		String synsetNodeId = updateNode(synsetNode, Enums.ObjectType.Synset.name(), languageId);
		return synsetNodeId;
	}

	/**
	 * Creates/updates the word.
	 *
	 * @param languageId
	 *            the language id
	 * @param sWord
	 *            the s word
	 * @param identifier
	 *            the identifier
	 * @param manager
	 *            the manager
	 * @param nodeIDcache
	 *            the node I dcache
	 * @param errorMessages
	 *            the error messages
	 * @return true, if successful
	 */
	private boolean createWord(String languageId, String sWord, String identifier, DictionaryManagerImpl manager,
			Map<String, String> nodeIDcache, StringBuilder errorMessages) {
		String nodeId;
		WordUtil wordUtil = new WordUtil();

		// check if word is in the execution cache
		if (nodeIDcache.get(sWord) == null) {
			// check if word is in the graph
			Node node = wordUtil.searchWord(languageId, sWord);
			if (null == node)
				nodeId = wordUtil.createWord(languageId, sWord, Enums.ObjectType.Word.name());
			else
				nodeId = node.getIdentifier();
			nodeIDcache.put(sWord, nodeId);
		} else {
			nodeId = nodeIDcache.get(sWord);
		}
		if (StringUtils.isNotBlank(identifier)) {
			// create relation
			Response relationResponse = manager.addRelation(languageId, Enums.ObjectType.Synset.name(), identifier,
					RelationTypes.SYNONYM.relationName(), nodeId);
			if (checkError(relationResponse)) {
				errorMessages.append(", ")
						.append("Synset relation creation failed: " + identifier + " word id: " + nodeId);
				return false;
			}
		}
		return true;
	}

	/**
	 * Deletes the file.
	 *
	 * @param file
	 *            the file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void delete(File file) throws IOException {
		if (file.isDirectory()) {
			// directory is empty, then delete it
			if (file.list().length == 0) {
				file.delete();
			} else {
				// list all the directory contents
				String files[] = file.list();
				for (String temp : files) {
					// construct the file structure
					File fileDelete = new File(file, temp);
					// recursive delete
					delete(fileDelete);
				}
				// check the directory again, if empty then delete it
				if (file.list().length == 0) {
					file.delete();
				}
			}

		} else {
			// if file, then delete it
			file.delete();
		}
	}

	/**
	 * Updates the node object in the graph.
	 *
	 * @param node
	 *            the node
	 * @param objectType
	 *            the object type
	 * @param languageId
	 *            the language id
	 * @return the string
	 */
	public String updateNode(Node node, String objectType, String languageId) {
		node.setObjectType(objectType);
		// validate the request
		Request validateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "validateNode");
		validateReq.put(GraphDACParams.node.name(), node);
		String lstNodeId = StringUtils.EMPTY;
		Response validateRes = getResponse(validateReq, LOGGER);
		if (!checkError(validateRes)) {
			Request createReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
			createReq.put(GraphDACParams.node.name(), node);
			createReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
			Response res = getResponse(createReq, LOGGER);
			if (!checkError(res)) {
				Map<String, Object> result = res.getResult();
				if (result != null) {
					String nodeId = (String) result.get("node_id");
					if (nodeId != null) {
						lstNodeId = nodeId;
					}
				}
			}

		}
		return lstNodeId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.language.mgr.IImportManager#transformData(java.lang.String,
	 * java.lang.String, java.io.InputStream)
	 */
	@Override
	public Response transformData(String languageId, String sourceId, InputStream stream) {
		if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
		if (StringUtils.isBlank(sourceId) || !LanguageSourceTypeMap.containsSourceType(sourceId))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_SOURCE_TYPE.name(), "Invalid Source Id");
		if (null == stream)
			throw new ClientException(LanguageErrorCodes.ERR_SOURCE_EMPTY_INPUT_STREAM.name(),
					"Source object is emtpy");
		LOGGER.info("Import : " + stream);
		Request request = getLanguageRequest(languageId, LanguageActorNames.IMPORT_ACTOR.name(),
				LanguageOperations.transformWordNetData.name());
		request.put(LanguageParams.format.name(), LanguageParams.CSVInputStream);
		request.put(LanguageParams.input_stream.name(), stream);
		request.put(LanguageParams.source_type.name(), LanguageSourceTypeMap.getSourceType(sourceId));
		LOGGER.info("Import | Request: " + request);
		Response importRes = getLanguageResponse(request, LOGGER);
		return importRes;
	}

	/**
	 * Imports data from an input stream asynchronously.
	 *
	 * @param source
	 *            the source
	 * @param languageId
	 *            the language id
	 * @param taskId
	 *            the task id
	 */
	public void importDataAsync(String source, String languageId, String taskId) {
		try (InputStream in = new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8))) {
			Request request = getLanguageRequest(languageId, LanguageActorNames.ENRICH_ACTOR.name(),
					LanguageOperations.importDataAsync.name());
			request.put(LanguageParams.input_stream.name(), in);
			if (taskId != null) {
				request.put(LanguageParams.prev_task_id.name(), taskId);
			}
			controllerUtil.makeLanguageAsyncRequest(request, LOGGER);
		} catch (IOException e) {
			LOGGER.error("Error! While Closing the Input Stream.", e);
		}
	}

	/**
	 * Converts stream To byte array.
	 *
	 * @param input
	 *            Stream the input Stream
	 * @return the byte[]
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static byte[] toByteArrayUsingJava(InputStream is) throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			int reads = is.read();
			while (reads != -1) {
				baos.write(reads);
				reads = is.read();
			}
			return baos.toByteArray();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.language.mgr.IImportManager#importData(java.lang.String,
	 * java.io.InputStream, java.io.InputStream)
	 */
	@Override
	public Response importData(String languageId, InputStream synsetStream, InputStream wordStream) {
		if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
		if (null == synsetStream)
			throw new ClientException(LanguageErrorCodes.ERR_EMPTY_INPUT_STREAM.name(), "Synset object is emtpy");
		if (null == wordStream)
			throw new ClientException(LanguageErrorCodes.ERR_EMPTY_INPUT_STREAM.name(), "Word object is emtpy");
		LOGGER.info("Enrich | Synset : " + synsetStream);
		LOGGER.info("Enrich | Word : " + wordStream);

		// Indices : Note- Change the value of index if there is change in CSV
		// File structure
		final int IDX_WORD_IDENTIFIER = 0;
		final int IDX_WORD_LEMMA = 1;
		final int IDX_SYNSET_IDENTIFIER = 0;
		final int IDX_SYNSET_WORD_MEMBER = 1;
		final int IDX_SYNSET_ANTONYM_SYNSET_ID = 2;
		final int IDX_SYNSET_HYPONYM_SYNSET_ID = 3;
		final int IDX_SYNSET_MERONYM_SYNSET_ID = 4;
		final int IDX_SYNSET_HOLONYM_SYNSET_ID = 5;
		final int IDX_SYNSET_HYPERNYM_SYNSET_ID = 6;
		final int IDX_SYNSET_MEANING = 7;
		final int IDX_SYNSET_USAGE = 8;
		final int IDX_SYNSET_POS = 9;

		Reader reader = null;
		BufferedReader br = null;
		DictionaryObject dictionaryObject = new DictionaryObject();
		List<WordModel> lstEnrichedWord = new ArrayList<WordModel>();
		List<WordModel> lstWord = new ArrayList<WordModel>();
		List<SynsetModel> lstSynset = new ArrayList<SynsetModel>();
		String line = "";
		String[] objectDetails = null;
		String CSV_SPLIT_BY = ",";
		StringBuffer wordContentBuffer = new StringBuffer();
		StringBuffer synsetContentBuffer = new StringBuffer();

		try {
			// Form word models and word content buffer
			wordContentBuffer.append("identifier,Lemma,objectType");
			wordContentBuffer.append(NEW_LINE);
			reader = new InputStreamReader(wordStream, "UTF8");
			br = new BufferedReader(reader);
			while ((line = br.readLine()) != null) {
				try {
					WordModel word = new WordModel();
					objectDetails = line.split(CSV_SPLIT_BY);
					word.setIdentifier(objectDetails[IDX_WORD_IDENTIFIER]);
					word.setWordLemma(objectDetails[IDX_WORD_LEMMA]);
					lstWord.add(word);
					wordContentBuffer.append(line);
					wordContentBuffer.append(NEW_LINE);
				} catch (ArrayIndexOutOfBoundsException e) {
					LOGGER.error(e.getMessage(), e);
					continue;
				}
			}

			// Cleanup
			line = "";
			if (null != reader)
				reader.close();
			if (null != br)
				br.close();

			// Form Synset Models and synset content buffer
			synsetContentBuffer.append(
					"identifier,rel:synonym,rel:hasAntonym,rel:hasHyponym,rel:hasMeronym,rel:hasHolonym,rel:hasHypernym,gloss,exampleSentences,pos,objectType");
			synsetContentBuffer.append(NEW_LINE);
			reader = new InputStreamReader(synsetStream, "UTF8");
			br = new BufferedReader(reader);
			while ((line = br.readLine()) != null) {
				try {
					SynsetModel synset = new SynsetModel();
					objectDetails = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
					synset.setIdentifier(objectDetails[IDX_SYNSET_IDENTIFIER]);
					synset.setWordMember(objectDetails[IDX_SYNSET_WORD_MEMBER]);
					synset.setAntonymSynsetId(objectDetails[IDX_SYNSET_ANTONYM_SYNSET_ID]);
					synset.setHyponymSynsetId(objectDetails[IDX_SYNSET_HYPONYM_SYNSET_ID]);
					synset.setMeronymSynsetId(objectDetails[IDX_SYNSET_MERONYM_SYNSET_ID]);
					synset.setHolonymSynsetId(objectDetails[IDX_SYNSET_HOLONYM_SYNSET_ID]);
					synset.setHypernymSynsetId(objectDetails[IDX_SYNSET_HYPERNYM_SYNSET_ID]);
					synset.setMeaning(objectDetails[IDX_SYNSET_MEANING]);
					synset.setUsage(objectDetails[IDX_SYNSET_USAGE].replace("\"", ""));
					synset.setPartOfSpeech(objectDetails[IDX_SYNSET_POS]);
					lstSynset.add(synset);
					synsetContentBuffer.append(line);
					synsetContentBuffer.append(NEW_LINE);
				} catch (ArrayIndexOutOfBoundsException e) {
					LOGGER.error(e.getMessage(), e);
					continue;
				}
			}

			String wordIdList = "";
			String wordContent = "";
			String synsetContent = "";

			// replace words with word ids from ElasticSearch index and import
			// the streams.
			if (lstWord.size() > 0) {
				dictionaryObject = replaceWordsIfPresentAlready(languageId, lstWord, lstSynset);
				if (null != dictionaryObject) {
					lstEnrichedWord = dictionaryObject.getLstWord();
					if (null != lstEnrichedWord) {
						wordIdList = writeWordsAndIdstoCSV(lstEnrichedWord);
						wordContent = getWordsListAsCSVString(lstEnrichedWord);
					}
					List<SynsetModel> lstEnrichedSynset = dictionaryObject.getLstSynset();
					if (null != lstEnrichedSynset) {
						synsetContent = getSynsetsListAsCSVString(lstEnrichedSynset);
					}
				}
			}

			// creates a task node whose status will determine the next task
			// execution
			String taskId = controllerUtil.createTaskNode(languageId);
			// performs the import asynchronously
			controllerUtil.importNodesFromStreamAsync(wordContent, languageId, taskId);
			// imports the data asynchronously once the task is completed
			importDataAsync(synsetContent, languageId, taskId);
			return OK("wordList", wordIdList);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			e.printStackTrace();
		} finally {
			line = "";
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					LOGGER.error(e.getMessage(), e);
					e.printStackTrace();
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					LOGGER.error(e.getMessage(), e);
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * Gets the words list as CSV output.
	 *
	 * @param lstEnrichedWord
	 *            the lst enriched word
	 * @return the words list as CSV string
	 */
	private String getWordsListAsCSVString(List<WordModel> lstEnrichedWord) {
		StringBuffer oneLine = new StringBuffer();
		oneLine.append("identifier,Lemma,objectType");
		oneLine.append(NEW_LINE);
		for (WordModel word : lstEnrichedWord) {
			oneLine.append(word.getIdentifier() == null ? "" : StringEscapeUtils.escapeCsv(word.getIdentifier()));
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(word.getWordLemma() == null ? "" : StringEscapeUtils.escapeCsv(word.getWordLemma()));
			oneLine.append(CSV_SEPARATOR);
			oneLine.append("Word");
			oneLine.append(NEW_LINE);
		}
		return oneLine.toString();
	}

	/**
	 * Gets the synsets list as CSV output.
	 *
	 * @param lstEnrichedSynset
	 *            the lst enriched synset
	 * @return the synsets list as CSV string
	 */
	private String getSynsetsListAsCSVString(List<SynsetModel> lstEnrichedSynset) {
		StringBuffer oneLine = new StringBuffer();
		oneLine.append(
				"identifier,rel:synonym,rel:hasAntonym,rel:hasHyponym,rel:hasMeronym,rel:hasHolonym,rel:hasHypernym,gloss,exampleSentences,pos,objectType");
		oneLine.append(NEW_LINE);
		for (SynsetModel synset : lstEnrichedSynset) {
			oneLine.append(synset.getIdentifier() == null ? "" : StringEscapeUtils.escapeCsv(synset.getIdentifier()));
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(synset.getWordMember() == null ? "" : StringEscapeUtils.escapeCsv(synset.getWordMember()));
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(synset.getAntonymSynsetId() == null ? ""
					: StringEscapeUtils.escapeCsv(synset.getAntonymSynsetId()));
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(synset.getHyponymSynsetId() == null ? ""
					: StringEscapeUtils.escapeCsv(synset.getHyponymSynsetId()));
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(synset.getMeronymSynsetId() == null ? ""
					: StringEscapeUtils.escapeCsv(synset.getMeronymSynsetId()));
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(synset.getHolonymSynsetId() == null ? ""
					: StringEscapeUtils.escapeCsv(synset.getHolonymSynsetId()));
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(synset.getHypernymSynsetId() == null ? ""
					: StringEscapeUtils.escapeCsv(synset.getHypernymSynsetId()));
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(synset.getMeaning() == null ? "" : StringEscapeUtils.escapeCsv(synset.getMeaning()));
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(synset.getUsage() == null ? "" : StringEscapeUtils.escapeCsv(synset.getUsage()));
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(
					synset.getPartOfSpeech() == null ? "" : StringEscapeUtils.escapeCsv(synset.getPartOfSpeech()));
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(ObjectType.Synset.toString());
			oneLine.append(NEW_LINE);
		}
		return oneLine.toString();
	}

	/**
	 * Write words and word ids into CSV.
	 *
	 * @param lstEnrichedWord
	 *            the lst enriched word
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private String writeWordsAndIdstoCSV(List<WordModel> lstEnrichedWord) throws IOException {
		StringBuffer oneLine = new StringBuffer();
		oneLine.append("identifier");
		oneLine.append(CSV_SEPARATOR);
		oneLine.append("Lemma");
		oneLine.append(NEW_LINE);
		for (WordModel word : lstEnrichedWord) {
			oneLine.append(word.getIdentifier() == null ? "" : StringEscapeUtils.escapeCsv(word.getIdentifier()));
			oneLine.append(CSV_SEPARATOR);
			oneLine.append(word.getWordLemma() == null ? "" : StringEscapeUtils.escapeCsv(word.getWordLemma()));
			oneLine.append(NEW_LINE);
		}
		return oneLine.toString();
	}

	/**
	 * Gets the word index info from ElasticSearch.
	 *
	 * @param languageId
	 *            the language id
	 * @param indexesMap
	 *            the indexes map
	 * @param lstWord
	 *            the lst word
	 * @return the index info
	 */
	@SuppressWarnings("unchecked")
	private void getIndexInfo(String languageId, Map<String, Object> indexesMap, List<WordModel> lstWord) {
		if (null != lstWord && !lstWord.isEmpty()) {
			int start = 0;
			int batch = 100;
			if (batch > lstWord.size())
				batch = lstWord.size();
			while (start < lstWord.size()) {
				List<String> list = new ArrayList<String>();
				for (int i = start; i < batch; i++) {
					list.add(lstWord.get(i).getWordLemma());
				}

				// gets the word index info by passing a message to the Indexes
				// actor
				Request langReq = getLanguageRequest(languageId, LanguageActorNames.INDEXES_ACTOR.name(),
						LanguageOperations.getIndexInfo.name());
				langReq.put(LanguageParams.words.name(), list);
				Response langRes = getLanguageResponse(langReq, LOGGER);
				if (!checkError(langRes)) {
					Map<String, Object> map = (Map<String, Object>) langRes.get(LanguageParams.index_info.name());
					if (null != map && !map.isEmpty()) {
						indexesMap.putAll(map);
					}
				}
				start += 100;
				batch += 100;
				if (batch > lstWord.size())
					batch = lstWord.size();
			}
		}
	}

	/**
	 * Replace word ids in the input stream if present already in the platform.
	 *
	 * @param languageId
	 *            the language id
	 * @param lstWord
	 *            the lst word
	 * @param lstSynset
	 *            the lst synset
	 * @return the dictionary object
	 */
	@SuppressWarnings("unchecked")
	private DictionaryObject replaceWordsIfPresentAlready(String languageId, List<WordModel> lstWord,
			List<SynsetModel> lstSynset) {
		Map<String, Object> indexesMap = new HashMap<String, Object>();
		if (lstWord.size() > 0) {
			getIndexInfo(languageId, indexesMap, lstWord);
			DictionaryObject dictionaryObject = new DictionaryObject();
			Map<String, String> replacedWordIdMap = new HashMap<String, String>();

			for (String key : indexesMap.keySet()) {
				for (WordModel word : lstWord) {
					try {
						if (StringUtils.equalsIgnoreCase(word.getWordLemma().trim(), key.trim())) {
							// Record the changed/updated word identifier which
							// needs to be replaced in Synset List as well.
							Map<String, Object> wordIndexInfo = (Map<String, Object>) indexesMap
									.get(word.getWordLemma());
							if (!StringUtils.equalsIgnoreCase(word.getIdentifier(),
									wordIndexInfo.get(LanguageParams.wordId.name()).toString())) {
								replacedWordIdMap.put(word.getIdentifier().trim(),
										wordIndexInfo.get(LanguageParams.wordId.name()).toString());
							}
							word.setIdentifier(wordIndexInfo.get(LanguageParams.wordId.name()).toString());
							break;
						}
					} catch (Exception e) {
						LOGGER.error(e.getMessage(), e);
						e.printStackTrace();
						continue;
					}
				}
			}
			// Remove duplicate words from Word List

			Set<WordModel> uniqueWordList = new HashSet<WordModel>();
			uniqueWordList.addAll(lstWord);
			lstWord.clear();
			lstWord.addAll(uniqueWordList);

			// Replace new Word Ids with existing one in Synset List.
			if (lstSynset.size() > 0) {
				for (SynsetModel synset : lstSynset) {
					String[] lstMemberWordId = null;
					String ogMemberWordId = synset.getWordMember();
					String memberWordId = ogMemberWordId.replaceAll("\"", "");
					String newMemberWordId = "";
					if (!StringUtils.isBlank(memberWordId)) {
						lstMemberWordId = memberWordId.split(CSV_SEPARATOR);
						for (String wordId : lstMemberWordId) {
							if (replacedWordIdMap.containsKey(wordId.trim())) {
								wordId = replacedWordIdMap.get(wordId).trim();
							}
							newMemberWordId = newMemberWordId + CSV_SEPARATOR + wordId;
						}
						synset.setWordMember(newMemberWordId.substring(1));
					}
				}
			}
			dictionaryObject.setLstWord(lstWord);
			dictionaryObject.setLstSynset(lstSynset);
			dictionaryObject.put(LanguageParams.replacedWordIdMap.name(), replacedWordIdMap);
			return dictionaryObject;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.language.mgr.IImportManager#importCSV(java.lang.String,
	 * java.io.InputStream)
	 */
	@Override
	public Response importCSV(String languageId, InputStream stream) {
		if (StringUtils.isBlank(languageId))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
		if (null == stream)
			throw new ClientException(LanguageErrorCodes.ERR_EMPTY_INPUT_STREAM.name(), "Input Zip object is emtpy");
		LOGGER.info("Import language CSV : " + stream);
		Request request = getRequest(languageId, GraphEngineManagers.GRAPH_MANAGER, "importGraph");
		request.put(GraphEngineParams.format.name(), ImportType.CSV.name());
		request.put(GraphEngineParams.input_stream.name(), new InputStreamValue(stream));
		Response createRes = getResponse(request, LOGGER);
		if (checkError(createRes)) {
			return createRes;
		} else {
			Response response = copyResponse(createRes);
			OutputStreamValue os = (OutputStreamValue) createRes.get(GraphEngineParams.output_stream.name());
			if (null != os && null != os.getOutputStream() && null != os.getOutputStream().toString()) {
				try (ByteArrayOutputStream bos = (ByteArrayOutputStream) os.getOutputStream()) {
					String csv = new String(bos.toByteArray());
					response.put(LanguageParams.response.name(), csv);
				} catch (IOException e) {
					LOGGER.error("Error! While Closing the Input Stream.", e);
				}
			}
			return response;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.language.mgr.IImportManager#updateDefinition(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Response updateDefinition(String languageId, String json) {
		if (StringUtils.isBlank(languageId))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
		if (StringUtils.isBlank(json))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(),
					"Definition nodes JSON is empty");
		LOGGER.info("Update Definition : " + languageId);
		Request request = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "importDefinitions");
		request.put(GraphEngineParams.input_stream.name(), json);
		return getResponse(request, LOGGER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.language.mgr.IImportManager#findAllDefinitions(java.lang.
	 * String)
	 */
	@Override
	public Response findAllDefinitions(String id) {
		if (StringUtils.isBlank(id))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
		LOGGER.info("Get All Definitions : " + id);
		Request request = getRequest(id, GraphEngineManagers.SEARCH_MANAGER, "getAllDefinitions");
		return getResponse(request, LOGGER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.language.mgr.IImportManager#findDefinition(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Response findDefinition(String id, String objectType) {
		if (StringUtils.isBlank(id))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECTTYPE.name(), "Object Type is empty");
		LOGGER.info("Get Definition : " + id + " : Object Type : " + objectType);
		Request request = getRequest(id, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinitionFromCache",
				GraphDACParams.object_type.name(), objectType);
		return getResponse(request, LOGGER);
	}
}