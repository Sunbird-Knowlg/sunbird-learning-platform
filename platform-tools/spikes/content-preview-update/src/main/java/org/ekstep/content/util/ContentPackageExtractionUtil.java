package org.ekstep.content.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.common.util.UnzipUtility;

public class ContentPackageExtractionUtil {
	
	
	private static final String DASH = "-";
	private UnzipUtility unzipUtil = new UnzipUtility();
	
	public String getExtractionPath(String contentId, Map<String, Object> content) {
		String path = "";
		String contentFolder = "content";

		// Getting the Path Suffix
		String mimeType = (String) content.get(ContentPreveiwUpdaterParams.mimeType.name());
		String pathSuffix = "latest";
		
		switch (mimeType) {
		case "application/vnd.ekstep.ecml-archive":
			path += contentFolder + File.separator + ContentPreveiwUpdaterParams.ecml.name() + File.separator + contentId + DASH
					+ pathSuffix;
			break;
		case "application/vnd.ekstep.html-archive":
			path += contentFolder + File.separator + ContentPreveiwUpdaterParams.html.name() + File.separator + contentId + DASH
					+ pathSuffix;
			break;
		case "application/vnd.ekstep.h5p-archive":
			path += contentFolder + File.separator + ContentPreveiwUpdaterParams.h5p.name() + File.separator + contentId + DASH
					+ pathSuffix;
			break;
		default:
			break;
		}
		return path;
	}
	
	public void uploadExtractedPackage(String awsFolderPath, String basePath, boolean slugFile) {
		try {
			// Get List of All the Files in the Extracted Folder
			File extractionDir = new File(basePath);
			List<File> lstFilesToUpload = (List<File>) FileUtils.listFiles(extractionDir, TrueFileFilter.INSTANCE,
					TrueFileFilter.INSTANCE);

			// Upload All the File to S3 Recursively and Concurrently
			bulkFileUpload(lstFilesToUpload, awsFolderPath, basePath, slugFile);
		}catch (Exception e) {
			cleanUpAWSFolder(awsFolderPath);
		} finally {
			try {
				File dir = new File(basePath);
				if (dir.exists())
					dir.delete();
			}catch (Exception e) {
			}
		}
	}
	
	private void cleanUpAWSFolder(String AWSFolderPath) {
		try {
			if (StringUtils.isNoneBlank(AWSFolderPath))
				AWSUploader.deleteFile(AWSFolderPath);
		} catch (Exception ex) {
		}
	}
	
	private void bulkFileUpload(List<File> files, String AWSFolderPath, String basePath, boolean slugFile)
			throws InterruptedException, ExecutionException {

		// Validating Parameters
		if (null == files || files.size() < 1)
			return;

		List<String> lstUploadedFileUrls = new ArrayList<String>();
		ExecutorService pool = Executors.newFixedThreadPool(10);
		List<Callable<Map<String, String>>> tasks = new ArrayList<Callable<Map<String, String>>>(files.size());

		for (final File file : files) {
			tasks.add(new Callable<Map<String, String>>() {
				public Map<String, String> call() throws Exception {
					Map<String, String> uploadMap = new HashMap<String, String>();
					if (file.exists() && !file.isDirectory()) {
						String folderName = AWSFolderPath;
						String path = getFolderPath(file, basePath);
						if (StringUtils.isNotBlank(path))
							folderName += File.separator + path;
						String[] uploadedFileUrl = AWSUploader.uploadFile(folderName, file, slugFile);
						if (null != uploadedFileUrl && uploadedFileUrl.length > 1)
							uploadMap.put(file.getAbsolutePath(), uploadedFileUrl[1]);
					}

					return uploadMap;
				}
			});
		}
		List<Future<Map<String, String>>> results = pool.invokeAll(tasks);
		for (Future<Map<String, String>> uMap : results) {
			Map<String, String> m = uMap.get();
			if (null != m)
				lstUploadedFileUrls.addAll(m.values());
		}

		pool.shutdown();

	}
	
	private String getFolderPath(File file, String basePath) {
		String path = "";
		String filePath = file.getAbsolutePath();
		String base = new File(basePath).getPath();
		path = filePath.replace(base, "");
		path = FilenameUtils.getPathNoEndSeparator(path);
		return path;
	}
	
	public void downloadAndExtract(String ecarUrl, String localFolder) throws Exception {
		try {
			//download the file
			File downladedEcarFile = HttpDownloadUtility.downloadFile(ecarUrl, localFolder);
			//extract content into same folder
			unzipUtil.unzip(downladedEcarFile.getAbsolutePath(), localFolder);
			//delete the file after extracted
			if(downladedEcarFile.exists())
				downladedEcarFile.delete();
		}catch(Exception e) {
			throw new Exception("Something went wrong while downloading and extracting");
		}
	}
	
}
