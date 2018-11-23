package org.ekstep.taxonomy.mgr.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.mgr.BaseManager;
import org.ekstep.graph.common.enums.GraphEngineParams;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.enums.ImportType;
import org.ekstep.graph.importer.InputStreamValue;
import org.ekstep.graph.importer.OutputStreamValue;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.graph.model.node.MetadataDefinition;
import org.ekstep.graph.model.node.RelationDefinition;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.taxonomy.enums.TaxonomyAPIParams;
import org.ekstep.taxonomy.mgr.ITaxonomyManager;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Component
public class TaxonomyManagerImpl extends BaseManager implements ITaxonomyManager {

	private ControllerUtil util = new ControllerUtil();
	private ObjectMapper mapper = new ObjectMapper();

	public static String[] taxonomyIds = { "numeracy", "literacy", "literacy_v2" };

	@Override
	public Response getSubGraph(String graphId, String id, Integer depth, List<String> relations) {
		if (StringUtils.isBlank(id))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Domain Id is blank");
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "traverseSubGraph",
				GraphDACParams.start_node_id.name(), id);
		request.put(GraphDACParams.relations.name(), relations);
		if (null != depth && depth.intValue() > 0)
			request.put(GraphDACParams.depth.name(), depth);
		Response subGraphRes = getResponse(request);
		return subGraphRes;
	}

	@Override
	public Response create(String id, InputStream stream) {
		if (StringUtils.isBlank(id))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (null == stream)
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_EMPTY_INPUT_STREAM.name(),
					"Taxonomy object is emtpy");
		TelemetryManager.log("Create Taxonomy : " + stream);
		Request request = getRequest(id, GraphEngineManagers.GRAPH_MANAGER, "importGraph");
		request.put(GraphEngineParams.format.name(), ImportType.CSV.name());
		request.put(GraphEngineParams.input_stream.name(), new InputStreamValue(stream));
		Response createRes = getResponse(request);
		if (checkError(createRes)) {
			return createRes;
		} else {
			Response response = copyResponse(createRes);
			OutputStreamValue os = (OutputStreamValue) createRes.get(GraphEngineParams.output_stream.name());
			if (null != os && null != os.getOutputStream() && null != os.getOutputStream().toString()) {
				try (ByteArrayOutputStream bos = (ByteArrayOutputStream) os.getOutputStream()) {
					String csv = new String(bos.toByteArray());
					response.put(TaxonomyAPIParams.taxonomy.name(), csv);
				} catch (IOException e) {
					TelemetryManager.error("Error! While Closing the ByteArrayOutputStream [operation : create]",  e);
				}
			}
			return response;
		}
	}

	@Override
	public Response export(String id, Request req) {
		if (StringUtils.isBlank(id))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		String format = (String) req.get(GraphEngineParams.format.name());
		TelemetryManager.log("Export Taxonomy : " + id + " | Format: " + format);
		Request request = new Request(req);
		setContext(request, id, GraphEngineManagers.GRAPH_MANAGER, "exportGraph");
		request.put(GraphEngineParams.format.name(), format);
		request.put(GraphEngineParams.search_criteria.name(), req.get(TaxonomyAPIParams.search_criteria.name()));
		Response exportRes = getResponse(request);
		return exportRes;
	}

	@Override
	public Response delete(String id) {
		if (StringUtils.isBlank(id))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		TelemetryManager.log("Delete Taxonomy : " + id);
		Request request = getRequest(id, GraphEngineManagers.GRAPH_MANAGER, "deleteGraph");
		return getResponse(request);
	}

	@Override
	public Response updateDefinition(String id, String json) {
		if (StringUtils.isBlank(id))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (StringUtils.isBlank(json))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_NULL_DEFINITION.name(),
					"Definition nodes JSON is empty");
		TelemetryManager.log("Update Definition : " + id);
		Request request = getRequest(id, GraphEngineManagers.NODE_MANAGER, "importDefinitions");
		request.put(GraphEngineParams.input_stream.name(), json);
		return getResponse(request);
	}

	@Override
	public Response findAllDefinitions(String id) {
		if (StringUtils.isBlank(id))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		TelemetryManager.log("Get All Definitions : " + id);
		Request request = getRequest(id, GraphEngineManagers.SEARCH_MANAGER, "getAllDefinitions");
		return getResponse(request);
	}

	@Override
	public Response findDefinition(String id, String objectType) {
		if (StringUtils.isBlank(id))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_NULL_DEFINITION.name(), "Object Type is empty");
		TelemetryManager.log("Get Definition : " + id + " : Object Type : " + objectType);
		Request request = getRequest(id, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinitionFromCache",
				GraphDACParams.object_type.name(), objectType);
		return getResponse(request);
	}

	@Override
	public Response deleteDefinition(String id, String objectType) {
		if (StringUtils.isBlank(id))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_NULL_DEFINITION.name(), "Object Type is empty");
		TelemetryManager.log("Delete Definition : " + id + " : Object Type : " + objectType);
		Request request = getRequest(id, GraphEngineManagers.NODE_MANAGER, "deleteDefinition",
				GraphDACParams.object_type.name(), objectType);
		return getResponse(request);
	}

	@Override
	public Response createIndex(String id, List<String> keys, Boolean unique) {
		if (StringUtils.isBlank(id))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (null == keys || keys.isEmpty())
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_EMPTY_INDEX_KEYS.name(),
					"Property keys are empty");
		TelemetryManager.log("Create Index : " + id + " : Keys : " + keys);
		Request request = null;
		if (null != unique && unique)
			request = getRequest(id, GraphEngineManagers.GRAPH_MANAGER, "createUniqueConstraint",
					GraphDACParams.property_keys.name(), keys);
		else
			request = getRequest(id, GraphEngineManagers.GRAPH_MANAGER, "createIndex",
					GraphDACParams.property_keys.name(), keys);
		return getResponse(request);
	}

	@Override
	public Response findAllByObjectType(String graphId, String objectType) {
		TelemetryManager.log("Find All nodes by Object type");
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getNodesByObjectType");
		request.put(GraphDACParams.object_type.name(), objectType);
		request.put(GraphDACParams.get_tags.name(), true);
		Response response = getResponse(request);
		return response;
	}

	private <T> List<T> toObject(List<Map<String, Object>> list, Class<T> clazz) {
		return Optional.ofNullable(list).
				filter(l -> l.size() != 0).
				map(l -> l.
						stream().
						filter(e -> e != null).
						map(e -> mapper.convertValue(e, clazz)).
						collect(toList())).
				orElseGet(ArrayList::new);
	}

    @Override
    public Response updateDefinition(String id, String objectType, Map<String, Object> request) {
		if (StringUtils.isBlank(id))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_OBJECT_TYPE.name(), "Object Type is blank");

		DefinitionDTO definitionDTO = util.getDefinition(id, objectType);
		DefinitionDTO definition;
		try {
			definition = mapper.readValue(mapper.writeValueAsString(definitionDTO),
					new TypeReference<DefinitionDTO>() {});
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServerException(TaxonomyErrorCodes.ERR_UPDATING_DEFINIITON.name(),
					"Error while Updating Definition", e);
		}

		Map<String, Object> requestDefinition =
				Optional.ofNullable((Map<String, Object>) request.get("definition")).
				orElseThrow(() -> new ClientException("ERROR_INVALID_REQUEST", "Definition cannot be empty"));

		List<MetadataDefinition> md =
				toObject((List<Map<String, Object>>) requestDefinition.get("properties"), MetadataDefinition.class);

		List<RelationDefinition> inRelationDefinitions =
				toObject((List<Map<String, Object>>) requestDefinition.get("inRelations"), RelationDefinition.class);

		List<RelationDefinition> outRelationDefinitions =
				toObject((List<Map<String, Object>>) requestDefinition.get("outRelations"), RelationDefinition.class);

		if (!md.isEmpty() || !inRelationDefinitions.isEmpty() || !outRelationDefinitions.isEmpty()) {
			definition.getProperties().addAll(md);
			definition.getInRelations().addAll(inRelationDefinitions);
			definition.getOutRelations().addAll(outRelationDefinitions);

			String updatedDefinitionJson;
			try {
				updatedDefinitionJson = "{\"definitionNodes\": [" + mapper.writeValueAsString(definition) + "]}";
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				throw new ServerException(TaxonomyErrorCodes.ERR_UPDATING_DEFINIITON.name(),
						"Error while Updating Definition", e);
			}
			Request req = getRequest(id, GraphEngineManagers.NODE_MANAGER, "importDefinitions");
			req.put(GraphEngineParams.input_stream.name(), updatedDefinitionJson);

			Response response = getResponse(req);
			if (!checkError(response)) {
				TelemetryManager.info("Updated Definition for Object Type:: " + objectType);
			} else {
				TelemetryManager.error("Could No Update Definition for Object Type:: " + objectType);
			}
			return response;
		} else {
			TelemetryManager.error("Could Not Update Definition for Object Type:: " + objectType);
			return ERROR(TaxonomyErrorCodes.ERR_UPDATING_DEFINIITON.name(), "Provide Property or Relation for Updating",
					ResponseCode.CLIENT_ERROR);
		}
    }

}
