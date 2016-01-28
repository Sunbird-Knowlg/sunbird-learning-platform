package org.ekstep.language.controller;

import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.mgr.IImportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ilimi.common.dto.Response;

@Controller
@RequestMapping("v1/language")
public class ImportController extends BaseLanguageController {
	
	@Autowired
    private IImportManager importManager;

    private static Logger LOGGER = LogManager.getLogger(ImportController.class.getName());

    @RequestMapping(value = "/{languageId:.+}/import/{sourceId:.+}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> importData(@PathVariable(value = "languageId") String languageId,
    		@PathVariable(value = "sourceId") String sourceId,
            @RequestParam("file") MultipartFile file, @RequestHeader(value = "user-id") String userId,
            HttpServletResponse resp) {
        String apiId = "language.import";
        LOGGER.info("Import | Language Id: " + languageId + " Source Id: " + sourceId + " | File: " + file + " | user-id: " + userId);
        try {
            InputStream stream = null;
            if (null != file)
                stream = file.getInputStream();
            Response response = importManager.importData(languageId, sourceId, stream);
            LOGGER.info("Import | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Import | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
    
    @RequestMapping(value = "/{languageId:.+}/enrich/{objectType:.+}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> enrich(@PathVariable(value = "languageId") String languageId,
    		@PathVariable(value = "objectType") String objectType,
            @RequestParam("synsetFile") MultipartFile synsetFile,
            @RequestParam("wordFile") MultipartFile wordFile, @RequestHeader(value = "user-id") String userId,
            HttpServletResponse resp) {
        String apiId = "language.enrich";
        LOGGER.info("Import | Language Id: " + languageId + " Source Id: " + objectType + " | Synset File: " + synsetFile + " Synset File: " + wordFile + "| user-id: " + userId);
        try {
            InputStream synsetStream = null;
            InputStream wordStream = null;
            if (null != synsetFile || null != wordFile) {
                synsetStream = synsetFile.getInputStream();
                wordStream = wordFile.getInputStream();
            }
            Response response = importManager.enrich(languageId, synsetStream, wordStream);
            LOGGER.info("Import | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Import | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
}