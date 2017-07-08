package com.ilimi.taxonomy.controller;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Response;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.taxonomy.mgr.IReferenceManager;

@Controller
@RequestMapping("/v2/reference")
public class ReferenceController extends BaseController {

	private static ILogger LOGGER = PlatformLogManager.getLogger();

	@Autowired
	private IReferenceManager referenceManager;

	@RequestMapping(value = "/upload/{referenceId:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> uploadReferenceDocument(@PathVariable(value = "referenceId") String referenceId,
			@RequestParam(value = "file", required = true) MultipartFile file) {
		String apiId = "media.upload";
		LOGGER.log("Upload | File: " + file);
		try {
			String name = FilenameUtils.getBaseName(file.getOriginalFilename()) + "_" + System.currentTimeMillis() + "."
					+ FilenameUtils.getExtension(file.getOriginalFilename());
			File uploadedFile = new File(name);
			file.transferTo(uploadedFile);
			Response response = referenceManager.uploadReferenceDocument(uploadedFile, referenceId);
			LOGGER.log("Upload | Response: " , response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("Upload | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
}
