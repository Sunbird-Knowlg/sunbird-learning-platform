package org.sunbird.sync.tool.mgr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Response;
import org.sunbird.common.enums.CompositeSearchErrorCodes;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.learning.util.ControllerUtil;
import org.sunbird.sync.tool.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("neo4jESSyncManager")
public class Neo4jESSyncManager implements ISyncManager {

	private ControllerUtil util = new ControllerUtil();
	private static int batchSize = 50;
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private ElasticSearchConnector esConnector;

	@PostConstruct
	private void init() throws Exception {
		int batch = Platform.config.hasPath("batch.size") ? Platform.config.getInt("batch.size"): 50;
		batchSize = batch;
	}

	@Override
	public void syncByIds(String graphId, List<String> identifiers) throws Exception {
		System.out.println("Total Number of Objects to be Synced : " + identifiers.size());
		System.out.println("Identifiers : " + identifiers);
		syncNode(graphId, identifiers, null);
	}
	
	@Override
	public void syncByFile(String graphId, String filePath, String fileType) throws Exception {
		List<String> identifiers;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		long startTime = System.currentTimeMillis();
		LocalDateTime start = LocalDateTime.now();
		
		if(StringUtils.equalsIgnoreCase(fileType, "csv")) 
			identifiers = CSVFileParserUtil.getIdentifiers(filePath, null);
		else if(StringUtils.equalsIgnoreCase(fileType, "json")) 
			identifiers = JsonFileParserUtil.getIdentifiers(filePath);
		else {
			System.out.println("Specified file type : " + fileType + " is not supported.");
			throw new ClientException("FILE_NOT_SUPPORTED", "Specified file type : " + fileType + " is not supported.");
		}
		syncByIds(graphId, identifiers);
		
		long endTime = System.currentTimeMillis();
		LocalDateTime end = LocalDateTime.now();
		System.out.println("Total time of execution: " + (endTime - startTime) + "ms");
		System.out.println("START_TIME: " + dtf.format(start) + ", END_TIME: " + dtf.format(end));
		
	}
	
	@Override
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

	@Override
	public void syncByObjectType(String graphId, String objectType) throws Exception {
		if (StringUtils.isBlank(graphId))
			throw new ClientException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_SYNC_BLANK_GRAPH_ID.name(),
					"Graph Id is blank.");
		Map<String, String> errors = new HashMap<>();
		DefinitionDTO def;
		if (StringUtils.isNotBlank(objectType)) {
			def = util.getDefinition(graphId, objectType);
			if (null != def) {
				System.out.println("\nSyncing object of type '" + objectType + "'.\n");
				Map<String, Object> definition = mapper.convertValue(def, new TypeReference<Map<String, Object>>() {
				});
				Map<String, String> relationMap = GraphUtil.getRelationMap(objectType, definition);
				SyncMessageGenerator.definitionMap.put(objectType, relationMap);
				int start = 0;
				int completed =0;
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
					if (CollectionUtils.isNotEmpty(nodes)) {
						filterDefinitionNodes(nodes);
						start += batchSize;
						Map<String, Object> messages = SyncMessageGenerator.getMessages(nodes, objectType, errors);
						esConnector.bulkImport(messages);
						completed += batchSize;
						System.out.println( "");
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
			List<Node> nodes;
			if (response.getResponseCode() != ResponseCode.OK) {
				throw new ResourceNotFoundException("ERR_COMPOSITE_SEARCH_SYNC_OBJECT_NOT_FOUND",
						"Error: " + response.getParams().getErrmsg());
			} else {
				nodes = (List<Node>) response.getResult().get(GraphDACParams.node_list.name());
			}

			if (CollectionUtils.isNotEmpty(nodes)) {
				filterDefinitionNodes(nodes);
				
				errors = new HashMap<>();
				Map<String, Object> messages = SyncMessageGenerator.getMessages(nodes, objectType, errors);
				if (!errors.isEmpty())
					System.out
							.println("Error! while forming ES document data from nodes, below nodes are ignored" + errors);
				esConnector.bulkImport(messages);
			}
			uniqueIds.removeAll(nodes.stream().map(x -> x.getIdentifier()).collect(Collectors.toList()));
			// clear the already batched node ids from the list
			identifiers.subList(0, currentBatchSize).clear();
		}

		if (uniqueIds.size() != 0) {
			System.out.println("(" + uniqueIds.size() + ") Nodes not found: " + uniqueIds
					+ ", remaining nodes got synced successfully");
		}
	}
	
	public void syncByObjectType(String graphId, String objectType, Long total, Integer delay) throws Exception {
		if (StringUtils.isBlank(graphId))
			throw new ClientException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_SYNC_BLANK_GRAPH_ID.name(),
					"Graph Id is blank.");
		Map<String, String> errors = new HashMap<>();
		DefinitionDTO def;
		if (StringUtils.isNotBlank(objectType)) {
			def = util.getDefinition(graphId, objectType);
			if (null != def) {
				System.out.println("-----------------------------------------");
				System.out.println("\nSyncing object of type '" + objectType + "' with batch size of " + batchSize + " having delay " + delay + "ms for each batch.\n");
				Map<String, Object> definition = mapper.convertValue(def, new TypeReference<Map<String, Object>>() {
				});
				Map<String, String> relationMap = GraphUtil.getRelationMap(objectType, definition);
				SyncMessageGenerator.definitionMap.put(objectType, relationMap);
				int start = 0;
				int current = 0;
				boolean found = true;
				long startTime = System.currentTimeMillis();
				while (found) {
					List<Node> nodes = null;
					try {
						nodes = util.getNodes(graphId, def.getObjectType(), start, batchSize);
					}catch(ResourceNotFoundException e) {
						System.out.println("error while fetching neo4j records for objectType="+objectType+", start="+start+",batchSize="+batchSize);
						start += batchSize;
						continue;
					}
					if (CollectionUtils.isNotEmpty(nodes)) {
						filterDefinitionNodes(nodes);
						start += batchSize;
						Map<String, Object> messages = SyncMessageGenerator.getMessages(nodes, objectType, errors);
						esConnector.bulkImport(messages);
						current += messages.size();
						printProgress(startTime, total, current);
						if (delay > 0) {
							Thread.sleep(delay);
						}
					} else {
						found = false;
						break;
					}
				}
				if (!errors.isEmpty())
					System.out.println("Error! while forming ES document data from nodes, below nodes are ignored. \n" + errors);
				long endTime = System.currentTimeMillis();
				System.out.println("\n'" + objectType + "' nodes sync completed in: " + (endTime - startTime) + "ms");
			}
		}
	}
	
	public void syncGraph(String graphId, Integer delay, String[] objectType) throws Exception {
		if (StringUtils.isBlank(graphId))
			throw new ClientException("BLANK_GRAPH_ID", "Graph Id is blank.");
		Map<String, Long> counts = util.getCountByObjectType(graphId);
		if (counts.isEmpty()) {
			System.out.println("No objects found in this graph.");
		} else {
			List<String> keys = new ArrayList(counts.keySet());
			if (objectType != null) {
				keys = counts.keySet().stream().filter(key -> Arrays.asList(objectType).contains(key)).collect(Collectors.toList());
			}
			for (String key: counts.keySet()) {
				Long count = counts.get(key);
				if (count > 1)
					System.out.println(count + " - " + key + " nodes.");
				else
					System.out.println(count + " - " + key + " node.");
			}
			long startTime = System.currentTimeMillis();
			System.out.println("\nSync starting at " + startTime);
			for (String key: keys) {
				syncByObjectType(graphId, key, counts.get(key), delay);
			}
			System.out.println("-----------------------------------------");
			long endTime = System.currentTimeMillis();
			System.out.println("Sync completed at " + endTime);
			System.out.println("Time taken to sync nodes: " + (endTime - startTime) + "ms");
		}
	}

	private static void printProgress(long startTime, long total, long current) {
	    long eta = current == 0 ? 0 : 
	        (total - current) * (System.currentTimeMillis() - startTime) / current;

	    String etaHms = current == 0 ? "N/A" : 
	            String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
	                    TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
	                    TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

	    StringBuilder string = new StringBuilder(140);   
	    int percent = (int) (current * 100 / total);
	    string
	        .append('\r')
	        .append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
	        .append(String.format(" %d%% [", percent))
	        .append(String.join("", Collections.nCopies(percent, "=")))
	        .append('>')
	        .append(String.join("", Collections.nCopies(100 - percent, " ")))
	        .append(']')
	        .append(String.join("", Collections.nCopies((int) (Math.log10(total)) - (int) (Math.log10(current)), " ")))
	        .append(String.format(" %d/%d, ETA: %s", current, total, etaHms));

	    System.out.print(string);
	}

	public static void filterDefinitionNodes(List<Node> nodes) {
		nodes.removeIf(n -> SystemNodeTypes.DEFINITION_NODE.name().equals(n.getNodeType()));
	}

}