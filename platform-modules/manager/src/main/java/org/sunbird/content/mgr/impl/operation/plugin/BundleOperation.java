package org.sunbird.content.mgr.impl.operation.plugin;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Slug;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.enums.TaxonomyErrorCodes;
import org.sunbird.common.exception.ClientException;
import org.sunbird.content.dto.ContentSearchCriteria;
import org.sunbird.content.pipeline.initializer.InitializePipeline;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Filter;
import org.sunbird.graph.dac.model.MetadataCriterion;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.SearchConditions;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.sunbird.learning.common.enums.ContentAPIParams;
import org.sunbird.learning.common.enums.ContentErrorCodes;
import org.sunbird.taxonomy.mgr.impl.BaseContentManager;
import org.sunbird.taxonomy.mgr.impl.TaxonomyManagerImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BundleOperation extends BaseContentManager {

    @SuppressWarnings("unchecked")
    public Response bundle(Request request, String version) {
        String bundleFileName = (String) request.get("file_name");
        List<String> contentIds = (List<String>) request.get("content_identifiers");
        if (contentIds.size() > 1 && StringUtils.isBlank(bundleFileName))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_INVALID_BUNDLE_CRITERIA.name(),
                    "ECAR file name should not be blank");

        Response response = searchNodes(TAXONOMY_ID, contentIds);
        Response listRes = copyResponse(response);
        if (checkError(response)) {
            return response;
        } else {
            List<Object> list = (List<Object>) response.get(ContentAPIParams.contents.name());
            List<Node> nodes = new ArrayList<Node>();
            List<Node> imageNodes = new ArrayList<Node>();
            if (null != list && !list.isEmpty()) {
                for (Object obj : list) {
                    List<Node> nodelist = (List<Node>) obj;
                    if (null != nodelist && !nodelist.isEmpty())
                        nodes.addAll(nodelist);
                }

                validateInputNodesForBundling(nodes);

                for (Node node : nodes) {
                    String contentImageId = getImageId(node.getIdentifier());
                    Response getNodeResponse = getDataNode(TAXONOMY_ID, contentImageId);
                    if (!checkError(getNodeResponse)) {
                        node = (Node) getNodeResponse.get(GraphDACParams.node.name());
                    }
                    // Fetch body from content store.
                    String body = getContentBody(node.getIdentifier());
                    node.getMetadata().put(ContentAPIParams.body.name(), body);
                    imageNodes.add(node);
                }
                if (imageNodes.size() == 1 && StringUtils.isBlank(bundleFileName))
                    bundleFileName = (String) imageNodes.get(0).getMetadata().get(ContentAPIParams.name.name()) + "_"
                            + System.currentTimeMillis() + "_" + (String) imageNodes.get(0).getIdentifier();
            }
            bundleFileName = Slug.makeSlug(bundleFileName, true);
            String fileName = bundleFileName + ".ecar";

            // Preparing the Parameter Map for 'Bundle' Pipeline;
            InitializePipeline pipeline = new InitializePipeline(tempFileLocation, "node");
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put(ContentAPIParams.nodes.name(), imageNodes);
            parameterMap.put(ContentAPIParams.bundleFileName.name(), fileName);
            parameterMap.put(ContentAPIParams.contentIdList.name(), contentIds);
            parameterMap.put(ContentAPIParams.manifestVersion.name(), DEFAULT_CONTENT_MANIFEST_VERSION);

            // Calling Content Workflow 'Bundle' Pipeline.
            listRes.getResult().putAll(pipeline.init(ContentAPIParams.bundle.name(), parameterMap).getResult());

            return listRes;
        }
    }

    /**
     * Search nodes.
     *
     * @param taxonomyId
     *            the taxonomy id
     * @param contentIds
     *            the content ids
     * @return the response
     */
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
        Response response = getResponse(requests, GraphDACParams.node_list.name(), ContentAPIParams.contents.name());
        return response;
    }

    private void validateInputNodesForBundling(List<Node> nodes) {
        if (null != nodes && !nodes.isEmpty()) {
            for (Node node : nodes) {
                // Validating for Content Image Node
                if (null != node && isContentImageObject(node))
                    throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name(),
                            "Invalid Content Identifier! | [Given Content Identifier '" + node.getIdentifier()
                                    + "' does not Exist.]");
            }
        }
    }

}
