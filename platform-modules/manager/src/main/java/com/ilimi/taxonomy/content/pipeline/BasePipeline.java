package com.ilimi.taxonomy.content.pipeline;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.util.AWSUploader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.NodeDTO;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
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
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.util.PropertiesUtil;
import com.ilimi.taxonomy.dto.ContentSearchCriteria;
import com.ilimi.taxonomy.enums.ContentAPIParams;
import com.ilimi.taxonomy.mgr.impl.TaxonomyManagerImpl;

/**
 * The Class BasePipeline is a PipeLineClass between initializers and finalizers 
 * mainly holds Common Methods and operations of a ContentNode .
 */
public class BasePipeline extends BaseManager {
	
	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(BasePipeline.class.getName());
	
	/** The SimpleDateformatter. */
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	/** The Constant DEF_AWS_BUCKET_NAME */
	private static final String DEF_AWS_BUCKET_NAME = "ekstep-public";
	
	/** The Constant DEF_AWS_FOLDER_NAME */
	private static final String DEF_AWS_FOLDER_NAME = "content";

	/**
	 * Updates the ContentNode.
	 *
	 * @param Node the node
	 * @param Url the url
	 * @return the response of UpdateContentNode with node_id
	 */
	protected Response updateContentNode(Node node, String url) {
		Response response = new Response();
		if (null != node) {
			response = updateNode(node);
			if (StringUtils.isNotBlank(url))
				response.put(ContentWorkflowPipelineParams.content_url.name(), url);
		}
		return response;
	}
	
	/**
	 * Updates the given Node.
	 * 
	 * @param Node the node
	 * @return the response of UpdateContentNode with node_id
	 */
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
	
	/**
	 * validates BasePath
	 * 
	 * @param path the Path
	 * checks if the path exists else
	 * return false
	 */
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
	
	/**
	 * validates Path
	 * 
	 * @param Path the path
	 * checks if the path exists, if not null then creates a Path for it
	 * @return true if its a validBasePath 
	 * return false
	 */
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
	
	/**
	 * gets the AwsUploadFolder Name
	 * from the PropertiesUtil class by loading from propertiesFile
	 * 
	 * @return AWS Upload FolderName
	 */
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
	
	/**
	 * gets the AwsUploadBucket Name
	 * from the PropertiesUtil class by loading from propertiesFile
	 * 
	 * @return AWS Upload BucketName
	 */
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
	
	/**
	 * uploads the file to AWS
	 * 
	 * @param uploadFile is the file to to uploaded
	 * @param folder is the AWS folder
	 * calls the AWSUploader to upload the file the AWS
	 * @return String[] of the uploaded URL
	 */
	protected String[] uploadToAWS(File uploadFile, String folder) {
		String[] urlArray = new String[] {};
		try {
			if (StringUtils.isBlank(folder))
				folder = DEF_AWS_FOLDER_NAME;
			urlArray = AWSUploader.uploadFile(DEF_AWS_BUCKET_NAME, folder, uploadFile);
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodeConstants.UPLOAD_ERROR.name(),
					ContentErrorMessageConstants.FILE_UPLOAD_ERROR, e);
		}
		return urlArray;
	}
	
	/**
	 * gets the NumericValue of the object
	 * 
	 * @param object
	 * @return Number
	 */
	protected Number getNumericValue(Object obj) {
		try {
			return (Number) obj;
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * gets the DoubleValue of the object
	 * 
	 * @param object
	 * @return NumberDoubleVale
	 */
	protected Double getDoubleValue(Object obj) {
		Number n = getNumericValue(obj);
		if (null == n)
			return 0.0;
		return n.doubleValue();
	}
	
	/**
	 * gets the S3FilesSize
	 * 
	 * @param key
	 * @return fileSize(double)
	 */
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
	
	/**
	 * formats Any given Date
	 * 
	 * @param date
	 * @return NumberDoubleVale
	 */
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
	
	/**
	 * gets the date after the given Days
	 * 
	 * @param number of Days
	 * @return date
	 */
	protected static String getDateAfter(int days) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, days);
		return sdf.format(c.getTime());
	}

	/**
	 * validates the XML from the ContentBody
	 * 
	 * @param ContentBody
	 * @return true if validation is successful else false
	 */
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
	
	/**
	 * validates the JSON from ContentBody
	 * 
	 * @param ContentBody
	 * @return true if validation is successful else false
	 */
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
	
	/**
	 * gets the basePath from the ContentId
	 * 
	 * @param ContentId
	 * @return BasePath
	 */
	protected String getBasePath(String contentId) {
		String path = "";
		if (!StringUtils.isBlank(contentId))
			path = ContentConfigurationConstants.CONTENT_BASE_PATH + File.separator + System.currentTimeMillis()
					+ ContentAPIParams._temp.name() + File.separator + contentId;
		return path;
	}
	
	/**
	 * gets the ResponseTimestamp
	 * 
	 * @return TimeStamp
	 */
	protected String getResponseTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'XXX");
		return sdf.format(new Date());
	}
	
	/**
	 * gets the UUID(unique Identifier)
	 * @return UUID(random generator)
	 */
	protected String getUUID() {
		UUID uid = UUID.randomUUID();
		return uid.toString();
	}
	
	/**
	 * gets the ContentBundleData form all Collections
	 * 
	 * @param graphId, listOfNodes to be bundled, contents, childrenIds and Status(live)
	 * call getContentBundleData() to Bundle all data with status as LIVE
	 */
	protected void getContentBundleData(String graphId, List<Node> nodes, List<Map<String, Object>> ctnts,
			List<String> childrenIds) {
		getContentBundleData(graphId, nodes, ctnts, childrenIds, true);
	}

	protected void getContentBundleData(String graphId, List<Node> nodes, List<Map<String, Object>> ctnts,
			List<String> childrenIds, boolean onlyLive) {
		Map<String, Node> nodeMap = new HashMap<String, Node>();
		if (null != nodes && !nodes.isEmpty()) {
			LOGGER.info("Starting Data Collection For Bundling...");
			List<Node> childrenNodes = new ArrayList<Node>();
			for (Node node : nodes) {
				LOGGER.info("Collecting Hierarchical Bundling Data For Content Id: " + node.getIdentifier());
				getContentRecursive(graphId, childrenNodes, node, nodeMap, childrenIds, ctnts, onlyLive);
			}
			nodes.addAll(childrenNodes);
		}
	}

	@SuppressWarnings("unchecked")
	protected void getContentRecursive(String graphId, List<Node> childrenNodes, Node node, Map<String, Node> nodeMap, List<String> childrenIds,
			List<Map<String, Object>> ctnts, boolean onlyLive) {
		if (!nodeMap.containsKey(node.getIdentifier())) {
			nodeMap.put(node.getIdentifier(), node);
			Map<String, Object> metadata = new HashMap<String, Object>();
			if (null == node.getMetadata())
				node.setMetadata(new HashMap<String, Object>());
			String status = (String) node.getMetadata().get(ContentWorkflowPipelineParams.status.name());
			if ((onlyLive && StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.Live.name(), status)) || !onlyLive) {
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
								&& StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.Library.name(), rel.getEndNodeObjectType())) {
							childrenIds.add(rel.getEndNodeId());
							preRequisites.add(new NodeDTO(rel.getEndNodeId(), rel.getEndNodeName(), 
									rel.getEndNodeObjectType(), rel.getRelationType(), rel.getMetadata()));
						}
					}
					if (!children.isEmpty())
						metadata.put(ContentWorkflowPipelineParams.children.name(), children);
					if (!preRequisites.isEmpty())
						metadata.put(ContentWorkflowPipelineParams.pre_requisites.name(), preRequisites);
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
									childrenNodes.add(child);
									getContentRecursive(graphId, childrenNodes, child, nodeMap, childrenIds, ctnts, onlyLive);
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * search Nodes and return SearchResponse
	 * 
	 * @param taxonomyId and ContentId
	 * @return Response of the search
	 */
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
