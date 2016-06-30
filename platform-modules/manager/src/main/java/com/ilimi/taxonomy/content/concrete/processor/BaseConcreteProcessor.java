package com.ilimi.taxonomy.content.concrete.processor;

import java.io.File;
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
	
	public Response updateNode(String contentId, Map<String, Object> fields) {
		Response response = new Response();
		return response; 
	}
	
	public List<Media> getMedia(Plugin content) {
		List<Media> medias = new ArrayList<Media>();
		if (null != content) {
			medias = content.getManifest().getMedias();
		}
		return medias;
	}
	
	public List<String> getMediaSrcList(List<Media> medias) {
		List<String> mediaSrcList = new ArrayList<String>();
		if (null != medias) {
			for (Media media: medias) {
				String src = media.getData().get(ContentWorkflowPipelineParams.src.name());
				if (!StringUtils.isBlank(src))
					mediaSrcList.add(src);
			}
		}
		return mediaSrcList;
	}
	
	public Map<String, String> getMediaSrcMap(List<Media> medias) {
		Map<String, String> srcMap = new HashMap<String, String>();
		if (null != medias) {
			for (Media media: medias) {
				Map<String, String> data = media.getData();
				if (null != data) {
					String src = data.get(ContentWorkflowPipelineParams.src.name());
					String type = data.get(ContentWorkflowPipelineParams.type.name());
					if (!StringUtils.isBlank(src) &&
							!StringUtils.isBlank(type))
						srcMap.put(src, type);
				}
			}
		}
		return srcMap;
	}
	
	public Map<String, String> getNonAssetObjMediaSrcMap(List<Media> medias) {
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
	
	public List<Media> getUpdatedMediaWithUrl(Map<String, String> urlMap, List<Media> mediaList) {
		List<Media> medias = new ArrayList<Media>();
		if (null != urlMap && null != mediaList) {
			for (Media media: mediaList) {
				if (null != media.getData()) {
					String uUrl = urlMap.get(media.getData().get(ContentWorkflowPipelineParams.src.name()));
					if (!StringUtils.isBlank(uUrl))
						media.getData().put(ContentWorkflowPipelineParams.src.name(), uUrl);
				}
				medias.add(media);
			}
		}
		return medias;
	}
	
	public List<Media> getUpdatedMediaWithAssetId(Map<String, String> assetIdMap, List<Media> mediaList) {
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
	
	public List<File> getControllersFileList(List<Controller> controllers, String type, String basePath) {
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
	
	public Response createContentNode(Map<String, Object> map) {
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
	
	public Response updateContentNode(Node node, Map<String, Object> map) {
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
	
	private Node updateDataNode(Node node, Map<String, Object> map) {
		if (null != map && null != node) {
			for (Entry<String, Object> entry: map.entrySet())
				node.getMetadata().put(entry.getKey(), entry.getValue());
		}
		return node;
	}
	
	private Node getDataNode(Map<String, Object> map) {
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
	public List<String> createRelation(String graphId, Map<String, Object> mapRelation,
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
	
	public Response addRelation(String graphId, String objectId1, String relation, String objectId2) {
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
	
	public boolean isWidgetTypeAsset(String assetType) {
		return StringUtils.equalsIgnoreCase(assetType, ContentWorkflowPipelineParams.js.name()) ||
				StringUtils.equalsIgnoreCase(assetType, ContentWorkflowPipelineParams.css.name()) ||
				StringUtils.equalsIgnoreCase(assetType, ContentWorkflowPipelineParams.plugin.name());
	}
	
	public void createDirectoryIfNeeded(String directoryName) {
        File theDir = new File(directoryName);
        if (!theDir.exists()) {
            theDir.mkdir();
        }
    }
	
}
