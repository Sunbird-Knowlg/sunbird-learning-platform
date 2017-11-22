/**
 * 
 */
package org.ekstep.graph.service;

import java.util.List;
import java.util.Map;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.graph.dac.model.Node;

/**
 * @author pradyumna
 *
 */
public interface INeo4JBoltNodeOperations {

	com.ilimi.graph.dac.model.Node upsertNode(String graphId, com.ilimi.graph.dac.model.Node node, Request request);

	com.ilimi.graph.dac.model.Node addNode(String graphId, com.ilimi.graph.dac.model.Node node, Request request);

	com.ilimi.graph.dac.model.Node updateNode(String graphId, com.ilimi.graph.dac.model.Node node, Request request);

	void importNodes(String graphId, List<com.ilimi.graph.dac.model.Node> nodes, Request request);

	void updatePropertyValue(String graphId, String nodeId, Property property, Request request);

	void updatePropertyValues(String graphId, String nodeId, Map<String, Object> metadata, Request request);

	void removePropertyValue(String graphId, String nodeId, String key, Request request);

	void removePropertyValues(String graphId, String nodeId, List<String> keys, Request request);

	void deleteNode(String graphId, String nodeId, Request request);

	com.ilimi.graph.dac.model.Node upsertRootNode(String graphId, Request request);

}