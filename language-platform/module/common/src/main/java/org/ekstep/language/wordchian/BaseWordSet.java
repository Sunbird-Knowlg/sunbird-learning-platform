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
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.enums.CollectionTypes;

public abstract class BaseWordSet extends BaseManager{

	protected String languageId ;
	protected Node wordNode;
	protected WordComplexity wc;
	protected List<Relation> existingWordSetRelatios;
	private Logger LOGGER;
	
	public BaseWordSet(String languageId, Node wordNode, WordComplexity wc, List<Relation> existingWordSetRelatios, Logger LOGGER){
		this.LOGGER = LOGGER;
		this.languageId = languageId;
		this.wordNode = wordNode;
		this.existingWordSetRelatios = existingWordSetRelatios;
		this.wc = wc;
	}
	
	protected String getWordSet(String lemma, String type){
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
	
	protected String createWordSetCollection(String setLemma, String setType){
        Request setReq = getRequest(languageId, GraphEngineManagers.COLLECTION_MANAGER, "createSet");
        //setReq.put(GraphDACParams.criteria.name(), getItemSetCriteria(node));
        
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(LanguageParams.lemma.name(), setLemma);
        metadata.put(LanguageParams.type.name(), setType);
		Node wordSet = new Node();
		wordSet.setMetadata(metadata);
		wordSet.setObjectType(LanguageObjectTypes.WordSet.name());

		List<String> members = null;
		members = Arrays.asList(wordNode.getIdentifier());
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
	
	protected void addMemberToSet(String collectionId){
        Request setReq = getRequest(languageId, GraphEngineManagers.COLLECTION_MANAGER, "addMember");

        setReq.put(GraphDACParams.member_id.name(), wordNode.getIdentifier());
        setReq.put(GraphDACParams.collection_id.name(), collectionId);
        setReq.put(GraphDACParams.collection_type.name(), CollectionTypes.SET.name());
        Response res = getResponse(setReq, LOGGER);
		if (checkError(res))
			throw new ServerException(LanguageErrorCodes.ERROR_ADD_WORD_SET.name(),
					getErrorMessage(res));
	}

	protected void createRelation(String startNodeId, String endNodeId, String relationType){
        Request req = getRequest(languageId, GraphEngineManagers.GRAPH_MANAGER, "createRelation");
        req.put(GraphDACParams.start_node_id.name(), startNodeId);
        req.put(GraphDACParams.end_node_id.name(), endNodeId);
        req.put(GraphDACParams.relation_type.name(), relationType);
		Response res = getResponse(req, LOGGER);
		if (checkError(res)) {
			throw new ServerException(LanguageErrorCodes.ERROR_ADD_WORD_SET.name(), getErrorMessage(res));
		}

	}
	
	public Node getDataNode() {
		String wordId = wordNode.getIdentifier();
		Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode");
		request.put(GraphDACParams.node_id.name(), wordId);
		request.put(GraphDACParams.get_tags.name(), true);

		Response findRes = getResponse(request, LOGGER);
		if (checkError(findRes))
			return null;
		else {
			Node node = (Node) findRes.get(GraphDACParams.node.name());
			if (null != node)
				return node;
		}
		return null;
	}
		
	protected boolean isExist(String type, String lemma){
		
		for(Relation relation : existingWordSetRelatios) {
			if(type.equalsIgnoreCase((String)relation.getStartNodeMetadata().get(LanguageParams.type.name()))){
				//same type of WordSet is already associated with
				if (StringUtils.isBlank(lemma) || !lemma.equalsIgnoreCase((String)relation.getStartNodeMetadata().get(LanguageParams.lemma.name()))) {
					//different lemma - remove set membership
					removeWordFromWordSet(relation.getStartNodeId());
					return false;
				} else {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean isExist(String type, List<String> newLemmas){
		
		if(existingWordSetRelatios.size() > 0){
			List<String> existingLemmas = new ArrayList<String>();
			Map<String, String> existingSetMap = new HashMap<String, String>();
			for(Relation relation : existingWordSetRelatios) {
				if(type.equalsIgnoreCase((String)relation.getStartNodeMetadata().get(LanguageParams.type.name()))){
					String lemma = (String)relation.getStartNodeMetadata().get(LanguageParams.lemma.name());
					lemma = lemma.substring(lemma.lastIndexOf("_")+1); //trimming of constant precedence like "ENDS_WITH_"
					existingLemmas.add(lemma);
					existingSetMap.put(lemma, relation.getStartNodeId());
				}
			}
			
			if(existingLemmas.containsAll(newLemmas))
				return true;
			
			List<String> dupLemmas = new ArrayList<>(newLemmas);
			//remove already existing lemma from newLemmas
			newLemmas.removeAll(existingLemmas);
			//remove newLemmas from existingLemmas so that remaining lemma will be the one which should be removed
			existingLemmas.removeAll(dupLemmas);
			for(String lemma : existingLemmas)
				removeWordFromWordSet(existingSetMap.get(lemma));
			return false;
		}
		return false;
	}
	
	protected void removeWordFromWordSet(String setId){
        Request setReq = getRequest(languageId, GraphEngineManagers.COLLECTION_MANAGER, "removeMember");

        setReq.put(GraphDACParams.member_id.name(), wordNode.getIdentifier());
        setReq.put(GraphDACParams.collection_id.name(), setId);
        setReq.put(GraphDACParams.collection_type.name(), CollectionTypes.SET.name());
        Response res = getResponse(setReq, LOGGER);
		if (checkError(res))
			throw new ServerException(LanguageErrorCodes.ERROR_ADD_WORD_SET.name(),
					getErrorMessage(res));
	}
	

}
