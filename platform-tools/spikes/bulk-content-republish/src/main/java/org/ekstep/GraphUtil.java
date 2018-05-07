package org.ekstep;

import java.util.Arrays;
import java.util.List;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Filter;
import org.ekstep.graph.dac.model.MetadataCriterion;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.SearchConditions;
import org.ekstep.graph.dac.model.SearchCriteria;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.learning.util.ControllerUtil;

public class GraphUtil extends ControllerUtil{

	public List<Node> getLiveContentNodes(int startPosition, int batchSize) {
		SearchCriteria sc = new SearchCriteria();
		sc.setObjectType("Content");
		sc.addMetadata(MetadataCriterion.create(Arrays.asList(new Filter(GraphDACParams.status.name(), SearchConditions.OP_EQUAL, "Live"))));
		sc.setResultSize(batchSize);
		sc.setStartPosition(startPosition);
		Request req = getRequest("domain", GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.search_criteria.name(), sc);
		req.put(GraphDACParams.get_tags.name(), true);
		Response listRes = getResponse(req);
		if (checkError(listRes))
			throw new ResourceNotFoundException("NODES_NOT_FOUND", "Nodes not found: domain");
		else {
			List<Node> nodes = (List<Node>) listRes.get(GraphDACParams.node_list.name());
			return nodes;
		}
	}
}
