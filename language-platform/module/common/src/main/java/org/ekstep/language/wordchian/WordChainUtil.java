package org.ekstep.language.wordchian;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.ekstep.language.measures.entity.WordComplexity;
import org.ekstep.language.util.WordUtil;

import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

/**
 * The Class WordChainUtil, provides functionality to update the word with
 * WordSet for RhymingSound and AksharaBoundary WordChains
 *
 * @author karthik
 */
public class WordChainUtil {

	/** The LOGGER. */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	/** The word util. */
	private WordUtil wordUtil = new WordUtil();

	/**
	 * Update word with WordSet of AksharaBoundary and RhymingSound
	 *
	 * @param languageId
	 *            the language id
	 * @param node
	 *            the node
	 * @param wc
	 *            the wc
	 * @throws Exception
	 *             the exception
	 */
	public void updateWordSet(String languageId, Node node, WordComplexity wc) throws Exception {
		LOGGER.log("updateWordSet  languageId " + languageId + " | Word Identifier" + node.getIdentifier());
		List<Relation> existingWordSetRelatios = getExistingWordSetRelations(node);
		new RhymingSoundSet(languageId, node, wc, existingWordSetRelatios).create();
		new PhoneticBoundarySet(languageId, node, wc, existingWordSetRelatios).create();
		Node updatedNode = wordUtil.getDataNode(languageId, node.getIdentifier());
		node.setInRelations(updatedNode.getInRelations());
	}

	/**
	 * Gets the existing WordSet relations for any given word.
	 *
	 * @param word
	 *            the word
	 * @return the existing word set relations
	 */
	protected List<Relation> getExistingWordSetRelations(Node word) {

		List<Relation> wordSetRelations = new ArrayList<Relation>();
		List<Relation> inRelation = word.getInRelations();
		for (Relation rel : inRelation) {
			String relType = rel.getRelationType();
			Map<String, Object> startNodeMetadata = rel.getStartNodeMetadata();
			String startNodeObjType = (String) startNodeMetadata.get(SystemProperties.IL_FUNC_OBJECT_TYPE.name());
			// String startNodeWordSetType = (String)
			// startNodeMetadata.get(LanguageParams.type.name());
			if (relType.equalsIgnoreCase(RelationTypes.SET_MEMBERSHIP.relationName())
					&& startNodeObjType.equalsIgnoreCase(LanguageObjectTypes.WordSet.name())) {
				wordSetRelations.add(rel);
			}
		}
		return wordSetRelations;
	}
}
