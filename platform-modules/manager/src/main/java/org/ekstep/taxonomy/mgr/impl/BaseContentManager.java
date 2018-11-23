package org.ekstep.taxonomy.mgr.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
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
import org.ekstep.graph.dac.enums.GraphDACParams;
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
import org.ekstep.taxonomy.enums.TaxonomyAPIParams;
import org.ekstep.telemetry.logger.TelemetryManager;
import scala.concurrent.Await;
import scala.concurrent.Future;

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

	protected static final String DIALCODE_GENERATE_URI = Platform.config.hasPath("dialcode.api.generate.url")
			? Platform.config.getString("dialcode.api.generate.url") : "http://localhost:8080/learning-service/v3/dialcode/generate";

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
					if(StringUtils.equalsIgnoreCase((String) node.getMetadata().get(TaxonomyAPIParams.status.name()),
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

		if (StringUtils.equalsIgnoreCase("edit", mode)) {
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
	private Response updateDataNode(Node node) {
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

	private Response updateNode(String identifier, String objectType, Node domainNode) {
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

	private List<String> getExternalPropsList(DefinitionDTO definition) {
		List<String> list = new ArrayList<>();
		if (null != definition) {
			List<MetadataDefinition> props = definition.getProperties();
			if (null != props && !props.isEmpty()) {
				for (MetadataDefinition prop : props) {
					if (StringUtils.equalsIgnoreCase("external", prop.getDataType())) {
						list.add(prop.getPropertyName().trim());
					}
				}
			}
		}
		return list;
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
	private Response makeLearningRequest(Request request) {
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

	private Response updateContentProperties(String contentId, Map<String, Object> properties) {
		Request request = new Request();
		request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
		request.setOperation(ContentStoreOperations.updateContentProperties.name());
		request.put(ContentStoreParams.content_id.name(), contentId);
		request.put(ContentStoreParams.properties.name(), properties);
		Response response = makeLearningRequest(request);
		return response;
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
				if (StringUtils.equalsIgnoreCase(ContentAPIParams.screenshots.name(), prop) && null != map.get(prop)) {
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

	protected void validateContentForReservedDialcodes(Map<String, Object> metaData) {
		List<String> validContentType = Platform.config.hasPath("learning.reserve_dialcode.content_type") ?
				Platform.config.getStringList("learning.reserve_dialcode.content_type") :
				Arrays.asList("TextBook");

		if(!validContentType.contains(metaData.get(ContentAPIParams.contentType.name())))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_CONTENTTYPE.name(),
					"Invalid Content Type.");
	}

	private Optional<List<String>> getList(String[] params) {
		return Optional.ofNullable(params).filter(a -> a.length != 0).map(a -> new ArrayList<>(Arrays.asList(a)));
	}

	protected Optional<List<String>> getReservedDialCodes(Node node) {
		return getList((String[]) node.getMetadata().get(ContentAPIParams.reservedDialcodes.name()));
	}

	protected void validateChannel(Map<String, Object> metadata, String channelId) {
		if(!StringUtils.equals((String) metadata.get(ContentAPIParams.channel.name()), channelId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_INVALID_CHANNEL.name(), "Invalid Channel Id.");
	}

	protected Optional<List<NodeDTO>> getChildren(Node node, DefinitionDTO definition) {
		Map<String, Object> contentMap = ConvertGraphNode.convertGraphNode(node, TAXONOMY_ID, definition, null);
		return Optional.ofNullable((List<NodeDTO>) contentMap.get(ContentAPIParams.children.name()));
	}

	protected boolean isNodeVisibilityParent(Node node) {
		return StringUtils.equals(ContentAPIParams.Parent.name(),
				(String) node.getMetadata().get(ContentAPIParams.visibility.name()));
	}

	protected Optional<String[]> getDialcodes(Node node) {
		return Optional.ofNullable((String[]) node.getMetadata().get(ContentAPIParams.dialcodes.name())).filter(dialcodes -> dialcodes.length > 0);
	}

}
