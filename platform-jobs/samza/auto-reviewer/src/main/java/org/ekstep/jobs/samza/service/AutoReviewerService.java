package org.ekstep.jobs.samza.service;

import com.microsoft.azure.storage.core.Logger;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.common.util.PDFUtil;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.jobs.samza.util.SamzaCommonParams;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.learning.util.ControllerUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ekstep.common.util.HttpRestUtil.makeDSPostRequest;

public class AutoReviewerService  implements ISamzaService {

	private static JobLogger LOGGER = new JobLogger(AutoReviewerService.class);
	private Config config = null;
	private Integer MAX_ITERATION_COUNT = null;
	private ControllerUtil util = new ControllerUtil();
	private static Boolean isDummyResponse = false;
	private static String passportKey = "";
	private static String dsUri = "http://50.1.0.13:5000/ML/AutoReview";
	private static Integer pdfSize = 5;

	@Override
	public void initialize(Config config) throws Exception {
		this.config = config;
		JSONUtils.loadProperties(config);
		LOGGER.info("Service config initialized");
		MAX_ITERATION_COUNT = (Platform.config.hasPath("max.iteration.count.samza.job")) ?
				Platform.config.getInt("max.iteration.count.samza.job") : 1;
		LearningRequestRouterPool.init();
		LOGGER.info("Learning Actor System initialized");
		isDummyResponse = StringUtils.equalsIgnoreCase("true",Platform.config.getString("curate.dummy.response"))?true:false;
		passportKey = Platform.config.getString("graph.passport.key.base");
		dsUri = Platform.config.getString("ds.api.url");
		pdfSize = Platform.config.getInt("pdf.size.allowed");
	}

	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception {
		LOGGER.info("AutoReviewerService ---> processMessage ---->>> start");
		if (null == message) {
			LOGGER.info("Null Event Received. So Skipped Processing.");
			return;
		}
		Map<String, Object> edata = (Map<String, Object>) message.get(SamzaCommonParams.edata.name());
		Map<String, Object> object = (Map<String, Object>) message.get(SamzaCommonParams.object.name());
		if (!validateEvent(edata, object)) {
			LOGGER.info("Event Ignored. Event Validation Failed for post-publish-processor operations.");
			return;
		}
		LOGGER.info("Event Received: "+message);
		List<String> tasks = (List<String>) edata.get("tasks");
		LOGGER.info("Tasks Received: "+tasks);
		String identifier = (String) object.get("id");
		String artifactUrl = (String) edata.get("artifactUrl");
		Node node = util.getNode("domain",identifier);
		LOGGER.info("=============== DEBUG INFO ============");
		LOGGER.info("identifier : "+identifier);
		LOGGER.info("artifactUrl : "+artifactUrl);
		if(null!=node)
			LOGGER.info("Node is not null for "+identifier);
		LOGGER.info("isDummyResponse : "+isDummyResponse);
		LOGGER.info("passportKey : "+passportKey);
		LOGGER.info("dsUri : "+dsUri);
		LOGGER.info("pdfSize : "+pdfSize);
		LOGGER.info("=============== DEBUG INFO ============");

		if(CollectionUtils.isNotEmpty(tasks) && (tasks.contains("quality") || tasks.contains("keywords") || tasks.contains("tags"))){
			if(isDummyResponse){
				LOGGER.info("Inserting Dummy Metadata For ML API Call : "+identifier);
				delay(30000);
				updateSuccessMeta(identifier, node);
			}
			else{
				//TODO: Make ML API Call
				Map<String, Object> request = new HashMap<String, Object>(){{
					put("content_id",identifier);
					put("artifactUrl",artifactUrl);
					put("mimeType",(String)node.getMetadata().get("mimeType"));
					put("version","1.0");
					put("timestamp",(String)node.getMetadata().get("lastUpdatedOn"));
				}};
				Response response = makeDSPostRequest(dsUri,request,new HashMap<String, String>());
				if(checkError(response)){
					LOGGER.info("ML API Call Failed For Content Id : "+identifier);
				}else{
					LOGGER.info("ML API Call Successful For Content Id : "+identifier);
				}
			}
		}

		// kp metadata update
		if(CollectionUtils.isNotEmpty(tasks) && tasks.contains("size") && StringUtils.equalsIgnoreCase("application/pdf",(String)node.getMetadata().get("mimeType"))){
			LOGGER.info("Computing Size For "+identifier);
			//String directory = "/tmp/"+identifier+File.separator+getFieNameFromURL(artifactUrl);
			File file = HttpDownloadUtility.downloadFile(artifactUrl,"/tmp");
			int count = PDFUtil.getPageCount(file);
			LOGGER.info("count :: "+count);
			String sizeSt = count<=pdfSize?"Passed":"Failed";
			Map<String, Object> meta = new HashMap<String, Object>(){{
				put("name","Size");
				put("type", String.valueOf(count));
				put("status",sizeSt);
				put("result",sizeSt);
			}};
			Node n = util.getNode("domain", identifier);
			n.getMetadata().put("ckp_size",meta);
			n.getMetadata().put("versionKey",passportKey);
			Response response = util.updateNode(n);

			if(checkError(response)){
				LOGGER.info("Error Occurred While Performing Curation. Error in updating content with size metadata");
				LOGGER.info("Error Response | Result : "+response.getResult()+" | Params :"+response.getParams() + " | Code :"+response.getResponseCode().toString());
			}else{
				LOGGER.info("size metadata updated for "+identifier);
			}

		}
	}

	private String  getFieNameFromURL(String fileUrl) {
		String fileName = FilenameUtils.getBaseName(fileUrl) + "_" + System.currentTimeMillis();
		if (!FilenameUtils.getExtension(fileUrl).isEmpty()) fileName += "." + FilenameUtils.getExtension(fileUrl);
		return fileName;
	}

	private void delay(int time) {
		try{
			Thread.sleep(time);
		}catch(Exception e){

		}
	}

	private void updateSuccessMeta(String identifier, Node node) {
		Map<String, Object> curationMetadata = new HashMap<>();
		curationMetadata.put("cml_tags",new HashMap<String, Object>(){{
			put("name","Suggested Tags");
			put("type", "tags");
			put("status","Passed");
			put("result",new ArrayList<String>(){{
				add("Science");
				add("Physics");
			}});
		}});
		curationMetadata.put("cml_keywords",new HashMap<String, Object>(){{
			put("name","Suggested Keywords");
			put("type", "keywords");
			put("status","Passed");
			put("result",new ArrayList<String>(){{
				add("Force");
				add("Newton's Law");
				add("Motion");
			}});
		}});
		curationMetadata.put("cml_quality",new HashMap<String, Object>(){{
			put("name","Content Quality Check");
			put("type", "quality");
			put("status","Passed");
			put("result",new HashMap<String, Object>(){{
				put("audio",new HashMap<String, Object>(){{
					put("status","Passed");
					put("score","8/10");
					put("percentile","80%");
				}});
				put("image",new HashMap<String, Object>(){{
					put("status","Passed");
					put("score","7/10");
					put("percentile","70%");
				}});
				put("language_complexity",new HashMap<String, Object>(){{
					put("status","Passed");
					put("ph_score","20");
					put("og_score","15.5");
					put("percentile","10%");
				}});
				put("profanity",new HashMap<String, Object>(){{
					put("status","Passed");
					put("result",new ArrayList<String>());
				}});
			}});
		}});

		node.getMetadata().putAll(curationMetadata);
		node.getMetadata().put("versionKey",passportKey);
		Response response = util.updateNode(node);
		if(checkError(response)){
			LOGGER.info("Error Occurred While Performing Curation. Error in updating content with dummy metadata for ml");
		}else{
			LOGGER.info("Updated Dummy ML Metadata for "+identifier);
		}
	}

	private boolean validateEvent(Map<String, Object> edata, Map<String, Object> object) {
		if (MapUtils.isEmpty(object) || StringUtils.isBlank((String) object.get("id")) ||
				MapUtils.isEmpty(edata) || StringUtils.isBlank((String) edata.get("action")))
			return false;
		String action= (String) edata.get("action");
		Integer iteration = (Integer) edata.get(SamzaCommonParams.iteration.name());
		String contentType = (String) edata.get("contentType");
		return (StringUtils.isNotEmpty(action) && iteration <= MAX_ITERATION_COUNT);
	}

	public boolean checkError(Response response) {
		ResponseParams params = response.getParams();
		if (null != params) {
			if (StringUtils.equals(ResponseParams.StatusType.failed.name(), params.getStatus())) {
				return true;
			}
		}
		return false;
	}
}
