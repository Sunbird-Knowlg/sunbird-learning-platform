package org.ekstep.common.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.*;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisionUtil {

	private static final String APPLICATION_NAME = "Google-VisionSample/1.0";

	private final Vision vision;

	public VisionUtil(Vision vision) {
		this.vision = vision;
	}

	public Map<String, Object> getTags(File url, VisionUtil vision) throws IOException, GeneralSecurityException {
		Map<String, Object> label = vision.labelImage(url.toPath());
		return label;
	}

	public List<String> getFlags(File url, VisionUtil vision) throws IOException, GeneralSecurityException {
		List<String> flags = vision.safeSearch(url.toPath());
		return flags;
	}

	private static Map<String, Object> processLabels(List<EntityAnnotation> label_map) {

		Map<String, Object> labelMap = new HashMap<String, Object>();
		List<String> list_90 = new ArrayList<String>();
		List<String> list_80 = new ArrayList<String>();
		for (EntityAnnotation label : label_map) {
			if (label.getScore() >= 0.90) {
				list_90.add(label.getDescription());
				labelMap.put("90-100", list_90);
			} else if (label.getScore() >= 0.80) {
				list_80.add(label.getDescription());
				labelMap.put("80-90", list_80);
			}
		}

		return labelMap;
	}

	public static Vision getVisionService() throws IOException, GeneralSecurityException {

		GoogleCredential credential = GoogleCredential.getApplicationDefault().createScoped(VisionScopes.all());
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		return new Vision.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, credential).setApplicationName(
				APPLICATION_NAME).build();
	}

	public Map<String, Object> labelImage(Path path) throws IOException {

		byte[] data = Files.readAllBytes(path);
		AnnotateImageRequest request = new AnnotateImageRequest().setImage(new Image().encodeContent(data)).setFeatures(
				ImmutableList.of(new Feature().setType("LABEL_DETECTION")));
		Vision.Images.Annotate annotate = vision.images().annotate(new BatchAnnotateImagesRequest().setRequests(ImmutableList.of(request)));
		annotate.setDisableGZipContent(true);
		BatchAnnotateImagesResponse batchResponse = annotate.execute();
		assert batchResponse.getResponses().size() == 1;
		AnnotateImageResponse response = batchResponse.getResponses().get(0);
		if (response.getLabelAnnotations() == null) {
			throw new IOException(response.getError() != null ? response.getError().getMessage()
					: "Unknown error getting image annotations");
		}
		Map<String, Object> labels = processLabels(response.getLabelAnnotations());
		return labels;
	}

	public List<String> safeSearch(Path path) throws IOException {

		byte[] data = Files.readAllBytes(path);

		AnnotateImageRequest request = new AnnotateImageRequest().setImage(new Image().encodeContent(data)).setFeatures(
				ImmutableList.of(new Feature().setType("SAFE_SEARCH_DETECTION")));
		Vision.Images.Annotate annotate = vision.images().annotate(new BatchAnnotateImagesRequest().setRequests(ImmutableList.of(request)));
		annotate.setDisableGZipContent(true);
		BatchAnnotateImagesResponse batchResponse = annotate.execute();
		assert batchResponse.getResponses().size() == 1;
		AnnotateImageResponse response = batchResponse.getResponses().get(0);
		if (response.getSafeSearchAnnotation() == null) {
			throw new IOException(response.getError() != null ? response.getError().getMessage()
					: "Unknown error getting image annotations");
		}

		List<String> search = processSearch(response.getSafeSearchAnnotation());
		return search;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<String> processSearch(SafeSearchAnnotation safeSearchAnnotation) {

		Map<String, String> map = (Map) safeSearchAnnotation;
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		List<String> flagList = new ArrayList<String>();
		List<String> res;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (result.containsKey(entry.getValue())) {
				res = result.get(entry.getValue());
				res.add(entry.getKey());
				result.put(entry.getValue(), res);
			} else {
				res = new ArrayList<String>();
				res.add(entry.getKey());
				result.put(entry.getValue(), res);
			}
		}

		for (Map.Entry<String, List<String>> entry : result.entrySet()) {
			if (entry.getKey().equalsIgnoreCase("LIKELY") || entry.getKey().equalsIgnoreCase("VERY_LIKELY")
					|| entry.getKey().equalsIgnoreCase("POSSIBLE")) {
				flagList.addAll(entry.getValue());
			}
		}
		return flagList;

	}

}
