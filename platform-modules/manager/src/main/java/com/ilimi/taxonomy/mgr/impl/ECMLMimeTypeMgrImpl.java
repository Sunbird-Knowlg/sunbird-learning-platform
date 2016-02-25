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
import org.springframework.beans.factory.annotation.Qualifier;
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
import com.ilimi.common.dto.NodeDTO;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.RequestParams;
import com.ilimi.common.dto.Response;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.router.RequestRouterPool;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.taxonomy.dto.ContentSearchCriteria;
import com.ilimi.taxonomy.enums.ContentAPIParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;
import com.ilimi.taxonomy.mgr.IMimeTypeManager;
import com.ilimi.taxonomy.util.AWSUploader;
import com.ilimi.taxonomy.util.ContentBundle;
import com.ilimi.taxonomy.util.CustomParser;
import com.ilimi.taxonomy.util.HttpDownloadUtility;
import com.ilimi.taxonomy.util.UnzipUtility;
import com.ilimi.taxonomy.util.ZipUtility;

@Component
@Qualifier("ECMLMimeTypeMgrImpl")
public class ECMLMimeTypeMgrImpl extends BaseMimeTypeManager implements IMimeTypeManager {
	
	@Autowired
	private IAssessmentManager assessmentMgr;
	@Autowired
	private ContentBundle contentBundle;

	
	private static final String bucketName = "ekstep-public";
	private static final String folderName = "content";
	private static final String tempFileLocation = "/temp/";
	private ObjectMapper mapper = new ObjectMapper();
	
	private static Logger LOGGER = LogManager.getLogger(IMimeTypeManager.class.getName());
	
	@Override
	public void upload() {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unchecked")
	@Override
	public Response extract(Node node) {
		String zipFilUrl = (String) node.getMetadata().get("downloadUrl");
		String tempFileDwn = tempFileLocation + System.currentTimeMillis() + "_temp";
		File zipFile = null;
		System.out.println("zipFilUrl is " + zipFilUrl);
		if (StringUtils.isNotBlank(zipFilUrl)) {
			zipFile = HttpDownloadUtility.downloadFile(zipFilUrl, tempFileDwn);
			System.out.println("zipFile is " + zipFile);
		}
		String zipFilePath = zipFile.getPath();
		String zipFileDir = zipFile.getParent();
		String filePath = zipFileDir + File.separator + "index.ecml";
		String uploadFilePath = zipFileDir + File.separator + "assets" + File.separator;
		Map<String, List<String>> mediaIdURL = new HashMap<String, List<String>>();
		List<String> listOfCtrlType = new ArrayList<>();
		listOfCtrlType.add("items");
		listOfCtrlType.add("data");
		String taxonomyId = node.getGraphId();
		String contentId =  node.getIdentifier();
		UnzipUtility unzipper = new UnzipUtility();
		Response response = new Response();
		try {
			unzipper.unzip(zipFilePath, zipFileDir);
			List<Relation> outRelations = new ArrayList<Relation>();
			createAssessmentItemFromContent(taxonomyId, zipFileDir, contentId, outRelations);
			Map<String, List<String>> mediaIdMap = CustomParser.readECMLFile(filePath);
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
			CustomParser customParser = new CustomParser(new File(filePath));
			if (null != mediaIdSet && !mediaIdSet.isEmpty()) {
				Request mediaReq = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER,"getDataNodes", GraphDACParams.node_ids.name(), new ArrayList<>(mediaIdSet));
				Response mediaRes = getResponse(mediaReq, LOGGER);
				List<Node> nodeList = (List<Node>) mediaRes.get(GraphDACParams.node_list.name());
				if (null != mediaIdSet && !mediaIdSet.isEmpty()) {
					for (Node nodeExt : nodeList) {
						if (mediaIdSet.contains(nodeExt.getIdentifier())) {
							mediaIdSet.remove(nodeExt.getIdentifier());
							String downloadUrl = (String) nodeExt.getMetadata().get("downloadUrl");
							if (StringUtils.isBlank(downloadUrl)) {
								long timeStempInMiliSec = System.currentTimeMillis();
								String mediaSrc = mediaSrcMap.get(mediaAssetIdMap.get(nodeExt
										.getIdentifier()));
								File olderName = new File(uploadFilePath + mediaSrc);
								if (olderName.exists() && olderName.isFile()) {
									String parentFolderName = olderName.getParent();
									File newName = new File(parentFolderName + File.separator
											+ timeStempInMiliSec + olderName.getName());
									olderName.renameTo(newName);
									String[] url = AWSUploader.uploadFile(bucketName, folderName,
											newName);
									Node newItem = createNode(nodeExt, url[1], nodeExt.getIdentifier(),
											olderName);
									List<String> values = new ArrayList<String>();
									values.add(url[1]);
									values.add(nodeExt.getIdentifier());
									mediaIdURL.put(mediaAssetIdMap.get(nodeExt.getIdentifier()),
											values);
									Request validateReq = getRequest(taxonomyId,
											GraphEngineManagers.NODE_MANAGER, "validateNode");
									validateReq.put(GraphDACParams.node.name(), newItem);
									Response validateRes = getResponse(validateReq, LOGGER);
									if (checkError(validateRes)) {
										return validateRes;
									} else {
										Request updateReq = getRequest(taxonomyId,
												GraphEngineManagers.NODE_MANAGER, "updateDataNode");
										updateReq.put(GraphDACParams.node.name(), newItem);
										updateReq.put(GraphDACParams.node_id.name(),
												newItem.getIdentifier());
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
								File newName = new File(parentFolderName + File.separator
										+ timeStempInMiliSec + olderName.getName());
								olderName.renameTo(newName);
								String[] url = AWSUploader.uploadFile(bucketName, folderName,
										newName);
								Node item = createNode(new Node(), url[1], mediaId, olderName);
								// Creating a graph.
								Request validateReq = getRequest(taxonomyId,
										GraphEngineManagers.NODE_MANAGER, "validateNode");
								validateReq.put(GraphDACParams.node.name(), item);
								Response validateRes = getResponse(validateReq, LOGGER);
								if (checkError(validateRes)) {
									return validateRes;
								} else {
									Request createReq = getRequest(taxonomyId,
											GraphEngineManagers.NODE_MANAGER, "createDataNode");
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
				customParser.updateSrcInEcml(filePath, mediaIdURL);
			}
			customParser.updateJsonInEcml(filePath, listOfCtrlType);
			response.put("ecmlBody", CustomParser.readFile(new File(filePath)));
			response.put("outRelations", outRelations);
		} catch (Exception ex) {
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
		Map<String, Object> metadata = new HashMap<String, Object>();
		metadata.put("name", mediaId);
		metadata.put("code", mediaId);
		metadata.put("body", "<content></content>");
		metadata.put("status", "Live");
		metadata.put("owner", "ekstep");
		metadata.put("contentType", "Asset");
		metadata.put("downloadUrl", url);
		if (metadata.get("pkgVersion") == null) {
			metadata.put("pkgVersion", 1);
		} else {
			Double version = (Double) metadata.get("pkgVersion") + 1;
			metadata.put("pkgVersion", version);
		}
		Object mimeType = getMimeType(new File(olderName.getName()));
		metadata.put("mimeType", mimeType);
		String mediaType = getMediaType(olderName.getName());
		metadata.put("mediaType", mediaType);
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
			} else if (fileName.endsWith(".mp4") || fileName.endsWith(".avi")
					|| fileName.endsWith(".3gpp") || fileName.endsWith(".webm")) {
				mediaType = "video";
			} else if (fileName.endsWith(".mp3") || fileName.endsWith(".ogg")
					|| fileName.endsWith(".wav")) {
				mediaType = "audio";
			} else if (fileName.endsWith(".txt") || fileName.endsWith(".json")
					|| fileName.endsWith(".xml")) {
				mediaType = "text";
			}
		}
		return mediaType;
	}

	@SuppressWarnings({ "unchecked" })
	private Map<String, Object> createAssessmentItemFromContent(String taxonomyId,
			String contentExtractedPath, String contentId, List<Relation> outRelations) {
		if (null != contentExtractedPath) {
			List<File> fileList = getControllerFileList(contentExtractedPath);
			Map<String, Object> mapResDetail = new HashMap<String, Object>();
			if (null == fileList)
				return null;
			for (File file : fileList) {
				System.out.println(file.getName());
				if (file.exists()) {
					try {
						Map<String, Object> fileJSON = new ObjectMapper().readValue(file,
								HashMap.class);
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
									Request request = getAssessmentItemRequestObject(map,
											"AssessmentItem", contentId,
											ContentAPIParams.assessment_item.name());
									if (null != request) {
										Node itemNode = (Node) request
												.get(ContentAPIParams.assessment_item.name());
										Response response = null;
										if (StringUtils.isBlank(itemNode.getIdentifier())) {
											response = assessmentMgr.createAssessmentItem(
													taxonomyId, request);
										} else {
											response = assessmentMgr.updateAssessmentItem(
													itemNode.getIdentifier(), taxonomyId, request);
										}
										LOGGER.info("Create Item | Response: " + response);
										Map<String, Object> resMap = response.getResult();
										if (null != resMap.get(ContentAPIParams.node_id.name())) {
											String identifier = (String) resMap
													.get(ContentAPIParams.node_id.name());
											mapRelation.put(identifier,
													map.get(ContentAPIParams.concepts.name()));
											lstAssessmentItemId.add(identifier);
											mapAssessItemRes.put(identifier, "Assessment Item "
													+ identifier + " Added Successfully");
										} else {
											System.out.println("Item validation failed: "
													+ resMap.get(ContentAPIParams.messages.name()));
											String id = (String) map
													.get(ContentAPIParams.identifier.name());
											if (StringUtils.isNotBlank(id))
												mapAssessItemRes.put(id, (String) resMap
														.get(ContentAPIParams.messages.name()));
										}
									}
								}
								assessResMap.put(ContentAPIParams.assessment_item.name(),
										mapAssessItemRes);
								Response itemSetRes = createItemSet(taxonomyId, contentId,
										lstAssessmentItemId, fileJSON);
								if (null != itemSetRes) {
									Map<String, Object> mapItemSetRes = itemSetRes.getResult();
									assessResMap.put(ContentAPIParams.assessment_item_set.name(),
											mapItemSetRes);
									String itemSetNodeId = (String) mapItemSetRes
											.get(ContentAPIParams.set_id.name());
									System.out.println("itemSetNodeId: " + itemSetNodeId);
									if (StringUtils.isNotBlank(itemSetNodeId)) {
										Relation outRel = new Relation(null,
												RelationTypes.ASSOCIATED_TO.relationName(),
												itemSetNodeId);
										outRelations.add(outRel);
									}
								}
								List<String> lstAssessItemRelRes = createRelation(taxonomyId,
										mapRelation, outRelations);
								assessResMap.put(ContentAPIParams.AssessmentItemRelation.name(),
										lstAssessItemRelRes);
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
				List<Map<String, Object>> lstConceptMap = (List<Map<String, Object>>) entry
						.getValue();
				if (null != lstConceptMap && !lstConceptMap.isEmpty()) {
					for (Map<String, Object> conceptMap : lstConceptMap) {
						String conceptId = (String) conceptMap.get(ContentAPIParams.identifier
								.name());
						Response response = addRelation(taxonomyId, entry.getKey(),
								RelationTypes.ASSOCIATED_TO.relationName(), conceptId);
						lstResponse.add(response.getResult().toString());
						Relation outRel = new Relation(null,
								RelationTypes.ASSOCIATED_TO.relationName(), conceptId);
						outRelations.add(outRel);
					}
				}
			}
			return lstResponse;
		}
		return null;
	}
	private List<File> getControllerFileList(String contentExtractedPath) {
		try {
			String indexFile = contentExtractedPath + File.separator + "index.ecml";
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(indexFile);
			NodeList attrList = doc.getElementsByTagName("controller");
			List<File> lstControllerFile = new ArrayList<File>();
			for (int i = 0; i < attrList.getLength(); i++) {
				Element controller = (Element) attrList.item(i);
				if (controller.getAttribute("type").equalsIgnoreCase("Items")) {
					controller = (Element) attrList.item(i);
					if (!StringUtils.isBlank(controller.getAttribute("id"))) {
						lstControllerFile.add(new File(contentExtractedPath + File.separator
								+ "items" + File.separator + controller.getAttribute("id")
								+ ".json"));
					}
				}
			}
			return lstControllerFile;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	private Response createItemSet(String taxonomyId, String contentId,
			List<String> lstAssessmentItemId, Map<String, Object> fileJSON) {
		if (null != lstAssessmentItemId && lstAssessmentItemId.size() > 0
				&& StringUtils.isNotBlank(taxonomyId)) {
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

	private Request getAssessmentItemRequestObject(Map<String, Object> map, String objectType,
			String contentId, String param) {
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

	public Response addRelation(String taxonomyId, String objectId1, String relation,
			String objectId2) {
		if (StringUtils.isBlank(taxonomyId))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(),
					"Invalid taxonomy Id");
		if (StringUtils.isBlank(objectId1) || StringUtils.isBlank(objectId2))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
					"Object Id is blank");
		if (StringUtils.isBlank(relation))
			throw new ClientException(ContentErrorCodes.ERR_INVALID_RELATION_NAME.name(),
					"Relation name is blank");
		Request request = getRequest(taxonomyId, GraphEngineManagers.GRAPH_MANAGER,
				"createRelation");
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
					RequestParams params = (RequestParams) mapper.convertValue(reqParams,
							RequestParams.class);
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
		String artifactUrl = (String)node.getMetadata().get("artifactUrl");
		String tempFolderWithTimeStamp = tempFileLocation + File.separator + System.currentTimeMillis() + "_temp";
		if (StringUtils.isNotBlank(artifactUrl)) {
			response = rePublish(node);
		}else{
			response = compress(node,tempFolderWithTimeStamp);
		}
		
		return response;
	}
	
	private Response compress(Node node, String tempFolderWithTimeStamp) {
		String fileName = System.currentTimeMillis() + "_" + node.getIdentifier();
		Map<String, Object> metadata = new HashMap<String, Object>();
		metadata = node.getMetadata();
		String contentBody = (String) metadata.get("body");
		String contentType = checkBodyContentType(contentBody);
		if (StringUtils.isBlank(contentType))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BODY_INVALID.name(),"Content of Body Either Invalid or Null");
		try {
			
			File file = null;
			if (StringUtils.equalsIgnoreCase("ecml", contentType)) {
				file = new File(tempFolderWithTimeStamp + File.separator + "index.ecml");
			} else if (StringUtils.equalsIgnoreCase("json", contentType)) {
				file = new File(tempFolderWithTimeStamp + File.separator + "index.json");
			}
			if (null != file) {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
					if (!file.exists()) {
						file.createNewFile();
					}
				}
				FileUtils.writeStringToFile(file, contentBody);
			}
			String appIcon = (String) metadata.get("appIcon");
			if (StringUtils.isNotBlank(appIcon)) {
				File logoFile = HttpDownloadUtility.downloadFile(appIcon, tempFolderWithTimeStamp);
				try {
					if (null != logoFile && logoFile.exists() && logoFile.isFile()) {
						String parentFolderName = logoFile.getParent();
						File newName = new File(parentFolderName + File.separator + "logo.png");
						logoFile.renameTo(newName);
					}
				} catch (Exception ex) {
					throw new ServerException(ContentErrorCodes.ERR_CONTENT_PUBLISH.name(),
							ex.getMessage());
				}
			}
		}catch(IOException e){
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_PUBLISH.name(), e.getMessage());
		}
		String filePath = tempFolderWithTimeStamp;
		String taxonomyId = node.getGraphId();
		String contentId = node.getIdentifier();
		File file = new File(filePath);
		String fileLocation = filePath + File.separator + "index.ecml";
		String sourceFolder = file.getParent() + File.separator;
		Response response = new Response();
		try {
			if (contentType.equalsIgnoreCase("json")) {
				CustomParser.readJsonFileDownload(filePath);
			} else if (contentType.equalsIgnoreCase("ecml")) {
				new CustomParser(new File(fileLocation)).updateEcml(filePath);
			}
			String zipFilePathName = sourceFolder + fileName + ".zip";
			List<String> fileList = new ArrayList<String>();
			ZipUtility appZip = new ZipUtility(fileList, zipFilePathName, filePath);
			appZip.generateFileList(new File(filePath));
			appZip.zipIt(zipFilePathName);
			File olderName = new File(zipFilePathName);
			if (olderName.exists() && olderName.isFile()) {
				File newName = new File(sourceFolder + File.separator + olderName.getName());
				olderName.renameTo(newName);
				Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER,
						"getDataNode", GraphDACParams.node_id.name(), contentId);
				request.put(GraphDACParams.get_tags.name(), true);
				Response getNodeRes = getResponse(request, LOGGER);
				if (checkError(getNodeRes)) {
					return getNodeRes;
				}
				Node nodePublish = (Node) getNodeRes.get(GraphDACParams.node.name());
				nodePublish.getMetadata().put("downloadUrl", newName);
				List<Node> nodes = new ArrayList<Node>();
				nodes.add(nodePublish);
				List<Map<String, Object>> ctnts = new ArrayList<Map<String, Object>>();
				List<String> childrenIds = new ArrayList<String>();
				getContentBundleData(taxonomyId, nodes, ctnts, childrenIds);
				String bundleFileName = contentId + "_" + System.currentTimeMillis() + ".ecar";
				String[] urlArray = contentBundle.createContentBundle(ctnts, childrenIds,
						bundleFileName, "1.1");
				nodePublish.getMetadata().put("s3Key", urlArray[0]);
				nodePublish.getMetadata().put("downloadUrl", urlArray[1]);
				Number pkgVersion = (Number) nodePublish.getMetadata().get("pkgVersion");
				if (null == pkgVersion || pkgVersion.intValue() < 1) {
					pkgVersion = 1.0;
				} else {
					pkgVersion = pkgVersion.doubleValue() + 1;
				}
				nodePublish.getMetadata().put("pkgVersion", pkgVersion);
				nodePublish.getMetadata().put("status", "Live");
				Request updateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER,
						"updateDataNode");
				updateReq.put(GraphDACParams.node.name(), nodePublish);
				updateReq.put(GraphDACParams.node_id.name(), nodePublish.getIdentifier());
				Response updateRes = getResponse(updateReq, LOGGER);
				updateRes.put(ContentAPIParams.content_url.name(), urlArray[1]);
				response = updateRes;
			}

		} catch (Exception e) {
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_PUBLISH.name(), e.getMessage());
		} finally {
			File directory = new File(sourceFolder);
			if (!directory.exists()) {
				System.out.println("Directory does not exist.");
				System.exit(0);
			} else {
				try {
					delete(directory);
					if (!directory.exists()) {
						directory.mkdirs();
					}
				} catch (IOException e) {
					throw new ServerException(ContentErrorCodes.ERR_CONTENT_PUBLISH.name(),	e.getMessage());
				}
			}
		}
		return response;
	}

	private Response rePublish(Node node) {
		Response response = new Response();
		String tempFolderWithTimeStamp = tempFileLocation + File.separator + System.currentTimeMillis() + "_temp";
		File ecarFile = HttpDownloadUtility.downloadFile((String)node.getMetadata().get("artifactUrl"), tempFolderWithTimeStamp);
		try {
			UnzipUtility unzip = new UnzipUtility();
			unzip.unzip(ecarFile.getPath(), ecarFile.getParent());
			File olderZipFile = CustomParser.getZipFile(ecarFile,node);
			File newName = new File(ecarFile.getParent() + File.separator + olderZipFile.getName());
			node.getMetadata().put("downloadUrl", newName);
			List<Node> nodes = new ArrayList<Node>();
			nodes.add(node);
			List<Map<String, Object>> ctnts = new ArrayList<Map<String, Object>>();
			List<String> childrenIds = new ArrayList<String>();
			getContentBundleData(node.getGraphId(), nodes, ctnts, childrenIds);
			String bundleFileName = node.getIdentifier() + "_" + System.currentTimeMillis() + ".ecar";
			String[] urlArray = contentBundle.createContentBundle(ctnts, childrenIds,
					bundleFileName, "1.1");
			node.getMetadata().put("s3Key", urlArray[0]);
			node.getMetadata().put("downloadUrl", urlArray[1]);
			node.getMetadata().put("artifactUrl", urlArray[1]);
			Number pkgVersion = (Number) node.getMetadata().get("pkgVersion");
			if (null == pkgVersion || pkgVersion.intValue() < 1) {
				pkgVersion = 1.0;
			} else {
				pkgVersion = pkgVersion.doubleValue() + 1;
			}
			node.getMetadata().put("pkgVersion", pkgVersion);
			node.getMetadata().put("status", "Live");
			Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER,
					"updateDataNode");
			updateReq.put(GraphDACParams.node.name(), node);
			updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
			Response updateRes = getResponse(updateReq, LOGGER);
			updateRes.put(ContentAPIParams.content_url.name(), urlArray[1]);
			response = updateRes;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	private void getContentBundleData(String taxonomyId, List<Node> nodes,
			List<Map<String, Object>> ctnts, List<String> childrenIds) {
		Map<String, Node> nodeMap = new HashMap<String, Node>();
		if (null != nodes && !nodes.isEmpty()) {
			for (Node node : nodes) {
				nodeMap.put(node.getIdentifier(), node);
				Map<String, Object> metadata = new HashMap<String, Object>();
				if (null == node.getMetadata())
					node.setMetadata(new HashMap<String, Object>());
				metadata.putAll(node.getMetadata());
				metadata.put("identifier", node.getIdentifier());
				metadata.put("objectType", node.getObjectType());
				metadata.put("subject", node.getGraphId());
				metadata.remove("body");
				metadata.remove("editorState");
				if (null != node.getTags() && !node.getTags().isEmpty())
					metadata.put("tags", node.getTags());
				if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
					List<NodeDTO> children = new ArrayList<NodeDTO>();
					for (Relation rel : node.getOutRelations()) {
						if (StringUtils.equalsIgnoreCase(
								RelationTypes.SEQUENCE_MEMBERSHIP.relationName(),
								rel.getRelationType())
								&& StringUtils.equalsIgnoreCase(node.getObjectType(),
										rel.getEndNodeObjectType())) {
							childrenIds.add(rel.getEndNodeId());
							children.add(new NodeDTO(rel.getEndNodeId(), rel.getEndNodeName(), rel
									.getEndNodeObjectType(), rel.getRelationType(), rel
									.getMetadata()));
						}
					}
					if (!children.isEmpty()) {
						metadata.put("children", children);
					}
				}
				ctnts.add(metadata);
			}
			List<String> searchIds = new ArrayList<String>();
			for (String nodeId : childrenIds) {
				if (!nodeMap.containsKey(nodeId)) {
					searchIds.add(nodeId);
				}
			}
			if (!searchIds.isEmpty()) {
				Response searchRes = searchNodes(taxonomyId, searchIds);
				if (checkError(searchRes)) {
					throw new ServerException(ContentErrorCodes.ERR_CONTENT_SEARCH_ERROR.name(),
							getErrorMessage(searchRes));
				} else {
					List<Object> list = (List<Object>) searchRes.get(ContentAPIParams.contents
							.name());
					if (null != list && !list.isEmpty()) {
						for (Object obj : list) {
							List<Node> nodeList = (List<Node>) obj;
							for (Node node : nodeList) {
								nodeMap.put(node.getIdentifier(), node);
								Map<String, Object> metadata = new HashMap<String, Object>();
								if (null == node.getMetadata())
									node.setMetadata(new HashMap<String, Object>());
								metadata.putAll(node.getMetadata());
								metadata.put("identifier", node.getIdentifier());
								metadata.put("objectType", node.getObjectType());
								metadata.put("subject", node.getGraphId());
								metadata.remove("body");
								if (null != node.getTags() && !node.getTags().isEmpty())
									metadata.put("tags", node.getTags());
								ctnts.add(metadata);
							}
						}
					}
				}
			}
		}
	}
	
	private Response searchNodes(String taxonomyId, List<String> contentIds) {
		ContentSearchCriteria criteria = new ContentSearchCriteria();
		List<Filter> filters = new ArrayList<Filter>();
		Filter filter = new Filter("identifier", SearchConditions.OP_IN, contentIds);
		filters.add(filter);
		MetadataCriterion metadata = MetadataCriterion.create(filters);
		metadata.addFilter(filter);
		criteria.setMetadata(metadata);
		List<Request> requests = new ArrayList<Request>();
		if (StringUtils.isNotBlank(taxonomyId)) {
			Request req = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
					GraphDACParams.search_criteria.name(), criteria.getSearchCriteria());
			req.put(GraphDACParams.get_tags.name(), true);
			requests.add(req);
		} else {
			for (String tId : TaxonomyManagerImpl.taxonomyIds) {
				Request req = getRequest(tId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
						GraphDACParams.search_criteria.name(), criteria.getSearchCriteria());
				req.put(GraphDACParams.get_tags.name(), true);
				requests.add(req);
			}
		}
		Response response = getResponse(requests, LOGGER, GraphDACParams.node_list.name(),
				ContentAPIParams.contents.name());
		return response;
	}


	
	private String checkBodyContentType(String contentBody) {
		if (StringUtils.isNotEmpty(contentBody)) {
			if (isECMLValid(contentBody)) {
				return "ecml";
			} else if (isJSONValid(contentBody)) {
				return "json";
			}
		}
		return null;
	}

	@Override
	public void bundle() {
		// TODO Auto-generated method stub

	}

}
