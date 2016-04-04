package org.ekstep.language.controller;

import java.util.List;
import java.util.Map;

import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.dac.dto.AuditRecord;
import com.ilimi.graph.dac.enums.GraphDACParams;

@Controller
@RequestMapping("v1/language/dictionary/word")
public class WordController extends DictionaryController {

    @Override
    protected String getObjectType() {
        return LanguageObjectTypes.Word.name();
    }

	@SuppressWarnings("unchecked")
    @RequestMapping(value = "/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@PathVariable(value = "languageId") String languageId,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".save";
		Request request = getRequest(map);
		try {
			Response response = dictionaryManager.create(languageId, objectType, request);
			LOGGER.info("Create | Response: " + response);
			if (!checkError(response)) {
			    List<String> nodeIds = (List<String>) response.get(GraphDACParams.node_ids.name());
			    asyncUpdate(nodeIds, languageId);
			    AuditRecord audit = new AuditRecord(languageId, null, "CREATE", response.getParams(), userId,
	                    map.get("request").toString(), (String) map.get("COMMENT"));
	            auditLogManager.saveAuditRecord(audit);
			}
			
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			LOGGER.error("Create | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{languageId}/{objectId:.+}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> update(@PathVariable(value = "languageId") String languageId,
			@PathVariable(value = "objectId") String objectId, @RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId) {
		String objectType = getObjectType();
		String apiId = objectType.toLowerCase() + ".update";
		Request request = getRequest(map);
		try {
			Response response = dictionaryManager.update(languageId, objectId, objectType, request);
			LOGGER.info("Update | Response: " + response);
			if (!checkError(response)) {
				List<String> nodeIds = (List<String>) response.get(GraphDACParams.node_ids.name());
			    asyncUpdate(nodeIds, languageId);
			    AuditRecord audit = new AuditRecord(languageId, null, "UPDATE", response.getParams(), userId,
	                    map.get("request").toString(), (String) map.get("COMMENT"));
	            auditLogManager.saveAuditRecord(audit);
			}
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			LOGGER.error("Create | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}
    
}
