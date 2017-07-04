package org.ekstep.language.translation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageObjectTypes;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.RelationCriterion;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.enums.CollectionTypes;

public class BaseTranslationSet extends BaseManager{

	protected String graphId ;
	protected Node proxyNode;
	private Map<String, Object> metadata;

	private PlatformLogger<BaseTranslationSet> LOGGER;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public BaseTranslationSet(String graphId, Node proxyNode, PlatformLogger LOGGER, Map<String, Object> metadata){
		this.LOGGER = LOGGER;
		this.graphId = graphId;
		this.proxyNode = proxyNode;
		this.metadata = metadata;
	}
	
	public BaseTranslationSet(String graphId){
		this.graphId = graphId;
	}
	
	@SuppressWarnings("unchecked")
	public String getTranslationSet(String wordnetId){
		LOGGER.log("Logging data:"+wordnetId);
		Node node = null;
        SearchCriteria sc = new SearchCriteria();
        sc.setNodeType(SystemNodeTypes.SET.name());
        sc.setObjectType(LanguageObjectTypes.TranslationSet.name());
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new Filter("indowordnetId", SearchConditions.OP_EQUAL, wordnetId));
        MetadataCriterion mc = MetadataCriterion.create(filters);
        sc.addMetadata(mc);
        sc.setResultSize(1);
        Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
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
	
	@SuppressWarnings("unchecked")
	public String getTranslationSetWithMember(String id, String wordnetId){
		LOGGER.log("Logging data: "+id+": "+wordnetId);
		Node node = null;
		RelationCriterion rc = new RelationCriterion("hasMember","Synset");
		List<String> identifiers = new ArrayList<String>();
		identifiers.add(id);
		rc.setIdentifiers(identifiers);
        SearchCriteria sc = new SearchCriteria();
        sc.setNodeType(SystemNodeTypes.SET.name());
        sc.setObjectType(LanguageObjectTypes.TranslationSet.name());
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new Filter("indowordnetId", SearchConditions.OP_EQUAL, wordnetId));
        MetadataCriterion mc = MetadataCriterion.create(filters);
        sc.addMetadata(mc);
        sc.addRelationCriterion(rc);
        sc.setResultSize(1);
        Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
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
	
	public String createTranslationSetCollection(){
		LOGGER.log("Creating new set!!!!!!!!!!!!!!!!!");
        Request setReq = getRequest(graphId, GraphEngineManagers.COLLECTION_MANAGER, "createSet");
        //setReq.put(GraphDACParams.criteria.name(), getItemSetCriteria(node));

		Node translationSet = new Node();
		translationSet.setObjectType(LanguageObjectTypes.TranslationSet.name());
		if(null!=this.metadata && this.metadata.size()>0)
			translationSet.setMetadata(this.metadata);

		List<String> members = null;
		members = Arrays.asList(proxyNode.getIdentifier());
        setReq.put(GraphDACParams.members.name(), members);
        setReq.put(GraphDACParams.node.name(), translationSet);
        setReq.put(GraphDACParams.object_type.name(), LanguageObjectTypes.TranslationSet.name());
        setReq.put(GraphDACParams.member_type.name(), LanguageObjectTypes.Synset.name());
        Response res = getResponse(setReq, LOGGER);
		if (checkError(res))
			throw new ServerException(LanguageErrorCodes.ERROR_ADD_WORD_SET.name(),
					getErrorMessage(res));
		String setId = (String) res.get(GraphDACParams.set_id.name());
		System.out.println("Returning id after creation!!!!!!!!!!!!!!!!!");
		return setId;
	}
	
	public void addMemberToSet(String collectionId){
        Request setReq = getRequest(graphId, GraphEngineManagers.COLLECTION_MANAGER, "addMember");

        setReq.put(GraphDACParams.member_id.name(), proxyNode.getIdentifier());
        setReq.put(GraphDACParams.collection_id.name(), collectionId);
        setReq.put(GraphDACParams.collection_type.name(), CollectionTypes.SET.name());
        Response res = getResponse(setReq, LOGGER);
		if (checkError(res))
			throw new ServerException(LanguageErrorCodes.ERROR_ADD_WORD_SET.name(),
					getErrorMessage(res));
	}

	public void createRelation(String startNodeId, String endNodeId, String relationType){
        Request req = getRequest(graphId, GraphEngineManagers.GRAPH_MANAGER, "createRelation");
        req.put(GraphDACParams.start_node_id.name(), startNodeId);
        req.put(GraphDACParams.end_node_id.name(), endNodeId);
        req.put(GraphDACParams.relation_type.name(), relationType);
		Response res = getResponse(req, LOGGER);
		if (checkError(res)) {
			throw new ServerException(LanguageErrorCodes.ERROR_ADD_WORD_SET.name(), getErrorMessage(res));
		}

	}
	
	public Node getProxyNode() {
		String proxyId = proxyNode.getIdentifier();
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getProxyNode");
		request.put(GraphDACParams.node_id.name(), proxyId);
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

	
	public void removeProxyNodeFromTranslationSet(String setId){
		LOGGER.log("Deleting relation : " + setId + " --> " + proxyNode.getIdentifier());
        Request setReq = getRequest(graphId, GraphEngineManagers.COLLECTION_MANAGER, "removeMember");
        setReq.put(GraphDACParams.member_id.name(), proxyNode.getIdentifier());
        setReq.put(GraphDACParams.collection_id.name(), setId);
        setReq.put(GraphDACParams.collection_type.name(), CollectionTypes.SET.name());
        Response res = getResponse(setReq, LOGGER);
		if (checkError(res))
			throw new ServerException(LanguageErrorCodes.ERROR_ADD_WORD_SET.name(),
					getErrorMessage(res));
	}
	

}
