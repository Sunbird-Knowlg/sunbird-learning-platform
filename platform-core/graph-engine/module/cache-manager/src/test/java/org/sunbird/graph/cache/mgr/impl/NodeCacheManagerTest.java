package org.sunbird.graph.cache.mgr.impl;

import org.sunbird.common.exception.ClientException;
import org.junit.Assert;
import org.junit.Test;

public class NodeCacheManagerTest {
	
	@Test
	public void testSaveAndGetDefinitionNode() {
		String graphId = "domain";
		String objectType = "Content";
		String node = "ContentDefNode";
		NodeCacheManager.saveDefinitionNode(graphId, objectType, node);
		Object cachedData = NodeCacheManager.getDefinitionNode(graphId, objectType);
		Assert.assertTrue(node.equals(cachedData));
	}
	
	@Test
	public void testSaveAndGetAndDeleteDefinitionNode() {
		String graphId = "domain";
		String objectType = "Content";
		String node = "ContentNewDefNode";
		NodeCacheManager.saveDefinitionNode(graphId, objectType, node);
		Object cachedData = NodeCacheManager.getDefinitionNode(graphId, objectType);
		Assert.assertTrue(node.equals(cachedData));
		NodeCacheManager.deleteDefinitionNode(graphId, objectType);
		Object definition = NodeCacheManager.getDefinitionNode(graphId, objectType);
		Assert.assertEquals(null, definition);
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

	@Test
	public void testSaveAndGetAndDeleteDataNode() {
		String graphId = "domain";
		String identifier = "content_node_4";
		String node = "ContentNode";
		NodeCacheManager.saveDataNode(graphId, identifier, node);
		Object cachedData = NodeCacheManager.getDataNode(graphId, identifier);
		Assert.assertTrue(node.equals(cachedData));
		NodeCacheManager.deleteDataNode(graphId, identifier);
		Object deleteResult = NodeCacheManager.getDataNode(graphId, identifier);
		Assert.assertEquals(null, deleteResult);
	}
	
	@Test(expected = ClientException.class)
	public void testGetDataNodeWithoutGraphId() {
		NodeCacheManager.getDataNode(null, "Content_1");
	}

	@Test(expected = ClientException.class)
	public void testGetDataNodeWithoutIdentifier() {
		NodeCacheManager.getDataNode("domain", null);
	}

	@Test(expected = ClientException.class)
	public void testDeleteDataNodeWithoutGraphId() {
		NodeCacheManager.deleteDataNode(null, "Content_2");
	}

	@Test(expected = ClientException.class)
	public void testDeleteDataNodeWithoutIdentifier() {
		NodeCacheManager.getDataNode("domain", null);
	}
	
	@Test(expected = ClientException.class)
	public void testDeleteDataNodeWithoutAllParams() {
		NodeCacheManager.getDataNode(null, null);
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

	@Test(expected = ClientException.class)
	public void saveDataNodeWithoutNodeAndObjectType() {
		NodeCacheManager.saveDataNode(null, null, "ContentNode_1");
	}
	
	@Test(expected = ClientException.class)
	public void saveDataNodeWithoutGraphIdAndNode() {
		NodeCacheManager.saveDataNode(null, null, "ContentNode_1");
	}
	
	@Test(expected = ClientException.class)
	public void saveDataNodeWithoutGraphIdAndObjectType() {
		NodeCacheManager.saveDataNode(null, null, "ContentNode_1");
	}
	
	@Test(expected = ClientException.class)
	public void saveDataNodeWithoutAllParams() {
		NodeCacheManager.saveDataNode(null, null, null);
	}
	
	@Test(expected = ClientException.class)
	public void getDefinitionNodeWithoutGraphId() {
		NodeCacheManager.getDefinitionNode(null, "ContentDefNode");
	}

	@Test(expected = ClientException.class)
	public void getDefinitionNodeWithoutObjectType() {
		NodeCacheManager.getDefinitionNode("Content", null);
	}

	@Test(expected = ClientException.class)
	public void getDefinitionNodeWithoutAllParams() {
		NodeCacheManager.getDefinitionNode(null, null);
	}

	@Test(expected = ClientException.class)
	public void deleteDefinitionNodeWithoutGraphId() {
		NodeCacheManager.deleteDefinitionNode(null, "ContentDefNode");
	}

	@Test(expected = ClientException.class)
	public void deleteDefinitionNodeWithoutObjectType() {
		NodeCacheManager.deleteDefinitionNode("domain", null);
	}
	
	@Test(expected = ClientException.class)
	public void deleteDefinitionNodeWithoutAllParams() {
		NodeCacheManager.deleteDefinitionNode(null, null);
	}
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
	
	@Test(expected = ClientException.class)
	public void saveDefinitionNodeWithoutGraphIdAndObjectType() {
		NodeCacheManager.saveDefinitionNode(null, null, "ContentDefNode");
	}
	
	@Test(expected = ClientException.class)
	public void saveDefinitionNodeWithoutGraphIdAndNode() {
		NodeCacheManager.saveDefinitionNode(null, "Content", null);
	}
	
	@Test(expected = ClientException.class)
	public void saveDefinitionNodeWithoutNodeAndObjectType() {
		NodeCacheManager.saveDefinitionNode("domain", null, null);
	}
	
	@Test(expected = ClientException.class)
	public void saveDefinitionNodeWithoutObjectTypeAndNode() {
		NodeCacheManager.saveDefinitionNode("domain",null, null);
	}

	@Test(expected = ClientException.class)
	public void saveDefinitionNodeWithoutAllParams() {
		NodeCacheManager.saveDefinitionNode(null, null, null);
	}

}
