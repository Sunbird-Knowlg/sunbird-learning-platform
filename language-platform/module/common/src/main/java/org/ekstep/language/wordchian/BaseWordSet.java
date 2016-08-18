package org.ekstep.language.wordchian;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.entity.WordComplexity;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.enums.CollectionTypes;

public abstract class BaseWordSet extends BaseManager{

	protected String languageId ;
	protected Node wordNode;
	protected WordComplexity wc;
	private Logger LOGGER;
	
	public BaseWordSet(String languageId, Node wordNode, WordComplexity wc, Logger LOGGER){
		this.LOGGER = LOGGER;
		this.languageId = languageId;
		this.wordNode = wordNode;
		this.wc = wc;
	}
	
	protected String getWordSet(String languageId, String lemma, String type){
		Node node = null;
        SearchCriteria sc = new SearchCriteria();
        sc.setNodeType(SystemNodeTypes.SET.name());
        sc.setObjectType(LanguageObjectTypes.WordSet.name());
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new Filter("lemma", SearchConditions.OP_EQUAL, lemma));
        filters.add(new Filter("type", SearchConditions.OP_EQUAL, type));
        MetadataCriterion mc = MetadataCriterion.create(filters);
        sc.addMetadata(mc);
        sc.setResultSize(1);
        Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
                GraphDACParams.search_criteria.name(), sc);
        request.put(GraphDACParams.get_tags.name(), true);
        Response findRes = getResponse(request, LOGGER);
        if (checkError(findRes))
            return null;
        else {
            List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
            if (null != nodes && nodes.size() > 0){
            	node = nodes.get(0);
            	return node.getIdentifier();
            }
            return null;
        }
	}
	
	protected String createWordSetCollection(String languageId, String wordId, String setLemma, String setType){
        Request setReq = getRequest(languageId, GraphEngineManagers.COLLECTION_MANAGER, "createSet");
        //setReq.put(GraphDACParams.criteria.name(), getItemSetCriteria(node));
        
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(LanguageParams.lemma.name(), setLemma);
        metadata.put(LanguageParams.type.name(), setType);
		Node wordSet = new Node();
		wordSet.setMetadata(metadata);
		wordSet.setObjectType(LanguageObjectTypes.WordSet.name());

		List<String> members = null;
		if(StringUtils.isNotBlank(wordId))
			members = Arrays.asList(wordId);
		else
			throw new ServerException(LanguageErrorCodes.ERROR_ADD_WORD_SET.name(),
					"WordId cannot be empty");
			
        setReq.put(GraphDACParams.members.name(), members);
        setReq.put(GraphDACParams.node.name(), wordSet);
        setReq.put(GraphDACParams.object_type.name(), LanguageObjectTypes.WordSet.name());
        setReq.put(GraphDACParams.member_type.name(), LanguageObjectTypes.Word.name());
        Response res = getResponse(setReq, LOGGER);
		if (checkError(res))
			throw new ServerException(LanguageErrorCodes.ERROR_ADD_WORD_SET.name(),
					getErrorMessage(res));
		String setId = (String) res.get(GraphDACParams.set_id.name());
		return setId;
	}
	
	protected void addMemberToSet(String languageId, String collectionId, String wordId){
        Request setReq = getRequest(languageId, GraphEngineManagers.COLLECTION_MANAGER, "addMember");

        setReq.put(GraphDACParams.member_id.name(), wordId);
        setReq.put(GraphDACParams.collection_id.name(), collectionId);
        setReq.put(GraphDACParams.collection_type.name(), CollectionTypes.SET.name());
        Response res = getResponse(setReq, LOGGER);
		if (checkError(res))
			throw new ServerException(LanguageErrorCodes.ERROR_ADD_WORD_SET.name(),
					getErrorMessage(res));
	}

	protected void createRelation(String languageId, String startNodeId, String endNodeId, String relationType){
        Request req = getRequest(languageId, GraphEngineManagers.GRAPH_MANAGER, "createRelation");
        req.put(GraphDACParams.start_node_id.name(), startNodeId);
        req.put(GraphDACParams.end_node_id.name(), endNodeId);
        req.put(GraphDACParams.relation_type.name(), relationType);
		Response res = getResponse(req, LOGGER);
		if (checkError(res)) {
			throw new ServerException(LanguageErrorCodes.ERROR_ADD_WORD_SET.name(), getErrorMessage(res));
		}

	}
}
