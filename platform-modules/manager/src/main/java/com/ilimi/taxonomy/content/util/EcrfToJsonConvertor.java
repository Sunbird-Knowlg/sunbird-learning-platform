package com.ilimi.taxonomy.content.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ilimi.taxonomy.content.common.ElementMap;
import com.ilimi.taxonomy.content.entity.Content;
import com.ilimi.taxonomy.content.entity.Controller;
import com.ilimi.taxonomy.content.entity.Manifest;
import com.ilimi.taxonomy.content.entity.Media;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.dto.MediaDTO;

public class EcrfToJsonConvertor {
	
	public String getContentJsonString(Content ecrf) {
		String content = "";
		Map<String, Object> map = new HashMap<String, Object>(); 
		if (null != ecrf) {
			map.putAll(getElementMap(ecrf.getData()));
			map.put(ContentWorkflowPipelineParams.manifest.name(), getManifestMap(ecrf.getManifest()));
			map.put(ContentWorkflowPipelineParams.controller.name(), getControllerMaps(ecrf.getControllers()));
		}
		return content;
	}
	
	private Map<String, Object> getManifestMap(Manifest manifest) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (null != manifest) {
			map.put(ContentWorkflowPipelineParams.media.name(), getMediaMaps(manifest.getMedias()));
		}
		return map;
	}
	
	private List<Map<String, Object>> getMediaMaps(List<Media> medias) {
		List<Map<String, Object>> mediaMaps = new ArrayList<Map<String, Object>>();
		if (null != medias && medias.size() > 0) {
			for (Media media: medias)
				mediaMaps.add(getMediaMap(media));
		}
		return mediaMaps;
	}
	
	private Map<String, Object> getMediaMap(Media media) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (null != media) {
			map.putAll(getElementMap(media.getData()));
		}
		return map;
	}
	
	private List<Map<String, Object>> getControllerMaps(List<Controller> controllers) {
		List<Map<String, Object>> controllerMaps = new ArrayList<Map<String, Object>>();
		if (null != controllers && controllers.size() > 0) {
			for (Controller controller: controllers)
				controllerMaps.add(getControllerMap(controller));
		}
		return controllerMaps;
	}
	
	private Map<String, Object> getControllerMap(Controller controller) {
		Map<String, Object> controllerMap = new HashMap<String, Object>();
		if (null != controller) {
			controllerMap.putAll(getElementMap(controller.getData()));
			controllerMap.put(ContentWorkflowPipelineParams.__cdata.name(), controller.getcData());
		}
		return controllerMap;
	}
	
	private Map<String, String> getElementMap(Map<String, String> data) {
		Map<String, String> map = new HashMap<String, String>();
		if (null != data) {
			for (Entry<String, String> entry: data.entrySet()) {
				if (!ElementMap.isSystemGenerateAttribute(entry.getKey()))
					map.put(entry.getKey(), entry.getValue());
			}
		}
		return map;
	}

}
