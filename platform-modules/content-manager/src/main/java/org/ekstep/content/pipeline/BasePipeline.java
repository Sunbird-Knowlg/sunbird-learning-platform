package org.ekstep.content.pipeline;

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

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.slugs.Slug;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.content.common.ContentConfigurationConstants;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.contentstore.util.ContentStoreOperations;
import org.ekstep.contentstore.util.ContentStoreParams;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.LearningActorNames;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.NodeDTO;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.router.RequestRouterPool;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import org.ekstep.content.dto.ContentSearchCriteria;
import com.rits.cloning.Cloner;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

/**
 * The Class BasePipeline is a PipeLineClass between initializers and finalizers
 * mainly holds Common Methods and operations of a ContentNode .
 */
public class BasePipeline extends BaseManager {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(BasePipeline.class.getName());

	/** The SimpleDateformatter. */
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	/** The Constant DEF_AWS_FOLDER_NAME */
	private static final String DEF_AWS_FOLDER_NAME = "content";

	private static final String s3Content = "s3.content.folder";;

	/**
	 * Updates the ContentNode.
	 *
	 * @param Node
	 *            the node
	 * @param Url
	 *            the url
	 * @return the response of UpdateContentNode with node_id
	 */
	protected Response updateContentNode(Node node, String url) {
		Response response = new Response();
		if (null != node) {
			String contentId = node.getIdentifier();
			node.setIdentifier(getContentObjectIdentifier(node));
			response = updateNode(node);
			if (StringUtils.isNotBlank(url))
				response.put(ContentWorkflowPipelineParams.content_url.name(), url);
			response.put(ContentWorkflowPipelineParams.node_id.name(), contentId);
		}
		
		return response;
	}

	/**
	 * Updates the given Node.
	 * 
	 * @param Node
	 *            the node
	 * @return the response of UpdateContentNode with node_id
	 */
	protected Response updateNode(Node node) {
		Response response = new Response();
		if (null != node) {
			Cloner cloner = new Cloner();
			Node clonedNode = cloner.deepClone(node);
			Request updateReq = getRequest(clonedNode.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
			updateReq.put(GraphDACParams.node.name(), clonedNode);
			updateReq.put(GraphDACParams.node_id.name(), clonedNode.getIdentifier());
			response = getResponse(updateReq, LOGGER);
		}
		return response;
	}

	/**
	 * Updates the content body in content store
	 * 
	 * @param contentId
	 *            identifier of the content
	 * @param body
	 *            ECML body of the content
	 * @return response of updatedContentBody request
	 */
	protected Response updateContentBody(String contentId, String body) {
		Request request = new Request();
		request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
		request.setOperation(ContentStoreOperations.updateContentBody.name());
		request.put(ContentStoreParams.content_id.name(), contentId);
		request.put(ContentStoreParams.body.name(), body);
		return makeLearningRequest(request, LOGGER);
	}

	/**
	 * Make a sync request to LearningRequestRouter
	 * 
	 * @param request
	 *            the request object
	 * @param logger
	 *            the logger object
	 * @return the LearningActor response
	 */
	protected Response makeLearningRequest(Request request, Logger logger) {
		ActorRef router = LearningRequestRouterPool.getRequestRouter();
		try {
			Future<Object> future = Patterns.ask(router, request, RequestRouterPool.REQ_TIMEOUT);
			Object obj = Await.result(future, RequestRouterPool.WAIT_TIMEOUT.duration());
			if (obj instanceof Response) {
				Response response = (Response) obj;
				logger.info("Response Params: " + response.getParams() + " | Code: " + response.getResponseCode()
						+ " | Result: " + response.getResult().keySet());
				return response;
			} else {
				return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
		}
	}

	/**
	 * validates BasePath
	 * 
	 * @param path
	 *            the Path checks if the path exists else return false
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
	 * @param Path
	 *            the path checks if the path exists, if not null then creates a
	 *            Path for it
	 * @return true if its a validBasePath return false
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
	 * gets the AwsUploadFolder Name from the PropertiesUtil class by loading
	 * from propertiesFile
	 * 
	 * @return AWS Upload FolderName
	 */
	protected String getUploadFolderName(String identifier, String folder) {
		String folderName = DEF_AWS_FOLDER_NAME;
		// String env =
		// PropertiesUtil.getProperty(ContentWorkflowPipelineParams.OPERATION_MODE.name());
		folderName = S3PropertyReader.getProperty(s3Content);
		if (!StringUtils.isBlank(folderName)) {
			folderName = folderName + "/" + Slug.makeSlug(identifier, true) + "/" + folder;
		}
		return folderName;
	}

	/**
	 * uploads the file to AWS
	 * 
	 * @param uploadFile
	 *            is the file to to uploaded
	 * @param folder
	 *            is the AWS folder calls the AWSUploader to upload the file the
	 *            AWS
	 * @return String[] of the uploaded URL
	 */
	protected String[] uploadToAWS(File uploadFile, String folder) {
		String[] urlArray = new String[] {};
		try {
			if (StringUtils.isBlank(folder))
				folder = DEF_AWS_FOLDER_NAME;
			urlArray = AWSUploader.uploadFile(folder, uploadFile);
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
				return AWSUploader.getObjectSize(key);
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
	 * @param number
	 *            of Days
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
					+ ContentWorkflowPipelineParams._temp.name() + File.separator + contentId;
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
	 * 
	 * @return UUID(random generator)
	 */
	protected String getUUID() {
		UUID uid = UUID.randomUUID();
		return uid.toString();
	}

	/**
	 * gets the ContentBundleData form all Collections
	 * 
	 * @param graphId,
	 *            listOfNodes to be bundled, contents, childrenIds and
	 *            Status(live) call getContentBundleData() to Bundle all data
	 *            with status as LIVE
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
	protected void getContentRecursive(String graphId, List<Node> childrenNodes, Node node, Map<String, Node> nodeMap,
			List<String> childrenIds, List<Map<String, Object>> ctnts, boolean onlyLive) {
		if (!nodeMap.containsKey(node.getIdentifier())) {
			nodeMap.put(node.getIdentifier(), node);
			Map<String, Object> metadata = new HashMap<String, Object>();
			if (null == node.getMetadata())
				node.setMetadata(new HashMap<String, Object>());
			String status = (String) node.getMetadata().get(ContentWorkflowPipelineParams.status.name());
			if ((onlyLive && StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.Live.name(), status))
					|| !onlyLive) {
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
								&& StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.Library.name(),
										rel.getEndNodeObjectType())) {
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
									String body = getContentBody(child.getIdentifier());
									child.getMetadata().put(ContentWorkflowPipelineParams.body.name(), body);
									childrenNodes.add(child);
									getContentRecursive(graphId, childrenNodes, child, nodeMap, childrenIds, ctnts,
											onlyLive);
								}
							}
						}
					}
				}
			}
		}
	}
	
	protected String getContentBody(String contentId) {
		Request request = new Request();
		request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
		request.setOperation(ContentStoreOperations.getContentBody.name());
		request.put(ContentStoreParams.content_id.name(), contentId);
		Response response = makeLearningRequest(request, LOGGER);
		String body = (String) response.get(ContentStoreParams.body.name());
		return body;
	}

	/**
	 * search Nodes and return SearchResponse
	 * 
	 * @param taxonomyId
	 *            and ContentId
	 * @return Response of the search
	 */
	protected Response searchNodes(String taxonomyId, List<String> contentIds) {
		LOGGER.info("Searching Nodes For Bundling...");
		ContentSearchCriteria criteria = new ContentSearchCriteria();
		List<Filter> filters = new ArrayList<Filter>();
		Filter filter = new Filter(ContentWorkflowPipelineParams.identifier.name(), SearchConditions.OP_IN, contentIds);
		filters.add(filter);
		
		List<String> status = new ArrayList<String>();
		status.add(ContentWorkflowPipelineParams.Draft.name());
		status.add(ContentWorkflowPipelineParams.Live.name());
		status.add(ContentWorkflowPipelineParams.Review.name());
		status.add(ContentWorkflowPipelineParams.Processing.name());
		Filter statusFilter = new Filter(ContentWorkflowPipelineParams.status.name(), SearchConditions.OP_IN, status);
		filters.add(statusFilter);
		
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
		} 
//		else {
//			for (String tId : TaxonomyManagerImpl.taxonomyIds) {
//				Request req = getRequest(tId, GraphEngineManagers.SEARCH_MANAGER,
//						ContentWorkflowPipelineParams.searchNodes.name(), GraphDACParams.search_criteria.name(),
//						criteria.getSearchCriteria());
//				req.put(GraphDACParams.get_tags.name(), true);
//				requests.add(req);
//			}
//		}
		Response response = getResponse(requests, LOGGER, GraphDACParams.node_list.name(),
				ContentWorkflowPipelineParams.contents.name());
		return response;
	}
	
	protected String getContentObjectIdentifier(Node node) {
		String identifier = "";
		if (null != node) {
			identifier = node.getIdentifier();
			if (BooleanUtils.isTrue((Boolean) node.getMetadata().get(ContentWorkflowPipelineParams.isImageObject.name())))
				identifier += ContentConfigurationConstants.DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX; 
		}
		return identifier;
	}
	
	/**
	 * Recursively deletes the give file/folder
	 * 
	 * @param file the file to be deleted
	 * @throws IOException when there is a file I/O error
	 */
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
}
