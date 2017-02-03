package org.ekstep.visionApi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.SafeSearchAnnotation;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
/**
 * This class TestVisionAPI is a sample 
 * spike project to demonstrate the GOOGLE_VISION_SERVICE
 * for Image-tagging and Image-flagging
 * 
 * @author Rashmi
 * 
 */
public class TestVisionApi {

	/** The APPLICATION_NAME */
	private static final String APPLICATION_NAME = "Google-VisionSample/1.0";

	/** The output path */
	private static String outputPath = "src/main/resources/output.json";

	/** The folder */
	private static File folder = new File("src/main/resources/images");

	/** The Object Mapper */
	private static ObjectMapper mapper = new ObjectMapper();

	/** The main entry point to Test the vision API */ 
	public static void main(String[] args) throws IOException, GeneralSecurityException {

		TestVisionApi app = new TestVisionApi(getVisionService());
		List<Map<String, Object>> result_list = new ArrayList<Map<String, Object>>();
		List<Path> paths = listFilesForFolder(folder);
		Map<String, Object> result = null;
		for (Path path : paths) {
			Map<String, Object> labels = app.labelImage(path);
			Map<String, List<String>> search = app.safeSearch(path);
			result = new HashMap<String, Object>();
			result.put("file", path.getFileName().toString());
			result.put("labels", labels);
			result.put("flags", search);
			result_list.add(result);
		}
		String str = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result_list);
		PrintWriter pw = new PrintWriter(new FileOutputStream(outputPath));
		pw.write(str);
		pw.close();
	}

	/** The Vision Variable */
	private final Vision vision;

	/** The constructer */
	public TestVisionApi(Vision vision) {
		this.vision = vision;
	}
	
	/**
	 * The method getVisionService() holds logic for
	 * instantiaing and Authentication GOOGLE_VISION_SERVICE
	 * @return
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public static Vision getVisionService() throws IOException, GeneralSecurityException {
		GoogleCredential credential = GoogleCredential.getApplicationDefault().createScoped(VisionScopes.all());
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		return new Vision.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, credential)
				.setApplicationName(APPLICATION_NAME).build();
	}

	/**
	 * The method labelImage holds logic to call the GOOGLE_VISION_API
	 * LABEL_DETECTION to get LABELS for a given image
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public Map<String, Object> labelImage(Path path) throws IOException {
		byte[] data = Files.readAllBytes(path);

		AnnotateImageRequest request = new AnnotateImageRequest().setImage(new Image().encodeContent(data))
				.setFeatures(ImmutableList.of(new Feature().setType("LABEL_DETECTION")));
		Vision.Images.Annotate annotate = vision.images()
				.annotate(new BatchAnnotateImagesRequest().setRequests(ImmutableList.of(request)));
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

	/**
	 * The method processLabels method holds logic to 
	 * process the result for LABEL_DETECTION from 
	 * Google Vision API as a MAP
	 * 
	 * @param label_map
	 * @return
	 */
	private static Map<String, Object> processLabels(List<EntityAnnotation> label_map) {
		Map<String, Object> labelMap = new HashMap<String, Object>();
		List<String> list_90 = new ArrayList<String>();
		List<String> list_80 = new ArrayList<String>();
		List<String> list_70 = new ArrayList<String>();
		List<String> list_60 = new ArrayList<String>();
		List<String> list_50 = new ArrayList<String>();
		for (EntityAnnotation label : label_map) {

			if (label.getScore() >= 0.90) {
				list_90.add(label.getDescription());
				labelMap.put("90-100", list_90);
			} else if (label.getScore() >= 0.80) {
				list_80.add(label.getDescription());
				labelMap.put("80-90", list_80);
			} else if (label.getScore() >= 0.70) {
				list_70.add(label.getDescription());
				labelMap.put("70-80", list_70);
			} else if (label.getScore() >= 0.60) {
				list_60.add(label.getDescription());
				labelMap.put("60-70", list_60);
			} else if (label.getScore() >= 0.50) {
				list_50.add(label.getDescription());
				labelMap.put("50-60", list_50);
			}
		}
		return labelMap;
	}

	/**
	 * The method safeSearch holds logic to call the GOOGLE_VISION_API
	 * safe_search to get FLAGS for a given image
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public Map<String, List<String>> safeSearch(Path path) throws IOException {
		byte[] data = Files.readAllBytes(path);

		AnnotateImageRequest request = new AnnotateImageRequest().setImage(new Image().encodeContent(data))
				.setFeatures(ImmutableList.of(new Feature().setType("SAFE_SEARCH_DETECTION")));
		Vision.Images.Annotate annotate = vision.images()
				.annotate(new BatchAnnotateImagesRequest().setRequests(ImmutableList.of(request)));
		annotate.setDisableGZipContent(true);
		BatchAnnotateImagesResponse batchResponse = annotate.execute();
		assert batchResponse.getResponses().size() == 1;
		AnnotateImageResponse response = batchResponse.getResponses().get(0);
		if (response.getSafeSearchAnnotation() == null) {
			throw new IOException(response.getError() != null ? response.getError().getMessage()
					: "Unknown error getting image annotations");
		}
		Map<String, List<String>> search = processSearch(response.getSafeSearchAnnotation());
		return search;
	}

	/**
	 * The method processflags method holds logic to 
	 * process the result for SAFE_SEARCH from 
	 * Google Vision API as a MAP
	 * 
	 * @param safeSearchAnnotation
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, List<String>> processSearch(SafeSearchAnnotation safeSearchAnnotation) {
		Map<String, String> map = (Map) safeSearchAnnotation;
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		List<String> res;
		for (Entry<String, String> entry : map.entrySet()) {
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
		return result;
	}

	/**
	 * This method is used to listFiles in the given folder
	 */
	public static List<Path> listFilesForFolder(final File folder) {
		List<Path> paths = new ArrayList<Path>();
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			}
			paths.add(fileEntry.toPath());
		}
		return paths;
	}
}
