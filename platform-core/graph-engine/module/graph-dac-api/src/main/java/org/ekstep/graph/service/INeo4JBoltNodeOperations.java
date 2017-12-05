/**
 * 
 */
package org.ekstep.graph.service;

import java.util.List;
import java.util.Map;

import org.ekstep.common.dto.Property;
import org.ekstep.common.dto.Request;

/**
 * @author pradyumna
 *
 */
public interface INeo4JBoltNodeOperations {

	org.ekstep.graph.dac.model.Node upsertNode(String graphId, org.ekstep.graph.dac.model.Node node, Request request);

	org.ekstep.graph.dac.model.Node addNode(String graphId, org.ekstep.graph.dac.model.Node node, Request request);

	org.ekstep.graph.dac.model.Node updateNode(String graphId, org.ekstep.graph.dac.model.Node node, Request request);

	void importNodes(String graphId, List<org.ekstep.graph.dac.model.Node> nodes, Request request);

	void updatePropertyValue(String graphId, String nodeId, Property property, Request request);

	void updatePropertyValues(String graphId, String nodeId, Map<String, Object> metadata, Request request);

	void removePropertyValue(String graphId, String nodeId, String key, Request request);

	void removePropertyValues(String graphId, String nodeId, List<String> keys, Request request);

	void deleteNode(String graphId, String nodeId, Request request);

	org.ekstep.graph.dac.model.Node upsertRootNode(String graphId, Request request);

}