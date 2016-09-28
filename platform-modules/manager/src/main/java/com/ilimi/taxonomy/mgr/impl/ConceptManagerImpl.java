package com.ilimi.taxonomy.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Graph;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.taxonomy.dto.ConceptDTO;
import com.ilimi.taxonomy.dto.ConceptHierarhy;
import com.ilimi.taxonomy.enums.TaxonomyAPIParams;
import com.ilimi.taxonomy.mgr.IConceptManager;

@Component
public class ConceptManagerImpl extends BaseManager implements IConceptManager {

    private static Logger LOGGER = LogManager.getLogger(IConceptManager.class.getName());

    @Override
    public Response getConcepts(String id, String relationType, int depth, String taxonomyId, String[] cfields, boolean isHierarchical) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_CONCEPT_ID.name(), "Concept Id is blank");
        if (StringUtils.isBlank(relationType))
            relationType = RelationTypes.HIERARCHY.relationName();
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getSubGraph");
        request.put(GraphDACParams.start_node_id.name(), id);
        request.put(GraphDACParams.relation_type.name(), relationType);
        if (depth > 0)
            request.put(GraphDACParams.depth.name(), depth);
        Response findRes = getResponse(request, LOGGER);
        Response response = copyResponse(findRes);
        if (checkError(response))
            return response;
        Graph graph = (Graph) findRes.get(GraphDACParams.sub_graph.name());
        if (null != graph && null != graph.getNodes() && !graph.getNodes().isEmpty()) {
        	if(isHierarchical) {
        		Node parent = null;
        		Map<String, Node> nodes = new HashMap<String, Node>();
                for (Node node : graph.getNodes()) {
                		nodes.put(node.getIdentifier(), node);
                		if(id.equals(node.getIdentifier()))
                			parent = node;
                }
                if(null != parent) {
                    response.put(TaxonomyAPIParams.taxonomy_hierarchy.name(), new ConceptHierarhy(parent, nodes, cfields));
                } else {
                	response.put(TaxonomyAPIParams.taxonomy_hierarchy.name(), null);
                }
        	} else {
        		List<ConceptDTO> concepts = new ArrayList<ConceptDTO>();
                for (Node node : graph.getNodes()) {
                	if(SystemNodeTypes.DATA_NODE.name().equals(node.getNodeType())) {
                		ConceptDTO dto = new ConceptDTO(node);
                        concepts.add(dto);
                	}
                }
                response.put(TaxonomyAPIParams.concepts.name(), concepts);
        	}
        }
        return response;
    }

}
