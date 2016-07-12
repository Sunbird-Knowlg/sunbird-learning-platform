package com.ilimi.taxonomy.content.finalizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.slugs.Slug;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.common.util.ZipUtility;
import org.springframework.beans.factory.annotation.Autowired;

import com.ilimi.common.dto.NodeDTO;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.taxonomy.content.common.ContentConfigurationConstants;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.pipeline.BasePipeline;
import com.ilimi.taxonomy.content.util.ECRFToJSONConvertor;
import com.ilimi.taxonomy.content.util.ECRFToXMLConvertor;
import com.ilimi.taxonomy.dto.ContentSearchCriteria;
import com.ilimi.taxonomy.mgr.impl.TaxonomyManagerImpl;
import com.ilimi.taxonomy.util.ContentBundle;

public class FinalizePipeline extends BasePipeline{
	
	@Autowired
    private ContentBundle contentBundle;
	
	private static Logger LOGGER = LogManager.getLogger(FinalizePipeline.class.getName());
	
	private static final int IDX_S3_KEY = 0;
	private static final int IDX_S3_URL = 1;
	
	protected String basePath;
	protected String contentId;
	
	public FinalizePipeline (String basePath, String contentId) {
		if (!isValidBasePath(basePath))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Path does not Exist.]");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Invalid Content Id.]");
		this.basePath = basePath;
		this.contentId = contentId;
	}
	
	public Response finalyze(String operation, Map<String, Object> parameterMap) {
		Response response = new Response();
		if (StringUtils.isBlank(operation))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
					ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid Operation.]");
		if (null != parameterMap && !StringUtils.isBlank(operation)) {
			switch (operation) {
			case "upload":
			case "UPLOAD": {
				File file = (File) parameterMap.get(ContentWorkflowPipelineParams.file.name());
				Plugin ecrf = (Plugin) parameterMap.get(ContentWorkflowPipelineParams.ecrf.name());
				String ecmlType = (String) parameterMap.get(ContentWorkflowPipelineParams.ecmlType.name());
				Node node = (Node) parameterMap.get(ContentWorkflowPipelineParams.node.name());
				if (null == file || !file.exists()) 
					throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
							ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [File does not Exist.]");
				if (null == ecrf)
					throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
							ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid or null ECRF Object.]");
				if (StringUtils.isBlank(ecmlType))
					throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
							ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid ECML Type.]");
				if (null == node) 
					throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
							ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid or null Node.]");
				
				// Get Content String
				String ecml = getECMLString(ecrf, ecmlType);
				
				// Upload Package
				String[] urlArray = uploadToAWS(file, getUploadFolderName());
				
				// Update Body, Reset Editor State and Update Content Node
				node.getMetadata().put(ContentWorkflowPipelineParams.s3Key.name(), urlArray[IDX_S3_KEY]);
		        node.getMetadata().put(ContentWorkflowPipelineParams.artifactUrl.name(), urlArray[IDX_S3_URL]);
		        node.getMetadata().put(ContentWorkflowPipelineParams.body.name(), ecml);
		        node.getMetadata().put(ContentWorkflowPipelineParams.editorState.name(), null);
				
				// Update Node
		        response = updateContentNode(node, urlArray[IDX_S3_URL]);
			}
				break;
				
			case "publish":
			case "PUBLISH": {
				Node node = (Node) parameterMap.get(ContentWorkflowPipelineParams.node.name());
				Plugin ecrf = (Plugin) parameterMap.get(ContentWorkflowPipelineParams.ecrf.name());
				String ecmlType = (String) parameterMap.get(ContentWorkflowPipelineParams.ecmlType.name());
				boolean isCompressionApplied = (boolean) parameterMap.get(ContentWorkflowPipelineParams.isCompressionApplied.name());
				if (null == node) 
					throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
							ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid or null Node.]");
				if (null == ecrf)
					throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
							ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid or null ECRF Object.]");
				if (StringUtils.isBlank(ecmlType))
					throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
							ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid ECML Type.]");
				
				// Get Content String
				String ecml = getECMLString(ecrf, ecmlType);
				
				// Create 'artifactUrl' Package
				if (BooleanUtils.isTrue(isCompressionApplied)) {
					// Write ECML File
					writeECMLFile(ecml, ecmlType);
					
					// Create 'ZIP' Package
					String zipFileName = basePath + File.separator + System.currentTimeMillis() + "_"
							+ contentId 
							+ ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR 
							+ ContentConfigurationConstants.DEFAULT_ZIP_EXTENSION;
					createZipPackage(zipFileName);
					
					// Upload Package
					File packageFile = new File(zipFileName);
					if (packageFile.exists()) {
						// Upload to S3
						String[] urlArray = uploadToAWS(packageFile, getUploadFolderName());
						
						// Set 'artifactUrl' For Node
						node.getMetadata().put(ContentWorkflowPipelineParams.artifactUrl.name(), urlArray[IDX_S3_URL]);
						
						// Delete the Package From File System
						packageFile.delete();
					}
				}
				
				// Download App Icon
				downloadAppIcon(node);
				
				// Create ECAR Bundle
				List<Node> nodes = new ArrayList<Node>();
		        nodes.add(node);
		        List<Map<String, Object>> ctnts = new ArrayList<Map<String, Object>>();
		        List<String> childrenIds = new ArrayList<String>();
		        getContentBundleData(node.getGraphId(), nodes, ctnts, childrenIds);
		        String bundleFileName = Slug.makeSlug((String) node.getMetadata().get(ContentWorkflowPipelineParams.name.name()), true) + "_"
						+ System.currentTimeMillis() + "_" + node.getIdentifier() + ".ecar";
		        String[] urlArray = contentBundle.createContentBundle(ctnts, childrenIds, bundleFileName, "1.1");
		        double version = 1.0;
		        if (null != node 
		        		&& null != node.getMetadata() 
		        		&& null != node.getMetadata().get(ContentWorkflowPipelineParams.pkgVersion.name())) 
		        	version = getDoubleValue(node.getMetadata().get(ContentWorkflowPipelineParams.pkgVersion.name())) + 1;
				
				// Populate Fields and Update Node
		        node.getMetadata().put(ContentWorkflowPipelineParams.pkgVersion.name(), version);
		        node.getMetadata().put(ContentWorkflowPipelineParams.s3Key.name(), urlArray[IDX_S3_KEY]);
		        node.getMetadata().put(ContentWorkflowPipelineParams.downloadUrl.name(), urlArray[IDX_S3_URL]);
		        node.getMetadata().put(ContentWorkflowPipelineParams.status.name(), ContentWorkflowPipelineParams.Live.name());
		        node.getMetadata().put(ContentWorkflowPipelineParams.lastPublishedOn.name(), formatCurrentDate());
		        node.getMetadata().put(ContentWorkflowPipelineParams.size.name(), getS3FileSize(urlArray[IDX_S3_KEY]));
		        Node newNode = new Node(node.getIdentifier(), node.getNodeType(), node.getObjectType());
		        newNode.setGraphId(node.getGraphId());
		        newNode.setMetadata(node.getMetadata());
		        response = updateContentNode(newNode, urlArray[IDX_S3_URL]);
			}
				break;

			default:
				break;
			}
		}
		return response;
	}
	
	private void createZipPackage(String zipFileName) {
		ZipUtility appZip = new ZipUtility(basePath, zipFileName);
		appZip.generateFileList(new File(basePath));
		appZip.zipIt(zipFileName);
	}
	
	private void writeECMLFile(String ecml, String ecmlType) {
		try {
			if (StringUtils.isBlank(ecml))
				throw new ClientException(ContentErrorCodeConstants.EMPTY_ECML.name(), 
						ContentErrorMessageConstants.EMPTY_ECML_STRING + " | [Unable to write Empty ECML File.]");
			if (StringUtils.isBlank(ecmlType))
				throw new ClientException(ContentErrorCodeConstants.INVALID_ECML_TYPE.name(), 
						ContentErrorMessageConstants.INVALID_ECML_TYPE + " | [System is in a fix between (XML & JSON) ECML Type.]");
			
			File file = new File(basePath + File.separator 
					+ ContentConfigurationConstants.DEFAULT_ECML_FILE_NAME 
					+ ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR + ecmlType);
			FileUtils.writeStringToFile(file, ecml);
		} catch(IOException e) {
			throw new ServerException(ContentErrorCodeConstants.ECML_FILE_WRITE.name(), 
					ContentErrorMessageConstants.ECML_FILE_WRITE_ERROR + " | [Unable to Write ECML File.]");
		}
	}
	
	private void downloadAppIcon(Node node) {
		try {
			if (null != node) {
				String appIcon = (String) node.getMetadata().get(ContentWorkflowPipelineParams.appIcon.name());
				if (!StringUtils.isBlank(appIcon)) {
					LOGGER.info("Content Id: " + node.getIdentifier() + " | App Icon: " + appIcon);
					File appIconFile = HttpDownloadUtility.downloadFile(appIcon, basePath);
					if (null != appIconFile && appIconFile.exists() && appIconFile.isFile()) {
						String parentFolderName = appIconFile.getParent();
						File logoFileName = new File(parentFolderName + File.separator + "logo.png");
						appIconFile.renameTo(logoFileName);
					}
				}
			}
		} catch(Exception e) {
			throw new ServerException(ContentErrorCodeConstants.DOWNLOAD_ERROR.name(), 
					ContentErrorMessageConstants.APP_ICON_DOWNLOAD_ERROR 
					+ " | [Unable to Download App Icon for Content Id: '" + node.getIdentifier() + "' ]", e);
		}
	}
	
	private String getECMLString(Plugin ecrf, String ecmlType) {
		String ecml = "";
		if (null != ecrf) {
			LOGGER.info("Converting ECML From ECRF Object.");
			if (StringUtils.equalsIgnoreCase(ecmlType, ContentWorkflowPipelineParams.xml.name())) {
				ECRFToXMLConvertor convertor = new ECRFToXMLConvertor();
				ecml = convertor.getContentXmlString(ecrf);
			} else if (StringUtils.equalsIgnoreCase(ecmlType, ContentWorkflowPipelineParams.json.name())) {
				ECRFToJSONConvertor convertor = new ECRFToJSONConvertor();
				ecml = convertor.getContentJsonString(ecrf);
			}
		}
		return ecml;
	}
	
	private void getContentBundleData(String graphId, List<Node> nodes, List<Map<String, Object>> ctnts,
            List<String> childrenIds) {
        Map<String, Node> nodeMap = new HashMap<String, Node>();
        if (null != nodes && !nodes.isEmpty()) {
            for (Node node : nodes) {
                getContentRecursive(graphId, node, nodeMap, childrenIds, ctnts);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void getContentRecursive(String graphId, Node node, Map<String, Node> nodeMap, List<String> childrenIds,
            List<Map<String, Object>> ctnts) {
        if (!nodeMap.containsKey(node.getIdentifier())) {
            nodeMap.put(node.getIdentifier(), node);
            Map<String, Object> metadata = new HashMap<String, Object>();
            if (null == node.getMetadata())
                node.setMetadata(new HashMap<String, Object>());
            String status = (String) node.getMetadata().get(ContentWorkflowPipelineParams.status.name());
            if (StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.Live.name(), status)) {
                metadata.putAll(node.getMetadata());
                metadata.put(ContentWorkflowPipelineParams.identifier.name(), node.getIdentifier());
                metadata.put(ContentWorkflowPipelineParams.objectType.name(), node.getObjectType());
                metadata.put(ContentWorkflowPipelineParams.subject.name(), node.getGraphId());
                metadata.remove(ContentWorkflowPipelineParams.body.name());
                metadata.remove(ContentWorkflowPipelineParams.editorState.name());
                if (null != node.getTags() && !node.getTags().isEmpty())
                    metadata.put(ContentWorkflowPipelineParams.tags.name(), node.getTags());
                List<String> searchIds = new ArrayList<String>();
                if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
                    List<NodeDTO> children = new ArrayList<NodeDTO>();
                    for (Relation rel : node.getOutRelations()) {
                        if (StringUtils.equalsIgnoreCase(RelationTypes.SEQUENCE_MEMBERSHIP.relationName(),
                                rel.getRelationType())
                                && StringUtils.equalsIgnoreCase(node.getObjectType(), rel.getEndNodeObjectType())) {
                            childrenIds.add(rel.getEndNodeId());
                            if (!nodeMap.containsKey(rel.getEndNodeId())) {
                                searchIds.add(rel.getEndNodeId());
                            }
                            children.add(new NodeDTO(rel.getEndNodeId(), rel.getEndNodeName(), rel.getEndNodeObjectType(),
                                    rel.getRelationType(), rel.getMetadata()));
                        }
                    }
                    if (!children.isEmpty()) {
                        metadata.put(ContentWorkflowPipelineParams.children.name(), children);
                    }
                }
                ctnts.add(metadata);
                if (!searchIds.isEmpty()) {
                    Response searchRes = searchNodes(graphId, searchIds);
                    if (checkError(searchRes)) {
                        throw new ServerException(ContentErrorCodeConstants.SEARCH_ERROR.name(),
                                getErrorMessage(searchRes));
                    } else {
                        List<Object> list = (List<Object>) searchRes.get(ContentWorkflowPipelineParams.contents.name());
                        if (null != list && !list.isEmpty()) {
                            for (Object obj : list) {
                                List<Node> nodeList = (List<Node>) obj;
                                for (Node child : nodeList) {
                                    getContentRecursive(graphId, child, nodeMap, childrenIds, ctnts);
                                }
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
        Filter filter = new Filter(ContentWorkflowPipelineParams.identifier.name(), 
        		SearchConditions.OP_IN, contentIds);
        filters.add(filter);
        MetadataCriterion metadata = MetadataCriterion.create(filters);
        metadata.addFilter(filter);
        criteria.setMetadata(metadata);
        List<Request> requests = new ArrayList<Request>();
        if (StringUtils.isNotBlank(taxonomyId)) {
            Request req = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, 
            		ContentWorkflowPipelineParams.searchNodes.name(),
                    GraphDACParams.search_criteria.name(), criteria.getSearchCriteria());
            req.put(GraphDACParams.get_tags.name(), true);
            requests.add(req);
        } else {
            for (String tId : TaxonomyManagerImpl.taxonomyIds) {
                Request req = getRequest(tId, GraphEngineManagers.SEARCH_MANAGER, 
                		ContentWorkflowPipelineParams.searchNodes.name(),
                        GraphDACParams.search_criteria.name(), criteria.getSearchCriteria());
                req.put(GraphDACParams.get_tags.name(), true);
                requests.add(req);
            }
        }
        Response response = getResponse(requests, LOGGER, GraphDACParams.node_list.name(),
                ContentWorkflowPipelineParams.contents.name());
        return response;
    }

}
