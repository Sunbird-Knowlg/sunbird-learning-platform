package com.ilimi.graph.cache.mgr.impl;

import org.junit.Assert;
import org.junit.Test;

import com.ilimi.common.exception.ClientException;

public class NodeCacheManagerTest {
	
	@Test(expected = ClientException.class)
	public void saveDefinitionNodeWithoutGraphId() {
		NodeCacheManager.saveDefinitionNode(null, "Content", "ContentDefNode");
	}
	
	@Test(expected = ClientException.class)
	public void saveDefinitionNodeWithoutObjectType() {
		NodeCacheManager.saveDefinitionNode("domain", null, "ContentDefNode");
	}
	
	@Test(expected = ClientException.class)
	public void saveDefinitionNodeWithoutNode() {
		NodeCacheManager.saveDefinitionNode("domain", "Content", null);
	}
	
	@Test
	public void testSaveAndGetDefinitionNode() {
		String graphId = "domain";
		String objectType = "Content";
		String node = "ContentDefNode";
		NodeCacheManager.saveDefinitionNode(graphId, objectType, node);
		Object cachedData = NodeCacheManager.getDefinitionNode(graphId, objectType);
		Assert.assertTrue(node.equals(cachedData));
	}
	
	@Test(expected = ClientException.class)
	public void saveDataNodeWithoutGraphId() {
		NodeCacheManager.saveDataNode(null, "content_node_1", "ContentNode");
	}
	
	@Test(expected = ClientException.class)
	public void saveDataNodeWithoutObjectType() {
		NodeCacheManager.saveDataNode("domain", null, "ContentNode");
	}
	
	@Test(expected = ClientException.class)
	public void saveDataNodeWithoutNode() {
		NodeCacheManager.saveDataNode("domain", "content_node_1", null);
	}
	
	@Test
	public void testSaveAndGetDataNode() {
		String graphId = "domain";
		String identifier = "content_node_1";
		String node = "ContentNode";
		NodeCacheManager.saveDataNode(graphId, identifier, node);
		Object cachedData = NodeCacheManager.getDataNode(graphId, identifier);
		Assert.assertTrue(node.equals(cachedData));
	}
	
}
