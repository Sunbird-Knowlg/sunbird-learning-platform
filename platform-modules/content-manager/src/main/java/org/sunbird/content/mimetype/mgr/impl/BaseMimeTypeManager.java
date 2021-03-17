package org.sunbird.content.mimetype.mgr.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.common.Slug;
import org.sunbird.common.dto.NodeDTO;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.enums.TaxonomyErrorCodes;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.util.HttpDownloadUtility;
import org.sunbird.common.util.S3PropertyReader;
import org.sunbird.common.util.UnzipUtility;
import org.sunbird.common.util.ZipUtility;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.common.EcarPackageType;
import org.sunbird.content.common.ExtractionType;
import org.sunbird.content.dto.ContentSearchCriteria;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.content.util.ContentBundle;
import org.sunbird.content.util.ContentPackageExtractionUtil;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.RelationTypes;
import org.sunbird.graph.dac.model.*;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.sunbird.learning.common.enums.ContentAPIParams;
import org.sunbird.learning.common.enums.ContentErrorCodes;
import org.sunbird.learning.util.BaseLearningManager;
import org.sunbird.learning.util.CloudStore;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class BaseMimeTypeManager extends BaseLearningManager {

	private static final String tempFileLocation = "/data/contentBundle/";
	
	protected static final int IDX_S3_KEY = 0;
	
	protected static final int IDX_S3_URL = 1;

	private static final String CONTENT_FOLDER = "cloud_storage.content.folder";
	private static final String ARTEFACT_FOLDER = "cloud_storage.artefact.folder";
	private static final String defaultBucketType = "public";

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	public static String getBucketName() {
		return getBucketName(defaultBucketType);
	}
	public static String getBucketName(String type) {
		return CloudStore.getContainerName();
	}
	
	public boolean isArtifactUrlSet(Map<String, Object> contentMap) {
		return false;
	}

	public boolean isJSONValid(String content) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.readTree(content);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public boolean isECMLValid(String content) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			dBuilder.parse(IOUtils.toInputStream(content, "UTF-8"));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Map<String, List<Object>> readECMLFile(String filePath) {
		final Map<String, List<Object>> mediaIdMap = new HashMap<String, List<Object>>();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new DefaultHandler() {
				public void startElement(String uri, String localName, String qName, Attributes attributes)
						throws SAXException {
					if (qName.equalsIgnoreCase("media")) {
						String id = attributes.getValue("id");
						if (StringUtils.isNotBlank(id)) {
							String src = attributes.getValue("src");
							if (StringUtils.isNotBlank(src)) {
								String assetId = attributes.getValue("assetId");
								List<Object> mediaValues = new ArrayList<Object>();
								mediaValues.add(src);
								mediaValues.add(assetId);
								mediaIdMap.put(id, mediaValues);
							}
						}
					}
				}

				public void endElement(String uri, String localName, String qName) throws SAXException {
					// System.out.println("End Element :" + qName);
				}
			};
			saxParser.parse(filePath, handler);
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_EXTRACT.name(), "Error while extracting the zipFile");
		}
		return mediaIdMap;
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

	protected Node setNodeStatus(Node node, String status) {
		if (null != node && !StringUtils.isBlank(status)) {
			Map<String, Object> metadata = node.getMetadata();
			if (null != metadata) {
				metadata.put(ContentAPIParams.status.name(), status);
				node.setMetadata(metadata);
			}
		}
		return node;
	}
	
	protected File copyURLToFile(String fileUrl) {
		try {
			String fileName = getFieNameFromURL(fileUrl);
			File file = new File(fileName);
			FileUtils.copyURLToFile(new URL(fileUrl), file);
			return file;
		} catch (IOException e) {
			throw new ClientException(TaxonomyErrorCodes.ERR_INVALID_UPLOAD_FILE_URL.name(), "fileUrl is invalid.");
		}
	}
	
	protected String getFieNameFromURL(String fileUrl) {
		String fileName = FilenameUtils.getBaseName(fileUrl)+"_"+ System.currentTimeMillis();
		if (!FilenameUtils.getExtension(fileUrl).isEmpty()) 
			fileName += "." + FilenameUtils.getExtension(fileUrl);
		return fileName;
	}
	
	/**
	 * This return true if at least one member matches from the checkList.
	 * @param file
	 * @param checkFile
	 * @return
	 * @throws IOException
	 */
	protected boolean hasGivenFile(File file, String checkFile) {
		boolean isValidPackage = false;
		try {
			if (file.exists()) {
				TelemetryManager.log("Validating File For Folder Structure: " + file.getName());
				if (StringUtils.isBlank(checkFile)) {
					isValidPackage = true;
				} else {
					try (ZipFile zipFile = new ZipFile(file)) {
						Enumeration<? extends ZipEntry> entries = zipFile.entries();
						while (entries.hasMoreElements()) {
							ZipEntry entry = entries.nextElement();
							if (StringUtils.equalsIgnoreCase(entry.getName(), checkFile)) {
								isValidPackage = true;
								break;
							}
						}
					}
				}
			}
		} catch (ZipException e) {
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_UPLOAD_FILE.name(), "Invalid zip file");
		} catch (IOException e) {
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_UPLOAD_FILE.name(), "Error while validating the content");
		}
		return isValidPackage;
	}

	private void downloadAppIcon(Node node, String tempFolder) {
		String appIcon = (String) node.getMetadata().get("appIcon");
		if (StringUtils.isNotBlank(appIcon)) {
			File logoFile = HttpDownloadUtility.downloadFile(appIcon, tempFolder);
			try {
				if (null != logoFile && logoFile.exists() && logoFile.isFile()) {
					String parentFolderName = logoFile.getParent();
					File newName = new File(parentFolderName + File.separator + "logo.png");
					logoFile.renameTo(newName);
				}
			} catch (Exception ex) {
				throw new ServerException(ContentErrorCodes.ERR_CONTENT_PUBLISH.name(), "Error while publish the content");
			}
		}
	}

	private void deleteTemp(String sourceFolder) {
		File directory = new File(sourceFolder);
		if (!directory.exists()) {
			System.out.println("Directory does not exist.");
		} else {
			try {
				delete(directory);
				if (!directory.exists()) {
					directory.mkdirs();
				}
			} catch (IOException e) {
				throw new ServerException(ContentErrorCodes.ERR_CONTENT_PUBLISH.name(), "Error while publishing the content");
			}
		}
	}

	@Deprecated
	protected Response rePublish(Node node) {
		Response response = new Response();
		node = setNodeStatus(node, ContentAPIParams.Live.name());
		String tempFolder = tempFileLocation + File.separator + System.currentTimeMillis() + "_temp";
		File ecarFile = null;
		String artifactUrl = (String) node.getMetadata().get(ContentAPIParams.artifactUrl.name());
		if (StringUtils.isNotBlank(artifactUrl))
			ecarFile = HttpDownloadUtility.downloadFile(artifactUrl, tempFolder);
		try {
			if (null != ecarFile && ecarFile.exists() && ecarFile.isFile()) {
				File newName = new File(ecarFile.getParent() + File.separator + System.currentTimeMillis() + "_"
						+ node.getIdentifier() + "." + FilenameUtils.getExtension(ecarFile.getPath()));
				ecarFile.renameTo(newName);
				node.getMetadata().put(ContentAPIParams.downloadUrl.name(), newName);
			}
			downloadAppIcon(node, tempFolder);
			response = addDataToContentNode(node);
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_PUBLISH.name(), "Error while publishing the content");
		} finally {
			deleteTemp(tempFolder);
		}
		return response;
	}

	private Response addDataToContentNode(Node node) {
		Number pkgVersion = (Number) node.getMetadata().get("pkgVersion");
		if (null == pkgVersion || pkgVersion.intValue() < 1) {
			pkgVersion = 1.0;
		} else {
			pkgVersion = pkgVersion.doubleValue() + 1;
		}
		node.getMetadata().put("pkgVersion", pkgVersion);
		List<Node> nodes = new ArrayList<Node>();
		nodes.add(node);
		List<Map<String, Object>> ctnts = new ArrayList<Map<String, Object>>();
		List<String> childrenIds = new ArrayList<String>();
		getContentBundleData(node.getGraphId(), nodes, ctnts, childrenIds);
		String bundleFileName = Slug
				.makeSlug((String) node.getMetadata().get(ContentWorkflowPipelineParams.name.name()), true) + "_"
				+ System.currentTimeMillis() + "_" + node.getIdentifier() + "_"
				+ node.getMetadata().get(ContentWorkflowPipelineParams.pkgVersion.name()) + ".ecar";
		ContentBundle contentBundle = new ContentBundle();
		Map<Object, List<String>> downloadUrls = contentBundle.createContentManifestData(ctnts, childrenIds, null, EcarPackageType.FULL);
		String[] urlArray = contentBundle.createContentBundle(ctnts, bundleFileName, "1.1", downloadUrls,
				node,null);
		node.getMetadata().put(ContentAPIParams.s3Key.name(), urlArray[0]);
		node.getMetadata().put("downloadUrl", urlArray[1]);
		node.getMetadata().put("status", "Live");
		node.getMetadata().put(ContentAPIParams.lastPublishedOn.name(), formatCurrentDate());
		node.getMetadata().put(ContentAPIParams.size.name(), getCloudStoredFileSize(urlArray[0]));
		Node newNode = new Node(node.getIdentifier(), node.getNodeType(), node.getObjectType());
		newNode.setGraphId(node.getGraphId());
		newNode.setMetadata(node.getMetadata());
		return updateContentNode(newNode.getIdentifier(), newNode, urlArray[1]);
	}

	protected Double getCloudStoredFileSize(String key) {
		Double bytes = null;
		if (StringUtils.isNotBlank(key)) {
			try {
				return CloudStore.getObjectSize(key);
			} catch (Exception e) {
				TelemetryManager.error("Error: While getting the file size from AWS: " + key, e);
			}
		}
		return bytes;
	}

	private static String formatCurrentDate() {
		return format(new Date());
	}

	private static String format(Date date) {
		if (null != date) {
			try {
				return sdf.format(date);
			} catch (Exception e) {
			}
		}
		return null;
	}

	protected Response updateContentNode(String contentId, Node node, String url) {
		Response updateRes = updateNode(node);
		if (!checkError(updateRes)) {
			if (StringUtils.isNotBlank(url))
				updateRes.put(ContentAPIParams.content_url.name(), url);
			updateRes.put(ContentAPIParams.node_id.name(), contentId);
		}
		return updateRes;
	}

	protected Response updateNode(Node node) {
		Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
		updateReq.put(GraphDACParams.node.name(), node);
		updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
		Response updateRes = getResponse(updateReq);
		return updateRes;
	}

	protected void getContentBundleData(String taxonomyId, List<Node> nodes, List<Map<String, Object>> ctnts,
			List<String> childrenIds) {
		Map<String, Node> nodeMap = new HashMap<String, Node>();
		if (null != nodes && !nodes.isEmpty()) {
			for (Node node : nodes) {
				getContentRecursive(taxonomyId, node, nodeMap, childrenIds, ctnts);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void getContentRecursive(String taxonomyId, Node node, Map<String, Node> nodeMap, List<String> childrenIds,
			List<Map<String, Object>> ctnts) {
		if (!nodeMap.containsKey(node.getIdentifier())) {
			nodeMap.put(node.getIdentifier(), node);
			Map<String, Object> metadata = new HashMap<String, Object>();
			if (null == node.getMetadata())
				node.setMetadata(new HashMap<String, Object>());
			String status = (String) node.getMetadata().get("status");
			if (StringUtils.equalsIgnoreCase("Live", status)) {
				metadata.putAll(node.getMetadata());
				metadata.put("identifier", node.getIdentifier());
				metadata.put("objectType", node.getObjectType());
				metadata.put("subject", node.getGraphId());
				metadata.remove("body");
				metadata.remove("editorState");
				if (null != node.getTags() && !node.getTags().isEmpty())
					metadata.put("keywords", node.getTags());
				List<String> searchIds = new ArrayList<String>();
				if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
					List<NodeDTO> children = new ArrayList<NodeDTO>();
					List<NodeDTO> preRequisites = new ArrayList<NodeDTO>();
					for (Relation rel : node.getOutRelations()) {
						if (StringUtils.equalsIgnoreCase(RelationTypes.SEQUENCE_MEMBERSHIP.relationName(),
								rel.getRelationType())
								&& StringUtils.equalsIgnoreCase(node.getObjectType(), rel.getEndNodeObjectType())) {
							childrenIds.add(rel.getEndNodeId());
							if (!nodeMap.containsKey(rel.getEndNodeId())) {
								searchIds.add(rel.getEndNodeId());
							}
							children.add(new NodeDTO(rel.getEndNodeId(), rel.getEndNodeName(),
									rel.getEndNodeObjectType(), rel.getRelationType(), rel.getMetadata()));
						} else if (StringUtils.equalsIgnoreCase(RelationTypes.PRE_REQUISITE.relationName(),
								rel.getRelationType())
								&& StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.Library.name(),
										rel.getEndNodeObjectType())) {
							childrenIds.add(rel.getEndNodeId());
							if (!nodeMap.containsKey(rel.getEndNodeId())) {
								searchIds.add(rel.getEndNodeId());
							}
							preRequisites.add(new NodeDTO(rel.getEndNodeId(), rel.getEndNodeName(),
									rel.getEndNodeObjectType(), rel.getRelationType(), rel.getMetadata()));
						}
					}
					if (!children.isEmpty()) {
						metadata.put("children", children);
					}
					if (!preRequisites.isEmpty()) {
						metadata.put(ContentWorkflowPipelineParams.pre_requisites.name(), preRequisites);
					}
				}
				ctnts.add(metadata);
				if (!searchIds.isEmpty()) {
					Response searchRes = searchNodes(taxonomyId, searchIds);
					if (checkError(searchRes)) {
						throw new ServerException(ContentErrorCodes.ERR_CONTENT_SEARCH_ERROR.name(),
								getErrorMessage(searchRes));
					} else {
						List<Object> list = (List<Object>) searchRes.get(ContentAPIParams.contents.name());
						if (null != list && !list.isEmpty()) {
							for (Object obj : list) {
								List<Node> nodeList = (List<Node>) obj;
								for (Node child : nodeList) {
									getContentRecursive(taxonomyId, child, nodeMap, childrenIds, ctnts);
								}
							}
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private double getFileSizeInKB(File file) {
		double bytes = 0;
		try {
			bytes = getFileSize(file) / 1024;
		} catch (IOException e) {
			TelemetryManager.error("Error: While Calculating the file size: "+file.getName(), e);
		}
		return bytes;
	}

	protected double getFileSize(File file) throws IOException {
		double bytes = 0;
		if (file.exists()) {
			bytes = file.length();
		}
		return bytes;
	}

	private Response searchNodes(String taxonomyId, List<String> contentIds) {
		ContentSearchCriteria criteria = new ContentSearchCriteria();
		List<Filter> filters = new ArrayList<Filter>();
		Filter filter = new Filter("identifier", SearchConditions.OP_IN, contentIds);
		filters.add(filter);
		MetadataCriterion metadata = MetadataCriterion.create(filters);
		metadata.addFilter(filter);
		criteria.setMetadata(metadata);
		List<Request> requests = new ArrayList<Request>();
		if (StringUtils.isNotBlank(taxonomyId)) {
			Request req = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
					GraphDACParams.search_criteria.name(), criteria.getSearchCriteria());
			req.put(GraphDACParams.get_tags.name(), true);
			requests.add(req);
		} 
//		else {
//			for (String tId : TaxonomyManagerImpl.taxonomyIds) {
//				Request req = getRequest(tId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
//						GraphDACParams.search_criteria.name(), criteria.getSearchCriteria());
//				req.put(GraphDACParams.get_tags.name(), true);
//				requests.add(req);
//			}
//		}
		Response response = getResponse(requests, GraphDACParams.node_list.name(),
				ContentAPIParams.contents.name());
		return response;
	}

	public String[] uploadArtifactToAWS(File uploadedFile, String identifier) {
		String[] urlArray = new String[] {};
		try {
			String folder = S3PropertyReader.getProperty(CONTENT_FOLDER);
			folder = folder + "/" + Slug.makeSlug(identifier, true) + "/" + S3PropertyReader.getProperty(ARTEFACT_FOLDER);
			urlArray = CloudStore.uploadFile(folder, uploadedFile, true);
		} catch (Exception e) {
			TelemetryManager.error("Error while uploading the file.", e);
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_UPLOAD_FILE.name(),
					"Error while uploading the File.", e);
		}
		return urlArray;
	}
	public boolean isS3Url(String url) {
		String bucketName = getBucketName();
		if(url.contains(bucketName)) {
			return true;
		}else {
			return false;
		}
	}
	
	public Response uploadContentArtifact(String contentId, Node node, File uploadedFile, boolean slugFile) {
		String[] urlArray = uploadArtifactToAWS(uploadedFile, contentId);
		node.getMetadata().put("s3Key", urlArray[0]);
		node.getMetadata().put(ContentAPIParams.artifactUrl.name(), urlArray[1]);
		
		ContentPackageExtractionUtil contentPackageExtractionUtil = new ContentPackageExtractionUtil();
		contentPackageExtractionUtil.extractContentPackage(contentId, node, uploadedFile, ExtractionType.snapshot, slugFile);
		
		return updateContentNode(contentId, node, urlArray[1]);
	}

	public String getKeyName(String url) {
		return url.substring(url.lastIndexOf('/') + 1);
	}

	public Number getNumericValue(Object obj) {
		try {
			return (Number) obj;
		} catch (Exception e) {
			return 0;
		}
	}

	public Double getDoubleValue(Object obj) {
		Number n = getNumericValue(obj);
		if (null == n)
			return 0.0;
		return n.doubleValue();
	}

	protected String getBasePath(String contentId) {
		String path = "";
		if (!StringUtils.isBlank(contentId))
			path = tempFileLocation + File.separator + System.currentTimeMillis() + ContentAPIParams._temp.name()
					+ File.separator + contentId;
		return path;
	}

	
	/**
	 * extractContentPackage() 
	 * extracts the ContentPackageZip file
	 * 
	 * @param file the ContentPackageFile
	 */
	protected void extractContentPackage(File file, String basePath) {
		try {
			UnzipUtility util = new UnzipUtility();
			util.unzip(file.getAbsolutePath(), basePath);
		} catch (IOException e) {
			throw new ServerException(ContentErrorCodeConstants.ZIP_EXTRACTION.name(),
					ContentErrorMessageConstants.ZIP_EXTRACTION_ERROR + " | [ZIP Extraction Failed.]");
		}
	}
	
	protected void createZipPackage(String basePath, String zipFileName) {
		if (!StringUtils.isBlank(zipFileName)) {
			TelemetryManager.log("Creating Zip File: " + zipFileName);
			ZipUtility appZip = new ZipUtility(basePath, zipFileName);
			appZip.generateFileList(new File(basePath));
			appZip.zipIt(zipFileName);
		}
	}

	protected void deleteTempDirectory(String contendId) {
			String path = tempFileLocation + contendId;
			try {
				FileUtils.deleteDirectory(new File(path));
			} catch (Exception e) {
				TelemetryManager.error("Unable to delete directory with path: " + path, e);
			}
		}
}
