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

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.enums.CollectionTypes;

public abstract class BaseWordSet extends BaseManager implements IWordSet{

	protected String languageId ;
	protected Node wordNode;
	protected WordComplexity wc;
	private Logger LOGGER;
	private static final String STARTS_WITH = "startsWith";
	private static final String ENDS_WITH = "endsWith";
	private static final String RHYMING_SOUND = "rhymingSound";
	
	public BaseWordSet(String languageId, Node wordNode, WordComplexity wc, Logger LOGGER){
		this.LOGGER = LOGGER;
		this.languageId = languageId;
		this.wordNode = wordNode;
		this.wc = wc;
	}
	
	abstract String getRymingSoundText();
	abstract String getStartsWithAksharaText();
	abstract List<String> getEndsWithAksharaText();
	
	
	@Override
	public void addAkshara() {
		String startsWith = getStartsWithAksharaText();
		List<String> endsWith = getEndsWithAksharaText();
		
		addAksharaSetsWithDefualt(startsWith, STARTS_WITH, ENDS_WITH);
		for(String text : endsWith)
			addAksharaSetsWithDefualt(text, ENDS_WITH, STARTS_WITH);
	}

	private void addAksharaSetsWithDefualt(String text, String akshara , String connectingAkshara){
		String aksharaText = akshara + "_" + text;
		String connectingAksharaText = connectingAkshara + "_" + text;
		
		String aksharaSetId = getWordSet(languageId, aksharaText);
		String connectingAksharaSetId = getWordSet(languageId, connectingAksharaText);
		boolean startAksharaRelCreate = false;

//		if(StringUtils.isBlank(aksharaSetId) || StringUtils.isBlank(connectingAksharaSetId))
//			startAksharaRelCreate = true;

		if(StringUtils.isBlank(aksharaSetId) && StringUtils.isNotBlank(connectingAksharaSetId)){
			startAksharaRelCreate = true;
		}
		
		if(StringUtils.isBlank(aksharaSetId)){
			aksharaSetId = createWordSetCollection(languageId, wordNode.getIdentifier(), aksharaText, LanguageParams.Akshara.name());
		}else{
			addMemberToSet(languageId, aksharaSetId, wordNode.getIdentifier());
		}
		
//		if(StringUtils.isBlank(connectingAksharaSetId)){
//			connectingAksharaSetId = createWordSetCollection(languageId, null, connectingAksharaText, LanguageParams.Akshara.name());
//		}

		if(startAksharaRelCreate){
			if(akshara.equalsIgnoreCase(STARTS_WITH))
				createRelation(languageId, connectingAksharaSetId, aksharaSetId, RelationTypes.STARTS_WITH_AKSHARA.relationName());
			else
				createRelation(languageId, aksharaSetId, connectingAksharaSetId, RelationTypes.STARTS_WITH_AKSHARA.relationName());
		}
	}
	
	@Override
	public void addRhymingSound() {
		String rhymingSoundText = RHYMING_SOUND + "_" + getRymingSoundText();

		if(StringUtils.isNotBlank(rhymingSoundText)){
			String setId = getWordSet(languageId, rhymingSoundText);
			if(StringUtils.isBlank(setId)){
				createWordSetCollection(languageId, wordNode.getIdentifier(), rhymingSoundText, LanguageParams.RhymingSound.name());
			}else{
				addMemberToSet(languageId, setId,  wordNode.getIdentifier());
			}
		}
	}

	protected Response getDataNodeByProperty(String languageId, Property property){
		Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getNodesByProperty");
		request.put(GraphDACParams.metadata.name(), property);
		request.put(GraphDACParams.get_tags.name(), true);
		Response findRes = getResponse(request, LOGGER);
		if (!checkError(findRes)) {
			return findRes;
		}
		return null;
	}
	
	protected String getWordSet(String languageId, String text){
		Node node = null;
		Property property = new Property(LanguageParams.text.name(), text);
		Response findRes = getDataNodeByProperty(languageId, property);
		if (findRes != null) {
			List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
			if (null != nodes && nodes.size() > 0){
				node = nodes.get(0);
				return node.getIdentifier();				
			}
		}

		return null;
	}
	
	protected String createWordSetCollection(String languageId, String wordId, String setText, String setType){
        Request setReq = getRequest(languageId, GraphEngineManagers.COLLECTION_MANAGER, "createSet");
        //setReq.put(GraphDACParams.criteria.name(), getItemSetCriteria(node));
        
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(LanguageParams.text.name(), setText);
        metadata.put(LanguageParams.type.name(), setType);
		Node wordSet = new Node();
		wordSet.setMetadata(metadata);
		wordSet.setObjectType(LanguageObjectTypes.WordSet.name());

		List<String> members = null;
		if(StringUtils.isNotBlank(wordId))
			members = Arrays.asList(wordId);
		else
			members = new ArrayList<String>();
			
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
	
	protected String createWordSet(String languageId, String setText, String setType){
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(LanguageParams.text.name(), setText);
        metadata.put(LanguageParams.type.name(), setType);
		Node wordSet = new Node();
		wordSet.setMetadata(metadata);
		wordSet.setObjectType(LanguageObjectTypes.WordSet.name());
        Request req = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
		req.put(GraphDACParams.node.name(), wordSet);
		Response res = getResponse(req, LOGGER);
		if (checkError(res)) {
			throw new ServerException(LanguageErrorCodes.ERROR_ADD_WORD_SET.name(), getErrorMessage(res));
		}
		String nodeId = (String) res.get(GraphDACParams.node_id.name());
		return nodeId;
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
