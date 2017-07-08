package org.ekstep.language.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.engine.router.GraphEngineManagers;

/**
 * 
 * 
 * @author rayulu
 *
 */
public class VarnaCache extends BaseManager {
	
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	private final Map<String, List<Node>> varnaMap = new HashMap<String, List<Node>>();
	private final Map<String, Map<String, String>> isoSymbolMap = new HashMap<String, Map<String, String>>();
	private final Map<String, Map<String, String>> altISOSymbolMap = new HashMap<String, Map<String, String>>();
	private final Map<String, Map<String, Node>> isoVarnaMap = new HashMap<String, Map<String, Node>>();
	private final Map<String, Map<String, String>> vowelSignMap = new HashMap<String, Map<String, String>>();
	private final Map<String, String> viramaMap = new HashMap<String, String>();
	private static VarnaCache instance = null;
	
	private VarnaCache() {
	}
	
	public static VarnaCache getInstance() {
		if (null == instance)
			instance = new VarnaCache();
		return instance;
	}
	
	public List<Node> getVarnaNodes(String languageId) {
		if (StringUtils.isNotBlank(languageId) && !varnaMap.containsKey(languageId))
			loadVarnas(languageId);
		return varnaMap.get(languageId);
	}
	
	public String getISOSymbol(String languageId, String varna) {
		if (!varnaMap.containsKey(languageId))
			loadVarnas(languageId);
		if (isoSymbolMap.containsKey(languageId) && isoSymbolMap.get(languageId).containsKey(varna))
			return isoSymbolMap.get(languageId).get(varna);
		return null;
	}
	
	public String getAltISOSymbol(String languageId, String varna) {
		if (!varnaMap.containsKey(languageId))
			loadVarnas(languageId);
		if (altISOSymbolMap.containsKey(languageId) && altISOSymbolMap.get(languageId).containsKey(varna))
			return altISOSymbolMap.get(languageId).get(varna);
		return null;
	}
	
	public Node getVarna(String languageId, String varna) {
		if (!varnaMap.containsKey(languageId))
			loadVarnas(languageId);
		if (isoSymbolMap.containsKey(languageId) && isoSymbolMap.get(languageId).containsKey(varna)) {
			String isoSymbol = isoSymbolMap.get(languageId).get(varna);
			if (isoVarnaMap.containsKey(languageId) && isoVarnaMap.get(languageId).containsKey(isoSymbol))
				return isoVarnaMap.get(languageId).get(isoSymbol);
		}
		return null;
	}
	
	public Node getVarnaNode(String languageId, String isoSymbol) {
		if (!varnaMap.containsKey(languageId))
			loadVarnas(languageId);
		if (isoVarnaMap.containsKey(languageId) && isoVarnaMap.get(languageId).containsKey(isoSymbol))
			return isoVarnaMap.get(languageId).get(isoSymbol);
		return null;
	}
	
	public String getViramaUnicode(String languageId) {
		if (!varnaMap.containsKey(languageId))
			loadVarnas(languageId);
		return viramaMap.get(languageId);
	}
	
	public String getVowelSign(String languageId, String vowel) {
		if (!varnaMap.containsKey(languageId))
			loadVarnas(languageId);
		if (vowelSignMap.containsKey(languageId) && vowelSignMap.get(languageId).containsKey(vowel))
			return vowelSignMap.get(languageId).get(vowel);
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public void loadVarnas(String languageId) {
		Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getNodesByObjectType");
		request.put(GraphDACParams.object_type.name(), "Varna");
		request.put(GraphDACParams.get_tags.name(), true);
		Response findRes = getResponse(request, LOGGER);
		if (!checkError(findRes)) {
			List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
			if (null != nodes && !nodes.isEmpty()) {
				for (Node node : nodes) {
					String isoSymbol = (String) node.getMetadata().get("isoSymbol");
					String altIsoSymbol = (String) node.getMetadata().get("altIsoSymbol");
					String type = (String) node.getMetadata().get("type");

					if (StringUtils.isNotBlank(isoSymbol)) {
						Map<String, String> map = isoSymbolMap.get(languageId);
						if (null == map)
							map = new HashMap<String, String>();
						map.put(node.getIdentifier(), isoSymbol);
						isoSymbolMap.put(languageId, map);
						
						Map<String, Node> nodeMap = isoVarnaMap.get(languageId);
						if (null == nodeMap)
							nodeMap = new HashMap<String, Node>();
						nodeMap.put(isoSymbol, node);
						isoVarnaMap.put(languageId, nodeMap);
					}
					if (StringUtils.isNotBlank(altIsoSymbol)) {
						Map<String, String> map = altISOSymbolMap.get(languageId);
						if (null == map)
							map = new HashMap<String, String>();
						map.put(node.getIdentifier(), altIsoSymbol);
						altISOSymbolMap.put(languageId, map);
					}
					
					if (StringUtils.equalsIgnoreCase("Vowel", type)) {
						String vowelSign = getVowelSignUnicode(node);
						if (StringUtils.isNotBlank(vowelSign)) {
							Map<String, String> map = vowelSignMap.get(languageId);
							if (null == map)
								map = new HashMap<String, String>();
							map.put(node.getIdentifier(), vowelSign);
							vowelSignMap.put(languageId, map);
						}
					} else if (StringUtils.equalsIgnoreCase("Virama", type)) {
						String virama = (String) node.getMetadata().get(GraphDACParams.unicode.name());
						if (StringUtils.isNotBlank(virama))
							viramaMap.put(languageId, virama);
					}
				}
				varnaMap.put(languageId, nodes);
			}
		}
	}
	
	private String getVowelSignUnicode(Node vowel){
		String unicode = "";
		List<Relation> inRelations = vowel.getInRelations();		
		if (null != inRelations && inRelations.size() > 0) {
			Relation associatedTo = inRelations.get(0);
			if(associatedTo != null){
				Map<String, Object> vowelSignMetaData = associatedTo.getStartNodeMetadata();
				unicode = (String) vowelSignMetaData.get(GraphDACParams.unicode.name());
			}
		}
		return unicode;
	}

}
