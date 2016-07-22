package com.ilimi.taxonomy.content.pipeline;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.optimizr.ThumbnailGenerator;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.common.util.ZipUtility;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.NodeDTO;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.taxonomy.content.common.ContentConfigurationConstants;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.util.ECRFToJSONConvertor;
import com.ilimi.taxonomy.content.util.ECRFToXMLConvertor;
import com.ilimi.taxonomy.content.util.JSONContentParser;
import com.ilimi.taxonomy.content.util.PropertiesUtil;
import com.ilimi.taxonomy.content.util.XMLContentParser;
import com.ilimi.taxonomy.dto.ContentSearchCriteria;
import com.ilimi.taxonomy.enums.ContentAPIParams;
import com.ilimi.taxonomy.mgr.impl.TaxonomyManagerImpl;

public class BasePipeline extends BaseManager {

	private static Logger LOGGER = LogManager.getLogger(BasePipeline.class.getName());

	private ObjectMapper mapper = new ObjectMapper();

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	private static final int IDX_S3_URL = 1;

	private static final String DEF_AWS_BUCKET_NAME = "ekstep-public";
	private static final String DEF_AWS_FOLDER_NAME = "content";

	protected Response updateContentNode(Node node, String url) {
		Response response = new Response();
		if (null != node) {
			response = updateNode(node);
			if (StringUtils.isNotBlank(url))
				response.put(ContentWorkflowPipelineParams.content_url.name(), url);
		}
		return response;
	}

	protected Response updateNode(Node node) {
		Response response = new Response();
		if (null != node) {
			Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
			updateReq.put(GraphDACParams.node.name(), node);
			updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
			response = getResponse(updateReq, LOGGER);
		}
		return response;
	}

	protected boolean isValidBasePath(String path) {
		boolean isValid = true;
		try {
			LOGGER.info("Validating the Base Path: " + path);
			isValid = isPathExist(Paths.get(path));
		} catch (InvalidPathException | NullPointerException e) {
			isValid = false;
		}
		return isValid;
	}

	protected boolean isPathExist(Path path) {
		boolean exist = true;
		try {
			if (null != path) {
				LOGGER.info("Creating the Base Path: " + path.getFileName());
				if (!Files.exists(path))
					Files.createDirectories(path);
			}
		} catch (FileAlreadyExistsException e) {
			LOGGER.info("Base Path Already Exist: " + path.getFileName());
		} catch (Exception e) {
			exist = false;
			LOGGER.error("Error! Something went wrong while creating the path - " + path.getFileName(), e);
		}
		return exist;
	}

	protected String getUploadFolderName() {
		String folderName = DEF_AWS_FOLDER_NAME;
		String env = PropertiesUtil.getProperty(ContentWorkflowPipelineParams.OPERATION_MODE.name());
		if (!StringUtils.isBlank(env)) {
			LOGGER.info("Fetching the Upload Folder (AWS) Name for Environment: " + env);
			// TODO: Write the logic for fetching the environment(DEV, PROD, QA,
			// TEST) aware folder name.
		}
		return folderName;
	}

	protected String getUploadBucketName() {
		String folderName = DEF_AWS_BUCKET_NAME;
		String env = PropertiesUtil.getProperty(ContentWorkflowPipelineParams.OPERATION_MODE.name());
		if (!StringUtils.isBlank(env)) {
			LOGGER.info("Fetching the Upload Bucket (AWS) Name for Environment: " + env);
			// TODO: Write the logic for fetching the environment(DEV, PROD, QA,
			// TEST) aware bucket name.
		}
		return folderName;
	}

	protected String[] uploadToAWS(File uploadedFile, String folder) {
		String[] urlArray = new String[] {};
		try {
			if (StringUtils.isBlank(folder))
				folder = DEF_AWS_FOLDER_NAME;
			urlArray = AWSUploader.uploadFile(DEF_AWS_BUCKET_NAME, folder, uploadedFile);
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodeConstants.UPLOAD_ERROR.name(),
					ContentErrorMessageConstants.FILE_UPLOAD_ERROR, e);
		}
		return urlArray;
	}

	protected Number getNumericValue(Object obj) {
		try {
			return (Number) obj;
		} catch (Exception e) {
			return 0;
		}
	}

	protected Double getDoubleValue(Object obj) {
		Number n = getNumericValue(obj);
		if (null == n)
			return 0.0;
		return n.doubleValue();
	}

	protected Double getS3FileSize(String key) {
		Double bytes = null;
		if (StringUtils.isNotBlank(key)) {
			try {
				return AWSUploader.getObjectSize(ContentConfigurationConstants.BUCKET_NAME, key);
			} catch (IOException e) {
				LOGGER.error("Error! While getting the file size from AWS", e);
			}
		}
		return bytes;
	}

	protected static String formatCurrentDate() {
		return format(new Date());
	}

	protected static String format(Date date) {
		if (null != date) {
			try {
				return sdf.format(date);
			} catch (Exception e) {
				LOGGER.error("Error! While Converting the Date Format.", e);
			}
		}
		return null;
	}

	protected boolean isCompressRequired(Node node) {
		boolean required = false;
		if (null != node && null != node.getMetadata()) {
			LOGGER.info("Compression Required Check For Content Id: " + node.getIdentifier());
			String contentBody = (String) node.getMetadata().get(ContentWorkflowPipelineParams.body.name());
			String artifactUrl = (String) node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name());
			if (StringUtils.isBlank(artifactUrl) && StringUtils.isNotBlank(contentBody))
				required = true;
		}
		return required;
	}

	protected Plugin getECRFObject(String contentBody) {
		Plugin plugin = new Plugin();
		String ecml = contentBody;
		String ecmlType = getECMLType(contentBody);
		if (StringUtils.equalsIgnoreCase(ecmlType, ContentWorkflowPipelineParams.ecml.name())) {
			XMLContentParser parser = new XMLContentParser();
			plugin = parser.parseContent(ecml);
		} else if (StringUtils.equalsIgnoreCase(ecmlType, ContentWorkflowPipelineParams.json.name())) {
			JSONContentParser parser = new JSONContentParser();
			plugin = parser.parseContent(ecml);
		}
		return plugin;
	}

	protected String getECMLType(String contentBody) {
		String type = "";
		if (!StringUtils.isBlank(contentBody)) {
			if (isValidJSON(contentBody))
				type = ContentWorkflowPipelineParams.json.name();
			else if (isValidXML(contentBody))
				type = ContentWorkflowPipelineParams.ecml.name();
			else
				throw new ClientException(ContentErrorCodeConstants.INVALID_BODY.name(),
						ContentErrorMessageConstants.INVALID_CONTENT_BODY);
			LOGGER.info("ECML Type: " + type);
		}
		return type;
	}

	protected boolean isValidXML(String contentBody) {
		boolean isValid = true;
		if (!StringUtils.isBlank(contentBody)) {
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				dBuilder.parse(new InputSource(new StringReader(contentBody)));
			} catch (ParserConfigurationException | SAXException | IOException e) {
				isValid = false;
			}
		}
		return isValid;
	}

	protected boolean isValidJSON(String contentBody) {
		boolean isValid = true;
		if (!StringUtils.isBlank(contentBody)) {
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
				objectMapper.readTree(contentBody);
			} catch (IOException e) {
				isValid = false;
			}
		}
		return isValid;
	}

	protected String getBasePath(String contentId) {
		String path = "";
		if (!StringUtils.isBlank(contentId))
			path = ContentConfigurationConstants.CONTENT_BASE_PATH + File.separator + System.currentTimeMillis()
					+ ContentAPIParams._temp.name() + File.separator + contentId;
		return path;
	}

	protected void createZipPackage(String basePath, String zipFileName) {
		if (!StringUtils.isBlank(zipFileName)) {
			LOGGER.info("Creating Zip File: " + zipFileName);
			ZipUtility appZip = new ZipUtility(basePath, zipFileName);
			appZip.generateFileList(new File(basePath));
			appZip.zipIt(zipFileName);
		}
	}

	protected void writeECMLFile(String basePath, String ecml, String ecmlType) {
		try {
			if (StringUtils.isBlank(ecml))
				throw new ClientException(ContentErrorCodeConstants.EMPTY_ECML.name(),
						ContentErrorMessageConstants.EMPTY_ECML_STRING + " | [Unable to write Empty ECML File.]");
			if (StringUtils.isBlank(ecmlType))
				throw new ClientException(ContentErrorCodeConstants.INVALID_ECML_TYPE.name(),
						ContentErrorMessageConstants.INVALID_ECML_TYPE
								+ " | [System is in a fix between (XML & JSON) ECML Type.]");

			LOGGER.info("ECML File Type: " + ecmlType);
			File file = new File(basePath + File.separator + ContentConfigurationConstants.DEFAULT_ECML_FILE_NAME
					+ ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR + ecmlType);
			LOGGER.info("Creating ECML File With Name: " + file.getAbsolutePath());
			FileUtils.writeStringToFile(file, ecml);
		} catch (IOException e) {
			throw new ServerException(ContentErrorCodeConstants.ECML_FILE_WRITE.name(),
					ContentErrorMessageConstants.ECML_FILE_WRITE_ERROR + " | [Unable to Write ECML File.]");
		}
	}

	protected String getECMLString(Plugin ecrf, String ecmlType) {
		String ecml = "";
		if (null != ecrf) {
			LOGGER.info("Converting ECML From ECRF Object.");
			if (StringUtils.equalsIgnoreCase(ecmlType, ContentWorkflowPipelineParams.ecml.name())) {
				ECRFToXMLConvertor convertor = new ECRFToXMLConvertor();
				ecml = convertor.getContentXmlString(ecrf);
			} else if (StringUtils.equalsIgnoreCase(ecmlType, ContentWorkflowPipelineParams.json.name())) {
				ECRFToJSONConvertor convertor = new ECRFToJSONConvertor();
				ecml = convertor.getContentJsonString(ecrf);
			}
		}
		return ecml;
	}

	protected void createThumbnail(String basePath, Node node) {
		try {
			if (null != node) {
				String appIcon = (String) node.getMetadata().get(ContentWorkflowPipelineParams.appIcon.name());
				if (!StringUtils.isBlank(appIcon)) {
					LOGGER.info("Content Id: " + node.getIdentifier() + " | App Icon: " + appIcon);
					File appIconFile = HttpDownloadUtility.downloadFile(appIcon, basePath);
					if (null != appIconFile && appIconFile.exists() && appIconFile.isFile()) {
						boolean generated = ThumbnailGenerator.generate(appIconFile);
						if (generated) {
							String thumbnail = appIconFile.getParent() + File.separator
									+ FilenameUtils.getBaseName(appIconFile.getPath()) + ".thumb."
									+ FilenameUtils.getExtension(appIconFile.getPath());
							File thumbFile = new File(thumbnail);
							if (thumbFile.exists()) {
								LOGGER.info("Thumbnail created for Content Id: " + node.getIdentifier());
								String[] urlArray = uploadToAWS(thumbFile, getUploadFolderName());
								if (null != urlArray && urlArray.length >= 2) {
									String thumbUrl = urlArray[IDX_S3_URL];
									node.getMetadata().put(ContentWorkflowPipelineParams.appIcon.name(), thumbUrl);
									node.getMetadata().put(ContentWorkflowPipelineParams.posterImage.name(), appIcon);
								}
								try {
									thumbFile.delete();
									LOGGER.info("Deleted local Thumbnail file");
								} catch (Exception e) {
									LOGGER.info("Error! While deleting the Thumbnail File.", e);
								}
							}
						}
						try {
							appIconFile.delete();
							LOGGER.info("Deleted local AppIcon file");
						} catch (Exception e) {
							LOGGER.info("Error! While deleting the App Icon File.", e);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodeConstants.DOWNLOAD_ERROR.name(),
					ContentErrorMessageConstants.APP_ICON_DOWNLOAD_ERROR
							+ " | [Unable to Download App Icon for Content Id: '" + node.getIdentifier() + "' ]",
					e);
		}
	}

	protected void createManifestFile(File manifestFileName, String manifestVersion,
			List<Map<String, Object>> contents) {
		try {
			if (null == contents || contents.isEmpty())
				throw new ClientException(ContentErrorCodeConstants.MANIFEST_FILE_WRITE.name(),
						ContentErrorMessageConstants.MANIFEST_FILE_WRITE_ERROR
								+ " | [Content List is 'null' or Empty.]");
			if (StringUtils.isBlank(manifestVersion))
				manifestVersion = "1.0";

			LOGGER.info("Manifest Header Version: " + manifestVersion);

			String header = "{ \"id\": \"ekstep.content.archive\", \"ver\": \"" + manifestVersion + "\", \"ts\": \""
					+ getResponseTimestamp() + "\", \"params\": { \"resmsgid\": \"" + getUUID()
					+ "\"}, \"archive\": { \"count\": " + contents.size() + ", \"ttl\": 24, \"items\": ";

			LOGGER.info("Content Items in Manifest JSON: " + contents.size());

			// Convert to JSON String
			String manifestJSON = header + mapper.writeValueAsString(contents) + "}}";
			LOGGER.info("Manifest JSON: " + manifestJSON);

			FileUtils.writeStringToFile(manifestFileName, manifestJSON);
			LOGGER.info("Manifest JSON Written");
		} catch (IOException e) {
			throw new ServerException(ContentErrorCodeConstants.MANIFEST_FILE_WRITE.name(),
					ContentErrorMessageConstants.MANIFEST_FILE_WRITE_ERROR + " | [Unable to Write Manifest File.]", e);
		}
	}

	protected byte[] createECAR(List<File> files) throws IOException {
		// creating byteArray stream, make it bufferable and passing this buffer
		// to ZipOutputStream
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
		ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);
		// packing files
		for (File file : files) {
			if (null != file) {
				String fileName = null;
				if (file.getName().toLowerCase().endsWith("manifest.json")) {
					fileName = file.getName();
				} else {
					fileName = file.getParent().substring(file.getParent().lastIndexOf(File.separator) + 1)
							+ File.separator + file.getName();
				}
				// new zip entry and copying inputstream with file to
				// zipOutputStream, after all closing streams
				zipOutputStream.putNextEntry(new ZipEntry(fileName));
				FileInputStream fileInputStream = new FileInputStream(file);

				IOUtils.copy(fileInputStream, zipOutputStream);

				fileInputStream.close();
				zipOutputStream.closeEntry();
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

	protected File createBundle(List<File> files, String bundleFileName) {
		File bundleFile = new File(bundleFileName);
		try {
			if (null == files || files.isEmpty())
				throw new ClientException(ContentErrorCodeConstants.BUNDLE_FILE_WRITE.name(),
						ContentErrorMessageConstants.NO_FILES_TO_BUNDLE + " | [Atleast one file is needed to bundle.]");
			if (StringUtils.isBlank(bundleFileName))
				throw new ClientException(ContentErrorCodeConstants.BUNDLE_FILE_WRITE.name(),
						ContentErrorMessageConstants.INVALID_BUNDLE_FILE_NAME + " | [Bundle File Name is Required.]");
			FileOutputStream stream = new FileOutputStream(bundleFileName);
			stream.write(createECAR(files));
			stream.close();
		} catch (Throwable e) {
			throw new ServerException(ContentErrorCodeConstants.BUNDLE_FILE_WRITE.name(),
					ContentErrorMessageConstants.BUNDLE_FILE_WRITE_ERROR + " | [Unable to Bundle File.]", e);
		}
		return bundleFile;
	}

	protected String getResponseTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'XXX");
		return sdf.format(new Date());
	}

	protected String getUUID() {
		UUID uid = UUID.randomUUID();
		return uid.toString();
	}

	protected void getContentBundleData(String graphId, List<Node> nodes, List<Map<String, Object>> ctnts,
			List<String> childrenIds) {
		Map<String, Node> nodeMap = new HashMap<String, Node>();
		if (null != nodes && !nodes.isEmpty()) {
			LOGGER.info("Starting Data Collection For Bundling...");
			for (Node node : nodes) {
				LOGGER.info("Collecting Hierarchical Bundling Data For Content Id: " + node.getIdentifier());
				getContentRecursive(graphId, node, nodeMap, childrenIds, ctnts);
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void getContentRecursive(String graphId, Node node, Map<String, Node> nodeMap, List<String> childrenIds,
			List<Map<String, Object>> ctnts) {
		if (!nodeMap.containsKey(node.getIdentifier())) {
			nodeMap.put(node.getIdentifier(), node);
			Map<String, Object> metadata = new HashMap<String, Object>();
			if (null == node.getMetadata())
				node.setMetadata(new HashMap<String, Object>());
			String status = (String) node.getMetadata().get("status");
			if (StringUtils.equalsIgnoreCase("Live", status)) {
				metadata.putAll(node.getMetadata());
				metadata.put(ContentWorkflowPipelineParams.identifier.name(), node.getIdentifier());
				metadata.put(ContentWorkflowPipelineParams.objectType.name(), node.getObjectType());
				metadata.put(ContentWorkflowPipelineParams.subject.name(), node.getGraphId());
				metadata.remove(ContentWorkflowPipelineParams.body.name());
				metadata.remove(ContentWorkflowPipelineParams.editorState.name());
				if (null != node.getTags() && !node.getTags().isEmpty())
					metadata.put(ContentWorkflowPipelineParams.tags.name(), node.getTags());
				List<String> searchIds = new ArrayList<String>();
				if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
					List<NodeDTO> children = new ArrayList<NodeDTO>();
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
						}
					}
					if (!children.isEmpty()) {
						metadata.put(ContentWorkflowPipelineParams.children.name(), children);
					}
				}
				ctnts.add(metadata);
				if (!searchIds.isEmpty()) {
					Response searchRes = searchNodes(graphId, searchIds);
					if (checkError(searchRes)) {
						throw new ServerException(ContentErrorCodeConstants.SEARCH_ERROR.name(),
								getErrorMessage(searchRes));
					} else {
						List<Object> list = (List<Object>) searchRes.get(ContentWorkflowPipelineParams.contents.name());
						if (null != list && !list.isEmpty()) {
							for (Object obj : list) {
								List<Node> nodeList = (List<Node>) obj;
								for (Node child : nodeList) {
									getContentRecursive(graphId, child, nodeMap, childrenIds, ctnts);
								}
							}
						}
					}
				}
			}
		}
	}

	protected Response searchNodes(String taxonomyId, List<String> contentIds) {
		LOGGER.info("Searching Nodes For Bundling...");
		ContentSearchCriteria criteria = new ContentSearchCriteria();
		List<Filter> filters = new ArrayList<Filter>();
		Filter filter = new Filter(ContentWorkflowPipelineParams.identifier.name(), SearchConditions.OP_IN, contentIds);
		filters.add(filter);
		MetadataCriterion metadata = MetadataCriterion.create(filters);
		metadata.addFilter(filter);
		criteria.setMetadata(metadata);
		List<Request> requests = new ArrayList<Request>();
		if (StringUtils.isNotBlank(taxonomyId)) {
			Request req = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER,
					ContentWorkflowPipelineParams.searchNodes.name(), GraphDACParams.search_criteria.name(),
					criteria.getSearchCriteria());
			req.put(GraphDACParams.get_tags.name(), true);
			requests.add(req);
		} else {
			for (String tId : TaxonomyManagerImpl.taxonomyIds) {
				Request req = getRequest(tId, GraphEngineManagers.SEARCH_MANAGER,
						ContentWorkflowPipelineParams.searchNodes.name(), GraphDACParams.search_criteria.name(),
						criteria.getSearchCriteria());
				req.put(GraphDACParams.get_tags.name(), true);
				requests.add(req);
			}
		}
		Response response = getResponse(requests, LOGGER, GraphDACParams.node_list.name(),
				ContentWorkflowPipelineParams.contents.name());
		return response;
	}
}
