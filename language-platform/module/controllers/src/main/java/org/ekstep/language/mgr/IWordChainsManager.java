package org.ekstep.language.mgr;

import java.util.List;
import java.util.Map;

import com.ilimi.graph.dac.model.Node;

public interface IWordChainsManager {

	public void getWordChain(String traversalId, int wordChainsLimit, List<Map> words, Node ruleNode, String graphId);

}
