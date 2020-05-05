package org.ekstep.content.operation.finalizer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.cloning.Cloner;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.Slug;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.mgr.ConvertGraphNode;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.common.util.HttpRestUtil;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.content.common.ContentConfigurationConstants;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.common.EcarPackageType;
import org.ekstep.content.common.ExtractionType;
import org.ekstep.content.entity.Plugin;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.util.ContentBundle;
import org.ekstep.content.util.ContentPackageExtractionUtil;
import org.ekstep.content.util.GraphUtil;
import org.ekstep.content.util.PublishFinalizeUtil;
import org.ekstep.content.util.SyncMessageGenerator;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.enums.RelationTypes;
import org.ekstep.graph.dac.enums.SystemProperties;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.itemset.publish.ItemsetPublishManager;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.contentstore.VideoStreamingJobRequest;
import org.ekstep.learning.hierarchy.store.HierarchyStore;
import org.ekstep.learning.util.CloudStore;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * The Class BundleFinalizer, extends BaseFinalizer which mainly holds common
 * methods and operations of a ContentBody. PublishFinalizer holds methods which
 * perform ContentPublishPipeline operations
 */
public class PublishFinalizer extends BaseFinalizer {

	private VideoStreamingJobRequest streamJobRequest = new VideoStreamingJobRequest();
	
	private static final String TAXONOMY_ID = "domain";
	
	/** The Constant IDX_S3_KEY. */
	private static final int IDX_S3_KEY = 0;

	/** The Constant IDX_S3_URL. */
	private static final int IDX_S3_URL = 1;
	
	private static int batchSize = 50;

	/** The BasePath. */
	protected String basePath;

	/** The ContentId. */
	protected String contentId;

	private static final String COLLECTION_MIMETYPE = "application/vnd.ekstep.content-collection";
	private static final String ECML_MIMETYPE = "application/vnd.ekstep.ecml-archive";
	private static final String CONTENT_FOLDER = "cloud_storage.content.folder";
	private static final String ARTEFACT_FOLDER = "cloud_storage.artefact.folder";
	private static final List<String> LEVEL4_MIME_TYPES = Arrays.asList("video/x-youtube","application/pdf","application/msword","application/epub","application/vnd.ekstep.h5p-archive","text/x-url");
	private static final List<String> LEVEL4_CONTENT_TYPES = Arrays.asList("Course","CourseUnit","LessonPlan","LessonPlanUnit");
	private static final String  ES_INDEX_NAME = CompositeSearchConstants.COMPOSITE_SEARCH_INDEX;
	private static final String DOCUMENT_TYPE = Platform.config.hasPath("search.document.type") ? Platform.config.getString("search.document.type") : CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE;
	private static final List<String> PUBLISHED_STATUS_LIST = Arrays.asList("Live", "Unlisted");
	private static final Boolean ITEMSET_GENERATE_PDF = Platform.config.hasPath("itemset.generate.pdf") ? Platform.config.getBoolean("itemset.generate.pdf") : true;
	
	protected static final String PRINT_SERVICE_BASE_URL = Platform.config.hasPath("kp.print.service.base.url")
			? Platform.config.getString("kp.print.service.base.url") : "http://localhost:5001";

	private static ContentPackageExtractionUtil contentPackageExtractionUtil = new ContentPackageExtractionUtil();
	private static ObjectMapper mapper = new ObjectMapper();
	private HierarchyStore hierarchyStore = new HierarchyStore();
	private ControllerUtil util = new ControllerUtil();
	private ItemsetPublishManager itemsetPublishManager = new ItemsetPublishManager(util);
	private PublishFinalizeUtil publishFinalizeUtil = new PublishFinalizeUtil();
	public void setItemsetPublishManager(ItemsetPublishManager itemsetPublishManager) {
		this.itemsetPublishManager = itemsetPublishManager;
	}
	
	public void setPublishFinalizeUtil(PublishFinalizeUtil publishFinalizeUtil) {
		this.publishFinalizeUtil = publishFinalizeUtil;
	}

	public void setHierarchyStore(HierarchyStore hierarchyStore) {
		this.hierarchyStore = hierarchyStore;
	}

	static {
		ElasticSearchUtil.initialiseESClient(ES_INDEX_NAME, Platform.config.getString("search.es_conn_info"));
	}

	/** 3Days TTL for Collection hierarchy cache*/
	private static final int CONTENT_CACHE_TTL = (Platform.config.hasPath("content.cache.ttl"))
			? Platform.config.getInt("content.cache.ttl")
			: 259200;
	private static final String COLLECTION_CACHE_KEY_PREFIX = "hierarchy_";
    private static final String COLLECTION_CACHE_KEY_SUFFIX = ":leafnodes";

	/**
	 * Instantiates a new PublishFinalizer and sets the base path and current
	 * content id for further processing.
	 *
	 * @param basePath
	 *            the base path is the location for content package file
	 *            handling and all manipulations.
	 * @param contentId
	 *            the content id is the identifier of content for which the
	 *            Processor is being processed currently.
	 */
	public PublishFinalizer(String basePath, String contentId) {
		if (!isValidBasePath(basePath))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Path does not Exist.]");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Invalid Content Id.]");
		this.basePath = basePath;
		this.contentId = contentId;
	}

	/**
	 * finalize()
	 *
	 * @param parameterMap
	 *            the parameterMap
	 *
	 *            checks if Node, ecrfType,ecmlType exists in the parameterMap
	 *            else throws ClientException Output only ECML format create
	 *            'artifactUrl' Get Content String write ECML File Create 'ZIP'
	 *            Package Upload Package Upload to S3 Set artifact file For Node
	 *            Download App Icon and create thumbnail Set Package Version
	 *            Create ECAR Bundle Delete local compressed artifactFile
	 *            Populate Fields and Update Node
	 * @return the response
	 */
	public Response finalize(Map<String, Object> parameterMap) {

		String artifactUrl = null;
		File packageFile=null;
		Node node = (Node) parameterMap.get(ContentWorkflowPipelineParams.node.name());
		List<String> unitNodes = null;
		DefinitionDTO definition = util.getDefinition(TAXONOMY_ID, ContentWorkflowPipelineParams.Content.name());

		if (null == node)
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid or null Node.]");
		RedisStoreUtil.delete(contentId, contentId + COLLECTION_CACHE_KEY_SUFFIX, COLLECTION_CACHE_KEY_PREFIX + contentId);
		if (node.getIdentifier().endsWith(".img")) {
			String updatedVersion = preUpdateNode(node.getIdentifier());
			node.getMetadata().put(GraphDACParams.versionKey.name(), updatedVersion);
			if(StringUtils.equalsIgnoreCase((String)node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name()), COLLECTION_MIMETYPE)) {
				unitNodes = new ArrayList<>();
				getUnitFromLiveContent(unitNodes);
				cleanUnitsInRedis(unitNodes);
			}
		}
		node.setIdentifier(contentId);
		node.setObjectType(ContentWorkflowPipelineParams.Content.name());
		
		try {
			String itemsetPreviewUrl = getItemsetPreviewUrl(node);
			if(StringUtils.isNotBlank(itemsetPreviewUrl))
				node.getMetadata().put(ContentWorkflowPipelineParams.itemSetPreviewUrl.name(), itemsetPreviewUrl);
		}catch(Exception e) {
			TelemetryManager.error("Error in Itemset PreviewUrl generation :: " + e.getStackTrace());
			e.printStackTrace();
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage() + ". Please Try Again After Sometime!");
		}
		 
		boolean isCompressionApplied = (boolean) parameterMap.get(ContentWorkflowPipelineParams.isCompressionApplied.name());
		TelemetryManager.log("Compression Applied ? " + isCompressionApplied);

		if (BooleanUtils.isTrue(isCompressionApplied)) {
			Plugin ecrf = (Plugin) parameterMap.get(ContentWorkflowPipelineParams.ecrf.name());

			if (null == ecrf)
				throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
						ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid or null ECRF Object.]");

			// Output only ECML format
			String ecmlType = ContentWorkflowPipelineParams.ecml.name();

			// Get Content String
			String ecml = getECMLString(ecrf, ecmlType);
			// Write ECML File
			writeECMLFile(basePath, ecml, ecmlType);

			//upload snapshot of content into aws
			contentPackageExtractionUtil.uploadExtractedPackage(contentId, node, basePath, ExtractionType.snapshot, true);

			// Create 'ZIP' Package
			String zipFileName = basePath + File.separator + System.currentTimeMillis() + "_" + Slug.makeSlug(contentId)
					+ ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR
					+ ContentConfigurationConstants.DEFAULT_ZIP_EXTENSION;
			TelemetryManager.info("Zip file name: " + zipFileName);
			createZipPackage(basePath, zipFileName);
			// Upload Package
			packageFile = new File(zipFileName);
			if (packageFile.exists()) {
				// Upload to S3
				String folderName = S3PropertyReader.getProperty(ARTEFACT_FOLDER);
				String[] urlArray = uploadToAWS(packageFile, getUploadFolderName(contentId, folderName));
				if (null != urlArray && urlArray.length >= 2)
					artifactUrl = urlArray[IDX_S3_URL];

				// Set artifact file For Node
				node.getMetadata().put(ContentWorkflowPipelineParams.artifactUrl.name(), artifactUrl);
			}
		}
		// Download App Icon and create thumbnail
		createThumbnail(basePath, node);


		// Set Package Version
		double version = 1.0;
		if (null != node && null != node.getMetadata()
				&& null != node.getMetadata().get(ContentWorkflowPipelineParams.pkgVersion.name()))
			version = getDoubleValue(node.getMetadata().get(ContentWorkflowPipelineParams.pkgVersion.name())) + 1;
		node.getMetadata().put(ContentWorkflowPipelineParams.pkgVersion.name(), version);
		node.getMetadata().put(ContentWorkflowPipelineParams.lastPublishedOn.name(), formatCurrentDate());
		node.getMetadata().put(ContentWorkflowPipelineParams.flagReasons.name(), null);
		node.getMetadata().put(ContentWorkflowPipelineParams.body.name(), null);
		node.getMetadata().put(ContentWorkflowPipelineParams.publishError.name(), null);
		node.getMetadata().put(ContentWorkflowPipelineParams.variants.name(), null);

		setCompatibilityLevel(node);
		setPragma(node);

		String publishType = (String) node.getMetadata().get(ContentWorkflowPipelineParams.publish_type.name());
		node.getMetadata().put(ContentWorkflowPipelineParams.status.name(),
				StringUtils.equalsIgnoreCase(publishType, ContentWorkflowPipelineParams.Unlisted.name())?
						ContentWorkflowPipelineParams.Unlisted.name(): ContentWorkflowPipelineParams.Live.name());
		node.getMetadata().put(ContentWorkflowPipelineParams.publish_type.name(), null);

		Object artifact = node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name());
		if (null != artifact && artifact instanceof File) {
			File pkgFile = (File) artifact;
			if (pkgFile.exists())
				pkgFile.delete();
			TelemetryManager.log("Deleting Local Artifact Package File: " + pkgFile.getAbsolutePath());
			node.getMetadata().remove(ContentWorkflowPipelineParams.artifactUrl.name());

			if (StringUtils.isNotBlank(artifactUrl))
				node.getMetadata().put(ContentWorkflowPipelineParams.artifactUrl.name(), artifactUrl);
		}

		Map<String,Object> collectionHierarchy = getHierarchy(node.getIdentifier(), true);
		TelemetryManager.log("Hierarchy for content : " + node.getIdentifier() + " : " + collectionHierarchy);
		List<Map<String, Object>> children = null;
		if(MapUtils.isNotEmpty(collectionHierarchy)) {
			Set<String> collectionResourceChildNodes = new HashSet<>();
			children = (List<Map<String,Object>>)collectionHierarchy.get("children");
			enrichChildren(children, collectionResourceChildNodes, node);
			if(!collectionResourceChildNodes.isEmpty()) {
				List<String> collectionChildNodes = getList(node.getMetadata().get(ContentWorkflowPipelineParams.childNodes.name()));
				collectionChildNodes.addAll(collectionResourceChildNodes);
			}

		}

		if (StringUtils.equalsIgnoreCase(((String) node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name())),COLLECTION_MIMETYPE)) {
			TelemetryManager.log("Collection processing started for content: " + node.getIdentifier());
			processCollection(node, children);
			TelemetryManager.log("Collection processing done for content: " + node.getIdentifier());
		}
		TelemetryManager.log("Ecar processing started for content: " + node.getIdentifier());
		processForEcar(node, children);
		TelemetryManager.log("Ecar processing done for content: " + node.getIdentifier());

		try {
			TelemetryManager.log("Deleting the temporary folder: " + basePath);
			delete(new File(basePath));
		} catch (Exception e) {
			e.printStackTrace();
			TelemetryManager.error("Error deleting the temporary folder: " + basePath, e);
		}
		if (BooleanUtils.isTrue(ContentConfigurationConstants.IS_ECAR_EXTRACTION_ENABLED)) {
			contentPackageExtractionUtil.copyExtractedContentPackage(contentId, node, ExtractionType.version);
			contentPackageExtractionUtil.copyExtractedContentPackage(contentId, node, ExtractionType.latest);
		}

		//update previewUrl for content streaming
		updatePreviewURL(node);

		Node newNode = new Node(node.getIdentifier(), node.getNodeType(), node.getObjectType());
		newNode.setGraphId(node.getGraphId());
		newNode.setMetadata(node.getMetadata());
		newNode.setTags(node.getTags());

		// Setting default version key for internal node update
		String graphPassportKey = Platform.config.getString(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY);
		newNode.getMetadata().put(GraphDACParams.versionKey.name(), graphPassportKey);

		newNode.setInRelations(node.getInRelations());
		newNode.setOutRelations(node.getOutRelations());

		TelemetryManager.log("Migrating the Image Data to the Live Object. | [Content Id: " + contentId + ".]");
		Response response = migrateContentImageObjectData(contentId, newNode);

		// delete image..
		Request request = getRequest(TAXONOMY_ID, GraphEngineManagers.NODE_MANAGER,
				"deleteDataNode");
		request.put(ContentWorkflowPipelineParams.node_id.name(), contentId + ".img");

		getResponse(request);

		List<String> streamableMimeType = Platform.config.hasPath("stream.mime.type") ?
				Arrays.asList(Platform.config.getString("stream.mime.type").split(",")) : Arrays.asList("video/mp4");
		if (streamableMimeType.contains((String) node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name()))) {
			streamJobRequest.insert(contentId, (String) node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name()),
					(String) node.getMetadata().get(ContentWorkflowPipelineParams.channel.name()),
					String.valueOf(node.getMetadata().get(ContentWorkflowPipelineParams.pkgVersion.name())));
		}

		Node publishedNode = util.getNode(ContentWorkflowPipelineParams.domain.name(), newNode.getIdentifier());

		if (StringUtils.equalsIgnoreCase((String) newNode.getMetadata().get("mimeType"),
				COLLECTION_MIMETYPE)) {
			updateHierarchyMetadata(children, publishedNode);
			publishHierarchy(publishedNode, children);
			syncNodes(children, unitNodes);
		}

		return response;
	}

	private void cleanUnitsInRedis(List<String> unitNodes) {
		if(CollectionUtils.isNotEmpty(unitNodes)) {
			String[] unitsIds = unitNodes.stream().map(id -> (COLLECTION_CACHE_KEY_PREFIX + id)).collect(Collectors.toList()).toArray(new String[unitNodes.size()]);
			if(unitsIds.length > 0)
				RedisStoreUtil.delete(unitsIds);
		}
	}

	private void enrichChildren(List<Map<String, Object>> children, Set<String> collectionResourceChildNodes, Node node) {
		try {
			if (CollectionUtils.isNotEmpty(children)) {
				List<Map<String, Object>> newChildren = new ArrayList<>(children);
				if (null != newChildren && !newChildren.isEmpty()) {
					for (Map<String, Object> child : newChildren) {
						if (StringUtils.equalsIgnoreCase((String) child.get(ContentWorkflowPipelineParams.visibility.name()), "Parent") &&
								StringUtils.equalsIgnoreCase((String) child.get(ContentWorkflowPipelineParams.mimeType.name()), COLLECTION_MIMETYPE))
							enrichChildren((List<Map<String, Object>>) child.get(ContentWorkflowPipelineParams.children.name()), collectionResourceChildNodes, node);
						if (StringUtils.equalsIgnoreCase((String) child.get(ContentWorkflowPipelineParams.visibility.name()), "Default") &&
								StringUtils.equalsIgnoreCase((String) child.get(ContentWorkflowPipelineParams.mimeType.name()), COLLECTION_MIMETYPE)) {
							Map<String, Object> collectionHierarchy = getHierarchy((String) child.get(ContentWorkflowPipelineParams.identifier.name()), false);
							TelemetryManager.log("Collection hierarchy for childNode : " + child.get(ContentWorkflowPipelineParams.identifier.name()) + " : " + collectionHierarchy);
							if (MapUtils.isNotEmpty(collectionHierarchy)) {
								collectionHierarchy.put(ContentWorkflowPipelineParams.index.name(), child.get(ContentWorkflowPipelineParams.index.name()));
								collectionHierarchy.put(ContentWorkflowPipelineParams.parent.name(), child.get(ContentWorkflowPipelineParams.parent.name()));
								List<String> childNodes = getList(collectionHierarchy.get(ContentWorkflowPipelineParams.childNodes.name()));
								if (!CollectionUtils.isEmpty(childNodes))
									collectionResourceChildNodes.addAll(childNodes);
								if (!MapUtils.isEmpty(collectionHierarchy)) {
									children.remove(child);
									children.add(collectionHierarchy);
								}
							}
						}
						if (StringUtils.equalsIgnoreCase((String) child.get(ContentWorkflowPipelineParams.visibility.name()), "Default") &&
								!StringUtils.equalsIgnoreCase((String) child.get(ContentWorkflowPipelineParams.mimeType.name()), COLLECTION_MIMETYPE)) {
							Response readResponse = getDataNode(TAXONOMY_ID, (String) child.get(ContentWorkflowPipelineParams.identifier.name()));
							children.remove(child);
							List<String> childNodes = getList(node.getMetadata().get(ContentWorkflowPipelineParams.childNodes.name()));
							if (!checkError(readResponse)) {
								Node resNode = (Node) readResponse.get(GraphDACParams.node.name());
								if (PUBLISHED_STATUS_LIST.contains(resNode.getMetadata().get(ContentWorkflowPipelineParams.status.name()))) {
									DefinitionDTO definition = util.getDefinition(TAXONOMY_ID, ContentWorkflowPipelineParams.Content.name());

									String nodeString = mapper.writeValueAsString(ConvertGraphNode.convertGraphNode(resNode, TAXONOMY_ID, definition, null));
									Map<String, Object> resourceNode = mapper.readValue(nodeString, Map.class);
									resourceNode.put("index", child.get(ContentWorkflowPipelineParams.index.name()));
									resourceNode.put("depth", child.get(ContentWorkflowPipelineParams.depth.name()));
									resourceNode.put("parent", child.get(ContentWorkflowPipelineParams.parent.name()));
									resourceNode.remove(ContentWorkflowPipelineParams.collections.name());
									resourceNode.remove(ContentWorkflowPipelineParams.children.name());
									children.add(resourceNode);
								} else {
									childNodes.remove((String) child.get(ContentWorkflowPipelineParams.identifier.name()));
								}
								node.getMetadata().put(ContentWorkflowPipelineParams.childNodes.name(), childNodes);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServerException("ERR_INTERNAL_SERVER_ERROR", e.getMessage(), e);
		}
	}
	
	private void getUnitFromLiveContent(List<String> unitNodes){
		Map<String, Object> liveContentHierarchy = getHierarchy(contentId, false);
		if(MapUtils.isNotEmpty(liveContentHierarchy)) {
			List<Map<String, Object>> children = (List<Map<String, Object>>)liveContentHierarchy.get("children");
			getUnitFromLiveContent(unitNodes, children);
		}
	}
	private void getUnitFromLiveContent(List<String> unitNodes, List<Map<String, Object>> children) {
		if(CollectionUtils.isNotEmpty(children)) {
			children.stream().forEach(child -> {
				if(StringUtils.equalsIgnoreCase("Parent", (String) child.get("visibility"))) {
                		unitNodes.add((String)child.get("identifier"));
                		getUnitFromLiveContent(unitNodes, (List<Map<String, Object>>) child.get("children"));
                }
            });
		}
	}
	
	private void syncNodes(List<Map<String, Object>> children, List<String> unitNodes) {
		DefinitionDTO definition = util.getDefinition(TAXONOMY_ID, ContentWorkflowPipelineParams.Content.name());
		if (null == definition) {
			TelemetryManager.error("Content definition is null.");
		}
		List<String> nodeIds = new ArrayList<>();
		List<Node> nodes = new ArrayList<>();
		getNodeForSyncing(children, nodes, nodeIds, definition);
		if(CollectionUtils.isNotEmpty(unitNodes))
			unitNodes.removeAll(nodeIds);
		
		if(CollectionUtils.isEmpty(nodes) && CollectionUtils.isEmpty(unitNodes))
			return;

		Map<String, String> errors;
		Map<String, Object> def =  mapper.convertValue(definition, new TypeReference<Map<String, Object>>() {});
		Map<String, String> relationMap = GraphUtil.getRelationMap(ContentWorkflowPipelineParams.Content.name(), def);
		if(CollectionUtils.isNotEmpty(nodes)) {
			while (!nodes.isEmpty()) {
				int currentBatchSize = (nodes.size() >= batchSize) ? batchSize : nodes.size();
				List<Node> nodeBatch = nodes.subList(0, currentBatchSize);
	
				if (CollectionUtils.isNotEmpty(nodeBatch)) {
					
					errors = new HashMap<>();
					Map<String, Object> messages = SyncMessageGenerator.getMessages(nodeBatch, ContentWorkflowPipelineParams.Content.name(), relationMap, errors);
					if (!errors.isEmpty())
						TelemetryManager.error("Error! while forming ES document data from nodes, below nodes are ignored: " + errors);
					if(MapUtils.isNotEmpty(messages)) {
						try {
                            System.out.println("Number of units to be synced : " + messages.size());
							ElasticSearchUtil.bulkIndexWithIndexId(ES_INDEX_NAME, DOCUMENT_TYPE, messages);
                            System.out.println("UnitIds synced : " + messages.keySet());
						} catch (Exception e) {
						    e.printStackTrace();
							TelemetryManager.error("Elastic Search indexing failed: " + e);
						}					
					}
				}
				// clear the already batched node ids from the list
				nodes.subList(0, currentBatchSize).clear();
			}
		}
		try {
			//Unindexing not utilized units
			if(CollectionUtils.isNotEmpty(unitNodes))
				ElasticSearchUtil.bulkDeleteDocumentById(ES_INDEX_NAME, DOCUMENT_TYPE, unitNodes);
		} catch (Exception e) {
			TelemetryManager.error("Elastic Search indexing failed: " + e);
		}
	}
	
	private void getNodeMap(List<Map<String, Object>> children, List<Node> nodes, List<String> nodeIds, DefinitionDTO definition) {
        if (CollectionUtils.isNotEmpty(children)) {
            children.stream().forEach(child -> {
                Node node = null;
                try {
                    if(StringUtils.equalsIgnoreCase("Default", (String) child.get("visibility"))) {
                        node = util.getNode(ContentWorkflowPipelineParams.domain.name(), (String)child.get("identifier"));//getContentNode(TAXONOMY_ID, (String) child.get("identifier"), null);
                        node.getMetadata().remove("children");
                        Map<String, Object> childData = new HashMap<>();
                        childData.putAll(child);
                        List<Map<String, Object>> nextLevelNodes = (List<Map<String, Object>>) childData.get("children");
                        List<Map<String, Object>> finalChildList = new ArrayList<>();
						if (CollectionUtils.isNotEmpty(nextLevelNodes)) {
							finalChildList = nextLevelNodes.stream().map(nextLevelNode -> {
								Map<String, Object> metadata = new HashMap<String, Object>() {{
									put("identifier", nextLevelNode.get("identifier"));
									put("name", nextLevelNode.get("name"));
									put("objectType", "Content");
									put("description", nextLevelNode.get("description"));
									put("index", nextLevelNode.get("index"));
								}};
								return metadata;
							}).collect(Collectors.toList());
						}
						node.getMetadata().put("children", finalChildList);
                        
                    }else {
                    		Map<String, Object> childData = new HashMap<>();
                        childData.putAll(child);
                        List<Map<String, Object>> nextLevelNodes = (List<Map<String, Object>>) childData.get("children");
                        childData.remove("children");
                        node = ConvertToGraphNode.convertToGraphNode(childData, definition, null);
                        List<Map<String, Object>> finalChildList = new ArrayList<>();
						if (CollectionUtils.isNotEmpty(nextLevelNodes)) {
							finalChildList = nextLevelNodes.stream().map(nextLevelNode -> {
								Map<String, Object> metadata = new HashMap<String, Object>() {{
									put("identifier", nextLevelNode.get("identifier"));
									put("name", nextLevelNode.get("name"));
									put("objectType", "Content");
									put("description", nextLevelNode.get("description"));
									put("index", nextLevelNode.get("index"));
								}};
								return metadata;
							}).collect(Collectors.toList());
						}
						node.getMetadata().put("children", finalChildList);
                        if(StringUtils.isBlank(node.getObjectType()))
                        		node.setObjectType(ContentWorkflowPipelineParams.Content.name());
                        if(StringUtils.isBlank(node.getGraphId()))
                        		node.setGraphId(ContentWorkflowPipelineParams.domain.name());
                    }
                    if(!nodeIds.contains(node.getIdentifier())) {
                    		nodes.add(node);
                    		nodeIds.add(node.getIdentifier());
                    }
                } catch (Exception e) {
                		TelemetryManager.error("Error while generating node map. ", e);
                }
                getNodeMap((List<Map<String, Object>>) child.get("children"), nodes, nodeIds, definition);
            });
        }
    }
	private List<String> getRelationList(DefinitionDTO definition){
		List<String> relationshipProperties = new ArrayList<>();
		relationshipProperties.addAll(definition.getInRelations().stream().map(rel -> rel.getTitle()).collect(Collectors.toList()));
		relationshipProperties.addAll(definition.getOutRelations().stream().map(rel -> rel.getTitle()).collect(Collectors.toList()));
		return relationshipProperties;
	}
	
	private void getNodeForSyncing(List<Map<String, Object>> children, List<Node> nodes, List<String> nodeIds, DefinitionDTO definition) {
		List<String> relationshipProperties = getRelationList(definition);
        if (CollectionUtils.isNotEmpty(children)) {
            children.stream().forEach(child -> {
            		try {
            			if(StringUtils.equalsIgnoreCase("Parent", (String) child.get("visibility"))) {
            				Map<String, Object> childData = new HashMap<>();
            				childData.putAll(child);
            				Map<String, Object> relationProperties = new HashMap<>();
            				for(String property : relationshipProperties) {
            					if(childData.containsKey(property)) {
                        			relationProperties.put(property, (List<Map<String, Object>>) childData.get(property));
                        			childData.remove(property);
                        		}
            				}
            				Node node = ConvertToGraphNode.convertToGraphNode(childData, definition, null);
            				if(MapUtils.isNotEmpty(relationProperties)) {
            					for(String key : relationProperties.keySet()) {
                        			List<String> finalPropertyList = null;
                        			List<Map<String, Object>> properties = (List<Map<String, Object>>)relationProperties.get(key);
                        			if (CollectionUtils.isNotEmpty(properties)) {
                        				finalPropertyList = properties.stream().map(property -> {
            								String identifier = (String)property.get("identifier");
            								return identifier;
            							}).collect(Collectors.toList());
            						}
                        			if(CollectionUtils.isNotEmpty(finalPropertyList))
                        				node.getMetadata().put(key, finalPropertyList);
                        		}
                        }
                        if(StringUtils.isBlank(node.getObjectType()))
                        		node.setObjectType(ContentWorkflowPipelineParams.Content.name());
                        if(StringUtils.isBlank(node.getGraphId()))
                        		node.setGraphId(ContentWorkflowPipelineParams.domain.name());
                        if(!nodeIds.contains(node.getIdentifier())) {
                    			nodes.add(node);
                    			nodeIds.add(node.getIdentifier());
                        }
                        getNodeForSyncing((List<Map<String, Object>>) child.get("children"), nodes, nodeIds, definition);
                    }
                    
                } catch (Exception e) {
                	TelemetryManager.error("Error while fetching unit nodes for syncing. ", e);
                }
                
            });
        }
    }
	
	private void publishHierarchy(Node node, List<Map<String,Object>> childrenList) {
		Map<String, Object> hierarchy = getContentMap(node, childrenList);
		hierarchyStore.saveOrUpdateHierarchy(node.getIdentifier(), hierarchy);
	}

	private Map<String, Object> getContentMap(Node node, List<Map<String,Object>> childrenList) {
		DefinitionDTO definition = util.getDefinition(TAXONOMY_ID, "Content");
		Map<String, Object> collectionHierarchy  = ConvertGraphNode.convertGraphNode(node, TAXONOMY_ID, definition, null);
		collectionHierarchy.put("children", childrenList);
		collectionHierarchy.put("identifier", node.getIdentifier());
		collectionHierarchy.put("objectType", node.getObjectType());
		return collectionHierarchy;
	}
	
	private Map<String, Object> getHierarchy(String nodeId, boolean needImageHierarchy) {
		if(needImageHierarchy){
			String identifier = StringUtils.endsWith(nodeId, ".img") ? nodeId : nodeId + ".img";
			Map<String, Object> hierarchy = hierarchyStore.getHierarchy(identifier);
			if(MapUtils.isEmpty(hierarchy)) {
				return hierarchyStore.getHierarchy(nodeId);
			} else {
				return hierarchy;
			}
		} else {
			return hierarchyStore.getHierarchy(nodeId.replaceAll(".img", ""));
		}
	}
	
	private void updateHierarchyMetadata(List<Map<String, Object>> children, Node node) {
		if(CollectionUtils.isNotEmpty(children)) {
			for(Map<String, Object> child : children) {
				if(StringUtils.equalsIgnoreCase("Parent", 
						(String)child.get("visibility"))){
					//set child metadata -- compatibilityLevel, appIcon, posterImage, lastPublishedOn, pkgVersion, status
					populatePublishMetadata(child, node);
					updateHierarchyMetadata((List<Map<String,Object>>)child.get("children"), node);
				}
			}
		}
	}
	
	private void populatePublishMetadata(Map<String, Object> content, Node node) {
		content.put("compatibilityLevel", null != content.get("compatibilityLevel") ? 
				((Number) content.get("compatibilityLevel")).intValue() : 1);
		//TODO:  For appIcon, posterImage and screenshot createThumbNail method has to be implemented.
		content.put(ContentWorkflowPipelineParams.lastPublishedOn.name(), node.getMetadata().get(ContentWorkflowPipelineParams.lastPublishedOn.name()));
		content.put(ContentWorkflowPipelineParams.pkgVersion.name(), node.getMetadata().get(ContentWorkflowPipelineParams.pkgVersion.name()));
		content.put(ContentWorkflowPipelineParams.leafNodesCount.name(), getLeafNodeCount(content));
		content.put(ContentWorkflowPipelineParams.status.name(), node.getMetadata().get(ContentWorkflowPipelineParams.status.name()));
		content.put(ContentWorkflowPipelineParams.lastUpdatedOn.name(), node.getMetadata().get(ContentWorkflowPipelineParams.lastUpdatedOn.name()));
		content.put(ContentWorkflowPipelineParams.downloadUrl.name(), node.getMetadata().get(ContentWorkflowPipelineParams.downloadUrl.name()));
		content.put(ContentWorkflowPipelineParams.variants.name(), node.getMetadata().get(ContentWorkflowPipelineParams.variants.name()));
	}

	@SuppressWarnings("unchecked")
	private Integer getLeafNodeCount(Map<String, Object> data) {
	    Set<String> leafNodeIds = new HashSet<>();
	    getLeafNodesIds(data, leafNodeIds);
	    return leafNodeIds.size();
	}

	private double getTotalCompressedSize(Map<String, Object> data, double totalCompressed) {
		List<Map<String,Object>> children = (List<Map<String,Object>>) data.get("children");
		if(CollectionUtils.isNotEmpty(children)) {
			for(Map<String,Object> child : children ){
				if(!StringUtils.equals((String)child.get(ContentAPIParams.mimeType.name()), COLLECTION_MIMETYPE)
						&& StringUtils.equals((String) child.get(ContentAPIParams.visibility.name()),"Default")) {
					if(null != child.get(ContentAPIParams.totalCompressedSize.name())) {
						totalCompressed += ((Number) child.get(ContentAPIParams.totalCompressedSize.name())).doubleValue();
					} else if(null != child.get(ContentAPIParams.size.name())) {
						totalCompressed += ((Number) child.get(ContentAPIParams.size.name())).doubleValue();
					}
				}
				totalCompressed = getTotalCompressedSize(child, totalCompressed);
			}
		}
		return totalCompressed;
	}

	/**
	 * @param identifier
	 * @return
	 */
	private String preUpdateNode(String identifier) {
		identifier = identifier.replace(".img", "");
		Response response = getDataNode(TAXONOMY_ID, identifier);
		if (!checkError(response)) {
			Node node = (Node) response.get(GraphDACParams.node.name());
			Response updateResp = updateNode(node, true);
			if (!checkError(updateResp)) {
				return (String) updateResp.get(GraphDACParams.versionKey.name());
			} else {
				throw new ServerException(ContentErrorCodeConstants.PUBLISH_ERROR.name(),
						ContentErrorMessageConstants.CONTENT_IMAGE_MIGRATION_ERROR + " | [Content Id: " + contentId
								+ "]" + "::" + updateResp.getParams().getErr() + " :: " + updateResp.getParams().getErrmsg() + " :: "
								+ updateResp.getResult());
			}
		} else {
			throw new ServerException(ContentErrorCodeConstants.PUBLISH_ERROR.name(),
					ContentErrorMessageConstants.CONTENT_IMAGE_MIGRATION_ERROR + " | [Content Id: " + contentId + "]");
		}
	}

	private void setPragma(Node node) {
		List<String> mimeTypes = Arrays.asList("video/x-youtube", "application/pdf");
		if (mimeTypes.contains((String) node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name()))) {
			Object obj = node.getMetadata().get("pragma");
			List<String> pragma = null;
			String value = "external";
			if (obj instanceof List) {
				pragma = (List<String>) obj;
			} else {
				pragma = new ArrayList<>();
			}
			if (!pragma.contains(value))
				pragma.add(value);
			node.getMetadata().put("pragma", pragma);
		}
	}

	private void updatePreviewURL(Node content) {
		if (null != content) {
			String mimeType = (String) content.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name());
			if (StringUtils.isNotBlank(mimeType)) {
				TelemetryManager.log("Checking Required Fields For: " + mimeType);
				switch (mimeType) {
					case "application/vnd.ekstep.content-collection":
						break;
					case "application/vnd.ekstep.plugin-archive":
						break;
					case "application/vnd.android.package-archive":
						break;
					case "assets":
						break;
					case "application/vnd.ekstep.ecml-archive":
					case "application/vnd.ekstep.html-archive":
					case "application/vnd.ekstep.h5p-archive":
						copyLatestS3Url(content);
						break;
					default:
						String artifactUrl = (String) content.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name());
						content.getMetadata().put(ContentWorkflowPipelineParams.previewUrl.name(), artifactUrl);
						List<String> streamableMimeType = Platform.config.hasPath("stream.mime.type")?
								Arrays.asList(Platform.config.getString("stream.mime.type").split(",")) : Arrays.asList("video/mp4");
						if(!streamableMimeType.contains(mimeType))
							content.getMetadata().put(ContentWorkflowPipelineParams.streamingUrl.name(), artifactUrl);
						break;
				}
			}
		}
	}
	
	private void copyLatestS3Url(Node content) {
		try {
			String latestFolderS3Url = contentPackageExtractionUtil.getS3URL(contentId, content, ExtractionType.latest);
			//copy into previewUrl
			content.getMetadata().put(ContentWorkflowPipelineParams.previewUrl.name(), latestFolderS3Url);
			content.getMetadata().put(ContentWorkflowPipelineParams.streamingUrl.name(), latestFolderS3Url);
		} catch (Exception e) {
			TelemetryManager.error("Something Went Wrong While copying latest s3 folder path to preveiwUrl for the content" + contentId, e);
		}
	}
	
	private String getBundleFileName(String contentId, Node node, EcarPackageType packageType) {
		TelemetryManager.info("Generating Bundle File Name For ECAR Package Type: " + packageType.name());
		String fileName = "";
		if (null != node && null != node.getMetadata() && null != packageType) {
			String suffix = "";
			if (packageType != EcarPackageType.FULL)
				suffix = "_" + packageType.name();
			fileName = Slug.makeSlug((String) node.getMetadata().get(ContentWorkflowPipelineParams.name.name()), true)
					+ "_" + System.currentTimeMillis() + "_" + contentId + "_"
					+ node.getMetadata().get(ContentWorkflowPipelineParams.pkgVersion.name()) + suffix + ".ecar";
		}
		return fileName;
	}
	
	private void removeExtraProperties(Node imgNode) {
		Response originalResponse = getDataNode(TAXONOMY_ID, imgNode.getIdentifier());
		if (checkError(originalResponse))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name(),
					"Error! While Fetching the Content for Operation | [Content Id: " + imgNode.getIdentifier() + "]");
		Node originalNode = (Node)originalResponse.get(GraphDACParams.node.name());
		
		Set<String> originalNodeMetaDataSet = originalNode.getMetadata().keySet();
		Set<String> imgNodeMetaDataSet = imgNode.getMetadata().keySet();
		Set<String> extraMetadata = originalNodeMetaDataSet.stream().filter(element -> !imgNodeMetaDataSet.contains(element)).collect(Collectors.toSet());
		
		if(!extraMetadata.isEmpty()) {
			for(String prop : extraMetadata) {
				imgNode.getMetadata().put(prop, null);
			}
		}
	}

	private Response migrateContentImageObjectData(String contentId, Node contentImage) {
		Response response = new Response();
		if (null != contentImage && StringUtils.isNotBlank(contentId)) {
			String contentImageId = contentId + ContentConfigurationConstants.DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
			
			TelemetryManager.info("Fetching the Content Image Node for actual state . | [Content Id: " + contentImageId + "]");
			//Response getDataNodeResponse = getDataNode(contentImage.getGraphId(), contentImageId);
			//Node dbNode = (Node) getDataNodeResponse.get(ContentWorkflowPipelineParams.node.name());
			
			
			
			//if (null != dbNode) {
				//contentImage.setInRelations(dbNode.getInRelations());
				//contentImage.setOutRelations(dbNode.getOutRelations());
			if(null == contentImage.getInRelations()) 
				contentImage.setInRelations(new ArrayList<>());
			if(null == contentImage.getOutRelations())
				contentImage.setOutRelations(new ArrayList<>());
			removeExtraProperties(contentImage);
			//}
			TelemetryManager.info("Migrating the Content Body. | [Content Id: " + contentId + "]");

			// Get body only for ECML content.
			String mimeType = (String) contentImage.getMetadata().get("mimeType");
			if (StringUtils.equalsIgnoreCase(ECML_MIMETYPE, mimeType)) {
				String imageBody = getContentBody(contentImageId);
				if (StringUtils.isNotBlank(imageBody)) {
					response = updateContentBody(contentId, imageBody);
					if (checkError(response)) {
						TelemetryManager.error("Content Body Update Failed During Publish. Error Code :" + response.getParams().getErr() + " | Error Message : " + response.getParams().getErrmsg() + " | Result : " + response.getResult());
						throw new ServerException(ContentErrorCodeConstants.PUBLISH_ERROR.name(),
								ContentErrorMessageConstants.CONTENT_BODY_MIGRATION_ERROR + " | [Content Id: " + contentId
										+ "]" + response.getParams().getErrmsg() + " :: " + response.getParams().getErr() + " :: " + response.getResult());
					}
				}
			}

			TelemetryManager.log("Migrating the Content Object Metadata. | [Content Id: " + contentId + "]");
			response = updateNode(contentImage);
			if (checkError(response)) {
				TelemetryManager.error(response.getParams().getErrmsg() + " :: " + response.getParams().getErr() + " :: " + response.getResult());
				throw new ServerException(ContentErrorCodeConstants.PUBLISH_ERROR.name(),
						ContentErrorMessageConstants.CONTENT_IMAGE_MIGRATION_ERROR + " | [Content Id: " + contentId
								+ "]" + response.getParams().getErrmsg() + " :: " + response.getParams().getErr() + " :: " + response.getResult());
			}
		}

		TelemetryManager.log("Returning the Response Object After Migrating the Content Body and Metadata.");
		return response;
	}
	
	private boolean disableCollectionFullECAR() {
		if (Platform.config.hasPath("publish.collection.fullecar.disable"))
			return "true".equalsIgnoreCase(Platform.config.getString("publish.collection.fullecar.disable"));
		else 
			return true;
	}

	private List<String> generateEcar(EcarPackageType pkgType, Node node, ContentBundle contentBundle,
									  List<Map<String, Object>> ecarContents, List<String> childrenIds, List<Map<String, Object>> children) {

		Map<Object, List<String>> downloadUrls = null;
		TelemetryManager.log("Creating " + pkgType.toString() + " ECAR For Content Id: " + node.getIdentifier());
		String bundleFileName = getBundleFileName(contentId, node, pkgType);
		downloadUrls = contentBundle.createContentManifestData(ecarContents, childrenIds, null,
				pkgType);

		List<String> ecarUrl = Arrays.asList(contentBundle.createContentBundle(ecarContents, bundleFileName,
				ContentConfigurationConstants.DEFAULT_CONTENT_MANIFEST_VERSION, downloadUrls, node, children));
		TelemetryManager.log(pkgType.toString() + " ECAR created For Content Id: " + node.getIdentifier());

		if (!EcarPackageType.FULL.name().equalsIgnoreCase(pkgType.toString())) {
			Map<String, Object> ecarMap = new HashMap<>();
			ecarMap.put(ContentWorkflowPipelineParams.ecarUrl.name(), ecarUrl.get(IDX_S3_URL));
			ecarMap.put(ContentWorkflowPipelineParams.size.name(), getCloudStorageFileSize(ecarUrl.get(IDX_S3_KEY)));

			TelemetryManager.log("Adding " + pkgType.toString() + " Ecar Information to Variants Map For Content Id: " + node.getIdentifier());
			((Map<String, Object>) node.getMetadata().get(ContentWorkflowPipelineParams.variants.name())).put(pkgType.toString().toLowerCase(), ecarMap);

		}
		return ecarUrl;
	}

	private void setCompatibilityLevel(Node node) {
		if (LEVEL4_MIME_TYPES.contains(node.getMetadata().getOrDefault(ContentWorkflowPipelineParams.mimeType.name(), "").toString())
				|| LEVEL4_CONTENT_TYPES.contains(node.getMetadata().getOrDefault(ContentWorkflowPipelineParams.contentType.name(), "").toString())) {
			TelemetryManager.info("setting compatibility level for content id : " + node.getIdentifier() + " as 4.");
			node.getMetadata().put(ContentWorkflowPipelineParams.compatibilityLevel.name(), 4);
		}
	}

	// TODO: rewrite this specific code
	private void updateRootChildrenList(Node node, List<Map<String, Object>> nextLevelNodes) {
	    List<Map<String, Object>> childrenMap = new ArrayList<>();
	    if (CollectionUtils.isNotEmpty(nextLevelNodes)) {
	        for (Map<String, Object> nextLevelNode: nextLevelNodes) {
	            childrenMap.add(new HashMap<String, Object>() {{
                    put("identifier", nextLevelNode.get("identifier"));
                    put("name", nextLevelNode.get("name"));
                    put("objectType", "Content");
                    put("description", nextLevelNode.get("description"));
                    put("index", nextLevelNode.get("index"));
                }});
            }
        }
	    node.getMetadata().put("children", childrenMap);
    }
	
	private void processForEcar(Node node, List<Map<String, Object>> children) {
		List<Node> nodes = new ArrayList<Node>();
		String downloadUrl = null;
		String s3Key = null;
		String mimeType = (String) node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name());
		nodes.add(node);
		
		if (StringUtils.equalsIgnoreCase((String) node.getMetadata().get("mimeType"),COLLECTION_MIMETYPE)) {
			DefinitionDTO definition = util.getDefinition(TAXONOMY_ID, "Content");
			updateHierarchyMetadata(children, node);

			List<String> nodeIds = new ArrayList<>();
			nodeIds.add(node.getIdentifier());
            updateRootChildrenList(node, children);
			getNodeMap(children, nodes, nodeIds, definition);
		}
		
		List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();
		List<String> childrenIds = new ArrayList<String>();
		getContentBundleData(node.getGraphId(), nodes, contents, childrenIds);

		// Cloning contents to spineContent
		Cloner cloner = new Cloner();
		List<Map<String, Object>> spineContents = cloner.deepClone(contents);
		List<Map<String, Object>> onlineContents = cloner.deepClone(contents);

		TelemetryManager.info("Initialising the ECAR variant Map For Content Id: " + node.getIdentifier());
		ContentBundle contentBundle = new ContentBundle();
		// ECARs Generation - START
		node.getMetadata().put(ContentWorkflowPipelineParams.variants.name(), new HashMap<String, Object>());
		if (COLLECTION_MIMETYPE.equalsIgnoreCase(mimeType) && disableCollectionFullECAR()) {
			TelemetryManager.log("Disabled full ECAR generation for collections. So not generating for collection id: " + node.getIdentifier());
			// TODO: START : Remove the below when mobile app is ready to accept Resources as Default in manifest
			List<String> nodeChildList = getList(node.getMetadata().get("childNodes"));
			if(CollectionUtils.isNotEmpty(nodeChildList))
				childrenIds = nodeChildList;
		} else {
			List<String> fullECARURL = generateEcar(EcarPackageType.FULL, node, contentBundle, contents, childrenIds, null);
			downloadUrl = fullECARURL.get(IDX_S3_URL);
			s3Key = fullECARURL.get(IDX_S3_KEY);
		}
		// Generate spine ECAR.
		List<String> spineECARUrl = generateEcar(EcarPackageType.SPINE, node, contentBundle, spineContents, childrenIds, children);

		// if collection full ECAR creation disabled set spine as download url.
		if (COLLECTION_MIMETYPE.equalsIgnoreCase(mimeType) && disableCollectionFullECAR()) {
			downloadUrl = spineECARUrl.get(IDX_S3_URL);
			s3Key = spineECARUrl.get(IDX_S3_KEY);
		}

		// generate online ECAR for Collection
		if (COLLECTION_MIMETYPE.equalsIgnoreCase(mimeType)) {
			generateEcar(EcarPackageType.ONLINE, node, contentBundle, onlineContents, childrenIds, children);
			node.getMetadata().remove("children");
		}
		// ECAR generation - END
		
		// Populate Fields and Update Node
		node.getMetadata().put(ContentWorkflowPipelineParams.s3Key.name(), s3Key);
		node.getMetadata().put(ContentWorkflowPipelineParams.downloadUrl.name(), downloadUrl);
		node.getMetadata().put(ContentWorkflowPipelineParams.size.name(), getCloudStorageFileSize(s3Key));
	}
	
	@SuppressWarnings("unchecked")
	private void processCollection(Node node, List<Map<String, Object>> children) {

		String contentId = node.getIdentifier();
		Map<String, Object> dataMap = null;
		dataMap = processChildren(node, children);
		TelemetryManager.log("Children nodes process for collection - " + contentId);
		if (MapUtils.isNotEmpty(dataMap)) {
			for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
				if ("concepts".equalsIgnoreCase(entry.getKey()) || "keywords".equalsIgnoreCase(entry.getKey())) {
					continue;
				} else {
					Set<String> valueSet = (HashSet<String>) entry.getValue();
					String[] value = valueSet.toArray(new String[valueSet.size()]);
					node.getMetadata().put(entry.getKey(), value);
				}
			}
			Set<String> keywords = (HashSet<String>) dataMap.get("keywords");
			if (null != keywords && !keywords.isEmpty()) {
				if (null != node.getMetadata().get("keywords")) {
					Object object = node.getMetadata().get("keywords");
					if (object instanceof String[]) {
						String[] stringArray = (String[]) node.getMetadata().get("keywords");
						keywords.addAll(Arrays.asList(stringArray));
					} else if (object instanceof String) {
						String keyword = (String) node.getMetadata().get("keywords");
						keywords.add(keyword);
					}
				}
				List<String> keywordsList = new ArrayList<>();
				keywordsList.addAll(keywords);
				node.getMetadata().put("keywords", keywordsList);
			}
		}

		enrichCollection(node, children);

		addResourceToCollection(node, children);

	}

	private void addResourceToCollection(Node node, List<Map<String, Object>> children) {
		List<Map<String, Object>> leafNodes = getLeafNodes(children, 1);
		if (CollectionUtils.isNotEmpty(leafNodes)) {
			List<Relation> relations = new ArrayList<>();
			for(Map<String, Object> leafNode : leafNodes) {

				String id = (String) leafNode.get("identifier");
				int index = 1;
				Number num = (Number) leafNode.get("index");
				if (num != null) {
					index = num.intValue();
				}
				Relation rel = new Relation(node.getIdentifier(), RelationTypes.SEQUENCE_MEMBERSHIP.relationName(), id);
				Map<String, Object> metadata = new HashMap<>();
				metadata.put(SystemProperties.IL_SEQUENCE_INDEX.name(), index);
				metadata.put("depth", leafNode.get("depth"));
				rel.setMetadata(metadata);
				relations.add(rel);
			}
			List<Relation> existingRelations = node.getOutRelations();
			if (CollectionUtils.isNotEmpty(existingRelations)) {
				relations.addAll(existingRelations);
			}
			node.setOutRelations(relations);
		}
		
	}
	
	private List<Map<String, Object>> getLeafNodes(List<Map<String, Object>> children, int depth) {
		List<Map<String, Object>> leafNodes = new ArrayList<>();
		if(CollectionUtils.isNotEmpty(children)) {
			int index = 1;
			for(Map<String, Object> child : children) {
				String visibility = (String) child.get(ContentWorkflowPipelineParams.visibility.name());
				if(StringUtils.equalsIgnoreCase(visibility, ContentWorkflowPipelineParams.Parent.name())) {
					List<Map<String,Object>> nextChildren = (List<Map<String,Object>>)child.get("children");
					int nextDepth = depth + 1;
					List<Map<String, Object>> nextLevelLeafNodes = getLeafNodes(nextChildren, nextDepth);
					leafNodes.addAll(nextLevelLeafNodes);
				}else {
					Map<String, Object> leafNode = new HashMap<>(child);
					leafNode.put("index", index);
					leafNode.put("depth", depth);
					leafNodes.add(leafNode);
					index++;
				}
			}
		}
		return leafNodes;
	}

	private Map<String, Object> processChildren(Node node, List<Map<String, Object>> children) {
		Map<String, Object> dataMap = new HashMap<>();
		processChildren(children, dataMap);
		return dataMap;
	}
	private void processChildren(List<Map<String, Object>> children, Map<String, Object> dataMap){
		if (null!=children && !children.isEmpty()) {
			for (Map<String, Object> child : children) {
				mergeMap(dataMap, processChild(child));
				processChildren((List<Map<String, Object>>)child.get("children"), dataMap);
			}
		}
	}
	
	public void enrichCollection(Node node, List<Map<String, Object>> children)  {

		String contentId = node.getIdentifier();
		TelemetryManager.info("Processing Collection Content :" + contentId);
		if (null != children && !children.isEmpty()) {
			Map<String, Object> content = getContentMap(node, children);
			if(MapUtils.isEmpty(content))
				return;
			int leafCount = getLeafNodeCount(content);
			double totalCompressedSize = 0.0;
			totalCompressedSize = getTotalCompressedSize(content, totalCompressedSize);
			content.put(ContentAPIParams.leafNodesCount.name(), leafCount);
			node.getMetadata().put(ContentAPIParams.leafNodesCount.name(), leafCount);
			content.put(ContentAPIParams.totalCompressedSize.name(), totalCompressedSize);
			node.getMetadata().put(ContentAPIParams.totalCompressedSize.name(), totalCompressedSize);
			updateLeafNodeIds(node, content);


			Map<String, Object> mimeTypeMap = new HashMap<>();
			Map<String, Object> contentTypeMap = new HashMap<>();
			List<String> childNodes = getChildNode(content);
			
			getTypeCount(content, "mimeType", mimeTypeMap);
			getTypeCount(content, "contentType", contentTypeMap);
			
			content.put(ContentAPIParams.mimeTypesCount.name(), mimeTypeMap);
			content.put(ContentAPIParams.contentTypesCount.name(), contentTypeMap);
			content.put(ContentAPIParams.childNodes.name(), childNodes);
			
			node.getMetadata().put(ContentAPIParams.toc_url.name(), generateTOC(node, content));
			try {
				node.getMetadata().put(ContentAPIParams.mimeTypesCount.name(), convertToString(mimeTypeMap));
				node.getMetadata().put(ContentAPIParams.contentTypesCount.name(), convertToString(contentTypeMap));
			} catch (Exception e) {
				TelemetryManager.error("Error while stringifying mimeTypeCount or contentTypesCount.", e);
			}
			node.getMetadata().put(ContentAPIParams.childNodes.name(), childNodes);
		}
	}

	private void updateLeafNodeIds(Node node, Map<String, Object> content) {
        Set<String> leafNodeIds = new HashSet<>();
        getLeafNodesIds(content, leafNodeIds);
        node.getMetadata().put(ContentAPIParams.leafNodes.name(), new ArrayList<>(leafNodeIds));
	}

	private String convertToString(Object obj) throws Exception {
		return mapper.writeValueAsString(obj);
	}

	private Map<String, Object> processChild(Map<String, Object> node) {
		Map<String, Object> result = new HashMap<>();
		List<String> taggingProperties = Arrays.asList("language", "domain", "ageGroup", "genre", "theme", "keywords");
		for(String prop : node.keySet()) {
			if(taggingProperties.contains(prop)) {
				Object o = node.get(prop);
				if(o instanceof String)
					result.put(prop, new HashSet<Object>(Arrays.asList((String)o)));
				if(o instanceof List)
					result.put(prop, new HashSet<Object>((List<String>)o));
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> mergeMap(Map<String, Object> dataMap, Map<String, Object> childDataMap){
		if (dataMap.isEmpty()) {
			dataMap.putAll(childDataMap);
		} else {
			for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
				Set<Object> value = new HashSet<Object>();
				if (childDataMap.containsKey(entry.getKey())) {
					value.addAll((Collection<? extends Object>) childDataMap.get(entry.getKey()));
				}
				value.addAll((Collection<? extends Object>) entry.getValue());
				dataMap.replace(entry.getKey(), value);
			}
			if (!dataMap.keySet().containsAll(childDataMap.keySet())) {
				for (Map.Entry<String, Object> entry : childDataMap.entrySet()) {
					if (!dataMap.containsKey(entry.getKey())) {
						dataMap.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}
		return dataMap;
	}
	
	private List<String> getChildNode(Map<String, Object> data) {
		Set<String> childrenSet = new HashSet<>();
		getChildNode(data, childrenSet);
		return new ArrayList<>(childrenSet);
	}

	@SuppressWarnings("unchecked")
	private void getChildNode(Map<String, Object> data, Set<String> childrenSet) {
		List<Object> children = (List<Object>) data.get("children");
		if (null != children && !children.isEmpty()) {
			for (Object child : children) {
				Map<String, Object> childMap = (Map<String, Object>) child;
				childrenSet.add((String) childMap.get("identifier"));
				getChildNode(childMap, childrenSet);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void getTypeCount(Map<String, Object> data, String type, Map<String, Object> typeMap) {
		List<Object> children = (List<Object>) data.get("children");
		if (null != children && !children.isEmpty()) {
			for (Object child : children) {
				Map<String, Object> childMap = (Map<String, Object>) child;
				String typeValue = childMap.get(type).toString();
				if (typeMap.containsKey(typeValue)) {
					int count = (int) typeMap.get(typeValue);
					count++;
					typeMap.put(typeValue, count);
				} else {
					typeMap.put(typeValue, 1);
				}
				if (childMap.containsKey("children")) {
					getTypeCount(childMap, type, typeMap);
				}
			}
		}

	}
	
	public String generateTOC(Node node, Map<String, Object> content) {
		TelemetryManager.info("Write hirerachy to JSON File :" + node.getIdentifier());
		String url = null;
		String data = null;
		File file = new File(getTOCBasePath(node.getIdentifier()) + "_toc.json");
		
		try {
			data = mapper.writeValueAsString(content);
			FileUtils.writeStringToFile(file, data);
			if (file.exists()) {
				TelemetryManager.info("Upload File to cloud storage :" + file.getName());
				String[] uploadedFileUrl = CloudStore.uploadFile(getAWSPath(node.getIdentifier()), file, true);
				if (null != uploadedFileUrl && uploadedFileUrl.length > 1) {
					url = uploadedFileUrl[IDX_S3_URL];
					TelemetryManager.info("Update cloud storage url to node" + url);
				}
			}
		} catch(JsonProcessingException e) {
			TelemetryManager.error("Error while parsing map object to string.", e);
		}catch (Exception e) {
			TelemetryManager.error("Error while uploading file ", e);
		}finally {
			try {
				TelemetryManager.info("Deleting Uploaded files");
				FileUtils.deleteDirectory(file.getParentFile());
			} catch (IOException e) {
				TelemetryManager.error("Error while deleting file ", e);
			}
		}
		return url;
	}

	private String getTOCBasePath(String contentId) {
		String path = "";
		if (!StringUtils.isBlank(contentId))
			//TODO: Get the configuration of tmp file location from publish Pipeline
			path = "/tmp" + File.separator + System.currentTimeMillis()
					+ ContentAPIParams._temp.name() + File.separator + contentId;
		return path;
	}

	private String getAWSPath(String identifier) {
		String folderName = S3PropertyReader.getProperty(CONTENT_FOLDER);
		if (!StringUtils.isBlank(folderName)) {
			folderName = folderName + File.separator + Slug.makeSlug(identifier, true) + File.separator
					+ S3PropertyReader.getProperty(ARTEFACT_FOLDER);
		}
		return folderName;
	}

	/**
	 *
	 * @param obj
	 * @return
	 */
	private List<String> getList(Object obj) {
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
			list = list.stream().filter(x -> StringUtils.isNotBlank(x)).collect(toList());
		}
		return list;
	}

    private void getLeafNodesIds(Map<String, Object> data, Set<String> leafNodeIds) {
        List<Map<String,Object>> children = (List<Map<String,Object>>)data.get("children");
        if(CollectionUtils.isNotEmpty(children)) {
            for(Map<String, Object> child : children) {
                getLeafNodesIds(child, leafNodeIds);
            }
        } else {
            if (!StringUtils.equalsIgnoreCase(COLLECTION_MIMETYPE, (String) data.get(ContentAPIParams.mimeType.name()))) {
                leafNodeIds.add((String) data.get(ContentAPIParams.identifier.name()));
            }
        }
    }
    
    protected String getItemsetPreviewUrl(Node node) throws Exception {
    	
    		List<Relation> outRelations = node.getOutRelations();
    		if(CollectionUtils.isEmpty(outRelations)) {
    			return null;
    		}
    		List<String> itemSetRelations = outRelations.stream()
    				.filter(r -> StringUtils.equalsIgnoreCase(r.getEndNodeObjectType(), "ItemSet"))
    				.map(x -> x.getEndNodeId()).collect(Collectors.toList());
		if(CollectionUtils.isNotEmpty(itemSetRelations)){
			String questionBankHtml = itemsetPublishManager.publish(itemSetRelations);
			if(StringUtils.isNotBlank(questionBankHtml)) {
				if(ITEMSET_GENERATE_PDF) {
					Response generateResponse = HttpRestUtil.makePostRequest(PRINT_SERVICE_BASE_URL + "/v1/print/preview/generate?fileUrl=" 
							+ questionBankHtml, new HashMap<>(), new HashMap<>());
					
					if (generateResponse.getResponseCode() == ResponseCode.OK) {
			            String itemsetPreviewUrl = (String)generateResponse.getResult().get(ContentAPIParams.pdfUrl.name());
			            if(StringUtils.isNotBlank(itemsetPreviewUrl))
			            		return publishFinalizeUtil.uploadFile(itemsetPreviewUrl, node, basePath);
			            else
			                throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(),
			                        "Itemset generated previewUrl is empty. Please Try Again After Sometime!");
			        }else {
			            if (generateResponse.getResponseCode() == ResponseCode.CLIENT_ERROR) {
			                TelemetryManager.error("Client Error during Generate Itemset previewUrl: " + generateResponse.getParams().getErrmsg() + " :: " + generateResponse.getResult());
			                throw new ClientException(generateResponse.getParams().getErr(), generateResponse.getParams().getErrmsg());
			            }
			            else {
			                TelemetryManager.error("Server Error during Generate Itemset preiewUrl: " + generateResponse.getParams().getErrmsg() + " :: " + generateResponse.getResult());
			                throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(),
			                        "Error During generate Itemset previewUrl. Please Try Again After Sometime!");
			            }
			        }
				}else {
					return publishFinalizeUtil.uploadFile(questionBankHtml, node, basePath);
				}
				
			}
		}
    		return null;
    }
}