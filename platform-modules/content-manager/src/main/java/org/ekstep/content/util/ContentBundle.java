package org.ekstep.content.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.slugs.Slug;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.common.util.UnzipUtility;
import org.ekstep.content.common.ContentConfigurationConstants;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.common.EcarPackageType;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;

import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.graph.common.JSONUtils;

/**
 * The Class ContentBundle.
 */
public class ContentBundle {

	/** The logger. */
	

	/** The mapper. */
	private ObjectMapper mapper = new ObjectMapper();

	/** The Constant URL_FIELD. */
	protected static final String URL_FIELD = "URL";

	/** The Constant BUNDLE_PATH. */
	protected static final String BUNDLE_PATH = "/data/contentBundle";

	/** The s3 ecar folder */
	private static final String s3EcarFolder = "s3.ecar.folder";

	/** The default youtube mimeType */
	private static final String YOUTUBE_MIMETYPE = "video/youtube";
	/**
	 * Creates the content manifest data.
	 *
	 * @param contents
	 *            the contents
	 * @param children
	 *            the children
	 * @param expiresOn
	 *            the expires on
	 * @return the map
	 */
	@SuppressWarnings("unchecked")
	public Map<Object, List<String>> createContentManifestData(List<Map<String, Object>> contents,
			List<String> children, String expiresOn, EcarPackageType packageType) {
		List<String> urlFields = new ArrayList<String>();
		urlFields.add("appIcon");
		urlFields.add("grayScaleAppIcon");
		urlFields.add("posterImage");
		urlFields.add("artifactUrl");
		urlFields.add("screenshots");

		Map<Object, List<String>> downloadUrls = new HashMap<Object, List<String>>();
		for (Map<String, Object> content : contents) {
			String identifier = (String) content.get(ContentWorkflowPipelineParams.identifier.name());
			String mimeType = (String) content.get(ContentWorkflowPipelineParams.mimeType.name());
			if (children.contains(identifier))
				content.put(ContentWorkflowPipelineParams.visibility.name(),
						ContentWorkflowPipelineParams.Parent.name());
			if (StringUtils.isNotBlank(expiresOn))
				content.put(ContentWorkflowPipelineParams.expires.name(), expiresOn);
			for (Map.Entry<String, Object> entry : content.entrySet()) {
				if (urlFields.contains(entry.getKey())) {
					Object val = entry.getValue();
					if (null != val) {
						if (!StringUtils.equalsIgnoreCase(mimeType, YOUTUBE_MIMETYPE)) {
							if (val instanceof File) {
								File file = (File) val;
								addDownloadUrl(downloadUrls, val, identifier, entry.getKey(), packageType);
								entry.setValue(identifier.trim() + File.separator + file.getName());
							} else if (HttpDownloadUtility.isValidUrl(val)) {
								addDownloadUrl(downloadUrls, val, identifier, entry.getKey(), packageType);
								String file = FilenameUtils.getName(entry.getValue().toString());
								if (file.endsWith(ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR
										+ ContentConfigurationConstants.DEFAULT_ECAR_EXTENSION)) {
									entry.setValue(identifier.trim() + File.separator + identifier.trim() + ".zip");
								} else {
									entry.setValue(identifier.trim() + File.separator + Slug.makeSlug(file, true));
								}

							} else if(val instanceof List) {
								List<String> data = (List<String>) val;
								List<String> id = new ArrayList<>();
								id.add(identifier.trim() + File.separator + entry.getKey());
								List<String> screeshot = new ArrayList<>();
								for(String value : data){
									if(HttpDownloadUtility.isValidUrl(value)){
										downloadUrls.put(value, id);
										String file = FilenameUtils.getName(value);
										screeshot.add(identifier.trim() + File.separator + ContentWorkflowPipelineParams.screenshots.name() + File.separator + Slug.makeSlug(file, true));
									}
								}
								entry.setValue(screeshot);
							}
						} else {
							if (entry.getKey().equals(ContentWorkflowPipelineParams.artifactUrl.name())
									|| entry.getKey().equals(ContentWorkflowPipelineParams.downloadUrl.name())) {
								entry.setValue(entry.getValue());
							} else if (HttpDownloadUtility.isValidUrl(val)) {
										addDownloadUrl(downloadUrls, val, identifier, entry.getKey(), packageType);
										String file = FilenameUtils.getName(entry.getValue().toString());
										entry.setValue(identifier.trim() + File.separator + Slug.makeSlug(file, true));
							}
						}
					}
				}
			}
			content.put(ContentWorkflowPipelineParams.downloadUrl.name(),
					(String) content.get(ContentWorkflowPipelineParams.artifactUrl.name()));
			Object posterImage = content.get(ContentWorkflowPipelineParams.posterImage.name());
			if (null != posterImage && StringUtils.isNotBlank((String) posterImage))
				content.put(ContentWorkflowPipelineParams.appIcon.name(), posterImage);
			String status = (String) content.get(ContentWorkflowPipelineParams.status.name());
			if (!StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.Live.name(), status))
				content.put(ContentWorkflowPipelineParams.pkgVersion.name(), 0);
		}
		return downloadUrls;

	}

	/**
	 * Creates the content bundle.
	 *
	 * @param contents
	 *            the contents
	 * @param fileName
	 *            the file name
	 * @param version
	 *            the version
	 * @param downloadUrls
	 *            the download urls
	 * @return the string[]
	 */
	public String[] createContentBundle(List<Map<String, Object>> contents, String fileName, String version,
			Map<Object, List<String>> downloadUrls, String contentId) {
		String bundleFileName = BUNDLE_PATH + File.separator + fileName;
		String bundlePath = BUNDLE_PATH + File.separator + System.currentTimeMillis() + "_temp";
		List<File> downloadedFiles = getContentBundle(downloadUrls, bundlePath);
		try {
			File manifestFile = new File(
					bundlePath + File.separator + ContentConfigurationConstants.CONTENT_BUNDLE_MANIFEST_FILE_NAME);
			createManifestFile(manifestFile, version, null, contents);
			if (null != downloadedFiles) {
				if (null != manifestFile)
					downloadedFiles.add(manifestFile);
				try {
					File contentBundle = createBundle(downloadedFiles, bundleFileName);
					String folderName = S3PropertyReader.getProperty(s3EcarFolder);
					folderName = folderName + "/" + contentId;
					String[] url = AWSUploader.uploadFile(folderName, contentBundle);
					downloadedFiles.add(contentBundle);
					return url;
				} catch (Throwable e) {
					e.printStackTrace();
					throw e;
				} finally {
					HttpDownloadUtility.DeleteFiles(downloadedFiles);
				}
			}
			return null;
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodes.ERR_ECAR_BUNDLE_FAILED.name(),
					"[Error! something went wrong while bundling ECAR]");
		}
	}

	/**
	 * Creates the bundle.
	 *
	 * @param files
	 *            the files
	 * @param bundleFileName
	 *            the bundle file name
	 * @return the file
	 */
	public File createBundle(List<File> files, String bundleFileName) {
		File bundleFile = new File(bundleFileName);
		try {
			if (null == files || files.isEmpty())
				throw new ClientException(ContentErrorCodeConstants.BUNDLE_FILE_WRITE.name(),
						ContentErrorMessageConstants.NO_FILES_TO_BUNDLE + " | [Atleast one file is needed to bundle.]");
			if (StringUtils.isBlank(bundleFileName))
				throw new ClientException(ContentErrorCodeConstants.BUNDLE_FILE_WRITE.name(),
						ContentErrorMessageConstants.INVALID_BUNDLE_FILE_NAME + " | [Bundle File Name is Required.]");
			try (FileOutputStream stream = new FileOutputStream(bundleFileName)) {
				stream.write(createECAR(files));
			}
		} catch (Throwable e) {
			throw new ServerException(ContentErrorCodeConstants.BUNDLE_FILE_WRITE.name(),
					ContentErrorMessageConstants.BUNDLE_FILE_WRITE_ERROR + " | [Unable to Bundle File.]", e);
		}
		return bundleFile;
	}

	/**
	 * Creates the manifest file.
	 *
	 * @param manifestFileName
	 *            the manifest file name
	 * @param manifestVersion
	 *            the manifest version
	 * @param expiresOn
	 *            the expires on
	 * @param contents
	 *            the contents
	 */
	public void createManifestFile(File manifestFileName, String manifestVersion, String expiresOn,
			List<Map<String, Object>> contents) {
		try {
			if (null == contents || contents.isEmpty())
				throw new ClientException(ContentErrorCodeConstants.MANIFEST_FILE_WRITE.name(),
						ContentErrorMessageConstants.MANIFEST_FILE_WRITE_ERROR
								+ " | [Content List is 'null' or Empty.]");
			if (StringUtils.isBlank(manifestVersion))
				manifestVersion = ContentConfigurationConstants.DEFAULT_CONTENT_MANIFEST_VERSION;
			PlatformLogger.log("Manifest Header Version: " , manifestVersion);
			StringBuilder header = new StringBuilder();
			header.append("{ \"id\": \"ekstep.content.archive\", \"ver\": \"").append(manifestVersion);
			header.append("\", \"ts\": \"").append(getResponseTimestamp()).append("\", \"params\": { \"resmsgid\": \"");
			header.append(getUUID()).append("\"}, \"archive\": { \"count\": ").append(contents.size()).append(", ");
			if (StringUtils.isNotBlank(expiresOn))
				header.append("\"expires\": \"").append(expiresOn).append("\", ");
			header.append("\"ttl\": 24, \"items\": ");
			PlatformLogger.log("Content Items in Manifest JSON: " , contents.size());

			// Updating the 'variant' Property
			PlatformLogger.log("Contents Before Updating for 'variant' Properties : " , contents);

			PlatformLogger.log("Updating the 'variant' map from JSON string to JSON Object.");
			contents.stream().forEach(c -> c.put(ContentWorkflowPipelineParams.variants.name(),
					JSONUtils.convertJSONString((String) c.get(ContentWorkflowPipelineParams.variants.name()))));

			PlatformLogger.log("Contents After Updating for 'variant' Properties : " , contents);

			// Convert to JSON String
			String manifestJSON = header + mapper.writeValueAsString(contents) + "}}";

			FileUtils.writeStringToFile(manifestFileName, manifestJSON);
			PlatformLogger.log("Manifest JSON Written");
		} catch (IOException e) {
			throw new ServerException(ContentErrorCodeConstants.MANIFEST_FILE_WRITE.name(),
					ContentErrorMessageConstants.MANIFEST_FILE_WRITE_ERROR + " | [Unable to Write Manifest File.]", e);
		}
	}

	/**
	 * Adds the download url.
	 *
	 * @param downloadUrls
	 *            the download urls
	 * @param val
	 *            the val
	 * @param identifier
	 *            the identifier
	 * @param key
	 *            TODO
	 * @param packageType
	 *            TODO
	 */
	private void addDownloadUrl(Map<Object, List<String>> downloadUrls, Object val, String identifier, String key,
			EcarPackageType packageType) {
		List<String> contentPackageKeys = new ArrayList<String>();
		contentPackageKeys.add(ContentWorkflowPipelineParams.artifactUrl.name());
		contentPackageKeys.add(ContentWorkflowPipelineParams.downloadUrl.name());
		if (!contentPackageKeys.contains(key) || packageType != EcarPackageType.SPINE) {
			List<String> ids = downloadUrls.get(val);
			if (null == ids) {
				ids = new ArrayList<String>();
				downloadUrls.put(val, ids);
			}
			ids.add(identifier.trim());
		}
	}

	/**
	 * Gets the content bundle.
	 *
	 * @param downloadUrls
	 *            the download urls
	 * @param bundlePath
	 *            the bundle path
	 * @return the content bundle
	 */
	private List<File> getContentBundle(final Map<Object, List<String>> downloadUrls, final String bundlePath) {
		List<File> files = new ArrayList<File>();
		try {
			ExecutorService pool = Executors.newFixedThreadPool(10);
			List<Callable<List<File>>> tasks = new ArrayList<Callable<List<File>>>(downloadUrls.size());
			for (final Object val : downloadUrls.keySet()) {
				tasks.add(new Callable<List<File>>() {
					public List<File> call() throws Exception {
						List<String> ids = downloadUrls.get(val);
						List<File> files = new ArrayList<File>();
						for (String id : ids) {
							String destPath = bundlePath + File.separator + id;
							createDirectoryIfNeeded(destPath);
							if (val instanceof File) {
								File file = (File) val;
								File newFile = new File(destPath + File.separator + file.getName());
								FileUtils.copyFile(file, newFile);
								files.add(newFile);
							} else {
								String url = val.toString();
								if (url.endsWith(".ecar")) {
									File ecarFile = HttpDownloadUtility.downloadFile(url, destPath + "_ecar");
									UnzipUtility unzipper = new UnzipUtility();
									unzipper.unzip(ecarFile.getPath(), destPath + "_ecar");
									File ecarFolder = new File(destPath + "_ecar" + File.separator + id);
									File[] fileList = ecarFolder.listFiles();
									File zipFile = null;
									if (null != fileList && fileList.length > 0) {
										for (File f : fileList) {
											if (f.getName().endsWith(".zip")) {
												zipFile = f;
											}
										}
									}
									if (null != zipFile) {
										String newFileName = id + ".zip";
										File contentDir = new File(destPath);
										if (!contentDir.exists())
											contentDir.mkdirs();
										zipFile.renameTo(new File(contentDir + File.separator + newFileName));
										File ecarTemp = new File(destPath + "_ecar");
										FileUtils.deleteDirectory(ecarTemp);
										File newFile = new File(contentDir + File.separator + newFileName);
										files.add(newFile);
									} else {
										// do nothing
									}
								} else {
									File newFile = HttpDownloadUtility.downloadFile(url, destPath);
									if (null != newFile)
										files.add(newFile);
								}
							}
						}
						return files;
					}
				});
			}
			List<Future<List<File>>> results = pool.invokeAll(tasks);
			for (Future<List<File>> ff : results) {
				List<File> f = ff.get();
				if (null != f && !f.isEmpty())
					files.addAll(f);
			}
			pool.shutdown();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return files;
	}

	/**
	 * Creates the ECAR.
	 *
	 * @param files
	 *            the files
	 * @return the byte[]
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private byte[] createECAR(List<File> files) throws IOException {
		// creating byteArray stream, make it bufforable and passing this buffor
		// to ZipOutputStream
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
				ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream)) {
			// packing files
			for (File file : files) {
				if (null != file) {
					String fileName = null;
					if (file.getName().toLowerCase().endsWith("manifest.json")) {
						fileName = file.getName();
					} else if (file.getParentFile().getName().toLowerCase().endsWith("screenshots")) {
						fileName = file.getParent().substring(file.getParentFile().getParent().lastIndexOf(File.separator) + 1)
								+ File.separator + file.getName();
					}else {
						fileName = file.getParent().substring(file.getParent().lastIndexOf(File.separator) + 1)
								+ File.separator + file.getName();
					}
					// new zip entry and copying inputstream with file to
					// zipOutputStream, after all closing streams
					zipOutputStream.putNextEntry(new ZipEntry(fileName));
					try (FileInputStream fileInputStream = new FileInputStream(file)) {
						IOUtils.copy(fileInputStream, zipOutputStream);
						zipOutputStream.closeEntry();
					}
				}
			}

			if (zipOutputStream != null) {
				zipOutputStream.finish();
				zipOutputStream.flush();
				IOUtils.closeQuietly(zipOutputStream);
			}
			IOUtils.closeQuietly(bufferedOutputStream);
			IOUtils.closeQuietly(byteArrayOutputStream);
			PlatformLogger.log("Printing Byte Array for Content Bundle" , byteArrayOutputStream.toByteArray());
			return byteArrayOutputStream.toByteArray();
		}
	}

	/**
	 * Creates the directory if needed.
	 *
	 * @param directoryName
	 *            the directory name
	 */
	private void createDirectoryIfNeeded(String directoryName) {
		File theDir = new File(directoryName);
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			theDir.mkdir();
		}
	}

	/**
	 * Gets the response timestamp.
	 *
	 * @return the response timestamp
	 */
	private String getResponseTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'XXX");
		return sdf.format(new Date());
	}

	/**
	 * Gets the uuid.
	 *
	 * @return the uuid
	 */
	private String getUUID() {
		UUID uid = UUID.randomUUID();
		return uid.toString();
	}

}
