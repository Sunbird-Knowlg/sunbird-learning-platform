package org.sunbird.test;

import org.ekstep.common.dto.Response;
import org.ekstep.common.util.ApiUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ApiUtilTest {

	@Test
	public void testKeywords() throws Exception {
		Map<String, Object> request = new HashMap<String, Object>();
		String input = "Astronomy is one of the oldest natural sciences. Early civilizations dating back before 3000 BCE, such as the Sumerians, ancient Egyptians, and the Indus Valley Civilisation, had a predictive knowledge and a basic understanding of the motions of the Sun, Moon, and stars. The stars and planets, believed to represent gods, were often worshipped. While the explanations for the observed positions of the stars were often unscientific and lacking in evidence, these early observations laid the foundation for later astronomy, as the stars were found to traverse great circles across the sky,[9] which however did not explain the positions of the planets.";
		request.put("text",input);
		Response resp = ApiUtil.makeKeyWordsPostRequest(request);
		System.out.println(resp.getResult());
	}
}
