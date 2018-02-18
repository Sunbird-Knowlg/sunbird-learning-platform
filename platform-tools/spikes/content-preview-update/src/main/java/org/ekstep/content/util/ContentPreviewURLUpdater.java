package org.ekstep.content.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.graph.util.NodeUtil;
import org.ekstep.graph.util.SearchUtil;

public class ContentPreviewURLUpdater {

	private final static String GRAPH_ID = "domain";
	private static String path = "";
	private final static String PREVIEW_URL = "previewUrl";
	
	public static void main(String args[]) throws Exception {
		path = args[0];
		if (StringUtils.isBlank(path))
			throw new Exception("Invalid neo4j path.");
		System.out.println("Neo4j path : "+path);
		ContentPreviewURLUpdater updater = new ContentPreviewURLUpdater();
		//update videp/pdf/epub/youtube content previewURL
		updater.updateResourseTypeContent();
	}
	
	private void updateResourseTypeContent() {
		int startPosition = 0;
		int resultSize = 1000;
		boolean update = true;
		while (update) {
			List<Map<String, Object>> nodes = SearchUtil.getResourceContentNodes(GRAPH_ID, path, startPosition, resultSize);
			System.out.println("updating first "+nodes.size()+" nodes");
			if (nodes.size() > 0 && !nodes.isEmpty()) {
				for(Map<String, Object> node : nodes) {
					String contentId = (String) node.get("IL_UNIQUE_ID");
					String artifactUrl = (String) node.get("artifactUrl");
					if (StringUtils.isNotBlank(contentId) && StringUtils.isNotBlank(artifactUrl)) {
						Map<String, Object> content = new HashMap<>();
						content.put(PREVIEW_URL, artifactUrl);
						NodeUtil.updateNode(GRAPH_ID, path, contentId, content);
					}
				}
			} else {
				update = false;
			}
			startPosition += resultSize;
		}
	}
	
}
