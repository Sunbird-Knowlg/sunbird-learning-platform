package com.ilimi.taxonomy.mgr.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.NodeDTO;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.Sort;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.taxonomy.enums.ContentAPIParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;
import com.ilimi.taxonomy.mgr.IContentManager;
import com.ilimi.taxonomy.util.AWSUploader;

@Component
public class ContentManagerImpl extends BaseManager implements IContentManager {

    private static Logger LOGGER = LogManager.getLogger(IContentManager.class.getName());
    
    private static final List<String> DEFAULT_FIELDS = new ArrayList<String>();
    private static final List<String> DEFAULT_STATUS = new ArrayList<String>();
    
    private ObjectMapper mapper = new ObjectMapper();
    
    static {
        DEFAULT_FIELDS.add("identifier");
        DEFAULT_FIELDS.add("name");
        DEFAULT_FIELDS.add("description");

        DEFAULT_STATUS.add("Live");
    }
    
    private static final String bucketName = "ekstep-public";
    private static final String folderName = "worksheets";
    
    @Override
    public Response create(String taxonomyId, String objectType, Request request) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(objectType)) 
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "ObjectType is blank");
        Node item = (Node) request.get(ContentAPIParams.content.name());
        if (null == item)
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT.name(), objectType+" Object is blank");
        item.setObjectType(objectType);
        Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "validateNode");
        validateReq.put(GraphDACParams.node.name(), item);
        Response validateRes = getResponse(validateReq, LOGGER);
        if(checkError(validateRes)) {
            return validateRes;
        } else {
            Request createReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
            createReq.put(GraphDACParams.node.name(), item);
            Response createRes = getResponse(createReq, LOGGER);
            return createRes;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response findAll(String taxonomyId, String objectType, Integer offset, Integer limit, String[] gfields) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(objectType))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_INVALID_OBJECT_TYPE.name(), "Object Type is blank");
        LOGGER.info("Find All Content : " + taxonomyId + ", ObjectType: " + objectType);
        SearchCriteria sc = new SearchCriteria();
        sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
        sc.setObjectType(objectType);
        sc.sort(new Sort(PARAM_STATUS, Sort.SORT_ASC));
        if (null != offset && offset.intValue() >= 0)
            sc.setStartPosition(offset);
        if (null != limit && limit.intValue() > 0)
            sc.setResultSize(limit);
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes", GraphDACParams.search_criteria.name(),
                sc);
        request.put(GraphDACParams.get_tags.name(), true);
        Response findRes = getResponse(request, LOGGER);
        Response response = copyResponse(findRes);
        if (checkError(response))
            return response;
        else {
            List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
            if (null != nodes && !nodes.isEmpty()) {
                if (null != gfields && gfields.length > 0) {
                    for (Node node : nodes) {
                        setMetadataFields(node, gfields);
                    }
                }
                response.put(ContentAPIParams.contents.name(), nodes);
            }
            Request countReq = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getNodesCount",
                    GraphDACParams.search_criteria.name(), sc);
            Response countRes = getResponse(countReq, LOGGER);
            if (checkError(countRes)) {
                return countRes;
            } else {
                Long count = (Long) countRes.get(GraphDACParams.count.name());
                response.put(GraphDACParams.count.name(), count);
            }
        }
        return response;
    }

    @Override
    public Response find(String id, String taxonomyId, String objectType, String[] fields) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(), "Content Object Id is blank");
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode", GraphDACParams.node_id.name(), id);
        request.put(GraphDACParams.get_tags.name(), true);
        Response getNodeRes = getResponse(request, LOGGER);
        Response response = copyResponse(getNodeRes);
        if (checkError(response)) {
            return response;
        }
        Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
        if(!objectType.equalsIgnoreCase(node.getObjectType())) {
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_INVALID_OBJECT_TYPE.name(), "Invalid object type of the content for given id: "+id);
        }
        return getNodeRes;
    }

    @Override
    public Response update(String id, String taxonomyId, String objectType, Request request) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(), "Content Object Id is blank");
        if (StringUtils.isBlank(objectType)) 
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "ObjectType is blank");        
        Node item = (Node) request.get(ContentAPIParams.content.name());
        if (null == item)
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT.name(), objectType+" Object is blank");
        item.setIdentifier(id);
        item.setObjectType(objectType);
        Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "validateNode");
        validateReq.put(GraphDACParams.node.name(), item);
        Response validateRes = getResponse(validateReq, LOGGER);
        if(checkError(validateRes)) {
            return validateRes;
        } else {
            Request updateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
            updateReq.put(GraphDACParams.node.name(), item);
            updateReq.put(GraphDACParams.node_id.name(), item.getIdentifier());
            Response updateRes = getResponse(updateReq, LOGGER);
            return updateRes;
        }
    }

    @Override
    public Response delete(String id, String taxonomyId) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(), "Content Object Id is blank");
        Request request = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "deleteDataNode", GraphDACParams.node_id.name(), id);
        return getResponse(request, LOGGER);
    }
    

    @Override
    public Response upload(String id, String taxonomyId, File uploadedFile) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank.");
        if (StringUtils.isBlank(id))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(), "Content Object Id is blank.");
        if(null == uploadedFile) {
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_UPLOAD_OBJECT.name(), "Upload file is blank.");
        }
        if(null != uploadedFile && !Arrays.asList("zip", "gzip").contains(FilenameUtils.getExtension(uploadedFile.getName()))) {
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_INVALID_UPLOAD_OBJECT.name(), "Upload file is invalid.");
        }
        
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode", GraphDACParams.node_id.name(), id);
        request.put(GraphDACParams.get_tags.name(), true);
        Response getNodeRes = getResponse(request, LOGGER);
        Response response = copyResponse(getNodeRes);
        if (checkError(response)) {
            return response;
        }
        Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
        String[] urlArray = new String[]{};
        try {
            urlArray = AWSUploader.uploadFile(bucketName, folderName, uploadedFile);
        } catch (Exception e) {
            throw new ServerException(ContentErrorCodes.ERR_CONTENT_UPLOAD_FILE.name(), "Error wihile uploading the File.", e);
        }
        node.getMetadata().put("s3Key", urlArray[0]);
        node.getMetadata().put("downloadUrl", urlArray[1]);
        Request updateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
        updateReq.put(GraphDACParams.node.name(), node);
        updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
        Response updateRes = getResponse(updateReq, LOGGER);
        updateRes.put(ContentAPIParams.content_url.name(), urlArray[1]);
        return updateRes;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Response listContents(String objectType, Request request) {
        String taxonomyId = (String) request.get(PARAM_SUBJECT);
        LOGGER.info("List Contents : " + taxonomyId);
        Map<String, DefinitionDTO> definitions = new HashMap<String, DefinitionDTO>();
        List<Request> requests = new ArrayList<Request>();
        if (StringUtils.isNotBlank(taxonomyId)) {
            DefinitionDTO definition = getDefinition(taxonomyId, objectType);
            definitions.put(taxonomyId, definition);
            Request req = getContentsListRequest(request, taxonomyId, objectType, definition);
            requests.add(req);
        } else {
            DefinitionDTO definition = getDefinition(TaxonomyManagerImpl.taxonomyIds[0], objectType);
            // TODO: need to get definition from the specific taxonomy.
            for (String id : TaxonomyManagerImpl.taxonomyIds) {
                definitions.put(id, definition);
                Request req = getContentsListRequest(request, id, objectType, definition);
                requests.add(req);
            }
        }
        Response response = getResponse(requests, LOGGER, GraphDACParams.node_list.name(), ContentAPIParams.contents.name());
        Response listRes = copyResponse(response);
        if (checkError(response))
            return response;
        else {
            List<List<Node>> nodes = (List<List<Node>>) response.get(ContentAPIParams.contents.name());
            List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();
            if (null != nodes && !nodes.isEmpty()) {
                for (List<Node> list : nodes) {
                    if (null != list && !list.isEmpty()) {
                        for (Node node : list) {
                            Map<String, Object> content = getContent(node, request, definitions);
                            contents.add(content);
                        }
                    }
                }
            }
            String returnKey = ContentAPIParams.contents.name();
            if("Worksheet".equals(objectType)) {
                returnKey = ContentAPIParams.worksheets.name();
            } else if("Game".equals(objectType)) {
                returnKey = ContentAPIParams.games.name();
            }
            listRes.put(returnKey, contents);
            Integer ttl = null;
            if (null != definitions.get(TaxonomyManagerImpl.taxonomyIds[0]) && null != definitions.get(TaxonomyManagerImpl.taxonomyIds[0]).getMetadata())
                ttl = (Integer) definitions.get(TaxonomyManagerImpl.taxonomyIds[0]).getMetadata().get(PARAM_TTL);
            if (null == ttl || ttl.intValue() <= 0)
                ttl = DEFAULT_TTL;
            listRes.put(PARAM_TTL, ttl);
            return listRes;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getContent(Node node, Request request, Map<String, DefinitionDTO> definitions) {
        String subject = node.getGraphId();
        Object objFields = request.get(PARAM_FIELDS);
        List<String> fields = getList(mapper, objFields, PARAM_FIELDS);
        if (null == fields || fields.isEmpty()) {
            if (null != definitions.get(subject) && null != definitions.get(subject).getMetadata()) {
                String[] arr = (String[]) definitions.get(subject).getMetadata().get(PARAM_FIELDS);
                if (null != arr && arr.length > 0) {
                    fields = Arrays.asList(arr);
                }
            }
        }
        Map<String, Object> content = null;
        if(null != fields && fields.size() > 0) {
            content = new HashMap<String, Object>();
            for(String key : fields)
                content.put(key, node.getMetadata().get(key));
        } else {
            content = new HashMap<String, Object>(node.getMetadata());
        }
        content.put("subject", subject);
        content.put("identifier", node.getIdentifier());
        List<NodeDTO> concepts = new ArrayList<NodeDTO>();
        NodeDTO questionnaire = null;
        if(null != node.getOutRelations() && node.getOutRelations().size() > 0) {
            concepts = new ArrayList<NodeDTO>();
            for (Relation rel : node.getOutRelations()) {
                if (StringUtils.equals(RelationTypes.ASSOCIATED_TO.relationName(), rel.getRelationType())
                        && StringUtils.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name(), rel.getEndNodeType())) {
                    if (StringUtils.equalsIgnoreCase("Concept", rel.getEndNodeObjectType())) {
                        concepts.add(new NodeDTO(rel.getEndNodeId(), rel.getEndNodeName(), rel.getEndNodeObjectType()));
                    } else if(StringUtils.equalsIgnoreCase("Questionnaire", rel.getEndNodeObjectType())) {
                        questionnaire = new NodeDTO(rel.getEndNodeId(), rel.getEndNodeName(), rel.getEndNodeObjectType());
                    }
                }
            }
        }
        content.put("concepts", concepts);
        content.put("questionnaire", questionnaire);
        return content;
    }

    private DefinitionDTO getDefinition(String taxonomyId, String objectType) {
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
                GraphDACParams.object_type.name(), objectType);
        Response response = getResponse(request, LOGGER);
        if (!checkError(response)) {
            DefinitionDTO definition = (DefinitionDTO) response.get(GraphDACParams.definition_node.name());
            return definition;
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private Request getContentsListRequest(Request request, String taxonomyId, String objectType, DefinitionDTO definition) {
        SearchCriteria sc = new SearchCriteria();
        sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
        sc.setObjectType(objectType);
        sc.sort(new Sort(SystemProperties.IL_UNIQUE_ID.name(), Sort.SORT_ASC));
        setLimit(request, sc, definition);

        ObjectMapper mapper = new ObjectMapper();
        // status filter
        List<String> statusList = new ArrayList<String>();
        Object statusParam = request.get(PARAM_STATUS);
        if (null != statusParam)
            statusList = getList(mapper, statusParam, PARAM_STATUS);
        if (null == statusList || statusList.isEmpty()) {
            if (null != definition && null != definition.getMetadata()) {
                String[] arr = (String[]) definition.getMetadata().get(PARAM_STATUS);
                if (null != arr && arr.length > 0) {
                    statusList = Arrays.asList(arr);
                }
            }
        }
        if (null == statusList || statusList.isEmpty())
            statusList = DEFAULT_STATUS;
        MetadataCriterion mc = MetadataCriterion.create(Arrays.asList(new Filter(PARAM_STATUS, SearchConditions.OP_IN, statusList)));

        // set metadata filter params
        for (Entry<String, Object> entry : request.getRequest().entrySet()) {
            if (!StringUtils.equalsIgnoreCase(PARAM_SUBJECT, entry.getKey()) && !StringUtils.equalsIgnoreCase(PARAM_FIELDS, entry.getKey())
                    && !StringUtils.equalsIgnoreCase(PARAM_LIMIT, entry.getKey())
                    && !StringUtils.equalsIgnoreCase(PARAM_UID, entry.getKey())
                    && !StringUtils.equalsIgnoreCase(PARAM_STATUS, entry.getKey())) {
                List<String> list = getList(mapper, entry.getValue(), entry.getKey());
                if (null != list && !list.isEmpty()) {
                    mc.addFilter(new Filter(entry.getKey(), SearchConditions.OP_IN, list));
                }
            }
        }
        sc.addMetadata(mc);
//        Object objFields = request.get(PARAM_FIELDS);
//        List<String> fields = getList(mapper, objFields, PARAM_FIELDS);
//        if (null == fields || fields.isEmpty()) {
//            if (null != definition && null != definition.getMetadata()) {
//                String[] arr = (String[]) definition.getMetadata().get(PARAM_FIELDS);
//                if (null != arr && arr.length > 0) {
//                    fields = Arrays.asList(arr);
//                }
//            }
//        }
//        if (null == fields || fields.isEmpty())
//            fields = DEFAULT_FIELDS;
//        sc.setFields(fields);

        Request req = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes", GraphDACParams.search_criteria.name(), sc);
        return req;
    }
    
    private void setLimit(Request request, SearchCriteria sc, DefinitionDTO definition) {
        Integer defaultLimit = null;
        if (null != definition && null != definition.getMetadata())
            defaultLimit = (Integer) definition.getMetadata().get(PARAM_LIMIT);
        if (null == defaultLimit || defaultLimit.intValue() <= 0)
            defaultLimit = DEFAULT_LIMIT;
        Integer limit = null;
        try {
            Object obj = request.get(PARAM_LIMIT);
            if (obj instanceof String)
                limit = Integer.parseInt((String) obj);
            else
                limit = (Integer) request.get(PARAM_LIMIT);
            if (null == limit || limit.intValue() <= 0)
                limit = defaultLimit;
        } catch (Exception e) {
        }
        sc.setResultSize(limit);
    }
    
    @SuppressWarnings("rawtypes")
    private List getList(ObjectMapper mapper, Object object, String propName) {
        if (null != object) {
            try {
                String strObject = mapper.writeValueAsString(object);
                List list = mapper.readValue(strObject.toString(), List.class);
                return list;
            } catch (Exception e) {
                throw new ClientException(ContentErrorCodes.ERR_CONTENT_INVALID_PARAM.name(), "Request Parameter '" + propName
                        + "' should be a list");
                
            }
        }
        return null;
    }
}
