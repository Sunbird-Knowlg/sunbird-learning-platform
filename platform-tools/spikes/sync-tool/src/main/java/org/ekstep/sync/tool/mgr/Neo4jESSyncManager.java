package org.ekstep.sync.tool.mgr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.CompositeSearchErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.sync.tool.util.CSVFileParserUtil;
import org.ekstep.sync.tool.util.ElasticSearchConnector;
import org.ekstep.sync.tool.util.GraphUtil;
import org.ekstep.sync.tool.util.SyncMessageGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("neo4jESSyncManager")
public class Neo4jESSyncManager implements ISyncManager {

	private ControllerUtil util = new ControllerUtil();
	private static int batchSize = 500;
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private ElasticSearchConnector esConnector;

	@PostConstruct
	private void init() throws Exception {
		int batch = Platform.config.hasPath("batch.size") ? Platform.config.getInt("batch.size")
				: 500;
		batchSize = batch;
	}
	
	public void syncByIds(String graphId, String[] ids) throws Exception {
		List<String> identifiers = new ArrayList<>(Arrays.asList(ids));
		syncNode(graphId, identifiers, null);
	}

	public void syncByFile(String graphId, String filePath, String objectType) throws Exception {
		List<String> identifiers;
		identifiers = CSVFileParserUtil.getIdentifiers(filePath, objectType);
		syncNode(graphId, identifiers, objectType);
	}

	public void syncByDateRange(String graphId, String startDate, String endDate, String objectType) throws Exception {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
		inputFormat.setLenient(false);

		Date start;
		Date end;
		try {
			start = inputFormat.parse(startDate);
			end = inputFormat.parse(endDate);
		} catch (ParseException e) {
			throw new ClientException("ERR_DATE_FORMAT", "DATE Should be in the format of yyyy-MM-dd");
		}

		long diffInMillies = end.getTime() - start.getTime();
		if (diffInMillies < 0)
			throw new ClientException("ERR_DATE_RANGE_INCORRECT",
					"End Date should be more than or equal to Start Date");

		long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
		if (diff > 364)
			throw new ClientException("ERR_DATE_RANGE_MORE_THAN_TEAR",
					"Date range differnce should not be more than a year");

		startDate = startDate.replaceAll("-", "");
		endDate = endDate.replaceAll("-", "");
		List<String> ids = util.getNodesWithInDateRange(graphId, objectType, startDate, endDate);

		if (!ids.isEmpty()) {
			System.out.println(ids.size() + " node(s) got matched with given condition(s) for sync");
			syncNode(graphId, ids, objectType);
		} else {
			System.out.println("No node got matched with given condition(s)");
		}
	}

	public void syncByObjectType(String graphId, String objectType) throws Exception {
		if (StringUtils.isBlank(graphId))
			throw new ClientException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_SYNC_BLANK_GRAPH_ID.name(),
					"Graph Id is blank.");
		Map<String, String> errors = null;
		DefinitionDTO def;
		if (StringUtils.isNotBlank(objectType)) {
			def = util.getDefinition(graphId, objectType);
			if (null != def) {
				Map<String, Object> definition = mapper.convertValue(def, new TypeReference<Map<String, Object>>() {
				});
				Map<String, String> relationMap = GraphUtil.getRelationMap(objectType, definition);
				SyncMessageGenerator.definitionMap.put(objectType, relationMap);
				int start = 0;
				boolean found = true;
				while (found) {
					List<Node> nodes = null;
					try {
						nodes = util.getNodes(graphId, def.getObjectType(), start, batchSize);
					}catch(ResourceNotFoundException e) {
						System.out.println("error while fetching neo4j records for objectType="+objectType+", start="+start+",batchSize="+batchSize);
						start += batchSize;
						continue;
					}
					if (null != nodes && !nodes.isEmpty()) {
						System.out.println(batchSize + " -- " + def.getObjectType() + " objects are getting synced");
						start += batchSize;
						errors = new HashMap<>();
						Map<String, Object> messages = SyncMessageGenerator.getMessages(nodes, objectType, errors);
						if (!errors.isEmpty())
							System.out
									.println("Error! while forming ES document data from nodes, below nodes are ignored"
											+ errors);
						esConnector.bulkImport(messages);
					} else {
						found = false;
						break;
					}
				}
			}
		}
	}

	public void syncNode(String graphId, List<String> identifiers, String objectType) throws Exception {
		if (StringUtils.isBlank(graphId))
			throw new ClientException("BLANK_GRAPH_ID", "Graph Id is blank.");
		if (identifiers.isEmpty())
			throw new ClientException("BLANK_IDENTIFIER", "Identifier is blank.");

		Map<String, String> errors;
		Set<String> uniqueIds = new HashSet<>(identifiers);
		if (uniqueIds.size() != identifiers.size()) {
			System.out.println("unique number of node identifiers: " + uniqueIds.size());
			identifiers = new ArrayList<>(uniqueIds);
		}

		// set defintionMap if objectType is known
		if (StringUtils.isNotBlank(objectType)) {
			DefinitionDTO def = util.getDefinition(graphId, objectType);
			if (null != def) {
				Map<String, Object> definition = mapper.convertValue(def, new TypeReference<Map<String, Object>>() {
				});
				Map<String, String> relationMap = GraphUtil.getRelationMap(objectType, definition);
				SyncMessageGenerator.definitionMap.put(objectType, relationMap);
			}
		}

		while (!identifiers.isEmpty()) {
			int currentBatchSize = (identifiers.size() >= batchSize) ? batchSize : identifiers.size();
			List<String> batch_ids = identifiers.subList(0, currentBatchSize);

			Response response = util.getDataNodes(graphId, batch_ids);
			if (response.getResponseCode() != ResponseCode.OK)
				throw new ResourceNotFoundException("ERR_COMPOSITE_SEARCH_SYNC_OBJECT_NOT_FOUND",
						"Error: " + response.getParams().getErrmsg());
			List<Node> nodes = (List<Node>) response.getResult().get(GraphDACParams.node_list.name());
			if (nodes == null || nodes.isEmpty())
				throw new ResourceNotFoundException("ERR_COMPOSITE_SEARCH_SYNC_OBJECT_NOT_FOUND", "Objects not found ");

			errors = new HashMap<>();
			Map<String, Object> messages = SyncMessageGenerator.getMessages(nodes, objectType, errors);
			if (!errors.isEmpty())
				System.out
						.println("Error! while forming ES document data from nodes, below nodes are ignored" + errors);
			esConnector.bulkImport(messages);

			uniqueIds.removeAll(nodes.stream().map(x -> x.getIdentifier()).collect(Collectors.toList()));
			// clear the already batched node ids from the list
			identifiers.subList(0, currentBatchSize).clear();
		}

		if (uniqueIds.size() != 0) {
			System.out.println("(" + uniqueIds.size() + ") Nodes not found: " + uniqueIds
					+ ", remaining nodes got synced successfully");
		}
	}
}
