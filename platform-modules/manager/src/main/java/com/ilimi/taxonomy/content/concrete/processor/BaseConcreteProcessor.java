package com.ilimi.taxonomy.content.concrete.processor;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.taxonomy.content.common.ContentConfigurationConstants;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.entity.Controller;
import com.ilimi.taxonomy.content.entity.Media;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.enums.ContentAPIParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;

public class BaseConcreteProcessor extends BaseManager {
	
	private static Logger LOGGER = LogManager.getLogger(BaseConcreteProcessor.class.getName());
	
	protected List<Media> getMedia(Plugin content) {
		List<Media> medias = new ArrayList<Media>();
		if (null != content && null != content.getManifest()) {
			medias = content.getManifest().getMedias();
		}
		return medias;
	}
	
	protected Map<String, String> getNonAssetObjMediaSrcMap(List<Media> medias) {
		Map<String, String> srcMap = new HashMap<String, String>();
		if (null != medias) {
			for (Media media: medias) {
				Map<String, String> data = media.getData();
				if (null != data && data.containsKey(ContentWorkflowPipelineParams.assetId.name())) {
					if (StringUtils.isBlank(data.get(ContentWorkflowPipelineParams.assetId.name()))) {
						String src = data.get(ContentWorkflowPipelineParams.src.name());
						String type = data.get(ContentWorkflowPipelineParams.type.name());
						if (!StringUtils.isBlank(src) &&
								!StringUtils.isBlank(type))
							srcMap.put(src, type);
					}
				}
			}
		}
		return srcMap;
	}
	
	protected List<Media> getUpdatedMediaWithUrl(Map<String, String> urlMap, List<Media> mediaList) {
		List<Media> medias = new ArrayList<Media>();
		if (null != urlMap && null != mediaList) {
			for (Media media: mediaList) {
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
	
	protected List<Media> getUpdatedMediaWithAssetId(Map<String, String> assetIdMap, List<Media> mediaList) {
		List<Media> medias = new ArrayList<Media>();
		if (null != assetIdMap && null != mediaList) {
			for (Media media: mediaList) {
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
	
	protected List<File> getControllersFileList(List<Controller> controllers, String type, String basePath) {
		List<File> controllerFileList = new ArrayList<File>();
		if (null != controllers && !StringUtils.isBlank(type) && !StringUtils.isBlank(basePath)) {
			for (Controller controller: controllers) {
				if (null != controller.getData()) {
					if (StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.items.name(), 
							controller.getData().get(ContentWorkflowPipelineParams.type.name()))) {
						String controllerId = controller.getData().get(ContentWorkflowPipelineParams.id.name());
						if (!StringUtils.isBlank(controllerId))
							controllerFileList.add(new File(basePath + File.separator + 
									ContentWorkflowPipelineParams.items.name() + File.separator + controllerId + 
									ContentConfigurationConstants.ITEM_CONTROLLER_FILE_EXTENSION));
					}
				}
			}
		}
		return controllerFileList;
	}
	
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
	
	protected Response updateContentNode(Node node, Map<String, Object> map) {
		Response response = new Response();
		if (null != map && null != node) {
			node = updateDataNode(node, map);
			Request validateReq = getRequest(ContentConfigurationConstants.GRAPH_ID, GraphEngineManagers.NODE_MANAGER,
					ContentWorkflowPipelineParams.validateNode.name());
			validateReq.put(GraphDACParams.node.name(), node);
			Response validateRes = getResponse(validateReq, LOGGER);
			if (checkError(validateRes)) {
				response =  validateRes;
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
	
	protected Node updateDataNode(Node node, Map<String, Object> map) {
		if (null != map && null != node) {
			for (Entry<String, Object> entry: map.entrySet())
				node.getMetadata().put(entry.getKey(), entry.getValue());
		}
		return node;
	}
	
	protected Node getDataNode(Map<String, Object> map) {
		Node node = new Node();
		if (null != map) {
			Map<String, Object> metadata = new HashMap<String, Object>();
			node.setIdentifier((String) map.get(ContentWorkflowPipelineParams.identifier.name()));
			node.setObjectType(ContentWorkflowPipelineParams.Content.name());
			for (Entry<String, Object> entry: map.entrySet())
				metadata.put(entry.getKey(), entry.getValue());
			node.setMetadata(metadata);
		}
		return node;
	}
	
	@SuppressWarnings("unchecked")
	protected List<String> createRelation(String graphId, Map<String, Object> mapRelation,
			List<Relation> outRelations) {
		if (null != mapRelation) {
			List<String> lstResponse = new ArrayList<String>();
			for (Entry<String, Object> entry : mapRelation.entrySet()) {
				List<Map<String, Object>> lstConceptMap = (List<Map<String, Object>>) entry.getValue();
				if (null != lstConceptMap && !lstConceptMap.isEmpty()) {
					for (Map<String, Object> conceptMap : lstConceptMap) {
						String conceptId = (String) conceptMap.get(ContentAPIParams.identifier.name());
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
	
	protected boolean isWidgetTypeAsset(String assetType) {
		return StringUtils.equalsIgnoreCase(assetType, ContentWorkflowPipelineParams.js.name()) ||
				StringUtils.equalsIgnoreCase(assetType, ContentWorkflowPipelineParams.css.name()) ||
				StringUtils.equalsIgnoreCase(assetType, ContentWorkflowPipelineParams.plugin.name());
	}
	
	protected void createDirectoryIfNeeded(String directoryName) {
        File theDir = new File(directoryName);
        if (!theDir.exists()) {
            theDir.mkdir();
        }
    }
	
	protected boolean isValidBasePath(String path) {
		boolean isValid = true;
		try {
			LOGGER.info("Validating the Base Path: " + path);
			isValid = isPathExist(Paths.get(path));
		} catch (InvalidPathException |  NullPointerException e) {
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
	
}
