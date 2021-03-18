package org.sunbird.graph.reader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.model.collection.Sequence;
import org.sunbird.graph.model.node.DataNode;
import org.sunbird.graph.model.node.MetadataDefinition;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;

public class RDFGraphReader implements GraphReader {

    private ObjectMapper mapper;
    private List<Node> definitionNodes;
    private List<Node> dataNodes;
    private Map<String, List<String>> tagMembersMap;
    private List<Relation> relations;
    private List<String> validations;
    private BaseGraphManager manager;

    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_NS = "namespace";
    public static final String PROPERTY_RELATION_START = "relationStart";
    public static final String PROPERTY_RELATION_END = "relationEnd";
    private static final String PROPERTY_RELATION_TYPE = "relationType";

    public static final String PROPERTY_NODE_TYPE = "nodeType";
    public static final String PROPERTY_OBJECT_TYPE = "objectType";

    public static final String MIME_RDFXML = "application/rdf+xml";

    protected String defaultNamespaceUri = "http://www.ilimi.in";
    protected String defaultNamespacePrefix = "ili";

    public RDFGraphReader(BaseGraphManager manager, ObjectMapper mapper, String graphId, InputStream inputStream) throws Exception {
        this.manager = manager;
        this.mapper = mapper;
        validations = new ArrayList<String>();
        tagMembersMap = new HashMap<String, List<String>>();
        RDFFormat format = Rio.getParserFormatForMIMEType(MIME_RDFXML, RDFFormat.RDFXML);
        RDFParser parser = Rio.createParser(format);
        ParserHandler handler = new ParserHandler();
        parser.setRDFHandler(handler);
        parser.parse(inputStream, defaultNamespaceUri);

        if (null != handler) {
            Map<String, List<RDFStatement>> map = handler.getMap();
            Set<String> relations = handler.getRelations();
            Map<String, Map<String, Object>> nodeProperties = new HashMap<String, Map<String, Object>>();
            Map<String, String> nodeURIMap = new HashMap<String, String>();
            createNodesAndRelations(manager, graphId, map, relations, nodeProperties, nodeURIMap);
        }
    }

    private void createNodesAndRelations(BaseGraphManager manager, String graphId, Map<String, List<RDFStatement>> map,
            Set<String> relations, Map<String, Map<String, Object>> nodeProperties, Map<String, String> nodeURIMap) {
        definitionNodes = new ArrayList<Node>();
        dataNodes = new ArrayList<Node>();
        for (Entry<String, List<RDFStatement>> entry : map.entrySet()) {
            String subject = entry.getKey();
            if (!relations.contains(subject)) {
                List<RDFStatement> list = entry.getValue();
                Map<String, Object> properties = getProperties(list, true);
                if (null != properties && properties.size() > 0) {
                    createNode(graphId, properties);
                }
            }
        }

        this.relations = new ArrayList<Relation>();
        for (String rel : relations) {
            List<RDFStatement> list = map.get(rel);
            if (null != list && list.size() > 0) {
                Map<String, Object> properties = getProperties(list, false);
                String startNodeId = removeUri((String) properties.get(PROPERTY_RELATION_START));
                String endNodeId = removeUri((String) properties.get(PROPERTY_RELATION_END));
                String relationType = (String) properties.get(PROPERTY_RELATION_TYPE);
                removeProperties(properties, PROPERTY_RELATION_START, PROPERTY_RELATION_END, PROPERTY_RELATION_TYPE);
                Relation relation = new Relation(startNodeId, relationType, endNodeId);
                relation.setMetadata(properties);
                this.relations.add(relation);
            }

        }
    }

    private void removeProperties(Map<String, Object> properties, String... keys) {
        for (String key : keys) {
            properties.remove(key);
        }
    }

    private String removeUri(String nodeIdUri) {
        return nodeIdUri.replaceAll(defaultNamespaceUri + "#", "");
    }

    @SuppressWarnings("unchecked")
    private void createNode(String graphId, Map<String, Object> props) {
        Map<String, Object> properties = null;
        properties = mapper.convertValue(props, Map.class);

        String nodeType = (String) properties.get(PROPERTY_NODE_TYPE);
        if (SystemNodeTypes.DEFINITION_NODE.name().equals(nodeType)) {
            // System.out.println("indexedMetadata:"+properties.get("indexedMetadata"));
            // List<Map<String, Object>> indexedMetaMapList = (List<Map<String,
            // Object>>) properties.get("indexedMetadata");
            // //mapper.convertValue(properties.get("indexedMetadata").toString().replaceAll("^\"|\"$",
            // ""), List.class);
            // List<MetadataDefinition> indexedMetadata =
            // getMetadataDefinitions(indexedMetaMapList);
            //
            // List<Map<String, Object>> nonIndexedMetaMapList =
            // (List<Map<String, Object>>)properties.get("nonIndexedMetadata");
            // List<MetadataDefinition> nonIndexedMetadata =
            // getMetadataDefinitions(nonIndexedMetaMapList);
            //
            // List<Map<String, Object>> inRelationMapList = (List<Map<String,
            // Object>>) properties.get("inRelations");
            // List<RelationDefinition> inRelations = new
            // ArrayList<RelationDefinition>();
            // for(Map<String, Object> relationItem: inRelationMapList) {
            // RelationDefinition relationDefinition =
            // mapper.convertValue(relationItem, RelationDefinition.class);
            // if(relationDefinition != null)
            // inRelations.add(relationDefinition);
            // }
            //
            // List<Map<String, Object>> outRelationMapList = (List<Map<String,
            // Object>>) properties.get("outRelations");
            // List<RelationDefinition> outRelations = new
            // ArrayList<RelationDefinition>();
            // for(Map<String, Object> relationItem: outRelationMapList) {
            // RelationDefinition relationDefinition =
            // mapper.convertValue(relationItem, RelationDefinition.class);
            // if(relationDefinition != null)
            // outRelations.add(relationDefinition);
            // }
            // DefinitionNode node = new DefinitionNode(manager, graphId,
            // (String)properties.get(PROPERTY_OBJECT_TYPE), indexedMetadata,
            // nonIndexedMetadata, inRelations, outRelations);
            // definitionNodes.add(node.toNode());
        } else if (SystemNodeTypes.DATA_NODE.name().equals(nodeType)) {
            String objectType = (String) properties.get(PROPERTY_OBJECT_TYPE);
            String uniqueId = (String) properties.get(PROPERTY_ID);
            removeProperties(properties, PROPERTY_ID, PROPERTY_NODE_TYPE, PROPERTY_OBJECT_TYPE);
            DataNode node = new DataNode(manager, graphId, uniqueId, objectType, properties);
            dataNodes.add(node.toNode());
        } else if (SystemNodeTypes.SEQUENCE.name().equals(nodeType)) {
            Sequence sequence = new Sequence(manager, graphId, (String) properties.get(PROPERTY_ID));
            dataNodes.add(sequence.toNode());
        }
    }

    private List<MetadataDefinition> getMetadataDefinitions(List<Map<String, Object>> metaMapList) {
        List<MetadataDefinition> metaDefinitions = new ArrayList<MetadataDefinition>();
        for (Map<String, Object> metaItem : metaMapList) {
            MetadataDefinition metaDefinition = mapper.convertValue(metaItem, MetadataDefinition.class);
            if (metaDefinition != null)
                metaDefinitions.add(metaDefinition);
        }
        return metaDefinitions;
    }

    private Map<String, Object> getProperties(List<RDFStatement> list, boolean skipURIs) {
        if (null != list && list.size() > 0) {
            Map<String, Object> properties = new HashMap<String, Object>();
            for (RDFStatement st : list) {
                Value object = st.getObject();
                URI predicate = st.getPredicate();
                if (object instanceof Literal) {
                    properties.put(predicate.getLocalName(), object.toString().replaceAll("^\"|\"$", ""));
                } else if (object instanceof URI) {
                    properties.put(predicate.getLocalName(), object.toString().replaceAll("^\"|\"$", ""));
                }
            }
            return properties;
        }
        return null;
    }

    /**
     * @return the definitionNodes
     */
    public List<Node> getDefinitionNodes() {
        return definitionNodes;
    }

    /**
     * @param definitionNodes
     *            the definitionNodes to set
     */
    public void setDefinitionNodes(List<Node> definitionNodes) {
        this.definitionNodes = definitionNodes;
    }

    /**
     * @return the dataNodes
     */
    public List<Node> getDataNodes() {
        return dataNodes;
    }

    /**
     * @param dataNodes
     *            the dataNodes to set
     */
    public void setDataNodes(List<Node> dataNodes) {
        this.dataNodes = dataNodes;
    }

    /**
     * @return the relations
     */
    public List<Relation> getRelations() {
        return relations;
    }

    /**
     * @param relations
     *            the relations to set
     */
    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }

    /**
     * Callback class to receive the RDF parsing events - like the SAX XML
     * parser.
     */
    public static class ParserHandler extends RDFHandlerBase {

        private Map<String, List<RDFStatement>> map = new HashMap<String, List<RDFStatement>>();
        private Set<String> relations = new HashSet<String>();

        @Override
        public void handleStatement(Statement st) throws RDFHandlerException {

            Resource subject = st.getSubject();
            URI predicate = st.getPredicate();
            Value object = st.getObject();

            String propName = predicate.getLocalName();
            if (StringUtils.equalsIgnoreCase("relationStart", propName)) {
                relations.add(subject.toString());
            }
            List<RDFStatement> list = map.get(subject.toString());
            if (null == list) {
                list = new ArrayList<RDFStatement>();
                map.put(subject.toString(), list);
            }
            list.add(new RDFStatement(predicate, object));
        }

        public Map<String, List<RDFStatement>> getMap() {
            return this.map;
        }

        public Set<String> getRelations() {
            return this.relations;
        }
    }

    private static class RDFStatement {

        private URI predicate;
        private Value object;

        public RDFStatement(URI predicate, Value object) {
            this.predicate = predicate;
            this.object = object;
        }

        public URI getPredicate() {
            return this.predicate;
        }

        public Value getObject() {
            return this.object;
        }
    }

    @Override
    public List<String> getValidations() {
        return validations;
    }

    /**
     * @return the tagMembersMap
     */
    public Map<String, List<String>> getTagMembersMap() {
        return tagMembersMap;
    }

    /**
     * @param tagMembersMap
     *            the tagMembersMap to set
     */
    public void setTagMembersMap(Map<String, List<String>> tagMembersMap) {
        this.tagMembersMap = tagMembersMap;
    }
}
