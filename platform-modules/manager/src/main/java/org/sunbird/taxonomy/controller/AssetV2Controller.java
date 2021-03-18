package org.sunbird.taxonomy.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLConnection;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.sunbird.common.controller.BaseController;
import org.sunbird.common.optimizr.Optimizr;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/v2/asset")
public class AssetV2Controller extends BaseController {

	private Optimizr ekstepOptimizr = new Optimizr();
	private static final String tempFileLocation = "/data/contentBundle/";

	@RequestMapping(value = "/optimize/", method = RequestMethod.POST)
	@ResponseBody
	public void optimize(@RequestParam("inputFile") MultipartFile inputFile, HttpServletResponse resp) {
		// String apiId = "asset.optimize";

		try {
			String tempFileDwn = tempFileLocation + System.currentTimeMillis() + "_temp";
			File convInputFile = new File(tempFileDwn, inputFile.getOriginalFilename());

			// Create the file using the touch method of the FileUtils class.
			FileUtils.touch(convInputFile);

			// Write bytes from the multipart file to disk.
			FileUtils.writeByteArrayToFile(convInputFile, inputFile.getBytes());

			File optimizedFile = ekstepOptimizr.optimizeFile(convInputFile);
			if (optimizedFile != null) {
				try (InputStream optimizedFileStream = new BufferedInputStream(new FileInputStream(optimizedFile))) {

					String mimeType = URLConnection.guessContentTypeFromName(optimizedFile.getName());
					if (mimeType == null) {
						mimeType = "application/octet-stream";
					}
					resp.setContentType(mimeType);
					resp.setHeader("Content-Disposition", "attachment; filename=" + inputFile.getOriginalFilename());
					resp.setContentLength((int) optimizedFile.length());
					// send file response
					FileCopyUtils.copy(optimizedFileStream, resp.getOutputStream());
					// delete the temp directory
					org.apache.commons.io.FileUtils.deleteDirectory(optimizedFile.getParentFile());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
