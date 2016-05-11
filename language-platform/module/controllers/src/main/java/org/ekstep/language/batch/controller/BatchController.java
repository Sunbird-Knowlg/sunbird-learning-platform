package org.ekstep.language.batch.controller;

import java.util.Map;

import org.ekstep.language.batch.mgr.IBatchManager;
import org.ekstep.language.batch.mgr.IWordnetCSVManager;
import org.ekstep.language.controller.BaseLanguageController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.dto.Response;

@Controller
@RequestMapping("v1/language/batch")
public class BatchController extends BaseLanguageController {

    @Autowired
    private IBatchManager batchManager;

    @Autowired
    private IWordnetCSVManager wordnetCSVManager;
    
    @RequestMapping(value = "/{languageId}/updatePictures", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> updatePictures(@PathVariable(value = "languageId") String languageId) {
        String apiId = "language.updatePictures";
        try {
            Response response = batchManager.updatePictures(languageId);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            e.printStackTrace();
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
    
    @RequestMapping(value = "/{languageId}/cleanupWordNetData", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> cleanupWordNetData(@PathVariable(value = "languageId") String languageId) {
        String apiId = "language.cleanupWordNetData";
        try {
            Response response = batchManager.cleanupWordNetData(languageId);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            e.printStackTrace();
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
    
    @RequestMapping(value = "/{languageId}/updatePosList", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> updatePosList(@PathVariable(value = "languageId") String languageId) {
        String apiId = "language.updatePosList";
        try {
            Response response = batchManager.updatePosList(languageId);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            e.printStackTrace();
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/{languageId}/updateWordFeatures", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> updateWordFeatures(@PathVariable(value = "languageId") String languageId) {
        String apiId = "language.updateWordFeatures";
        try {
            Response response = batchManager.updateWordFeatures(languageId);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            e.printStackTrace();
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/{languageId}/updateFrequencyCounts", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> updateFrequencyCounts(@PathVariable(value = "languageId") String languageId) {
        String apiId = "language.updateFrequencyCounts";
        try {
            Response response = batchManager.updateFrequencyCounts(languageId);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            e.printStackTrace();
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/{languageId}/createWordnetCitations", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> createWordnetCitations(@PathVariable(value = "languageId") String languageId,
            @RequestBody Map<String, Object> map) {
        String apiId = "language.createWordnetCitations";
        try {
            String wordsCSV = (String) map.get("words_csv");
            Response response = wordnetCSVManager.createWordnetCitations(languageId, wordsCSV);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            e.printStackTrace();
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/{languageId}/replaceWordnetIds", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> replaceWordnetIds(@PathVariable(value = "languageId") String languageId,
            @RequestBody Map<String, Object> map) {
        String apiId = "language.replaceWordnetIds";
        try {
            String wordsCSV = (String) map.get("words_csv");
            String synsetCSV = (String) map.get("synsets_csv");
            String outputDir = (String) map.get("output_dir");
            Response response = wordnetCSVManager.replaceWordnetIds(languageId, wordsCSV, synsetCSV, outputDir);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            e.printStackTrace();
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    @RequestMapping(value = "/{languageId}/addWordnetIndexes", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> addWordnetIndexes(@PathVariable(value = "languageId") String languageId,
            @RequestBody Map<String, Object> map) {
        String apiId = "language.addWordnetIndexes";
        try {
            String wordsCSV = (String) map.get("words_csv");
            Response response = wordnetCSVManager.addWordnetIndexes(languageId, wordsCSV);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            e.printStackTrace();
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
}
