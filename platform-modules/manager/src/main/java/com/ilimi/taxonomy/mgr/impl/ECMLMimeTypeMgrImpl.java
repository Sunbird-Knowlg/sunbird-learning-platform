package com.ilimi.taxonomy.mgr.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.activation.MimetypesFileTypeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import scala.concurrent.Await;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.pattern.Patterns;

import com.ilimi.assessment.enums.QuestionnaireType;
import com.ilimi.assessment.mgr.IAssessmentManager;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.RequestParams;
import com.ilimi.common.dto.Response;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.router.RequestRouterPool;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.taxonomy.enums.ContentAPIParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;
import com.ilimi.taxonomy.mgr.IMimeTypeManager;
import com.ilimi.taxonomy.util.AWSUploader;
import com.ilimi.taxonomy.util.CustomParser;
import com.ilimi.taxonomy.util.HttpDownloadUtility;
import com.ilimi.taxonomy.util.UnzipUtility;

@Component
public class ECMLMimeTypeMgrImpl extends BaseMimeTypeManager implements IMimeTypeManager {

	@Autowired
	private IAssessmentManager assessmentMgr;

	private static final String bucketName = "ekstep-public";
	private static final String folderName = "content";

	private static final String tempFileLocation = "/data/contentBundle/";

	private ObjectMapper mapper = new ObjectMapper();

	private static Logger LOGGER = LogManager.getLogger(IMimeTypeManager.class.getName());

	@SuppressWarnings("unchecked")
	@Override
	public Response extract(Node node) {
		String zipFilUrl = (String) node.getMetadata().get(ContentAPIParams.artifactUrl.name());
		String tempFileDwn = tempFileLocation + System.currentTimeMillis() + "_temp";
		File zipFile = null;
		if (StringUtils.isBlank(zipFilUrl)) {
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_EXTRACT.name(), "artifactUrl is not set");
		}
		zipFile = HttpDownloadUtility.downloadFile(zipFilUrl, tempFileDwn);
		boolean isJSONIndex;
		String zipFilePath = zipFile.getPath();
		String zipFileDir = zipFile.getParent();
		String filePath = zipFileDir + File.separator + "index.ecml";
		String jsonFilePath = zipFileDir + File.separator + "index.json";
		String uploadFilePath = zipFileDir + File.separator + "assets" + File.separator;
		Map<String, List<String>> mediaIdURL = new HashMap<String, List<String>>();
		String taxonomyId = node.getGraphId();
		String contentId = node.getIdentifier();
		UnzipUtility unzipper = new UnzipUtility();
		Response response = new Response();
		try {
			unzipper.unzip(zipFilePath, zipFileDir);
			File jsonFile = new File(jsonFilePath);
			File ecmlFile = new File(filePath);
			if (jsonFile.exists()) {
				isJSONIndex = true;
			} else if (ecmlFile.exists()) {
				isJSONIndex = false;
			} else {
				return ERROR("Failed", "'index' file does not exist.", ResponseCode.CLIENT_ERROR);
			}
			List<Relation> outRelations = new ArrayList<Relation>();
			createAssessmentItemFromContent(taxonomyId, zipFileDir, contentId, outRelations, isJSONIndex);
			Map<String, List<String>> mediaIdMap = new HashMap<String, List<String>>();
			if (isJSONIndex == false) {
				mediaIdMap = CustomParser.readECMLFile(filePath);
			} else {
				mediaIdMap = CustomParser.readJSONFile(jsonFilePath);
			}
			Map<String, String> mediaSrcMap = new HashMap<String, String>();
			Map<String, String> mediaAssetIdMap = new HashMap<String, String>();
			Set<String> mediaIdSet = new HashSet<String>();
			for (Entry<String, List<String>> entry : mediaIdMap.entrySet()) {
				String id = entry.getKey();
				List<String> values = entry.getValue();
				if (null != values && values.size() == 2) {
					String src = values.get(0);
					String assetId = values.get(1);
					String nodeId = null;
					if (StringUtils.isNotBlank(assetId)) {
						nodeId = assetId;
					} else {
						nodeId = contentId + "_" + id;
					}
					mediaIdSet.add(nodeId);
					mediaSrcMap.put(id, src);
					mediaAssetIdMap.put(nodeId, id);
				}
			}
			CustomParser customParser = new CustomParser();
			if (isJSONIndex == false)
				customParser.init(new File(filePath));
			if (null != mediaIdSet && !mediaIdSet.isEmpty()) {
				Request mediaReq = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNodes",
						GraphDACParams.node_ids.name(), new ArrayList<>(mediaIdSet));
				Response mediaRes = getResponse(mediaReq, LOGGER);
				List<Node> nodeList = (List<Node>) mediaRes.get(GraphDACParams.node_list.name());
				if (null != mediaIdSet && !mediaIdSet.isEmpty()) {
					for (Node nodeExt : nodeList) {
						if (mediaIdSet.contains(nodeExt.getIdentifier())) {
							mediaIdSet.remove(nodeExt.getIdentifier());
							String downloadUrl = (String) nodeExt.getMetadata()
									.get(ContentAPIParams.downloadUrl.name());
							if (StringUtils.isBlank(downloadUrl)) {
								long timeStempInMiliSec = System.currentTimeMillis();
								String mediaSrc = mediaSrcMap.get(mediaAssetIdMap.get(nodeExt.getIdentifier()));
								File olderName = new File(uploadFilePath + mediaSrc);
								if (olderName.exists() && olderName.isFile()) {
									String parentFolderName = olderName.getParent();
									File newName = new File(parentFolderName + File.separator + timeStempInMiliSec
											+ olderName.getName());
									FileUtils.copyFile(olderName, newName);
									String[] url = AWSUploader.uploadFile(bucketName, folderName, newName);
									Node newItem = createNode(nodeExt, url[1], nodeExt.getIdentifier(), olderName);
									List<String> values = new ArrayList<String>();
									values.add(url[1]);
									values.add(nodeExt.getIdentifier());
									mediaIdURL.put(mediaAssetIdMap.get(nodeExt.getIdentifier()), values);
									Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER,
											"validateNode");
									validateReq.put(GraphDACParams.node.name(), newItem);
									Response validateRes = getResponse(validateReq, LOGGER);
									if (checkError(validateRes)) {
										return validateRes;
									} else {
										Request updateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER,
												"updateDataNode");
										updateReq.put(GraphDACParams.node.name(), newItem);
										updateReq.put(GraphDACParams.node_id.name(), newItem.getIdentifier());
										getResponse(updateReq, LOGGER);
									}
								}
							} else {
								List<String> values = new ArrayList<String>();
								values.add(downloadUrl);
								values.add(nodeExt.getIdentifier());
								mediaIdURL.put(mediaAssetIdMap.get(nodeExt.getIdentifier()), values);
							}
						}
					}
					if (mediaIdSet != null && !mediaIdSet.isEmpty()) {
						for (String mediaId : mediaIdSet) {
							long timeStempInMiliSec = System.currentTimeMillis();
							String mediaSrc = mediaSrcMap.get(mediaAssetIdMap.get(mediaId));
							File olderName = new File(uploadFilePath + mediaSrc);
							if (olderName.exists() && olderName.isFile()) {
								String parentFolderName = olderName.getParent();
								File newName = new File(
										parentFolderName + File.separator + timeStempInMiliSec + olderName.getName());
								FileUtils.copyFile(olderName, newName);
								String[] url = AWSUploader.uploadFile(bucketName, folderName, newName);
								Node item = createNode(new Node(), url[1], mediaId, olderName);
								// Creating a graph.
								Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER,
										"validateNode");
								validateReq.put(GraphDACParams.node.name(), item);
								Response validateRes = getResponse(validateReq, LOGGER);
								if (checkError(validateRes)) {
									return validateRes;
								} else {
									Request createReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER,
											"createDataNode");
									createReq.put(GraphDACParams.node.name(), item);
									getResponse(createReq, LOGGER);
									List<String> values = new ArrayList<String>();
									values.add(url[1]);
									values.add(item.getIdentifier());
									mediaIdURL.put(mediaAssetIdMap.get(mediaId), values);
								}
							}
						}
					}
				}
				if (isJSONIndex == false) {
					customParser.updateSrcInEcml(filePath, mediaIdURL);
				} else {
					customParser.updateSrcInJSON(jsonFilePath, mediaIdURL);
				}
			}
			List<String> listOfCtrlType = new ArrayList<>();
			listOfCtrlType.add("items");
			listOfCtrlType.add("data");
			if (isJSONIndex == false) {
				customParser.updateJsonInEcml(filePath, listOfCtrlType);
				response.put("ecmlBody", CustomParser.readFile(new File(filePath)));
			} else {
				customParser.updateJsonInIndexJSON(jsonFilePath, listOfCtrlType);
				response.put("ecmlBody", CustomParser.readFile(new File(jsonFilePath)));
			}
			response.put(ContentAPIParams.outRelations.name(), outRelations);
		} catch (Exception ex) {
		    ex.printStackTrace();
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_EXTRACT.name(), ex.getMessage());
		} finally {
			File directory = new File(zipFileDir);
			if (!directory.exists()) {
				System.out.println("Directory does not exist.");
				System.exit(0);
			} else {
				try {
					delete(directory);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return response;

	}

	private Node createNode(Node item, String url, String mediaId, File olderName) {
		item.setIdentifier(mediaId);
		item.setObjectType("Content");
		Map<String, Object> metadata;
		if (null == item.getMetadata()) {
			metadata = new HashMap<>();
		} else {
			metadata = item.getMetadata();
		}
		metadata.put(ContentAPIParams.name.name(), mediaId);
		metadata.put(ContentAPIParams.code.name(), mediaId);
		metadata.put(ContentAPIParams.body.name(), "<content></content>");
		metadata.put(ContentAPIParams.status.name(), "Live");
		metadata.put(ContentAPIParams.owner.name(), "ekstep");
		metadata.put(ContentAPIParams.contentType.name(), "Asset");
		metadata.put(ContentAPIParams.downloadUrl.name(), url);
		if (metadata.get(ContentAPIParams.pkgVersion.name()) == null) {
			metadata.put(ContentAPIParams.pkgVersion.name(), 1);
		} else {
			Double version = (Double) metadata.get(ContentAPIParams.pkgVersion.name()) + 1;
			metadata.put(ContentAPIParams.pkgVersion.name(), version);
		}
		Object mimeType = getMimeType(new File(olderName.getName()));
		metadata.put(ContentAPIParams.mimeType.name(), mimeType);
		String mediaType = getMediaType(olderName.getName());
		metadata.put(ContentAPIParams.mediaType.name(), mediaType);
		item.setMetadata(metadata);
		return item;
	}

	private Object getMimeType(File file) {
		MimetypesFileTypeMap mimeType = new MimetypesFileTypeMap();
		return mimeType.getContentType(file);
	}

	private String getMediaType(String fileName) {
		String mediaType = "image";
		if (StringUtils.isNotBlank(fileName)) {
			if (fileName.endsWith(".pdf")) {
				mediaType = "pdf";
			} else if (fileName.endsWith(".mp4") || fileName.endsWith(".avi") || fileName.endsWith(".3gpp")
					|| fileName.endsWith(".webm")) {
				mediaType = "video";
			} else if (fileName.endsWith(".mp3") || fileName.endsWith(".ogg") || fileName.endsWith(".wav")) {
				mediaType = "audio";
			} else if (fileName.endsWith(".txt") || fileName.endsWith(".json") || fileName.endsWith(".xml")) {
				mediaType = "text";
			}
		}
		return mediaType;
	}

	@SuppressWarnings({ "unchecked" })
	private Map<String, Object> createAssessmentItemFromContent(String taxonomyId, String contentExtractedPath,
			String contentId, List<Relation> outRelations, boolean isJSONIndex) {
		if (null != contentExtractedPath) {
			List<File> fileList = getControllerFileList(contentExtractedPath, isJSONIndex);
			Map<String, Object> mapResDetail = new HashMap<String, Object>();
			if (null == fileList)
				return null;
			for (File file : fileList) {
				System.out.println(file.getName());
				if (file.exists()) {
					try {
						Map<String, Object> fileJSON = new ObjectMapper().readValue(file, HashMap.class);
						if (null != fileJSON) {
							Map<String, Object> itemSet = (Map<String, Object>) fileJSON
									.get(ContentAPIParams.items.name());
							List<Object> lstAssessmentItem = new ArrayList<Object>();
							for (Entry<String, Object> entry : itemSet.entrySet()) {
								Object assessmentItem = (Object) entry.getValue();
								lstAssessmentItem.add(assessmentItem);
								List<Map<String, Object>> lstMap = (List<Map<String, Object>>) assessmentItem;
								List<String> lstAssessmentItemId = new ArrayList<String>();
								Map<String, Object> assessResMap = new HashMap<String, Object>();
								Map<String, String> mapAssessItemRes = new HashMap<String, String>();
								Map<String, Object> mapRelation = new HashMap<String, Object>();
								for (Map<String, Object> map : lstMap) {
									Request request = getAssessmentItemRequestObject(map, "AssessmentItem", contentId,
											ContentAPIParams.assessment_item.name());
									if (null != request) {
										Node itemNode = (Node) request.get(ContentAPIParams.assessment_item.name());
										Response response = null;
										if (StringUtils.isBlank(itemNode.getIdentifier())) {
											response = assessmentMgr.createAssessmentItem(taxonomyId, request);
										} else {
											response = assessmentMgr.updateAssessmentItem(itemNode.getIdentifier(),
													taxonomyId, request);
										}
										LOGGER.info("Create Item | Response: " + response);
										Map<String, Object> resMap = response.getResult();
										if (null != resMap.get(ContentAPIParams.node_id.name())) {
											String identifier = (String) resMap.get(ContentAPIParams.node_id.name());
											mapRelation.put(identifier, map.get(ContentAPIParams.concepts.name()));
											lstAssessmentItemId.add(identifier);
											mapAssessItemRes.put(identifier,
													"Assessment Item " + identifier + " Added Successfully");
										} else {
											System.out.println("Item validation failed: "
													+ resMap.get(ContentAPIParams.messages.name()));
											String id = (String) map.get(ContentAPIParams.identifier.name());
											if (StringUtils.isNotBlank(id))
												mapAssessItemRes.put(id,
														(String) resMap.get(ContentAPIParams.messages.name()));
										}
									}
								}
								assessResMap.put(ContentAPIParams.assessment_item.name(), mapAssessItemRes);
								Response itemSetRes = createItemSet(taxonomyId, contentId, lstAssessmentItemId,
										fileJSON);
								if (null != itemSetRes) {
									Map<String, Object> mapItemSetRes = itemSetRes.getResult();
									assessResMap.put(ContentAPIParams.assessment_item_set.name(), mapItemSetRes);
									String itemSetNodeId = (String) mapItemSetRes.get(ContentAPIParams.set_id.name());
									System.out.println("itemSetNodeId: " + itemSetNodeId);
									if (StringUtils.isNotBlank(itemSetNodeId)) {
										Relation outRel = new Relation(null, RelationTypes.ASSOCIATED_TO.relationName(),
												itemSetNodeId);
										outRelations.add(outRel);
									}
								}
								List<String> lstAssessItemRelRes = createRelation(taxonomyId, mapRelation,
										outRelations);
								assessResMap.put(ContentAPIParams.AssessmentItemRelation.name(), lstAssessItemRelRes);
								mapResDetail.put(file.getName(), assessResMap);
							}
						} else {
							mapResDetail.put(file.getName(), "Error : Invalid JSON");
						}
					} catch (Exception e) {
						mapResDetail.put(file.getName(), "Error : Unable to Parse JSON");
						e.printStackTrace();
					}
				} else {
					mapResDetail.put(file.getName(), "Error: File doesn't Exist");
				}
			}
			return mapResDetail;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private List<String> createRelation(String taxonomyId, Map<String, Object> mapRelation,
			List<Relation> outRelations) {
		if (null != mapRelation && !mapRelation.isEmpty()) {
			List<String> lstResponse = new ArrayList<String>();
			for (Entry<String, Object> entry : mapRelation.entrySet()) {
				List<Map<String, Object>> lstConceptMap = (List<Map<String, Object>>) entry.getValue();
				if (null != lstConceptMap && !lstConceptMap.isEmpty()) {
					for (Map<String, Object> conceptMap : lstConceptMap) {
						String conceptId = (String) conceptMap.get(ContentAPIParams.identifier.name());
						Response response = addRelation(taxonomyId, entry.getKey(),
								RelationTypes.ASSOCIATED_TO.relationName(), conceptId);
						lstResponse.add(response.getResult().toString());
						Relation outRel = new Relation(null, RelationTypes.ASSOCIATED_TO.relationName(), conceptId);
						outRelations.add(outRel);
					}
				}
			}
			return lstResponse;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private List<File> getControllerFileList(String contentExtractedPath, boolean isJSONIndex) {
		List<File> lstControllerFile = new ArrayList<File>();
		try {
			String indexFile = "";
			if (isJSONIndex == true) {
				indexFile = contentExtractedPath + File.separator + "index.json";
				Map<String, Object> jsonMap = CustomParser.getJsonMap(indexFile);
				Map<String, Object> jsonThemeMap = (Map<String, Object>) jsonMap.get("theme");
				if (null != jsonThemeMap) {
					List<Map<String, Object>> controllers = (List<Map<String, Object>>) jsonThemeMap.get("controller");
					if (null != controllers) {
						for (Map<String, Object> controller : controllers) {
							if (StringUtils.equalsIgnoreCase("Items", (String) controller.get("type"))) {
								if (!StringUtils.isBlank((String) controller.get("id")))
									lstControllerFile.add(new File(contentExtractedPath + File.separator + "items"
											+ File.separator + controller.get("id") + ".json"));
							}
						}
					}
				}
			} else if (isJSONIndex == false) {
				indexFile = contentExtractedPath + File.separator + "index.ecml";
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(indexFile);
				NodeList attrList = doc.getElementsByTagName("controller");
				for (int i = 0; i < attrList.getLength(); i++) {
					Element controller = (Element) attrList.item(i);
					if (controller.getAttribute("type").equalsIgnoreCase("Items")) {
						controller = (Element) attrList.item(i);
						if (!StringUtils.isBlank(controller.getAttribute("id"))) {
							lstControllerFile.add(new File(contentExtractedPath + File.separator + "items"
									+ File.separator + controller.getAttribute("id") + ".json"));
						}
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return lstControllerFile;
	}

	private Response createItemSet(String taxonomyId, String contentId, List<String> lstAssessmentItemId,
			Map<String, Object> fileJSON) {
		if (null != lstAssessmentItemId && lstAssessmentItemId.size() > 0 && StringUtils.isNotBlank(taxonomyId)) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(ContentAPIParams.memberIds.name(), lstAssessmentItemId);
			Integer totalItems = (Integer) fileJSON.get("total_items");
			if (null == totalItems || totalItems > lstAssessmentItemId.size())
				totalItems = lstAssessmentItemId.size();
			map.put("total_items", totalItems);
			Integer maxScore = (Integer) fileJSON.get("max_score");
			if (null == maxScore)
				maxScore = totalItems;
			map.put("max_score", maxScore);
			String title = (String) fileJSON.get("title");
			if (StringUtils.isNotBlank(title))
				map.put("title", title);
			map.put("type", QuestionnaireType.materialised.name());
			String identifier = (String) fileJSON.get("identifier");
			if (StringUtils.isNotBlank(identifier)) {
				map.put("code", identifier);
			} else {
				map.put("code", "item_set_" + RandomUtils.nextInt(1, 10000));
			}
			Request request = getAssessmentItemRequestObject(map, "ItemSet", contentId,
					ContentAPIParams.assessment_item_set.name());
			if (null != request) {
				Response response = assessmentMgr.createItemSet(taxonomyId, request);
				LOGGER.info("Create Item | Response: " + response);
				return response;
			}
		}
		return null;
	}

	private String[] qlevels = new String[] { "EASY", "MEDIUM", "DIFFICULT", "RARE" };
	private List<String> qlevelList = Arrays.asList(qlevels);

	private Request getAssessmentItemRequestObject(Map<String, Object> map, String objectType, String contentId,
			String param) {
		if (null != objectType && null != map) {
			Map<String, Object> reqMap = new HashMap<String, Object>();
			Map<String, Object> assessMap = new HashMap<String, Object>();
			Map<String, Object> requestMap = new HashMap<String, Object>();
			reqMap.put(ContentAPIParams.objectType.name(), objectType);
			reqMap.put(ContentAPIParams.metadata.name(), map);
			String identifier = null;
			if (null != map.get("qid")) {
				String qid = (String) map.get("qid");
				if (StringUtils.isNotBlank(qid))
					identifier = qid;
			}
			if (StringUtils.isBlank(identifier)) {
				if (null != map.get(ContentAPIParams.identifier.name())) {
					String id = (String) map.get(ContentAPIParams.identifier.name());
					if (StringUtils.isNotBlank(id))
						identifier = id;
				}
			}
			if (StringUtils.isNotBlank(identifier)) {
				reqMap.put(ContentAPIParams.identifier.name(), identifier);
				map.put("code", identifier);
				map.put("name", identifier);
			} else {
				map.put("name", "Assessment Item");
				map.put("code", "item_" + RandomUtils.nextInt(1, 10000));
			}
			map.put("usedIn", contentId);
			String qlevel = (String) map.get("qlevel");
			if (StringUtils.isBlank(qlevel)) {
				qlevel = "MEDIUM";
			} else {
				if (!qlevelList.contains(qlevel))
					qlevel = "MEDIUM";
			}
			map.put("qlevel", qlevel);
			assessMap.put(param, reqMap);
			requestMap.put(ContentAPIParams.skipValidations.name(), true);
			requestMap.put(ContentAPIParams.request.name(), assessMap);
			return getRequestObjectForAssessmentMgr(requestMap, param);
		}
		return null;
	}

	private Request getRequestObjectForAssessmentMgr(Map<String, Object> requestMap, String param) {
		Request request = getRequest(requestMap);
		Map<String, Object> map = request.getRequest();
		if (null != map && !map.isEmpty()) {
			try {
				Object obj = map.get(param);
				if (null != obj) {
					Node item = (Node) mapper.convertValue(obj, Node.class);
					request.put(param, item);
					request.put(ContentAPIParams.skipValidations.name(), true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return request;
	}

	public Response addRelation(String taxonomyId, String objectId1, String relation, String objectId2) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Invalid taxonomy Id");
		if (StringUtils.isBlank(objectId1) || StringUtils.isBlank(objectId2))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(), "Object Id is blank");
		if (StringUtils.isBlank(relation))
			throw new ClientException(ContentErrorCodes.ERR_INVALID_RELATION_NAME.name(), "Relation name is blank");
		Request request = getRequest(taxonomyId, GraphEngineManagers.GRAPH_MANAGER, "createRelation");
		request.put(GraphDACParams.start_node_id.name(), objectId1);
		request.put(GraphDACParams.relation_type.name(), relation);
		request.put(GraphDACParams.end_node_id.name(), objectId2);
		Response response = getResponse(request, LOGGER);
		return response;
	}

	@SuppressWarnings("unchecked")
	protected Request getRequest(Map<String, Object> requestMap) {
		Request request = new Request();
		if (null != requestMap && !requestMap.isEmpty()) {
			String id = (String) requestMap.get("id");
			String ver = (String) requestMap.get("ver");
			String ts = (String) requestMap.get("ts");
			request.setId(id);
			request.setVer(ver);
			request.setTs(ts);
			Object reqParams = requestMap.get("params");
			if (null != reqParams) {
				try {
					RequestParams params = (RequestParams) mapper.convertValue(reqParams, RequestParams.class);
					request.setParams(params);
				} catch (Exception e) {
				}
			}
			Object requestObj = requestMap.get("request");
			if (null != requestObj) {
				try {
					String strRequest = mapper.writeValueAsString(requestObj);
					Map<String, Object> map = mapper.readValue(strRequest, Map.class);
					if (null != map && !map.isEmpty())
						request.setRequest(map);
				} catch (Exception e) {
				}
			}
		}
		return request;
	}

	protected Response getResponse(List<Request> requests, Logger logger, String paramName, String returnParam) {
		if (null != requests && !requests.isEmpty()) {
			ActorRef router = RequestRouterPool.getRequestRouter();
			try {
				List<Future<Object>> futures = new ArrayList<Future<Object>>();
				for (Request request : requests) {
					Future<Object> future = Patterns.ask(router, request, RequestRouterPool.REQ_TIMEOUT);
					futures.add(future);
				}
				Future<Iterable<Object>> objects = Futures.sequence(futures,
						RequestRouterPool.getActorSystem().dispatcher());
				Iterable<Object> responses = Await.result(objects, RequestRouterPool.WAIT_TIMEOUT.duration());
				if (null != responses) {
					List<Object> list = new ArrayList<Object>();
					Response response = new Response();
					for (Object obj : responses) {
						if (obj instanceof Response) {
							Response res = (Response) obj;
							if (!checkError(res)) {
								Object vo = res.get(paramName);
								response = copyResponse(response, res);
								if (null != vo) {
									list.add(vo);
								}
							} else {
								return res;
							}
						} else {
							return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error",
									ResponseCode.SERVER_ERROR);
						}
					}
					response.put(returnParam, list);
					return response;
				} else {
					return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
			}
		} else {
			return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
		}
	}

	@Override
	public Response publish(Node node) {
		Response response = new Response();
		String artifactUrl = (String) node.getMetadata().get(ContentAPIParams.artifactUrl.name());
		if (StringUtils.isNotBlank(artifactUrl)) {
			response = rePublish(node);
		} else {
			response = compress(node);
		}
		return response;
	}

	@Override
	public Node tuneInputForBundling(Node node) {
		if (null == node.getMetadata().get(ContentAPIParams.artifactUrl.name())
				|| StringUtils.isBlank(node.getMetadata().get(ContentAPIParams.artifactUrl.name()).toString()))
			node = (Node) Ncompress(node, true).get(ContentAPIParams.updated_node.name());
		return node;
	}

	@Override
	public Response upload(Node node, File uploadedFile, String folder) {
		Response response = uploadContent(node, uploadedFile, folder);
		if (checkError(response))
			throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
					"Content not found with node id: " + node.getIdentifier());
		response = extract(node);
		if (null != response.get("ecmlBody") && StringUtils.isNotBlank(response.get("ecmlBody").toString())) {
			node.getMetadata().put(ContentAPIParams.body.name(), response.get("ecmlBody"));
			return updateNode(node);
		} else {
			return response;
		}
	}

}
