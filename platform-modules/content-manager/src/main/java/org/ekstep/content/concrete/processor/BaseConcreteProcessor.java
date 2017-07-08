package org.ekstep.content.concrete.processor;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.content.common.ContentConfigurationConstants;
import org.ekstep.content.entity.Controller;
import org.ekstep.content.entity.Media;
import org.ekstep.content.entity.Plugin;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.processor.ContentPipelineProcessor;
import org.ekstep.learning.common.enums.ContentErrorCodes;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.engine.router.GraphEngineManagers;

/**
 * The Class BaseConcreteProcessor provides the common utility methods for all
 * the Concrete Processor.
 * 
 * @author Mohammad Azharuddin
 * 
 * @see AssessmentItemCreatorProcessor
 * @see AssetCreatorProcessor
 * @see AssetsValidatorProcessor
 * @see BaseConcreteProcessor
 * @see EmbedControllerProcessor
 * @see GlobalizeAssetProcessor
 * @see LocalizeAssetProcessor
 * @see MissingAssetValidatorProcessor
 * @see MissingControllerValidatorProcessor
 * @see ContentPipelineProcessor
 */
public class BaseConcreteProcessor extends BaseManager {

	/** The logger. */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	/**
	 * Gets the media.
	 *
	 * @param content
	 *            the content is the ECRF Object.
	 * @return the media from the given ECRF Object.
	 */
	protected List<Media> getMedia(Plugin content) {
		List<Media> medias = new ArrayList<Media>();
		if (null != content && null != content.getManifest()) {
			medias = content.getManifest().getMedias();
		}
		return medias;
	}

	/**
	 * Gets the non-asset object media 'src' map.
	 *
	 * @param medias
	 *            the medias is the set of media from ECRF.
	 * @return the map of non-asset 'id' and 'type'
	 */
	protected Map<String, String> getNonAssetObjMediaSrcMap(List<Media> medias) {
		Map<String, String> srcMap = new HashMap<String, String>();
		if (null != medias) {
			for (Media media : medias) {
				Map<String, Object> data = media.getData();
				if (null != data && data.containsKey(ContentWorkflowPipelineParams.assetId.name())) {
					Object obj = data.get(ContentWorkflowPipelineParams.assetId.name());
					if (null == obj || StringUtils.isBlank(obj.toString())) {
						String src = String.valueOf(data.get(ContentWorkflowPipelineParams.src.name()));
						String type = String.valueOf(data.get(ContentWorkflowPipelineParams.type.name()));
						if (!StringUtils.isBlank(src) && !StringUtils.isBlank(type))
							srcMap.put(src, type);
					}
				}
			}
		}
		return srcMap;
	}

	/**
	 * Gets the updated media with url.
	 *
	 * @param urlMap
	 *            the url map
	 * @param mediaList
	 *            the media list
	 * @return the updated media with url
	 */
	protected List<Media> getUpdatedMediaWithUrl(Map<String, String> urlMap, List<Media> mediaList) {
		List<Media> medias = new ArrayList<Media>();
		if (null != urlMap && null != mediaList) {
			for (Media media : mediaList) {
				if (null != media.getData()) {
					String uUrl = urlMap.get(media.getId());
					if (!StringUtils.isBlank(uUrl)) {
						media.getData().put(ContentWorkflowPipelineParams.src.name(), uUrl);
						media.setSrc(uUrl);
					}
				}
				medias.add(media);
			}
		}
		return medias;
	}

	/**
	 * Gets the updated media with asset id.
	 *
	 * @param assetIdMap
	 *            the asset id map
	 * @param mediaList
	 *            the media list
	 * @return the updated media with asset id
	 */
	protected List<Media> getUpdatedMediaWithAssetId(Map<String, String> assetIdMap, List<Media> mediaList) {
		List<Media> medias = new ArrayList<Media>();
		if (null != assetIdMap && null != mediaList) {
			for (Media media : mediaList) {
				if (null != media.getData()) {
					String assetId = assetIdMap.get(media.getData().get(ContentWorkflowPipelineParams.src.name()));
					if (!StringUtils.isBlank(assetId))
						media.getData().put(ContentWorkflowPipelineParams.assetId.name(), assetId);
				}
				medias.add(media);
			}
		}
		return medias;
	}

	/**
	 * Gets the controllers file list.
	 *
	 * @param controllers
	 *            the controllers
	 * @param type
	 *            the type
	 * @param basePath
	 *            the base path
	 * @return the controllers file list
	 */
	protected List<File> getControllersFileList(List<Controller> controllers, String type, String basePath) {
		List<File> controllerFileList = new ArrayList<File>();
		if (null != controllers && !StringUtils.isBlank(type) && !StringUtils.isBlank(basePath)) {
			for (Controller controller : controllers) {
				if (null != controller.getData()) {
					Object objType = controller.getData().get(ContentWorkflowPipelineParams.type.name());
					String ctrlType = ((null == objType) ? "" : objType.toString());
					if (StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.items.name(), ctrlType)) {
						String controllerId = String
								.valueOf(controller.getData().get(ContentWorkflowPipelineParams.id.name()));
						if (!StringUtils.isBlank(controllerId))
							controllerFileList.add(new File(basePath + File.separator
									+ ContentWorkflowPipelineParams.items.name() + File.separator + controllerId
									+ ContentConfigurationConstants.ITEM_CONTROLLER_FILE_EXTENSION));
					}
				}
			}
		}
		return controllerFileList;
	}

	/**
	 * Creates the content node.
	 *
	 * @param map
	 *            the map
	 * @return the response
	 */
	protected Response createContentNode(Map<String, Object> map) {
		Response response = new Response();
		if (null != map) {
			Node node = getDataNode(map);
			Request validateReq = getRequest(ContentConfigurationConstants.GRAPH_ID, GraphEngineManagers.NODE_MANAGER,
					ContentWorkflowPipelineParams.validateNode.name());
			validateReq.put(GraphDACParams.node.name(), node);
			Response validateRes = getResponse(validateReq, LOGGER);
			if (checkError(validateRes)) {
				response = validateRes;
			} else {
				Request createReq = getRequest(ContentConfigurationConstants.GRAPH_ID, GraphEngineManagers.NODE_MANAGER,
						ContentWorkflowPipelineParams.createDataNode.name());
				createReq.put(GraphDACParams.node.name(), node);
				response = getResponse(createReq, LOGGER);
			}
		}
		return response;
	}

	/**
	 * Update content node.
	 *
	 * @param node
	 *            the node
	 * @param map
	 *            the map
	 * @return the response
	 */
	protected Response updateContentNode(Node node, Map<String, Object> map) {
		Response response = new Response();
		if (null != map && null != node) {
			node = updateDataNode(node, map);
			Request validateReq = getRequest(ContentConfigurationConstants.GRAPH_ID, GraphEngineManagers.NODE_MANAGER,
					ContentWorkflowPipelineParams.validateNode.name());
			validateReq.put(GraphDACParams.node.name(), node);
			Response validateRes = getResponse(validateReq, LOGGER);
			if (checkError(validateRes)) {
				response = validateRes;
			} else {
				Request updateReq = getRequest(ContentConfigurationConstants.GRAPH_ID, GraphEngineManagers.NODE_MANAGER,
						ContentWorkflowPipelineParams.updateDataNode.name());
				updateReq.put(GraphDACParams.node.name(), node);
				updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
				response = getResponse(updateReq, LOGGER);
			}
		}
		return response;
	}

	/**
	 * Update data node.
	 *
	 * @param node
	 *            the node
	 * @param map
	 *            the map
	 * @return the node
	 */
	protected Node updateDataNode(Node node, Map<String, Object> map) {
		if (null != map && null != node) {
			for (Entry<String, Object> entry : map.entrySet())
				node.getMetadata().put(entry.getKey(), entry.getValue());
		}
		return node;
	}

	/**
	 * Gets the data node.
	 *
	 * @param map
	 *            the map
	 * @return the data node
	 */
	protected Node getDataNode(Map<String, Object> map) {
		Node node = new Node();
		if (null != map) {
			Map<String, Object> metadata = new HashMap<String, Object>();
			node.setIdentifier((String) map.get(ContentWorkflowPipelineParams.identifier.name()));
			node.setObjectType(ContentWorkflowPipelineParams.Content.name());
			for (Entry<String, Object> entry : map.entrySet())
				metadata.put(entry.getKey(), entry.getValue());
			node.setMetadata(metadata);
		}
		return node;
	}

	/**
	 * Creates the relation.
	 *
	 * @param graphId
	 *            the graph id
	 * @param mapRelation
	 *            the map relation
	 * @param outRelations
	 *            the out relations
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	protected List<String> createRelation(String graphId, Map<String, Object> mapRelation,
			List<Relation> outRelations) {
		if (null != mapRelation) {
			List<String> lstResponse = new ArrayList<String>();
			for (Entry<String, Object> entry : mapRelation.entrySet()) {
				List<Map<String, Object>> lstConceptMap = (List<Map<String, Object>>) entry.getValue();
				if (null != lstConceptMap && !lstConceptMap.isEmpty()) {
					for (Map<String, Object> conceptMap : lstConceptMap) {
						String conceptId = (String) conceptMap.get(ContentWorkflowPipelineParams.identifier.name());
						Response response = addRelation(graphId, entry.getKey(),
								RelationTypes.ASSOCIATED_TO.relationName(), conceptId);
						lstResponse.add(response.getResult().toString());
						Relation outRel = new Relation(null, RelationTypes.ASSOCIATED_TO.relationName(), conceptId);
						outRelations.add(outRel);
					}
				}
			}
			return lstResponse;
		}
		return null;
	}

	/**
	 * Adds the relation.
	 *
	 * @param graphId
	 *            the graph id
	 * @param objectId1
	 *            the object id 1
	 * @param relation
	 *            the relation
	 * @param objectId2
	 *            the object id 2
	 * @return the response
	 */
	protected Response addRelation(String graphId, String objectId1, String relation, String objectId2) {
		if (StringUtils.isBlank(graphId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Invalid graph Id");
		if (StringUtils.isBlank(objectId1) || StringUtils.isBlank(objectId2))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(), "Object Id is blank");
		if (StringUtils.isBlank(relation))
			throw new ClientException(ContentErrorCodes.ERR_INVALID_RELATION_NAME.name(), "Relation name is blank");
		Request request = getRequest(graphId, GraphEngineManagers.GRAPH_MANAGER, "createRelation");
		request.put(GraphDACParams.start_node_id.name(), objectId1);
		request.put(GraphDACParams.relation_type.name(), relation);
		request.put(GraphDACParams.end_node_id.name(), objectId2);
		Response response = getResponse(request, LOGGER);
		return response;
	}

	/**
	 * Checks if is widget type asset.
	 *
	 * @param assetType
	 *            the asset type
	 * @return true, if is widget type asset
	 */
	protected boolean isWidgetTypeAsset(String assetType) {
		return StringUtils.equalsIgnoreCase(assetType, ContentWorkflowPipelineParams.js.name())
				|| StringUtils.equalsIgnoreCase(assetType, ContentWorkflowPipelineParams.css.name())
				|| StringUtils.equalsIgnoreCase(assetType, ContentWorkflowPipelineParams.json.name())
				|| StringUtils.equalsIgnoreCase(assetType, ContentWorkflowPipelineParams.plugin.name());
	}

	/**
	 * Creates the directory if needed.
	 *
	 * @param directoryName
	 *            the directory name
	 */
	protected void createDirectoryIfNeeded(String directoryName) {
		File theDir = new File(directoryName);
		if (!theDir.exists()) {
			theDir.mkdirs();
		}
	}

	/**
	 * Checks if is valid base path.
	 *
	 * @param path
	 *            the path
	 * @return true, if is valid base path
	 */
	protected boolean isValidBasePath(String path) {
		boolean isValid = true;
		try {
			LOGGER.log("Validating the Base Path: " , path);
			isValid = isPathExist(Paths.get(path));
		} catch (InvalidPathException | NullPointerException e) {
			isValid = false;
		}
		return isValid;
	}

	/**
	 * Checks if is path exist.
	 *
	 * @param path
	 *            the path
	 * @return true, if is path exist
	 */
	protected boolean isPathExist(Path path) {
		boolean exist = true;
		try {
			if (null != path) {
				LOGGER.log("Creating the Base Path: " + path.getFileName());
				if (!Files.exists(path))
					Files.createDirectories(path);
			}
		} catch (FileAlreadyExistsException e) {
			LOGGER.log("Base Path Already Exist: " , path.getFileName(), e, "WARN");
		} catch (Exception e) {
			exist = false;
			LOGGER.log("Error! Something went wrong while creating the path - " , path.getFileName(), e);
		}
		return exist;
	}

}
