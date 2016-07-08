package org.ekstep.language.wordchian;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.ekstep.language.common.enums.LanguageParams;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.engine.router.GraphEngineManagers;

public abstract class BasePhoneticBoundary extends BaseManager{

	
	protected String getPhoneticBoundary(String languageId, String text, Logger LOGGER){
		Node node = null;
		Property property = new Property(LanguageParams.text.name(), text);
		Response findRes = getDataNodeByProperty(languageId, property, LOGGER);
		if (findRes != null) {
			List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
			if (null != nodes && nodes.size() > 0)
				node = nodes.get(0);
			return node.getIdentifier();
		}

		return null;
	}
	
	protected Response getDataNodeByProperty(String languageId, Property property, Logger LOGGER){
		Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getNodesByProperty");
		request.put(GraphDACParams.metadata.name(), property);
		request.put(GraphDACParams.get_tags.name(), true);
		Response findRes = getResponse(request, LOGGER);
		if (!checkError(findRes)) {
			return findRes;
		}
		return null;
	}
	
	protected Response createPhoneticBoundary(String languageId, Map<String, Object> obj, Logger LOGGER)
			throws Exception {
		Node phoneticBoundaryNode = new Node();
		phoneticBoundaryNode.setMetadata(obj);
		phoneticBoundaryNode.setObjectType(LanguageObjectTypes.Phonetic_Boundary.name());
		Request synsetReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
		synsetReq.put(GraphDACParams.node.name(), phoneticBoundaryNode);
		Response res = getResponse(synsetReq, LOGGER);
		if (checkError(res))
			throw new ServerException(LanguageErrorCodes.ERROR_ADD_PHONETIC_BOUNTARY.name(),
					getErrorMessage(res));
		return res;
	}
	
	protected Relation createRelation(String startNodeId, String startNodeType, String relationType, String endNodeId, String endNodeType){
        Relation newRelation = new Relation();
        newRelation.setStartNodeId(startNodeId);
        newRelation.setStartNodeObjectType(startNodeType);
        newRelation.setEndNodeId(endNodeId);
        newRelation.setEndNodeObjectType(endNodeType);
        newRelation.setRelationType(relationType);
		return newRelation;
	}
	
}
