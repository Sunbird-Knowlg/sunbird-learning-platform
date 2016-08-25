package com.ilimi.resource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.controller.BaseController;

@Controller
@RequestMapping("/v1/resourcebundle")
public class ResourceBundleController extends BaseController {

	@RequestMapping(value = "/{id:.+}", method = RequestMethod.GET, headers = "Accept=application/json")
	@ResponseBody
	public void find(@PathVariable(value = "id") String id) throws Exception {
		String ApiUrl = UrlMapper(id);
		readFromJson(ApiUrl);
	}

	public void readFromJson(String url) throws Exception {
		URL oracle = new URL(url);
		try (BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()))) {
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				System.out.println(inputLine);
		}
	}

	private String UrlMapper(String id) {
		String baseUrl = "http://www.oracle.com/";
		String ApiUrl = baseUrl + id;
		return ApiUrl;
	}

}
