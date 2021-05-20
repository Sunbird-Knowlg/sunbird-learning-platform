package org.sunbird.content.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.sunbird.common.Platform;
import org.sunbird.common.Slug;
import org.sunbird.common.dto.Response;
import org.sunbird.common.enums.TaxonomyErrorCodes;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.util.HttpDownloadUtility;
import org.sunbird.common.util.S3PropertyReader;
import org.sunbird.content.entity.Manifest;
import org.sunbird.content.entity.Media;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.operation.finalizer.BaseFinalizer;
import org.sunbird.graph.cache.util.RedisStoreUtil;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.learning.contentstore.ContentStore;
import org.sunbird.learning.util.CloudStore;
import org.sunbird.learning.util.ControllerUtil;
import org.sunbird.telemetry.logger.TelemetryManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PublishFinalizeUtil extends BaseFinalizer{
	private static final String CONTENT_FOLDER = "cloud_storage.content.folder";
	private static final String ARTEFACT_FOLDER = "cloud_storage.artefact.folder";
	private static final Boolean CATEGORY_CACHE_READ = Platform.config.hasPath("master.category.cache.read") ? Platform.config.getBoolean("master.category.cache.read") : false;
	private static final int CATEGORY_CACHE_TTL = Platform.config.hasPath("master.category.cache.ttl") ? Platform.config.getInt("master.category.cache.ttl") : 86400;
	private static ObjectMapper mapper = new ObjectMapper();
	private static final Map<String, String> frameworkCategorySearchMetadataMapping = new HashMap<String, String>(){{
		put("se_boards", "board");
		put("se_subjects", "subject");
		put("se_mediums", "medium");
		put("se_topics", "topic");
		put("se_gradeLevels", "gradeLevel");
	}};
	
	private ContentStore contentStore = new ContentStore();
	ControllerUtil controllerUtil = new ControllerUtil();
	
	public PublishFinalizeUtil(ContentStore contentStore) {
		this.contentStore = contentStore;
	}
	public PublishFinalizeUtil(ControllerUtil controllerUtil) {
		this.controllerUtil = controllerUtil;
	}
	public PublishFinalizeUtil() {}

	public String uploadFile(String fileUrl, Node node, String basePath) {
    	
		File file = HttpDownloadUtility.downloadFile(fileUrl, basePath + File.separator + "itemset");
		if (null != file) {
			String newFileName = node.getIdentifier() + "_" + System.currentTimeMillis() + "." + FilenameUtils.getExtension(file.getName());
    		boolean renameStatus = file.renameTo(new File(file.getParentFile(),newFileName));
			
			try {
				String folder = S3PropertyReader.getProperty(CONTENT_FOLDER);
				folder = folder + "/" + Slug.makeSlug(node.getIdentifier(), true) + "/" + S3PropertyReader.getProperty(ARTEFACT_FOLDER);
				String[] url = null;
				if(renameStatus)
					url = CloudStore.uploadFile(folder, new File(file.getParent() + "/" + newFileName), true);
				else
					url = CloudStore.uploadFile(folder, file, true);
				return url[1];
			} catch (Throwable e) {
				TelemetryManager.error("Error during uploading question paper pdf file for content:: " + node.getIdentifier() + " Error:: " + e.getStackTrace());
				throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(),
	                    "Error during uploading question paper pdf file for content :" + node.getIdentifier()+". Please Try Again After Sometime!");
			} finally {
				try {
					if(null != file)
						delete(file);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public void replaceArtifactUrl(Node node) {
		String artifactBasePath = (String)node.getMetadata().get("artifactBasePath");
		String artifactUrl = (String)node.getMetadata().get("artifactUrl");
		if(StringUtils.contains(artifactUrl, artifactBasePath)) {
			String sourcePath = StringUtils.substring(artifactUrl, artifactUrl.indexOf((artifactBasePath)));
			String destinationPath = StringUtils.replace(sourcePath, artifactBasePath + File.separator, "");
			
			try	{
				CloudStore.copyObjectsByPrefix(sourcePath, destinationPath, false);
				TelemetryManager.log("Copying Objects...DONE | Under: " + destinationPath);
				String newArtifactUrl = StringUtils.replace(artifactUrl, sourcePath, destinationPath);
				node.getMetadata().put("artifactUrl", newArtifactUrl);
				if(StringUtils.isNotBlank((String)node.getMetadata().get("cloudStorageKey"))) {
					String cloudStorageKey = StringUtils.replace((String)node.getMetadata().get("cloudStorageKey"), artifactBasePath + File.separator, "");
					node.getMetadata().put("cloudStorageKey", cloudStorageKey);
				}
				if(StringUtils.isNotBlank((String)node.getMetadata().get("s3Key"))) {
					String s3Key = StringUtils.replace((String)node.getMetadata().get("s3Key"), artifactBasePath + File.separator, "");
					node.getMetadata().put("s3Key", s3Key);
				}
			} catch(Exception e) {
				TelemetryManager.error("Error while copying object by prefix", e);
			}
		}
	}
	
	public void handleAssetWithExternalLink(Plugin ecrf, String contentId) {
		if (null != ecrf) {
			try {
				Manifest manifest = ecrf.getManifest();
				if (null != manifest) {
					List<Media> medias = manifest.getMedias();
					if(CollectionUtils.isNotEmpty(medias)) {
						List<Map<String, Object>> externalLink = new ArrayList<Map<String,Object>>();
						for (Media media: medias) {
							TelemetryManager.log("Validating Asset for External link: " + media.getId());
							if(validateAssetMediaForExternalLink(media)) {
								Map<String, Object> assetMap = new HashMap<String, Object>();
								assetMap.put("id", media.getId());
								assetMap.put("src", media.getSrc());
								assetMap.put("type", media.getType());
								externalLink.add(assetMap);
							}
						}
						contentStore.updateExternalLink(contentId, externalLink);
					}
				}
			}catch(Exception e) {
				TelemetryManager.error("Error while pushing externalLink details of content Id: " + contentId +" into cassandra.", e);
			}
		}
	}
	
	protected boolean validateAssetMediaForExternalLink(Media media){
		boolean isExternal = false;
		UrlValidator validator = new UrlValidator();
		String urlLink = media.getSrc();
		if(StringUtils.isNotBlank(urlLink) && 
				validator.isValid(media.getSrc()) &&
				!StringUtils.contains(urlLink, CloudStore.getContainerName()))
			isExternal = true; 
		return isExternal;
	}
	
	public Map<String, List<String>> enrichFrameworkMetadata(Node node){
		String[] defaultArray = {};
		Map<String, List<String>> frameworkMetadata = new HashMap<String, List<String>>();
		List<String> organisationFrameworkIds = StringUtils.isNotBlank((String)node.getMetadata().get("framework")) ?
				Arrays.asList((String)node.getMetadata().get("framework")) : new ArrayList<String>();
		List<String> targetFrameworkIds =  Arrays.asList((String[])node.getMetadata().getOrDefault("targetFWIds", defaultArray));
		
		frameworkMetadata.put("se_FWIds", mergeIds(organisationFrameworkIds, targetFrameworkIds));
		enrichFrameworkCategoryMetadata(frameworkMetadata, node);
		revalidateFrameworkCategoryMetadata(frameworkMetadata, node);
		return frameworkMetadata;
	}
	protected void enrichFrameworkCategoryMetadata(Map<String, List<String>> frameworkMetadata, Node node) {
		String[] defaultArray = {};
		Map<String, Object> metaData = node.getMetadata();
		Map<String, Object> frameworkCategoryFieldsMap = getFrameworkCategoryMap();
		
		Map<String, List<String>> idMap = (Map<String, List<String>>)frameworkCategoryFieldsMap.get("id");
		Map<String, List<String>> nameMap = (Map<String, List<String>>)frameworkCategoryFieldsMap.get("name");
		Map<String, List<String>> frameworkMetafieldsLabel = getLabels(metaData, node.getIdentifier(), (List<String>)frameworkCategoryFieldsMap.get("contentFrameworkMetafields"));
		
		idMap.keySet().forEach(category -> {
			List<String> orgData = Arrays.asList((String[])metaData.getOrDefault(idMap.get(category).get(0), defaultArray));
			List<String> targetData = Arrays.asList((String[])metaData.getOrDefault(idMap.get(category).get(1), defaultArray));
			frameworkMetadata.put(category, mergeIds(orgData, targetData));
		});
		nameMap.keySet().forEach(category -> {
			List<String> orgData = (List<String>)frameworkMetafieldsLabel.getOrDefault(nameMap.get(category).get(0), new ArrayList<String>());
			List<String> targetData = (List<String>)frameworkMetafieldsLabel.getOrDefault(nameMap.get(category).get(1), new ArrayList<String>());
			frameworkMetadata.put(category, mergeIds(orgData, targetData));
		});
		
	}
	
	private Map<String, Object> getFrameworkCategoryMap() {
		
		String masterCategoryCacheData =  null;
		List<Map<String, String>> masterCategory = null;
		if(CATEGORY_CACHE_READ) 
			masterCategoryCacheData =  RedisStoreUtil.get("masterCategories");
		
        if (StringUtils.isNotBlank(masterCategoryCacheData)) 
            try {
            	masterCategory = mapper.readValue(masterCategoryCacheData, new TypeReference<List<Map<String, String>>>() {});
            } catch (Exception e) {
                TelemetryManager.error("Error Occurred While Parsing master category data. Error is: ", e);
                throw new ServerException("ERR_MASTER_CATEGORY_DATA_PARSE", "Something Went Wrong While Processing the Master Category Data. ", e);
            }
        else 
        	try {
        			masterCategory = fetchMasterCategory();
        			if(CATEGORY_CACHE_READ)
        				RedisStoreUtil.save("masterCategories", mapper.writeValueAsString(masterCategory), CATEGORY_CACHE_TTL);
			}catch (JsonProcessingException e) {
				TelemetryManager.error("Error while parsnig master category data to list of object.", e);
                throw new ServerException("ERR_WHILE_PARSHING_DATA", "Error while parsnig master category data to list of object.", e);
			}
        return getFrameworkCategoryFieldsMap(masterCategory);
	}
	private List<Map<String, String>> fetchMasterCategory() {
		List<Map<String, String>> masterCategory = new ArrayList<Map<String, String>>();
		try {
			List<Node> categoryNodes = controllerUtil.getNodes("domain", "Category", 0, 50);
			if(CollectionUtils.isNotEmpty(categoryNodes)) {
				categoryNodes.forEach(node -> {
					if(!SystemNodeTypes.DEFINITION_NODE.name().equals(node.getNodeType())){
						masterCategory.add(new HashMap<String, String>() {{
							put("code", (String)node.getMetadata().get("code"));
							put("orgIdFieldName", (String)node.getMetadata().get("orgIdFieldName"));
							put("targetIdFieldName", (String)node.getMetadata().get("targetIdFieldName"));
							put("searchIdFieldName", (String)node.getMetadata().get("searchIdFieldName"));
							put("searchLabelFieldName", (String)node.getMetadata().get("searchLabelFieldName"));
						}});
					}
				});
				if(CollectionUtils.isEmpty(masterCategory))
					throw new ServerException("ERR_EMPTY_MASTER_CATEGORY_DATA", "Master category list can not be empty.");
			}else {
				throw new ServerException("ERR_FETCHING_MASTER_CATEGORY_DATA", "No object found with objectType: Category.");
			}
		}catch(ResourceNotFoundException e) {
			TelemetryManager.error("Error while fetching neo4j records for objectType: Category", e);
            throw new ServerException("ERR_FETCHING_CATEGORY_DATA", "Error while fetching neo4j records for objectType: Category.", e);
		}
		return masterCategory;
	}
	private Map<String, Object> getFrameworkCategoryFieldsMap(List<Map<String, String>> masterCategory) {
		Map<String, Object> frameworkCategoryFieldsMap = new HashMap<String, Object>();
		if(CollectionUtils.isNotEmpty(masterCategory))
		{
			Map<String, List<String>> searchableCategoryId = new HashMap<String, List<String>>();
			Map<String, List<String>> searchableCategoryLabel = new HashMap<String, List<String>>();
			List<String> contentFrameworkMetafields = new ArrayList<String>();
			masterCategory.forEach(category -> {
				searchableCategoryId.put(category.get("searchIdFieldName"), Arrays.asList(category.get("orgIdFieldName"), category.get("targetIdFieldName")));
				searchableCategoryLabel.put(category.get("searchLabelFieldName"), Arrays.asList(category.get("orgIdFieldName"), category.get("targetIdFieldName")));
				contentFrameworkMetafields.addAll(Arrays.asList(category.get("orgIdFieldName"), category.get("targetIdFieldName")));
			});
			
			frameworkCategoryFieldsMap.put("id", searchableCategoryId);
			frameworkCategoryFieldsMap.put("name", searchableCategoryLabel);
			frameworkCategoryFieldsMap.put("contentFrameworkMetafields", contentFrameworkMetafields);
		}
		return frameworkCategoryFieldsMap;
	}
	
	protected void revalidateFrameworkCategoryMetadata(Map<String, List<String>> frameworkMetadata, Node node) {
		frameworkCategorySearchMetadataMapping.keySet().forEach(category -> {
			Object data = node.getMetadata().get(frameworkCategorySearchMetadataMapping.get(category));
			if(null == frameworkMetadata.getOrDefault(category, null) && data != null) 
				frameworkMetadata.put(category, data instanceof String ? Arrays.asList((String)data) : Arrays.asList((String[])data));
		});
		System.out.println("**frameworkMetadata** : " + frameworkMetadata.toString());
	}
	protected List<String> mergeIds(List<String> orgList, List<String> targetList){
		Set<String> mergedList = new HashSet<String>();
		mergedList.addAll(orgList);
		mergedList.addAll(targetList);
		return CollectionUtils.isEmpty(mergedList)? null : new ArrayList<String>(mergedList);
	}
	protected Map<String, List<String>> getLabels(Map<String, Object> metadata, String identifier, List<String> contentFrameworkMetafields){
		Map<String, List<String>> frameworkMetadata = new HashMap<String, List<String>>();
		List<String> ids = new ArrayList<String>();
		String[] defaultArray = {};
		for(String id : contentFrameworkMetafields) 
			ids.addAll(Arrays.asList((String[])metadata.getOrDefault(id, defaultArray)));
		if(CollectionUtils.isEmpty(ids)) {
			TelemetryManager.info("For Content :: " + identifier + " no framework categories are set in metadata.");
			return frameworkMetadata;
		}
		Response response = controllerUtil.getDataNodes("domain", ids);
		if (response.getResponseCode() != ResponseCode.OK) {
			TelemetryManager.error("Error while fetching framework related objects:: ResponseCode:: " + response.getResponseCode() + 
					" Error Mesaage:: " + response.getParams().getErrmsg());
			return frameworkMetadata;
		}else {
			List<Node> nodes = (List<Node>) response.getResult().get(GraphDACParams.node_list.name());
			if(CollectionUtils.isEmpty(nodes)) {
				TelemetryManager.info("For Content :: " + identifier + " no framework categories object found for ids:: " + ids);
				return frameworkMetadata;
			}
			Map<String, Map<String, Object>> nodeMap = nodes.stream().collect(Collectors.toMap(Node:: getIdentifier, Node:: getMetadata));
			
			if(!nodeMap.isEmpty()) {
				for(String metaField : contentFrameworkMetafields) {
					List<String> idList = Arrays.asList((String[])metadata.getOrDefault(metaField, defaultArray));
					if(CollectionUtils.isNotEmpty(idList)) {
						List<String> labelList =  new ArrayList<String>();
						idList.stream().forEach(id -> labelList.add((String)((Map<String, Object>)nodeMap.get(id)).get("name")));
						frameworkMetadata.put(metaField, labelList);
					}
					
				}
			}
			return frameworkMetadata;
		}
	}
}
