package org.ekstep.language.mgr;

import java.util.List;
import java.util.Map;

import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.model.Node;

public interface IWordChainsManager {

	@SuppressWarnings("rawtypes")
	public Response getWordChain(int wordChainsLimit, List<Map> words, Node ruleNode, String graphId) throws Exception;

}
