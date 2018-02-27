package org.ekstep.taxonomy.util;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.ekstep.common.exception.ClientException;
import org.ekstep.graph.dac.model.Node;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test Cases for YouTube Service
 * 
 * @see YouTubeDataAPIV3Service
 * 
 * @author gauraw
 *
 */
public class YouTubeDataAPIV3ServiceTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void testYouTubeService_01() throws Exception {
		Node node = new Node();
		node.setMetadata(new HashMap<>());
		String artifactUrl = "https://www.youtube.com/watch?v=owr198WQpM8";
		YouTubeDataAPIV3Service.validateYoutubeLicense(artifactUrl, node);
		assertEquals("Creative Commons Attribution (CC BY)", (String) node.getMetadata().get("license"));
	}

	@Test
	public void testYouTubeService_02() throws Exception {
		Node node = new Node();
		node.setMetadata(new HashMap<>());
		String artifactUrl = "https://www.youtube.com/watch?v=_UR-l3QI2nE";
		YouTubeDataAPIV3Service.validateYoutubeLicense(artifactUrl, node);
		assertEquals("Standard YouTube License", (String) node.getMetadata().get("license"));
	}

	@Test
	public void testYouTubeService_03() throws Exception {
		exception.expect(ClientException.class);
		Node node = new Node();
		node.setMetadata(new HashMap<>());
		String artifactUrl = "https://goo.gl/bVBJNK";
		YouTubeDataAPIV3Service.validateYoutubeLicense(artifactUrl, node);
	}
}
