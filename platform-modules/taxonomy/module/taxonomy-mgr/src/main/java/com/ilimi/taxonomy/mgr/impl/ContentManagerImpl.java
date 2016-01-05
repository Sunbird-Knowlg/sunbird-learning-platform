package com.ilimi.taxonomy.mgr.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.exception.GraphDACErrorCodes;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.Sort;
import com.ilimi.graph.dac.model.TagCriterion;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.taxonomy.dto.ContentDTO;
import com.ilimi.taxonomy.dto.ContentSearchCriteria;
import com.ilimi.taxonomy.enums.ContentAPIParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;
import com.ilimi.taxonomy.mgr.IContentManager;
import com.ilimi.taxonomy.util.AWSUploader;
import com.ilimi.taxonomy.util.ContentBundle;

@Component
public class ContentManagerImpl extends BaseManager implements IContentManager {

    @Autowired
    private ContentBundle contentBundle;

    private static Logger LOGGER = LogManager.getLogger(IContentManager.class.getName());

    private static final List<String> DEFAULT_FIELDS = new ArrayList<String>();
    private static final List<String> DEFAULT_STATUS = new ArrayList<String>();

    private ObjectMapper mapper = new ObjectMapper();

    static {
        DEFAULT_FIELDS.add("identifier");
        DEFAULT_FIELDS.add("name");
        DEFAULT_FIELDS.add("description");
        DEFAULT_FIELDS.add(SystemProperties.IL_UNIQUE_ID.name());

        DEFAULT_STATUS.add("Live");
    }

    private static final String bucketName = "ekstep-public";
    private static final String folderName = "content";
    private static final String ecarFolderName = "ecar_files";

    protected static final String URL_FIELD = "URL";

    @Override
    public Response create(String taxonomyId, String objectType, Request request) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(objectType))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "ObjectType is blank");
        Node item = (Node) request.get(ContentAPIParams.content.name());
        if (null == item)
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT.name(),
                    objectType + " Object is blank");
        item.setObjectType(objectType);
        Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "validateNode");
        validateReq.put(GraphDACParams.node.name(), item);
        Response validateRes = getResponse(validateReq, LOGGER);
        if (checkError(validateRes)) {
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
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
                GraphDACParams.search_criteria.name(), sc);
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
    public Response find(String id, String taxonomyId, String[] fields) {
        if (StringUtils.isBlank(id))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
                    "Content Object Id is blank");
        if (StringUtils.isNotBlank(taxonomyId)) {
            return getDataNode(taxonomyId, id);
        } else {
            for (String tid : TaxonomyManagerImpl.taxonomyIds) {
                Response response = getDataNode(tid, id);
                if (!checkError(response)) {
                    return response;
                }
            }
        }
        Response response = new Response();
        ResponseParams params = new ResponseParams();
        params.setErr(GraphDACErrorCodes.ERR_GRAPH_NODE_NOT_FOUND.name());
        params.setStatus(StatusType.failed.name());
        params.setErrmsg("Content not found");
        response.setParams(params);
        response.setResponseCode(ResponseCode.RESOURCE_NOT_FOUND);
        return response;
    }

    private Response getDataNode(String taxonomyId, String id) {
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
                GraphDACParams.node_id.name(), id);
        request.put(GraphDACParams.get_tags.name(), true);
        Response getNodeRes = getResponse(request, LOGGER);
        return getNodeRes;
    }

    @Override
    public Response update(String id, String taxonomyId, String objectType, Request request) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
                    "Content Object Id is blank");
        if (StringUtils.isBlank(objectType))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "ObjectType is blank");
        Node item = (Node) request.get(ContentAPIParams.content.name());
        if (null == item)
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT.name(),
                    objectType + " Object is blank");
        item.setIdentifier(id);
        item.setObjectType(objectType);
        Request validateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "validateNode");
        validateReq.put(GraphDACParams.node.name(), item);
        Response validateRes = getResponse(validateReq, LOGGER);
        if (checkError(validateRes)) {
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
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
                    "Content Object Id is blank");
        Request request = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "deleteDataNode",
                GraphDACParams.node_id.name(), id);
        return getResponse(request, LOGGER);
    }

    @Override
    public Response upload(String id, String taxonomyId, File uploadedFile, String folder) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank.");
        if (StringUtils.isBlank(id))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.name(),
                    "Content Object Id is blank.");
        if (null == uploadedFile) {
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_UPLOAD_OBJECT.name(),
                    "Upload file is blank.");
        }
        /*
         * if (null != uploadedFile && !Arrays.asList("zip",
         * "gzip").contains(FilenameUtils.getExtension(uploadedFile.getName())))
         * { throw new
         * ClientException(ContentErrorCodes.ERR_CONTENT_INVALID_UPLOAD_OBJECT.
         * name(), "Upload file is invalid."); }
         */

        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
                GraphDACParams.node_id.name(), id);
        request.put(GraphDACParams.get_tags.name(), true);
        Response getNodeRes = getResponse(request, LOGGER);
        Response response = copyResponse(getNodeRes);
        if (checkError(response)) {
            return response;
        }
        Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
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
        node.getMetadata().put("downloadUrl", urlArray[1]);

        Integer pkgVersion = (Integer) node.getMetadata().get("pkgVersion");
        if (null == pkgVersion || pkgVersion.intValue() < 1) {
            pkgVersion = 1;
        } else {
            pkgVersion += 1;
        }
        node.getMetadata().put("pkgVersion", pkgVersion);

        Request updateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
        updateReq.put(GraphDACParams.node.name(), node);
        updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
        Response updateRes = getResponse(updateReq, LOGGER);
        updateRes.put(ContentAPIParams.content_url.name(), urlArray[1]);
        return updateRes;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response listContents(String taxonomyId, String objectType, Request request) {
        if (StringUtils.isBlank(taxonomyId))
            taxonomyId = (String) request.get(PARAM_SUBJECT);
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
        Response response = getResponse(requests, LOGGER, GraphDACParams.node_list.name(),
                ContentAPIParams.contents.name());
        Response listRes = copyResponse(response);
        if (checkError(response))
            return response;
        else {
            List<List<Node>> nodes = (List<List<Node>>) response.get(ContentAPIParams.contents.name());
            List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();
            if (null != nodes && !nodes.isEmpty()) {
                Object objFields = request.get(PARAM_FIELDS);
                List<String> fields = getList(mapper, objFields, PARAM_FIELDS);
                for (List<Node> list : nodes) {
                    if (null != list && !list.isEmpty()) {
                        for (Node node : list) {
                            if (null == fields || fields.isEmpty()) {
                                String subject = node.getGraphId();
                                if (null != definitions.get(subject)
                                        && null != definitions.get(subject).getMetadata()) {
                                    String[] arr = (String[]) definitions.get(subject).getMetadata().get(PARAM_FIELDS);
                                    if (null != arr && arr.length > 0) {
                                        fields = Arrays.asList(arr);
                                    }
                                }
                            }
                            ContentDTO content = new ContentDTO(node, fields);
                            contents.add(content.returnMap());
                        }
                    }
                }
            }
            String returnKey = ContentAPIParams.content.name();
            listRes.put(returnKey, contents);
            Integer ttl = null;
            if (null != definitions.get(TaxonomyManagerImpl.taxonomyIds[0])
                    && null != definitions.get(TaxonomyManagerImpl.taxonomyIds[0]).getMetadata())
                ttl = (Integer) definitions.get(TaxonomyManagerImpl.taxonomyIds[0]).getMetadata().get(PARAM_TTL);
            if (null == ttl || ttl.intValue() <= 0)
                ttl = DEFAULT_TTL;
            listRes.put(PARAM_TTL, ttl);
            return listRes;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response bundle(Request request) {
        ContentSearchCriteria criteria = new ContentSearchCriteria();
        List<Filter> filters = new ArrayList<Filter>();
        String bundleFileName = (String) request.get("file_name");
        bundleFileName = bundleFileName.replaceAll("\\s+","_");
        List<String> contentIds = (List<String>) request.get("content_identifiers");
        Filter filter = new Filter("identifier", SearchConditions.OP_IN, contentIds);
        filters.add(filter);
        MetadataCriterion metadata = MetadataCriterion.create(filters);
        metadata.addFilter(filter);
        criteria.setMetadata(metadata);
        List<Request> requests = new ArrayList<Request>();
        for (String taxonomyId : TaxonomyManagerImpl.taxonomyIds) {
            Request req = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
                    GraphDACParams.search_criteria.name(), criteria.getSearchCriteria());
            requests.add(req);
        }
        Response response = getResponse(requests, LOGGER, GraphDACParams.node_list.name(),
                ContentAPIParams.contents.name());
        Response listRes = copyResponse(response);
        if (checkError(response)) {
            return response;
        } else {
            List<Object> nodes = (List<Object>) response.get(ContentAPIParams.contents.name());
            List<Map<String, Object>> ctnts = new ArrayList<Map<String, Object>>();
            if (null != nodes && !nodes.isEmpty()) {
                for (Object obj : nodes) {
                    List<Node> list = (List<Node>) obj;
                    for (Node node : list) {
                        if (null != node.getMetadata()) {
                            node.getMetadata().put("identifier", node.getIdentifier());
                            node.getMetadata().put("objectType", node.getObjectType());
                            node.getMetadata().put("subject", node.getGraphId());
                            ctnts.add(node.getMetadata());
                        }
                    }
                }
            }
            if (contentIds.size() != ctnts.size()) {
                throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
                        "One or more of the input content identifier are not found");
            }
            String fileName = bundleFileName + "_" + System.currentTimeMillis() + ".ecar";
            contentBundle.asyncCreateContentBundle(ctnts, fileName);
            String url = "https://" + bucketName + ".s3-ap-southeast-1.amazonaws.com/" + ecarFolderName + "/"
                    + fileName;
            String returnKey = ContentAPIParams.bundle.name();
            listRes.put(returnKey, url);
            return listRes;
        }
    }

    @SuppressWarnings("unchecked")
    public Response search(String taxonomyId, String objectType, Request request) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank.");
        ContentSearchCriteria criteria = (ContentSearchCriteria) request.get(ContentAPIParams.search_criteria.name());
        if (null == criteria)
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_INVALID_SEARCH_CRITERIA.name(),
                    "Search Criteria Object is blank");

        Map<String, DefinitionDTO> definitions = new HashMap<String, DefinitionDTO>();
        if (StringUtils.isNotBlank(taxonomyId)) {
            DefinitionDTO definition = getDefinition(taxonomyId, objectType);
            definitions.put(taxonomyId, definition);
        } else {
            DefinitionDTO definition = getDefinition(TaxonomyManagerImpl.taxonomyIds[0], objectType);
            for (String id : TaxonomyManagerImpl.taxonomyIds) {
                definitions.put(id, definition);
            }
        }
        Request req = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
                GraphDACParams.search_criteria.name(), criteria.getSearchCriteria());
        Response response = getResponse(req, LOGGER);
        Response listRes = copyResponse(response);
        if (checkError(response)) {
            return response;
        } else {
            List<Node> nodes = (List<Node>) response.get(GraphDACParams.node_list.name());
            List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();
            if (null != nodes && !nodes.isEmpty()) {
                Object objFields = request.get(PARAM_FIELDS);
                List<String> fields = getList(mapper, objFields, PARAM_FIELDS);
                for (Node node : nodes) {
                    if (null == fields || fields.isEmpty()) {
                        String subject = node.getGraphId();
                        if (null != definitions.get(subject) && null != definitions.get(subject).getMetadata()) {
                            String[] arr = (String[]) definitions.get(subject).getMetadata().get(PARAM_FIELDS);
                            if (null != arr && arr.length > 0) {
                                fields = Arrays.asList(arr);
                            }
                        }
                    }
                    ContentDTO content = new ContentDTO(node, fields);
                    contents.add(content.returnMap());
                }
            }
            String returnKey = ContentAPIParams.content.name();
            listRes.put(returnKey, contents);
            return listRes;
        }
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
    private Request getContentsListRequest(Request request, String taxonomyId, String objectType,
            DefinitionDTO definition) {
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
        MetadataCriterion mc = MetadataCriterion
                .create(Arrays.asList(new Filter(PARAM_STATUS, SearchConditions.OP_IN, statusList)));

        // set metadata filter params
        for (Entry<String, Object> entry : request.getRequest().entrySet()) {
            if (!StringUtils.equalsIgnoreCase(PARAM_SUBJECT, entry.getKey())
                    && !StringUtils.equalsIgnoreCase(PARAM_FIELDS, entry.getKey())
                    && !StringUtils.equalsIgnoreCase(PARAM_LIMIT, entry.getKey())
                    && !StringUtils.equalsIgnoreCase(PARAM_UID, entry.getKey())
                    && !StringUtils.equalsIgnoreCase(PARAM_STATUS, entry.getKey())
                    && !StringUtils.equalsIgnoreCase(PARAM_TAGS, entry.getKey())) {
                List<String> list = getList(mapper, entry.getValue(), entry.getKey());
                if (null != list && !list.isEmpty()) {
                    mc.addFilter(new Filter(entry.getKey(), SearchConditions.OP_IN, list));
                }
            } else if (StringUtils.equalsIgnoreCase(PARAM_TAGS, entry.getKey())) {
                List<String> tags = getList(mapper, entry.getValue(), entry.getKey());
                if (null != tags && !tags.isEmpty()) {
                    TagCriterion tc = new TagCriterion(tags);
                    sc.setTag(tc);
                }
            }
        }
        sc.addMetadata(mc);
        Object objFields = request.get(PARAM_FIELDS);
        List<String> fields = getList(mapper, objFields, PARAM_FIELDS);
        if (null == fields || fields.isEmpty()) {
            if (null != definition && null != definition.getMetadata()) {
                String[] arr = (String[]) definition.getMetadata().get(PARAM_FIELDS);
                if (null != arr && arr.length > 0) {
                    List<String> arrFields = Arrays.asList(arr);
                    fields = new ArrayList<String>();
                    fields.addAll(arrFields);
                }
            }
        }
        if (null == fields || fields.isEmpty())
            fields = DEFAULT_FIELDS;
        else {
            fields.add(SystemProperties.IL_UNIQUE_ID.name());
        }
        sc.setFields(fields);
        Request req = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
                GraphDACParams.search_criteria.name(), sc);
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
                List<String> list = new ArrayList<String>();
                list.add(object.toString());
                return list;
            }
        }
        return null;
    }
}
