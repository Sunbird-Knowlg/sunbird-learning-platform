package org.sunbird.content.pipeline;

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
import org.sunbird.common.Platform;
import org.sunbird.common.Slug;
import org.sunbird.common.dto.NodeDTO;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.enums.TaxonomyErrorCodes;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.mgr.BaseManager;
import org.sunbird.common.router.RequestRouterPool;
import org.sunbird.common.util.S3PropertyReader;
import org.sunbird.content.common.ContentConfigurationConstants;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.dto.ContentSearchCriteria;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.RelationTypes;
import org.sunbird.graph.dac.model.Filter;
import org.sunbird.graph.dac.model.MetadataCriterion;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.dac.model.SearchConditions;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.sunbird.learning.common.enums.LearningActorNames;
import org.sunbird.learning.contentstore.ContentStoreOperations;
import org.sunbird.learning.contentstore.ContentStoreParams;
import org.sunbird.learning.router.LearningRequestRouterPool;
import org.sunbird.learning.util.CloudStore;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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

	/** The SimpleDateformatter. */
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	/** The Constant DEF_AWS_FOLDER_NAME */
	private static final String DEF_AWS_FOLDER_NAME = "content";

	private static final String CONTEN_FOLDER = "cloud_storage.content.folder";

	private static final String ECML_MIMETYPE = "application/vnd.ekstep.ecml-archive";


	/**
	 * Updates the ContentNode.
	 *
	 * @param node
	 *            the node
	 * @param url
	 *            the url
	 * @return the response of UpdateContentNode with node_id
	 */
	protected Response updateContentNode(String contentId, Node node, String url) {
		Response response = new Response();
		if (null != node) {
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
	 * @param node
	 *            the node
	 * @return the response of UpdateContentNode with node_id
	 */
	protected Response updateNode(Node node, Boolean isSkipValid) {
		Response response = new Response();
		if (null != node) {
			Cloner cloner = new Cloner();
			Node clonedNode = cloner.deepClone(node);
			Request updateReq = getRequest(clonedNode.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
			if(null != clonedNode.getMetadata().get("channel"))
				updateReq.getContext().put(GraphDACParams.CHANNEL_ID.name(), (String)clonedNode.getMetadata().get("channel"));
			updateReq.put(GraphDACParams.node.name(), clonedNode);
			updateReq.put(GraphDACParams.skip_validations.name(), isSkipValid);

			updateReq.put(GraphDACParams.node_id.name(), clonedNode.getIdentifier());
			response = getResponse(updateReq);
		}
		return response;
	}
	
	protected Response updateNode(Node node) {
		return updateNode(node, false);
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
		TelemetryManager.info("Update Content Body For Content Id: " + contentId + ".");
		Request request = new Request();
		request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
		request.setOperation(ContentStoreOperations.updateContentBody.name());
		request.put(ContentStoreParams.content_id.name(), contentId);
		request.put(ContentStoreParams.body.name(), body);
		return makeLearningRequest(request);
	}

	/**
	 * Cassandra call to fetch hierarchy data
	 *
	 * @param contentId
	 * @return
	 */
	public Response getCollectionHierarchy(String contentId) {
		Request request = new Request();
		request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
		request.setOperation(ContentStoreOperations.getCollectionHierarchy.name());
		request.put(ContentStoreParams.content_id.name(), contentId);
		Response response = getResponse(request, LearningRequestRouterPool.getRequestRouter());
		return response;
	}


	/**
	 * Make a sync request to LearningRequestRouter
	 * 
	 * @param request
	 *            the request object
	 * @return the LearningActor response
	 */
	protected Response makeLearningRequest(Request request) {
		ActorRef router = LearningRequestRouterPool.getRequestRouter();
		try {
			Future<Object> future = Patterns.ask(router, request, RequestRouterPool.REQ_TIMEOUT);
			Object obj = Await.result(future, RequestRouterPool.WAIT_TIMEOUT.duration());
			if (obj instanceof Response) {
				Response response = (Response) obj;
				TelemetryManager.log("Response Params: " + response.getParams() + " | Code: " + response.getResponseCode()
						+ " | Result: " + response.getResult().keySet());
				return response;
			} else {
				return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
			}
		} catch (Exception e) {
			TelemetryManager.error("Error! Something went wrong" + e.getMessage(), e);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Something went wrong while processing the request", e);
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
			TelemetryManager.log("Validating the Base Path: " + path);
			isValid = isPathExist(Paths.get(path));
		} catch (InvalidPathException | NullPointerException e) {
			isValid = false;
		}
		return isValid;
	}

	/**
	 * validates Path
	 * 
	 * @param path
	 *            the path checks if the path exists, if not null then creates a
	 *            Path for it
	 * @return true if its a validBasePath return false
	 */
	protected boolean isPathExist(Path path) {
		boolean exist = true;
		try {
			if (null != path) {
				TelemetryManager.log("Creating the Base Path: " + path.getFileName());
				if (!Files.exists(path))
					Files.createDirectories(path);
			}
		} catch (FileAlreadyExistsException e) {
			TelemetryManager.log("Base Path Already Exist: " + path.getFileName());
		} catch (Exception e) {
			exist = false;
			TelemetryManager.error("Error! Something went wrong while creating the path - " + path.getFileName(), e);
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
		folderName = S3PropertyReader.getProperty(CONTEN_FOLDER);
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
			//urlArray = AWSUploader.uploadFile(folder, uploadFile);
			urlArray = CloudStore.uploadFile(folder, uploadFile, true);
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodeConstants.UPLOAD_ERROR.name(),
					ContentErrorMessageConstants.FILE_UPLOAD_ERROR, e);
		}
		return urlArray;
	}

	/**
	 * gets the NumericValue of the object
	 * 
	 * @param obj
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
	 * @param obj
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
	protected Double getCloudStorageFileSize(String key) {
		Double bytes = null;
		if (StringUtils.isNotBlank(key)) {
			try {
				return CloudStore.getObjectSize(key);
			} catch (Exception e) {
				TelemetryManager.warn("Error! While getting the file size from AWS"+ key);
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
				TelemetryManager.error("Error! While Converting the Date Format."+ date, e);
			}
		}
		return null;
	}

	/**
	 * gets the date after the given Days
	 * 
	 * @param days
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
	 * @param contentBody
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
	 * @param contentBody
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
	 * @param contentId
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
			TelemetryManager.log("Starting Data Collection For Bundling...");
			List<Node> childrenNodes = new ArrayList<Node>();
			for (Node node : nodes) {
				TelemetryManager.log("Collecting Hierarchical Bundling Data For Content Id: " + node.getIdentifier());
				getContentRecursive(graphId, childrenNodes, node, nodeMap, childrenIds, ctnts, onlyLive);
			}
			nodes.addAll(childrenNodes);
		}
	}

	protected void getContentRecursive(String graphId, List<Node> childrenNodes, Node node, Map<String, Node> nodeMap,
			List<String> childrenIds, List<Map<String, Object>> ctnts, boolean onlyLive) {
		if (!nodeMap.containsKey(node.getIdentifier())) {
			nodeMap.put(node.getIdentifier(), node);
			Map<String, Object> metadata = new HashMap<String, Object>();
			if (null == node.getMetadata())
				node.setMetadata(new HashMap<String, Object>());
			String status = (String) node.getMetadata().get(ContentWorkflowPipelineParams.status.name());
			if ((onlyLive && (StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.Live.name(), status) 
					|| StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.Unlisted.name(), status)))
					|| !onlyLive) {
				metadata.putAll(node.getMetadata());
				metadata.put(ContentWorkflowPipelineParams.identifier.name(), node.getIdentifier());
				metadata.put(ContentWorkflowPipelineParams.objectType.name(), node.getObjectType());
				metadata.remove(ContentWorkflowPipelineParams.body.name());
				metadata.remove(ContentWorkflowPipelineParams.editorState.name());
				if (null != node.getTags() && !node.getTags().isEmpty())
					 metadata.put(ContentWorkflowPipelineParams.tags.name(), node.getTags());
				
				ctnts.add(metadata);
				if(StringUtils.equalsIgnoreCase((String)node.getMetadata().get(ContentWorkflowPipelineParams.visibility.name()), ContentWorkflowPipelineParams.Parent.name()))
					childrenIds.add(node.getIdentifier());
				
				
				
				
				
			}
		}
	}

	protected String getContentBody(String contentId) {
		Request request = new Request();
		request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
		request.setOperation(ContentStoreOperations.getContentBody.name());
		request.put(ContentStoreParams.content_id.name(), contentId);
		Response response = makeLearningRequest(request);
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
		TelemetryManager.log("Searching Nodes For Bundling...");
		ContentSearchCriteria criteria = new ContentSearchCriteria();
		String maxSizeKey = "publish.content.limit";
		if (Platform.config.hasPath(maxSizeKey)) {
			criteria.setResultSize(Platform.config.getInt(maxSizeKey));
		}
		List<Filter> filters = new ArrayList<Filter>();
		Filter filter = new Filter(ContentWorkflowPipelineParams.identifier.name(), SearchConditions.OP_IN, contentIds);
		filters.add(filter);

		List<String> status = new ArrayList<String>();
		status.add(ContentWorkflowPipelineParams.Draft.name());
		status.add(ContentWorkflowPipelineParams.Live.name());
		status.add(ContentWorkflowPipelineParams.Unlisted.name());
		status.add(ContentWorkflowPipelineParams.Review.name());
		status.add(ContentWorkflowPipelineParams.Processing.name());
		status.add(ContentWorkflowPipelineParams.Pending.name());
		Filter statusFilter = new Filter(ContentWorkflowPipelineParams.status.name(), SearchConditions.OP_IN, status);
		filters.add(statusFilter);

		MetadataCriterion metadata = MetadataCriterion.create(filters);
		criteria.setMetadata(metadata);
		List<Request> requests = new ArrayList<Request>();
		if (StringUtils.isNotBlank(taxonomyId)) {
			Request req = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER,
					ContentWorkflowPipelineParams.searchNodes.name(), GraphDACParams.search_criteria.name(),
					criteria.getSearchCriteria());
			req.put(GraphDACParams.get_tags.name(), true);
			requests.add(req);
		}
		Response response = getResponse(requests, GraphDACParams.node_list.name(),
				ContentWorkflowPipelineParams.contents.name());
		return response;
	}

	protected String getContentObjectIdentifier(Node node) {
		String identifier = "";
		if (null != node) {
			identifier = node.getIdentifier();
			if (StringUtils.equalsIgnoreCase(node.getObjectType(), ContentWorkflowPipelineParams.ContentImage.name()))
				identifier += ContentConfigurationConstants.DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
		}
		return identifier;
	}

	/**
	 * Recursively deletes the give file/folder
	 * 
	 * @param file
	 *            the file to be deleted
	 * @throws IOException
	 *             when there is a file I/O error
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
