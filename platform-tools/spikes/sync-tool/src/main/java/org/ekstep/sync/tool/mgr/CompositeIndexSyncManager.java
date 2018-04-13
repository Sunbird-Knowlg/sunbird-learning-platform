package org.ekstep.sync.tool.mgr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.CompositeSearchErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.sync.tool.util.CompositeIndexMessageGenerator;
import org.ekstep.sync.tool.util.CompositeIndexSyncer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CompositeIndexSyncManager {

	private ControllerUtil util = new ControllerUtil();
	private final static int batchSize = 1000;

	@Autowired
	private CompositeIndexSyncer compositeIndexSyncer;

	@PostConstruct
	public void init() throws Exception {
		compositeIndexSyncer.createCSIndexIfNotExist();
	}

	public void syncNode(String graphId, List<String> identifiers) throws Exception {
		if (StringUtils.isBlank(graphId))
			throw new ClientException("BLANK_GRAPH_ID", "Graph Id is blank.");
		if (identifiers.isEmpty())
			throw new ClientException("BLANK_IDENTIFIER", "Identifier is blank.");

		Set<String> uniqueIds = new HashSet<>(identifiers);
		if (uniqueIds.size() != identifiers.size()) {
			System.out.println("unique number of node identifiers: " + uniqueIds.size());
			identifiers = new ArrayList<>(uniqueIds);
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
			for (Node node : nodes) {
				Map<String, Object> csMessage = CompositeIndexMessageGenerator.getMessage(node);
				compositeIndexSyncer.processMessage(csMessage);
				uniqueIds.remove(node.getIdentifier());
			}

			//clear the already batched node ids from  the list
			identifiers.subList(0, currentBatchSize).clear();
		}

		if (uniqueIds.size() != 0) {
			System.out.println("(" + uniqueIds.size() + ") Nodes not found: " + uniqueIds
					+ ", remaining nodes got synced successfully");
		}
	}

	public void syncNode(String graphId, String objectType) throws Exception {
		List<Node> nodes = getNodeByObjectType(graphId, objectType);
		if (!nodes.isEmpty()) {
			int i = 1;
			for (Node node : nodes) {
				if (i % 1000 == 0)
					System.out.println("IDENTIFIER GOING FOR PROCESS: " + i); // TODO: Remove this line

				if (null != node) {
					Map<String, Object> csMessage = CompositeIndexMessageGenerator.getMessage(node);
					compositeIndexSyncer.processMessage(csMessage);
				}
				i++;
			}
		}

	}

	public void syncNode(String graphId, String objectType, String startDate, String endDate) throws Exception {
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
			syncNode(graphId, ids);
		} else {
			System.out.println("No node got matched with given condition(s)");
		}

	}

	private List<Node> getNodeByObjectType(String graphId, String objectType) throws Exception {
		if (StringUtils.isBlank(graphId))
			throw new ClientException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_SYNC_BLANK_GRAPH_ID.name(),
					"Graph Id is blank.");
		DefinitionDTO def;
		List<Node> identifiers = new ArrayList<>();
		if (StringUtils.isNotBlank(objectType)) {
			def = util.getDefinition(graphId, objectType);
			if (null != def) {
				int start = 0;
				boolean found = true;
				while (found) {
					List<Node> nodes = util.getNodes(graphId, def.getObjectType(), start, batchSize);
					if (null != nodes && !nodes.isEmpty()) {
						for (Node node : nodes) {
							identifiers.add(node);
						}
						System.out.println(
								"sent " + start + " + " + batchSize + " -- " + def.getObjectType() + " objects");
						start += batchSize;
					} else {
						found = false;
						break;
					}
				}
			}
		}
		return identifiers;
	}

}
