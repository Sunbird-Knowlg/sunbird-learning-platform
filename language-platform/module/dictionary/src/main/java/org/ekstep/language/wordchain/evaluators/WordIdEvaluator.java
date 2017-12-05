package org.ekstep.language.wordchain.evaluators;

import java.util.ArrayList;
import java.util.List;

import org.ekstep.graph.dac.enums.SystemProperties;
import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;

public class WordIdEvaluator implements Evaluator {

	private List<String> ids = new ArrayList<String>();

	public WordIdEvaluator(List<String> ids) {
		super();
		this.ids = ids;
	}

	@Override
	public Evaluation evaluate(Path path) {
		Node endNode = path.endNode();
		if (endNode.hasProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name())) {
			String objectType = (String) endNode.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name());
			if (objectType.equalsIgnoreCase(LanguageObjectTypes.Word.name())) {
				if (endNode.hasProperty(SystemProperties.IL_UNIQUE_ID.name())) {
					String identifier = (String) endNode.getProperty(SystemProperties.IL_UNIQUE_ID.name());
					if (ids.contains(identifier)) {
						return Evaluation.INCLUDE_AND_CONTINUE;
					}
				}
			} else {
				return Evaluation.INCLUDE_AND_CONTINUE;
			}
		}
		return Evaluation.EXCLUDE_AND_PRUNE;
	}
}
