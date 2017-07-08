package com.ilimi.taxonomy.mgr.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogger;;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.enums.ImportType;
import com.ilimi.graph.importer.InputStreamValue;
import com.ilimi.graph.importer.OutputStreamValue;
import com.ilimi.taxonomy.enums.TaxonomyAPIParams;
import com.ilimi.taxonomy.mgr.ITaxonomyManager;

@Component
public class TaxonomyManagerImpl extends BaseManager implements ITaxonomyManager {

	private static ILogger LOGGER = PlatformLogManager.getLogger();

	public static String[] taxonomyIds = { "numeracy", "literacy", "literacy_v2" };

	@Override
	public Response getSubGraph(String graphId, String id, Integer depth, List<String> relations) {
		if (StringUtils.isBlank(id))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Domain Id is blank");
		LOGGER.log("Find Taxonomy : " , id);
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "traverseSubGraph",
				GraphDACParams.start_node_id.name(), id);
		request.put(GraphDACParams.relations.name(), relations);
		if (null != depth && depth.intValue() > 0)
			request.put(GraphDACParams.depth.name(), depth);
		Response subGraphRes = getResponse(request, LOGGER);
		return subGraphRes;
	}

	@Override
	public Response create(String id, InputStream stream) {
		if (StringUtils.isBlank(id))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (null == stream)
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_EMPTY_INPUT_STREAM.name(),
					"Taxonomy object is emtpy");
		LOGGER.log("Create Taxonomy : " + stream);
		Request request = getRequest(id, GraphEngineManagers.GRAPH_MANAGER, "importGraph");
		request.put(GraphEngineParams.format.name(), ImportType.CSV.name());
		request.put(GraphEngineParams.input_stream.name(), new InputStreamValue(stream));
		Response createRes = getResponse(request, LOGGER);
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
					LOGGER.log("Error! While Closing the ByteArrayOutputStream [operation : create]", response,  e);
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
		LOGGER.log("Export Taxonomy : " + id , " | Format: " + format);
		Request request = new Request(req);
		setContext(request, id, GraphEngineManagers.GRAPH_MANAGER, "exportGraph");
		request.put(GraphEngineParams.format.name(), format);
		request.put(GraphEngineParams.search_criteria.name(), req.get(TaxonomyAPIParams.search_criteria.name()));
		Response exportRes = getResponse(request, LOGGER);
		return exportRes;
	}

	@Override
	public Response delete(String id) {
		if (StringUtils.isBlank(id))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		LOGGER.log("Delete Taxonomy : " , id);
		Request request = getRequest(id, GraphEngineManagers.GRAPH_MANAGER, "deleteGraph");
		return getResponse(request, LOGGER);
	}

	@Override
	public Response updateDefinition(String id, String json) {
		if (StringUtils.isBlank(id))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (StringUtils.isBlank(json))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_NULL_DEFINITION.name(),
					"Definition nodes JSON is empty");
		LOGGER.log("Update Definition : " , id);
		Request request = getRequest(id, GraphEngineManagers.NODE_MANAGER, "importDefinitions");
		request.put(GraphEngineParams.input_stream.name(), json);
		return getResponse(request, LOGGER);
	}

	@Override
	public Response findAllDefinitions(String id) {
		if (StringUtils.isBlank(id))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		LOGGER.log("Get All Definitions : " + id);
		Request request = getRequest(id, GraphEngineManagers.SEARCH_MANAGER, "getAllDefinitions");
		return getResponse(request, LOGGER);
	}

	@Override
	public Response findDefinition(String id, String objectType) {
		if (StringUtils.isBlank(id))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_NULL_DEFINITION.name(), "Object Type is empty");
		LOGGER.log("Get Definition : " + id , " : Object Type : " + objectType);
		Request request = getRequest(id, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinitionFromCache",
				GraphDACParams.object_type.name(), objectType);
		return getResponse(request, LOGGER);
	}

	@Override
	public Response deleteDefinition(String id, String objectType) {
		if (StringUtils.isBlank(id))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_NULL_DEFINITION.name(), "Object Type is empty");
		LOGGER.log("Delete Definition : " + id , " : Object Type : " + objectType);
		Request request = getRequest(id, GraphEngineManagers.NODE_MANAGER, "deleteDefinition",
				GraphDACParams.object_type.name(), objectType);
		return getResponse(request, LOGGER);
	}

	@Override
	public Response createIndex(String id, List<String> keys, Boolean unique) {
		if (StringUtils.isBlank(id))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
		if (null == keys || keys.isEmpty())
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_EMPTY_INDEX_KEYS.name(),
					"Property keys are empty");
		LOGGER.log("Create Index : " + id , " : Keys : " + keys);
		Request request = null;
		if (null != unique && unique)
			request = getRequest(id, GraphEngineManagers.GRAPH_MANAGER, "createUniqueConstraint",
					GraphDACParams.property_keys.name(), keys);
		else
			request = getRequest(id, GraphEngineManagers.GRAPH_MANAGER, "createIndex",
					GraphDACParams.property_keys.name(), keys);
		return getResponse(request, LOGGER);
	}

	@Override
	public Response findAllByObjectType(String graphId, String objectType) {
		LOGGER.log("Find All nodes by Object type");
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getNodesByObjectType");
		request.put(GraphDACParams.object_type.name(), objectType);
		request.put(GraphDACParams.get_tags.name(), true);
		Response response = getResponse(request, LOGGER);
		return response;
	}

}
