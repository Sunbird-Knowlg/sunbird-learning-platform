package com.ilimi.graph.writer;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

public class CSVGraphWriter implements GraphWriter {
    
    private static final String NEW_LINE_SEPARATOR = "\n";
    private List<Node> nodes;
    private List<Relation> relations;

    public CSVGraphWriter(List<Node> nodes, List<Relation> relations) {
        this.nodes = nodes;
        this.relations = relations;
    }

    @Override
    public OutputStream getData() throws Exception {
        Set<String> finalHeaders = new LinkedHashSet<String>();
        finalHeaders.addAll(Arrays.asList("uniqueId", "nodeType", "objectType", "startNodeId", "endNodeId", "indexedMetadata",
                "nonIndexedMetadata", "inRelations", "outRelations"));
        Set<String> metaHeaders = new LinkedHashSet<String>();
        List<String[]> dataRows = new ArrayList<String[]>();
        if (null != nodes) {
            for (Node node : nodes) {
                if(!SystemNodeTypes.ROOT_NODE.name().equals(node.getNodeType())) {
                    String[] nodeData = getNode(node, metaHeaders);
                    dataRows.add(nodeData);
                }
            }
        }

        if (null != relations) {
            for(Relation relation : relations) {
                String[] relData = getRelation(relation, metaHeaders);
                dataRows.add(relData);
            }
        }
        List<String[]> allRows = new ArrayList<String[]>();
        finalHeaders.addAll(metaHeaders);
        allRows.add(finalHeaders.toArray(new String[finalHeaders.size()]));
        allRows.addAll(dataRows);
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
        OutputStream outputStream = new ByteArrayOutputStream();
        OutputStreamWriter osWriter = new OutputStreamWriter(outputStream);
        CSVPrinter writer = new CSVPrinter(osWriter, csvFileFormat);
        writer.printRecords(allRows);
        writer.close();
        return outputStream;
    }

    private String[] getRelation(Relation relation, Set<String> headers) {
        List<String> relData = new ArrayList<String>();
        headers.addAll(relation.getMetadata().keySet());
        relData.add("");// uniqueId
        relData.add("RELATION");// nodeType
        relData.add(relation.getRelationType());// objectType
        relData.add(relation.getStartNodeId());
        relData.add(relation.getEndNodeId());
        relData.add("");// indexedMetadata
        relData.add("");// nonIndexedMetadata
        relData.add("");// inRelations
        relData.add("");// outRelations
        for(String key : headers) {
            if(null != relation.getMetadata().get(key)) {
                relData.add(relation.getMetadata().get(key).toString());
            } else {
                relData.add("");
            }
        }
        return relData.toArray(new String[relData.size()]);
    }

    public String[] getNode(Node node, Set<String> headers) {
        List<String> nodeData = new ArrayList<String>();
        headers.addAll(getKeys(node.getMetadata()));
        nodeData.add(node.getIdentifier());
        nodeData.add(node.getNodeType());
        nodeData.add(node.getObjectType());
        nodeData.add("");// startNodeId
        nodeData.add("");// endNodeId
        if (SystemNodeTypes.DEFINITION_NODE.name().equals(node.getNodeType())) {
            nodeData.add((String) node.getMetadata().get("INDEXABLE_METADATA_KEY"));
            nodeData.add((String) node.getMetadata().get("NON_INDEXABLE_METADATA_KEY"));
            nodeData.add((String) node.getMetadata().get("IN_RELATIONS_KEY"));
            nodeData.add((String) node.getMetadata().get("OUT_RELATIONS_KEY"));
        } else {
            nodeData.add("");// indexedMetadata
            nodeData.add("");// indexedMetadata
            nodeData.add("");// inRelations
            nodeData.add("");// outRelations
            for(String key : headers) {
                if(null != node.getMetadata().get(key)) {
                    nodeData.add(node.getMetadata().get(key).toString());
                } else {
                    nodeData.add("");
                }
                
            }
        }
        return nodeData.toArray(new String[nodeData.size()]);
    }

    public Set<String> getKeys(Map<String, Object> metadata) {
        List<String> skipKeys = Arrays.asList("nodeId", "INDEXABLE_METADATA_KEY", "OUT_RELATION_OBJECTS", "OUT_RELATIONS_KEY",
                "NON_INDEXABLE_METADATA_KEY", "IN_RELATIONS_KEY", "IN_RELATION_OBJECTS", "relationsCount", "nodesCount");
        Set<String> keys = new LinkedHashSet<String>(metadata.keySet());
        for(String skipKey: skipKeys) {
            keys.remove(skipKey);
        }
        return keys;
    }
}
