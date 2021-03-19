package org.sunbird.content.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.common.Platform;
import org.sunbird.common.Slug;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.mgr.ConvertGraphNode;
import org.sunbird.common.util.HttpDownloadUtility;
import org.sunbird.common.util.S3PropertyReader;
import org.sunbird.common.util.UnzipUtility;
import org.sunbird.content.common.ContentConfigurationConstants;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.common.EcarPackageType;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.graph.common.JSONUtils;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.learning.common.enums.ContentAPIParams;
import org.sunbird.learning.common.enums.ContentErrorCodes;
import org.sunbird.learning.util.CloudStore;
import org.sunbird.learning.util.ControllerUtil;
import org.sunbird.telemetry.logger.TelemetryManager;

import static java.util.stream.Collectors.toList;

/**
 * The Class ContentBundle.
 */
public class ContentBundle {

	/** The logger. */

	/** The mapper. */
	private static ObjectMapper mapper = new ObjectMapper();
	public final String TAXONOMY_ID = "domain";
	public final ControllerUtil util = new ControllerUtil();
	
	private boolean disableAkka = false;

	/** The Constant BUNDLE_PATH. */
	protected static final String BUNDLE_PATH = "/tmp";

	/** The s3 ecar folder */
	private static final String ECAR_FOLDER = "cloud_storage.ecar.folder";

	/** The default youtube mimeType */
	private static final String YOUTUBE_MIMETYPE = "video/youtube";

	private static final String ARTIFACT_YOUTUBE_MIMETYPE = "video/x-youtube";
	
	private static final String WEB_URL_MIMETYPE = "text/x-url";
	
	private static final List<String> EXCLUDE_ECAR_METADATA_FIELDS=Arrays.asList("screenshots","posterImage");
	private static final String COLLECTION_MIMETYPE = "application/vnd.ekstep.content-collection";

	public ContentBundle() {
		// TODO Auto-generated constructor stub
	}
	
	public ContentBundle(boolean disableAkka) {
		this.disableAkka = disableAkka;
	}

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
		if (!EcarPackageType.ONLINE.equals(packageType)) {
			urlFields.add("appIcon");
			urlFields.add("grayScaleAppIcon");
			urlFields.add("artifactUrl");
			urlFields.add("itemSetPreviewUrl");
		}

		Map<Object, List<String>> downloadUrls = new HashMap<Object, List<String>>();
		for (Map<String, Object> content : contents) {
			String identifier = (String) content.get(ContentWorkflowPipelineParams.identifier.name());
			String mimeType = (String) content.get(ContentWorkflowPipelineParams.mimeType.name());
			String contentDisposition = (String) content.get(ContentAPIParams.contentDisposition.name());
			// TODO: START : Remove this when mobile app is ready
			if (children.contains(identifier))
				content.put(ContentWorkflowPipelineParams.visibility.name(),
						ContentWorkflowPipelineParams.Parent.name());
			// TODO: END

			//TODO: Added for backward compatibility in mobile - start
//			updateContentTaggedProperty(content);

			if (StringUtils.isNotBlank(expiresOn))
				content.put(ContentWorkflowPipelineParams.expires.name(), expiresOn);
			content.keySet().removeIf(metadata -> EXCLUDE_ECAR_METADATA_FIELDS.contains(metadata));
			for (Map.Entry<String, Object> entry : content.entrySet()) {
				if (urlFields.contains(entry.getKey())) {
					Object val = entry.getValue();
					if (null != val) {
						if (!isOnlineContent(mimeType, contentDisposition)) {
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

							} else if (val instanceof List) {
								List<String> data = (List<String>) val;
								List<String> id = new ArrayList<>();
								id.add(identifier.trim() + File.separator + entry.getKey());
								List<String> screeshot = new ArrayList<>();
								for (String value : data) {
									if (HttpDownloadUtility.isValidUrl(value)) {
										downloadUrls.put(value, id);
										String file = FilenameUtils.getName(value);
										screeshot.add(identifier.trim() + File.separator
												+ ContentWorkflowPipelineParams.screenshots.name() + File.separator
												+ Slug.makeSlug(file, true));
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
			if (StringUtils.equalsIgnoreCase(contentDisposition, "online-only")) {
				content.put(ContentWorkflowPipelineParams.downloadUrl.name(), "");
			} else
				content.put(ContentWorkflowPipelineParams.downloadUrl.name(),
						(String) content.get(ContentWorkflowPipelineParams.artifactUrl.name()));
		}
		return downloadUrls;

	}

	
	private boolean isOnlineContent(String mimeType, String contentDisposition) {
		return StringUtils.equalsIgnoreCase(mimeType, YOUTUBE_MIMETYPE) || StringUtils.equalsIgnoreCase(mimeType, ARTIFACT_YOUTUBE_MIMETYPE)
				|| StringUtils.equalsIgnoreCase(mimeType, WEB_URL_MIMETYPE) || StringUtils.equalsIgnoreCase(contentDisposition, "online-only");
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
										Map<Object, List<String>> downloadUrls, Node node,
										List<Map<String, Object>> children) {
		String contentId = node.getIdentifier();
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
				//Adding Hierarchy into hierarchy.json file
				if (StringUtils.isNotBlank((String) node.getMetadata().get("mimeType")) &&
						StringUtils.equalsIgnoreCase((String) node.getMetadata().get("mimeType"), COLLECTION_MIMETYPE)) {
					File hierarchyFile = createHierarchyFile(bundlePath, node, children);
					if (null != hierarchyFile)
						downloadedFiles.add(hierarchyFile);
				}
				try {
					File contentBundle = createBundle(downloadedFiles, bundleFileName);
					String folderName = S3PropertyReader.getProperty(ECAR_FOLDER);
					folderName = folderName + "/" + contentId;
					String[] url = CloudStore.uploadFile(folderName, contentBundle, true);
					downloadedFiles.add(contentBundle);
					return url;
				} catch (Throwable e) {
					e.printStackTrace();
					throw e;
				} finally {
					HttpDownloadUtility.deleteFiles(downloadedFiles);
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServerException(ContentErrorCodes.ERR_ECAR_BUNDLE_FAILED.name(),
					"[Error! something went wrong while bundling ECAR]", e);
		} finally {
			try {
				FileUtils.deleteDirectory(new File(bundlePath));
			} catch (IOException e) {
				e.printStackTrace();
				TelemetryManager.error("Error while deleting the bundle base path", e);
			}
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
			TelemetryManager.log("Manifest Header Version: "+ manifestVersion);
			StringBuilder header = new StringBuilder();
			header.append("{ \"id\": \"ekstep.content.archive\", \"ver\": \"").append(manifestVersion);
			header.append("\", \"ts\": \"").append(getResponseTimestamp()).append("\", \"params\": { \"resmsgid\": \"");
			header.append(getUUID()).append("\"}, \"archive\": { \"count\": ").append(contents.size()).append(", ");
			if (StringUtils.isNotBlank(expiresOn))
				header.append("\"expires\": \"").append(expiresOn).append("\", ");
			header.append("\"ttl\": 24, \"items\": ");
			TelemetryManager.log("Content Items in Manifest JSON: "+ contents.size());

			convertStringToMapInMetadata(contents, ContentWorkflowPipelineParams.variants.name());
			convertStringToMapInMetadata(contents, ContentWorkflowPipelineParams.originData.name());

			// Convert to JSON String
			String manifestJSON = header + mapper.writeValueAsString(contents) + "}}";

			FileUtils.writeStringToFile(manifestFileName, manifestJSON);
			TelemetryManager.log("Manifest JSON Written");
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodeConstants.MANIFEST_FILE_WRITE.name(),
					ContentErrorMessageConstants.MANIFEST_FILE_WRITE_ERROR + " | [Unable to Write Manifest File.]", e);
		}
	}

	private void convertStringToMapInMetadata(List<Map<String, Object>> contents, String metadataName) {
		TelemetryManager.log("Contents Before Updating for '" + metadataName + "' Properties : "+ contents);

		TelemetryManager.log("Updating the '" + metadataName + "' map from JSON string to JSON Object.");
		contents.stream().forEach(c -> {
			Object metadata = c.get(metadataName);
			if(metadata instanceof String){
				c.put(metadataName, JSONUtils.convertJSONString((String) metadata));
			}
		});

		TelemetryManager.log("Contents After Updating for '" + metadataName + "' Properties : "+ contents);
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
		if (!contentPackageKeys.contains(key) || (packageType != EcarPackageType.SPINE && packageType != EcarPackageType.ONLINE)) {
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
			ExecutorService pool = Executors.newFixedThreadPool(1);
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
				List<File> f = ff.get(60, TimeUnit.SECONDS);
				if (null != f && !f.isEmpty())
					files.addAll(f);
			}
			pool.shutdown();
		} catch (InterruptedException | ExecutionException | TimeoutException e ) {
			e.printStackTrace();
			throw new ServerException(ContentErrorCodeConstants.MANIFEST_FILE_WRITE.name(),
					ContentErrorMessageConstants.MANIFEST_FILE_WRITE_ERROR + "Error while creating contentBundle", e);
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
					if (file.getName().toLowerCase().endsWith(ContentConfigurationConstants.CONTENT_BUNDLE_MANIFEST_FILE_NAME)
							|| file.getName().equalsIgnoreCase(ContentConfigurationConstants.CONTENT_BUNDLE_HIERARCHY_FILE_NAME)) {
						fileName = file.getName();
					} else if (file.getParentFile().getName().toLowerCase().endsWith("screenshots")) {
						fileName = file.getParent()
								.substring(file.getParentFile().getParent().lastIndexOf(File.separator) + 1)
								+ File.separator + file.getName();
					} else {
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

	public  File createHierarchyFile(String bundlePath, Node node, List<Map<String, Object>> children) throws Exception {
		String contentId = node.getIdentifier();
		File hierarchyFile = null;
		if (node == null || StringUtils.isBlank(bundlePath)) {
			TelemetryManager.error("Hierarchy File creation failed for identifier : " +contentId);
			return hierarchyFile;
		}
		Map<String, Object> hierarchyMap = getContentMap(node, children);
		if (MapUtils.isNotEmpty(hierarchyMap)) {
			hierarchyFile = new File(bundlePath + File.separator + ContentConfigurationConstants.CONTENT_BUNDLE_HIERARCHY_FILE_NAME);
			if (hierarchyFile == null) {
				TelemetryManager.error("Hierarchy File creation failed for identifier : " + contentId);
				return hierarchyFile;
			}
			String header = getHeaderForHierarchy("1.0", null);
			String hierarchyJSON = mapper.writeValueAsString(hierarchyMap);
			hierarchyJSON = header + hierarchyJSON + "}";
			FileUtils.writeStringToFile(hierarchyFile, hierarchyJSON);
			TelemetryManager.log("Hierarchy JSON Written for identifier : " +contentId);
		} else {
			TelemetryManager.log("Hierarchy JSON can't be created for identifier : " +contentId);
		}
		return hierarchyFile;
	}

	public  Map<String, Object> getContentMap(Node node, List<Map<String, Object>> childrenList) {
		DefinitionDTO definition = util.getDefinition(TAXONOMY_ID, "Content", disableAkka);
		Map<String, Object> collectionHierarchy = ConvertGraphNode.convertGraphNode(node, TAXONOMY_ID, definition, null);
		if (CollectionUtils.isNotEmpty(childrenList))
			collectionHierarchy.put("children", childrenList);
		collectionHierarchy.put("identifier", node.getIdentifier());
		collectionHierarchy.put("objectType", node.getObjectType());
		return collectionHierarchy;
	}

	public String getHeaderForHierarchy(String hierarchyVersion, String expiresOn) {
		if (StringUtils.isBlank(hierarchyVersion))
			hierarchyVersion = ContentConfigurationConstants.DEFAULT_CONTENT_HIERARCHY_VERSION;
		TelemetryManager.log("Hierarchy Header Version: " + hierarchyVersion);
		StringBuilder header = new StringBuilder();
		header.append("{ \"id\": \"ekstep.content.hierarchy\", \"ver\": \"").append(hierarchyVersion);
		header.append("\", \"ts\": \"").append(getResponseTimestamp()).append("\", \"params\": { \"resmsgid\": \"");
		header.append(getUUID()).append("\"");
		if (StringUtils.isNotBlank(expiresOn))
			header.append(", \"expires\": \"").append(expiresOn).append("\" }, ");
		else
			header.append(" }, ");

		header.append(" \"content\": ");
		return header.toString();
	}

	/**
	 *
	 * @param contentMap
	 */
	private void updateContentTaggedProperty(Map<String,Object> contentMap) {
		Boolean contentTaggingFlag = Platform.config.hasPath("content.tagging.backward_enable")?
				Platform.config.getBoolean("content.tagging.backward_enable"): false;
		if(contentTaggingFlag) {
			List <String> contentTaggedKeys = Platform.config.hasPath("content.tagging.property") ?
					Arrays.asList(Platform.config.getString("content.tagging.property").split(",")):
					new ArrayList<>(Arrays.asList("subject","medium"));
			contentTaggedKeys.forEach(contentTagKey -> {
				if(contentMap.containsKey(contentTagKey)) {
					List<String> prop = prepareList(contentMap.get(contentTagKey));
					contentMap.put(contentTagKey, prop.get(0));
				}
			});
		}
	}

	/**
	 *
	 * @param obj
	 * @return
	 */
	private static List<String> prepareList(Object obj) {
		List<String> list = new ArrayList<String>();
		try {
			if (obj instanceof String) {
				list.add((String) obj);
			} else if (obj instanceof String[]) {
				list = Arrays.asList((String[]) obj);
			} else if (obj instanceof List){
				list.addAll((List<String>) obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (null != list) {
			list = list.stream().filter(x -> org.apache.commons.lang3.StringUtils.isNotBlank(x) && !org.apache.commons.lang3.StringUtils.equals(" ", x)).collect(toList());
		}
		return list;
	}

}
