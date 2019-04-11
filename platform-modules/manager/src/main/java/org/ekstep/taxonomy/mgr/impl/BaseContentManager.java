package org.ekstep.taxonomy.mgr.impl;

import akka.actor.ActorRef;
import akka.pattern.Patterns;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.NodeDTO;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.mgr.BaseManager;
import org.ekstep.common.mgr.ConvertGraphNode;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.common.router.RequestRouterPool;
import org.ekstep.common.util.YouTubeUrlUtil;
import org.ekstep.content.enums.ContentMetadata;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.enums.SystemNodeTypes;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.graph.model.node.MetadataDefinition;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.learning.common.enums.LearningActorNames;
import org.ekstep.learning.contentstore.ContentStoreOperations;
import org.ekstep.learning.contentstore.ContentStoreParams;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.taxonomy.enums.TaxonomyAPIParams;
import org.ekstep.telemetry.logger.TelemetryManager;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.ekstep.common.util.RequestValidatorUtil.getEmptyErrorMessageFor;

public abstract class BaseContentManager extends BaseManager {

	/**
	 * The Default 'ContentImage' Object Suffix (Content_Object_Identifier +
	 * ".img")
	 */
	protected static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";
	
	private static final String DEFAULT_MIME_TYPE = "assets";

	/** The Disk Location where the operations on file will take place. */
	protected static final String tempFileLocation = "/data/contentBundle/";

	/** The Default Manifest Version */
	protected static final String DEFAULT_CONTENT_MANIFEST_VERSION = "1.2";

	/**
	 * Content Image Object Type
	 */
	protected static final String CONTENT_IMAGE_OBJECT_TYPE = "ContentImage";

	/**
	 * Content Object Type
	 */
	protected static final String CONTENT_OBJECT_TYPE = "Content";

	protected static final String TAXONOMY_ID = "domain";

	protected static ObjectMapper objectMapper = new ObjectMapper();

	protected static final String DIALCODE_GENERATE_URI = Platform.config.hasPath("dialcode.api.generate.url")
			? Platform.config.getString("dialcode.api.generate.url") : "http://localhost:8080/learning-service/v3/dialcode/generate";

	protected List<String> finalStatus = Arrays.asList("Flagged", "Live", "Unlisted");
	protected List<String> reviewStatus = Arrays.asList("Review", "FlagReview");

    protected ControllerUtil util = new ControllerUtil();

	protected String getId(String identifier) {
		if (StringUtils.endsWith(identifier, ".img")) {
			return identifier.replace(".img", "");
		}
		return identifier;
	}
	
	protected String getImageId(String identifier) {
		String imageId = "";
		if (StringUtils.isNotBlank(identifier))
			imageId = identifier + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
		return imageId;
	}
	
	protected void isImageContentId(String identifier) {
		if (StringUtils.endsWithIgnoreCase(identifier, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX))
			throw new ClientException(ContentErrorCodes.OPERATION_DENIED.name(),
					"Invalid Content Identifier. | [Content Identifier does not Exists.]");
	}
	
	protected void isNodeUnderProcessing(Node node, String operation) {
		List<String> status = new ArrayList<>();
		status.add(TaxonomyAPIParams.Processing.name());
		//status.add(TaxonomyAPIParams.Pending.name());
		boolean isProccssing = checkNodeStatus(node, status);
		if (BooleanUtils.isTrue(isProccssing)) {
			TelemetryManager.log("Given Content is in Processing Status.");
			throw new ClientException(TaxonomyErrorCodes.ERR_NODE_ACCESS_DENIED.name(),
					"Operation Denied! | [Cannot Apply '"+ operation +"' Operation on the Content in '" + 
							(String)node.getMetadata().get(TaxonomyAPIParams.status.name()) + "' Status.] ");
		} else {
			TelemetryManager.log("Given Content is not in " + (String)node.getMetadata().get(TaxonomyAPIParams.status.name()) + " Status.");
		}
	}
	
	protected String getMimeType(Node node) {
		String mimeType = (String) node.getMetadata().get("mimeType");
		if (StringUtils.isBlank(mimeType)) {
			mimeType = DEFAULT_MIME_TYPE;
		}
		return mimeType;
	}
	
	// TODO: if exception occurs it return false. It is invalid. Check.
	private boolean checkNodeStatus(Node node, List<String> status) {
		boolean inGivenStatus = false;
		try {
			if (null != node && null != node.getMetadata()) {
				for(String st : status) {
					if(equalsIgnoreCase((String) node.getMetadata().get(TaxonomyAPIParams.status.name()),
							st)) {
						inGivenStatus = true;
					}
				}
			}
		} catch (Exception e) {
			TelemetryManager.error("Something went wrong while checking the object whether it is under processing or not.", e);
		}
		return inGivenStatus;
	}

	protected Node getContentNode(String graphId, String contentId, String mode) {

		if (equalsIgnoreCase("edit", mode)) {
			String contentImageId = getImageId(contentId);
			Response responseNode = getDataNode(graphId, contentImageId);
			if (!checkError(responseNode)) {
				Node content = (Node) responseNode.get(GraphDACParams.node.name());
				return content;
			}
		}
		Response responseNode = getDataNode(graphId, contentId);
		if (checkError(responseNode))
			throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
                "Content not found with id: " + contentId);

		Node content = (Node) responseNode.get(GraphDACParams.node.name());
		return content;
	}

	/**
	 * Update node.
	 *
	 * @param node
	 *            the node
	 * @return the response
	 */
	protected Response updateDataNode(Node node) {
		Response response = new Response();
		if (null != node) {
			String contentId = node.getIdentifier();
			// Checking if Content Image Object is being Updated, then return
			// the Original Content Id
			if (BooleanUtils.isTrue((Boolean) node.getMetadata().get(TaxonomyAPIParams.isImageObject.name()))) {
				node.getMetadata().remove(TaxonomyAPIParams.isImageObject.name());
				node.setIdentifier(node.getIdentifier() + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX);
			}

			TelemetryManager.log("Getting Update Node Request For Node ID: " + node.getIdentifier());
			Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
			updateReq.put(GraphDACParams.node.name(), node);
			updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());

			TelemetryManager.log("Updating the Node ID: " + node.getIdentifier());
			response = getResponse(updateReq);

			response.put(TaxonomyAPIParams.node_id.name(), contentId);
			TelemetryManager.log("Returning Node Update Response.");
		}
		return response;
	}

	protected Response updateNode(String identifier, String objectType, Node domainNode) {
		domainNode.setGraphId(TAXONOMY_ID);
		domainNode.setIdentifier(identifier);
		domainNode.setObjectType(objectType);
		return updateDataNode(domainNode);
	}

	protected DefinitionDTO getDefinition(String graphId, String objectType) {
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
				GraphDACParams.object_type.name(), objectType);
		Response response = getResponse(request);
		if (!checkError(response)) {
			DefinitionDTO definition = (DefinitionDTO) response.get(GraphDACParams.definition_node.name());
			return definition;
		}
		return null;
	}

	protected List<String> getExternalPropsList(DefinitionDTO definition) {
		List<String> list = new ArrayList<>();
		if (null != definition) {
			List<MetadataDefinition> props = definition.getProperties();
			if (null != props && !props.isEmpty()) {
				for (MetadataDefinition prop : props) {
					if (equalsIgnoreCase("external", prop.getDataType())) {
						list.add(prop.getPropertyName().trim());
					}
				}
			}
		}
		return list;
	}

    protected Response createDataNode(Node node) {
        Response response = new Response();
        if (null != node) {
            Request request = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "createDataNode");
            request.put(GraphDACParams.node.name(), node);

            TelemetryManager.log("Creating the Node ID: " + node.getIdentifier());
            response = getResponse(request);
        }
        return response;
    }

    protected void restrictProps(DefinitionDTO definition, Map<String, Object> map, String... props) {
        for (String prop : props) {
            Object allow = definition.getMetadata().get("allowupdate_" + prop);
            if (allow == null || BooleanUtils.isFalse((Boolean) allow)) {
                if (map.containsKey(prop))
                    throw new ClientException(ContentErrorCodes.ERR_CONTENT_UPDATE.name(),
                            "Error! " + prop + " can't be set for the content.");
            }

        }
    }

    // TODO: push this to publish-pipeline.
    protected void updateDefaultValuesByMimeType(Map<String, Object> map, String mimeType) {
        if (StringUtils.isNotBlank(mimeType)) {
            if (mimeType.endsWith("archive") || mimeType.endsWith("vnd.ekstep.content-collection")
                    || mimeType.endsWith("epub"))
                map.put(TaxonomyAPIParams.contentEncoding.name(), ContentMetadata.ContentEncoding.gzip.name());
            else
                map.put(TaxonomyAPIParams.contentEncoding.name(), ContentMetadata.ContentEncoding.identity.name());

            if (mimeType.endsWith("youtube") || mimeType.endsWith("x-url"))
                map.put(TaxonomyAPIParams.contentDisposition.name(), ContentMetadata.ContentDisposition.online.name());
            else
                map.put(TaxonomyAPIParams.contentDisposition.name(), ContentMetadata.ContentDisposition.inline.name());
        }
    }

    public Response updateAllContents(String originalId, Map<String, Object> map) throws Exception {
        if (null == map)
            return ERROR("ERR_CONTENT_INVALID_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);

        DefinitionDTO definition = getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE);

        Map<String, Object> externalProps = new HashMap<>();
        List<String> externalPropsList = getExternalPropsList(definition);
        if (null != externalPropsList && !externalPropsList.isEmpty()) {
            for (String prop : externalPropsList) {
                if (null != map.get(prop))
                    externalProps.put(prop, map.get(prop));
                if (equalsIgnoreCase(ContentAPIParams.screenshots.name(), prop) && null != map.get(prop)) {
                    map.put(prop, null);
                } else {
                    map.remove(prop);
                }

            }
        }

        String graphPassportKey = Platform.config.getString(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY);
        map.put("versionKey", graphPassportKey);
        Node domainObj = ConvertToGraphNode.convertToGraphNode(map, definition, null);
        Response updateResponse = updateNode(originalId, CONTENT_OBJECT_TYPE, domainObj);
        if (checkError(updateResponse))
            return updateResponse;
        updateResponse.put(GraphDACParams.node_id.name(), originalId);

        Response getNodeResponse = getDataNode(TAXONOMY_ID, originalId + ".img");
        if(!checkError(getNodeResponse)){
            Node imgDomainObj = ConvertToGraphNode.convertToGraphNode(map, definition, null);
            updateNode(originalId + ".img", CONTENT_IMAGE_OBJECT_TYPE, imgDomainObj);
        }

        if (null != externalProps && !externalProps.isEmpty()) {
            Response externalPropsResponse = updateContentProperties(originalId, externalProps);
            if (checkError(externalPropsResponse))
                return externalPropsResponse;
        }
        return updateResponse;
    }

    protected Node getNodeForOperation(String contentId, String operation) {
        Node node = new Node();

        TelemetryManager.log("Fetching the Content Node. | [Content ID: " + contentId + "]");
        String contentImageId = getImageId(contentId);
        Response response = getDataNode(TAXONOMY_ID, contentImageId);
        if (checkError(response)) {
            TelemetryManager.log("Unable to Fetch Content Image Node for Content Id: " + contentId);

            TelemetryManager.log("Trying to Fetch Content Node (Not Image Node) for Content Id: " + contentId);
            response = getDataNode(TAXONOMY_ID, contentId);

            TelemetryManager.log("Checking for Fetched Content Node (Not Image Node) for Content Id: " + contentId);
            if (checkError(response))
                throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name(),
                        "Error! While Fetching the Content for Operation | [Content Id: " + contentId + "]");

            // Content Image Node is not Available so assigning the original
            // Content Node as node
            node = (Node) response.get(GraphDACParams.node.name());

            if (!equalsIgnoreCase(operation, "publish")
                    && !equalsIgnoreCase(operation, "review")) {
                // Checking if given Content Id is Image Node
                if (null != node && isContentImageObject(node))
                    throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name(),
                            "Invalid Content Identifier! | [Given Content Identifier '" + node.getIdentifier()
                                    + "' does not Exist.]");

                String status = (String) node.getMetadata().get(TaxonomyAPIParams.status.name());
                if (StringUtils.isNotBlank(status)
                        && (equalsIgnoreCase(TaxonomyAPIParams.Live.name(), status)
                        || equalsIgnoreCase(TaxonomyAPIParams.Unlisted.name(), status)
                        || equalsIgnoreCase(TaxonomyAPIParams.Flagged.name(), status)))
                    node = createContentImageNode(TAXONOMY_ID, contentImageId, node);
            }
        } else {
            // Content Image Node is Available so assigning it as node
            node = (Node) response.get(GraphDACParams.node.name());
            TelemetryManager.log("Getting Content Image Node and assigning it as node" + node.getIdentifier());
        }

        TelemetryManager.log("Returning the Node for Operation with Identifier: " + node.getIdentifier());
        return node;
    }

    protected boolean isContentImageObject(Node node) {
        boolean isContentImage = false;
        if (null != node && equalsIgnoreCase(node.getObjectType(),
                ContentWorkflowPipelineParams.ContentImage.name()))
            isContentImage = true;
        return isContentImage;
    }

    protected Node createContentImageNode(String taxonomyId, String contentImageId, Node node) {

        Node imageNode = new Node(taxonomyId, SystemNodeTypes.DATA_NODE.name(), CONTENT_IMAGE_OBJECT_TYPE);
        imageNode.setGraphId(taxonomyId);
        imageNode.setIdentifier(contentImageId);
        imageNode.setMetadata(node.getMetadata());
        imageNode.setInRelations(node.getInRelations());
        imageNode.setOutRelations(node.getOutRelations());
        imageNode.setTags(node.getTags());
        imageNode.getMetadata().put(TaxonomyAPIParams.status.name(), TaxonomyAPIParams.Draft.name());
        Response response = createDataNode(imageNode);
        if (checkError(response))
            throw new ServerException(TaxonomyErrorCodes.ERR_NODE_CREATION.name(),
                    "Error! Something went wrong while performing the operation. | [Content Id: " + node.getIdentifier()
                            + "]");
        Response resp = getDataNode(taxonomyId, contentImageId);
        Node nodeData = (Node) resp.get(GraphDACParams.node.name());
        TelemetryManager.log("Returning Content Image Node Identifier" + nodeData.getIdentifier());
        return nodeData;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> contentCleanUp(Map<String, Object> map) {
        if (map.containsKey(TaxonomyAPIParams.identifier.name())) {
            String identifier = (String) map.get(TaxonomyAPIParams.identifier.name());
            TelemetryManager.log("Checking if identifier ends with .img" + identifier);
            if (StringUtils.endsWithIgnoreCase(identifier, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX)) {
                String newIdentifier = identifier.replace(DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX, "");
                TelemetryManager.log("replacing image id with content id in response " + identifier + newIdentifier);
                map.replace(TaxonomyAPIParams.identifier.name(), identifier, newIdentifier);
            }
        }
        return map;
    }

    /**
     * @param param
     * @return
     */
    @SuppressWarnings("unchecked")
    protected static List<String> getList(Object param) {
        List<String> paramList = null;
        try {
            paramList = (List<String>) param;
        } catch (Exception e) {
            String str = (String) param;
            paramList = Arrays.asList(str);
        }
        if (null != paramList) {
            paramList = paramList.stream().filter(x -> StringUtils.isNotBlank(x) && !StringUtils.equals(" ", x)).collect(toList());
        }
        return paramList;
    }

    /**
     * Gets the folder name.
     *
     * @param url
     *            the url
     * @return the folder name
     */
    protected String getFolderName(String url) {
        try {
            String s = url.substring(0, url.lastIndexOf('/'));
            return s.substring(s.lastIndexOf('/') + 1);
        } catch (Exception e) {
        }
        return "";
    }

    /**
     * This method will check YouTube License and Insert as Node MetaData
     *
     * @param String
     * @param Node
     * @return
     */
    protected void checkYoutubeLicense(String artifactUrl, Node node) {
        Boolean isValReq = Platform.config.hasPath("learning.content.youtube.validate.license")
                ? Platform.config.getBoolean("learning.content.youtube.validate.license") : false;

        if (isValReq) {
            String licenseType = YouTubeUrlUtil.getLicense(artifactUrl);
            if (equalsIgnoreCase("youtube", licenseType))
                node.getMetadata().put("license", "Standard YouTube License");
            else if (equalsIgnoreCase("creativeCommon", licenseType))
                node.getMetadata().put("license", "Creative Commons Attribution (CC BY)");
            else {
                TelemetryManager.log("Got Unsupported Youtube License Type : " + licenseType + " | [Content ID: "
                        + node.getIdentifier() + "]");
                throw new ClientException(TaxonomyErrorCodes.ERR_YOUTUBE_LICENSE_VALIDATION.name(),
                        "Unsupported Youtube License!");
            }
        }
    }

    /**
     * @param publishChecklistObj
     */
    protected boolean validateList(Object publishChecklistObj) {
        try {
            List<String> publishChecklist = (List<String>) publishChecklistObj;
            if (null == publishChecklist || publishChecklist.isEmpty()) {
                return false;
            }

        } catch (Exception e) {
            return false;
        }
        return true;
    }

	protected Response updateContentProperties(String contentId, Map<String, Object> properties) {
		Request request = new Request();
		request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
		request.setOperation(ContentStoreOperations.updateContentProperties.name());
		request.put(ContentStoreParams.content_id.name(), contentId);
		request.put(ContentStoreParams.properties.name(), properties);
		Response response = makeLearningRequest(request);
		return response;
	}

    protected Response getContentProperties(String contentId, List<String> properties) {
        Request request = new Request();
        request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
        request.setOperation(ContentStoreOperations.getContentProperties.name());
        request.put(ContentStoreParams.content_id.name(), contentId);
        request.put(ContentStoreParams.properties.name(), properties);
        Response response = makeLearningRequest(request);
        return response;
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

    protected Response deleteHierarchy(List<String> identifiers) {
        Request request = new Request();
        request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
        request.setOperation(ContentStoreOperations.deleteHierarchy.name());
        request.put(ContentStoreParams.content_id.name(), identifiers);
        Response response = makeLearningRequest(request);
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
                TelemetryManager.log("Response Params: " + response.getParams() + " | Code: "
                        + response.getResponseCode() + " | Result: " + response.getResult().keySet());
                return response;
            } else {
                return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
            }
        } catch (Exception e) {
            TelemetryManager.error("Error! Something went wrong: " + e.getMessage(), e);
            throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", e);
        }
    }

	protected void validateContentForReservedDialcodes(Map<String, Object> metaData) {
		List<String> validContentType = Platform.config.hasPath("learning.reserve_dialcode.content_type") ?
				Platform.config.getStringList("learning.reserve_dialcode.content_type") :
				Arrays.asList("TextBook");

		if(!validContentType.contains(metaData.get(ContentAPIParams.contentType.name())))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_CONTENTTYPE.name(),
					"Invalid Content Type.");
	}

	protected Map<String, Integer> getReservedDialCodes(Node node) throws JsonParseException, JsonMappingException, IOException {
		String reservedDialcode = (String)node.getMetadata().get(ContentAPIParams.reservedDialcodes.name());
		if(StringUtils.isNotBlank(reservedDialcode))
			return objectMapper.readValue((String)node.getMetadata().get(ContentAPIParams.reservedDialcodes.name()), new TypeReference<Map<String, Integer>>() {});
		return null;
	}
	

	protected void validateChannel(Map<String, Object> metadata, String channelId) {
		if(!StringUtils.equals((String) metadata.get(ContentAPIParams.channel.name()), channelId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_INVALID_CHANNEL.name(), "Invalid Channel Id.");
	}

	protected Optional<List<NodeDTO>> getChildren(Node node, DefinitionDTO definition) {
		Map<String, Object> contentMap = ConvertGraphNode.convertGraphNode(node, TAXONOMY_ID, definition, null);
		return ofNullable((List<NodeDTO>) contentMap.get(ContentAPIParams.children.name()));
	}

	protected boolean isNodeVisibilityParent(Node node) {
		return StringUtils.equals(ContentAPIParams.Parent.name(),
				(String) node.getMetadata().get(ContentAPIParams.visibility.name()));
	}

	protected Optional<String[]> getDialcodes(Node node) {
		return ofNullable((String[]) node.getMetadata().get(ContentAPIParams.dialcodes.name())).filter(dialcodes -> dialcodes.length > 0);
	}

	protected boolean isContent(Node node) {
		return equalsIgnoreCase(ContentAPIParams.Content.name(), node.getObjectType());
	}

	protected void validateIsContent(Node node) {
		if (!isContent(node))
			throw new ClientException(ContentErrorCodes.ERR_NOT_A_CONTENT.name(), "Error! Not a Content.");
	}

	protected boolean isRetired(Map<String, Object> metadata) {
		return equalsIgnoreCase((String) metadata.get(ContentAPIParams.status.name()),
                                ContentAPIParams.Retired.name());
	}

	protected void validateIsNodeRetired(Map<String, Object> metadata) {
		if (isRetired(metadata))
			throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
                    "Error! Content not found with id: " + metadata.get("identifier"));
	}

	protected void validateEmptyOrNullContentId(String contentId) {
		if (isEmptyOrNull(contentId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
				"Content Object Id cannot is Blank.");
	}

	protected void validateEmptyOrNullFileUrl(String fileUrl) {
		if (StringUtils.isBlank(fileUrl))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_UPLOAD_OBJECT.name(),
					"File Url cannot be Blank.");
	}

	protected boolean isYoutubeMimeType(String mimeType) {
		return StringUtils.equals("video/x-youtube", mimeType);
	}

	protected Response validateAndGetNodeResponseForOperation(String contentId) {
		Response response = getDataNode(TAXONOMY_ID, contentId);
		if (checkError(response))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name(),
					"Error! While Fetching the Content for Operation | [Content Id: " + contentId + "]");
		return response;
	}

	protected void validateEmptyOrNullChannelId(String channelId) {
		if (StringUtils.isBlank(channelId)) {
			throw new ClientException(ContentErrorCodes.ERR_CHANNEL_BLANK_OBJECT.name(),
					"Channel can not be blank.");
		}
	}

	protected boolean isPluginMimeType(String mimeType) {
		return equalsIgnoreCase("application/vnd.ekstep.plugin-archive", mimeType);
	}

	protected boolean isEcmlMimeType(String mimeType) {
		return equalsIgnoreCase("application/vnd.ekstep.ecml-archive", mimeType);
	}

	protected boolean isH5PMimeType(String mimeType) {
		return equalsIgnoreCase(mimeType, "application/vnd.ekstep.h5p-archive");
	}

	protected String getContentTypeFrom(Node node) {
        return (String) node.getMetadata().get("contentType");
    }

    protected String getMimeTypeFrom(Node node) {
	    return (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
    }

    protected String getArtifactUrlFrom(Node node) {
	    return (String) node.getMetadata().get(ContentAPIParams.artifactUrl.name());
    }

    protected boolean isCollectionMimeType(String mimeType) {
		return equalsIgnoreCase(mimeType, "application/vnd.ekstep.content-collection");
	}

	public String getNodeStatus(Node node) {
		return (String) node.getMetadata().get(ContentAPIParams.status.name());
	}

	protected boolean isStatus(String givenStatus, String status) {
		return equalsIgnoreCase(givenStatus, status);
	}

	protected boolean isNodeStatus(Node node, String status) {
		return isStatus(getNodeStatus(node), status);
	}

	public boolean isLiveStatus(Node node) {
		return isNodeStatus(node, ContentAPIParams.Live.name());
	}

	public boolean isLiveStatus(String status) {
		return isStatus(status, ContentAPIParams.Live.name());
	}

	protected String getDownloadUrlFrom(Node node) {
		return (String) node.getMetadata().get(ContentAPIParams.downloadUrl.name());
	}

    public boolean validateOrThrowExceptionForEmptyKeys(Map<String, Object> requestMap,
                                                        String prefix,
                                                        List<String> keys) {
        String errMsg = "Please provide valid value for ";
        boolean flag = false;
        List<String> notFoundKeys = null;
        for (String key : keys) {
            if (null == requestMap.get(key)) {
                flag = true;
            } else if (requestMap.get(key) instanceof Map) {
                flag = MapUtils.isEmpty((Map) requestMap.get(key));
            } else if (requestMap.get(key) instanceof List) {
                flag = CollectionUtils.isEmpty((List) requestMap.get(key));
            } else {
                flag = isBlank((String) requestMap.get(key));
            }
            if (flag) {
            		if(null==notFoundKeys)
            			notFoundKeys = new ArrayList<>();
            		notFoundKeys.add(key);
            }
        }
        if (CollectionUtils.isEmpty(notFoundKeys)) 
        		return true;
        else {
        		errMsg = errMsg + String.join(", ", notFoundKeys) + ".";
        }
        throw new ClientException("ERR_INVALID_REQUEST", errMsg.trim().substring(0, errMsg
                .length()-1));
    }


    /**
     * Cassandra call to fetch hierarchy data
     *
     * @param contentId
     * @return
     */
    protected Response getCollectionHierarchy(String contentId) {
        Request request = new Request();
        request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
        request.setOperation(ContentStoreOperations.getCollectionHierarchy.name());
        request.put(ContentStoreParams.content_id.name(), contentId);
        Response response = makeLearningRequest(request);
        return response;
    }

}
