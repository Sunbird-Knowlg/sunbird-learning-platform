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
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
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
import org.ekstep.language.util.WordUtil;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.importer.InputStreamValue;
import com.ilimi.taxonomy.enums.ContentErrorCodes;
import com.ilimi.taxonomy.mgr.ITaxonomyManager;
import com.ilimi.taxonomy.util.UnzipUtility;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.util.RelationType;
import com.ilimi.graph.engine.router.GraphEngineManagers;


@Component
public class ImportManagerImpl extends BaseLanguageManager implements IImportManager {
	
	private static final String CSV_SEPARATOR = ",";
	private static final String NEW_LINE = "\n";
	private ControllerUtil controllerUtil = new ControllerUtil();
	private static final String tempFileLocation = "/data/temp/";
	
	private ObjectMapper mapper = new ObjectMapper();
	private static Logger LOGGER = LogManager.getLogger(ITaxonomyManager.class.getName());
	
	private List<String> getWordList(Object wordJSONArrObj){
		String JSONarrStr;
		try {
			JSONarrStr = mapper.writeValueAsString(wordJSONArrObj);
			List<String> list = mapper.readValue(JSONarrStr, new TypeReference<List<String>>(){});
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public Response importJSON(String languageId, InputStream synsetsStreamInZIPStream){

		if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
        if (null == synsetsStreamInZIPStream)
            throw new ClientException(LanguageErrorCodes.ERR_EMPTY_INPUT_STREAM.name(),
            		"Input Zip object is emtpy");
        
        String tempFileDwn = tempFileLocation + System.currentTimeMillis() + "_temp";
        UnzipUtility unzipper = new UnzipUtility();
        StringBuffer errorMessages = new StringBuffer();
        Response importResponse = OK();
		try {
			unzipper.unzip(synsetsStreamInZIPStream, tempFileDwn);
			File zipFileDirectory = new File(tempFileDwn);
			List<String> wordList=new ArrayList<String>();			
			String files[] = zipFileDirectory.list();
            for (String temp : files) {
                // construct the file structure
                File jsonFile = new File(zipFileDirectory, temp);
                FileInputStream jsonFIS=new FileInputStream(jsonFile);
                InputStreamReader isReader=new InputStreamReader(jsonFIS, "UTF8");
                String fileName=jsonFile.getName();
                String word=fileName.substring(0, fileName.indexOf(".json"));
                wordList.add(word);
                String jsonContent=IOUtils.toString(isReader);
                System.out.println("fileName="+fileName+",word="+word+",jsoncontent-->\n"+jsonContent);
                
                // Keys : Note- Change the key name if there is change in JSON File structure
                final String KEY_NAME_IDENTIFIER = "sid";
                final String KEY_NAME_GLOSS = "gloss";
                final String KEY_NAME_GLOSS_ENG = "gloss_eng";
                final String KEY_NAME_EXAM_STMT="example_stmt";
                final String KEY_NAME_POS="pos";
                
                List<Map<String, Object>> jsonObj = mapper.readValue(jsonContent, new TypeReference<List<Map<String, Object>>>() {});
                List<SynsetModel> synsetList=new ArrayList<SynsetModel>();
				DictionaryManagerImpl manager=new DictionaryManagerImpl();
                for(Map<String, Object> synsetJSON:jsonObj){
                	
                	String identifier=languageId + ':' + "S:" + synsetJSON.get(KEY_NAME_IDENTIFIER);
    				Node synsetNode=new Node();
    				synsetNode.setGraphId(languageId);
    				synsetNode.setIdentifier(identifier);
    				synsetNode.setNodeType(SystemNodeTypes.DATA_NODE.name());
    				synsetNode.setObjectType(Enums.ObjectType.Synset.name());
    				
    				Map<String, Object> metadata =new HashMap<>();
    				metadata.put("gloss", synsetJSON.get(KEY_NAME_GLOSS));
    				metadata.put("glossInEnglish", synsetJSON.get(KEY_NAME_GLOSS_ENG));
    				metadata.put("exampleSentences", Arrays.asList(synsetJSON.get(KEY_NAME_EXAM_STMT)));
    				metadata.put("pos", synsetJSON.get(KEY_NAME_POS));
    				metadata.put("category", "Default");
    				synsetNode.setMetadata(metadata);
    				
    				String synsetNodeId=updateNode(synsetNode,Enums.ObjectType.Synset.name(),languageId);
    				
    				if(StringUtils.isEmpty(synsetNodeId)){
    					errorMessages.append(", ").append("Synset create/Update failed: "+ identifier);
    					continue;
    				}

    				List<String> words=getWordList(synsetJSON.get("synonyms"));
    				if(!words.contains(word)){
    					words.add(word);
    				}
    				
    				for(String sWord:words){
    					if (StringUtils.isNotBlank(sWord)){
        					WordUtil wordUtil=new WordUtil();
        					Node node=wordUtil.searchWord(languageId,Enums.ObjectType.Word.name(),sWord);
        					String nodeId;
        					if (null == node) {
        						nodeId=wordUtil.createWord(languageId, sWord, Enums.ObjectType.Word.name());
        					}else{
        						nodeId=node.getIdentifier();
        					}
        					Response relationResponse = manager.addRelation(languageId, Enums.ObjectType.Synset.name(), synsetNodeId, RelationTypes.SYNONYM.relationName(), nodeId);
        					if(checkError(relationResponse)){
        						errorMessages.append(", ").append("Synset relation creation failed: "+ identifier + " word id: "+nodeId);
        						continue;
        					}
    					}
    				}
                }
            }
		}catch (Exception ex) {
			errorMessages.append(", ").append(ex.getMessage());
		} finally {
			File zipFileDirectory = new File(tempFileDwn);
			if (!zipFileDirectory.exists()) {
				System.out.println("Directory does not exist.");
			} else {
				try {
					delete(zipFileDirectory);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		String errorMessageString = errorMessages.toString();
		if(!errorMessageString.isEmpty()){
			errorMessageString = errorMessageString.substring(2);
			importResponse = ERROR(LanguageErrorCodes.SYSTEM_ERROR.name(), "Internal Error", ResponseCode.SERVER_ERROR);
			importResponse.put("errorMessage", errorMessageString);
		}
		return importResponse;
	}
	
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
	
	public String updateNode(Node node, String objectType, String languageId){
		node.setObjectType(objectType);
		Request validateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "validateNode");
		validateReq.put(GraphDACParams.node.name(), node);
		String lstNodeId=StringUtils.EMPTY;
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
						lstNodeId=nodeId;
					}
				}
			}
			
		}
		return lstNodeId;
	}
	
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
        Request request = getLanguageRequest(languageId, LanguageActorNames.IMPORT_ACTOR.name(), LanguageOperations.transformWordNetData.name());
        request.put(LanguageParams.format.name(), LanguageParams.CSVInputStream);
        request.put(LanguageParams.input_stream.name(), stream);
        request.put(LanguageParams.source_type.name(), LanguageSourceTypeMap.getSourceType(sourceId));
        LOGGER.info("Import | Request: " + request);
        Response importRes = getLanguageResponse(request, LOGGER);
        return importRes;
	}
	
	public void importDataAsync(String source, String languageId, String taskId) {
        InputStream in = new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));
		Request request = getLanguageRequest(languageId, LanguageActorNames.ENRICH_ACTOR.name(), LanguageOperations.importDataAsync.name());
		request.put(LanguageParams.input_stream.name(),in);
		if(taskId != null){
			request.put(LanguageParams.prev_task_id.name(), taskId);
		}
		controllerUtil.makeLanguageAsyncRequest(request, LOGGER);
	}
	
	public static byte[] toByteArrayUsingJava(InputStream is)
		    throws IOException{
		        ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        int reads = is.read();
		       
		        while(reads != -1){
		            baos.write(reads);
		            reads = is.read();
		        }
		      
		        return baos.toByteArray();
		       
		    }

	@Override
	public Response importData(String languageId, InputStream synsetStream, InputStream wordStream) {
		if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
		/*if (StringUtils.isBlank(sourceId) || !LanguageSourceTypeMap.containsLanguage(sourceId))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Source Id");*/
        if (null == synsetStream)
            throw new ClientException(LanguageErrorCodes.ERR_EMPTY_INPUT_STREAM.name(),
                    "Synset object is emtpy");
        if (null == wordStream)
            throw new ClientException(LanguageErrorCodes.ERR_EMPTY_INPUT_STREAM.name(),
                    "Word object is emtpy");
        LOGGER.info("Enrich | Synset : " + synsetStream);
        LOGGER.info("Enrich | Word : " + wordStream);
      
        // Indices : Note- Change the value of index if there is change in CSV File structure
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
	        // For Word
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
				} catch(ArrayIndexOutOfBoundsException e) {
					continue;
				}	
			}
	        
	        // Cleanup 
	        line = "";
	        if (null != reader) reader.close();
	        if (null != br) br.close();
	        
	        // For Synset
	        synsetContentBuffer.append("identifier,rel:synonym,rel:hasAntonym,rel:hasHyponym,rel:hasMeronym,rel:hasHolonym,rel:hasHypernym,gloss,exampleSentences,pos,objectType");
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
				} catch(ArrayIndexOutOfBoundsException e) {
					continue;
				}	
			}
	        
	        String wordIdList = "";
	        String wordContent="";
	        String synsetContent = "";
	        if (lstWord.size() > 0) {
		        //callAddCitationToIndex(languageId, lstWord);
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
	        
	        String taskId = controllerUtil.createTaskNode(languageId);
	        controllerUtil.importNodesFromStreamAsync(wordContent, languageId, taskId);
	        importDataAsync(synsetContent, languageId, taskId);
	        //controllerUtil.importWordsAndSynsets(wordContent, synsetContent, languageId);
	        return OK("wordList", wordIdList);
        } catch(IOException e) {
        	e.printStackTrace();
        } finally {
        	line = "";
        	if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
        	if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
        return null; 
	}
	
	private String getWordsListAsCSVString(List<WordModel> lstEnrichedWord) {
		StringBuffer oneLine = new StringBuffer();
		oneLine.append("identifier,Lemma,objectType");
        oneLine.append(NEW_LINE);
        for (WordModel word : lstEnrichedWord)
        {
            oneLine.append(word.getIdentifier() == null ? "" : StringEscapeUtils.escapeCsv(word.getIdentifier()));
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(word.getWordLemma() == null ? "" : StringEscapeUtils.escapeCsv(word.getWordLemma()));
            oneLine.append(CSV_SEPARATOR);
            oneLine.append("Word");
            oneLine.append(NEW_LINE);
        }
		return oneLine.toString();
	}
	
	private String getSynsetsListAsCSVString(List<SynsetModel> lstEnrichedSynset) {
		StringBuffer oneLine = new StringBuffer();
		oneLine.append("identifier,rel:synonym,rel:hasAntonym,rel:hasHyponym,rel:hasMeronym,rel:hasHolonym,rel:hasHypernym,gloss,exampleSentences,pos,objectType");
        oneLine.append(NEW_LINE);
        for (SynsetModel synset : lstEnrichedSynset)
        {
        	oneLine.append(synset.getIdentifier() == null ? "" : StringEscapeUtils.escapeCsv(synset.getIdentifier()));
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(synset.getWordMember() == null ? "" : StringEscapeUtils.escapeCsv(synset.getWordMember()));
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(synset.getAntonymSynsetId() == null ? "" : StringEscapeUtils.escapeCsv(synset.getAntonymSynsetId()));
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(synset.getHyponymSynsetId() == null ? "" : StringEscapeUtils.escapeCsv(synset.getHyponymSynsetId()));
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(synset.getMeronymSynsetId() == null ? "" : StringEscapeUtils.escapeCsv(synset.getMeronymSynsetId()));
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(synset.getHolonymSynsetId() == null ? "" : StringEscapeUtils.escapeCsv(synset.getHolonymSynsetId()));
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(synset.getHypernymSynsetId() == null ? "" : StringEscapeUtils.escapeCsv(synset.getHypernymSynsetId()));
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(synset.getMeaning() == null ? "" : StringEscapeUtils.escapeCsv(synset.getMeaning()));
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(synset.getUsage() == null ? "" : StringEscapeUtils.escapeCsv(synset.getUsage()));
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(synset.getPartOfSpeech() == null ? "" : StringEscapeUtils.escapeCsv(synset.getPartOfSpeech()));
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(ObjectType.Synset.toString());
            oneLine.append(NEW_LINE);
        }
		return oneLine.toString();
	}

	private String writeWordsAndIdstoCSV(List<WordModel> lstEnrichedWord) throws IOException {
		StringBuffer oneLine = new StringBuffer();
		oneLine.append("identifier");
        oneLine.append(CSV_SEPARATOR);
        oneLine.append("Lemma");
        oneLine.append(NEW_LINE);
        for (WordModel word : lstEnrichedWord)
        {
            oneLine.append(word.getIdentifier() == null ? "" : StringEscapeUtils.escapeCsv(word.getIdentifier()));
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(word.getWordLemma() == null ? "" : StringEscapeUtils.escapeCsv(word.getWordLemma()));
            oneLine.append(NEW_LINE);
        }
		return oneLine.toString();
	}

	
	/*private void callAddCitationToIndex(String languageId, List<WordModel> lstWord) {
		if (!StringUtils.isBlank(languageId) && LanguageMap.containsLanguage(languageId) && null != lstWord) {
			LOGGER.info("Enrich - callAddCitationToIndex :- Word List : " + lstWord + ", Language Id : " + languageId);
	        Request request = getLanguageRequest(languageId, LanguageActorNames.INDEXES_ACTOR.name(), LanguageOperations.addCitationIndex.name());
	        request.put(LanguageParams.citations.name(), getWordMapList(lstWord));
	        LOGGER.info("List | Request: " + request);
	        Response addCitationRes = getLanguageResponse(request, LOGGER);
	        if (checkError(addCitationRes)) {
	            throw new ClientException(LanguageErrorCodes.SYSTEM_ERROR.name(), addCitationRes.getParams().getErrmsg());
	        } 
		}
	}
	
	private List<Map<String, String>> getWordMapList( List<WordModel> lstWord) {
		List<Map<String, String>> lstMap = new ArrayList<Map<String, String>>();
		for (WordModel word : lstWord) {
			Map<String, String> map = new HashMap<String, String>();
			map.put(LanguageParams.word.name(), word.getWordLemma());
			//map.put(LanguageParams.date.name(), DateTime.now().toString());
			map.put(LanguageParams.sourceType.name(), "iwn");
			map.put(LanguageParams.grade.name(), "1");
			map.put(LanguageParams.source.name(), LanguageSourceTypeMap.getSourceType("iwn"));
			lstMap.add(map);
		}
		return lstMap;
	}
	*/
	private Response callGetIndexInfo(String languageId, List<WordModel> lstWord) {
		if (lstWord.size() > 0) {
			List<String> lstLemma = getWordLemmaList(lstWord);
			if (lstLemma.size() > 0) {
				Request request = getLanguageRequest(languageId, LanguageActorNames.INDEXES_ACTOR.name(), LanguageOperations.getIndexInfo.name());
				request.put(LanguageParams.words.name(), lstLemma);
				Response getIndexInfoRes = getLanguageResponse(request, LOGGER);
	            return getIndexInfoRes;
			}
		}
		return null;
	}
	
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
	
	@SuppressWarnings("unchecked")
	private DictionaryObject replaceWordsIfPresentAlready(String languageId, List<WordModel> lstWord,
			List<SynsetModel> lstSynset) {
		Map<String, Object> indexesMap = new HashMap<String, Object>();
		Map<String, Object> wordInfoMap = new HashMap<String, Object>();
		if (lstWord.size() > 0) {
			// Response getIndexInfoResponse = callGetIndexInfo(languageId,
			// lstWord);
			getIndexInfo(languageId, indexesMap, lstWord);
			// Response response = copyResponse(getIndexInfoResponse);
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
	
	private List<String> getWordLemmaList(List<WordModel> lstWord) {
		List<String> lstLemma = new ArrayList<String>();
		for (WordModel word : lstWord) {
			if (!lstLemma.contains(word.getWordLemma()) && !StringUtils.isBlank(word.getWordLemma()))
				lstLemma.add(word.getWordLemma().trim());
		}
		return lstLemma;
	}

}