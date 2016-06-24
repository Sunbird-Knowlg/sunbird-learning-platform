package org.ekstep.language.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.mgr.IImportManager;
import org.ekstep.language.mgr.impl.ControllerUtil;
import org.ekstep.language.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;


@Controller
@RequestMapping("v1/language")
public class ImportController extends BaseLanguageController {
	
	@Autowired
    private IImportManager importManager;
	private ControllerUtil controllerUtil = new ControllerUtil();
	
    private static Logger LOGGER = LogManager.getLogger(ImportController.class.getName());

	@RequestMapping(value = "/{languageId:.+}/importJSON", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> importJSON(@PathVariable(value = "languageId") String languageId,
			@RequestParam("zipFile") MultipartFile zipFile, @RequestHeader(value = "user-id") String userId,
			HttpServletResponse resp) {
		String apiId = "language.fixAssociation";
		LOGGER.info("Import | Language Id: " + languageId +" Synset File: " + zipFile + "| user-id: " + userId);
		try {
			InputStream synsetsStreamInZIP = null;
			if (null != zipFile || null != zipFile) {
				synsetsStreamInZIP = zipFile.getInputStream();
			}
			Response response =importManager.importJSON(languageId, synsetsStreamInZIP);
			LOGGER.info("Import | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Import | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
    @RequestMapping(value = "/{languageId:.+}/transform/{sourceId:.+}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> transformData(@PathVariable(value = "languageId") String languageId,
    		@PathVariable(value = "sourceId") String sourceId,
            @RequestParam("file") MultipartFile file, @RequestHeader(value = "user-id") String userId,
            HttpServletResponse resp) {
        String apiId = "language.transform";
        LOGGER.info("Import | Language Id: " + languageId + " Source Id: " + sourceId + " | File: " + file + " | user-id: " + userId);
        try {
            InputStream stream = null;
            if (null != file)
                stream = file.getInputStream();
            Response response = importManager.transformData(languageId, sourceId, stream);
            LOGGER.info("Import | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Import | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
    
	@RequestMapping(value = "/{languageId:.+}/importData", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> importData(@PathVariable(value = "languageId") String languageId,
			@RequestParam("synsetFile") MultipartFile synsetFile,
			@RequestParam("wordFile") MultipartFile wordFile, @RequestHeader(value = "user-id") String userId,
			HttpServletResponse resp) {
		String apiId = "language.fixAssociation";
		LOGGER.info("Import | Language Id: " + " | Synset File: " + synsetFile
				+ " Synset File: " + wordFile + "| user-id: " + userId);
		try {
			InputStream synsetStream = null;
			InputStream wordStream = null;
			if (null != synsetFile || null != wordFile) {
				synsetStream = synsetFile.getInputStream();
				wordStream = wordFile.getInputStream();
			}
			Response response = importManager.importData(languageId, synsetStream, wordStream);
			LOGGER.info("Import | Response: " + response);
			String csv = (String) response.getResult().get("wordList");
			if (StringUtils.isNotBlank(csv)) {
				resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
				resp.setContentType("text/csv;charset=utf-8");
				resp.setHeader("Content-Disposition", "attachment; filename=WordList.csv");
				resp.getOutputStream().write(csv.getBytes(StandardCharsets.UTF_8));
				resp.getOutputStream().close();
			}
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Import | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	@RequestMapping(value = "/importwordnet/{languageId:.+}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> importwordnet(@PathVariable(value = "languageId") String languageId,
            @RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
        String apiId = "wordnet.import";
        Request request = getRequestObject(map);

        request.setManagerName(LanguageActorNames.INDOWORDNET_ACTOR.name());
        request.setOperation(LanguageOperations.importIndowordnet.name());
        request.getContext().put(LanguageParams.language_id.name(), languageId);
        LOGGER.info("List | Request: " + request);
        try {
        	controllerUtil.makeAsyncLanguageRequest(request, LOGGER);
            Response response = new Response();
            LOGGER.info("List | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("List | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }
	
	@RequestMapping(value = "/{languageId:.+}/enrich/{sourceId:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> enrich(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "sourceId") String sourceId, @RequestParam("wordListFile") MultipartFile wordListFile,
			@RequestHeader(value = "user-id") String userId,
			HttpServletResponse resp) {
		String apiId = "language.enrich";
		try {
			InputStream wordListStream = null;
			if (null != wordListFile) {
				wordListStream = wordListFile.getInputStream();
			}
			
			Reader reader = null;
		    BufferedReader br = null; 
		    String line;
		    String[] objectDetails;
			reader = new InputStreamReader(wordListStream, "UTF8");
	        br = new BufferedReader(reader);
	        ArrayList<Map<String,String>> wordInfoList = new ArrayList<Map<String,String>>();
	        ArrayList<String> node_ids = new ArrayList<String>();
	        boolean firstLine = true;
	        while ((line = br.readLine()) != null) {
				try {
					if(firstLine){
						firstLine = false;
						continue;
					}
					objectDetails = line.split(Constants.CSV_SPLIT_BY);
					Map<String, String> wordInfo = new HashMap<String, String>();
					wordInfo.put("id", objectDetails[Constants.WL_INDEX_IDENTIFER]);
					wordInfo.put("word", objectDetails[Constants.WL_INDEX_LEMMA]);
					node_ids.add(wordInfo.get("id"));
					wordInfoList.add(wordInfo);
				} catch(ArrayIndexOutOfBoundsException e) {
					continue;
				}	
			}
	        
	        addWordIndex(wordInfoList, languageId);   
	        enrichWords(node_ids, languageId);
	        
			Response response = new Response();
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Import | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	private void enrichWords(ArrayList<String> node_ids, String languageId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map = new HashMap<String, Object>();
		map.put(LanguageParams.node_ids.name(), node_ids);
		Request request = new Request();
		request.setRequest(map);
		request.setManagerName(LanguageActorNames.ENRICH_ACTOR.name());
		request.setOperation(LanguageOperations.enrichWords.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);
		makeAsyncRequest(request, LOGGER);
	}

	/*@SuppressWarnings("unchecked")
	private List<Node> getNodesList(ArrayList<String> node_ids, String languageId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(LanguageParams.node_ids.name(), node_ids);
		Request getDataNodesRequest = new Request();
		getDataNodesRequest.setRequest(map);
		getDataNodesRequest.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
		getDataNodesRequest.setOperation("getDataNodes");
		getDataNodesRequest.getContext().put(GraphHeaderParams.graph_id.name(), languageId);
		Response response = getNonLanguageResponse(getDataNodesRequest, LOGGER);
		if(checkError(response)){
			throw new ClientException(LanguageErrorCodes.SYSTEM_ERROR.name(), response.getParams().getErrmsg());
		}
		List<Node> nodeList = (List<Node>) response.get("node_list");
		return nodeList;
	}

	private void updateFrequencyCount(List<Node> nodeList, String languageId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map = new HashMap<String, Object>();
		map.put(LanguageParams.node_list.name(), nodeList);
		Request updateLexileMeasuresRequest = new Request();
		updateLexileMeasuresRequest.setRequest(map);
		updateLexileMeasuresRequest.setManagerName(LanguageActorNames.ENRICH_ACTOR.name());
		updateLexileMeasuresRequest.setOperation(LanguageOperations.updateFrequencyCount.name());
		updateLexileMeasuresRequest.getContext().put(LanguageParams.language_id.name(), languageId);
		makeAsyncRequest(updateLexileMeasuresRequest, LOGGER);
	}

	private void updateLexileMeasures(List<Node> nodeList, String languageId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map = new HashMap<String, Object>();
		map.put(LanguageParams.node_list.name(), nodeList);
		Request updateLexileMeasuresRequest = new Request();
		updateLexileMeasuresRequest.setRequest(map);
		updateLexileMeasuresRequest.setManagerName(LanguageActorNames.ENRICH_ACTOR.name());
		updateLexileMeasuresRequest.setOperation(LanguageOperations.updateLexileMeasures.name());
		updateLexileMeasuresRequest.getContext().put(LanguageParams.language_id.name(), languageId);
		makeAsyncRequest(updateLexileMeasuresRequest, LOGGER);
	}
*/
	private void addWordIndex(ArrayList<Map<String, String>> wordInfoList, String languageId) {
			Map<String, Object> map = new HashMap<String, Object>();
	        map.put(LanguageParams.words.name(), wordInfoList);
	        Request addWordIndexRequest = new Request();
	        addWordIndexRequest.setRequest(map);
	        addWordIndexRequest.setManagerName(LanguageActorNames.INDEXES_ACTOR.name());
	        addWordIndexRequest.setOperation(LanguageOperations.addWordIndex.name());
	        addWordIndexRequest.getContext().put(LanguageParams.language_id.name(), languageId);
	        makeAsyncRequest(addWordIndexRequest, LOGGER);
	}
}