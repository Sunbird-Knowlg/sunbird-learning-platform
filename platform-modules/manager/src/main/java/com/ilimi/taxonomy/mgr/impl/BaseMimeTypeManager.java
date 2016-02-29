package com.ilimi.taxonomy.mgr.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ilimi.common.dto.NodeDTO;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
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

public class BaseMimeTypeManager extends BaseManager{
	
	@Autowired
	private ContentBundle contentBundle;

	private static final String tempFileLocation = "/data/contentBundle/";
	private static Logger LOGGER = LogManager.getLogger(IMimeTypeManager.class.getName());
	
	private static final String bucketName = "ekstep-public";
	private static final String folderName = "content";
	public boolean isArtifactUrlSet(Map<String, Object> contentMap) {
		return false;
	}
	
	public String uploadFile(String folder, String filename) {
		File olderName = new File(folder + filename);
		try {
			if (null != olderName && olderName.exists() && olderName.isFile()) {
				String parentFolderName = olderName.getParent();
				File newName = new File(parentFolderName + File.separator
						+ System.currentTimeMillis() + "_" + olderName.getName());
				olderName.renameTo(newName);
				String[] url = AWSUploader.uploadFile(bucketName, folderName, newName);
				return url[1];
			}
		} catch (Exception ex) {
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_EXTRACT.name(), ex.getMessage());
		}
		return null;
	}
	
	public boolean isJSONValid(String content) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			mapper.readTree(content);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public boolean isECMLValid(String content) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			dBuilder.parse(IOUtils.toInputStream(content, "UTF-8"));
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public  Map<String, List<Object>> readECMLFile(String filePath) {
		final Map<String, List<Object>> mediaIdMap = new HashMap<String, List<Object>>();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new DefaultHandler() {
				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException {
					if (qName.equalsIgnoreCase("media")) {
						String id = attributes.getValue("id");
						if (StringUtils.isNotBlank(id)) {
							String src = attributes.getValue("src");
							if (StringUtils.isNotBlank(src)) {
								String assetId = attributes.getValue("assetId");
								List<Object> mediaValues = new ArrayList<Object>();
								mediaValues.add(src);
								mediaValues.add(assetId);
								mediaIdMap.put(id, mediaValues);
							}
						}
					}
				}

				public void endElement(String uri, String localName,
						String qName) throws SAXException {
					// System.out.println("End Element :" + qName);
				}
			};
			saxParser.parse(filePath, handler);
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_EXTRACT.name(),e.getMessage());
		}
		return mediaIdMap;
	}
	public  void delete(File file) throws IOException {
		if (file.isDirectory()) {
			// directory is empty, then delete it
			if (file.list().length == 0) {
				file.delete();
			} else {
				// list all the directory contents
				String files[] = file.list();
				for (String temp : files) {
					// construct the file structure
					File fileDelete = new File(file, temp);
					// recursive delete
					delete(fileDelete);
				}
				// check the directory again, if empty then delete it
				if (file.list().length == 0) {
					file.delete();
				}
			}

		} else {
			// if file, then delete it
			file.delete();
		}
	}
	
	protected Response compress(Node node) {
		String tempFolderWithTimeStamp = tempFileLocation + File.separator
				+ System.currentTimeMillis() + "_temp";
		String fileName = System.currentTimeMillis() + "_" + node.getIdentifier();
		Map<String, Object> metadata = new HashMap<String, Object>();
		metadata = node.getMetadata();
		String contentBody = (String) metadata.get("body");
		String contentType = checkBodyContentType(contentBody);
		if (StringUtils.isBlank(contentType))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_BODY_INVALID.name(),
					"Content of Body Either Invalid or Null");
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
			downloadAppIcon(node, tempFolderWithTimeStamp);
		} catch (IOException e) {
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
				String[] urlArray = AWSUploader.uploadFile(bucketName, folderName, newName);
				if (!StringUtils.isBlank(urlArray[1]))
					node.getMetadata().put(ContentAPIParams.artifactUrl.name(), urlArray[1]);
				Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER,
						"getDataNode", GraphDACParams.node_id.name(), contentId);
				request.put(GraphDACParams.get_tags.name(), true);
				Response getNodeRes = getResponse(request, LOGGER);
				if (checkError(getNodeRes)) {
					return getNodeRes;
				}
				Node nodePublish = (Node) getNodeRes.get(GraphDACParams.node.name());
				node.getMetadata().put(ContentAPIParams.downloadUrl.name(), nodePublish);
				response = addDataToContentNode(node);
			}

		} catch (Exception e) {
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_PUBLISH.name(), e.getMessage());
		} finally {
			deleteTemp(sourceFolder);
		}
		return response;
	}
	
	private void downloadAppIcon(Node node, String tempFolder) {
		String appIcon = (String) node.getMetadata().get("appIcon");
		if (StringUtils.isNotBlank(appIcon)) {
			File logoFile = HttpDownloadUtility.downloadFile(appIcon, tempFolder);
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
	}
	private void deleteTemp(String sourceFolder) {
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
				throw new ServerException(ContentErrorCodes.ERR_CONTENT_PUBLISH.name(),
						e.getMessage());
			}
		}
		
	}

	protected Response rePublish(Node node) {
		Response response = new Response();
		String tempFolder = tempFileLocation + File.separator
				+ System.currentTimeMillis() + "_temp";
		File ecarFile = HttpDownloadUtility.downloadFile(
				(String) node.getMetadata().get("artifactUrl"), tempFolder);
		try {
			UnzipUtility unzip = new UnzipUtility();
			String unZipLocation = ecarFile.getParent() + File.separator
					+ ecarFile.getName().split("\\.")[0];
			unzip.unzip(ecarFile.getPath(), unZipLocation);
			File olderZipFile = CustomParser.getZipFile(ecarFile, node);
			File newName = new File(unZipLocation + File.separator + node.getIdentifier()
					+ File.separator + olderZipFile.getName());
			node.getMetadata().put(ContentAPIParams.downloadUrl.name(), newName);
			response = addDataToContentNode(node);
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_PUBLISH.name(), e.getMessage());
		}finally{
			deleteTemp(tempFolder);
		}
		return response;
	}

	private Response addDataToContentNode(Node node) {
		List<Node> nodes = new ArrayList<Node>();
		nodes.add(node);
		List<Map<String, Object>> ctnts = new ArrayList<Map<String, Object>>();
		List<String> childrenIds = new ArrayList<String>();
		getContentBundleData(node.getGraphId(), nodes, ctnts, childrenIds);
		String bundleFileName = node.getIdentifier() + "_" + System.currentTimeMillis() + ".ecar";
		String[] urlArray = contentBundle.createContentBundle(ctnts, childrenIds, bundleFileName,
				"1.1");
		node.getMetadata().put("s3Key", urlArray[0]);
		node.getMetadata().put("downloadUrl", urlArray[1]);
		Number pkgVersion = (Number) node.getMetadata().get("pkgVersion");
		if (null == pkgVersion || pkgVersion.intValue() < 1) {
			pkgVersion = 1.0;
		} else {
			pkgVersion = pkgVersion.doubleValue() + 1;
		}
		node.getMetadata().put("pkgVersion", pkgVersion);
		node.getMetadata().put("status", "Live");
		return updateContentNode(node, urlArray[1]);
	}

	protected Response updateContentNode(Node node, String url) {
		Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER,
				"updateDataNode");
		updateReq.put(GraphDACParams.node.name(), node);
		updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
		Response updateRes = getResponse(updateReq, LOGGER);
		updateRes.put(ContentAPIParams.content_url.name(), url);
		updateRes.put(ContentAPIParams.updated_node.name(), node);
		return updateRes;
	}
	


	@SuppressWarnings("unchecked")
	protected void getContentBundleData(String taxonomyId, List<Node> nodes,
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

	public Response uploadContent(Node node, File uploadedFile, String folder) {
		String[] urlArray = new String[] {};
		try {
			if (StringUtils.isBlank(folder))
				folder = folderName;
			urlArray = AWSUploader.uploadFile(bucketName, folder, uploadedFile);
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_UPLOAD_FILE.name(),
					"Error wihile uploading the File.", e);
		}
		node.getMetadata().put("s3Key", urlArray[0]);
		node.getMetadata().put(ContentAPIParams.downloadUrl.name(), urlArray[1]);
		node.getMetadata().put(ContentAPIParams.artifactUrl.name(), urlArray[1]);
		Number pkgVersion = (Number) node.getMetadata().get(ContentAPIParams.pkgVersion.name());
		if (null == pkgVersion || pkgVersion.intValue() < 1) {
			pkgVersion = 1;
		} else {
			pkgVersion = pkgVersion.doubleValue() + 1;
		}
		node.getMetadata().put(ContentAPIParams.pkgVersion.name(), pkgVersion);
		return updateContentNode(node, urlArray[1]);
	}
}
