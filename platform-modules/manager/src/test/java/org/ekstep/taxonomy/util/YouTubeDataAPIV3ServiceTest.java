package org.ekstep.taxonomy.util;

import static org.junit.Assert.assertEquals;

import org.ekstep.common.exception.ClientException;
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
		String artifactUrl = "https://www.youtube.com/watch?v=owr198WQpM8";
		String result = YouTubeDataAPIV3Service.getLicense(artifactUrl);
		assertEquals("creativeCommon", result);
	}

	@Test
	public void testYouTubeService_02() throws Exception {
		String artifactUrl = "https://www.youtube.com/watch?v=_UR-l3QI2nE";
		String result = YouTubeDataAPIV3Service.getLicense(artifactUrl);
		assertEquals("youtube", result);
	}

	@Test
	public void testYouTubeService_03() throws Exception {
		exception.expect(ClientException.class);
		String artifactUrl = "https://goo.gl/bVBJNK";
		String result = YouTubeDataAPIV3Service.getLicense(artifactUrl);
	}
}
