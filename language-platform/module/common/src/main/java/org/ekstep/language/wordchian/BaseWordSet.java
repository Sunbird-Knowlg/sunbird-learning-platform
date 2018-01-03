package org.ekstep.language.wordchian;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ServerException;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.enums.SystemNodeTypes;
import org.ekstep.graph.dac.model.Filter;
import org.ekstep.graph.dac.model.MetadataCriterion;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.dac.model.SearchConditions;
import org.ekstep.graph.dac.model.SearchCriteria;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.enums.CollectionTypes;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.entity.WordComplexity;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.ekstep.common.mgr.BaseManager;

/**
 * The Class BaseWordSet, provides basic functionality to find WordSet, create
 * WordSet, add member to existing WordSet, create relation between node and
 * remove word membership relation from WordSet
 *
 * @author karthik
 */
public abstract class BaseWordSet extends BaseManager {

	/** The language id. */
	protected String languageId;

	/** The word node. */
	protected Node wordNode;

	/** The wc. */
	protected WordComplexity wc;

	/** The existing word set relatios. */
	protected List<Relation> existingWordSetRelatios;

	/**
	 * Instantiates a new base word set.
	 *
	 * @param languageId
	 *            the language id
	 * @param wordNode
	 *            the word node
	 * @param wc
	 *            the wc
	 * @param existingWordSetRelatios
	 *            the existing word set relatios
	 * @param LOGGER
	 *            the logger
	 */
	public BaseWordSet(String languageId, Node wordNode, WordComplexity wc, List<Relation> existingWordSetRelatios) {
		this.languageId = languageId;
		this.wordNode = wordNode;
		this.existingWordSetRelatios = existingWordSetRelatios;
		this.wc = wc;
	}

	/**
	 * Gets the word set based on its lemma and type of WordSet
	 *
	 * @param lemma
	 *            the lemma
	 * @param type
	 *            the type
	 * @return the word set
	 */
	@SuppressWarnings("unchecked")
	protected String getWordSet(String lemma, String type) {
		TelemetryManager.log("get Word Set (" + type + ") lemma " + lemma);

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
		Response findRes = getResponse(request);
		if (checkError(findRes))
			return null;
		else {
			List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
			if (null != nodes && nodes.size() > 0) {
				node = nodes.get(0);
				TelemetryManager.log("got  WordSet id " , node.getIdentifier());
				return node.getIdentifier();
			}
			TelemetryManager.log("WordSet is not found");
			return null;
		}
	}

	/**
	 * Creates the WordSet .
	 *
	 * @param setLemma
	 *            the set lemma
	 * @param setType
	 *            the set type
	 * @return the string
	 */
	protected String createWordSetCollection(String setLemma, String setType) {

		TelemetryManager.log("creating Word Set (" + setType + ") lemma " + setLemma);

		Request setReq = getRequest(languageId, GraphEngineManagers.COLLECTION_MANAGER, "createSet");
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
		Response res = getResponse(setReq);
		if (checkError(res))
			throw new ServerException(LanguageErrorCodes.ERROR_ADD_WORD_SET.name(), getErrorMessage(res));
		String setId = (String) res.get(GraphDACParams.set_id.name());
		return setId;
	}

	/**
	 * Adds the member(word) to set(WordSet).
	 *
	 * @param collectionId
	 *            the collection id
	 */
	protected void addMemberToSet(String collectionId) {
		TelemetryManager.log("adding word " + wordNode.getIdentifier() + "as member to wordSet " , collectionId);

		Request setReq = getRequest(languageId, GraphEngineManagers.COLLECTION_MANAGER, "addMember");

		setReq.put(GraphDACParams.member_id.name(), wordNode.getIdentifier());
		setReq.put(GraphDACParams.collection_id.name(), collectionId);
		setReq.put(GraphDACParams.collection_type.name(), CollectionTypes.SET.name());
		Response res = getResponse(setReq);
		if (checkError(res))
			throw new ServerException(LanguageErrorCodes.ERROR_ADD_WORD_SET.name(), getErrorMessage(res));
	}

	/**
	 * Creates the relation between any two given node with the relation type
	 *
	 * @param startNodeId
	 *            the start node id
	 * @param endNodeId
	 *            the end node id
	 * @param relationType
	 *            the relation type
	 */
	protected void createRelation(String startNodeId, String endNodeId, String relationType) {
		TelemetryManager.log("createRelation " , relationType + " between sets " + startNodeId + " and " + endNodeId);

		Request req = getRequest(languageId, GraphEngineManagers.GRAPH_MANAGER, "createRelation");
		req.put(GraphDACParams.start_node_id.name(), startNodeId);
		req.put(GraphDACParams.end_node_id.name(), endNodeId);
		req.put(GraphDACParams.relation_type.name(), relationType);
		Response res = getResponse(req);
		if (checkError(res)) {
			throw new ServerException(LanguageErrorCodes.ERROR_ADD_WORD_SET.name(), getErrorMessage(res));
		}

	}
	
	/**
	 * Checks if set with given lemma and type is found in existing relations of
	 * word
	 *
	 * @param type
	 *            the type
	 * @param lemma
	 *            the lemma
	 * @return true, if is exist
	 */
	protected boolean isExist(String type, String lemma) {

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
	
	protected void removeSetRelation(String type){
		for(Relation relation : existingWordSetRelatios) {
			if(type.equalsIgnoreCase((String)relation.getStartNodeMetadata().get(LanguageParams.type.name())))
				removeWordFromWordSet(relation.getStartNodeId());
		}
	}

	/**
	 * Checks if set with given lemmas and type is found in existing relations
	 * of word when all of them were found return true or when few of them only
	 * found, remove non found lemmas from new lemmas, remove membership
	 * relation for the one which were not found and return false
	 * 
	 * @param type
	 *            the type
	 * @param newLemmas
	 *            the new lemmas
	 * @return true, if is exist
	 */
	protected boolean isExist(String type, List<String> newLemmas) {

		if (existingWordSetRelatios.size() > 0) {
			List<String> existingLemmas = new ArrayList<String>();
			Map<String, String> existingSetMap = new HashMap<String, String>();
			for (Relation relation : existingWordSetRelatios) {
				if (type.equalsIgnoreCase((String) relation.getStartNodeMetadata().get(LanguageParams.type.name()))) {
					String lemma = (String) relation.getStartNodeMetadata().get(LanguageParams.lemma.name());
					// trimming of constant precedence like "ENDS_WITH_"
					lemma = lemma.substring(lemma.lastIndexOf("_") + 1);
					existingLemmas.add(lemma);
					existingSetMap.put(lemma, relation.getStartNodeId());
				}
			}

			if (existingLemmas.containsAll(newLemmas))
				return true;

			List<String> dupLemmas = new ArrayList<>(newLemmas);
			// remove already existing lemma from newLemmas
			newLemmas.removeAll(existingLemmas);
			// remove newLemmas from existingLemmas so that remaining lemma will
			// be the one which should be removed
			existingLemmas.removeAll(dupLemmas);
			for (String lemma : existingLemmas)
				removeWordFromWordSet(existingSetMap.get(lemma));
			return false;
		}
		return false;
	}

	/**
	 * Removes the word from word set.
	 *
	 * @param setId
	 *            the set id
	 */
	protected void removeWordFromWordSet(String setId){
		TelemetryManager.log("Deleting relation : " , setId + " --> " + wordNode.getIdentifier());
        Request setReq = getRequest(languageId, GraphEngineManagers.COLLECTION_MANAGER, "removeMember");
        setReq.put(GraphDACParams.member_id.name(), wordNode.getIdentifier());
        setReq.put(GraphDACParams.collection_id.name(), setId);
        setReq.put(GraphDACParams.collection_type.name(), CollectionTypes.SET.name());
        Response res = getResponse(setReq);
		if (checkError(res))
			throw new ServerException(LanguageErrorCodes.ERROR_ADD_WORD_SET.name(), getErrorMessage(res));
	}

}
